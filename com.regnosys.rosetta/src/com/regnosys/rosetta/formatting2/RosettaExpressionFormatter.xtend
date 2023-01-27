package com.regnosys.rosetta.formatting2

/*
 * For info on the formatter API, see https://www.slideshare.net/meysholdt/xtexts-new-formatter-api.
 */

import org.eclipse.xtext.formatting2.IFormattableDocument
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.services.RosettaGrammarAccess
import javax.inject.Inject
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.FunctionReference
import com.regnosys.rosetta.rosetta.expression.NamedFunctionReference
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import org.eclipse.xtext.formatting2.FormatterRequest
import com.regnosys.rosetta.rosetta.BlueprintNodeExp
import com.regnosys.rosetta.rosetta.BlueprintNode
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintOr
import com.regnosys.rosetta.rosetta.BlueprintRef
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintReturn
import com.regnosys.rosetta.rosetta.BlueprintLookup

class RosettaExpressionFormatter extends AbstractRosettaFormatter2 {
	
	@Inject extension RosettaGrammarAccess
	@Inject extension FormattingUtil
	
	override void initialize(FormatterRequest request) {
		super.initialize(request)
	}
	
	override format(Object obj, IFormattableDocument document) {
		switch (obj) {
			RosettaExpression: formatExpression(obj, document)
			BlueprintNodeExp: formatRuleExpression(obj, document)
			default: throw new UnsupportedOperationException('''«RosettaExpressionFormatter» does not support formatting «obj».''')
		}
	}
	
	def void formatExpression(RosettaExpression expr, IFormattableDocument document) {
		formatExpression(expr, document, FormattingMode.NORMAL)
	}
	def void formatExpression(RosettaExpression expr, IFormattableDocument document, FormattingMode mode) {
		if (!expr.generated) {
			unsafeFormatExpression(expr, document, mode)
		}
	}
	
	
	private def dispatch void unsafeFormatExpression(ListLiteral expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keywords(',').forEach[
			prepend[noSpace]
		]
		interior(
			expr.regionFor.keyword('['),
			expr.regionFor.keyword(']'),
			[indent]
		)
		
		formatInlineOrMultiline(document, expr, FormattingMode.NORMAL,
			[extension doc | // case: short list
				expr.regionFor.keyword('[')
					.append[noSpace]
				expr.regionFor.keyword(']')
					.prepend[noSpace]
				expr.regionFor.keywords(',').forEach[
					append[oneSpace]
				]
				expr.elements.forEach[formatExpression(doc, FormattingMode.NORMAL)]
			],
			[extension doc | // case: long list
				expr.regionFor.keyword('[')
					.append[newLine]
				expr.elements.last
					.append[newLine]
				expr.regionFor.keywords(',').forEach[
					append[newLine]
				]
				expr.elements.forEach[formatExpression(doc, FormattingMode.NORMAL)]
			]
		);
	}
	
	private def dispatch void unsafeFormatExpression(RosettaConditionalExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension conditionalGrammarAccess = rosettaCalcConditionalExpressionAccess
		
		expr.regionFor.keywords(ifKeyword_1, thenKeyword_3, fullElseKeyword_5_0_0).forEach[
			append[oneSpace]
		]
		
		formatInlineOrMultiline(document, expr, mode,
			[extension doc | // case: short conditional
				expr.regionFor.keyword(thenKeyword_3)
					.prepend[oneSpace]
				expr.regionFor.keyword(fullElseKeyword_5_0_0)
					.prepend[oneSpace]
				expr.^if.formatExpression(doc, FormattingMode.NORMAL)
				expr.ifthen.formatExpression(doc, FormattingMode.NORMAL)
				expr.elsethen.formatExpression(doc, FormattingMode.NORMAL)
			],
			[extension doc | // case: long conditional
				expr.regionFor.keyword(thenKeyword_3)
					.prepend[newLine]
				expr.regionFor.keyword(fullElseKeyword_5_0_0)
					.prepend[newLine]
				expr.^if.formatExpression(doc, FormattingMode.NORMAL)
				expr.ifthen.formatExpression(doc, FormattingMode.NORMAL)
				expr.elsethen.formatExpression(doc, FormattingMode.MULTI_LINE)
			]
		)
	}
	
