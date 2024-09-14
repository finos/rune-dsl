package com.regnosys.rosetta.generator.java.object

import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import java.util.Map
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

/**
 * This test is meant to prevent accidental changes to the generated code
 * of Rosetta Model Objects. If any unwanted change accidentally gets
 * through, please add to this test so it does not happen again.
 */
@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
@TestInstance(Lifecycle.PER_CLASS)
class PojoRegressionTest {
	@Inject extension CodeGeneratorTestHelper
	
	Map<String, String> code
	
	@BeforeAll
	def void setup() {
		code = '''
			type Pojo:
				[metadata key]
				simpleAttr string (1..1)
				multiSimpleAttr string (0..*)
				
				simpleAttrWithMeta string (1..1)
					[metadata scheme]
				multiSimpleAttrWithMeta string (0..*)
					[metadata scheme]
				
				simpleAttrWithId string (1..1)
					[metadata id]
				multiSimpleAttrWithId string (0..*)
					[metadata id]
				
				complexAttr Foo (1..1)
				multiComplexAttr Foo (0..*)
				
				complexAttrWithRef Foo (1..1)
					[metadata reference]
				multiComplexAttrWithRef Foo (0..*)
					[metadata reference]
			
			type Foo:
				[metadata key]
		'''.generateCode
	}
	
	private def void assertGeneratedCode(String className, String expected) {
		assertEquals(expected, code.get(className))
	}
	
	@Test
	def void testCodeCompiles() {
		code.compileToClasses
	}

