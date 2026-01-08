package com.regnosys.rosetta.scoping;

import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration;
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch;
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

public class RosettaQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

	protected QualifiedName qualifiedName(RosettaMetaType metaType) {
		return QualifiedName.create(metaType.getName());
	}
	
	protected QualifiedName qualifiedName(RosettaQualifiableConfiguration ele) {
		QualifiedName modelQName = getFullyQualifiedName(ele.getModel());
		if (modelQName != null) {
			return modelQName.append("is" + ele.getQType().getName() + "Root");
		}
		return null;
	}

	protected QualifiedName qualifiedName(FunctionDispatch ele) {
		QualifiedName mainQName = computeFullyQualifiedNameFromNameAttribute(ele);
		if (mainQName != null) {
			if (ele.getValue() != null) {
				return mainQName.append("Dispatch");
			}
		}
		return mainQName;
	}
}
