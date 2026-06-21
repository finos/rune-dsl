/*
 * For info on the formatter API, see https://www.slideshare.net/meysholdt/xtexts-new-formatter-api.
 */
package com.regnosys.rosetta.formatting2;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter;
import org.eclipse.xtext.formatting2.regionaccess.IEObjectRegion;
import org.eclipse.xtext.formatting2.regionaccess.IHiddenRegion;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;
import org.eclipse.xtext.formatting2.regionaccess.ITextSegment;

import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaCallableReference;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.rosetta.expression.SwitchCaseOrDefault;
import com.regnosys.rosetta.rosetta.expression.SwitchOperation;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.rosetta.expression.WithMetaEntry;
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

import jakarta.inject.Inject;

public class RosettaExpressionFormatter extends AbstractRosettaFormatter2 {
	@Inject
	private RosettaGrammarAccess grammarAccess;
	@Inject
	private FormattingUtil formattingUtil;

	private boolean isSimple(RosettaExpression expr) {
		if (expr instanceof RosettaLiteral) {
			return true;
		} else if (expr instanceof RosettaImplicitVariable) {
			return true;
		} else if (expr instanceof RosettaSymbolReference) {
			return !((RosettaSymbolReference) expr).isExplicitArguments();
		} else if (expr instanceof RosettaFeatureCall) {
			return true;
		} else if (expr instanceof ArithmeticOperation) {
			return true;
		} else if (expr instanceof ComparisonOperation) {
			return true;
		}
		return false;
	}

	private boolean shouldBeOnSingleLine(RosettaExpression expr) {
		if (isSimple(expr)) {
			return true;
		}
		if (expr instanceof WithMetaOperation) {
			return true;
		} else if (expr instanceof RosettaBinaryOperation) {
			RosettaBinaryOperation op = (RosettaBinaryOperation) expr;
			return isSimple(op.getLeft()) || isSimple(op.getRight());
		} else if (expr instanceof RosettaFunctionalOperation) {
			RosettaFunctionalOperation op = (RosettaFunctionalOperation) expr;
			return op.getFunction() == null && isSimple(op.getArgument());
		} else if (expr instanceof RosettaUnaryOperation) {
			return isSimple(((RosettaUnaryOperation) expr).getArgument());
		} else if (expr instanceof ListLiteral) {
			return ((ListLiteral) expr).getElements().stream().allMatch(this::isSimple);
		}
		return false;
	}

	private boolean isEmpty(RosettaExpression expr) {
		return expr == null || expr.isGenerated();
	}

	@Override
	public void initialize(FormatterRequest request) {
		super.initialize(request);
	}

	@Override
	public void format(Object obj, IFormattableDocument document) {
		if (obj instanceof RosettaExpression) {
			formatExpression((RosettaExpression) obj, document);
		} else {
			throw new UnsupportedOperationException(
					RosettaExpressionFormatter.class.getSimpleName() + " does not support formatting " + obj + ".");
		}
	}

	public void formatExpression(RosettaExpression expr, IFormattableDocument document) {
		formatExpression(expr, document, FormattingMode.NORMAL);
	}

	public void formatExpression(RosettaExpression expr, IFormattableDocument document, FormattingMode mode) {
		if (!expr.isGenerated()) {
			ISemanticRegion leftParenthesis = regionFor(expr)
					.keyword(grammarAccess.getPrimaryExpressionAccess().getLeftParenthesisKeyword_7_0());
			ISemanticRegion rightParenthesis = regionFor(expr)
					.keyword(grammarAccess.getPrimaryExpressionAccess().getRightParenthesisKeyword_7_2());
			if (leftParenthesis != null && rightParenthesis != null) {
				document.append(leftParenthesis, IHiddenRegionFormatter::noSpace);
				document.prepend(rightParenthesis, IHiddenRegionFormatter::noSpace);
				if (!isMultiline(expr)) {
					unsafeFormatExpression(expr, document, FormattingMode.SINGLE_LINE);
				} else {
					unsafeFormatExpression(expr, document, mode.stopChain());
				}
			} else {
				unsafeFormatExpression(expr, document, mode);
			}
		}
	}

