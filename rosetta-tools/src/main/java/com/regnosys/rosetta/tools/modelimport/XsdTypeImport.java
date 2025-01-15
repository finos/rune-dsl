/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.tools.modelimport;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.Necessity;
import org.xmlet.xsdparser.xsdelements.*;
import org.xmlet.xsdparser.xsdelements.enums.UsageEnum;
import org.xmlet.xsdparser.xsdelements.visitors.AttributesVisitor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.rosetta.util.serialisation.AttributeXMLConfiguration;
import com.rosetta.util.serialisation.AttributeXMLRepresentation;
import com.rosetta.util.serialisation.TypeXMLConfiguration;

import static org.xmlet.xsdparser.xsdelements.XsdAbstractElement.MIN_OCCURS_TAG;

public class XsdTypeImport extends AbstractXsdImport<XsdNamedElements, List<Data>> {
    private record ChoiceGroup(List<Attribute> attributes, boolean required) {}

	public final String UNBOUNDED = "unbounded";
	public final String SIMPLE_EXTENSION_ATTRIBUTE_NAME = "value";

	private final XsdUtil util;
	private final RosettaEcoreUtil ecoreUtil;
	
	@Inject
	public XsdTypeImport(XsdUtil util, RosettaEcoreUtil ecoreUtil) {
		super(XsdNamedElements.class);
		this.util = util;
		this.ecoreUtil = ecoreUtil;
	}

    @Override
    public List<XsdNamedElements> filterTypes(List<XsdAbstractElement> elements) {
        return elements.stream()
                .filter(elem -> elem instanceof XsdComplexType || elem instanceof XsdGroup)
                .map(elem -> (XsdNamedElements)elem)
                .collect(Collectors.toList());
    }

	private Optional<XsdNamedElements> getBaseSimpleType(XsdComplexType xsdType) {
        Optional<XsdExtension> optExt = getExtension(xsdType);
		return optExt
				.<XsdNamedElements>map(XsdExtension::getBaseAsSimpleType)
                .or(() -> optExt.map(XsdExtension::getBaseAsBuiltInDataType));
	}
	private Optional<XsdComplexType> getBaseComplexType(XsdComplexType xsdType) {
		return getExtension(xsdType)
				.map(XsdExtension::getBaseAsComplexType);
	}
	private Optional<XsdExtension> getExtension(XsdComplexType xsdType) {
		return Optional.ofNullable(xsdType.getSimpleContent())
				.map(XsdSimpleContent::getXsdExtension)
				.or(() -> Optional.ofNullable(xsdType.getComplexContent())
						.map(XsdComplexContent::getXsdExtension));
	}
	private Optional<XsdAbstractElement> getChildElement(XsdComplexType xsdType) {		
		return getExtension(xsdType)
	    		.map(XsdExtension::getXsdChildElement)
	    		.or(() -> Optional.ofNullable(xsdType.getXsdChildElement()));
	}
    private Stream<XsdAttribute> getAttributes(XsdComplexType xsdType) {
        return getExtension(xsdType)
                .map(XsdExtension::getVisitor)
                .or(() -> Optional.ofNullable(xsdType.getVisitor()))
                .filter(v -> v instanceof AttributesVisitor)
                .map(v -> (AttributesVisitor)v)
                .map(AttributesVisitor::getAllAttributes).stream()
                .flatMap(List::stream)
                .filter(xsdElement -> xsdElement.getType() != null);
    }

