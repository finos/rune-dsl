/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.parsing;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.ValueConverterWithValueException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;

public class PATTERNValueConverter extends AbstractLexerBasedConverter<Pattern> {

	public PATTERNValueConverter() {
		super();
	}
	
	@Override
	protected String toEscapedString(Pattern value) {
		return '/' + value.toString() + '/';
	}
	
	@Override
	public Pattern toValue(String string, INode node) {
		if (string == null)
			return null;
		try {
			if (string.length() == 1) {
				throw new ValueConverterException(getPatternNotClosedMessage(), node, null);
			}
			return convertFromString(string, node);
		} catch (IllegalArgumentException e) {
			throw new ValueConverterException(e.getMessage(), node, e);
		}
	}
	
	protected Pattern convertFromString(String literal, INode node) throws ValueConverterWithValueException {
		Implementation converter = createConverter();
		Pattern result = converter.convertFromJavaString(literal);
		if (converter.errorMessage != null) {
			throw new ValueConverterWithValueException(converter.errorMessage, node, result, converter.errorIndex,
					converter.errorLength, null);
		}
		return result;
	}

	/**
	 * @since 2.16
	 */
	protected Implementation createConverter() {
		return new Implementation();
	}
	
	/**
	 * @since 2.16
	 */
	protected class Implementation {
		String errorMessage = null;
		int errorIndex = -1;
		int errorLength = -1;
		int nextIndex = 1;
		
		protected Implementation() {}
		
		private Pattern compile(String javaRegex) {
			try {
				return Pattern.compile(javaRegex);
			} catch (PatternSyntaxException e) {
				errorMessage = getInvalidSyntaxMessage(e);
				errorIndex = e.getIndex();
				return null;
			}
		}
		
		public Pattern convertFromJavaString(String literal) {
			int idx = literal.indexOf('\\');
			if (idx < 0 && literal.length() > 1 && literal.charAt(0) == literal.charAt(literal.length() - 1)) {
				return compile(literal.substring(1, literal.length() - 1));
			}
			return convertFromJavaString(literal, 1, new StringBuilder(literal.length()));
		}
		
		protected Pattern convertFromJavaString(String string, int index, StringBuilder result) {
			int length = string.length();
			while(index < length - 1) {
				nextIndex = index = unescapeCharAndAppendTo(string, index, result);
			}
			if (nextIndex < length) {
				if (nextIndex != length - 1) {
					throw new IllegalStateException();
				}
				char next = string.charAt(nextIndex);
				if (string.charAt(0) != next) {
					result.append(next);
					if (errorMessage == null) {
						if (next == '\\') {
							errorMessage = getInvalidEscapeSequenceMessage();
							errorIndex = nextIndex;
							errorLength = 1;
						} else {
							errorMessage = getPatternNotClosedMessage();
						}
					} else {
						errorMessage = getPatternNotClosedMessage();
						errorIndex = -1;
						errorLength = -1;
					}
				}
			} else if (nextIndex == length) {
				errorMessage = getPatternNotClosedMessage();
			}
			return compile(result.toString());
		}
		
		protected int doUnescapeCharAndAppendTo(String string, int index, StringBuilder result) {
			if (string.length() == index) {
				if (errorMessage == null) {
					errorMessage = getInvalidEscapeSequenceMessage();
					errorIndex = index - 1;
					errorLength = 1;
				}
				return index;
			}
			char c = string.charAt(index++);
			switch(c) {
				case '/':
				case '\\':
					// append as is
					break;
				default:
					result.append('\\');
			}
			validateAndAppendChar(c, result);
			return index;
		}
		
		protected int handleUnknownEscapeSequence(String string, char c, int index, StringBuilder result) {
			if (errorMessage == null) {
				errorMessage = getInvalidEscapeSequenceMessage();
				errorIndex = index - 2;
				errorLength = 2;
			}
			result.append(c);
			return index;
		}
		
		protected int unescapeCharAndAppendTo(String string, int index, StringBuilder result) {
			char c = string.charAt(index++);
			if (c == '\\') {
				return doUnescapeCharAndAppendTo(string, index, result);
			}
			validateAndAppendChar(c, result);
			return index;
		}
		
		protected void validateAndAppendChar(char c, StringBuilder result) {
			if (validate(c, result)) {
				result.append(c);	
			}
		}
		
		protected boolean validate(char c, StringBuilder result) {
			return true;
		}

	}
	
	protected String getInvalidSyntaxMessage(PatternSyntaxException e) {
		return "Invalid syntax: " + e.getMessage();
	}
	
	protected String getInvalidEscapeSequenceMessage() {
		return "Invalid escape sequence (valid ones are  \\/  \\\\ )";
	}

	protected String getPatternNotClosedMessage() {
		return "Pattern literal is not properly closed";
	}
}
