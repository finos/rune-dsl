package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Multimaps
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import java.util.Collection
import java.util.Collections
import org.eclipse.xtext.generator.IFileSystemAccess2

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaFullType
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.regnosys.rosetta.rosetta.simple.Data

class MetaFieldGenerator {
	def generate(IFileSystemAccess2 fsa, Iterable<RosettaMetaType> metaTypes, Iterable<RosettaRootElement> allClasses, Iterable<String> namespaces) {
		
		val namespaceToMetas = Multimaps.index(metaTypes, [namespace]).asMap
		for (namespace:namespaces) {
			val rosettaMetas = namespaceToMetas.getOrDefault(namespace, Collections.emptyList)
			val packages = new RosettaJavaPackages(namespace)
			fsa.generateFile('''«packages.metaField.directoryName»/MetaFields.java''',
				metaFields(packages, rosettaMetas))
			
		}
			

		//find all the reference types
		val namespaceClasses =   Multimaps.index(allClasses, [c|c.namespace]).asMap
		for (nsc:namespaceClasses.entrySet) {
			val packages = new RosettaJavaPackages(nsc.key);
			val refs = nsc.value.flatMap[expandedAttributes].filter[hasMetas && metas.exists[name=="reference"]].map[type].toSet
			
			for (ref:refs) {
				if (ref instanceof RosettaClass || ref instanceof Data)
					fsa.generateFile('''«packages.metaField.directoryName»/ReferenceWithMeta«ref.name.toFirstUpper».java''', referenceWithMeta(packages, ref))
				else
					fsa.generateFile('''«packages.metaField.directoryName»/BasicReferenceWithMeta«ref.name.toFirstUpper».java''', basicReferenceWithMeta(packages, ref))
			}
			//find all the metaed types
			val metas =  nsc.value.flatMap[expandedAttributes].filter[hasMetas && !metas.exists[name=="reference"]].map[type].toSet
			for (meta:metas) {
				fsa.generateFile('''«packages.metaField.directoryName»/FieldWithMeta«meta.name.toFirstUpper».java''', fieldWithMeta(packages, meta))
			}
		}

	}
	
