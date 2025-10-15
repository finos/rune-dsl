/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.tests.compiler;

import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Based on org.mdkt.compiler.InMemoryJavaCompiler.
 * Enhanced with http://atamur.blogspot.com/2009/10/using-built-in-javacompiler-with-custom.html
 * to properly support a parent classloader.
 */
public class InMemoryJavacCompiler {
	private static Logger LOGGER = LoggerFactory.getLogger(InMemoryJavacCompiler.class);

	private JavaCompiler javac;
	private DynamicClassLoaderWithCompiledResources classLoader;
	private Iterable<String> options;

	private Map<String, SourceCode> sourceCodes = new HashMap<String, SourceCode>();

	public static InMemoryJavacCompiler newInstance() {
		return new InMemoryJavacCompiler();
	}

	private InMemoryJavacCompiler() {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classLoader = new DynamicClassLoaderWithCompiledResources(ClassLoader.getSystemClassLoader());
	}

	public InMemoryJavacCompiler useParentClassLoader(ClassLoader parent) {
		this.classLoader = new DynamicClassLoaderWithCompiledResources(parent);
		return this;
	}

	/**
	 * @return the class loader used internally by the compiler
	 */
	public ClassLoader getClassloader() {
		return classLoader;
	}

	/**
	 * Options used by the compiler, e.g. '-Xlint:unchecked'.
	 *
	 * @param options
	 * @return
	 */
	public InMemoryJavacCompiler useOptions(String... options) {
		this.options = Arrays.asList(options);
		return this;
	}

	/**
	 * Compile all sources
	 *
	 * @return Map containing instances of all compiled classes
	 */
	public Map<String, Class<?>> compileAll() {
		if (sourceCodes.size() == 0) {
			return Collections.emptyMap();
		}
		Collection<SourceCode> compilationUnits = sourceCodes.values();

		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		JavaFileManager fileManager = new ExtendedStandardJavaFileManager(
				javac.getStandardFileManager(null, null, StandardCharsets.UTF_8), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null,
				compilationUnits);
		boolean result = task.call();
		Map<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsPerClass = collector.getDiagnostics()
				.stream().collect(Collectors.groupingBy(d -> d.getSource() == null ? "unknown class"
						: d.getSource().getName().substring(1).replace(".java", "").replace('/', '.')));
		diagnosticsPerClass.values().forEach(ds -> ds.sort(Comparator.comparing(Diagnostic::getKind)));
		if (!result || !diagnosticsPerClass.isEmpty()) {
			boolean hasErrors = diagnosticsPerClass.values().stream()
					.anyMatch(ds -> ds.stream().anyMatch(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.OTHER));
			StringBuilder exceptionMsg = new StringBuilder();
			boolean hasWarnings = false;
			for (Map.Entry<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsOfClass : diagnosticsPerClass
					.entrySet()) {
				String className = diagnosticsOfClass.getKey();
				List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsOfClass.getValue();
				if (hasErrors && diagnostics.stream()
						.noneMatch(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.OTHER)) {
					continue;
				}
				exceptionMsg.append("\n").append("Class ").append(className);
				int number = 0;
				for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
					switch (d.getKind()) {
					case NOTE:
					case MANDATORY_WARNING:
					case WARNING:
						hasWarnings = true;
						if (hasErrors) {
							continue;
						}
						break;
					case OTHER:
					case ERROR:
					default:
						break;
					}
					exceptionMsg.append("\n").append(++number).append(". ").append(d);
				}
			}
			if (hasErrors) {
				throw new CompilationException("Java code compiled with errors:" + exceptionMsg.toString(), diagnosticsPerClass);
			} else if (hasWarnings) {
				// TODO: turn this back on and fix warnings.
				// LOGGER.debug("There were warnings during compilation:" +
				// exceptionMsg.toString());
			}
		}

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		try {
			for (String className : sourceCodes.keySet()) {
				classes.put(className, classLoader.loadClass(className));
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return classes;
	}

	/**
	 * Compile single source
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 */
	public Class<?> compile(String className, String sourceCode) {
		return addSource(className, sourceCode).compileAll().get(className);
	}

	/**
	 * Add source code to the compiler
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @see {@link #compileAll()}
	 */
	public InMemoryJavacCompiler addSource(String className, String sourceCode) {
		String normalizedSource = sourceCode.replace("\r\n", "\n").replace("\t", "    ");
		sourceCodes.put(className, new SourceCode(className, normalizedSource));
		return this;
	}

	private static class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

		private DynamicClassLoaderWithCompiledResources cl;

		/**
		 * Creates a new instance of ForwardingJavaFileManager.
		 *
		 * @param fileManager delegate to this file manager
		 * @param cl
		 */
		protected ExtendedStandardJavaFileManager(JavaFileManager fileManager, DynamicClassLoaderWithCompiledResources cl) {
			super(fileManager);
			this.cl = cl;
		}

		@Override
		public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
				JavaFileObject.Kind kind, FileObject sibling) throws IOException {

			try {
				CompiledCode innerClass = new CompiledCode(className);
				cl.addCode(innerClass);
				return innerClass;
			} catch (Exception e) {
				throw new RuntimeException("Error while creating in-memory output file for " + className, e);
			}
		}

