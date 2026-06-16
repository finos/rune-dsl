package test.deeppath.util;

import com.rosetta.model.lib.mapper.MapperS;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import test.deeppath.InnerChoice;
import test.deeppath.Leaf;
import test.deeppath.OuterChoice;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

public class OuterChoiceDeepPathUtil {
    private final InnerChoiceDeepPathUtil innerChoiceDeepPathUtil;

    @Inject
    public OuterChoiceDeepPathUtil(InnerChoiceDeepPathUtil innerChoiceDeepPathUtil) {
        this.innerChoiceDeepPathUtil = innerChoiceDeepPathUtil;
    }

    public String chooseCommon(OuterChoice outerChoice) {
        final MapperS<InnerChoice> innerChoice = MapperS.of(outerChoice).<InnerChoice>map("getInnerChoice", _outerChoice -> _outerChoice.getInnerChoice());
        if (exists(innerChoice).getOrDefault(false)) {
            return innerChoice.<String>map("chooseCommon", _innerChoice -> innerChoiceDeepPathUtil.chooseCommon(_innerChoice)).get();
        }
        final MapperS<Leaf> leaf = MapperS.of(outerChoice).<Leaf>map("getLeaf", _outerChoice -> _outerChoice.getLeaf());
        if (exists(leaf).getOrDefault(false)) {
            return leaf.<String>map("getCommon", _leaf -> _leaf.getCommon()).get();
        }
        return null;
    }

    public List<String> chooseItems(OuterChoice outerChoice) {
        final MapperS<InnerChoice> innerChoice = MapperS.of(outerChoice).<InnerChoice>map("getInnerChoice", _outerChoice -> _outerChoice.getInnerChoice());
        if (exists(innerChoice).getOrDefault(false)) {
            return innerChoice.<String>mapC("chooseItems", _innerChoice -> innerChoiceDeepPathUtil.chooseItems(_innerChoice)).getMulti();
        }
        final MapperS<Leaf> leaf = MapperS.of(outerChoice).<Leaf>map("getLeaf", _outerChoice -> _outerChoice.getLeaf());
        if (exists(leaf).getOrDefault(false)) {
            return leaf.<String>mapC("getItems", _leaf -> _leaf.getItems()).getMulti();
        }
        return Collections.<String>emptyList();
    }

}