	def metaFields(RosettaJavaPackages packages, Collection<RosettaMetaType> utypes) {
		val stringType = RosettaFactoryImpl.eINSTANCE.createRosettaBasicType
		stringType.name="string"
		val rosettaKeyType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		rosettaKeyType.setName("globalKey")
		rosettaKeyType.type = stringType;
		val externalKeyType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		externalKeyType.setName("externalKey")
		externalKeyType.type = stringType;
		val filteredTypes = utypes.filter[t|t.name!="id" && t.name!="reference"].toList();
		filteredTypes.add(rosettaKeyType)
		filteredTypes.add(externalKeyType)
		
	'''
		package «packages.metaField.packageName»;
		
		import static java.util.Optional.ofNullable;
		
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		
		public class MetaFields extends RosettaModelObject implements com.rosetta.model.lib.meta.MetaFieldsI{
			«FOR type:filteredTypes»
				private final «type.type.name.toJavaFullType» «type.name.toFirstLower»;
			«ENDFOR»
			private static BasicRosettaMetaData<MetaFields> metaData = new BasicRosettaMetaData<>();
			
			@Override
			public RosettaMetaData<? extends MetaFields> metaData() {
				return metaData;
			}
			
			private MetaFields(MetaFieldsBuilder builder) {
				«FOR type:filteredTypes»
					«type.name.toFirstLower» = builder.get«type.name.toFirstUpper»();
				«ENDFOR»
			}
			
			«FOR type:filteredTypes»
				public «type.type.name.toJavaFullType» get«type.name.toFirstUpper»() {
					return «type.name.toFirstLower»;
				}
			«ENDFOR»
			
			public MetaFieldsBuilder toBuilder() {
				MetaFieldsBuilder builder = new MetaFieldsBuilder();
				«FOR type:filteredTypes»
					ofNullable(get«type.name.toFirstUpper»()).ifPresent(builder::set«type.name.toFirstUpper»);
				«ENDFOR»
				return builder;
			}
			
			public static MetaFieldsBuilder builder() {
				return new MetaFieldsBuilder();
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int _result = 1;
				«FOR type:filteredTypes»
					_result = prime * _result + ((«type.name.toFirstLower» == null) ? 0 : «type.name.toFirstLower».hashCode());
				«ENDFOR»
				return _result;
			}
		
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null || getClass() != obj.getClass())
					return false;
				MetaFields other = (MetaFields) obj;
				«FOR type:filteredTypes»
					if («type.name.toFirstLower» != null ? !«type.name.toFirstLower».equals(other.«type.name.toFirstLower») : other.«type.name.toFirstLower»!=null) return false;
				«ENDFOR»
				return true;
			}
		
			@Override
			public String toString() {
				return "MetaFields {" +
				«FOR type:filteredTypes»
						"«type.name.toFirstLower»=" + this.«type.name.toFirstLower» + ", " +
				«ENDFOR»
				'}';
			}
			
			public static class MetaFieldsBuilder extends RosettaModelObjectBuilder implements com.rosetta.model.lib.meta.MetaFieldsI.MetaFieldsBuilderI{
				«FOR type:filteredTypes»
					private «type.type.name.toJavaFullType» «type.name.toFirstLower»;
				«ENDFOR»			
				
				@Override
				public RosettaMetaData<? extends MetaFields> metaData() {
					return metaData;
				}
				
				«FOR type:filteredTypes»
					public «type.type.name.toJavaFullType» get«type.name.toFirstUpper»() {
						return «type.name.toFirstLower»;
					}
				«ENDFOR»
				
				«FOR type:filteredTypes»
					public MetaFieldsBuilder set«type.name.toFirstUpper»(«type.type.name.toJavaFullType» «type.name.toFirstLower») {
						this.«type.name.toFirstLower» = «type.name.toFirstLower»;
						return this;
					}
				«ENDFOR»
				
				public MetaFields build() {
					return new MetaFields(this);
				}
				
				@Override
				public MetaFieldsBuilder prune() {
					return this;
				}
				
				@Override
				public boolean hasData() {
					«IF filteredTypes.empty»
					return false;
					«ELSE»
					return «FOR type:filteredTypes SEPARATOR " || \n"»«type.name.toFirstLower»!=null«ENDFOR»;
					«ENDIF»
				}
				
				@Override
				public void process(RosettaPath path, BuilderProcessor processor) {
					«FOR type:filteredTypes»
						processor.processBasic(path.newSubPath("«type.name.toFirstLower»"), «type.type.name.toJavaType».class, «type.name.toFirstLower», this, AttributeMeta.IS_META);
					«ENDFOR»
				}
				
				@Override
				public int hashCode() {
					final int prime = 31;
					int _result = 1;
					«FOR type:filteredTypes»
						_result = prime * _result + ((«type.name.toFirstLower» == null) ? 0 : «type.name.toFirstLower».hashCode());
					«ENDFOR»
					return _result;
				}
			
				@Override
				public boolean equals(Object obj) {
					if (this == obj)
						return true;
					if (obj == null || getClass() != obj.getClass())
						return false;
					MetaFieldsBuilder other = (MetaFieldsBuilder) obj;
					«FOR type:filteredTypes»
						if («type.name.toFirstLower» != null ? !«type.name.toFirstLower».equals(other.«type.name.toFirstLower») : other.«type.name.toFirstLower»!=null) return false;
					«ENDFOR»
					return true;
				}
			
				@Override
				public String toString() {
					return "MetaFieldsBuilder {" +
					«FOR type:filteredTypes»
							"«type.name.toFirstLower»=" + this.«type.name.toFirstLower» + ", " +
					«ENDFOR»
					'}';
				}
			}
			
			@Override
			protected void process(RosettaPath path, Processor processor) {
				«FOR type:filteredTypes»
					processor.processBasic(path.newSubPath("«type.name.toFirstLower»"), «type.type.name.toJavaType».class, «type.name.toFirstLower», this, AttributeMeta.IS_META);
				«ENDFOR»
			}
		}
	'''
	}
	