	@Test
	def void testPojoCode() {
		assertGeneratedCode('com.rosetta.test.model.Pojo', '''
		package com.rosetta.test.model;
		
		import com.google.common.collect.ImmutableList;
		import com.rosetta.model.lib.GlobalKey;
		import com.rosetta.model.lib.GlobalKey.GlobalKeyBuilder;
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.AttributeMeta;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.model.metafields.FieldWithMetaString;
		import com.rosetta.model.metafields.FieldWithMetaString.FieldWithMetaStringBuilder;
		import com.rosetta.model.metafields.MetaFields;
		import com.rosetta.test.model.Foo;
		import com.rosetta.test.model.Pojo;
		import com.rosetta.test.model.Pojo.PojoBuilder;
		import com.rosetta.test.model.Pojo.PojoBuilderImpl;
		import com.rosetta.test.model.Pojo.PojoImpl;
		import com.rosetta.test.model.meta.PojoMeta;
		import com.rosetta.test.model.metafields.ReferenceWithMetaFoo;
		import com.rosetta.test.model.metafields.ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder;
		import com.rosetta.util.ListEquals;
		import java.util.ArrayList;
		import java.util.List;
		import java.util.Objects;
		import java.util.function.Consumer;
		import java.util.stream.Collectors;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version test
		 */
		@RosettaDataType(value="Pojo", builder=Pojo.PojoBuilderImpl.class, version="test")
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
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface PojoBuilder extends Pojo, RosettaModelObjectBuilder {
				FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateSimpleAttrWithMeta();
				FieldWithMetaString.FieldWithMetaStringBuilder getSimpleAttrWithMeta();
				FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiSimpleAttrWithMeta(int _index);
				List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithMeta();
				FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateSimpleAttrWithId();
				FieldWithMetaString.FieldWithMetaStringBuilder getSimpleAttrWithId();
				FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMultiSimpleAttrWithId(int _index);
				List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithId();
				Foo.FooBuilder getOrCreateComplexAttr();
				Foo.FooBuilder getComplexAttr();
				Foo.FooBuilder getOrCreateMultiComplexAttr(int _index);
				List<? extends Foo.FooBuilder> getMultiComplexAttr();
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getOrCreateComplexAttrWithRef();
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getComplexAttrWithRef();
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder getOrCreateMultiComplexAttrWithRef(int _index);
				List<? extends ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder> getMultiComplexAttrWithRef();
				MetaFields.MetaFieldsBuilder getOrCreateMeta();
				MetaFields.MetaFieldsBuilder getMeta();
				Pojo.PojoBuilder setSimpleAttr(String simpleAttr);
				Pojo.PojoBuilder addMultiSimpleAttr(String multiSimpleAttr0);
				Pojo.PojoBuilder addMultiSimpleAttr(String multiSimpleAttr1, int _idx);
				Pojo.PojoBuilder addMultiSimpleAttr(List<String> multiSimpleAttr2);
				Pojo.PojoBuilder setMultiSimpleAttr(List<String> multiSimpleAttr3);
				Pojo.PojoBuilder setSimpleAttrWithMeta(FieldWithMetaString simpleAttrWithMeta0);
				Pojo.PojoBuilder setSimpleAttrWithMetaValue(String simpleAttrWithMeta1);
				Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString multiSimpleAttrWithMeta0);
				Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString multiSimpleAttrWithMeta1, int _idx);
				Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String multiSimpleAttrWithMeta2);
				Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String multiSimpleAttrWithMeta3, int _idx);
				Pojo.PojoBuilder addMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMeta4);
				Pojo.PojoBuilder setMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMeta5);
				Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMeta6);
				Pojo.PojoBuilder setMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMeta7);
				Pojo.PojoBuilder setSimpleAttrWithId(FieldWithMetaString simpleAttrWithId0);
				Pojo.PojoBuilder setSimpleAttrWithIdValue(String simpleAttrWithId1);
				Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString multiSimpleAttrWithId0);
				Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString multiSimpleAttrWithId1, int _idx);
				Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String multiSimpleAttrWithId2);
				Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String multiSimpleAttrWithId3, int _idx);
				Pojo.PojoBuilder addMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithId4);
				Pojo.PojoBuilder setMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithId5);
				Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithId6);
				Pojo.PojoBuilder setMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithId7);
				Pojo.PojoBuilder setComplexAttr(Foo complexAttr);
				Pojo.PojoBuilder addMultiComplexAttr(Foo multiComplexAttr0);
				Pojo.PojoBuilder addMultiComplexAttr(Foo multiComplexAttr1, int _idx);
				Pojo.PojoBuilder addMultiComplexAttr(List<? extends Foo> multiComplexAttr2);
				Pojo.PojoBuilder setMultiComplexAttr(List<? extends Foo> multiComplexAttr3);
				Pojo.PojoBuilder setComplexAttrWithRef(ReferenceWithMetaFoo complexAttrWithRef0);
				Pojo.PojoBuilder setComplexAttrWithRefValue(Foo complexAttrWithRef1);
				Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo multiComplexAttrWithRef0);
				Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo multiComplexAttrWithRef1, int _idx);
				Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo multiComplexAttrWithRef2);
				Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo multiComplexAttrWithRef3, int _idx);
				Pojo.PojoBuilder addMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRef4);
				Pojo.PojoBuilder setMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRef5);
				Pojo.PojoBuilder addMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRef6);
				Pojo.PojoBuilder setMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRef7);
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
				public String getSimpleAttr() {
					return simpleAttr;
				}
				
				@Override
				@RosettaAttribute("multiSimpleAttr")
				public List<String> getMultiSimpleAttr() {
					return multiSimpleAttr;
				}
				
				@Override
				@RosettaAttribute("simpleAttrWithMeta")
				public FieldWithMetaString getSimpleAttrWithMeta() {
					return simpleAttrWithMeta;
				}
				
				@Override
				@RosettaAttribute("multiSimpleAttrWithMeta")
				public List<? extends FieldWithMetaString> getMultiSimpleAttrWithMeta() {
					return multiSimpleAttrWithMeta;
				}
				
				@Override
				@RosettaAttribute("simpleAttrWithId")
				public FieldWithMetaString getSimpleAttrWithId() {
					return simpleAttrWithId;
				}
				
				@Override
				@RosettaAttribute("multiSimpleAttrWithId")
				public List<? extends FieldWithMetaString> getMultiSimpleAttrWithId() {
					return multiSimpleAttrWithId;
				}
				
				@Override
				@RosettaAttribute("complexAttr")
				public Foo getComplexAttr() {
					return complexAttr;
				}
				
				@Override
				@RosettaAttribute("multiComplexAttr")
				public List<? extends Foo> getMultiComplexAttr() {
					return multiComplexAttr;
				}
				
				@Override
				@RosettaAttribute("complexAttrWithRef")
				public ReferenceWithMetaFoo getComplexAttrWithRef() {
					return complexAttrWithRef;
				}
				
				@Override
				@RosettaAttribute("multiComplexAttrWithRef")
				public List<? extends ReferenceWithMetaFoo> getMultiComplexAttrWithRef() {
					return multiComplexAttrWithRef;
				}
				
				@Override
				@RosettaAttribute("meta")
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
			class PojoBuilderImpl implements Pojo.PojoBuilder, GlobalKeyBuilder {
			
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
			
				public PojoBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("simpleAttr")
				public String getSimpleAttr() {
					return simpleAttr;
				}
				
				@Override
				@RosettaAttribute("multiSimpleAttr")
				public List<String> getMultiSimpleAttr() {
					return multiSimpleAttr;
				}
				
				@Override
				@RosettaAttribute("simpleAttrWithMeta")
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
				public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithMeta() {
					return multiSimpleAttrWithMeta;
				}
				
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
				public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMultiSimpleAttrWithId() {
					return multiSimpleAttrWithId;
				}
				
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
				public List<? extends Foo.FooBuilder> getMultiComplexAttr() {
					return multiComplexAttr;
				}
				
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
				public List<? extends ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder> getMultiComplexAttrWithRef() {
					return multiComplexAttrWithRef;
				}
				
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
				public Pojo.PojoBuilder setSimpleAttr(String simpleAttr) {
					this.simpleAttr = simpleAttr==null?null:simpleAttr;
					return this;
				}
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttr(String multiSimpleAttr) {
					if (multiSimpleAttr!=null) this.multiSimpleAttr.add(multiSimpleAttr);
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttr(String multiSimpleAttr, int _idx) {
					getIndex(this.multiSimpleAttr, _idx, () -> multiSimpleAttr);
					return this;
				}
				@Override 
				public Pojo.PojoBuilder addMultiSimpleAttr(List<String> multiSimpleAttrs) {
					if (multiSimpleAttrs != null) {
						for (String toAdd : multiSimpleAttrs) {
							this.multiSimpleAttr.add(toAdd);
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("multiSimpleAttr")
				public Pojo.PojoBuilder setMultiSimpleAttr(List<String> multiSimpleAttrs) {
					if (multiSimpleAttrs == null)  {
						this.multiSimpleAttr = new ArrayList<>();
					}
					else {
						this.multiSimpleAttr = multiSimpleAttrs.stream()
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				@Override
				@RosettaAttribute("simpleAttrWithMeta")
				public Pojo.PojoBuilder setSimpleAttrWithMeta(FieldWithMetaString simpleAttrWithMeta) {
					this.simpleAttrWithMeta = simpleAttrWithMeta==null?null:simpleAttrWithMeta.toBuilder();
					return this;
				}
				@Override
				public Pojo.PojoBuilder setSimpleAttrWithMetaValue(String simpleAttrWithMeta) {
					this.getOrCreateSimpleAttrWithMeta().setValue(simpleAttrWithMeta);
					return this;
				}
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString multiSimpleAttrWithMeta) {
					if (multiSimpleAttrWithMeta!=null) this.multiSimpleAttrWithMeta.add(multiSimpleAttrWithMeta.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithMeta(FieldWithMetaString multiSimpleAttrWithMeta, int _idx) {
					getIndex(this.multiSimpleAttrWithMeta, _idx, () -> multiSimpleAttrWithMeta.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String multiSimpleAttrWithMeta) {
					this.getOrCreateMultiSimpleAttrWithMeta(-1).setValue(multiSimpleAttrWithMeta);
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(String multiSimpleAttrWithMeta, int _idx) {
					this.getOrCreateMultiSimpleAttrWithMeta(_idx).setValue(multiSimpleAttrWithMeta);
					return this;
				}
				@Override 
				public Pojo.PojoBuilder addMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMetas) {
					if (multiSimpleAttrWithMetas != null) {
						for (FieldWithMetaString toAdd : multiSimpleAttrWithMetas) {
							this.multiSimpleAttrWithMeta.add(toAdd.toBuilder());
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("multiSimpleAttrWithMeta")
				public Pojo.PojoBuilder setMultiSimpleAttrWithMeta(List<? extends FieldWithMetaString> multiSimpleAttrWithMetas) {
					if (multiSimpleAttrWithMetas == null)  {
						this.multiSimpleAttrWithMeta = new ArrayList<>();
					}
					else {
						this.multiSimpleAttrWithMeta = multiSimpleAttrWithMetas.stream()
							.map(_a->_a.toBuilder())
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMetas) {
					if (multiSimpleAttrWithMetas != null) {
						for (String toAdd : multiSimpleAttrWithMetas) {
							this.addMultiSimpleAttrWithMetaValue(toAdd);
						}
					}
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder setMultiSimpleAttrWithMetaValue(List<? extends String> multiSimpleAttrWithMetas) {
					this.multiSimpleAttrWithMeta.clear();
					if (multiSimpleAttrWithMetas!=null) {
						multiSimpleAttrWithMetas.forEach(this::addMultiSimpleAttrWithMetaValue);
					}
					return this;
				}
				
				@Override
				@RosettaAttribute("simpleAttrWithId")
				public Pojo.PojoBuilder setSimpleAttrWithId(FieldWithMetaString simpleAttrWithId) {
					this.simpleAttrWithId = simpleAttrWithId==null?null:simpleAttrWithId.toBuilder();
					return this;
				}
				@Override
				public Pojo.PojoBuilder setSimpleAttrWithIdValue(String simpleAttrWithId) {
					this.getOrCreateSimpleAttrWithId().setValue(simpleAttrWithId);
					return this;
				}
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString multiSimpleAttrWithId) {
					if (multiSimpleAttrWithId!=null) this.multiSimpleAttrWithId.add(multiSimpleAttrWithId.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithId(FieldWithMetaString multiSimpleAttrWithId, int _idx) {
					getIndex(this.multiSimpleAttrWithId, _idx, () -> multiSimpleAttrWithId.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String multiSimpleAttrWithId) {
					this.getOrCreateMultiSimpleAttrWithId(-1).setValue(multiSimpleAttrWithId);
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(String multiSimpleAttrWithId, int _idx) {
					this.getOrCreateMultiSimpleAttrWithId(_idx).setValue(multiSimpleAttrWithId);
					return this;
				}
				@Override 
				public Pojo.PojoBuilder addMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithIds) {
					if (multiSimpleAttrWithIds != null) {
						for (FieldWithMetaString toAdd : multiSimpleAttrWithIds) {
							this.multiSimpleAttrWithId.add(toAdd.toBuilder());
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("multiSimpleAttrWithId")
				public Pojo.PojoBuilder setMultiSimpleAttrWithId(List<? extends FieldWithMetaString> multiSimpleAttrWithIds) {
					if (multiSimpleAttrWithIds == null)  {
						this.multiSimpleAttrWithId = new ArrayList<>();
					}
					else {
						this.multiSimpleAttrWithId = multiSimpleAttrWithIds.stream()
							.map(_a->_a.toBuilder())
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithIds) {
					if (multiSimpleAttrWithIds != null) {
						for (String toAdd : multiSimpleAttrWithIds) {
							this.addMultiSimpleAttrWithIdValue(toAdd);
						}
					}
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder setMultiSimpleAttrWithIdValue(List<? extends String> multiSimpleAttrWithIds) {
					this.multiSimpleAttrWithId.clear();
					if (multiSimpleAttrWithIds!=null) {
						multiSimpleAttrWithIds.forEach(this::addMultiSimpleAttrWithIdValue);
					}
					return this;
				}
				
				@Override
				@RosettaAttribute("complexAttr")
				public Pojo.PojoBuilder setComplexAttr(Foo complexAttr) {
					this.complexAttr = complexAttr==null?null:complexAttr.toBuilder();
					return this;
				}
				@Override
				public Pojo.PojoBuilder addMultiComplexAttr(Foo multiComplexAttr) {
					if (multiComplexAttr!=null) this.multiComplexAttr.add(multiComplexAttr.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiComplexAttr(Foo multiComplexAttr, int _idx) {
					getIndex(this.multiComplexAttr, _idx, () -> multiComplexAttr.toBuilder());
					return this;
				}
				@Override 
				public Pojo.PojoBuilder addMultiComplexAttr(List<? extends Foo> multiComplexAttrs) {
					if (multiComplexAttrs != null) {
						for (Foo toAdd : multiComplexAttrs) {
							this.multiComplexAttr.add(toAdd.toBuilder());
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("multiComplexAttr")
				public Pojo.PojoBuilder setMultiComplexAttr(List<? extends Foo> multiComplexAttrs) {
					if (multiComplexAttrs == null)  {
						this.multiComplexAttr = new ArrayList<>();
					}
					else {
						this.multiComplexAttr = multiComplexAttrs.stream()
							.map(_a->_a.toBuilder())
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				@Override
				@RosettaAttribute("complexAttrWithRef")
				public Pojo.PojoBuilder setComplexAttrWithRef(ReferenceWithMetaFoo complexAttrWithRef) {
					this.complexAttrWithRef = complexAttrWithRef==null?null:complexAttrWithRef.toBuilder();
					return this;
				}
				@Override
				public Pojo.PojoBuilder setComplexAttrWithRefValue(Foo complexAttrWithRef) {
					this.getOrCreateComplexAttrWithRef().setValue(complexAttrWithRef);
					return this;
				}
				@Override
				public Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo multiComplexAttrWithRef) {
					if (multiComplexAttrWithRef!=null) this.multiComplexAttrWithRef.add(multiComplexAttrWithRef.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiComplexAttrWithRef(ReferenceWithMetaFoo multiComplexAttrWithRef, int _idx) {
					getIndex(this.multiComplexAttrWithRef, _idx, () -> multiComplexAttrWithRef.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo multiComplexAttrWithRef) {
					this.getOrCreateMultiComplexAttrWithRef(-1).setValue(multiComplexAttrWithRef.toBuilder());
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiComplexAttrWithRefValue(Foo multiComplexAttrWithRef, int _idx) {
					this.getOrCreateMultiComplexAttrWithRef(_idx).setValue(multiComplexAttrWithRef.toBuilder());
					return this;
				}
				@Override 
				public Pojo.PojoBuilder addMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRefs) {
					if (multiComplexAttrWithRefs != null) {
						for (ReferenceWithMetaFoo toAdd : multiComplexAttrWithRefs) {
							this.multiComplexAttrWithRef.add(toAdd.toBuilder());
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("multiComplexAttrWithRef")
				public Pojo.PojoBuilder setMultiComplexAttrWithRef(List<? extends ReferenceWithMetaFoo> multiComplexAttrWithRefs) {
					if (multiComplexAttrWithRefs == null)  {
						this.multiComplexAttrWithRef = new ArrayList<>();
					}
					else {
						this.multiComplexAttrWithRef = multiComplexAttrWithRefs.stream()
							.map(_a->_a.toBuilder())
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder addMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRefs) {
					if (multiComplexAttrWithRefs != null) {
						for (Foo toAdd : multiComplexAttrWithRefs) {
							this.addMultiComplexAttrWithRefValue(toAdd);
						}
					}
					return this;
				}
				
				@Override
				public Pojo.PojoBuilder setMultiComplexAttrWithRefValue(List<? extends Foo> multiComplexAttrWithRefs) {
					this.multiComplexAttrWithRef.clear();
					if (multiComplexAttrWithRefs!=null) {
						multiComplexAttrWithRefs.forEach(this::addMultiComplexAttrWithRefValue);
					}
					return this;
				}
				
				@Override
				@RosettaAttribute("meta")
				public Pojo.PojoBuilder setMeta(MetaFields meta) {
					this.meta = meta==null?null:meta.toBuilder();
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
		''')
	}
	
