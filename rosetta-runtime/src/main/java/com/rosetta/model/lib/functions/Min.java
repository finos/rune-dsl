package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Min {
	
	public Integer execute(Integer x, Integer y) {
		return Integer.min(x, y);
	}
	
	public Long execute(Long x, Long y) {
		return Long.min(x, y);
	}
	
	public BigInteger execute(BigInteger x, BigInteger y) {
		return x.min(y);
	}

	public BigDecimal execute(BigDecimal x, BigDecimal y) {
		return x.min(y);
	}
}
