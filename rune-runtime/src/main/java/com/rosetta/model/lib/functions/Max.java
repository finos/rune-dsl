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

package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Max {

	public Integer execute(Integer x, Integer y) {
		if (x == null) {
			return y;
		} else if (y == null) {
			return x;
		}
		return Integer.max(x, y);
	}
	
	public Long execute(Long x, Long y) {
		if (x == null) {
			return y;
		} else if (y == null) {
			return x;
		}
		return Long.max(x, y);
	}
	
	public BigInteger execute(BigInteger x, BigInteger y) {
		if (x == null) {
			return y;
		} else if (y == null) {
			return x;
		}
		return x.max(y);
	}

	public BigDecimal execute(BigDecimal x, BigDecimal y) {
		if (x == null) {
			return y;
		} else if (y == null) {
			return x;
		}
		return x.max(y);
	}
}
