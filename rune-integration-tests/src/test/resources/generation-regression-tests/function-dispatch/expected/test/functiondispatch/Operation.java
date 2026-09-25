package test.functiondispatch;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @version 0.0.0
 */
@RosettaEnum("Operation")
public enum Operation {

    @RosettaEnumValue(value = "INCREMENT")
    INCREMENT("INCREMENT", null),

    @RosettaEnumValue(value = "DECREMENT")
    DECREMENT("DECREMENT", null)
    ;

    private static Map<String, Operation> values;
    static {
        Map<String, Operation> map = new ConcurrentHashMap<>();
        for (Operation instance : Operation.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    Operation(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static Operation fromDisplayName(String name) {
        Operation value = values.get(name);
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
