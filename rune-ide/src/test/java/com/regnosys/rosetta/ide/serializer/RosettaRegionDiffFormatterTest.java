/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.ide.serializer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccessDiff;
import org.eclipse.xtext.ide.serializer.impl.RegionDiffFormatter;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.util.ITextRegion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.ide.tests.RosettaIdeInjectorProvider;

import jakarta.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaIdeInjectorProvider.class)
class RosettaRegionDiffFormatterTest {
	@Inject
	private RegionDiffFormatter regionDiffFormatter;

	@Test
	void bindsRosettaRegionDiffFormatter() {
		assertInstanceOf(RosettaRegionDiffFormatter.class, regionDiffFormatter);
	}

	@Test
	void emptyDiffRegionsReturnNoReplacementsWithoutCallingFormatter() {
		EmptyDiffFormatter formatter = new EmptyDiffFormatter();

		assertTrue(formatter.format(diff()).isEmpty());
		assertFalse(formatter.formatterRequested);
	}

	private ITextRegionAccessDiff diff() {
		return (ITextRegionAccessDiff) Proxy.newProxyInstance(
				ITextRegionAccessDiff.class.getClassLoader(),
				new Class<?>[]{ITextRegionAccessDiff.class},
				(proxy, method, args) -> {
					throw new AssertionError("Unexpected ITextRegionAccessDiff method call: " + method.getName());
				});
	}

	private static class EmptyDiffFormatter extends RosettaRegionDiffFormatter {
		private boolean formatterRequested;

		@Override
		protected Collection<ITextRegion> collectRegionsToFormat(ITextRegionAccessDiff regions) {
			return Collections.emptyList();
		}

		@Override
		protected IFormatter2 getFormatter() {
			formatterRequested = true;
			return request -> {
				throw new AssertionError("The language formatter should not be called for an empty diff.");
			};
		}
	}
}
