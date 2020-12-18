package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;

public class Reference extends RosettaModelObject{
	private final String scope;
	private final String pointsTo;
	private final String reference;
	
	public Reference(String scope, String pointsTo, String reference) {
		super();
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
			return this.reference!=null;
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
			ReferenceBuilder o = (ReferenceBuilder)other;
			merger.mergeBasic(getScope(), o.getScope(), this::setScope);
			merger.mergeBasic(getPointsTo(), o.getPointsTo(), this::setPointsTo);
			merger.mergeBasic(getReference(), o.getReference(), this::setReference);
			return this;
		}
		
	}
}
