package com.regnosys.rosetta;

import jakarta.inject.Inject;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.RosettaSynonym;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.utils.RosettaConfigExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import org.eclipse.emf.ecore.util.EcoreUtil;
import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;

import java.util.function.Predicate;
import com.regnosys.rosetta.types.RMetaAttribute;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.rosetta.ExternalAnnotationSource;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import java.util.stream.Collectors;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;

public class RosettaEcoreUtil {
	@Inject 
	private RBuiltinTypeService builtins;
	@Inject 
	private RosettaConfigExtension configs;
	
	public boolean isResolved(EObject obj) {
		return obj != null && !obj.eIsProxy();
	}
	
	public Iterable<? extends RosettaFeature> allFeatures(RMetaAnnotatedType t, EObject context) {
		Iterable<? extends RosettaFeature> features = allFeatures(t.getRType(), getResourceSet(context));
		if (t.equals(builtins.NOTHING_WITH_ANY_META)) {
			return features;
		}
		return Iterables.concat(features, getMetaDescriptions(t, context));
	}
	
	public Iterable<? extends RosettaFeature> allFeatures(RMetaAnnotatedType t, EObject context, Predicate<RType> restrictType) {
		Iterable<? extends RosettaFeature> features = allFeatures(t.getRType(), context, restrictType);
		if (t.equals(builtins.NOTHING_WITH_ANY_META)) {
			return features;
		}
		return Iterables.concat(features, getMetaDescriptions(t, context));
	}
	
