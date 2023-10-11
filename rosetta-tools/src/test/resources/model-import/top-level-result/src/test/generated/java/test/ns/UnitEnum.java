package test.ns;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @version ${project.version}
 */
@RosettaEnum("UnitEnum")
public enum UnitEnum {

	@RosettaEnumValue(value = "Meter") METER("Meter"),
	
	@RosettaEnumValue(value = "Kilogram") KILOGRAM("Kilogram")
;
	private static Map<String, UnitEnum> values;
	static {
        Map<String, UnitEnum> map = new ConcurrentHashMap<>();
		for (UnitEnum instance : UnitEnum.values()) {
			map.put(instance.toDisplayString(), instance);
		}
		values = Collections.unmodifiableMap(map);
    }

	private final String rosettaName;
	private final String displayName;

	UnitEnum(String rosettaName) {
		this(rosettaName, null);
	}

	UnitEnum(String rosettaName, String displayName) {
		this.rosettaName = rosettaName;
		this.displayName = displayName;
	}

	public static UnitEnum fromDisplayName(String name) {
		UnitEnum value = values.get(name);
		if (value == null) {
			throw new IllegalArgumentException("No enum constant with display name \"" + name + "\".");
		}
		return value;
	}

	@Override
	public String toString() {
		return toDisplayString();
	}

	public String toDisplayString() {
		return displayName != null ?  displayName : rosettaName;
	}
}
