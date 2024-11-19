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
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import org.eclipse.xtext.formatting2.FormatterRequest
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.RosettaOperation
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall

class RosettaExpressionFormatter extends AbstractRosettaFormatter2 {
	
	@Inject extension RosettaGrammarAccess
	@Inject extension FormattingUtil
	
	private def boolean isSimple(RosettaExpression expr) {
		switch expr {
			RosettaLiteral:
				true
			RosettaImplicitVariable:
				true
			RosettaSymbolReference:
				!expr.explicitArguments
			RosettaFeatureCall:
				true
			ArithmeticOperation:
				true
			ComparisonOperation:
				true
			default:
				false
		}
	}
	
	private def boolean shouldBeOnSingleLine(RosettaExpression expr) {
		if (expr.isSimple) {
			return true
		}
		switch expr {
			RosettaBinaryOperation:
				expr.left.isSimple || expr.right.isSimple
			RosettaFunctionalOperation:
				expr.function === null && expr.argument.isSimple
			RosettaUnaryOperation:
				expr.argument.isSimple
			ListLiteral:
				expr.elements.forall[isSimple]
			default:
				false
		}
	}
	
	private def boolean isEmpty(RosettaExpression expr) {
		expr === null || expr.isGenerated
	}
	
	override void initialize(FormatterRequest request) {
		super.initialize(request)
	}
	
	override format(Object obj, IFormattableDocument document) {
		switch (obj) {
			RosettaExpression: formatExpression(obj, document)
			default: throw new UnsupportedOperationException('''«RosettaExpressionFormatter» does not support formatting «obj».''')
		}
	}
	
	def void formatExpression(RosettaExpression expr, IFormattableDocument document) {
		formatExpression(expr, document, FormattingMode.NORMAL)
	}
	def void formatExpression(RosettaExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		if (!expr.isGenerated) {
			val leftParenthesis = expr.regionFor.keyword(rosettaCalcPrimaryAccess.leftParenthesisKeyword_7_0)
			val rightParenthesis = expr.regionFor.keyword(rosettaCalcPrimaryAccess.rightParenthesisKeyword_7_2);
			if (leftParenthesis !== null && rightParenthesis !== null) {
				leftParenthesis
					.append[noSpace]
			    rightParenthesis
					.prepend[noSpace]
				if (!expr.isMultiline) {
					unsafeFormatExpression(expr, document, FormattingMode.SINGLE_LINE)
				} else {
					unsafeFormatExpression(expr, document, mode.stopChain)
				}
			} else {
				unsafeFormatExpression(expr, document, mode)
			}
		}
	}
	
	private def dispatch void unsafeFormatExpression(RosettaConstructorExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension constructorGrammarAccess = rosettaCalcConstructorExpressionAccess
				
		interior(
			expr.regionFor.keyword(leftCurlyBracketKeyword_2)
				.prepend[oneSpace]
				.append[newLine],
			expr.regionFor.keyword(rightCurlyBracketKeyword_4)
				.prepend[newLine],
			[indent]
		)
		
		expr.regionFor.keywords(',').forEach[
			prepend[noSpace]
			append[newLine]
		]
		
		expr.values.forEach[
			indentInner(document)
			regionFor.keyword(':').
				prepend[noSpace]
				.append[oneSpace]
			value.formatExpression(document, mode)
		]
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
		
		formatInlineOrMultiline(document, expr, mode.singleLineIf(expr.shouldBeOnSingleLine),
			[extension doc | // case: short list
				expr.regionFor.keyword('[')
					.append[noSpace]
				expr.regionFor.keyword(']')
					.prepend[noSpace]
				expr.regionFor.keywords(',').forEach[
					append[oneSpace]
				]
				expr.elements.forEach[formatExpression(doc, mode)]
			],
			[extension doc | // case: long list
				expr.regionFor.keyword('[')
					.append[newLine]
				expr.elements.last
					.append[newLine]
				expr.regionFor.keywords(',').forEach[
					append[newLine]
				]
				expr.elements.forEach[formatExpression(doc, mode.stopChain)]
			]
		);
	}
	
