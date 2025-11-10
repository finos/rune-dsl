package com.regnosys.rosetta.types;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.expression.*;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.utils.OptionalUtil;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;
import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;

import java.math.BigInteger;
import java.util.*;

public class RosettaTypeProvider extends RosettaExpressionSwitch<RMetaAnnotatedType, Map<RosettaSymbol, RMetaAnnotatedType>> {

    @Inject
    private RosettaEcoreUtil extensions;
    @Inject
    private ImplicitVariableUtil implicitVariableUtil;
    @Inject
    private TypeSystem typeSystem;
    @Inject
    private TypeFactory typeFactory;
    @Inject
    private RBuiltinTypeService builtins;
    @Inject
    private RObjectFactory rObjectFactory;
    @Inject
    private ExpectedTypeProvider expectedTypeProvider;
    @Inject
    private AnnotationPathExpressionUtil annotationPathUtil;

    // Public API

    public RMetaAnnotatedType getRMetaAnnotatedType(RosettaExpression expression) {
        return safeRType(expression, new HashMap<>());
    }

    public RMetaAnnotatedType getRTypeOfFeature(RosettaFeature feature, EObject context) {
        return safeRType(feature, context, new HashMap<>());
    }

    public RMetaAnnotatedType getRTypeOfSymbol(RosettaSymbol symbol, EObject context) {
        return safeRType(symbol, context, new HashMap<>());
    }

    public RMetaAnnotatedType getRTypeOfSymbol(TypeParameter feature) {
        return getRTypeOfSymbol(feature, null);
    }

    public RMetaAnnotatedType getRTypeOfSymbol(AssignPathRoot feature) {
        return getRTypeOfSymbol(feature, null);
    }

    public RMetaAnnotatedType getRTypeOfSymbol(RosettaCallableWithArgs feature) {
        return getRTypeOfSymbol(feature, null);
    }

    public RType getRTypeOfAttributeReference(RosettaAttributeReferenceSegment seg) {
        if (seg instanceof RosettaAttributeReference attrRef) {
            return typeSystem.typeCallToRType(attrRef.getAttribute().getTypeCall());
        } else if (seg instanceof RosettaDataReference dataRef) {
            if (extensions.isResolved(dataRef.getData())) {
                return rObjectFactory.buildRDataType(dataRef.getData());
            } else {
                return builtins.NOTHING;
            }
        }
        return builtins.NOTHING;
    }

    public Iterable<? extends RosettaFeature> findFeaturesOfImplicitVariable(EObject context) {
        return extensions.allFeaturesExcludingEnumValues(typeOfImplicitVariable(context), context);
    }

    public RMetaAnnotatedType getRMetaAnnotatedType(AnnotationPathExpression expr) {
        return getRTypeOfSymbol(annotationPathUtil.getTargetAttribute(expr));
    }

    public List<RMetaAttribute> getRMetaAttributesOfSymbol(RosettaSymbol symbol) {
    	Set<RMetaAttribute> acc = new HashSet<>();
        if (symbol instanceof Attribute a) {
            if (a.isOverride()) {
                acc.addAll(getRMetaAttributesOfSymbol(extensions.getParentAttribute(a)));
                acc.addAll(getRMetaAttributes(a.getAnnotations())); 
            }
            
        	RosettaType attributeType = a.getTypeCall().getType();
        	if (attributeType instanceof Data data) {
        		acc.addAll(getRMetaAttributesOfType(data));
        	}
        }
        if (symbol instanceof Annotated ann) {
            acc.addAll(getRMetaAttributes(ann.getAnnotations()));
        }
        return new ArrayList<>(acc);
    }

    public List<RMetaAttribute> getRMetaAttributesOfFeature(RosettaFeature feature) {
        if (feature instanceof RosettaSymbol s) {
            return getRMetaAttributesOfSymbol(s);
        }
        if (feature instanceof Annotated ann) {
            return getRMetaAttributes(ann.getAnnotations());
        }
        return List.of();
    }

    public List<RMetaAttribute> getRMetaAttributesOfType(Data data) {
        Set<AnnotationRef> allAnnotations = new HashSet<>();
        Set<Data> visited = new HashSet<>();
        Data current = data;
        while (current != null && visited.add(current)) {
            allAnnotations.addAll(current.getAnnotations());
            Data superType = current.getSuperType();
            if (superType != null && !extensions.isResolved(superType)) {
                break;
            }
            current = superType;
        }
        return getRMetaAttributes(new ArrayList<>(allAnnotations));
    }

