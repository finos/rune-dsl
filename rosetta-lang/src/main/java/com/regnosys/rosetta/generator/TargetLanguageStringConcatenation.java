package com.regnosys.rosetta.generator;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

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
	
	public StringConcatenationClient resolveIdentifiers(StringConcatenationClient code) {
		IdentifierResolver resolver = new IdentifierResolver();
		StringConcatenationClient.appendTo(code, resolver);
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				resolver.replay(target);
			}
		};
	}
	
	private static class IdentifierResolver implements TargetStringConcatenation {
		private List<Consumer<TargetStringConcatenation>> replay;
		
		@Override
		public int length() {
			throw new UnsupportedOperationException();
		}
		@Override
		public char charAt(int index) {
			throw new UnsupportedOperationException();
		}
		@Override
		public CharSequence subSequence(int start, int end) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void newLineIfNotEmpty() {
			replay.add(t -> t.newLineIfNotEmpty());
		}

		@Override
		public void newLine() {
			replay.add(t -> t.newLine());
		}

		@Override
		public void appendImmediate(Object object, String indentation) {
			replay.add(t -> t.appendImmediate(object, indentation));
		}

		@Override
		public void append(Object object, String indentation) {
			replay.add(t -> t.append(object, indentation));
		}

		@Override
		public void append(Object object) {
			replay.add(t -> t.append(object));
		}
		
		public void replay(TargetStringConcatenation target) {
			replay.forEach(c -> c.accept(target));
		}
	}
}
