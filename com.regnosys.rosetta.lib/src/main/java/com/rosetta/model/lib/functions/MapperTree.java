package com.rosetta.model.lib.functions;

public class MapperTree<T> {
	public enum Operator { AND, OR }
	
	private final Mapper<T> data;
	private final Operator operator;
	private final MapperTree<T> left;
	private final MapperTree<T> right;
	
	public static <T> MapperTree<T> of(Mapper<T> data) {
		return new MapperTree<>(data);
	}
	
	public static <T> MapperTree<T> and(MapperTree<T> left, MapperTree<T> right) {
		return new MapperTree<>(Operator.AND, left, right);
	}
	
	public static <T> MapperTree<T> or(MapperTree<T> left, MapperTree<T> right) {
		return new MapperTree<>(Operator.OR, left, right);
	}
	
	@SuppressWarnings("unchecked")
	public static MapperTree<Object> andDifferent(MapperTree<?> left, MapperTree<?> right) {
		return new MapperTree<>(Operator.AND, (MapperTree<Object>) left, (MapperTree<Object>) right);
	}
	
	@SuppressWarnings("unchecked")
	public static MapperTree<Object> orDifferent(MapperTree<?> left, MapperTree<?> right) {
		return new MapperTree<>(Operator.OR, (MapperTree<Object>) left, (MapperTree<Object>) right);
	}
	
	private MapperTree(Mapper<T> data) {
		this.operator = null;
		this.left = null;
		this.right = null;
		this.data = data;
	}
	
	private MapperTree(Operator operator, MapperTree<T> left, MapperTree<T> right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
		this.data = null;
	}
	
	public Mapper<T> getData() {
		return data;
	}

	public Operator getOperator() {
		return operator;
	}

	public MapperTree<T> getLeft() {
		return left;
	}

	public MapperTree<T> getRight() {
		return right;
	}

	public boolean isLeaf() {
		return operator == null;
	}
	
	@Override
	public String toString() {
		return data == null ? "[" + left + " " + operator + " " + right + "]" : "[" + data + "]";
	}
}