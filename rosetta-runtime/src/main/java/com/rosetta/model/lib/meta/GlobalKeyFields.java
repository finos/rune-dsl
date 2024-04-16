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

package com.rosetta.model.lib.meta;

import java.util.List;

public interface GlobalKeyFields {
	
	String getGlobalKey();
	
	String getExternalKey();
	
	List<? extends Key> getKey();
	
	interface GlobalKeyFieldsBuilder extends GlobalKeyFields{
		
		GlobalKeyFieldsBuilder setGlobalKey(String globalKey);
		
		GlobalKeyFieldsBuilder setExternalKey(String ExternalKey);
		
		GlobalKeyFieldsBuilder setKey(List<? extends Key> keys);
		GlobalKeyFieldsBuilder addKey(Key key);
		GlobalKeyFieldsBuilder addKey(Key key, int _idx);
		GlobalKeyFieldsBuilder addKey(List<? extends Key> keys);
		Key.KeyBuilder getOrCreateKey(int _idx);
		List<? extends Key.KeyBuilder> getKey();
	}
}
