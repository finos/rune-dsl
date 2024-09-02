package com.regnosys.rosetta.serializer;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Parameter;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService.ValueTransient;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.DocumentRationale;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaAttributeReference;
import com.regnosys.rosetta.rosetta.RosettaBasicType;
import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaClassSynonym;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaDataReference;
import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalClassSynonym;
import com.regnosys.rosetta.rosetta.RosettaExternalEnum;
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaMapPath;
import com.regnosys.rosetta.rosetta.RosettaMapPathValue;
import com.regnosys.rosetta.rosetta.RosettaMapRosettaPath;
import com.regnosys.rosetta.rosetta.RosettaMapTestAbsentExpression;
import com.regnosys.rosetta.rosetta.RosettaMapTestEqualityOperation;
import com.regnosys.rosetta.rosetta.RosettaMapTestExistsExpression;
import com.regnosys.rosetta.rosetta.RosettaMapTestFunc;
import com.regnosys.rosetta.rosetta.RosettaMapping;
import com.regnosys.rosetta.rosetta.RosettaMappingInstance;
import com.regnosys.rosetta.rosetta.RosettaMappingPathTests;
import com.regnosys.rosetta.rosetta.RosettaMergeSynonymValue;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaParameter;
import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration;
import com.regnosys.rosetta.rosetta.RosettaRecordFeature;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSegment;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.RosettaSynonym;
import com.regnosys.rosetta.rosetta.RosettaSynonymBody;
import com.regnosys.rosetta.rosetta.RosettaSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.TypeCallArgument;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.AsReferenceOperation;
import com.regnosys.rosetta.rosetta.expression.CaseStatement;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ClosureParameter;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.DefaultOperation;
import com.regnosys.rosetta.rosetta.expression.DistinctOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.expression.FilterOperation;
import com.regnosys.rosetta.rosetta.expression.FirstOperation;
import com.regnosys.rosetta.rosetta.expression.FlattenOperation;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.LastOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.MaxOperation;
import com.regnosys.rosetta.rosetta.expression.MinOperation;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.ReverseOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.SortOperation;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.SwitchOperation;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToIntOperation;
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation;
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationQualifier;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ReferenceKeyAnnotation;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.rosetta.translate.TranslatePackage;
import com.regnosys.rosetta.rosetta.translate.TranslateSource;
import com.regnosys.rosetta.rosetta.translate.Translation;
import com.regnosys.rosetta.rosetta.translate.TranslationParameter;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

public class RosettaSemanticSequencer extends AbstractDelegatingSemanticSequencer {
	@Inject
	private RosettaGrammarAccess grammarAccess;
	
