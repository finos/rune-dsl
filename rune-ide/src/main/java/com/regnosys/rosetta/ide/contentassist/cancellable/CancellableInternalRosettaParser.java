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

package com.regnosys.rosetta.ide.contentassist.cancellable;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.ide.contentassist.antlr.internal.InternalRosettaParser;

/**
 * A patch of the generated `InternalRosettaParser` which makes a completion request cancellable.
 * TODO: contribute to Xtext.
 */
public class CancellableInternalRosettaParser extends InternalRosettaParser {
	private final CancelIndicator cancelIndicator;
	private final OperationCanceledManager operationCanceledManager;
	
	public CancellableInternalRosettaParser(TokenStream input, OperationCanceledManager operationCanceledManager, CancelIndicator cancelIndicator) {
		super(input);
		this.operationCanceledManager = operationCanceledManager;
		this.cancelIndicator = cancelIndicator;
	}
	public CancellableInternalRosettaParser(TokenStream input, RecognizerSharedState state, OperationCanceledManager operationCanceledManager, CancelIndicator cancelIndicator) {
        super(input, state);
        this.operationCanceledManager = operationCanceledManager;
        this.cancelIndicator = cancelIndicator;
    }
	
	// Hook into the `before` method to check for cancellation.
	@Override
	public void before(EObject grammarElement) {
		operationCanceledManager.checkCanceled(cancelIndicator);
		super.before(grammarElement);
	}
	
	// Hook into the `pushFollow` method to check for cancellation.
	@Override
	protected void pushFollow(BitSet fset) {
		operationCanceledManager.checkCanceled(cancelIndicator);
		super.pushFollow(fset);
	}
}
