package test.labels;

import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.path.RosettaPath;
import java.util.HashMap;
import java.util.Map;


public class BodyCorpusLabelProvider implements LabelProvider {
	private final Map<RosettaPath, String> labelMap;
	
	public BodyCorpusLabelProvider() {
		labelMap = new HashMap<>();
		
		labelMap.put(RosettaPath.valueOf("attr"), "My attribute");
		labelMap.put(RosettaPath.valueOf("other"), "Other from rule");
	}
	
	@Override
	public String getLabel(RosettaPath path) {
		RosettaPath normalized = path.toIndexless();
		return labelMap.get(normalized);
	}
}