	@Override
	public List<Data> registerType(XsdNamedElements xsdType, RosettaXsdMapping xsdMapping, ImportTargetConfig targetConfig) {
        List<Data> result = new ArrayList<>();

        String name = util.toTypeName(xsdType.getRawName(), targetConfig);
        
		Optional<Data> elementType =
			xsdMapping.getElementsWithComplexType(xsdType)
				.map(xsdMapping::getRosettaTypeFromElement)
				.filter(t -> t.getName().toUpperCase().equals(name.toUpperCase()))
				.findAny();
		elementType.ifPresent(d -> d.setName(name));

		Data data = elementType.orElseGet(() -> {
			Data newData = SimpleFactory.eINSTANCE.createData();
			newData.setName(name);
            result.add(newData);
			return newData;
		});
		util.extractDocs(xsdType).ifPresent(typeDocs -> {
			if (data.getDefinition() == null) {
				data.setDefinition(typeDocs);
			} else {
				data.setDefinition(data.getDefinition() + " " + typeDocs);
			}
		});
		
		if (xsdType instanceof XsdGroup group) {
            xsdMapping.registerGroup(group, data);
            initialCompleteData(data, Stream.of(group.getChildElement()), null, xsdMapping, result, targetConfig);
        } else {
        	XsdComplexType ct = (XsdComplexType)xsdType;
            xsdMapping.registerComplexType(ct, data);
        	
        	// If the complex type extends a simple type, simulate this
            // by adding a `value` attribute of the corresponding type.
            if (getBaseSimpleType(ct).isPresent()) {
                Attribute valueAttr = createValueAttribute(targetConfig);
                data.getAttributes().add(valueAttr);
                xsdMapping.registerAttribute(ct, valueAttr);
            }
            
            initialCompleteData(data, Streams.concat(getChildElement(ct).stream(), getAttributes(ct)), null, xsdMapping, result, targetConfig);
        }

        // Post process: make sure all names are unique:
        util.makeNamesUnique(result);
        result.forEach(d -> {
            util.makeNamesUnique(d.getAttributes());
        });

		elementType.ifPresent(d -> util.makeNamesUnique(d.getAttributes()));
		
		return result;
	}

	private void registerXsdElementsRecursively(Data currentData, XsdAbstractElement abstractElement, ChoiceGroup currentChoiceGroup, List<ChoiceGroup> currentChoiceGroups, RosettaXsdMapping xsdMapping, List<Data> result, ImportTargetConfig config) {
        if (abstractElement instanceof XsdElement elem) {
            Attribute attr = createAttributeFromElement(elem, currentChoiceGroup, config);
            xsdMapping.registerAttribute(elem, attr);
            currentData.getAttributes().add(attr);
        } else if (abstractElement instanceof XsdGroup group) {
            Attribute attr = createAttributeFromGroup(group, currentChoiceGroup, config);
            xsdMapping.registerAttribute(group, attr);
            currentData.getAttributes().add(attr);
        } else if (abstractElement instanceof XsdAttribute xsdAttr) {
        	Attribute attr = createAttributeFromXsdAttribute(xsdAttr, config);
            xsdMapping.registerAttribute(xsdAttr, attr);
            currentData.getAttributes().add(attr);
        } else if (abstractElement instanceof XsdSequence seq) {
            if (currentChoiceGroup != null || isMulti(seq.getMaxOccurs()) || seq.getMinOccurs() == 0) {
                Data newData = createData(currentData.getName() + "Sequence", seq.getXsdElements(), null, xsdMapping, result, config);
                xsdMapping.registerComplexType(seq, newData);

                Attribute attr = createAttributeFromSequence(seq, newData.getName(), currentChoiceGroup, config);
                xsdMapping.registerAttribute(seq, attr);
                currentData.getAttributes().add(attr);
            } else {
                seq.getXsdElements().forEach(child -> registerXsdElementsRecursively(currentData, child, null, currentChoiceGroups, xsdMapping, result, config));
            }
        } else if (abstractElement instanceof XsdAll all) {
            if (currentChoiceGroup != null || all.getMinOccurs() == 0) {
                Data newData = createData(currentData.getName() + "All", all.getXsdElements(), null, xsdMapping, result, config);
                xsdMapping.registerComplexType(all, newData);

                Attribute attr = createAttributeFromAll(all, newData.getName(), currentChoiceGroup, config);
                xsdMapping.registerAttribute(all, attr);
                currentData.getAttributes().add(attr);
            } else {
                all.getXsdElements().forEach(child -> registerXsdElementsRecursively(currentData, child, null, currentChoiceGroups, xsdMapping, result, config));
            }
        } else if (abstractElement instanceof XsdChoice choice) {
            if (currentChoiceGroup != null || isMulti(choice.getMaxOccurs())) {
                boolean required = choice.getMinOccurs() > 0 && choice.getXsdElements().allMatch(elem -> Integer.parseInt(elem.getAttributesMap().getOrDefault(MIN_OCCURS_TAG, "1")) > 0);
                ChoiceGroup initialChoiceGroup = new ChoiceGroup(new ArrayList<>(), required);
                Data newData = createData(currentData.getName() + "Choice", choice.getXsdElements(), initialChoiceGroup, xsdMapping, result, config);
                xsdMapping.registerComplexType(choice, newData);

                Attribute attr = createAttributeFromChoice(choice, newData.getName(), currentChoiceGroup, config);
                xsdMapping.registerAttribute(choice, attr);
                currentData.getAttributes().add(attr);
            } else {
                ChoiceGroup newChoiceGroup = new ChoiceGroup(new ArrayList<>(), choice.getMinOccurs() > 0);
                currentChoiceGroups.add(newChoiceGroup);
                choice.getXsdElements().forEach(child -> registerXsdElementsRecursively(currentData, child, newChoiceGroup, currentChoiceGroups, xsdMapping, result, config));
            }
        }
    }
    private Data createData(String name, Stream<XsdAbstractElement> abstractElements, ChoiceGroup initialChoiceGroup, RosettaXsdMapping xsdMapping, List<Data> result, ImportTargetConfig config) {
        // Create type
        Data data = SimpleFactory.eINSTANCE.createData();
        data.setName(name);
        result.add(data);

        initialCompleteData(data, abstractElements, initialChoiceGroup, xsdMapping, result, config);

        return data;
    }
    private void initialCompleteData(Data data, Stream<XsdAbstractElement> abstractElements, ChoiceGroup initialChoiceGroup, RosettaXsdMapping xsdMapping, List<Data> result, ImportTargetConfig config) {
        // Add attributes
        List<ChoiceGroup> choiceGroups = new ArrayList<>();
        if (initialChoiceGroup != null) {
            choiceGroups.add(initialChoiceGroup);
        }
        abstractElements.forEach(elem -> registerXsdElementsRecursively(data, elem, initialChoiceGroup, choiceGroups, xsdMapping, result, config));
    }

