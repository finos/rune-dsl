package test.pojo;

import com.google.common.collect.ImmutableList;
import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.GlobalKey.GlobalKeyBuilder;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneMetaType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.FieldWithMetaString.FieldWithMetaStringBuilder;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.model.metafields.MetaFields.MetaFieldsBuilder;
import com.rosetta.util.ListEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import test.pojo.Foo;
import test.pojo.Foo.FooBuilder;
import test.pojo.Pojo;
import test.pojo.Pojo.PojoBuilder;
import test.pojo.Pojo.PojoBuilderImpl;
import test.pojo.Pojo.PojoImpl;
import test.pojo.meta.PojoMeta;
import test.pojo.metafields.ReferenceWithMetaFoo;
import test.pojo.metafields.ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Pojo", builder=Pojo.PojoBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Pojo", model="test", builder=Pojo.PojoBuilderImpl.class, version="0.0.0")
public interface Pojo extends RosettaModelObject, GlobalKey {

	PojoMeta metaData = new PojoMeta();

	/*********************** Getter Methods  ***********************/
	String getSimpleAttr();
	List<String> getMultiSimpleAttr();
	FieldWithMetaString getSimpleAttrWithMeta();
	List<? extends FieldWithMetaString> getMultiSimpleAttrWithMeta();
	FieldWithMetaString getSimpleAttrWithId();
	List<? extends FieldWithMetaString> getMultiSimpleAttrWithId();
	Foo getComplexAttr();
	List<? extends Foo> getMultiComplexAttr();
	ReferenceWithMetaFoo getComplexAttrWithRef();
	List<? extends ReferenceWithMetaFoo> getMultiComplexAttrWithRef();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	Pojo build();
	
	Pojo.PojoBuilder toBuilder();
	
