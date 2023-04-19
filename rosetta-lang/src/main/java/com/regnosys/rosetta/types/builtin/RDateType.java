package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.List;

import com.regnosys.rosetta.rosetta.RosettaRecordType;

public class RDateType extends RRecordType {
	private final RRecordFeature dayFeature;
	private final RRecordFeature monthFeature;
	private final RRecordFeature yearFeature;

	public RDateType() {
		super("date");
		this.dayFeature = new RRecordFeature("day");
		this.monthFeature = new RRecordFeature("month");
		this.yearFeature = new RRecordFeature("year");
	}

	@Override
	public Collection<RRecordFeature> getFeatures() {
		return List.of(dayFeature, monthFeature, yearFeature);
	}
}