	private def dispatch void unsafeFormatExpression(RosettaFeatureCall expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keyword('->').surround[oneSpace]
		expr.receiver.formatExpression(document, FormattingMode.NORMAL)
	}
	
	private def dispatch void unsafeFormatExpression(RosettaLiteral expr, extension IFormattableDocument document, FormattingMode mode) {

	}
	
	private def dispatch void unsafeFormatExpression(RosettaOnlyExistsExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension onlyExistsGrammarAccess = rosettaCalcOnlyExistsAccess
		
		expr.regionFor.keyword('(')
			.append[noSpace]
		expr.regionFor.keyword(')')
			.prepend[noSpace]
		expr.regionFor.keywords(',').forEach[
			prepend[noSpace]
			append[oneSpace]
		]
		expr.regionFor.keyword(onlyKeyword_2)
			.prepend[oneSpace]
		expr.regionFor.keyword(existsKeyword_3)
			.prepend[oneSpace]
			
		expr.args.forEach[
			formatExpression(document, FormattingMode.NORMAL)
		]
	}
	
	private def dispatch void unsafeFormatExpression(RosettaImplicitVariable expr, extension IFormattableDocument document, FormattingMode mode) {
		
	}
	
	private def dispatch void unsafeFormatExpression(RosettaSymbolReference expr, extension IFormattableDocument document, FormattingMode mode) {
		if (expr.explicitArguments) {
			expr.regionFor.keywords(',').forEach[
				prepend[noSpace]
			]
			expr.regionFor.keyword('(')
				.prepend[noSpace]
			
			formatInlineOrMultiline(document, expr, FormattingMode.NORMAL,
				[extension doc | // case: short argument list
					expr.regionFor.keyword('(')
						.append[noSpace]
					expr.regionFor.keyword(')')
						.prepend[noSpace]
					expr.regionFor.keywords(',').forEach[
						append[oneSpace]
					]
					expr.args.forEach[formatExpression(doc, FormattingMode.NORMAL)]
				],
				[extension doc | // case: long argument list
					expr.indentInner(doc)
					interior(
						expr.regionFor.keyword('(')
							.append[newLine],
						expr.regionFor.keyword(')')
							.prepend[newLine],
						[indent]
					)
					expr.regionFor.keywords(',').forEach[
						append[newLine]
					]
					expr.args.forEach[formatExpression(doc, FormattingMode.NORMAL)]
				]
			)
		}
	}
	
	private def dispatch void unsafeFormatExpression(ModifiableBinaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaBinaryOperation
		expr.formatBinaryOperation(document, mode)
		expr.regionFor.feature(MODIFIABLE_BINARY_OPERATION__CARD_MOD)
			.append[oneSpace]
	}
	
	private def dispatch void unsafeFormatExpression(RosettaBinaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.formatBinaryOperation(document, mode)
	}
	
	private def void formatBinaryOperation(RosettaBinaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.feature(ROSETTA_OPERATION__OPERATOR)
			.append[oneSpace]
		
		formatInlineOrMultiline(document, expr, mode,
			[extension doc | // case: short operation
				if (expr.left !== null) {
					expr.left.nextHiddenRegion
						.set[oneSpace]
					expr.left
						.formatExpression(doc, FormattingMode.NORMAL)
				}
				expr.right.formatExpression(doc, FormattingMode.NORMAL)
			],
			[extension doc | // case: long operation
				if (expr.left !== null) {
					val afterArgument = expr.left.nextHiddenRegion
					expr.indentInner(afterArgument, doc)
					afterArgument
						.set[newLine]
					
					val shouldChain = if (expr.left instanceof RosettaBinaryOperation) {
						expr.operator == (expr.left as RosettaBinaryOperation).operator
					} else {
						false
					}
					expr.left.formatExpression(doc, shouldChain ? FormattingMode.MULTI_LINE : FormattingMode.NORMAL)
				}
				expr.right.formatExpression(doc, FormattingMode.NORMAL)
			]
		)
	}
	
