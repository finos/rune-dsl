package com.regnosys.rosetta.ide.quickfix;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import com.regnosys.rosetta.ide.util.RangeUtils;
import jakarta.inject.Inject;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

public class QuickFixTest extends AbstractRosettaLanguageServerTest {
	@Inject
	private RangeUtils ru;
    
    @Test
    public void testResolveChangedExtendedFunctionParameters() {
        String model = """
                namespace test
                scope MyScope
                version "1"
                
                func Bar extends Foo:
                    inputs:
                        ab int (1..1)
                        b U (1..1)
                        c string (0..1)
                        d int (0..1)
                	output:
                		result int (1..1)
                	set result: 42
                """;
        String additionalModel = """
                namespace test
                version "1"
           
                type T:
                type U extends T:
           
                func Foo:
                    inputs:
                        a int (0..1)
                        b T (1..1)
                        c string (0..1)
                            [metadata scheme]
                    output:
                        result number (1..1)
                    set result: 0
                """;
        testResultCodeAction(cfg -> {
            cfg.setModel(model);
            cfg.setFilesInScope(Map.of("additional.rosetta", additionalModel));
            cfg.setExpectedCodeActions("""
                    title : Copy original inputs and output
                    kind : quickfix
                    command :\s
                    codes : RosettaIssueCodes.changedExtendedFunctionParameters
                    edit : changes :
                        MyModel.rosetta :\s
                                a int (0..1)
                                b T (1..1)
                                c string (0..1)
                                    [metadata scheme] [[5, 11] .. [9, 20]]
                       \s
                                result number (1..1) [[10, 8] .. [11, 19]]
                    documentChanges :\s
                    title : Copy original inputs and output
                    kind : quickfix
                    command :\s
                    codes : RosettaIssueCodes.changedExtendedFunctionParameters
                    edit : changes :
                        MyModel.rosetta :\s
                                a int (0..1)
                                b T (1..1)
                                c string (0..1)
                                    [metadata scheme] [[5, 11] .. [9, 20]]
                       \s
                                result number (1..1) [[10, 8] .. [11, 19]]
                    documentChanges :\s
                    title : Copy original inputs and output
                    kind : quickfix
                    command :\s
                    codes : RosettaIssueCodes.changedExtendedFunctionParameters
                    edit : changes :
                        MyModel.rosetta :\s
                                a int (0..1)
                                b T (1..1)
                                c string (0..1)
                                    [metadata scheme] [[5, 11] .. [9, 20]]
                       \s
                                result number (1..1) [[10, 8] .. [11, 19]]
                    documentChanges :\s
                    title : Copy original inputs and output
                    kind : quickfix
                    command :\s
                    codes : RosettaIssueCodes.changedExtendedFunctionParameters
                    edit : changes :
                        MyModel.rosetta :\s
                                a int (0..1)
                                b T (1..1)
                                c string (0..1)
                                    [metadata scheme] [[5, 11] .. [9, 20]]
                       \s
                                result number (1..1) [[10, 8] .. [11, 19]]
                    documentChanges :\s
                    title : Copy original inputs and output
                    kind : quickfix
                    command :\s
                    codes : RosettaIssueCodes.changedExtendedFunctionParameters
                    edit : changes :
                        MyModel.rosetta :\s
                                a int (0..1)
                                b T (1..1)
                                c string (0..1)
                                    [metadata scheme] [[5, 11] .. [9, 20]]
                       \s
                                result number (1..1) [[10, 8] .. [11, 19]]
                    documentChanges :\s
                    """);
        });
    }

