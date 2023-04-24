package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.List;

public class RZonedDateTimeType extends RRecordType {
	private final RRecordFeature dateFeature;
	private final RRecordFeature timeFeature;
	private final RRecordFeature timezoneFeature;

	public RZonedDateTimeType() {
		super("zonedDateTime");
		this.dateFeature = new RRecordFeature("date");
		this.timeFeature = new RRecordFeature("time");
		this.timezoneFeature = new RRecordFeature("timezone");
	}

	@Override
	public Collection<RRecordFeature> getFeatures() {
		return List.of(dateFeature, timeFeature, timezoneFeature);
	}
}
