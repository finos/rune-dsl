package com.regnosys.rosetta.validation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.expression.*;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.types.*;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.utils.ImportManagementService;
import com.regnosys.rosetta.utils.RosettaConfigExtension;
import jakarta.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Do not write any more validators in here for the following reasons:
 * 1. We are moving away from Xtend to Java for all test and implementation code
 * 2. This approach to putting every validator type in one place is messy and hard to navigate, better to split into classes by type of validator
 * <p>
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

    @Check
    public void deprecatedWarning(EObject object) {
        EList<EStructuralFeature> features = object.eClass().getEAllStructuralFeatures();
        EStructuralFeature[] crossReferencesArray = ((EClassImpl.FeatureSubsetSupplier) features).crossReferences();
        List<EReference> crossRefs = ((List<EReference>) Conversions.doWrapArray(crossReferencesArray));

        if (crossRefs != null) {
            for (EReference ref : crossRefs) {
                if (ref.isMany()) {
                    Object eGet = object.eGet(ref);
                    Iterable<Annotated> annotatedList = Iterables.filter(((List<?>) eGet), Annotated.class);
                    List<Annotated> annotatedArray = IterableExtensions.toList(annotatedList);

                    for (int i = 0; i < annotatedArray.size(); i++) {
                        this.checkDeprecatedAnnotation(annotatedArray.get(i), object, ref, i);
                    }
                } else {
                    Object annotated = object.eGet(ref);
                    if (annotated instanceof Annotated) {
                        this.checkDeprecatedAnnotation(((Annotated) annotated), object, ref, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
                    }
                }
            }
        }
    }

    @Check
    public void deprecatedLabelAsWarning(LabelAnnotation labelAnnotation) {
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
    public void ruleMustHaveInputTypeDeclared(RosettaRule rule) {
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
    public void unnecesarrySquareBracketsCheck(RosettaFunctionalOperation op) {
        if (this.hasSquareBrackets(op)) {
            if (!this.areSquareBracketsMandatory(op)) {
                this.warning("Usage of brackets is unnecessary.",
                        op.getFunction(),
                        null,
                        RosettaIssueCodes.REDUNDANT_SQUARE_BRACKETS);
            }
        }
    }

    public boolean hasSquareBrackets(RosettaFunctionalOperation op) {
        return op.getFunction() != null &&
               this.findDirectKeyword(op.getFunction(),
                       this.grammarAccess.getInlineFunctionAccess().getLeftSquareBracketKeyword_0_0_1()) != null;
    }

    public boolean areSquareBracketsMandatory(RosettaFunctionalOperation op) {
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
    public void mandatoryThenCheck(RosettaUnaryOperation op) {
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

    public boolean isThenMandatory(RosettaUnaryOperation op) {
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
    public void ambiguousFunctionalOperation(RosettaFunctionalOperation op) {
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

    public boolean isNestedFunctionalOperation(RosettaFunctionalOperation op) {
        InlineFunction enclosingInlineFunction = EcoreUtil2.getContainerOfType(op, InlineFunction.class);

        if (enclosingInlineFunction != null && !(enclosingInlineFunction.eContainer() instanceof ThenOperation)) {
            EObject opCodeBlock = this.getEnclosingCodeBlock(op);
            EObject functionCodeBlock = this.getEnclosingCodeBlock(enclosingInlineFunction);

            if (opCodeBlock == functionCodeBlock) {
                return true;
            }
        }

        return false;
    }

    public boolean containsNestedFunctionalOperation(RosettaFunctionalOperation op) {
        if (op.getFunction() == null) {
            return false;
        }

        EObject codeBlock = this.getEnclosingCodeBlock(op.getFunction());

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
    public EObject getEnclosingCodeBlock(EObject obj) {
        return this.getEnclosingCodeBlock(NodeModelUtils.findActualNodeFor(obj), obj);
    }

    private EObject getEnclosingCodeBlock(INode node, EObject potentialCodeBlock) {
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
    public void checkParametrizedType(ParametrizedRosettaType type) {
        Set<String> visited = new HashSet<>();

        for (TypeParameter param : type.getParameters()) {
            if (!visited.add(param.getName())) {
                this.error("Duplicate parameter name `" + param.getName() + "`.",
                        param,
                        RosettaPackage.Literals.ROSETTA_NAMED__NAME);
            }
        }
    }

    @Check
    public void checkAnnotationSource(ExternalAnnotationSource source) {
        // NOTE: Should be error, but kept as warning for backward compatibility
        Set<RosettaType> visited = new HashSet<>();
        for (RosettaExternalRef t : source.getExternalRefs()) {
            if (!visited.add(t.getTypeRef())) {
                this.warning("Duplicate type `" + t.getTypeRef().getName() + "`.", t, null);
            }
        }
    }

    @Check
    public void checkSynonymSource(RosettaExternalSynonymSource source) {
        for (RosettaExternalClass t : source.getExternalClasses()) {
            for (RosettaExternalRegularAttribute attr : t.getRegularAttributes()) {
                attr.getExternalRuleReferences().forEach(it ->
                        this.error("You may not define rule references in a synonym source.", it, null)
                );
            }
        }
    }

    @Check
    public void checkRuleSource(RosettaExternalRuleSource source) {
        if (source.getSuperSources().size() > 1) {
            this.error("A rule source may not extend more than one other rule source.", source,
                    RosettaPackage.Literals.ROSETTA_EXTERNAL_RULE_SOURCE__SUPER_SOURCES, 1);
        }
        for (RosettaExternalClass t : source.getExternalClasses()) {
            t.getExternalClassSynonyms().forEach(it ->
                    this.error("You may not define synonyms in a rule source.", it, null)
            );
            for (RosettaExternalRegularAttribute attr : t.getRegularAttributes()) {
                attr.getExternalSynonyms().forEach(it ->
                        this.error("You may not define synonyms in a rule source.", it, null)
                );
            }
        }
        this.errorKeyword("A rule source cannot define annotations for enums.", source,
                this.grammarAccess.getExternalAnnotationSourceAccess().getEnumsKeyword_2_0());
    }

    @Check
    public void checkGeneratedInputInContextWithImplicitVariable(HasGeneratedInput e) {
        if (e.needsGeneratedInput() && !this.implicitVariableUtil.implicitVariableExistsInContext(e)) {
            this.error(
                    "There is no implicit variable in this context. This operator needs an explicit input in this context.", e, null);
        }
    }

    @Check
    public void checkImplicitVariableReferenceInContextWithoutImplicitVariable(RosettaImplicitVariable e) {
        if (!this.implicitVariableUtil.implicitVariableExistsInContext(e)) {
            this.error("There is no implicit variable in this context.", e, null);
        }
    }

    @Check
    public void checkConditionName(Condition condition) {
        if (condition.getName() == null && !this.rosettaEcoreUtil.isConstraintCondition(condition)) {
            this.warning("Condition name should be specified", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_NAME);
        } else {
            if (condition.getName() != null && Character.isLowerCase(condition.getName().charAt(0))) {
                this.warning("Condition name should start with a capital", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_CASE);
            }
        }
    }

    @Check
    public void checkFeatureCallFeature(RosettaFeatureCall fCall) {
        if (fCall.getFeature() == null) {
            this.error("Attribute is missing after \'->\'", fCall, ExpressionPackage.Literals.ROSETTA_FEATURE_CALL__FEATURE);
        }
    }

    @Check
    public void checkFunctionNameStartsWithCapital(com.regnosys.rosetta.rosetta.simple.Function func) {
        if (Character.isLowerCase(func.getName().charAt(0))) {
            this.warning("Function name should start with a capital", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_CASE);
        }
    }

    @Check
    public void checkAttributes(Data clazz) {
        HashMultimap<String, RAttribute> name2attr = HashMultimap.create();
        // Collect all non-override attributes from the full type hierarchy
        for (RAttribute attr : this.objectFactory.buildRDataType(clazz).getAllAttributes()) {
            if (!attr.isOverride()) {
                name2attr.put(attr.getName(), attr);
            }
        }
        // TODO: remove once `override` keyword is mandatory
        // For each non-override attribute declared on the class, check duplicates against hierarchy
        for (Attribute a : clazz.getAttributes()) {
            if (a.isOverride()) continue;
            String name = a.getName();
            Set<RAttribute> attrByName = name2attr.get(name);
            if (attrByName.size() > 1) {
                // Split into attributes declared on this class vs inherited
                List<RAttribute> attrFromClazzes = attrByName.stream()
                        .filter(it -> Objects.equals(it.getEObject().eContainer(), clazz))
                        .toList();
                List<RAttribute> attrFromSuperClasses = attrByName.stream()
                        .filter(it -> !Objects.equals(it.getEObject().eContainer(), clazz))
                        .toList();
                this.checkTypeAttributeMustHaveSameTypeAsParent(attrFromClazzes, attrFromSuperClasses, name);
                this.checkAttributeCardinalityMatchSuper(attrFromClazzes, attrFromSuperClasses, name);
            }
        }
    }

    protected void checkTypeAttributeMustHaveSameTypeAsParent(Iterable<RAttribute> attrFromClazzes, Iterable<RAttribute> attrFromSuperClasses, String name) {
        for (RAttribute childAttr : attrFromClazzes) {
            RType childAttrType = childAttr.getRMetaAnnotatedType().getRType();
            for (RAttribute parentAttr : attrFromSuperClasses) {
                RType parentAttrType = parentAttr.getRMetaAnnotatedType().getRType();
                if (!Objects.equals(childAttrType, parentAttrType)) {
                    this.error("Overriding attribute '" + name + "' with type " + childAttrType
                               + " must match the type of the attribute it overrides (" + parentAttrType + ")",
                            childAttr.getEObject(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.DUPLICATE_ATTRIBUTE);
                }
            }
        }
    }

    protected void checkAttributeCardinalityMatchSuper(Iterable<RAttribute> attrFromClazzes, Iterable<RAttribute> attrFromSuperClasses, String name) {
        for (RAttribute childAttr : attrFromClazzes) {
            for (RAttribute parentAttr : attrFromSuperClasses) {
                if (!Objects.equals(childAttr.getCardinality(), parentAttr.getCardinality())) {
                    this.error("Overriding attribute '" + name + "' with cardinality (" + childAttr.getCardinality()
                               + ") must match the cardinality of the attribute it overrides (" + parentAttr.getCardinality() + ")",
                            childAttr.getEObject(), RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.CARDINALITY_ERROR);
                }
            }
        }
    }

    @Check
    public void checkFunctionElementNamesAreUnique(com.regnosys.rosetta.rosetta.simple.Function ele) {
        // Combine inputs, shortcuts, and output into a single list of AssignPathRoot elements (ignoring nulls)
        List<AssignPathRoot> features = new java.util.ArrayList<>();
        for (Attribute in : ele.getInputs()) features.add(in);
        for (ShortcutDeclaration sc : ele.getShortcuts()) features.add(sc);
        if (ele.getOutput() != null) features.add(ele.getOutput());

        Map<String, List<AssignPathRoot>> grouped = features.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.groupingBy(AssignPathRoot::getName));

        grouped.forEach((k, v) -> {
            if (v.size() > 1) {
                v.forEach(it -> this.error("Duplicate feature \"" + k + "\"", (EObject) it, RosettaPackage.Literals.ROSETTA_NAMED__NAME));
            }
        });
    }

    @Check
    public void checkClosureParameterNamesAreUnique(ClosureParameter param) {
        IScope scope = this.scopeProvider.getScope(param.getFunction().eContainer(), ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE__SYMBOL);
        IEObjectDescription sameNamedElement = scope.getSingleElement(QualifiedName.create(param.getName()));
        if (sameNamedElement != null) {
            this.error("Duplicate name.", param, null);
        }
    }

    @Check(CheckType.FAST)
    public void checkTypeNamesAreUnique(RosettaModel model) {
        // TODO This probably should be made namespace aware
        // NOTE: case-insensitive uniqueness to avoid clashes on case-insensitive filesystems
        List<RosettaNamed> namedRootElems = model.getElements().stream()
                .filter(RosettaNamed.class::isInstance)
                .map(RosettaNamed.class::cast)
                .filter(it -> !(it instanceof com.regnosys.rosetta.rosetta.simple.FunctionDispatch))
                .toList();

        IResourceDescriptions descr = this.resourceDescriptionsProvider.getResourceDescriptions(model.eResource());

        List<RosettaNamed> nonAnnotations = namedRootElems.stream()
                .filter(it -> !(it instanceof Annotation))
                .toList();
        this.checkNamesInGroupAreUnique(nonAnnotations, true, descr);

        List<RosettaNamed> annotations = namedRootElems.stream()
                .filter(it -> it instanceof Annotation)
                .toList();
        this.checkNamesInGroupAreUnique(annotations, true, descr);
    }

    private void checkNamesInGroupAreUnique(Collection<RosettaNamed> namedElems, boolean ignoreCase, IResourceDescriptions descr) {
        HashMultimap<String, RosettaNamed> groupedElems = HashMultimap.create();
        // Populate groupedElems by (optionally case-insensitive) name, ignoring nulls
        for (RosettaNamed it : namedElems) {
            String name = it.getName();
            if (name == null) continue;
            groupedElems.put(ignoreCase ? name.toUpperCase() : name, it);
        }
        for (String key : groupedElems.keySet()) {
            Set<RosettaNamed> group = groupedElems.get(key);
            if (group.size() > 1) {
                for (RosettaNamed it : group) {
                    this.error("Duplicate element named '" + it.getName() + "'", it, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.DUPLICATE_ELEMENT_NAME);
                }
            } else if (group.size() == 1) {
                RosettaNamed toCheck = group.iterator().next();
                if (toCheck.eResource().getURI().isPlatformResource() && toCheck instanceof RosettaRootElement) {
                    QualifiedName qName = this.qualifiedNameProvider.getFullyQualifiedName(toCheck);
                    Iterable<IEObjectDescription> exported = descr.getExportedObjects(toCheck.eClass(), qName, false);
                    // Filter to project-local and exclude function dispatch
                    java.util.List<URI> sameNamed = new java.util.ArrayList<>();
                    for (IEObjectDescription d : exported) {
                        if (this.confExtensions.isProjectLocal(toCheck.eResource().getURI(), d.getEObjectURI())
                            && d.getEClass() != SimplePackage.Literals.FUNCTION_DISPATCH) {
                            sameNamed.add(d.getEObjectURI());
                        }
                    }
                    if (sameNamed.size() > 1) {
                        // Build comma-separated list of other file names (excluding the current one)
                        StringBuilder sb = new StringBuilder();
                        for (URI uri : sameNamed) {
                            if (Objects.equals(EcoreUtil.getURI(toCheck), uri)) continue;
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(uri.lastSegment());
                        }
                        if (sb.length() > 0) {
                            this.error("Duplicate element named '" + qName + "' in " + sb, toCheck, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.DUPLICATE_ELEMENT_NAME);
                        }
                    }
                }
            }
        }
    }

    @Check
    public void checkMappingSetToCase(RosettaMapping element) {
        // Only one instance is allowed where set != null and when == null
        long defaultSetCount = element.getInstances().stream()
                .filter(inst -> inst.getSet() != null && inst.getWhen() == null)
                .count();
        if (defaultSetCount > 1) {
            this.error("Only one set to with no when clause allowed.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
        }

        // If exactly one such instance exists, it must be the last instance
        if (defaultSetCount == 1) {
            RosettaMappingInstance defaultInstance = element.getInstances().stream()
                    .filter(inst -> inst.getSet() != null && inst.getWhen() == null)
                    .findFirst().orElse(null);
            RosettaMappingInstance lastInstance = element.getInstances().isEmpty() ? null : element.getInstances().get(element.getInstances().size() - 1);
            if (defaultInstance != lastInstance) {
                this.error("Set to without when case must be ordered last.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
            }
        }

        RosettaType type = this.getContainerType(element);
        if (type != null) {
            // If container type is a Data, constant set is invalid
            boolean anySetConstants = element.getInstances().stream().anyMatch(inst -> inst.getSet() != null);
            if (type instanceof Data && anySetConstants) {
                this.error("Set to constant type does not match type of field.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
            } else if (type instanceof RosettaEnumeration) {
                // For enum types, ensure set is an enum value reference of the same enum
                for (RosettaMappingInstance inst : element.getInstances()) {
                    if (inst.getSet() != null) {
                        RosettaMapTestExpression setExpr = inst.getSet();
                        if (!(setExpr instanceof RosettaEnumValueReference)) {
                            this.error("Set to constant type does not match type of field.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
                        } else {
                            RosettaEnumValueReference setEnum = (RosettaEnumValueReference) setExpr;
                            String containerEnumName = ((RosettaEnumeration) type).getName();
                            String setEnumName = setEnum.getEnumeration().getName();
                            if (!Objects.equals(containerEnumName, setEnumName)) {
                                this.error("Set to constant type does not match type of field.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
                            }
                        }
                    }
                }
            }
        }
    }

    public RosettaType getContainerType(RosettaMapping element) {
        EObject container = element.eContainer().eContainer().eContainer();
        if (container instanceof RosettaExternalRegularAttribute attrContainer) {
            RosettaFeature attributeRef = attrContainer.getAttributeRef();
            if (attributeRef instanceof RosettaTyped typed) {
                return typed.getTypeCall().getType();
            }
        } else if (container instanceof RosettaTyped typed) {
            return typed.getTypeCall().getType();
        }
        return null;
    }

    @Check
    public void checkMappingDefaultCase(RosettaMapping element) {
        long defaultCount = element.getInstances().stream().filter(RosettaMappingInstance::isDefault).count();
        if (defaultCount > 1) {
            this.error("Only one default case allowed.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
        }
        if (defaultCount == 1) {
            RosettaMappingInstance defaultInstance = element.getInstances().stream().filter(RosettaMappingInstance::isDefault).findFirst().orElse(null);
            RosettaMappingInstance lastInstance = element.getInstances().isEmpty() ? null : element.getInstances().get(element.getInstances().size() - 1);
            if (defaultInstance != lastInstance) {
                this.error("Default case must be ordered last.", element, RosettaPackage.Literals.ROSETTA_MAPPING__INSTANCES);
            }
        }
    }

    // CONTINUE MIGRATION FROM HERE
    @Check
    public void checkMergeSynonymAttributeCardinality(Attribute attribute) {
        for (RosettaSynonym syn : attribute.getSynonyms()) {
            boolean multi = attribute.getCard().isIsMany();
            RosettaSynonymBody body = syn.getBody();
            RosettaMergeSynonymValue merge = body == null ? null : body.getMerge();
            if (merge != null && !multi) {
                this.error("Merge synonym can only be specified on an attribute with multiple cardinality.",
                        syn.getBody(), RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__MERGE);
            }
        }
    }

    @Check
    public void checkMergeSynonymAttributeCardinality(RosettaExternalRegularAttribute attribute) {
        RosettaFeature att = attribute.getAttributeRef();
        if (att instanceof Attribute attr) {
            for (RosettaExternalSynonym syn : attribute.getExternalSynonyms()) {
                boolean multi = attr.getCard().isIsMany();
                RosettaSynonymBody body = syn.getBody();
                RosettaMergeSynonymValue merge = body == null ? null : body.getMerge();
                if (merge != null && !multi) {
                    this.error("Merge synonym can only be specified on an attribute with multiple cardinality.",
                            syn.getBody(), RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__MERGE);
                }
            }
        }
    }

    @Check
    public void checkPatternAndFormat(RosettaExternalRegularAttribute attribute) {
        boolean isDateTime = this.isDateTime(this.rosettaTypeProvider
                .getRTypeOfFeature(attribute.getAttributeRef(), attribute).getRType());
        if (!isDateTime) {
            for (RosettaExternalSynonym s : attribute.getExternalSynonyms()) {
                this.checkFormatNull(s.getBody());
                this.checkPatternValid(s.getBody());
            }
        } else {
            for (RosettaExternalSynonym s : attribute.getExternalSynonyms()) {
                this.checkFormatValid(s.getBody());
                this.checkPatternNull(s.getBody());
            }
        }
    }

    @Check
    public void checkPatternAndFormat(Attribute attribute) {
        boolean isDateTime = this.isDateTime(this.rosettaTypeProvider.getRTypeOfSymbol(attribute).getRType());
        if (!isDateTime) {
            for (RosettaSynonym s : attribute.getSynonyms()) {
                this.checkFormatNull(s.getBody());
                this.checkPatternValid(s.getBody());
            }
        } else {
            for (RosettaSynonym s : attribute.getSynonyms()) {
                this.checkFormatValid(s.getBody());
                this.checkPatternNull(s.getBody());
            }
        }
    }

    public void checkFormatNull(RosettaSynonymBody body) {
        if (body.getFormat() != null) {
            this.error("Format can only be applied to date/time types", body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__FORMAT);
        }
    }

    public DateTimeFormatter checkFormatValid(RosettaSynonymBody body) {
        if (body == null || body.getFormat() == null) return null;
        try {
            return DateTimeFormatter.ofPattern(body.getFormat());
        } catch (IllegalArgumentException e) {
            this.error("Format must be a valid date/time format - " + e.getMessage(),
                    body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__FORMAT);
            return null;
        }
    }

    public void checkPatternNull(RosettaSynonymBody body) {
        if (body.getPatternMatch() != null) {
            this.error("Pattern cannot be applied to date/time types",
                    body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__PATTERN_MATCH);
        }
    }

    public Pattern checkPatternValid(RosettaSynonymBody body) {
        if (body == null || body.getPatternMatch() == null) return null;
        try {
            return Pattern.compile(body.getPatternMatch());
        } catch (PatternSyntaxException e) {
            this.error("Pattern to match must be a valid regular expression - " + this.getPatternSyntaxErrorMessage(e),
                    body, RosettaPackage.Literals.ROSETTA_SYNONYM_BODY__PATTERN_MATCH);
            return null;
        }
    }

    private boolean isDateTime(RType rType) {
        String name = rType == null ? null : rType.getName();
        return java.util.Set.of("date", "time", "zonedDateTime").contains(name);
    }

    @Check
    public void checkPatternOnEnum(RosettaEnumSynonym synonym) {
        if (synonym.getPatternMatch() != null) {
            try {
                Pattern.compile(synonym.getPatternMatch());
            } catch (PatternSyntaxException e) {
                this.error("Pattern to match must be a valid regular expression - " + this.getPatternSyntaxErrorMessage(e),
                        synonym, RosettaPackage.Literals.ROSETTA_ENUM_SYNONYM__PATTERN_MATCH);
            }
        }
    }

    private String getPatternSyntaxErrorMessage(PatternSyntaxException e) {
        return e.getMessage().replace(System.lineSeparator(), "\n");
    }

    @Check
    public void checkFuncDispatchAttr(FunctionDispatch ele) {
        if (this.rosettaEcoreUtil.isResolved(ele.getAttribute())
            && this.rosettaEcoreUtil.isResolved(ele.getAttribute().getTypeCall())) {
            RosettaType type = ele.getAttribute().getTypeCall().getType();
            if (!(type instanceof RosettaEnumeration)) {
                this.error(
                        "Dispatching function may refer to an enumeration typed attributes only. Current type is "
                        + ele.getAttribute().getTypeCall().getType().getName(),
                        ele, SimplePackage.Literals.FUNCTION_DISPATCH__ATTRIBUTE);
            }
        }
    }

    @Check
    public void checkDispatch(com.regnosys.rosetta.rosetta.simple.Function ele) {
        if (ele instanceof FunctionDispatch) return;

        List<FunctionDispatch> dispatches =
                IterableExtensions.toList(this.rosettaFunctionExtensions.getDispatchingFunctions(ele));
        if (dispatches.isEmpty()) return;

        // Map: enum -> (valueName -> list of dispatches using that value)
        Map<RosettaEnumeration, Map<String, List<FunctionDispatch>>> byEnum = new HashMap<>();
        for (FunctionDispatch fd : dispatches) {
            RosettaEnumValueReference enumRef = fd.getValue();
            if (enumRef == null || enumRef.getEnumeration() == null || enumRef.getValue() == null) continue;
            RosettaEnumeration en = enumRef.getEnumeration();
            String valueName = enumRef.getValue().getName();
            byEnum.computeIfAbsent(en, k -> new HashMap<>())
                    .computeIfAbsent(valueName, k -> new java.util.ArrayList<>())
                    .add(fd);
        }
        if (byEnum.isEmpty()) return;

        // Find enum with most used values
        RosettaEnumeration mostUsedEnum = null;
        int maxSize = -1;
        for (Map.Entry<RosettaEnumeration, Map<String, List<FunctionDispatch>>> e : byEnum.entrySet()) {
            int size = e.getValue().size();
            if (size > maxSize) {
                maxSize = size;
                mostUsedEnum = e.getKey();
            }
        }
        if (mostUsedEnum == null) return;

        // Warn on missing implementations for the most used enum
        Set<String> toImplement = new HashSet<>();
        for (RosettaEnumValue v : this.objectFactory.buildREnumType(mostUsedEnum).getAllEnumValues()) {
            toImplement.add(v.getName());
        }
        Map<String, List<FunctionDispatch>> mostUsedMap = byEnum.getOrDefault(mostUsedEnum, Map.of());
        toImplement.removeAll(mostUsedMap.keySet());
        if (!toImplement.isEmpty()) {
            this.warning("Missing implementation for " + mostUsedEnum.getName() + ": "
                         + toImplement.stream().sorted().collect(java.util.stream.Collectors.joining(", ")),
                    ele, RosettaPackage.Literals.ROSETTA_NAMED__NAME);
        }

        // Errors for wrong enum used
        for (Map.Entry<RosettaEnumeration, Map<String, List<FunctionDispatch>>> e : byEnum.entrySet()) {
            RosettaEnumeration usedEnum = e.getKey();
            Map<String, List<FunctionDispatch>> entries = e.getValue();
            if (!Objects.equals(usedEnum, mostUsedEnum)) {
                for (List<FunctionDispatch> fds : entries.values()) {
                    for (FunctionDispatch fd : fds) {
                        this.error("Wrong " + usedEnum.getName() + " enumeration used. Expecting " + mostUsedEnum.getName() + ".",
                                fd.getValue(), RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION);
                    }
                }
            } else {
                // Duplicate value usage
                for (Map.Entry<String, List<FunctionDispatch>> valEntry : entries.entrySet()) {
                    if (valEntry.getValue().size() > 1) {
                        for (FunctionDispatch fd : valEntry.getValue()) {
                            this.error("Dupplicate usage of " + valEntry.getKey() + " enumeration value.",
                                    fd.getValue(), RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__VALUE);
                        }
                    }
                }
            }
        }
    }

    @Check
    public void checkConditionDontUseOutput(com.regnosys.rosetta.rosetta.simple.Function ele) {
        for (Condition cond : ele.getConditions()) {
            if (cond.isPostCondition()) continue;
            RosettaExpression expr = cond.getExpression();
            if (expr == null) continue;

            Stack<String> trace = new Stack<>();
            List<RosettaSymbol> outRef = this.exprHelper.findOutputRef(expr, trace);
            if (outRef != null && !outRef.isEmpty()) {
                RosettaSymbol first = outRef.get(0);
                StringBuilder msg = new StringBuilder();
                msg.append("output '").append(first.getName()).append("' or alias on output '")
                        .append(first.getName()).append("' not allowed in condition blocks.");
                if (!trace.isEmpty()) {
                    msg.append("\n").append(String.join(" > ", trace)).append(" > ").append(first.getName());
                }
                this.error(msg.toString(), expr, null);
            }
        }
    }

    @Check
    public void checkAssignAnAlias(Operation ele) {
        if (ele.getPath() == null && ele.getAssignRoot() instanceof ShortcutDeclaration) {
            this.error("An alias can not be assigned. Assign target must be an attribute.",
                    ele, SimplePackage.Literals.OPERATION__ASSIGN_ROOT);
        }
    }

    // Helper type to avoid xbase Pair
    private static class InvalidPathChar {
        char ch;
        boolean atSegmentStart;

        InvalidPathChar(char ch, boolean atSegmentStart) {
            this.ch = ch;
            this.atSegmentStart = atSegmentStart;
        }
    }

    @Check
    public void checkSynonymMapPath(RosettaMapPathValue ele) {
        if (!StringExtensions.isNullOrEmpty(ele.getPath())) {
            InvalidPathChar invalid = this.checkPathChars(ele.getPath());
            if (invalid != null) {
                String msg = "Character '" + invalid.ch + "' is not allowed "
                             + (invalid.atSegmentStart ? "as first symbol in a path segment." : "in paths. Use '->' to separate path segments.");
                this.error(msg, ele, RosettaPackage.Literals.ROSETTA_MAP_PATH_VALUE__PATH);
            }
        }
    }

    @Check
    public void checkSynonyValuePath(RosettaSynonymValueBase ele) {
        if (!StringExtensions.isNullOrEmpty(ele.getPath())) {
            InvalidPathChar invalid = this.checkPathChars(ele.getPath());
            if (invalid != null) {
                String msg = "Character '" + invalid.ch + "' is not allowed "
                             + (invalid.atSegmentStart ? "as first symbol in a path segment." : "in paths. Use '->' to separate path segments.");
                this.error(msg, ele, RosettaPackage.Literals.ROSETTA_SYNONYM_VALUE_BASE__PATH);
            }
        }
    }

    @Check
    public void checkToStringOpArgument(ToStringOperation ele) {
        RosettaExpression arg = ele.getArgument();
        if (this.rosettaEcoreUtil.isResolved(arg)) {
            if (this.cardinality.isMulti(arg)) {
                this.error("The argument of " + ele.getOperator() + " should be of singular cardinality.",
                        ele, ExpressionPackage.Literals.ROSETTA_UNARY_OPERATION__ARGUMENT);
            }
            RType type = this.typeSystem.stripFromTypeAliases(
                    this.rosettaTypeProvider.getRMetaAnnotatedType(arg).getRType());
            if (!(type instanceof RBasicType || type instanceof RRecordType || type instanceof REnumType)) {
                this.error("The argument of " + ele.getOperator() + " should be of a builtin type or an enum.",
                        ele, ExpressionPackage.Literals.ROSETTA_UNARY_OPERATION__ARGUMENT);
            }
        }
    }

    @Check
    public void checkFunctionPrefix(com.regnosys.rosetta.rosetta.simple.Function ele) {
        ele.getAnnotations().forEach(a -> {
            String prefix = a.getAnnotation().getPrefix();
            if (prefix != null && !ele.getName().startsWith(prefix + "_")) {
                this.warning("Function name " + ele.getName() + " must have prefix '" + prefix + "' followed by an underscore.",
                        RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
            }
        });
    }

    @Check
    public void checkMetadataAnnotation(Annotated ele) {
        List<AnnotationRef> metadatas = this.rosettaFunctionExtensions.getMetadataAnnotations(ele);
        for (AnnotationRef it : metadatas) {
            Attribute attr = it.getAttribute();
            String name = attr == null ? null : attr.getName();
            if (name == null) continue;

            switch (name) {
                case "key":
                    if (!(ele instanceof Data)) {
                        this.error("[metadata key] annotation only allowed on a type.", it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    }
                    break;
                case "id":
                    if (!(ele instanceof Attribute)) {
                        this.error("[metadata id] annotation only allowed on an attribute.",
                                it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    }
                    break;
                case "reference":
                    if (!(ele instanceof Attribute)) {
                        this.error("[metadata reference] annotation only allowed on an attribute.",
                                it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    }
                    break;
                case "scheme":
                    if (!(ele instanceof Attribute || ele instanceof Data)) {
                        this.error("[metadata scheme] annotation only allowed on an attribute or a type.",
                                it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    }
                    break;
                case "template":
                    if (!(ele instanceof Data)) {
                        this.error("[metadata template] annotation only allowed on a type.",
                                it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    } else {
                        boolean hasKey = metadatas.stream()
                                .map(AnnotationRef::getAttribute)
                                .map(a -> a == null ? null : a.getName())
                                .anyMatch("key"::equals);
                        if (!hasKey) {
                            this.error("Types with [metadata template] annotation must also specify the [metadata key] annotation.",
                                    it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                        }
                    }
                    break;
                case "location":
                    if (ele instanceof Attribute) {
                        boolean hasPointsTo = it.getQualifiers().stream()
                                .anyMatch(q -> Objects.equals(q.getQualName(), "pointsTo"));
                        if (hasPointsTo) {
                            this.error("pointsTo qualifier belongs on the address not the location.",
                                    it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                        }
                    } else {
                        this.error("[metadata location] annotation only allowed on an attribute.",
                                it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    }
                    break;
                case "address":
                    if (ele instanceof Attribute) {
                        for (AnnotationQualifier q : it.getQualifiers()) {
                            if (!Objects.equals(q.getQualName(), "pointsTo")) continue;

                            RosettaAttributeReferenceSegment qualPath = q.getQualPath();
                            if (qualPath instanceof RosettaAttributeReference attrRef) {
                                if (this.rosettaEcoreUtil.isResolved(attrRef.getAttribute())) {
                                    this.checkForLocation(attrRef.getAttribute(), q);
                                    RType targetType = this.typeSystem.typeCallToRType(attrRef.getAttribute().getTypeCall());
                                    RType thisType = this.rosettaTypeProvider.getRTypeOfFeature(((RosettaFeature) ele), null).getRType();
                                    if (!this.typeSystem.isSubtypeOf(thisType, targetType)) {
                                        this.error("Expected address target type of '" + thisType.getName() + "' but was '"
                                                   + (targetType != null ? targetType.getName() : "null") + "'",
                                                q, SimplePackage.Literals.ANNOTATION_QUALIFIER__QUAL_PATH, RosettaIssueCodes.TYPE_ERROR);
                                    }
                                }
                            } else {
                                this.error("Target of an address must be an attribute",
                                        q, SimplePackage.Literals.ANNOTATION_QUALIFIER__QUAL_PATH, RosettaIssueCodes.TYPE_ERROR);
                            }
                        }
                    } else {
                        this.error("[metadata address] annotation only allowed on an attribute.",
                                it, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE);
                    }
                    break;
                default:
                    // No-op
            }
        }
    }

    private void checkForLocation(Attribute attribute, AnnotationQualifier checked) {
        boolean locationFound = this.rosettaFunctionExtensions.getMetadataAnnotations(attribute).stream()
                .map(AnnotationRef::getAttribute)
                .map(a -> a == null ? null : a.getName())
                .anyMatch("location"::equals);
        if (!locationFound) {
            this.error("Target of address must be annotated with metadata location",
                    checked, SimplePackage.Literals.ANNOTATION_QUALIFIER__QUAL_PATH);
        }
    }

    @Check
    public void checkTransformAnnotations(Annotated ele) {
        List<AnnotationRef> annotations = this.rosettaFunctionExtensions.getTransformAnnotations(ele);
        if (annotations.isEmpty()) return;

        if (!(ele instanceof com.regnosys.rosetta.rosetta.simple.Function)) {
            this.error("Transformation annotations only allowed on a function.", RosettaPackage.Literals.ROSETTA_NAMED__NAME);
        }
        if (annotations.size() > 1) {
            // Keep first, error on the rest
            annotations.stream().skip(1).forEach(it ->
                    this.error("Only one transform annotation allowed.", it, null)
            );
        }
        AnnotationRef annotationRef = annotations.get(0);
        String name = annotationRef.getAnnotation().getName();
        if (Objects.equals(name, "ingest")) {
            if (annotationRef.getAttribute() == null) {
                this.error("The `ingest` annotation must have a source format such as JSON or XML",
                        annotationRef, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
            }
        }
        if (Objects.equals(name, "projection")) {
            if (annotationRef.getAttribute() == null) {
                this.error("The `projection` annotation must have a target format such as JSON or XML",
                        annotationRef, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
            }
        }
    }

    @Check
    public void checkCreationAnnotation(Annotated ele) {
        List<AnnotationRef> annotations = this.rosettaFunctionExtensions.getCreationAnnotations(ele);
        if (annotations.isEmpty()) return;

        if (!(ele instanceof com.regnosys.rosetta.rosetta.simple.Function)) {
            this.error("Creation annotation only allowed on a function.",
                    RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
            return;
        }
        if (annotations.size() > 1) {
            this.error("Only 1 creation annotation allowed.",
                    RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
            return;
        }
        com.regnosys.rosetta.rosetta.simple.Function func = (com.regnosys.rosetta.rosetta.simple.Function) ele;

        RType annotationType = this.rosettaTypeProvider.getRTypeOfSymbol(annotations.get(0).getAttribute()).getRType();
        if (annotationType instanceof RChoiceType choice) {
            annotationType = choice.asRDataType();
        }
        RType funcOutputType = this.rosettaTypeProvider.getRTypeOfSymbol(func).getRType();
        if (funcOutputType instanceof RChoiceType choice) {
            funcOutputType = choice.asRDataType();
        }

        if (annotationType instanceof RDataType annotationDataType && funcOutputType instanceof RDataType funcOutputDataType) {
            Set<RDataType> funcOutputSuperTypes = new HashSet<>();
            funcOutputDataType.getAllSuperTypes().forEach(t -> funcOutputSuperTypes.add((RDataType) t));

            // Attributes types list is used as a proxy check; keep behavior equivalent
            List<Class<RType>> annotationAttributeTypes = new java.util.ArrayList<>();
            for (RAttribute a : annotationDataType.getAllAttributes()) {
                annotationAttributeTypes.add(RType.class);
            }

            boolean invalid =
                    !Objects.equals(annotationDataType, funcOutputDataType)
                    && !funcOutputSuperTypes.contains(annotationDataType)
                    && !annotationAttributeTypes.contains(funcOutputDataType);

            if (invalid) {
                this.warning(
                        "Invalid output type for creation annotation.  The output type must match the type specified in the annotation '"
                        + annotationDataType.getName() + "' (or extend the annotation type, or be a sub-type as part of a one-of condition).",
                        func, SimplePackage.Literals.FUNCTION__OUTPUT);
            }
        }
    }

    @Check
    public void checkQualificationAnnotation(Annotated ele) {
        List<AnnotationRef> annotations = this.rosettaFunctionExtensions.getQualifierAnnotations(ele);
        if (annotations.isEmpty()) return;

        if (!(ele instanceof com.regnosys.rosetta.rosetta.simple.Function)) {
            this.error("Qualification annotation only allowed on a function.",
                    RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
            return;
        }
        com.regnosys.rosetta.rosetta.simple.Function func = (com.regnosys.rosetta.rosetta.simple.Function) ele;

        if (annotations.size() > 1) {
            this.error("Only 1 qualification annotation allowed.",
                    RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_ELEMENT_NAME);
            return;
        }

        List<Attribute> inputs = this.rosettaFunctionExtensions.getInputs(func);
        if (inputs == null || inputs.size() != 1) {
            this.error("Qualification functions must have exactly 1 input.",
                    func, SimplePackage.Literals.FUNCTION__INPUTS);
            return;
        }

        RosettaType inputType = inputs.get(0).getTypeCall() == null ? null : inputs.get(0).getTypeCall().getType();
        if (!this.rosettaEcoreUtil.isResolved(inputType)) {
            this.error("Invalid input type for qualification function.", func, SimplePackage.Literals.FUNCTION__INPUTS);
        } else if (!this.confExtensions.isRootEventOrProduct(inputType)) {
            this.warning("Input type does not match qualification root type.", func, SimplePackage.Literals.FUNCTION__INPUTS);
        }

        RType outType = func.getOutput() == null ? null
                : (func.getOutput().getTypeCall() == null ? null : this.typeSystem.typeCallToRType(func.getOutput().getTypeCall()));
        if (!Objects.equals(outType, this.builtinTypeService.BOOLEAN)) {
            this.error("Qualification functions must output a boolean.", func, SimplePackage.Literals.FUNCTION__OUTPUT);
        }
    }

    private InvalidPathChar checkPathChars(String str) {
        String[] segments = str.split("->");
        for (String segment : segments) {
            if (segment.length() > 0) {
                if (!Character.isJavaIdentifierStart(segment.charAt(0))) {
                    return new InvalidPathChar(segment.charAt(0), true);
                }
                for (char c : segment.toCharArray()) {
                    if (!Character.isJavaIdentifierPart(c)) {
                        return new InvalidPathChar(c, false);
                    }
                }
            }
        }
        return null;
    }

    @Check
    public void checkUnaryOperation(RosettaUnaryOperation e) {
        RosettaExpression receiver = e.getArgument();
        if (e instanceof ListOperation && this.rosettaEcoreUtil.isResolved(receiver) && !this.cardinality.isMulti(receiver)) {
            this.warning("List " + e.getOperator() + " operation cannot be used for single cardinality expressions.",
                    e, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
        if (!(e instanceof CanHandleListOfLists) && receiver != null && this.cardinality.isOutputListOfLists(receiver)) {
            this.error("List must be flattened before " + e.getOperator() + " operation.",
                    e, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
    }

    @Check
    public void checkInlineFunction(InlineFunction f) {
        if (f.getBody() == null) {
            this.error("Missing function body.", f, null);
        }
    }

    @Check
    public void checkMandatoryFunctionalOperation(MandatoryFunctionalOperation e) {
        this.checkBodyExists(e);
    }

    @Check
    public void checkUnaryFunctionalOperation(UnaryFunctionalOperation e) {
        this.checkOptionalNamedParameter(e.getFunction());
    }

    @Check
    public void checkFilterOperation(FilterOperation o) {
        this.checkBodyType(o.getFunction(), this.builtinTypeService.BOOLEAN);
        this.checkBodyIsSingleCardinality(o.getFunction());
    }

    @Check
    public void checkMapOperation(MapOperation o) {
        if (this.cardinality.isOutputListOfListOfLists(o)) {
            this.error("Each list item is already a list, mapping the item into a list of lists is not allowed. List map item expression must maintain existing cardinality (e.g. list to list), or reduce to single cardinality (e.g. list to single using expression such as count, sum etc).",
                    o, ExpressionPackage.Literals.ROSETTA_FUNCTIONAL_OPERATION__FUNCTION);
        }
    }

    @Check
    public void checkFlattenOperation(FlattenOperation o) {
        if (!this.cardinality.isOutputListOfLists(o.getArgument())) {
            this.error("List flatten only allowed for list of lists.",
                    o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
    }

    @Check
    public void checkReduceOperation(ReduceOperation o) {
        this.checkNumberOfMandatoryNamedParameters(o.getFunction(), 2);
        boolean subtype = this.typeSystem.isSubtypeOf(
                this.rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument()),
                this.rosettaTypeProvider.getRMetaAnnotatedType(o.getFunction().getBody()));
        if (!subtype) {
            this.error("List reduce expression must evaluate to the same type as the input. Found types "
                       + this.rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument()).getRType()
                       + " and " + this.rosettaTypeProvider.getRMetaAnnotatedType(o.getFunction().getBody()).getRType() + ".",
                    o, ExpressionPackage.Literals.ROSETTA_FUNCTIONAL_OPERATION__FUNCTION);
        }
        this.checkBodyIsSingleCardinality(o.getFunction());
    }

    @Check
    public void checkNumberReducerOperation(SumOperation o) {
        this.checkInputType(o, this.builtinTypeService.UNCONSTRAINED_NUMBER_WITH_NO_META);
    }

    @Check
    public void checkComparingFunctionalOperation(ComparingFunctionalOperation o) {
        this.checkBodyIsSingleCardinality(o.getFunction());
        this.checkBodyIsComparable(o);
        if (o.getFunction() == null) {
            this.checkInputIsComparable(o);
        }
    }

    @Check
    public void checkAsKeyOperation(AsKeyOperation o) {
        EObject container = o.eContainer();
        if (container instanceof Operation op) {
            if (op.getPath() == null) {
                this.error("'" + o.getOperator() + "' can only be used when assigning an attribute. Example: \"set out -> attribute: value as-key\"",
                        o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
                return;
            }
            EList<Segment> segments = op.getPath().asSegmentList(op.getPath());
            Segment last = segments == null ? null : IterableExtensions.lastOrNull(segments);
            RosettaFeature feature = last == null ? null : last.getFeature();
            if (feature instanceof RosettaMetaType || !(feature instanceof Attribute) || !this.rosettaEcoreUtil.hasReferenceAnnotation((Attribute) feature)) {
                this.error("'" + o.getOperator() + "' can only be used with attributes annotated with [metadata reference] annotation.",
                        o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
            }
        } else if (container instanceof ConstructorKeyValuePair kv) {
            RosettaFeature attr = kv.getKey();
            if (!(attr instanceof Attribute) || !this.rosettaEcoreUtil.hasReferenceAnnotation((Attribute) attr)) {
                this.error("'" + o.getOperator() + "' can only be used with attributes annotated with [metadata reference] annotation.",
                        o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
            }
        } else {
            this.error("'" + o.getOperator() + "' may only be used in context of an attribute.\"",
                    o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
    }

    @Check
    public void checkImport(RosettaModel model) {
        for (Import ns : model.getImports()) {
            if (ns.getImportedNamespace() != null) {
                QualifiedName qn = QualifiedName.create(ns.getImportedNamespace().split("\\."));
                boolean isWildcard = qn.getLastSegment().equals("*");
                if (!isWildcard && ns.getNamespaceAlias() != null) {
                    this.error("\"as\" statement can only be used with wildcard imports", ns, RosettaPackage.Literals.IMPORT__NAMESPACE_ALIAS);
                }
            }
        }
        List<Import> unused = this.importManagementService.findUnused(model);
        for (Import ns : unused) {
            this.warning("Unused import " + ns.getImportedNamespace(),
                    ns, RosettaPackage.Literals.IMPORT__IMPORTED_NAMESPACE, RosettaIssueCodes.UNUSED_IMPORT);
        }
        List<Import> duplicates = this.importManagementService.findDuplicates(model.getImports());
        for (Import imp : duplicates) {
            this.warning("Duplicate import " + imp.getImportedNamespace(),
                    imp, RosettaPackage.Literals.IMPORT__IMPORTED_NAMESPACE, RosettaIssueCodes.DUPLICATE_IMPORT);
        }
    }

    private void checkBodyExists(RosettaFunctionalOperation operation) {
        if (operation.getFunction() == null) {
            this.error("Missing an expression.", operation, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
    }

    private void checkOptionalNamedParameter(InlineFunction ref) {
        if (ref != null && ref.getParameters() != null && ref.getParameters().size() != 0 && ref.getParameters().size() != 1) {
            this.error("Function must have 1 named parameter.", ref, ExpressionPackage.Literals.INLINE_FUNCTION__PARAMETERS);
        }
    }

    private void checkNumberOfMandatoryNamedParameters(InlineFunction ref, int max) {
        if ((ref != null && ref.getParameters() == null) || (ref != null && ref.getParameters().size() != max)) {
            this.error("Function must have " + max + " named parameter" + (max > 1 ? "s" : "") + ".",
                    ref, ExpressionPackage.Literals.INLINE_FUNCTION__PARAMETERS);
        }
    }

    private void checkInputType(RosettaUnaryOperation o, RMetaAnnotatedType type) {
        boolean ok = this.typeSystem.isSubtypeOf(this.rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument()), type);
        if (!ok) {
            this.error("Input type must be a " + type + ".", o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
    }

    private void checkInputIsComparable(RosettaUnaryOperation o) {
        RMetaAnnotatedType inputRType = this.rosettaTypeProvider.getRMetaAnnotatedType(o.getArgument());
        if (!inputRType.getRType().hasNaturalOrder()) {
            this.error("Operation " + o.getOperator()
                       + " only supports comparable types (string, int, number, boolean, date). Found type "
                       + inputRType.getRType().getName() + ".",
                    o, ExpressionPackage.Literals.ROSETTA_OPERATION__OPERATOR);
        }
    }

    private void checkBodyIsSingleCardinality(InlineFunction ref) {
        if (ref != null && this.cardinality.isBodyExpressionMulti(ref)) {
            this.error("Operation only supports single cardinality expressions.", ref, null);
        }
    }

    private void checkBodyType(InlineFunction ref, RType type) {
        RType bodyType = (ref == null || ref.getBody() == null)
                ? null
                : this.rosettaTypeProvider.getRMetaAnnotatedType(ref.getBody()).getRType();
        if (ref != null && bodyType != null && !Objects.equals(bodyType, type)) {
            this.error("Expression must evaluate to a " + type.getName() + ".", ref, null);
        }
    }

    private void checkBodyIsComparable(RosettaFunctionalOperation op) {
        InlineFunction ref = op.getFunction();
        if (ref != null) {
            RMetaAnnotatedType bodyRType = this.rosettaTypeProvider.getRMetaAnnotatedType(ref.getBody());
            if (!bodyRType.getRType().hasNaturalOrder()) {
                this.error("Operation " + op.getOperator()
                           + " only supports comparable types (string, int, number, boolean, date). Found type "
                           + bodyRType.getRType().getName() + ".",
                        ref, null);
            }
        }
    }

    @Check
    public void checkAlias(ShortcutDeclaration o) {
        RosettaExpression expr = o == null ? null : o.getExpression();
        if (expr != null && this.cardinality.isOutputListOfLists(expr)) {
            this.error("Alias expression contains a list of lists, use flatten to create a list.",
                    o, SimplePackage.Literals.SHORTCUT_DECLARATION__EXPRESSION);
        }
    }
}
