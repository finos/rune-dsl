package com.regnosys.rosetta.generator.java;

import java.util.Optional;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.generator.java.util.JavaType;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRootElement;

public class RosettaJavaPackages {

	private static final Package DEFAULT_NAMESPACE = new Package("com.rosetta.model");
	private static final Package BLUEPRINT_NAMESPACE = new Package("com.regnosys.rosetta");
	private RootPackage root;

	public RosettaJavaPackages(RosettaModel model) {
		this.root = new RootPackage(model);
	}

	public RosettaJavaPackages() {
	}

	private Package defaultNamespace() {
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

	public static class Package {
		private Package parent;
		private String name;

		Package(String name) {
			this.name = name;
		}

		private Package(Package parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		public Package child(String child) {
			return new Package(this, child);
		}

		public JavaType javaType(RosettaRootElement ctx, String typeName) {
			String subPackage = "";
			if(!(this instanceof RootPackage))
			{
				subPackage = "." + this.name ;
			}
			if(ctx.getModel() == null) {
				// Faked attributes
				return JavaType.create(name() + '.' + typeName);
			}
			return JavaType.create(ctx.getModel().getName() + subPackage + '.' + typeName);
		}

		public JavaType javaType(RosettaNamed namedType) {
			RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(namedType, RosettaRootElement.class);
			return javaType(rootElement, namedType.getName());
		}

		public String name() {
			if (parent != null) {
				return parent.name() + "." + name;
			}
			return name;
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

		public Package calculation() {
			return new Package(this, "calculation");
		}

		public Package functions() {
			return new Package(this, "functions");
		}

		public Package binding() {
			return new Package(this, "binding");
		}

		public Package bindingUtil() {
			return binding().child("util");
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

		public Package ingestion() {
			return new Package(this, "ingestion");
		}

		public Package ingestionChild() {
			return new Package(this, "ingestion");
		}

		public Package processor() {
			return new Package(this, "processor");
		}
	}
}
