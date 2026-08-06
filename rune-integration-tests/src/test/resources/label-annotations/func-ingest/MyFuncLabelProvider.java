package test.labels;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class MyFuncLabelProvider extends GraphBasedLabelProvider {
	public MyFuncLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("attr"), "My attribute");
	}
}
