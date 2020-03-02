package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;

public class RosettaJavaPackages {

	public static final Package DEFAULT_NAMESPACE = new Package(RosettaScopeProvider.LIB_NAMESPACE);
	private static final Package BLUEPRINT_NAMESPACE = new Package("com.regnosys.rosetta");
	private RootPackage root;

	public RosettaJavaPackages(RosettaModel model) {
		this(new RootPackage(model));
	}

	protected RosettaJavaPackages() {
	}
	
	protected RosettaJavaPackages(RootPackage root) {
		this.root = root;
	}
	public Package defaultNamespace() {
		return DEFAULT_NAMESPACE;
	}

	public RootPackage model() {
		return this.root;
	}

	public Package blueprintLib() {
		return BLUEPRINT_NAMESPACE.child("blueprints");
	}

	public Package defaultLib() {
		return defaultNamespace().child("lib");
	}

	public Package defaultLibAnnotations() {
		return defaultLib().child("annotations");
	}

	public Package defaultLibFunctions() {
		return defaultLib().child("functions");
	}

	public Package defaultLibRecords() {
		return defaultLib().child("records");
	}

	public Package defaultLibValidation() {
		return defaultLib().child("validation");
	}

	public Package defaultLibQualify() {
		return defaultLib().child("qualify");
	}

	public Package defaultLibMeta() {
		return defaultLib().child("meta");
	}
	
	public Package basicMetafields() {
		return defaultNamespace().child("metafields");
	}

	public static class Package {
		private Package parent;
		private String name;
		private String qName;

		Package(String name) {
			this.name = name;
		}

		public Package(Package parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		public Package child(String child) {
			return new Package(this, child);
		}

		public String name() {
			if (this.qName == null) {
				if (this.parent != null) {
					this.qName = this.parent.name() + "." + name;
				} else {
					this.qName = name;
				}
			}
			return this.qName;
		}

		public String getSimpleName() {
			return this.name;
		}

		public String directoryName() {
			return name().replace('.', '/');
		}

		@Override
		public String toString() {
			return name();
		}
	}

	public static class RootPackage extends Package {

		public RootPackage(RosettaModel model) {
			super(model.getName());
		}

		public RootPackage(String namespace) {
			super(namespace);
		}

		public Package metaField() {
			return new Package(this, "metafields");
		}

		public Package meta() {
			return new Package(this, "meta");
		}

		public Package functions() {
			return new Package(this, "functions");
		}

		public Package typeValidation() {
			return new Package(this, "validation");
		}

		public Package choiceRule() {
			return typeValidation().child("choicerule");
		}

		public Package dataRule() {
			return typeValidation().child("datarule");
		}

		public Package existsValidation() {
			return typeValidation().child("exists");
		}

		public Package blueprint() {
			return new Package(this, "blueprint");
		}

		public Package qualifyEvent() {
			return new Package(this, "qualify.event");
		}

		public Package qualifyProduct() {
			return new Package(this, "qualify.product");
		}
	}
}
