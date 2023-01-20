package com.regnosys.rosetta.formatting2

/*
 * For info on the formatter API, see https://www.slideshare.net/meysholdt/xtexts-new-formatter-api.
 */

import org.eclipse.xtext.formatting2.AbstractFormatter2
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
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys
import org.eclipse.xtext.formatting2.IFormattableSubDocument
import org.eclipse.xtext.formatting2.regionaccess.internal.TextSegment
import com.regnosys.rosetta.rosetta.BlueprintNodeExp
import com.regnosys.rosetta.rosetta.BlueprintNode
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintOr
import com.regnosys.rosetta.rosetta.BlueprintRef
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintReturn
import com.regnosys.rosetta.rosetta.BlueprintLookup

class RosettaExpressionFormatter extends AbstractFormatter2 {
	
	@Inject extension RosettaGrammarAccess
	
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
	
	
	def dispatch void formatExpression(ListLiteral expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keywords(',').forEach[
			prepend[noSpace]
		]
		interior(
			expr.regionFor.keyword('['),
			expr.regionFor.keyword(']'),
			[indent]
		)
		
		expr.formatConditionally(
			[doc | // case: short list
				val extension singleLineDoc = doc.requireFitsInLine
				expr.regionFor.keyword('[')
					.append[noSpace]
				expr.regionFor.keyword(']')
					.prepend[noSpace]
				expr.regionFor.keywords(',').forEach[
					append[oneSpace]
				]
				expr.elements.forEach[formatExpression(singleLineDoc, FormattingMode.NORMAL)]
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
	
	def dispatch void formatExpression(RosettaConditionalExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension conditionalGrammarAccess = rosettaCalcConditionalExpressionAccess
		
		expr.regionFor.keywords(ifKeyword_1, thenKeyword_3, fullElseKeyword_5_0_0).forEach[
			append[oneSpace]
		]
		
		if (mode == FormattingMode.NORMAL) {
			expr.formatConditionally(
				[doc | // case: short conditional
					val extension singleLineDoc = doc.requireFitsInLine
					expr.regionFor.keyword(thenKeyword_3)
						.prepend[oneSpace]
					expr.regionFor.keyword(fullElseKeyword_5_0_0)
						.prepend[oneSpace]
					expr.^if.formatExpression(singleLineDoc, FormattingMode.NORMAL)
					expr.ifthen.formatExpression(singleLineDoc, FormattingMode.NORMAL)
					expr.elsethen.formatExpression(singleLineDoc, FormattingMode.NORMAL)
				],
				[extension doc | // case: long conditional
					expr.formatConditionalMultiLine(doc)
				]
			);
		} else if (mode == FormattingMode.MULTI_LINE) {
			expr.formatConditionalMultiLine(document)
		}
	}
	
	private def void formatConditionalMultiLine(RosettaConditionalExpression expr, extension IFormattableDocument document) {
		val extension conditionalGrammarAccess = rosettaCalcConditionalExpressionAccess
		
		expr.regionFor.keyword(thenKeyword_3)
			.prepend[newLine]
		expr.regionFor.keyword(fullElseKeyword_5_0_0)
			.prepend[newLine]
		expr.^if.formatExpression(document, FormattingMode.NORMAL)
		expr.ifthen.formatExpression(document, FormattingMode.NORMAL)
		expr.elsethen.formatExpression(document, FormattingMode.MULTI_LINE)
	}
	
	def dispatch void formatExpression(RosettaFeatureCall expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.regionFor.keyword('->').surround[oneSpace]
		expr.receiver.formatExpression(document, FormattingMode.NORMAL)
	}
	
	def dispatch void formatExpression(RosettaLiteral expr, extension IFormattableDocument document, FormattingMode mode) {

	}
	
	def dispatch void formatExpression(RosettaOnlyExistsExpression expr, extension IFormattableDocument document, FormattingMode mode) {
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
	
	def dispatch void formatExpression(RosettaImplicitVariable expr, extension IFormattableDocument document, FormattingMode mode) {
		
	}
	
	def dispatch void formatExpression(RosettaSymbolReference expr, extension IFormattableDocument document, FormattingMode mode) {
		if (expr.explicitArguments) {
			expr.regionFor.keywords(',').forEach[
				prepend[noSpace]
			]
			expr.regionFor.keyword('(')
				.prepend[noSpace]
			interior(
				expr.regionFor.keyword('('),
				expr.regionFor.keyword(')'),
				[indent]
			)
			
			expr.formatConditionally(
				[doc | // case: short argument list
					val extension singleLineDoc = doc.requireFitsInLine
					expr.regionFor.keyword('(')
						.append[noSpace]
					expr.regionFor.keyword(')')
						.prepend[noSpace]
					expr.regionFor.keywords(',').forEach[
						append[oneSpace]
					]
					expr.args.forEach[formatExpression(singleLineDoc, FormattingMode.NORMAL)]
				],
				[extension doc | // case: long argument list
					expr.regionFor.keyword('(')
						.append[newLine]
					expr.regionFor.keyword(')')
						.prepend[newLine]
					expr.regionFor.keywords(',').forEach[
						append[newLine]
					]
					expr.args.forEach[formatExpression(doc, FormattingMode.NORMAL)]
				]
			);
		}
	}
	
	def dispatch void formatExpression(ModifiableBinaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaBinaryOperation
		expr.formatBinaryExpression(document, FormattingMode.NORMAL)
		expr.regionFor.feature(MODIFIABLE_BINARY_OPERATION__CARD_MOD).surround[oneSpace]
	}
	
	def dispatch void formatExpression(RosettaBinaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.formatBinaryExpression(document, FormattingMode.NORMAL)
	}
	
	private def void formatBinaryExpression(RosettaBinaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		if (expr.left !== null) {
			expr.regionFor.feature(ROSETTA_OPERATION__OPERATOR).surround[oneSpace]
		
			expr.left.formatExpression(document, FormattingMode.NORMAL)
		} else {
			expr.regionFor.feature(ROSETTA_OPERATION__OPERATOR).append[oneSpace]
		}
		expr.right.formatExpression(document, FormattingMode.NORMAL)
	}
	
	def dispatch void formatExpression(RosettaFunctionalOperation expr, extension IFormattableDocument document, FormattingMode mode) {
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
				interior(
					ref.regionFor.keyword('['),
					ref.regionFor.keyword(']'),
					[indent]
				)
				ref.parameters.forEach[
					prepend[oneSpace]
				]
				ref.regionFor.keyword('[')
					.prepend[oneSpace]
				ref.regionFor.keywords(',').forEach[
					prepend[noSpace]
				]
				
				ref.formatConditionally(
					[doc | // case: short inline function
						val extension singleLineDoc = doc.requireFitsInLine
						ref.regionFor.keyword('[')
							.append[oneSpace]
						ref.regionFor.keyword(']')
							.prepend[oneSpace]
						ref.body.formatExpression(singleLineDoc)
					],
					[extension doc | // case: long inline function
						ref.regionFor.keyword('[')
							.append[newLine]
						ref.regionFor.keyword(']')
							.prepend[newLine]
						ref.body.formatExpression(doc)
					]
				)
			}
		}
	}

	def dispatch void formatExpression(RosettaExistsExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		expr.formatUnaryOperation(document, mode, [])
		expr.regionFor.feature(ROSETTA_EXISTS_EXPRESSION__MODIFIER)
			.append[oneSpace]
	}
	
	def dispatch void formatExpression(RosettaAbsentExpression expr, extension IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation		
		expr.formatUnaryOperation(document, mode, [])
		expr.regionFor.keyword('is')
			.append[oneSpace]
	}
	
	def dispatch void formatExpression(RosettaUnaryOperation expr, extension IFormattableDocument document, FormattingMode mode) {
		expr.formatUnaryOperation(document, mode, [])
	}
	
	private def void formatUnaryOperation(RosettaUnaryOperation expr, extension IFormattableDocument document, FormattingMode mode, (IFormattableDocument) => void internalFormatter) {
		if (mode == FormattingMode.NORMAL) {
			val opRegion = expr.regionForEObject
			 // I need to include the next hidden region in the conditional formatting as well,
			 // because that's where I decrease indentation in case of a (long) multi-line operation.
			val region = opRegion.merge(expr.nextHiddenRegion)
			formatConditionally(region.offset, region.getLength(),
				[doc | // case: short operation
					val extension singleLineDoc = doc.requireTrimmedFitsInLine(region.offset, region.length, doc.request.preferences.getPreference(FormatterPreferenceKeys.maxLineWidth))
					if (expr.argument !== null) {
						expr.argument.nextHiddenRegion
							.set[oneSpace]
						expr.argument.formatExpression(singleLineDoc, FormattingMode.NORMAL)
					}
					internalFormatter.apply(singleLineDoc)
				],
				[extension doc | // case: long operation
					formatUnaryOperationMultiLine(expr, doc, internalFormatter)
				]
			)
		} else if (mode == FormattingMode.MULTI_LINE) {
			formatUnaryOperationMultiLine(expr, document, internalFormatter)
		}
	}
	
	private def void formatUnaryOperationMultiLine(RosettaUnaryOperation expr, extension IFormattableDocument document, (IFormattableDocument) => void internalFormatter) {
		if (expr.argument !== null) {
			val afterArgument = expr.argument.nextHiddenRegion
			afterArgument
				.set[newLine]
			set(
				afterArgument,
				expr.nextHiddenRegion,
				[indent]
			)
			expr.argument.formatExpression(document, FormattingMode.MULTI_LINE)
		}
		internalFormatter.apply(document)
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
			if (mode == FormattingMode.NORMAL) {
				val opRegion = expr.regionForEObject
				 // I need to include the next hidden region in the conditional formatting as well,
				 // because that's where I decrease indentation in case of a (long) multi-line operation.
				val region = opRegion.merge(expr.nextHiddenRegion)
				formatConditionally(region.offset, region.getLength(),
					[doc | // case: short operation
						val extension singleLineDoc = doc.requireTrimmedFitsInLine(region.offset, region.length, doc.request.preferences.getPreference(FormatterPreferenceKeys.maxLineWidth))
						expr.regionFor.keyword(whenKeyword_1)
							.append[oneSpace]
						expr.filter.formatExpression(singleLineDoc, FormattingMode.NORMAL)
						expr.formatAsInline(singleLineDoc)
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
			} else if (mode == FormattingMode.MULTI_LINE) {
				expr.regionFor.keyword(whenKeyword_1)
					.append[newLine]
				expr.filter
					.surround[indent]
					.formatExpression(document, FormattingMode.NORMAL)
				expr.formatAsMultiline(document)
			}
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
		if (mode == FormattingMode.NORMAL) {
			val opRegion = expr.regionForEObject
			 // I need to include the next hidden region in the conditional formatting as well,
			 // because that's where I decrease indentation in case of a (long) multi-line operation.
			val region = opRegion.merge(expr.nextHiddenRegion)
			formatConditionally(region.offset, region.getLength(),
				[doc | // case: short operation
					val extension singleLineDoc = doc.requireTrimmedFitsInLine(region.offset, region.length, doc.request.preferences.getPreference(FormatterPreferenceKeys.maxLineWidth))
					lastKeyword
						.append[oneSpace]
					expr.call.formatExpression(singleLineDoc, FormattingMode.NORMAL)
					expr.formatAsInline(singleLineDoc)
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
		} else if (mode == FormattingMode.MULTI_LINE) {
			lastKeyword
				.append[newLine]
			expr.call
				.surround[indent]
				.formatExpression(document, FormattingMode.NORMAL)
			expr.formatAsMultiline(document)
		}
	}
	
	private def dispatch void formatRuleNode(BlueprintReturn expr, extension IFormattableDocument document, FormattingMode mode) {
		val extension returnGrammarAccess = blueprintReturnAccess
		
		if (mode == FormattingMode.NORMAL) {
			val opRegion = expr.regionForEObject
			 // I need to include the next hidden region in the conditional formatting as well,
			 // because that's where I decrease indentation in case of a (long) multi-line operation.
			val region = opRegion.merge(expr.nextHiddenRegion)
			formatConditionally(region.offset, region.getLength(),
				[doc | // case: short operation
					val extension singleLineDoc = doc.requireTrimmedFitsInLine(region.offset, region.length, doc.request.preferences.getPreference(FormatterPreferenceKeys.maxLineWidth))
					expr.regionFor.keyword(returnKeyword_0)
						.append[oneSpace]
					expr.expression.formatExpression(singleLineDoc, FormattingMode.NORMAL)
					expr.formatAsInline(singleLineDoc)
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
		} else if (mode == FormattingMode.MULTI_LINE) {
			expr.regionFor.keyword(returnKeyword_0)
				.append[newLine]
			expr.expression
				.surround[indent]
				.formatExpression(document, FormattingMode.NORMAL)
			expr.formatAsMultiline(document)
		}
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
	
	private def IFormattableSubDocument requireTrimmedFitsInLine(IFormattableDocument doc, int offset, int length, int maxLineWidth) {
		val segment = new TextSegment(textRegionAccess, offset, length);
		val document = new TrimmedMaxLineWidthDocument(segment, doc, maxLineWidth);
		doc.addReplacer(document);
		return document;
	}
}