package com.regnosys.rosetta.ide.contentassist;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.eclipse.xtext.testing.TestCompletionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

// TODO: fix completion
public class ContentAssistTest extends AbstractRosettaLanguageServerTest {

    @Test
    public void testInheritedAttributesCondition() {
        String model = """
                namespace "test"
                version "test"
                type SuperSuperQuote:
                    superSuperAttr SuperQuote (0..1)
                
                type SuperQuote extends SuperSuperQuote:
                    superAttr SuperQuote (0..1)
                
                    condition SomeRule:
                        if Quote ->\s
                
                
                type Quote extends SuperQuote:
                    attr Quote (1..1)
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(9);
            it.setColumn(20);
            // TODO: this is broken.
            it.setExpectedCompletionItems("""
                    all -> all [[9, 20] .. [9, 20]]
                    and -> and [[9, 20] .. [9, 20]]
                    any -> any [[9, 20] .. [9, 20]]
                    contains -> contains [[9, 20] .. [9, 20]]
                    count -> count [[9, 20] .. [9, 20]]
                    default -> default [[9, 20] .. [9, 20]]
                    disjoint -> disjoint [[9, 20] .. [9, 20]]
                    distinct -> distinct [[9, 20] .. [9, 20]]
                    exists -> exists [[9, 20] .. [9, 20]]
                    extract -> extract [[9, 20] .. [9, 20]]
                    filter -> filter [[9, 20] .. [9, 20]]
                    first -> first [[9, 20] .. [9, 20]]
                    flatten -> flatten [[9, 20] .. [9, 20]]
                    is -> is [[9, 20] .. [9, 20]]
                    join -> join [[9, 20] .. [9, 20]]
                    last -> last [[9, 20] .. [9, 20]]
                    max -> max [[9, 20] .. [9, 20]]
                    min -> min [[9, 20] .. [9, 20]]
                    multiple -> multiple [[9, 20] .. [9, 20]]
                    one-of -> one-of [[9, 20] .. [9, 20]]
                    only-element -> only-element [[9, 20] .. [9, 20]]
                    optional -> optional [[9, 20] .. [9, 20]]
                    or -> or [[9, 20] .. [9, 20]]
                    reduce -> reduce [[9, 20] .. [9, 20]]
                    required -> required [[9, 20] .. [9, 20]]
                    reverse -> reverse [[9, 20] .. [9, 20]]
                    single -> single [[9, 20] .. [9, 20]]
                    sort -> sort [[9, 20] .. [9, 20]]
                    sum -> sum [[9, 20] .. [9, 20]]
                    switch -> switch [[9, 20] .. [9, 20]]
                    then -> then [[9, 20] .. [9, 20]]
                    to-date -> to-date [[9, 20] .. [9, 20]]
                    to-date-time -> to-date-time [[9, 20] .. [9, 20]]
                    to-enum -> to-enum [[9, 20] .. [9, 20]]
                    to-int -> to-int [[9, 20] .. [9, 20]]
                    to-number -> to-number [[9, 20] .. [9, 20]]
                    to-string -> to-string [[9, 20] .. [9, 20]]
                    to-time -> to-time [[9, 20] .. [9, 20]]
                    to-zoned-date-time -> to-zoned-date-time [[9, 20] .. [9, 20]]
                    with-meta -> with-meta [[9, 20] .. [9, 20]]
                    * -> * [[9, 20] .. [9, 20]]
                    + -> + [[9, 20] .. [9, 20]]
                    - -> - [[9, 20] .. [9, 20]]
                    -> -> -> [[9, 20] .. [9, 20]]
                    ->> -> ->> [[9, 20] .. [9, 20]]
                    / -> / [[9, 20] .. [9, 20]]
                    < -> < [[9, 20] .. [9, 20]]
                    <= -> <= [[9, 20] .. [9, 20]]
                    <> -> <> [[9, 20] .. [9, 20]]
                    = -> = [[9, 20] .. [9, 20]]
                    > -> > [[9, 20] .. [9, 20]]
                    >= -> >= [[9, 20] .. [9, 20]]
                    """);
        });
    }

    // TODO: debug null pointer exception in log
    @Test
    public void testConditionAfterArrow() {
        String model = """
                namespace "test"
                version "test"
                
                type Test: otherAttr Other (1..1)
                
                condition CreditDefaultSwap_LongForm:
                    if Test -> otherAttr -> testAttr -> otherAttr -> testAttr
                    then Test -> otherAttr ->\s
                
                type Other: testAttr Test (1..1)
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(7);
            it.setColumn(30);
            // TODO: this is broken
            it.setExpectedCompletionItems("""
                    all -> all [[7, 30] .. [7, 30]]
                    and -> and [[7, 30] .. [7, 30]]
                    annotation -> annotation [[7, 30] .. [7, 30]]
                    any -> any [[7, 30] .. [7, 30]]
                    basicType -> basicType [[7, 30] .. [7, 30]]
                    body -> body [[7, 30] .. [7, 30]]
                    choice -> choice [[7, 30] .. [7, 30]]
                    condition -> condition [[7, 30] .. [7, 30]]
                    contains -> contains [[7, 30] .. [7, 30]]
                    corpus -> corpus [[7, 30] .. [7, 30]]
                    count -> count [[7, 30] .. [7, 30]]
                    default -> default [[7, 30] .. [7, 30]]
                    disjoint -> disjoint [[7, 30] .. [7, 30]]
                    distinct -> distinct [[7, 30] .. [7, 30]]
                    eligibility -> eligibility [[7, 30] .. [7, 30]]
                    else -> else [[7, 30] .. [7, 30]]
                    enum -> enum [[7, 30] .. [7, 30]]
                    exists -> exists [[7, 30] .. [7, 30]]
                    extract -> extract [[7, 30] .. [7, 30]]
                    filter -> filter [[7, 30] .. [7, 30]]
                    first -> first [[7, 30] .. [7, 30]]
                    flatten -> flatten [[7, 30] .. [7, 30]]
                    func -> func [[7, 30] .. [7, 30]]
                    is -> is [[7, 30] .. [7, 30]]
                    join -> join [[7, 30] .. [7, 30]]
                    last -> last [[7, 30] .. [7, 30]]
                    library -> library [[7, 30] .. [7, 30]]
                    max -> max [[7, 30] .. [7, 30]]
                    metaType -> metaType [[7, 30] .. [7, 30]]
                    min -> min [[7, 30] .. [7, 30]]
                    multiple -> multiple [[7, 30] .. [7, 30]]
                    one-of -> one-of [[7, 30] .. [7, 30]]
                    only-element -> only-element [[7, 30] .. [7, 30]]
                    optional -> optional [[7, 30] .. [7, 30]]
                    or -> or [[7, 30] .. [7, 30]]
                    recordType -> recordType [[7, 30] .. [7, 30]]
                    reduce -> reduce [[7, 30] .. [7, 30]]
                    report -> report [[7, 30] .. [7, 30]]
                    reporting -> reporting [[7, 30] .. [7, 30]]
                    required -> required [[7, 30] .. [7, 30]]
                    reverse -> reverse [[7, 30] .. [7, 30]]
                    rule -> rule [[7, 30] .. [7, 30]]
                    segment -> segment [[7, 30] .. [7, 30]]
                    single -> single [[7, 30] .. [7, 30]]
                    sort -> sort [[7, 30] .. [7, 30]]
                    sum -> sum [[7, 30] .. [7, 30]]
                    switch -> switch [[7, 30] .. [7, 30]]
                    synonym -> synonym [[7, 30] .. [7, 30]]
                    then -> then [[7, 30] .. [7, 30]]
                    to-date -> to-date [[7, 30] .. [7, 30]]
                    to-date-time -> to-date-time [[7, 30] .. [7, 30]]
                    to-enum -> to-enum [[7, 30] .. [7, 30]]
                    to-int -> to-int [[7, 30] .. [7, 30]]
                    to-number -> to-number [[7, 30] .. [7, 30]]
                    to-string -> to-string [[7, 30] .. [7, 30]]
                    to-time -> to-time [[7, 30] .. [7, 30]]
                    to-zoned-date-time -> to-zoned-date-time [[7, 30] .. [7, 30]]
                    type -> type [[7, 30] .. [7, 30]]
                    typeAlias -> typeAlias [[7, 30] .. [7, 30]]
                    with-meta -> with-meta [[7, 30] .. [7, 30]]
                    * -> * [[7, 30] .. [7, 30]]
                    + -> + [[7, 30] .. [7, 30]]
                    - -> - [[7, 30] .. [7, 30]]
                    -> -> -> [[7, 30] .. [7, 30]]
                    ->> -> ->> [[7, 30] .. [7, 30]]
                    / -> / [[7, 30] .. [7, 30]]
                    < -> < [[7, 30] .. [7, 30]]
                    <= -> <= [[7, 30] .. [7, 30]]
                    <> -> <> [[7, 30] .. [7, 30]]
                    = -> = [[7, 30] .. [7, 30]]
                    > -> > [[7, 30] .. [7, 30]]
                    >= -> >= [[7, 30] .. [7, 30]]
                    """);
        });
    }

    @Test
    public void testConditionAfterArrow2() {
        String model = """
                namespace "test"
                version "test"
                
                type Test: otherAttr Other (1..1)
                
                condition CreditDefaultSwap_LongForm:
                    if Test -> otherAttr ->\s
                
                type Other: testAttr Test (1..1)
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(6);
            it.setColumn(28);
            // TODO: this is broken
            it.setExpectedCompletionItems("""
                    all -> all [[6, 28] .. [6, 28]]
                    and -> and [[6, 28] .. [6, 28]]
                    any -> any [[6, 28] .. [6, 28]]
                    contains -> contains [[6, 28] .. [6, 28]]
                    count -> count [[6, 28] .. [6, 28]]
                    default -> default [[6, 28] .. [6, 28]]
                    disjoint -> disjoint [[6, 28] .. [6, 28]]
                    distinct -> distinct [[6, 28] .. [6, 28]]
                    exists -> exists [[6, 28] .. [6, 28]]
                    extract -> extract [[6, 28] .. [6, 28]]
                    filter -> filter [[6, 28] .. [6, 28]]
                    first -> first [[6, 28] .. [6, 28]]
                    flatten -> flatten [[6, 28] .. [6, 28]]
                    is -> is [[6, 28] .. [6, 28]]
                    join -> join [[6, 28] .. [6, 28]]
                    last -> last [[6, 28] .. [6, 28]]
                    max -> max [[6, 28] .. [6, 28]]
                    min -> min [[6, 28] .. [6, 28]]
                    multiple -> multiple [[6, 28] .. [6, 28]]
                    one-of -> one-of [[6, 28] .. [6, 28]]
                    only-element -> only-element [[6, 28] .. [6, 28]]
                    optional -> optional [[6, 28] .. [6, 28]]
                    or -> or [[6, 28] .. [6, 28]]
                    reduce -> reduce [[6, 28] .. [6, 28]]
                    required -> required [[6, 28] .. [6, 28]]
                    reverse -> reverse [[6, 28] .. [6, 28]]
                    single -> single [[6, 28] .. [6, 28]]
                    sort -> sort [[6, 28] .. [6, 28]]
                    sum -> sum [[6, 28] .. [6, 28]]
                    switch -> switch [[6, 28] .. [6, 28]]
                    then -> then [[6, 28] .. [6, 28]]
                    to-date -> to-date [[6, 28] .. [6, 28]]
                    to-date-time -> to-date-time [[6, 28] .. [6, 28]]
                    to-enum -> to-enum [[6, 28] .. [6, 28]]
                    to-int -> to-int [[6, 28] .. [6, 28]]
                    to-number -> to-number [[6, 28] .. [6, 28]]
                    to-string -> to-string [[6, 28] .. [6, 28]]
                    to-time -> to-time [[6, 28] .. [6, 28]]
                    to-zoned-date-time -> to-zoned-date-time [[6, 28] .. [6, 28]]
                    with-meta -> with-meta [[6, 28] .. [6, 28]]
                    * -> * [[6, 28] .. [6, 28]]
                    + -> + [[6, 28] .. [6, 28]]
                    - -> - [[6, 28] .. [6, 28]]
                    -> -> -> [[6, 28] .. [6, 28]]
                    ->> -> ->> [[6, 28] .. [6, 28]]
                    / -> / [[6, 28] .. [6, 28]]
                    < -> < [[6, 28] .. [6, 28]]
                    <= -> <= [[6, 28] .. [6, 28]]
                    <> -> <> [[6, 28] .. [6, 28]]
                    = -> = [[6, 28] .. [6, 28]]
                    > -> > [[6, 28] .. [6, 28]]
                    >= -> >= [[6, 28] .. [6, 28]]
                    """);
        });
    }

    @Test
    public void testSynonymSource() {
        String model = """
                namespace "test"
                
                synonym source FIX
                synonym source FpML
                
                type Foo:
                    [synonym ]
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(6);
            it.setColumn(13);
            // TODO: should only have the first two?
            it.setExpectedCompletionItems("""
                    FIX (RosettaSynonymSource) -> FIX [[6, 13] .. [6, 13]]
                    FpML (RosettaSynonymSource) -> FpML [[6, 13] .. [6, 13]]
                    test.FIX (RosettaSynonymSource) -> test.FIX [[6, 13] .. [6, 13]]
                    test.FpML (RosettaSynonymSource) -> test.FpML [[6, 13] .. [6, 13]]
                    """);
        });
    }

    @Test
    public void testSynonymSetToEnum() {
        String model = """
                namespace "test"
                version "test"
                
                type Foo:
                    action ActionEnum (1..1)
                        [synonym FpML set to ActionEnum -> ]
                
                enum ActionEnum: new correct cancel
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(5);
            it.setColumn(43);
            it.setExpectedCompletionItems("""
                    cancel (RosettaEnumValue) -> cancel [[5, 43] .. [5, 43]]
                    correct (RosettaEnumValue) -> correct [[5, 43] .. [5, 43]]
                    new (RosettaEnumValue) -> new [[5, 43] .. [5, 43]]
                    """);
        });
    }

    @Test
    public void testSynonymSetToBoolean() {
        String model = """
                namespace "test"
                version "test"
                
                type Foo:
                    attr boolean (1..1)
                    [synonym FpML set to T]
                }
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(5);
            it.setColumn(26);
            it.setExpectedCompletionItems("""
                    True -> True [[5, 25] .. [5, 26]]
                    -> -> -> [[5, 26] .. [5, 26]]
                    """);
        });
    }

    @Test
    public void testAssignOutputEnumLiteral() {
        String model = """
                namespace "test"
                
                type Quote:
                    action ActionEnum (1..1)
                
                enum ActionEnum:
                    new
                    correct
                    cancel
                
                enum BadEnum:
                    new
                    correct
                    cancel
                
                func test:
                    inputs: attrIn Quote (0..1)
                    output: attrOut Quote (0..1)
                    set attrOut -> action:
                       \s
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(19);
            it.setColumn(8);
            // TODO: shouldn't have this much?
            it.setExpectedCompletionItems("""
                    ActionEnum (RosettaEnumeration) -> ActionEnum [[19, 8] .. [19, 8]]
                    attrIn (Attribute) -> attrIn [[19, 8] .. [19, 8]]
                    attrOut (Attribute) -> attrOut [[19, 8] .. [19, 8]]
                    BadEnum (RosettaEnumeration) -> BadEnum [[19, 8] .. [19, 8]]
                    boolean (RosettaBasicType) -> boolean [[19, 8] .. [19, 8]]
                    calculation (RosettaTypeAlias) -> calculation [[19, 8] .. [19, 8]]
                    com.rosetta.model.boolean (RosettaBasicType) -> com.rosetta.model.boolean [[19, 8] .. [19, 8]]
                    com.rosetta.model.calculation (RosettaTypeAlias) -> com.rosetta.model.calculation [[19, 8] .. [19, 8]]
                    com.rosetta.model.date (RosettaRecordType) -> com.rosetta.model.date [[19, 8] .. [19, 8]]
                    com.rosetta.model.dateTime (RosettaRecordType) -> com.rosetta.model.dateTime [[19, 8] .. [19, 8]]
                    com.rosetta.model.eventType (RosettaTypeAlias) -> com.rosetta.model.eventType [[19, 8] .. [19, 8]]
                    com.rosetta.model.int (RosettaTypeAlias) -> com.rosetta.model.int [[19, 8] .. [19, 8]]
                    com.rosetta.model.number (RosettaBasicType) -> com.rosetta.model.number [[19, 8] .. [19, 8]]
                    com.rosetta.model.pattern (RosettaBasicType) -> com.rosetta.model.pattern [[19, 8] .. [19, 8]]
                    com.rosetta.model.productType (RosettaTypeAlias) -> com.rosetta.model.productType [[19, 8] .. [19, 8]]
                    com.rosetta.model.string (RosettaBasicType) -> com.rosetta.model.string [[19, 8] .. [19, 8]]
                    com.rosetta.model.time (RosettaBasicType) -> com.rosetta.model.time [[19, 8] .. [19, 8]]
                    com.rosetta.model.zonedDateTime (RosettaRecordType) -> com.rosetta.model.zonedDateTime [[19, 8] .. [19, 8]]
                    date (RosettaRecordType) -> date [[19, 8] .. [19, 8]]
                    dateTime (RosettaRecordType) -> dateTime [[19, 8] .. [19, 8]]
                    eventType (RosettaTypeAlias) -> eventType [[19, 8] .. [19, 8]]
                    int (RosettaTypeAlias) -> int [[19, 8] .. [19, 8]]
                    number (RosettaBasicType) -> number [[19, 8] .. [19, 8]]
                    pattern (RosettaBasicType) -> pattern [[19, 8] .. [19, 8]]
                    productType (RosettaTypeAlias) -> productType [[19, 8] .. [19, 8]]
                    Quote (Data) -> Quote [[19, 8] .. [19, 8]]
                    string (RosettaBasicType) -> string [[19, 8] .. [19, 8]]
                    test.ActionEnum (RosettaEnumeration) -> test.ActionEnum [[19, 8] .. [19, 8]]
                    test.BadEnum (RosettaEnumeration) -> test.BadEnum [[19, 8] .. [19, 8]]
                    test.Quote (Data) -> test.Quote [[19, 8] .. [19, 8]]
                    time (RosettaBasicType) -> time [[19, 8] .. [19, 8]]
                    zonedDateTime (RosettaRecordType) -> zonedDateTime [[19, 8] .. [19, 8]]
                    "value" (STRING) -> "value" [[19, 8] .. [19, 8]]
                    all -> all [[19, 8] .. [19, 8]]
                    and -> and [[19, 8] .. [19, 8]]
                    any -> any [[19, 8] .. [19, 8]]
                    contains -> contains [[19, 8] .. [19, 8]]
                    count -> count [[19, 8] .. [19, 8]]
                    default -> default [[19, 8] .. [19, 8]]
                    disjoint -> disjoint [[19, 8] .. [19, 8]]
                    distinct -> distinct [[19, 8] .. [19, 8]]
                    empty -> empty [[19, 8] .. [19, 8]]
                    exists -> exists [[19, 8] .. [19, 8]]
                    extract -> extract [[19, 8] .. [19, 8]]
                    False -> False [[19, 8] .. [19, 8]]
                    filter -> filter [[19, 8] .. [19, 8]]
                    first -> first [[19, 8] .. [19, 8]]
                    flatten -> flatten [[19, 8] .. [19, 8]]
                    if -> if [[19, 8] .. [19, 8]]
                    is -> is [[19, 8] .. [19, 8]]
                    item -> item [[19, 8] .. [19, 8]]
                    join -> join [[19, 8] .. [19, 8]]
                    last -> last [[19, 8] .. [19, 8]]
                    max -> max [[19, 8] .. [19, 8]]
                    min -> min [[19, 8] .. [19, 8]]
                    multiple -> multiple [[19, 8] .. [19, 8]]
                    one-of -> one-of [[19, 8] .. [19, 8]]
                    only-element -> only-element [[19, 8] .. [19, 8]]
                    optional -> optional [[19, 8] .. [19, 8]]
                    or -> or [[19, 8] .. [19, 8]]
                    reduce -> reduce [[19, 8] .. [19, 8]]
                    required -> required [[19, 8] .. [19, 8]]
                    reverse -> reverse [[19, 8] .. [19, 8]]
                    single -> single [[19, 8] .. [19, 8]]
                    sort -> sort [[19, 8] .. [19, 8]]
                    sum -> sum [[19, 8] .. [19, 8]]
                    switch -> switch [[19, 8] .. [19, 8]]
                    to-date -> to-date [[19, 8] .. [19, 8]]
                    to-date-time -> to-date-time [[19, 8] .. [19, 8]]
                    to-enum -> to-enum [[19, 8] .. [19, 8]]
                    to-int -> to-int [[19, 8] .. [19, 8]]
                    to-number -> to-number [[19, 8] .. [19, 8]]
                    to-string -> to-string [[19, 8] .. [19, 8]]
                    to-time -> to-time [[19, 8] .. [19, 8]]
                    to-zoned-date-time -> to-zoned-date-time [[19, 8] .. [19, 8]]
                    True -> True [[19, 8] .. [19, 8]]
                    with-meta -> with-meta [[19, 8] .. [19, 8]]
                    ( -> ( [[19, 8] .. [19, 8]]
                    * -> * [[19, 8] .. [19, 8]]
                    / -> / [[19, 8] .. [19, 8]]
                    < -> < [[19, 8] .. [19, 8]]
                    <= -> <= [[19, 8] .. [19, 8]]
                    <> -> <> [[19, 8] .. [19, 8]]
                    = -> = [[19, 8] .. [19, 8]]
                    > -> > [[19, 8] .. [19, 8]]
                    >= -> >= [[19, 8] .. [19, 8]]
                    [ -> [ [[19, 8] .. [19, 8]]
                    """);
        });
    }

    @Test
    public void testImport() {
        String model = """
                namespace my.ns
                
                import\s
                """;

        testCompletion((TestCompletionConfiguration it) -> {
            it.setFilesInScope(Map.of("otherns.rosetta", "namespace my.other.ns"));
            it.setModel(model);
            it.setLine(2);
            it.setColumn(7);
            // TODO: should have an auto completion?
            it.setExpectedCompletionItems("""
                    """);
        });
    }

    @Test
    public void testAttributeOverride() {
        String model = """
                namespace my.ns
                
                type Parent:
                    attr int (0..1)
                    parentAttr int (1..1)
                
                type Foo extends Parent:
                    override attr int (1..1)
                    otherAttr string (1..1)
                
                type Bar extends Foo:
                    override\s
                    barAttr int (1..1)
                """;

        testCompletion(it -> {
            it.setModel(model);
            it.setLine(11);
            it.setColumn(13);
            it.setExpectedCompletionItems("""
                    attr -> attr [[11, 13] .. [11, 13]]
                    otherAttr -> otherAttr [[11, 13] .. [11, 13]]
                    parentAttr -> parentAttr [[11, 13] .. [11, 13]]
                    """);
        });
    }
}