package test.enumeration;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An enumeration with documentation.
 * @version 0.0.0
 *
 * Body Org1
 * Corpus Agreement Agr1 Agreement 1  
 * name "something"
 *
 * Provision some provision
 *
 */
@RosettaEnum("Documented")
public enum Documented {

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
    FOURTH("FOURTH", null)
    ;

    private static Map<String, Documented> values;
    static {
        Map<String, Documented> map = new ConcurrentHashMap<>();
        for (Documented instance : Documented.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    Documented(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static Documented fromDisplayName(String name) {
        Documented value = values.get(name);
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
