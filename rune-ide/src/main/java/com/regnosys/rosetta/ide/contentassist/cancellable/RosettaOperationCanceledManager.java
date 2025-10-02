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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.xtext.service.OperationCanceledManager;

// TODO: contribute to Xtext.
public class RosettaOperationCanceledManager extends OperationCanceledManager {
	@Override
	protected RuntimeException getPlatformOperationCanceledException(Throwable t) {
		RuntimeException result = super.getPlatformOperationCanceledException(t);
		if (result != null) {
			return result;
		}
		// Also inspect `InvocationTargetException`'s target exception.
		if (t instanceof InvocationTargetException) {
			return getPlatformOperationCanceledException(((InvocationTargetException)t).getTargetException());
		} else if (t instanceof RuntimeException && t.getCause() instanceof InvocationTargetException) {
			return getPlatformOperationCanceledException(((InvocationTargetException)t.getCause()).getTargetException());
		}
		return null;
	}
}