	def fieldWithMeta(RosettaJavaPackages packages, RosettaType type) '''		
		package «packages.metaField.packageName»;
		
		import static java.util.Optional.ofNullable;
		
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.meta.FieldWithMeta;
		import «packages.model.packageName».*;
		import «packages.lib.packageName».RosettaModelObject;
		import «packages.lib.packageName».RosettaModelObjectBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.records.Date;
		import java.math.BigDecimal;
		
		public class FieldWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements FieldWithMeta<«type.name.toJavaType»>{
			private final «type.name.toJavaType» value;
			private final MetaFields meta;
			private static BasicRosettaMetaData<FieldWithMeta«type.name.toFirstUpper»> metaData = new BasicRosettaMetaData<>();
						
			@Override
			public RosettaMetaData<? extends FieldWithMeta«type.name.toFirstUpper»> metaData() {
				return metaData;
			}
			
			private FieldWithMeta«type.name.toFirstUpper»(FieldWithMeta«type.name.toFirstUpper»Builder builder) {
				«IF type instanceof RosettaClass»
					value = ofNullable(builder.getValue()).map(v->v.build()).orElse(null);
				«ELSE»
					value = builder.getValue();
				«ENDIF»
				meta = ofNullable(builder.getMeta()).map(MetaFields.MetaFieldsBuilder::build).orElse(null);
			}
			
			public «type.name.toJavaType» getValue() {
				return value;
			}
			
			public MetaFields getMeta() {
				return meta;
			}
			
			public FieldWithMeta«type.name.toFirstUpper»Builder toBuilder() {
				FieldWithMeta«type.name.toFirstUpper»Builder builder = new FieldWithMeta«type.name.toFirstUpper»Builder();
				builder.setValue(value);
				builder.setMeta(meta);
				return builder;
			}
			
			public static FieldWithMeta«type.name.toFirstUpper»Builder builder() {
				return new FieldWithMeta«type.name.toFirstUpper»Builder();
			}
			
			@Override
			protected void process(RosettaPath path, Processor processor) {
				processRosetta(path.newSubPath("meta"), processor, MetaFields.class, meta, AttributeMeta.IS_META);
				«IF type instanceof RosettaClass»
					processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
				«ELSE»
					processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
				«ENDIF»
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int _result = 1;
				_result = prime * _result + ((meta == null) ? 0 : meta.hashCode());
				_result = prime * _result + ((value == null) ? 0 : value.hashCode());
				return _result;
			}
		
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null || getClass() != obj.getClass())
					return false;
				FieldWithMeta«type.name.toFirstUpper» other = (FieldWithMeta«type.name.toFirstUpper») obj;
				if (meta != null ? !meta.equals(other.meta) : other.meta!=null) return false;
				if (value != null ? !value.equals(other.value) : other.value!=null) return false;
				return true;
			}
		
			@Override
			public String toString() {
				return "FieldWithMeta«type.name.toFirstUpper» {" +
					"value=" + this.value + ", " +
					"meta=" + this.meta + ", " +
				'}';
			}
			
			public static class FieldWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder{
				«IF type instanceof RosettaClass»
					private «type.name».«type.name»Builder  value;
				«ELSE»
					private «type.name.toJavaType»  value;
				«ENDIF»
				private MetaFields.MetaFieldsBuilder meta;
				
				public FieldWithMeta«type.name.toFirstUpper»Builder() {}
				
				@Override
				public RosettaMetaData<? extends FieldWithMeta«type.name.toFirstUpper»> metaData() {
					return metaData;
				}
				
				«IF type instanceof RosettaClass»
					public «type.name».«type.name»Builder  getValue() {
				«ELSE»
					public «type.name.toJavaType» getValue() {
				«ENDIF»
					return value;
				}
				
				public MetaFields.MetaFieldsBuilder getMeta() {
					return meta;
				}
				
				public MetaFields.MetaFieldsBuilder getOrCreateMeta() {
					return meta=ofNullable(meta).orElseGet(MetaFields::builder);
				}
				
				«IF type instanceof RosettaClass»
					public FieldWithMeta«type.name.toFirstUpper»Builder setValueBuilder(«type.name».«type.name»Builder value) {
						this.value = value;
						return this;
					}
					
					public FieldWithMeta«type.name.toFirstUpper»Builder setValue(«type.name» value) {
						this.value = ofNullable(value).map(t->t.toBuilder()).orElse(null);
						return this;
					}
				«ELSE»
					public FieldWithMeta«type.name.toFirstUpper»Builder setValue(«type.name.toJavaType» value) {
						this.value = value;
						return this;
					}
				«ENDIF»
				
				public FieldWithMeta«type.name.toFirstUpper»Builder setMeta(MetaFields meta) {
					this.meta = ofNullable(meta).map(MetaFields::toBuilder).orElse(null);
					return this;
				}
				
				public FieldWithMeta«type.name.toFirstUpper»Builder setMetaBuilder(MetaFields.MetaFieldsBuilder meta) {
					this.meta = meta;
					return this;
				}
				
				public FieldWithMeta«type.name.toFirstUpper» build() {
					return new FieldWithMeta«type.name.toFirstUpper»(this);
				}
				
				@Override
				public FieldWithMeta«type.name.toFirstUpper»Builder prune() {
					return this;
				}
				
				@Override
				public boolean hasData() {
					return value!=null || value!=null;
				}
				
				@Override
				public void process(RosettaPath path, BuilderProcessor processor) {
					processRosetta(path.newSubPath("meta"), processor, MetaFields.class, meta, AttributeMeta.IS_META);
					«IF type instanceof RosettaClass»
						processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
					«ELSE»
						processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
					«ENDIF»
				}
				
				@Override
				public int hashCode() {
					final int prime = 31;
					int _result = 1;
					_result = prime * _result + ((meta == null) ? 0 : meta.hashCode());
					_result = prime * _result + ((value == null) ? 0 : value.hashCode());
					return _result;
				}
			
				@Override
				public boolean equals(Object obj) {
					if (this == obj)
						return true;
					if (obj == null || getClass() != obj.getClass())
						return false;
					FieldWithMeta«type.name.toFirstUpper»Builder other = (FieldWithMeta«type.name.toFirstUpper»Builder) obj;
					if (meta != null ? !meta.equals(other.meta) : other.meta!=null) return false;
					if (value != null ? !value.equals(other.value) : other.value!=null) return false;
					return true;
				}
			
				@Override
				public String toString() {
					return "FieldWithMeta«type.name.toFirstUpper»Builder {" +
						"value=" + this.value + ", " +
						"meta=" + this.meta + ", " +
					'}';
				}
			}
		}
	'''
	
