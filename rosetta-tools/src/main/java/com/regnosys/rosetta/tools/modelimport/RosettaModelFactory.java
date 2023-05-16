package com.regnosys.rosetta.tools.modelimport;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdAnnotatedElements;
import org.xmlet.xsdparser.xsdelements.XsdAnnotation;
import org.xmlet.xsdparser.xsdelements.XsdAnnotationChildren;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdElement;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;
import org.xmlet.xsdparser.xsdelements.XsdRestriction;
import org.xmlet.xsdparser.xsdelements.XsdSimpleContent;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdEnumeration;

import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaBody;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalEnum;
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaSegment;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.RosettaSynonymBody;
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;

@Singleton
public class RosettaModelFactory {
	
	public static final String UNBOUNDED = "unbounded";
	public static final String DOC_ANNOTATION_SOURCE_NAME = "Name";
	public static final String BASIC_TYPE_NAMESPACE = "com.rosetta.model";
	public static final String PROJECT_VERSION = "${project.version}";
	
	private final ResourceSet resourceSet;
	private final RosettaTypeMappings rosettaTypeMappings;

	private final List<String> documentationSources = List.of("Definition");
	private final Resource basicTypeResource;

	@Inject
	public RosettaModelFactory(ResourceSet resourceSet, RosettaTypeMappings rosettaTypeMappings) {
		this.resourceSet = resourceSet;
		this.rosettaTypeMappings = rosettaTypeMappings;
		this.basicTypeResource = createBasicType(rosettaTypeMappings);
	}

	public RosettaModel createRosettaModel(String type, GenerationProperties properties, List<String> imports) {
		Resource resource = createResource(properties.getNamespace(), type);

		RosettaModel rosettaModel = RosettaFactory.eINSTANCE.createRosettaModel();
		rosettaModel.setName(properties.getNamespace());
		rosettaModel.setDefinition(properties.getNamespaceDefinition());
		rosettaModel.setVersion(PROJECT_VERSION);
		imports.stream().map(this::createImport).forEach(rosettaModel.getImports()::add);

		resource.getContents().add(rosettaModel);
		return rosettaModel;
	}

	public RosettaBody createBody(String bodyType, String bodyName, String bodyDefinition) {
		RosettaBody rosettaBody = RosettaFactory.eINSTANCE.createRosettaBody();
		rosettaBody.setBodyType(bodyType);
		rosettaBody.setDefinition(bodyDefinition);
		rosettaBody.setName(bodyName);
		return rosettaBody;
	}

	public RosettaSegment createSegment(String segmentName) {
		RosettaSegment rosettaSegment = RosettaFactory.eINSTANCE.createRosettaSegment();
		rosettaSegment.setName(segmentName);
		return rosettaSegment;
	}
	public RosettaCorpus createCorpus(RosettaBody rosettaBody, String corpusType, String corpusName, String corpusDisplayName, String corpusDefinition) {
		RosettaCorpus rosettaCorpus = RosettaFactory.eINSTANCE.createRosettaCorpus();
		rosettaCorpus.setBody(rosettaBody);
		rosettaCorpus.setCorpusType(corpusType);
		rosettaCorpus.setName(corpusName);
		rosettaCorpus.setDisplayName(corpusDisplayName);
		rosettaCorpus.setDefinition(corpusDefinition);
		return rosettaCorpus;
	}

	public Data createData(XsdNamedElements namedElements) {
		Data data = SimpleFactory.eINSTANCE.createData();
		data.setName(namedElements.getName());
		extractDocs(namedElements).ifPresent(data::setDefinition);
		return data;
	}

	public RosettaExternalSynonymSource createExternalSynonymSource(String synonymSourceName) {
		RosettaExternalSynonymSource externalSynonymSource = RosettaFactory.eINSTANCE.createRosettaExternalSynonymSource();
		externalSynonymSource.setName(synonymSourceName);
		return externalSynonymSource;
	}