	static Pojo.PojoBuilder builder() {
		return new Pojo.PojoBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Pojo> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Pojo> getType() {
		return Pojo.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("simpleAttr"), String.class, getSimpleAttr(), this);
		processor.processBasic(path.newSubPath("multiSimpleAttr"), String.class, getMultiSimpleAttr(), this);
		processRosetta(path.newSubPath("simpleAttrWithMeta"), processor, FieldWithMetaString.class, getSimpleAttrWithMeta());
		processRosetta(path.newSubPath("multiSimpleAttrWithMeta"), processor, FieldWithMetaString.class, getMultiSimpleAttrWithMeta());
		processRosetta(path.newSubPath("simpleAttrWithId"), processor, FieldWithMetaString.class, getSimpleAttrWithId(), AttributeMeta.GLOBAL_KEY_FIELD);
		processRosetta(path.newSubPath("multiSimpleAttrWithId"), processor, FieldWithMetaString.class, getMultiSimpleAttrWithId(), AttributeMeta.GLOBAL_KEY_FIELD);
		processRosetta(path.newSubPath("complexAttr"), processor, Foo.class, getComplexAttr());
		processRosetta(path.newSubPath("multiComplexAttr"), processor, Foo.class, getMultiComplexAttr());
		processRosetta(path.newSubPath("complexAttrWithRef"), processor, ReferenceWithMetaFoo.class, getComplexAttrWithRef());
		processRosetta(path.newSubPath("multiComplexAttrWithRef"), processor, ReferenceWithMetaFoo.class, getMultiComplexAttrWithRef());
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface PojoBuilder extends Pojo, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder {
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateSimpleAttrWithMeta();
		@Override
		FieldWithMetaString.FieldWithMetaStringBuilder getSimpleAttrWithMeta();
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiSimpleAttrWithMeta(int _index);
		@Override
		List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithMeta();
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateSimpleAttrWithId();
		@Override
		FieldWithMetaString.FieldWithMetaStringBuilder getSimpleAttrWithId();
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiSimpleAttrWithId(int _index);
		@Override
		List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithId();
		Foo.FooBuilder getOrCreateComplexAttr();
		@Override
		Foo.FooBuilder getComplexAttr();
		Foo.FooBuilder getOrCreateMultiComplexAttr(int _index);
		@Override
		List<? extends Foo.FooBuilder> getMultiComplexAttr();
		ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getOrCreateComplexAttrWithRef();
		@Override
		ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getComplexAttrWithRef();
		ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getOrCreateMultiComplexAttrWithRef(int _index);
		@Override
		List<? extends ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder> getMultiComplexAttrWithRef();
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		@Override
		MetaFields.MetaFieldsBuilder getMeta();
		Pojo.PojoBuilder setSimpleAttr(String simpleAttr);
		Pojo.PojoBuilder addMultiSimpleAttr(String multiSimpleAttr);
		Pojo.PojoBuilder addMultiSimpleAttr(String multiSimpleAttr, int _idx);
		Pojo.PojoBuilder addMultiSimpleAttr(List<String> multiSimpleAttr);
		Pojo.PojoBuilder setMultiSimpleAttr(List<String> multiSimpleAttr);
		Pojo.PojoBuilder setSimpleAttrWithMeta(FieldWithMetaString simpleAttrWithMeta);
		Pojo.PojoBuilder setSimpleAttrWithMetaValue(String simpleAttrWithMeta);
		Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString multiSimpleAttrWithMeta);
		Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString multiSimpleAttrWithMeta, int _idx);
		Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String multiSimpleAttrWithMeta);
		Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String multiSimpleAttrWithMeta, int _idx);
		Pojo.PojoBuilder addMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMeta);
		Pojo.PojoBuilder setMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMeta);
		Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMeta);
		Pojo.PojoBuilder setMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMeta);
		Pojo.PojoBuilder setSimpleAttrWithId(FieldWithMetaString simpleAttrWithId);
		Pojo.PojoBuilder setSimpleAttrWithIdValue(String simpleAttrWithId);
		Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString multiSimpleAttrWithId);
		Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString multiSimpleAttrWithId, int _idx);
		Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String multiSimpleAttrWithId);
		Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String multiSimpleAttrWithId, int _idx);
		Pojo.PojoBuilder addMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithId);
		Pojo.PojoBuilder setMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithId);
		Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithId);
		Pojo.PojoBuilder setMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithId);
		Pojo.PojoBuilder setComplexAttr(Foo complexAttr);
		Pojo.PojoBuilder addMultiComplexAttr(Foo multiComplexAttr);
		Pojo.PojoBuilder addMultiComplexAttr(Foo multiComplexAttr, int _idx);
		Pojo.PojoBuilder addMultiComplexAttr(List<? extends Foo> multiComplexAttr);
		Pojo.PojoBuilder setMultiComplexAttr(List<? extends Foo> multiComplexAttr);
		Pojo.PojoBuilder setComplexAttrWithRef(ReferenceWithMetaFoo complexAttrWithRef);
		Pojo.PojoBuilder setComplexAttrWithRefValue(Foo complexAttrWithRef);
		Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo multiComplexAttrWithRef);
		Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo multiComplexAttrWithRef, int _idx);
		Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo multiComplexAttrWithRef);
		Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo multiComplexAttrWithRef, int _idx);
		Pojo.PojoBuilder addMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRef);
		Pojo.PojoBuilder setMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRef);
		Pojo.PojoBuilder addMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRef);
		Pojo.PojoBuilder setMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRef);
		Pojo.PojoBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("simpleAttr"), String.class, getSimpleAttr(), this);
			processor.processBasic(path.newSubPath("multiSimpleAttr"), String.class, getMultiSimpleAttr(), this);
			processRosetta(path.newSubPath("simpleAttrWithMeta"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getSimpleAttrWithMeta());
			processRosetta(path.newSubPath("multiSimpleAttrWithMeta"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMultiSimpleAttrWithMeta());
			processRosetta(path.newSubPath("simpleAttrWithId"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getSimpleAttrWithId(), AttributeMeta.GLOBAL_KEY_FIELD);
			processRosetta(path.newSubPath("multiSimpleAttrWithId"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMultiSimpleAttrWithId(), AttributeMeta.GLOBAL_KEY_FIELD);
			processRosetta(path.newSubPath("complexAttr"), processor, Foo.FooBuilder.class, getComplexAttr());
			processRosetta(path.newSubPath("multiComplexAttr"), processor, Foo.FooBuilder.class, getMultiComplexAttr());
			processRosetta(path.newSubPath("complexAttrWithRef"), processor, ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder.class, getComplexAttrWithRef());
			processRosetta(path.newSubPath("multiComplexAttrWithRef"), processor, ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder.class, getMultiComplexAttrWithRef());
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		Pojo.PojoBuilder prune();
	}

	/*********************** Immutable Implementation of Pojo  ***********************/
	class PojoImpl implements Pojo {
		private final String simpleAttr;
		private final List<String> multiSimpleAttr;
		private final FieldWithMetaString simpleAttrWithMeta;
		private final List<? extends FieldWithMetaString> multiSimpleAttrWithMeta;
		private final FieldWithMetaString simpleAttrWithId;
		private final List<? extends FieldWithMetaString> multiSimpleAttrWithId;
		private final Foo complexAttr;
		private final List<? extends Foo> multiComplexAttr;
		private final ReferenceWithMetaFoo complexAttrWithRef;
		private final List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRef;
		private final MetaFields meta;
		
		protected PojoImpl(Pojo.PojoBuilder builder) {
			this.simpleAttr = builder.getSimpleAttr();
			this.multiSimpleAttr = ofNullable(builder.getMultiSimpleAttr()).filter(_l->!_l.isEmpty()).map(ImmutableList::copyOf).orElse(null);
			this.simpleAttrWithMeta = ofNullable(builder.getSimpleAttrWithMeta()).map(f->f.build()).orElse(null);
			this.multiSimpleAttrWithMeta = ofNullable(builder.getMultiSimpleAttrWithMeta()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.simpleAttrWithId = ofNullable(builder.getSimpleAttrWithId()).map(f->f.build()).orElse(null);
			this.multiSimpleAttrWithId = ofNullable(builder.getMultiSimpleAttrWithId()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.complexAttr = ofNullable(builder.getComplexAttr()).map(f->f.build()).orElse(null);
			this.multiComplexAttr = ofNullable(builder.getMultiComplexAttr()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.complexAttrWithRef = ofNullable(builder.getComplexAttrWithRef()).map(f->f.build()).orElse(null);
			this.multiComplexAttrWithRef = ofNullable(builder.getMultiComplexAttrWithRef()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("simpleAttr")
		@RuneAttribute("simpleAttr")
		public String getSimpleAttr() {
			return simpleAttr;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttr")
		@RuneAttribute("multiSimpleAttr")
		public List<String> getMultiSimpleAttr() {
			return multiSimpleAttr;
		}
		
		@Override
		@RosettaAttribute("simpleAttrWithMeta")
		@RuneAttribute("simpleAttrWithMeta")
		public FieldWithMetaString getSimpleAttrWithMeta() {
			return simpleAttrWithMeta;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttrWithMeta")
		@RuneAttribute("multiSimpleAttrWithMeta")
		public List<? extends FieldWithMetaString> getMultiSimpleAttrWithMeta() {
			return multiSimpleAttrWithMeta;
		}
		
		@Override
		@RosettaAttribute("simpleAttrWithId")
		@RuneAttribute("simpleAttrWithId")
		public FieldWithMetaString getSimpleAttrWithId() {
			return simpleAttrWithId;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttrWithId")
		@RuneAttribute("multiSimpleAttrWithId")
		public List<? extends FieldWithMetaString> getMultiSimpleAttrWithId() {
			return multiSimpleAttrWithId;
		}
		
		@Override
		@RosettaAttribute("complexAttr")
		@RuneAttribute("complexAttr")
		public Foo getComplexAttr() {
			return complexAttr;
		}
		
		@Override
		@RosettaAttribute("multiComplexAttr")
		@RuneAttribute("multiComplexAttr")
		public List<? extends Foo> getMultiComplexAttr() {
			return multiComplexAttr;
		}
		
		@Override
		@RosettaAttribute("complexAttrWithRef")
		@RuneAttribute("complexAttrWithRef")
		public ReferenceWithMetaFoo getComplexAttrWithRef() {
			return complexAttrWithRef;
		}
		
		@Override
		@RosettaAttribute("multiComplexAttrWithRef")
		@RuneAttribute("multiComplexAttrWithRef")
		public List<? extends ReferenceWithMetaFoo> getMultiComplexAttrWithRef() {
			return multiComplexAttrWithRef;
		}
		
		@Override
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public Pojo build() {
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder toBuilder() {
			Pojo.PojoBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Pojo.PojoBuilder builder) {
			ofNullable(getSimpleAttr()).ifPresent(builder::setSimpleAttr);
			ofNullable(getMultiSimpleAttr()).ifPresent(builder::setMultiSimpleAttr);
			ofNullable(getSimpleAttrWithMeta()).ifPresent(builder::setSimpleAttrWithMeta);
			ofNullable(getMultiSimpleAttrWithMeta()).ifPresent(builder::setMultiSimpleAttrWithMeta);
			ofNullable(getSimpleAttrWithId()).ifPresent(builder::setSimpleAttrWithId);
			ofNullable(getMultiSimpleAttrWithId()).ifPresent(builder::setMultiSimpleAttrWithId);
			ofNullable(getComplexAttr()).ifPresent(builder::setComplexAttr);
			ofNullable(getMultiComplexAttr()).ifPresent(builder::setMultiComplexAttr);
			ofNullable(getComplexAttrWithRef()).ifPresent(builder::setComplexAttrWithRef);
			ofNullable(getMultiComplexAttrWithRef()).ifPresent(builder::setMultiComplexAttrWithRef);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Pojo _that = getType().cast(o);
		
			if (!Objects.equals(simpleAttr, _that.getSimpleAttr())) return false;
			if (!ListEquals.listEquals(multiSimpleAttr, _that.getMultiSimpleAttr())) return false;
			if (!Objects.equals(simpleAttrWithMeta, _that.getSimpleAttrWithMeta())) return false;
			if (!ListEquals.listEquals(multiSimpleAttrWithMeta, _that.getMultiSimpleAttrWithMeta())) return false;
			if (!Objects.equals(simpleAttrWithId, _that.getSimpleAttrWithId())) return false;
			if (!ListEquals.listEquals(multiSimpleAttrWithId, _that.getMultiSimpleAttrWithId())) return false;
			if (!Objects.equals(complexAttr, _that.getComplexAttr())) return false;
			if (!ListEquals.listEquals(multiComplexAttr, _that.getMultiComplexAttr())) return false;
			if (!Objects.equals(complexAttrWithRef, _that.getComplexAttrWithRef())) return false;
			if (!ListEquals.listEquals(multiComplexAttrWithRef, _that.getMultiComplexAttrWithRef())) return false;
			if (!Objects.equals(meta, _that.getMeta())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (simpleAttr != null ? simpleAttr.hashCode() : 0);
			_result = 31 * _result + (multiSimpleAttr != null ? multiSimpleAttr.hashCode() : 0);
			_result = 31 * _result + (simpleAttrWithMeta != null ? simpleAttrWithMeta.hashCode() : 0);
			_result = 31 * _result + (multiSimpleAttrWithMeta != null ? multiSimpleAttrWithMeta.hashCode() : 0);
			_result = 31 * _result + (simpleAttrWithId != null ? simpleAttrWithId.hashCode() : 0);
			_result = 31 * _result + (multiSimpleAttrWithId != null ? multiSimpleAttrWithId.hashCode() : 0);
			_result = 31 * _result + (complexAttr != null ? complexAttr.hashCode() : 0);
			_result = 31 * _result + (multiComplexAttr != null ? multiComplexAttr.hashCode() : 0);
			_result = 31 * _result + (complexAttrWithRef != null ? complexAttrWithRef.hashCode() : 0);
			_result = 31 * _result + (multiComplexAttrWithRef != null ? multiComplexAttrWithRef.hashCode() : 0);
			_result = 31 * _result + (meta != null ? meta.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Pojo {" +
				"simpleAttr=" + this.simpleAttr + ", " +
				"multiSimpleAttr=" + this.multiSimpleAttr + ", " +
				"simpleAttrWithMeta=" + this.simpleAttrWithMeta + ", " +
				"multiSimpleAttrWithMeta=" + this.multiSimpleAttrWithMeta + ", " +
				"simpleAttrWithId=" + this.simpleAttrWithId + ", " +
				"multiSimpleAttrWithId=" + this.multiSimpleAttrWithId + ", " +
				"complexAttr=" + this.complexAttr + ", " +
				"multiComplexAttr=" + this.multiComplexAttr + ", " +
				"complexAttrWithRef=" + this.complexAttrWithRef + ", " +
				"multiComplexAttrWithRef=" + this.multiComplexAttrWithRef + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of Pojo  ***********************/
	class PojoBuilderImpl implements Pojo.PojoBuilder {
	
		protected String simpleAttr;
		protected List<String> multiSimpleAttr = new ArrayList<>();
		protected FieldWithMetaString.FieldWithMetaStringBuilder simpleAttrWithMeta;
		protected List<FieldWithMetaString.FieldWithMetaStringBuilder> multiSimpleAttrWithMeta = new ArrayList<>();
		protected FieldWithMetaString.FieldWithMetaStringBuilder simpleAttrWithId;
		protected List<FieldWithMetaString.FieldWithMetaStringBuilder> multiSimpleAttrWithId = new ArrayList<>();
		protected Foo.FooBuilder complexAttr;
		protected List<Foo.FooBuilder> multiComplexAttr = new ArrayList<>();
		protected ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder complexAttrWithRef;
		protected List<ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder> multiComplexAttrWithRef = new ArrayList<>();
		protected MetaFields.MetaFieldsBuilder meta;
		
		@Override
		@RosettaAttribute("simpleAttr")
		@RuneAttribute("simpleAttr")
		public String getSimpleAttr() {
			return simpleAttr;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttr")
		@RuneAttribute("multiSimpleAttr")
		public List<String> getMultiSimpleAttr() {
			return multiSimpleAttr;
		}
		
		@Override
		@RosettaAttribute("simpleAttrWithMeta")
		@RuneAttribute("simpleAttrWithMeta")
		public FieldWithMetaString.FieldWithMetaStringBuilder getSimpleAttrWithMeta() {
			return simpleAttrWithMeta;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateSimpleAttrWithMeta() {
			FieldWithMetaString.FieldWithMetaStringBuilder result;
			if (simpleAttrWithMeta!=null) {
				result = simpleAttrWithMeta;
			}
			else {
				result = simpleAttrWithMeta = FieldWithMetaString.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttrWithMeta")
		@RuneAttribute("multiSimpleAttrWithMeta")
		public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithMeta() {
			return multiSimpleAttrWithMeta;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiSimpleAttrWithMeta(int _index) {
		
			if (multiSimpleAttrWithMeta==null) {
				this.multiSimpleAttrWithMeta = new ArrayList<>();
			}
			FieldWithMetaString.FieldWithMetaStringBuilder result;
			return getIndex(multiSimpleAttrWithMeta, _index, () -> {
						FieldWithMetaString.FieldWithMetaStringBuilder newMultiSimpleAttrWithMeta = FieldWithMetaString.builder();
						return newMultiSimpleAttrWithMeta;
					});
		}
		
		@Override
		@RosettaAttribute("simpleAttrWithId")
		@RuneAttribute("simpleAttrWithId")
		public FieldWithMetaString.FieldWithMetaStringBuilder getSimpleAttrWithId() {
			return simpleAttrWithId;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateSimpleAttrWithId() {
			FieldWithMetaString.FieldWithMetaStringBuilder result;
			if (simpleAttrWithId!=null) {
				result = simpleAttrWithId;
			}
			else {
				result = simpleAttrWithId = FieldWithMetaString.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttrWithId")
		@RuneAttribute("multiSimpleAttrWithId")
		public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithId() {
			return multiSimpleAttrWithId;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiSimpleAttrWithId(int _index) {
		
			if (multiSimpleAttrWithId==null) {
				this.multiSimpleAttrWithId = new ArrayList<>();
			}
			FieldWithMetaString.FieldWithMetaStringBuilder result;
			return getIndex(multiSimpleAttrWithId, _index, () -> {
						FieldWithMetaString.FieldWithMetaStringBuilder newMultiSimpleAttrWithId = FieldWithMetaString.builder();
						return newMultiSimpleAttrWithId;
					});
		}
		
		@Override
		@RosettaAttribute("complexAttr")
		@RuneAttribute("complexAttr")
		public Foo.FooBuilder getComplexAttr() {
			return complexAttr;
		}
		
		@Override
		public Foo.FooBuilder getOrCreateComplexAttr() {
			Foo.FooBuilder result;
			if (complexAttr!=null) {
				result = complexAttr;
			}
			else {
				result = complexAttr = Foo.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("multiComplexAttr")
		@RuneAttribute("multiComplexAttr")
		public List<? extends Foo.FooBuilder> getMultiComplexAttr() {
			return multiComplexAttr;
		}
		
		@Override
		public Foo.FooBuilder getOrCreateMultiComplexAttr(int _index) {
		
			if (multiComplexAttr==null) {
				this.multiComplexAttr = new ArrayList<>();
			}
			Foo.FooBuilder result;
			return getIndex(multiComplexAttr, _index, () -> {
						Foo.FooBuilder newMultiComplexAttr = Foo.builder();
						return newMultiComplexAttr;
					});
		}
		
		@Override
		@RosettaAttribute("complexAttrWithRef")
		@RuneAttribute("complexAttrWithRef")
		public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getComplexAttrWithRef() {
			return complexAttrWithRef;
		}
		
		@Override
		public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getOrCreateComplexAttrWithRef() {
			ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder result;
			if (complexAttrWithRef!=null) {
				result = complexAttrWithRef;
			}
			else {
				result = complexAttrWithRef = ReferenceWithMetaFoo.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("multiComplexAttrWithRef")
		@RuneAttribute("multiComplexAttrWithRef")
		public List<? extends ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder> getMultiComplexAttrWithRef() {
			return multiComplexAttrWithRef;
		}
		
		@Override
		public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getOrCreateMultiComplexAttrWithRef(int _index) {
		
			if (multiComplexAttrWithRef==null) {
				this.multiComplexAttrWithRef = new ArrayList<>();
			}
			ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder result;
			return getIndex(multiComplexAttrWithRef, _index, () -> {
						ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder newMultiComplexAttrWithRef = ReferenceWithMetaFoo.builder();
						return newMultiComplexAttrWithRef;
					});
		}
		
		@Override
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
		public MetaFields.MetaFieldsBuilder getMeta() {
			return meta;
		}
		
		@Override
		public MetaFields.MetaFieldsBuilder getOrCreateMeta() {
			MetaFields.MetaFieldsBuilder result;
			if (meta!=null) {
				result = meta;
			}
			else {
				result = meta = MetaFields.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("simpleAttr")
		@RuneAttribute("simpleAttr")
		public Pojo.PojoBuilder setSimpleAttr(String _simpleAttr) {
			this.simpleAttr = _simpleAttr == null ? null : _simpleAttr;
			return this;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttr")
		@RuneAttribute("multiSimpleAttr")
		public Pojo.PojoBuilder addMultiSimpleAttr(String _multiSimpleAttr) {
			if (_multiSimpleAttr != null) {
				this.multiSimpleAttr.add(_multiSimpleAttr);
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttr(String _multiSimpleAttr, int _idx) {
			getIndex(this.multiSimpleAttr, _idx, () -> _multiSimpleAttr);
			return this;
		}
		
		@Override 
		public Pojo.PojoBuilder addMultiSimpleAttr(List<String> multiSimpleAttrs) {
			if (multiSimpleAttrs != null) {
				for (final String toAdd : multiSimpleAttrs) {
					this.multiSimpleAttr.add(toAdd);
				}
			}
			return this;
		}
		
		@Override 
		@RuneAttribute("multiSimpleAttr")
		public Pojo.PojoBuilder setMultiSimpleAttr(List<String> multiSimpleAttrs) {
			if (multiSimpleAttrs == null) {
				this.multiSimpleAttr = new ArrayList<>();
			} else {
				this.multiSimpleAttr = multiSimpleAttrs.stream()
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		@RosettaAttribute("simpleAttrWithMeta")
		@RuneAttribute("simpleAttrWithMeta")
		public Pojo.PojoBuilder setSimpleAttrWithMeta(FieldWithMetaString _simpleAttrWithMeta) {
			this.simpleAttrWithMeta = _simpleAttrWithMeta == null ? null : _simpleAttrWithMeta.toBuilder();
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder setSimpleAttrWithMetaValue(String _simpleAttrWithMeta) {
			this.getOrCreateSimpleAttrWithMeta().setValue(_simpleAttrWithMeta);
			return this;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttrWithMeta")
		@RuneAttribute("multiSimpleAttrWithMeta")
		public Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString _multiSimpleAttrWithMeta) {
			if (_multiSimpleAttrWithMeta != null) {
				this.multiSimpleAttrWithMeta.add(_multiSimpleAttrWithMeta.toBuilder());
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString _multiSimpleAttrWithMeta, int _idx) {
			getIndex(this.multiSimpleAttrWithMeta, _idx, () -> _multiSimpleAttrWithMeta.toBuilder());
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String _multiSimpleAttrWithMeta) {
			this.getOrCreateMultiSimpleAttrWithMeta(-1).setValue(_multiSimpleAttrWithMeta);
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String _multiSimpleAttrWithMeta, int _idx) {
			this.getOrCreateMultiSimpleAttrWithMeta(_idx).setValue(_multiSimpleAttrWithMeta);
			return this;
		}
		
		@Override 
		public Pojo.PojoBuilder addMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMetas) {
			if (multiSimpleAttrWithMetas != null) {
				for (final FieldWithMetaString toAdd : multiSimpleAttrWithMetas) {
					this.multiSimpleAttrWithMeta.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@Override 
		@RuneAttribute("multiSimpleAttrWithMeta")
		public Pojo.PojoBuilder setMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMetas) {
			if (multiSimpleAttrWithMetas == null) {
				this.multiSimpleAttrWithMeta = new ArrayList<>();
			} else {
				this.multiSimpleAttrWithMeta = multiSimpleAttrWithMetas.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMetas) {
			if (multiSimpleAttrWithMetas != null) {
				for (final String toAdd : multiSimpleAttrWithMetas) {
					this.addMultiSimpleAttrWithMetaValue(toAdd);
				}
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder setMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMetas) {
			this.multiSimpleAttrWithMeta.clear();
			if (multiSimpleAttrWithMetas != null) {
				multiSimpleAttrWithMetas.forEach(this::addMultiSimpleAttrWithMetaValue);
			}
			return this;
		}
		
		@Override
		@RosettaAttribute("simpleAttrWithId")
		@RuneAttribute("simpleAttrWithId")
		public Pojo.PojoBuilder setSimpleAttrWithId(FieldWithMetaString _simpleAttrWithId) {
			this.simpleAttrWithId = _simpleAttrWithId == null ? null : _simpleAttrWithId.toBuilder();
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder setSimpleAttrWithIdValue(String _simpleAttrWithId) {
			this.getOrCreateSimpleAttrWithId().setValue(_simpleAttrWithId);
			return this;
		}
		
		@Override
		@RosettaAttribute("multiSimpleAttrWithId")
		@RuneAttribute("multiSimpleAttrWithId")
		public Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString _multiSimpleAttrWithId) {
			if (_multiSimpleAttrWithId != null) {
				this.multiSimpleAttrWithId.add(_multiSimpleAttrWithId.toBuilder());
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString _multiSimpleAttrWithId, int _idx) {
			getIndex(this.multiSimpleAttrWithId, _idx, () -> _multiSimpleAttrWithId.toBuilder());
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String _multiSimpleAttrWithId) {
			this.getOrCreateMultiSimpleAttrWithId(-1).setValue(_multiSimpleAttrWithId);
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String _multiSimpleAttrWithId, int _idx) {
			this.getOrCreateMultiSimpleAttrWithId(_idx).setValue(_multiSimpleAttrWithId);
			return this;
		}
		
		@Override 
		public Pojo.PojoBuilder addMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithIds) {
			if (multiSimpleAttrWithIds != null) {
				for (final FieldWithMetaString toAdd : multiSimpleAttrWithIds) {
					this.multiSimpleAttrWithId.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@Override 
		@RuneAttribute("multiSimpleAttrWithId")
		public Pojo.PojoBuilder setMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithIds) {
			if (multiSimpleAttrWithIds == null) {
				this.multiSimpleAttrWithId = new ArrayList<>();
			} else {
				this.multiSimpleAttrWithId = multiSimpleAttrWithIds.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithIds) {
			if (multiSimpleAttrWithIds != null) {
				for (final String toAdd : multiSimpleAttrWithIds) {
					this.addMultiSimpleAttrWithIdValue(toAdd);
				}
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder setMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithIds) {
			this.multiSimpleAttrWithId.clear();
			if (multiSimpleAttrWithIds != null) {
				multiSimpleAttrWithIds.forEach(this::addMultiSimpleAttrWithIdValue);
			}
			return this;
		}
		
		@Override
		@RosettaAttribute("complexAttr")
		@RuneAttribute("complexAttr")
		public Pojo.PojoBuilder setComplexAttr(Foo _complexAttr) {
			this.complexAttr = _complexAttr == null ? null : _complexAttr.toBuilder();
			return this;
		}
		
		@Override
		@RosettaAttribute("multiComplexAttr")
		@RuneAttribute("multiComplexAttr")
		public Pojo.PojoBuilder addMultiComplexAttr(Foo _multiComplexAttr) {
			if (_multiComplexAttr != null) {
				this.multiComplexAttr.add(_multiComplexAttr.toBuilder());
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiComplexAttr(Foo _multiComplexAttr, int _idx) {
			getIndex(this.multiComplexAttr, _idx, () -> _multiComplexAttr.toBuilder());
			return this;
		}
		
		@Override 
		public Pojo.PojoBuilder addMultiComplexAttr(List<? extends Foo> multiComplexAttrs) {
			if (multiComplexAttrs != null) {
				for (final Foo toAdd : multiComplexAttrs) {
					this.multiComplexAttr.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@Override 
		@RuneAttribute("multiComplexAttr")
		public Pojo.PojoBuilder setMultiComplexAttr(List<? extends Foo> multiComplexAttrs) {
			if (multiComplexAttrs == null) {
				this.multiComplexAttr = new ArrayList<>();
			} else {
				this.multiComplexAttr = multiComplexAttrs.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		@RosettaAttribute("complexAttrWithRef")
		@RuneAttribute("complexAttrWithRef")
		public Pojo.PojoBuilder setComplexAttrWithRef(ReferenceWithMetaFoo _complexAttrWithRef) {
			this.complexAttrWithRef = _complexAttrWithRef == null ? null : _complexAttrWithRef.toBuilder();
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder setComplexAttrWithRefValue(Foo _complexAttrWithRef) {
			this.getOrCreateComplexAttrWithRef().setValue(_complexAttrWithRef);
			return this;
		}
		
		@Override
		@RosettaAttribute("multiComplexAttrWithRef")
		@RuneAttribute("multiComplexAttrWithRef")
		public Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo _multiComplexAttrWithRef) {
			if (_multiComplexAttrWithRef != null) {
				this.multiComplexAttrWithRef.add(_multiComplexAttrWithRef.toBuilder());
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo _multiComplexAttrWithRef, int _idx) {
			getIndex(this.multiComplexAttrWithRef, _idx, () -> _multiComplexAttrWithRef.toBuilder());
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo _multiComplexAttrWithRef) {
			this.getOrCreateMultiComplexAttrWithRef(-1).setValue(_multiComplexAttrWithRef.toBuilder());
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo _multiComplexAttrWithRef, int _idx) {
			this.getOrCreateMultiComplexAttrWithRef(_idx).setValue(_multiComplexAttrWithRef.toBuilder());
			return this;
		}
		
		@Override 
		public Pojo.PojoBuilder addMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRefs) {
			if (multiComplexAttrWithRefs != null) {
				for (final ReferenceWithMetaFoo toAdd : multiComplexAttrWithRefs) {
					this.multiComplexAttrWithRef.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@Override 
		@RuneAttribute("multiComplexAttrWithRef")
		public Pojo.PojoBuilder setMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRefs) {
			if (multiComplexAttrWithRefs == null) {
				this.multiComplexAttrWithRef = new ArrayList<>();
			} else {
				this.multiComplexAttrWithRef = multiComplexAttrWithRefs.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder addMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRefs) {
			if (multiComplexAttrWithRefs != null) {
				for (final Foo toAdd : multiComplexAttrWithRefs) {
					this.addMultiComplexAttrWithRefValue(toAdd);
				}
			}
			return this;
		}
		
		@Override
		public Pojo.PojoBuilder setMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRefs) {
			this.multiComplexAttrWithRef.clear();
			if (multiComplexAttrWithRefs != null) {
				multiComplexAttrWithRefs.forEach(this::addMultiComplexAttrWithRefValue);
			}
			return this;
		}
		
		@Override
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
		public Pojo.PojoBuilder setMeta(MetaFields _meta) {
			this.meta = _meta == null ? null : _meta.toBuilder();
			return this;
		}
		
		@Override
		public Pojo build() {
			return new Pojo.PojoImpl(this);
		}
		
		@Override
		public Pojo.PojoBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Pojo.PojoBuilder prune() {
			if (simpleAttrWithMeta!=null && !simpleAttrWithMeta.prune().hasData()) simpleAttrWithMeta = null;
			multiSimpleAttrWithMeta = multiSimpleAttrWithMeta.stream().filter(b->b!=null).<FieldWithMetaString.FieldWithMetaStringBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (simpleAttrWithId!=null && !simpleAttrWithId.prune().hasData()) simpleAttrWithId = null;
			multiSimpleAttrWithId = multiSimpleAttrWithId.stream().filter(b->b!=null).<FieldWithMetaString.FieldWithMetaStringBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (complexAttr!=null && !complexAttr.prune().hasData()) complexAttr = null;
			multiComplexAttr = multiComplexAttr.stream().filter(b->b!=null).<Foo.FooBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (complexAttrWithRef!=null && !complexAttrWithRef.prune().hasData()) complexAttrWithRef = null;
			multiComplexAttrWithRef = multiComplexAttrWithRef.stream().filter(b->b!=null).<ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (meta!=null && !meta.prune().hasData()) meta = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getSimpleAttr()!=null) return true;
			if (getMultiSimpleAttr()!=null && !getMultiSimpleAttr().isEmpty()) return true;
			if (getSimpleAttrWithMeta()!=null) return true;
			if (getMultiSimpleAttrWithMeta()!=null && !getMultiSimpleAttrWithMeta().isEmpty()) return true;
			if (getSimpleAttrWithId()!=null) return true;
			if (getMultiSimpleAttrWithId()!=null && !getMultiSimpleAttrWithId().isEmpty()) return true;
			if (getComplexAttr()!=null && getComplexAttr().hasData()) return true;
			if (getMultiComplexAttr()!=null && getMultiComplexAttr().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
			if (getComplexAttrWithRef()!=null && getComplexAttrWithRef().hasData()) return true;
			if (getMultiComplexAttrWithRef()!=null && getMultiComplexAttrWithRef().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Pojo.PojoBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Pojo.PojoBuilder o = (Pojo.PojoBuilder) other;
			
			merger.mergeRosetta(getSimpleAttrWithMeta(), o.getSimpleAttrWithMeta(), this::setSimpleAttrWithMeta);
			merger.mergeRosetta(getMultiSimpleAttrWithMeta(), o.getMultiSimpleAttrWithMeta(), this::getOrCreateMultiSimpleAttrWithMeta);
			merger.mergeRosetta(getSimpleAttrWithId(), o.getSimpleAttrWithId(), this::setSimpleAttrWithId);
			merger.mergeRosetta(getMultiSimpleAttrWithId(), o.getMultiSimpleAttrWithId(), this::getOrCreateMultiSimpleAttrWithId);
			merger.mergeRosetta(getComplexAttr(), o.getComplexAttr(), this::setComplexAttr);
			merger.mergeRosetta(getMultiComplexAttr(), o.getMultiComplexAttr(), this::getOrCreateMultiComplexAttr);
			merger.mergeRosetta(getComplexAttrWithRef(), o.getComplexAttrWithRef(), this::setComplexAttrWithRef);
			merger.mergeRosetta(getMultiComplexAttrWithRef(), o.getMultiComplexAttrWithRef(), this::getOrCreateMultiComplexAttrWithRef);
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getSimpleAttr(), o.getSimpleAttr(), this::setSimpleAttr);
			merger.mergeBasic(getMultiSimpleAttr(), o.getMultiSimpleAttr(), (Consumer<String>) this::addMultiSimpleAttr);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Pojo _that = getType().cast(o);
		
			if (!Objects.equals(simpleAttr, _that.getSimpleAttr())) return false;
			if (!ListEquals.listEquals(multiSimpleAttr, _that.getMultiSimpleAttr())) return false;
			if (!Objects.equals(simpleAttrWithMeta, _that.getSimpleAttrWithMeta())) return false;
			if (!ListEquals.listEquals(multiSimpleAttrWithMeta, _that.getMultiSimpleAttrWithMeta())) return false;
			if (!Objects.equals(simpleAttrWithId, _that.getSimpleAttrWithId())) return false;
			if (!ListEquals.listEquals(multiSimpleAttrWithId, _that.getMultiSimpleAttrWithId())) return false;
			if (!Objects.equals(complexAttr, _that.getComplexAttr())) return false;
			if (!ListEquals.listEquals(multiComplexAttr, _that.getMultiComplexAttr())) return false;
			if (!Objects.equals(complexAttrWithRef, _that.getComplexAttrWithRef())) return false;
			if (!ListEquals.listEquals(multiComplexAttrWithRef, _that.getMultiComplexAttrWithRef())) return false;
			if (!Objects.equals(meta, _that.getMeta())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (simpleAttr != null ? simpleAttr.hashCode() : 0);
			_result = 31 * _result + (multiSimpleAttr != null ? multiSimpleAttr.hashCode() : 0);
			_result = 31 * _result + (simpleAttrWithMeta != null ? simpleAttrWithMeta.hashCode() : 0);
			_result = 31 * _result + (multiSimpleAttrWithMeta != null ? multiSimpleAttrWithMeta.hashCode() : 0);
			_result = 31 * _result + (simpleAttrWithId != null ? simpleAttrWithId.hashCode() : 0);
			_result = 31 * _result + (multiSimpleAttrWithId != null ? multiSimpleAttrWithId.hashCode() : 0);
			_result = 31 * _result + (complexAttr != null ? complexAttr.hashCode() : 0);
			_result = 31 * _result + (multiComplexAttr != null ? multiComplexAttr.hashCode() : 0);
			_result = 31 * _result + (complexAttrWithRef != null ? complexAttrWithRef.hashCode() : 0);
			_result = 31 * _result + (multiComplexAttrWithRef != null ? multiComplexAttrWithRef.hashCode() : 0);
			_result = 31 * _result + (meta != null ? meta.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "PojoBuilder {" +
				"simpleAttr=" + this.simpleAttr + ", " +
				"multiSimpleAttr=" + this.multiSimpleAttr + ", " +
				"simpleAttrWithMeta=" + this.simpleAttrWithMeta + ", " +
				"multiSimpleAttrWithMeta=" + this.multiSimpleAttrWithMeta + ", " +
				"simpleAttrWithId=" + this.simpleAttrWithId + ", " +
				"multiSimpleAttrWithId=" + this.multiSimpleAttrWithId + ", " +
				"complexAttr=" + this.complexAttr + ", " +
				"multiComplexAttr=" + this.multiComplexAttr + ", " +
				"complexAttrWithRef=" + this.complexAttrWithRef + ", " +
				"multiComplexAttrWithRef=" + this.multiComplexAttrWithRef + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}
