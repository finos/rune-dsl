package test.pojo.labels.types;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class QuxLabelProvider extends GraphBasedLabelProvider {
	public QuxLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("qux"), "Qux \"q\" in C:\\units");
	}
}