	public RosettaExternalClass createRosettaExternalClass(XsdComplexType complexType) {

		Data data = findData(complexType.getName());

		RosettaExternalClass rosettaExternalClass = RosettaFactory.eINSTANCE.createRosettaExternalClass();
		rosettaExternalClass.setTypeRef(data);

		data.getAttributes().forEach(attr -> {
			RosettaExternalRegularAttribute rosettaExternalRegularAttribute = RosettaFactory.eINSTANCE.createRosettaExternalRegularAttribute();
			rosettaExternalRegularAttribute.setAttributeRef(attr);
			rosettaExternalRegularAttribute.setOperator(ExternalValueOperator.PLUS);
			rosettaExternalClass.getRegularAttributes().add(rosettaExternalRegularAttribute);

			RosettaExternalSynonym rosettaExternalSynonym = RosettaFactory.eINSTANCE.createRosettaExternalSynonym();

			RosettaSynonymBody rosettaSynonymBody = RosettaFactory.eINSTANCE.createRosettaSynonymBody();

			RosettaSynonymValueBase rosettaSynonymValueBase = RosettaFactory.eINSTANCE.createRosettaSynonymValueBase();
			rosettaSynonymValueBase.setName(StringExtensions.toFirstUpper(attr.getName()));

			rosettaSynonymBody.getValues().add(rosettaSynonymValueBase);
			rosettaExternalSynonym.setBody(rosettaSynonymBody);
			rosettaExternalRegularAttribute.getExternalSynonyms().add(rosettaExternalSynonym);
		});

		return rosettaExternalClass;
	}

	public RosettaExternalEnum createRosettaExternalEnum(XsdSimpleType simpleType) {

		RosettaEnumeration rosettaEnumeration = findEnum(simpleType.getName());

		RosettaExternalEnum rosettaExternalEnum = RosettaFactory.eINSTANCE.createRosettaExternalEnum();
		rosettaExternalEnum.setTypeRef(rosettaEnumeration);

		rosettaEnumeration.getEnumValues().forEach(enumValue -> {
			RosettaExternalEnumValue rosettaExternalEnumValue = RosettaFactory.eINSTANCE.createRosettaExternalEnumValue();
			rosettaExternalEnumValue.setEnumRef(enumValue);
			rosettaExternalEnumValue.setOperator(ExternalValueOperator.PLUS);
			rosettaExternalEnum.getRegularValues().add(rosettaExternalEnumValue);

			RosettaEnumSynonym rosettaEnumSynonym = RosettaFactory.eINSTANCE.createRosettaEnumSynonym();
			rosettaEnumSynonym.setSynonymValue(StringExtensions.toFirstUpper(enumValue.getName()));
			rosettaExternalEnumValue.getExternalEnumSynonyms().add(rosettaEnumSynonym);
		});

		return rosettaExternalEnum;
	}
	
	public void addAttributesToData(XsdNamedElements complexType, RosettaBody rosettaBody, RosettaCorpus rosettaCorpus, RosettaSegment rosettaSegment, List<? extends XsdAbstractElement> xsdElements) {
		Data data = findData(complexType.getName());
		// handle complex types
		Optional.of(complexType)
			.filter(XsdComplexType.class::isInstance)
			.map(XsdComplexType.class::cast)
			.map(XsdComplexType::getElements).stream()
			.flatMap(Collection::stream)
			.map(ReferenceBase::getElement)
			.filter(XsdElement.class::isInstance)
			.map(XsdElement.class::cast)
			.filter(xsdElement -> xsdElement.getType() != null)
			.map(element -> createAttribute(element, rosettaBody, rosettaCorpus, rosettaSegment))
			.forEach(data.getAttributes()::add);
	}