	@Test
	public void testQuickFixRedundantSquareBrackets() {
		String model = """
				namespace foo.bar

				type Foo:
					a int (1..1)

				func Bar:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result:
						foo
							extract [ a ]
							extract [ 42 ]
				""";
		testCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setAssertCodeActions(codeActions -> {
				Assertions.assertEquals(2, codeActions.size());

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getRight().getDiagnostics().getFirst().getRange().getStart(),
								b.getRight().getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst().getRight();
				assertEquals("Add `then`", action0.getTitle());
				assertNull(action0.getEdit()); // make sure no edits are made at this point

				var action1 = sorted.get(1).getRight();
				assertEquals("Remove square brackets", action1.getTitle());
				assertNull(action1.getEdit()); // make sure no edits are made at this point
			});
		});
	}

	@Test
	public void testResolveRedundantSquareBrackets() {
		String model = """
				namespace foo.bar

				type Foo:
					a int (1..1)

				func Bar:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result:
						foo
							extract [ a ]
							extract [ 42 ]
				""";
		testResultCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setAssertCodeActionResolution(codeActions -> {
				Assertions.assertEquals(2, codeActions.size());

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getDiagnostics().getFirst().getRange().getStart(),
								b.getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst();
				assertEquals("Add `then`", action0.getTitle());
				var edit0 = action0.getEdit().getChanges().values().iterator().next().getFirst();
				assertEquals("then extract", edit0.getNewText());
				Assertions.assertEquals(new Position(12, 3), edit0.getRange().getStart());
				Assertions.assertEquals(new Position(12, 10), edit0.getRange().getEnd());

				var action1 = sorted.get(1);
				assertEquals("Remove square brackets", action1.getTitle());
				var edit1 = action1.getEdit().getChanges().values().iterator().next().getFirst();
				assertEquals("42", edit1.getNewText());
				Assertions.assertEquals(new Position(12, 11), edit1.getRange().getStart());
				Assertions.assertEquals(new Position(12, 17), edit1.getRange().getEnd());
			});
		});
	}

	@Test
	public void testQuickFixDuplicateImport() {
		String model = """
				namespace foo.bar

				import dsl.foo.*
				import dsl.foo.*

				func Bar:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result: foo -> a
				""";
		testCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setFilesInScope(Map.of("foo.rosetta", """
					namespace dsl.foo

					type Foo:
						a int (1..1)
					"""));
			cfg.setAssertCodeActions(codeActions -> {
				Assertions.assertEquals(1, codeActions.size()); // duplicate import

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getRight().getDiagnostics().getFirst().getRange().getStart(),
								b.getRight().getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst().getRight();
				assertEquals("Optimize imports", action0.getTitle());
				assertNull(action0.getEdit()); // make sure no edits are made at this point
			});
		});
	}

	//This feature to remove duplicate is removed temperately
	@Test
	public void testResolveDuplicateImport() {
		String model = """
				namespace foo.bar

				import dsl.foo.* as foo
				import dsl.foo.* as foo
				        import dsl.foo.*
				        import dsl.foo.*

				func Bar:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result: foo -> a
				""";

		testResultCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setFilesInScope(Map.of("foo.rosetta", """
					namespace dsl.foo

					type Foo:
						a int (1..1)
					"""));
			cfg.setAssertCodeActionResolution(codeActions -> {
				Assertions.assertEquals(3, codeActions.size()); //duplicate import

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getDiagnostics().getFirst().getRange().getStart(),
								b.getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst();
				assertEquals("Optimize imports", action0.getTitle());
				var edit0 = action0.getEdit().getChanges().values().iterator().next().getFirst();
				assertEquals("import dsl.foo.* as foo\nimport dsl.foo.* as foo\nimport dsl.foo.*\nimport dsl.foo.*", edit0.getNewText()); // second import is deleted
				Assertions.assertEquals(new Position(2, 0), edit0.getRange().getStart());
				Assertions.assertEquals(new Position(5, 24), edit0.getRange().getEnd());
			});
		});
	}

	@Test
	public void testQuickFixUnusedImport() {
		String model = """
				namespace foo.bar

				import dsl.foo.*
				import dsl.bar.*

				func Bar:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result: foo -> a
				""";
		testCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setFilesInScope(Map.of("foo.rosetta", """
					namespace dsl.foo

					type Foo:
						a int (1..1)
					"""));
			cfg.setAssertCodeActions(codeActions -> {
				Assertions.assertEquals(2, codeActions.size()); //one unused, one 'Sort Imports' codeAction

				var sorted = codeActions.stream()
						.sorted((a, b) -> a.getRight().getTitle().compareTo(b.getRight().getTitle()))
						.toList();

				var action0 = sorted.getFirst().getRight();
				assertEquals("Optimize imports", action0.getTitle());
				assertNull(action0.getEdit()); // make sure no edits are made at this point

				var action1 = sorted.get(1).getRight();
				assertEquals("Sort imports", action1.getTitle());
				assertNull(action1.getEdit()); // make sure no edits are made at this point
			});
		});
	}

	@Test
	public void testResolveUnusedImport() {
		String model = """
				namespace foo.bar

				import dsl.foo.*
				import dsl.bar.*

				func Bar:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result: foo -> a
				""";

		testResultCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setFilesInScope(Map.of("foo.rosetta", """
					namespace dsl.foo

					type Foo:
						a int (1..1)
					"""));
			cfg.setAssertCodeActionResolution(codeActions -> {
				Assertions.assertEquals(2, codeActions.size()); //one unused, one 'Sort Imports' codeAction

				var sorted = codeActions.stream()
						.sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))
						.toList();

				var action0 = sorted.getFirst();
				assertEquals("Optimize imports", action0.getTitle());
				var edit0 = action0.getEdit().getChanges().values().iterator().next().getFirst();
				assertEquals("import dsl.bar.*\nimport dsl.foo.*", edit0.getNewText()); // second import is deleted
				Assertions.assertEquals(new Position(2, 0), edit0.getRange().getStart());
				Assertions.assertEquals(new Position(3, 16), edit0.getRange().getEnd());

				var action1 = sorted.get(1);
				assertEquals("Sort imports", action1.getTitle());
				var edit1 = action1.getEdit().getChanges().values().iterator().next().getFirst();
				assertEquals("import dsl.bar.*\nimport dsl.foo.*", edit1.getNewText()); // imports are sorted
				Assertions.assertEquals(new Position(2, 0), edit1.getRange().getStart());
				Assertions.assertEquals(new Position(3, 16), edit1.getRange().getEnd());
			});
		});
	}

	@Test
	public void testQuickFixUnsortedImports() {
		String model = """
				namespace foo.bar

				import dsl.foo.*
				import dsl.aaa.*

				func Bar:
					inputs:\s
						foo Foo (1..1)
						aaa Aaa (1..1)
					output: result int (1..1)

					set result: aaa -> a
				""";
		testCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setFilesInScope(Map.of(
					"foo.rosetta", """
							namespace dsl.foo

							type Foo:
								a int (1..1)
							""",
					"ach.rosetta", """
							namespace dsl.aaa

							type Aaa:
								a int (1..1)
							"""));
			cfg.setAssertCodeActions(codeActions -> {
				Assertions.assertEquals(1, codeActions.size()); // one 'Sort Imports' codeAction

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getRight().getDiagnostics().getFirst().getRange().getStart(),
								b.getRight().getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst().getRight();
				assertEquals("Sort imports", action0.getTitle());
				assertNull(action0.getEdit()); // make sure no edits are made at this point
			});
		});
	}

	@Test
	public void testResolveUnsortedImports() {
		String model = """
				namespace foo.bar

				import dsl.foo.*
				import asl.aaa.*

				func Bar:
					inputs: foo Foo (1..1)
							aaa Aaa (1..1)
					output: result int (1..1)

					set result: foo -> a
				""";

		testResultCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setFilesInScope(Map.of(
					"foo.rosetta", """
							namespace dsl.foo

							type Foo:
								a int (1..1)
							""",
					"ach.rosetta", """
							namespace asl.aaa

							type Aaa:
								a int (1..1)
							"""));
			cfg.setAssertCodeActionResolution(codeActions -> {
				Assertions.assertEquals(1, codeActions.size());

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getDiagnostics().getFirst().getRange().getStart(),
								b.getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst();
				assertEquals("Sort imports", action0.getTitle());
				var edit0 = action0.getEdit().getChanges().values().iterator().next().getFirst();
				assertEquals("import asl.aaa.*\n\nimport dsl.foo.*", edit0.getNewText()); // imports are sorted
				Assertions.assertEquals(new Position(2, 0), edit0.getRange().getStart());
				Assertions.assertEquals(new Position(3, 16), edit0.getRange().getEnd());
			});
		});
	}


	@Test
	public void testQuickFixConstructorAttributes() {
		String model = """
				namespace test

				type T:
					a string (1..1)
				func F:
					output: t T (1..1)
					set t : T {}
				""";

		testCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setAssertCodeActions(codeActions -> {
				Assertions.assertEquals(2, codeActions.size()); // mandatory attributes + all attributes quickfixes

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getRight().getDiagnostics().getFirst().getRange().getStart(),
								b.getRight().getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst().getRight();
				assertEquals("Add all attributes", action0.getTitle());
				assertNull(action0.getEdit()); // make sure no edits are made at this point

				var action1 = sorted.get(1).getRight();
				assertEquals("Add mandatory attributes", action1.getTitle());
				assertNull(action1.getEdit()); // make sure no edits are made at this point
			});
		});
	}


	@Test
	public void testResolveConstructorAttributes() {
		String model = """
				namespace test

				type T:
					a string (1..1)
					b string (0..1)
				func F:
					output: t T (1..1)
					set t : T {}
				""";

		testResultCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setAssertCodeActionResolution(codeActions -> {
				Assertions.assertEquals(2, codeActions.size()); // mandatory attributes + all attributes quickfixes

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getDiagnostics().getFirst().getRange().getStart(),
								b.getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst();
				assertEquals("Add all attributes", action0.getTitle());
				var edit0 = action0.getEdit().getChanges().values().iterator().next().getFirst();
				String expectedResult0 = """
						T {
						            a: empty,
						            b: empty
						        }
						""";
				assertEquals(expectedResult0, edit0.getNewText()); // all attributes are added
				Assertions.assertEquals(new Position(7, 9), edit0.getRange().getStart());
				Assertions.assertEquals(new Position(8, 0), edit0.getRange().getEnd());

				var action1 = sorted.get(1);
				assertEquals("Add mandatory attributes", action1.getTitle());
				var edit1 = action1.getEdit().getChanges().values().iterator().next().getFirst();
				String expectedResult1 = """
						T {
						            a: empty,
						            ...
						        }
						""";
				assertEquals(expectedResult1, edit1.getNewText()); // mandatory attribute is added
				Assertions.assertEquals(new Position(7, 9), edit1.getRange().getStart());
				Assertions.assertEquals(new Position(8, 0), edit1.getRange().getEnd());
			});
		});
	}

	@Test
	public void testQuickFixChoiceAttributes() {
		String model = """
				namespace test

				type A:
					n string (0..1)

				type B:
					m string (0..1)

				choice C:
					A
					B

				func F:
					output: c C (1..1)
					set c : C {}
				""";

		testCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setAssertCodeActions(codeActions -> {
				Assertions.assertEquals(2, codeActions.size()); // mandatory attributes + all attributes quickfixes

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getRight().getDiagnostics().getFirst().getRange().getStart(),
								b.getRight().getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst().getRight();
				assertEquals("Add all attributes", action0.getTitle());
				assertNull(action0.getEdit()); // make sure no edits are made at this point

				var action1 = sorted.get(1).getRight();
				assertEquals("Add mandatory attributes", action1.getTitle());
				assertNull(action1.getEdit()); // make sure no edits are made at this point
			});
		});
	}


	@Test
	public void testResolveChoiceAttributes() {
		String model = """
				namespace test

				type A:
					n string (0..1)

				type B:
					m string (0..1)

				choice C:
					A
					B

				func F:
					output: c C (1..1)
					set c : C {}
				""";

		testResultCodeAction(cfg -> {
			cfg.setModel(model);
			cfg.setAssertCodeActionResolution(codeActions -> {
				Assertions.assertEquals(2, codeActions.size()); // mandatory attributes + all attributes quickfixes

				var sorted = codeActions.stream()
						.sorted((a, b) -> ru.comparePositions(
								a.getDiagnostics().getFirst().getRange().getStart(),
								b.getDiagnostics().getFirst().getRange().getStart()))
						.toList();

				var action0 = sorted.getFirst();
				assertEquals("Add all attributes", action0.getTitle());
				var edit0 = action0.getEdit().getChanges().values().iterator().next().getFirst();
				String expectedResult0 = """
						C {
						            A: empty,
						            B: empty
						        }
						""";
				assertEquals(expectedResult0, edit0.getNewText()); // all attributes are added
				Assertions.assertEquals(new Position(14, 9), edit0.getRange().getStart());
				Assertions.assertEquals(new Position(15, 0), edit0.getRange().getEnd());

				var action1 = sorted.get(1);
				assertEquals("Add mandatory attributes", action1.getTitle());
				var edit1 = action1.getEdit().getChanges().values().iterator().next().getFirst();
				String expectedResult1 = """
						C {
						            ...
						        }
						""";
				assertEquals(expectedResult1, edit1.getNewText()); // mandatory attribute is added
				Assertions.assertEquals(new Position(14, 9), edit1.getRange().getStart());
				Assertions.assertEquals(new Position(15, 0), edit1.getRange().getEnd());
			});
		});
	}
}