		@Override
		public ClassLoader getClassLoader(JavaFileManager.Location location) {
			return cl;
		}

		@Override
		public String inferBinaryName(Location location, JavaFileObject file) {
			if (file instanceof JavaFileObjectWithName) {
				return ((JavaFileObjectWithName) file).getClassName();
			} else if (file instanceof CompiledCode) {
				return ((CompiledCode) file).getClassName();
			} else { // if it's not CustomJavaFileObject, then it's coming from standard file manager
						// - let it handle the file
				return super.inferBinaryName(location, file);
			}
		}

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
				boolean recurse) throws IOException {
			return find(packageName);
		}

		private List<JavaFileObject> find(String packageName) throws IOException {
			String javaPackageName = packageName.replaceAll("\\.", "/");

			List<JavaFileObject> result = new ArrayList<JavaFileObject>();

			Enumeration<URL> urlEnumeration = cl.getResources(javaPackageName);
			while (urlEnumeration.hasMoreElements()) { // one URL for each jar on the classpath that has the given
														// package
				URL packageFolderURL = urlEnumeration.nextElement();
				result.addAll(listUnder(packageName, packageFolderURL));
			}
			result.addAll(cl.getCompiledCode(packageName));

			return result;
		}

		private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) {
			File directory = new File(packageFolderURL.getFile());
			if (directory.isDirectory()) { // browse local .class files - useful for local execution
				return processDir(packageName, directory);
			} else { // browse a jar file
				return processJar(packageFolderURL);
			} // maybe there can be something else for more involved class loaders
		}

		private List<JavaFileObject> processJar(URL packageFolderURL) {
			List<JavaFileObject> result = new ArrayList<JavaFileObject>();
			try {
				String jarUri = packageFolderURL.toExternalForm().split("!")[0];

				JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
				String rootEntryName = jarConn.getEntryName();
				int rootEnd = rootEntryName.length() + 1;

				Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
				while (entryEnum.hasMoreElements()) {
					JarEntry jarEntry = entryEnum.nextElement();
					String name = jarEntry.getName();
					if (name.startsWith(rootEntryName) && name.indexOf('/', rootEnd) == -1 && name.endsWith(".class")) {
						URI uri = URI.create(jarUri + "!/" + name);
						String binaryName = name.replaceAll("/", ".");
						binaryName = binaryName.replaceAll(".class$", "");

						result.add(new JavaFileObjectWithName(binaryName, uri));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", e);
			}
			return result;
		}

		private List<JavaFileObject> processDir(String packageName, File directory) {
			List<JavaFileObject> result = new ArrayList<JavaFileObject>();

			File[] childFiles = directory.listFiles();
			for (File childFile : childFiles) {
				if (childFile.isFile()) {
					// We only want the .class files.
					if (childFile.getName().endsWith(".class")) {
						String binaryName = packageName + "." + childFile.getName();
						binaryName = binaryName.replaceAll(".class$", "");

						result.add(new JavaFileObjectWithName(binaryName, childFile.toURI()));
					}
				}
			}

			return result;
		}
	}

	private static class SourceCode extends SimpleJavaFileObject {
		private String contents = null;
		private String className;

		public SourceCode(String className, String contents) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.contents = contents;
			this.className = className;
		}

		public String getClassName() {
			return className;
		}

		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return contents;
		}
	}

	// Slight modification of `SimpleJavaFileObject` that also supports classes in jar files.
	private static class JavaFileObjectWithName implements JavaFileObject {
		private final URI uri;
		private final String className;

		public JavaFileObjectWithName(String className, URI uri) {
			this.uri = uri;
			this.className = className;
		}
		
		public String getClassName() {
			return className;
		}

		public URI toUri() {
	        return uri;
	    }

	    public String getName() {
	    	// for FS based URI the path is
			// not null, for JAR URI the
			// scheme specific part is not
			// null
	        return toUri().getPath() == null ? toUri().getSchemeSpecificPart() : toUri().getPath();
	    }

	    /**
	     * This implementation always throws {@linkplain
	     * UnsupportedOperationException}.  Subclasses can change this
	     * behavior as long as the contract of {@link FileObject} is
	     * obeyed.
	     */
	    public InputStream openInputStream() throws IOException {
	        return toUri().toURL().openStream();
	    }

	    /**
	     * This implementation always throws {@linkplain
	     * UnsupportedOperationException}.  Subclasses can change this
	     * behavior as long as the contract of {@link FileObject} is
	     * obeyed.
	     */
	    public OutputStream openOutputStream() throws IOException {
	        throw new UnsupportedOperationException();
	    }

	    /**
	     * Wraps the result of {@linkplain #getCharContent} in a Reader.
	     * Subclasses can change this behavior as long as the contract of
	     * {@link FileObject} is obeyed.
	     *
	     * @param  ignoreEncodingErrors {@inheritDoc}
	     * @return a Reader wrapping the result of getCharContent
	     * @throws IllegalStateException {@inheritDoc}
	     * @throws UnsupportedOperationException {@inheritDoc}
	     * @throws IOException {@inheritDoc}
	     */
	    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
	        CharSequence charContent = getCharContent(ignoreEncodingErrors);
	        if (charContent == null)
	            throw new UnsupportedOperationException();
	        if (charContent instanceof CharBuffer) {
	            CharBuffer buffer = (CharBuffer)charContent;
	            if (buffer.hasArray())
	                return new CharArrayReader(buffer.array());
	        }
	        return new StringReader(charContent.toString());
	    }

	    /**
	     * This implementation always throws {@linkplain
	     * UnsupportedOperationException}.  Subclasses can change this
	     * behavior as long as the contract of {@link FileObject} is
	     * obeyed.
	     */
	    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
	        throw new UnsupportedOperationException();
	    }

	    /**
	     * Wraps the result of openOutputStream in a Writer.  Subclasses
	     * can change this behavior as long as the contract of {@link
	     * FileObject} is obeyed.
	     *
	     * @return a Writer wrapping the result of openOutputStream
	     * @throws IllegalStateException {@inheritDoc}
	     * @throws UnsupportedOperationException {@inheritDoc}
	     * @throws IOException {@inheritDoc}
	     */
	    public Writer openWriter() throws IOException {
	        return new OutputStreamWriter(openOutputStream());
	    }

	    /**
	     * This implementation returns {@code 0L}.  Subclasses can change
	     * this behavior as long as the contract of {@link FileObject} is
	     * obeyed.
	     *
	     * @return {@code 0L}
	     */
	    public long getLastModified() {
	        return 0L;
	    }

	    /**
	     * This implementation does nothing.  Subclasses can change this
	     * behavior as long as the contract of {@link FileObject} is
	     * obeyed.
	     *
	     * @return {@code false}
	     */
	    public boolean delete() {
	        return false;
	    }

	    /**
	     * @return {@code this.kind}
	     */
	    public Kind getKind() {
	        return Kind.CLASS;
	    }

	    /**
	     * This implementation compares the path of its URI to the given
	     * simple name.  This method returns true if the given kind is
	     * equal to the kind of this object, and if the path is equal to
	     * {@code simpleName + kind.extension} or if it ends with {@code
	     * "/" + simpleName + kind.extension}.
	     *
	     * <p>This method calls {@link #getKind} and {@link #toUri} and
	     * does not access the fields {@link #uri} and {@link #kind}
	     * directly.
	     *
	     * <p>Subclasses can change this behavior as long as the contract
	     * of {@link JavaFileObject} is obeyed.
	     */
	    public boolean isNameCompatible(String simpleName, Kind kind) {
	        String baseName = simpleName + kind.extension;
	        return kind.equals(getKind())
	            && (baseName.equals(toUri().getPath())
	                || toUri().getPath().endsWith("/" + baseName));
	    }

	    /**
	     * This implementation returns {@code null}.  Subclasses can
	     * change this behavior as long as the contract of
	     * {@link JavaFileObject} is obeyed.
	     */
	    public NestingKind getNestingKind() { return null; }

	    /**
	     * This implementation returns {@code null}.  Subclasses can
	     * change this behavior as long as the contract of
	     * {@link JavaFileObject} is obeyed.
	     */
	    public Modifier getAccessLevel()  { return null; }

	    @Override
	    public String toString() {
	        return getClass().getName() + "[" + toUri() + "]";
	    }
	}
}
