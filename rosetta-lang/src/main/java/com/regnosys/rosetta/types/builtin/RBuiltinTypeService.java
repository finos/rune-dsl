package com.regnosys.rosetta.types.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Singleton;

@Singleton
public class RBuiltinTypeService {
	private Map<String, Function<Map<String, Object>, RBuiltinType>> typeMap = new HashMap<>();

	public final RBasicType BOOLEAN = registerConstantType(new RBasicType("boolean"));
	public final RBasicType TIME = registerConstantType(new RBasicType("time"));
	public final RBasicType MISSING = registerConstantType(new RBasicType("missing"));
	public final RBasicType NOTHING = registerConstantType(new RBasicType("nothing"));
	public final RBasicType UNCONSTRAINED_INT = new RNumberType(Optional.empty(), Optional.of(0), Optional.empty(), Optional.empty(), Optional.empty());
	public final RBasicType UNCONSTRAINED_NUMBER = new RNumberType(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	public final RBasicType UNCONSTRAINED_STRING = new RStringType(Optional.empty(), Optional.empty(), Optional.empty());
	
	public final RRecordType DATE = registerConstantType(new RRecordType("date"));
	public final RRecordType DATE_TIME = registerConstantType(new RRecordType("dateTime"));
	public final RRecordType ZONED_DATE_TIME = registerConstantType(new RRecordType("zonedDateTime"));
	
	public RBuiltinTypeService() {
		register("number", (m) -> RNumberType.from(m));
		register("string", (m) -> RStringType.from(m));
	}
	
	public Optional<RBuiltinType> getType(String name, Map<String, Object> params) {
		return Optional.ofNullable(typeMap.get(name))
				.map(constr -> constr.apply(params));
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
