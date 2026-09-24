package test.enums;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An enumeration that extends another one.
 * @version 0.0.0
 */
@RosettaEnum("Extended")
public enum Extended {

    /**
     * The first value.
     */
    @RosettaEnumValue(value = "FIRST")
    FIRST("FIRST", null),

    /**
     * The second value, with a display name.
     *
     * Body Org1
     * Corpus Agreement Agr1 Agreement 1  
     * name "something else"
     *
     * Provision another provision
     *
     */
    @RosettaEnumValue(value = "SECOND", displayName = "second value")
    SECOND("SECOND", "second value"),

    @RosettaEnumValue(value = "THIRD", displayName = "third")
    THIRD("THIRD", "third"),

    @RosettaEnumValue(value = "FOURTH")
    FOURTH("FOURTH", null),

    /**
     * An extra value.
     */
    @RosettaEnumValue(value = "FIFTH")
    FIFTH("FIFTH", null)
    ;

    private static Map<String, Extended> values;
    static {
        Map<String, Extended> map = new ConcurrentHashMap<>();
        for (Extended instance : Extended.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    Extended(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static Extended fromDisplayName(String name) {
        Extended value = values.get(name);
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
