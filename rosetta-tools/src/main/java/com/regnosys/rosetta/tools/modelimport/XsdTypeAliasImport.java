package com.regnosys.rosetta.tools.modelimport;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;
import org.xmlet.xsdparser.xsdelements.XsdRestriction;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.ParametrizedRosettaType;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.TypeCallArgument;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;

public class XsdTypeAliasImport extends AbstractXsdImport<XsdSimpleType, RosettaTypeAlias> {	
	
	private final XsdUtil util;

	@Inject
	public XsdTypeAliasImport(XsdUtil util) {
		super(XsdSimpleType.class);
		this.util = util;
	}

	@Override
	public List<XsdSimpleType> filterTypes(List<XsdAbstractElement> elements) {
		return super.filterTypes(elements).stream()
			.filter(x -> !util.isEnumType(x))
			.collect(Collectors.toList());
	}

	@Override
	public RosettaTypeAlias registerType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, Map<XsdNamedElements, String> rootTypeNames, GenerationProperties properties) {
		RosettaTypeAlias typeAlias = RosettaFactory.eINSTANCE.createRosettaTypeAlias();
		typeAlias.setName(xsdType.getName());
		util.extractDocs(xsdType).ifPresent(typeAlias::setDefinition);
		typeMappings.registerSimpleType(xsdType, typeAlias);
		return typeAlias;
	}

	private TypeParameter findParameter(String name, ParametrizedRosettaType type) {
		return type.getParameters().stream()
				.filter(p -> p.getName().equals(name))
				.findFirst()
				.orElseThrow();
	}
	private RosettaIntLiteral createIntLiteral(BigInteger value) {
		RosettaIntLiteral lit = ExpressionFactory.eINSTANCE.createRosettaIntLiteral();
		lit.setValue(value);
		return lit;
	}
	private RosettaNumberLiteral createNumberLiteral(BigDecimal value) {
		RosettaNumberLiteral lit = ExpressionFactory.eINSTANCE.createRosettaNumberLiteral();
		lit.setValue(value);
		return lit;
	}
	private RosettaStringLiteral createStringLiteral(String value) {
		RosettaStringLiteral lit = ExpressionFactory.eINSTANCE.createRosettaStringLiteral();
		lit.setValue(value);
		return lit;
	}
	private TypeCallArgument createTypeArgument(ParametrizedRosettaType baseType, String parameterName, BigInteger value) {
		TypeCallArgument arg = RosettaFactory.eINSTANCE.createTypeCallArgument();
		arg.setParameter(findParameter(parameterName, baseType));
		arg.setValue(createIntLiteral(value));
		return arg;
	}
	private TypeCallArgument createTypeArgument(ParametrizedRosettaType baseType, String parameterName, BigDecimal value) {
		TypeCallArgument arg = RosettaFactory.eINSTANCE.createTypeCallArgument();
		arg.setParameter(findParameter(parameterName, baseType));
		arg.setValue(createNumberLiteral(value));
		return arg;
	}
	private TypeCallArgument createTypeArgument(ParametrizedRosettaType baseType, String parameterName, String value) {
		TypeCallArgument arg = RosettaFactory.eINSTANCE.createTypeCallArgument();
		arg.setParameter(findParameter(parameterName, baseType));
		arg.setValue(createStringLiteral(value));
		return arg;
	}
	@Override
	public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, Map<XsdNamedElements, String> rootTypeNames) {
		RosettaTypeAlias typeAlias = typeMappings.getRosettaTypeFromSimple(xsdType);
		
		TypeCall tc = RosettaFactory.eINSTANCE.createTypeCall();
		typeAlias.setTypeCall(tc);
		
		XsdRestriction restr = xsdType.getRestriction();
		RosettaType baseType = typeMappings.getRosettaTypeFromBuiltin(restr.getBaseAsBuiltInDataType().getName());
		if (baseType instanceof ParametrizedRosettaType) {
			ParametrizedRosettaType paramBaseType = (ParametrizedRosettaType)baseType;
			// If fractionDigits is 0, use int instead.
			if (restr.getFractionDigits() != null && restr.getFractionDigits().getValue() == 0) {
				RosettaTypeAlias intType = (RosettaTypeAlias)typeMappings.getRosettaTypeFromBuiltin("integer");
				baseType = intType;
				paramBaseType = intType;
			}
			// add type arguments
			if (restr.getTotalDigits() != null) {
				BigInteger digits = BigInteger.valueOf(restr.getTotalDigits().getValue());
				TypeCallArgument arg = createTypeArgument(paramBaseType, RNumberType.DIGITS_PARAM_NAME, digits);
				tc.getArguments().add(arg);
			}
			if (restr.getFractionDigits() != null) {
				BigInteger fractionalDigits = BigInteger.valueOf(restr.getFractionDigits().getValue());
				if (!fractionalDigits.equals(BigInteger.ZERO)) {
					TypeCallArgument arg = createTypeArgument(paramBaseType, RNumberType.FRACTIONAL_DIGITS_PARAM_NAME, fractionalDigits);
					tc.getArguments().add(arg);
				}
			}
			if (restr.getMinInclusive() != null) {
				BigDecimal min = new BigDecimal(restr.getMinInclusive().getValue());
				TypeCallArgument arg = createTypeArgument(paramBaseType, RNumberType.MIN_PARAM_NAME, min);
				tc.getArguments().add(arg);
			}
			if (restr.getMaxInclusive() != null) {
				BigDecimal max = new BigDecimal(restr.getMaxInclusive().getValue());
				TypeCallArgument arg = createTypeArgument(paramBaseType, RNumberType.MAX_PARAM_NAME, max);
				tc.getArguments().add(arg);
			}
			
			if (restr.getLength() != null) {
				BigInteger length = BigInteger.valueOf(restr.getLength().getValue());
				TypeCallArgument argMin = createTypeArgument(paramBaseType, RStringType.MIN_LENGTH_PARAM_NAME, length);
				TypeCallArgument argMax = createTypeArgument(paramBaseType, RStringType.MAX_LENGTH_PARAM_NAME, length);
				tc.getArguments().add(argMin);
				tc.getArguments().add(argMax);
			}
			if (restr.getMinLength() != null) {
				BigInteger minLength = BigInteger.valueOf(restr.getMinLength().getValue());
				TypeCallArgument arg = createTypeArgument(paramBaseType, RStringType.MIN_LENGTH_PARAM_NAME, minLength);
				tc.getArguments().add(arg);
			}
			if (restr.getMaxLength() != null) {
				BigInteger maxLength = BigInteger.valueOf(restr.getMaxLength().getValue());
				TypeCallArgument arg = createTypeArgument(paramBaseType, RStringType.MAX_LENGTH_PARAM_NAME, maxLength);
				tc.getArguments().add(arg);
			}
			if (restr.getPattern() != null) {
				String pattern = restr.getPattern().getValue();
				TypeCallArgument arg = createTypeArgument(paramBaseType, RStringType.PATTERN_PARAM_NAME, pattern);
				tc.getArguments().add(arg);
			}
		}
		tc.setType(baseType);
	}
}
