package com.regnosys.rosetta.ide.quickfix;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import com.regnosys.rosetta.ide.util.RangeUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.head;
import static org.eclipse.xtext.xbase.lib.IterableExtensions.sortWith;

public class ConstructorQuickFixTest extends AbstractRosettaLanguageServerTest {

    @Inject
    RangeUtils rangeUtils;

    @Test
    public void testQuickFixMandatoryAttributes() {
        testCodeAction(configurator -> {
            configurator.setModel((
                    "namespace testQuickFixDeprecatedMap;" +
                            "type T:;" +
                            "    a string (1..1);" +
                            "func F:;" +
                            "    output: t T (1..1);" +
                            "    set t : T {};"
            ).replace(";", "\n"));
            configurator.setAssertCodeActions(codeActions -> {
                assertCodeAction(codeActions, "T { a: empty }");
            });
        });
    }
    
    @Test
    public void testQuickFixMandatoryAndOptionalAttributes() {
        testCodeAction(configurator -> {
            configurator.setModel((
                    "namespace testQuickFixDeprecatedMap;" +
                            "type T:;" +
                            "    a string (1..1);" +
                            "    b string (0..1);" +
                            "func F:;" +
                            "    output: t T (1..1);" +
                            "    set t : T {};"
            ).replace(";", "\n"));
            configurator.setAssertCodeActions(codeActions -> {
                assertCodeAction(codeActions, "T { a: empty, ... }");
            });
        });
    }

    @Test
    public void testQuickFixMandatoryChoiceAttributes() {
        testCodeAction(configurator -> {
            configurator.setModel((
                    "namespace testQuickFixDeprecatedMap;" +
                            "type A:;" +
                            "    n string (0..1);" +
                            "type B:;" +
                            "    m string (0..1);" +
                            "choice C:;" +
                            "    A;" +
                            "    B;" +
                            "func F:;" +
                            "    output: c C (1..1);" +
                            "    set c : C {};"
            ).replace(";", "\n"));
            configurator.setAssertCodeActions(codeActions -> {
                assertCodeAction(codeActions, "C { A: empty, B: empty }");
            });
        });
    }

    private void assertCodeAction(List<Either<Command, CodeAction>> codeActions, String expectedFix) {
        CodeAction codeAction = getFirstCodeAction(codeActions);
        assertEquals("Auto add mandatory attributes.", codeAction.getTitle());
        TextEdit textEdit = getFirstTextEdit(codeAction);
        String actual = textEdit.getNewText().replaceAll("\\s", "");
        String expected = expectedFix.replaceAll("\\s", "");
        assertEquals(expected, actual);
    }

    private static TextEdit getFirstTextEdit(CodeAction codeAction) {
        return codeAction.getEdit().getChanges().values()
                .stream().flatMap(Collection::stream)
                .findFirst().orElseThrow();
    }

    private CodeAction getFirstCodeAction(List<Either<Command, CodeAction>> codeActions) {
        List<Either<Command, CodeAction>> sorted =
                sortWith(codeActions, (a1, b1) ->
                        this.rangeUtils.comparePositions(head(a1.getRight().getDiagnostics()).getRange().getStart(),
                                head(b1.getRight().getDiagnostics()).getRange().getStart()));

        return sorted.get(0).getRight();
    }
}
