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

package com.rosetta.model.lib.expression;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperS;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class ComparisonResult implements Mapper<Boolean> {
	private final Boolean result;
	@Deprecated
	private final Boolean emptyOperand;
	private final String error;
	
	public static ComparisonResult success() {
		return new ComparisonResult(true, false, null);
	}
	
	@Deprecated
	public static ComparisonResult successEmptyOperand(String error) {
		return new ComparisonResult(true, true, error);
	}
	
	
	public static ComparisonResult failure(String error) {
		return new ComparisonResult(false, false, error);
	}
	
	@Deprecated
	public static ComparisonResult failureEmptyOperand(String error) {
		return new ComparisonResult(false, true, error);
	}
	
	public static ComparisonResult ofEmpty() {
		return new ComparisonResult(null, null, null);
	}
	
	public static ComparisonResult notComparable(String error) {
		return new ComparisonResult(null, null, error);
	}

	public static ComparisonResult of(Mapper<Boolean> result) {
        if (result.getMulti().stream().allMatch(Objects::isNull)) {
            return ofEmpty();
        }
        List<Boolean> filteredResults = result.getMulti().stream().filter(Objects::nonNull).collect(Collectors.toList());
        return new ComparisonResult(!filteredResults.isEmpty() && filteredResults.stream().allMatch(r -> r == true), false, null);
	}
	
	private ComparisonResult(Boolean result, Boolean emptyOperand, String error) {
		this.result = result;
		this.emptyOperand = emptyOperand;
		this.error = error;
	}
	
	@Override
	public Boolean get() {
		return getOrDefault(false);
	}
	
	@Override
	public Boolean getOrDefault(Boolean defaultValue) {
		return result == null ? defaultValue : result;
	}
	
	public String getError() {
		return error;
	}
	
	// and
	
	public boolean isEmptyOperand() {
		return result == null || (emptyOperand != null && emptyOperand == true);
	}
	
	public ComparisonResult and(ComparisonResult other) {
		return and(this, other);
	}
	
	public ComparisonResult andIgnoreEmptyOperand(ComparisonResult other) {
		return combineIgnoreEmptyOperand(other, this::and);
	}
	
	private ComparisonResult and(ComparisonResult r1, ComparisonResult r2) {
		boolean newResult = r1.get() && r2.get();
		String newError = "";
		if (!r1.get()) {
			newError+=r1.error;
		}
		if (!r2.get()) {
			if (!r1.get()) {
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
		boolean newResult = r1.get() || r2.get();
		String newError = "";
		newError+=r1.error;
		newError+=" and ";
		newError+=r2.error;
		return new ComparisonResult(newResult, false, newResult?null:newError);
	}
	
	// utils
	
	private ComparisonResult combineIgnoreEmptyOperand(ComparisonResult other, BinaryOperator<ComparisonResult> combineFunc) {
		if(this.isEmptyOperand() && other.isEmptyOperand()) {
			return ComparisonResult.ofEmpty();
		}
		if(this.isEmptyOperand()) {
			return other;
		}
		if(other.isEmptyOperand()) {
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
		return isEmptyOperand() ? 0 : 1;
	}

	public MapperS<Boolean> asMapper() {
		return isEmptyOperand() ? MapperS.ofNull() : MapperS.of(this.get());
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