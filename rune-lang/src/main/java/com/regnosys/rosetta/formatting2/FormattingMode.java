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

package com.regnosys.rosetta.formatting2;

/**
 * NORMAL: put on multiple lines if it's too long, otherwise on a single line.
 * SINGLE_LINE: put everything on a single line.
 * CHAIN: put everything on newlines until the chain stops. What a "chain" is, is
 *        left up to the implementation of the formatter.
 */
public enum FormattingMode {
	NORMAL,
	SINGLE_LINE,
	CHAIN;
	
	public FormattingMode stopChain() {
		if (this.equals(FormattingMode.CHAIN)) {
			return FormattingMode.NORMAL;
		}
		return this;
	}
	
	public FormattingMode chainIf(boolean condition) {
		if (this.equals(FormattingMode.SINGLE_LINE)) {
			return this;
		}
		if (condition) {
			return FormattingMode.CHAIN;
		}
		return FormattingMode.NORMAL;
	}
	
	public FormattingMode singleLineIf(boolean condition) {
		if (this.equals(FormattingMode.CHAIN)) {
			return this;
		}
		if (condition) {
			return FormattingMode.SINGLE_LINE;
		}
		return this;
	}
}
