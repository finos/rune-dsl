package com.regnosys.rosetta.generator;

import org.eclipse.xtend2.lib.StringConcatenationClient;

public class DebuggingTargetLanguageStringConcatenation extends TargetLanguageStringConcatenation {
	public static String convertToDebugString(Object object) {
		DebuggingTargetLanguageStringConcatenation repr = new DebuggingTargetLanguageStringConcatenation();
		StringConcatenationClient processed = repr.preprocess(new StringConcatenationClient() {
			protected void appendTo(TargetStringConcatenation target) {
				target.append(object);
			}
		});
		repr.append(processed);
		return repr.toString();
	}
	
	@Override
	protected Object handle(Object object) {
		if (object instanceof GeneratedIdentifier) {
			return ((GeneratedIdentifier) object).getDesiredName();
		}
		return object;
	}
}
