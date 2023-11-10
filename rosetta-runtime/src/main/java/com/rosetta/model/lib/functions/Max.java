package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Max {

	public Integer execute(Integer x, Integer y) {
		return Integer.max(x, y);
	}
	
	public Long execute(Long x, Long y) {
		return Long.max(x, y);
	}
	
	public BigInteger execute(BigInteger x, BigInteger y) {
		return x.max(y);
	}

	public BigDecimal execute(BigDecimal x, BigDecimal y) {
		return x.max(y);
	}
}
