package test.deeppath.util;

import com.rosetta.model.lib.mapper.MapperS;
import java.util.Collections;
import java.util.List;
import test.deeppath.InnerChoice;
import test.deeppath.Option1;
import test.deeppath.Option2;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

public class InnerChoiceDeepPathUtil {
    public String chooseCommon(InnerChoice innerChoice) {
        final MapperS<Option1> option1 = MapperS.of(innerChoice).<Option1>map("getOption1", _innerChoice -> _innerChoice.getOption1());
        if (exists(option1).getOrDefault(false)) {
            return option1.<String>map("getCommon", _option1 -> _option1.getCommon()).get();
        }
        final MapperS<Option2> option2 = MapperS.of(innerChoice).<Option2>map("getOption2", _innerChoice -> _innerChoice.getOption2());
        if (exists(option2).getOrDefault(false)) {
            return option2.<String>map("getCommon", _option2 -> _option2.getCommon()).get();
        }
        return null;
    }

    public List<String> chooseItems(InnerChoice innerChoice) {
        final MapperS<Option1> option1 = MapperS.of(innerChoice).<Option1>map("getOption1", _innerChoice -> _innerChoice.getOption1());
        if (exists(option1).getOrDefault(false)) {
            return option1.<String>mapC("getItems", _option1 -> _option1.getItems()).getMulti();
        }
        final MapperS<Option2> option2 = MapperS.of(innerChoice).<Option2>map("getOption2", _innerChoice -> _innerChoice.getOption2());
        if (exists(option2).getOrDefault(false)) {
            return option2.<String>mapC("getItems", _option2 -> _option2.getItems()).getMulti();
        }
        return Collections.<String>emptyList();
    }

}
