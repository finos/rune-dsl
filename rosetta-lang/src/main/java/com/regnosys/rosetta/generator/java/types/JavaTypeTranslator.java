package com.regnosys.rosetta.generator.java.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.generator.object.ExpandedAttribute;
import com.regnosys.rosetta.generator.object.ExpandedType;
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RErrorType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.ROperation;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RDateTimeType;
import com.regnosys.rosetta.types.builtin.RDateType;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType;
import com.regnosys.rosetta.utils.RosettaTypeSwitch;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.reports.ModelReportId;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.GeneratedJavaClassService;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParametrizedType;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaWildcardTypeArgument;

public class JavaTypeTranslator extends RosettaTypeSwitch<JavaType, Void> {
	private RBuiltinTypeService builtins;
	@Inject
	public JavaTypeTranslator(RBuiltinTypeService builtins) {
		super(builtins);
		this.builtins = builtins;
	}
	@Inject
	private RosettaJavaPackages packages;
	@Inject
	private RosettaExtensions extensions;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private GeneratedJavaClassService generatedJavaClassService;
	
	private JavaClass listClass = JavaClass.from(List.class);
	private JavaClass objectClass = JavaClass.from(Object.class);
	
	private DottedPath getModelPackage(RosettaNamed object) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(object, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + object.eClass().getName() + " " + object.getName() + ". Element is not attached to a RosettaModel.");
		return modelPackage(model);
	}
	private JavaClass rosettaNamedToJavaClass(RosettaNamed object) {
		ModelSymbolId id = getSymbolId(object);
		return new JavaClass(id.getNamespace(), id.getName());
	}
	
	public JavaParametrizedType toPolymorphicList(JavaReferenceType t) {
		return new JavaParametrizedType(listClass, JavaWildcardTypeArgument.extendsBound(t));
	}
	public JavaClass toFunctionJavaClass(RFunction func) {
		switch (func.getOrigin()) {
		case FUNCTION:
			return generatedJavaClassService.toJavaFunction(func.getModelSymbolId());
		case REPORT:
			throw new UnsupportedOperationException();
		case RULE:
			return generatedJavaClassService.toJavaRule(func.getModelSymbolId());
		default:
			throw new IllegalStateException();
		}			 
	}
	public JavaClass toFunctionJavaClass(Function func) {
		return generatedJavaClassService.toJavaFunction(getSymbolId(func));
	}
	public JavaClass toFunctionJavaClass(RosettaExternalFunction func) {
		return new JavaClass(packages.defaultLibFunctions(), func.getName());
	}
	public JavaClass toReportFunctionJavaClass(RosettaBlueprintReport report) {
		return generatedJavaClassService.toJavaReportFunction(getReportId(report));
	}
	public JavaClass toReportTabulatorJavaClass(RosettaBlueprintReport report) {
		return generatedJavaClassService.toJavaReportTabulator(getReportId(report));
	}
	public JavaClass toTabulatorJavaClass(Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		ModelSymbolId typeId = getSymbolId(type);
		if (ruleSource.isEmpty()) {
			DottedPath packageName = typeId.getNamespace().child("reports");
			String simpleName = typeId.getName() + "Tabulator";
			return new JavaClass(packageName, simpleName);
		}
		ModelSymbolId sourceId = getSymbolId(ruleSource.get());
		DottedPath packageName = sourceId.getNamespace().child("reports");
		String simpleName = typeId.getName() + sourceId.getName() + "Tabulator";
		return new JavaClass(packageName, simpleName);
	}
	public JavaReferenceType toMetaJavaType(Attribute attribute) {
		JavaReferenceType attrType = toJavaReferenceType(typeProvider.getRTypeOfSymbol(attribute));
		DottedPath namespace = getModelPackage(attribute.getTypeCall().getType());
		return toMetaJavaType(attrType, extensions.hasMetaFieldAnnotations(attribute), namespace);
	}
	public JavaReferenceType toMetaOrRegularJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType attrType;
		if (expAttr.getRosettaType() != null) {
			attrType = toJavaReferenceType(typeSystem.typeCallToRType(expAttr.getRosettaType()));
		} else {
			attrType = expandedTypeToJavaType(expAttr.getType());
		}
		if (!expAttr.hasMetas()) {
			return attrType;
		}
		DottedPath namespace = getModelPackage(expAttr.getRosettaType().getType());
		return toMetaJavaType(attrType, expAttr.refIndex() < 0, namespace);
	}
	public JavaReferenceType toMultiMetaOrRegularJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType singleType = toMetaOrRegularJavaType(expAttr);
		if (expAttr.isMultiple()) {
			if (expAttr.isDataType() || expAttr.hasMetas()) {
				return toPolymorphicList(singleType);
			} else {
				return new JavaParametrizedType(listClass, singleType);
			}
		}
		return singleType;
	}
	public JavaClass toMetaJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType attrType;
		if (expAttr.getRosettaType() != null) {
			attrType = toJavaReferenceType(typeSystem.typeCallToRType(expAttr.getRosettaType()));
		} else {
			attrType = expandedTypeToJavaType(expAttr.getType());
		}
		DottedPath namespace = getModelPackage(expAttr.getRosettaType().getType());
		return toMetaJavaType(attrType, expAttr.refIndex() < 0, namespace);
	}
	public JavaReferenceType expandedTypeToJavaType(ExpandedType type) {
		if (type.getName().equals(RosettaAttributeExtensions.METAFIELDS_CLASS_NAME) || type.getName().equals(RosettaAttributeExtensions.META_AND_TEMPLATE_FIELDS_CLASS_NAME)) {
			return new JavaClass(packages.basicMetafields(), type.getName());
		}
		if (type.isMetaType()) {//TODO ExpandedType needs to store the underlying type for meta types if we want them to be anything other than strings
			return JavaClass.from(String.class);
		}
		if (type.isBuiltInType()) {
			return toJavaReferenceType(builtins.getType(type.getName(), Collections.emptyMap()));
		}
		return new JavaClass(modelPackage(type.getModel()), type.getName());
	}
	private JavaClass toMetaJavaType(JavaReferenceType base, boolean hasMetaFieldAnnotations, DottedPath namespace) {
		String attributeTypeName = base.getSimpleName();
		String name;
		if (hasMetaFieldAnnotations) {
			name = "FieldWithMeta" + attributeTypeName;
		} else {
			name = "ReferenceWithMeta" + attributeTypeName;
		}
		DottedPath pkg = metaField(namespace);
		return new JavaClass(pkg, name);
	}
	public JavaClass operationToReferenceWithMetaType(Operation op) {
		Attribute attr;
		if (op.getPath() == null) {
			attr = (Attribute)op.getAssignRoot(); // TODO: this won't work when assigning to an alias
		} else {
			List<Segment> segments = op.pathAsSegmentList();
			attr = segments.get(segments.size() - 1).getAttribute();
		}
		DottedPath namespace = getModelPackage(attr.getTypeCall().getType());
		return toMetaJavaType(toJavaReferenceType(typeProvider.getRTypeOfSymbol(attr)), false, namespace);
	}
	
	public JavaClass operationToReferenceWithMetaType(ROperation op) {
		RAttribute attr;
		if (op.getPathTail().isEmpty()) {
			attr = (RAttribute)op.getPathHead(); // TODO: this won't work when assigning to an alias
		} else {
			List<RAttribute> segments = op.getPathTail();
			attr = segments.get(segments.size() - 1);
		}
		return toMetaJavaType(toJavaReferenceType(attr.getRType()), false, attr.getRType().getNamespace());
	}
	
	private String getTypeDebugInfo(RType type) {
		return type.toString() + " (" + type.getClass().getSimpleName() + ")";
	}
	public JavaReferenceType toJavaReferenceType(RType type) {
		JavaType jt = toJavaType(type);
		if (jt instanceof JavaPrimitiveType) {
			return ((JavaPrimitiveType)jt).toReferenceType();
		} else if (jt instanceof JavaReferenceType) {
			return (JavaReferenceType)jt;
		} else {
			throw new UnsupportedOperationException("Cannot convert type " + getTypeDebugInfo(type) + " to a Java reference type.");
		}
	}
	public JavaReferenceType toJavaReferenceType(Optional<RType> type) {
		return type.map(t -> toJavaReferenceType(t)).orElse(objectClass);
	}
	public JavaType toJavaType(RType type) {
		return doSwitch(type, null);
	}
	public JavaType toJavaType(Optional<RType> type) {
		return type.map(t -> toJavaType(t)).orElse(objectClass);
	}
	public JavaClass toJavaType(RDataType type) {
		return caseDataType(type, null);
	}
	
	public JavaType toPolymorphicListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return toPolymorphicList(toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	public JavaReferenceType toListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return new JavaParametrizedType(listClass, toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public JavaClass toImplType(JavaClass type) {
		return new JavaClass(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "Impl");
	}
	public JavaClass toBuilderType(JavaClass type) {
		return new JavaClass(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "Builder");
	}
	public JavaClass toBuilderImplType(JavaClass type) {
		return new JavaClass(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "BuilderImpl");
	}
	
	public JavaClass toValidatorClass(RDataType t) {
		return new JavaClass(validation(getModelPackage(t.getData())), t.getName() + "Validator");
	}
	public JavaClass toTypeFormatValidatorClass(RDataType t) {
		return new JavaClass(validation(getModelPackage(t.getData())), t.getName() + "TypeFormatValidator");
	}
	public JavaClass toOnlyExistsValidatorClass(RDataType t) {
		return new JavaClass(existsValidation(getModelPackage(t.getData())), t.getName() + "OnlyExistsValidator");
	}
	
	private ModelSymbolId getSymbolId(RosettaNamed named) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(named, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + named.eClass().getName() + " " + named.getName() + ". Element is not attached to a RosettaModel.");
		DottedPath namespace = DottedPath.splitOnDots(model.getName());
		return new ModelSymbolId(namespace, named.getName());
	}
	private ModelReportId getReportId(RosettaBlueprintReport report) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(report, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + report.eClass().getName() + " " + report.name() + ". Element is not attached to a RosettaModel.");
		DottedPath namespace = DottedPath.splitOnDots(model.getName());
		
		RegulatoryDocumentReference ref = report.getRegulatoryBody();
		String body = ref.getBody().getName();
		String[] corpuses = ref.getCorpuses().stream().map(c -> c.getName()).toArray(String[]::new);
		
		return new ModelReportId(namespace, body, corpuses);
	}
	private DottedPath modelPackage(RosettaModel model) {
		return DottedPath.splitOnDots(model.getName());
	}
	private DottedPath metaField(DottedPath p) {
		return p.child("metafields");
	}
	private DottedPath validation(DottedPath p) {
		return p.child("validation");
	}
	public DottedPath existsValidation(DottedPath p) {
		return validation(p).child("exists");
	}
	
	@Override
	protected JavaType caseErrorType(RErrorType type, Void context) {
		throw new IllegalArgumentException("Cannot convert an error type to a Java type.");
	}
	@Override
	protected JavaClass caseDataType(RDataType type, Void context) {
		return rosettaNamedToJavaClass(type.getData());
	}
	@Override
	protected JavaClass caseEnumType(REnumType type, Void context) {
		return rosettaNamedToJavaClass(type.getEnumeration());
	}
	@Override
	protected JavaType caseAliasType(RAliasType type, Void context) {
		return toJavaType(type.getRefersTo());
	}
	@Override
	protected JavaType caseNumberType(RNumberType type, Void context) {
		if (!type.isInteger()) {
			return JavaClass.from(BigDecimal.class);
		} else {
			int digits = type.getDigits().orElse(9);
			if (digits <= 9) {
				return JavaPrimitiveType.INT;
			} else if (digits <= 18) {
				return JavaPrimitiveType.LONG;
			} else {
				return JavaClass.from(BigInteger.class);
			}
		}
	}
	@Override
	protected JavaClass caseStringType(RStringType type, Void context) {
		return JavaClass.from(String.class);
	}
	@Override
	protected JavaPrimitiveType caseBooleanType(RBasicType type, Void context) {
		return JavaPrimitiveType.BOOLEAN;
	}
	@Override
	protected JavaClass caseTimeType(RBasicType type, Void context) {
		return JavaClass.from(LocalTime.class);
	}
	@Override
	protected JavaType caseMissingType(RBasicType type, Void context) {
		throw new IllegalArgumentException("Cannot convert a missing type to a Java type.");
	}
	@Override
	protected JavaClass caseNothingType(RBasicType type, Void context) {
		return JavaClass.from(Void.class);
	}
	@Override
	protected JavaClass caseAnyType(RBasicType type, Void context) {
		return objectClass;
	}
	@Override
	protected JavaClass caseDateType(RDateType type, Void context) {
		return JavaClass.from(com.rosetta.model.lib.records.Date.class);
	}
	@Override
	protected JavaClass caseDateTimeType(RDateTimeType type, Void context) {
		return JavaClass.from(LocalDateTime.class);
	}
	@Override
	protected JavaClass caseZonedDateTimeType(RZonedDateTimeType type, Void context) {
		return JavaClass.from(ZonedDateTime.class);
	}
}
