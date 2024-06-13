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

import com.google.common.collect.Streams;
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
	
	@Inject
	public XsdTypeImport(XsdUtil util) {
		super(XsdNamedElements.class);
		this.util = util;
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
	public List<Data> registerType(XsdNamedElements xsdType, RosettaXsdMapping xsdMapping, GenerationProperties properties) {
        List<Data> result = new ArrayList<>();

		Optional<Data> elementType =
			xsdMapping.getElementsWithComplexType(xsdType)
				.map(xsdMapping::getRosettaTypeFromElement)
				.filter(t -> t.getName().equals(xsdType.getName()))
				.findAny();

		Data data = elementType.orElseGet(() -> {
			Data newData = SimpleFactory.eINSTANCE.createData();
			newData.setName(util.toTypeName(xsdType.getRawName()));
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
        	completeData(data, Stream.of(group.getChildElement()), null, xsdMapping, result);
        } else {
        	XsdComplexType ct = (XsdComplexType)xsdType;
            xsdMapping.registerComplexType(ct, data);
        	
        	// If the complex type extends a simple type, simulate this
            // by adding a `value` attribute of the corresponding type.
            if (getBaseSimpleType(ct).isPresent()) {
                Attribute valueAttr = createValueAttribute();
                data.getAttributes().add(valueAttr);
                xsdMapping.registerAttribute(ct, valueAttr);
            }
            
	    	completeData(data, Streams.concat(getChildElement(ct).stream(), getAttributes(ct)), null, xsdMapping, result);
        }

        // Post process: make sure all names are unique:
        util.makeNamesUnique(result);
        result.forEach(d -> {
            util.makeNamesUnique(d.getAttributes());
            util.makeNamesUnique(d.getConditions());
        });

		elementType.ifPresent(d -> util.makeNamesUnique(d.getAttributes()));
		
		return result;
	}

	private void registerXsdElementsRecursively(Data currentData, XsdAbstractElement abstractElement, ChoiceGroup currentChoiceGroup, List<ChoiceGroup> currentChoiceGroups, RosettaXsdMapping xsdMapping, List<Data> result) {
        if (abstractElement instanceof XsdElement elem) {
            Attribute attr = createAttributeFromElement(elem, currentChoiceGroup);
            xsdMapping.registerAttribute(elem, attr);
            currentData.getAttributes().add(attr);
        } else if (abstractElement instanceof XsdGroup group) {
            Attribute attr = createAttributeFromGroup(group, currentChoiceGroup);
            xsdMapping.registerAttribute(group, attr);
            currentData.getAttributes().add(attr);
        } else if (abstractElement instanceof XsdAttribute xsdAttr) {
        	Attribute attr = createAttributeFromXsdAttribute(xsdAttr);
            xsdMapping.registerAttribute(xsdAttr, attr);
            currentData.getAttributes().add(attr);
        }

		else if (abstractElement instanceof XsdSequence seq) {

            if (currentChoiceGroup != null || isMulti(seq.getMaxOccurs()) || seq.getMinOccurs() == 0) {
                Data newData = createData(currentData.getName() + "Sequence", seq.getXsdElements(), null, xsdMapping, result);
                xsdMapping.registerComplexType(seq, newData);

                Attribute attr = createAttributeFromSequence(seq, newData.getName(), currentChoiceGroup);
                xsdMapping.registerAttribute(seq, attr);
                currentData.getAttributes().add(attr);
            } else {
                seq.getXsdElements().forEach(child -> registerXsdElementsRecursively(currentData, child, null, currentChoiceGroups, xsdMapping, result));
            }
        }
		else if (abstractElement instanceof XsdAll all) {
            if (currentChoiceGroup != null || all.getMinOccurs() == 0) {
                Data newData = createData(currentData.getName() + "All", all.getXsdElements(), null, xsdMapping, result);
                xsdMapping.registerComplexType(all, newData);

                Attribute attr = createAttributeFromAll(all, newData.getName(), currentChoiceGroup);
                xsdMapping.registerAttribute(all, attr);
                currentData.getAttributes().add(attr);
            } else {
                all.getXsdElements().forEach(child -> registerXsdElementsRecursively(currentData, child, null, currentChoiceGroups, xsdMapping, result));
            }
        } else if (abstractElement instanceof XsdChoice choice) {
            if (currentChoiceGroup != null || isMulti(choice.getMaxOccurs())) {
                boolean required = choice.getMinOccurs() > 0 && choice.getXsdElements().allMatch(elem -> Integer.parseInt(elem.getAttributesMap().getOrDefault(MIN_OCCURS_TAG, "1")) > 0);
                ChoiceGroup initialChoiceGroup = new ChoiceGroup(new ArrayList<>(), required);
                Data newData = createData(currentData.getName() + "Choice", choice.getXsdElements(), initialChoiceGroup, xsdMapping, result);
                xsdMapping.registerComplexType(choice, newData);

                Attribute attr = createAttributeFromChoice(choice, newData.getName(), currentChoiceGroup);
                xsdMapping.registerAttribute(choice, attr);
                currentData.getAttributes().add(attr);
            } else {
                ChoiceGroup newChoiceGroup = new ChoiceGroup(new ArrayList<>(), choice.getMinOccurs() > 0);
                currentChoiceGroups.add(newChoiceGroup);
                choice.getXsdElements().forEach(child -> registerXsdElementsRecursively(currentData, child, newChoiceGroup, currentChoiceGroups, xsdMapping, result));
            }
        }
    }
    private Data createData(String name, Stream<XsdAbstractElement> abstractElements, ChoiceGroup initialChoiceGroup, RosettaXsdMapping xsdMapping, List<Data> result) {
        // Create type
        Data data = SimpleFactory.eINSTANCE.createData();
        data.setName(name);
        result.add(data);

        completeData(data, abstractElements, initialChoiceGroup, xsdMapping, result);

        return data;
    }
    private void completeData(Data data, Stream<XsdAbstractElement> abstractElements, ChoiceGroup initialChoiceGroup, RosettaXsdMapping xsdMapping, List<Data> result) {
        // Add attributes
        List<ChoiceGroup> choiceGroups = new ArrayList<>();
        if (initialChoiceGroup != null) {
            choiceGroups.add(initialChoiceGroup);
        }
        abstractElements.forEach(elem -> registerXsdElementsRecursively(data, elem, initialChoiceGroup, choiceGroups, xsdMapping, result));

        // Add conditions
        choiceGroups.forEach(choiceGroup -> {
			if (choiceGroup.attributes.size() > 1) {
				Condition choice = SimpleFactory.eINSTANCE.createCondition();
				choice.setName("Choice");
				if (choiceGroup.attributes.size() == data.getAttributes().size() && choiceGroup.required) {
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
			}
        });
    }

	@Override
	public void completeType(XsdNamedElements xsdType, RosettaXsdMapping xsdMapping) {
		if (xsdType instanceof XsdGroup group) {
			completeXsdElementsRecursively(group.getChildElement(), false, xsdMapping);
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
			
			Stream.concat(getChildElement(ct).stream(), getAttributes(ct))
				.forEach(content -> completeXsdElementsRecursively(content, false, xsdMapping));
		}
	}
	private void completeXsdElementsRecursively(XsdAbstractElement abstractElement, boolean isChoiceGroup, RosettaXsdMapping xsdMapping) {
        if (abstractElement instanceof XsdElement elem) {
        	Attribute attr = xsdMapping.getAttribute(elem);
			if (elem.getTypeAsXsd() == null) {
				attr.setTypeCall(xsdMapping.getRosettaTypeCallFromBuiltin("string"));
				return; // TODO
			}
			attr.setTypeCall(xsdMapping.getRosettaTypeCall(elem.getTypeAsXsd()));
        } else if (abstractElement instanceof XsdGroup group) {
        	Attribute attr = xsdMapping.getAttribute(group);
			attr.setTypeCall(xsdMapping.getRosettaTypeCall(group));
        } else if (abstractElement instanceof XsdAttribute xsdAttr) {
        	Attribute attr = xsdMapping.getAttribute(xsdAttr);
			if (xsdAttr.getXsdSimpleType() == null) {
				attr.setTypeCall(xsdMapping.getRosettaTypeCallFromBuiltin("string"));
				return; // TODO
			}
			attr.setTypeCall(xsdMapping.getRosettaTypeCall(xsdAttr.getXsdSimpleType()));
        } else if (abstractElement instanceof XsdSequence seq) {
            if (isChoiceGroup || isMulti(seq.getMaxOccurs()) || seq.getMinOccurs() == 0) {
            	seq.getXsdElements().forEach(child -> completeXsdElementsRecursively(child, false, xsdMapping));
            	Attribute attr = xsdMapping.getAttribute(seq);
    			attr.setTypeCall(xsdMapping.getRosettaTypeCall(seq));
            } else {
            	seq.getXsdElements().forEach(child -> completeXsdElementsRecursively(child, isChoiceGroup, xsdMapping));
            }
        } else if (abstractElement instanceof XsdAll all) {
            if (isChoiceGroup || all.getMinOccurs() == 0) {
            	all.getXsdElements().forEach(child -> completeXsdElementsRecursively(child, false, xsdMapping));
            	Attribute attr = xsdMapping.getAttribute(all);
    			attr.setTypeCall(xsdMapping.getRosettaTypeCall(all));
            } else {
            	all.getXsdElements().forEach(child -> completeXsdElementsRecursively(child, isChoiceGroup, xsdMapping));
            }
        } else if (abstractElement instanceof XsdChoice choice) {
            if (isChoiceGroup || isMulti(choice.getMaxOccurs())) {
            	choice.getXsdElements().forEach(child -> completeXsdElementsRecursively(child, true, xsdMapping));
            	Attribute attr = xsdMapping.getAttribute(choice);
    			attr.setTypeCall(xsdMapping.getRosettaTypeCall(choice));
            } else {
            	choice.getXsdElements().forEach(child -> completeXsdElementsRecursively(child, true, xsdMapping));
            }
        }
    }
	
	public Optional<TypeXMLConfiguration> getXMLConfiguration(XsdComplexType xsdType, RosettaXsdMapping xsdMapping, String schemaTargetNamespace) {
		Data data = xsdMapping.getRosettaTypeFromComplex(xsdType);
		if (xsdMapping.getElementsWithComplexType(xsdType)
				.map(xsdMapping::getRosettaTypeFromElement)
				.anyMatch(t -> t.equals(data))) {
			// This type is merged into an element.
			return Optional.empty();
		}
		
		Map<String, AttributeXMLConfiguration> attributeConfig = getAttributeConfiguration(xsdType, xsdMapping);
		if (attributeConfig.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(
					new TypeXMLConfiguration(
						Optional.empty(),
						Optional.empty(),
						Optional.of(attributeConfig)
					));
		}
	}
	public Map<String, AttributeXMLConfiguration> getAttributeConfiguration(XsdComplexType xsdType, RosettaXsdMapping xsdMapping) {
		Map<String, AttributeXMLConfiguration> result = new LinkedHashMap<>();
		
		Optional<XsdNamedElements> baseSimpleType = getBaseSimpleType(xsdType);
		if (baseSimpleType.isPresent()) {
			Attribute attr = xsdMapping.getAttribute(xsdType);
			result.put(attr.getName(), new AttributeXMLConfiguration(
					Optional.empty(),
					Optional.empty(),
					Optional.of(AttributeXMLRepresentation.VALUE)));
		}
		//TODO
//		getTypedXsdElements(xsdType)
//			.forEach(element -> {
//				Attribute attr = xsdMapping.getAttribute(element);
//				if (!element.getName().equals(attr.getName())) {
//					result.put(attr.getName(), new AttributeXMLConfiguration(
//							Optional.of(element.getName()),
//							Optional.empty(),
//							Optional.empty()));
//				}
//			});
//		
//		getTypedXsdAttributes(xsdType)
//			.forEach(element -> {
//				Attribute attr = xsdMapping.getAttribute(element);
//				result.put(attr.getName(), new AttributeXMLConfiguration(
//						element.getName().equals(attr.getName()) ? Optional.empty() : Optional.of(element.getName()),
//						Optional.empty(),
//						Optional.of(AttributeXMLRepresentation.ATTRIBUTE)));
//			});

		return result;
	}

	private Attribute createAttribute(String rawName, String docs, int minOccurs, String maxOccurs, ChoiceGroup choiceGroup) {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();

		// definition
        attribute.setDefinition(docs);

		// name
		attribute.setName(util.toAttributeName(rawName));

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
    private Attribute createAttributeFromElement(XsdElement elem, ChoiceGroup choiceGroup) {
        return createAttribute(elem.getRawName(), util.extractDocs(elem).orElse(null), elem.getMinOccurs(), elem.getMaxOccurs(), choiceGroup);
    }
    private Attribute createAttributeFromGroup(XsdGroup group, ChoiceGroup choiceGroup) {
        return createAttribute(group.getRawName(), util.extractDocs(group).orElse(null), group.getMinOccurs(), group.getMaxOccurs(), choiceGroup);
    }
    private Attribute createAttributeFromSequence(XsdSequence seq, String rawName, ChoiceGroup choiceGroup) {
        return createAttribute(rawName, null, seq.getMinOccurs(), seq.getMaxOccurs(), choiceGroup);
    }
    private Attribute createAttributeFromAll(XsdAll all, String rawName, ChoiceGroup choiceGroup) {
        return createAttribute(rawName, null, all.getMinOccurs(), String.valueOf(all.getMaxOccurs()), choiceGroup);
    }
    private Attribute createAttributeFromChoice(XsdChoice choice, String rawName, ChoiceGroup choiceGroup) {
        return createAttribute(rawName, null, choice.getMinOccurs(), choice.getMaxOccurs(), choiceGroup);
    }
	private Attribute createAttributeFromXsdAttribute(XsdAttribute xsdAttribute) {
		int minOccurs;
		if (xsdAttribute.getUse().equals(UsageEnum.REQUIRED.getValue())) {
			minOccurs = 1;
		} else if (xsdAttribute.getUse().equals(UsageEnum.OPTIONAL.getValue())) {
			minOccurs = 0;
		} else {
			throw new RuntimeException("Unknown XSD attribute usage: " + xsdAttribute.getUse());
		}
		return createAttribute(xsdAttribute.getRawName(), util.extractDocs(xsdAttribute).orElse(null), minOccurs, "1", null);
	}
	public Attribute createValueAttribute() {
        return createAttribute(SIMPLE_EXTENSION_ATTRIBUTE_NAME, null, 1, "1", null);
	}
    private boolean isMulti(String maxOccurs) {
        return maxOccurs.equals(UNBOUNDED) || Integer.parseInt(maxOccurs) > 1;
    }
}