	def referenceWithMeta(RosettaJavaPackages packages, RosettaType type) '''
		package «packages.metaField.packageName»;
		
		import static java.util.Optional.ofNullable;
		
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.meta.ReferenceWithMeta;
		import com.rosetta.model.lib.meta.ReferenceWithMetaBuilder;
		import «packages.model.packageName».*;
		import «packages.lib.packageName».RosettaModelObject;
		import «packages.lib.packageName».RosettaModelObjectBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		
		public class ReferenceWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements ReferenceWithMeta<«type.name»>{
			private final String globalReference;
			private final String externalReference;
			private final «type.name» value;
			
			private ReferenceWithMeta«type.name.toFirstUpper»(ReferenceWithMeta«type.name.toFirstUpper»Builder builder) {
				value = ofNullable(builder.getValue()).map(v->v.build()).orElse(null);
				globalReference = builder.globalReference;
				externalReference = builder.externalReference;
			}
			
			public «type.name» getValue() {
				return value;
			}
			
			public String getGlobalReference() {
				return globalReference;
			}
			
			public String getExternalReference() {
				return externalReference;
			}
			
			private static BasicRosettaMetaData<ReferenceWithMeta«type.name.toFirstUpper»> metaData = new BasicRosettaMetaData<>();
									
			@Override
			public RosettaMetaData<? extends ReferenceWithMeta«type.name.toFirstUpper»> metaData() {
				return metaData;
			}
			
			public ReferenceWithMeta«type.name.toFirstUpper»Builder toBuilder() {
				ReferenceWithMeta«type.name.toFirstUpper»Builder builder = new ReferenceWithMeta«type.name.toFirstUpper»Builder();
				builder.setValue(value);
				builder.setGlobalReference(globalReference);
				builder.setExternalReference(externalReference);
				return builder;
			}
			
			public static ReferenceWithMeta«type.name.toFirstUpper»Builder builder() {
				return new ReferenceWithMeta«type.name.toFirstUpper»Builder();
			}
			
			@Override
			protected void process(RosettaPath path, Processor processor) {
				processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
				processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.IS_META);
				processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.IS_META);
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int _result = 1;
				_result = prime * _result + ((globalReference == null) ? 0 : globalReference.hashCode());
				_result = prime * _result + ((externalReference == null) ? 0 : externalReference.hashCode());
				_result = prime * _result + ((value == null) ? 0 : value.hashCode());
				return _result;
			}
		
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null || getClass() != obj.getClass())
					return false;
				ReferenceWithMeta«type.name.toFirstUpper» other = (ReferenceWithMeta«type.name.toFirstUpper») obj;
				if (globalReference != null ? !globalReference.equals(other.globalReference) : other.globalReference!=null) return false;
				if (externalReference != null ? !externalReference.equals(other.externalReference) : other.externalReference!=null) return false;
				if (value != null ? !value.equals(other.value) : other.value!=null) return false;
				return true;
			}
		
			@Override
			public String toString() {
				return "ReferenceWitMeta {" +
					"globalReference=" + this.globalReference + ", " +
					"externalReference=" + this.externalReference + ", " +
					"value=" + this.value + ", " +
				'}';
			}
			
			public static class ReferenceWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder implements ReferenceWithMetaBuilder<«type.name.toJavaType»>{
				private «type.name».«type.name»Builder value;
				private String globalReference;
				private String externalReference;
				
				public ReferenceWithMeta«type.name.toFirstUpper»Builder() {}
				
				@Override
				public RosettaMetaData<? extends ReferenceWithMeta«type.name.toFirstUpper»> metaData() {
					return metaData;
				}
				
				public «type.name».«type.name»Builder getValue() {
					return value;
				}
				
				public String getGlobalReference() {
					return globalReference;
				}
				
				public String getExternalReference() {
					return externalReference;
				}
				
				@SuppressWarnings("unchecked")
				public ReferenceWithMeta«type.name.toFirstUpper»Builder setValue(«type.name» value) {
					this.value = ofNullable(value).map(t->t.toBuilder()).orElse(null);
					return this;
				}
				
				public ReferenceWithMeta«type.name.toFirstUpper»Builder setValueBuilder(«type.name».«type.name»Builder value) {
					this.value = value;
					return this;
				}
				
				public ReferenceWithMeta«type.name.toFirstUpper»Builder setGlobalReference(String reference) {
					this.globalReference = reference;
					return this;
				}
				
				public ReferenceWithMeta«type.name.toFirstUpper»Builder setExternalReference(String reference) {
					this.externalReference = reference;
					return this;
				}
				
				public ReferenceWithMeta«type.name.toFirstUpper» build() {
					return new ReferenceWithMeta«type.name.toFirstUpper»(this);
				}
				
				@Override
				public ReferenceWithMeta«type.name.toFirstUpper»Builder prune() {
					if (value!=null && !value.hasData()) value = null;
					return this;
				}
				
				@Override
				public boolean hasData() {
					return !(value==null && globalReference==null && externalReference==null);
				}
				
				@Override
				public void process(RosettaPath path, BuilderProcessor processor) {
					processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
					processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.IS_META);
					processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.IS_META);
				}
			}
		}
	'''
	
