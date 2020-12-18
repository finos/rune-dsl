package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Multimaps
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.object.MetaFieldGenerator.MetaFieldType
import com.regnosys.rosetta.generator.object.ExpandedType
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.process.AttributeMeta
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
				metaFields(packages, "MetaFields", newArrayList("GlobalKeyFields"), allMetaTypes.metaFieldTypes))
				
				fsa.generateFile('''«packages.basicMetafields.directoryName»/MetaAndTemplateFields.java''',
				metaFields(packages, "MetaAndTemplateFields", newArrayList("GlobalKeyFields", "TemplateFields"), allMetaTypes.metaAndTemplateFieldTypes))
//			} finally {
//				resource.resourceSet.adapterFactories.add(new MarkerAdapterFactory(model.name))
//			}
//		}
		
		val modelClasses = model.elements.filter [
			it instanceof Data
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
			val refs = nsc.value.flatMap[expandedAttributes].filter[hasMetas && metas.exists[name=="reference" || name=="address"]].map[type].toSet
			
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
			val metas =  nsc.value.flatMap[expandedAttributes].filter[hasMetas && !metas.exists[name=="reference" || name=="address"]].map[type].toSet
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

	def getStringType() {
		val stringType = RosettaFactoryImpl.eINSTANCE.createRosettaBasicType
		stringType.name="string"
		return stringType
	}

	def getMetaFieldTypes(Collection<RosettaMetaType> utypes) {
		val globalKeyType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		globalKeyType.setName("globalKey")
		globalKeyType.type = stringType;

		val externalKeyType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		externalKeyType.setName("externalKey")
		externalKeyType.type = stringType;
		
		val keysType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		keysType.setName("Keys")
		val keyType = RosettaFactoryImpl.eINSTANCE.createRosettaBasicType
		keyType.name="Keys"
		keysType.type = keyType;

		val filteredTypes = utypes.filter[t|t.name != "key" && t.name != "id" && t.name != "reference"].map[new MetaFieldType(it, null)].toSet;
		filteredTypes.add(new MetaFieldType(globalKeyType, AttributeMeta.GLOBAL_KEY))
		filteredTypes.add(new MetaFieldType(externalKeyType, AttributeMeta.EXTERNAL_KEY))
		filteredTypes.add(new MetaFieldType(keysType, AttributeMeta.EXTERNAL_KEY))
		return filteredTypes
	}

	def getMetaAndTemplateFieldTypes(Collection<RosettaMetaType> utypes) {
		val templateGlobalReferenceType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		templateGlobalReferenceType.setName("templateGlobalReference")
		templateGlobalReferenceType.type = stringType;

		val metaFieldTypes = utypes.metaFieldTypes
		metaFieldTypes.add(new MetaFieldType(templateGlobalReferenceType, null))
		return metaFieldTypes
	}

	def metaFields(RosettaJavaPackages packages, String name, Collection<String> interfaces, Collection<MetaFieldGenerator.MetaFieldType> metaFieldTypes) {
		if (metaFieldTypes.map[metaType].exists[t|t.name == "scheme"]) {
			interfaces.add("MetaDataFields")
		}

	'''
		package «packages.basicMetafields.name»;

		import com.rosetta.model.lib.RosettaModelObject;
		import com.rosetta.model.lib.RosettaModelObjectBuilder;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.*;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.AttributeMeta;
		import com.rosetta.model.lib.process.BuilderMerger;
		import com.rosetta.model.lib.process.BuilderProcessor;
		import com.rosetta.model.lib.process.Processor;
		import com.rosetta.model.lib.meta.Keys;

		import java.util.Objects;

		import static java.util.Optional.ofNullable;

		public class «name» extends RosettaModelObject implements «FOR i : interfaces SEPARATOR ', '»«i»«ENDFOR» {
			«FOR type : metaFieldTypes»
				private final «type.metaType.type.name.toJavaFullType» «type.metaType.name.toFirstLower»;
			«ENDFOR»
			private static BasicRosettaMetaData<«name»> metaData = new BasicRosettaMetaData<>();
			
			@Override
			public RosettaMetaData<? extends «name»> metaData() {
				return metaData;
			}
			
			private «name»(«name»Builder builder) {
				«FOR type : metaFieldTypes»
					«IF type.metaType.type.name=="Keys"»
						«type.metaType.name.toFirstLower»  =builder.keys == null?null:builder.keys.build();
					«ELSE»
						«type.metaType.name.toFirstLower» = builder.«type.metaType.getter»;
					«ENDIF»
				«ENDFOR»
			}
			
			«FOR type : metaFieldTypes»
				@Override
				public «type.metaType.type.name.toJavaFullType» «type.metaType.getter» {
					return «type.metaType.name.toFirstLower»;
				}
			«ENDFOR»
			
			@Override
			public «name»Builder toBuilder() {
				«name»Builder builder = new «name»Builder();
				«FOR type : metaFieldTypes»
					ofNullable(«type.metaType.getter»).ifPresent(builder::set«type.metaType.name.toFirstUpper»);
				«ENDFOR»
				return builder;
			}
			
			public static «name»Builder builder() {
				return new «name»Builder();
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int _result = 1;
				«FOR type : metaFieldTypes»
					_result = prime * _result + ((«type.metaType.name.toFirstLower» == null) ? 0 : «type.metaType.name.toFirstLower».hashCode());
				«ENDFOR»
				return _result;
			}
		
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null || getClass() != obj.getClass())
					return false;

				«name» other = («name») obj;

				«FOR type : metaFieldTypes»
					if (!Objects.equals(«type.metaType.name.toFirstLower», other.«type.metaType.name.toFirstLower»)) return false;
				«ENDFOR»
				return true;
			}
		
			@Override
			public String toString() {
				return "«name» {" +
					«FOR type : metaFieldTypes SEPARATOR ' + ", " +'»
						"«type.metaType.name.toFirstLower»=" + this.«type.metaType.name.toFirstLower»
					«ENDFOR»
					+ "}";
			}
			
			public static class «name»Builder extends RosettaModelObjectBuilder implements «FOR i : interfaces SEPARATOR ', '»«i»Builder«ENDFOR» {
				«FOR type : metaFieldTypes»
					«IF type.metaType.type.name=="Keys"»
						private Keys.KeysBuilder keys;
					«ELSE»
						private «type.metaType.type.name.toJavaType» «type.metaType.name.toFirstLower»;
					«ENDIF»
				«ENDFOR»			
				
				@Override
				public RosettaMetaData<? extends «name»> metaData() {
					return metaData;
				}
				
				«FOR type : metaFieldTypes»
					«IF type.metaType.type.name=="Keys"»
						@Override
						public Keys.KeysBuilder «type.metaType.getter» {
							return keys;
						}
						public Keys.KeysBuilder getOrCreateKeys() {
							if (keys == null) {
								keys = new Keys.KeysBuilder();
							}
							return keys;
						}
					«ELSE»
						@Override
						public «type.metaType.type.name.toJavaFullType» «type.metaType.getter» {
							return «type.metaType.name.toFirstLower»;
						}
					«ENDIF»
				«ENDFOR»
				
				«FOR type : metaFieldTypes»
					@Override
					«IF type.metaType.type.name=="Keys"»
						public «name»Builder set«type.metaType.name.toFirstUpper»(Keys.KeysBuilder keys) {
							this.«type.metaType.name.toFirstLower» = «type.metaType.name.toFirstLower»;
							return this;
						}
						public «name»Builder set«type.metaType.name.toFirstUpper»(Keys keys) {
							this.keys = keys.toBuilder();
							return this;
						}
					«ELSE»
						public «name»Builder set«type.metaType.name.toFirstUpper»(«type.metaType.type.name.toJavaFullType» «type.metaType.name.toFirstLower») {
							this.«type.metaType.name.toFirstLower» = «type.metaType.name.toFirstLower»;
							return this;
						}
					«ENDIF»
				«ENDFOR»

				@Override
				public «name» build() {
					return new «name»(this);
				}

				@Override
				public «name»Builder prune() {
					return this;
				}
				
				@Override
				public boolean hasData() {
					«IF metaFieldTypes.empty»
					return false;
					«ELSE»
					return «FOR type : metaFieldTypes SEPARATOR " || \n"»«type.metaType.name.toFirstLower»!=null«ENDFOR»;
					«ENDIF»
				}
				
				@Override
				public void process(RosettaPath path, BuilderProcessor processor) {
					«FOR type : metaFieldTypes.filter[m|m.metaType.type.name!="Keys"]»
						processor.processBasic(path.newSubPath("«type.metaType.name.toFirstLower»"), «type.metaType.type.name.toJavaType».class, «type.metaType.name.toFirstLower», this, «addAttributeMeta(type)»);
					«ENDFOR»
				}

				@Override
				public «name»Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					«name»Builder o = («name»Builder) other;
					
					«FOR type : metaFieldTypes»
						merger.mergeBasic(get«type.metaType.name.toFirstUpper»(), o.get«type.metaType.name.toFirstUpper»(), this::set«type.metaType.name.toFirstUpper», «addAttributeMeta(type)»);
					«ENDFOR»
					return this;
				}

				@Override
				public int hashCode() {
					final int prime = 31;
					int _result = 1;
					«FOR type : metaFieldTypes»
						_result = prime * _result + ((«type.metaType.name.toFirstLower» == null) ? 0 : «type.metaType.name.toFirstLower».hashCode());
					«ENDFOR»
					return _result;
				}
			
				@Override
				public boolean equals(Object obj) {
					if (this == obj)
						return true;
					if (obj == null || getClass() != obj.getClass())
						return false;

					«name»Builder other = («name»Builder) obj;

					«FOR type : metaFieldTypes»
						if (!Objects.equals(«type.metaType.name.toFirstLower», other.«type.metaType.name.toFirstLower»)) return false;
					«ENDFOR»
					return true;
				}
			
				@Override
				public String toString() {
					return "«name»Builder {" +
						«FOR type : metaFieldTypes SEPARATOR ' + ", " +'»
							"«type.metaType.name.toFirstLower»=" + this.«type.metaType.name.toFirstLower»
						«ENDFOR»
						+ "}";
				}
			}
			
			@Override
			protected void process(RosettaPath path, Processor processor) {
				«FOR type : metaFieldTypes»
					processor.processBasic(path.newSubPath("«type.metaType.name.toFirstLower»"), «type.metaType.type.name.toJavaType».class, «type.metaType.name.toFirstLower», this, AttributeMeta.META);
				«ENDFOR»
			}
		}
	'''
	}
	
	def CharSequence addAttributeMeta(MetaFieldType type)
		'''AttributeMeta.META«IF type.attributeMeta !== null», AttributeMeta.«type.attributeMeta»«ENDIF»'''
	
	
	@org.eclipse.xtend.lib.annotations.Data 
	static class MetaFieldType {
		RosettaMetaType metaType
		AttributeMeta attributeMeta
	}

	def fieldWithMeta(RosettaJavaPackages packages, ExpandedType type) '''
		«IF type.isBuiltInType»
		package «packages.basicMetafields.name»;
		«ELSE»
		package «packages.model.metaField.name»;
		«ENDIF»
		
		«IF !type.isBuiltInType»
		import «packages.model.name».*;
		«ENDIF»
		import «packages.defaultLib.name».GlobalKey;
		import «packages.defaultLib.name».GlobalKeyBuilder;
		import «packages.defaultLib.name».RosettaModelObject;
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.FieldWithMeta;
		import com.rosetta.model.lib.meta.FieldWithMetaBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.*;
		import com.rosetta.model.lib.records.Date;
		import «packages.basicMetafields.name».MetaFields;
		
		import java.util.Objects;

		import static java.util.Optional.ofNullable;
		
		public class FieldWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements FieldWithMeta<«type.name.toJavaType»>, GlobalKey {
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
				processRosetta(path.newSubPath("meta"), processor, MetaFields.class, meta, AttributeMeta.META);
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
				
				if (!Objects.equals(meta, other.meta)) return false;
				if (!Objects.equals(value, other.value)) return false;
				return true;
			}
		
			@Override
			public String toString() {
				return "FieldWithMeta«type.name.toFirstUpper» {" +
					"value=" + this.value + ", " +
					"meta=" + this.meta +
				"}";
			}
			
			public static class FieldWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder implements FieldWithMetaBuilder<«type.name.toJavaType»>, GlobalKeyBuilder {
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
					public «type.name».«type.name»Builder getValue() {
						return value;
					}
					
					public «type.name».«type.name»Builder getOrCreateValue() {
						return value = ofNullable(value).orElseGet(«type.name»::builder);
					}
				«ELSE»
					public «type.name.toJavaType» getValue() {
						return value;
					}
				«ENDIF»
				
				public Class<«type.name.toJavaType»> getValueType() {
					return «type.name.toJavaType».class;
				}
				
				public MetaFields.MetaFieldsBuilder getMeta() {
					return meta;
				}
				
				public MetaFields.MetaFieldsBuilder getOrCreateMeta() {
					return meta = ofNullable(meta).orElseGet(MetaFields::builder);
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
					if (getMeta()!=null && getMeta().hasData()) return true;
					«IF type.isType»
						if (getValue()!=null && getValue().hasData()) return true;
					«ELSE»
						if (getValue()!=null) return true;
					«ENDIF»
					return false;
				}
				
				@Override
				public void process(RosettaPath path, BuilderProcessor processor) {
					processRosetta(path.newSubPath("meta"), processor, MetaFields.class, meta, AttributeMeta.META);
					«IF type.isType»
						processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
					«ELSE»
						processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
					«ENDIF»
				}
				
				@Override
				public FieldWithMeta«type.name.toFirstUpper»Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					FieldWithMeta«type.name.toFirstUpper»Builder o = (FieldWithMeta«type.name.toFirstUpper»Builder) other;
					
					merger.mergeRosetta(getMeta(), o.getMeta(), this::setMetaBuilder);
					«IF type.isType»
						merger.mergeRosetta(getValue(), o.getValue(), this::setValueBuilder);
					«ELSE»
						merger.mergeBasic(getValue(), o.getValue(), this::setValue);
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
					
					if (!Objects.equals(meta, other.meta)) return false;
					if (!Objects.equals(value, other.value)) return false;
					return true;
				}
			
				@Override
				public String toString() {
					return "FieldWithMeta«type.name.toFirstUpper»Builder {" +
						"value=" + this.value + ", " +
						"meta=" + this.meta +
					"}";
				}
			}
		}
	'''
	
	def boolean isClassOrData(RosettaType type) {
		return type instanceof Data
	}
	
	def referenceWithMeta(RosettaJavaPackages packages, ExpandedType type) '''
		package «packages.model.metaField.name»;
		
		import static java.util.Optional.ofNullable;
		
		import «packages.model.name».*;
		import «packages.defaultLib.name».RosettaModelObject;
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		import com.rosetta.model.lib.meta.BasicRosettaMetaData;
		import com.rosetta.model.lib.meta.ReferenceWithMeta;
		import com.rosetta.model.lib.meta.Reference;
		import com.rosetta.model.lib.meta.Reference.ReferenceBuilder;
		import com.rosetta.model.lib.meta.ReferenceWithMetaBuilder;
		import com.rosetta.model.lib.meta.RosettaMetaData;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.*;
		
		import java.util.Objects;
		
		import static java.util.Optional.ofNullable;
		
		public class ReferenceWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements ReferenceWithMeta<«type.name»>{
			private final String globalReference;
			private final String externalReference;
			private final «type.name» value;
			private final Reference reference;
			
			private ReferenceWithMeta«type.name.toFirstUpper»(ReferenceWithMeta«type.name.toFirstUpper»Builder builder) {
				value = ofNullable(builder.getValue()).map(v->v.build()).orElse(null);
				globalReference = builder.globalReference;
				externalReference = builder.externalReference;
				this.reference = builder.reference==null?null:builder.reference.build();
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
			
			public Reference getReference() {
				return reference;
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
				if (reference!=null) builder.setReference(reference.toBuilder());
				return builder;
			}
			
			public static ReferenceWithMeta«type.name.toFirstUpper»Builder builder() {
				return new ReferenceWithMeta«type.name.toFirstUpper»Builder();
			}
			
			@Override
			protected void process(RosettaPath path, Processor processor) {
				processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
				processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.META);
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
				
				if (!Objects.equals(globalReference, other.globalReference)) return false;
				if (!Objects.equals(externalReference, other.externalReference)) return false;
				if (!Objects.equals(value, other.value)) return false;
				return true;
			}
		
			@Override
			public String toString() {
				return "ReferenceWitMeta«type.name.toFirstUpper» {" +
					"globalReference=" + this.globalReference + ", " +
					"externalReference=" + this.externalReference + ", " +
					"value=" + this.value +
				"}";
			}
			
			public static class ReferenceWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder implements ReferenceWithMetaBuilder<«type.name.toJavaType»>{
				private «type.name».«type.name»Builder value;
				private String globalReference;
				private String externalReference;
				private ReferenceBuilder reference;
				
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
				
				public ReferenceBuilder getReference() {
					return reference;
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
				
				public ReferenceWithMeta«type.name.toFirstUpper»Builder setReference(ReferenceBuilder reference) {
					this.reference = reference;
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
					if (getValue()!=null && getValue().hasData()) return true;
					return !(globalReference==null && externalReference==null && (reference==null || reference.getReference()==null));
				}
				
				@Override
				public void process(RosettaPath path, BuilderProcessor processor) {
					processRosetta(path.newSubPath("value"), processor, «type.name.toJavaType».class, value);
					processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.META);
					processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.META);
				}

				@Override
				public ReferenceWithMeta«type.name.toFirstUpper»Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
					ReferenceWithMeta«type.name.toFirstUpper»Builder o = (ReferenceWithMeta«type.name.toFirstUpper»Builder) other;
					
					merger.mergeRosetta(getValue(), o.getValue(), this::setValueBuilder);
					merger.mergeBasic(getGlobalReference(), o.getGlobalReference(), this::setGlobalReference);
					merger.mergeBasic(getExternalReference(), o.getExternalReference(), this::setExternalReference);
					return this;
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
					
					ReferenceWithMeta«type.name.toFirstUpper»Builder other = (ReferenceWithMeta«type.name.toFirstUpper»Builder) obj;
					
					if (!Objects.equals(globalReference, other.globalReference)) return false;
					if (!Objects.equals(externalReference, other.externalReference)) return false;
					if (!Objects.equals(value, other.value)) return false;
					return true;
				}
			
				@Override
				public String toString() {
					return "ReferenceWitMeta«type.name.toFirstUpper»Builder {" +
						"globalReference=" + this.globalReference + ", " +
						"externalReference=" + this.externalReference + ", " +
						"value=" + this.value +
					"}";
				}
			}
		}
	'''
	
	def basicReferenceWithMeta(RosettaJavaPackages packages, ExpandedType type) '''
	package «packages.basicMetafields.name»;
	
	import com.rosetta.model.lib.RosettaModelObject;
	import com.rosetta.model.lib.RosettaModelObjectBuilder;
	import com.rosetta.model.lib.process.*;
	import com.rosetta.model.lib.meta.BasicReferenceWithMetaBuilder;
	import com.rosetta.model.lib.meta.BasicRosettaMetaData;
	import com.rosetta.model.lib.meta.ReferenceWithMeta;
	import com.rosetta.model.lib.meta.Reference;
	import com.rosetta.model.lib.meta.Reference.ReferenceBuilder;
	import com.rosetta.model.lib.meta.RosettaMetaData;
	import com.rosetta.model.lib.path.RosettaPath;
	import com.rosetta.model.lib.records.Date;
	
	import java.math.BigDecimal;
	import java.util.Objects;
	
	public class BasicReferenceWithMeta«type.name.toFirstUpper» extends RosettaModelObject implements ReferenceWithMeta<«type.name.toJavaType»>{
		private final String globalReference;
		private final String externalReference;
		private final «type.name.toJavaType» value;
		private final Reference reference;
		
		private BasicReferenceWithMeta«type.name.toFirstUpper»(BasicReferenceWithMeta«type.name.toFirstUpper»Builder builder){
			value = builder.getValue();
			globalReference = builder.globalReference;
			externalReference = builder.externalReference;
			this.reference = builder.reference==null?null:builder.reference.build();
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
		
		public Reference getReference() {
			return reference;
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
			if (reference!=null) builder.setReference(reference.toBuilder());
			return builder;
		}
		
		public static BasicReferenceWithMeta«type.name.toFirstUpper»Builder builder() {
			return new BasicReferenceWithMeta«type.name.toFirstUpper»Builder();
		}
		
		@Override
		protected void process(RosettaPath path, Processor processor) {
			processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
			processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.META);
			processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.META);
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
			
			if (!Objects.equals(globalReference, other.globalReference)) return false;
			if (!Objects.equals(externalReference, other.externalReference)) return false;
			if (!Objects.equals(value, other.value)) return false;
			return true;
		}
	
		@Override
		public String toString() {
			return "BasicReferenceWitMeta«type.name.toFirstUpper» {" +
				"globalReference=" + this.globalReference + ", " +
				"externalReference=" + this.externalReference + ", " +
				"value=" + this.value +
			"}";
		}
		
		public static class BasicReferenceWithMeta«type.name.toFirstUpper»Builder extends RosettaModelObjectBuilder implements BasicReferenceWithMetaBuilder<«type.name.toJavaType»>{
			private «type.name.toJavaType» value;
			private String globalReference;
			private String externalReference;
			private ReferenceBuilder reference;
			
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
			
			public ReferenceBuilder getReference() {
				return reference;
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
			
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder setReference(ReferenceBuilder reference) {
				this.reference = reference;
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
				return !(value==null && globalReference==null && externalReference==null && (reference==null || reference.getReference()==null));
			}
			
			@Override
			public void process(RosettaPath path, BuilderProcessor processor) {
				processor.processBasic(path.newSubPath("value"), «type.name.toJavaType».class, value, this);
				processor.processBasic(path.newSubPath("globalReference"), String.class, globalReference, this, AttributeMeta.META);
				processor.processBasic(path.newSubPath("externalReference"), String.class, externalReference, this, AttributeMeta.META);
			}

			@Override
			public BasicReferenceWithMeta«type.name.toFirstUpper»Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
				BasicReferenceWithMeta«type.name.toFirstUpper»Builder o = (BasicReferenceWithMeta«type.name.toFirstUpper»Builder) other;
				
				merger.mergeBasic(getValue(), o.getValue(), this::setValue);
				merger.mergeBasic(getGlobalReference(), o.getGlobalReference(), this::setGlobalReference);
				merger.mergeBasic(getExternalReference(), o.getExternalReference(), this::setExternalReference);
				return this;
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
				
				BasicReferenceWithMeta«type.name.toFirstUpper»Builder other = (BasicReferenceWithMeta«type.name.toFirstUpper»Builder) obj;
				
				if (!Objects.equals(globalReference, other.globalReference)) return false;
				if (!Objects.equals(externalReference, other.externalReference)) return false;
				if (!Objects.equals(value, other.value)) return false;
				return true;
			}
		
			@Override
			public String toString() {
				return "BasicReferenceWitMeta«type.name.toFirstUpper»Builder {" +
					"globalReference=" + this.globalReference + ", " +
					"externalReference=" + this.externalReference + ", " +
					"value=" + this.value +
				"}";
			}
		}
	}'''
	
	def namespace(RosettaRootElement rc) {
		return rc.model.name
	}
	
	def getter(RosettaNamed type) {
		'''get«type.name.toFirstUpper»()'''
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