	@Override
	public void completeType(XsdNamedElements xsdType, RosettaXsdMapping xsdMapping) {
		if (xsdType instanceof XsdGroup group) {
			Data data = xsdMapping.getRosettaTypeFromGroup(group);
			completeData(data, Stream.of(group.getChildElement()), null, xsdMapping);
		} else {
			XsdComplexType ct = (XsdComplexType)xsdType;
            Data data = xsdMapping.getRosettaTypeFromComplex(ct);
			
			// Add supertype
			getBaseComplexType(ct)
				.ifPresent(base -> {
					Data superType = xsdMapping.getRosettaTypeFromComplex(base);
					data.setSuperType(superType);
				});
			
			// If the complex type extends a simple type, add the corresponding type
			// to the dedicated `value` attribute.
			getBaseSimpleType(ct)
				.ifPresent(base -> {
					Attribute attr = xsdMapping.getAttribute(xsdType);
					attr.setTypeCall(xsdMapping.getRosettaTypeCall(base));
				});
			
			completeData(data, Stream.concat(getChildElement(ct).stream(), getAttributes(ct)), null, xsdMapping);
		}
	}
	private void completeXsdElementsRecursively(Data currentData, XsdAbstractElement abstractElement, ChoiceGroup currentChoiceGroup, List<ChoiceGroup> currentChoiceGroups, RosettaXsdMapping xsdMapping) {
        if (abstractElement instanceof XsdElement elem) {
        	Attribute attr = xsdMapping.getAttribute(elem);
			if (elem.getTypeAsXsd() != null) {
				attr.setTypeCall(xsdMapping.getRosettaTypeCall(elem.getTypeAsXsd()));
			} else {
				// TODO
				attr.setTypeCall(xsdMapping.getRosettaTypeCallFromBuiltin("string"));
			}
			if (currentChoiceGroup != null) {
				currentChoiceGroup.attributes.add(attr);
			}
        } else if (abstractElement instanceof XsdGroup group) {
        	Attribute attr = xsdMapping.getAttribute(group);
			attr.setTypeCall(xsdMapping.getRosettaTypeCall(group));
			if (currentChoiceGroup != null) {
				currentChoiceGroup.attributes.add(attr);
			}
        } else if (abstractElement instanceof XsdAttribute xsdAttr) {
        	Attribute attr = xsdMapping.getAttribute(xsdAttr);
			if (xsdAttr.getXsdSimpleType() != null) {
				attr.setTypeCall(xsdMapping.getRosettaTypeCall(xsdAttr.getXsdSimpleType()));
			} else if (xsdAttr.getTypeAsBuiltInType() != null) {
				attr.setTypeCall(xsdMapping.getRosettaTypeCall(xsdAttr.getTypeAsBuiltInType()));
			} else {
				// TODO
				attr.setTypeCall(xsdMapping.getRosettaTypeCallFromBuiltin("string"));
			}
			if (currentChoiceGroup != null) {
				currentChoiceGroup.attributes.add(attr);
			}
        } else if (abstractElement instanceof XsdSequence seq) {
            if (currentChoiceGroup != null || isMulti(seq.getMaxOccurs()) || seq.getMinOccurs() == 0) {
            	Data newData = xsdMapping.getRosettaTypeFromComplex(seq);
            	completeData(newData, seq.getXsdElements(), null, xsdMapping);
            	
            	Attribute attr = xsdMapping.getAttribute(seq);
    			attr.setTypeCall(xsdMapping.getRosettaTypeCall(seq));
    			if (currentChoiceGroup != null) {
    				currentChoiceGroup.attributes.add(attr);
    			}
            } else {
            	seq.getXsdElements().forEach(elem -> completeXsdElementsRecursively(currentData, elem, null, currentChoiceGroups, xsdMapping));
            }
        } else if (abstractElement instanceof XsdAll all) {
            if (currentChoiceGroup != null || all.getMinOccurs() == 0) {
            	Data newData = xsdMapping.getRosettaTypeFromComplex(all);
            	completeData(newData, all.getXsdElements(), null, xsdMapping);
            	
            	Attribute attr = xsdMapping.getAttribute(all);
    			attr.setTypeCall(xsdMapping.getRosettaTypeCall(all));
    			if (currentChoiceGroup != null) {
    				currentChoiceGroup.attributes.add(attr);
    			}
            } else {
            	all.getXsdElements().forEach(elem -> completeXsdElementsRecursively(currentData, elem, null, currentChoiceGroups, xsdMapping));
            }
        } else if (abstractElement instanceof XsdChoice choice) {
            if (currentChoiceGroup != null || isMulti(choice.getMaxOccurs())) {
            	boolean required = choice.getMinOccurs() > 0 && choice.getXsdElements().allMatch(elem -> Integer.parseInt(elem.getAttributesMap().getOrDefault(MIN_OCCURS_TAG, "1")) > 0);
                ChoiceGroup initialChoiceGroup = new ChoiceGroup(new ArrayList<>(), required);
                Data newData = xsdMapping.getRosettaTypeFromComplex(choice);
                completeData(newData, choice.getXsdElements(), initialChoiceGroup, xsdMapping);
            	
                Attribute attr = xsdMapping.getAttribute(choice);
    			attr.setTypeCall(xsdMapping.getRosettaTypeCall(choice));
    			if (currentChoiceGroup != null) {
    				currentChoiceGroup.attributes.add(attr);
    			}
            } else {
            	ChoiceGroup newChoiceGroup = new ChoiceGroup(new ArrayList<>(), choice.getMinOccurs() > 0);
                currentChoiceGroups.add(newChoiceGroup);
            	choice.getXsdElements().forEach(child -> completeXsdElementsRecursively(currentData, child, newChoiceGroup, currentChoiceGroups, xsdMapping));
            }
        }
    }
	private void completeData(Data data, Stream<XsdAbstractElement> abstractElements, ChoiceGroup initialChoiceGroup, RosettaXsdMapping xsdMapping) {
        List<ChoiceGroup> choiceGroups = new ArrayList<>();
        if (initialChoiceGroup != null) {
            choiceGroups.add(initialChoiceGroup);
        }
		
        // Complete attributes
        abstractElements.forEach(elem -> completeXsdElementsRecursively(data, elem, initialChoiceGroup, choiceGroups, xsdMapping));

        // Add conditions
        choiceGroups.forEach(choiceGroup -> {
			if (choiceGroup.attributes.size() > 1) {
				Condition choice = SimpleFactory.eINSTANCE.createCondition();
				choice.setName("Choice");
				if (choiceGroup.attributes.size() == Iterables.size(ecoreUtil.getAllAttributes(data)) && choiceGroup.required) {
					OneOfOperation oneOf = ExpressionFactory.eINSTANCE.createOneOfOperation();
					oneOf.setOperator("one-of");
					choice.setExpression(oneOf);
				} else {
					ChoiceOperation op = ExpressionFactory.eINSTANCE.createChoiceOperation();
					op.setOperator("choice");
					op.setNecessity(choiceGroup.required ? Necessity.REQUIRED : Necessity.OPTIONAL);
					op.getAttributes().addAll(choiceGroup.attributes);
					choice.setExpression(op);
				}
				data.getConditions().add(choice);
			} else if (choiceGroup.attributes.size() == 1 && choiceGroup.required) {
				Attribute attr = choiceGroup.attributes.get(0);
				attr.getCard().setInf(1);
			}
        });
        
        util.makeNamesUnique(data.getConditions());
    }
	
