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
import com.rosetta.model.lib.meta.*;
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
@RosettaDataType(value="MetaFields", builder=MetaFields.MetaFieldsBuilderImpl.class, version="0.0.0")
@RuneDataType(value="MetaFields", builder=MetaFields.MetaFieldsBuilderImpl.class, version="0.0.0")
public interface MetaFields extends RosettaModelObject, GlobalKeyFields, MetaDataFields {

	MetaFieldsMeta metaData = new MetaFieldsMeta();

	/*********************** Getter Methods  ***********************/
	String getScheme();
	String getTemplate();
	@Deprecated
	String getLocation();
	@Deprecated
	String getAddress();
	String getGlobalKey();
	String getExternalKey();
	@Deprecated
	List<? extends Key> getKey();
	String getScopedKey();
	
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
		processor.processBasic(path.newSubPath(ProcessorPathConstants.SCHEME), String.class, getScheme(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.TEMPLATE), String.class, getTemplate(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.LOCATION), String.class, getScopedKey(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.GLOBAL_KEY), String.class, getGlobalKey(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath(ProcessorPathConstants.EXTERNAL_KEY), String.class, getExternalKey(), this, AttributeMeta.META);
        processRosetta(path.newSubPath("key"), processor, Key.class, getKey());
	}
	

	/*********************** Builder Interface  ***********************/
	interface MetaFieldsBuilder extends MetaFields, RosettaModelObjectBuilder, GlobalKeyFields.GlobalKeyFieldsBuilder, MetaDataFields.MetaDataFieldsBuilder {
		Key.KeyBuilder getOrCreateKey(int _index);
		List<? extends Key.KeyBuilder> getKey();
		MetaFields.MetaFieldsBuilder setScheme(String scheme);
		MetaFields.MetaFieldsBuilder setTemplate(String template);
		@Deprecated
		MetaFields.MetaFieldsBuilder setLocation(String location);
		@Deprecated
		MetaFields.MetaFieldsBuilder setAddress(String address);
		MetaFields.MetaFieldsBuilder setGlobalKey(String globalKey);
		MetaFields.MetaFieldsBuilder setExternalKey(String externalKey);
		@Deprecated
		MetaFields.MetaFieldsBuilder addKey(Key key0);
		@Deprecated
		MetaFields.MetaFieldsBuilder addKey(Key key1, int _idx);
		@Deprecated
		MetaFields.MetaFieldsBuilder addKey(List<? extends Key> key2);
		@Deprecated
		MetaFields.MetaFieldsBuilder setKey(List<? extends Key> key3);
		MetaFields.MetaFieldsBuilder setScopedKey(String scopedKey);
		String getScopedKey();

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath(ProcessorPathConstants.SCHEME), String.class, getScheme(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.TEMPLATE), String.class, getTemplate(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.LOCATION), String.class, getScopedKey(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.GLOBAL_KEY), String.class, getGlobalKey(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath(ProcessorPathConstants.EXTERNAL_KEY), String.class, getExternalKey(), this, AttributeMeta.META);
	        processRosetta(path.newSubPath("key"), processor, Key.KeyBuilder.class, getKey());
		}
		

