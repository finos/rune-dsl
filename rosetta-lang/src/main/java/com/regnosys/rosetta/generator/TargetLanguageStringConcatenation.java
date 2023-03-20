package com.regnosys.rosetta.generator;

import java.util.Optional;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;

public class TargetLanguageStringConcatenation extends StringConcatenation {
	@Override
	protected void append(Object object, int index) {
		Object normalizedObject = normalize(object);
		normalizedAppend(normalizedObject, index);
	}
	
	protected void normalizedAppend(Object normalizedObject, int index) {
		if (normalizedObject instanceof TargetLanguageRepresentation) {
			TargetLanguageRepresentation repr = (TargetLanguageRepresentation)normalizedObject;
			super.append(new StringConcatenationClient() {
				@Override
				protected void appendTo(TargetStringConcatenation target) {
					repr.appendTo(target);
				}
			}, index);
		}
		super.append(normalizedObject, index);
	}
	
	protected Object normalize(Object object) {
		if (object instanceof Optional<?>) {
			return ((Optional<?>)object).orElseThrow();
		}
		return object;
	}
}
