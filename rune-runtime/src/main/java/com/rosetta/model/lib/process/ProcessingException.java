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