    public List<RMetaAttribute> getRMetaAttributes(List<AnnotationRef> annotations) {
        List<RMetaAttribute> res = new ArrayList<>();
        for (AnnotationRef a : annotations) {
            if (extensions.isResolved(a.getAnnotation())
                && "metadata".equals(a.getAnnotation().getName())
                && extensions.isResolved(a.getAttribute())) {
                RMetaAnnotatedType attrType = getRTypeOfSymbol(a.getAttribute());
                res.add(new RMetaAttribute(a.getAttribute().getName(), attrType.getRType()));
            }
        }
        return res;
    }

    public RMetaAnnotatedType typeOfImplicitVariable(EObject context) {
        return safeTypeOfImplicitVariable(context, new HashMap<>());
    }

    // Internal helpers

    private RMetaAnnotatedType safeRType(RosettaSymbol symbol, EObject context, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        if (!extensions.isResolved(symbol)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        RMetaAnnotatedType existing = cycleTracker.get(symbol);
        if (existing != null) {
            return existing;
        }
        cycleTracker.put(symbol, builtins.NOTHING_WITH_ANY_META);
        RMetaAnnotatedType result;
        if (symbol instanceof RosettaFeature f) {
            result = safeRType(f, context, cycleTracker);
        } else if (symbol instanceof RosettaParameter p) {
            result = RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(p.getTypeCall()));
        } else if (symbol instanceof ClosureParameter cp) {
            RosettaFunctionalOperation setOp = cp.getFunction() == null
                    ? null
                    : (RosettaFunctionalOperation) cp.getFunction().eContainer();
            if (setOp != null) {
                result = safeRType(setOp.getArgument(), cycleTracker);
            } else {
                result = builtins.NOTHING_WITH_ANY_META;
            }
        } else if (symbol instanceof RosettaEnumeration e) { // @Compat
            result = RMetaAnnotatedType.withMeta(rObjectFactory.buildREnumType(e),
                    getRMetaAttributesOfSymbol(symbol));
        } else if (symbol instanceof Function func) {
            Attribute out = func.getOutput();
            result = out != null ? safeRType((RosettaFeature) out, context, cycleTracker) : builtins.NOTHING_WITH_ANY_META;
        } else if (symbol instanceof RosettaRule rule) {
            RosettaExpression e = rule.getExpression();
            result = e != null ? safeRType(e, cycleTracker) : builtins.NOTHING_WITH_ANY_META;
        } else if (symbol instanceof RosettaExternalFunction extFunc) {
            result = RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(extFunc.getTypeCall()));
        } else if (symbol instanceof ShortcutDeclaration alias) {
            result = safeRType(alias.getExpression(), cycleTracker);
        } else if (symbol instanceof TypeParameter tp) {
            result = RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(tp.getTypeCall()));
        } else {
            result = builtins.NOTHING_WITH_ANY_META;
        }
        cycleTracker.put(symbol, result);
        return result;
    }

    private RMetaAnnotatedType safeRType(RosettaFeature feature, EObject context, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        if (!extensions.isResolved(feature)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        if (feature instanceof RosettaTypedFeature tf) {
            if (tf.getTypeCall() == null) {
                return builtins.NOTHING_WITH_ANY_META;
            }
            return RMetaAnnotatedType.withMeta(
                    typeSystem.typeCallToRType(tf.getTypeCall()),
                    getRMetaAttributesOfFeature(feature)
            );
        } else if (feature instanceof RosettaEnumValue) {
            if (context instanceof RosettaFeatureCall fc) {
                return safeRType(fc.getReceiver(), cycleTracker);
            } else {
                RMetaAnnotatedType fromContainer = expectedTypeProvider.getExpectedTypeFromContainer(context);
                return fromContainer != null ? fromContainer : builtins.NOTHING_WITH_ANY_META;
            }
        }
        return builtins.NOTHING_WITH_ANY_META;
    }

    private RMetaAnnotatedType safeRType(RosettaExpression expression, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        if (expression == null) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        return doSwitch(expression, cycleTracker);
    }

    private RMetaAnnotatedType safeTypeOfImplicitVariable(EObject context, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        Optional<? extends EObject> definingContainer = implicitVariableUtil.findContainerDefiningImplicitVariable(context);
        return definingContainer.map(it -> {
            if (it instanceof RosettaTypeWithConditions) {
                if (it instanceof Data data) {
                    return RMetaAnnotatedType.withNoMeta(rObjectFactory.buildRDataType(data));
                } else if (it instanceof RosettaTypeAlias alias) {
                    return RMetaAnnotatedType.withNoMeta(typeSystem.typeWithUnknownArgumentsToRType(alias));
                } else {
                    return builtins.NOTHING_WITH_ANY_META;
                }
            } else if (it instanceof RosettaFunctionalOperation fo) {
                return safeRType(fo.getArgument(), cycleTracker);
            } else if (it instanceof RosettaRule rule) {
                return RMetaAnnotatedType.withNoMeta(typeSystem.getRuleInputType(rule));
            } else if (it instanceof SwitchCaseOrDefault sc) {
                SwitchCaseGuard guard = sc.getGuard();
                if (guard != null) {
                    ChoiceOption choiceOption = guard.getChoiceOptionGuard();
                    if (choiceOption != null) {
                        return getRTypeOfSymbol(choiceOption, context);
                    }
                    Data data = guard.getDataGuard();
                    if (data != null) {
                        return RMetaAnnotatedType.withNoMeta(rObjectFactory.buildRDataType(data));
                    }
                }
            }
            return builtins.NOTHING_WITH_ANY_META;
        }).orElse(builtins.NOTHING_WITH_ANY_META);
    }

    // Switch overrides

    @Override
    protected RMetaAnnotatedType caseAbsentOperation(RosettaAbsentExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseAddOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RMetaAnnotatedType left = safeRType(expr.getLeft(), cycleTracker);
        RMetaAnnotatedType right = safeRType(expr.getRight(), cycleTracker);
        if (typeSystem.isSubtypeOf(left, builtins.NOTHING_WITH_ANY_META)) {
            return builtins.NOTHING_WITH_ANY_META;
        } else if (typeSystem.isSubtypeOf(left, builtins.DATE_WITH_NO_META)) {
            return builtins.DATE_TIME_WITH_NO_META;
        } else if (typeSystem.isSubtypeOf(left, builtins.UNCONSTRAINED_STRING_WITH_NO_META)) {
            return typeSystem.keepTypeAliasIfPossibleWithAnyMeta(left.getRType(), right.getRType(), (l, r) -> {
                if (l instanceof RStringType s1 && r instanceof RStringType s2) {
                    var newInterval = s1.getInterval().add(s2.getInterval());
                    return new RStringType(newInterval, Optional.empty());
                }
                return builtins.NOTHING;
            });
        } else if (typeSystem.isSubtypeOf(left, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
            return typeSystem.keepTypeAliasIfPossibleWithAnyMeta(left.getRType(), right.getRType(), (l, r) -> {
                if (l instanceof RNumberType n1 && r instanceof RNumberType n2) {
                    var newFractional = OptionalUtil.zipWith(n1.getFractionalDigits(), n2.getFractionalDigits(), Math::max);
                    var newInterval = n1.getInterval().add(n2.getInterval());
                    return new RNumberType(Optional.empty(), newFractional, newInterval, Optional.empty());
                }
                return builtins.NOTHING;
            });
        } else {
            return builtins.NOTHING_WITH_ANY_META;
        }
    }

    @Override
    protected RMetaAnnotatedType caseAndOperation(LogicalOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseAsKeyOperation(AsKeyOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseBooleanLiteral(RosettaBooleanLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseChoiceOperation(ChoiceOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseConditionalExpression(RosettaConditionalExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RMetaAnnotatedType ifT = safeRType(expr.getIfthen(), cycleTracker);
        RMetaAnnotatedType elseT = safeRType(expr.getElsethen(), cycleTracker);
        RMetaAnnotatedType joined = typeSystem.joinMetaAnnotatedTypes(ifT, elseT);
        if (typeSystem.isSubtypeOf(builtins.ANY_WITH_NO_META, joined)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        return joined;
    }

    @Override
    protected RMetaAnnotatedType caseContainsOperation(RosettaContainsExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseDefaultOperation(DefaultOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RMetaAnnotatedType left = safeRType(expr.getLeft(), cycleTracker);
        RMetaAnnotatedType right = safeRType(expr.getRight(), cycleTracker);
        RMetaAnnotatedType result = typeSystem.joinMetaAnnotatedTypes(left, right);
        if (typeSystem.isSubtypeOf(builtins.ANY_WITH_NO_META, result)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        return result;
    }

    @Override
    protected RMetaAnnotatedType caseCountOperation(RosettaCountOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return RMetaAnnotatedType.withNoMeta(typeFactory.constrainedInt(Optional.empty(), Optional.of(BigInteger.ZERO), Optional.empty()));
    }

    @Override
    protected RMetaAnnotatedType caseDisjointOperation(RosettaDisjointExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseDistinctOperation(DistinctOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseDivideOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.UNCONSTRAINED_NUMBER_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseEqualsOperation(EqualityOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseExistsOperation(RosettaExistsExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseFeatureCall(RosettaFeatureCall expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RosettaFeature feature = expr.getFeature();
        if (!extensions.isResolved(feature)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        if (feature instanceof RosettaEnumValue) {
            return safeRType(expr.getReceiver(), cycleTracker);
        }
        return safeRType(feature, expr, cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseDeepFeatureCall(RosettaDeepFeatureCall expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RosettaFeature feature = expr.getFeature();
        if (!extensions.isResolved(feature)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        return safeRType(feature, expr, cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseFilterOperation(FilterOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseFirstOperation(FirstOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseFlattenOperation(FlattenOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseGreaterThanOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseGreaterThanOrEqualOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseImplicitVariable(RosettaImplicitVariable expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeTypeOfImplicitVariable(expr, cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseIntLiteral(RosettaIntLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        int len = expr.getValue().signum() >= 0 ? expr.getValue().toString().length() : expr.getValue().toString().length() - 1;
        return RMetaAnnotatedType.withNoMeta(typeFactory.constrainedInt(len, expr.getValue(), expr.getValue()));
    }

    @Override
    protected RMetaAnnotatedType caseJoinOperation(JoinOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseLastOperation(LastOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseLessThanOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseLessThanOrEqualOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseListLiteral(ListLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        List<RMetaAnnotatedType> types = new ArrayList<>();
        for (RosettaExpression e : expr.getElements()) {
            RMetaAnnotatedType t = safeRType(e, cycleTracker);
            if (t != null) types.add(t);
        }
        RMetaAnnotatedType joined = typeSystem.joinMetaAnnotatedTypes(types);
        if (typeSystem.isSubtypeOf(builtins.ANY_WITH_NO_META, joined)) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        return joined;
    }

    @Override
    protected RMetaAnnotatedType caseMapOperation(MapOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RosettaExpression body = expr.getFunction() != null ? expr.getFunction().getBody() : null;
        return body != null ? safeRType(body, cycleTracker) : builtins.NOTHING_WITH_ANY_META;
    }

    @Override
    protected RMetaAnnotatedType caseMaxOperation(MaxOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseMinOperation(MinOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseMultiplyOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RMetaAnnotatedType left = safeRType(expr.getLeft(), cycleTracker);
        RMetaAnnotatedType right = safeRType(expr.getRight(), cycleTracker);
        return typeSystem.keepTypeAliasIfPossibleWithAnyMeta(left.getRType(), right.getRType(), (l, r) -> {
            if (l instanceof RNumberType n1 && r instanceof RNumberType n2) {
                var newFractional = OptionalUtil.zipWith(n1.getFractionalDigits(), n2.getFractionalDigits(), Integer::sum);
                var newInterval = n1.getInterval().multiply(n2.getInterval());
                return new RNumberType(Optional.empty(), newFractional, newInterval, Optional.empty());
            }
            return builtins.NOTHING;
        });
    }

    @Override
    protected RMetaAnnotatedType caseNotEqualsOperation(EqualityOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseNumberLiteral(RosettaNumberLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        if (expr.getValue() == null) {
            return builtins.NOTHING_WITH_ANY_META;
        }
        String plain = expr.getValue().toPlainString();
        int digits = plain.replaceAll("[.\\-]", "").length();
        int scale = Math.max(0, expr.getValue().scale());
        return RMetaAnnotatedType.withNoMeta(typeFactory.constrainedNumber(digits, scale, expr.getValue(), expr.getValue()));
    }

    @Override
    protected RMetaAnnotatedType caseOneOfOperation(OneOfOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseOnlyElementOperation(RosettaOnlyElement expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseOnlyExists(RosettaOnlyExistsExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseOrOperation(LogicalOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.BOOLEAN_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseReduceOperation(ReduceOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RosettaExpression body = expr.getFunction() != null ? expr.getFunction().getBody() : null;
        return body != null ? safeRType(body, cycleTracker) : builtins.NOTHING_WITH_ANY_META;
    }

    @Override
    protected RMetaAnnotatedType caseReverseOperation(ReverseOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseSortOperation(SortOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseStringLiteral(RosettaStringLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return RMetaAnnotatedType.withNoMeta(typeFactory.constrainedString(expr.getValue().length(), expr.getValue().length()));
    }

    @Override
    protected RMetaAnnotatedType caseSubtractOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RMetaAnnotatedType left = safeRType(expr.getLeft(), cycleTracker);
        RMetaAnnotatedType right = safeRType(expr.getRight(), cycleTracker);
        if (typeSystem.isSubtypeOf(left, builtins.NOTHING_WITH_ANY_META)) {
            return builtins.NOTHING_WITH_ANY_META;
        } else if (typeSystem.isSubtypeOf(left, builtins.DATE_WITH_NO_META)) {
            return builtins.UNCONSTRAINED_INT_WITH_NO_META;
        } else if (typeSystem.isSubtypeOf(left, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
            return typeSystem.keepTypeAliasIfPossibleWithAnyMeta(left.getRType(), right.getRType(), (l, r) -> {
                if (l instanceof RNumberType n1 && r instanceof RNumberType n2) {
                    var newFractional = OptionalUtil.zipWith(n1.getFractionalDigits(), n2.getFractionalDigits(), Math::max);
                    var newInterval = n1.getInterval().subtract(n2.getInterval());
                    return new RNumberType(Optional.empty(), newFractional, newInterval, Optional.empty());
                }
                return builtins.NOTHING;
            });
        } else {
            return builtins.NOTHING_WITH_ANY_META;
        }
    }

    @Override
    protected RMetaAnnotatedType caseSumOperation(SumOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return safeRType(expr.getArgument(), cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseSymbolReference(RosettaSymbolReference expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        if (expr.getSymbol() instanceof RosettaExternalFunction fun) {
            RMetaAnnotatedType returnType = safeRType(fun, expr, cycleTracker);
            List<RMetaAnnotatedType> argTypes = new ArrayList<>();
            for (RosettaExpression arg : expr.getArgs()) {
                argTypes.add(safeRType(arg, cycleTracker));
            }
            boolean allSub = argTypes.stream().allMatch(a -> typeSystem.isSubtypeOf(a, returnType));
            if (allSub) {
                return typeSystem.joinMetaAnnotatedTypes(argTypes);
            }
            return returnType;
        }
        return safeRType(expr.getSymbol(), expr, cycleTracker);
    }

    @Override
    protected RMetaAnnotatedType caseThenOperation(ThenOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        RosettaExpression body = expr.getFunction() != null ? expr.getFunction().getBody() : null;
        return body != null ? safeRType(body, cycleTracker) : builtins.NOTHING_WITH_ANY_META;
    }

    @Override
    protected RMetaAnnotatedType caseToEnumOperation(ToEnumOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return RMetaAnnotatedType.withNoMeta(rObjectFactory.buildREnumType(expr.getEnumeration()));
    }

    @Override
    protected RMetaAnnotatedType caseToIntOperation(ToIntOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.UNCONSTRAINED_INT_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseToNumberOperation(ToNumberOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.UNCONSTRAINED_NUMBER_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseToStringOperation(ToStringOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseToTimeOperation(ToTimeOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.TIME_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseConstructorExpression(RosettaConstructorExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(expr.getTypeCall()));
    }

    @Override
    protected RMetaAnnotatedType caseToDateOperation(ToDateOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.DATE_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseToDateTimeOperation(ToDateTimeOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.DATE_TIME_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        return builtins.ZONED_DATE_TIME_WITH_NO_META;
    }

    @Override
    protected RMetaAnnotatedType caseSwitchOperation(SwitchOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        List<RMetaAnnotatedType> caseTypes = new ArrayList<>();
        for (SwitchCaseOrDefault c : expr.getCases()) {
            caseTypes.add(safeRType(c.getExpression(), cycleTracker));
        }
        return typeSystem.joinMetaAnnotatedTypes(caseTypes);
    }

    @Override
    protected RMetaAnnotatedType caseWithMetaOperation(WithMetaOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
        var argType = safeRType(expr.getArgument(), cycleTracker);

        var newMetaAttributes = expr.getEntries().stream()
                .map(WithMetaEntry::getKey)
                .filter(f -> extensions.isResolved(f))
                .map(f -> new RMetaAttribute(f.getName(), getRTypeOfFeature(f, null).getRType()))
                .toList();
        
        return argType.addMeta(newMetaAttributes);
    }
}