	public Map<Data, TypeXMLConfiguration> getXMLConfiguration(XsdNamedElements xsdType, RosettaXsdMapping xsdMapping, String schemaTargetNamespace) {
		Data data;
		if (xsdType instanceof XsdGroup group) {
			data = xsdMapping.getRosettaTypeFromGroup(group);
		} else {
			XsdComplexType ct = (XsdComplexType)xsdType;
			data = xsdMapping.getRosettaTypeFromComplex(ct);
			if (xsdMapping.getElementsWithComplexType(ct)
					.map(xsdMapping::getRosettaTypeFromElement)
					.anyMatch(t -> t.equals(data))) {
				// This type is merged into an element.
				return Collections.emptyMap();
			}
		}
		
		Map<Data, TypeXMLConfiguration> result = new LinkedHashMap<>();
		
		Map<String, AttributeXMLConfiguration> attributeConfig = new LinkedHashMap<>();
		TypeXMLConfiguration config = new TypeXMLConfiguration(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.of(attributeConfig),
				Optional.empty()
			);
		result.put(data, config);
		
		completeAttributeConfiguration(attributeConfig, xsdType, xsdMapping, result);
		
		return result;
	}
	public void completeAttributeConfiguration(Map<String, AttributeXMLConfiguration> attributeConfig, XsdNamedElements xsdType, RosettaXsdMapping xsdMapping, Map<Data, TypeXMLConfiguration> result) {
		if (xsdType instanceof XsdGroup group) {
            completeTypeConfiguration(attributeConfig, Stream.of(group.getChildElement()), false, xsdMapping, result);
        } else {
        	XsdComplexType ct = (XsdComplexType)xsdType;
        	
        	// If the complex type extends a simple type, simulate this
            // by adding a `value` attribute of the corresponding type.
            if (getBaseSimpleType(ct).isPresent()) {
            	Attribute attr = xsdMapping.getAttribute(xsdType);
            	attributeConfig.put(attr.getName(), new AttributeXMLConfiguration(
    					Optional.empty(),
    					Optional.empty(),
    					Optional.of(AttributeXMLRepresentation.VALUE)));
            }
            
            completeTypeConfiguration(attributeConfig, Streams.concat(getChildElement(ct).stream(), getAttributes(ct)), false, xsdMapping, result);
        }
	}
	private void getAttributeConfigurationRecursively(Map<String, AttributeXMLConfiguration> currentConfig, XsdAbstractElement abstractElement, boolean isChoiceGroup, RosettaXsdMapping xsdMapping, Map<Data, TypeXMLConfiguration> result) {
        if (abstractElement instanceof XsdElement elem) {
            Attribute attr = xsdMapping.getAttribute(elem);
            if (!elem.getName().equals(attr.getName())) {
            	currentConfig.put(attr.getName(), new AttributeXMLConfiguration(
						Optional.of(elem.getName()),
						Optional.empty(),
						Optional.empty()));
            }
        } else if (abstractElement instanceof XsdGroup group) {
            Attribute attr = xsdMapping.getAttribute(group);
            currentConfig.put(attr.getName(), new AttributeXMLConfiguration(
					Optional.empty(),
					Optional.empty(),
					Optional.of(AttributeXMLRepresentation.VIRTUAL)));
        } else if (abstractElement instanceof XsdAttribute xsdAttr) {
        	Attribute attr = xsdMapping.getAttribute(xsdAttr);
        	currentConfig.put(attr.getName(), new AttributeXMLConfiguration(
        			xsdAttr.getName().equals(attr.getName()) ? Optional.empty() : Optional.of(xsdAttr.getName()),
					Optional.empty(),
					Optional.of(AttributeXMLRepresentation.ATTRIBUTE)));
        } else if (abstractElement instanceof XsdSequence seq) {
            if (isChoiceGroup || isMulti(seq.getMaxOccurs()) || seq.getMinOccurs() == 0) {
            	Data data = xsdMapping.getRosettaTypeFromComplex(seq);
            	createTypeConfiguration(data, seq.getXsdElements(), false, xsdMapping, result);

                Attribute attr = xsdMapping.getAttribute(seq);
                currentConfig.put(attr.getName(), new AttributeXMLConfiguration(
    					Optional.empty(),
    					Optional.empty(),
    					Optional.of(AttributeXMLRepresentation.VIRTUAL)));
            } else {
                seq.getXsdElements().forEach(child -> getAttributeConfigurationRecursively(currentConfig, child, false, xsdMapping, result));
            }
        } else if (abstractElement instanceof XsdAll all) {
            if (isChoiceGroup || all.getMinOccurs() == 0) {
            	Data data = xsdMapping.getRosettaTypeFromComplex(all);
            	createTypeConfiguration(data, all.getXsdElements(), false, xsdMapping, result);

                Attribute attr = xsdMapping.getAttribute(all);
                currentConfig.put(attr.getName(), new AttributeXMLConfiguration(
    					Optional.empty(),
    					Optional.empty(),
    					Optional.of(AttributeXMLRepresentation.VIRTUAL)));
            } else {
                all.getXsdElements().forEach(child -> getAttributeConfigurationRecursively(currentConfig, child, false, xsdMapping, result));
            }
        } else if (abstractElement instanceof XsdChoice choice) {
            if (isChoiceGroup || isMulti(choice.getMaxOccurs())) {
            	Data data = xsdMapping.getRosettaTypeFromComplex(choice);
            	createTypeConfiguration(data, choice.getXsdElements(), true, xsdMapping, result);

                Attribute attr = xsdMapping.getAttribute(choice);
                currentConfig.put(attr.getName(), new AttributeXMLConfiguration(
    					Optional.empty(),
    					Optional.empty(),
    					Optional.of(AttributeXMLRepresentation.VIRTUAL)));
            } else {
                choice.getXsdElements().forEach(child -> getAttributeConfigurationRecursively(currentConfig, child, true, xsdMapping, result));
            }
        }
	}
	private TypeXMLConfiguration createTypeConfiguration(Data data, Stream<XsdAbstractElement> abstractElements, boolean isChoiceGroup, RosettaXsdMapping xsdMapping, Map<Data, TypeXMLConfiguration> result) {
        // Create type config
		Map<String, AttributeXMLConfiguration> currentConfig = new LinkedHashMap<>();
		TypeXMLConfiguration config = new TypeXMLConfiguration(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.of(currentConfig),
				Optional.empty());
        result.put(data, config);

        completeTypeConfiguration(currentConfig, abstractElements, isChoiceGroup, xsdMapping, result);

        return config;
    }
	private void completeTypeConfiguration(Map<String, AttributeXMLConfiguration> currentConfig, Stream<XsdAbstractElement> abstractElements, boolean isChoiceGroup, RosettaXsdMapping xsdMapping, Map<Data, TypeXMLConfiguration> result) {
		abstractElements.forEach(elem -> getAttributeConfigurationRecursively(currentConfig, elem, isChoiceGroup, xsdMapping, result));
    }

