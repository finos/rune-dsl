package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.util.Objects;
import test.pojo.meta.QuxMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Qux", builder=Qux.QuxBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Qux", model="test", builder=Qux.QuxBuilderImpl.class, version="0.0.0")
public interface Qux extends RosettaModelObject {

	QuxMeta metaData = new QuxMeta();

	/*********************** Getter Methods  ***********************/
	FieldWithMetaString getQux();

	/*********************** Build Methods  ***********************/
	Qux build();
	
	Qux.QuxBuilder toBuilder();
	
	static Qux.QuxBuilder builder() {
		return new Qux.QuxBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Qux> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Qux> getType() {
		return Qux.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("qux"), processor, FieldWithMetaString.class, getQux());
	}
	

	/*********************** Builder Interface  ***********************/
	interface QuxBuilder extends Qux, RosettaModelObjectBuilder {
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateQux();
		@Override
		FieldWithMetaString.FieldWithMetaStringBuilder getQux();
		Qux.QuxBuilder setQux(FieldWithMetaString qux);
		Qux.QuxBuilder setQuxValue(String qux);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("qux"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getQux());
		}
		

		Qux.QuxBuilder prune();
	}

	/*********************** Immutable Implementation of Qux  ***********************/
	class QuxImpl implements Qux {
		private final FieldWithMetaString qux;
		
		protected QuxImpl(Qux.QuxBuilder builder) {
			this.qux = ofNullable(builder.getQux()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute(value="qux", isRequired=true, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(value="qux", isRequired=true)
		@RuneScopedAttributeKey
		public FieldWithMetaString getQux() {
			return qux;
		}
		
		@Override
		public Qux build() {
			return this;
		}
		
		@Override
		public Qux.QuxBuilder toBuilder() {
			Qux.QuxBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Qux.QuxBuilder builder) {
			ofNullable(getQux()).ifPresent(builder::setQux);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Qux _that = getType().cast(o);
		
			if (!Objects.equals(qux, _that.getQux())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (qux != null ? qux.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Qux {" +
				"qux=" + this.qux +
			'}';
		}
	}

	/*********************** Builder Implementation of Qux  ***********************/
	class QuxBuilderImpl implements Qux.QuxBuilder {
	
		protected FieldWithMetaString.FieldWithMetaStringBuilder qux;
		
		@Override
		@RosettaAttribute(value="qux", isRequired=true, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(value="qux", isRequired=true)
		@RuneScopedAttributeKey
		public FieldWithMetaString.FieldWithMetaStringBuilder getQux() {
			return qux;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateQux() {
			FieldWithMetaString.FieldWithMetaStringBuilder result;
			if (qux!=null) {
				result = qux;
			}
			else {
				result = qux = FieldWithMetaString.builder();
				result.getOrCreateMeta().toBuilder().addKey(Key.builder().setScope("DOCUMENT"));
			}
			
			return result;
		}
		
		@RosettaAttribute(value="qux", isRequired=true, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(value="qux", isRequired=true)
		@RuneScopedAttributeKey
		@Override
		public Qux.QuxBuilder setQux(FieldWithMetaString _qux) {
			this.qux = _qux == null ? null : _qux.toBuilder();
			return this;
		}
		
		@Override
		public Qux.QuxBuilder setQuxValue(String _qux) {
			this.getOrCreateQux().setValue(_qux);
			return this;
		}
		
		@Override
		public Qux build() {
			return new Qux.QuxImpl(this);
		}
		
		@Override
		public Qux.QuxBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Qux.QuxBuilder prune() {
			if (qux!=null && !qux.prune().hasData()) qux = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getQux()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Qux.QuxBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Qux.QuxBuilder o = (Qux.QuxBuilder) other;
			
			merger.mergeRosetta(getQux(), o.getQux(), this::setQux);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Qux _that = getType().cast(o);
		
			if (!Objects.equals(qux, _that.getQux())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (qux != null ? qux.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "QuxBuilder {" +
				"qux=" + this.qux +
			'}';
		}
	}
}