	// Dispatch on the dynamic type of the expression, ordered from most specific to least specific
	// to mirror Xtend's dispatch resolution.
	private void unsafeFormatExpression(RosettaExpression expr, IFormattableDocument document, FormattingMode mode) {
		if (expr instanceof WithMetaOperation) {
			unsafeFormatWithMetaOperation((WithMetaOperation) expr, document, mode);
		} else if (expr instanceof RosettaConstructorExpression) {
			unsafeFormatConstructorExpression((RosettaConstructorExpression) expr, document, mode);
		} else if (expr instanceof ListLiteral) {
			unsafeFormatListLiteral((ListLiteral) expr, document, mode);
		} else if (expr instanceof RosettaConditionalExpression) {
			unsafeFormatConditionalExpression((RosettaConditionalExpression) expr, document, mode);
		} else if (expr instanceof RosettaFeatureCall) {
			unsafeFormatFeatureCall((RosettaFeatureCall) expr, document, mode);
		} else if (expr instanceof RosettaDeepFeatureCall) {
			unsafeFormatDeepFeatureCall((RosettaDeepFeatureCall) expr, document, mode);
		} else if (expr instanceof RosettaOnlyExistsExpression) {
			unsafeFormatOnlyExistsExpression((RosettaOnlyExistsExpression) expr, document, mode);
		} else if (expr instanceof RosettaCallableReference) {
			unsafeFormatCallableReference((RosettaCallableReference) expr, document, mode);
		} else if (expr instanceof ModifiableBinaryOperation) {
			unsafeFormatModifiableBinaryOperation((ModifiableBinaryOperation) expr, document, mode);
		} else if (expr instanceof SwitchOperation) {
			unsafeFormatSwitchOperation((SwitchOperation) expr, document, mode);
		} else if (expr instanceof RosettaExistsExpression) {
			unsafeFormatExistsExpression((RosettaExistsExpression) expr, document, mode);
		} else if (expr instanceof ChoiceOperation) {
			unsafeFormatChoiceOperation((ChoiceOperation) expr, document, mode);
		} else if (expr instanceof RosettaAbsentExpression) {
			unsafeFormatAbsentExpression((RosettaAbsentExpression) expr, document, mode);
		} else if (expr instanceof RosettaFunctionalOperation) {
			unsafeFormatFunctionalOperation((RosettaFunctionalOperation) expr, document, mode);
		} else if (expr instanceof RosettaBinaryOperation) {
			// ModifiableBinaryOperation already handled above
			unsafeFormatBinaryOperation((RosettaBinaryOperation) expr, document, mode);
		} else if (expr instanceof RosettaUnaryOperation) {
			// RosettaFunctionalOperation, RosettaExistsExpression, ChoiceOperation,
			// RosettaAbsentExpression and WithMetaOperation already handled above
			unsafeFormatUnaryOperation((RosettaUnaryOperation) expr, document, mode);
		} else if (expr instanceof RosettaLiteral) {
			unsafeFormatLiteral((RosettaLiteral) expr, document, mode);
		} else if (expr instanceof RosettaImplicitVariable) {
			unsafeFormatImplicitVariable((RosettaImplicitVariable) expr, document, mode);
		}
		// No-op for any other expression type, mirroring Xtend's behaviour for the
		// (non-existent) default case among the declared dispatch methods.
	}

	private void unsafeFormatWithMetaOperation(WithMetaOperation expr, IFormattableDocument document,
			FormattingMode mode) {
		RosettaGrammarAccess.UnaryOperationElements unaryOperationGrammarAccess = grammarAccess.getUnaryOperationAccess();

		formatUnaryOperation(expr, document, mode, doc -> {
		});

		List<Keyword> leftCurlyBracketKeywords = List.of(
				unaryOperationGrammarAccess.getLeftCurlyBracketKeyword_0_1_0_0_23_2_0(),
				unaryOperationGrammarAccess.getLeftCurlyBracketKeyword_1_0_0_21_2_0(),
				unaryOperationGrammarAccess.getLeftCurlyBracketKeyword_1_1_0_0_23_2_0());
		Keyword leftCurlyBracketKeyword = leftCurlyBracketKeywords.stream()
				.filter(kw -> regionFor(expr).keyword(kw) != null)
				.findFirst()
				.orElse(null);

		List<Keyword> rightCurlyBracketKeywords = List.of(
				unaryOperationGrammarAccess.getRightCurlyBracketKeyword_0_1_0_0_23_2_2(),
				unaryOperationGrammarAccess.getRightCurlyBracketKeyword_1_0_0_21_2_2(),
				unaryOperationGrammarAccess.getRightCurlyBracketKeyword_1_1_0_0_23_2_2());
		Keyword rightCurlyBracketKeyword = rightCurlyBracketKeywords.stream()
				.filter(kw -> regionFor(expr).keyword(kw) != null)
				.findFirst()
				.orElse(null);

		constructorLikeFormat(expr, document, mode, leftCurlyBracketKeyword, rightCurlyBracketKeyword);
	}

