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

package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.rosetta.util.DottedPath;

public class RosettaJavaPackages {

	public static final DottedPath DEFAULT_NAMESPACE = DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE);

	public DottedPath defaultNamespace() {
		return DEFAULT_NAMESPACE;
	}

	public DottedPath defaultLib() {
		return defaultNamespace().child("lib");
	}

	public DottedPath defaultLibAnnotations() {
		return defaultLib().child("annotations");
	}

	public DottedPath defaultLibFunctions() {
		return defaultLib().child("functions");
	}

	public DottedPath defaultLibRecords() {
		return defaultLib().child("records");
	}

	public DottedPath defaultLibValidation() {
		return defaultLib().child("validation");
	}

	public DottedPath defaultLibQualify() {
		return defaultLib().child("qualify");
	}

	public DottedPath defaultLibMeta() {
		return defaultLib().child("meta");
	}
	
	public DottedPath basicMetafields() {
		return defaultNamespace().child("metafields");
	}

	public static class RootPackage extends DottedPath {

		public RootPackage(RosettaModel model) {
			this(model.getName());
		}

		public RootPackage(String namespace) {
			super(namespace.split("\\."));
		}

		public DottedPath metaField() {
			return child("metafields");
		}

		public DottedPath meta() {
			return child("meta");
		}

		public DottedPath functions() {
			return child("functions");
		}

		public DottedPath typeValidation() {
			return child("validation");
		}

		public DottedPath condition() {
			return typeValidation().child("datarule");
		}

		public DottedPath existsValidation() {
			return typeValidation().child("exists");
		}
		
		public DottedPath reports() {
			return child("reports");
		}

		public DottedPath qualifyEvent() {
			return child("qualify").child("event");
		}

		public DottedPath qualifyProduct() {
			return child("qualify").child("product");
		}
	}
}
