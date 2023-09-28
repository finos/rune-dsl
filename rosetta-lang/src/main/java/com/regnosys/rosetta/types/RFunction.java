package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.rosetta.model.lib.ModelId;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class RFunction {
	private ModelSymbolId symbolId;
	private ModelReportId reportId;
	private String definition;
	private List<RAttribute> inputs;
	private RAttribute output;
	private RFunctionOrigin origin;
	private List<Condition> preConditions;
	private List<Condition> postConditions;
	private List<RShortcut> shortcuts;
	private List<ROperation> operations;
	private List<AnnotationRef> annotations;
	
	private RFunction(String definition, List<RAttribute> inputs,
			RAttribute output, RFunctionOrigin origin, List<Condition> preConditions, List<Condition> postConditions,
			List<RShortcut> shortcuts, List<ROperation> operations, List<AnnotationRef> annotations) {
		this.definition = definition;
		this.inputs = inputs;
		this.output = output;
		this.origin = origin;
		this.preConditions = preConditions;
		this.postConditions = postConditions;
		this.shortcuts = shortcuts;
		this.operations = operations;
		this.annotations = annotations;
	}
	
	public RFunction(ModelSymbolId symbolId, String definition, List<RAttribute> inputs,
			RAttribute output, RFunctionOrigin origin, List<Condition> preConditions, List<Condition> postConditions,
			List<RShortcut> shortcuts, List<ROperation> operations, List<AnnotationRef> annotations) {
		this(definition, inputs, output, origin, preConditions, postConditions,
			shortcuts, operations, annotations);
		this.symbolId = symbolId;
	}
	public RFunction(ModelReportId reportId, String definition, List<RAttribute> inputs,
			RAttribute output, RFunctionOrigin origin, List<Condition> preConditions, List<Condition> postConditions,
			List<RShortcut> shortcuts, List<ROperation> operations, List<AnnotationRef> annotations) {
		this(definition, inputs, output, origin, preConditions, postConditions,
			shortcuts, operations, annotations);
		this.reportId = reportId;
	}
	
	public DottedPath getNamespace() {
		if (symbolId != null) {
			return symbolId.getNamespace();
		}
		return reportId.getNamespace();
	}
	
	public ModelId getId() {
		if (symbolId != null) {
			return symbolId;
		}
		return reportId;
	}
	
	public ModelSymbolId getSymbolId() {
		return symbolId;
	}
	
	public ModelReportId getReportId() {
		return reportId;
	}
	
	public String getAlphanumericName() {
		return getId().getAlphanumericName();
	}

	public String getDefinition() {
		return definition;
	}

	public List<RAttribute> getInputs() {
		return inputs;
	}

	public RAttribute getOutput() {
		return output;
	}

	public RFunctionOrigin getOrigin() {
		return origin;
	}

	public List<Condition> getPreConditions() {
		return preConditions;
	}

	public List<Condition> getPostConditions() {
		return postConditions;
	}

	public List<RShortcut> getShortcuts() {
		return shortcuts;
	}

	public List<ROperation> getOperations() {
		return operations;
	}

	public List<AnnotationRef> getAnnotations() {
		return annotations;
	}

	@Override
	public int hashCode() {
		return Objects.hash(symbolId, reportId, annotations, definition, inputs, operations, origin, output,
				postConditions, preConditions, shortcuts);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RFunction other = (RFunction) obj;
		return Objects.equals(reportId, other.reportId)
				&& Objects.equals(annotations, other.annotations) && Objects.equals(definition, other.definition)
				&& Objects.equals(inputs, other.inputs) && Objects.equals(symbolId, other.symbolId)
				&& Objects.equals(operations, other.operations)
				&& origin == other.origin && Objects.equals(output, other.output)
				&& Objects.equals(postConditions, other.postConditions)
				&& Objects.equals(preConditions, other.preConditions) && Objects.equals(shortcuts, other.shortcuts);
	}
}