	public void addSuperType(XsdComplexType complexType) {
		Data data = findData(complexType.getName());
		Optional.of(complexType)
			.map(XsdComplexType::getSimpleContent)
			.map(XsdSimpleContent::getXsdExtension)
			.ifPresent(ex -> {
				XsdNamedElements base = ex.getBase();
				Data superType = findData(base.getName());
				data.setSuperType(superType);
			});
	}

	public RosettaEnumeration createRosettaEnumeration(XsdSimpleType en) {
		RosettaEnumeration rosettaEnumeration = RosettaFactory.eINSTANCE.createRosettaEnumeration();
		rosettaEnumeration.setName(en.getName());
		extractDocs(en).ifPresent(rosettaEnumeration::setDefinition);
		List<XsdEnumeration> enumeration = en.getRestriction().getEnumeration();

		enumeration.stream()
			.map(this::createEnumValue)
			.forEach(rosettaEnumeration.getEnumValues()::add);
		return rosettaEnumeration;
	}

	public void saveResources(String outputDirectory) throws IOException {
		List<Resource> resources = resourceSet.getResources().stream()
			.filter(r -> r != basicTypeResource)
			.collect(Collectors.toList());
		for (Resource resource : resources) {
			String fileName = resource.getURI().toFileString();
			resource.setURI(URI.createFileURI(outputDirectory + "/" + fileName));
			resource.save(Map.of());
		}
	}

	private Import createImport(String imp) {
		Import anImport = RosettaFactory.eINSTANCE.createImport();
		anImport.setImportedNamespace(imp);
		return anImport;
	}

	private Attribute createAttribute(XsdElement element, RosettaBody rosettaBody, RosettaCorpus rosettaCorpus, RosettaSegment rosettaSegment) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// definition
		extractDocs(element).ifPresent(attribute::setDefinition);

		// name
		attribute.setName(StringExtensions.toFirstLower(element.getName()));
		
		// type
		RosettaType rosettaType = Optional.of(element)
				// Built-in type with no restrictions
				.map(XsdElement::getTypeAsBuiltInDataType)
				.map(XsdNamedElements::getRawName)
				// Or Built-in type with restrictions
				.or(() -> Optional.of(element)
						.map(XsdElement::getTypeAsSimpleType)
						.map(XsdSimpleType::getRestriction)
						.map(XsdRestriction::getBaseAsBuiltInDataType)
						.map(XsdNamedElements::getRawName))
				.map(rosettaTypeMappings::getRosettaBasicType)
				// Or complex type
				.orElseGet(() -> getRosettaComplexType(element.getType()));
		TypeCall typeCall = RosettaFactory.eINSTANCE.createTypeCall();
		typeCall.setType(rosettaType);
		attribute.setTypeCall(typeCall);

		// cardinality
		RosettaCardinality rosettaCardinality = RosettaFactory.eINSTANCE.createRosettaCardinality();
		rosettaCardinality.setInf(element.getMinOccurs());
		if (element.getMaxOccurs().equals(UNBOUNDED)) {
			rosettaCardinality.setUnbounded(true);
		} else {
			rosettaCardinality.setSup(Integer.parseInt(element.getMaxOccurs()));
		}
		attribute.setCard(rosettaCardinality);

		// docReference
		Optional.ofNullable(element.getTypeAsSimpleType())
			.map(xsdName -> createRosettaDocReference(xsdName.getName(), rosettaBody, rosettaCorpus, rosettaSegment, extractDocs(xsdName)))
			.ifPresent(attribute.getReferences()::add);
		
