package com.regnosys.rosetta.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaParameter;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.CanHandleListOfLists;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ClosureParameter;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.DefaultOperation;
import com.regnosys.rosetta.rosetta.expression.DistinctOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
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
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSuperCall;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.rosetta.expression.SortOperation;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.SwitchCaseOrDefault;
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
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;

public class CardinalityProvider extends RosettaExpressionSwitch<Boolean, Map<RosettaSymbol, Boolean>> {
	
	static Logger LOGGER = LoggerFactory.getLogger(CardinalityProvider.class);
			
	@Inject
	private ImplicitVariableUtil implicitVariableUtil;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	
	public boolean isMulti(RosettaExpression expr) {
		return safeIsMulti(expr, new HashMap<>());
	}
	private boolean safeIsMulti(RosettaExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		if (expr == null) {
			return false;
		}
		return doSwitch(expr, cycleTracker);
	}
	public boolean isSymbolMulti(RosettaSymbol symbol) {
		return safeIsSymbolMulti(symbol, new HashMap<>());
	}
	public boolean isFeatureMulti(RosettaFeature feature) {
		if (feature instanceof Attribute) {
            Attribute attribute = (Attribute) feature;
            return attribute.getCard() != null && attribute.getCard().isIsMany();
        }
        return false;
	}
	
	private boolean safeIsSymbolMulti(RosettaSymbol symbol, Map<RosettaSymbol, Boolean> cycleTracker) {
		if (!ecoreUtil.isResolved(symbol)) {
			return false;
		}
		if (cycleTracker.containsKey(symbol)) {
			Boolean existing = cycleTracker.get(symbol);
			if (existing == null) {
				// Cycle detected, assuming single cardinality
				return false;
			}
			return existing;
		}
		cycleTracker.put(symbol, null);
		
		boolean result;
	    if (symbol instanceof RosettaFeature) {
	        result = isFeatureMulti((RosettaFeature) symbol);
	    } else if (symbol instanceof RosettaParameter) {
	        result = false;
	    } else if (symbol instanceof ClosureParameter) {
	        result = safeIsClosureParameterMulti(((ClosureParameter) symbol).getFunction(), cycleTracker);
	    } else if (symbol instanceof RosettaEnumeration) { // @Compat: RosettaEnumeration should not be a RosettaSymbol.
	        result = false;
	    } else if (symbol instanceof Function) {
	        if (((Function) symbol).getOutput() != null) {
	            result = isFeatureMulti((RosettaFeature) ((Function) symbol).getOutput());
	        } else {
	            result = false;
	        }
	    } else if (symbol instanceof RosettaRule) {
	        if (((RosettaRule) symbol).getExpression() != null) {
	            result = safeIsMulti(((RosettaRule) symbol).getExpression(), cycleTracker);
	        } else {
	            result = false;
	        }
	    } else if (symbol instanceof RosettaExternalFunction) {
	        result = false;
	    } else if (symbol instanceof ShortcutDeclaration) {
	        result = safeIsMulti(((ShortcutDeclaration) symbol).getExpression(), cycleTracker);
	    } else if (symbol instanceof TypeParameter) {
	        result = false;
	    } else {
	        LOGGER.error("Cardinality not defined for symbol: " + (symbol != null ? symbol.eClass().getName() : "null"));
	        result = false;
	    }
	    
	    cycleTracker.put(symbol, result);
	    return result;
	}
	
	public boolean isImplicitVariableMulti(EObject context) {
        return safeIsImplicitVariableMulti(context, new HashMap<>());
    }

    private boolean safeIsImplicitVariableMulti(EObject context, Map<RosettaSymbol, Boolean> cycleTracker) {
        Optional<? extends EObject> definingContainer = implicitVariableUtil.findContainerDefiningImplicitVariable(context);

        return definingContainer.map(container -> {
            if (container instanceof RosettaTypeWithConditions) {
                return false;
            } else if (container instanceof RosettaFunctionalOperation) {
                return safeIsClosureParameterMulti(((RosettaFunctionalOperation) container).getFunction(), cycleTracker);
            } else if (container instanceof RosettaRule) {
            	return false;
            } else if (container instanceof SwitchCaseOrDefault) {
                return false;
            }
            return false;
        }).orElse(false);
    }
	
    private boolean isFeatureOfImplicitVariable(EObject context, RosettaFeature feature) {
        return IterableExtensions.contains(typeProvider.findFeaturesOfImplicitVariable(context), feature);
    }
	
