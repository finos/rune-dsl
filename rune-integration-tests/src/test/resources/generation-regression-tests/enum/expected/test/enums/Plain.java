package test.enums;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @version 0.0.0
 */
@RosettaEnum("Plain")
public enum Plain {

    @RosettaEnumValue(value = "ONE")
    ONE("ONE", null),

    @RosettaEnumValue(value = "TWO")
    TWO("TWO", null)
    ;

    private static Map<String, Plain> values;
    static {
        Map<String, Plain> map = new ConcurrentHashMap<>();
        for (Plain instance : Plain.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    Plain(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static Plain fromDisplayName(String name) {
        Plain value = values.get(name);
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
