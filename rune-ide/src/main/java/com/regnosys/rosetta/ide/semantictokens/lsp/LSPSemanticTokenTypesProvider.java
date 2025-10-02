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

package com.regnosys.rosetta.ide.semantictokens.lsp;

import java.util.Arrays;
import java.util.List;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenType;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenTypesProvider;

/**
 * TODO: contribute to Xtext.
 *
 */
public class LSPSemanticTokenTypesProvider implements ISemanticTokenTypesProvider {
	@Override
	public List<ISemanticTokenType> getSemanticTokenTypes() {
		return Arrays.asList(LSPSemanticTokenTypesEnum.values());
	}
}
