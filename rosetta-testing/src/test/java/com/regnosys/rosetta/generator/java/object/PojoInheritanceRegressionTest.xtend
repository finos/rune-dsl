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
 * This test is meant to prevent accidental changes to the generated code of
 * Rosetta Model Objects related to inheritance. If any unwanted change accidentally
 * gets through, please add to this test so it does not happen again.
 */
@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
@TestInstance(Lifecycle.PER_CLASS)
class PojoInheritanceRegressionTest {
	@Inject extension CodeGeneratorTestHelper
	
	Map<String, String> code
	
	@BeforeAll
	def void setup() {
		code = '''
			type Foo1:
				attr int (1..1)
				numberAttr number (0..1)
				parent Parent (1..1)
				parentList Parent (0..10)
				stringAttr string (1..1)
					[metadata scheme]
			
			type Foo2 extends Foo1:
				restrict numberAttr int(digits: 30, max: 100) (1..1)
				restrict parent Child (1..1)
				restrict parentList Child (1..1)
				restrict stringAttr string(maxLength: 42) (1..1)
					[metadata scheme]
			
			type Foo3 extends Foo2:
				restrict numberAttr int (1..1)
				restrict parentList GrandChild (1..1)
			
			type Parent:
			
			type Child extends Parent:
			
			type GrandChild extends Child:
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
	def void testFoo2Code() {
		assertGeneratedCode('com.rosetta.test.model.Foo2', '''
		package com.rosetta.test.model;
		
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.mapper.MapperC;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.model.metafields.FieldWithMetaString;
		import com.rosetta.model.metafields.FieldWithMetaString.FieldWithMetaStringBuilder;
		import com.rosetta.test.model.Child;
		import com.rosetta.test.model.Child.ChildBuilder;
		import com.rosetta.test.model.Foo1;
		import com.rosetta.test.model.Foo1.Foo1Builder;
		import com.rosetta.test.model.Foo2;
		import com.rosetta.test.model.Foo2.Foo2Builder;
		import com.rosetta.test.model.Foo2.Foo2BuilderImpl;
		import com.rosetta.test.model.Foo2.Foo2Impl;
		import com.rosetta.test.model.Parent;
		import com.rosetta.test.model.Parent.ParentBuilder;
		import com.rosetta.test.model.meta.Foo2Meta;
		import java.math.BigDecimal;
		import java.math.BigInteger;
		import java.util.Collections;
		import java.util.List;
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version test
		 */
		@RosettaDataType(value="Foo2", builder=Foo2.Foo2BuilderImpl.class, version="test")
		public interface Foo2 extends Foo1 {
		
			Foo2Meta metaData = new Foo2Meta();
		
			/*********************** Getter Methods  ***********************/
			BigInteger getNumberAttrRestrictedAsBigInteger();
			@Override
			Child getParent();
			Child getParentListRestrictedAsSingleChild();
		
			/*********************** Build Methods  ***********************/
			Foo2 build();
			
			Foo2.Foo2Builder toBuilder();
			
			static Foo2.Foo2Builder builder() {
				return new Foo2.Foo2BuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends Foo2> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends Foo2> getType() {
				return Foo2.class;
			}
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
				processor.processBasic(path.newSubPath("numberAttr"), BigInteger.class, getNumberAttrRestrictedAsBigInteger(), this);
				processRosetta(path.newSubPath("parent"), processor, Child.class, getParent());
				processRosetta(path.newSubPath("parentList"), processor, Child.class, getParentListRestrictedAsSingleChild());
				processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.class, getStringAttr());
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface Foo2Builder extends Foo2, Foo1.Foo1Builder {
				Child.ChildBuilder getOrCreateParent();
				@Override
				Child.ChildBuilder getParent();
				Child.ChildBuilder getOrCreateParentList();
				@Override
				Child.ChildBuilder getParentListRestrictedAsSingleChild();
				@Override
				Foo2.Foo2Builder setAttr(Integer attr);
				@Override
				Foo2.Foo2Builder setNumberAttr(BigDecimal numberAttr);
				@Override
				Foo2.Foo2Builder setParent(Parent parent);
				@Override
				Foo2.Foo2Builder addParentList(Parent parentList);
				@Override
				Foo2.Foo2Builder addParentList(Parent parentList, int _idx);
				@Override
				Foo2.Foo2Builder addParentList(List<? extends Parent> parentList);
				@Override
				Foo2.Foo2Builder setParentList(List<? extends Parent> parentList);
				@Override
				Foo2.Foo2Builder setStringAttr(FieldWithMetaString stringAttr);
				@Override
				Foo2.Foo2Builder setStringAttrValue(String stringAttr);
				Foo2.Foo2Builder setNumberAttr(BigInteger numberAttr);
				Foo2.Foo2Builder setParent(Child parent);
				Foo2.Foo2Builder setParentList(Child parentList);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
					processor.processBasic(path.newSubPath("numberAttr"), BigInteger.class, getNumberAttrRestrictedAsBigInteger(), this);
					processRosetta(path.newSubPath("parent"), processor, Child.ChildBuilder.class, getParent());
					processRosetta(path.newSubPath("parentList"), processor, Child.ChildBuilder.class, getParentListRestrictedAsSingleChild());
					processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getStringAttr());
				}
				
		
				Foo2.Foo2Builder prune();
			}
		
			/*********************** Immutable Implementation of Foo2  ***********************/
			class Foo2Impl implements Foo2 {
				private final Integer attr;
				private final BigInteger numberAttr;
				private final Child parent;
				private final Child parentList;
				private final FieldWithMetaString stringAttr;
				
