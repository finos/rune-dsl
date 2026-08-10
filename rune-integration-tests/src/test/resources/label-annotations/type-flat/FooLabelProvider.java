package test.labels.types;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class FooLabelProvider extends GraphBasedLabelProvider {
	public FooLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("attr1"), "Attr One");
		startNode.addLabel(Arrays.asList("attr2"), "Attr Two");
	}
}