	private def dispatch void unsafeFormatExpression(RosettaFunctionalOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		expr.formatUnaryOperation(
			document,
			mode,
			[expr.functionRef.formatFunctionReference(it)]
		)
	}
	
	private def void formatFunctionReference(FunctionReference ref, extension IFormattableDocument document) {
		switch (ref) {
			NamedFunctionReference:
				ref.prepend[oneSpace]
			InlineFunction: {
				ref.parameters.forEach[
					prepend[oneSpace]
				]
				ref.regionFor.keyword('[')
					.prepend[oneSpace]
				ref.regionFor.keywords(',').forEach[
					prepend[noSpace]
				]
				
				formatInlineOrMultiline(document, ref, FormattingMode.NORMAL,
					[extension doc | // case: short inline function
						ref.regionFor.keyword('[')
							.append[oneSpace]
						ref.regionFor.keyword(']')
							.prepend[oneSpace]
						ref.body.formatExpression(doc)
					],
					[extension doc | // case: long inline function
						interior(
							ref.regionFor.keyword('[')
								.append[newLine],
							ref.regionFor.keyword(']')
								.prepend[newLine],
							[indent]
						)
						ref.body.formatExpression(doc)
					]
				)
			}
		}
	}

	private def dispatch void unsafeFormatExpression(RosettaExistsExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		expr.formatUnaryOperation(document, mode, [])
		expr.regionFor.feature(ROSETTA_EXISTS_EXPRESSION__MODIFIER)
			.append[oneSpace]
	}
	
	private def dispatch void unsafeFormatExpression(RosettaAbsentExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation		
		expr.formatUnaryOperation(document, mode, [])
		expr.regionFor.keyword('is')
			.append[oneSpace]
	}
	
	private def dispatch void unsafeFormatExpression(RosettaUnaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.formatUnaryOperation(document, mode, [])
	}
	
	private def void formatUnaryOperation(RosettaUnaryOperation expr, extension IFormattableDocument document, FormattingMode mode, (IFormattableDocument) => void internalFormatter) {
		formatInlineOrMultiline(document, expr, mode,
			[extension doc | // case: short operation
				if (expr.argument !== null) {
					val afterArgument = expr.argument.nextHiddenRegion
					afterArgument
						.set[oneSpace]
					expr.argument.formatExpression(doc, FormattingMode.NORMAL)
				}
				internalFormatter.apply(doc)
			],
			[extension doc | // case: long operation
				if (expr.argument !== null) {
					val afterArgument = expr.argument.nextHiddenRegion
					expr.indentInner(afterArgument, doc)
					afterArgument
						.set[newLine]
					expr.argument.formatExpression(doc, FormattingMode.MULTI_LINE)
				} else {
					expr.indentInner(doc)
				}
				internalFormatter.apply(doc)
			]
		)
	}
	
	
	
	def void formatRuleExpression(BlueprintNodeExp expr, extension IFormattableDocument document) {
		val extension ruleExprGrammarAccess = blueprintNodeExpAccess
		
		expr.node.formatRuleNode(document, FormattingMode.NORMAL)
		if (expr.next !== null) {
			expr.regionFor.keyword(thenKeyword_2_0)
				.prepend[newLine]
				.append[oneSpace]
			expr.next.formatRuleExpression(document)
		}
	}
	
