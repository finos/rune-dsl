package test.labels;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class BodyCorpusLabelProvider extends GraphBasedLabelProvider {
	public BodyCorpusLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("attr1"), "My Overridden Label");
		startNode.addLabel(Arrays.asList("qux", "Opt1", "opt1Attr"), "Super option 1 Attribute");
		startNode.addLabel(Arrays.asList("qux", "Opt1", "id"), "Deep path ID");
		startNode.addLabel(Arrays.asList("qux", "Opt2", "id"), "Deep path ID");
		startNode.addLabel(Arrays.asList("attr2"), "Label with item");
		startNode.addLabel(Arrays.asList("bar", "barAttr"), "Bar attribute using path");
		startNode.addLabel(Arrays.asList("bar", "nestedBarList", "nestedAttr"), "Nested bar attribute $");
		
		LabelNode quxNode = new LabelNode();
		
		LabelNode opt1Node = new LabelNode();
		opt1Node.addLabel(Arrays.asList("opt1Attr"), "Option 1 Attribute");
		
		LabelNode barNode = new LabelNode();
		barNode.addLabel(Arrays.asList("barAttr"), "Bar attribute from rule");
		
		startNode.addOutgoingEdge("qux", quxNode);
		startNode.addOutgoingEdge("bar", barNode);
		
		quxNode.addOutgoingEdge("Opt1", opt1Node);
	}
}
