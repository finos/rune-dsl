package test.escaping;

import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;
import com.rosetta.model.lib.annotations.RosettaSynonym;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An enum stored in C:&#92;users, which *&#47; ends a comment.
 * @version 1.0 "beta" C:&#92;users
 *
 * Body Org1
 * Corpus Agreement Agr1 Agreement in C:&#92;users *&#47; "A corpus in C:&#92;users *&#47;" 
 * name "C:&#92;users *&#47;"
 *
 * Provision A provision in C:&#92;users *&#47;
 *
 */
@RosettaEnum("DocumentedEnum")
public enum DocumentedEnum {

    /**
     * A value stored in C:&#92;users, which *&#47; ends a comment.
     */
    @RosettaSynonym(value = "C:\\users \"quoted\"", source = "FpML")
    @RosettaEnumValue(value = "VALUE")
    VALUE("VALUE", null)
    ;

    private static Map<String, DocumentedEnum> values;
    static {
        Map<String, DocumentedEnum> map = new ConcurrentHashMap<>();
        for (DocumentedEnum instance : DocumentedEnum.values()) {
            map.put(instance.toDisplayString(), instance);
        }
        values = Collections.unmodifiableMap(map);
    }

    private final String rosettaName;
    private final String displayName;

    DocumentedEnum(String rosettaName, String displayName) {
        this.rosettaName = rosettaName;
        this.displayName = displayName;
    }

    public static DocumentedEnum fromDisplayName(String name) {
        DocumentedEnum value = values.get(name);
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