		MetaFields.MetaFieldsBuilder prune();
	}

	/*********************** Immutable Implementation of MetaFields  ***********************/
	class MetaFieldsImpl implements MetaFields {
		private final String scheme;
		private final String template;
		private final String location;
		private final String address;
		private final String globalKey;
		private final String externalKey;
		private final List<? extends Key> key;
		
		protected MetaFieldsImpl(MetaFields.MetaFieldsBuilder builder) {
			this.scheme = builder.getScheme();
			this.template = builder.getTemplate();
			this.location = builder.getLocation();
			this.address = builder.getAddress();
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
        @Deprecated
		public List<? extends Key> getKey() {
			return key;
		}
		
        @Override
        @RuneAttribute(SCOPED_KEY)
        public String getScopedKey() {
            if (key == null || key.isEmpty()) {
                return null;
            }
            return key.get(0).getKeyValue();
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
			ofNullable(getTemplate()).ifPresent(builder::setTemplate);
			ofNullable(getLocation()).ifPresent(builder::setLocation);
			ofNullable(getAddress()).ifPresent(builder::setAddress);
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
			if (!Objects.equals(template, _that.getTemplate())) return false;
			if (!Objects.equals(location, _that.getLocation())) return false;
			if (!Objects.equals(address, _that.getAddress())) return false;
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
			_result = 31 * _result + (globalKey != null ? globalKey.hashCode() : 0);
			_result = 31 * _result + (externalKey != null ? externalKey.hashCode() : 0);
			_result = 31 * _result + (key != null ? key.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "MetaFields {" +
				"scheme=" + this.scheme + ", " +
				"template=" + this.template + ", " +
				"location=" + this.location + ", " +
				"address=" + this.address + ", " +
				"globalKey=" + this.globalKey + ", " +
				"externalKey=" + this.externalKey + ", " +
				"key=" + this.key +
			'}';
		}
	}

	/*********************** Builder Implementation of MetaFields  ***********************/
	class MetaFieldsBuilderImpl implements MetaFields.MetaFieldsBuilder {
	
		protected String scheme;
		protected String template;
		protected String location;
		protected String address;
		protected String globalKey;
		protected String externalKey;
		protected List<Key.KeyBuilder> key = new ArrayList<>();
	
		public MetaFieldsBuilderImpl() {
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
        @Deprecated
		public List<? extends Key.KeyBuilder> getKey() {
			return key;
		}
		
        @Override
        @RuneAttribute(SCOPED_KEY)
        public String getScopedKey() {
            if (key == null || key.isEmpty()) {
                return null;
            }
            return key.get(0).getKeyValue();
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
		@RosettaAttribute(value="scheme", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(SCHEME)
		public MetaFields.MetaFieldsBuilder setScheme(String scheme) {
			this.scheme = scheme==null?null:scheme;
			return this;
		}
		@Override
		@RosettaAttribute(value="template", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		public MetaFields.MetaFieldsBuilder setTemplate(String template) {
			this.template = template==null?null:template;
			return this;
		}
		@Override
		@RosettaAttribute(value="scopedLocation", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		public MetaFields.MetaFieldsBuilder setLocation(String location) {
			this.location = location==null?null:location;
			return this;
		}
		@Override
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(SCOPED_REFERENCE)
		public MetaFields.MetaFieldsBuilder setAddress(String address) {
			this.address = address==null?null:address;
			return this;
		}
		@Override
		@RosettaAttribute(value="globalKey", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(KEY)
		public MetaFields.MetaFieldsBuilder setGlobalKey(String globalKey) {
			this.globalKey = globalKey==null?null:globalKey;
			return this;
		}
		@Override
		@RosettaAttribute(value="externalKey", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(EXTERNAL_KEY)
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
		@RosettaAttribute(value="location", isRequired=false, isMulti=true, accessorType=AccessorType.SETTER)
        @Deprecated
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
        @RuneAttribute(SCOPED_KEY)
        public MetaFields.MetaFieldsBuilder setScopedKey(String scopedKey) {
            this.key = new ArrayList<>();
            if (scopedKey!=null)  {
                this.key = Lists.newArrayList(Key.builder().setKeyValue(scopedKey).setScope("DOCUMENT"));
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
			if (getTemplate()!=null) return true;
			if (getLocation()!=null) return true;
			if (getAddress()!=null) return true;
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
			merger.mergeBasic(getTemplate(), o.getTemplate(), this::setTemplate);
			merger.mergeBasic(getLocation(), o.getLocation(), this::setLocation);
			merger.mergeBasic(getAddress(), o.getAddress(), this::setAddress);
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
			if (!Objects.equals(template, _that.getTemplate())) return false;
			if (!Objects.equals(location, _that.getLocation())) return false;
			if (!Objects.equals(address, _that.getAddress())) return false;
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
			_result = 31 * _result + (globalKey != null ? globalKey.hashCode() : 0);
			_result = 31 * _result + (externalKey != null ? externalKey.hashCode() : 0);
			_result = 31 * _result + (key != null ? key.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "MetaFieldsBuilder {" +
				"scheme=" + this.scheme + ", " +
				"template=" + this.template + ", " +
				"location=" + this.location + ", " +
				"address=" + this.address + ", " +
				"globalKey=" + this.globalKey + ", " +
				"externalKey=" + this.externalKey + ", " +
				"key=" + this.key +
			'}';
		}
	}
}

class MetaFieldsMeta extends BasicRosettaMetaData<MetaFields>{

}
