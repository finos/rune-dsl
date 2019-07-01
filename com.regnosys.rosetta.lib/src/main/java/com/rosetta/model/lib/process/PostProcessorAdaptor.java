package com.rosetta.model.lib.process;

import java.util.Optional;

import com.rosetta.lib.postprocess.PostProcessor;
import com.rosetta.lib.postprocess.PostProcessorReport;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;

/**
 * @author TomForwood
 * This class is intended as a temporary wrapper for old style post processors to transform them into the newer PostProcessorStep
 */
public class PostProcessorAdaptor implements PostProcessStep {

	private final PostProcessor processor;
	
	public PostProcessorAdaptor(PostProcessor processor) {
		super();
		this.processor = processor;
	}

	@Override
	public Integer getPriority() {
		return 2;
	}

	@Override
	public String getName() {
		return processor.getClass().getSimpleName();
	}
	
	public PostProcessor getUnderlying() {
		return processor;
	}

	@Override
	public <T extends RosettaModelObject> PostProcessorReport runProcessStep(Class<T> topClass, RosettaModelObjectBuilder builder) {
		@SuppressWarnings("unchecked")
		T process = processor.process(topClass, (T)builder.build());
		Optional<PostProcessorReport> report = processor.getReport();
		RosettaModelObjectBuilder resultBuilder = process.toBuilder();
		return new PostProcessReportAdapter<>(report, resultBuilder);
	}
	
	public class PostProcessReportAdapter<T extends RosettaModelObject> implements PostProcessorReport {
		private Optional<PostProcessorReport> resultReport;
		private RosettaModelObjectBuilder resultObject;
		
		public PostProcessReportAdapter(Optional<PostProcessorReport> resultReport, RosettaModelObjectBuilder resultObject) {
			super();
			this.resultReport = resultReport;
			this.resultObject = resultObject;
		}

		public Optional<PostProcessorReport> getResultReport() {
			return resultReport;
		}

		public void setResultReport(Optional<PostProcessorReport> resultReport) {
			this.resultReport = resultReport;
		}

		@Override
		public RosettaModelObjectBuilder getResultObject() {
			return resultObject;
		}

		public void setResultObject(RosettaModelObjectBuilder resultObject) {
			this.resultObject = resultObject;
		}
	}

}
