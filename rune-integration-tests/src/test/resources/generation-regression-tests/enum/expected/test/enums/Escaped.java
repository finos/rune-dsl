package test.enums;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An enumeration whose display name needs escaping in Java.
 * @version 0.0.0
 */
@RosettaEnum("Escaped")
public enum Escaped {

    @RosettaEnumValue(value = "ESCAPED", displayName = "C:\\dir \"quoted\" \\u0041")
    ESCAPED("ESCAPED", "C:\\dir \"quoted\" \\u0041")
    ;

    private static Map<String, Escaped> values;
    static {
        Map<String, Escaped> map = new ConcurrentHashMap<>();
        for (Escaped instance : Escaped.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    Escaped(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static Escaped fromDisplayName(String name) {
        Escaped value = values.get(name);
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