    private boolean safeIsClosureParameterMulti(InlineFunction obj, Map<RosettaSymbol, Boolean> cycleTracker) {
        EObject op = obj.eContainer();
        if (op instanceof RosettaFunctionalOperation) {
            if (op instanceof ThenOperation) {
                return safeIsMulti(((ThenOperation) op).getArgument(), cycleTracker);
            }
            return isOutputListOfLists(((RosettaFunctionalOperation) op).getArgument());
        }
        return false;
    }
	
	public boolean isItemMulti(InlineFunction op) {
		return safeIsItemMulti(op, new HashMap<>());
	}
	private boolean safeIsItemMulti(InlineFunction op, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsClosureParameterMulti(op, cycleTracker);
	}
	
	/**
	 * List MAP/FILTER/Extract-all operations can handle a list of lists, however it cannot be handled anywhere else (e.g. a list of list cannot be assigned to a func output or alias)
	 */
	public boolean isOutputListOfLists(RosettaExpression expr) {
        return safeIsOutputListOfLists(expr, new HashMap<>());
    }
	private boolean safeIsOutputListOfLists(RosettaExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
        if (expr instanceof FlattenOperation) {
            return false;
        } else if (expr instanceof MapOperation) {
            MapOperation mapOperation = (MapOperation) expr;
            if (mapOperation.getFunction() == null) {
                return false;
            } else if (safeIsItemMulti(mapOperation.getFunction(), cycleTracker)) {
                return safeIsBodyExpressionMulti(mapOperation.getFunction(), cycleTracker);
            } else {
                return safeIsBodyExpressionMulti(mapOperation.getFunction(), cycleTracker) && safeIsPreviousOperationMulti(mapOperation, cycleTracker);
            }
        } else if (expr instanceof ThenOperation) {
            InlineFunction function = ((ThenOperation) expr).getFunction();
            if (function instanceof InlineFunction) {
                return safeIsOutputListOfLists(function.getBody(), cycleTracker);
            }
            return false;
        } else if (expr instanceof RosettaSymbolReference) {
            RosettaSymbol symbol = ((RosettaSymbolReference) expr).getSymbol();
            if (symbol instanceof ClosureParameter) {
                InlineFunction function = ((ClosureParameter) symbol).getFunction();
                EObject enclosed = function.eContainer();
                if (enclosed instanceof ThenOperation) {
                    return safeIsOutputListOfLists(((ThenOperation) enclosed).getArgument(), cycleTracker);
                }
            }
            return false;
        } else if (expr instanceof RosettaImplicitVariable) {
            Optional<? extends EObject> container = implicitVariableUtil.findContainerDefiningImplicitVariable(expr);
            return container.map(obj -> obj instanceof ThenOperation && safeIsOutputListOfLists(((ThenOperation) obj).getArgument(), cycleTracker)).orElse(false);
        } else if (expr instanceof CanHandleListOfLists) {
            CanHandleListOfLists listExpression = (CanHandleListOfLists) expr;
            return safeIsOutputListOfLists(listExpression.getArgument(), cycleTracker);
        }
        return false;
    }
	
	public boolean isPreviousOperationMulti(RosettaUnaryOperation op) {
		return safeIsPreviousOperationMulti(op, new HashMap<>());
	}
	private boolean safeIsPreviousOperationMulti(RosettaUnaryOperation op, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsMulti(op.getArgument(), cycleTracker);
	}
	
	public boolean isBodyExpressionMulti(InlineFunction op) {
		return safeIsBodyExpressionMulti(op, new HashMap<>());
	}
	private boolean safeIsBodyExpressionMulti(InlineFunction op, Map<RosettaSymbol, Boolean> cycleTracker) {
		return op.getBody() != null && safeIsMulti(op.getBody(), cycleTracker);
	}

	/**
	 * Nothing handles a list of list of list
	 */
	public boolean isOutputListOfListOfLists(RosettaExpression op) {
		return false; // The output of an expression never results a list of lists of lists.
	}

