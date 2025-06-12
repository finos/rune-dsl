package com.regnosys.rosetta.tests.compiler;

import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompilationException extends RuntimeException {
	private static final long serialVersionUID = 5272588827551900536L;
	
	private final Map<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsPerClass;

	public CompilationException(String msg, Map<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsPerClass) {
		super(msg);
		this.diagnosticsPerClass = diagnosticsPerClass;
	}

	public Map<String, List<Diagnostic<? extends JavaFileObject>>> getDiagnosticsPerClass() {
		return diagnosticsPerClass;
	}
}