	@Test
	def testFieldWithMetaStringCode() {
		assertGeneratedCode('com.rosetta.model.metafields.FieldWithMetaString', '''
		package com.rosetta.model.metafields;
		
		import com.rosetta.model.lib.GlobalKey;
		import com.rosetta.model.lib.GlobalKey.GlobalKeyBuilder;
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.FieldWithMeta;
		import com.rosetta.model.lib.meta.FieldWithMeta.FieldWithMetaBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version 1
		 */
		@RosettaDataType(value="FieldWithMetaString", builder=FieldWithMetaString.FieldWithMetaStringBuilderImpl.class, version="0.0.0")
		public interface FieldWithMetaString extends RosettaModelObject, FieldWithMeta<String>, GlobalKey {
		
			FieldWithMetaStringMeta metaData = new FieldWithMetaStringMeta();
		
			/*********************** Getter Methods  ***********************/
			String getValue();
			MetaFields getMeta();
		
			/*********************** Build Methods  ***********************/
			FieldWithMetaString build();
			
			FieldWithMetaString.FieldWithMetaStringBuilder toBuilder();
			
			static FieldWithMetaString.FieldWithMetaStringBuilder builder() {
				return new FieldWithMetaString.FieldWithMetaStringBuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends FieldWithMetaString> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends FieldWithMetaString> getType() {
				return FieldWithMetaString.class;
			}
			
			@Override
			default Class<String> getValueType() {
				return String.class;
			}
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
				processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface FieldWithMetaStringBuilder extends FieldWithMetaString, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<String> {
				MetaFields.MetaFieldsBuilder getOrCreateMeta();
				MetaFields.MetaFieldsBuilder getMeta();
				FieldWithMetaString.FieldWithMetaStringBuilder setValue(String value);
				FieldWithMetaString.FieldWithMetaStringBuilder setMeta(MetaFields meta);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
					processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
				}
				
		
				FieldWithMetaString.FieldWithMetaStringBuilder prune();
			}
		
			/*********************** Immutable Implementation of FieldWithMetaString  ***********************/
			class FieldWithMetaStringImpl implements FieldWithMetaString {
				private final String value;
				private final MetaFields meta;
				
				protected FieldWithMetaStringImpl(FieldWithMetaString.FieldWithMetaStringBuilder builder) {
					this.value = builder.getValue();
					this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
				}
				
				@Override
				@RosettaAttribute("value")
				public String getValue() {
					return value;
				}
				
				@Override
				@RosettaAttribute("meta")
				public MetaFields getMeta() {
					return meta;
				}
				
				@Override
				public FieldWithMetaString build() {
					return this;
				}
				
				@Override
				public FieldWithMetaString.FieldWithMetaStringBuilder toBuilder() {
					FieldWithMetaString.FieldWithMetaStringBuilder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(FieldWithMetaString.FieldWithMetaStringBuilder builder) {
					ofNullable(getValue()).ifPresent(builder::setValue);
					ofNullable(getMeta()).ifPresent(builder::setMeta);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					FieldWithMetaString _that = getType().cast(o);
				
					if (!Objects.equals(value, _that.getValue())) return false;
					if (!Objects.equals(meta, _that.getMeta())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (value != null ? value.hashCode() : 0);
					_result = 31 * _result + (meta != null ? meta.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "FieldWithMetaString {" +
						"value=" + this.value + ", " +
						"meta=" + this.meta +
					'}';
				}
			}
		
			/*********************** Builder Implementation of FieldWithMetaString  ***********************/
			class FieldWithMetaStringBuilderImpl implements FieldWithMetaString.FieldWithMetaStringBuilder {
			
				protected String value;
				protected MetaFields.MetaFieldsBuilder meta;
			
				public FieldWithMetaStringBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("value")
				public String getValue() {
					return value;
				}
				
				@Override
				@RosettaAttribute("meta")
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
				@RosettaAttribute("value")
				public FieldWithMetaString.FieldWithMetaStringBuilder setValue(String value) {
					this.value = value==null?null:value;
					return this;
				}
				@Override
				@RosettaAttribute("meta")
				public FieldWithMetaString.FieldWithMetaStringBuilder setMeta(MetaFields meta) {
					this.meta = meta==null?null:meta.toBuilder();
					return this;
				}
				
				@Override
				public FieldWithMetaString build() {
					return new FieldWithMetaString.FieldWithMetaStringImpl(this);
				}
				
				@Override
				public FieldWithMetaString.FieldWithMetaStringBuilder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public FieldWithMetaString.FieldWithMetaStringBuilder prune() {
					if (meta!=null && !meta.prune().hasData()) meta = null;
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getValue()!=null) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public FieldWithMetaString.FieldWithMetaStringBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					FieldWithMetaString.FieldWithMetaStringBuilder o = (FieldWithMetaString.FieldWithMetaStringBuilder) other;
					
					merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
					
					merger.mergeBasic(getValue(), o.getValue(), this::setValue);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					FieldWithMetaString _that = getType().cast(o);
				
					if (!Objects.equals(value, _that.getValue())) return false;
					if (!Objects.equals(meta, _that.getMeta())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (value != null ? value.hashCode() : 0);
					_result = 31 * _result + (meta != null ? meta.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "FieldWithMetaStringBuilder {" +
						"value=" + this.value + ", " +
						"meta=" + this.meta +
					'}';
				}
			}
		}
		
		class FieldWithMetaStringMeta extends BasicRosettaMetaData<FieldWithMetaString>{
		
		}
		''')
	}
	
