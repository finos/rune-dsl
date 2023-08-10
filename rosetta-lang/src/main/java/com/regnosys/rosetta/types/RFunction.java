package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.rosetta.util.DottedPath;

public class RFunction {
	private String name;
	private DottedPath namespace;
	private String definition;
	private List<RAttribute> inputs;
	private RAttribute output;
	private List<Condition> preConditions;
	private List<Condition> postConditions;
	private List<RShortcut> shortcuts;
	private List<ROperation> operations;
	private List<AnnotationRef> annotations;
	
	public RFunction(String name, DottedPath namespace, String definition, List<RAttribute> inputs,
			RAttribute output, List<Condition> preConditions, List<Condition> postConditions,
			List<RShortcut> shortcuts, List<ROperation> operations, List<AnnotationRef> annotations) {
		this.name = name;
		this.namespace = namespace;
		this.definition = definition;
		this.inputs = inputs;
		this.output = output;
		this.preConditions = preConditions;
		this.postConditions = postConditions;
		this.shortcuts = shortcuts;
		this.operations = operations;
		this.annotations = annotations;
	}

	public String getName() {
		return name;
	}

	public DottedPath getNamespace() {
		return namespace;
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
	
	public DottedPath getCanonicalName() {
		return namespace.child(name);
	}
	
	

	public List<AnnotationRef> getAnnotations() {
		return annotations;
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotations, definition, inputs, name, namespace, operations, output, postConditions,
				preConditions, shortcuts);
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
		return Objects.equals(annotations, other.annotations) && Objects.equals(definition, other.definition)
				&& Objects.equals(inputs, other.inputs) && Objects.equals(name, other.name)
				&& Objects.equals(namespace, other.namespace) && Objects.equals(operations, other.operations)
				&& Objects.equals(output, other.output) && Objects.equals(postConditions, other.postConditions)
				&& Objects.equals(preConditions, other.preConditions) && Objects.equals(shortcuts, other.shortcuts);
	}

	
}
