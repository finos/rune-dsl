package com.regnosys.rosetta.tests.util;

import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class InMemoryJavacCompiler {
	private JavaCompiler javac;
	private ClassLoader classLoader;
	
	public InMemoryJavacCompiler(ClassLoader parent) {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classLoader = parent;
	}

	public Map<String, Class<?>> compile(Map<String, String> sources, List<String> options) {
		Collection<SourceCode> compilationUnits = sourceCodes.values();
		CompiledCode[] code;

		code = new CompiledCode[compilationUnits.size()];
		Iterator<SourceCode> iter = compilationUnits.iterator();
		for (int i = 0; i < code.length; i++) {
			code[i] = new CompiledCode(iter.next().getClassName());
		}
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);
		boolean result = task.call();
		if (!result || collector.getDiagnostics().size() > 0) {
			StringBuffer exceptionMsg = new StringBuffer();
			exceptionMsg.append("Unable to compile the source");
			boolean hasWarnings = false;
			boolean hasErrors = false;
			for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarnings = true;
					break;
				case OTHER:
				case ERROR:
				default:
					hasErrors = true;
					break;
				}
				exceptionMsg.append("\n").append("[kind=").append(d.getKind());
				exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
				exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
			}
			if (hasWarnings && !ignoreWarnings || hasErrors) {
				throw new CompilationException(exceptionMsg.toString());
			}
		}

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		for (String className : sourceCodes.keySet()) {
			classes.put(className, classLoader.loadClass(className));
		}
		return classes;
	}

}
