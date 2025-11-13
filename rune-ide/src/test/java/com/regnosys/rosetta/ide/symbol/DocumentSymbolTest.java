package com.regnosys.rosetta.ide.symbol;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.junit.jupiter.api.Test;

public class DocumentSymbolTest extends AbstractRosettaLanguageServerTest {
    @Test
    void testGoToSuper() {
        String model = """
                namespace a
                scope MyScope
                
                func Foo:
                    output:
                        result int (1..1)
                    set result: 42
                
                func Bar extends Foo:
                    output:
                        result int (1..1)
                    set result: super()
                """;
        
        testDefinition(it -> {
            it.setModel(model);
            it.setLine(11);
            it.setColumn(16);
            it.setExpectedDefinitions("""
                    MyModel.rosetta [[3, 5] .. [3, 8]]
                    """);
        });
    }
}