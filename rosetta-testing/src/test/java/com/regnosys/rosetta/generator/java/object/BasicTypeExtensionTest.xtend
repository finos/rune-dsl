package com.regnosys.rosetta.generator.java.object

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class BasicTypeExtensionTest {

	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper

	@Test
	def void testPojoGenerationForDataExtendingDataAliasExtendingBasicType() {
		val code = '''
			type MyString extends Alias:
				foo int (1..1)
			
			typeAlias Alias: Max3StringWithScheme
			
			type Max3StringWithScheme extends string(maxLength: 3):
				scheme string (1..1)
		'''.generateCode

		val myStringCode = code.get(rootPackage + ".MyString")
		val expectedMyStringCode = '''
		package com.rosetta.test.model;
		
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.test.model.Max3StringWithScheme;
		import com.rosetta.test.model.Max3StringWithScheme.Max3StringWithSchemeBuilder;
		import com.rosetta.test.model.Max3StringWithScheme.Max3StringWithSchemeBuilderImpl;
		import com.rosetta.test.model.Max3StringWithScheme.Max3StringWithSchemeImpl;
		import com.rosetta.test.model.MyString;
		import com.rosetta.test.model.MyString.MyStringBuilder;
		import com.rosetta.test.model.MyString.MyStringBuilderImpl;
		import com.rosetta.test.model.MyString.MyStringImpl;
		import com.rosetta.test.model.meta.MyStringMeta;
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version test
		 */
		@RosettaDataType(value="MyString", builder=MyString.MyStringBuilderImpl.class, version="test")
		public interface MyString extends Max3StringWithScheme {
		
			MyStringMeta metaData = new MyStringMeta();
		
			/*********************** Getter Methods  ***********************/
			Integer getFoo();
		
			/*********************** Build Methods  ***********************/
			MyString build();
			
			MyString.MyStringBuilder toBuilder();
			
			static MyString.MyStringBuilder builder() {
				return new MyString.MyStringBuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends MyString> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends MyString> getType() {
				return MyString.class;
			}
			
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
				processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this);
				processor.processBasic(path.newSubPath("foo"), Integer.class, getFoo(), this);
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface MyStringBuilder extends MyString, Max3StringWithScheme.Max3StringWithSchemeBuilder {
				MyString.MyStringBuilder setValue(String value);
				MyString.MyStringBuilder setScheme(String scheme);
				MyString.MyStringBuilder setFoo(Integer foo);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
					processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this);
					processor.processBasic(path.newSubPath("foo"), Integer.class, getFoo(), this);
				}
				
		
				MyString.MyStringBuilder prune();
			}
		
			/*********************** Immutable Implementation of MyString  ***********************/
			class MyStringImpl extends Max3StringWithScheme.Max3StringWithSchemeImpl implements MyString {
				private final Integer foo;
				
				protected MyStringImpl(MyString.MyStringBuilder builder) {
					super(builder);
					this.foo = builder.getFoo();
				}
				
				@Override
				@RosettaAttribute("foo")
				public Integer getFoo() {
					return foo;
				}
				
				@Override
				public MyString build() {
					return this;
				}
				
				@Override
				public MyString.MyStringBuilder toBuilder() {
					MyString.MyStringBuilder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(MyString.MyStringBuilder builder) {
					super.setBuilderFields(builder);
					ofNullable(getFoo()).ifPresent(builder::setFoo);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
					if (!super.equals(o)) return false;
				
					MyString _that = getType().cast(o);
				
					if (!Objects.equals(foo, _that.getFoo())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = super.hashCode();
					_result = 31 * _result + (foo != null ? foo.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "MyString {" +
						"foo=" + this.foo +
					'}' + " " + super.toString();
				}
			}
		
			/*********************** Builder Implementation of MyString  ***********************/
			class MyStringBuilderImpl extends Max3StringWithScheme.Max3StringWithSchemeBuilderImpl  implements MyString.MyStringBuilder {
			
				protected Integer foo;
			
				public MyStringBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("foo")
				public Integer getFoo() {
					return foo;
				}
				
				@Override
				@RosettaAttribute("value")
				public MyString.MyStringBuilder setValue(String value) {
					this.value = value==null?null:value;
					return this;
				}
				@Override
				@RosettaAttribute("scheme")
				public MyString.MyStringBuilder setScheme(String scheme) {
					this.scheme = scheme==null?null:scheme;
					return this;
				}
				@Override
				@RosettaAttribute("foo")
				public MyString.MyStringBuilder setFoo(Integer foo) {
					this.foo = foo==null?null:foo;
					return this;
				}
				
				@Override
				public MyString build() {
					return new MyString.MyStringImpl(this);
				}
				
				@Override
				public MyString.MyStringBuilder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public MyString.MyStringBuilder prune() {
					super.prune();
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (super.hasData()) return true;
					if (getFoo()!=null) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public MyString.MyStringBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					super.merge(other, merger);
					
					MyString.MyStringBuilder o = (MyString.MyStringBuilder) other;
					
					
					merger.mergeBasic(getFoo(), o.getFoo(), this::setFoo);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
					if (!super.equals(o)) return false;
				
					MyString _that = getType().cast(o);
				
					if (!Objects.equals(foo, _that.getFoo())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = super.hashCode();
					_result = 31 * _result + (foo != null ? foo.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "MyStringBuilder {" +
						"foo=" + this.foo +
					'}' + " " + super.toString();
				}
			}
		}
		'''
		assertEquals(expectedMyStringCode, myStringCode)
		
		val max3StringWithSchemeCode = code.get(rootPackage + ".Max3StringWithScheme")
		val expectedMax3StringWithSchemeCode = '''
		package com.rosetta.test.model;
		
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.annotations.RosettaAttribute;
		import com.rosetta.model.lib.annotations.RosettaDataType;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.test.model.Max3StringWithScheme;
		import com.rosetta.test.model.Max3StringWithScheme.Max3StringWithSchemeBuilder;
		import com.rosetta.test.model.Max3StringWithScheme.Max3StringWithSchemeBuilderImpl;
		import com.rosetta.test.model.Max3StringWithScheme.Max3StringWithSchemeImpl;
		import com.rosetta.test.model.meta.Max3StringWithSchemeMeta;
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		/**
		 * @version test
		 */
		@RosettaDataType(value="Max3StringWithScheme", builder=Max3StringWithScheme.Max3StringWithSchemeBuilderImpl.class, version="test")
		public interface Max3StringWithScheme extends RosettaModelObject {
		
			Max3StringWithSchemeMeta metaData = new Max3StringWithSchemeMeta();
		
			/*********************** Getter Methods  ***********************/
			String getValue();
			String getScheme();
		
			/*********************** Build Methods  ***********************/
			Max3StringWithScheme build();
			
			Max3StringWithScheme.Max3StringWithSchemeBuilder toBuilder();
			
			static Max3StringWithScheme.Max3StringWithSchemeBuilder builder() {
				return new Max3StringWithScheme.Max3StringWithSchemeBuilderImpl();
			}
		
			/*********************** Utility Methods  ***********************/
			@Override
			default RosettaMetaData<? extends Max3StringWithScheme> metaData() {
				return metaData;
			}
			
			@Override
			default Class<? extends Max3StringWithScheme> getType() {
				return Max3StringWithScheme.class;
			}
			
			
			@Override
			default void process(RosettaPath path, Processor processor) {
				processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
				processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this);
			}
			
		
			/*********************** Builder Interface  ***********************/
			interface Max3StringWithSchemeBuilder extends Max3StringWithScheme, RosettaModelObjectBuilder {
				Max3StringWithScheme.Max3StringWithSchemeBuilder setValue(String value);
				Max3StringWithScheme.Max3StringWithSchemeBuilder setScheme(String scheme);
		
				@Override
				default void process(RosettaPath path, BuilderProcessor processor) {
					processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
					processor.processBasic(path.newSubPath("scheme"), String.class, getScheme(), this);
				}
				
		
				Max3StringWithScheme.Max3StringWithSchemeBuilder prune();
			}
		
			/*********************** Immutable Implementation of Max3StringWithScheme  ***********************/
			class Max3StringWithSchemeImpl implements Max3StringWithScheme {
				private final String value;
				private final String scheme;
				
				protected Max3StringWithSchemeImpl(Max3StringWithScheme.Max3StringWithSchemeBuilder builder) {
					this.value = builder.getValue();
					this.scheme = builder.getScheme();
				}
				
				@Override
				@RosettaAttribute("value")
				public String getValue() {
					return value;
				}
				
				@Override
				@RosettaAttribute("scheme")
				public String getScheme() {
					return scheme;
				}
				
				@Override
				public Max3StringWithScheme build() {
					return this;
				}
				
				@Override
				public Max3StringWithScheme.Max3StringWithSchemeBuilder toBuilder() {
					Max3StringWithScheme.Max3StringWithSchemeBuilder builder = builder();
					setBuilderFields(builder);
					return builder;
				}
				
				protected void setBuilderFields(Max3StringWithScheme.Max3StringWithSchemeBuilder builder) {
					ofNullable(getValue()).ifPresent(builder::setValue);
					ofNullable(getScheme()).ifPresent(builder::setScheme);
				}
		
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					Max3StringWithScheme _that = getType().cast(o);
				
					if (!Objects.equals(value, _that.getValue())) return false;
					if (!Objects.equals(scheme, _that.getScheme())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (value != null ? value.hashCode() : 0);
					_result = 31 * _result + (scheme != null ? scheme.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "Max3StringWithScheme {" +
						"value=" + this.value + ", " +
						"scheme=" + this.scheme +
					'}';
				}
			}
		
			/*********************** Builder Implementation of Max3StringWithScheme  ***********************/
			class Max3StringWithSchemeBuilderImpl implements Max3StringWithScheme.Max3StringWithSchemeBuilder {
			
				protected String value;
				protected String scheme;
			
				public Max3StringWithSchemeBuilderImpl() {
				}
			
				@Override
				@RosettaAttribute("value")
				public String getValue() {
					return value;
				}
				
				@Override
				@RosettaAttribute("scheme")
				public String getScheme() {
					return scheme;
				}
				
				@Override
				@RosettaAttribute("value")
				public Max3StringWithScheme.Max3StringWithSchemeBuilder setValue(String value) {
					this.value = value==null?null:value;
					return this;
				}
				@Override
				@RosettaAttribute("scheme")
				public Max3StringWithScheme.Max3StringWithSchemeBuilder setScheme(String scheme) {
					this.scheme = scheme==null?null:scheme;
					return this;
				}
				
				@Override
				public Max3StringWithScheme build() {
					return new Max3StringWithScheme.Max3StringWithSchemeImpl(this);
				}
				
				@Override
				public Max3StringWithScheme.Max3StringWithSchemeBuilder toBuilder() {
					return this;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public Max3StringWithScheme.Max3StringWithSchemeBuilder prune() {
					return this;
				}
				
				@Override
				public boolean hasData() {
					if (getValue()!=null) return true;
					if (getScheme()!=null) return true;
					return false;
				}
			
				@SuppressWarnings("unchecked")
				@Override
				public Max3StringWithScheme.Max3StringWithSchemeBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					Max3StringWithScheme.Max3StringWithSchemeBuilder o = (Max3StringWithScheme.Max3StringWithSchemeBuilder) other;
					
					
					merger.mergeBasic(getValue(), o.getValue(), this::setValue);
					merger.mergeBasic(getScheme(), o.getScheme(), this::setScheme);
					return this;
				}
			
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
				
					Max3StringWithScheme _that = getType().cast(o);
				
					if (!Objects.equals(value, _that.getValue())) return false;
					if (!Objects.equals(scheme, _that.getScheme())) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (value != null ? value.hashCode() : 0);
					_result = 31 * _result + (scheme != null ? scheme.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "Max3StringWithSchemeBuilder {" +
						"value=" + this.value + ", " +
						"scheme=" + this.scheme +
					'}';
				}
			}
		}
		'''
		assertEquals(expectedMax3StringWithSchemeCode, max3StringWithSchemeCode)
	
		val max3StringWithSchemeTypeFormatValidatorCode = code.get("com.rosetta.test.model.validation.Max3StringWithSchemeTypeFormatValidator")
		val expectedMax3StringWithSchemeTypeFormatValidatorCode = '''
		package com.rosetta.test.model.validation;
		
		import com.google.common.collect.Lists;
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.validation.ValidationResult;
		import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
		import com.rosetta.model.lib.validation.Validator;
		import com.rosetta.test.model.Max3StringWithScheme;
		import java.util.List;
		
		import static com.google.common.base.Strings.isNullOrEmpty;
		import static com.rosetta.model.lib.validation.ValidationResult.failure;
		import static com.rosetta.model.lib.validation.ValidationResult.success;
		import static java.util.stream.Collectors.joining;
		import static java.util.stream.Collectors.toList;
		
		public class Max3StringWithSchemeTypeFormatValidator implements Validator<Max3StringWithScheme> {
		
			private List<ComparisonResult> getComparisonResults(Max3StringWithScheme o) {
				return Lists.<ComparisonResult>newArrayList(
					);
			}
		
			@Override
			public ValidationResult<Max3StringWithScheme> validate(RosettaPath path, Max3StringWithScheme o) {
				String error = getComparisonResults(o)
					.stream()
					.filter(res -> !res.get())
					.map(res -> res.getError())
					.collect(joining("; "));
		
				if (!isNullOrEmpty(error)) {
					return failure("Max3StringWithScheme", ValidationType.TYPE_FORMAT, "Max3StringWithScheme", path, "", error);
				}
				return success("Max3StringWithScheme", ValidationType.TYPE_FORMAT, "Max3StringWithScheme", path, "");
			}
		
			@Override
			public List<ValidationResult<?>> getValidationResults(RosettaPath path, Max3StringWithScheme o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!isNullOrEmpty(res.getError())) {
							return failure("Max3StringWithScheme", ValidationType.TYPE_FORMAT, "Max3StringWithScheme", path, "", res.getError());
						}
						return success("Max3StringWithScheme", ValidationType.TYPE_FORMAT, "Max3StringWithScheme", path, "");
					})
					.collect(toList());
			}
		
		}
		'''
		assertEquals(expectedMax3StringWithSchemeTypeFormatValidatorCode, max3StringWithSchemeTypeFormatValidatorCode)
		
		code.compileToClasses
	}
	
}