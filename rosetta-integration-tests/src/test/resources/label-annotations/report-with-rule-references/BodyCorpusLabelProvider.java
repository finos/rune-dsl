package test.labels;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import java.util.Arrays;


public class BodyCorpusLabelProvider extends GraphBasedLabelProvider {
	public BodyCorpusLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("attr"), "My attribute");
		startNode.addLabel(Arrays.asList("other"), "Other from rule");
	}
}
