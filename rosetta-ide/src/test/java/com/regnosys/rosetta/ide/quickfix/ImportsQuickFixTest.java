package com.regnosys.rosetta.ide.quickfix;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.TextEdit;

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import com.regnosys.rosetta.ide.util.RangeUtils;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.head;
import static org.eclipse.xtext.xbase.lib.IterableExtensions.sortWith;

public class ImportsQuickFixTest extends AbstractRosettaLanguageServerTest{
	@Inject
    RangeUtils rangeUtils;

    @Test
    public void testQuickFixMandatoryAttributes() {
        testCodeAction(configurator -> {
            configurator.setModel((
                    "namespace foo.bar\r\n"
                    + "		\r\n"
                    + "		import dsl.foo.*\r\n"
                    + "		import dsl.bar.*\r\n"
                    + "		import dsl.foo.*\r\n"
                    + "		import dsl.aaa.*\r\n"
                    + "		\r\n"
                    + "		func Bar:\r\n"
                    + "			inputs: \r\n"
                    + "				foo Foo (1..1)\r\n"
                    + "				aaa Aaa (1..1)\r\n"
                    + "			output: result int (1..1)\r\n"
                    + "			\r\n"
                    + "			set result: aaa.a"
            ));
            Map<String, CharSequence> filesInScope = new HashMap<>();
            filesInScope.put("foo.rosetta", "namespace dsl.foo\r\n"
            		+ "				\r\n"
            		+ "				type Foo:\r\n"
            		+ "					a int (1..1)");
            filesInScope.put("ach.rosetta", "namespace dsl.aaa\r\n"
            		+ "								\r\n"
            		+ "				type Aaa:\r\n"
            		+ "					a int (1..1)");
            configurator.setFilesInScope(filesInScope);
            configurator.setAssertCodeActions(codeActions -> {
                assertCodeAction(codeActions, "import dsl.aaa.*\nimport dsl.foo.*");
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
