package com.regnosys.rosetta.tests.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.mdkt.compiler.CompilationException;
import org.mdkt.compiler.CompiledCode;
import org.mdkt.compiler.DynamicClassLoader;
import org.mdkt.compiler.SourceCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryJavacCompiler {
	private static Logger LOGGER = LoggerFactory.getLogger(InMemoryJavacCompiler.class);
	
	private JavaCompiler javac;
	private DynamicClassLoader classLoader;
	private Iterable<String> options;

	private Map<String, SourceCode> sourceCodes = new HashMap<String, SourceCode>();

	public static InMemoryJavacCompiler newInstance() {
		return new InMemoryJavacCompiler();
	}

	private InMemoryJavacCompiler() {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
	}

	public InMemoryJavacCompiler useParentClassLoader(ClassLoader parent) {
		this.classLoader = new DynamicClassLoader(parent);
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
	 * @throws Exception
	 */
	public Map<String, Class<?>> compileAll() throws Exception {
		if (sourceCodes.size() == 0) {
			return Collections.emptyMap();
		}
		Collection<SourceCode> compilationUnits = sourceCodes.values();
		CompiledCode[] code;

		code = new CompiledCode[compilationUnits.size()];
		Iterator<SourceCode> iter = compilationUnits.iterator();
		for (int i = 0; i < code.length; i++) {
			code[i] = new CompiledCode(iter.next().getClassName());
		}
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(
				javac.getStandardFileManager(null, null, StandardCharsets.UTF_8), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null,
				compilationUnits);
		boolean result = task.call();
		Map<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsPerClass = 
			collector.getDiagnostics().stream()
				.collect(Collectors.groupingBy(d -> d.getSource() == null ? "unknown class" : d.getSource().getName().substring(1).replace(".java", "").replace('/', '.')));
		diagnosticsPerClass.values().forEach(ds -> ds.sort(Comparator.comparing(Diagnostic::getKind)));
		if (!result || diagnosticsPerClass.size() > 0) {
			boolean hasErrors = diagnosticsPerClass.values().stream().anyMatch(ds -> ds.stream().anyMatch(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.OTHER));
			StringBuffer exceptionMsg = new StringBuffer();
			boolean hasWarnings = false;
			for (Map.Entry<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsOfClass : diagnosticsPerClass.entrySet()) {
				String className = diagnosticsOfClass.getKey();
				List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsOfClass.getValue();
				if (hasErrors && !diagnostics.stream().anyMatch(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.OTHER)) {
					continue;
				}
				exceptionMsg.append("\n")
					.append("Class ").append(className);
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
					exceptionMsg.append("\n")
						.append(++number).append(". ").append(d);
				}
			}
			if (hasErrors) {
				throw new CompilationException("Java code compiled with errors:" + exceptionMsg.toString());
			} else if (hasWarnings) {
				// TODO: turn this back on and fix warnings.
				// LOGGER.debug("There were warnings during compilation:" + exceptionMsg.toString());
			}
		}

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		for (String className : sourceCodes.keySet()) {
			classes.put(className, classLoader.loadClass(className));
		}
		return classes;
	}

	/**
	 * Compile single source
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 */
	public Class<?> compile(String className, String sourceCode) throws Exception {
		return addSource(className, sourceCode).compileAll().get(className);
	}

	/**
	 * Add source code to the compiler
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 * @see {@link #compileAll()}
	 */
	public InMemoryJavacCompiler addSource(String className, String sourceCode) throws Exception {
		String normalizedSource = sourceCode.replace("\r\n", "\n").replace("\t", "    ");
		sourceCodes.put(className, new SourceCode(className, normalizedSource));
		return this;
	}

	private static class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

		private List<CompiledCode> compiledCode = new ArrayList<CompiledCode>();
		private DynamicClassLoader cl;

		/**
		 * Creates a new instance of ForwardingJavaFileManager.
		 *
		 * @param fileManager delegate to this file manager
		 * @param cl
		 */
		protected ExtendedStandardJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl) {
			super(fileManager);
			this.cl = cl;
		}

		@Override
		public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
				JavaFileObject.Kind kind, FileObject sibling) throws IOException {

			try {
				CompiledCode innerClass = new CompiledCode(className);
				compiledCode.add(innerClass);
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
	}
}