	private void unsafeFormatConstructorExpression(RosettaConstructorExpression expr, IFormattableDocument document,
			FormattingMode mode) {
		RosettaGrammarAccess.ConstructorExpressionElements constructorGrammarAccess = grammarAccess
				.getConstructorExpressionAccess();
		constructorLikeFormat(expr, document, mode, constructorGrammarAccess.getLeftCurlyBracketKeyword_2(),
				constructorGrammarAccess.getRightCurlyBracketKeyword_4());
	}

	private void constructorLikeFormat(RosettaExpression expr, IFormattableDocument document, FormattingMode mode,
			Keyword leftCurlyBracket, Keyword rightCurlyBracket) {
		interiorIndentWithoutCurlyBracket(
				document.append(
						document.prepend(regionFor(expr).keyword(leftCurlyBracket), IHiddenRegionFormatter::oneSpace),
						IHiddenRegionFormatter::newLine),
				regionFor(expr).keyword(rightCurlyBracket),
				document);

		ISemanticRegion rightCurlyBracketRegion = regionFor(expr).keyword(rightCurlyBracket);
		document.prepend(rightCurlyBracketRegion, f -> {
			if (comesAfter(rightCurlyBracketRegion, "}") // case '}}'
					|| (comesAfter(rightCurlyBracketRegion, ",")
							&& comesAfter(rightCurlyBracketRegion.getPreviousSemanticRegion(), "}")) // case '},}'
			) {
				f.noSpace();
			} else {
				f.newLine();
			}
		});

		for (ISemanticRegion valueExpr : regionFor(expr).keywords(",")) {
			document.prepend(valueExpr, IHiddenRegionFormatter::noSpace);
			if ("}".equals(valueExpr.getNextSemanticRegion().getText())) {
				document.append(valueExpr, IHiddenRegionFormatter::noSpace);
			} else {
				document.append(valueExpr, IHiddenRegionFormatter::newLine);
			}
		}

		if (expr instanceof RosettaConstructorExpression) {
			for (ConstructorKeyValuePair pair : ((RosettaConstructorExpression) expr).getValues()) {
				RosettaExpression value = pair.getValue();
				if (value instanceof RosettaConstructorExpression
						|| (value instanceof RosettaUnaryOperation
								&& ((RosettaUnaryOperation) value).getArgument() instanceof RosettaConstructorExpression)) {
					document.append(
							document.prepend(regionFor(pair).keyword(":"), IHiddenRegionFormatter::noSpace),
							IHiddenRegionFormatter::newLine);
				} else {
					document.append(
							document.prepend(regionFor(pair).keyword(":"), IHiddenRegionFormatter::noSpace),
							IHiddenRegionFormatter::oneSpace);
				}

				indentInnerWithoutCurlyBracket(pair, document);
				formatExpression(value, document, mode);
			}
		}

		if (expr instanceof WithMetaOperation) {
			for (WithMetaEntry entry : ((WithMetaOperation) expr).getEntries()) {
				document.append(
						document.prepend(regionFor(entry).keyword(":"), IHiddenRegionFormatter::noSpace),
						IHiddenRegionFormatter::oneSpace);
				indentInnerWithoutCurlyBracket(entry, document);
				formatExpression(entry.getValue(), document, mode);
			}
		}
	}

	private boolean comesAfter(ISemanticRegion region, String el) {
		if (region != null && region.getPreviousSemanticRegion() != null) {
			String prevRegionElement = region.getPreviousSemanticRegion().getText();
			return prevRegionElement.equals(el);
		}
		return false;
	}

	private boolean comesBefore(ISemanticRegion region, String el) {
		if (region != null && region.getNextSemanticRegion() != null) {
			String nextRegionElement = region.getNextSemanticRegion().getText();
			return nextRegionElement.equals(el);
		}
		return false;
	}