				protected Foo2Impl(Foo2.Foo2Builder builder) {
					this.attr = builder.getAttr();
					this.numberAttr = builder.getNumberAttrRestrictedAsBigInteger();
					this.parent = ofNullable(builder.getParent()).map(f->f.build()).orElse(null);
					this.parentList = ofNullable(builder.getParentListRestrictedAsSingleChild()).map(f->f.build()).orElse(null);
					this.stringAttr = ofNullable(builder.getStringAttr()).map(f->f.build()).orElse(null);
				}
				
				@Override
				@RosettaAttribute("attr")
				public Integer getAttr() {
					return attr;
				}
				
				@Override
				@RosettaAttribute("numberAttr")
				public BigInteger getNumberAttrRestrictedAsBigInteger() {
					return numberAttr;
				}
				
				@Override
				public BigDecimal getNumberAttr() {
					return numberAttr == null ? null : new BigDecimal(numberAttr);
				}
				
				@Override
				@RosettaAttribute("parent")
				public Child getParent() {
					return parent;
				}
				
				@Override
				@RosettaAttribute("parentList")
				public Child getParentListRestrictedAsSingleChild() {
					return parentList;
				}
				
				@Override
				public List<? extends Parent> getParentList() {
					return parentList == null ? Collections.<Parent>emptyList() : Collections.singletonList(parentList);
				}
				
				@Override
				@RosettaAttribute("stringAttr")
				public FieldWithMetaString getStringAttr() {
					return stringAttr;
				}
				
				@Override
				public Foo2 build() {
					return this;
				}
				