	public Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context) {
		return allFeatures(t, getResourceSet(context));
	}
	
	public Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context, Predicate<RType> restrictType) {
		if (restrictType.test(t)) {
			return allFeatures(t, context);
		}
		return Collections::emptyIterator;
	}
	
	public ResourceSet getResourceSet(EObject context) {
		if (context == null) {
			return null;
		}
		Resource resource = context.eResource();
		if (resource == null) {
			return null;
		}
		return resource.getResourceSet();
	}
	
	public Iterable<? extends RosettaFeature> allFeatures(RType t, ResourceSet resourceSet) {
		if (t instanceof RDataType) {
			return Iterables.transform(((RDataType) t).getAllAttributes(), RAttribute::getEObject);
		} else if (t instanceof RChoiceType) {
			return allFeatures(((RChoiceType) t).asRDataType(), resourceSet);
		} else if (t instanceof REnumType) {
			return ((REnumType) t).getAllEnumValues();
		} else if (t instanceof RRecordType) {
			if (resourceSet != null) {
				return builtins.toRosettaType(t, RosettaRecordType.class, resourceSet).getFeatures();
			}
		}
		return Collections::emptyIterator;
	}
	
	/*
	 * This method is resolving references during scoping which is not an advised approach.
	 * It could lead to poor performance as it is possible that it could be called upon to
	 * resolve across multiple files. For now this is acceptable as in reality it's not going
	 * going to get called to run across multiple files.
	 * 
	 * TODO: find an alternative approach to this.
	 * 
	 */
    public List<RosettaFeature> getMetaDescriptions(List<RMetaAttribute> metaAttributes, EObject context) {
    	Set<String> metaNames = metaAttributes.stream().map(a -> a.getName()).collect(Collectors.toSet());
 		if (!metaNames.isEmpty()) {
 			List<RosettaFeature> result = new ArrayList<>();
 			for (var mt : configs.findMetaTypes(context)) {
 				if (metaNames.contains(mt.getName().getLastSegment().toString())) {
 					EObject resolved = EcoreUtil.resolve(mt.getEObjectOrProxy(), context);
 					if (resolved instanceof RosettaFeature) {
 						result.add((RosettaFeature) resolved);
 					}
 				}
 			}
 			return result;
 		}
 		return Collections.emptyList();
 	}
	
	@Deprecated // Use RDataType#getAllSuperTypes instead
	public List<Data> getAllSuperTypes(Data data) {
		Set<Data> reversedResult = new LinkedHashSet<>();
		doGetAllSuperTypes(data, reversedResult);
		ArrayList<Data> result = new ArrayList<>(reversedResult);
		Collections.reverse(result);
		return result;
	}
	private void doGetAllSuperTypes(Data current, Set<Data> superTypes) {
		if (superTypes.add(current)) {
			Data s = current.getSuperType();
			if (s != null) {
				doGetAllSuperTypes(s, superTypes);
			}
		}
	}
	
	@Deprecated // Use RDataType#getAllAttributes instead
	public Iterable<Attribute> getAllAttributes(Data data) {
		return Iterables.concat(Iterables.transform(getAllSuperTypes(data), Data::getAttributes));
	}
	
	@Deprecated // Use RDataType#getAllNonOverridenAttributes instead
	public Collection<Attribute> getAllNonOverridenAttributes(Data data) {
		Map<String, Attribute> result = new LinkedHashMap<>();
		getAllAttributes(data).forEach(it -> result.put(it.getName(), it));
		return result.values();
	}
	
	@Deprecated // Use REnumType#getAllParents instead
	public Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		return doGetSuperEnumerations(e, new LinkedHashSet<>());
	}
	private Set<RosettaEnumeration> doGetSuperEnumerations(RosettaEnumeration e, Set<RosettaEnumeration> seenEnums) {
		if(e != null && seenEnums.add(e))
			doGetSuperEnumerations(e.getParent(), seenEnums);
		return seenEnums;
	}
	
	@Deprecated // Use REnumType#getAllEnumValues instead
	public Iterable<RosettaEnumValue> getAllEnumValues(RosettaEnumeration e) {
		return Iterables.concat(Iterables.transform(getAllSuperEnumerations(e), RosettaEnumeration::getEnumValues));
	}
	
	public Attribute getParentAttribute(Attribute attr) {
		EObject container = attr.eContainer();
		if (container instanceof Data) {
			Data t = (Data) container;
			Set<Data> visited = new HashSet<>();
			visited.add(t);
			Data st = t.getSuperType();
			while (st != null) {
				Attribute p = st.getAttributes().stream().filter(it -> it.getName().equals(attr.getName())).findAny().orElse(null);
				if (p != null) {
					return p;
				}
				st = st.getSuperType();
				if (!visited.add(st)) {
					return null;
				}
			}
		}
		return null;
	}
	public List<EObject> getParentsOfExternalType(RosettaExternalClass externalType) {
		ExternalAnnotationSource source = EcoreUtil2.getContainerOfType(externalType, ExternalAnnotationSource.class);
		if (source == null) {
			return Collections.emptyList();
		}
		
		Data type = externalType.getData();
		Set<EObject> parents = new LinkedHashSet<>();
		RosettaExternalClass superTypeInSource = null;
		if (type.getSuperType() != null) {
			superTypeInSource = findSuperTypeInSource(type.getSuperType(), null, source);
			if (superTypeInSource != null) {
				parents.add(superTypeInSource);
			}
		}
		
		List<ExternalAnnotationSource> superSources = getSuperSources(source);
		if (superSources.isEmpty()) {
			parents.add(type);
		} else {
			Set<ExternalAnnotationSource> visitedSources = new HashSet<>();
			visitedSources.add(source);
			Data stop = superTypeInSource == null ? null : superTypeInSource.getData();
			superSources.forEach(it ->
				collectParentsOfTypeInSource(parents, type, stop, it, visitedSources)
			);
		}
				
		return new ArrayList<>(parents);
	}
	private void collectParentsOfTypeInSource(Set<EObject> parents, Data type, Data stop, ExternalAnnotationSource currentSource, Set<ExternalAnnotationSource> visitedSources) {
		if (!visitedSources.add(currentSource)) {
			return;
		}
		
		RosettaExternalClass superTypeInSource = findSuperTypeInSource(type, stop, currentSource);
		if (superTypeInSource != null) {
			parents.add(superTypeInSource);
		}
		List<ExternalAnnotationSource> superSources = getSuperSources(currentSource);
		if (superSources.isEmpty()) {
			parents.add(type);
		} else {
			Data newStop;
			if (superTypeInSource != null && superTypeInSource.getData() != null) {
				newStop = superTypeInSource.getData();
			} else {
				newStop = stop;
			}
			superSources.forEach(it ->
				collectParentsOfTypeInSource(parents, type, newStop, it, visitedSources)
			);
		}
	}
	private RosettaExternalClass findSuperTypeInSource(Data type, Data stop, ExternalAnnotationSource source) {
		Set<Data> visitedTypes = new HashSet<>();
		Data current = type;
		
		while (current != null && !current.equals(stop) && !visitedTypes.add(current)) {
			Data d = current;
			RosettaExternalClass externalType = source.getExternalClasses().stream().filter(ext -> ext.getData().equals(d)).findAny().orElse(null);
			if (externalType != null) {
				return externalType;
			}
			current = current.getSuperType();
		}
		return null;
	}
	private List<ExternalAnnotationSource> getSuperSources(ExternalAnnotationSource source) {
		if (source instanceof RosettaExternalSynonymSource) {
			return ((RosettaExternalSynonymSource) source).getSuperSources().stream()
					.filter(s -> s instanceof ExternalAnnotationSource)
					.map(s -> (ExternalAnnotationSource) s)
					.toList();
		} else if (source instanceof RosettaExternalRuleSource) {
			return ((RosettaExternalRuleSource) source).getSuperSources();
		} else {
			return Collections.emptyList();
		}
	}
	
	
	public Set<RosettaSynonym> getAllSynonyms(RosettaSynonym s) {
		return doGetSynonyms(s, new LinkedHashSet<>());
	}
		
	private Set<RosettaSynonym> doGetSynonyms(RosettaSynonym s, Set<RosettaSynonym> seenSynonyms) {
		if(s != null && seenSynonyms.add(s)) 
			doGetSynonyms(s, seenSynonyms);
		return seenSynonyms;
	}
	
	@Deprecated
	public Iterable<AnnotationRef> metaAnnotations(Annotated it) {
		return allAnnotations(it, ann -> "metadata".equals(ann.getName()));
	}
	private boolean metaAttributeExists(Annotated it, Predicate<Attribute> test) {
		return Iterables.any(metaAnnotations(it), ref -> isResolved(ref.getAttribute()) && test.test(ref.getAttribute()));
	}
	public boolean isAttributeMeta(String name) {
		return !isTypeMeta(name);
	}
	public boolean isTypeMeta(String name) {
		return "key".equals(name) || "template".equals(name);
	}
	@Deprecated
	public boolean hasKeyedAnnotation(Annotated it) {
		return metaAttributeExists(it, attr -> "key".equals(attr.getName()));
	}
	
	@Deprecated
	public boolean hasTemplateAnnotation(Annotated it) {
		return metaAttributeExists(it, attr -> "template".equals(attr.getName()));
	}
	
	@Deprecated
	public boolean hasMetaDataAnnotations(Annotated it) {
		return metaAttributeExists(it, attr -> List.of("reference", "location", "scheme", "id").contains(attr.getName()));
	}
	
	@Deprecated
	public boolean hasMetaFieldAnnotations(Annotated it) {
		return metaAttributeExists(it, attr -> !List.of("reference", "address").contains(attr.getName()));
	}
	
	@Deprecated
	public boolean hasMetaDataAddress(Annotated it) {
		return metaAttributeExists(it, attr -> "address".equals(attr.getName()));
	}
	
	@Deprecated
	public boolean hasIdAnnotation(Annotated it) {
		return metaAttributeExists(it, attr -> "id".equals(attr.getName()));
	}
	@Deprecated
	public boolean hasReferenceAnnotation(Annotated it) {
		return metaAttributeExists(it, attr -> "reference".equals(attr.getName()));
	}
	@Deprecated
	public boolean hasCalculationAnnotation(Annotated it) {
		return allAnnotations(it, ann -> "calculation".equals(ann.getName())).iterator().hasNext();
	}
	@Deprecated
	private Iterable<AnnotationRef> allAnnotations(Annotated withAnnotations, Predicate<Annotation> filter) {
		return Iterables.filter(
				withAnnotations.getAnnotations(),
				ref -> isResolved(ref.getAnnotation()) && filter.test(ref.getAnnotation())
			);
	}
	
	@Deprecated
	public boolean isConstraintCondition(Condition cond) {
		return isOneOf(cond) || isChoice(cond);
	}
	
	private boolean isOneOf(Condition cond) {
		return cond.getExpression() instanceof OneOfOperation;
	}
	
	private boolean isChoice(Condition cond) {
		return cond.getExpression() instanceof ChoiceOperation;
	}
	
 	private List<RosettaFeature> getMetaDescriptions(RMetaAnnotatedType type, EObject context) {
 		return getMetaDescriptions(type.getMetaAttributes(), context);
 	}
}
