package com.rosetta.model.lib.reports;

import com.rosetta.model.lib.functions.RosettaFunction;

public interface ReportFunction<Input, Report> extends RosettaFunction {
	Report evaluate(Input reportableInput);
}