	def basicReferenceWithMeta(RosettaJavaPackages packages,RosettaType type) '''
	package «packages.metaField.packageName»;
		
	import static java.util.Optional.ofNullable;
		
	import com.rosetta.model.lib.process.*;
	import com.rosetta.model.lib.path.RosettaPath;
	import com.rosetta.model.lib.meta.ReferenceWithMeta;
	import com.rosetta.model.lib.meta.ReferenceWithMetaBuilder;
	import com.rosetta.model.lib.RosettaModelObject;
	import com.rosetta.model.lib.RosettaModelObjectBuilder;
	import com.rosetta.model.lib.meta.RosettaMetaData;
	import com.rosetta.model.lib.meta.BasicRosettaMetaData;
	import com.rosetta.model.lib.records.Date;
	import java.math.BigDecimal;
	
	public class BasicReferenceWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements ReferenceWithMeta<«type.name.toJavaType»>{
		private final String globalReference;
		private final String externalReference;
		private final «type.name.toJavaType» value;
		
		private BasicReferenceWithMeta«type.name.toFirstUpper»(BasicReferenceWithMeta«type.name.toFirstUpper»Builder builder){
			value = builder.getValue();
			globalReference = builder.globalReference;
			externalReference = builder.externalReference;
		}
		
		public «type.name.toJavaType» getValue() {
			return value;
		}
		
		public String getGlobalReference() {
			return globalReference;
		}
		
		public String getExternalReference() {
			return externalReference;
		}
		
		private static BasicRosettaMetaData<BasicReferenceWithMeta«type.name.toFirstUpper»> metaData = new BasicRosettaMetaData<>();
		
		@Override
		public RosettaMetaData<? extends BasicReferenceWithMeta«type.name.toFirstUpper»> metaData() {
			return metaData;
		}
		
		public BasicReferenceWithMeta«type.name.toFirstUpper»Builder toBuilder() {
			BasicReferenceWithMeta«type.name.toFirstUpper»Builder builder = new BasicReferenceWithMeta«type.name.toFirstUpper»Builder();
			builder.setValue(value);
			builder.setGlobalReference(globalReference);
			builder.setExternalReference(externalReference);
			return builder;
		}
		
		public static BasicReferenceWithMeta«type.name.toFirstUpper»Builder builder() {
			return new BasicReferenceWithMeta«type.name.toFirstUpper»Builder();
		}
		
		@Override
		protected void process(RosettaPath path, Processor processor) {
			processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
			processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.IS_META);
			processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.IS_META);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int _result = 1;
			_result = prime * _result + ((globalReference == null) ? 0 : globalReference.hashCode());
			_result = prime * _result + ((externalReference == null) ? 0 : externalReference.hashCode());
			_result = prime * _result + ((value == null) ? 0 : value.hashCode());
			return _result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			BasicReferenceWithMeta«type.name.toFirstUpper» other = (BasicReferenceWithMeta«type.name.toFirstUpper») obj;
			if (globalReference != null ? !globalReference.equals(other.globalReference) : other.globalReference!=null) return false;
			if (externalReference != null ? !externalReference.equals(other.externalReference) : other.externalReference!=null) return false;
			if (value != null ? !value.equals(other.value) : other.value!=null) return false;
			return true;
		}
	
		@Override
		public String toString() {
			return "BasicReferenceWitMeta {" +
				"globalReference=" + this.globalReference + ", " +
				"externalReference=" + this.externalReference + ", " +
				"value=" + this.value + ", " +
			'}';
		}
		
		public static class BasicReferenceWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder implements ReferenceWithMetaBuilder<«type.name.toJavaType»>{
			private «type.name.toJavaType» value;
			private String globalReference;
			private String externalReference;
			
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder() {}
			
			@Override
			public RosettaMetaData<? extends BasicReferenceWithMeta«type.name.toFirstUpper»> metaData() {
				return metaData;
			}
			
			public «type.name.toJavaType» getValue() {
				return value;
			}
			
			public String getGlobalReference() {
				return globalReference;
			}
			
			public String getExternalReference() {
				return externalReference;
			}
			
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder setValue(«type.name.toJavaType» value) {
				this.value = value;
				return this;
			}
			
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder setGlobalReference(String reference) {
				this.globalReference = reference;
				return this;
			}
			
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder setExternalReference(String reference) {
				this.externalReference = reference;
				return this;
			}
			
			public BasicReferenceWithMeta«type.name.toFirstUpper» build() {
				return new BasicReferenceWithMeta«type.name.toFirstUpper»(this);
			}
			
			@Override
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder prune() {
				return this;
			}
			
			@Override
			public boolean hasData() {
				return !(value==null && globalReference==null && externalReference==null);
			}
			
			@Override
			public void process(RosettaPath path, BuilderProcessor processor) {
				processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
				processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.IS_META);
				processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.IS_META);
			}
		}
	}'''
	
	def namespace(RosettaRootElement rc) {
		return rc.model.header.namespace
	}
}
