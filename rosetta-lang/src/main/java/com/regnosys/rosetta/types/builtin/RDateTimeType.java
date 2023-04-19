package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.List;

import com.regnosys.rosetta.rosetta.RosettaRecordType;

public class RDateTimeType extends RRecordType {
	private final RRecordFeature dateFeature;
	private final RRecordFeature timeFeature;

	public RDateTimeType() {
		super("dateTime");
		this.dateFeature = new RRecordFeature("date");
		this.timeFeature = new RRecordFeature("time");
	}

	@Override
	public Collection<RRecordFeature> getFeatures() {
		return List.of(dateFeature, timeFeature);
	}
}
