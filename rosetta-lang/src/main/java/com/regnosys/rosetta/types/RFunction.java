/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.rosetta.model.lib.ModelId;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.ModelTranslationId;
import com.rosetta.util.DottedPath;

public class RFunction {
	private ModelSymbolId symbolId;
	private ModelReportId reportId;
	private ModelTranslationId translationId;
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
	public RFunction(ModelTranslationId translationId, String definition, List<RAttribute> inputs,
			RAttribute output, RFunctionOrigin origin, List<Condition> preConditions, List<Condition> postConditions,
			List<RShortcut> shortcuts, List<ROperation> operations, List<AnnotationRef> annotations) {
		this(definition, inputs, output, origin, preConditions, postConditions,
			shortcuts, operations, annotations);
		this.translationId = translationId;
	}
	
	public ModelId getId() {
		if (symbolId != null) {
			return symbolId;
		}
		if (reportId != null) {
			return reportId;
		}
		return translationId;
	}
	
	public ModelSymbolId getSymbolId() {
		return symbolId;
	}
	
	public ModelReportId getReportId() {
		return reportId;
	}
	
	public ModelTranslationId getTranslationId() {
		return translationId;
	}
	
	public DottedPath getNamespace() {
		return getId().getNamespace();
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
