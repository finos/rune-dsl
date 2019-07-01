package com.rosetta.model.lib.functions;

import java.math.BigDecimal;

public class Min {
	
	public Integer execute(Integer x, Integer y) {
		return Integer.min(x, y);
	}

	public BigDecimal execute(BigDecimal x, BigDecimal y) {
		return x.min(y);
	}
}