	private ISemanticRegion findInnermostClosingCurlyBracket(ISemanticRegion region) {
		if (comesAfter(region, "}")) { // case '}}'
			ISemanticRegion prevRegion = region.getPreviousSemanticRegion();
			return findInnermostClosingCurlyBracket(prevRegion);
		} else if (comesAfter(region, ",") && comesAfter(region.getPreviousSemanticRegion(), "}")) { // case '},}'
			ISemanticRegion prevRegion = region.getPreviousSemanticRegion().getPreviousSemanticRegion();
			return findInnermostClosingCurlyBracket(prevRegion);
		} else {
			return region;
		}
	}

	private boolean shouldBracketNotBeIndented(ISemanticRegion region) {
		return region.getText().equals("}")
				&& (comesAfter(region, "}") || comesBefore(region, "}"))
				|| ((comesAfter(region, ",") && comesAfter(region.getPreviousSemanticRegion(), "}"))
						|| (comesBefore(region, ",") && comesBefore(region.getNextSemanticRegion(), "}")));
	}

	private void indentInnerWithoutCurlyBracket(EObject expr, IFormattableDocument document) {
		IHiddenRegion ext = formattingUtil.getTextRegionExt(document).previousHiddenRegion(expr);
		indentInnerWithoutCurlyBracket(expr, ext.getNextHiddenRegion(), document);
	}

	private void indentInnerWithoutCurlyBracket(EObject expr, IHiddenRegion firstRegion, IFormattableDocument document) {
		if (expr == null || firstRegion == null) {
			return;
		}
		IHiddenRegion nextRegion = formattingUtil.getTextRegionExt(document).nextHiddenRegion(expr);
		ISemanticRegion end = nextRegion.getPreviousSemanticRegion();
		document.set(
				firstRegion,
				shouldBracketNotBeIndented(end)
						? findInnermostClosingCurlyBracket(end).getPreviousHiddenRegion()
						: end.getNextHiddenRegion(),
				IHiddenRegionFormatter::indent);
	}

	private void surroundIndentWithoutCurlyBracket(EObject expr, IFormattableDocument document) {
		if (expr == null) {
			return;
		}
		IEObjectRegion objectRegion = regionForEObject(expr);
		ISemanticRegion end = objectRegion.getNextHiddenRegion().getPreviousSemanticRegion();

		document.set(
				objectRegion.getPreviousHiddenRegion(),
				shouldBracketNotBeIndented(end)
						? findInnermostClosingCurlyBracket(end).getPreviousHiddenRegion()
						: end.getNextHiddenRegion(),
				IHiddenRegionFormatter::indent);
	}

	private void interiorIndentWithoutCurlyBracket(ISemanticRegion start, ISemanticRegion end,
			IFormattableDocument document) {
		if (start != null && end != null) {
			document.set(
					start.getNextHiddenRegion(),
					shouldBracketNotBeIndented(end)
							? findInnermostClosingCurlyBracket(end).getPreviousHiddenRegion()
							: end.getPreviousHiddenRegion(),
					IHiddenRegionFormatter::indent);
		}
	}

	private void unsafeFormatListLiteral(ListLiteral expr, IFormattableDocument document, FormattingMode mode) {
		for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
			document.prepend(comma, IHiddenRegionFormatter::noSpace);
		}
		interiorIndentWithoutCurlyBracket(
				regionFor(expr).keyword("["),
				regionFor(expr).keyword("]"),
				document);