	private def dispatch void formatRuleNode(BlueprintFilter expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension filterGrammarAccess = blueprintFilterAccess
		
		expr.regionFor.keyword(whenKeyword_1)
			.prepend[oneSpace]
		if (expr.filterBP !== null) {
			expr.regionFor.keyword(ruleKeyword_2_1_0)
				.surround[oneSpace]
			expr.formatAsInline(document)
		} else {
			formatInlineOrMultiline(document, expr, mode,
				[extension doc | // case: short operation
					expr.regionFor.keyword(whenKeyword_1)
						.append[oneSpace]
					expr.filter.formatExpression(doc, FormattingMode.NORMAL)
					expr.formatAsInline(doc)
				],
				[extension doc | // case: long operation
					expr.regionFor.keyword(whenKeyword_1)
						.append[newLine]
					expr.filter
						.surround[indent]
						.formatExpression(doc, FormattingMode.NORMAL)
					expr.formatAsMultiline(doc)
				]
			)
		}
	}
	
	private def dispatch void formatRuleNode(BlueprintOr expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keywords(',').forEach[
			prepend[noSpace]
		]
		interior(
			expr.regionFor.keyword('('),
			expr.regionFor.keyword(')'),
			[indent]
		)
		expr.regionFor.keyword('(')
			.append[newLine]
		expr.bps.last
			.append[newLine]
		expr.regionFor.keywords(',').forEach[
			append[newLine]
		]
		expr.bps.forEach[formatRuleExpression(document)]
	}
	
	private def dispatch void formatRuleNode(BlueprintRef expr, extension IFormattableDocument document, FormattingMode mode) {
		
	}
	
	private def dispatch void formatRuleNode(BlueprintExtract expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension extractGrammarAccess = blueprintExtractAccess
		
		val lastKeyword = if (expr.repeatable) {
			expr.regionFor.keyword(repeatableRepeatableKeyword_1_0)
				.prepend[oneSpace]
		} else {
			expr.regionFor.keyword(extractKeyword_0)
		}
		formatInlineOrMultiline(document, expr, mode,
			[extension doc | // case: short operation
				lastKeyword
					.append[oneSpace]
				expr.call.formatExpression(doc, FormattingMode.NORMAL)
				expr.formatAsInline(doc)
			],
			[extension doc | // case: long operation
				lastKeyword
					.append[newLine]
				expr.call
					.surround[indent]
					.formatExpression(doc, FormattingMode.NORMAL)
				expr.formatAsMultiline(doc)
			]
		)
	}
	
	private def dispatch void formatRuleNode(BlueprintReturn expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension returnGrammarAccess = blueprintReturnAccess
		
		formatInlineOrMultiline(document, expr, mode,
			[extension doc | // case: short operation
				expr.regionFor.keyword(returnKeyword_0)
					.append[oneSpace]
				expr.expression.formatExpression(doc, FormattingMode.NORMAL)
				expr.formatAsInline(doc)
			],
			[extension doc | // case: long operation
				expr.regionFor.keyword(returnKeyword_0)
					.append[newLine]
				expr.expression
					.surround[indent]
					.formatExpression(doc, FormattingMode.NORMAL)
				expr.formatAsMultiline(doc)
			]
		)
	}
	
	private def dispatch void formatRuleNode(BlueprintLookup expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension lookupGrammarAccess = blueprintLookupAccess
		
		expr.regionFor.assignment(nameAssignment_1)
			.surround[oneSpace]
		expr.formatAsInline(document)
	}
	
	private def void formatAsInline(BlueprintNode expr, extension IFormattableDocument document) {
		val extension filterGrammarAccess = blueprintNodeAccess
		
		if (expr.identifier !== null) {
			expr.regionFor.keyword(asKeyword_1_0)
				.surround[oneSpace]
		}
	}
	
	private def void formatAsMultiline(BlueprintNode expr, extension IFormattableDocument document) {
		val extension filterGrammarAccess = blueprintNodeAccess
		
		if (expr.identifier !== null) {
			expr.regionFor.keyword(asKeyword_1_0)
				.prepend[newLine]
				.append[oneSpace]
		}
	}
}