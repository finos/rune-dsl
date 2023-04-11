package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.List;

public class RDateType extends RRecordType {
	public final RRecordFeature dayFeature;
	public final RRecordFeature monthFeature;
	public final RRecordFeature yearFeature;

	public RDateType() {
		super("date");
		this.dayFeature = new RRecordFeature(this, "day");
		this.monthFeature = new RRecordFeature(this, "month");
		this.yearFeature = new RRecordFeature(this, "year");
	}

	@Override
	public Collection<RRecordFeature> getFeatures() {
		return List.of(dayFeature, monthFeature, yearFeature);
	}
}