	@Test
	def testReferenceWithMetaFooCode() {
		assertGeneratedCode('com.rosetta.test.model.metafields.ReferenceWithMetaFoo', '''
		package com.rosetta.test.model.metafields;
		
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.Reference;
		import com.rosetta.model.lib.meta.ReferenceWithMeta;
		import com.rosetta.model.lib.meta.ReferenceWithMeta.ReferenceWithMetaBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.AttributeMeta;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.test.model.Foo;
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version 1
		 */
		@RosettaDataType(value="ReferenceWithMetaFoo", builder=ReferenceWithMetaFoo.ReferenceWithMetaFooBuilderImpl.class, version="0.0.0")
		public interface ReferenceWithMetaFoo extends RosettaModelObject, ReferenceWithMeta<Foo> {
		
			ReferenceWithMetaFooMeta metaData = new ReferenceWithMetaFooMeta();
		
			/*********************** Getter Methods  ***********************/
			Foo getValue();
			String getGlobalReference();
			String getExternalReference();
			Reference getReference();
		
			/*********************** Build Methods  ***********************/
			ReferenceWithMetaFoo build();
			
			ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder toBuilder();
			
			static ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder builder() {
				return new ReferenceWithMetaFoo.ReferenceWithMetaFooBuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends ReferenceWithMetaFoo> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends ReferenceWithMetaFoo> getType() {
				return ReferenceWithMetaFoo.class;
			}
			
			@Override
			default Class<Foo> getValueType() {
				return Foo.class;
			}
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processRosetta(path.newSubPath("value"), processor, Foo.class, getValue());
				processor.processBasic(path.newSubPath("globalReference"), String.class, getGlobalReference(), this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("externalReference"), String.class, getExternalReference(), this, AttributeMeta.META);
				processRosetta(path.newSubPath("reference"), processor, Reference.class, getReference());
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface ReferenceWithMetaFooBuilder extends ReferenceWithMetaFoo, RosettaModelObjectBuilder, ReferenceWithMeta.ReferenceWithMetaBuilder<Foo> {
				Foo.FooBuilder getOrCreateValue();
				Foo.FooBuilder getValue();
				Reference.ReferenceBuilder getOrCreateReference();
				Reference.ReferenceBuilder getReference();
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setValue(Foo value);
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setGlobalReference(String globalReference);
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setExternalReference(String externalReference);
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setReference(Reference reference);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processRosetta(path.newSubPath("value"), processor, Foo.FooBuilder.class, getValue());
					processor.processBasic(path.newSubPath("globalReference"), String.class, getGlobalReference(), this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("externalReference"), String.class, getExternalReference(), this, AttributeMeta.META);
					processRosetta(path.newSubPath("reference"), processor, Reference.ReferenceBuilder.class, getReference());
				}
				
		
				ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder prune();
			}
		
			/*********************** Immutable Implementation of ReferenceWithMetaFoo  ***********************/
			class ReferenceWithMetaFooImpl implements ReferenceWithMetaFoo {
				private final Foo value;
				private final String globalReference;
				private final String externalReference;
				private final Reference reference;
				
				protected ReferenceWithMetaFooImpl(ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder builder) {
					this.value = ofNullable(builder.getValue()).map(f->f.build()).orElse(null);
					this.globalReference = builder.getGlobalReference();
					this.externalReference = builder.getExternalReference();
					this.reference = ofNullable(builder.getReference()).map(f->f.build()).orElse(null);
				}
				
				@Override
				@RosettaAttribute("value")
				public Foo getValue() {
					return value;
				}
				
				@Override
				@RosettaAttribute("globalReference")
				public String getGlobalReference() {
					return globalReference;
				}
				
				@Override
				@RosettaAttribute("externalReference")
				public String getExternalReference() {
					return externalReference;
				}
				
				@Override
				@RosettaAttribute("address")
				public Reference getReference() {
					return reference;
				}
				
				@Override
				public ReferenceWithMetaFoo build() {
					return this;
				}
				
				@Override
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder toBuilder() {
					ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder builder) {
					ofNullable(getValue()).ifPresent(builder::setValue);
					ofNullable(getGlobalReference()).ifPresent(builder::setGlobalReference);
					ofNullable(getExternalReference()).ifPresent(builder::setExternalReference);
					ofNullable(getReference()).ifPresent(builder::setReference);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					ReferenceWithMetaFoo _that = getType().cast(o);
				
					if (!Objects.equals(value, _that.getValue())) return false;
					if (!Objects.equals(globalReference, _that.getGlobalReference())) return false;
					if (!Objects.equals(externalReference, _that.getExternalReference())) return false;
					if (!Objects.equals(reference, _that.getReference())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (value != null ? value.hashCode() : 0);
					_result = 31 * _result + (globalReference != null ? globalReference.hashCode() : 0);
					_result = 31 * _result + (externalReference != null ? externalReference.hashCode() : 0);
					_result = 31 * _result + (reference != null ? reference.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "ReferenceWithMetaFoo {" +
						"value=" + this.value + ", " +
						"globalReference=" + this.globalReference + ", " +
						"externalReference=" + this.externalReference + ", " +
						"reference=" + this.reference +
					'}';
				}
			}
		
			/*********************** Builder Implementation of ReferenceWithMetaFoo  ***********************/
			class ReferenceWithMetaFooBuilderImpl implements ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder {
			
				protected Foo.FooBuilder value;
				protected String globalReference;
				protected String externalReference;
				protected Reference.ReferenceBuilder reference;
			
				public ReferenceWithMetaFooBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("value")
				public Foo.FooBuilder getValue() {
					return value;
				}
				
				@Override
				public Foo.FooBuilder getOrCreateValue() {
					Foo.FooBuilder result;
					if (value!=null) {
						result = value;
					}
					else {
						result = value = Foo.builder();
					}
					
					return result;
				}
				
				@Override
				@RosettaAttribute("globalReference")
				public String getGlobalReference() {
					return globalReference;
				}
				
				@Override
				@RosettaAttribute("externalReference")
				public String getExternalReference() {
					return externalReference;
				}
				
				@Override
				@RosettaAttribute("address")
				public Reference.ReferenceBuilder getReference() {
					return reference;
				}
				
				@Override
				public Reference.ReferenceBuilder getOrCreateReference() {
					Reference.ReferenceBuilder result;
					if (reference!=null) {
						result = reference;
					}
					else {
						result = reference = Reference.builder();
					}
					
					return result;
				}
				
				@Override
				@RosettaAttribute("value")
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setValue(Foo value) {
					this.value = value==null?null:value.toBuilder();
					return this;
				}
				@Override
				@RosettaAttribute("globalReference")
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setGlobalReference(String globalReference) {
					this.globalReference = globalReference==null?null:globalReference;
					return this;
				}
				@Override
				@RosettaAttribute("externalReference")
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setExternalReference(String externalReference) {
					this.externalReference = externalReference==null?null:externalReference;
					return this;
				}
				@Override
				@RosettaAttribute("address")
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder setReference(Reference reference) {
					this.reference = reference==null?null:reference.toBuilder();
					return this;
				}
				
				@Override
				public ReferenceWithMetaFoo build() {
					return new ReferenceWithMetaFoo.ReferenceWithMetaFooImpl(this);
				}
				
				@Override
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder prune() {
					if (value!=null && !value.prune().hasData()) value = null;
					if (reference!=null && !reference.prune().hasData()) reference = null;
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getValue()!=null && getValue().hasData()) return true;
					if (getGlobalReference()!=null) return true;
					if (getExternalReference()!=null) return true;
					if (getReference()!=null && getReference().hasData()) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder o = (ReferenceWithMetaFoo.ReferenceWithMetaFooBuilder) other;
					
					merger.mergeRosetta(getValue(), o.getValue(), this::setValue);
					merger.mergeRosetta(getReference(), o.getReference(), this::setReference);
					
					merger.mergeBasic(getGlobalReference(), o.getGlobalReference(), this::setGlobalReference);
					merger.mergeBasic(getExternalReference(), o.getExternalReference(), this::setExternalReference);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					ReferenceWithMetaFoo _that = getType().cast(o);
				
					if (!Objects.equals(value, _that.getValue())) return false;
					if (!Objects.equals(globalReference, _that.getGlobalReference())) return false;
					if (!Objects.equals(externalReference, _that.getExternalReference())) return false;
					if (!Objects.equals(reference, _that.getReference())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (value != null ? value.hashCode() : 0);
					_result = 31 * _result + (globalReference != null ? globalReference.hashCode() : 0);
					_result = 31 * _result + (externalReference != null ? externalReference.hashCode() : 0);
					_result = 31 * _result + (reference != null ? reference.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "ReferenceWithMetaFooBuilder {" +
						"value=" + this.value + ", " +
						"globalReference=" + this.globalReference + ", " +
						"externalReference=" + this.externalReference + ", " +
						"reference=" + this.reference +
					'}';
				}
			}
		}
		
		class ReferenceWithMetaFooMeta extends BasicRosettaMetaData<ReferenceWithMetaFoo>{
		
		}
		''')
	}
	
