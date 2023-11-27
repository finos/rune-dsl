package com.regnosys.rosetta.generator.java.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.Processor;

public class RosettaAttributePathProcessor implements Processor {

	private final List<RosettaPath> result = new ArrayList<>();

	@Override
	public <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<? extends R> rosettaType,
			R instance, RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
		return true;
	}

	@Override
	public <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<? extends R> rosettaType,
			List<? extends R> instance, RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
		return true;
	}

	@Override
	public <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, T instance,
			RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
	}

	@Override
	public <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, Collection<? extends T> instance,
			RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
	}

	@Override
	public Report report() {
		return null;
	}

	public List<RosettaPath> getResult() {
		return result;
	}
}