		return attribute;
	}

	private RosettaDocReference createRosettaDocReference(String xsdName, RosettaBody rosettaBody, RosettaCorpus rosettaCorpus, RosettaSegment rosettaSegment, Optional<String> provision) {
		RosettaDocReference rosettaDocReference = RosettaFactory.eINSTANCE.createRosettaDocReference();

		RegulatoryDocumentReference regulatoryDocumentReference = RosettaFactory.eINSTANCE.createRegulatoryDocumentReference();
		regulatoryDocumentReference.setBody(rosettaBody);
		regulatoryDocumentReference.getCorpuses().add(rosettaCorpus);
		rosettaDocReference.setDocReference(regulatoryDocumentReference);

		provision.ifPresent(rosettaDocReference::setProvision);

		RosettaSegmentRef rosettaSegmentRef = RosettaFactory.eINSTANCE.createRosettaSegmentRef();
		rosettaSegmentRef.setSegment(rosettaSegment);
		rosettaSegmentRef.setSegmentRef(xsdName);
		regulatoryDocumentReference.getSegments().add(rosettaSegmentRef);

		return rosettaDocReference;
	}

	private RosettaEnumValue createEnumValue(XsdEnumeration ev) {
		String value = ev.getValue();
		RosettaEnumValue rosettaEnumValue = RosettaFactory.eINSTANCE.createRosettaEnumValue();
		rosettaEnumValue.setName(value);
		extractDocs(ev).ifPresent(rosettaEnumValue::setDefinition);
		extractDocs(ev, DOC_ANNOTATION_SOURCE_NAME)
			.filter(x -> StringUtils.isNotEmpty(x))
			.filter(x -> !x.equals(ev.getValue()))
			.ifPresent(rosettaEnumValue::setDisplay);

		return rosettaEnumValue;
	}

	private Resource createResource(String namespace, String type) {
		return resourceSet.createResource(URI.createURI(namespace.substring(namespace.indexOf('.') + 1)
			.replace('.', '-') + "-" + type + ".rosetta"));
	}
	
	private Optional<String> extractDocs(XsdAnnotatedElements ev) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				// default to definition if source not specified
				.filter(x -> x.getSource() == null || documentationSources.contains(x.getSource()))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			);
	}

	private Optional<String> extractDocs(XsdAnnotatedElements ev, String docAnnotationSourceName) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				.filter(x -> x.getSource() != null)
				.filter(x -> x.getSource().equals(docAnnotationSourceName))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			);
	}

	private RosettaType getRosettaComplexType(String typeName) {
		return findRosettaTypeIfPresent(typeName)
			.orElseThrow(() -> new RuntimeException("No type found for " + typeName));
	}

	private Data findData(String typeName) {
		return findRosettaTypeIfPresent(typeName)
			.filter(Data.class::isInstance)
			.map(Data.class::cast)
			.orElseThrow(() -> new RuntimeException("No type found for " + typeName));
	}

	private RosettaEnumeration findEnum(String enumName) {
		return findRosettaTypeIfPresent(enumName)
			.filter(RosettaEnumeration.class::isInstance)
			.map(RosettaEnumeration.class::cast)
			.orElseThrow(() -> new RuntimeException("No enum found for " + enumName));
	}

	private Optional<RosettaType> findRosettaTypeIfPresent(String typeName) {
		return resourceSet.getResources().stream()
			.map(Resource::getContents)
			.flatMap(Collection::stream)
			.filter(RosettaModel.class::isInstance)
			.map(RosettaModel.class::cast)
			.map(RosettaModel::getElements)
			.flatMap(Collection::stream)
			.filter(RosettaType.class::isInstance)
			.map(RosettaType.class::cast)
			.filter(x -> x.getName().equals(typeName))
			.findFirst();
	}

	private Resource createBasicType(RosettaTypeMappings rosettaTypeMappings) {
		Resource resource = createResource(BASIC_TYPE_NAMESPACE, "build-in");
		RosettaModel rosettaModel = RosettaFactory.eINSTANCE.createRosettaModel();
		rosettaModel.setName(BASIC_TYPE_NAMESPACE);
		rosettaModel.setVersion(PROJECT_VERSION);
		resource.getContents().add(rosettaModel);
		rosettaModel.getElements().addAll(rosettaTypeMappings.getAllBasicTypes());
		return resource;
	}
}