	@Test
	def testMetaFieldsCode() {
		assertGeneratedCode('com.rosetta.model.metafields.MetaFields', '''
		package com.rosetta.model.metafields;
		
		import com.google.common.collect.ImmutableList;
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.GlobalKeyFields;
		import com.rosetta.model.lib.meta.GlobalKeyFields.GlobalKeyFieldsBuilder;
		import com.rosetta.model.lib.meta.Key;
		import com.rosetta.model.lib.meta.MetaDataFields;
		import com.rosetta.model.lib.meta.MetaDataFields.MetaDataFieldsBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.AttributeMeta;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.util.ListEquals;
		import java.util.ArrayList;
		import java.util.List;
		import java.util.Objects;
		import java.util.stream.Collectors;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version 1
		 */
		@RosettaDataType(value="MetaFields", builder=MetaFields.MetaFieldsBuilderImpl.class, version="0.0.0")
		public interface MetaFields extends RosettaModelObject, GlobalKeyFields, MetaDataFields {
		
			MetaFieldsMeta metaData = new MetaFieldsMeta();
		
			/*********************** Getter Methods  ***********************/
			String getScheme();
			String getGlobalKey();
			String getExternalKey();
			List<? extends Key> getKey();
		
			/*********************** Build Methods  ***********************/
			MetaFields build();
			
			MetaFields.MetaFieldsBuilder toBuilder();
			
			static MetaFields.MetaFieldsBuilder builder() {
				return new MetaFields.MetaFieldsBuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends MetaFields> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends MetaFields> getType() {
				return MetaFields.class;
			}
			
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("globalKey"), String.class, getGlobalKey(), this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("externalKey"), String.class, getExternalKey(), this, AttributeMeta.META);
				processRosetta(path.newSubPath("key"), processor, Key.class, getKey());
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface MetaFieldsBuilder extends MetaFields, RosettaModelObjectBuilder, GlobalKeyFields.GlobalKeyFieldsBuilder, MetaDataFields.MetaDataFieldsBuilder {
				Key.KeyBuilder getOrCreateKey(int _index);
				List<? extends Key.KeyBuilder> getKey();
				MetaFields.MetaFieldsBuilder setScheme(String scheme);
				MetaFields.MetaFieldsBuilder setGlobalKey(String globalKey);
				MetaFields.MetaFieldsBuilder setExternalKey(String externalKey);
				MetaFields.MetaFieldsBuilder addKey(Key key0);
				MetaFields.MetaFieldsBuilder addKey(Key key1, int _idx);
				MetaFields.MetaFieldsBuilder addKey(List<? extends Key> key2);
				MetaFields.MetaFieldsBuilder setKey(List<? extends Key> key3);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("globalKey"), String.class, getGlobalKey(), this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("externalKey"), String.class, getExternalKey(), this, AttributeMeta.META);
					processRosetta(path.newSubPath("key"), processor, Key.KeyBuilder.class, getKey());
				}
				
		
				MetaFields.MetaFieldsBuilder prune();
			}
		
			/*********************** Immutable Implementation of MetaFields  ***********************/
			class MetaFieldsImpl implements MetaFields {
				private final String scheme;
				private final String globalKey;
				private final String externalKey;
				private final List<? extends Key> key;
				
				protected MetaFieldsImpl(MetaFields.MetaFieldsBuilder builder) {
					this.scheme = builder.getScheme();
					this.globalKey = builder.getGlobalKey();
					this.externalKey = builder.getExternalKey();
					this.key = ofNullable(builder.getKey()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
				}
				
				@Override
				@RosettaAttribute("scheme")
				public String getScheme() {
					return scheme;
				}
				
				@Override
				@RosettaAttribute("globalKey")
				public String getGlobalKey() {
					return globalKey;
				}
				
				@Override
				@RosettaAttribute("externalKey")
				public String getExternalKey() {
					return externalKey;
				}
				
				@Override
				@RosettaAttribute("location")
				public List<? extends Key> getKey() {
					return key;
				}
				
				@Override
				public MetaFields build() {
					return this;
				}
				
				@Override
				public MetaFields.MetaFieldsBuilder toBuilder() {
					MetaFields.MetaFieldsBuilder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(MetaFields.MetaFieldsBuilder builder) {
					ofNullable(getScheme()).ifPresent(builder::setScheme);
					ofNullable(getGlobalKey()).ifPresent(builder::setGlobalKey);
					ofNullable(getExternalKey()).ifPresent(builder::setExternalKey);
					ofNullable(getKey()).ifPresent(builder::setKey);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					MetaFields _that = getType().cast(o);
				
					if (!Objects.equals(scheme, _that.getScheme())) return false;
					if (!Objects.equals(globalKey, _that.getGlobalKey())) return false;
					if (!Objects.equals(externalKey, _that.getExternalKey())) return false;
					if (!ListEquals.listEquals(key, _that.getKey())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (scheme != null ? scheme.hashCode() : 0);
					_result = 31 * _result + (globalKey != null ? globalKey.hashCode() : 0);
					_result = 31 * _result + (externalKey != null ? externalKey.hashCode() : 0);
					_result = 31 * _result + (key != null ? key.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "MetaFields {" +
						"scheme=" + this.scheme + ", " +
						"globalKey=" + this.globalKey + ", " +
						"externalKey=" + this.externalKey + ", " +
						"key=" + this.key +
					'}';
				}
			}
		
			/*********************** Builder Implementation of MetaFields  ***********************/
			class MetaFieldsBuilderImpl implements MetaFields.MetaFieldsBuilder {
			
				protected String scheme;
				protected String globalKey;
				protected String externalKey;
				protected List<Key.KeyBuilder> key = new ArrayList<>();
			
				public MetaFieldsBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("scheme")
				public String getScheme() {
					return scheme;
				}
				
				@Override
				@RosettaAttribute("globalKey")
				public String getGlobalKey() {
					return globalKey;
				}
				
				@Override
				@RosettaAttribute("externalKey")
				public String getExternalKey() {
					return externalKey;
				}
				
				@Override
				@RosettaAttribute("location")
				public List<? extends Key.KeyBuilder> getKey() {
					return key;
				}
				
				public Key.KeyBuilder getOrCreateKey(int _index) {
				
					if (key==null) {
						this.key = new ArrayList<>();
					}
					Key.KeyBuilder result;
					return getIndex(key, _index, () -> {
								Key.KeyBuilder newKey = Key.builder();
								return newKey;
							});
				}
				
				@Override
				@RosettaAttribute("scheme")
				public MetaFields.MetaFieldsBuilder setScheme(String scheme) {
					this.scheme = scheme==null?null:scheme;
					return this;
				}
				@Override
				@RosettaAttribute("globalKey")
				public MetaFields.MetaFieldsBuilder setGlobalKey(String globalKey) {
					this.globalKey = globalKey==null?null:globalKey;
					return this;
				}
				@Override
				@RosettaAttribute("externalKey")
				public MetaFields.MetaFieldsBuilder setExternalKey(String externalKey) {
					this.externalKey = externalKey==null?null:externalKey;
					return this;
				}
				@Override
				public MetaFields.MetaFieldsBuilder addKey(Key key) {
					if (key!=null) this.key.add(key.toBuilder());
					return this;
				}
				
				@Override
				public MetaFields.MetaFieldsBuilder addKey(Key key, int _idx) {
					getIndex(this.key, _idx, () -> key.toBuilder());
					return this;
				}
				@Override 
				public MetaFields.MetaFieldsBuilder addKey(List<? extends Key> keys) {
					if (keys != null) {
						for (Key toAdd : keys) {
							this.key.add(toAdd.toBuilder());
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("location")
				public MetaFields.MetaFieldsBuilder setKey(List<? extends Key> keys) {
					if (keys == null)  {
						this.key = new ArrayList<>();
					}
					else {
						this.key = keys.stream()
							.map(_a->_a.toBuilder())
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				
				@Override
				public MetaFields build() {
					return new MetaFields.MetaFieldsImpl(this);
				}
				
				@Override
				public MetaFields.MetaFieldsBuilder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public MetaFields.MetaFieldsBuilder prune() {
					key = key.stream().filter(b->b!=null).<Key.KeyBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getScheme()!=null) return true;
					if (getGlobalKey()!=null) return true;
					if (getExternalKey()!=null) return true;
					if (getKey()!=null && getKey().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public MetaFields.MetaFieldsBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					MetaFields.MetaFieldsBuilder o = (MetaFields.MetaFieldsBuilder) other;
					
					merger.mergeRosetta(getKey(), o.getKey(), this::getOrCreateKey);
					
					merger.mergeBasic(getScheme(), o.getScheme(), this::setScheme);
					merger.mergeBasic(getGlobalKey(), o.getGlobalKey(), this::setGlobalKey);
					merger.mergeBasic(getExternalKey(), o.getExternalKey(), this::setExternalKey);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					MetaFields _that = getType().cast(o);
				
					if (!Objects.equals(scheme, _that.getScheme())) return false;
					if (!Objects.equals(globalKey, _that.getGlobalKey())) return false;
					if (!Objects.equals(externalKey, _that.getExternalKey())) return false;
					if (!ListEquals.listEquals(key, _that.getKey())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (scheme != null ? scheme.hashCode() : 0);
					_result = 31 * _result + (globalKey != null ? globalKey.hashCode() : 0);
					_result = 31 * _result + (externalKey != null ? externalKey.hashCode() : 0);
					_result = 31 * _result + (key != null ? key.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "MetaFieldsBuilder {" +
						"scheme=" + this.scheme + ", " +
						"globalKey=" + this.globalKey + ", " +
						"externalKey=" + this.externalKey + ", " +
						"key=" + this.key +
					'}';
				}
			}
		}
		
		class MetaFieldsMeta extends BasicRosettaMetaData<MetaFields>{
		
		}
		''')
	}
	