	private def dispatch void unsafeFormatExpression(RosettaConditionalExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension conditionalGrammarAccess = rosettaCalcConditionalExpressionAccess
		
		expr.regionFor.keywords(ifKeyword_1, thenKeyword_3, fullElseKeyword_5_0_0).forEach[
			append[oneSpace]
		]
		val subExprs = #[expr.^if, expr.ifthen, expr.elsethen]
		#[expr.^if, expr.ifthen].forEach[
			if (!(it instanceof RosettaUnaryOperation)) {
				surround(
					it,
					[indent]
				)
			}
		]
		formatInlineOrMultiline(document, expr, mode.singleLineIf(expr.shouldBeOnSingleLine), document.getPreference(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth),
			[extension doc | // case: short conditional
				expr.regionFor.keyword(thenKeyword_3)
					.prepend[oneSpace]
				expr.regionFor.keyword(fullElseKeyword_5_0_0)
					.prepend[oneSpace]
				subExprs.forEach[formatExpression(doc, mode)]
			],
			[extension doc | // case: long conditional
				expr.regionFor.keyword(thenKeyword_3)
					.prepend[newLine]
				expr.regionFor.keyword(fullElseKeyword_5_0_0)
					.prepend[newLine]
				if (expr.eContainingFeature == ROSETTA_BINARY_OPERATION__RIGHT) {
					expr.indentInner(doc)
				}
				expr.^if.formatExpression(doc, mode.stopChain)
				expr.ifthen.formatExpression(doc, mode.stopChain)
				expr.elsethen.formatExpression(doc, mode.chainIf(expr.elsethen instanceof RosettaConditionalExpression))
			]
		)
	}
	
	private def dispatch void unsafeFormatExpression(RosettaFeatureCall expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keyword('->').surround[oneSpace]
		expr.receiver.formatExpression(document, mode.stopChain)
	}
	
	private def dispatch void unsafeFormatExpression(RosettaDeepFeatureCall expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keyword('->>').surround[oneSpace]
		expr.receiver.formatExpression(document, mode.stopChain)
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
			formatExpression(document, mode.stopChain)
		]
	}
	
	private def dispatch void unsafeFormatExpression(RosettaImplicitVariable expr, extension IFormattableDocument document, FormattingMode mode) {
		
	}
	
	private def dispatch void unsafeFormatExpression(RosettaSymbolReference expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension referenceCallGrammarAccess = rosettaReferenceOrFunctionCallAccess
		
		if (expr.explicitArguments) {
			expr.regionFor.keywords(',').forEach[
				prepend[noSpace]
			]
			expr.regionFor.keyword(explicitArgumentsLeftParenthesisKeyword_0_2_0_0)
				.prepend[noSpace]
			
			formatInlineOrMultiline(document, expr, mode.singleLineIf(expr.shouldBeOnSingleLine),
				[extension doc | // case: short argument list
					expr.regionFor.keyword('(')
						.append[noSpace]
					expr.regionFor.keyword(')')
						.prepend[noSpace]
					expr.regionFor.keywords(',').forEach[
						append[oneSpace]
					]
					expr.args.forEach[formatExpression(doc, mode)]
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
					expr.args.forEach[formatExpression(doc, mode.stopChain)]
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
		
		formatInlineOrMultiline(document, expr, mode.singleLineIf(expr.shouldBeOnSingleLine),
			[extension doc | // case: short operation
				if (!expr.left.isEmpty) {
					expr.left.nextHiddenRegion
						.set[oneSpace]
					expr.left
						.formatExpression(doc, mode)
				}
				expr.right.formatExpression(doc, mode)
			],
			[extension doc | // case: long operation
				if (!expr.left.isEmpty) {
					val afterArgument = expr.left.nextHiddenRegion
					expr.indentInner(afterArgument, doc)
					afterArgument
						.set[newLine]
					
					val leftIsSameOperation = if (expr.left instanceof RosettaBinaryOperation) {
							expr.operator == (expr.left as RosettaBinaryOperation).operator
						} else {
							false
						}
					if (expr.left instanceof RosettaBinaryOperation && !leftIsSameOperation) {
						expr.left.indentInner(doc)
					}
					
					expr.left.formatExpression(doc, mode.chainIf(leftIsSameOperation))
				}
				expr.right.formatExpression(doc, mode.stopChain)
			]
		)
	}
	
	private def dispatch void unsafeFormatExpression(RosettaFunctionalOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		expr.formatUnaryOperation(
			document,
			mode,
			[extension doc |
				if (expr.function !== null) {
					expr.function.formatInlineFunction(doc, mode.stopChain)
				}
			]
		)
	}
	
	private def void formatInlineFunction(InlineFunction f, extension IFormattableDocument document, FormattingMode mode) {
		val op = f.eContainer as RosettaFunctionalOperation
		val left = f.regionFor.keyword('[')
		if (left !== null) { // case inline function with brackets
			val right = f.regionFor.keyword(']')
			f.parameters.forEach[
				prepend[oneSpace]
			]
			left
				.prepend[oneSpace]
			f.regionFor.keywords(',').forEach[
				prepend[noSpace]
			]
			
			formatInlineOrMultiline(document, f, mode,
				[extension doc | // case: short inline function
					left
						.append[oneSpace]
					right
						.prepend[oneSpace]
					f.body.formatExpression(doc, mode)
					if (op.eContainer instanceof RosettaOperation) {
						// Always put next operations on a new line.
						f.append[highPriority; newLine]
					}
				],
				[extension doc | // case: long inline function
					interior(
						left
							.append[newLine],
						right
							.prepend[newLine],
						[indent]
					)
					f.body.formatExpression(doc, mode.stopChain)
				]
			)
		} else { // case inline function without brackets
			val astRegion = f.regionForEObject
			val formattableRegion = astRegion.merge(astRegion.previousHiddenRegion).merge(astRegion.nextHiddenRegion)
			if (!(op instanceof ThenOperation && f.body instanceof RosettaUnaryOperation)) {
				surround(
					f.body,
					[indent]
				)
			}
			formatInlineOrMultiline(document, astRegion, formattableRegion, mode.singleLineIf(op instanceof ThenOperation),
				[extension doc | // case: short inline function
					f.body
						.prepend[oneSpace]
						.formatExpression(doc, mode)
					if (f.eContainer.eContainer instanceof RosettaOperation) {
						// Always put next operations on a new line.
						f.append[highPriority; newLine]
					}
				],
				[extension doc | // case: long inline function
					f.body
						.prepend[newLine]
						.formatExpression(doc, mode)
				]
			)
		}
	}

	private def dispatch void unsafeFormatExpression(RosettaExistsExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		expr.formatUnaryOperation(document, mode, [])
		expr.regionFor.feature(ROSETTA_EXISTS_EXPRESSION__MODIFIER)
			.append[oneSpace]
	}
	
	private def dispatch void unsafeFormatExpression(ChoiceOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		expr.formatUnaryOperation(document, mode, [])
		expr.regionFor.feature(ROSETTA_OPERATION__OPERATOR)
			.surround[oneSpace]
		expr.allRegionsFor.keyword(',')
			.prepend[noSpace]
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
		formatInlineOrMultiline(document, expr, mode.singleLineIf(expr.shouldBeOnSingleLine),
			[extension doc | // case: short operation
				if (!expr.argument.isEmpty) {
					val afterArgument = expr.argument.nextHiddenRegion
					afterArgument
						.set[oneSpace]
					expr.argument.formatExpression(doc, mode)
				}
				internalFormatter.apply(doc)
			],
			[extension doc | // case: long operation
				if (!expr.argument.isEmpty) {
					val afterArgument = expr.argument.nextHiddenRegion
					var initialArgument = expr.argument
					while (initialArgument instanceof RosettaUnaryOperation) {
						initialArgument = initialArgument.argument
					}
					if (!initialArgument.isEmpty) {
						expr.indentInner(afterArgument, doc)
					}
					afterArgument
						.set[newLine]
					expr.argument.formatExpression(doc, mode.chainIf(expr.argument instanceof RosettaUnaryOperation))
				}
				internalFormatter.apply(doc)
			]
		)
	}
}