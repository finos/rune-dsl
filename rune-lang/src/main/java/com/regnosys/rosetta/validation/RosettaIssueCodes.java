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

import java.util.Set;

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

	static final String UNUSED_FUNCTION = PREFIX + "unusedFunction";
	static final String UNUSED_TYPE = PREFIX + "unusedType";
	static final String UNUSED_ENUMERATION = PREFIX + "unusedEnumeration";
	static final String UNUSED_REPORTING_RULE = PREFIX + "unusedReportingRule";
	static final String UNUSED_ELIGIBILITY_RULE = PREFIX + "unusedEligibilityRule";
	static final String UNUSED_TYPE_ALIAS = PREFIX + "unusedTypeAlias";
	static final String UNUSED_ANNOTATION = PREFIX + "unusedAnnotation";
	static final String UNUSED_SCHEMA = PREFIX + "unusedSchema";

	/**
	 * The fallback code for a declaration kind with no code of its own: the documentation elements
	 * ({@code body}, {@code corpus}, {@code segment}), {@code rule source}, the builtin-shaped kinds
	 * ({@code basicType}, {@code recordType}, {@code library function}) and any root element the grammar
	 * gains in future. Splitting one of these out into its own code later is not a breaking change, because
	 * consumers gate on {@link #UNUSED_CODES} rather than on an individual code.
	 */
	static final String UNUSED_DECLARATION = PREFIX + "unusedDeclaration";

	/**
	 * The issue codes for declarations that are never referenced. These are the codes the language server
	 * renders as a faded editor marker rather than a problem; the codes are kept separate per kind, where a
	 * kind-specific quick fix is plausible, so that one remains possible.
	 */
	static final Set<String> UNUSED_CODES = Set.of(
			UNUSED_FUNCTION, UNUSED_TYPE, UNUSED_ENUMERATION, UNUSED_REPORTING_RULE, UNUSED_ELIGIBILITY_RULE,
			UNUSED_TYPE_ALIAS, UNUSED_ANNOTATION, UNUSED_SCHEMA, UNUSED_DECLARATION);
}
