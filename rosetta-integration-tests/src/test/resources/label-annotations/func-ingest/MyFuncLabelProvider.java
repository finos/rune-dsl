package test.labels;

import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.path.RosettaPath;
import java.util.HashMap;
import java.util.Map;


public class MyFuncLabelProvider implements LabelProvider {
	private final Map<RosettaPath, String> labelMap;
	
	public MyFuncLabelProvider() {
		labelMap = new HashMap<>();
		
		labelMap.put(RosettaPath.valueOf("attr"), "My attribute");
	}
	
	@Override
	public String getLabel(RosettaPath path) {
		RosettaPath normalized = path.toIndexless();
		return labelMap.get(normalized);
	}
}
