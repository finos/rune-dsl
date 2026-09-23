package test.escaping;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.escaping.meta.DocumentedMeta;

import static java.util.Optional.ofNullable;

/**
 * A type stored in C:&#92;users, which *&#47; ends a comment.
 * @version 1.0 "beta" C:&#92;users
 *
 * Body Org1
 * Corpus Agreement Agr1 Agreement in C:&#92;users *&#47; "A corpus in C:&#92;users *&#47;" 
 * name "C:&#92;users *&#47;"
 *
 * Provision A provision in C:&#92;users, which *&#47; ends a comment.
 *
 */
@RosettaDataType(value="Documented", builder=Documented.DocumentedBuilderImpl.class, version="1.0 \"beta\" C:\\users")
@RuneDataType(value="Documented", model="test", builder=Documented.DocumentedBuilderImpl.class, version="1.0 \"beta\" C:\\users")
public interface Documented extends RosettaModelObject {

	DocumentedMeta metaData = new DocumentedMeta();

	/*********************** Getter Methods  ***********************/
	/**
	 * An attribute stored in C:&#92;users, which *&#47; ends a comment.
	 *
	 * Body Org1
	 * Corpus Agreement Agr1 Agreement in C:&#92;users *&#47; "A corpus in C:&#92;users *&#47;" 
	 * name "C:&#92;users *&#47;"
	 *
	 * Provision A provision in C:&#92;users *&#47;
	 *
	 */
	String getAttr();

	/*********************** Build Methods  ***********************/
	Documented build();
	
	Documented.DocumentedBuilder toBuilder();
	
	static Documented.DocumentedBuilder builder() {
		return new Documented.DocumentedBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Documented> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Documented> getType() {
		return Documented.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("attr"), String.class, getAttr(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface DocumentedBuilder extends Documented, RosettaModelObjectBuilder {
		Documented.DocumentedBuilder setAttr(String attr);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("attr"), String.class, getAttr(), this);
		}
		

		Documented.DocumentedBuilder prune();
	}

	/*********************** Immutable Implementation of Documented  ***********************/
	class DocumentedImpl implements Documented {
		private final String attr;
		
		protected DocumentedImpl(Documented.DocumentedBuilder builder) {
			this.attr = builder.getAttr();
		}
		
		@Override
		@RosettaAttribute("attr")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("attr")
		public String getAttr() {
			return attr;
		}
		
		@Override
		public Documented build() {
			return this;
		}
		
		@Override
		public Documented.DocumentedBuilder toBuilder() {
			Documented.DocumentedBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Documented.DocumentedBuilder builder) {
			ofNullable(getAttr()).ifPresent(builder::setAttr);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Documented _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Documented {" +
				"attr=" + this.attr +
			'}';
		}
	}

	/*********************** Builder Implementation of Documented  ***********************/
	class DocumentedBuilderImpl implements Documented.DocumentedBuilder {
	
		protected String attr;
		
		@Override
		@RosettaAttribute("attr")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("attr")
		public String getAttr() {
			return attr;
		}
		
		@RosettaAttribute("attr")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("attr")
		@Override
		public Documented.DocumentedBuilder setAttr(String _attr) {
			this.attr = _attr == null ? null : _attr;
			return this;
		}
		
		@Override
		public Documented build() {
			return new Documented.DocumentedImpl(this);
		}
		
		@Override
		public Documented.DocumentedBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Documented.DocumentedBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getAttr()!=null) return true;
			return false;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Documented _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "DocumentedBuilder {" +
				"attr=" + this.attr +
			'}';
		}
	}
}
