package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.utils.DottedPath;

public class RosettaJavaPackages {

	public static final DottedPath DEFAULT_NAMESPACE = DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE);
	private static final DottedPath BLUEPRINT_NAMESPACE = DottedPath.splitOnDots("com.regnosys.rosetta");

	public DottedPath defaultNamespace() {
		return DEFAULT_NAMESPACE;
	}

	public DottedPath blueprintLib() {
		return BLUEPRINT_NAMESPACE.child("blueprints");
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

		public DottedPath dataRule() {
			return typeValidation().child("datarule");
		}

		public DottedPath existsValidation() {
			return typeValidation().child("exists");
		}

		public DottedPath blueprint() {
			return child("blueprint");
		}

		public DottedPath qualifyEvent() {
			return child("qualify").child("event");
		}

		public DottedPath qualifyProduct() {
			return child("qualify").child("product");
		}
	}
}