	private Attribute createAttribute(String rawName, String docs, int minOccurs, String maxOccurs, ChoiceGroup choiceGroup, ImportTargetConfig config) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// definition
        attribute.setDefinition(docs);

		// name
		attribute.setName(util.toAttributeName(rawName, config));

		// cardinality
		RosettaCardinality rosettaCardinality = RosettaFactory.eINSTANCE.createRosettaCardinality();
		if (choiceGroup != null) {
			rosettaCardinality.setInf(0);
            choiceGroup.attributes.add(attribute);
		} else {
			rosettaCardinality.setInf(minOccurs);
		}
		if (maxOccurs.equals(UNBOUNDED)) {
			rosettaCardinality.setUnbounded(true);
		} else {
			rosettaCardinality.setSup(Integer.parseInt(maxOccurs));
		}
		attribute.setCard(rosettaCardinality);
		
		return attribute;
	}
    private Attribute createAttributeFromElement(XsdElement elem, ChoiceGroup choiceGroup, ImportTargetConfig config) {
        return createAttribute(elem.getRawName(), util.extractDocs(elem).orElse(null), elem.getMinOccurs(), elem.getMaxOccurs(), choiceGroup, config);
    }
    private Attribute createAttributeFromGroup(XsdGroup group, ChoiceGroup choiceGroup, ImportTargetConfig config) {
        return createAttribute(group.getRawName(), util.extractDocs(group).orElse(null), group.getMinOccurs(), group.getMaxOccurs(), choiceGroup, config);
    }
    private Attribute createAttributeFromSequence(XsdSequence seq, String rawName, ChoiceGroup choiceGroup, ImportTargetConfig config) {
        return createAttribute(rawName, null, seq.getMinOccurs(), seq.getMaxOccurs(), choiceGroup, config);
    }
    private Attribute createAttributeFromAll(XsdAll all, String rawName, ChoiceGroup choiceGroup, ImportTargetConfig config) {
        return createAttribute(rawName, null, all.getMinOccurs(), String.valueOf(all.getMaxOccurs()), choiceGroup, config);
    }
    private Attribute createAttributeFromChoice(XsdChoice choice, String rawName, ChoiceGroup choiceGroup, ImportTargetConfig config) {
        return createAttribute(rawName, null, choice.getMinOccurs(), choice.getMaxOccurs(), choiceGroup, config);
    }
	private Attribute createAttributeFromXsdAttribute(XsdAttribute xsdAttribute, ImportTargetConfig config) {
		int minOccurs;
		if (xsdAttribute.getUse().equals(UsageEnum.REQUIRED.getValue())) {
			minOccurs = 1;
		} else if (xsdAttribute.getUse().equals(UsageEnum.OPTIONAL.getValue())) {
			minOccurs = 0;
		} else {
			throw new RuntimeException("Unknown XSD attribute usage: " + xsdAttribute.getUse());
		}
		return createAttribute(xsdAttribute.getRawName(), util.extractDocs(xsdAttribute).orElse(null), minOccurs, "1", null, config);
	}
	public Attribute createValueAttribute(ImportTargetConfig config) {
        return createAttribute(SIMPLE_EXTENSION_ATTRIBUTE_NAME, null, 1, "1", null, config);
	}
    private boolean isMulti(String maxOccurs) {
        return maxOccurs.equals(UNBOUNDED) || Integer.parseInt(maxOccurs) > 1;
    }
}