				@Override
				public Foo2.Foo2Builder toBuilder() {
					Foo2.Foo2Builder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(Foo2.Foo2Builder builder) {
					ofNullable(getAttr()).ifPresent(builder::setAttr);
					ofNullable(getNumberAttrRestrictedAsBigInteger()).ifPresent(builder::setNumberAttr);
					ofNullable(getParent()).ifPresent(builder::setParent);
					ofNullable(getParentListRestrictedAsSingleChild()).ifPresent(builder::setParentList);
					ofNullable(getStringAttr()).ifPresent(builder::setStringAttr);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					Foo2 _that = getType().cast(o);
				
					if (!Objects.equals(attr, _that.getAttr())) return false;
					if (!Objects.equals(numberAttr, _that.getNumberAttrRestrictedAsBigInteger())) return false;
					if (!Objects.equals(parent, _that.getParent())) return false;
					if (!Objects.equals(parentList, _that.getParentListRestrictedAsSingleChild())) return false;
					if (!Objects.equals(stringAttr, _that.getStringAttr())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
					_result = 31 * _result + (numberAttr != null ? numberAttr.hashCode() : 0);
					_result = 31 * _result + (parent != null ? parent.hashCode() : 0);
					_result = 31 * _result + (parentList != null ? parentList.hashCode() : 0);
					_result = 31 * _result + (stringAttr != null ? stringAttr.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "Foo2 {" +
						"attr=" + this.attr + ", " +
						"numberAttr=" + this.numberAttr + ", " +
						"parent=" + this.parent + ", " +
						"parentList=" + this.parentList + ", " +
						"stringAttr=" + this.stringAttr +
					'}';
				}
			}
		
			/*********************** Builder Implementation of Foo2  ***********************/
			class Foo2BuilderImpl implements Foo2.Foo2Builder {
			
				protected Integer attr;
				protected BigInteger numberAttr;
				protected Child.ChildBuilder parent;
				protected Child.ChildBuilder parentList;
				protected FieldWithMetaString.FieldWithMetaStringBuilder stringAttr;
				
				@Override
				@RosettaAttribute("attr")
				public Integer getAttr() {
					return attr;
				}
				
				@Override
				@RosettaAttribute("numberAttr")
				public BigInteger getNumberAttrRestrictedAsBigInteger() {
					return numberAttr;
				}
				@Override
				public BigDecimal getNumberAttr() {
					return numberAttr == null ? null : new BigDecimal(numberAttr);
				}
				
				
				@Override
				@RosettaAttribute("parent")
				public Child.ChildBuilder getParent() {
					return parent;
				}
				
				@Override
				public Child.ChildBuilder getOrCreateParent() {
					Child.ChildBuilder result;
					if (parent!=null) {
						result = parent;
					}
					else {
						result = parent = Child.builder();
					}
					
					return result;
				}
				
				@Override
				@RosettaAttribute("parentList")
				public Child.ChildBuilder getParentListRestrictedAsSingleChild() {
					return parentList;
				}
				@Override
				public List<? extends Parent.ParentBuilder> getParentList() {
					return parentList == null ? Collections.<Parent.ParentBuilder>emptyList() : Collections.singletonList(parentList);
				}
				
				
				@Override
				public Child.ChildBuilder getOrCreateParentList() {
					Child.ChildBuilder result;
					if (parentList!=null) {
						result = parentList;
					}
					else {
						result = parentList = Child.builder();
					}
					
					return result;
				}
				
				@Override
				public Child.ChildBuilder getOrCreateParentList(int _index) {
					return getOrCreateParentList();
				}
				
				@Override
				@RosettaAttribute("stringAttr")
				public FieldWithMetaString.FieldWithMetaStringBuilder getStringAttr() {
					return stringAttr;
				}
				
				@Override
				public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateStringAttr() {
					FieldWithMetaString.FieldWithMetaStringBuilder result;
					if (stringAttr!=null) {
						result = stringAttr;
					}
					else {
						result = stringAttr = FieldWithMetaString.builder();
					}
					
					return result;
				}
				
				@Override
				@RosettaAttribute("attr")
				public Foo2.Foo2Builder setAttr(Integer _attr) {
					this.attr = _attr == null ? null : _attr;
					return this;
				}
				
				@Override
				@RosettaAttribute("numberAttr")
				public Foo2.Foo2Builder setNumberAttr(BigInteger _numberAttr) {
					this.numberAttr = _numberAttr == null ? null : _numberAttr;
					return this;
				}
				
				@Override
				public Foo2.Foo2Builder setNumberAttr(BigDecimal _numberAttr) {
					final BigInteger ifThenElseResult;
					if (_numberAttr == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = new BigDecimal(_numberAttr.toBigInteger()).compareTo(_numberAttr) == 0 ? _numberAttr.toBigInteger() : null;
					}
					return setNumberAttr(ifThenElseResult);
				}
				
				@Override
				@RosettaAttribute("parent")
				public Foo2.Foo2Builder setParent(Child _parent) {
					this.parent = _parent == null ? null : _parent.toBuilder();
					return this;
				}
				
				@Override
				public Foo2.Foo2Builder setParent(Parent _parent) {
					final Child ifThenElseResult;
					if (_parent == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parent instanceof Child ? Child.class.cast(_parent) : null;
					}
					return setParent(ifThenElseResult);
				}
				
				@Override
				@RosettaAttribute("parentList")
				public Foo2.Foo2Builder setParentList(Child _parentList) {
					this.parentList = _parentList == null ? null : _parentList.toBuilder();
					return this;
				}
				
				@Override
				public Foo2.Foo2Builder addParentList(Parent parentList0) {
					final Child ifThenElseResult;
					if (parentList0 == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = parentList0 instanceof Child ? Child.class.cast(parentList0) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override
				public Foo2.Foo2Builder addParentList(Parent _parentList, int _idx) {
					final Child ifThenElseResult;
					if (_parentList == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parentList instanceof Child ? Child.class.cast(_parentList) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override 
				public Foo2.Foo2Builder addParentList(List<? extends Parent> parentLists) {
					final Parent _parent = MapperC.of(parentLists).get();
					final Child ifThenElseResult;
					if (_parent == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parent instanceof Child ? Child.class.cast(_parent) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override 
				public Foo2.Foo2Builder setParentList(List<? extends Parent> parentLists) {
					final Parent _parent = MapperC.of(parentLists).get();
					final Child ifThenElseResult;
					if (_parent == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parent instanceof Child ? Child.class.cast(_parent) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override
				@RosettaAttribute("stringAttr")
				public Foo2.Foo2Builder setStringAttr(FieldWithMetaString _stringAttr) {
					this.stringAttr = _stringAttr == null ? null : _stringAttr.toBuilder();
					return this;
				}
				
				@Override
				public Foo2.Foo2Builder setStringAttrValue(String _stringAttr) {
					this.getOrCreateStringAttr().setValue(_stringAttr);
					return this;
				}
				
				@Override
				public Foo2 build() {
					return new Foo2.Foo2Impl(this);
				}
				
				@Override
				public Foo2.Foo2Builder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public Foo2.Foo2Builder prune() {
					if (parent!=null && !parent.prune().hasData()) parent = null;
					if (parentList!=null && !parentList.prune().hasData()) parentList = null;
					if (stringAttr!=null && !stringAttr.prune().hasData()) stringAttr = null;
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getAttr()!=null) return true;
					if (getNumberAttrRestrictedAsBigInteger()!=null) return true;
					if (getParent()!=null && getParent().hasData()) return true;
					if (getParentListRestrictedAsSingleChild()!=null && getParentListRestrictedAsSingleChild().hasData()) return true;
					if (getStringAttr()!=null) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public Foo2.Foo2Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					Foo2.Foo2Builder o = (Foo2.Foo2Builder) other;
					
					merger.mergeRosetta(getParent(), o.getParent(), this::setParent);
					merger.mergeRosetta(getParentListRestrictedAsSingleChild(), o.getParentListRestrictedAsSingleChild(), this::setParentList);
					merger.mergeRosetta(getStringAttr(), o.getStringAttr(), this::setStringAttr);
					
					merger.mergeBasic(getAttr(), o.getAttr(), this::setAttr);
					merger.mergeBasic(getNumberAttrRestrictedAsBigInteger(), o.getNumberAttrRestrictedAsBigInteger(), this::setNumberAttr);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					Foo2 _that = getType().cast(o);
				
					if (!Objects.equals(attr, _that.getAttr())) return false;
					if (!Objects.equals(numberAttr, _that.getNumberAttrRestrictedAsBigInteger())) return false;
					if (!Objects.equals(parent, _that.getParent())) return false;
					if (!Objects.equals(parentList, _that.getParentListRestrictedAsSingleChild())) return false;
					if (!Objects.equals(stringAttr, _that.getStringAttr())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
					_result = 31 * _result + (numberAttr != null ? numberAttr.hashCode() : 0);
					_result = 31 * _result + (parent != null ? parent.hashCode() : 0);
					_result = 31 * _result + (parentList != null ? parentList.hashCode() : 0);
					_result = 31 * _result + (stringAttr != null ? stringAttr.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "Foo2Builder {" +
						"attr=" + this.attr + ", " +
						"numberAttr=" + this.numberAttr + ", " +
						"parent=" + this.parent + ", " +
						"parentList=" + this.parentList + ", " +
						"stringAttr=" + this.stringAttr +
					'}';
				}
			}
		}
		''')
	}
	
	@Test
	def void testFoo2TypeFormatValidator() {
		assertGeneratedCode('com.rosetta.test.model.validation.Foo2TypeFormatValidator', '''
		package com.rosetta.test.model.validation;
		
		import com.google.common.collect.Lists;
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.validation.ValidationResult;
		import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
		import com.rosetta.model.lib.validation.Validator;
		import com.rosetta.test.model.Foo2;
		import java.math.BigDecimal;
		import java.util.List;
		
		import static com.google.common.base.Strings.isNullOrEmpty;
		import static com.rosetta.model.lib.expression.ExpressionOperators.checkNumber;
		import static com.rosetta.model.lib.expression.ExpressionOperators.checkString;
		import static com.rosetta.model.lib.validation.ValidationResult.failure;
		import static com.rosetta.model.lib.validation.ValidationResult.success;
		import static java.util.Optional.empty;
		import static java.util.Optional.of;
		import static java.util.stream.Collectors.joining;
		import static java.util.stream.Collectors.toList;
		
		public class Foo2TypeFormatValidator implements Validator<Foo2> {
		
			private List<ComparisonResult> getComparisonResults(Foo2 o) {
				return Lists.<ComparisonResult>newArrayList(
						checkNumber("attr", o.getAttr(), empty(), of(0), empty(), empty()), 
						checkNumber("numberAttr", o.getNumberAttrRestrictedAsBigInteger(), of(30), of(0), empty(), of(new BigDecimal("1E+2"))), 
						checkString("stringAttr", o.getStringAttr().getValue(), 0, of(42), empty())
					);
			}
		
			@Override
			public ValidationResult<Foo2> validate(RosettaPath path, Foo2 o) {
				String error = getComparisonResults(o)
					.stream()
					.filter(res -> !res.get())
					.map(res -> res.getError())
					.collect(joining("; "));
		
				if (!isNullOrEmpty(error)) {
					return failure("Foo2", ValidationType.TYPE_FORMAT, "Foo2", path, "", error);
				}
				return success("Foo2", ValidationType.TYPE_FORMAT, "Foo2", path, "");
			}
		
			@Override
			public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo2 o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!isNullOrEmpty(res.getError())) {
							return failure("Foo2", ValidationType.TYPE_FORMAT, "Foo2", path, "", res.getError());
						}
						return success("Foo2", ValidationType.TYPE_FORMAT, "Foo2", path, "");
					})
					.collect(toList());
			}
		
		}
		''')
	}
	
	@Test
	def void testFoo2OnlyExistsValidator() {
		assertGeneratedCode('com.rosetta.test.model.validation.exists.Foo2OnlyExistsValidator', '''
		package com.rosetta.test.model.validation.exists;
		
		import com.google.common.collect.ImmutableMap;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.validation.ExistenceChecker;
		import com.rosetta.model.lib.validation.ValidationResult;
		import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
		import com.rosetta.model.lib.validation.ValidatorWithArg;
		import com.rosetta.model.metafields.FieldWithMetaString;
		import com.rosetta.test.model.Child;
		import com.rosetta.test.model.Foo2;
		import java.math.BigInteger;
		import java.util.Map;
		import java.util.Set;
		import java.util.stream.Collectors;
		
		import static com.rosetta.model.lib.validation.ValidationResult.failure;
		import static com.rosetta.model.lib.validation.ValidationResult.success;
		
		public class Foo2OnlyExistsValidator implements ValidatorWithArg<Foo2, Set<String>> {
		
			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends Foo2> ValidationResult<Foo2> validate(RosettaPath path, T2 o, Set<String> fields) {
				Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
						.put("attr", ExistenceChecker.isSet((Integer) o.getAttr()))
						.put("numberAttr", ExistenceChecker.isSet((BigInteger) o.getNumberAttrRestrictedAsBigInteger()))
						.put("parent", ExistenceChecker.isSet((Child) o.getParent()))
						.put("parentList", ExistenceChecker.isSet((Child) o.getParentListRestrictedAsSingleChild()))
						.put("stringAttr", ExistenceChecker.isSet((FieldWithMetaString) o.getStringAttr()))
						.build();
				
				// Find the fields that are set
				Set<String> setFields = fieldExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.map(Map.Entry::getKey)
						.collect(Collectors.toSet());
				
				if (setFields.equals(fields)) {
					return success("Foo2", ValidationType.ONLY_EXISTS, "Foo2", path, "");
				}
				return failure("Foo2", ValidationType.ONLY_EXISTS, "Foo2", path, "",
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
			}
		}
		''')
	}
	
	@Test
	def void testFoo3Code() {
		assertGeneratedCode('com.rosetta.test.model.Foo3', '''
		package com.rosetta.test.model;
		
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.mapper.MapperC;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.model.metafields.FieldWithMetaString;
		import com.rosetta.model.metafields.FieldWithMetaString.FieldWithMetaStringBuilder;
		import com.rosetta.test.model.Child;
		import com.rosetta.test.model.Child.ChildBuilder;
		import com.rosetta.test.model.Foo2;
		import com.rosetta.test.model.Foo2.Foo2Builder;
		import com.rosetta.test.model.Foo3;
		import com.rosetta.test.model.Foo3.Foo3Builder;
		import com.rosetta.test.model.Foo3.Foo3BuilderImpl;
		import com.rosetta.test.model.Foo3.Foo3Impl;
		import com.rosetta.test.model.GrandChild;
		import com.rosetta.test.model.GrandChild.GrandChildBuilder;
		import com.rosetta.test.model.Parent;
		import com.rosetta.test.model.Parent.ParentBuilder;
		import com.rosetta.test.model.meta.Foo3Meta;
		import java.math.BigDecimal;
		import java.math.BigInteger;
		import java.util.Collections;
		import java.util.List;
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version test
		 */
		@RosettaDataType(value="Foo3", builder=Foo3.Foo3BuilderImpl.class, version="test")
		public interface Foo3 extends Foo2 {
		
			Foo3Meta metaData = new Foo3Meta();
		
			/*********************** Getter Methods  ***********************/
			Integer getNumberAttrRestrictedAsInteger();
			@Override
			GrandChild getParentListRestrictedAsSingleChild();
		
			/*********************** Build Methods  ***********************/
			Foo3 build();
			
			Foo3.Foo3Builder toBuilder();
			
			static Foo3.Foo3Builder builder() {
				return new Foo3.Foo3BuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends Foo3> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends Foo3> getType() {
				return Foo3.class;
			}
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
				processor.processBasic(path.newSubPath("numberAttr"), Integer.class, getNumberAttrRestrictedAsInteger(), this);
				processRosetta(path.newSubPath("parent"), processor, Child.class, getParent());
				processRosetta(path.newSubPath("parentList"), processor, GrandChild.class, getParentListRestrictedAsSingleChild());
				processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.class, getStringAttr());
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface Foo3Builder extends Foo3, Foo2.Foo2Builder {
				GrandChild.GrandChildBuilder getOrCreateParentList();
				@Override
				GrandChild.GrandChildBuilder getParentListRestrictedAsSingleChild();
				@Override
				Foo3.Foo3Builder setAttr(Integer attr);
				@Override
				Foo3.Foo3Builder setNumberAttr(BigDecimal numberAttr);
				@Override
				Foo3.Foo3Builder setParent(Parent parent);
				@Override
				Foo3.Foo3Builder addParentList(Parent parentList);
				@Override
				Foo3.Foo3Builder addParentList(Parent parentList, int _idx);
				@Override
				Foo3.Foo3Builder addParentList(List<? extends Parent> parentList);
				@Override
				Foo3.Foo3Builder setParentList(List<? extends Parent> parentList);
				@Override
				Foo3.Foo3Builder setStringAttr(FieldWithMetaString stringAttr);
				@Override
				Foo3.Foo3Builder setStringAttrValue(String stringAttr);
				@Override
				Foo3.Foo3Builder setNumberAttr(BigInteger numberAttr);
				@Override
				Foo3.Foo3Builder setParent(Child parent);
				@Override
				Foo3.Foo3Builder setParentList(Child parentList);
				Foo3.Foo3Builder setNumberAttr(Integer numberAttr);
				Foo3.Foo3Builder setParentList(GrandChild parentList);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
					processor.processBasic(path.newSubPath("numberAttr"), Integer.class, getNumberAttrRestrictedAsInteger(), this);
					processRosetta(path.newSubPath("parent"), processor, Child.ChildBuilder.class, getParent());
					processRosetta(path.newSubPath("parentList"), processor, GrandChild.GrandChildBuilder.class, getParentListRestrictedAsSingleChild());
					processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getStringAttr());
				}
				
		
				Foo3.Foo3Builder prune();
			}
		
			/*********************** Immutable Implementation of Foo3  ***********************/
			class Foo3Impl implements Foo3 {
				private final Integer attr;
				private final Integer numberAttr;
				private final Child parent;
				private final GrandChild parentList;
				private final FieldWithMetaString stringAttr;
				
				protected Foo3Impl(Foo3.Foo3Builder builder) {
					this.attr = builder.getAttr();
					this.numberAttr = builder.getNumberAttrRestrictedAsInteger();
					this.parent = ofNullable(builder.getParent()).map(f->f.build()).orElse(null);
					this.parentList = ofNullable(builder.getParentListRestrictedAsSingleChild()).map(f->f.build()).orElse(null);
					this.stringAttr = ofNullable(builder.getStringAttr()).map(f->f.build()).orElse(null);
				}
				
				@Override
				@RosettaAttribute("attr")
				public Integer getAttr() {
					return attr;
				}
				
				@Override
				@RosettaAttribute("numberAttr")
				public Integer getNumberAttrRestrictedAsInteger() {
					return numberAttr;
				}
				
				@Override
				public BigInteger getNumberAttrRestrictedAsBigInteger() {
					return numberAttr == null ? null : BigInteger.valueOf(numberAttr);
				}
				
				@Override
				public BigDecimal getNumberAttr() {
					return numberAttr == null ? null : BigDecimal.valueOf(numberAttr);
				}
				
				@Override
				@RosettaAttribute("parent")
				public Child getParent() {
					return parent;
				}
				
				@Override
				@RosettaAttribute("parentList")
				public GrandChild getParentListRestrictedAsSingleChild() {
					return parentList;
				}
				
				@Override
				public List<? extends Parent> getParentList() {
					return parentList == null ? Collections.<Parent>emptyList() : Collections.singletonList(parentList);
				}
				
				@Override
				@RosettaAttribute("stringAttr")
				public FieldWithMetaString getStringAttr() {
					return stringAttr;
				}
				
				@Override
				public Foo3 build() {
					return this;
				}
				
				@Override
				public Foo3.Foo3Builder toBuilder() {
					Foo3.Foo3Builder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(Foo3.Foo3Builder builder) {
					ofNullable(getAttr()).ifPresent(builder::setAttr);
					ofNullable(getNumberAttrRestrictedAsInteger()).ifPresent(builder::setNumberAttr);
					ofNullable(getParent()).ifPresent(builder::setParent);
					ofNullable(getParentListRestrictedAsSingleChild()).ifPresent(builder::setParentList);
					ofNullable(getStringAttr()).ifPresent(builder::setStringAttr);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					Foo3 _that = getType().cast(o);
				
					if (!Objects.equals(attr, _that.getAttr())) return false;
					if (!Objects.equals(numberAttr, _that.getNumberAttrRestrictedAsInteger())) return false;
					if (!Objects.equals(parent, _that.getParent())) return false;
					if (!Objects.equals(parentList, _that.getParentListRestrictedAsSingleChild())) return false;
					if (!Objects.equals(stringAttr, _that.getStringAttr())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
					_result = 31 * _result + (numberAttr != null ? numberAttr.hashCode() : 0);
					_result = 31 * _result + (parent != null ? parent.hashCode() : 0);
					_result = 31 * _result + (parentList != null ? parentList.hashCode() : 0);
					_result = 31 * _result + (stringAttr != null ? stringAttr.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "Foo3 {" +
						"attr=" + this.attr + ", " +
						"numberAttr=" + this.numberAttr + ", " +
						"parent=" + this.parent + ", " +
						"parentList=" + this.parentList + ", " +
						"stringAttr=" + this.stringAttr +
					'}';
				}
			}
		
			/*********************** Builder Implementation of Foo3  ***********************/
			class Foo3BuilderImpl implements Foo3.Foo3Builder {
			
				protected Integer attr;
				protected Integer numberAttr;
				protected Child.ChildBuilder parent;
				protected GrandChild.GrandChildBuilder parentList;
				protected FieldWithMetaString.FieldWithMetaStringBuilder stringAttr;
				
				@Override
				@RosettaAttribute("attr")
				public Integer getAttr() {
					return attr;
				}
				
				@Override
				@RosettaAttribute("numberAttr")
				public Integer getNumberAttrRestrictedAsInteger() {
					return numberAttr;
				}
				@Override
				public BigInteger getNumberAttrRestrictedAsBigInteger() {
					return numberAttr == null ? null : BigInteger.valueOf(numberAttr);
				}
				
				@Override
				public BigDecimal getNumberAttr() {
					return numberAttr == null ? null : BigDecimal.valueOf(numberAttr);
				}
				
				
				@Override
				@RosettaAttribute("parent")
				public Child.ChildBuilder getParent() {
					return parent;
				}
				
				@Override
				public Child.ChildBuilder getOrCreateParent() {
					Child.ChildBuilder result;
					if (parent!=null) {
						result = parent;
					}
					else {
						result = parent = Child.builder();
					}
					
					return result;
				}
				
				@Override
				@RosettaAttribute("parentList")
				public GrandChild.GrandChildBuilder getParentListRestrictedAsSingleChild() {
					return parentList;
				}
				@Override
				public List<? extends Parent.ParentBuilder> getParentList() {
					return parentList == null ? Collections.<Parent.ParentBuilder>emptyList() : Collections.singletonList(parentList);
				}
				
				
				@Override
				public GrandChild.GrandChildBuilder getOrCreateParentList() {
					GrandChild.GrandChildBuilder result;
					if (parentList!=null) {
						result = parentList;
					}
					else {
						result = parentList = GrandChild.builder();
					}
					
					return result;
				}
				
				@Override
				public GrandChild.GrandChildBuilder getOrCreateParentList(int _index) {
					return getOrCreateParentList();
				}
				
				@Override
				@RosettaAttribute("stringAttr")
				public FieldWithMetaString.FieldWithMetaStringBuilder getStringAttr() {
					return stringAttr;
				}
				
				@Override
				public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateStringAttr() {
					FieldWithMetaString.FieldWithMetaStringBuilder result;
					if (stringAttr!=null) {
						result = stringAttr;
					}
					else {
						result = stringAttr = FieldWithMetaString.builder();
					}
					
					return result;
				}
				
				@Override
				@RosettaAttribute("attr")
				public Foo3.Foo3Builder setAttr(Integer _attr) {
					this.attr = _attr == null ? null : _attr;
					return this;
				}
				
				@Override
				@RosettaAttribute("numberAttr")
				public Foo3.Foo3Builder setNumberAttr(Integer _numberAttr) {
					this.numberAttr = _numberAttr == null ? null : _numberAttr;
					return this;
				}
				
				@Override
				public Foo3.Foo3Builder setNumberAttr(BigInteger _numberAttr) {
					final Integer ifThenElseResult;
					if (_numberAttr == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = BigInteger.valueOf(_numberAttr.intValue()).equals(_numberAttr) ? _numberAttr.intValue() : null;
					}
					return setNumberAttr(ifThenElseResult);
				}
				
				@Override
				public Foo3.Foo3Builder setNumberAttr(BigDecimal _numberAttr) {
					final Integer ifThenElseResult;
					if (_numberAttr == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = BigDecimal.valueOf(_numberAttr.intValue()).compareTo(_numberAttr) == 0 ? _numberAttr.intValue() : null;
					}
					return setNumberAttr(ifThenElseResult);
				}
				
				@Override
				@RosettaAttribute("parent")
				public Foo3.Foo3Builder setParent(Child _parent) {
					this.parent = _parent == null ? null : _parent.toBuilder();
					return this;
				}
				
				@Override
				public Foo3.Foo3Builder setParent(Parent _parent) {
					final Child ifThenElseResult;
					if (_parent == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parent instanceof Child ? Child.class.cast(_parent) : null;
					}
					return setParent(ifThenElseResult);
				}
				
				@Override
				@RosettaAttribute("parentList")
				public Foo3.Foo3Builder setParentList(GrandChild _parentList) {
					this.parentList = _parentList == null ? null : _parentList.toBuilder();
					return this;
				}
				
				@Override
				public Foo3.Foo3Builder setParentList(Child _parentList) {
					final GrandChild ifThenElseResult;
					if (_parentList == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parentList instanceof GrandChild ? GrandChild.class.cast(_parentList) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override
				public Foo3.Foo3Builder addParentList(Parent parentList0) {
					final GrandChild ifThenElseResult;
					if (parentList0 == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = parentList0 instanceof GrandChild ? GrandChild.class.cast(parentList0) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override
				public Foo3.Foo3Builder addParentList(Parent _parentList, int _idx) {
					final GrandChild ifThenElseResult;
					if (_parentList == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parentList instanceof GrandChild ? GrandChild.class.cast(_parentList) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override 
				public Foo3.Foo3Builder addParentList(List<? extends Parent> parentLists) {
					final Parent _parent = MapperC.of(parentLists).get();
					final GrandChild ifThenElseResult;
					if (_parent == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parent instanceof GrandChild ? GrandChild.class.cast(_parent) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override 
				public Foo3.Foo3Builder setParentList(List<? extends Parent> parentLists) {
					final Parent _parent = MapperC.of(parentLists).get();
					final GrandChild ifThenElseResult;
					if (_parent == null) {
						ifThenElseResult = null;
					} else {
						ifThenElseResult = _parent instanceof GrandChild ? GrandChild.class.cast(_parent) : null;
					}
					return setParentList(ifThenElseResult);
				}
				
				@Override
				@RosettaAttribute("stringAttr")
				public Foo3.Foo3Builder setStringAttr(FieldWithMetaString _stringAttr) {
					this.stringAttr = _stringAttr == null ? null : _stringAttr.toBuilder();
					return this;
				}
				
				@Override
				public Foo3.Foo3Builder setStringAttrValue(String _stringAttr) {
					this.getOrCreateStringAttr().setValue(_stringAttr);
					return this;
				}
				
				@Override
				public Foo3 build() {
					return new Foo3.Foo3Impl(this);
				}
				
				@Override
				public Foo3.Foo3Builder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public Foo3.Foo3Builder prune() {
					if (parent!=null && !parent.prune().hasData()) parent = null;
					if (parentList!=null && !parentList.prune().hasData()) parentList = null;
					if (stringAttr!=null && !stringAttr.prune().hasData()) stringAttr = null;
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getAttr()!=null) return true;
					if (getNumberAttrRestrictedAsInteger()!=null) return true;
					if (getParent()!=null && getParent().hasData()) return true;
					if (getParentListRestrictedAsSingleChild()!=null && getParentListRestrictedAsSingleChild().hasData()) return true;
					if (getStringAttr()!=null) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public Foo3.Foo3Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					Foo3.Foo3Builder o = (Foo3.Foo3Builder) other;
					
					merger.mergeRosetta(getParent(), o.getParent(), this::setParent);
					merger.mergeRosetta(getParentListRestrictedAsSingleChild(), o.getParentListRestrictedAsSingleChild(), this::setParentList);
					merger.mergeRosetta(getStringAttr(), o.getStringAttr(), this::setStringAttr);
					
					merger.mergeBasic(getAttr(), o.getAttr(), this::setAttr);
					merger.mergeBasic(getNumberAttrRestrictedAsInteger(), o.getNumberAttrRestrictedAsInteger(), this::setNumberAttr);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					Foo3 _that = getType().cast(o);
				
					if (!Objects.equals(attr, _that.getAttr())) return false;
					if (!Objects.equals(numberAttr, _that.getNumberAttrRestrictedAsInteger())) return false;
					if (!Objects.equals(parent, _that.getParent())) return false;
					if (!Objects.equals(parentList, _that.getParentListRestrictedAsSingleChild())) return false;
					if (!Objects.equals(stringAttr, _that.getStringAttr())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
					_result = 31 * _result + (numberAttr != null ? numberAttr.hashCode() : 0);
					_result = 31 * _result + (parent != null ? parent.hashCode() : 0);
					_result = 31 * _result + (parentList != null ? parentList.hashCode() : 0);
					_result = 31 * _result + (stringAttr != null ? stringAttr.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "Foo3Builder {" +
						"attr=" + this.attr + ", " +
						"numberAttr=" + this.numberAttr + ", " +
						"parent=" + this.parent + ", " +
						"parentList=" + this.parentList + ", " +
						"stringAttr=" + this.stringAttr +
					'}';
				}
			}
		}
		''')
	}
	
	@Test
	def void testFoo3Validator() {
		assertGeneratedCode('com.rosetta.test.model.validation.Foo3Validator', '''
		package com.rosetta.test.model.validation;
		
		import com.google.common.collect.Lists;
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.validation.ValidationResult;
		import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
		import com.rosetta.model.lib.validation.Validator;
		import com.rosetta.model.metafields.FieldWithMetaString;
		import com.rosetta.test.model.Child;
		import com.rosetta.test.model.Foo3;
		import com.rosetta.test.model.GrandChild;
		import java.util.List;
		
		import static com.google.common.base.Strings.isNullOrEmpty;
		import static com.rosetta.model.lib.expression.ExpressionOperators.checkCardinality;
		import static com.rosetta.model.lib.validation.ValidationResult.failure;
		import static com.rosetta.model.lib.validation.ValidationResult.success;
		import static java.util.stream.Collectors.joining;
		import static java.util.stream.Collectors.toList;
		
		public class Foo3Validator implements Validator<Foo3> {
		
			private List<ComparisonResult> getComparisonResults(Foo3 o) {
				return Lists.<ComparisonResult>newArrayList(
						checkCardinality("attr", (Integer) o.getAttr() != null ? 1 : 0, 1, 1), 
						checkCardinality("numberAttr", (Integer) o.getNumberAttrRestrictedAsInteger() != null ? 1 : 0, 1, 1), 
						checkCardinality("parent", (Child) o.getParent() != null ? 1 : 0, 1, 1), 
						checkCardinality("parentList", (GrandChild) o.getParentListRestrictedAsSingleChild() != null ? 1 : 0, 1, 1), 
						checkCardinality("stringAttr", (FieldWithMetaString) o.getStringAttr() != null ? 1 : 0, 1, 1)
					);
			}
		
			@Override
			public ValidationResult<Foo3> validate(RosettaPath path, Foo3 o) {
				String error = getComparisonResults(o)
					.stream()
					.filter(res -> !res.get())
					.map(res -> res.getError())
					.collect(joining("; "));
		
				if (!isNullOrEmpty(error)) {
					return failure("Foo3", ValidationType.CARDINALITY, "Foo3", path, "", error);
				}
				return success("Foo3", ValidationType.CARDINALITY, "Foo3", path, "");
			}
		
			@Override
			public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo3 o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!isNullOrEmpty(res.getError())) {
							return failure("Foo3", ValidationType.CARDINALITY, "Foo3", path, "", res.getError());
						}
						return success("Foo3", ValidationType.CARDINALITY, "Foo3", path, "");
					})
					.collect(toList());
			}
		
		}
		''')
	}
	
	@Test
	def void testFoo3TypeFormatValidator() {
		assertGeneratedCode('com.rosetta.test.model.validation.Foo3TypeFormatValidator', '''
		package com.rosetta.test.model.validation;
		
		import com.google.common.collect.Lists;
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.validation.ValidationResult;
		import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
		import com.rosetta.model.lib.validation.Validator;
		import com.rosetta.test.model.Foo3;
		import java.util.List;
		
		import static com.google.common.base.Strings.isNullOrEmpty;
		import static com.rosetta.model.lib.expression.ExpressionOperators.checkNumber;
		import static com.rosetta.model.lib.expression.ExpressionOperators.checkString;
		import static com.rosetta.model.lib.validation.ValidationResult.failure;
		import static com.rosetta.model.lib.validation.ValidationResult.success;
		import static java.util.Optional.empty;
		import static java.util.Optional.of;
		import static java.util.stream.Collectors.joining;
		import static java.util.stream.Collectors.toList;
		
		public class Foo3TypeFormatValidator implements Validator<Foo3> {
		
			private List<ComparisonResult> getComparisonResults(Foo3 o) {
				return Lists.<ComparisonResult>newArrayList(
						checkNumber("attr", o.getAttr(), empty(), of(0), empty(), empty()), 
						checkNumber("numberAttr", o.getNumberAttrRestrictedAsInteger(), empty(), of(0), empty(), empty()), 
						checkString("stringAttr", o.getStringAttr().getValue(), 0, of(42), empty())
					);
			}
		
			@Override
			public ValidationResult<Foo3> validate(RosettaPath path, Foo3 o) {
				String error = getComparisonResults(o)
					.stream()
					.filter(res -> !res.get())
					.map(res -> res.getError())
					.collect(joining("; "));
		
				if (!isNullOrEmpty(error)) {
					return failure("Foo3", ValidationType.TYPE_FORMAT, "Foo3", path, "", error);
				}
				return success("Foo3", ValidationType.TYPE_FORMAT, "Foo3", path, "");
			}
		
			@Override
			public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo3 o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!isNullOrEmpty(res.getError())) {
							return failure("Foo3", ValidationType.TYPE_FORMAT, "Foo3", path, "", res.getError());
						}
						return success("Foo3", ValidationType.TYPE_FORMAT, "Foo3", path, "");
					})
					.collect(toList());
			}
		
		}
		''')
	}
	
	@Test
	def void testFoo3OnlyExistsValidator() {
		assertGeneratedCode('com.rosetta.test.model.validation.exists.Foo3OnlyExistsValidator', '''
		package com.rosetta.test.model.validation.exists;
		
		import com.google.common.collect.ImmutableMap;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.validation.ExistenceChecker;
		import com.rosetta.model.lib.validation.ValidationResult;
		import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
		import com.rosetta.model.lib.validation.ValidatorWithArg;
		import com.rosetta.model.metafields.FieldWithMetaString;
		import com.rosetta.test.model.Child;
		import com.rosetta.test.model.Foo3;
		import com.rosetta.test.model.GrandChild;
		import java.util.Map;
		import java.util.Set;
		import java.util.stream.Collectors;
		
		import static com.rosetta.model.lib.validation.ValidationResult.failure;
		import static com.rosetta.model.lib.validation.ValidationResult.success;
		
		public class Foo3OnlyExistsValidator implements ValidatorWithArg<Foo3, Set<String>> {
		
			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends Foo3> ValidationResult<Foo3> validate(RosettaPath path, T2 o, Set<String> fields) {
				Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
						.put("attr", ExistenceChecker.isSet((Integer) o.getAttr()))
						.put("numberAttr", ExistenceChecker.isSet((Integer) o.getNumberAttrRestrictedAsInteger()))
						.put("parent", ExistenceChecker.isSet((Child) o.getParent()))
						.put("parentList", ExistenceChecker.isSet((GrandChild) o.getParentListRestrictedAsSingleChild()))
						.put("stringAttr", ExistenceChecker.isSet((FieldWithMetaString) o.getStringAttr()))
						.build();
				
				// Find the fields that are set
				Set<String> setFields = fieldExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.map(Map.Entry::getKey)
						.collect(Collectors.toSet());
				
				if (setFields.equals(fields)) {
					return success("Foo3", ValidationType.ONLY_EXISTS, "Foo3", path, "");
				}
				return failure("Foo3", ValidationType.ONLY_EXISTS, "Foo3", path, "",
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
			}
		}
		''')
	}
}