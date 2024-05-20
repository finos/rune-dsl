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

package com.rosetta.model.lib.functions;

import java.util.Objects;

import com.rosetta.model.lib.functions.IResult.Attribute;

public class Formula {

	private String name;
	private String formula;
	private ICalculationInput calculationInput;

	public Formula(String name, String formula, ICalculationInput calculationInput) {
		this.name = name;
        this.formula = formula;
        this.calculationInput = calculationInput;
	}
	
	public String getFormula() {
		return formula;
	}
	
	public String getName() {
		return name;
	}
	
	public String getParameterisedFormula() {
		String parameterisedFormula = formula;
		for (Attribute<?> attribute : calculationInput.getAttributes()) {
			String name = attribute.getName();
			String value = Objects.toString(attribute.get(calculationInput));
			parameterisedFormula = parameterisedFormula.replaceAll(name, value);
		}
	    return parameterisedFormula;
	}
}