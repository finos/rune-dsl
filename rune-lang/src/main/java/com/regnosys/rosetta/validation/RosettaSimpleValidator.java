package com.regnosys.rosetta.validation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.ExternalAnnotationSource;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.ParametrizedRosettaType;
import com.regnosys.rosetta.rosetta.RosettaAttributeReference;
import com.regnosys.rosetta.rosetta.RosettaAttributeReferenceSegment;
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalClassSynonym;
import com.regnosys.rosetta.rosetta.RosettaExternalRef;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaMapPathValue;
import com.regnosys.rosetta.rosetta.RosettaMapTestExpression;
import com.regnosys.rosetta.rosetta.RosettaMapping;
import com.regnosys.rosetta.rosetta.RosettaMappingInstance;
import com.regnosys.rosetta.rosetta.RosettaMergeSynonymValue;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.RosettaSynonym;
import com.regnosys.rosetta.rosetta.RosettaSynonymBody;
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTyped;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.CanHandleListOfLists;
import com.regnosys.rosetta.rosetta.expression.ClosureParameter;
import com.regnosys.rosetta.rosetta.expression.ComparingFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.expression.FilterOperation;
import com.regnosys.rosetta.rosetta.expression.FlattenOperation;
import com.regnosys.rosetta.rosetta.expression.HasGeneratedInput;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.ListOperation;
import com.regnosys.rosetta.rosetta.expression.MandatoryFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.UnaryFunctionalOperation;
import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression;
import com.regnosys.rosetta.rosetta.simple.AnnotationQualifier;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch;
import com.regnosys.rosetta.rosetta.simple.LabelAnnotation;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RCardinality;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.utils.ImportManagementService;
import com.regnosys.rosetta.utils.RosettaConfigExtension;
import jakarta.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * Do not write any more validators in here for the following reasons:
 * 1. We are moving away from Xtend to Java for all test and implementation code
 * 2. This approach to putting every validator type in one place is messy and hard to navigate, better to split into classes by type of validator
 * 
 * The appropriate validators can be found by looking for [ValidatorType]Validator.java where validator type could be Expression for example ExpressionValidator.java
 */
@Deprecated
public class RosettaSimpleValidator extends AbstractDeclarativeRosettaValidator {
  @Inject
  private RosettaEcoreUtil rosettaEcoreUtil;

  @Inject
  private RosettaTypeProvider rosettaTypeProvider;

  @Inject
  private IQualifiedNameProvider qualifiedNameProvider;

  @Inject
  private ResourceDescriptionsProvider resourceDescriptionsProvider;

  @Inject
  private RosettaFunctionExtensions rosettaFunctionExtensions;

  @Inject
  private ExpressionHelper exprHelper;

  @Inject
  private CardinalityProvider cardinality;

  @Inject
  private RosettaConfigExtension confExtensions;

  @Inject
  private ImplicitVariableUtil implicitVariableUtil;

  @Inject
  private RosettaScopeProvider scopeProvider;

  @Inject
  private RBuiltinTypeService builtinTypeService;

  @Inject
  private TypeSystem typeSystem;

  @Inject
  private RosettaGrammarAccess grammarAccess;

  @Inject
  private RObjectFactory objectFactory;

  @Inject
  private ImportManagementService importManagementService;

  // Aliases for backward compatibility during migration
  private RosettaEcoreUtil _rosettaEcoreUtil;
  private RosettaTypeProvider _rosettaTypeProvider;
  private IQualifiedNameProvider _iQualifiedNameProvider;
  private ResourceDescriptionsProvider _resourceDescriptionsProvider;
  private RosettaFunctionExtensions _rosettaFunctionExtensions;
  private ImplicitVariableUtil _implicitVariableUtil;
  private RBuiltinTypeService _rBuiltinTypeService;
  private TypeSystem _typeSystem;
  private RosettaGrammarAccess _rosettaGrammarAccess;

  @Inject
  private void initializeAliases() {
    this._rosettaEcoreUtil = this.rosettaEcoreUtil;
    this._rosettaTypeProvider = this.rosettaTypeProvider;
    this._iQualifiedNameProvider = this.qualifiedNameProvider;
    this._resourceDescriptionsProvider = this.resourceDescriptionsProvider;
    this._rosettaFunctionExtensions = this.rosettaFunctionExtensions;
    this._implicitVariableUtil = this.implicitVariableUtil;
    this._rBuiltinTypeService = this.builtinTypeService;
    this._typeSystem = this.typeSystem;
    this._rosettaGrammarAccess = this.grammarAccess;
  }

