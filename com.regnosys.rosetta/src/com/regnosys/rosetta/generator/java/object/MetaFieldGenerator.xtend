package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Multimaps
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.object.ExpandedType
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import com.regnosys.rosetta.rosetta.simple.Data
import java.util.Collection
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaFullType
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class MetaFieldGenerator {
	
	 
	def void generate(RosettaJavaPackages packages, Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ctx) {
		// moved from RosettaGenerator
		val model = resource.contents.filter(RosettaModel).head
		if((model?.name).nullOrEmpty){
			return
		}
		
		
// TODO - This code is intended to only generate MetaFields.java once per name space. This however causes an issue when running with the incremental builder that deletes the file as a clean up and never re-generates it.
//		if (resource.resourceSet.adapterFactories.filter(MarkerAdapterFactory).findFirst[namespace == model.name] === null) {
//			try {
				val allModels = resource.resourceSet.resources.flatMap[contents].filter(RosettaModel).toList
				val allMetaTypes = allModels.flatMap[elements].filter(RosettaMetaType).toList
				fsa.generateFile('''«packages.basicMetafields.directoryName»/MetaFields.java''',
				metaFields(packages, allMetaTypes))
				
//			} finally {
//				resource.resourceSet.adapterFactories.add(new MarkerAdapterFactory(model.name))
//			}
//		}
		
		val modelClasses = model.elements.filter [
			it instanceof RosettaClass || it instanceof Data
		]
		if (modelClasses.empty) {
			return
		}
		
		//find all the reference types
		val namespaceClasses = Multimaps.index(modelClasses, [c|c.namespace]).asMap
		for (nsc: namespaceClasses.entrySet) {
			if (ctx.cancelIndicator.canceled) {
				return
			}
			val refs = nsc.value.flatMap[expandedAttributes].filter[hasMetas && metas.exists[name=="reference"]].map[type].toSet
			
			for (ref:refs) {
				val targetModel = ref.model
				val targetPackage = new RosettaJavaPackages(targetModel)
				
				if (ctx.cancelIndicator.canceled) {
					return
				}
				if (ref.isBuiltInType)
					fsa.generateFile('''«targetPackage.basicMetafields.directoryName»/BasicReferenceWithMeta«ref.name.toFirstUpper».java''', basicReferenceWithMeta(targetPackage, ref))
				else
					fsa.generateFile('''«targetPackage.model.metaField.directoryName»/ReferenceWithMeta«ref.name.toFirstUpper».java''', referenceWithMeta(targetPackage, ref))
			}
			//find all the metaed types
			val metas =  nsc.value.flatMap[expandedAttributes].filter[hasMetas && !metas.exists[name=="reference"]].map[type].toSet
			for (meta:metas) {
				if (ctx.cancelIndicator.canceled) {
					return
				}
				val targetModel = meta.model
				val targetPackage = new RosettaJavaPackages(targetModel)
				
				if(meta.isBuiltInType) {
					fsa.generateFile('''«targetPackage.basicMetafields.directoryName»/FieldWithMeta«meta.name.toFirstUpper».java''', fieldWithMeta(targetPackage, meta))
				} else {
					fsa.generateFile('''«targetPackage.model.metaField.directoryName»/FieldWithMeta«meta.name.toFirstUpper».java''', fieldWithMeta(targetPackage, meta))					
				}
			}
		}
	}

	def metaFields(RosettaJavaPackages packages, Collection<RosettaMetaType> utypes) {
		val stringType = RosettaFactoryImpl.eINSTANCE.createRosettaBasicType
		stringType.name="string"
		val globalKeyType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		globalKeyType.setName("globalKey")
		globalKeyType.type = stringType;
		val externalKeyType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		externalKeyType.setName("externalKey")
		externalKeyType.type = stringType;
		val filteredTypes = utypes.filter[t|t.name != "id" && t.name != "reference"].toSet;
		filteredTypes.add(globalKeyType)
		filteredTypes.add(externalKeyType)
		
	'''
		package «packages.basicMetafields.name»;
		
		import static java.util.Optional.ofNullable;
		
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.merge.BuilderMerger;
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
				public MetaFieldsBuilder merge(RosettaModelObjectBuilder b1, RosettaModelObjectBuilder b2, BuilderMerger merger) {
					MetaFieldsBuilder m1 = (MetaFieldsBuilder) b1;
					MetaFieldsBuilder m2 = (MetaFieldsBuilder) b2;
					«FOR type:filteredTypes»
						merger.mergeBasic(m1.get«type.name.toFirstUpper»(), m2.get«type.name.toFirstUpper»(), this::set«type.name.toFirstUpper»);
					«ENDFOR»
					return this;
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
	
	def fieldWithMeta(RosettaJavaPackages packages, ExpandedType type) '''		
		«IF type.isBuiltInType»
		package «packages.basicMetafields.name»;
		«ELSE»
		package «packages.model.metaField.name»;
		«ENDIF»
		
		import static java.util.Optional.ofNullable;
		
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.meta.FieldWithMeta;
		«IF !type.isBuiltInType»
		import «packages.model.name».*;
		«ENDIF»
		import «packages.defaultLib.name».RosettaModelObject;
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		import «packages.basicMetafields.name».MetaFields;
		import com.rosetta.model.lib.merge.BuilderMerger;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.records.Date;
		
		public class FieldWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements FieldWithMeta<«type.name.toJavaType»>{
			private final «type.name.toJavaType» value;
			private final MetaFields meta;
			private static BasicRosettaMetaData<FieldWithMeta«type.name.toFirstUpper»> metaData = new BasicRosettaMetaData<>();
						
			@Override
			public RosettaMetaData<? extends FieldWithMeta«type.name.toFirstUpper»> metaData() {
				return metaData;
			}
			
			private FieldWithMeta«type.name.toFirstUpper»(FieldWithMeta«type.name.toFirstUpper»Builder builder) {
				«IF type.isType»
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
				«IF type.isType»
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
				«IF type.isType»
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
				
				«IF type.isType»
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
				
				«IF type.isType»
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
					«IF type.isType»
						processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
					«ELSE»
						processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
					«ENDIF»
				}
				
				@Override
				public FieldWithMeta«type.name.toFirstUpper»Builder merge(RosettaModelObjectBuilder b1, RosettaModelObjectBuilder b2, BuilderMerger merger) {
					FieldWithMeta«type.name.toFirstUpper»Builder m1 = (FieldWithMeta«type.name.toFirstUpper»Builder) b1;
					FieldWithMeta«type.name.toFirstUpper»Builder m2 = (FieldWithMeta«type.name.toFirstUpper»Builder) b2;
					if (merger.mergeRosetta(m1.getMeta(), m2.getMeta(), this::setMetaBuilder)) {
						meta.merge(m1.getMeta(), m2.getMeta(), merger);
					}
					«IF type.isType»
						if (merger.mergeRosetta(m1.getValue(), m2.getValue(), this::setValueBuilder)) {
							value.merge(m1.getValue(), m2.getValue(), merger);
						}
					«ELSE»
						merger.mergeBasic(m1.getValue(), m2.getValue(), this::setValue);
					«ENDIF»
					return this;
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
	
	def boolean isClassOrData(RosettaType type) {
		return type instanceof Data || type instanceof RosettaClass
	}
	
	def referenceWithMeta(RosettaJavaPackages packages, ExpandedType type) '''
		package «packages.model.metaField.name»;
		
		import static java.util.Optional.ofNullable;
		
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.meta.ReferenceWithMeta;
		import com.rosetta.model.lib.meta.ReferenceWithMetaBuilder;
		import «packages.model.name».*;
		import «packages.defaultLib.name».RosettaModelObject;
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		import com.rosetta.model.lib.merge.BuilderMerger;
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
				
				public «type.name».«type.name»Builder getOrCreateValue() {
					if (value == null) {
						value = «type.name».builder();
					}
					return value;
				}
				
				public String getGlobalReference() {
					return globalReference;
				}
				
				public String getExternalReference() {
					return externalReference;
				}
				
				public Class<«type.name.toJavaType»> getValueType() {
					return «type.name.toJavaType».class;
				}
				
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
					if (value != null) value = value.prune();
					if (value != null && !value.hasData()) value = null;
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
				
				@Override
				public ReferenceWithMeta«type.name.toFirstUpper»Builder merge(RosettaModelObjectBuilder b1, RosettaModelObjectBuilder b2, BuilderMerger merger) {
					ReferenceWithMeta«type.name.toFirstUpper»Builder m1 = (ReferenceWithMeta«type.name.toFirstUpper»Builder) b1;
					ReferenceWithMeta«type.name.toFirstUpper»Builder m2 = (ReferenceWithMeta«type.name.toFirstUpper»Builder) b2;
					if (merger.mergeRosetta(m1.getValue(), m2.getValue(), this::setValueBuilder)) {
						value.merge(m1.getValue(), m2.getValue(), merger);
					}
					merger.mergeBasic(m1.getGlobalReference(), m2.getGlobalReference(), this::setGlobalReference);
					merger.mergeBasic(m1.getExternalReference(), m2.getExternalReference(), this::setExternalReference);
					return this;
				}
			}
		}
	'''
	
	def basicReferenceWithMeta(RosettaJavaPackages packages, ExpandedType type) '''
	package «packages.basicMetafields.name»;
		
	import static java.util.Optional.ofNullable;
		
	import com.rosetta.model.lib.process.*;
	import com.rosetta.model.lib.path.RosettaPath;
	import com.rosetta.model.lib.meta.ReferenceWithMeta;
	import com.rosetta.model.lib.meta.BasicReferenceWithMetaBuilder;
	import com.rosetta.model.lib.RosettaModelObject;
	import com.rosetta.model.lib.RosettaModelObjectBuilder;
	import com.rosetta.model.lib.merge.BuilderMerger;
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
		
		public static class BasicReferenceWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder implements BasicReferenceWithMetaBuilder<«type.name.toJavaType»>{
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
			
			public Class<«type.name.toJavaType»> getValueType() {
				return «type.name.toJavaType».class;
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
			
			@Override
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder merge(RosettaModelObjectBuilder b1, RosettaModelObjectBuilder b2, BuilderMerger merger) {
				BasicReferenceWithMeta«type.name.toFirstUpper»Builder m1 = (BasicReferenceWithMeta«type.name.toFirstUpper»Builder) b1;
				BasicReferenceWithMeta«type.name.toFirstUpper»Builder m2 = (BasicReferenceWithMeta«type.name.toFirstUpper»Builder) b2;
				merger.mergeBasic(m1.getValue(), m2.getValue(), this::setValue);
				merger.mergeBasic(m1.getGlobalReference(), m2.getGlobalReference(), this::setGlobalReference);
				merger.mergeBasic(m1.getExternalReference(), m2.getExternalReference(), this::setExternalReference);
				return this;
			}
		}
	}'''
	
	def namespace(RosettaRootElement rc) {
		return rc.model.name
	}
	
	/** generate once per resource marker */
	static class MarkerAdapterFactory extends AdapterFactoryImpl {

		final String namespace

		new(String namespace) {
			this.namespace = namespace
		}

		def getNamespace() {
			namespace
		}
	}
}
