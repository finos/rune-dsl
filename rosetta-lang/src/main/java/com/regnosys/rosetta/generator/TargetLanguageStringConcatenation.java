package com.regnosys.rosetta.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class TargetLanguageStringConcatenation extends StringConcatenation {
	@Override
	protected void append(Object object, int index) {
		if (object instanceof GeneratedIdentifier) {
			GeneratedIdentifier repr = (GeneratedIdentifier)object;
			super.append(new StringConcatenationClient() {
					@Override
					protected void appendTo(TargetStringConcatenation target) {
						repr.appendTo(target);
					}
				}, index);
		} else {
			super.append(object, index);
		}
	}
	@Override
	protected void append(Object object, String indentation, int index) {
		if (object instanceof GeneratedIdentifier) {
			GeneratedIdentifier repr = (GeneratedIdentifier)object;
			super.append(new StringConcatenationClient() {
					@Override
					protected void appendTo(TargetStringConcatenation target) {
						repr.appendTo(target);
					}
				}, indentation, index);
		} else {
			super.append(object, indentation, index);
		}
	}
	
	protected Object normalize(Object object) {
		if (object instanceof Optional<?>) {
			return ((Optional<?>)object).orElseThrow();
		}
		return object;
	}
	
	public StringConcatenationClient preprocess(StringConcatenationClient code) {
		return resolve(code);
	}
	
	protected Object handle(Object object) {
		return object;
	}
	
	private StringConcatenationClient resolve(StringConcatenationClient unresolved) {
		Preprocessor processor = new Preprocessor();
		StringConcatenationClient.appendTo(unresolved, processor);
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				processor.replay(target);
			}
		};
	}
	
	private class Preprocessor implements TargetStringConcatenation {
		private final List<Consumer<TargetStringConcatenation>> replay = new ArrayList<>();
		
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
			Object processed = process(object);
			replay.add(t -> t.appendImmediate(processed, indentation));
		}

		@Override
		public void append(Object object, String indentation) {
			Object processed = process(object);
			replay.add(t -> t.append(processed, indentation));
		}

		@Override
		public void append(Object object) {
			Object processed = process(object);
			replay.add(t -> t.append(processed));
		}
		
		public void replay(TargetStringConcatenation target) {
			replay.forEach(c -> c.accept(target));
		}
		
		private Object process(Object object) {
			Object normalized = handle(normalize(object));
			if (normalized instanceof GeneratedIdentifier) {
				return normalized;
			} else if (normalized instanceof TargetLanguageRepresentation) {
				TargetLanguageRepresentation repr = (TargetLanguageRepresentation)normalized;
				StringConcatenationClient resolved = resolve(new StringConcatenationClient() {
					@Override
					protected void appendTo(TargetStringConcatenation target) {
						repr.appendTo(target);
					}
				});
				return resolved;
			} else if (normalized instanceof StringConcatenationClient) {
				StringConcatenationClient resolved = resolve((StringConcatenationClient)normalized);
				return resolved;
			}
			return normalized;
		}
	}
}