	@Override
	protected Boolean caseAbsentOperation(RosettaAbsentExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseAddOperation(ArithmeticOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseAndOperation(LogicalOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseAsKeyOperation(AsKeyOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsMulti(expr.getArgument(), cycleTracker);
	}
	
	@Override
	protected Boolean caseBooleanLiteral(RosettaBooleanLiteral expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseChoiceOperation(ChoiceOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseConditionalExpression(RosettaConditionalExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsMulti(expr.getIfthen(), cycleTracker) || safeIsMulti(expr.getElsethen(), cycleTracker);
	}
	
	@Override
	protected Boolean caseContainsOperation(RosettaContainsExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseCountOperation(RosettaCountOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseDisjointOperation(RosettaDisjointExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseDefaultOperation(DefaultOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseDistinctOperation(DistinctOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsMulti(expr.getArgument(), cycleTracker);
	}
	
	@Override
	protected Boolean caseDivideOperation(ArithmeticOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseEqualsOperation(EqualityOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseExistsOperation(RosettaExistsExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseFeatureCall(RosettaFeatureCall expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		if (isFeatureMulti(expr.getFeature())) 
			return true; 
		else 
			return safeIsMulti(expr.getReceiver(), cycleTracker);
	}
	
	@Override
	protected Boolean caseDeepFeatureCall(RosettaDeepFeatureCall expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		if (isFeatureMulti(expr.getFeature())) 
			return true; 
		else 
			return safeIsMulti(expr.getReceiver(), cycleTracker);
	}
	
	@Override
	protected Boolean caseFilterOperation(FilterOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsMulti(expr.getArgument(), cycleTracker);
	}
	
	@Override
	protected Boolean caseFirstOperation(FirstOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseFlattenOperation(FlattenOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return true;
	}
	
	@Override
	protected Boolean caseGreaterThanOperation(ComparisonOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseGreaterThanOrEqualOperation(ComparisonOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseImplicitVariable(RosettaImplicitVariable expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return safeIsImplicitVariableMulti(expr, cycleTracker);
	}
	
	@Override
	protected Boolean caseIntLiteral(RosettaIntLiteral expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseJoinOperation(JoinOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseLastOperation(LastOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseLessThanOperation(ComparisonOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseLessThanOrEqualOperation(ComparisonOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseListLiteral(ListLiteral expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return expr.getElements().size() > 0; // TODO: the type system is currently not strong enough to implement this completely right
	}
	
	@Override
	protected Boolean caseMapOperation(MapOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		if (expr.getFunction() != null && safeIsMulti(expr.getFunction().getBody(), cycleTracker)) {
			return true;
		} else {
			return safeIsMulti(expr.getArgument(), cycleTracker);
		}
	}
	
	@Override
	protected Boolean caseMaxOperation(MaxOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseMinOperation(MinOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseMultiplyOperation(ArithmeticOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseNotEqualsOperation(EqualityOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseNumberLiteral(RosettaNumberLiteral expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseOneOfOperation(OneOfOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseOnlyElementOperation(RosettaOnlyElement expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseOnlyExists(RosettaOnlyExistsExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseOrOperation(LogicalOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseReduceOperation(ReduceOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseReverseOperation(ReverseOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return true;
	}
	
	@Override
	protected Boolean caseSortOperation(SortOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return true;
	}
	
	@Override
	protected Boolean caseStringLiteral(RosettaStringLiteral expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseSubtractOperation(ArithmeticOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseSumOperation(SumOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseSymbolReference(RosettaSymbolReference expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		RosettaSymbol s = expr.getSymbol();
		if (s instanceof RosettaFeature) {
			if (isFeatureOfImplicitVariable(expr, (RosettaFeature) s) && safeIsImplicitVariableMulti(expr, cycleTracker)) {
				return true;
			}
		}
		return safeIsSymbolMulti(s, cycleTracker);
	}
	
	@Override
	protected Boolean caseThenOperation(ThenOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		if (expr.getFunction() != null) {
			return safeIsMulti(expr.getFunction().getBody(), cycleTracker);
		} else {
			return false;
		}
	}
	
	@Override
	protected Boolean caseToEnumOperation(ToEnumOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToIntOperation(ToIntOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToNumberOperation(ToNumberOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToStringOperation(ToStringOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToTimeOperation(ToTimeOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseConstructorExpression(RosettaConstructorExpression expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToDateOperation(ToDateOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToDateTimeOperation(ToDateTimeOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}	
	
	@Override
	protected Boolean caseSwitchOperation(SwitchOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		for (SwitchCaseOrDefault switchCase : expr.getCases()) {
			if (safeIsMulti(switchCase.getExpression(), cycleTracker)) {
				return true;
			}
		}
 		return false;
 	}

	@Override
	protected Boolean caseWithMetaOperation(WithMetaOperation expr, Map<RosettaSymbol, Boolean> cycleTracker) {
		return false;
	}
	
	@Override
	protected Boolean caseSuperCall(RosettaSuperCall expr, Map<RosettaSymbol, Boolean> context) {
		// TODO Auto-generated method stub
		return null;
	}
}
