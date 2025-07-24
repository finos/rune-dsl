package test.labels;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class MyFuncLabelProvider extends GraphBasedLabelProvider {
	public MyFuncLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("fooAttr"), "Foo attribute");
		startNode.addLabel(Arrays.asList("nested", "b", "bAttr"), "Overridden B attribute");
		startNode.addLabel(Arrays.asList("nested", "b", "a", "b", "c", "b", "bAttr"), "Random path B attribute");
		
		LabelNode barNode = new LabelNode();
		barNode.addLabel(Arrays.asList("barAttr"), "Bar attribute");
		
		LabelNode aNode = new LabelNode();
		aNode.addLabel(Arrays.asList("b", "c", "b", "bAttr"), "A -> B -> C -> B attribute");
		
		LabelNode bNode = new LabelNode();
		bNode.addLabel(Arrays.asList("bAttr"), "Default B attribute");
		
		LabelNode cNode = new LabelNode();
		
		startNode.addOutgoingEdge("bar", barNode);
		startNode.addOutgoingEdge("nested", aNode);
		
		barNode.addOutgoingEdge("foos", startNode);
		
		aNode.addOutgoingEdge("b", bNode);
		
		bNode.addOutgoingEdge("a", aNode);
		bNode.addOutgoingEdge("c", cNode);
		
		cNode.addOutgoingEdge("b", bNode);
	}
}
