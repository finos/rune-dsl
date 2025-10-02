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

package com.regnosys.rosetta.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that will handle unexpected exceptions (e.g., a StackOverflowException)
 * during parsing in the following way:
 * - Log the exception.
 * - Raise a syntax validation diagnostic indicating an unexpected error occurred.
 * - Do not rethrow the underlying exception. Processing of the resource can continue
 *   as if a syntax error occurred. The resource will not contain any `EObject`, and
 *   `parseResult` will be `null`.
 */
public class RosettaResource extends DerivedStateAwareResource {
	private static Logger LOGGER = LoggerFactory.getLogger(RosettaResource.class);
	
	public static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected parse error occured.";
	
	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		try {
			super.doLoad(inputStream, options);
		} catch(Exception ex) {
			if (this.getParseResult() == null) {
				LOGGER.error("Unexpected error during parsing of " + getURI() + ".", ex);
				clearCache();
				clearErrorsAndWarnings();
				getErrors().add(new Diagnostic() {
					@Override
					public String getMessage() {
						return UNEXPECTED_ERROR_MESSAGE;
					}

					@Override
					public String getLocation() {
						return RosettaResource.this.getURI().toString();
					}

					@Override
					public int getLine() {
						return 0;
					}

					@Override
					public int getColumn() {
						return 0;
					}
				});
			} else {
				throw ex;
			}
		}
	}
}