	@Override
	public void sequence(ISerializationContext context, EObject semanticObject) {
		EPackage epackage = semanticObject.eClass().getEPackage();
		ParserRule rule = context.getParserRule();
		Action action = context.getAssignedAction();
		Set<Parameter> parameters = context.getEnabledBooleanParameters();
		if (epackage == ExpressionPackage.eINSTANCE)
			switch (semanticObject.eClass().getClassifierID()) {
			case ExpressionPackage.ARITHMETIC_OPERATION:
				if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcAdditive_RosettaCalcMultiplicative(context, (ArithmeticOperation) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_1_1_0_0_0()) {
					sequence_RosettaCalcMultiplicative_ArithmeticOperation_1_1_0_0_0(context, (ArithmeticOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.AS_KEY_OPERATION:
				sequence_RosettaCalcExpressionWithAsKey(context, (AsKeyOperation) semanticObject); 
				return; 
			case ExpressionPackage.AS_REFERENCE_OPERATION:
				if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (AsReferenceOperation) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (AsReferenceOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.CASE_STATEMENT:
				sequence_CaseStatement(context, (CaseStatement) semanticObject); 
				return; 
			case ExpressionPackage.CHOICE_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ChoiceOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ChoiceOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.CLOSURE_PARAMETER:
				sequence_ClosureParameter(context, (ClosureParameter) semanticObject); 
				return; 
			case ExpressionPackage.COMPARISON_OPERATION:
				if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcComparison(context, (ComparisonOperation) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_1_1_0_0_0()) {
					sequence_RosettaCalcComparison_ComparisonOperation_1_1_0_0_0(context, (ComparisonOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.CONSTRUCTOR_KEY_VALUE_PAIR:
				sequence_ConstructorKeyValuePair(context, (ConstructorKeyValuePair) semanticObject); 
				return; 
			case ExpressionPackage.DEFAULT_OPERATION:
				if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcBinary(context, (DefaultOperation) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_1_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_1_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_1_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_1_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_1_1_0_4_0()) {
					sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(context, (DefaultOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.DISTINCT_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (DistinctOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (DistinctOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.EQUALITY_OPERATION:
				if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcEquality(context, (EqualityOperation) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_1_1_0_0_0()) {
					sequence_RosettaCalcEquality_EqualityOperation_1_1_0_0_0(context, (EqualityOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.FILTER_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (FilterOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (FilterOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.FIRST_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (FirstOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (FirstOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.FLATTEN_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (FlattenOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (FlattenOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.INLINE_FUNCTION:
				if (rule == grammarAccess.getImplicitInlineFunctionRule()) {
					sequence_ImplicitInlineFunction(context, (InlineFunction) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getInlineFunctionRule()) {
					sequence_InlineFunction(context, (InlineFunction) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.JOIN_OPERATION:
				if (action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_1_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_1_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_1_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_1_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_1_1_0_4_0()) {
					sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(context, (JoinOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcBinary(context, (JoinOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.LAST_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (LastOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (LastOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.LIST_LITERAL:
				if (rule == grammarAccess.getEmptyLiteralRule()) {
					sequence_EmptyLiteral(context, (ListLiteral) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getListLiteralRule()
						|| rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_EmptyLiteral_ListLiteral(context, (ListLiteral) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.LOGICAL_OPERATION:
				if (action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_1_1_0_0_0()) {
					sequence_RosettaCalcAnd_LogicalOperation_1_1_0_0_0(context, (LogicalOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcAnd_RosettaCalcOr(context, (LogicalOperation) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_1_1_0_0_0()) {
					sequence_RosettaCalcOr_LogicalOperation_1_1_0_0_0(context, (LogicalOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.MAP_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (MapOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (MapOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.MAX_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (MaxOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (MaxOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.MIN_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (MinOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (MinOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ONE_OF_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (OneOfOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (OneOfOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.REDUCE_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ReduceOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ReduceOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.REVERSE_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ReverseOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ReverseOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_ABSENT_EXPRESSION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (RosettaAbsentExpression) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (RosettaAbsentExpression) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_BOOLEAN_LITERAL:
				sequence_RosettaBooleanLiteral(context, (RosettaBooleanLiteral) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_CONDITIONAL_EXPRESSION:
				sequence_RosettaCalcConditionalExpression(context, (RosettaConditionalExpression) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_CONSTRUCTOR_EXPRESSION:
				sequence_RosettaCalcConstructorExpression(context, (RosettaConstructorExpression) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_CONTAINS_EXPRESSION:
				if (action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_1_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_1_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_1_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_1_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_1_1_0_4_0()) {
					sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(context, (RosettaContainsExpression) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcBinary(context, (RosettaContainsExpression) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_COUNT_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (RosettaCountOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (RosettaCountOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_DEEP_FEATURE_CALL:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (RosettaDeepFeatureCall) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (RosettaDeepFeatureCall) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_DISJOINT_EXPRESSION:
				if (action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_1_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_1_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_1_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_1_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_1_1_0_4_0()) {
					sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(context, (RosettaDisjointExpression) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcBinary(context, (RosettaDisjointExpression) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_EXISTS_EXPRESSION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (RosettaExistsExpression) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (RosettaExistsExpression) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_FEATURE_CALL:
				if (rule == grammarAccess.getRosettaOnlyExistsElementRule()
						|| action == grammarAccess.getRosettaOnlyExistsElementAccess().getRosettaFeatureCallReceiverAction_1_0()) {
					sequence_RosettaOnlyExistsElement(context, (RosettaFeatureCall) semanticObject); 
					return; 
				}
				else if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (RosettaFeatureCall) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (RosettaFeatureCall) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_IMPLICIT_VARIABLE:
				if (rule == grammarAccess.getRosettaOnlyExistsElementRule()
						|| action == grammarAccess.getRosettaOnlyExistsElementAccess().getRosettaFeatureCallReceiverAction_1_0()
						|| rule == grammarAccess.getRosettaOnlyExistsElementRootRule()) {
					sequence_RosettaOnlyExistsElementRoot(context, (RosettaImplicitVariable) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaReferenceOrFunctionCallRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaReferenceOrFunctionCall(context, (RosettaImplicitVariable) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_INT_LITERAL:
				sequence_RosettaIntLiteral(context, (RosettaIntLiteral) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_NUMBER_LITERAL:
				sequence_RosettaNumberLiteral(context, (RosettaNumberLiteral) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_ONLY_ELEMENT:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (RosettaOnlyElement) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (RosettaOnlyElement) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.ROSETTA_ONLY_EXISTS_EXPRESSION:
				sequence_RosettaCalcOnlyExists(context, (RosettaOnlyExistsExpression) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_STRING_LITERAL:
				sequence_RosettaStringLiteral(context, (RosettaStringLiteral) semanticObject); 
				return; 
			case ExpressionPackage.ROSETTA_SYMBOL_REFERENCE:
				if (rule == grammarAccess.getRosettaOnlyExistsElementRule()
						|| action == grammarAccess.getRosettaOnlyExistsElementAccess().getRosettaFeatureCallReceiverAction_1_0()
						|| rule == grammarAccess.getRosettaOnlyExistsElementRootRule()) {
					sequence_RosettaOnlyExistsElementRoot(context, (RosettaSymbolReference) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaReferenceOrFunctionCallRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaReferenceOrFunctionCall(context, (RosettaSymbolReference) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getTypeCallArgumentExpressionRule()
						|| rule == grammarAccess.getTypeParameterReferenceRule()) {
					sequence_TypeParameterReference(context, (RosettaSymbolReference) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.SORT_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (SortOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (SortOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.SUM_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (SumOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (SumOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.SWITCH_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (SwitchOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (SwitchOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.THEN_OPERATION:
				sequence_ThenOperation(context, (ThenOperation) semanticObject); 
				return; 
			case ExpressionPackage.TO_DATE_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToDateOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToDateOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_DATE_TIME_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToDateTimeOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToDateTimeOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_ENUM_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToEnumOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToEnumOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_INT_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToIntOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToIntOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_NUMBER_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToNumberOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToNumberOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_STRING_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToStringOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToStringOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_TIME_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToTimeOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToTimeOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TO_ZONED_DATE_TIME_OPERATION:
				if (action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_1_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_1_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_1_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_1_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_1_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_1_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_1_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_1_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_1_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_1_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_1_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_1_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_1_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_1_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_1_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_1_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_1_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_1_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_1_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_1_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_1_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_1_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_1_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_1_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_1_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_1_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_1_1_2_0_0_2_0()) {
					sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(context, (ToZonedDateTimeOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_UnaryOperation(context, (ToZonedDateTimeOperation) semanticObject); 
					return; 
				}
				else break;
			case ExpressionPackage.TRANSLATE_DISPATCH_OPERATION:
				sequence_RosettaCalcTranslateDispatchOperation(context, (TranslateDispatchOperation) semanticObject); 
				return; 
			case ExpressionPackage.WITH_META_OPERATION:
				if (action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_1_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_1_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_1_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_1_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_1_1_0_4_0()) {
					sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(context, (WithMetaOperation) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getCaseDefaultStatementRule()
						|| rule == grammarAccess.getRosettaCalcExpressionWithAsKeyRule()
						|| action == grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0()
						|| rule == grammarAccess.getRosettaCalcExpressionRule()
						|| rule == grammarAccess.getThenOperationRule()
						|| action == grammarAccess.getThenOperationAccess().getThenOperationArgumentAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcOrRule()
						|| action == grammarAccess.getRosettaCalcOrAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAndRule()
						|| action == grammarAccess.getRosettaCalcAndAccess().getLogicalOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcEqualityRule()
						|| action == grammarAccess.getRosettaCalcEqualityAccess().getEqualityOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcComparisonRule()
						|| action == grammarAccess.getRosettaCalcComparisonAccess().getComparisonOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcAdditiveRule()
						|| action == grammarAccess.getRosettaCalcAdditiveAccess().getArithmeticOperationLeftAction_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcMultiplicativeRule()
						|| action == grammarAccess.getRosettaCalcMultiplicativeAccess().getArithmeticOperationLeftAction_0_1_0_0_0()
						|| rule == grammarAccess.getRosettaCalcBinaryRule()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaContainsExpressionLeftAction_0_1_0_0_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getRosettaDisjointExpressionLeftAction_0_1_0_1_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getDefaultOperationLeftAction_0_1_0_2_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getJoinOperationLeftAction_0_1_0_3_0()
						|| action == grammarAccess.getRosettaCalcBinaryAccess().getWithMetaOperationLeftAction_0_1_0_4_0()
						|| rule == grammarAccess.getUnaryOperationRule()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaFeatureCallReceiverAction_0_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaDeepFeatureCallReceiverAction_0_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaExistsExpressionArgumentAction_0_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaAbsentExpressionArgumentAction_0_1_0_0_3_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaOnlyElementArgumentAction_0_1_0_0_4_0()
						|| action == grammarAccess.getUnaryOperationAccess().getRosettaCountOperationArgumentAction_0_1_0_0_5_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFlattenOperationArgumentAction_0_1_0_0_6_0()
						|| action == grammarAccess.getUnaryOperationAccess().getDistinctOperationArgumentAction_0_1_0_0_7_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReverseOperationArgumentAction_0_1_0_0_8_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFirstOperationArgumentAction_0_1_0_0_9_0()
						|| action == grammarAccess.getUnaryOperationAccess().getLastOperationArgumentAction_0_1_0_0_10_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSumOperationArgumentAction_0_1_0_0_11_0()
						|| action == grammarAccess.getUnaryOperationAccess().getOneOfOperationArgumentAction_0_1_0_0_12_0()
						|| action == grammarAccess.getUnaryOperationAccess().getChoiceOperationArgumentAction_0_1_0_0_13_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToStringOperationArgumentAction_0_1_0_0_14_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToNumberOperationArgumentAction_0_1_0_0_15_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToIntOperationArgumentAction_0_1_0_0_16_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToTimeOperationArgumentAction_0_1_0_0_17_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToEnumOperationArgumentAction_0_1_0_0_18_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateOperationArgumentAction_0_1_0_0_19_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToDateTimeOperationArgumentAction_0_1_0_0_20_0()
						|| action == grammarAccess.getUnaryOperationAccess().getToZonedDateTimeOperationArgumentAction_0_1_0_0_21_0()
						|| action == grammarAccess.getUnaryOperationAccess().getAsReferenceOperationArgumentAction_0_1_0_0_22_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSwitchOperationArgumentAction_0_1_0_0_23_0()
						|| action == grammarAccess.getUnaryOperationAccess().getSortOperationArgumentAction_0_1_1_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMinOperationArgumentAction_0_1_1_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMaxOperationArgumentAction_0_1_1_0_0_2_0()
						|| action == grammarAccess.getUnaryOperationAccess().getReduceOperationArgumentAction_0_1_2_0_0_0_0()
						|| action == grammarAccess.getUnaryOperationAccess().getFilterOperationArgumentAction_0_1_2_0_0_1_0()
						|| action == grammarAccess.getUnaryOperationAccess().getMapOperationArgumentAction_0_1_2_0_0_2_0()
						|| rule == grammarAccess.getRosettaCalcPrimaryRule()) {
					sequence_RosettaCalcBinary(context, (WithMetaOperation) semanticObject); 
					return; 
				}
				else break;
			}
		else if (epackage == RosettaPackage.eINSTANCE)
			switch (semanticObject.eClass().getClassifierID()) {
			case RosettaPackage.DOCUMENT_RATIONALE:
				sequence_DocumentRationale(context, (DocumentRationale) semanticObject); 
				return; 
			case RosettaPackage.IMPORT:
				sequence_Import(context, (Import) semanticObject); 
				return; 
			case RosettaPackage.REGULATORY_DOCUMENT_REFERENCE:
				sequence_RegulatoryDocumentReference(context, (RegulatoryDocumentReference) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_ATTRIBUTE_REFERENCE:
				sequence_RosettaAttributeReference(context, (RosettaAttributeReference) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_BASIC_TYPE:
				if (rule == grammarAccess.getRosettaRootElementRule()
						|| rule == grammarAccess.getRosettaBasicTypeRule()) {
					sequence_RosettaDefinable_RosettaNamed_TypeParameters(context, (RosettaBasicType) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getRosettaQualifiedTypeRule()
						|| rule == grammarAccess.getRosettaCalculationTypeRule()) {
					sequence_RosettaNamed(context, (RosettaBasicType) semanticObject); 
					return; 
				}
				else break;
			case RosettaPackage.ROSETTA_BODY:
				sequence_RosettaBody_RosettaDefinable_RosettaNamed(context, (RosettaBody) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_CARDINALITY:
				sequence_RosettaCardinality(context, (RosettaCardinality) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_CLASS_SYNONYM:
				sequence_RosettaClassSynonym(context, (RosettaClassSynonym) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_CORPUS:
				sequence_RosettaCorpus_RosettaDefinable_RosettaNamed(context, (RosettaCorpus) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_DATA_REFERENCE:
				sequence_RosettaDataReference(context, (RosettaDataReference) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_DOC_REFERENCE:
				sequence_RosettaDocReference(context, (RosettaDocReference) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_ENUM_SYNONYM:
				if (rule == grammarAccess.getRosettaEnumSynonymRule()) {
					sequence_RosettaEnumSynonym(context, (RosettaEnumSynonym) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getRosettaExternalEnumSynonymRule()) {
					sequence_RosettaExternalEnumSynonym(context, (RosettaEnumSynonym) semanticObject); 
					return; 
				}
				else break;
			case RosettaPackage.ROSETTA_ENUM_VALUE:
				sequence_References_RosettaDefinable_RosettaEnumValue_RosettaNamed(context, (RosettaEnumValue) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_ENUM_VALUE_REFERENCE:
				sequence_EnumValueReference(context, (RosettaEnumValueReference) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_ENUMERATION:
				sequence_Enumeration_References_RosettaDefinable_RosettaNamed(context, (RosettaEnumeration) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_CLASS:
				sequence_RosettaExternalClass(context, (RosettaExternalClass) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_CLASS_SYNONYM:
				sequence_RosettaExternalClassSynonym(context, (RosettaExternalClassSynonym) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_ENUM:
				sequence_RosettaExternalEnum(context, (RosettaExternalEnum) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_ENUM_VALUE:
				sequence_RosettaExternalEnumValue(context, (RosettaExternalEnumValue) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_FUNCTION:
				sequence_RosettaDefinable_RosettaLibraryFunction_RosettaNamed_RosettaTyped(context, (RosettaExternalFunction) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_REGULAR_ATTRIBUTE:
				sequence_RosettaExternalRegularAttribute(context, (RosettaExternalRegularAttribute) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_RULE_SOURCE:
				sequence_ExternalAnnotationSource_RosettaExternalRuleSource_RosettaNamed(context, (RosettaExternalRuleSource) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_SYNONYM:
				sequence_RosettaExternalSynonym(context, (RosettaExternalSynonym) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_EXTERNAL_SYNONYM_SOURCE:
				sequence_ExternalAnnotationSource_RosettaExternalSynonymSource_RosettaNamed(context, (RosettaExternalSynonymSource) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_PATH:
				sequence_RosettaMapPath(context, (RosettaMapPath) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_PATH_VALUE:
				sequence_RosettaMapPathValue(context, (RosettaMapPathValue) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_ROSETTA_PATH:
				sequence_RosettaMapRosettaPath(context, (RosettaMapRosettaPath) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_TEST_ABSENT_EXPRESSION:
				sequence_RosettaMapTestExpression(context, (RosettaMapTestAbsentExpression) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_TEST_EQUALITY_OPERATION:
				sequence_RosettaMapTestExpression(context, (RosettaMapTestEqualityOperation) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_TEST_EXISTS_EXPRESSION:
				sequence_RosettaMapTestExpression(context, (RosettaMapTestExistsExpression) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAP_TEST_FUNC:
				sequence_RosettaMapTestFunc(context, (RosettaMapTestFunc) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MAPPING:
				if (rule == grammarAccess.getRosettaMappingSetToRule()) {
					sequence_RosettaMappingSetTo(context, (RosettaMapping) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getRosettaMappingRule()) {
					sequence_RosettaMapping(context, (RosettaMapping) semanticObject); 
					return; 
				}
				else break;
			case RosettaPackage.ROSETTA_MAPPING_INSTANCE:
				if (rule == grammarAccess.getRosettaMappingInstanceRule()) {
					sequence_RosettaMappingInstance(context, (RosettaMappingInstance) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getRosettaMappingSetToInstanceRule()) {
					sequence_RosettaMappingSetToInstance(context, (RosettaMappingInstance) semanticObject); 
					return; 
				}
				else break;
			case RosettaPackage.ROSETTA_MAPPING_PATH_TESTS:
				sequence_RosettaMappingPathTests(context, (RosettaMappingPathTests) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MERGE_SYNONYM_VALUE:
				sequence_RosettaMergeSynonymValue(context, (RosettaMergeSynonymValue) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_META_TYPE:
				sequence_RosettaNamed_RosettaTyped(context, (RosettaMetaType) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_MODEL:
				sequence_RosettaDefinable_RosettaModel(context, (RosettaModel) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_PARAMETER:
				sequence_RosettaNamed_RosettaParameter_RosettaTyped(context, (RosettaParameter) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_QUALIFIABLE_CONFIGURATION:
				sequence_RosettaQualifiableConfiguration(context, (RosettaQualifiableConfiguration) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_RECORD_FEATURE:
				sequence_RosettaNamed_RosettaTyped(context, (RosettaRecordFeature) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_RECORD_TYPE:
				sequence_RosettaDefinable_RosettaNamed_RosettaRecordType(context, (RosettaRecordType) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_REPORT:
				sequence_RosettaReport(context, (RosettaReport) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_RULE:
				sequence_References_RosettaDefinable_RosettaNamed_RosettaRule(context, (RosettaRule) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_SEGMENT:
				sequence_RosettaSegment(context, (RosettaSegment) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_SEGMENT_REF:
				sequence_RosettaSegmentRef(context, (RosettaSegmentRef) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_SYNONYM:
				sequence_RosettaSynonym(context, (RosettaSynonym) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_SYNONYM_BODY:
				sequence_RosettaSynonymBody(context, (RosettaSynonymBody) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_SYNONYM_SOURCE:
				sequence_RosettaNamed(context, (RosettaSynonymSource) semanticObject); 
				return; 
			case RosettaPackage.ROSETTA_SYNONYM_VALUE_BASE:
				if (rule == grammarAccess.getRosettaClassSynonymValueRule()) {
					sequence_RosettaClassSynonymValue(context, (RosettaSynonymValueBase) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getRosettaMetaSynonymValueRule()) {
					sequence_RosettaMetaSynonymValue(context, (RosettaSynonymValueBase) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getRosettaSynonymValueRule()) {
					sequence_RosettaSynonymValue(context, (RosettaSynonymValueBase) semanticObject); 
					return; 
				}
				else break;
			case RosettaPackage.ROSETTA_TYPE_ALIAS:
				sequence_RosettaDefinable_RosettaNamed_RosettaTyped_TypeParameters(context, (RosettaTypeAlias) semanticObject); 
				return; 
			case RosettaPackage.TYPE_CALL:
				sequence_TypeCall(context, (TypeCall) semanticObject); 
				return; 
			case RosettaPackage.TYPE_CALL_ARGUMENT:
				sequence_TypeCallArgument(context, (TypeCallArgument) semanticObject); 
				return; 
			case RosettaPackage.TYPE_PARAMETER:
				sequence_RosettaDefinable_RosettaTyped_TypeParameter(context, (TypeParameter) semanticObject); 
				return; 
			}
		else if (epackage == SimplePackage.eINSTANCE)
			switch (semanticObject.eClass().getClassifierID()) {
			case SimplePackage.ANNOTATION:
				sequence_Annotation_RosettaDefinable_RosettaNamed(context, (Annotation) semanticObject); 
				return; 
			case SimplePackage.ANNOTATION_QUALIFIER:
				sequence_AnnotationQualifier(context, (AnnotationQualifier) semanticObject); 
				return; 
			case SimplePackage.ANNOTATION_REF:
				sequence_AnnotationRef(context, (AnnotationRef) semanticObject); 
				return; 
			case SimplePackage.ATTRIBUTE:
				sequence_Annotations_Attribute_References_RosettaDefinable_RosettaNamed_RosettaTyped_RuleReference_Synonyms(context, (Attribute) semanticObject); 
				return; 
			case SimplePackage.CHOICE:
				sequence_Annotations_Choice_ClassSynonyms_RosettaDefinable_RosettaNamed(context, (Choice) semanticObject); 
				return; 
			case SimplePackage.CHOICE_OPTION:
				sequence_Annotations_References_RosettaDefinable_RosettaTyped_RuleReference_Synonyms(context, (ChoiceOption) semanticObject); 
				return; 
			case SimplePackage.CONDITION:
				if (rule == grammarAccess.getConditionRule()) {
					sequence_Annotations_Condition_References_RosettaDefinable_RosettaNamed(context, (Condition) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getPostConditionRule()) {
					sequence_PostCondition_RosettaDefinable_RosettaNamed(context, (Condition) semanticObject); 
					return; 
				}
				else break;
			case SimplePackage.DATA:
				sequence_Annotations_ClassSynonyms_Data_References_RosettaDefinable_RosettaNamed(context, (Data) semanticObject); 
				return; 
			case SimplePackage.FUNCTION:
				sequence_Annotations_Function_References_RosettaDefinable_RosettaNamed(context, (Function) semanticObject); 
				return; 
			case SimplePackage.FUNCTION_DISPATCH:
				sequence_Annotations_Function_References_RosettaDefinable_RosettaNamed(context, (FunctionDispatch) semanticObject); 
				return; 
			case SimplePackage.OPERATION:
				sequence_Operation_RosettaDefinable(context, (Operation) semanticObject); 
				return; 
			case SimplePackage.REFERENCE_KEY_ANNOTATION:
				sequence_ReferenceKeyAnnotation(context, (ReferenceKeyAnnotation) semanticObject); 
				return; 
			case SimplePackage.ROSETTA_RULE_REFERENCE:
				sequence_RosettaRuleReference(context, (RosettaRuleReference) semanticObject); 
				return; 
			case SimplePackage.SEGMENT:
				sequence_Segment(context, (Segment) semanticObject); 
				return; 
			case SimplePackage.SHORTCUT_DECLARATION:
				sequence_RosettaDefinable_RosettaNamed_ShortcutDeclaration(context, (ShortcutDeclaration) semanticObject); 
				return; 
			}
		else if (epackage == TranslatePackage.eINSTANCE)
			switch (semanticObject.eClass().getClassifierID()) {
			case TranslatePackage.TRANSLATE_SOURCE:
				sequence_RosettaNamed_TranslateSource(context, (TranslateSource) semanticObject); 
				return; 
			case TranslatePackage.TRANSLATION:
				sequence_Translation(context, (Translation) semanticObject); 
				return; 
			case TranslatePackage.TRANSLATION_PARAMETER:
				sequence_RosettaNamed_RosettaTyped(context, (TranslationParameter) semanticObject); 
				return; 
			}
		if (errorAcceptor != null)
			errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * <pre>
	 * Contexts:
	 *     AnnotationQualifier returns AnnotationQualifier
	 *
	 * Constraint:
	 *     (qualName=STRING (qualValue=STRING | qualPath=RosettaAttributeReference))
	 * </pre>
	 */
	protected void sequence_AnnotationQualifier(ISerializationContext context, AnnotationQualifier semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     AnnotationRef returns AnnotationRef
	 *
	 * Constraint:
	 *     (annotation=[Annotation|ValidID] (attribute=[Attribute|ValidID] qualifiers+=AnnotationQualifier*)?)
	 * </pre>
	 */
	protected void sequence_AnnotationRef(ISerializationContext context, AnnotationRef semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Annotation returns Annotation
	 *     RosettaRootElement returns Annotation
	 *
	 * Constraint:
	 *     (name=ValidID definition=STRING? prefix=ValidID? attributes+=Attribute*)
	 * </pre>
	 */
	protected void sequence_Annotation_RosettaDefinable_RosettaNamed(ISerializationContext context, Annotation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Attribute returns Attribute
	 *
	 * Constraint:
	 *     (
	 *         override?='override'? 
	 *         name=ValidID 
	 *         typeCall=TypeCall 
	 *         card=RosettaCardinality 
	 *         definition=STRING? 
	 *         (annotations+=AnnotationRef | synonyms+=RosettaSynonym | references+=RosettaDocReference)* 
	 *         ruleReference=RosettaRuleReference?
	 *     )
	 * </pre>
	 */
	protected void sequence_Annotations_Attribute_References_RosettaDefinable_RosettaNamed_RosettaTyped_RuleReference_Synonyms(ISerializationContext context, Attribute semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Choice returns Choice
	 *     RosettaRootElement returns Choice
	 *
	 * Constraint:
	 *     (name=ValidID definition=STRING? (annotations+=AnnotationRef | synonyms+=RosettaClassSynonym)* attributes+=ChoiceOption*)
	 * </pre>
	 */
	protected void sequence_Annotations_Choice_ClassSynonyms_RosettaDefinable_RosettaNamed(ISerializationContext context, Choice semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Data returns Data
	 *     RosettaRootElement returns Data
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         superType=TypeCall? 
	 *         definition=STRING? 
	 *         (annotations+=AnnotationRef | synonyms+=RosettaClassSynonym | references+=RosettaDocReference | referenceKeyAnnotation=ReferenceKeyAnnotation)* 
	 *         attributes+=Attribute* 
	 *         conditions+=Condition*
	 *     )
	 * </pre>
	 */
	protected void sequence_Annotations_ClassSynonyms_Data_References_RosettaDefinable_RosettaNamed(ISerializationContext context, Data semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Condition returns Condition
	 *
	 * Constraint:
	 *     (name=ValidID? definition=STRING? (annotations+=AnnotationRef | references+=RosettaDocReference)* expression=RosettaCalcExpression)
	 * </pre>
	 */
	protected void sequence_Annotations_Condition_References_RosettaDefinable_RosettaNamed(ISerializationContext context, Condition semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Function returns Function
	 *     RosettaRootElement returns Function
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         definition=STRING? 
	 *         (annotations+=AnnotationRef | references+=RosettaDocReference)* 
	 *         inputs+=Attribute* 
	 *         output=Attribute? 
	 *         shortcuts+=ShortcutDeclaration* 
	 *         conditions+=Condition* 
	 *         operations+=Operation* 
	 *         postConditions+=PostCondition*
	 *     )
	 * </pre>
	 */
	protected void sequence_Annotations_Function_References_RosettaDefinable_RosettaNamed(ISerializationContext context, Function semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Function returns FunctionDispatch
	 *     RosettaRootElement returns FunctionDispatch
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         attribute=[Attribute|ValidID] 
	 *         value=EnumValueReference 
	 *         definition=STRING? 
	 *         (annotations+=AnnotationRef | references+=RosettaDocReference)* 
	 *         inputs+=Attribute* 
	 *         output=Attribute? 
	 *         shortcuts+=ShortcutDeclaration* 
	 *         conditions+=Condition* 
	 *         operations+=Operation* 
	 *         postConditions+=PostCondition*
	 *     )
	 * </pre>
	 */
	protected void sequence_Annotations_Function_References_RosettaDefinable_RosettaNamed(ISerializationContext context, FunctionDispatch semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ChoiceOption returns ChoiceOption
	 *
	 * Constraint:
	 *     (
	 *         typeCall=TypeCall 
	 *         definition=STRING? 
	 *         (annotations+=AnnotationRef | synonyms+=RosettaSynonym | references+=RosettaDocReference)* 
	 *         ruleReference=RosettaRuleReference?
	 *     )
	 * </pre>
	 */
	protected void sequence_Annotations_References_RosettaDefinable_RosettaTyped_RuleReference_Synonyms(ISerializationContext context, ChoiceOption semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseStatement returns CaseStatement
	 *
	 * Constraint:
	 *     (condition=RosettaCalcOr expression=RosettaCalcExpression)
	 * </pre>
	 */
	protected void sequence_CaseStatement(ISerializationContext context, CaseStatement semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.CASE_STATEMENT__CONDITION) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.CASE_STATEMENT__CONDITION));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.CASE_STATEMENT__EXPRESSION) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.CASE_STATEMENT__EXPRESSION));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getCaseStatementAccess().getConditionRosettaCalcOrParserRuleCall_0_0(), semanticObject.getCondition());
		feeder.accept(grammarAccess.getCaseStatementAccess().getExpressionRosettaCalcExpressionParserRuleCall_2_0(), semanticObject.getExpression());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ClosureParameter returns ClosureParameter
	 *
	 * Constraint:
	 *     name=ID
	 * </pre>
	 */
	protected void sequence_ClosureParameter(ISerializationContext context, ClosureParameter semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getClosureParameterAccess().getNameIDTerminalRuleCall_0(), semanticObject.getName());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ConstructorKeyValuePair returns ConstructorKeyValuePair
	 *
	 * Constraint:
	 *     (key=[RosettaFeature|ValidID] value=RosettaCalcExpressionWithAsKey)
	 * </pre>
	 */
	protected void sequence_ConstructorKeyValuePair(ISerializationContext context, ConstructorKeyValuePair semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.CONSTRUCTOR_KEY_VALUE_PAIR__KEY) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.CONSTRUCTOR_KEY_VALUE_PAIR__KEY));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.CONSTRUCTOR_KEY_VALUE_PAIR__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.CONSTRUCTOR_KEY_VALUE_PAIR__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getConstructorKeyValuePairAccess().getKeyRosettaFeatureValidIDParserRuleCall_0_0_1(), semanticObject.eGet(ExpressionPackage.Literals.CONSTRUCTOR_KEY_VALUE_PAIR__KEY, false));
		feeder.accept(grammarAccess.getConstructorKeyValuePairAccess().getValueRosettaCalcExpressionWithAsKeyParserRuleCall_2_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     DocumentRationale returns DocumentRationale
	 *
	 * Constraint:
	 *     ((rationale=STRING rationaleAuthor=STRING?) | (rationaleAuthor=STRING rationale=STRING?))
	 * </pre>
	 */
	protected void sequence_DocumentRationale(ISerializationContext context, DocumentRationale semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     EmptyLiteral returns ListLiteral
	 *
	 * Constraint:
	 *     {ListLiteral}
	 * </pre>
	 */
	protected void sequence_EmptyLiteral(ISerializationContext context, ListLiteral semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ListLiteral returns ListLiteral
	 *     CaseDefaultStatement returns ListLiteral
	 *     RosettaCalcExpressionWithAsKey returns ListLiteral
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ListLiteral
	 *     RosettaCalcExpression returns ListLiteral
	 *     ThenOperation returns ListLiteral
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ListLiteral
	 *     RosettaCalcOr returns ListLiteral
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ListLiteral
	 *     RosettaCalcAnd returns ListLiteral
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ListLiteral
	 *     RosettaCalcEquality returns ListLiteral
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ListLiteral
	 *     RosettaCalcComparison returns ListLiteral
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ListLiteral
	 *     RosettaCalcAdditive returns ListLiteral
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ListLiteral
	 *     RosettaCalcMultiplicative returns ListLiteral
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ListLiteral
	 *     RosettaCalcBinary returns ListLiteral
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ListLiteral
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ListLiteral
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ListLiteral
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ListLiteral
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ListLiteral
	 *     UnaryOperation returns ListLiteral
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ListLiteral
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ListLiteral
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ListLiteral
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ListLiteral
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ListLiteral
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ListLiteral
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ListLiteral
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ListLiteral
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ListLiteral
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ListLiteral
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ListLiteral
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ListLiteral
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ListLiteral
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ListLiteral
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ListLiteral
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ListLiteral
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ListLiteral
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ListLiteral
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ListLiteral
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ListLiteral
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ListLiteral
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ListLiteral
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ListLiteral
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ListLiteral
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ListLiteral
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ListLiteral
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ListLiteral
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ListLiteral
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ListLiteral
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ListLiteral
	 *     RosettaCalcPrimary returns ListLiteral
	 *
	 * Constraint:
	 *     (elements+=RosettaCalcExpression elements+=RosettaCalcExpression*)?
	 * </pre>
	 */
	protected void sequence_EmptyLiteral_ListLiteral(ISerializationContext context, ListLiteral semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     EnumValueReference returns RosettaEnumValueReference
	 *     RosettaMapPrimaryExpression returns RosettaEnumValueReference
	 *
	 * Constraint:
	 *     (enumeration=[RosettaEnumeration|QualifiedName] value=[RosettaEnumValue|ValidID])
	 * </pre>
	 */
	protected void sequence_EnumValueReference(ISerializationContext context, RosettaEnumValueReference semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getEnumValueReferenceAccess().getEnumerationRosettaEnumerationQualifiedNameParserRuleCall_0_0_1(), semanticObject.eGet(RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION, false));
		feeder.accept(grammarAccess.getEnumValueReferenceAccess().getValueRosettaEnumValueValidIDParserRuleCall_2_0_1(), semanticObject.eGet(RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__VALUE, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Enumeration returns RosettaEnumeration
	 *     RosettaRootElement returns RosettaEnumeration
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         superType=[RosettaEnumeration|QualifiedName]? 
	 *         definition=STRING? 
	 *         references+=RosettaDocReference* 
	 *         synonyms+=RosettaSynonym* 
	 *         enumValues+=RosettaEnumValue*
	 *     )
	 * </pre>
	 */
	protected void sequence_Enumeration_References_RosettaDefinable_RosettaNamed(ISerializationContext context, RosettaEnumeration semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaExternalRuleSource
	 *     RosettaExternalRuleSource returns RosettaExternalRuleSource
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         (superSources+=[RosettaExternalRuleSource|QualifiedName] superSources+=[RosettaExternalRuleSource|QualifiedName]*)? 
	 *         externalClasses+=RosettaExternalClass* 
	 *         externalEnums+=RosettaExternalEnum*
	 *     )
	 * </pre>
	 */
	protected void sequence_ExternalAnnotationSource_RosettaExternalRuleSource_RosettaNamed(ISerializationContext context, RosettaExternalRuleSource semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaExternalSynonymSource
	 *     RosettaExternalSynonymSource returns RosettaExternalSynonymSource
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         (superSources+=[RosettaSynonymSource|QualifiedName] superSources+=[RosettaSynonymSource|QualifiedName]*)? 
	 *         externalClasses+=RosettaExternalClass* 
	 *         externalEnums+=RosettaExternalEnum*
	 *     )
	 * </pre>
	 */
	protected void sequence_ExternalAnnotationSource_RosettaExternalSynonymSource_RosettaNamed(ISerializationContext context, RosettaExternalSynonymSource semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ImplicitInlineFunction returns InlineFunction
	 *
	 * Constraint:
	 *     body=RosettaCalcOr
	 * </pre>
	 */
	protected void sequence_ImplicitInlineFunction(ISerializationContext context, InlineFunction semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.INLINE_FUNCTION__BODY) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.INLINE_FUNCTION__BODY));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getImplicitInlineFunctionAccess().getBodyRosettaCalcOrParserRuleCall_0(), semanticObject.getBody());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Import returns Import
	 *
	 * Constraint:
	 *     (importedNamespace=QualifiedNameWithWildcard namespaceAlias=QualifiedName?)
	 * </pre>
	 */
	protected void sequence_Import(ISerializationContext context, Import semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     InlineFunction returns InlineFunction
	 *
	 * Constraint:
	 *     ((parameters+=ClosureParameter parameters+=ClosureParameter*)? body=RosettaCalcExpression)
	 * </pre>
	 */
	protected void sequence_InlineFunction(ISerializationContext context, InlineFunction semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Operation returns Operation
	 *
	 * Constraint:
	 *     (add?='add'? assignRoot=[AssignPathRoot|ValidID] path=Segment? definition=STRING? expression=RosettaCalcExpressionWithAsKey)
	 * </pre>
	 */
	protected void sequence_Operation_RosettaDefinable(ISerializationContext context, Operation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     PostCondition returns Condition
	 *
	 * Constraint:
	 *     (postCondition?='post-condition' name=ValidID? definition=STRING? expression=RosettaCalcExpression)
	 * </pre>
	 */
	protected void sequence_PostCondition_RosettaDefinable_RosettaNamed(ISerializationContext context, Condition semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ReferenceKeyAnnotation returns ReferenceKeyAnnotation
	 *
	 * Constraint:
	 *     expression=RosettaCalcExpression
	 * </pre>
	 */
	protected void sequence_ReferenceKeyAnnotation(ISerializationContext context, ReferenceKeyAnnotation semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, SimplePackage.Literals.REFERENCE_KEY_ANNOTATION__EXPRESSION) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, SimplePackage.Literals.REFERENCE_KEY_ANNOTATION__EXPRESSION));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getReferenceKeyAnnotationAccess().getExpressionRosettaCalcExpressionParserRuleCall_2_0(), semanticObject.getExpression());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaEnumValue returns RosettaEnumValue
	 *
	 * Constraint:
	 *     (name=ValidID display=STRING? definition=STRING? references+=RosettaDocReference* enumSynonyms+=RosettaEnumSynonym*)
	 * </pre>
	 */
	protected void sequence_References_RosettaDefinable_RosettaEnumValue_RosettaNamed(ISerializationContext context, RosettaEnumValue semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaRule
	 *     RosettaRule returns RosettaRule
	 *
	 * Constraint:
	 *     (
	 *         eligibility?='eligibility'? 
	 *         name=ValidID 
	 *         input=TypeCall? 
	 *         definition=STRING? 
	 *         references+=RosettaDocReference* 
	 *         expression=RosettaCalcExpression 
	 *         identifier=STRING?
	 *     )
	 * </pre>
	 */
	protected void sequence_References_RosettaDefinable_RosettaNamed_RosettaRule(ISerializationContext context, RosettaRule semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RegulatoryDocumentReference returns RegulatoryDocumentReference
	 *
	 * Constraint:
	 *     (body=[RosettaBody|QualifiedName] corpusList+=[RosettaCorpus|QualifiedName]+ segments+=RosettaSegmentRef*)
	 * </pre>
	 */
	protected void sequence_RegulatoryDocumentReference(ISerializationContext context, RegulatoryDocumentReference semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaAttributeReference returns RosettaAttributeReference
	 *     RosettaAttributeReference.RosettaAttributeReference_1_0 returns RosettaAttributeReference
	 *
	 * Constraint:
	 *     (receiver=RosettaAttributeReference_RosettaAttributeReference_1_0 attribute=[Attribute|ValidID])
	 * </pre>
	 */
	protected void sequence_RosettaAttributeReference(ISerializationContext context, RosettaAttributeReference semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_ATTRIBUTE_REFERENCE__RECEIVER) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_ATTRIBUTE_REFERENCE__RECEIVER));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_ATTRIBUTE_REFERENCE__ATTRIBUTE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_ATTRIBUTE_REFERENCE__ATTRIBUTE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaAttributeReferenceAccess().getRosettaAttributeReferenceReceiverAction_1_0(), semanticObject.getReceiver());
		feeder.accept(grammarAccess.getRosettaAttributeReferenceAccess().getAttributeAttributeValidIDParserRuleCall_1_2_0_1(), semanticObject.eGet(RosettaPackage.Literals.ROSETTA_ATTRIBUTE_REFERENCE__ATTRIBUTE, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaBody
	 *     RosettaBody returns RosettaBody
	 *
	 * Constraint:
	 *     (bodyType=ID name=ValidID definition=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaBody_RosettaDefinable_RosettaNamed(ISerializationContext context, RosettaBody semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCallArgumentExpression returns RosettaBooleanLiteral
	 *     CaseDefaultStatement returns RosettaBooleanLiteral
	 *     RosettaMapPrimaryExpression returns RosettaBooleanLiteral
	 *     RosettaLiteral returns RosettaBooleanLiteral
	 *     RosettaBooleanLiteral returns RosettaBooleanLiteral
	 *     RosettaCalcExpressionWithAsKey returns RosettaBooleanLiteral
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcExpression returns RosettaBooleanLiteral
	 *     ThenOperation returns RosettaBooleanLiteral
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcOr returns RosettaBooleanLiteral
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcAnd returns RosettaBooleanLiteral
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcEquality returns RosettaBooleanLiteral
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcComparison returns RosettaBooleanLiteral
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcAdditive returns RosettaBooleanLiteral
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcMultiplicative returns RosettaBooleanLiteral
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcBinary returns RosettaBooleanLiteral
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaBooleanLiteral
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaBooleanLiteral
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaBooleanLiteral
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaBooleanLiteral
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaBooleanLiteral
	 *     UnaryOperation returns RosettaBooleanLiteral
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaBooleanLiteral
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaBooleanLiteral
	 *     RosettaCalcPrimary returns RosettaBooleanLiteral
	 *
	 * Constraint:
	 *     value?='True'?
	 * </pre>
	 */
	protected void sequence_RosettaBooleanLiteral(ISerializationContext context, RosettaBooleanLiteral semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ArithmeticOperation
	 *     RosettaCalcExpressionWithAsKey returns ArithmeticOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ArithmeticOperation
	 *     RosettaCalcExpression returns ArithmeticOperation
	 *     ThenOperation returns ArithmeticOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcOr returns ArithmeticOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcAnd returns ArithmeticOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcEquality returns ArithmeticOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcComparison returns ArithmeticOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcAdditive returns ArithmeticOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcMultiplicative returns ArithmeticOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcBinary returns ArithmeticOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ArithmeticOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ArithmeticOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ArithmeticOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ArithmeticOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ArithmeticOperation
	 *     UnaryOperation returns ArithmeticOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ArithmeticOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ArithmeticOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ArithmeticOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ArithmeticOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ArithmeticOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ArithmeticOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ArithmeticOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ArithmeticOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ArithmeticOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ArithmeticOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ArithmeticOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ArithmeticOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ArithmeticOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ArithmeticOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ArithmeticOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ArithmeticOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ArithmeticOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ArithmeticOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ArithmeticOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ArithmeticOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ArithmeticOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ArithmeticOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ArithmeticOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ArithmeticOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ArithmeticOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ArithmeticOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ArithmeticOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ArithmeticOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ArithmeticOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ArithmeticOperation
	 *     RosettaCalcPrimary returns ArithmeticOperation
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcAdditive_ArithmeticOperation_1_0_0_0 (operator='+' | operator='-') right=RosettaCalcMultiplicative) | 
	 *         (left=RosettaCalcMultiplicative_ArithmeticOperation_0_1_0_0_0 (operator='*' | operator='/') right=RosettaCalcBinary) | 
	 *         ((operator='*' | operator='/') right=RosettaCalcBinary) | 
	 *         (left=RosettaCalcMultiplicative_ArithmeticOperation_1_1_0_0_0 (operator='*' | operator='/') right=RosettaCalcBinary)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcAdditive_RosettaCalcMultiplicative(ISerializationContext context, ArithmeticOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcAnd.LogicalOperation_1_1_0_0_0 returns LogicalOperation
	 *
	 * Constraint:
	 *     ((operator='and' right=RosettaCalcEquality) | (left=RosettaCalcAnd_LogicalOperation_1_1_0_0_0 operator='and' right=RosettaCalcEquality))
	 * </pre>
	 */
	protected void sequence_RosettaCalcAnd_LogicalOperation_1_1_0_0_0(ISerializationContext context, LogicalOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns LogicalOperation
	 *     RosettaCalcExpressionWithAsKey returns LogicalOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns LogicalOperation
	 *     RosettaCalcExpression returns LogicalOperation
	 *     ThenOperation returns LogicalOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcOr returns LogicalOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcAnd returns LogicalOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcEquality returns LogicalOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcComparison returns LogicalOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcAdditive returns LogicalOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcMultiplicative returns LogicalOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcBinary returns LogicalOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns LogicalOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns LogicalOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns LogicalOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns LogicalOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns LogicalOperation
	 *     UnaryOperation returns LogicalOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns LogicalOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns LogicalOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns LogicalOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns LogicalOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns LogicalOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns LogicalOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns LogicalOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns LogicalOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns LogicalOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns LogicalOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns LogicalOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns LogicalOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns LogicalOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns LogicalOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns LogicalOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns LogicalOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns LogicalOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns LogicalOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns LogicalOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns LogicalOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns LogicalOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns LogicalOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns LogicalOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns LogicalOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns LogicalOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns LogicalOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns LogicalOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns LogicalOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns LogicalOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns LogicalOperation
	 *     RosettaCalcPrimary returns LogicalOperation
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcOr_LogicalOperation_0_1_0_0_0 operator='or' right=RosettaCalcAnd) | 
	 *         (operator='or' right=RosettaCalcAnd) | 
	 *         (left=RosettaCalcOr_LogicalOperation_1_1_0_0_0 operator='or' right=RosettaCalcAnd) | 
	 *         (left=RosettaCalcAnd_LogicalOperation_0_1_0_0_0 operator='and' right=RosettaCalcEquality) | 
	 *         (operator='and' right=RosettaCalcEquality) | 
	 *         (left=RosettaCalcAnd_LogicalOperation_1_1_0_0_0 operator='and' right=RosettaCalcEquality)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcAnd_RosettaCalcOr(ISerializationContext context, LogicalOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns DefaultOperation
	 *     RosettaCalcExpressionWithAsKey returns DefaultOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns DefaultOperation
	 *     RosettaCalcExpression returns DefaultOperation
	 *     ThenOperation returns DefaultOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcOr returns DefaultOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcAnd returns DefaultOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcEquality returns DefaultOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcComparison returns DefaultOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcAdditive returns DefaultOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcMultiplicative returns DefaultOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcBinary returns DefaultOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns DefaultOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns DefaultOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns DefaultOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns DefaultOperation
	 *     UnaryOperation returns DefaultOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns DefaultOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns DefaultOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns DefaultOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns DefaultOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns DefaultOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns DefaultOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns DefaultOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns DefaultOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns DefaultOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns DefaultOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns DefaultOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns DefaultOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns DefaultOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns DefaultOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns DefaultOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns DefaultOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns DefaultOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns DefaultOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns DefaultOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns DefaultOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns DefaultOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns DefaultOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns DefaultOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns DefaultOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns DefaultOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns DefaultOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns DefaultOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns DefaultOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns DefaultOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns DefaultOperation
	 *     RosettaCalcPrimary returns DefaultOperation
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcBinary_DefaultOperation_0_1_0_2_0 operator='default' right=UnaryOperation) | 
	 *         (operator='default' right=UnaryOperation) | 
	 *         (left=RosettaCalcBinary_DefaultOperation_1_1_0_2_0 operator='default' right=UnaryOperation)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary(ISerializationContext context, DefaultOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcBinary.RosettaContainsExpression_1_1_0_0_0 returns DefaultOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_1_1_0_1_0 returns DefaultOperation
	 *     RosettaCalcBinary.DefaultOperation_1_1_0_2_0 returns DefaultOperation
	 *     RosettaCalcBinary.JoinOperation_1_1_0_3_0 returns DefaultOperation
	 *     RosettaCalcBinary.WithMetaOperation_1_1_0_4_0 returns DefaultOperation
	 *
	 * Constraint:
	 *     (operator='default' right=UnaryOperation)
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(ISerializationContext context, DefaultOperation semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getOperatorDefaultKeyword_1_0_2_1_0(), semanticObject.getOperator());
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getRightUnaryOperationParserRuleCall_1_0_2_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcBinary.RosettaContainsExpression_1_1_0_0_0 returns JoinOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_1_1_0_1_0 returns JoinOperation
	 *     RosettaCalcBinary.DefaultOperation_1_1_0_2_0 returns JoinOperation
	 *     RosettaCalcBinary.JoinOperation_1_1_0_3_0 returns JoinOperation
	 *     RosettaCalcBinary.WithMetaOperation_1_1_0_4_0 returns JoinOperation
	 *
	 * Constraint:
	 *     (operator='join' right=UnaryOperation?)
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(ISerializationContext context, JoinOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcBinary.RosettaContainsExpression_1_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_1_1_0_1_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.DefaultOperation_1_1_0_2_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.JoinOperation_1_1_0_3_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.WithMetaOperation_1_1_0_4_0 returns RosettaContainsExpression
	 *
	 * Constraint:
	 *     (operator='contains' right=UnaryOperation)
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(ISerializationContext context, RosettaContainsExpression semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getOperatorContainsKeyword_1_0_0_1_0(), semanticObject.getOperator());
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getRightUnaryOperationParserRuleCall_1_0_0_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcBinary.RosettaContainsExpression_1_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_1_1_0_1_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.DefaultOperation_1_1_0_2_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.JoinOperation_1_1_0_3_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.WithMetaOperation_1_1_0_4_0 returns RosettaDisjointExpression
	 *
	 * Constraint:
	 *     (operator='disjoint' right=UnaryOperation)
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(ISerializationContext context, RosettaDisjointExpression semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getOperatorDisjointKeyword_1_0_1_1_0(), semanticObject.getOperator());
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getRightUnaryOperationParserRuleCall_1_0_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcBinary.RosettaContainsExpression_1_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_1_1_0_1_0 returns WithMetaOperation
	 *     RosettaCalcBinary.DefaultOperation_1_1_0_2_0 returns WithMetaOperation
	 *     RosettaCalcBinary.JoinOperation_1_1_0_3_0 returns WithMetaOperation
	 *     RosettaCalcBinary.WithMetaOperation_1_1_0_4_0 returns WithMetaOperation
	 *
	 * Constraint:
	 *     (operator='with-meta' metaFeature=[RosettaMetaType|ID] right=UnaryOperation)
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary_DefaultOperation_1_1_0_2_0_JoinOperation_1_1_0_3_0_RosettaContainsExpression_1_1_0_0_0_RosettaDisjointExpression_1_1_0_1_0_WithMetaOperation_1_1_0_4_0(ISerializationContext context, WithMetaOperation semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.WITH_META_OPERATION__META_FEATURE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.WITH_META_OPERATION__META_FEATURE));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_BINARY_OPERATION__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getOperatorWithMetaKeyword_1_0_4_1_0(), semanticObject.getOperator());
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getMetaFeatureRosettaMetaTypeIDTerminalRuleCall_1_0_4_2_0_1(), semanticObject.eGet(ExpressionPackage.Literals.WITH_META_OPERATION__META_FEATURE, false));
		feeder.accept(grammarAccess.getRosettaCalcBinaryAccess().getRightUnaryOperationParserRuleCall_1_0_4_4_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns JoinOperation
	 *     RosettaCalcExpressionWithAsKey returns JoinOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns JoinOperation
	 *     RosettaCalcExpression returns JoinOperation
	 *     ThenOperation returns JoinOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns JoinOperation
	 *     RosettaCalcOr returns JoinOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns JoinOperation
	 *     RosettaCalcAnd returns JoinOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns JoinOperation
	 *     RosettaCalcEquality returns JoinOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns JoinOperation
	 *     RosettaCalcComparison returns JoinOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns JoinOperation
	 *     RosettaCalcAdditive returns JoinOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns JoinOperation
	 *     RosettaCalcMultiplicative returns JoinOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns JoinOperation
	 *     RosettaCalcBinary returns JoinOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns JoinOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns JoinOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns JoinOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns JoinOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns JoinOperation
	 *     UnaryOperation returns JoinOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns JoinOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns JoinOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns JoinOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns JoinOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns JoinOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns JoinOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns JoinOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns JoinOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns JoinOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns JoinOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns JoinOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns JoinOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns JoinOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns JoinOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns JoinOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns JoinOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns JoinOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns JoinOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns JoinOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns JoinOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns JoinOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns JoinOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns JoinOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns JoinOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns JoinOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns JoinOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns JoinOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns JoinOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns JoinOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns JoinOperation
	 *     RosettaCalcPrimary returns JoinOperation
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcBinary_JoinOperation_0_1_0_3_0 operator='join' right=UnaryOperation?) | 
	 *         (operator='join' right=UnaryOperation?) | 
	 *         (left=RosettaCalcBinary_JoinOperation_1_1_0_3_0 operator='join' right=UnaryOperation?)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary(ISerializationContext context, JoinOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaContainsExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaContainsExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaContainsExpression
	 *     RosettaCalcExpression returns RosettaContainsExpression
	 *     ThenOperation returns RosettaContainsExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcOr returns RosettaContainsExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcAnd returns RosettaContainsExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcEquality returns RosettaContainsExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcComparison returns RosettaContainsExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcAdditive returns RosettaContainsExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcMultiplicative returns RosettaContainsExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary returns RosettaContainsExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaContainsExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaContainsExpression
	 *     UnaryOperation returns RosettaContainsExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaContainsExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaContainsExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaContainsExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaContainsExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaContainsExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaContainsExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaContainsExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaContainsExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaContainsExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaContainsExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaContainsExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaContainsExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaContainsExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaContainsExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaContainsExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaContainsExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaContainsExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaContainsExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaContainsExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaContainsExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaContainsExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaContainsExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaContainsExpression
	 *     RosettaCalcPrimary returns RosettaContainsExpression
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcBinary_RosettaContainsExpression_0_1_0_0_0 operator='contains' right=UnaryOperation) | 
	 *         (operator='contains' right=UnaryOperation) | 
	 *         (left=RosettaCalcBinary_RosettaContainsExpression_1_1_0_0_0 operator='contains' right=UnaryOperation)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary(ISerializationContext context, RosettaContainsExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaDisjointExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaDisjointExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcExpression returns RosettaDisjointExpression
	 *     ThenOperation returns RosettaDisjointExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcOr returns RosettaDisjointExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcAnd returns RosettaDisjointExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcEquality returns RosettaDisjointExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcComparison returns RosettaDisjointExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcAdditive returns RosettaDisjointExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcMultiplicative returns RosettaDisjointExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary returns RosettaDisjointExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaDisjointExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaDisjointExpression
	 *     UnaryOperation returns RosettaDisjointExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaDisjointExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaDisjointExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaDisjointExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaDisjointExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaDisjointExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaDisjointExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaDisjointExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaDisjointExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaDisjointExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaDisjointExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaDisjointExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaDisjointExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaDisjointExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaDisjointExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaDisjointExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaDisjointExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaDisjointExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaDisjointExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaDisjointExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaDisjointExpression
	 *     RosettaCalcPrimary returns RosettaDisjointExpression
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcBinary_RosettaDisjointExpression_0_1_0_1_0 operator='disjoint' right=UnaryOperation) | 
	 *         (operator='disjoint' right=UnaryOperation) | 
	 *         (left=RosettaCalcBinary_RosettaDisjointExpression_1_1_0_1_0 operator='disjoint' right=UnaryOperation)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary(ISerializationContext context, RosettaDisjointExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns WithMetaOperation
	 *     RosettaCalcExpressionWithAsKey returns WithMetaOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns WithMetaOperation
	 *     RosettaCalcExpression returns WithMetaOperation
	 *     ThenOperation returns WithMetaOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcOr returns WithMetaOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcAnd returns WithMetaOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcEquality returns WithMetaOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcComparison returns WithMetaOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcAdditive returns WithMetaOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcMultiplicative returns WithMetaOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcBinary returns WithMetaOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns WithMetaOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns WithMetaOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns WithMetaOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns WithMetaOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns WithMetaOperation
	 *     UnaryOperation returns WithMetaOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns WithMetaOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns WithMetaOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns WithMetaOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns WithMetaOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns WithMetaOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns WithMetaOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns WithMetaOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns WithMetaOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns WithMetaOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns WithMetaOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns WithMetaOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns WithMetaOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns WithMetaOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns WithMetaOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns WithMetaOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns WithMetaOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns WithMetaOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns WithMetaOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns WithMetaOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns WithMetaOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns WithMetaOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns WithMetaOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns WithMetaOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns WithMetaOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns WithMetaOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns WithMetaOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns WithMetaOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns WithMetaOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns WithMetaOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns WithMetaOperation
	 *     RosettaCalcPrimary returns WithMetaOperation
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcBinary_WithMetaOperation_0_1_0_4_0 operator='with-meta' metaFeature=[RosettaMetaType|ID] right=UnaryOperation) | 
	 *         (operator='with-meta' metaFeature=[RosettaMetaType|ID] right=UnaryOperation) | 
	 *         (left=RosettaCalcBinary_WithMetaOperation_1_1_0_4_0 operator='with-meta' metaFeature=[RosettaMetaType|ID] right=UnaryOperation)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcBinary(ISerializationContext context, WithMetaOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ComparisonOperation
	 *     RosettaCalcExpressionWithAsKey returns ComparisonOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ComparisonOperation
	 *     RosettaCalcExpression returns ComparisonOperation
	 *     ThenOperation returns ComparisonOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcOr returns ComparisonOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcAnd returns ComparisonOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcEquality returns ComparisonOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcComparison returns ComparisonOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcAdditive returns ComparisonOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcMultiplicative returns ComparisonOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcBinary returns ComparisonOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ComparisonOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ComparisonOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ComparisonOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ComparisonOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ComparisonOperation
	 *     UnaryOperation returns ComparisonOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ComparisonOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ComparisonOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ComparisonOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ComparisonOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ComparisonOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ComparisonOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ComparisonOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ComparisonOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ComparisonOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ComparisonOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ComparisonOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ComparisonOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ComparisonOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ComparisonOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ComparisonOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ComparisonOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ComparisonOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ComparisonOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ComparisonOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ComparisonOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ComparisonOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ComparisonOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ComparisonOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ComparisonOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ComparisonOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ComparisonOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ComparisonOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ComparisonOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ComparisonOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ComparisonOperation
	 *     RosettaCalcPrimary returns ComparisonOperation
	 *
	 * Constraint:
	 *     (
	 *         (
	 *             left=RosettaCalcComparison_ComparisonOperation_0_1_0_0_0 
	 *             cardMod=CardinalityModifier? 
	 *             (operator='&gt;=' | operator='&lt;=' | operator='&gt;' | operator='&lt;') 
	 *             right=RosettaCalcAdditive
	 *         ) | 
	 *         (cardMod=CardinalityModifier? (operator='&gt;=' | operator='&lt;=' | operator='&gt;' | operator='&lt;') right=RosettaCalcAdditive) | 
	 *         (
	 *             left=RosettaCalcComparison_ComparisonOperation_1_1_0_0_0 
	 *             cardMod=CardinalityModifier? 
	 *             (operator='&gt;=' | operator='&lt;=' | operator='&gt;' | operator='&lt;') 
	 *             right=RosettaCalcAdditive
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcComparison(ISerializationContext context, ComparisonOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcComparison.ComparisonOperation_1_1_0_0_0 returns ComparisonOperation
	 *
	 * Constraint:
	 *     (
	 *         (cardMod=CardinalityModifier? (operator='&gt;=' | operator='&lt;=' | operator='&gt;' | operator='&lt;') right=RosettaCalcAdditive) | 
	 *         (
	 *             left=RosettaCalcComparison_ComparisonOperation_1_1_0_0_0 
	 *             cardMod=CardinalityModifier? 
	 *             (operator='&gt;=' | operator='&lt;=' | operator='&gt;' | operator='&lt;') 
	 *             right=RosettaCalcAdditive
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcComparison_ComparisonOperation_1_1_0_0_0(ISerializationContext context, ComparisonOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaConditionalExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaConditionalExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcExpression returns RosettaConditionalExpression
	 *     ThenOperation returns RosettaConditionalExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcOr returns RosettaConditionalExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcAnd returns RosettaConditionalExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcEquality returns RosettaConditionalExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcComparison returns RosettaConditionalExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcAdditive returns RosettaConditionalExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcMultiplicative returns RosettaConditionalExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcBinary returns RosettaConditionalExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaConditionalExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaConditionalExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaConditionalExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaConditionalExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaConditionalExpression
	 *     UnaryOperation returns RosettaConditionalExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaConditionalExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaConditionalExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaConditionalExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaConditionalExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaConditionalExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaConditionalExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaConditionalExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaConditionalExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaConditionalExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaConditionalExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaConditionalExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaConditionalExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaConditionalExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaConditionalExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaConditionalExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaConditionalExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaConditionalExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaConditionalExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaConditionalExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaConditionalExpression
	 *     RosettaCalcPrimary returns RosettaConditionalExpression
	 *     RosettaCalcConditionalExpression returns RosettaConditionalExpression
	 *
	 * Constraint:
	 *     (if=RosettaCalcOr ifthen=RosettaCalcOr (full?='else' elsethen=RosettaCalcOr)?)
	 * </pre>
	 */
	protected void sequence_RosettaCalcConditionalExpression(ISerializationContext context, RosettaConditionalExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaConstructorExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaConstructorExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcExpression returns RosettaConstructorExpression
	 *     ThenOperation returns RosettaConstructorExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcOr returns RosettaConstructorExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcAnd returns RosettaConstructorExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcEquality returns RosettaConstructorExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcComparison returns RosettaConstructorExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcAdditive returns RosettaConstructorExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcMultiplicative returns RosettaConstructorExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcBinary returns RosettaConstructorExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaConstructorExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaConstructorExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaConstructorExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaConstructorExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaConstructorExpression
	 *     UnaryOperation returns RosettaConstructorExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaConstructorExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaConstructorExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaConstructorExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaConstructorExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaConstructorExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaConstructorExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaConstructorExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaConstructorExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaConstructorExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaConstructorExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaConstructorExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaConstructorExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaConstructorExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaConstructorExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaConstructorExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaConstructorExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaConstructorExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaConstructorExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaConstructorExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaConstructorExpression
	 *     RosettaCalcPrimary returns RosettaConstructorExpression
	 *     RosettaCalcConstructorExpression returns RosettaConstructorExpression
	 *
	 * Constraint:
	 *     (
	 *         typeCall=TypeCall 
	 *         valueExpression=RosettaCalcExpression? 
	 *         ((values+=ConstructorKeyValuePair values+=ConstructorKeyValuePair* implicitEmpty?='...'?) | implicitEmpty?='...')?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcConstructorExpression(ISerializationContext context, RosettaConstructorExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns EqualityOperation
	 *     RosettaCalcExpressionWithAsKey returns EqualityOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns EqualityOperation
	 *     RosettaCalcExpression returns EqualityOperation
	 *     ThenOperation returns EqualityOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcOr returns EqualityOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcAnd returns EqualityOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcEquality returns EqualityOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcComparison returns EqualityOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcAdditive returns EqualityOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcMultiplicative returns EqualityOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcBinary returns EqualityOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns EqualityOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns EqualityOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns EqualityOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns EqualityOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns EqualityOperation
	 *     UnaryOperation returns EqualityOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns EqualityOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns EqualityOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns EqualityOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns EqualityOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns EqualityOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns EqualityOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns EqualityOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns EqualityOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns EqualityOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns EqualityOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns EqualityOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns EqualityOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns EqualityOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns EqualityOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns EqualityOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns EqualityOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns EqualityOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns EqualityOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns EqualityOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns EqualityOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns EqualityOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns EqualityOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns EqualityOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns EqualityOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns EqualityOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns EqualityOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns EqualityOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns EqualityOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns EqualityOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns EqualityOperation
	 *     RosettaCalcPrimary returns EqualityOperation
	 *
	 * Constraint:
	 *     (
	 *         (left=RosettaCalcEquality_EqualityOperation_0_1_0_0_0 cardMod=CardinalityModifier? (operator='=' | operator='&lt;&gt;') right=RosettaCalcComparison) | 
	 *         (cardMod=CardinalityModifier? (operator='=' | operator='&lt;&gt;') right=RosettaCalcComparison) | 
	 *         (left=RosettaCalcEquality_EqualityOperation_1_1_0_0_0 cardMod=CardinalityModifier? (operator='=' | operator='&lt;&gt;') right=RosettaCalcComparison)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcEquality(ISerializationContext context, EqualityOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcEquality.EqualityOperation_1_1_0_0_0 returns EqualityOperation
	 *
	 * Constraint:
	 *     (
	 *         (cardMod=CardinalityModifier? (operator='=' | operator='&lt;&gt;') right=RosettaCalcComparison) | 
	 *         (left=RosettaCalcEquality_EqualityOperation_1_1_0_0_0 cardMod=CardinalityModifier? (operator='=' | operator='&lt;&gt;') right=RosettaCalcComparison)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcEquality_EqualityOperation_1_1_0_0_0(ISerializationContext context, EqualityOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcExpressionWithAsKey returns AsKeyOperation
	 *
	 * Constraint:
	 *     (argument=RosettaCalcExpressionWithAsKey_AsKeyOperation_1_0_0 operator='as-key')
	 * </pre>
	 */
	protected void sequence_RosettaCalcExpressionWithAsKey(ISerializationContext context, AsKeyOperation semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_UNARY_OPERATION__ARGUMENT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_UNARY_OPERATION__ARGUMENT));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getAsKeyOperationArgumentAction_1_0_0(), semanticObject.getArgument());
		feeder.accept(grammarAccess.getRosettaCalcExpressionWithAsKeyAccess().getOperatorAsKeyKeyword_1_0_1_0(), semanticObject.getOperator());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcMultiplicative.ArithmeticOperation_1_1_0_0_0 returns ArithmeticOperation
	 *
	 * Constraint:
	 *     (
	 *         ((operator='*' | operator='/') right=RosettaCalcBinary) | 
	 *         (left=RosettaCalcMultiplicative_ArithmeticOperation_1_1_0_0_0 (operator='*' | operator='/') right=RosettaCalcBinary)
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaCalcMultiplicative_ArithmeticOperation_1_1_0_0_0(ISerializationContext context, ArithmeticOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaOnlyExistsExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaOnlyExistsExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcExpression returns RosettaOnlyExistsExpression
	 *     ThenOperation returns RosettaOnlyExistsExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcOr returns RosettaOnlyExistsExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcAnd returns RosettaOnlyExistsExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcEquality returns RosettaOnlyExistsExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcComparison returns RosettaOnlyExistsExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcAdditive returns RosettaOnlyExistsExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcMultiplicative returns RosettaOnlyExistsExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcBinary returns RosettaOnlyExistsExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation returns RosettaOnlyExistsExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaOnlyExistsExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaOnlyExistsExpression
	 *     RosettaCalcPrimary returns RosettaOnlyExistsExpression
	 *     RosettaCalcOnlyExists returns RosettaOnlyExistsExpression
	 *
	 * Constraint:
	 *     (args+=RosettaOnlyExistsElement | (args+=RosettaOnlyExistsElement args+=RosettaOnlyExistsElement*))
	 * </pre>
	 */
	protected void sequence_RosettaCalcOnlyExists(ISerializationContext context, RosettaOnlyExistsExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCalcOr.LogicalOperation_1_1_0_0_0 returns LogicalOperation
	 *
	 * Constraint:
	 *     ((operator='or' right=RosettaCalcAnd) | (left=RosettaCalcOr_LogicalOperation_1_1_0_0_0 operator='or' right=RosettaCalcAnd))
	 * </pre>
	 */
	protected void sequence_RosettaCalcOr_LogicalOperation_1_1_0_0_0(ISerializationContext context, LogicalOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns TranslateDispatchOperation
	 *     RosettaCalcExpressionWithAsKey returns TranslateDispatchOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcExpression returns TranslateDispatchOperation
	 *     ThenOperation returns TranslateDispatchOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcOr returns TranslateDispatchOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcAnd returns TranslateDispatchOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcEquality returns TranslateDispatchOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcComparison returns TranslateDispatchOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcAdditive returns TranslateDispatchOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcMultiplicative returns TranslateDispatchOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcBinary returns TranslateDispatchOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns TranslateDispatchOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns TranslateDispatchOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns TranslateDispatchOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns TranslateDispatchOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns TranslateDispatchOperation
	 *     UnaryOperation returns TranslateDispatchOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns TranslateDispatchOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns TranslateDispatchOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns TranslateDispatchOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns TranslateDispatchOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns TranslateDispatchOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns TranslateDispatchOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns TranslateDispatchOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns TranslateDispatchOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns TranslateDispatchOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns TranslateDispatchOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns TranslateDispatchOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns TranslateDispatchOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns TranslateDispatchOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns TranslateDispatchOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns TranslateDispatchOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns TranslateDispatchOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns TranslateDispatchOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns TranslateDispatchOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns TranslateDispatchOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns TranslateDispatchOperation
	 *     RosettaCalcPrimary returns TranslateDispatchOperation
	 *     RosettaCalcTranslateDispatchOperation returns TranslateDispatchOperation
	 *
	 * Constraint:
	 *     (inputs+=RosettaCalcExpression inputs+=RosettaCalcExpression* outputType=TypeCall source=[TranslateSource|QualifiedName]?)
	 * </pre>
	 */
	protected void sequence_RosettaCalcTranslateDispatchOperation(ISerializationContext context, TranslateDispatchOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaCardinality returns RosettaCardinality
	 *
	 * Constraint:
	 *     ((inf=INT sup=INT) | (inf=INT unbounded?='*'))
	 * </pre>
	 */
	protected void sequence_RosettaCardinality(ISerializationContext context, RosettaCardinality semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaClassSynonymValue returns RosettaSynonymValueBase
	 *
	 * Constraint:
	 *     (name=STRING (refType=RosettaSynonymRef value=INT)? path=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaClassSynonymValue(ISerializationContext context, RosettaSynonymValueBase semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaClassSynonym returns RosettaClassSynonym
	 *
	 * Constraint:
	 *     (
	 *         sources+=[RosettaSynonymSource|QualifiedName] 
	 *         sources+=[RosettaSynonymSource|QualifiedName]* 
	 *         value=RosettaClassSynonymValue? 
	 *         metaValue=RosettaMetaSynonymValue?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaClassSynonym(ISerializationContext context, RosettaClassSynonym semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaCorpus
	 *     RosettaCorpus returns RosettaCorpus
	 *
	 * Constraint:
	 *     (corpusType=ID body=[RosettaBody|QualifiedName]? displayName=STRING? name=ValidID definition=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaCorpus_RosettaDefinable_RosettaNamed(ISerializationContext context, RosettaCorpus semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaDataReference returns RosettaDataReference
	 *     RosettaAttributeReference.RosettaAttributeReference_1_0 returns RosettaDataReference
	 *
	 * Constraint:
	 *     data=[Data|QualifiedName]
	 * </pre>
	 */
	protected void sequence_RosettaDataReference(ISerializationContext context, RosettaDataReference semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_DATA_REFERENCE__DATA) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_DATA_REFERENCE__DATA));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaDataReferenceAccess().getDataDataQualifiedNameParserRuleCall_1_0_1(), semanticObject.eGet(RosettaPackage.Literals.ROSETTA_DATA_REFERENCE__DATA, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaExternalFunction
	 *     RosettaLibraryFunction returns RosettaExternalFunction
	 *
	 * Constraint:
	 *     (name=ValidID (parameters+=RosettaParameter parameters+=RosettaParameter*)? typeCall=TypeCall definition=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaLibraryFunction_RosettaNamed_RosettaTyped(ISerializationContext context, RosettaExternalFunction semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaModel returns RosettaModel
	 *
	 * Constraint:
	 *     (
	 *         overridden?='override'? 
	 *         (name=QualifiedName | name=STRING) 
	 *         definition=STRING? 
	 *         version=STRING? 
	 *         imports+=Import* 
	 *         configurations+=RosettaQualifiableConfiguration* 
	 *         elements+=RosettaRootElement*
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaModel(ISerializationContext context, RosettaModel semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaRecordType
	 *     RosettaRecordType returns RosettaRecordType
	 *
	 * Constraint:
	 *     (name=ValidID definition=STRING? features+=RosettaRecordFeature*)
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaNamed_RosettaRecordType(ISerializationContext context, RosettaRecordType semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaTypeAlias
	 *     RosettaTypeAlias returns RosettaTypeAlias
	 *
	 * Constraint:
	 *     (name=ValidID (parameters+=TypeParameter parameters+=TypeParameter*)? definition=STRING? typeCall=TypeCall)
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaNamed_RosettaTyped_TypeParameters(ISerializationContext context, RosettaTypeAlias semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     ShortcutDeclaration returns ShortcutDeclaration
	 *
	 * Constraint:
	 *     (name=ValidID definition=STRING? expression=RosettaCalcExpression)
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaNamed_ShortcutDeclaration(ISerializationContext context, ShortcutDeclaration semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaBasicType
	 *     RosettaBasicType returns RosettaBasicType
	 *
	 * Constraint:
	 *     (name=ValidID (parameters+=TypeParameter parameters+=TypeParameter*)? definition=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaNamed_TypeParameters(ISerializationContext context, RosettaBasicType semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeParameter returns TypeParameter
	 *
	 * Constraint:
	 *     (name=TypeParameterValidID typeCall=TypeCall definition=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaDefinable_RosettaTyped_TypeParameter(ISerializationContext context, TypeParameter semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaDocReference returns RosettaDocReference
	 *
	 * Constraint:
	 *     (
	 *         docReference=RegulatoryDocumentReference 
	 *         rationales+=DocumentRationale* 
	 *         extraneousSegments+=RosettaSegmentRef* 
	 *         structuredProvision=STRING? 
	 *         provision=STRING? 
	 *         reportedField?='reportedField'?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaDocReference(ISerializationContext context, RosettaDocReference semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaEnumSynonym returns RosettaEnumSynonym
	 *
	 * Constraint:
	 *     (
	 *         sources+=[RosettaSynonymSource|QualifiedName] 
	 *         sources+=[RosettaSynonymSource|QualifiedName]* 
	 *         synonymValue=STRING 
	 *         definition=STRING? 
	 *         (patternMatch=STRING patternReplace=STRING)? 
	 *         removeHtml?='removeHtml'?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaEnumSynonym(ISerializationContext context, RosettaEnumSynonym semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalClassSynonym returns RosettaExternalClassSynonym
	 *
	 * Constraint:
	 *     (value=RosettaClassSynonymValue? metaValue=RosettaMetaSynonymValue)
	 * </pre>
	 */
	protected void sequence_RosettaExternalClassSynonym(ISerializationContext context, RosettaExternalClassSynonym semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalClass returns RosettaExternalClass
	 *
	 * Constraint:
	 *     (data=[Data|QualifiedName] externalClassSynonyms+=RosettaExternalClassSynonym* regularAttributes+=RosettaExternalRegularAttribute*)
	 * </pre>
	 */
	protected void sequence_RosettaExternalClass(ISerializationContext context, RosettaExternalClass semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalEnumSynonym returns RosettaEnumSynonym
	 *
	 * Constraint:
	 *     (synonymValue=STRING definition=STRING? (patternMatch=STRING patternReplace=STRING)?)
	 * </pre>
	 */
	protected void sequence_RosettaExternalEnumSynonym(ISerializationContext context, RosettaEnumSynonym semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalEnumValue returns RosettaExternalEnumValue
	 *
	 * Constraint:
	 *     (operator=ExternalValueOperator enumRef=[RosettaEnumValue|ValidID] externalEnumSynonyms+=RosettaExternalEnumSynonym*)
	 * </pre>
	 */
	protected void sequence_RosettaExternalEnumValue(ISerializationContext context, RosettaExternalEnumValue semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalEnum returns RosettaExternalEnum
	 *
	 * Constraint:
	 *     (enumeration=[RosettaEnumeration|QualifiedName] regularValues+=RosettaExternalEnumValue*)
	 * </pre>
	 */
	protected void sequence_RosettaExternalEnum(ISerializationContext context, RosettaExternalEnum semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalRegularAttribute returns RosettaExternalRegularAttribute
	 *
	 * Constraint:
	 *     (
	 *         operator=ExternalValueOperator 
	 *         attributeRef=[RosettaFeature|ValidID] 
	 *         externalSynonyms+=RosettaExternalSynonym* 
	 *         externalRuleReference=RosettaRuleReference?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaExternalRegularAttribute(ISerializationContext context, RosettaExternalRegularAttribute semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaExternalSynonym returns RosettaExternalSynonym
	 *
	 * Constraint:
	 *     body=RosettaSynonymBody
	 * </pre>
	 */
	protected void sequence_RosettaExternalSynonym(ISerializationContext context, RosettaExternalSynonym semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_EXTERNAL_SYNONYM__BODY) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_EXTERNAL_SYNONYM__BODY));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaExternalSynonymAccess().getBodyRosettaSynonymBodyParserRuleCall_1_0(), semanticObject.getBody());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCallArgumentExpression returns RosettaIntLiteral
	 *     CaseDefaultStatement returns RosettaIntLiteral
	 *     RosettaMapPrimaryExpression returns RosettaIntLiteral
	 *     RosettaLiteral returns RosettaIntLiteral
	 *     RosettaIntLiteral returns RosettaIntLiteral
	 *     RosettaCalcExpressionWithAsKey returns RosettaIntLiteral
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaIntLiteral
	 *     RosettaCalcExpression returns RosettaIntLiteral
	 *     ThenOperation returns RosettaIntLiteral
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcOr returns RosettaIntLiteral
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcAnd returns RosettaIntLiteral
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcEquality returns RosettaIntLiteral
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcComparison returns RosettaIntLiteral
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcAdditive returns RosettaIntLiteral
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcMultiplicative returns RosettaIntLiteral
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcBinary returns RosettaIntLiteral
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaIntLiteral
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaIntLiteral
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaIntLiteral
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaIntLiteral
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaIntLiteral
	 *     UnaryOperation returns RosettaIntLiteral
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaIntLiteral
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaIntLiteral
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaIntLiteral
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaIntLiteral
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaIntLiteral
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaIntLiteral
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaIntLiteral
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaIntLiteral
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaIntLiteral
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaIntLiteral
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaIntLiteral
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaIntLiteral
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaIntLiteral
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaIntLiteral
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaIntLiteral
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaIntLiteral
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaIntLiteral
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaIntLiteral
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaIntLiteral
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaIntLiteral
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaIntLiteral
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaIntLiteral
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaIntLiteral
	 *     RosettaCalcPrimary returns RosettaIntLiteral
	 *
	 * Constraint:
	 *     value=Integer
	 * </pre>
	 */
	protected void sequence_RosettaIntLiteral(ISerializationContext context, RosettaIntLiteral semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_INT_LITERAL__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_INT_LITERAL__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaIntLiteralAccess().getValueIntegerParserRuleCall_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTestExpression.RosettaMapTestExistsExpression_1_0_0 returns RosettaMapPathValue
	 *     RosettaMapTestExpression.RosettaMapTestAbsentExpression_1_1_0 returns RosettaMapPathValue
	 *     RosettaMapTestExpression.RosettaMapTestEqualityOperation_1_2_0 returns RosettaMapPathValue
	 *     RosettaMapPathValue returns RosettaMapPathValue
	 *
	 * Constraint:
	 *     path=STRING
	 * </pre>
	 */
	protected void sequence_RosettaMapPathValue(ISerializationContext context, RosettaMapPathValue semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_PATH_VALUE__PATH) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_PATH_VALUE__PATH));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaMapPathValueAccess().getPathSTRINGTerminalRuleCall_0(), semanticObject.getPath());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTest returns RosettaMapPath
	 *     RosettaMapPath returns RosettaMapPath
	 *
	 * Constraint:
	 *     path=RosettaMapPathValue
	 * </pre>
	 */
	protected void sequence_RosettaMapPath(ISerializationContext context, RosettaMapPath semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_PATH__PATH) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_PATH__PATH));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaMapPathAccess().getPathRosettaMapPathValueParserRuleCall_2_0(), semanticObject.getPath());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTest returns RosettaMapRosettaPath
	 *     RosettaMapRosettaPath returns RosettaMapRosettaPath
	 *
	 * Constraint:
	 *     path=RosettaAttributeReference
	 * </pre>
	 */
	protected void sequence_RosettaMapRosettaPath(ISerializationContext context, RosettaMapRosettaPath semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_ROSETTA_PATH__PATH) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_ROSETTA_PATH__PATH));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaMapRosettaPathAccess().getPathRosettaAttributeReferenceParserRuleCall_2_0(), semanticObject.getPath());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTest returns RosettaMapTestAbsentExpression
	 *     RosettaMapTestExpression returns RosettaMapTestAbsentExpression
	 *
	 * Constraint:
	 *     argument=RosettaMapTestExpression_RosettaMapTestAbsentExpression_1_1_0
	 * </pre>
	 */
	protected void sequence_RosettaMapTestExpression(ISerializationContext context, RosettaMapTestAbsentExpression semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_TEST_ABSENT_EXPRESSION__ARGUMENT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_TEST_ABSENT_EXPRESSION__ARGUMENT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaMapTestExpressionAccess().getRosettaMapTestAbsentExpressionArgumentAction_1_1_0(), semanticObject.getArgument());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTest returns RosettaMapTestEqualityOperation
	 *     RosettaMapTestExpression returns RosettaMapTestEqualityOperation
	 *
	 * Constraint:
	 *     (left=RosettaMapTestExpression_RosettaMapTestEqualityOperation_1_2_0 (operator='=' | operator='&lt;&gt;') right=RosettaMapPrimaryExpression)
	 * </pre>
	 */
	protected void sequence_RosettaMapTestExpression(ISerializationContext context, RosettaMapTestEqualityOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTest returns RosettaMapTestExistsExpression
	 *     RosettaMapTestExpression returns RosettaMapTestExistsExpression
	 *
	 * Constraint:
	 *     argument=RosettaMapTestExpression_RosettaMapTestExistsExpression_1_0_0
	 * </pre>
	 */
	protected void sequence_RosettaMapTestExpression(ISerializationContext context, RosettaMapTestExistsExpression semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_TEST_EXISTS_EXPRESSION__ARGUMENT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_MAP_TEST_EXISTS_EXPRESSION__ARGUMENT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaMapTestExpressionAccess().getRosettaMapTestExistsExpressionArgumentAction_1_0_0(), semanticObject.getArgument());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapTest returns RosettaMapTestFunc
	 *     RosettaMapTestFunc returns RosettaMapTestFunc
	 *
	 * Constraint:
	 *     (func=[RosettaCallableWithArgs|QualifiedName] predicatePath=RosettaMapPathValue?)
	 * </pre>
	 */
	protected void sequence_RosettaMapTestFunc(ISerializationContext context, RosettaMapTestFunc semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMappingInstance returns RosettaMappingInstance
	 *
	 * Constraint:
	 *     (when=RosettaMappingPathTests | (default?='default' set=RosettaMapPrimaryExpression))
	 * </pre>
	 */
	protected void sequence_RosettaMappingInstance(ISerializationContext context, RosettaMappingInstance semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMappingPathTests returns RosettaMappingPathTests
	 *
	 * Constraint:
	 *     (tests+=RosettaMapTest tests+=RosettaMapTest*)
	 * </pre>
	 */
	protected void sequence_RosettaMappingPathTests(ISerializationContext context, RosettaMappingPathTests semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMappingSetToInstance returns RosettaMappingInstance
	 *
	 * Constraint:
	 *     (set=RosettaMapPrimaryExpression when=RosettaMappingPathTests?)
	 * </pre>
	 */
	protected void sequence_RosettaMappingSetToInstance(ISerializationContext context, RosettaMappingInstance semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMappingSetTo returns RosettaMapping
	 *
	 * Constraint:
	 *     (instances+=RosettaMappingSetToInstance instances+=RosettaMappingSetToInstance*)
	 * </pre>
	 */
	protected void sequence_RosettaMappingSetTo(ISerializationContext context, RosettaMapping semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMapping returns RosettaMapping
	 *
	 * Constraint:
	 *     (instances+=RosettaMappingInstance instances+=RosettaMappingInstance*)
	 * </pre>
	 */
	protected void sequence_RosettaMapping(ISerializationContext context, RosettaMapping semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMergeSynonymValue returns RosettaMergeSynonymValue
	 *
	 * Constraint:
	 *     (name=STRING excludePath=STRING?)
	 * </pre>
	 */
	protected void sequence_RosettaMergeSynonymValue(ISerializationContext context, RosettaMergeSynonymValue semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaMetaSynonymValue returns RosettaSynonymValueBase
	 *
	 * Constraint:
	 *     (name=STRING (refType=RosettaSynonymRef value=INT)? path=STRING? maps=INT?)
	 * </pre>
	 */
	protected void sequence_RosettaMetaSynonymValue(ISerializationContext context, RosettaSynonymValueBase semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaQualifiedType returns RosettaBasicType
	 *     RosettaCalculationType returns RosettaBasicType
	 *
	 * Constraint:
	 *     name=ValidID
	 * </pre>
	 */
	protected void sequence_RosettaNamed(ISerializationContext context, RosettaBasicType semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaNamedAccess().getNameValidIDParserRuleCall_0(), semanticObject.getName());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaParameter returns RosettaParameter
	 *
	 * Constraint:
	 *     (name=ValidID typeCall=TypeCall isArray?='['?)
	 * </pre>
	 */
	protected void sequence_RosettaNamed_RosettaParameter_RosettaTyped(ISerializationContext context, RosettaParameter semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaSynonymSource
	 *     RosettaSynonymSource returns RosettaSynonymSource
	 *
	 * Constraint:
	 *     name=ValidID
	 * </pre>
	 */
	protected void sequence_RosettaNamed(ISerializationContext context, RosettaSynonymSource semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaNamedAccess().getNameValidIDParserRuleCall_0(), semanticObject.getName());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaMetaType
	 *     RosettaMetaType returns RosettaMetaType
	 *
	 * Constraint:
	 *     (name=ValidID typeCall=TypeCall)
	 * </pre>
	 */
	protected void sequence_RosettaNamed_RosettaTyped(ISerializationContext context, RosettaMetaType semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_TYPED__TYPE_CALL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_TYPED__TYPE_CALL));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaNamedAccess().getNameValidIDParserRuleCall_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getRosettaTypedAccess().getTypeCallTypeCallParserRuleCall_0(), semanticObject.getTypeCall());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRecordFeature returns RosettaRecordFeature
	 *
	 * Constraint:
	 *     (name=ValidID typeCall=TypeCall)
	 * </pre>
	 */
	protected void sequence_RosettaNamed_RosettaTyped(ISerializationContext context, RosettaRecordFeature semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_NAMED__NAME));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_TYPED__TYPE_CALL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_TYPED__TYPE_CALL));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaNamedAccess().getNameValidIDParserRuleCall_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getRosettaTypedAccess().getTypeCallTypeCallParserRuleCall_0(), semanticObject.getTypeCall());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TranslationParameter returns TranslationParameter
	 *
	 * Constraint:
	 *     (name=ValidID? typeCall=TypeCall)
	 * </pre>
	 */
	protected void sequence_RosettaNamed_RosettaTyped(ISerializationContext context, TranslationParameter semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns TranslateSource
	 *     TranslateSource returns TranslateSource
	 *
	 * Constraint:
	 *     (
	 *         name=ValidID 
	 *         (superSources+=[TranslateSource|QualifiedName] superSources+=[TranslateSource|QualifiedName]*)? 
	 *         (elements+=RosettaRootElement | translations+=Translation)*
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaNamed_TranslateSource(ISerializationContext context, TranslateSource semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCallArgumentExpression returns RosettaNumberLiteral
	 *     CaseDefaultStatement returns RosettaNumberLiteral
	 *     RosettaMapPrimaryExpression returns RosettaNumberLiteral
	 *     RosettaLiteral returns RosettaNumberLiteral
	 *     RosettaNumberLiteral returns RosettaNumberLiteral
	 *     RosettaCalcExpressionWithAsKey returns RosettaNumberLiteral
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcExpression returns RosettaNumberLiteral
	 *     ThenOperation returns RosettaNumberLiteral
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcOr returns RosettaNumberLiteral
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcAnd returns RosettaNumberLiteral
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcEquality returns RosettaNumberLiteral
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcComparison returns RosettaNumberLiteral
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcAdditive returns RosettaNumberLiteral
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcMultiplicative returns RosettaNumberLiteral
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcBinary returns RosettaNumberLiteral
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaNumberLiteral
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaNumberLiteral
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaNumberLiteral
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaNumberLiteral
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaNumberLiteral
	 *     UnaryOperation returns RosettaNumberLiteral
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaNumberLiteral
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaNumberLiteral
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaNumberLiteral
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaNumberLiteral
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaNumberLiteral
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaNumberLiteral
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaNumberLiteral
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaNumberLiteral
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaNumberLiteral
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaNumberLiteral
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaNumberLiteral
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaNumberLiteral
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaNumberLiteral
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaNumberLiteral
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaNumberLiteral
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaNumberLiteral
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaNumberLiteral
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaNumberLiteral
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaNumberLiteral
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaNumberLiteral
	 *     RosettaCalcPrimary returns RosettaNumberLiteral
	 *
	 * Constraint:
	 *     value=BigDecimal
	 * </pre>
	 */
	protected void sequence_RosettaNumberLiteral(ISerializationContext context, RosettaNumberLiteral semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_NUMBER_LITERAL__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_NUMBER_LITERAL__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaNumberLiteralAccess().getValueBigDecimalParserRuleCall_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaOnlyExistsElement returns RosettaImplicitVariable
	 *     RosettaOnlyExistsElement.RosettaFeatureCall_1_0 returns RosettaImplicitVariable
	 *     RosettaOnlyExistsElementRoot returns RosettaImplicitVariable
	 *
	 * Constraint:
	 *     (name='item' | name='it')
	 * </pre>
	 */
	protected void sequence_RosettaOnlyExistsElementRoot(ISerializationContext context, RosettaImplicitVariable semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaOnlyExistsElement returns RosettaSymbolReference
	 *     RosettaOnlyExistsElement.RosettaFeatureCall_1_0 returns RosettaSymbolReference
	 *     RosettaOnlyExistsElementRoot returns RosettaSymbolReference
	 *
	 * Constraint:
	 *     symbol=[RosettaSymbol|QualifiedName]
	 * </pre>
	 */
	protected void sequence_RosettaOnlyExistsElementRoot(ISerializationContext context, RosettaSymbolReference semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaOnlyExistsElementRootAccess().getSymbolRosettaSymbolQualifiedNameParserRuleCall_0_1_0_1(), semanticObject.eGet(ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaOnlyExistsElement returns RosettaFeatureCall
	 *     RosettaOnlyExistsElement.RosettaFeatureCall_1_0 returns RosettaFeatureCall
	 *
	 * Constraint:
	 *     (receiver=RosettaOnlyExistsElement_RosettaFeatureCall_1_0 feature=[RosettaFeature|ValidID])
	 * </pre>
	 */
	protected void sequence_RosettaOnlyExistsElement(ISerializationContext context, RosettaFeatureCall semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__RECEIVER) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__RECEIVER));
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__FEATURE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__FEATURE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaOnlyExistsElementAccess().getRosettaFeatureCallReceiverAction_1_0(), semanticObject.getReceiver());
		feeder.accept(grammarAccess.getRosettaOnlyExistsElementAccess().getFeatureRosettaFeatureValidIDParserRuleCall_1_2_0_1(), semanticObject.eGet(ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__FEATURE, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaQualifiableConfiguration returns RosettaQualifiableConfiguration
	 *
	 * Constraint:
	 *     (qType=RosettaQualifiableType rosettaClass=[Data|QualifiedName])
	 * </pre>
	 */
	protected void sequence_RosettaQualifiableConfiguration(ISerializationContext context, RosettaQualifiableConfiguration semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_QUALIFIABLE_CONFIGURATION__QTYPE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_QUALIFIABLE_CONFIGURATION__QTYPE));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_QUALIFIABLE_CONFIGURATION__ROSETTA_CLASS) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_QUALIFIABLE_CONFIGURATION__ROSETTA_CLASS));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaQualifiableConfigurationAccess().getQTypeRosettaQualifiableTypeEnumRuleCall_0_0(), semanticObject.getQType());
		feeder.accept(grammarAccess.getRosettaQualifiableConfigurationAccess().getRosettaClassDataQualifiedNameParserRuleCall_2_0_1(), semanticObject.eGet(RosettaPackage.Literals.ROSETTA_QUALIFIABLE_CONFIGURATION__ROSETTA_CLASS, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaImplicitVariable
	 *     RosettaReferenceOrFunctionCall returns RosettaImplicitVariable
	 *     RosettaCalcExpressionWithAsKey returns RosettaImplicitVariable
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcExpression returns RosettaImplicitVariable
	 *     ThenOperation returns RosettaImplicitVariable
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcOr returns RosettaImplicitVariable
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcAnd returns RosettaImplicitVariable
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcEquality returns RosettaImplicitVariable
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcComparison returns RosettaImplicitVariable
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcAdditive returns RosettaImplicitVariable
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcMultiplicative returns RosettaImplicitVariable
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcBinary returns RosettaImplicitVariable
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaImplicitVariable
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaImplicitVariable
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaImplicitVariable
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaImplicitVariable
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaImplicitVariable
	 *     UnaryOperation returns RosettaImplicitVariable
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaImplicitVariable
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaImplicitVariable
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaImplicitVariable
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaImplicitVariable
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaImplicitVariable
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaImplicitVariable
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaImplicitVariable
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaImplicitVariable
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaImplicitVariable
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaImplicitVariable
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaImplicitVariable
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaImplicitVariable
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaImplicitVariable
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaImplicitVariable
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaImplicitVariable
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaImplicitVariable
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaImplicitVariable
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaImplicitVariable
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaImplicitVariable
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaImplicitVariable
	 *     RosettaCalcPrimary returns RosettaImplicitVariable
	 *
	 * Constraint:
	 *     (name='item' | name='it')
	 * </pre>
	 */
	protected void sequence_RosettaReferenceOrFunctionCall(ISerializationContext context, RosettaImplicitVariable semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaSymbolReference
	 *     RosettaReferenceOrFunctionCall returns RosettaSymbolReference
	 *     RosettaCalcExpressionWithAsKey returns RosettaSymbolReference
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaSymbolReference
	 *     RosettaCalcExpression returns RosettaSymbolReference
	 *     ThenOperation returns RosettaSymbolReference
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcOr returns RosettaSymbolReference
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcAnd returns RosettaSymbolReference
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcEquality returns RosettaSymbolReference
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcComparison returns RosettaSymbolReference
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcAdditive returns RosettaSymbolReference
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcMultiplicative returns RosettaSymbolReference
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcBinary returns RosettaSymbolReference
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaSymbolReference
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaSymbolReference
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaSymbolReference
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaSymbolReference
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaSymbolReference
	 *     UnaryOperation returns RosettaSymbolReference
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaSymbolReference
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaSymbolReference
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaSymbolReference
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaSymbolReference
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaSymbolReference
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaSymbolReference
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaSymbolReference
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaSymbolReference
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaSymbolReference
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaSymbolReference
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaSymbolReference
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaSymbolReference
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaSymbolReference
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaSymbolReference
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaSymbolReference
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaSymbolReference
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaSymbolReference
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaSymbolReference
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaSymbolReference
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaSymbolReference
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaSymbolReference
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaSymbolReference
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaSymbolReference
	 *     RosettaCalcPrimary returns RosettaSymbolReference
	 *
	 * Constraint:
	 *     (symbol=[RosettaSymbol|QualifiedName] (explicitArguments?='(' (rawArgs+=RosettaCalcExpression rawArgs+=RosettaCalcExpression*)?)?)
	 * </pre>
	 */
	protected void sequence_RosettaReferenceOrFunctionCall(ISerializationContext context, RosettaSymbolReference semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaReport
	 *     RosettaReport returns RosettaReport
	 *
	 * Constraint:
	 *     (
	 *         regulatoryBody=RegulatoryDocumentReference 
	 *         inputType=TypeCall 
	 *         eligibilityRules+=[RosettaRule|QualifiedName] 
	 *         eligibilityRules+=[RosettaRule|QualifiedName]* 
	 *         reportingStandard=[RosettaCorpus|QualifiedName]? 
	 *         reportType=[Data|QualifiedName] 
	 *         ruleSource=[RosettaExternalRuleSource|QualifiedName]?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaReport(ISerializationContext context, RosettaReport semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRuleReference returns RosettaRuleReference
	 *
	 * Constraint:
	 *     reportingRule=[RosettaRule|QualifiedName]
	 * </pre>
	 */
	protected void sequence_RosettaRuleReference(ISerializationContext context, RosettaRuleReference semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, SimplePackage.Literals.ROSETTA_RULE_REFERENCE__REPORTING_RULE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, SimplePackage.Literals.ROSETTA_RULE_REFERENCE__REPORTING_RULE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaRuleReferenceAccess().getReportingRuleRosettaRuleQualifiedNameParserRuleCall_2_0_1(), semanticObject.eGet(SimplePackage.Literals.ROSETTA_RULE_REFERENCE__REPORTING_RULE, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaSegmentRef returns RosettaSegmentRef
	 *
	 * Constraint:
	 *     (segment=[RosettaSegment|QualifiedName] segmentRef=STRING)
	 * </pre>
	 */
	protected void sequence_RosettaSegmentRef(ISerializationContext context, RosettaSegmentRef semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_SEGMENT_REF__SEGMENT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_SEGMENT_REF__SEGMENT));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.ROSETTA_SEGMENT_REF__SEGMENT_REF) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.ROSETTA_SEGMENT_REF__SEGMENT_REF));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaSegmentRefAccess().getSegmentRosettaSegmentQualifiedNameParserRuleCall_0_0_1(), semanticObject.eGet(RosettaPackage.Literals.ROSETTA_SEGMENT_REF__SEGMENT, false));
		feeder.accept(grammarAccess.getRosettaSegmentRefAccess().getSegmentRefSTRINGTerminalRuleCall_1_0(), semanticObject.getSegmentRef());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaRootElement returns RosettaSegment
	 *     RosettaSegment returns RosettaSegment
	 *
	 * Constraint:
	 *     (name=ValidID | name='rationale' | name='rationale_author' | name='structured_provision')
	 * </pre>
	 */
	protected void sequence_RosettaSegment(ISerializationContext context, RosettaSegment semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCallArgumentExpression returns RosettaStringLiteral
	 *     CaseDefaultStatement returns RosettaStringLiteral
	 *     RosettaMapPrimaryExpression returns RosettaStringLiteral
	 *     RosettaLiteral returns RosettaStringLiteral
	 *     RosettaStringLiteral returns RosettaStringLiteral
	 *     RosettaCalcExpressionWithAsKey returns RosettaStringLiteral
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaStringLiteral
	 *     RosettaCalcExpression returns RosettaStringLiteral
	 *     ThenOperation returns RosettaStringLiteral
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcOr returns RosettaStringLiteral
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcAnd returns RosettaStringLiteral
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcEquality returns RosettaStringLiteral
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcComparison returns RosettaStringLiteral
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcAdditive returns RosettaStringLiteral
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcMultiplicative returns RosettaStringLiteral
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcBinary returns RosettaStringLiteral
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaStringLiteral
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaStringLiteral
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaStringLiteral
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaStringLiteral
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaStringLiteral
	 *     UnaryOperation returns RosettaStringLiteral
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaStringLiteral
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaStringLiteral
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaStringLiteral
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaStringLiteral
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaStringLiteral
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaStringLiteral
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaStringLiteral
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaStringLiteral
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaStringLiteral
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaStringLiteral
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaStringLiteral
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaStringLiteral
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaStringLiteral
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaStringLiteral
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaStringLiteral
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaStringLiteral
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaStringLiteral
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaStringLiteral
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaStringLiteral
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaStringLiteral
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaStringLiteral
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaStringLiteral
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaStringLiteral
	 *     RosettaCalcPrimary returns RosettaStringLiteral
	 *
	 * Constraint:
	 *     value=STRING
	 * </pre>
	 */
	protected void sequence_RosettaStringLiteral(ISerializationContext context, RosettaStringLiteral semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_STRING_LITERAL__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_STRING_LITERAL__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getRosettaStringLiteralAccess().getValueSTRINGTerminalRuleCall_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaSynonymBody returns RosettaSynonymBody
	 *
	 * Constraint:
	 *     (
	 *         (
	 *             (values+=RosettaSynonymValue* values+=RosettaSynonymValue mappingLogic=RosettaMapping? (metaValues+=STRING* metaValues+=STRING)?) | 
	 *             (hints+=STRING* hints+=STRING) | 
	 *             merge=RosettaMergeSynonymValue | 
	 *             mappingLogic=RosettaMappingSetTo | 
	 *             (metaValues+=STRING* metaValues+=STRING)
	 *         ) 
	 *         format=STRING? 
	 *         (patternMatch=STRING patternReplace=STRING)? 
	 *         removeHtml?='removeHtml'? 
	 *         mapper=STRING?
	 *     )
	 * </pre>
	 */
	protected void sequence_RosettaSynonymBody(ISerializationContext context, RosettaSynonymBody semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaSynonymValue returns RosettaSynonymValueBase
	 *
	 * Constraint:
	 *     (name=STRING (refType=RosettaSynonymRef value=INT)? path=STRING? maps=INT?)
	 * </pre>
	 */
	protected void sequence_RosettaSynonymValue(ISerializationContext context, RosettaSynonymValueBase semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     RosettaSynonym returns RosettaSynonym
	 *
	 * Constraint:
	 *     (sources+=[RosettaSynonymSource|QualifiedName] sources+=[RosettaSynonymSource|QualifiedName]* body=RosettaSynonymBody)
	 * </pre>
	 */
	protected void sequence_RosettaSynonym(ISerializationContext context, RosettaSynonym semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Segment returns Segment
	 *
	 * Constraint:
	 *     (attribute=[Attribute|ValidID] next=Segment?)
	 * </pre>
	 */
	protected void sequence_Segment(ISerializationContext context, Segment semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ThenOperation
	 *     RosettaCalcExpressionWithAsKey returns ThenOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ThenOperation
	 *     RosettaCalcExpression returns ThenOperation
	 *     ThenOperation returns ThenOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ThenOperation
	 *     RosettaCalcOr returns ThenOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ThenOperation
	 *     RosettaCalcAnd returns ThenOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ThenOperation
	 *     RosettaCalcEquality returns ThenOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ThenOperation
	 *     RosettaCalcComparison returns ThenOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ThenOperation
	 *     RosettaCalcAdditive returns ThenOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ThenOperation
	 *     RosettaCalcMultiplicative returns ThenOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ThenOperation
	 *     RosettaCalcBinary returns ThenOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ThenOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ThenOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ThenOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ThenOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ThenOperation
	 *     UnaryOperation returns ThenOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ThenOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ThenOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ThenOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ThenOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ThenOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ThenOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ThenOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ThenOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ThenOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ThenOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ThenOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ThenOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ThenOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ThenOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ThenOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ThenOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ThenOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ThenOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ThenOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ThenOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ThenOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ThenOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ThenOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ThenOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ThenOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ThenOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ThenOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ThenOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ThenOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ThenOperation
	 *     RosettaCalcPrimary returns ThenOperation
	 *
	 * Constraint:
	 *     (argument=ThenOperation_ThenOperation_1_0_0_0 operator='then' (function=InlineFunction | function=ImplicitInlineFunction)?)
	 * </pre>
	 */
	protected void sequence_ThenOperation(ISerializationContext context, ThenOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     Translation returns Translation
	 *
	 * Constraint:
	 *     (
	 *         parameters+=TranslationParameter 
	 *         parameters+=TranslationParameter* 
	 *         ((resultType=TypeCall hasExplicitOutputType?=':' expression=RosettaCalcExpression) | expression=RosettaCalcConstructorExpression)
	 *     )
	 * </pre>
	 */
	protected void sequence_Translation(ISerializationContext context, Translation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCallArgument returns TypeCallArgument
	 *
	 * Constraint:
	 *     (parameter=[TypeParameter|TypeParameterValidID] value=TypeCallArgumentExpression)
	 * </pre>
	 */
	protected void sequence_TypeCallArgument(ISerializationContext context, TypeCallArgument semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.TYPE_CALL_ARGUMENT__PARAMETER) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.TYPE_CALL_ARGUMENT__PARAMETER));
			if (transientValues.isValueTransient(semanticObject, RosettaPackage.Literals.TYPE_CALL_ARGUMENT__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, RosettaPackage.Literals.TYPE_CALL_ARGUMENT__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getTypeCallArgumentAccess().getParameterTypeParameterTypeParameterValidIDParserRuleCall_0_0_1(), semanticObject.eGet(RosettaPackage.Literals.TYPE_CALL_ARGUMENT__PARAMETER, false));
		feeder.accept(grammarAccess.getTypeCallArgumentAccess().getValueTypeCallArgumentExpressionParserRuleCall_2_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCall returns TypeCall
	 *
	 * Constraint:
	 *     (type=[RosettaType|QualifiedName] (arguments+=TypeCallArgument arguments+=TypeCallArgument*)?)
	 * </pre>
	 */
	protected void sequence_TypeCall(ISerializationContext context, TypeCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     TypeCallArgumentExpression returns RosettaSymbolReference
	 *     TypeParameterReference returns RosettaSymbolReference
	 *
	 * Constraint:
	 *     symbol=[RosettaSymbol|TypeParameterValidID]
	 * </pre>
	 */
	protected void sequence_TypeParameterReference(ISerializationContext context, RosettaSymbolReference semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getTypeParameterReferenceAccess().getSymbolRosettaSymbolTypeParameterValidIDParserRuleCall_0_1(), semanticObject.eGet(ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL, false));
		feeder.finish();
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns AsReferenceOperation
	 *     RosettaCalcExpressionWithAsKey returns AsReferenceOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns AsReferenceOperation
	 *     RosettaCalcExpression returns AsReferenceOperation
	 *     ThenOperation returns AsReferenceOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcOr returns AsReferenceOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcAnd returns AsReferenceOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcEquality returns AsReferenceOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcComparison returns AsReferenceOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcAdditive returns AsReferenceOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcMultiplicative returns AsReferenceOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcBinary returns AsReferenceOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns AsReferenceOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns AsReferenceOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns AsReferenceOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns AsReferenceOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns AsReferenceOperation
	 *     UnaryOperation returns AsReferenceOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns AsReferenceOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns AsReferenceOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns AsReferenceOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns AsReferenceOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns AsReferenceOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns AsReferenceOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns AsReferenceOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns AsReferenceOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns AsReferenceOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns AsReferenceOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns AsReferenceOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns AsReferenceOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns AsReferenceOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns AsReferenceOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns AsReferenceOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns AsReferenceOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns AsReferenceOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns AsReferenceOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns AsReferenceOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns AsReferenceOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns AsReferenceOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns AsReferenceOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns AsReferenceOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns AsReferenceOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns AsReferenceOperation
	 *     RosettaCalcPrimary returns AsReferenceOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_AsReferenceOperation_0_1_0_0_22_0 operator='as-reference') | 
	 *         operator='as-reference' | 
	 *         (argument=UnaryOperation_AsReferenceOperation_1_1_0_0_22_0 operator='as-reference')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, AsReferenceOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns AsReferenceOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns AsReferenceOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns AsReferenceOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns AsReferenceOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns AsReferenceOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns AsReferenceOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns AsReferenceOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns AsReferenceOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns AsReferenceOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns AsReferenceOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns AsReferenceOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns AsReferenceOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns AsReferenceOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns AsReferenceOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns AsReferenceOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns AsReferenceOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns AsReferenceOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns AsReferenceOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns AsReferenceOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns AsReferenceOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns AsReferenceOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns AsReferenceOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns AsReferenceOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns AsReferenceOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns AsReferenceOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns AsReferenceOperation
	 *
	 * Constraint:
	 *     (operator='as-reference' | (argument=UnaryOperation_AsReferenceOperation_1_1_0_0_22_0 operator='as-reference'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, AsReferenceOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ChoiceOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ChoiceOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ChoiceOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ChoiceOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ChoiceOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ChoiceOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ChoiceOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ChoiceOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ChoiceOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ChoiceOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ChoiceOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ChoiceOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ChoiceOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ChoiceOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ChoiceOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ChoiceOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ChoiceOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ChoiceOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ChoiceOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ChoiceOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ChoiceOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ChoiceOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ChoiceOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ChoiceOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ChoiceOperation
	 *
	 * Constraint:
	 *     (
	 *         (necessity=Necessity operator='choice' attributes+=[Attribute|ValidID] attributes+=[Attribute|ValidID]*) | 
	 *         (
	 *             argument=UnaryOperation_ChoiceOperation_1_1_0_0_13_0 
	 *             necessity=Necessity 
	 *             operator='choice' 
	 *             attributes+=[Attribute|ValidID] 
	 *             attributes+=[Attribute|ValidID]*
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ChoiceOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns DistinctOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns DistinctOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns DistinctOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns DistinctOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns DistinctOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns DistinctOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns DistinctOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns DistinctOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns DistinctOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns DistinctOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns DistinctOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns DistinctOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns DistinctOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns DistinctOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns DistinctOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns DistinctOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns DistinctOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns DistinctOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns DistinctOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns DistinctOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns DistinctOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns DistinctOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns DistinctOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns DistinctOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns DistinctOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns DistinctOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns DistinctOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns DistinctOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns DistinctOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns DistinctOperation
	 *
	 * Constraint:
	 *     (operator='distinct' | (argument=UnaryOperation_DistinctOperation_1_1_0_0_7_0 operator='distinct'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, DistinctOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns FilterOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns FilterOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns FilterOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns FilterOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns FilterOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns FilterOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns FilterOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns FilterOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns FilterOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns FilterOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns FilterOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns FilterOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns FilterOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns FilterOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns FilterOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns FilterOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns FilterOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns FilterOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns FilterOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns FilterOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns FilterOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns FilterOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns FilterOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns FilterOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns FilterOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns FilterOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns FilterOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns FilterOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns FilterOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns FilterOperation
	 *
	 * Constraint:
	 *     (
	 *         (operator='filter' (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (argument=UnaryOperation_FilterOperation_1_1_2_0_0_1_0 operator='filter' (function=InlineFunction | function=ImplicitInlineFunction)?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, FilterOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns FirstOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns FirstOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns FirstOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns FirstOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns FirstOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns FirstOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns FirstOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns FirstOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns FirstOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns FirstOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns FirstOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns FirstOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns FirstOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns FirstOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns FirstOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns FirstOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns FirstOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns FirstOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns FirstOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns FirstOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns FirstOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns FirstOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns FirstOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns FirstOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns FirstOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns FirstOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns FirstOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns FirstOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns FirstOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns FirstOperation
	 *
	 * Constraint:
	 *     (operator='first' | (argument=UnaryOperation_FirstOperation_1_1_0_0_9_0 operator='first'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, FirstOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns FlattenOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns FlattenOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns FlattenOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns FlattenOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns FlattenOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns FlattenOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns FlattenOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns FlattenOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns FlattenOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns FlattenOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns FlattenOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns FlattenOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns FlattenOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns FlattenOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns FlattenOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns FlattenOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns FlattenOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns FlattenOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns FlattenOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns FlattenOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns FlattenOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns FlattenOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns FlattenOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns FlattenOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns FlattenOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns FlattenOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns FlattenOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns FlattenOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns FlattenOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns FlattenOperation
	 *
	 * Constraint:
	 *     (operator='flatten' | (argument=UnaryOperation_FlattenOperation_1_1_0_0_6_0 operator='flatten'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, FlattenOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns LastOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns LastOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns LastOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns LastOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns LastOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns LastOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns LastOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns LastOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns LastOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns LastOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns LastOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns LastOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns LastOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns LastOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns LastOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns LastOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns LastOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns LastOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns LastOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns LastOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns LastOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns LastOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns LastOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns LastOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns LastOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns LastOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns LastOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns LastOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns LastOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns LastOperation
	 *
	 * Constraint:
	 *     (operator='last' | (argument=UnaryOperation_LastOperation_1_1_0_0_10_0 operator='last'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, LastOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns MapOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns MapOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns MapOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns MapOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns MapOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns MapOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns MapOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns MapOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns MapOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns MapOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns MapOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns MapOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns MapOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns MapOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns MapOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns MapOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns MapOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns MapOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns MapOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns MapOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns MapOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns MapOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns MapOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns MapOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns MapOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns MapOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns MapOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns MapOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns MapOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns MapOperation
	 *
	 * Constraint:
	 *     (
	 *         ((operator='map' | operator='extract') (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (
	 *             argument=UnaryOperation_MapOperation_1_1_2_0_0_2_0 
	 *             (operator='map' | operator='extract') 
	 *             (function=InlineFunction | function=ImplicitInlineFunction)?
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, MapOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns MaxOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns MaxOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns MaxOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns MaxOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns MaxOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns MaxOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns MaxOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns MaxOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns MaxOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns MaxOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns MaxOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns MaxOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns MaxOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns MaxOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns MaxOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns MaxOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns MaxOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns MaxOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns MaxOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns MaxOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns MaxOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns MaxOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns MaxOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns MaxOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns MaxOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns MaxOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns MaxOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns MaxOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns MaxOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns MaxOperation
	 *
	 * Constraint:
	 *     ((operator='max' function=InlineFunction?) | (argument=UnaryOperation_MaxOperation_1_1_1_0_0_2_0 operator='max' function=InlineFunction?))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, MaxOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns MinOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns MinOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns MinOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns MinOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns MinOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns MinOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns MinOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns MinOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns MinOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns MinOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns MinOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns MinOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns MinOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns MinOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns MinOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns MinOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns MinOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns MinOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns MinOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns MinOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns MinOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns MinOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns MinOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns MinOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns MinOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns MinOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns MinOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns MinOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns MinOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns MinOperation
	 *
	 * Constraint:
	 *     ((operator='min' function=InlineFunction?) | (argument=UnaryOperation_MinOperation_1_1_1_0_0_1_0 operator='min' function=InlineFunction?))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, MinOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns OneOfOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns OneOfOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns OneOfOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns OneOfOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns OneOfOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns OneOfOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns OneOfOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns OneOfOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns OneOfOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns OneOfOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns OneOfOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns OneOfOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns OneOfOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns OneOfOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns OneOfOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns OneOfOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns OneOfOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns OneOfOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns OneOfOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns OneOfOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns OneOfOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns OneOfOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns OneOfOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns OneOfOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns OneOfOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns OneOfOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns OneOfOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns OneOfOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns OneOfOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns OneOfOperation
	 *
	 * Constraint:
	 *     (operator='one-of' | (argument=UnaryOperation_OneOfOperation_1_1_0_0_12_0 operator='one-of'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, OneOfOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ReduceOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ReduceOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ReduceOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ReduceOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ReduceOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ReduceOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ReduceOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ReduceOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ReduceOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ReduceOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ReduceOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ReduceOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ReduceOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ReduceOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ReduceOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ReduceOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ReduceOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ReduceOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ReduceOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ReduceOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ReduceOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ReduceOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ReduceOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ReduceOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ReduceOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ReduceOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ReduceOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ReduceOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ReduceOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ReduceOperation
	 *
	 * Constraint:
	 *     (
	 *         (operator='reduce' (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (argument=UnaryOperation_ReduceOperation_1_1_2_0_0_0_0 operator='reduce' (function=InlineFunction | function=ImplicitInlineFunction)?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ReduceOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ReverseOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ReverseOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ReverseOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ReverseOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ReverseOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ReverseOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ReverseOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ReverseOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ReverseOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ReverseOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ReverseOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ReverseOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ReverseOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ReverseOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ReverseOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ReverseOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ReverseOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ReverseOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ReverseOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ReverseOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ReverseOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ReverseOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ReverseOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ReverseOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ReverseOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ReverseOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ReverseOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ReverseOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ReverseOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ReverseOperation
	 *
	 * Constraint:
	 *     (operator='reverse' | (argument=UnaryOperation_ReverseOperation_1_1_0_0_8_0 operator='reverse'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ReverseOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns RosettaAbsentExpression
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns RosettaAbsentExpression
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns RosettaAbsentExpression
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns RosettaAbsentExpression
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns RosettaAbsentExpression
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns RosettaAbsentExpression
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns RosettaAbsentExpression
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns RosettaAbsentExpression
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns RosettaAbsentExpression
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns RosettaAbsentExpression
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns RosettaAbsentExpression
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns RosettaAbsentExpression
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns RosettaAbsentExpression
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns RosettaAbsentExpression
	 *
	 * Constraint:
	 *     (operator='absent' | (argument=UnaryOperation_RosettaAbsentExpression_1_1_0_0_3_0 operator='absent'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, RosettaAbsentExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns RosettaCountOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns RosettaCountOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns RosettaCountOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns RosettaCountOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns RosettaCountOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns RosettaCountOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns RosettaCountOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns RosettaCountOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns RosettaCountOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns RosettaCountOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns RosettaCountOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns RosettaCountOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns RosettaCountOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns RosettaCountOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns RosettaCountOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns RosettaCountOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns RosettaCountOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns RosettaCountOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns RosettaCountOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns RosettaCountOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns RosettaCountOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns RosettaCountOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns RosettaCountOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns RosettaCountOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns RosettaCountOperation
	 *
	 * Constraint:
	 *     (operator='count' | (argument=UnaryOperation_RosettaCountOperation_1_1_0_0_5_0 operator='count'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, RosettaCountOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns RosettaDeepFeatureCall
	 *
	 * Constraint:
	 *     (receiver=UnaryOperation_RosettaDeepFeatureCall_1_1_0_0_1_0 feature=[Attribute|ValidID]?)
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, RosettaDeepFeatureCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns RosettaExistsExpression
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns RosettaExistsExpression
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns RosettaExistsExpression
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns RosettaExistsExpression
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns RosettaExistsExpression
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns RosettaExistsExpression
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns RosettaExistsExpression
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns RosettaExistsExpression
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns RosettaExistsExpression
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns RosettaExistsExpression
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns RosettaExistsExpression
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns RosettaExistsExpression
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns RosettaExistsExpression
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns RosettaExistsExpression
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns RosettaExistsExpression
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns RosettaExistsExpression
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns RosettaExistsExpression
	 *
	 * Constraint:
	 *     (
	 *         (modifier=ExistsModifier? operator='exists') | 
	 *         (argument=UnaryOperation_RosettaExistsExpression_1_1_0_0_2_0 modifier=ExistsModifier? operator='exists')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, RosettaExistsExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns RosettaFeatureCall
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns RosettaFeatureCall
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns RosettaFeatureCall
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns RosettaFeatureCall
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns RosettaFeatureCall
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns RosettaFeatureCall
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns RosettaFeatureCall
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns RosettaFeatureCall
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns RosettaFeatureCall
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns RosettaFeatureCall
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns RosettaFeatureCall
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns RosettaFeatureCall
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns RosettaFeatureCall
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns RosettaFeatureCall
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns RosettaFeatureCall
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns RosettaFeatureCall
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns RosettaFeatureCall
	 *
	 * Constraint:
	 *     (receiver=UnaryOperation_RosettaFeatureCall_1_1_0_0_0_0 feature=[RosettaFeature|ValidID]?)
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, RosettaFeatureCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns RosettaOnlyElement
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns RosettaOnlyElement
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns RosettaOnlyElement
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns RosettaOnlyElement
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns RosettaOnlyElement
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns RosettaOnlyElement
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns RosettaOnlyElement
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns RosettaOnlyElement
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns RosettaOnlyElement
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns RosettaOnlyElement
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns RosettaOnlyElement
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns RosettaOnlyElement
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns RosettaOnlyElement
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns RosettaOnlyElement
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns RosettaOnlyElement
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns RosettaOnlyElement
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns RosettaOnlyElement
	 *
	 * Constraint:
	 *     (operator='only-element' | (argument=UnaryOperation_RosettaOnlyElement_1_1_0_0_4_0 operator='only-element'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, RosettaOnlyElement semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns SortOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns SortOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns SortOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns SortOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns SortOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns SortOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns SortOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns SortOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns SortOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns SortOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns SortOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns SortOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns SortOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns SortOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns SortOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns SortOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns SortOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns SortOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns SortOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns SortOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns SortOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns SortOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns SortOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns SortOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns SortOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns SortOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns SortOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns SortOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns SortOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns SortOperation
	 *
	 * Constraint:
	 *     ((operator='sort' function=InlineFunction?) | (argument=UnaryOperation_SortOperation_1_1_1_0_0_0_0 operator='sort' function=InlineFunction?))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, SortOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns SumOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns SumOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns SumOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns SumOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns SumOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns SumOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns SumOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns SumOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns SumOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns SumOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns SumOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns SumOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns SumOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns SumOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns SumOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns SumOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns SumOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns SumOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns SumOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns SumOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns SumOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns SumOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns SumOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns SumOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns SumOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns SumOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns SumOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns SumOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns SumOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns SumOperation
	 *
	 * Constraint:
	 *     (operator='sum' | (argument=UnaryOperation_SumOperation_1_1_0_0_11_0 operator='sum'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, SumOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns SwitchOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns SwitchOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns SwitchOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns SwitchOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns SwitchOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns SwitchOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns SwitchOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns SwitchOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns SwitchOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns SwitchOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns SwitchOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns SwitchOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns SwitchOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns SwitchOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns SwitchOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns SwitchOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns SwitchOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns SwitchOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns SwitchOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns SwitchOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns SwitchOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns SwitchOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns SwitchOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns SwitchOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns SwitchOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns SwitchOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns SwitchOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns SwitchOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns SwitchOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns SwitchOperation
	 *
	 * Constraint:
	 *     (
	 *         (operator='switch' values+=CaseStatement values+=CaseStatement* default=CaseDefaultStatement?) | 
	 *         (
	 *             argument=UnaryOperation_SwitchOperation_1_1_0_0_23_0 
	 *             operator='switch' 
	 *             values+=CaseStatement 
	 *             values+=CaseStatement* 
	 *             default=CaseDefaultStatement?
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, SwitchOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToDateOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToDateOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToDateOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToDateOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToDateOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToDateOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToDateOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToDateOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToDateOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToDateOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToDateOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToDateOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToDateOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToDateOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToDateOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToDateOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToDateOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToDateOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToDateOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToDateOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToDateOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToDateOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToDateOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToDateOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToDateOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToDateOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToDateOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToDateOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToDateOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToDateOperation
	 *
	 * Constraint:
	 *     (operator='to-date' | (argument=UnaryOperation_ToDateOperation_1_1_0_0_19_0 operator='to-date'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToDateOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToDateTimeOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToDateTimeOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToDateTimeOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToDateTimeOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToDateTimeOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToDateTimeOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToDateTimeOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToDateTimeOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToDateTimeOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToDateTimeOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToDateTimeOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToDateTimeOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToDateTimeOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToDateTimeOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToDateTimeOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToDateTimeOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToDateTimeOperation
	 *
	 * Constraint:
	 *     (operator='to-date-time' | (argument=UnaryOperation_ToDateTimeOperation_1_1_0_0_20_0 operator='to-date-time'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToDateTimeOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToEnumOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToEnumOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToEnumOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToEnumOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToEnumOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToEnumOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToEnumOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToEnumOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToEnumOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToEnumOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToEnumOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToEnumOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToEnumOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToEnumOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToEnumOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToEnumOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToEnumOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToEnumOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToEnumOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToEnumOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToEnumOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToEnumOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToEnumOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToEnumOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToEnumOperation
	 *
	 * Constraint:
	 *     (
	 *         (operator='to-enum' enumeration=[RosettaEnumeration|QualifiedName]) | 
	 *         (argument=UnaryOperation_ToEnumOperation_1_1_0_0_18_0 operator='to-enum' enumeration=[RosettaEnumeration|QualifiedName])
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToEnumOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToIntOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToIntOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToIntOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToIntOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToIntOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToIntOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToIntOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToIntOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToIntOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToIntOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToIntOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToIntOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToIntOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToIntOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToIntOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToIntOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToIntOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToIntOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToIntOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToIntOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToIntOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToIntOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToIntOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToIntOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToIntOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToIntOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToIntOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToIntOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToIntOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToIntOperation
	 *
	 * Constraint:
	 *     (operator='to-int' | (argument=UnaryOperation_ToIntOperation_1_1_0_0_16_0 operator='to-int'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToIntOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToNumberOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToNumberOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToNumberOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToNumberOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToNumberOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToNumberOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToNumberOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToNumberOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToNumberOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToNumberOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToNumberOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToNumberOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToNumberOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToNumberOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToNumberOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToNumberOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToNumberOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToNumberOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToNumberOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToNumberOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToNumberOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToNumberOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToNumberOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToNumberOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToNumberOperation
	 *
	 * Constraint:
	 *     (operator='to-number' | (argument=UnaryOperation_ToNumberOperation_1_1_0_0_15_0 operator='to-number'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToNumberOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToStringOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToStringOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToStringOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToStringOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToStringOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToStringOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToStringOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToStringOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToStringOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToStringOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToStringOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToStringOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToStringOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToStringOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToStringOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToStringOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToStringOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToStringOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToStringOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToStringOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToStringOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToStringOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToStringOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToStringOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToStringOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToStringOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToStringOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToStringOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToStringOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToStringOperation
	 *
	 * Constraint:
	 *     (operator='to-string' | (argument=UnaryOperation_ToStringOperation_1_1_0_0_14_0 operator='to-string'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToStringOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToTimeOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToTimeOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToTimeOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToTimeOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToTimeOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToTimeOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToTimeOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToTimeOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToTimeOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToTimeOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToTimeOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToTimeOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToTimeOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToTimeOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToTimeOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToTimeOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToTimeOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToTimeOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToTimeOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToTimeOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToTimeOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToTimeOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToTimeOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToTimeOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToTimeOperation
	 *
	 * Constraint:
	 *     (operator='to-time' | (argument=UnaryOperation_ToTimeOperation_1_1_0_0_17_0 operator='to-time'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToTimeOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     UnaryOperation.RosettaFeatureCall_1_1_0_0_0_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_1_1_0_0_1_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaExistsExpression_1_1_0_0_2_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaAbsentExpression_1_1_0_0_3_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaOnlyElement_1_1_0_0_4_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaCountOperation_1_1_0_0_5_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.FlattenOperation_1_1_0_0_6_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.DistinctOperation_1_1_0_0_7_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ReverseOperation_1_1_0_0_8_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.FirstOperation_1_1_0_0_9_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.LastOperation_1_1_0_0_10_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.SumOperation_1_1_0_0_11_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.OneOfOperation_1_1_0_0_12_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ChoiceOperation_1_1_0_0_13_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToStringOperation_1_1_0_0_14_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToNumberOperation_1_1_0_0_15_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToIntOperation_1_1_0_0_16_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToTimeOperation_1_1_0_0_17_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToEnumOperation_1_1_0_0_18_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToDateOperation_1_1_0_0_19_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToDateTimeOperation_1_1_0_0_20_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_1_1_0_0_21_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.AsReferenceOperation_1_1_0_0_22_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.SwitchOperation_1_1_0_0_23_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.SortOperation_1_1_1_0_0_0_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.MinOperation_1_1_1_0_0_1_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.MaxOperation_1_1_1_0_0_2_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ReduceOperation_1_1_2_0_0_0_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.FilterOperation_1_1_2_0_0_1_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.MapOperation_1_1_2_0_0_2_0 returns ToZonedDateTimeOperation
	 *
	 * Constraint:
	 *     (operator='to-zoned-date-time' | (argument=UnaryOperation_ToZonedDateTimeOperation_1_1_0_0_21_0 operator='to-zoned-date-time'))
	 * </pre>
	 */
	protected void sequence_UnaryOperation_AsReferenceOperation_1_1_0_0_22_0_ChoiceOperation_1_1_0_0_13_0_DistinctOperation_1_1_0_0_7_0_FilterOperation_1_1_2_0_0_1_0_FirstOperation_1_1_0_0_9_0_FlattenOperation_1_1_0_0_6_0_LastOperation_1_1_0_0_10_0_MapOperation_1_1_2_0_0_2_0_MaxOperation_1_1_1_0_0_2_0_MinOperation_1_1_1_0_0_1_0_OneOfOperation_1_1_0_0_12_0_ReduceOperation_1_1_2_0_0_0_0_ReverseOperation_1_1_0_0_8_0_RosettaAbsentExpression_1_1_0_0_3_0_RosettaCountOperation_1_1_0_0_5_0_RosettaDeepFeatureCall_1_1_0_0_1_0_RosettaExistsExpression_1_1_0_0_2_0_RosettaFeatureCall_1_1_0_0_0_0_RosettaOnlyElement_1_1_0_0_4_0_SortOperation_1_1_1_0_0_0_0_SumOperation_1_1_0_0_11_0_SwitchOperation_1_1_0_0_23_0_ToDateOperation_1_1_0_0_19_0_ToDateTimeOperation_1_1_0_0_20_0_ToEnumOperation_1_1_0_0_18_0_ToIntOperation_1_1_0_0_16_0_ToNumberOperation_1_1_0_0_15_0_ToStringOperation_1_1_0_0_14_0_ToTimeOperation_1_1_0_0_17_0_ToZonedDateTimeOperation_1_1_0_0_21_0(ISerializationContext context, ToZonedDateTimeOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ChoiceOperation
	 *     RosettaCalcExpressionWithAsKey returns ChoiceOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ChoiceOperation
	 *     RosettaCalcExpression returns ChoiceOperation
	 *     ThenOperation returns ChoiceOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcOr returns ChoiceOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcAnd returns ChoiceOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcEquality returns ChoiceOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcComparison returns ChoiceOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcAdditive returns ChoiceOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcMultiplicative returns ChoiceOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcBinary returns ChoiceOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ChoiceOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ChoiceOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ChoiceOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ChoiceOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ChoiceOperation
	 *     UnaryOperation returns ChoiceOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ChoiceOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ChoiceOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ChoiceOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ChoiceOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ChoiceOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ChoiceOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ChoiceOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ChoiceOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ChoiceOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ChoiceOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ChoiceOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ChoiceOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ChoiceOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ChoiceOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ChoiceOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ChoiceOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ChoiceOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ChoiceOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ChoiceOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ChoiceOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ChoiceOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ChoiceOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ChoiceOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ChoiceOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ChoiceOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ChoiceOperation
	 *     RosettaCalcPrimary returns ChoiceOperation
	 *
	 * Constraint:
	 *     (
	 *         (
	 *             argument=UnaryOperation_ChoiceOperation_0_1_0_0_13_0 
	 *             necessity=Necessity 
	 *             operator='choice' 
	 *             attributes+=[Attribute|ValidID] 
	 *             attributes+=[Attribute|ValidID]*
	 *         ) | 
	 *         (necessity=Necessity operator='choice' attributes+=[Attribute|ValidID] attributes+=[Attribute|ValidID]*) | 
	 *         (
	 *             argument=UnaryOperation_ChoiceOperation_1_1_0_0_13_0 
	 *             necessity=Necessity 
	 *             operator='choice' 
	 *             attributes+=[Attribute|ValidID] 
	 *             attributes+=[Attribute|ValidID]*
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ChoiceOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns DistinctOperation
	 *     RosettaCalcExpressionWithAsKey returns DistinctOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns DistinctOperation
	 *     RosettaCalcExpression returns DistinctOperation
	 *     ThenOperation returns DistinctOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcOr returns DistinctOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcAnd returns DistinctOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcEquality returns DistinctOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcComparison returns DistinctOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcAdditive returns DistinctOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcMultiplicative returns DistinctOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcBinary returns DistinctOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns DistinctOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns DistinctOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns DistinctOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns DistinctOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns DistinctOperation
	 *     UnaryOperation returns DistinctOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns DistinctOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns DistinctOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns DistinctOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns DistinctOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns DistinctOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns DistinctOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns DistinctOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns DistinctOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns DistinctOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns DistinctOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns DistinctOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns DistinctOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns DistinctOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns DistinctOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns DistinctOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns DistinctOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns DistinctOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns DistinctOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns DistinctOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns DistinctOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns DistinctOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns DistinctOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns DistinctOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns DistinctOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns DistinctOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns DistinctOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns DistinctOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns DistinctOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns DistinctOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns DistinctOperation
	 *     RosettaCalcPrimary returns DistinctOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_DistinctOperation_0_1_0_0_7_0 operator='distinct') | 
	 *         operator='distinct' | 
	 *         (argument=UnaryOperation_DistinctOperation_1_1_0_0_7_0 operator='distinct')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, DistinctOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns FilterOperation
	 *     RosettaCalcExpressionWithAsKey returns FilterOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns FilterOperation
	 *     RosettaCalcExpression returns FilterOperation
	 *     ThenOperation returns FilterOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns FilterOperation
	 *     RosettaCalcOr returns FilterOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns FilterOperation
	 *     RosettaCalcAnd returns FilterOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns FilterOperation
	 *     RosettaCalcEquality returns FilterOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns FilterOperation
	 *     RosettaCalcComparison returns FilterOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns FilterOperation
	 *     RosettaCalcAdditive returns FilterOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns FilterOperation
	 *     RosettaCalcMultiplicative returns FilterOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns FilterOperation
	 *     RosettaCalcBinary returns FilterOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns FilterOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns FilterOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns FilterOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns FilterOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns FilterOperation
	 *     UnaryOperation returns FilterOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns FilterOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns FilterOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns FilterOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns FilterOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns FilterOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns FilterOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns FilterOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns FilterOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns FilterOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns FilterOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns FilterOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns FilterOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns FilterOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns FilterOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns FilterOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns FilterOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns FilterOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns FilterOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns FilterOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns FilterOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns FilterOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns FilterOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns FilterOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns FilterOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns FilterOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns FilterOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns FilterOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns FilterOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns FilterOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns FilterOperation
	 *     RosettaCalcPrimary returns FilterOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_FilterOperation_0_1_2_0_0_1_0 operator='filter' (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (operator='filter' (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (argument=UnaryOperation_FilterOperation_1_1_2_0_0_1_0 operator='filter' (function=InlineFunction | function=ImplicitInlineFunction)?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, FilterOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns FirstOperation
	 *     RosettaCalcExpressionWithAsKey returns FirstOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns FirstOperation
	 *     RosettaCalcExpression returns FirstOperation
	 *     ThenOperation returns FirstOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns FirstOperation
	 *     RosettaCalcOr returns FirstOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns FirstOperation
	 *     RosettaCalcAnd returns FirstOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns FirstOperation
	 *     RosettaCalcEquality returns FirstOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns FirstOperation
	 *     RosettaCalcComparison returns FirstOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns FirstOperation
	 *     RosettaCalcAdditive returns FirstOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns FirstOperation
	 *     RosettaCalcMultiplicative returns FirstOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns FirstOperation
	 *     RosettaCalcBinary returns FirstOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns FirstOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns FirstOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns FirstOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns FirstOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns FirstOperation
	 *     UnaryOperation returns FirstOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns FirstOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns FirstOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns FirstOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns FirstOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns FirstOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns FirstOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns FirstOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns FirstOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns FirstOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns FirstOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns FirstOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns FirstOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns FirstOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns FirstOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns FirstOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns FirstOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns FirstOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns FirstOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns FirstOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns FirstOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns FirstOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns FirstOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns FirstOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns FirstOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns FirstOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns FirstOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns FirstOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns FirstOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns FirstOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns FirstOperation
	 *     RosettaCalcPrimary returns FirstOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_FirstOperation_0_1_0_0_9_0 operator='first') | 
	 *         operator='first' | 
	 *         (argument=UnaryOperation_FirstOperation_1_1_0_0_9_0 operator='first')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, FirstOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns FlattenOperation
	 *     RosettaCalcExpressionWithAsKey returns FlattenOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns FlattenOperation
	 *     RosettaCalcExpression returns FlattenOperation
	 *     ThenOperation returns FlattenOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcOr returns FlattenOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcAnd returns FlattenOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcEquality returns FlattenOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcComparison returns FlattenOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcAdditive returns FlattenOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcMultiplicative returns FlattenOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcBinary returns FlattenOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns FlattenOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns FlattenOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns FlattenOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns FlattenOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns FlattenOperation
	 *     UnaryOperation returns FlattenOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns FlattenOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns FlattenOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns FlattenOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns FlattenOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns FlattenOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns FlattenOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns FlattenOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns FlattenOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns FlattenOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns FlattenOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns FlattenOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns FlattenOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns FlattenOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns FlattenOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns FlattenOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns FlattenOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns FlattenOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns FlattenOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns FlattenOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns FlattenOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns FlattenOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns FlattenOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns FlattenOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns FlattenOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns FlattenOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns FlattenOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns FlattenOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns FlattenOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns FlattenOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns FlattenOperation
	 *     RosettaCalcPrimary returns FlattenOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_FlattenOperation_0_1_0_0_6_0 operator='flatten') | 
	 *         operator='flatten' | 
	 *         (argument=UnaryOperation_FlattenOperation_1_1_0_0_6_0 operator='flatten')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, FlattenOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns LastOperation
	 *     RosettaCalcExpressionWithAsKey returns LastOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns LastOperation
	 *     RosettaCalcExpression returns LastOperation
	 *     ThenOperation returns LastOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns LastOperation
	 *     RosettaCalcOr returns LastOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns LastOperation
	 *     RosettaCalcAnd returns LastOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns LastOperation
	 *     RosettaCalcEquality returns LastOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns LastOperation
	 *     RosettaCalcComparison returns LastOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns LastOperation
	 *     RosettaCalcAdditive returns LastOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns LastOperation
	 *     RosettaCalcMultiplicative returns LastOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns LastOperation
	 *     RosettaCalcBinary returns LastOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns LastOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns LastOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns LastOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns LastOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns LastOperation
	 *     UnaryOperation returns LastOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns LastOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns LastOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns LastOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns LastOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns LastOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns LastOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns LastOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns LastOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns LastOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns LastOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns LastOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns LastOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns LastOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns LastOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns LastOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns LastOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns LastOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns LastOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns LastOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns LastOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns LastOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns LastOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns LastOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns LastOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns LastOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns LastOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns LastOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns LastOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns LastOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns LastOperation
	 *     RosettaCalcPrimary returns LastOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_LastOperation_0_1_0_0_10_0 operator='last') | 
	 *         operator='last' | 
	 *         (argument=UnaryOperation_LastOperation_1_1_0_0_10_0 operator='last')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, LastOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns MapOperation
	 *     RosettaCalcExpressionWithAsKey returns MapOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns MapOperation
	 *     RosettaCalcExpression returns MapOperation
	 *     ThenOperation returns MapOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns MapOperation
	 *     RosettaCalcOr returns MapOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns MapOperation
	 *     RosettaCalcAnd returns MapOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns MapOperation
	 *     RosettaCalcEquality returns MapOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns MapOperation
	 *     RosettaCalcComparison returns MapOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns MapOperation
	 *     RosettaCalcAdditive returns MapOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns MapOperation
	 *     RosettaCalcMultiplicative returns MapOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns MapOperation
	 *     RosettaCalcBinary returns MapOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns MapOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns MapOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns MapOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns MapOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns MapOperation
	 *     UnaryOperation returns MapOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns MapOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns MapOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns MapOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns MapOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns MapOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns MapOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns MapOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns MapOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns MapOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns MapOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns MapOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns MapOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns MapOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns MapOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns MapOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns MapOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns MapOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns MapOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns MapOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns MapOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns MapOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns MapOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns MapOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns MapOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns MapOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns MapOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns MapOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns MapOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns MapOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns MapOperation
	 *     RosettaCalcPrimary returns MapOperation
	 *
	 * Constraint:
	 *     (
	 *         (
	 *             argument=UnaryOperation_MapOperation_0_1_2_0_0_2_0 
	 *             (operator='map' | operator='extract') 
	 *             (function=InlineFunction | function=ImplicitInlineFunction)?
	 *         ) | 
	 *         ((operator='map' | operator='extract') (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (
	 *             argument=UnaryOperation_MapOperation_1_1_2_0_0_2_0 
	 *             (operator='map' | operator='extract') 
	 *             (function=InlineFunction | function=ImplicitInlineFunction)?
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, MapOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns MaxOperation
	 *     RosettaCalcExpressionWithAsKey returns MaxOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns MaxOperation
	 *     RosettaCalcExpression returns MaxOperation
	 *     ThenOperation returns MaxOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns MaxOperation
	 *     RosettaCalcOr returns MaxOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns MaxOperation
	 *     RosettaCalcAnd returns MaxOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns MaxOperation
	 *     RosettaCalcEquality returns MaxOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns MaxOperation
	 *     RosettaCalcComparison returns MaxOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns MaxOperation
	 *     RosettaCalcAdditive returns MaxOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns MaxOperation
	 *     RosettaCalcMultiplicative returns MaxOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns MaxOperation
	 *     RosettaCalcBinary returns MaxOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns MaxOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns MaxOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns MaxOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns MaxOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns MaxOperation
	 *     UnaryOperation returns MaxOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns MaxOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns MaxOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns MaxOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns MaxOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns MaxOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns MaxOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns MaxOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns MaxOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns MaxOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns MaxOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns MaxOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns MaxOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns MaxOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns MaxOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns MaxOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns MaxOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns MaxOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns MaxOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns MaxOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns MaxOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns MaxOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns MaxOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns MaxOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns MaxOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns MaxOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns MaxOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns MaxOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns MaxOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns MaxOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns MaxOperation
	 *     RosettaCalcPrimary returns MaxOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_MaxOperation_0_1_1_0_0_2_0 operator='max' function=InlineFunction?) | 
	 *         (operator='max' function=InlineFunction?) | 
	 *         (argument=UnaryOperation_MaxOperation_1_1_1_0_0_2_0 operator='max' function=InlineFunction?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, MaxOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns MinOperation
	 *     RosettaCalcExpressionWithAsKey returns MinOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns MinOperation
	 *     RosettaCalcExpression returns MinOperation
	 *     ThenOperation returns MinOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns MinOperation
	 *     RosettaCalcOr returns MinOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns MinOperation
	 *     RosettaCalcAnd returns MinOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns MinOperation
	 *     RosettaCalcEquality returns MinOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns MinOperation
	 *     RosettaCalcComparison returns MinOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns MinOperation
	 *     RosettaCalcAdditive returns MinOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns MinOperation
	 *     RosettaCalcMultiplicative returns MinOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns MinOperation
	 *     RosettaCalcBinary returns MinOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns MinOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns MinOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns MinOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns MinOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns MinOperation
	 *     UnaryOperation returns MinOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns MinOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns MinOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns MinOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns MinOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns MinOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns MinOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns MinOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns MinOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns MinOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns MinOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns MinOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns MinOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns MinOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns MinOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns MinOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns MinOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns MinOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns MinOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns MinOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns MinOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns MinOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns MinOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns MinOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns MinOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns MinOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns MinOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns MinOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns MinOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns MinOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns MinOperation
	 *     RosettaCalcPrimary returns MinOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_MinOperation_0_1_1_0_0_1_0 operator='min' function=InlineFunction?) | 
	 *         (operator='min' function=InlineFunction?) | 
	 *         (argument=UnaryOperation_MinOperation_1_1_1_0_0_1_0 operator='min' function=InlineFunction?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, MinOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns OneOfOperation
	 *     RosettaCalcExpressionWithAsKey returns OneOfOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns OneOfOperation
	 *     RosettaCalcExpression returns OneOfOperation
	 *     ThenOperation returns OneOfOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcOr returns OneOfOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcAnd returns OneOfOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcEquality returns OneOfOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcComparison returns OneOfOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcAdditive returns OneOfOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcMultiplicative returns OneOfOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcBinary returns OneOfOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns OneOfOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns OneOfOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns OneOfOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns OneOfOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns OneOfOperation
	 *     UnaryOperation returns OneOfOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns OneOfOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns OneOfOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns OneOfOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns OneOfOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns OneOfOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns OneOfOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns OneOfOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns OneOfOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns OneOfOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns OneOfOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns OneOfOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns OneOfOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns OneOfOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns OneOfOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns OneOfOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns OneOfOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns OneOfOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns OneOfOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns OneOfOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns OneOfOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns OneOfOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns OneOfOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns OneOfOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns OneOfOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns OneOfOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns OneOfOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns OneOfOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns OneOfOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns OneOfOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns OneOfOperation
	 *     RosettaCalcPrimary returns OneOfOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_OneOfOperation_0_1_0_0_12_0 operator='one-of') | 
	 *         operator='one-of' | 
	 *         (argument=UnaryOperation_OneOfOperation_1_1_0_0_12_0 operator='one-of')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, OneOfOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ReduceOperation
	 *     RosettaCalcExpressionWithAsKey returns ReduceOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ReduceOperation
	 *     RosettaCalcExpression returns ReduceOperation
	 *     ThenOperation returns ReduceOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcOr returns ReduceOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcAnd returns ReduceOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcEquality returns ReduceOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcComparison returns ReduceOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcAdditive returns ReduceOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcMultiplicative returns ReduceOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcBinary returns ReduceOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ReduceOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ReduceOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ReduceOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ReduceOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ReduceOperation
	 *     UnaryOperation returns ReduceOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ReduceOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ReduceOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ReduceOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ReduceOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ReduceOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ReduceOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ReduceOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ReduceOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ReduceOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ReduceOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ReduceOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ReduceOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ReduceOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ReduceOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ReduceOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ReduceOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ReduceOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ReduceOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ReduceOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ReduceOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ReduceOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ReduceOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ReduceOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ReduceOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ReduceOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ReduceOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ReduceOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ReduceOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ReduceOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ReduceOperation
	 *     RosettaCalcPrimary returns ReduceOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ReduceOperation_0_1_2_0_0_0_0 operator='reduce' (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (operator='reduce' (function=InlineFunction | function=ImplicitInlineFunction)?) | 
	 *         (argument=UnaryOperation_ReduceOperation_1_1_2_0_0_0_0 operator='reduce' (function=InlineFunction | function=ImplicitInlineFunction)?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ReduceOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ReverseOperation
	 *     RosettaCalcExpressionWithAsKey returns ReverseOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ReverseOperation
	 *     RosettaCalcExpression returns ReverseOperation
	 *     ThenOperation returns ReverseOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcOr returns ReverseOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcAnd returns ReverseOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcEquality returns ReverseOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcComparison returns ReverseOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcAdditive returns ReverseOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcMultiplicative returns ReverseOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcBinary returns ReverseOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ReverseOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ReverseOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ReverseOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ReverseOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ReverseOperation
	 *     UnaryOperation returns ReverseOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ReverseOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ReverseOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ReverseOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ReverseOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ReverseOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ReverseOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ReverseOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ReverseOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ReverseOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ReverseOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ReverseOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ReverseOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ReverseOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ReverseOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ReverseOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ReverseOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ReverseOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ReverseOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ReverseOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ReverseOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ReverseOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ReverseOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ReverseOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ReverseOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ReverseOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ReverseOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ReverseOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ReverseOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ReverseOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ReverseOperation
	 *     RosettaCalcPrimary returns ReverseOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ReverseOperation_0_1_0_0_8_0 operator='reverse') | 
	 *         operator='reverse' | 
	 *         (argument=UnaryOperation_ReverseOperation_1_1_0_0_8_0 operator='reverse')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ReverseOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaAbsentExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaAbsentExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcExpression returns RosettaAbsentExpression
	 *     ThenOperation returns RosettaAbsentExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcOr returns RosettaAbsentExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcAnd returns RosettaAbsentExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcEquality returns RosettaAbsentExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcComparison returns RosettaAbsentExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcAdditive returns RosettaAbsentExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcMultiplicative returns RosettaAbsentExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcBinary returns RosettaAbsentExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaAbsentExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaAbsentExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaAbsentExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaAbsentExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaAbsentExpression
	 *     UnaryOperation returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaAbsentExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaAbsentExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaAbsentExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaAbsentExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaAbsentExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaAbsentExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaAbsentExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaAbsentExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaAbsentExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaAbsentExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaAbsentExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaAbsentExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaAbsentExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaAbsentExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaAbsentExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaAbsentExpression
	 *     RosettaCalcPrimary returns RosettaAbsentExpression
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_RosettaAbsentExpression_0_1_0_0_3_0 operator='absent') | 
	 *         operator='absent' | 
	 *         (argument=UnaryOperation_RosettaAbsentExpression_1_1_0_0_3_0 operator='absent')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, RosettaAbsentExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaCountOperation
	 *     RosettaCalcExpressionWithAsKey returns RosettaCountOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaCountOperation
	 *     RosettaCalcExpression returns RosettaCountOperation
	 *     ThenOperation returns RosettaCountOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcOr returns RosettaCountOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcAnd returns RosettaCountOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcEquality returns RosettaCountOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcComparison returns RosettaCountOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcAdditive returns RosettaCountOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcMultiplicative returns RosettaCountOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcBinary returns RosettaCountOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaCountOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaCountOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaCountOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaCountOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaCountOperation
	 *     UnaryOperation returns RosettaCountOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaCountOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaCountOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaCountOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaCountOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaCountOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaCountOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaCountOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaCountOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaCountOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaCountOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaCountOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaCountOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaCountOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaCountOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaCountOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaCountOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaCountOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaCountOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaCountOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaCountOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaCountOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaCountOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaCountOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaCountOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaCountOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaCountOperation
	 *     RosettaCalcPrimary returns RosettaCountOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_RosettaCountOperation_0_1_0_0_5_0 operator='count') | 
	 *         operator='count' | 
	 *         (argument=UnaryOperation_RosettaCountOperation_1_1_0_0_5_0 operator='count')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, RosettaCountOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaDeepFeatureCall
	 *     RosettaCalcExpressionWithAsKey returns RosettaDeepFeatureCall
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcExpression returns RosettaDeepFeatureCall
	 *     ThenOperation returns RosettaDeepFeatureCall
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcOr returns RosettaDeepFeatureCall
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcAnd returns RosettaDeepFeatureCall
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcEquality returns RosettaDeepFeatureCall
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcComparison returns RosettaDeepFeatureCall
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcAdditive returns RosettaDeepFeatureCall
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcMultiplicative returns RosettaDeepFeatureCall
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcBinary returns RosettaDeepFeatureCall
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaDeepFeatureCall
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaDeepFeatureCall
	 *     RosettaCalcPrimary returns RosettaDeepFeatureCall
	 *
	 * Constraint:
	 *     (
	 *         (receiver=UnaryOperation_RosettaDeepFeatureCall_0_1_0_0_1_0 feature=[Attribute|ValidID]?) | 
	 *         (receiver=UnaryOperation_RosettaDeepFeatureCall_1_1_0_0_1_0 feature=[Attribute|ValidID]?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, RosettaDeepFeatureCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaExistsExpression
	 *     RosettaCalcExpressionWithAsKey returns RosettaExistsExpression
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaExistsExpression
	 *     RosettaCalcExpression returns RosettaExistsExpression
	 *     ThenOperation returns RosettaExistsExpression
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcOr returns RosettaExistsExpression
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcAnd returns RosettaExistsExpression
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcEquality returns RosettaExistsExpression
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcComparison returns RosettaExistsExpression
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcAdditive returns RosettaExistsExpression
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcMultiplicative returns RosettaExistsExpression
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcBinary returns RosettaExistsExpression
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaExistsExpression
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaExistsExpression
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaExistsExpression
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaExistsExpression
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaExistsExpression
	 *     UnaryOperation returns RosettaExistsExpression
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaExistsExpression
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaExistsExpression
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaExistsExpression
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaExistsExpression
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaExistsExpression
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaExistsExpression
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaExistsExpression
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaExistsExpression
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaExistsExpression
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaExistsExpression
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaExistsExpression
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaExistsExpression
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaExistsExpression
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaExistsExpression
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaExistsExpression
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaExistsExpression
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaExistsExpression
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaExistsExpression
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaExistsExpression
	 *     RosettaCalcPrimary returns RosettaExistsExpression
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_RosettaExistsExpression_0_1_0_0_2_0 modifier=ExistsModifier? operator='exists') | 
	 *         (modifier=ExistsModifier? operator='exists') | 
	 *         (argument=UnaryOperation_RosettaExistsExpression_1_1_0_0_2_0 modifier=ExistsModifier? operator='exists')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, RosettaExistsExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaFeatureCall
	 *     RosettaCalcExpressionWithAsKey returns RosettaFeatureCall
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaFeatureCall
	 *     RosettaCalcExpression returns RosettaFeatureCall
	 *     ThenOperation returns RosettaFeatureCall
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcOr returns RosettaFeatureCall
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcAnd returns RosettaFeatureCall
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcEquality returns RosettaFeatureCall
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcComparison returns RosettaFeatureCall
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcAdditive returns RosettaFeatureCall
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcMultiplicative returns RosettaFeatureCall
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcBinary returns RosettaFeatureCall
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaFeatureCall
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaFeatureCall
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaFeatureCall
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaFeatureCall
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaFeatureCall
	 *     UnaryOperation returns RosettaFeatureCall
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaFeatureCall
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaFeatureCall
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaFeatureCall
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaFeatureCall
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaFeatureCall
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaFeatureCall
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaFeatureCall
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaFeatureCall
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaFeatureCall
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaFeatureCall
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaFeatureCall
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaFeatureCall
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaFeatureCall
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaFeatureCall
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaFeatureCall
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaFeatureCall
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaFeatureCall
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaFeatureCall
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaFeatureCall
	 *     RosettaCalcPrimary returns RosettaFeatureCall
	 *
	 * Constraint:
	 *     (
	 *         (receiver=UnaryOperation_RosettaFeatureCall_0_1_0_0_0_0 feature=[RosettaFeature|ValidID]?) | 
	 *         (receiver=UnaryOperation_RosettaFeatureCall_1_1_0_0_0_0 feature=[RosettaFeature|ValidID]?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, RosettaFeatureCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns RosettaOnlyElement
	 *     RosettaCalcExpressionWithAsKey returns RosettaOnlyElement
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns RosettaOnlyElement
	 *     RosettaCalcExpression returns RosettaOnlyElement
	 *     ThenOperation returns RosettaOnlyElement
	 *     ThenOperation.ThenOperation_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcOr returns RosettaOnlyElement
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcAnd returns RosettaOnlyElement
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcEquality returns RosettaOnlyElement
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcComparison returns RosettaOnlyElement
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcAdditive returns RosettaOnlyElement
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcMultiplicative returns RosettaOnlyElement
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcBinary returns RosettaOnlyElement
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns RosettaOnlyElement
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns RosettaOnlyElement
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns RosettaOnlyElement
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns RosettaOnlyElement
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns RosettaOnlyElement
	 *     UnaryOperation returns RosettaOnlyElement
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns RosettaOnlyElement
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns RosettaOnlyElement
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns RosettaOnlyElement
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns RosettaOnlyElement
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns RosettaOnlyElement
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns RosettaOnlyElement
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns RosettaOnlyElement
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns RosettaOnlyElement
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns RosettaOnlyElement
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns RosettaOnlyElement
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns RosettaOnlyElement
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns RosettaOnlyElement
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns RosettaOnlyElement
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns RosettaOnlyElement
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns RosettaOnlyElement
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns RosettaOnlyElement
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns RosettaOnlyElement
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns RosettaOnlyElement
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns RosettaOnlyElement
	 *     RosettaCalcPrimary returns RosettaOnlyElement
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_RosettaOnlyElement_0_1_0_0_4_0 operator='only-element') | 
	 *         operator='only-element' | 
	 *         (argument=UnaryOperation_RosettaOnlyElement_1_1_0_0_4_0 operator='only-element')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, RosettaOnlyElement semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns SortOperation
	 *     RosettaCalcExpressionWithAsKey returns SortOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns SortOperation
	 *     RosettaCalcExpression returns SortOperation
	 *     ThenOperation returns SortOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns SortOperation
	 *     RosettaCalcOr returns SortOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns SortOperation
	 *     RosettaCalcAnd returns SortOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns SortOperation
	 *     RosettaCalcEquality returns SortOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns SortOperation
	 *     RosettaCalcComparison returns SortOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns SortOperation
	 *     RosettaCalcAdditive returns SortOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns SortOperation
	 *     RosettaCalcMultiplicative returns SortOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns SortOperation
	 *     RosettaCalcBinary returns SortOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns SortOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns SortOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns SortOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns SortOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns SortOperation
	 *     UnaryOperation returns SortOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns SortOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns SortOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns SortOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns SortOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns SortOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns SortOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns SortOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns SortOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns SortOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns SortOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns SortOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns SortOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns SortOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns SortOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns SortOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns SortOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns SortOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns SortOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns SortOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns SortOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns SortOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns SortOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns SortOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns SortOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns SortOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns SortOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns SortOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns SortOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns SortOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns SortOperation
	 *     RosettaCalcPrimary returns SortOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_SortOperation_0_1_1_0_0_0_0 operator='sort' function=InlineFunction?) | 
	 *         (operator='sort' function=InlineFunction?) | 
	 *         (argument=UnaryOperation_SortOperation_1_1_1_0_0_0_0 operator='sort' function=InlineFunction?)
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, SortOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns SumOperation
	 *     RosettaCalcExpressionWithAsKey returns SumOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns SumOperation
	 *     RosettaCalcExpression returns SumOperation
	 *     ThenOperation returns SumOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns SumOperation
	 *     RosettaCalcOr returns SumOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns SumOperation
	 *     RosettaCalcAnd returns SumOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns SumOperation
	 *     RosettaCalcEquality returns SumOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns SumOperation
	 *     RosettaCalcComparison returns SumOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns SumOperation
	 *     RosettaCalcAdditive returns SumOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns SumOperation
	 *     RosettaCalcMultiplicative returns SumOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns SumOperation
	 *     RosettaCalcBinary returns SumOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns SumOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns SumOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns SumOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns SumOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns SumOperation
	 *     UnaryOperation returns SumOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns SumOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns SumOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns SumOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns SumOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns SumOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns SumOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns SumOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns SumOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns SumOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns SumOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns SumOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns SumOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns SumOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns SumOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns SumOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns SumOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns SumOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns SumOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns SumOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns SumOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns SumOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns SumOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns SumOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns SumOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns SumOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns SumOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns SumOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns SumOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns SumOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns SumOperation
	 *     RosettaCalcPrimary returns SumOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_SumOperation_0_1_0_0_11_0 operator='sum') | 
	 *         operator='sum' | 
	 *         (argument=UnaryOperation_SumOperation_1_1_0_0_11_0 operator='sum')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, SumOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns SwitchOperation
	 *     RosettaCalcExpressionWithAsKey returns SwitchOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns SwitchOperation
	 *     RosettaCalcExpression returns SwitchOperation
	 *     ThenOperation returns SwitchOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcOr returns SwitchOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcAnd returns SwitchOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcEquality returns SwitchOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcComparison returns SwitchOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcAdditive returns SwitchOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcMultiplicative returns SwitchOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcBinary returns SwitchOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns SwitchOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns SwitchOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns SwitchOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns SwitchOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns SwitchOperation
	 *     UnaryOperation returns SwitchOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns SwitchOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns SwitchOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns SwitchOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns SwitchOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns SwitchOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns SwitchOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns SwitchOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns SwitchOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns SwitchOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns SwitchOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns SwitchOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns SwitchOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns SwitchOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns SwitchOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns SwitchOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns SwitchOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns SwitchOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns SwitchOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns SwitchOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns SwitchOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns SwitchOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns SwitchOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns SwitchOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns SwitchOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns SwitchOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns SwitchOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns SwitchOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns SwitchOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns SwitchOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns SwitchOperation
	 *     RosettaCalcPrimary returns SwitchOperation
	 *
	 * Constraint:
	 *     (
	 *         (
	 *             argument=UnaryOperation_SwitchOperation_0_1_0_0_23_0 
	 *             operator='switch' 
	 *             values+=CaseStatement 
	 *             values+=CaseStatement* 
	 *             default=CaseDefaultStatement?
	 *         ) | 
	 *         (operator='switch' values+=CaseStatement values+=CaseStatement* default=CaseDefaultStatement?) | 
	 *         (
	 *             argument=UnaryOperation_SwitchOperation_1_1_0_0_23_0 
	 *             operator='switch' 
	 *             values+=CaseStatement 
	 *             values+=CaseStatement* 
	 *             default=CaseDefaultStatement?
	 *         )
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, SwitchOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToDateOperation
	 *     RosettaCalcExpressionWithAsKey returns ToDateOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToDateOperation
	 *     RosettaCalcExpression returns ToDateOperation
	 *     ThenOperation returns ToDateOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcOr returns ToDateOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcAnd returns ToDateOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcEquality returns ToDateOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcComparison returns ToDateOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcAdditive returns ToDateOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcMultiplicative returns ToDateOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcBinary returns ToDateOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToDateOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToDateOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToDateOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToDateOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToDateOperation
	 *     UnaryOperation returns ToDateOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToDateOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToDateOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToDateOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToDateOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToDateOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToDateOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToDateOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToDateOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToDateOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToDateOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToDateOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToDateOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToDateOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToDateOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToDateOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToDateOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToDateOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToDateOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToDateOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToDateOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToDateOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToDateOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToDateOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToDateOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToDateOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToDateOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToDateOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToDateOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToDateOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToDateOperation
	 *     RosettaCalcPrimary returns ToDateOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToDateOperation_0_1_0_0_19_0 operator='to-date') | 
	 *         operator='to-date' | 
	 *         (argument=UnaryOperation_ToDateOperation_1_1_0_0_19_0 operator='to-date')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToDateOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToDateTimeOperation
	 *     RosettaCalcExpressionWithAsKey returns ToDateTimeOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToDateTimeOperation
	 *     RosettaCalcExpression returns ToDateTimeOperation
	 *     ThenOperation returns ToDateTimeOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcOr returns ToDateTimeOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcAnd returns ToDateTimeOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcEquality returns ToDateTimeOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcComparison returns ToDateTimeOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcAdditive returns ToDateTimeOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcMultiplicative returns ToDateTimeOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcBinary returns ToDateTimeOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToDateTimeOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToDateTimeOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToDateTimeOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToDateTimeOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToDateTimeOperation
	 *     UnaryOperation returns ToDateTimeOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToDateTimeOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToDateTimeOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToDateTimeOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToDateTimeOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToDateTimeOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToDateTimeOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToDateTimeOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToDateTimeOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToDateTimeOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToDateTimeOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToDateTimeOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToDateTimeOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToDateTimeOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToDateTimeOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToDateTimeOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToDateTimeOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToDateTimeOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToDateTimeOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToDateTimeOperation
	 *     RosettaCalcPrimary returns ToDateTimeOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToDateTimeOperation_0_1_0_0_20_0 operator='to-date-time') | 
	 *         operator='to-date-time' | 
	 *         (argument=UnaryOperation_ToDateTimeOperation_1_1_0_0_20_0 operator='to-date-time')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToDateTimeOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToEnumOperation
	 *     RosettaCalcExpressionWithAsKey returns ToEnumOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToEnumOperation
	 *     RosettaCalcExpression returns ToEnumOperation
	 *     ThenOperation returns ToEnumOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcOr returns ToEnumOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcAnd returns ToEnumOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcEquality returns ToEnumOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcComparison returns ToEnumOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcAdditive returns ToEnumOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcMultiplicative returns ToEnumOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcBinary returns ToEnumOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToEnumOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToEnumOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToEnumOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToEnumOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToEnumOperation
	 *     UnaryOperation returns ToEnumOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToEnumOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToEnumOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToEnumOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToEnumOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToEnumOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToEnumOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToEnumOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToEnumOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToEnumOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToEnumOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToEnumOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToEnumOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToEnumOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToEnumOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToEnumOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToEnumOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToEnumOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToEnumOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToEnumOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToEnumOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToEnumOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToEnumOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToEnumOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToEnumOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToEnumOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToEnumOperation
	 *     RosettaCalcPrimary returns ToEnumOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToEnumOperation_0_1_0_0_18_0 operator='to-enum' enumeration=[RosettaEnumeration|QualifiedName]) | 
	 *         (operator='to-enum' enumeration=[RosettaEnumeration|QualifiedName]) | 
	 *         (argument=UnaryOperation_ToEnumOperation_1_1_0_0_18_0 operator='to-enum' enumeration=[RosettaEnumeration|QualifiedName])
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToEnumOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToIntOperation
	 *     RosettaCalcExpressionWithAsKey returns ToIntOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToIntOperation
	 *     RosettaCalcExpression returns ToIntOperation
	 *     ThenOperation returns ToIntOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcOr returns ToIntOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcAnd returns ToIntOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcEquality returns ToIntOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcComparison returns ToIntOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcAdditive returns ToIntOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcMultiplicative returns ToIntOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcBinary returns ToIntOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToIntOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToIntOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToIntOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToIntOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToIntOperation
	 *     UnaryOperation returns ToIntOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToIntOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToIntOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToIntOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToIntOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToIntOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToIntOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToIntOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToIntOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToIntOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToIntOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToIntOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToIntOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToIntOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToIntOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToIntOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToIntOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToIntOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToIntOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToIntOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToIntOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToIntOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToIntOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToIntOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToIntOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToIntOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToIntOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToIntOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToIntOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToIntOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToIntOperation
	 *     RosettaCalcPrimary returns ToIntOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToIntOperation_0_1_0_0_16_0 operator='to-int') | 
	 *         operator='to-int' | 
	 *         (argument=UnaryOperation_ToIntOperation_1_1_0_0_16_0 operator='to-int')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToIntOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToNumberOperation
	 *     RosettaCalcExpressionWithAsKey returns ToNumberOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToNumberOperation
	 *     RosettaCalcExpression returns ToNumberOperation
	 *     ThenOperation returns ToNumberOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcOr returns ToNumberOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcAnd returns ToNumberOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcEquality returns ToNumberOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcComparison returns ToNumberOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcAdditive returns ToNumberOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcMultiplicative returns ToNumberOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcBinary returns ToNumberOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToNumberOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToNumberOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToNumberOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToNumberOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToNumberOperation
	 *     UnaryOperation returns ToNumberOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToNumberOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToNumberOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToNumberOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToNumberOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToNumberOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToNumberOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToNumberOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToNumberOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToNumberOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToNumberOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToNumberOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToNumberOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToNumberOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToNumberOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToNumberOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToNumberOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToNumberOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToNumberOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToNumberOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToNumberOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToNumberOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToNumberOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToNumberOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToNumberOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToNumberOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToNumberOperation
	 *     RosettaCalcPrimary returns ToNumberOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToNumberOperation_0_1_0_0_15_0 operator='to-number') | 
	 *         operator='to-number' | 
	 *         (argument=UnaryOperation_ToNumberOperation_1_1_0_0_15_0 operator='to-number')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToNumberOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToStringOperation
	 *     RosettaCalcExpressionWithAsKey returns ToStringOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToStringOperation
	 *     RosettaCalcExpression returns ToStringOperation
	 *     ThenOperation returns ToStringOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcOr returns ToStringOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcAnd returns ToStringOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcEquality returns ToStringOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcComparison returns ToStringOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcAdditive returns ToStringOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcMultiplicative returns ToStringOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcBinary returns ToStringOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToStringOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToStringOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToStringOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToStringOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToStringOperation
	 *     UnaryOperation returns ToStringOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToStringOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToStringOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToStringOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToStringOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToStringOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToStringOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToStringOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToStringOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToStringOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToStringOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToStringOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToStringOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToStringOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToStringOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToStringOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToStringOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToStringOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToStringOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToStringOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToStringOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToStringOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToStringOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToStringOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToStringOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToStringOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToStringOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToStringOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToStringOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToStringOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToStringOperation
	 *     RosettaCalcPrimary returns ToStringOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToStringOperation_0_1_0_0_14_0 operator='to-string') | 
	 *         operator='to-string' | 
	 *         (argument=UnaryOperation_ToStringOperation_1_1_0_0_14_0 operator='to-string')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToStringOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToTimeOperation
	 *     RosettaCalcExpressionWithAsKey returns ToTimeOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToTimeOperation
	 *     RosettaCalcExpression returns ToTimeOperation
	 *     ThenOperation returns ToTimeOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcOr returns ToTimeOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcAnd returns ToTimeOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcEquality returns ToTimeOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcComparison returns ToTimeOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcAdditive returns ToTimeOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcMultiplicative returns ToTimeOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcBinary returns ToTimeOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToTimeOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToTimeOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToTimeOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToTimeOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToTimeOperation
	 *     UnaryOperation returns ToTimeOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToTimeOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToTimeOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToTimeOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToTimeOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToTimeOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToTimeOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToTimeOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToTimeOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToTimeOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToTimeOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToTimeOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToTimeOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToTimeOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToTimeOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToTimeOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToTimeOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToTimeOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToTimeOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToTimeOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToTimeOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToTimeOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToTimeOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToTimeOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToTimeOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToTimeOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToTimeOperation
	 *     RosettaCalcPrimary returns ToTimeOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToTimeOperation_0_1_0_0_17_0 operator='to-time') | 
	 *         operator='to-time' | 
	 *         (argument=UnaryOperation_ToTimeOperation_1_1_0_0_17_0 operator='to-time')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToTimeOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * <pre>
	 * Contexts:
	 *     CaseDefaultStatement returns ToZonedDateTimeOperation
	 *     RosettaCalcExpressionWithAsKey returns ToZonedDateTimeOperation
	 *     RosettaCalcExpressionWithAsKey.AsKeyOperation_1_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcExpression returns ToZonedDateTimeOperation
	 *     ThenOperation returns ToZonedDateTimeOperation
	 *     ThenOperation.ThenOperation_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcOr returns ToZonedDateTimeOperation
	 *     RosettaCalcOr.LogicalOperation_0_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcAnd returns ToZonedDateTimeOperation
	 *     RosettaCalcAnd.LogicalOperation_0_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcEquality returns ToZonedDateTimeOperation
	 *     RosettaCalcEquality.EqualityOperation_0_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcComparison returns ToZonedDateTimeOperation
	 *     RosettaCalcComparison.ComparisonOperation_0_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcAdditive returns ToZonedDateTimeOperation
	 *     RosettaCalcAdditive.ArithmeticOperation_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcMultiplicative returns ToZonedDateTimeOperation
	 *     RosettaCalcMultiplicative.ArithmeticOperation_0_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcBinary returns ToZonedDateTimeOperation
	 *     RosettaCalcBinary.RosettaContainsExpression_0_1_0_0_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcBinary.RosettaDisjointExpression_0_1_0_1_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcBinary.DefaultOperation_0_1_0_2_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcBinary.JoinOperation_0_1_0_3_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcBinary.WithMetaOperation_0_1_0_4_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaFeatureCall_0_1_0_0_0_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaDeepFeatureCall_0_1_0_0_1_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaExistsExpression_0_1_0_0_2_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaAbsentExpression_0_1_0_0_3_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaOnlyElement_0_1_0_0_4_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.RosettaCountOperation_0_1_0_0_5_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.FlattenOperation_0_1_0_0_6_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.DistinctOperation_0_1_0_0_7_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ReverseOperation_0_1_0_0_8_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.FirstOperation_0_1_0_0_9_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.LastOperation_0_1_0_0_10_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.SumOperation_0_1_0_0_11_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.OneOfOperation_0_1_0_0_12_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ChoiceOperation_0_1_0_0_13_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToStringOperation_0_1_0_0_14_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToNumberOperation_0_1_0_0_15_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToIntOperation_0_1_0_0_16_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToTimeOperation_0_1_0_0_17_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToEnumOperation_0_1_0_0_18_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToDateOperation_0_1_0_0_19_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToDateTimeOperation_0_1_0_0_20_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ToZonedDateTimeOperation_0_1_0_0_21_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.AsReferenceOperation_0_1_0_0_22_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.SwitchOperation_0_1_0_0_23_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.SortOperation_0_1_1_0_0_0_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.MinOperation_0_1_1_0_0_1_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.MaxOperation_0_1_1_0_0_2_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.ReduceOperation_0_1_2_0_0_0_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.FilterOperation_0_1_2_0_0_1_0 returns ToZonedDateTimeOperation
	 *     UnaryOperation.MapOperation_0_1_2_0_0_2_0 returns ToZonedDateTimeOperation
	 *     RosettaCalcPrimary returns ToZonedDateTimeOperation
	 *
	 * Constraint:
	 *     (
	 *         (argument=UnaryOperation_ToZonedDateTimeOperation_0_1_0_0_21_0 operator='to-zoned-date-time') | 
	 *         operator='to-zoned-date-time' | 
	 *         (argument=UnaryOperation_ToZonedDateTimeOperation_1_1_0_0_21_0 operator='to-zoned-date-time')
	 *     )
	 * </pre>
	 */
	protected void sequence_UnaryOperation(ISerializationContext context, ToZonedDateTimeOperation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
		
}