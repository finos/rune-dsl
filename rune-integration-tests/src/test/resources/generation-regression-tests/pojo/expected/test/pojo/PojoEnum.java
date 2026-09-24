package test.pojo;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @version 0.0.0
 */
@RosettaEnum("PojoEnum")
public enum PojoEnum {

    @RosettaEnumValue(value = "A")
    A("A", null),

    @RosettaEnumValue(value = "B")
    B("B", null)
    ;

    private static Map<String, PojoEnum> values;
    static {
        Map<String, PojoEnum> map = new ConcurrentHashMap<>();
        for (PojoEnum instance : PojoEnum.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    PojoEnum(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static PojoEnum fromDisplayName(String name) {
        PojoEnum value = values.get(name);
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
        return displayName != null ? displayName : rosettaName;
    }
}
