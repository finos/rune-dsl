package com.rosetta.model.lib.expression;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

public class ComparisonResult implements Mapper<Boolean> {
	private final boolean result;
	private final boolean emptyOperand;
	private final String error;
	
	public static ComparisonResult success() {
		return new ComparisonResult(true, false, null);
	}
	
	public static ComparisonResult successEmptyOperand(String error) {
		return new ComparisonResult(true, true, error);
	}
	
	public static ComparisonResult failure(String error) {
		return new ComparisonResult(false, false, error);
	}
	
	public static ComparisonResult failureEmptyOperand(String error) {
		return new ComparisonResult(false, true, error);
	}
	
	private ComparisonResult(boolean result, boolean emptyOperand, String error) {
		this.result = result;
		this.emptyOperand = emptyOperand;
		this.error = error;
	}
	
	public Boolean get() {
		return result;
	}
	
	public String getError() {
		return error;
	}
	
	// and
	
	public ComparisonResult and(ComparisonResult other) {
		return and(this, other);
	}
	
	public ComparisonResult andIgnoreEmptyOperand(ComparisonResult other) {
		return combineIgnoreEmptyOperand(other, this::and);
	}
	
	private ComparisonResult and(ComparisonResult r1, ComparisonResult r2) {
		boolean newResult = r1.result && r2.result;
		String newError = "";
		if (!r1.result) {
			newError+=r1.error;
		}
		if (!r2.result) {
			if (!r1.result) {
				newError+=" and ";
			}
			newError+=r2.error;
		}
		return new ComparisonResult(newResult, false, newError);
	}
	
	// or
	
	public ComparisonResult or(ComparisonResult other) {
		return or(this, other);
	}
	
	public ComparisonResult orIgnoreEmptyOperand(ComparisonResult other) {
		return combineIgnoreEmptyOperand(other, this::or);
	}
	
	private ComparisonResult or(ComparisonResult r1, ComparisonResult r2) {
		boolean newResult = r1.result || r2.result;
		String newError = "";
		newError+=r1.error;
		newError+=" and ";
		newError+=r2.error;
		return new ComparisonResult(newResult, false, newResult?null:newError);
	}
	
	// utils
	
	private ComparisonResult combineIgnoreEmptyOperand(ComparisonResult other, BinaryOperator<ComparisonResult> combineFunc) {
		if(this.emptyOperand && other.emptyOperand) {
			return ComparisonResult.failureEmptyOperand(this.error + " and " + other.error);
		}
		if(this.emptyOperand) {
			return other;
		}
		if(other.emptyOperand) {
			return this;
		}
		return combineFunc.apply(this, other);
	}

	@Override
	public List<Boolean> getMulti() {
		return Collections.singletonList(get());
	}

	@Override
	public Optional<?> getParent() {
		return Optional.empty();
	}

	@Override
	public List<?> getParentMulti() {
		return Collections.emptyList();
	}

	@Override
	public int resultCount() {
		return 1;
	}

	public MapperS<Boolean> asMapper() {
		return MapperS.of(this.get());
	}
	
	@Override
	public List<Path> getPaths() {
		return Collections.emptyList();
	}

	@Override
	public List<Path> getErrorPaths() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getErrors() {
		return Collections.singletonList(getError()); 
	}
}