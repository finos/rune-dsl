package test.labels;

import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.path.RosettaPath;
import java.util.HashMap;
import java.util.Map;


public class BodyCorpusLabelProvider implements LabelProvider {
	private final Map<RosettaPath, String> labelMap;
	
	public BodyCorpusLabelProvider() {
		labelMap = new HashMap<>();
		
		labelMap.put(RosettaPath.valueOf("attr1"), "My Overridden Label");
		labelMap.put(RosettaPath.valueOf("qux.Opt1.opt1Attr"), "Super option 1 Attribute");
		labelMap.put(RosettaPath.valueOf("qux.Opt1.id"), "Deep path ID");
		labelMap.put(RosettaPath.valueOf("qux.Opt2.id"), "Deep path ID");
		labelMap.put(RosettaPath.valueOf("attr2"), "Label with item");
		labelMap.put(RosettaPath.valueOf("bar.barAttr"), "Bar attribute using path");
		labelMap.put(RosettaPath.valueOf("bar.nestedBarList.nestedAttr"), "Nested bar attribute $");
	}
	
	@Override
	public String getLabel(RosettaPath path) {
		RosettaPath normalized = path.toIndexless();
		return labelMap.get(normalized);
	}
}
