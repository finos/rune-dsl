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