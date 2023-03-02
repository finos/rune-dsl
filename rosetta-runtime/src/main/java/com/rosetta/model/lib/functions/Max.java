package com.rosetta.model.lib.functions;

import java.math.BigDecimal;

public class Max {

	public Integer execute(Integer x, Integer y) {
		return Integer.max(x, y);
	}

	public BigDecimal execute(BigDecimal x, BigDecimal y) {
		return x.max(y);
	}
}
