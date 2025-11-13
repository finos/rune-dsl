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

package com.regnosys.rosetta.validation;

public interface RosettaIssueCodes {
	
	static final String PREFIX = "RosettaIssueCodes.";
	
	static final String DUPLICATE_ATTRIBUTE = PREFIX + "duplicateAttribute" ;
	static final String DUPLICATE_ENUM_VALUE = PREFIX + "duplicateEnumValue";
	static final String DUPLICATE_ELEMENT_NAME = PREFIX + "duplicateName" ;
	static final String INVALID_NAME = PREFIX + "invalidName";
	static final String INVALID_CASE = PREFIX + "invalidCase";
	static final String MISSING_ATTRIBUTE = PREFIX + "missingAttribute";
	static final String TYPE_ERROR = PREFIX + "typeError";
	static final String INVALID_TYPE = PREFIX + "InvalidType";
	static final String DUPLICATE_CHOICE_RULE_ATTRIBUTE = PREFIX + "DuplicateChoiceRuleAttribute";
	static final String CLASS_WITH_CHOICE_RULE_AND_ONE_OF_RULE = PREFIX + "ClassWithChoiceRuleAndOneOfRule";
	static final String MULIPLE_CLASS_REFERENCES_DEFINED_FOR_CONDITION = PREFIX + "MulipleClassReferencesDefinedForCondition";
	static final String MAPPING_RULE_INVALID = PREFIX + "MappingRuleInvalid";
	static final String MAPPING_RULE_NOT_USED = PREFIX + "MappingRuleNotUsed";
	static final String MISSING_ENUM_VALUE = PREFIX + "MissingEnumValue";
	static final String CARDINALITY_ERROR=PREFIX +"cardinalityError";
	static final String INVALID_ELEMENT_NAME=PREFIX +"invalidElementName";
	static final String UNUSED_IMPORT = PREFIX + "unusedImport";
	static final String DUPLICATE_IMPORT = PREFIX + "duplicateImport";

	static final String MANDATORY_SQUARE_BRACKETS = PREFIX + "mandatorySquareBrackets";
	static final String REDUNDANT_SQUARE_BRACKETS = PREFIX + "redundantSquareBrackets";
	static final String MANDATORY_THEN = PREFIX + "mandatoryThen";
	static final String MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT = PREFIX + "missingAttributes";

    static final String CHANGED_EXTENDED_FUNCTION_PARAMETERS = PREFIX + "changedExtendedFunctionParameters";
}
