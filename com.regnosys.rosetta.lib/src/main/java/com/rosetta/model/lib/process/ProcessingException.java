package com.rosetta.model.lib.process;

/**
 * @author TomForwood
 * An exception wrapper to be thrown while post processing during ingestion
 */
public class ProcessingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessingException(String message) {
		super(message);
	}

	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

}
