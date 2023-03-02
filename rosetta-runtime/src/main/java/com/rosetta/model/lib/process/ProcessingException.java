package com.rosetta.model.lib.process;

import com.rosetta.model.lib.path.RosettaPath;

/**
 * @author TomForwood
 * An exception wrapper to be thrown while post processing during ingestion
 */
public class ProcessingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String objectName;
	private final String processorName;
	private final RosettaPath path;

	public ProcessingException(String message, String objectName, String processorName, RosettaPath path) {
		super(message);
		this.objectName = objectName;
		this.processorName = processorName;
		this.path = path;
	}

	public ProcessingException(String message, String objectName, String processorName, RosettaPath path, Throwable cause) {
		super(message, cause);
		this.objectName = objectName;
		this.processorName = processorName;
		this.path = path;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getProcessorName() {
		return processorName;
	}

	public RosettaPath getPath() {
		return path;
	}
}