  @Check
  public void deprecatedWarning(final EObject object) {
    EList<EStructuralFeature> features = object.eClass().getEAllStructuralFeatures();
    EStructuralFeature[] crossReferencesArray = ((EClassImpl.FeatureSubsetSupplier) features).crossReferences();
    final List<EReference> crossRefs = ((List<EReference>) Conversions.doWrapArray(crossReferencesArray));

    if (crossRefs != null) {
      for (final EReference ref : crossRefs) {
        if (ref.isMany()) {
          Object eGet = object.eGet(ref);
          final Iterable<Annotated> annotatedList = Iterables.filter(((List<?>) eGet), Annotated.class);
          List<Annotated> annotatedArray = IterableExtensions.toList(annotatedList);

          for (int i = 0; i < annotatedArray.size(); i++) {
            this.checkDeprecatedAnnotation(annotatedArray.get(i), object, ref, i);
          }
        } else {
          final Object annotated = object.eGet(ref);
          if (annotated instanceof Annotated) {
            this.checkDeprecatedAnnotation(((Annotated) annotated), object, ref, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
          }
        }
      }
    }
  }

  @Check
  public void deprecatedLabelAsWarning(final LabelAnnotation labelAnnotation) {
    if (labelAnnotation.isDeprecatedAs()) {
      if (labelAnnotation.getPath() == null) {
        this.warning("The `as` keyword without a path is deprecated", 
                    labelAnnotation, 
                    SimplePackage.Literals.LABEL_ANNOTATION__DEPRECATED_AS);
      } else {
        this.warning("The `as` keyword after a path is deprecated. Add the keyword `for` before the path instead", 
                    labelAnnotation, 
                    SimplePackage.Literals.LABEL_ANNOTATION__DEPRECATED_AS);
      }
    }
  }

  @Check
  public void ruleMustHaveInputTypeDeclared(final RosettaRule rule) {
    if (rule.getInput() == null) {
      this.error("A rule must declare its input type: `rule " + rule.getName() + " from <input type>: ...`", 
                rule, 
                RosettaPackage.Literals.ROSETTA_NAMED__NAME);
    }

    if (this.cardinality.isOutputListOfLists(rule.getExpression())) {
      this.error("Assign expression contains a list of lists, use flatten to create a list.", 
                rule.getExpression(), 
                null);
    }
  }

  @Check
  public void unnecesarrySquareBracketsCheck(final RosettaFunctionalOperation op) {
    if (this.hasSquareBrackets(op)) {
      if (!this.areSquareBracketsMandatory(op)) {
        this.warning("Usage of brackets is unnecessary.", 
                    op.getFunction(), 
                    null, 
                    RosettaIssueCodes.REDUNDANT_SQUARE_BRACKETS);
      }
    }
  }

  public boolean hasSquareBrackets(final RosettaFunctionalOperation op) {
    return op.getFunction() != null && 
           this.findDirectKeyword(op.getFunction(), 
                                 this.grammarAccess.getInlineFunctionAccess().getLeftSquareBracketKeyword_0_0_1()) != null;
  }

  public boolean areSquareBracketsMandatory(final RosettaFunctionalOperation op) {
    // Not a ThenOperation and has a function
    if (op instanceof ThenOperation || op.getFunction() == null) {
      return false;
    }

    // Is not a MandatoryFunctionalOperation or has parameters
    boolean condition1 = !(op instanceof MandatoryFunctionalOperation) || !op.getFunction().getParameters().isEmpty();

    // Contains nested functional operation
    boolean condition2 = this.containsNestedFunctionalOperation(op);

    // Container is a RosettaOperation but not a ThenOperation
    boolean condition3 = op.eContainer() instanceof RosettaOperation && 
                        !(op.eContainer() instanceof ThenOperation);

    return condition1 || condition2 || condition3;
  }

  @Check
  public void mandatoryThenCheck(final RosettaUnaryOperation op) {
    if (this.isThenMandatory(op)) {
      boolean previousOperationIsThen = op.getArgument().isGenerated() && 
                                       op.eContainer() instanceof InlineFunction && 
                                       op.eContainer().eContainer() instanceof ThenOperation;

      if (!previousOperationIsThen) {
        this.error("Usage of `then` is mandatory.", 
                  op, 
                  ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR, 
                  RosettaIssueCodes.MANDATORY_THEN);
      }
    }
  }

  public boolean isThenMandatory(final RosettaUnaryOperation op) {
    // ThenOperation is never mandatory
    if (op instanceof ThenOperation) {
      return false;
    }

    // Check if argument is a MandatoryFunctionalOperation
    if (op.getArgument() instanceof MandatoryFunctionalOperation) {
      return true;
    }

    // Recursively check if argument is a RosettaUnaryOperation that requires then
    if (op.getArgument() instanceof RosettaUnaryOperation) {
      return this.isThenMandatory((RosettaUnaryOperation) op.getArgument());
    }

    return false;
  }

  @Check
  public void ambiguousFunctionalOperation(final RosettaFunctionalOperation op) {
    if (op.getFunction() != null) {
      if (this.isNestedFunctionalOperation(op)) {
        InlineFunction inlineFunction = EcoreUtil2.getContainerOfType(op, InlineFunction.class);
        RosettaFunctionalOperation enclosingFunctionalOperation = (RosettaFunctionalOperation) inlineFunction.eContainer();

        if (!this.hasSquareBrackets(enclosingFunctionalOperation)) {
          this.error("Ambiguous operation. Either use `then` or surround with square brackets to define a nested operation.", 
                    op, 
                    ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
      }
    }
  }

  public boolean isNestedFunctionalOperation(final RosettaFunctionalOperation op) {
    final InlineFunction enclosingInlineFunction = EcoreUtil2.getContainerOfType(op, InlineFunction.class);

    if (enclosingInlineFunction != null && !(enclosingInlineFunction.eContainer() instanceof ThenOperation)) {
      EObject opCodeBlock = this.getEnclosingCodeBlock(op);
      EObject functionCodeBlock = this.getEnclosingCodeBlock(enclosingInlineFunction);

      if (opCodeBlock == functionCodeBlock) {
        return true;
      }
    }

    return false;
  }

  public boolean containsNestedFunctionalOperation(final RosettaFunctionalOperation op) {
    if (op.getFunction() == null) {
      return false;
    }

    final EObject codeBlock = this.getEnclosingCodeBlock(op.getFunction());

    // Find all RosettaFunctionalOperation instances in the function
    List<RosettaFunctionalOperation> allOperations = 
        EcoreUtil2.eAllOfType(op.getFunction(), RosettaFunctionalOperation.class);

    // Filter operations that have the same enclosing code block
    for (RosettaFunctionalOperation operation : allOperations) {
      if (this.getEnclosingCodeBlock(operation) == codeBlock) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the object that directly encloses the given object with parentheses or brackets, 
   * or the first encountered root element.
   */
  public EObject getEnclosingCodeBlock(final EObject obj) {
    return this.getEnclosingCodeBlock(NodeModelUtils.findActualNodeFor(obj), obj);
  }

  private EObject getEnclosingCodeBlock(final INode node, final EObject potentialCodeBlock) {
    // If we've reached a root element, return it
    if (potentialCodeBlock instanceof RosettaRootElement) {
      return potentialCodeBlock;
    }

    // Define bracket pairs
    Map<String, String> pairs = new HashMap<>();
    pairs.put("(", ")");
    pairs.put("[", "]");
    pairs.put("{", "}");

    // Find the first left bracket in the potential code block
    INode leftBracket = null;
    for (String leftBracketText : pairs.keySet()) {
      INode foundNode = this.findDirectKeyword(potentialCodeBlock, leftBracketText);
      if (foundNode != null) {
        leftBracket = foundNode;
        break;
      }
    }

    // If we found a left bracket
    if (leftBracket != null) {
      String rightBracketText = pairs.get(leftBracket.getText());
      INode rightBracket = this.findDirectKeyword(potentialCodeBlock, rightBracketText);

      // Check if the node is within the brackets
      boolean isWithinBrackets = leftBracket.getOffset() <= node.getOffset() && 
                               (rightBracket == null || rightBracket.getOffset() >= node.getOffset());

      if (isWithinBrackets) {
        return potentialCodeBlock;
      }
    }

    // Recursively check the container
    return this.getEnclosingCodeBlock(node, potentialCodeBlock.eContainer());
  }

  @Check
  public void checkParametrizedType(final ParametrizedRosettaType type) {
    final Set<String> visited = new HashSet<>();

    for (final TypeParameter param : type.getParameters()) {
      if (!visited.add(param.getName())) {
        this.error("Duplicate parameter name `" + param.getName() + "`.", 
                  param, 
                  RosettaPackage.Literals.ROSETTA_NAMED__NAME);
      }
    }
  }
  // CONTINUE MIGRATION FROM HERE
  @Check
  public void checkAnnotationSource(final ExternalAnnotationSource source) {
    final HashSet<RosettaType> visited = CollectionLiterals.<RosettaType>newHashSet();
    EList<RosettaExternalRef> _externalRefs = source.getExternalRefs();
    for (final RosettaExternalRef t : _externalRefs) {
      boolean _add = visited.add(t.getTypeRef());
      boolean _not = (!_add);
      if (_not) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Duplicate type `");
        String _name = t.getTypeRef().getName();
        _builder.append(_name);
        _builder.append("`.");
        this.warning(_builder.toString(), t, null);
      }
    }
  }

  @Check
  public void checkSynonymSource(final RosettaExternalSynonymSource source) {
    EList<RosettaExternalClass> _externalClasses = source.getExternalClasses();
    for (final RosettaExternalClass t : _externalClasses) {
      EList<RosettaExternalRegularAttribute> _regularAttributes = t.getRegularAttributes();
      for (final RosettaExternalRegularAttribute attr : _regularAttributes) {
        final Consumer<RuleReferenceAnnotation> _function = (RuleReferenceAnnotation it) -> {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("You may not define rule references in a synonym source.");
          this.error(_builder.toString(), it, null);
        };
        attr.getExternalRuleReferences().forEach(_function);
      }
    }
  }

  @Check
  public void checkRuleSource(final RosettaExternalRuleSource source) {
    int _size = source.getSuperSources().size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      this.error("A rule source may not extend more than one other rule source.", source, 
        RosettaPackage.Literals.ROSETTA_EXTERNAL_RULE_SOURCE__SUPER_SOURCES, 1);
    }
    EList<RosettaExternalClass> _externalClasses = source.getExternalClasses();
    for (final RosettaExternalClass t : _externalClasses) {
      {
        final Consumer<RosettaExternalClassSynonym> _function = (RosettaExternalClassSynonym it) -> {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("You may not define synonyms in a rule source.");
          this.error(_builder.toString(), it, null);
        };
        t.getExternalClassSynonyms().forEach(_function);
        EList<RosettaExternalRegularAttribute> _regularAttributes = t.getRegularAttributes();
        for (final RosettaExternalRegularAttribute attr : _regularAttributes) {
          final Consumer<RosettaExternalSynonym> _function_1 = (RosettaExternalSynonym it) -> {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("You may not define synonyms in a rule source.");
            this.error(_builder.toString(), it, null);
          };
          attr.getExternalSynonyms().forEach(_function_1);
        }
      }
    }
    this.errorKeyword("A rule source cannot define annotations for enums.", source, 
      this._rosettaGrammarAccess.getExternalAnnotationSourceAccess().getEnumsKeyword_2_0());
  }

  @Check
  public void checkGeneratedInputInContextWithImplicitVariable(final HasGeneratedInput e) {
    if ((e.needsGeneratedInput() && (!this._implicitVariableUtil.implicitVariableExistsInContext(e)))) {
      this.error(
        "There is no implicit variable in this context. This operator needs an explicit input in this context.", e, null);
    }
  }

  @Check
  public void checkImplicitVariableReferenceInContextWithoutImplicitVariable(final RosettaImplicitVariable e) {
    boolean _implicitVariableExistsInContext = this._implicitVariableUtil.implicitVariableExistsInContext(e);
    boolean _not = (!_implicitVariableExistsInContext);
    if (_not) {
      this.error("There is no implicit variable in this context.", e, null);
    }
  }

  @Check
  public void checkConditionName(final Condition condition) {
    if (((condition.getName() == null) && (!this._rosettaEcoreUtil.isConstraintCondition(condition)))) {
      this.warning("Condition name should be specified", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_NAME);
    } else {
      if (((condition.getName() != null) && Character.isLowerCase(condition.getName().charAt(0)))) {
        this.warning("Condition name should start with a capital", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_CASE);
      }
    }
  }

  @Check
  public void checkFeatureCallFeature(final RosettaFeatureCall fCall) {
    RosettaFeature _feature = fCall.getFeature();
    boolean _tripleEquals = (_feature == null);
    if (_tripleEquals) {
      this.error("Attribute is missing after \'->\'", fCall, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__FEATURE);
      return;
    }
  }

  @Check
  public void checkFunctionNameStartsWithCapital(final com.regnosys.rosetta.rosetta.simple.Function func) {
    boolean _isLowerCase = Character.isLowerCase(func.getName().charAt(0));
    if (_isLowerCase) {
      this.warning("Function name should start with a capital", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_CASE);
    }
  }

  @Check
  public void checkAttributes(final Data clazz) {
    final HashMultimap<String, RAttribute> name2attr = HashMultimap.<String, RAttribute>create();
    final Function1<RAttribute, Boolean> _function = (RAttribute it) -> {
      boolean _isOverride = it.isOverride();
      return Boolean.valueOf((!_isOverride));
    };
    final Consumer<RAttribute> _function_1 = (RAttribute it) -> {
      name2attr.put(it.getName(), it);
    };
    IterableExtensions.<RAttribute>filter(this.objectFactory.buildRDataType(clazz).getAllAttributes(), _function).forEach(_function_1);
    final Function1<Attribute, Boolean> _function_2 = (Attribute it) -> {
      boolean _isOverride = it.isOverride();
      return Boolean.valueOf((!_isOverride));
    };
    final Function1<Attribute, String> _function_3 = (Attribute it) -> {
      return it.getName();
    };
    Iterable<String> _map = IterableExtensions.<Attribute, String>map(IterableExtensions.<Attribute>filter(clazz.getAttributes(), _function_2), _function_3);
    for (final String name : _map) {
      {
        final Set<RAttribute> attrByName = name2attr.get(name);
        int _size = attrByName.size();
        boolean _greaterThan = (_size > 1);
        if (_greaterThan) {
          final Function1<RAttribute, Boolean> _function_4 = (RAttribute it) -> {
            EObject _eContainer = it.getEObject().eContainer();
            return Boolean.valueOf(Objects.equals(_eContainer, clazz));
          };
          final Iterable<RAttribute> attrFromClazzes = IterableExtensions.<RAttribute>filter(attrByName, _function_4);
          final Function1<RAttribute, Boolean> _function_5 = (RAttribute it) -> {
            EObject _eContainer = it.getEObject().eContainer();
            return Boolean.valueOf((!Objects.equals(_eContainer, clazz)));
          };
          final Iterable<RAttribute> attrFromSuperClasses = IterableExtensions.<RAttribute>filter(attrByName, _function_5);
          this.checkTypeAttributeMustHaveSameTypeAsParent(attrFromClazzes, attrFromSuperClasses, name);
          this.checkAttributeCardinalityMatchSuper(attrFromClazzes, attrFromSuperClasses, name);
        }
      }
    }
  }

  protected void checkTypeAttributeMustHaveSameTypeAsParent(final Iterable<RAttribute> attrFromClazzes, final Iterable<RAttribute> attrFromSuperClasses, final String name) {
    final Consumer<RAttribute> _function = (RAttribute childAttr) -> {
      final RType childAttrType = childAttr.getRMetaAnnotatedType().getRType();
      final Consumer<RAttribute> _function_1 = (RAttribute parentAttr) -> {
        final RType parentAttrType = parentAttr.getRMetaAnnotatedType().getRType();
        boolean _notEquals = (!Objects.equals(childAttrType, parentAttrType));
        if (_notEquals) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Overriding attribute \'");
          _builder.append(name);
          _builder.append("\' with type ");
          _builder.append(childAttrType);
          _builder.append(" must match the type of the attribute it overrides (");
          _builder.append(parentAttrType);
          _builder.append(")");
          this.error(_builder.toString(), 
            childAttr.getEObject(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.DUPLICATE_ATTRIBUTE);
        }
      };
      attrFromSuperClasses.forEach(_function_1);
    };
    attrFromClazzes.forEach(_function);
  }

  protected void checkAttributeCardinalityMatchSuper(final Iterable<RAttribute> attrFromClazzes, final Iterable<RAttribute> attrFromSuperClasses, final String name) {
    final Consumer<RAttribute> _function = (RAttribute childAttr) -> {
      final Consumer<RAttribute> _function_1 = (RAttribute parentAttr) -> {
        RCardinality _cardinality = childAttr.getCardinality();
        RCardinality _cardinality_1 = parentAttr.getCardinality();
        boolean _notEquals = (!Objects.equals(_cardinality, _cardinality_1));
        if (_notEquals) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Overriding attribute \'");
          _builder.append(name);
          _builder.append("\' with cardinality (");
          RCardinality _cardinality_2 = childAttr.getCardinality();
          _builder.append(_cardinality_2);
          _builder.append(") must match the cardinality of the attribute it overrides (");
          RCardinality _cardinality_3 = parentAttr.getCardinality();
          _builder.append(_cardinality_3);
          _builder.append(")");
          this.error(_builder.toString(), 
            childAttr.getEObject(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.CARDINALITY_ERROR);
        }
      };
      attrFromSuperClasses.forEach(_function_1);
    };
    attrFromClazzes.forEach(_function);
  }

  @Check
  public void checkFunctionElementNamesAreUnique(final com.regnosys.rosetta.rosetta.simple.Function ele) {
    EList<Attribute> _inputs = ele.getInputs();
    EList<ShortcutDeclaration> _shortcuts = ele.getShortcuts();
    Iterable<EObject> _plus = Iterables.<EObject>concat(_inputs, _shortcuts);
    Attribute _output = ele.getOutput();
    final Function1<EObject, String> _function = (EObject it) -> {
      return ((AssignPathRoot)it).getName();
    };
    final BiConsumer<String, List<EObject>> _function_1 = (String k, List<EObject> v) -> {
      int _size = v.size();
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        final Consumer<EObject> _function_2 = (EObject it) -> {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Duplicate feature \"");
          _builder.append(k);
          _builder.append("\"");
          this.error(_builder.toString(), it, RosettaPackage.Literals.ROSETTA_NAMED__NAME);
        };
        v.forEach(_function_2);
      }
    };
    IterableExtensions.<String, EObject>groupBy(IterableExtensions.<EObject>filterNull(Iterables.<EObject>concat(_plus, Collections.<Attribute>unmodifiableList(CollectionLiterals.<Attribute>newArrayList(_output)))), _function).forEach(_function_1);
  }

  @Check
  public void checkClosureParameterNamesAreUnique(final ClosureParameter param) {
    final IScope scope = this.scopeProvider.getScope(param.getFunction().eContainer(), ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL);
    final IEObjectDescription sameNamedElement = scope.getSingleElement(QualifiedName.create(param.getName()));
    if ((sameNamedElement != null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Duplicate name.");
      this.error(_builder.toString(), param, null);
    }
  }

  @Check(CheckType.FAST)
  public void checkTypeNamesAreUnique(final RosettaModel model) {
    final Function1<RosettaNamed, Boolean> _function = (RosettaNamed it) -> {
      return Boolean.valueOf((!(it instanceof FunctionDispatch)));
    };
    final Iterable<RosettaNamed> namedRootElems = IterableExtensions.<RosettaNamed>filter(Iterables.<RosettaNamed>filter(model.getElements(), RosettaNamed.class), _function);
    final IResourceDescriptions descr = this._resourceDescriptionsProvider.getResourceDescriptions(model.eResource());
    final Function1<RosettaNamed, Boolean> _function_1 = (RosettaNamed it) -> {
      return Boolean.valueOf((!(it instanceof Annotation)));
    };
    final Iterable<RosettaNamed> nonAnnotations = IterableExtensions.<RosettaNamed>filter(namedRootElems, _function_1);
    this.checkNamesInGroupAreUnique(IterableExtensions.<RosettaNamed>toList(nonAnnotations), true, descr);
    final Function1<RosettaNamed, Boolean> _function_2 = (RosettaNamed it) -> {
      return Boolean.valueOf((it instanceof Annotation));
    };
    final Iterable<RosettaNamed> annotations = IterableExtensions.<RosettaNamed>filter(namedRootElems, _function_2);
    this.checkNamesInGroupAreUnique(IterableExtensions.<RosettaNamed>toList(annotations), true, descr);
  }

  private void checkNamesInGroupAreUnique(final Collection<RosettaNamed> namedElems, final boolean ignoreCase, final IResourceDescriptions descr) {
    final HashMultimap<String, RosettaNamed> groupedElems = HashMultimap.<String, RosettaNamed>create();
    final Function1<RosettaNamed, Boolean> _function = (RosettaNamed it) -> {
      String _name = it.getName();
      return Boolean.valueOf((_name != null));
    };
    final Consumer<RosettaNamed> _function_1 = (RosettaNamed it) -> {
      String _xifexpression = null;
      if (ignoreCase) {
        _xifexpression = it.getName().toUpperCase();
      } else {
        _xifexpression = it.getName();
      }
      groupedElems.put(_xifexpression, it);
    };
    IterableExtensions.<RosettaNamed>filter(namedElems, _function).forEach(_function_1);
    Set<String> _keySet = groupedElems.keySet();
    for (final String key : _keySet) {
      {
        final Set<RosettaNamed> group = groupedElems.get(key);
        int _size = group.size();
        boolean _greaterThan = (_size > 1);
        if (_greaterThan) {
          final Consumer<RosettaNamed> _function_2 = (RosettaNamed it) -> {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Duplicate element named \'");
            String _name = it.getName();
            _builder.append(_name);
            _builder.append("\'");
            this.error(_builder.toString(), it, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.DUPLICATE_ELEMENT_NAME);
          };
          group.forEach(_function_2);
        } else {
          if (((group.size() == 1) && IterableExtensions.<RosettaNamed>head(group).eResource().getURI().isPlatformResource())) {
            final RosettaNamed toCheck = IterableExtensions.<RosettaNamed>head(group);
            if ((toCheck instanceof RosettaRootElement)) {
              final QualifiedName qName = this._iQualifiedNameProvider.getFullyQualifiedName(toCheck);
              final Function1<IEObjectDescription, Boolean> _function_3 = (IEObjectDescription it) -> {
                return Boolean.valueOf((this.confExtensions.isProjectLocal(toCheck.eResource().getURI(), it.getEObjectURI()) && (it.getEClass() != SimplePackage.Literals.FUNCTION_DISPATCH)));
              };
              final Function1<IEObjectDescription, URI> _function_4 = (IEObjectDescription it) -> {
                return it.getEObjectURI();
              };
              final Iterable<URI> sameNamed = IterableExtensions.<IEObjectDescription, URI>map(IterableExtensions.<IEObjectDescription>filter(descr.getExportedObjects(toCheck.eClass(), qName, false), _function_3), _function_4);
              int _size_1 = IterableExtensions.size(sameNamed);
              boolean _greaterThan_1 = (_size_1 > 1);
              if (_greaterThan_1) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("Duplicate element named \'");
                _builder.append(qName);
                _builder.append("\' in ");
                final Function1<URI, Boolean> _function_5 = (URI it) -> {
                  URI _uRI = EcoreUtil.getURI(toCheck);
                  return Boolean.valueOf((!Objects.equals(_uRI, it)));
                };
                final Function1<URI, CharSequence> _function_6 = (URI it) -> {
                  return it.lastSegment();
                };
                String _join = IterableExtensions.<URI>join(IterableExtensions.<URI>filter(sameNamed, _function_5), ", ", _function_6);
                _builder.append(_join);
                this.error(_builder.toString(), toCheck, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.DUPLICATE_ELEMENT_NAME);
              }
            }
          }
        }
      }
    }
  }

  @Check
  public void checkMappingSetToCase(final RosettaMapping element) {
    final Function1<RosettaMappingInstance, Boolean> _function = (RosettaMappingInstance it) -> {
      return Boolean.valueOf(((it.getSet() != null) && (it.getWhen() == null)));
    };
    int _size = IterableExtensions.size(IterableExtensions.<RosettaMappingInstance>filter(element.getInstances(), _function));
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Only one set to with no when clause allowed.");
      this.error(_builder.toString(), element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
    }
    final Function1<RosettaMappingInstance, Boolean> _function_1 = (RosettaMappingInstance it) -> {
      return Boolean.valueOf(((it.getSet() != null) && (it.getWhen() == null)));
    };
    int _size_1 = IterableExtensions.size(IterableExtensions.<RosettaMappingInstance>filter(element.getInstances(), _function_1));
    boolean _equals = (_size_1 == 1);
    if (_equals) {
      final Function1<RosettaMappingInstance, Boolean> _function_2 = (RosettaMappingInstance it) -> {
        return Boolean.valueOf(((it.getSet() != null) && (it.getWhen() == null)));
      };
      final RosettaMappingInstance defaultInstance = IterableExtensions.<RosettaMappingInstance>findFirst(element.getInstances(), _function_2);
      final RosettaMappingInstance lastInstance = IterableExtensions.<RosettaMappingInstance>lastOrNull(element.getInstances());
      if ((defaultInstance != lastInstance)) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("Set to without when case must be ordered last.");
        this.error(_builder_1.toString(), element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
      }
    }
    final RosettaType type = this.getContainerType(element);
    if ((type != null)) {
      if (((type instanceof Data) && (!IterableExtensions.isEmpty(IterableExtensions.<RosettaMappingInstance>filter(element.getInstances(), ((Function1<RosettaMappingInstance, Boolean>) (RosettaMappingInstance it) -> {
        RosettaMapTestExpression _set = it.getSet();
        return Boolean.valueOf((_set != null));
      })))))) {
        StringConcatenation _builder_2 = new StringConcatenation();
        _builder_2.append("Set to constant type does not match type of field.");
        this.error(_builder_2.toString(), element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
      } else {
        if ((type instanceof RosettaEnumeration)) {
          final Function1<RosettaMappingInstance, Boolean> _function_3 = (RosettaMappingInstance it) -> {
            RosettaMapTestExpression _set = it.getSet();
            return Boolean.valueOf((_set != null));
          };
          Iterable<RosettaMappingInstance> _filter = IterableExtensions.<RosettaMappingInstance>filter(element.getInstances(), _function_3);
          for (final RosettaMappingInstance inst : _filter) {
            RosettaMapTestExpression _set = inst.getSet();
            boolean _not = (!(_set instanceof RosettaEnumValueReference));
            if (_not) {
              StringConcatenation _builder_3 = new StringConcatenation();
              _builder_3.append("Set to constant type does not match type of field.");
              this.error(_builder_3.toString(), element, 
                RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
            } else {
              RosettaMapTestExpression _set_1 = inst.getSet();
              final RosettaEnumValueReference setEnum = ((RosettaEnumValueReference) _set_1);
              String _name = ((RosettaEnumeration)type).getName();
              String _name_1 = setEnum.getEnumeration().getName();
              boolean _notEquals = (!Objects.equals(_name, _name_1));
              if (_notEquals) {
                StringConcatenation _builder_4 = new StringConcatenation();
                _builder_4.append("Set to constant type does not match type of field.");
                this.error(_builder_4.toString(), element, 
                  RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
              }
            }
          }
        }
      }
    }
  }

  public RosettaType getContainerType(final RosettaMapping element) {
    final EObject container = element.eContainer().eContainer().eContainer();
    if ((container instanceof RosettaExternalRegularAttribute)) {
      final RosettaFeature attributeRef = ((RosettaExternalRegularAttribute)container).getAttributeRef();
      if ((attributeRef instanceof RosettaTyped)) {
        return ((RosettaTyped)attributeRef).getTypeCall().getType();
      }
    } else {
      if ((container instanceof RosettaTyped)) {
        return ((RosettaTyped)container).getTypeCall().getType();
      }
    }
    return null;
  }

  @Check
  public void checkMappingDefaultCase(final RosettaMapping element) {
    final Function1<RosettaMappingInstance, Boolean> _function = (RosettaMappingInstance it) -> {
      return Boolean.valueOf(it.isDefault());
    };
    int _size = IterableExtensions.size(IterableExtensions.<RosettaMappingInstance>filter(element.getInstances(), _function));
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Only one default case allowed.");
      this.error(_builder.toString(), element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
    }
    final Function1<RosettaMappingInstance, Boolean> _function_1 = (RosettaMappingInstance it) -> {
      return Boolean.valueOf(it.isDefault());
    };
    int _size_1 = IterableExtensions.size(IterableExtensions.<RosettaMappingInstance>filter(element.getInstances(), _function_1));
    boolean _equals = (_size_1 == 1);
    if (_equals) {
      final Function1<RosettaMappingInstance, Boolean> _function_2 = (RosettaMappingInstance it) -> {
        return Boolean.valueOf(it.isDefault());
      };
      final RosettaMappingInstance defaultInstance = IterableExtensions.<RosettaMappingInstance>findFirst(element.getInstances(), _function_2);
      final RosettaMappingInstance lastInstance = IterableExtensions.<RosettaMappingInstance>lastOrNull(element.getInstances());
      if ((defaultInstance != lastInstance)) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("Default case must be ordered last.");
        this.error(_builder_1.toString(), element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
      }
    }
  }

  @Check
  public void checkMergeSynonymAttributeCardinality(final Attribute attribute) {
    EList<RosettaSynonym> _synonyms = attribute.getSynonyms();
    for (final RosettaSynonym syn : _synonyms) {
      {
        final boolean multi = attribute.getCard().isIsMany();
        boolean _and = false;
        RosettaSynonymBody _body = syn.getBody();
        RosettaMergeSynonymValue _merge = null;
        if (_body!=null) {
          _merge=_body.getMerge();
        }
        boolean _tripleNotEquals = (_merge != null);
        if (!_tripleNotEquals) {
          _and = false;
        } else {
          _and = (!multi);
        }
        if (_and) {
          this.error("Merge synonym can only be specified on an attribute with multiple cardinality.", syn.getBody(), 
            RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__MERGE);
        }
      }
    }
  }

  @Check
  public void checkMergeSynonymAttributeCardinality(final RosettaExternalRegularAttribute attribute) {
    final RosettaFeature att = attribute.getAttributeRef();
    if ((att instanceof Attribute)) {
      EList<RosettaExternalSynonym> _externalSynonyms = attribute.getExternalSynonyms();
      for (final RosettaExternalSynonym syn : _externalSynonyms) {
        {
          final boolean multi = ((Attribute)att).getCard().isIsMany();
          boolean _and = false;
          RosettaSynonymBody _body = syn.getBody();
          RosettaMergeSynonymValue _merge = null;
          if (_body!=null) {
            _merge=_body.getMerge();
          }
          boolean _tripleNotEquals = (_merge != null);
          if (!_tripleNotEquals) {
            _and = false;
          } else {
            _and = (!multi);
          }
          if (_and) {
            this.error("Merge synonym can only be specified on an attribute with multiple cardinality.", syn.getBody(), 
              RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__MERGE);
          }
        }
      }
    }
  }

  @Check
  public void checkPatternAndFormat(final RosettaExternalRegularAttribute attribute) {
    boolean _isDateTime = this.isDateTime(this._rosettaTypeProvider.getRTypeOfFeature(attribute.getAttributeRef(), attribute).getRType());
    boolean _not = (!_isDateTime);
    if (_not) {
      EList<RosettaExternalSynonym> _externalSynonyms = attribute.getExternalSynonyms();
      for (final RosettaExternalSynonym s : _externalSynonyms) {
        {
          this.checkFormatNull(s.getBody());
          this.checkPatternValid(s.getBody());
        }
      }
    } else {
      EList<RosettaExternalSynonym> _externalSynonyms_1 = attribute.getExternalSynonyms();
      for (final RosettaExternalSynonym s_1 : _externalSynonyms_1) {
        {
          this.checkFormatValid(s_1.getBody());
          this.checkPatternNull(s_1.getBody());
        }
      }
    }
  }

  @Check
  public void checkPatternAndFormat(final Attribute attribute) {
    boolean _isDateTime = this.isDateTime(this._rosettaTypeProvider.getRTypeOfSymbol(attribute).getRType());
    boolean _not = (!_isDateTime);
    if (_not) {
      EList<RosettaSynonym> _synonyms = attribute.getSynonyms();
      for (final RosettaSynonym s : _synonyms) {
        {
          this.checkFormatNull(s.getBody());
          this.checkPatternValid(s.getBody());
        }
      }
    } else {
      EList<RosettaSynonym> _synonyms_1 = attribute.getSynonyms();
      for (final RosettaSynonym s_1 : _synonyms_1) {
        {
          this.checkFormatValid(s_1.getBody());
          this.checkPatternNull(s_1.getBody());
        }
      }
    }
  }

  public void checkFormatNull(final RosettaSynonymBody body) {
    String _format = body.getFormat();
    boolean _tripleNotEquals = (_format != null);
    if (_tripleNotEquals) {
      this.error("Format can only be applied to date/time types", body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__FORMAT);
    }
  }

  public DateTimeFormatter checkFormatValid(final RosettaSynonymBody body) {
    DateTimeFormatter _xifexpression = null;
    String _format = body.getFormat();
    boolean _tripleNotEquals = (_format != null);
    if (_tripleNotEquals) {
      DateTimeFormatter _xtrycatchfinallyexpression = null;
      try {
        _xtrycatchfinallyexpression = DateTimeFormatter.ofPattern(body.getFormat());
      } catch (final Throwable _t) {
        if (_t instanceof IllegalArgumentException) {
          final IllegalArgumentException e = (IllegalArgumentException)_t;
          String _message = e.getMessage();
          String _plus = ("Format must be a valid date/time format - " + _message);
          this.error(_plus, body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__FORMAT);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xifexpression = _xtrycatchfinallyexpression;
    }
    return _xifexpression;
  }

  public void checkPatternNull(final RosettaSynonymBody body) {
    String _patternMatch = body.getPatternMatch();
    boolean _tripleNotEquals = (_patternMatch != null);
    if (_tripleNotEquals) {
      this.error("Pattern cannot be applied to date/time types", body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__PATTERN_MATCH);
    }
  }

  public Pattern checkPatternValid(final RosettaSynonymBody body) {
    Pattern _xifexpression = null;
    String _patternMatch = body.getPatternMatch();
    boolean _tripleNotEquals = (_patternMatch != null);
    if (_tripleNotEquals) {
      Pattern _xtrycatchfinallyexpression = null;
      try {
        _xtrycatchfinallyexpression = Pattern.compile(body.getPatternMatch());
      } catch (final Throwable _t) {
        if (_t instanceof PatternSyntaxException) {
          final PatternSyntaxException e = (PatternSyntaxException)_t;
          String _patternSyntaxErrorMessage = this.getPatternSyntaxErrorMessage(e);
          String _plus = ("Pattern to match must be a valid regular expression - " + _patternSyntaxErrorMessage);
          this.error(_plus, body, 
            RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__PATTERN_MATCH);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xifexpression = _xtrycatchfinallyexpression;
    }
    return _xifexpression;
  }

  private boolean isDateTime(final RType rType) {
    String _name = null;
    if (rType!=null) {
      _name=rType.getName();
    }
    return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("date", "time", "zonedDateTime")).contains(_name);
  }

  @Check
  public void checkPatternOnEnum(final RosettaEnumSynonym synonym) {
    String _patternMatch = synonym.getPatternMatch();
    boolean _tripleNotEquals = (_patternMatch != null);
    if (_tripleNotEquals) {
      try {
        Pattern.compile(synonym.getPatternMatch());
      } catch (final Throwable _t) {
        if (_t instanceof PatternSyntaxException) {
          final PatternSyntaxException e = (PatternSyntaxException)_t;
          String _patternSyntaxErrorMessage = this.getPatternSyntaxErrorMessage(e);
          String _plus = ("Pattern to match must be a valid regular expression - " + _patternSyntaxErrorMessage);
          this.error(_plus, synonym, 
            RosettaPackage.Literals.ROSETTA_ENUM_SYNONYM__PATTERN_MATCH);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    }
  }

  private String getPatternSyntaxErrorMessage(final PatternSyntaxException e) {
    return e.getMessage().replace(System.lineSeparator(), "\n");
  }

  @Check
  public void checkFuncDispatchAttr(final FunctionDispatch ele) {
    if ((this._rosettaEcoreUtil.isResolved(ele.getAttribute()) && this._rosettaEcoreUtil.isResolved(ele.getAttribute().getTypeCall()))) {
      RosettaType _type = ele.getAttribute().getTypeCall().getType();
      boolean _not = (!(_type instanceof RosettaEnumeration));
      if (_not) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Dispatching function may refer to an enumeration typed attributes only. Current type is ");
        String _name = ele.getAttribute().getTypeCall().getType().getName();
        _builder.append(_name);
        this.error(_builder.toString(), ele, 
          SimplePackage.Literals.FUNCTION_DISPATCH__ATTRIBUTE);
      }
    }
  }

  @Check
  public void checkDispatch(final com.regnosys.rosetta.rosetta.simple.Function ele) {
    if ((ele instanceof FunctionDispatch)) {
      return;
    }
    final List<FunctionDispatch> dispath = IterableExtensions.<FunctionDispatch>toList(this._rosettaFunctionExtensions.getDispatchingFunctions(ele));
    boolean _isEmpty = dispath.isEmpty();
    if (_isEmpty) {
      return;
    }
    final LinkedHashMultimap<RosettaEnumeration, Pair<String, FunctionDispatch>> enumsUsed = LinkedHashMultimap.<RosettaEnumeration, Pair<String, FunctionDispatch>>create();
    final Consumer<FunctionDispatch> _function = (FunctionDispatch it) -> {
      final RosettaEnumValueReference enumRef = it.getValue();
      if ((((enumRef != null) && (enumRef.getEnumeration() != null)) && (enumRef.getValue() != null))) {
        RosettaEnumeration _enumeration = enumRef.getEnumeration();
        String _name = enumRef.getValue().getName();
        Pair<String, FunctionDispatch> _mappedTo = Pair.<String, FunctionDispatch>of(_name, it);
        enumsUsed.put(_enumeration, _mappedTo);
      }
    };
    dispath.forEach(_function);
    final Function1<RosettaEnumeration, Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>> _function_1 = (RosettaEnumeration it) -> {
      Set<Pair<String, FunctionDispatch>> _get = enumsUsed.get(it);
      return Pair.<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>of(it, _get);
    };
    final Function1<Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>, Boolean> _function_2 = (Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>> it) -> {
      return Boolean.valueOf(((it == null) || (it.getValue() == null)));
    };
    final Iterable<Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>> structured = IterableExtensions.<Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>>filter(IterableExtensions.<RosettaEnumeration, Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>>map(enumsUsed.keys(), _function_1), _function_2);
    boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(structured);
    if (_isNullOrEmpty) {
      return;
    }
    final Comparator<Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>> _function_3 = (Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>> $0, Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>> $1) -> {
      int _size = $0.getValue().size();
      int _size_1 = $1.getValue().size();
      return (Integer.valueOf(_size).compareTo(Integer.valueOf(_size_1)));
    };
    final RosettaEnumeration mostUsedEnum = IterableExtensions.<Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>>max(structured, _function_3).getKey();
    final Function1<RosettaEnumValue, String> _function_4 = (RosettaEnumValue it) -> {
      return it.getName();
    };
    final Set<String> toImplement = IterableExtensions.<String>toSet(ListExtensions.<RosettaEnumValue, String>map(this.objectFactory.buildREnumType(mostUsedEnum).getAllEnumValues(), _function_4));
    final Consumer<Pair<String, FunctionDispatch>> _function_5 = (Pair<String, FunctionDispatch> it) -> {
      toImplement.remove(it.getKey());
    };
    enumsUsed.get(mostUsedEnum).forEach(_function_5);
    boolean _isEmpty_1 = toImplement.isEmpty();
    boolean _not = (!_isEmpty_1);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Missing implementation for ");
      String _name = mostUsedEnum.getName();
      _builder.append(_name);
      _builder.append(": ");
      String _join = IterableExtensions.join(IterableExtensions.<String>sort(toImplement), ", ");
      _builder.append(_join);
      this.warning(_builder.toString(), ele, 
        RosettaPackage.Literals.ROSETTA_NAMED__NAME);
    }
    final Consumer<Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>>> _function_6 = (Pair<RosettaEnumeration, Set<Pair<String, FunctionDispatch>>> it) -> {
      RosettaEnumeration _key = it.getKey();
      boolean _notEquals = (!Objects.equals(_key, mostUsedEnum));
      if (_notEquals) {
        final Consumer<Pair<String, FunctionDispatch>> _function_7 = (Pair<String, FunctionDispatch> entry) -> {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("Wrong ");
          String _name_1 = it.getKey().getName();
          _builder_1.append(_name_1);
          _builder_1.append(" enumeration used. Expecting ");
          String _name_2 = mostUsedEnum.getName();
          _builder_1.append(_name_2);
          _builder_1.append(".");
          this.error(_builder_1.toString(), entry.getValue().getValue(), 
            RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION);
        };
        it.getValue().forEach(_function_7);
      } else {
        final Function1<Pair<String, FunctionDispatch>, String> _function_8 = (Pair<String, FunctionDispatch> it_1) -> {
          return it_1.getKey();
        };
        final Function2<String, List<Pair<String, FunctionDispatch>>, Boolean> _function_9 = (String enumVal, List<Pair<String, FunctionDispatch>> entries) -> {
          int _size = entries.size();
          return Boolean.valueOf((_size > 1));
        };
        final BiConsumer<String, List<Pair<String, FunctionDispatch>>> _function_10 = (String enumVal, List<Pair<String, FunctionDispatch>> entries) -> {
          final Consumer<Pair<String, FunctionDispatch>> _function_11 = (Pair<String, FunctionDispatch> it_1) -> {
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append("Dupplicate usage of ");
            String _key_1 = it_1.getKey();
            _builder_1.append(_key_1);
            _builder_1.append(" enumeration value.");
            this.error(_builder_1.toString(), it_1.getValue().getValue(), 
              RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__VALUE);
          };
          entries.forEach(_function_11);
        };
        MapExtensions.<String, List<Pair<String, FunctionDispatch>>>filter(IterableExtensions.<String, Pair<String, FunctionDispatch>>groupBy(it.getValue(), _function_8), _function_9).forEach(_function_10);
      }
    };
    structured.forEach(_function_6);
  }

  @Check
  public void checkConditionDontUseOutput(final com.regnosys.rosetta.rosetta.simple.Function ele) {
    final Function1<Condition, Boolean> _function = (Condition it) -> {
      boolean _isPostCondition = it.isPostCondition();
      return Boolean.valueOf((!_isPostCondition));
    };
    final Consumer<Condition> _function_1 = (Condition cond) -> {
      final RosettaExpression expr = cond.getExpression();
      if ((expr != null)) {
        final Stack<String> trace = new Stack<String>();
        final List<RosettaSymbol> outRef = this.exprHelper.findOutputRef(expr, trace);
        boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(outRef);
        boolean _not = (!_isNullOrEmpty);
        if (_not) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("output \'");
          String _name = IterableExtensions.<RosettaSymbol>head(outRef).getName();
          _builder.append(_name);
          _builder.append("\' or alias on output \'");
          String _name_1 = IterableExtensions.<RosettaSymbol>head(outRef).getName();
          _builder.append(_name_1);
          _builder.append("\' not allowed in condition blocks.");
          _builder.newLineIfNotEmpty();
          {
            boolean _isEmpty = trace.isEmpty();
            boolean _not_1 = (!_isEmpty);
            if (_not_1) {
              String _join = IterableExtensions.join(trace, " > ");
              _builder.append(_join);
              _builder.append(" > ");
              String _name_2 = IterableExtensions.<RosettaSymbol>head(outRef).getName();
              _builder.append(_name_2);
            }
          }
          this.error(_builder.toString(), expr, null);
        }
      }
    };
    IterableExtensions.<Condition>filter(ele.getConditions(), _function).forEach(_function_1);
  }

  @Check
  public void checkAssignAnAlias(final Operation ele) {
    if (((ele.getPath() == null) && (ele.getAssignRoot() instanceof ShortcutDeclaration))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("An alias can not be assigned. Assign target must be an attribute.");
      this.error(_builder.toString(), ele, SimplePackage.Literals.OPERATION__ASSIGN_ROOT);
    }
  }

  @Check
  public void checkSynonymMapPath(final RosettaMapPathValue ele) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(ele.getPath());
    boolean _not = (!_isNullOrEmpty);
    if (_not) {
      final Pair<Character, Boolean> invalidChar = this.checkPathChars(ele.getPath());
      if ((invalidChar != null)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Character \'");
        Character _key = invalidChar.getKey();
        _builder.append(_key);
        _builder.append("\' is not allowed ");
        {
          Boolean _value = invalidChar.getValue();
          if ((_value).booleanValue()) {
            _builder.append("as first symbol in a path segment.");
          } else {
            _builder.append("in paths. Use \'->\' to separate path segments.");
          }
        }
        this.error(_builder.toString(), ele, RosettaPackage.Literals.ROSETTA_MAP_PATH_VALUE__PATH);
      }
    }
  }

  @Check
  public void checkSynonyValuePath(final RosettaSynonymValueBase ele) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(ele.getPath());
    boolean _not = (!_isNullOrEmpty);
    if (_not) {
      final Pair<Character, Boolean> invalidChar = this.checkPathChars(ele.getPath());
      if ((invalidChar != null)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Character \'");
        Character _key = invalidChar.getKey();
        _builder.append(_key);
        _builder.append("\' is not allowed ");
        {
          Boolean _value = invalidChar.getValue();
          if ((_value).booleanValue()) {
            _builder.append("as first symbol in a path segment.");
          } else {
            _builder.append("in paths. Use \'->\' to separate path segments.");
          }
        }
        this.error(_builder.toString(), ele, RosettaPackage.Literals.ROSETTA_SYNONYM_VALUE_BASE__PATH);
      }
    }
  }

  @Check
  public void checkToStringOpArgument(final ToStringOperation ele) {
    final RosettaExpression arg = ele.getArgument();
    boolean _isResolved = this._rosettaEcoreUtil.isResolved(arg);
    if (_isResolved) {
      boolean _isMulti = this.cardinality.isMulti(arg);
      if (_isMulti) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("The argument of ");
        String _operator = ele.getOperator();
        _builder.append(_operator);
        _builder.append(" should be of singular cardinality.");
        this.error(_builder.toString(), ele, ExpressionPackage.Literals.ROSETTA_UNARY_OPERATION__ARGUMENT);
      }
      final RType type = this._typeSystem.stripFromTypeAliases(this._rosettaTypeProvider.getRMetaAnnotatedType(arg).getRType());
      if ((!(((type instanceof RBasicType) || (type instanceof RRecordType)) || (type instanceof REnumType)))) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("The argument of ");
        String _operator_1 = ele.getOperator();
        _builder_1.append(_operator_1);
        _builder_1.append(" should be of a builtin type or an enum.");
        this.error(_builder_1.toString(), ele, ExpressionPackage.Literals.ROSETTA_UNARY_OPERATION__ARGUMENT);
      }
    }
  }

  @Check
  public void checkFunctionPrefix(final com.regnosys.rosetta.rosetta.simple.Function ele) {
    final Consumer<AnnotationRef> _function = (AnnotationRef a) -> {
      final String prefix = a.getAnnotation().getPrefix();
      if (((prefix != null) && (!ele.getName().startsWith((prefix + "_"))))) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Function name ");
        String _name = ele.getName();
        _builder.append(_name);
        _builder.append(" must have prefix \'");
        _builder.append(prefix);
        _builder.append("\' followed by an underscore.");
        this.warning(_builder.toString(), 
          RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
      }
    };
    ele.getAnnotations().forEach(_function);
  }

  @Check
  public void checkMetadataAnnotation(final Annotated ele) {
    final List<AnnotationRef> metadatas = this._rosettaFunctionExtensions.getMetadataAnnotations(ele);
    final Consumer<AnnotationRef> _function = (AnnotationRef it) -> {
      Attribute _attribute = it.getAttribute();
      String _name = null;
      if (_attribute!=null) {
        _name=_attribute.getName();
      }
      if (_name != null) {
        switch (_name) {
          case "key":
            if ((!(ele instanceof Data))) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("[metadata key] annotation only allowed on a type.");
              this.error(_builder.toString(), it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            }
            break;
          case "id":
            if ((!(ele instanceof Attribute))) {
              StringConcatenation _builder_1 = new StringConcatenation();
              _builder_1.append("[metadata id] annotation only allowed on an attribute.");
              this.error(_builder_1.toString(), it, 
                SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            }
            break;
          case "reference":
            if ((!(ele instanceof Attribute))) {
              StringConcatenation _builder_2 = new StringConcatenation();
              _builder_2.append("[metadata reference] annotation only allowed on an attribute.");
              this.error(_builder_2.toString(), it, 
                SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            }
            break;
          case "scheme":
            if ((!((ele instanceof Attribute) || (ele instanceof Data)))) {
              StringConcatenation _builder_3 = new StringConcatenation();
              _builder_3.append("[metadata scheme] annotation only allowed on an attribute or a type.");
              this.error(_builder_3.toString(), it, 
                SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            }
            break;
          case "template":
            if ((!(ele instanceof Data))) {
              StringConcatenation _builder_4 = new StringConcatenation();
              _builder_4.append("[metadata template] annotation only allowed on a type.");
              this.error(_builder_4.toString(), it, 
                SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            } else {
              final Function1<AnnotationRef, String> _function_1 = (AnnotationRef it_1) -> {
                Attribute _attribute_1 = it_1.getAttribute();
                String _name_1 = null;
                if (_attribute_1!=null) {
                  _name_1=_attribute_1.getName();
                }
                return _name_1;
              };
              boolean _contains = ListExtensions.<AnnotationRef, String>map(metadatas, _function_1).contains("key");
              boolean _not = (!_contains);
              if (_not) {
                StringConcatenation _builder_5 = new StringConcatenation();
                _builder_5.append("Types with [metadata template] annotation must also specify the [metadata key] annotation.");
                this.error(_builder_5.toString(), it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
              }
            }
            break;
          case "location":
            if ((ele instanceof Attribute)) {
              final Function1<AnnotationQualifier, Boolean> _function_2 = (AnnotationQualifier it_1) -> {
                String _qualName = it_1.getQualName();
                return Boolean.valueOf(Objects.equals(_qualName, "pointsTo"));
              };
              boolean _exists = IterableExtensions.<AnnotationQualifier>exists(it.getQualifiers(), _function_2);
              if (_exists) {
                StringConcatenation _builder_6 = new StringConcatenation();
                _builder_6.append("pointsTo qualifier belongs on the address not the location.");
                this.error(_builder_6.toString(), it, 
                  SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
              }
            } else {
              StringConcatenation _builder_7 = new StringConcatenation();
              _builder_7.append("[metadata location] annotation only allowed on an attribute.");
              this.error(_builder_7.toString(), it, 
                SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            }
            break;
          case "address":
            if ((ele instanceof Attribute)) {
              final Consumer<AnnotationQualifier> _function_3 = (AnnotationQualifier it_1) -> {
                String _qualName = it_1.getQualName();
                boolean _equals = Objects.equals(_qualName, "pointsTo");
                if (_equals) {
                  RosettaAttributeReferenceSegment _qualPath = it_1.getQualPath();
                  boolean _matched = false;
                  if (_qualPath instanceof RosettaAttributeReference) {
                    _matched=true;
                    RosettaAttributeReferenceSegment _qualPath_1 = it_1.getQualPath();
                    final RosettaAttributeReference attrRef = ((RosettaAttributeReference) _qualPath_1);
                    boolean _isResolved = this._rosettaEcoreUtil.isResolved(attrRef.getAttribute());
                    if (_isResolved) {
                      this.checkForLocation(attrRef.getAttribute(), it_1);
                      final RType targetType = this._typeSystem.typeCallToRType(attrRef.getAttribute().getTypeCall());
                      final RType thisType = this._rosettaTypeProvider.getRTypeOfFeature(((RosettaFeature)ele), null).getRType();
                      boolean _isSubtypeOf = this._typeSystem.isSubtypeOf(thisType, targetType);
                      boolean _not_1 = (!_isSubtypeOf);
                      if (_not_1) {
                        StringConcatenation _builder_8 = new StringConcatenation();
                        _builder_8.append("Expected address target type of \'");
                        String _name_1 = thisType.getName();
                        _builder_8.append(_name_1);
                        _builder_8.append("\' but was \'");
                        String _elvis = null;
                        String _name_2 = null;
                        if (targetType!=null) {
                          _name_2=targetType.getName();
                        }
                        if (_name_2 != null) {
                          _elvis = _name_2;
                        } else {
                          _elvis = "null";
                        }
                        _builder_8.append(_elvis);
                        _builder_8.append("\'");
                        this.error(_builder_8.toString(), it_1, SimplePackage.Literals.ANNOTATION_QUALIFIER__QUAL_PATH, RosettaIssueCodes.TYPE_ERROR);
                      }
                    }
                  }
                  if (!_matched) {
                    StringConcatenation _builder_8 = new StringConcatenation();
                    _builder_8.append("Target of an address must be an attribute");
                    this.error(_builder_8.toString(), it_1, 
                      SimplePackage.Literals.ANNOTATION_QUALIFIER__QUAL_PATH, RosettaIssueCodes.TYPE_ERROR);
                  }
                }
              };
              it.getQualifiers().forEach(_function_3);
            } else {
              StringConcatenation _builder_8 = new StringConcatenation();
              _builder_8.append("[metadata address] annotation only allowed on an attribute.");
              this.error(_builder_8.toString(), it, 
                SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
            }
            break;
        }
      }
    };
    metadatas.forEach(_function);
  }

  private void checkForLocation(final Attribute attribute, final AnnotationQualifier checked) {
    final Function1<AnnotationRef, Boolean> _function = (AnnotationRef it) -> {
      Attribute _attribute = it.getAttribute();
      String _name = null;
      if (_attribute!=null) {
        _name=_attribute.getName();
      }
      return Boolean.valueOf(Objects.equals(_name, "location"));
    };
    boolean _isEmpty = IterableExtensions.isEmpty(IterableExtensions.<AnnotationRef>filter(this._rosettaFunctionExtensions.getMetadataAnnotations(attribute), _function));
    boolean locationFound = (!_isEmpty);
    if ((!locationFound)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Target of address must be annotated with metadata location");
      this.error(_builder.toString(), checked, 
        SimplePackage.Literals.ANNOTATION_QUALIFIER__QUAL_PATH);
    }
  }

  @Check
  public void checkTransformAnnotations(final Annotated ele) {
    final List<AnnotationRef> annotations = this._rosettaFunctionExtensions.getTransformAnnotations(ele);
    boolean _isEmpty = annotations.isEmpty();
    if (_isEmpty) {
      return;
    }
    if ((!(ele instanceof com.regnosys.rosetta.rosetta.simple.Function))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Transformation annotations only allowed on a function.");
      this.error(_builder.toString(), RosettaPackage.Literals.ROSETTA_NAMED__NAME);
    }
    int _size = annotations.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      final Consumer<AnnotationRef> _function = (AnnotationRef it) -> {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("Only one transform annotation allowed.");
        this.error(_builder_1.toString(), it, null);
      };
      annotations.stream().skip(1).forEach(_function);
    }
    final AnnotationRef annotationRef = annotations.get(0);
    String _name = annotationRef.getAnnotation().getName();
    boolean _equals = Objects.equals(_name, "ingest");
    if (_equals) {
      Attribute _attribute = annotationRef.getAttribute();
      boolean _tripleEquals = (_attribute == null);
      if (_tripleEquals) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("The `ingest` annotation must have a source format such as JSON or XML");
        this.error(_builder_1.toString(), annotationRef, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
      }
    }
    String _name_1 = annotationRef.getAnnotation().getName();
    boolean _equals_1 = Objects.equals(_name_1, "projection");
    if (_equals_1) {
      Attribute _attribute_1 = annotationRef.getAttribute();
      boolean _tripleEquals_1 = (_attribute_1 == null);
      if (_tripleEquals_1) {
        StringConcatenation _builder_2 = new StringConcatenation();
        _builder_2.append("The `projection` annotation must have a target format such as JSON or XML");
        this.error(_builder_2.toString(), annotationRef, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
      }
    }
  }

  @Check
  public void checkCreationAnnotation(final Annotated ele) {
    final List<AnnotationRef> annotations = this._rosettaFunctionExtensions.getCreationAnnotations(ele);
    boolean _isEmpty = annotations.isEmpty();
    if (_isEmpty) {
      return;
    }
    if ((!(ele instanceof com.regnosys.rosetta.rosetta.simple.Function))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Creation annotation only allowed on a function.");
      this.error(_builder.toString(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
      return;
    }
    int _size = annotations.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Only 1 creation annotation allowed.");
      this.error(_builder_1.toString(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
      return;
    }
    final com.regnosys.rosetta.rosetta.simple.Function func = ((com.regnosys.rosetta.rosetta.simple.Function) ele);
    RType annotationType = this._rosettaTypeProvider.getRTypeOfSymbol(IterableExtensions.<AnnotationRef>head(annotations).getAttribute()).getRType();
    if ((annotationType instanceof RChoiceType)) {
      annotationType = ((RChoiceType)annotationType).asRDataType();
    }
    RType funcOutputType = this._rosettaTypeProvider.getRTypeOfSymbol(func).getRType();
    if ((funcOutputType instanceof RChoiceType)) {
      funcOutputType = ((RChoiceType)funcOutputType).asRDataType();
    }
    if (((annotationType instanceof RDataType) && (funcOutputType instanceof RDataType))) {
      final RDataType annotationDataType = ((RDataType) annotationType);
      final RDataType funcOutputDataType = ((RDataType) funcOutputType);
      final Set<RDataType> funcOutputSuperTypes = IterableExtensions.<RDataType>toSet(funcOutputDataType.getAllSuperTypes());
      final Function1<RAttribute, Class<RType>> _function = (RAttribute it) -> {
        return RType.class;
      };
      final List<Class<RType>> annotationAttributeTypes = IterableExtensions.<Class<RType>>toList(IterableExtensions.<RAttribute, Class<RType>>map(annotationDataType.getAllAttributes(), _function));
      if ((((!Objects.equals(annotationDataType, funcOutputDataType)) && (!funcOutputSuperTypes.contains(annotationDataType))) && (!annotationAttributeTypes.contains(funcOutputDataType)))) {
        StringConcatenation _builder_2 = new StringConcatenation();
        _builder_2.append("Invalid output type for creation annotation.  The output type must match the type specified in the annotation \'");
        String _name = annotationDataType.getName();
        _builder_2.append(_name);
        _builder_2.append("\' (or extend the annotation type, or be a sub-type as part of a one-of condition).");
        this.warning(_builder_2.toString(), func, SimplePackage.Literals.FUNCTION__OUTPUT);
      }
    }
  }

  @Check
  public void checkQualificationAnnotation(final Annotated ele) {
    final List<AnnotationRef> annotations = this._rosettaFunctionExtensions.getQualifierAnnotations(ele);
    boolean _isEmpty = annotations.isEmpty();
    if (_isEmpty) {
      return;
    }
    if ((!(ele instanceof com.regnosys.rosetta.rosetta.simple.Function))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Qualification annotation only allowed on a function.");
      this.error(_builder.toString(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
      return;
    }
    final com.regnosys.rosetta.rosetta.simple.Function func = ((com.regnosys.rosetta.rosetta.simple.Function) ele);
    int _size = annotations.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Only 1 qualification annotation allowed.");
      this.error(_builder_1.toString(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
      return;
    }
    final List<Attribute> inputs = this._rosettaFunctionExtensions.getInputs(func);
    if ((IterableExtensions.isNullOrEmpty(inputs) || (inputs.size() != 1))) {
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("Qualification functions must have exactly 1 input.");
      this.error(_builder_2.toString(), func, SimplePackage.Literals.FUNCTION__INPUTS);
      return;
    }
    TypeCall _typeCall = inputs.get(0).getTypeCall();
    RosettaType _type = null;
    if (_typeCall!=null) {
      _type=_typeCall.getType();
    }
    final RosettaType inputType = _type;
    boolean _isResolved = this._rosettaEcoreUtil.isResolved(inputType);
    boolean _not = (!_isResolved);
    if (_not) {
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("Invalid input type for qualification function.");
      this.error(_builder_3.toString(), func, SimplePackage.Literals.FUNCTION__INPUTS);
    } else {
      boolean _isRootEventOrProduct = this.confExtensions.isRootEventOrProduct(inputType);
      boolean _not_1 = (!_isRootEventOrProduct);
      if (_not_1) {
        StringConcatenation _builder_4 = new StringConcatenation();
        _builder_4.append("Input type does not match qualification root type.");
        this.warning(_builder_4.toString(), func, SimplePackage.Literals.FUNCTION__INPUTS);
      }
    }
    Attribute _output = func.getOutput();
    TypeCall _typeCall_1 = null;
    if (_output!=null) {
      _typeCall_1=_output.getTypeCall();
    }
    RType _typeCallToRType = null;
    if (_typeCall_1!=null) {
      _typeCallToRType=this._typeSystem.typeCallToRType(_typeCall_1);
    }
    boolean _notEquals = (!Objects.equals(_typeCallToRType, this._rBuiltinTypeService.BOOLEAN));
    if (_notEquals) {
      StringConcatenation _builder_5 = new StringConcatenation();
      _builder_5.append("Qualification functions must output a boolean.");
      this.error(_builder_5.toString(), func, SimplePackage.Literals.FUNCTION__OUTPUT);
    }
  }

  private Pair<Character, Boolean> checkPathChars(final String str) {
    final String[] segments = str.split("->");
    for (final String segment : segments) {
      int _length = segment.length();
      boolean _greaterThan = (_length > 0);
      if (_greaterThan) {
        boolean _isJavaIdentifierStart = Character.isJavaIdentifierStart(segment.charAt(0));
        boolean _not = (!_isJavaIdentifierStart);
        if (_not) {
          char _charAt = segment.charAt(0);
          return Pair.<Character, Boolean>of(Character.valueOf(_charAt), Boolean.valueOf(true));
        }
        final Function1<Character, Boolean> _function = (Character it) -> {
          boolean _isJavaIdentifierPart = Character.isJavaIdentifierPart((it).charValue());
          return Boolean.valueOf((!_isJavaIdentifierPart));
        };
        final Character notValid = IterableExtensions.<Character>findFirst(((Iterable<Character>)Conversions.doWrapArray(segment.toCharArray())), _function);
        if ((notValid != null)) {
          return Pair.<Character, Boolean>of(notValid, Boolean.valueOf(false));
        }
      }
    }
    return null;
  }

  @Check
  public void checkUnaryOperation(final RosettaUnaryOperation e) {
    final RosettaExpression receiver = e.getArgument();
    if ((((e instanceof ListOperation) && this._rosettaEcoreUtil.isResolved(receiver)) && (!this.cardinality.isMulti(receiver)))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("List ");
      String _operator = e.getOperator();
      _builder.append(_operator);
      _builder.append(" operation cannot be used for single cardinality expressions.");
      this.warning(_builder.toString(), e, 
        ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
    }
    if ((((!(e instanceof CanHandleListOfLists)) && (receiver != null)) && this.cardinality.isOutputListOfLists(receiver))) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("List must be flattened before ");
      String _operator_1 = e.getOperator();
      _builder_1.append(_operator_1);
      _builder_1.append(" operation.");
      this.error(_builder_1.toString(), e, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
    }
  }

  @Check
  public void checkInlineFunction(final InlineFunction f) {
    RosettaExpression _body = f.getBody();
    boolean _tripleEquals = (_body == null);
    if (_tripleEquals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Missing function body.");
      this.error(_builder.toString(), f, null);
    }
  }

  @Check
  public void checkMandatoryFunctionalOperation(final MandatoryFunctionalOperation e) {
    this.checkBodyExists(e);
  }

  @Check
  public void checkUnaryFunctionalOperation(final UnaryFunctionalOperation e) {
    this.checkOptionalNamedParameter(e.getFunction());
  }

  @Check
  public void checkFilterOperation(final FilterOperation o) {
    this.checkBodyType(o.getFunction(), this._rBuiltinTypeService.BOOLEAN);
    this.checkBodyIsSingleCardinality(o.getFunction());
  }

  @Check
  public void checkMapOperation(final MapOperation o) {
    boolean _isOutputListOfListOfLists = this.cardinality.isOutputListOfListOfLists(o);
    if (_isOutputListOfListOfLists) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Each list item is already a list, mapping the item into a list of lists is not allowed. List map item expression must maintain existing cardinality (e.g. list to list), or reduce to single cardinality (e.g. list to single using expression such as count, sum etc).");
      this.error(_builder.toString(), o, ExpressionPackage.Literals.ROSETTA_FUNCTIONAL_OPERATION__FUNCTION);
    }
  }

  @Check
  public void checkFlattenOperation(final FlattenOperation o) {
    boolean _isOutputListOfLists = this.cardinality.isOutputListOfLists(o.getArgument());
    boolean _not = (!_isOutputListOfLists);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("List flatten only allowed for list of lists.");
      this.error(_builder.toString(), o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
    }
  }

  @Check
  public void checkReduceOperation(final ReduceOperation o) {
    this.checkNumberOfMandatoryNamedParameters(o.getFunction(), 2);
    boolean _isSubtypeOf = this._typeSystem.isSubtypeOf(this._rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument()), this._rosettaTypeProvider.getRMetaAnnotatedType(o.getFunction().getBody()));
    boolean _not = (!_isSubtypeOf);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("List reduce expression must evaluate to the same type as the input. Found types ");
      RType _rType = this._rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument()).getRType();
      _builder.append(_rType);
      _builder.append(" and ");
      RType _rType_1 = this._rosettaTypeProvider.getRMetaAnnotatedType(o.getFunction().getBody()).getRType();
      _builder.append(_rType_1);
      _builder.append(".");
      this.error(_builder.toString(), o, ExpressionPackage.Literals.ROSETTA_FUNCTIONAL_OPERATION__FUNCTION);
    }
    this.checkBodyIsSingleCardinality(o.getFunction());
  }

  @Check
  public void checkNumberReducerOperation(final SumOperation o) {
    this.checkInputType(o, this._rBuiltinTypeService.UNCONSTRAINED_NUMBER_WITH_NO_META);
  }

  @Check
  public void checkComparingFunctionalOperation(final ComparingFunctionalOperation o) {
    this.checkBodyIsSingleCardinality(o.getFunction());
    this.checkBodyIsComparable(o);
    InlineFunction _function = o.getFunction();
    boolean _tripleEquals = (_function == null);
    if (_tripleEquals) {
      this.checkInputIsComparable(o);
    }
  }

  @Check
  public void checkAsKeyOperation(final AsKeyOperation o) {
    final EObject container = o.eContainer();
    if ((container instanceof Operation)) {
      Segment _path = ((Operation)container).getPath();
      boolean _tripleEquals = (_path == null);
      if (_tripleEquals) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("\'");
        String _operator = o.getOperator();
        _builder.append(_operator);
        _builder.append("\' can only be used when assigning an attribute. Example: \"set out -> attribute: value as-key\"");
        this.error(_builder.toString(), o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        return;
      }
      final EList<Segment> segments = ((Operation)container).getPath().asSegmentList(((Operation)container).getPath());
      Segment _lastOrNull = null;
      if (segments!=null) {
        _lastOrNull=IterableExtensions.<Segment>lastOrNull(segments);
      }
      RosettaFeature _feature = null;
      if (_lastOrNull!=null) {
        _feature=_lastOrNull.getFeature();
      }
      final RosettaFeature feature = _feature;
      if (((feature instanceof RosettaMetaType) || (!this._rosettaEcoreUtil.hasReferenceAnnotation(((Attribute) feature))))) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("\'");
        String _operator_1 = o.getOperator();
        _builder_1.append(_operator_1);
        _builder_1.append("\' can only be used with attributes annotated with [metadata reference] annotation.");
        this.error(_builder_1.toString(), o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
      }
    } else {
      if ((container instanceof ConstructorKeyValuePair)) {
        final RosettaFeature attr = ((ConstructorKeyValuePair)container).getKey();
        if (((!(attr instanceof Attribute)) || (!this._rosettaEcoreUtil.hasReferenceAnnotation(((Attribute) attr))))) {
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("\'");
          String _operator_2 = o.getOperator();
          _builder_2.append(_operator_2);
          _builder_2.append("\' can only be used with attributes annotated with [metadata reference] annotation.");
          this.error(_builder_2.toString(), o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
      } else {
        StringConcatenation _builder_3 = new StringConcatenation();
        _builder_3.append("\'");
        String _operator_3 = o.getOperator();
        _builder_3.append(_operator_3);
        _builder_3.append("\' may only be used in context of an attribute.\"");
        this.error(_builder_3.toString(), o, 
          ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
      }
    }
  }

  @Check
  public void checkImport(final RosettaModel model) {
    EList<Import> _imports = model.getImports();
    for (final Import ns : _imports) {
      String _importedNamespace = ns.getImportedNamespace();
      boolean _tripleNotEquals = (_importedNamespace != null);
      if (_tripleNotEquals) {
        final QualifiedName qn = QualifiedName.create(ns.getImportedNamespace().split("\\."));
        final boolean isWildcard = qn.getLastSegment().equals("*");
        if (((!isWildcard) && (ns.getNamespaceAlias() != null))) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("\"as\" statement can only be used with wildcard imports");
          this.error(_builder.toString(), ns, RosettaPackage.Literals.IMPORT__NAMESPACE_ALIAS);
        }
      }
    }
    final List<Import> unused = this.importManagementService.findUnused(model);
    for (final Import ns_1 : unused) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Unused import ");
      String _importedNamespace_1 = ns_1.getImportedNamespace();
      _builder_1.append(_importedNamespace_1);
      this.warning(_builder_1.toString(), ns_1, RosettaPackage.Literals.IMPORT__IMPORTED_NAMESPACE, RosettaIssueCodes.UNUSED_IMPORT);
    }
    final List<Import> duplicates = this.importManagementService.findDuplicates(model.getImports());
    for (final Import imp : duplicates) {
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("Duplicate import ");
      String _importedNamespace_2 = imp.getImportedNamespace();
      _builder_2.append(_importedNamespace_2);
      this.warning(_builder_2.toString(), imp, RosettaPackage.Literals.IMPORT__IMPORTED_NAMESPACE, RosettaIssueCodes.DUPLICATE_IMPORT);
    }
  }

  private void checkBodyExists(final RosettaFunctionalOperation operation) {
    InlineFunction _function = operation.getFunction();
    boolean _tripleEquals = (_function == null);
    if (_tripleEquals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Missing an expression.");
      this.error(_builder.toString(), operation, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
    }
  }

  private void checkOptionalNamedParameter(final InlineFunction ref) {
    if (((((ref != null) && (ref.getParameters() != null)) && (ref.getParameters().size() != 0)) && (ref.getParameters().size() != 1))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Function must have 1 named parameter.");
      this.error(_builder.toString(), ref, ExpressionPackage.Literals.INLINE_FUNCTION__PARAMETERS);
    }
  }

  private void checkNumberOfMandatoryNamedParameters(final InlineFunction ref, final int max) {
    if ((((ref != null) && (ref.getParameters() == null)) || (ref.getParameters().size() != max))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Function must have ");
      _builder.append(max);
      _builder.append(" named parameter");
      {
        if ((max > 1)) {
          _builder.append("s");
        }
      }
      _builder.append(".");
      this.error(_builder.toString(), ref, ExpressionPackage.Literals.INLINE_FUNCTION__PARAMETERS);
    }
  }

  private void checkInputType(final RosettaUnaryOperation o, final RMetaAnnotatedType type) {
    boolean _isSubtypeOf = this._typeSystem.isSubtypeOf(this._rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument()), type);
    boolean _not = (!_isSubtypeOf);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Input type must be a ");
      _builder.append(type);
      _builder.append(".");
      this.error(_builder.toString(), o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
    }
  }

  private void checkInputIsComparable(final RosettaUnaryOperation o) {
    final RMetaAnnotatedType inputRType = this._rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument());
    boolean _hasNaturalOrder = inputRType.getRType().hasNaturalOrder();
    boolean _not = (!_hasNaturalOrder);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Operation ");
      String _operator = o.getOperator();
      _builder.append(_operator);
      _builder.append(" only supports comparable types (string, int, number, boolean, date). Found type ");
      String _name = inputRType.getRType().getName();
      _builder.append(_name);
      _builder.append(".");
      this.error(_builder.toString(), o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
    }
  }

  private void checkBodyIsSingleCardinality(final InlineFunction ref) {
    if (((ref != null) && this.cardinality.isBodyExpressionMulti(ref))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Operation only supports single cardinality expressions.");
      this.error(_builder.toString(), ref, null);
    }
  }

  private void checkBodyType(final InlineFunction ref, final RType type) {
    RosettaExpression _body = null;
    if (ref!=null) {
      _body=ref.getBody();
    }
    RMetaAnnotatedType _rMetaAnnotatedType = null;
    if (_body!=null) {
      _rMetaAnnotatedType=this._rosettaTypeProvider.getRMetaAnnotatedType(_body);
    }
    RType _rType = null;
    if (_rMetaAnnotatedType!=null) {
      _rType=_rMetaAnnotatedType.getRType();
    }
    final RType bodyType = _rType;
    if ((((ref != null) && (bodyType != null)) && (!Objects.equals(bodyType, type)))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Expression must evaluate to a ");
      String _name = type.getName();
      _builder.append(_name);
      _builder.append(".");
      this.error(_builder.toString(), ref, null);
    }
  }

  private void checkBodyIsComparable(final RosettaFunctionalOperation op) {
    final InlineFunction ref = op.getFunction();
    if ((ref != null)) {
      final RMetaAnnotatedType bodyRType = this._rosettaTypeProvider.getRMetaAnnotatedType(ref.getBody());
      boolean _hasNaturalOrder = bodyRType.getRType().hasNaturalOrder();
      boolean _not = (!_hasNaturalOrder);
      if (_not) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Operation ");
        String _operator = op.getOperator();
        _builder.append(_operator);
        _builder.append(" only supports comparable types (string, int, number, boolean, date). Found type ");
        String _name = bodyRType.getRType().getName();
        _builder.append(_name);
        _builder.append(".");
        this.error(_builder.toString(), ref, null);
      }
    }
  }

  @Check
  public void checkAlias(final ShortcutDeclaration o) {
    RosettaExpression _expression = null;
    if (o!=null) {
      _expression=o.getExpression();
    }
    final RosettaExpression expr = _expression;
    if (((expr != null) && this.cardinality.isOutputListOfLists(expr))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Alias expression contains a list of lists, use flatten to create a list.");
      this.error(_builder.toString(), o, 
        SimplePackage.Literals.SHORTCUT_DECLARATION__EXPRESSION);
    }
  }
}
