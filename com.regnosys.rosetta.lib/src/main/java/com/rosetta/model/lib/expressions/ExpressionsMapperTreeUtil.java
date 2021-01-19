package com.rosetta.model.lib.expressions;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperTree;
import java.util.function.BiFunction;
import java.util.function.Function;

class ExpressionsMapperTreeUtil {

	static <T> ComparisonResult evaluateTree(MapperTree<T> t, Function<Mapper<T>, ComparisonResult> func) {
		if(!t.isLeaf()) {
			ComparisonResult left = evaluateTree(t.getLeft(), func);
			ComparisonResult right = evaluateTree(t.getRight(), func);
			return evaluate(t.getOperator(), left, right);
		}
		return func.apply(t.getData());
	}
	
	static <T> ComparisonResult evaluateTree(MapperTree<T> t, boolean only, BiFunction<Mapper<T>, Boolean, ComparisonResult> func) {
		if(!t.isLeaf()) {
			ComparisonResult left = evaluateTree(t.getLeft(), only, func);
			ComparisonResult right = evaluateTree(t.getRight(), only, func);
			return evaluate(t.getOperator(), left, right);
		}
		return func.apply(t.getData(), only);
	}
	
	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult evaluateTrees(MapperTree<T> t1, MapperTree<X> t2, BiFunction<Mapper<T>, Mapper<X>, ComparisonResult> comparisonFunction) {
		if(!t1.isLeaf()) {
			ComparisonResult left = evaluateTrees(t1.getLeft(), t2, comparisonFunction);
			ComparisonResult right = evaluateTrees(t1.getRight(), t2, comparisonFunction);
			return evaluateIgnoreEmptyOperand(t1.getOperator(), left, right);
		}
		else
			return evaluateTrees(t1.getData(), t2, comparisonFunction);
	}
	
	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult evaluateTrees(Mapper<T> m1, MapperTree<X> t2, BiFunction<Mapper<T>, Mapper<X>, ComparisonResult> comparisonFunction) {
		if(!t2.isLeaf()) {
			ComparisonResult left = evaluateTrees(m1, t2.getLeft(), comparisonFunction);
			ComparisonResult right = evaluateTrees(m1, t2.getRight(), comparisonFunction);
			return evaluateIgnoreEmptyOperand(t2.getOperator(), left, right);
		}
		else
			return comparisonFunction.apply(m1, t2.getData());
	}
	
	static ComparisonResult evaluate(MapperTree.Operator op, ComparisonResult left, ComparisonResult right) {
		switch(op) {
		case AND:
			return left.and(right);
		case OR:
			return left.or(right);
		default:
			throw new IllegalArgumentException("Cannot evaluate ComparisonResult, unexpected operator " + op);	
		}
	}
	
	static ComparisonResult evaluateIgnoreEmptyOperand(MapperTree.Operator op, ComparisonResult left, ComparisonResult right) {
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
