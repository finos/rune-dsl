package com.regnosys.rosetta.types.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaType;

@Singleton
public class RBuiltinTypeService {
	@Inject
	private RosettaBuiltinsService builtinsService;
	
	private Map<String, Function<Map<String, Object>, RBuiltinType>> typeMap = new HashMap<>();

	public final RBasicType BOOLEAN = registerConstantType(new RBasicType("boolean"));
	public final RBasicType TIME = registerConstantType(new RBasicType("time"));
	// TODO: remove the MISSING type
	public final RBasicType MISSING = registerConstantType(new RBasicType("missing"));
	public final RBasicType NOTHING = registerConstantType(new RBasicType("nothing"));
	public final RBasicType ANY = registerConstantType(new RBasicType("any"));
	public final RNumberType UNCONSTRAINED_INT = new RNumberType(Optional.empty(), Optional.of(0), Optional.empty(), Optional.empty(), Optional.empty());
	public final RNumberType UNCONSTRAINED_NUMBER = new RNumberType(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	public final RStringType UNCONSTRAINED_STRING = new RStringType(Optional.empty(), Optional.empty(), Optional.empty());
	
	public final RDateType DATE = registerConstantType(new RDateType());
	public final RDateTimeType DATE_TIME = registerConstantType(new RDateTimeType());
	public final RZonedDateTimeType ZONED_DATE_TIME = registerConstantType(new RZonedDateTimeType());
	
	public RBuiltinTypeService() {
		register("int", (m) -> {
			Map<String, Object> newParams = new HashMap<>(m);
			newParams.put("fractionalDigits", 0);
			return RNumberType.from(newParams);
		});
		register("number", (m) -> RNumberType.from(m));
		register("string", (m) -> RStringType.from(m));
	}
	
	public Optional<RBuiltinType> getType(String name, Map<String, Object> params) {
		return Optional.ofNullable(typeMap.get(name))
				.map(constr -> constr.apply(params));
	}
	
	public <T extends RosettaType> T toRosettaType(RBuiltinType type, Class<T> resultType, ResourceSet resourceSet) {
		RosettaModel basicTypes = builtinsService.getBasicTypesModel(resourceSet);
		return basicTypes.getElements().stream()
				.filter(resultType::isInstance)
				.map(resultType::cast)
				.filter(t -> t.getName().equals(type.getName()))
				.findAny()
				.orElseThrow();
	}

	private <T extends RBuiltinType> T registerConstantType(T t) {
		register(t.getName(), (m) -> t);
		return t;
	}
	private void register(String name, Function<Map<String, Object>, RBuiltinType> constructor) {
		if (typeMap.put(name, constructor) != null) {
			throw new IllegalStateException("There already exists a builtin type named `" + name + "`.");
		}
	}
}
