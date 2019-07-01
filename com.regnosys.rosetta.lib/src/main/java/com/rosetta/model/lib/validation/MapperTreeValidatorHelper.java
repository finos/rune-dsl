package com.rosetta.model.lib.validation;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.rosetta.model.lib.functions.Mapper;
import com.rosetta.model.lib.functions.MapperTree;

public class MapperTreeValidatorHelper {

	public static <T> ComparisonResult notExists(MapperTree<T> t) {
		return evaluateTree(t, ValidatorHelper::notExists);
	}
	
	public static <T> ComparisonResult exists(MapperTree<T> t, boolean only) {
		return evaluateTree(t, only, ValidatorHelper::exists);
	}
	
	public static <T> ComparisonResult singleExists(MapperTree<T> t, boolean only) {
		return evaluateTree(t, only, ValidatorHelper::singleExists);
	}
	
	public static <T> ComparisonResult multipleExists(MapperTree<T> t, boolean only) {
		return evaluateTree(t, only, ValidatorHelper::multipleExists);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult notEqual(MapperTree<T> t1, MapperTree<T> t2) {
		return evaluateTrees(t1, t2, ValidatorHelper::notEqual);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult areEqual(MapperTree<T> t1, MapperTree<T> t2) {
		return evaluateTrees(t1, t2, ValidatorHelper::areEqual);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult notEqual(ComparisonResult r1, ComparisonResult r2) {
		return r1.get() != r2.get() ? ComparisonResult.success() : ComparisonResult.failure("Results are not equal");
	}
	
	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThan(MapperTree<T> t1, MapperTree<X> t2) {
		return evaluateTrees(t1, t2, ValidatorHelper::lessThan);
	}
	
	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThanEquals(MapperTree<T> t1, MapperTree<X> t2) {
		return evaluateTrees(t1, t2, ValidatorHelper::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThan(MapperTree<T> t1, MapperTree<X> t2) {
		return evaluateTrees(t1, t2, ValidatorHelper::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThanEquals(MapperTree<T> t1, MapperTree<X> t2) {
		return evaluateTrees(t1, t2, ValidatorHelper::greaterThanEquals);
	}
	
	// Util methods
	
	private static <T> ComparisonResult evaluateTree(MapperTree<T> t, Function<Mapper<T>, ComparisonResult> func) {
		if(!t.isLeaf()) {
			ComparisonResult left = evaluateTree(t.getLeft(), func);
			ComparisonResult right = evaluateTree(t.getRight(), func);
			return evaluate(t.getOperator(), left, right);
		}
		return func.apply(t.getData());
	}
	
	private static <T> ComparisonResult evaluateTree(MapperTree<T> t, boolean only, BiFunction<Mapper<T>, Boolean, ComparisonResult> func) {
		if(!t.isLeaf()) {
			ComparisonResult left = evaluateTree(t.getLeft(), only, func);
			ComparisonResult right = evaluateTree(t.getRight(), only, func);
			return evaluate(t.getOperator(), left, right);
		}
		return func.apply(t.getData(), only);
	}
	
	private static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult evaluateTrees(MapperTree<T> t1, MapperTree<X> t2, BiFunction<Mapper<T>, Mapper<X>, ComparisonResult> comparisonFunction) {
		if(!t1.isLeaf()) {
			ComparisonResult left = evaluateTrees(t1.getLeft(), t2, comparisonFunction);
			ComparisonResult right = evaluateTrees(t1.getRight(), t2, comparisonFunction);
			return evaluateIgnoreEmptyOperand(t1.getOperator(), left, right);
		}
		else
			return evaluateTrees(t1.getData(), t2, comparisonFunction);
	}
	
	private static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult evaluateTrees(Mapper<T> m1, MapperTree<X> t2, BiFunction<Mapper<T>, Mapper<X>, ComparisonResult> comparisonFunction) {
		if(!t2.isLeaf()) {
			ComparisonResult left = evaluateTrees(m1, t2.getLeft(), comparisonFunction);
			ComparisonResult right = evaluateTrees(m1, t2.getRight(), comparisonFunction);
			return evaluateIgnoreEmptyOperand(t2.getOperator(), left, right);
		}
		else
			return comparisonFunction.apply(m1, t2.getData());
	}
	
	private static ComparisonResult evaluate(MapperTree.Operator op, ComparisonResult left, ComparisonResult right) {
		switch(op) {
		case AND:
			return left.and(right);
		case OR:
			return left.or(right);
		default:
			throw new IllegalArgumentException("Cannot evaluate ComparisonResult, unexpected operator " + op);	
		}
	}
	
	private static ComparisonResult evaluateIgnoreEmptyOperand(MapperTree.Operator op, ComparisonResult left, ComparisonResult right) {
		switch(op) {
		case AND:
			return left.andIgnoreEmptyOperand(right);
		case OR:
			return left.orIgnoreEmptyOperand(right);
		default:
			throw new IllegalArgumentException("Cannot evaluate ComparisonResult, unexpected operator " + op);	
		}
	}
}