		formattingUtil.formatInlineOrMultiline(document, expr, mode.singleLineIf(shouldBeOnSingleLine(expr)),
				doc -> { // case: short list
					doc.append(regionFor(expr).keyword("["), IHiddenRegionFormatter::noSpace);
					doc.prepend(regionFor(expr).keyword("]"), IHiddenRegionFormatter::noSpace);
					for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
						doc.append(comma, IHiddenRegionFormatter::oneSpace);
					}
					expr.getElements().forEach(e -> formatExpression(e, doc, mode));
				},
				doc -> { // case: long list
					doc.append(regionFor(expr).keyword("["), IHiddenRegionFormatter::newLine);
					RosettaExpression last = lastOrNull(expr.getElements());
					if (last != null) {
						doc.append(last, IHiddenRegionFormatter::newLine);
					}
					for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
						doc.append(comma, IHiddenRegionFormatter::newLine);
					}
					expr.getElements().forEach(e -> formatExpression(e, doc, mode.stopChain()));
				});
	}

	private void unsafeFormatConditionalExpression(RosettaConditionalExpression expr, IFormattableDocument document,
			FormattingMode mode) {
		RosettaGrammarAccess.RosettaCalcConditionalExpressionElements conditionalGrammarAccess = grammarAccess
				.getRosettaCalcConditionalExpressionAccess();

		// fix edge case where 'then' inside constructor value is not indented correctly
		if (expr.eContainer() instanceof ConstructorKeyValuePair) {
			document.surround(regionFor(expr).keyword(conditionalGrammarAccess.getThenKeyword_3()),
					IHiddenRegionFormatter::indent);
		}

		for (ISemanticRegion kw : regionFor(expr).keywords(conditionalGrammarAccess.getIfKeyword_1(),
				conditionalGrammarAccess.getThenKeyword_3(), conditionalGrammarAccess.getFullElseKeyword_5_0_0())) {
			document.append(kw, IHiddenRegionFormatter::oneSpace);
		}
		List<RosettaExpression> subExprs = List.of(expr.getIf(), expr.getIfthen(), expr.getElsethen());
		for (RosettaExpression it : List.of(expr.getIf(), expr.getIfthen())) {
			if (!(it instanceof RosettaUnaryOperation)) {
				surroundIndentWithoutCurlyBracket(it, document);
			}
		}
		formattingUtil.formatInlineOrMultiline(document, expr, mode.singleLineIf(shouldBeOnSingleLine(expr)),
				formattingUtil.getPreference(document, RosettaFormatterPreferenceKeys.conditionalMaxLineWidth),
				doc -> { // case: short conditional
					doc.prepend(regionFor(expr).keyword(conditionalGrammarAccess.getThenKeyword_3()),
							IHiddenRegionFormatter::oneSpace);
					doc.prepend(regionFor(expr).keyword(conditionalGrammarAccess.getFullElseKeyword_5_0_0()),
							IHiddenRegionFormatter::oneSpace);
					subExprs.forEach(e -> formatExpression(e, doc, mode));
				},
				doc -> { // case: long conditional
					doc.prepend(regionFor(expr).keyword(conditionalGrammarAccess.getThenKeyword_3()),
							IHiddenRegionFormatter::newLine);
					doc.prepend(regionFor(expr).keyword(conditionalGrammarAccess.getFullElseKeyword_5_0_0()),
							IHiddenRegionFormatter::newLine);
					if (expr.eContainingFeature() == ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT) {
						indentInnerWithoutCurlyBracket(expr, doc);
					}
					formatExpression(expr.getIf(), doc, mode.stopChain());
					formatExpression(expr.getIfthen(), doc, mode.stopChain());
					formatExpression(expr.getElsethen(), doc,
							mode.chainIf(expr.getElsethen() instanceof RosettaConditionalExpression));
				});
	}

	private void unsafeFormatFeatureCall(RosettaFeatureCall expr, IFormattableDocument document, FormattingMode mode) {
		document.surround(regionFor(expr).keyword("->"), IHiddenRegionFormatter::oneSpace);
		formatExpression(expr.getReceiver(), document, mode.stopChain());
	}

	private void unsafeFormatDeepFeatureCall(RosettaDeepFeatureCall expr, IFormattableDocument document,
			FormattingMode mode) {
		document.surround(regionFor(expr).keyword("->>"), IHiddenRegionFormatter::oneSpace);
		formatExpression(expr.getReceiver(), document, mode.stopChain());
	}

	private void unsafeFormatLiteral(RosettaLiteral expr, IFormattableDocument document, FormattingMode mode) {
	}

	private void unsafeFormatOnlyExistsExpression(RosettaOnlyExistsExpression expr, IFormattableDocument document,
			FormattingMode mode) {
		RosettaGrammarAccess.RosettaCalcOnlyExistsElements onlyExistsGrammarAccess = grammarAccess
				.getRosettaCalcOnlyExistsAccess();

		document.append(regionFor(expr).keyword("("), IHiddenRegionFormatter::noSpace);
		document.prepend(regionFor(expr).keyword(")"), IHiddenRegionFormatter::noSpace);
		for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
			document.prepend(comma, IHiddenRegionFormatter::noSpace);
			document.append(comma, IHiddenRegionFormatter::oneSpace);
		}
		document.prepend(regionFor(expr).keyword(onlyExistsGrammarAccess.getOnlyKeyword_2()),
				IHiddenRegionFormatter::oneSpace);
		document.prepend(regionFor(expr).keyword(onlyExistsGrammarAccess.getExistsKeyword_3()),
				IHiddenRegionFormatter::oneSpace);

		expr.getArgs().forEach(arg -> formatExpression(arg, document, mode.stopChain()));
	}

	private void unsafeFormatImplicitVariable(RosettaImplicitVariable expr, IFormattableDocument document,
			FormattingMode mode) {
	}

	private void unsafeFormatCallableReference(RosettaCallableReference expr, IFormattableDocument document,
			FormattingMode mode) {
		RosettaGrammarAccess.RosettaReferenceOrFunctionCallElements referenceCallGrammarAccess = grammarAccess
				.getRosettaReferenceOrFunctionCallAccess();

		if (expr.isExplicitArguments()) {
			for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
				document.prepend(comma, IHiddenRegionFormatter::noSpace);
			}
			document.prepend(
					regionFor(expr).keyword(referenceCallGrammarAccess.getExplicitArgumentsLeftParenthesisKeyword_0_1_0_0()),
					IHiddenRegionFormatter::noSpace);

			formattingUtil.formatInlineOrMultiline(document, expr, mode.singleLineIf(shouldBeOnSingleLine(expr)),
					doc -> { // case: short argument list
						doc.append(regionFor(expr).keyword("("), IHiddenRegionFormatter::noSpace);
						doc.prepend(regionFor(expr).keyword(")"), IHiddenRegionFormatter::noSpace);
						for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
							doc.append(comma, IHiddenRegionFormatter::oneSpace);
						}
						expr.getRawArgs().forEach(arg -> formatExpression(arg, doc, mode));
					},
					doc -> { // case: long argument list
						indentInnerWithoutCurlyBracket(expr, doc);
						interiorIndentWithoutCurlyBracket(
								doc.append(regionFor(expr).keyword("("), IHiddenRegionFormatter::newLine),
								doc.prepend(regionFor(expr).keyword(")"), IHiddenRegionFormatter::newLine),
								doc);
						for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
							doc.append(comma, IHiddenRegionFormatter::newLine);
						}
						expr.getRawArgs().forEach(arg -> formatExpression(arg, doc, mode.stopChain()));
					});
		}
	}

	private void unsafeFormatModifiableBinaryOperation(ModifiableBinaryOperation expr, IFormattableDocument document,
			FormattingMode mode) {
		// specialization of RosettaBinaryOperation
		formatBinaryOperation(expr, document, mode);
		document.append(
				regionFor(expr).feature(ExpressionPackage.Literals.MODIFIABLE_BINARY_OPERATION__CARD_MOD),
				IHiddenRegionFormatter::oneSpace);
	}

	private void unsafeFormatBinaryOperation(RosettaBinaryOperation expr, IFormattableDocument document,
			FormattingMode mode) {
		formatBinaryOperation(expr, document, mode);
	}

	private void formatBinaryOperation(RosettaBinaryOperation expr, IFormattableDocument document,
			FormattingMode mode) {
		document.append(regionFor(expr).feature(ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR),
				IHiddenRegionFormatter::oneSpace);

		formattingUtil.formatInlineOrMultiline(document, expr, mode.singleLineIf(shouldBeOnSingleLine(expr)),
				doc -> { // case: short operation
					if (!isEmpty(expr.getLeft())) {
						doc.set(getTextRegionExtNextHiddenRegion(expr.getLeft(), doc),
								IHiddenRegionFormatter::oneSpace);
						formatExpression(expr.getLeft(), doc, mode);
					}
					formatExpression(expr.getRight(), doc, mode);
				},
				doc -> { // case: long operation
					if (!isEmpty(expr.getLeft())) {
						IHiddenRegion afterArgument = getTextRegionExtNextHiddenRegion(expr.getLeft(), doc);
						indentInnerWithoutCurlyBracket(expr, afterArgument, doc);
						doc.set(afterArgument, IHiddenRegionFormatter::newLine);

						boolean leftIsSameOperation;
						if (expr.getLeft() instanceof RosettaBinaryOperation) {
							leftIsSameOperation = expr.getOperator()
									.equals(((RosettaBinaryOperation) expr.getLeft()).getOperator());
						} else {
							leftIsSameOperation = false;
						}
						if (expr.getLeft() instanceof RosettaBinaryOperation && !leftIsSameOperation) {
							indentInnerWithoutCurlyBracket(expr.getLeft(), doc);
						}

						formatExpression(expr.getLeft(), doc, mode.chainIf(leftIsSameOperation));
					}
					formatExpression(expr.getRight(), doc, mode.stopChain());
				});
	}

	private void unsafeFormatFunctionalOperation(RosettaFunctionalOperation expr, IFormattableDocument document,
			FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		formatUnaryOperation(expr, document, mode, doc -> {
			if (expr.getFunction() != null) {
				formatInlineFunction(expr.getFunction(), doc, mode.stopChain());
			}
		});
	}

	private void formatInlineFunction(InlineFunction f, IFormattableDocument document, FormattingMode mode) {
		RosettaFunctionalOperation op = (RosettaFunctionalOperation) f.eContainer();
		ISemanticRegion left = regionFor(f).keyword("[");
		if (left != null) { // case inline function with brackets
			ISemanticRegion right = regionFor(f).keyword("]");
			f.getParameters().forEach(p -> document.prepend(p, IHiddenRegionFormatter::oneSpace));
			document.prepend(left, IHiddenRegionFormatter::oneSpace);
			for (ISemanticRegion comma : regionFor(f).keywords(",")) {
				document.prepend(comma, IHiddenRegionFormatter::noSpace);
			}

			formattingUtil.formatInlineOrMultiline(document, f, mode,
					doc -> { // case: short inline function
						doc.append(left, IHiddenRegionFormatter::oneSpace);
						doc.prepend(right, IHiddenRegionFormatter::oneSpace);
						formatExpression(f.getBody(), doc, mode);
						if (op.eContainer() instanceof RosettaOperation) {
							// Always put next operations on a new line.
							doc.append(f, fmt -> {
								fmt.highPriority();
								fmt.newLine();
							});
						}
					},
					doc -> { // case: long inline function
						interiorIndentWithoutCurlyBracket(
								doc.append(left, IHiddenRegionFormatter::newLine),
								doc.prepend(right, IHiddenRegionFormatter::newLine),
								doc);
						formatExpression(f.getBody(), doc, mode.stopChain());
					});
		} else { // case inline function without brackets
			IEObjectRegion astRegion = regionForEObject(f);
			ITextSegment formattableRegion = astRegion.merge(astRegion.getPreviousHiddenRegion())
					.merge(astRegion.getNextHiddenRegion());
			if (!(op instanceof ThenOperation && f.getBody() instanceof RosettaUnaryOperation)) {
				surroundIndentWithoutCurlyBracket(f.getBody(), document);
			}
			formattingUtil.formatInlineOrMultiline(document, astRegion, formattableRegion,
					mode.singleLineIf(op instanceof ThenOperation),
					doc -> { // case: short inline function
						formatExpression(doc.prepend(f.getBody(), IHiddenRegionFormatter::oneSpace), doc, mode);
						if (f.eContainer().eContainer() instanceof RosettaOperation) {
							// Always put next operations on a new line.
							doc.append(f, fmt -> {
								fmt.highPriority();
								fmt.newLine();
							});
						}
					},
					doc -> { // case: long inline function
						formatExpression(doc.prepend(f.getBody(), IHiddenRegionFormatter::newLine), doc, mode);
					});
		}
	}

	private void unsafeFormatSwitchOperation(SwitchOperation expr, IFormattableDocument document, FormattingMode mode) {
		RosettaGrammarAccess.SwitchCaseOrDefaultElements switchCaseGrammarAccess = grammarAccess
				.getSwitchCaseOrDefaultAccess();

		formattingUtil.indentInner(expr, document);
		for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
			document.prepend(comma, IHiddenRegionFormatter::noSpace);
		}
		formatUnaryOperation(expr, document, mode, doc -> {
			for (SwitchCaseOrDefault switchCase : expr.getCases()) {
				formattingUtil.indentInner(switchCase, doc);
				doc.prepend(switchCase, IHiddenRegionFormatter::newLine);
				doc.prepend(regionFor(switchCase).keyword(switchCaseGrammarAccess.getThenKeyword_1_1()),
						IHiddenRegionFormatter::oneSpace);

				ISemanticRegion thenOrDefault = regionFor(switchCase)
						.keyword(switchCaseGrammarAccess.getThenKeyword_1_1());
				if (thenOrDefault == null) {
					thenOrDefault = regionFor(switchCase).keyword(switchCaseGrammarAccess.getDefaultKeyword_0_0());
				}
				ISemanticRegion thenOrDefaultRegion = thenOrDefault;

				RosettaExpression caseExpression = switchCase.getExpression();
				formattingUtil.formatInlineOrMultiline(document, switchCase,
						mode.singleLineIf(shouldBeOnSingleLine(caseExpression)),
						formattingUtil.getPreference(doc, RosettaFormatterPreferenceKeys.conditionalMaxLineWidth),
						nestedDoc -> { // case: short conditional
							nestedDoc.append(thenOrDefaultRegion, IHiddenRegionFormatter::oneSpace);
							formatExpression(caseExpression, nestedDoc, mode);
						},
						nestedDoc -> { // case: long conditional
							nestedDoc.append(thenOrDefaultRegion, IHiddenRegionFormatter::newLine);
							formatExpression(caseExpression, nestedDoc, mode);
						});
			}
		});
	}

	private void unsafeFormatExistsExpression(RosettaExistsExpression expr, IFormattableDocument document,
			FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		formatUnaryOperation(expr, document, mode, doc -> {
		});
		document.append(regionFor(expr).feature(ExpressionPackage.Literals.ROSETTA_EXISTS_EXPRESSION__MODIFIER),
				IHiddenRegionFormatter::oneSpace);
	}

	private void unsafeFormatChoiceOperation(ChoiceOperation expr, IFormattableDocument document, FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		formatUnaryOperation(expr, document, mode, doc -> {
		});
		document.surround(regionFor(expr).feature(ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR),
				IHiddenRegionFormatter::oneSpace);
		for (ISemanticRegion comma : regionFor(expr).keywords(",")) {
			document.prepend(comma, IHiddenRegionFormatter::noSpace);
			document.append(comma, IHiddenRegionFormatter::oneSpace);
		}
	}

	private void unsafeFormatAbsentExpression(RosettaAbsentExpression expr, IFormattableDocument document,
			FormattingMode mode) {
		// specialization of RosettaUnaryOperation
		formatUnaryOperation(expr, document, mode, doc -> {
		});
		document.append(regionFor(expr).keyword("is"), IHiddenRegionFormatter::oneSpace);
	}

	private void unsafeFormatUnaryOperation(RosettaUnaryOperation expr, IFormattableDocument document,
			FormattingMode mode) {
		formatUnaryOperation(expr, document, mode, doc -> {
		});
	}

	private void formatUnaryOperation(RosettaUnaryOperation expr, IFormattableDocument document, FormattingMode mode,
			Consumer<IFormattableDocument> internalFormatter) {
		formattingUtil.formatInlineOrMultiline(document, expr, mode.singleLineIf(shouldBeOnSingleLine(expr)),
				doc -> { // case: short operation
					if (!isEmpty(expr.getArgument())) {
						IHiddenRegion afterArgument = getTextRegionExtNextHiddenRegion(expr.getArgument(), doc);
						doc.set(afterArgument, IHiddenRegionFormatter::oneSpace);
						formatExpression(expr.getArgument(), doc, mode);
					}
					internalFormatter.accept(doc);
				},
				doc -> { // case: long operation
					if (!isEmpty(expr.getArgument())) {
						IHiddenRegion afterArgument = getTextRegionExtNextHiddenRegion(expr.getArgument(), doc);
						RosettaExpression initialArgument = expr.getArgument();
						while (initialArgument instanceof RosettaUnaryOperation) {
							initialArgument = ((RosettaUnaryOperation) initialArgument).getArgument();
						}
						if (!isEmpty(initialArgument)) {
							indentInnerWithoutCurlyBracket(expr, afterArgument, doc);
						}
						doc.set(afterArgument, IHiddenRegionFormatter::newLine);
						formatExpression(expr.getArgument(), doc,
								mode.chainIf(expr.getArgument() instanceof RosettaUnaryOperation));
					}
					internalFormatter.accept(doc);
				});
	}

	// --- region navigation helpers ---

	private IHiddenRegion getTextRegionExtNextHiddenRegion(EObject obj, IFormattableDocument document) {
		return formattingUtil.getTextRegionExt(document).nextHiddenRegion(obj);
	}

	private static <T> T lastOrNull(List<T> list) {
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}
}
