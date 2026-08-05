package test.labels;

import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import java.util.Arrays;


/**
 * @deprecated Prefer the type-rooted label provider referenced by
 *     {@code @}{@link RuneLabelProvider} on the root type's interface, where one exists.
 *     Retained because a provider rooted at a transform's output type cannot always be
 *     replaced by one generated for that type - see the class javadoc of
 *     {@code LabelProviderGenerator}.
 */
@Deprecated
public class MyFuncLabelProvider extends GraphBasedLabelProvider {
	public MyFuncLabelProvider() {
		super(new LabelNode());
		
		startNode.addLabel(Arrays.asList("attr"), "My attribute");
	}
}
