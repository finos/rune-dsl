package test.pojo;

import com.google.common.collect.ImmutableList;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Multi;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.util.ListEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.pojo.labels.types.QuxLabelProvider;
import test.pojo.meta.QuxMeta;

import static java.util.Optional.ofNullable;

/**
 * A type with a definition.
 * @version 0.0.0
 */
@RosettaDataType(value="Qux", builder=Qux.QuxBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Qux", model="test", builder=Qux.QuxBuilderImpl.class, version="0.0.0")
@RuneLabelProvider(labelProvider=QuxLabelProvider.class)
public interface Qux extends RosettaModelObject {

	QuxMeta metaData = new QuxMeta();

	/*********************** Getter Methods  ***********************/
	FieldWithMetaString getQux();
	List<? extends FieldWithMetaString> getMultiQux();

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
		processRosetta(path.newSubPath("multiQux"), processor, FieldWithMetaString.class, getMultiQux());
	}
	

	/*********************** Builder Interface  ***********************/
	interface QuxBuilder extends Qux, RosettaModelObjectBuilder {
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateQux();
		@Override
		FieldWithMetaString.FieldWithMetaStringBuilder getQux();
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiQux(int index);
		@Override
		List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiQux();
		Qux.QuxBuilder setQux(FieldWithMetaString qux);
		Qux.QuxBuilder setQuxValue(String qux);
		Qux.QuxBuilder addMultiQux(FieldWithMetaString multiQux);
		Qux.QuxBuilder addMultiQux(FieldWithMetaString multiQux, int idx);
		Qux.QuxBuilder addMultiQuxValue(String multiQux);
		Qux.QuxBuilder addMultiQuxValue(String multiQux, int idx);
		Qux.QuxBuilder addMultiQux(List<? extends FieldWithMetaString> multiQux);
		Qux.QuxBuilder setMultiQux(List<? extends FieldWithMetaString> multiQux);
		Qux.QuxBuilder addMultiQuxValue(List<? extends String> multiQux);
		Qux.QuxBuilder setMultiQuxValue(List<? extends String> multiQux);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("qux"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getQux());
			processRosetta(path.newSubPath("multiQux"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMultiQux());
		}
		

		Qux.QuxBuilder prune();
	}

	/*********************** Immutable Implementation of Qux  ***********************/
	class QuxImpl implements Qux {
		private final FieldWithMetaString qux;
		private final List<? extends FieldWithMetaString> multiQux;
		
		protected QuxImpl(Qux.QuxBuilder builder) {
			this.qux = ofNullable(builder.getQux()).map(f->f.build()).orElse(null);
			this.multiQux = ofNullable(builder.getMultiQux()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
		}
		
		@Override
		@RosettaAttribute("qux")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("qux")
		@RuneScopedAttributeKey
		public FieldWithMetaString getQux() {
			return qux;
		}
		
		@Override
		@RosettaAttribute("multiQux")
		@Accessor(AccessorType.GETTER)
		@Multi
		@RuneAttribute("multiQux")
		@RuneScopedAttributeKey
		public List<? extends FieldWithMetaString> getMultiQux() {
			return multiQux;
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
			ofNullable(getMultiQux()).ifPresent(builder::setMultiQux);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Qux _that = getType().cast(o);
		
			if (!Objects.equals(qux, _that.getQux())) return false;
			if (!ListEquals.listEquals(multiQux, _that.getMultiQux())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (qux != null ? qux.hashCode() : 0);
			_result = 31 * _result + (multiQux != null ? multiQux.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Qux {" +
				"qux=" + this.qux + ", " +
				"multiQux=" + this.multiQux +
			'}';
		}
	}

	/*********************** Builder Implementation of Qux  ***********************/
	class QuxBuilderImpl implements Qux.QuxBuilder {
	
		protected FieldWithMetaString.FieldWithMetaStringBuilder qux;
		protected List<FieldWithMetaString.FieldWithMetaStringBuilder> multiQux = new ArrayList<>();
		
		@Override
		@RosettaAttribute("qux")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("qux")
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
		
		@Override
		@RosettaAttribute("multiQux")
		@Accessor(AccessorType.GETTER)
		@Multi
		@RuneAttribute("multiQux")
		@RuneScopedAttributeKey
		public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiQux() {
			return multiQux;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiQux(int index) {
			if (multiQux==null) {
				this.multiQux = new ArrayList<>();
			}
			return getIndex(multiQux, index, () -> {
						FieldWithMetaString.FieldWithMetaStringBuilder newMultiQux = FieldWithMetaString.builder();
						newMultiQux.getOrCreateMeta().addKey(Key.builder().setScope("DOCUMENT"));
						return newMultiQux;
					});
		}
		
		@RosettaAttribute("qux")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("qux")
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
		
		@RosettaAttribute("multiQux")
		@Accessor(AccessorType.ADDER)
		@Multi
		@RuneAttribute("multiQux")
		@RuneScopedAttributeKey
		@Override
		public Qux.QuxBuilder addMultiQux(FieldWithMetaString _multiQux) {
			if (_multiQux != null) {
				this.multiQux.add(_multiQux.toBuilder());
			}
			return this;
		}
		
		@Override
		public Qux.QuxBuilder addMultiQux(FieldWithMetaString _multiQux, int idx) {
			getIndex(this.multiQux, idx, () -> _multiQux.toBuilder());
			return this;
		}
		
		@Override
		public Qux.QuxBuilder addMultiQuxValue(String _multiQux) {
			this.getOrCreateMultiQux(-1).setValue(_multiQux);
			return this;
		}
		
		@Override
		public Qux.QuxBuilder addMultiQuxValue(String _multiQux, int idx) {
			this.getOrCreateMultiQux(idx).setValue(_multiQux);
			return this;
		}
		
		@Override
		public Qux.QuxBuilder addMultiQux(List<? extends FieldWithMetaString> multiQuxs) {
			if (multiQuxs != null) {
				for (final FieldWithMetaString toAdd : multiQuxs) {
					this.multiQux.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@RosettaAttribute("multiQux")
		@Accessor(AccessorType.SETTER)
		@Multi
		@RuneAttribute("multiQux")
		@RuneScopedAttributeKey
		@Override
		public Qux.QuxBuilder setMultiQux(List<? extends FieldWithMetaString> multiQuxs) {
			if (multiQuxs == null) {
				this.multiQux = new ArrayList<>();
			} else {
				this.multiQux = multiQuxs.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Qux.QuxBuilder addMultiQuxValue(List<? extends String> multiQuxs) {
			if (multiQuxs != null) {
				for (final String toAdd : multiQuxs) {
					this.addMultiQuxValue(toAdd);
				}
			}
			return this;
		}
		
		@Override
		public Qux.QuxBuilder setMultiQuxValue(List<? extends String> multiQuxs) {
			this.multiQux.clear();
			if (multiQuxs != null) {
				multiQuxs.forEach(this::addMultiQuxValue);
			}
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
			multiQux = multiQux.stream().filter(b->b!=null).<FieldWithMetaString.FieldWithMetaStringBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getQux()!=null) return true;
			if (getMultiQux()!=null && !getMultiQux().isEmpty()) return true;
			return false;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Qux _that = getType().cast(o);
		
			if (!Objects.equals(qux, _that.getQux())) return false;
			if (!ListEquals.listEquals(multiQux, _that.getMultiQux())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (qux != null ? qux.hashCode() : 0);
			_result = 31 * _result + (multiQux != null ? multiQux.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "QuxBuilder {" +
				"qux=" + this.qux + ", " +
				"multiQux=" + this.multiQux +
			'}';
		}
	}
}
