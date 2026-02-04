package com.rosetta.model.metafields;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.BasicRosettaMetaData;
import com.rosetta.model.lib.meta.GlobalKeyFields;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.lib.meta.MetaDataFields;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.meta.TemplateFields;
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

import static com.rosetta.model.lib.SerializedNameConstants.*;
import static java.util.Optional.ofNullable;

/**
 * @version 1
 * 
 * Note this class was made static as of DSL version 9.24.0 prior to that it was generated
 */
@RosettaDataType(value="MetaAndTemplateFields", builder=MetaAndTemplateFields.MetaAndTemplateFieldsBuilderImpl.class, version="0.0.0")
@RuneDataType(value="MetaAndTemplateFields", builder=MetaAndTemplateFields.MetaAndTemplateFieldsBuilderImpl.class, version="0.0.0")
public interface MetaAndTemplateFields extends RosettaModelObject, GlobalKeyFields, TemplateFields, MetaDataFields {

	MetaAndTemplateFieldsMeta metaData = new MetaAndTemplateFieldsMeta();

	/*********************** Getter Methods  ***********************/
	String getScheme();
	String getTemplate();
	@Deprecated
	String getLocation();
	@Deprecated
	String getAddress();
	String getTemplateGlobalReference();
	String getGlobalKey();
	String getExternalKey();
	@Deprecated
	List<? extends Key> getKey();
	String getKeyScoped();

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
		processor.processBasic(path.newSubPath(ProcessorPathConstants.SCHEME), String.class, getScheme(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.TEMPLATE), String.class, getTemplate(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.LOCATION), String.class, getLocation(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.TEMPLATE_GLOBAL_REFERENCE), String.class, getTemplateGlobalReference(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.GLOBAL_KEY), String.class, getKeyScoped(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.EXTERNAL_KEY), String.class, getExternalKey(), this, AttributeMeta.META);
        processRosetta(path.newSubPath("key"), processor, Key.class, getKey());
	}
	

	/*********************** Builder Interface  ***********************/
	interface MetaAndTemplateFieldsBuilder extends MetaAndTemplateFields, RosettaModelObjectBuilder, GlobalKeyFields.GlobalKeyFieldsBuilder, TemplateFields.TemplateFieldsBuilder, MetaDataFields.MetaDataFieldsBuilder {
		Key.KeyBuilder getOrCreateKey(int _index);
		List<? extends Key.KeyBuilder> getKey();
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setScheme(String scheme);
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setTemplate(String template);
		@Deprecated
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setLocation(String location);
		@Deprecated
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setAddress(String address);
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setTemplateGlobalReference(String templateGlobalReference);
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setGlobalKey(String globalKey);
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setExternalKey(String externalKey);
		@Deprecated
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(Key key0);
		@Deprecated
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(Key key1, int _idx);
		@Deprecated
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder addKey(List<? extends Key> key2);
		@Deprecated
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setKey(List<? extends Key> key3);
		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setKeyScoped(String keyScoped);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath(ProcessorPathConstants.SCHEME), String.class, getScheme(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.TEMPLATE), String.class, getTemplate(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.LOCATION), String.class, getKeyScoped(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.TEMPLATE_GLOBAL_REFERENCE), String.class, getTemplateGlobalReference(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.GLOBAL_KEY), String.class, getGlobalKey(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.EXTERNAL_KEY), String.class, getExternalKey(), this, AttributeMeta.META);
            processRosetta(path.newSubPath("key"), processor, Key.KeyBuilder.class, getKey());
		}
		

		MetaAndTemplateFields.MetaAndTemplateFieldsBuilder prune();
	}

	/*********************** Immutable Implementation of MetaAndTemplateFields  ***********************/
	class MetaAndTemplateFieldsImpl implements MetaAndTemplateFields {
		private final String scheme;
		private final String template;
		private final String location;
		private final String address;
		private final String templateGlobalReference;
		private final String globalKey;
		private final String externalKey;
		private final List<? extends Key> key;
		
		protected MetaAndTemplateFieldsImpl(MetaAndTemplateFields.MetaAndTemplateFieldsBuilder builder) {
			this.scheme = builder.getScheme();
			this.template = builder.getTemplate();
			this.location = builder.getLocation();
			this.address = builder.getAddress();
			this.templateGlobalReference = builder.getTemplateGlobalReference();
			this.globalKey = builder.getGlobalKey();
			this.externalKey = builder.getExternalKey();
			this.key = ofNullable(builder.getKey()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
		}
		
		@Override
		@RosettaAttribute(value="scheme", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(SCHEME)
		public String getScheme() {
			return scheme;
		}
		
		@Override
		@RosettaAttribute(value="template", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		public String getTemplate() {
			return template;
		}
		
		@Override
		@RosettaAttribute(value="scopedLocation", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		public String getLocation() {
			return location;
		}
		
		@Override
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(SCOPED_REFERENCE)
		public String getAddress() {
			return address;
		}
		
		@Override
		@RosettaAttribute(value="templateGlobalReference", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		public String getTemplateGlobalReference() {
			return templateGlobalReference;
		}
		
		@Override
		@RosettaAttribute(value="globalKey", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(KEY)
		public String getGlobalKey() {
			return globalKey;
		}
		
		@Override
		@RosettaAttribute(value="externalKey", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(EXTERNAL_KEY)
		public String getExternalKey() {
			return externalKey;
		}
		
		@Override
		@RosettaAttribute(value="location", isRequired=false, isMulti=true, accessorType=AccessorType.GETTER)
		public List<? extends Key> getKey() {
			return key;
		}
		
        @Override
        @RuneAttribute(SCOPED_KEY)
        public String getKeyScoped() {
            if (key == null || key.isEmpty()) {
                return null;
            }
            return key.get(0).getKeyValue();
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
			ofNullable(getTemplate()).ifPresent(builder::setTemplate);
			ofNullable(getLocation()).ifPresent(builder::setLocation);
			ofNullable(getAddress()).ifPresent(builder::setAddress);
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
			if (!Objects.equals(template, _that.getTemplate())) return false;
			if (!Objects.equals(location, _that.getLocation())) return false;
			if (!Objects.equals(address, _that.getAddress())) return false;
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
			_result = 31 * _result + (template != null ? template.hashCode() : 0);
			_result = 31 * _result + (location != null ? location.hashCode() : 0);
			_result = 31 * _result + (address != null ? address.hashCode() : 0);
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
				"template=" + this.template + ", " +
				"location=" + this.location + ", " +
				"address=" + this.address + ", " +
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
		protected String template;
		protected String location;
		protected String address;
		protected String templateGlobalReference;
		protected String globalKey;
		protected String externalKey;
		protected List<Key.KeyBuilder> key = new ArrayList<>();
	
		public MetaAndTemplateFieldsBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute(value="scheme", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(SCHEME)
		public String getScheme() {
			return scheme;
		}
		
		@Override
		@RosettaAttribute(value="template", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		public String getTemplate() {
			return template;
		}
		
		@Override
		@RosettaAttribute(value="scopedLocation", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		public String getLocation() {
			return location;
		}
		
		@Override
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(SCOPED_REFERENCE)
		public String getAddress() {
			return address;
		}
		
		@Override
		@RosettaAttribute(value="templateGlobalReference", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		public String getTemplateGlobalReference() {
			return templateGlobalReference;
		}
		
		@Override
		@RosettaAttribute(value="globalKey", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(KEY)
		public String getGlobalKey() {
			return globalKey;
		}
		
		@Override
		@RosettaAttribute(value="externalKey", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(EXTERNAL_KEY)
		public String getExternalKey() {
			return externalKey;
		}
		
		@Override
		@RosettaAttribute(value="location", isRequired=false, isMulti=true, accessorType=AccessorType.GETTER)
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
        @RuneAttribute(SCOPED_KEY)
        public String getKeyScoped() {
            if (key == null || key.isEmpty()) {
                return null;
            }
            return key.get(0).getKeyValue();
        }
		
		@Override
		@RosettaAttribute(value="scheme", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(SCHEME)
		public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setScheme(String scheme) {
			this.scheme = scheme==null?null:scheme;
			return this;
		}
		@Override
		@RosettaAttribute(value="template", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setTemplate(String template) {
			this.template = template==null?null:template;
			return this;
		}
		@Override
		@RosettaAttribute(value="scopedLocation", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setLocation(String location) {
			this.location = location==null?null:location;
			return this;
		}
		@Override
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(SCOPED_REFERENCE)
		public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setAddress(String address) {
			this.address = address==null?null:address;
			return this;
		}
		@Override
		@RosettaAttribute(value="templateGlobalReference", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setTemplateGlobalReference(String templateGlobalReference) {
			this.templateGlobalReference = templateGlobalReference==null?null:templateGlobalReference;
			return this;
		}
		@Override
		@RosettaAttribute(value="globalKey", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(KEY)
		public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setGlobalKey(String globalKey) {
			this.globalKey = globalKey==null?null:globalKey;
			return this;
		}
		@Override
		@RosettaAttribute(value="externalKey", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(EXTERNAL_KEY)
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
		@RosettaAttribute(value="location", isRequired=false, isMulti=true, accessorType=AccessorType.SETTER)
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
        @RuneAttribute(SCOPED_KEY)
        public MetaAndTemplateFields.MetaAndTemplateFieldsBuilder setKeyScoped(String keyScoped) {
            this.key = new ArrayList<>();
            if (keyScoped!=null)  {
                this.key = Lists.newArrayList(Key.builder().setKeyValue(keyScoped));
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
			if (getTemplate()!=null) return true;
			if (getLocation()!=null) return true;
			if (getAddress()!=null) return true;
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
			merger.mergeBasic(getTemplate(), o.getTemplate(), this::setTemplate);
			merger.mergeBasic(getLocation(), o.getLocation(), this::setLocation);
			merger.mergeBasic(getAddress(), o.getAddress(), this::setAddress);
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
			if (!Objects.equals(template, _that.getTemplate())) return false;
			if (!Objects.equals(location, _that.getLocation())) return false;
			if (!Objects.equals(address, _that.getAddress())) return false;
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
			_result = 31 * _result + (template != null ? template.hashCode() : 0);
			_result = 31 * _result + (location != null ? location.hashCode() : 0);
			_result = 31 * _result + (address != null ? address.hashCode() : 0);
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
				"template=" + this.template + ", " +
				"location=" + this.location + ", " +
				"address=" + this.address + ", " +
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