	@Test
	def testMetaAndTemplateFieldsCode() {
		assertGeneratedCode('com.rosetta.model.metafields.MetaAndTemplateFields', '''
		package com.rosetta.model.metafields;
		
		import com.google.common.collect.ImmutableList;
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.GlobalKeyFields;
		import com.rosetta.model.lib.meta.GlobalKeyFields.GlobalKeyFieldsBuilder;
		import com.rosetta.model.lib.meta.Key;
		import com.rosetta.model.lib.meta.MetaDataFields;
		import com.rosetta.model.lib.meta.MetaDataFields.MetaDataFieldsBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.meta.TemplateFields;
		import com.rosetta.model.lib.meta.TemplateFields.TemplateFieldsBuilder;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.AttributeMeta;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.util.ListEquals;
		import java.util.ArrayList;
		import java.util.List;
		import java.util.Objects;
		import java.util.stream.Collectors;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version 1
		 */
		@RosettaDataType(value="MetaAndTemplateFields", builder=MetaAndTemplateFields.MetaAndTemplateFieldsBuilderImpl.class, version="0.0.0")
		public interface MetaAndTemplateFields extends RosettaModelObject, GlobalKeyFields, TemplateFields, MetaDataFields {
		
			MetaAndTemplateFieldsMeta metaData = new MetaAndTemplateFieldsMeta();
		
			/*********************** Getter Methods  ***********************/
			String getScheme();
			String getTemplateGlobalReference();
			String getGlobalKey();
			String getExternalKey();
			List<? extends Key> getKey();
		
			/*********************** Build Methods  ***********************/
			MetaAndTemplateFields build();
			
			MetaAndTemplateFields.MetaAndTemplateFieldsBuilder toBuilder();
			
			static MetaAndTemplateFields.MetaAndTemplateFieldsBuilder builder() {
				return new MetaAndTemplateFields.MetaAndTemplateFieldsBuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends MetaAndTemplateFields> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends MetaAndTemplateFields> getType() {
				return MetaAndTemplateFields.class;
			}
			
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("templateGlobalReference"), String.class, getTemplateGlobalReference(), this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("globalKey"), String.class, getGlobalKey(), this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("externalKey"), String.class, getExternalKey(), this, AttributeMeta.META);
				processRosetta(path.newSubPath("key"), processor, Key.class, getKey());
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface MetaAndTemplateFieldsBuilder extends MetaAndTemplateFields, RosettaModelObjectBuilder, GlobalKeyFields.GlobalKeyFieldsBuilder, TemplateFields.TemplateFieldsBuilder, MetaDataFields.MetaDataFieldsBuilder {
				Key.KeyBuilder getOrCreateKey(int _index);
				List<? extends Key.KeyBuilder> getKey();
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setScheme(String scheme);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setTemplateGlobalReference(String templateGlobalReference);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setGlobalKey(String globalKey);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setExternalKey(String externalKey);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(Key key0);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(Key key1, int _idx);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(List<? extends Key> key2);
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setKey(List<? extends Key> key3);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("templateGlobalReference"), String.class, getTemplateGlobalReference(), this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("globalKey"), String.class, getGlobalKey(), this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("externalKey"), String.class, getExternalKey(), this, AttributeMeta.META);
					processRosetta(path.newSubPath("key"), processor, Key.KeyBuilder.class, getKey());
				}
				
		
				MetaAndTemplateFields.MetaAndTemplateFieldsBuilder prune();
			}
		
			/*********************** Immutable Implementation of MetaAndTemplateFields  ***********************/
			class MetaAndTemplateFieldsImpl implements MetaAndTemplateFields {
				private final String scheme;
				private final String templateGlobalReference;
				private final String globalKey;
				private final String externalKey;
				private final List<? extends Key> key;
				
				protected MetaAndTemplateFieldsImpl(MetaAndTemplateFields.MetaAndTemplateFieldsBuilder builder) {
					this.scheme = builder.getScheme();
					this.templateGlobalReference = builder.getTemplateGlobalReference();
					this.globalKey = builder.getGlobalKey();
					this.externalKey = builder.getExternalKey();
					this.key = ofNullable(builder.getKey()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
				}
				
				@Override
				@RosettaAttribute("scheme")
				public String getScheme() {
					return scheme;
				}
				
				@Override
				@RosettaAttribute("templateGlobalReference")
				public String getTemplateGlobalReference() {
					return templateGlobalReference;
				}
				
				@Override
				@RosettaAttribute("globalKey")
				public String getGlobalKey() {
					return globalKey;
				}
				
				@Override
				@RosettaAttribute("externalKey")
				public String getExternalKey() {
					return externalKey;
				}
				
				@Override
				@RosettaAttribute("location")
				public List<? extends Key> getKey() {
					return key;
				}
				
				@Override
				public MetaAndTemplateFields build() {
					return this;
				}
				
				@Override
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder toBuilder() {
					MetaAndTemplateFields.MetaAndTemplateFieldsBuilder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(MetaAndTemplateFields.MetaAndTemplateFieldsBuilder builder) {
					ofNullable(getScheme()).ifPresent(builder::setScheme);
					ofNullable(getTemplateGlobalReference()).ifPresent(builder::setTemplateGlobalReference);
					ofNullable(getGlobalKey()).ifPresent(builder::setGlobalKey);
					ofNullable(getExternalKey()).ifPresent(builder::setExternalKey);
					ofNullable(getKey()).ifPresent(builder::setKey);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					MetaAndTemplateFields _that = getType().cast(o);
				
					if (!Objects.equals(scheme, _that.getScheme())) return false;
					if (!Objects.equals(templateGlobalReference, _that.getTemplateGlobalReference())) return false;
					if (!Objects.equals(globalKey, _that.getGlobalKey())) return false;
					if (!Objects.equals(externalKey, _that.getExternalKey())) return false;
					if (!ListEquals.listEquals(key, _that.getKey())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (scheme != null ? scheme.hashCode() : 0);
					_result = 31 * _result + (templateGlobalReference != null ? templateGlobalReference.hashCode() : 0);
					_result = 31 * _result + (globalKey != null ? globalKey.hashCode() : 0);
					_result = 31 * _result + (externalKey != null ? externalKey.hashCode() : 0);
					_result = 31 * _result + (key != null ? key.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "MetaAndTemplateFields {" +
						"scheme=" + this.scheme + ", " +
						"templateGlobalReference=" + this.templateGlobalReference + ", " +
						"globalKey=" + this.globalKey + ", " +
						"externalKey=" + this.externalKey + ", " +
						"key=" + this.key +
					'}';
				}
			}
		
			/*********************** Builder Implementation of MetaAndTemplateFields  ***********************/
			class MetaAndTemplateFieldsBuilderImpl implements MetaAndTemplateFields.MetaAndTemplateFieldsBuilder {
			
				protected String scheme;
				protected String templateGlobalReference;
				protected String globalKey;
				protected String externalKey;
				protected List<Key.KeyBuilder> key = new ArrayList<>();
			
				public MetaAndTemplateFieldsBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("scheme")
				public String getScheme() {
					return scheme;
				}
				
				@Override
				@RosettaAttribute("templateGlobalReference")
				public String getTemplateGlobalReference() {
					return templateGlobalReference;
				}
				
				@Override
				@RosettaAttribute("globalKey")
				public String getGlobalKey() {
					return globalKey;
				}
				
				@Override
				@RosettaAttribute("externalKey")
				public String getExternalKey() {
					return externalKey;
				}
				
				@Override
				@RosettaAttribute("location")
				public List<? extends Key.KeyBuilder> getKey() {
					return key;
				}
				
				public Key.KeyBuilder getOrCreateKey(int _index) {
				
					if (key==null) {
						this.key = new ArrayList<>();
					}
					Key.KeyBuilder result;
					return getIndex(key, _index, () -> {
								Key.KeyBuilder newKey = Key.builder();
								return newKey;
							});
				}
				
				@Override
				@RosettaAttribute("scheme")
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setScheme(String scheme) {
					this.scheme = scheme==null?null:scheme;
					return this;
				}
				@Override
				@RosettaAttribute("templateGlobalReference")
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setTemplateGlobalReference(String templateGlobalReference) {
					this.templateGlobalReference = templateGlobalReference==null?null:templateGlobalReference;
					return this;
				}
				@Override
				@RosettaAttribute("globalKey")
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setGlobalKey(String globalKey) {
					this.globalKey = globalKey==null?null:globalKey;
					return this;
				}
				@Override
				@RosettaAttribute("externalKey")
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setExternalKey(String externalKey) {
					this.externalKey = externalKey==null?null:externalKey;
					return this;
				}
				@Override
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(Key key) {
					if (key!=null) this.key.add(key.toBuilder());
					return this;
				}
				
				@Override
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(Key key, int _idx) {
					getIndex(this.key, _idx, () -> key.toBuilder());
					return this;
				}
				@Override 
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(List<? extends Key> keys) {
					if (keys != null) {
						for (Key toAdd : keys) {
							this.key.add(toAdd.toBuilder());
						}
					}
					return this;
				}
				
				@Override 
				@RosettaAttribute("location")
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setKey(List<? extends Key> keys) {
					if (keys == null)  {
						this.key = new ArrayList<>();
					}
					else {
						this.key = keys.stream()
							.map(_a->_a.toBuilder())
							.collect(Collectors.toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
				
				@Override
				public MetaAndTemplateFields build() {
					return new MetaAndTemplateFields.MetaAndTemplateFieldsImpl(this);
				}
				
				@Override
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder prune() {
					key = key.stream().filter(b->b!=null).<Key.KeyBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getScheme()!=null) return true;
					if (getTemplateGlobalReference()!=null) return true;
					if (getGlobalKey()!=null) return true;
					if (getExternalKey()!=null) return true;
					if (getKey()!=null && getKey().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					MetaAndTemplateFields.MetaAndTemplateFieldsBuilder o = (MetaAndTemplateFields.MetaAndTemplateFieldsBuilder) other;
					
					merger.mergeRosetta(getKey(), o.getKey(), this::getOrCreateKey);
					
					merger.mergeBasic(getScheme(), o.getScheme(), this::setScheme);
					merger.mergeBasic(getTemplateGlobalReference(), o.getTemplateGlobalReference(), this::setTemplateGlobalReference);
					merger.mergeBasic(getGlobalKey(), o.getGlobalKey(), this::setGlobalKey);
					merger.mergeBasic(getExternalKey(), o.getExternalKey(), this::setExternalKey);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					MetaAndTemplateFields _that = getType().cast(o);
				
					if (!Objects.equals(scheme, _that.getScheme())) return false;
					if (!Objects.equals(templateGlobalReference, _that.getTemplateGlobalReference())) return false;
					if (!Objects.equals(globalKey, _that.getGlobalKey())) return false;
					if (!Objects.equals(externalKey, _that.getExternalKey())) return false;
					if (!ListEquals.listEquals(key, _that.getKey())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (scheme != null ? scheme.hashCode() : 0);
					_result = 31 * _result + (templateGlobalReference != null ? templateGlobalReference.hashCode() : 0);
					_result = 31 * _result + (globalKey != null ? globalKey.hashCode() : 0);
					_result = 31 * _result + (externalKey != null ? externalKey.hashCode() : 0);
					_result = 31 * _result + (key != null ? key.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "MetaAndTemplateFieldsBuilder {" +
						"scheme=" + this.scheme + ", " +
						"templateGlobalReference=" + this.templateGlobalReference + ", " +
						"globalKey=" + this.globalKey + ", " +
						"externalKey=" + this.externalKey + ", " +
						"key=" + this.key +
					'}';
				}
			}
		}
		
		class MetaAndTemplateFieldsMeta extends BasicRosettaMetaData<MetaAndTemplateFields>{
		
		}
		''')
	}
}