package com.rosetta.model.lib.meta;

import java.util.Objects;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;

/**
 * @author TomForwood
 * This class represents a reference to a rosetta object defined elsewhere.
 * <p>
 * The scope defines where the resolver should look to find the object linked to.
 * <p>
 * Scope can be
 * - global - the key is universally unique and can be looked up anywhere - e.g. external database
 * - document - the key must be unique in this document and can be found anywhere in the document
 * - the name of the rosetta class e.g. TradeableProduct- the key is only unique inside that TradeableProduct and should only be looked for inside that TradeableProduct
 */
public class Reference extends RosettaModelObject {
	
	private final String scope;
	private final String pointsTo;
	private final String reference;

	public Reference(String scope, String pointsTo, String reference) {
		this.scope = scope;
		this.pointsTo = pointsTo;
		this.reference = reference;
	}

	public String getScope() {
		return scope;
	}

	public String getPointsTo() {
		return pointsTo;
	}

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
			if (other.pointsTo != null)
				return false;
		} else if (!pointsTo.equals(other.pointsTo))
			return false;
		if (reference == null) {
			if (other.reference != null)
				return false;
		} else if (!reference.equals(other.reference))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	@Override public String toString() {
		return "Reference{" +
				"scope='" + scope + '\'' +
				", pointsTo='" + pointsTo + '\'' +
				", reference='" + reference + '\'' +
				'}';
	}

	@Override
	public ReferenceBuilder toBuilder() {
		return new ReferenceBuilder().setScope(scope).setPointsTo(pointsTo).setReference(reference);
	}

	@Override
	protected void process(RosettaPath path, Processor processor) {
	}

	@Override
	public RosettaMetaData<? extends RosettaModelObject> metaData() {
		return null;
	}

	public static class ReferenceBuilder extends RosettaModelObjectBuilder {

		private String scope;
		private String pointsTo;
		private String reference;

		public String getScope() {
			return scope;
		}

		public String getPointsTo() {
			return pointsTo;
		}

		public String getReference() {
			return reference;
		}

		@Override
		public Reference build() {
			return new Reference(scope, pointsTo, reference);
		}

		public ReferenceBuilder setReference(String reference) {
			this.reference = reference;
			return this;
		}

		public ReferenceBuilder setPointsTo(String pointsTo) {
			this.pointsTo = pointsTo;
			return this;
		}

		public ReferenceBuilder setScope(String scope) {
			this.scope = scope;
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ReferenceBuilder prune() {
			return this;
		}

		@Override
		public boolean hasData() {
			return this.reference != null;
		}

		@Override
		public RosettaMetaData<? extends RosettaModelObject> metaData() {
			return null;
		}

		@Override
		public void process(RosettaPath path, BuilderProcessor processor) {
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
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ReferenceBuilder that = (ReferenceBuilder) o;
			return Objects.equals(scope, that.scope) && 
					Objects.equals(pointsTo, that.pointsTo) && 
					Objects.equals(reference, that.reference);
		}

		@Override 
		public int hashCode() {
			return Objects.hash(scope, pointsTo, reference);
		}

		@Override 
		public String toString() {
			return "ReferenceBuilder{" +
					"scope='" + scope + '\'' +
					", pointsTo='" + pointsTo + '\'' +
					", reference='" + reference + '\'' +
					'}';
		}
	}
}
