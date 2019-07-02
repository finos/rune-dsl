package com.regnosys.rosetta.generator.java;

import java.util.Optional;

public class RosettaJavaPackages {

	private static final String LIB_NAMESPACE = "com.rosetta.model.lib";
	private static final String BLUEPRINT_NAMESPACE = "com.regnosys.rosetta";
	private static final String DEFAULT_NAMESPACE = "com.rosetta.model";
	
	private final String namespace;
	
	public RosettaJavaPackages(String namespace) {
		this.namespace = Optional.ofNullable(namespace).orElse(DEFAULT_NAMESPACE);
	}
	
	public Package lib() {
		return new Package(LIB_NAMESPACE);
	}
	
	public Package validation() {
		return new Package(LIB_NAMESPACE, "validation");
	}

	public Package qualify() {
		return new Package(LIB_NAMESPACE, "qualify");
	}
	
	public Package metaLib() {
		return new Package(LIB_NAMESPACE, "meta");
	}
	
	public Package metaField() {
		return new Package(namespace, "metafields");
	}
	
	public Package annotations() {
		return new Package(LIB_NAMESPACE, "annotations");
	}
	
	public Package libFunctions() {
		return new Package(LIB_NAMESPACE, "functions");
	}
	
	public Package libRecords() {
		return new Package(LIB_NAMESPACE, "records");
	}
	
	public Package libBlueprint() {
		return new Package(BLUEPRINT_NAMESPACE, "blueprints");
	}
	
	public Package model() {
		return new Package(namespace);
	}
	
	public Package meta() {
		return new Package(namespace, "meta");
	}

	public Package calculation() {
		return new Package(namespace, "calculation");
	}
	
	public Package functions() {
		return new Package(namespace, "functions");
	}

	public Package binding() {
		return new Package(namespace, "binding");
	}

	public Package bindingUtil() {
		return new Package(namespace, "binding.util");
	}

	public Package classValidation() {
		return new Package(namespace, "validation");
	}
	
	public Package choiceRule() {
		return new Package(namespace, "validation.choicerule");
	}

	public Package dataRule() {
		return new Package(namespace, "validation.datarule");
	}
	
	public Package existsValidation() {
		return new Package(namespace, "validation.exists");
	}
	
	public Package blueprint() {
		return new Package(namespace, "blueprint");
	}

	public Package qualifyEvent() {
		return new Package(namespace, "qualify.event");
	}
	
	public Package qualifyProduct() {
		return new Package(namespace, "qualify.product");
	}
	
	public Package ingestion() {
		return new Package(namespace, "ingestion");
	}
	
	public Package ingestionChild() {
		return new Package(namespace, "ingestion");
	}
	
	public Package processor() {
		return new Package(namespace, "processor");
	}
	
	public static class Package {
		private String nameSpace;
		private String name;

		Package(String nameSpace) {
			this.nameSpace = nameSpace;
			this.name = null;
		}
		
		Package(String nameSpace, String name) {
			this.nameSpace = nameSpace;
			this.name = name;
		}
		
		public Package child(String child) {
			return new Package(this.packageName(), child);
		}

		public String packageName() {
			return nameSpace + Optional.ofNullable(name).map(n -> "." + n).orElse("");
		}

		public String directoryName() {
			return packageName().replace('.', '/');
		}
	}
}
