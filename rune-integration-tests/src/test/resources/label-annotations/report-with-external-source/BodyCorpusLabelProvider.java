package test.labels;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class BodyCorpusLabelProvider extends GraphBasedLabelProvider {
	public BodyCorpusLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("attr"), "My attribute");
		startNode.addLabel(Arrays.asList("bar", "barAttr1"), "Bar Attribute 1 label");
		
		LabelNode barNode = new LabelNode();
		barNode.addLabel(Arrays.asList("barAttr1"), "My Bar Attribute 1 from rule");
		barNode.addLabel(Arrays.asList("barAttr2"), "My Bar Attribute 2 from rule");
		
		startNode.addOutgoingEdge("bar", barNode);
	}
}
