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

import org.eclipse.xtext.preferences.IntegerKey;

public class RosettaFormatterPreferenceKeys {
	// Preferred max line width
	public static IntegerKey maxLineWidth = new IntegerKey("line.width.max", 92);
	
	// Preferred max width of a conditional expression
	public static IntegerKey conditionalMaxLineWidth = new IntegerKey("line.width.max.conditional", 70);
}
