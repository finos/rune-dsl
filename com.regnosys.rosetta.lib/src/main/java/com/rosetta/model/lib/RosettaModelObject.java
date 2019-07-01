package com.rosetta.model.lib;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.Processor;

public abstract class RosettaModelObject {

	public abstract RosettaModelObjectBuilder toBuilder();

	protected abstract void process(RosettaPath path, Processor processor);
	
	/**
	 * @return The MetaData {@link RosettaMetaData} object for this class providing access to things like validation
	 */
	public abstract RosettaMetaData<? extends RosettaModelObject> metaData();
	
	public static <A> Stream<A> optionalStream(Collection<A> c) {
		if (c==null) return Stream.empty();
		return c.stream();
	}
	
	protected <R extends RosettaModelObject> void processRosetta(RosettaPath path, Processor processor, Class<R> clazz, RosettaModelObject child, AttributeMeta... metas) {
		processor.processRosetta(path, clazz, child, this, metas);
		if (child!=null) child.process(path, processor);
	}
	protected <R extends RosettaModelObject> void processRosetta(RosettaPath path, Processor processor, Class<R> clazz, List<? extends RosettaModelObject> children, AttributeMeta... metas) {
		if (children!=null)  {
			int index=0;
			for (Iterator<? extends RosettaModelObject> iterator = children.iterator(); iterator.hasNext();) {
				RosettaModelObject child = iterator.next();
				if (child!=null) {
					RosettaPath indexedPath = path.withIndex(index);
					processor.processRosetta(path, clazz, child, this, metas);
					child.process(indexedPath, processor);
					index++;
				}
			}
		}
	}

}