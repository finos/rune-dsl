package com.rosetta.model.lib.process;

/**
 * @author TomForwood
 * Class representing errors that occur while post processing
 */
public class ProcessMessages {
	
	enum Severity {
		DEBUG, INFO, WARNING, SEVERE
	}
	
	private final Severity severity;
	private final String processor;
	private final String message;
	
	public ProcessMessages(Severity severity, String processor, String message) {
		super();
		this.severity = severity;
		this.processor = processor;
		this.message = message;
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public String getProcessor() {
		return processor;
	}
	
	public String getMessage() {
		return message;
	}
}
