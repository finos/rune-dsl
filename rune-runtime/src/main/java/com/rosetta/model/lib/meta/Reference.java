/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.ProcessorPathConstants;

/**
 * @author TomForwood This class represents a reference to a rosetta object
 *         defined elsewhere.
 *         <p>
 *         The scope defines where the resolver should look to find the object
 *         linked to.
 *         <p>
 *         Scope can be - global - the key is universally unique and can be
 *         looked up anywhere - e.g. external database - document - the key must
 *         be unique in this document and can be found anywhere in the document
 *         - the name of the rosetta class e.g. TradeableProduct- the key is
 *         only unique inside that TradeableProduct and should only be looked
 *         for inside that TradeableProduct
 */
@RosettaDataType(value = "Reference", builder = Reference.ReferenceBuilderImpl.class)
@RuneDataType(value = "Reference", builder = Reference.ReferenceBuilderImpl.class, version="0.0.0")
public interface Reference extends RosettaModelObject {

	Reference build();

	ReferenceBuilder toBuilder();

	String getScope();

	String getPointsTo();

	String getReference();

	static RosettaMetaData<Reference> meta = new BasicRosettaMetaData<>();

	@Override
	default RosettaMetaData<? extends RosettaModelObject> metaData() {
		return meta;
	}

	static ReferenceBuilderImpl builder() {
		return new ReferenceBuilderImpl();
	}

	default Class<Reference> getType() {
		return Reference.class;
	}

	@Override
	default void process(RosettaPath path, Processor processor) {
	    processor.processBasic(path.newSubPath(ProcessorPathConstants.ADDRESS), String.class, getReference(), this, AttributeMeta.META);
	}

	static interface ReferenceBuilder extends Reference, RosettaModelObjectBuilder {
		ReferenceBuilder setScope(String scope);

		ReferenceBuilder setPointsTo(String pointsTo);

		ReferenceBuilder setReference(String reference);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
		    processor.processBasic(path.newSubPath(ProcessorPathConstants.ADDRESS), String.class, getReference(), this, AttributeMeta.META);
		}
	}

	static class ReferenceImpl implements Reference {
		private final String scope;
		private final String pointsTo;
		private final String reference;

		public ReferenceImpl(String scope, String pointsTo, String reference) {
			super();
			this.scope = scope;
			this.pointsTo = pointsTo;
			this.reference = reference;
		}

		@RosettaAttribute("scope")
		public String getScope() {
			return scope;
		}

		public String getPointsTo() {
			return pointsTo;
		}

		@RosettaAttribute("value")
		@RuneAttribute("@ref:scoped")
		public String getReference() {
			return reference;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pointsTo == null) ? 0 : pointsTo.hashCode());
			result = prime * result + ((reference == null) ? 0 : reference.hashCode());
			result = prime * result + ((scope == null) ? 0 : scope.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Reference other = (Reference) obj;
			if (pointsTo == null) {
				if (other.getPointsTo() != null)
					return false;
			} else if (!pointsTo.equals(other.getPointsTo()))
				return false;
			if (reference == null) {
				if (other.getReference() != null)
					return false;
			} else if (!reference.equals(other.getReference()))
				return false;
			if (scope == null) {
				if (other.getScope() != null)
					return false;
			} else if (!scope.equals(other.getScope()))
				return false;
			return true;
		}

		@Override
		public ReferenceBuilder toBuilder() {
			return new ReferenceBuilderImpl().setScope(scope).setPointsTo(pointsTo).setReference(reference);
		}

		@Override
		public Reference build() {
			return this;
		}
	}

	public static class ReferenceBuilderImpl implements ReferenceBuilder {

		private String scope;
		private String pointsTo;
		private String reference;

		@RosettaAttribute("scope")
		public String getScope() {
			return scope;
		}

		public String getPointsTo() {
			return pointsTo;
		}

		@RosettaAttribute("value")
		@RuneAttribute("@ref:scoped")
		public String getReference() {
			return reference;
		}

		@Override
		public Reference build() {
			return new ReferenceImpl(scope, pointsTo, reference);
		}

		@RosettaAttribute("value")
		@RuneAttribute("@ref:scoped")
		public ReferenceBuilder setReference(String reference) {
			this.reference = reference;
			return this;
		}

		public ReferenceBuilder setPointsTo(String pointsTo) {
			this.pointsTo = pointsTo;
			return this;
		}

		@RosettaAttribute("scope")
		public ReferenceBuilder setScope(String scope) {
			this.scope = scope;
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ReferenceBuilder prune() {
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ReferenceBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			ReferenceBuilder o = (ReferenceBuilder) other;
			merger.mergeBasic(getScope(), o.getScope(), this::setScope);
			merger.mergeBasic(getPointsTo(), o.getPointsTo(), this::setPointsTo);
			merger.mergeBasic(getReference(), o.getReference(), this::setReference);
			return this;
		}

		@Override
		public ReferenceBuilder toBuilder() {
			return this;
		}

		@Override
		public boolean hasData() {
			return getReference() != null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pointsTo == null) ? 0 : pointsTo.hashCode());
			result = prime * result + ((reference == null) ? 0 : reference.hashCode());
			result = prime * result + ((scope == null) ? 0 : scope.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReferenceBuilder other = (ReferenceBuilder) obj;
			if (pointsTo == null) {
				if (other.getPointsTo() != null)
					return false;
			} else if (!pointsTo.equals(other.getPointsTo()))
				return false;
			if (reference == null) {
				if (other.getReference() != null)
					return false;
			} else if (!reference.equals(other.getReference()))
				return false;
			if (scope == null) {
				if (other.getScope() != null)
					return false;
			} else if (!scope.equals(other.getScope()))
				return false;
			return true;
        }
	}
}
