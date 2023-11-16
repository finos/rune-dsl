package com.rosetta.util.types;

public abstract class BinaryCommunicativeJavaTypeVisitor<Result> {
	public Result visitTypes(JavaType left, JavaType right) {
		LeftVisitor leftVisitor = new LeftVisitor();
		left.accept(leftVisitor);
		right.accept(leftVisitor.nextVisitor);
		return leftVisitor.nextVisitor.result;
	}
	
	protected abstract Result visitTypes(JavaArrayType left, JavaArrayType right);
	protected abstract Result visitTypes(JavaArrayType left, JavaClass<?> right);
	protected abstract Result visitTypes(JavaArrayType left, JavaParameterizedType<?> right);
	protected abstract Result visitTypes(JavaArrayType left, JavaPrimitiveType right);
	protected abstract Result visitTypes(JavaArrayType left, JavaTypeVariable right);
	protected abstract Result visitTypeAndNull(JavaArrayType left);

	protected abstract Result visitTypes(JavaClass<?> left, JavaClass<?> right);
	protected abstract Result visitTypes(JavaClass<?> left, JavaParameterizedType<?> right);
	protected abstract Result visitTypes(JavaClass<?> left, JavaPrimitiveType right);
	protected abstract Result visitTypes(JavaClass<?> left, JavaTypeVariable right);
	protected abstract Result visitTypeAndNull(JavaClass<?> left);

	protected abstract Result visitTypes(JavaParameterizedType<?> left, JavaParameterizedType<?> right);
	protected abstract Result visitTypes(JavaParameterizedType<?> left, JavaPrimitiveType right);
	protected abstract Result visitTypes(JavaParameterizedType<?> left, JavaTypeVariable right);
	protected abstract Result visitTypeAndNull(JavaParameterizedType<?> left);

	protected abstract Result visitTypes(JavaPrimitiveType left, JavaPrimitiveType right);
	protected abstract Result visitTypes(JavaPrimitiveType left, JavaTypeVariable right);
	protected abstract Result visitTypeAndNull(JavaPrimitiveType left);

	protected abstract Result visitTypes(JavaTypeVariable left, JavaTypeVariable right);
	protected abstract Result visitTypeAndNull(JavaTypeVariable left);

	protected abstract Result visitBothNull();
	
	private class LeftVisitor implements JavaTypeVisitor {
		public RightVisitor nextVisitor;
		
		@Override
		public void visitType(JavaArrayType left) {
			this.nextVisitor = new LeftArrayTypeRightVisitor(left);
		}
		@Override
		public void visitType(JavaClass<?> left) {
			this.nextVisitor = new LeftClassRightVisitor(left);
		}
		@Override
		public void visitType(JavaParameterizedType<?> left) {
			this.nextVisitor = new LeftParameterizedTypeRightVisitor(left);
		}
		@Override
		public void visitType(JavaPrimitiveType left) {
			this.nextVisitor = new LeftPrimitiveTypeRightVisitor(left);
		}
		@Override
		public void visitType(JavaTypeVariable left) {
			this.nextVisitor = new LeftTypeVariableRightVisitor(left);
		}
		@Override
		public void visitNullType() {
			this.nextVisitor = new LeftNullTypeRightVisitor();
		}
	}
	private abstract class RightVisitor implements JavaTypeVisitor {
		public Result result;
	}
	private abstract class NonNullRightVisitor<Left extends JavaType> extends RightVisitor {
		protected final Left left;
		public NonNullRightVisitor(Left left) {
			this.left = left;
		}
	}
	private class LeftArrayTypeRightVisitor extends NonNullRightVisitor<JavaArrayType> {
		public LeftArrayTypeRightVisitor(JavaArrayType left) {
			super(left);
		}

		@Override
		public void visitType(JavaArrayType right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaClass<?> right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaParameterizedType<?> right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaPrimitiveType right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaTypeVariable right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitNullType() {
			this.result = visitTypeAndNull(left);
		}
	}
	private class LeftClassRightVisitor extends NonNullRightVisitor<JavaClass<?>> {
		public LeftClassRightVisitor(JavaClass<?> left) {
			super(left);
		}

		@Override
		public void visitType(JavaArrayType right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaClass<?> right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaParameterizedType<?> right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaPrimitiveType right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaTypeVariable right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitNullType() {
			this.result = visitTypeAndNull(left);
		}
	}
	private class LeftParameterizedTypeRightVisitor extends NonNullRightVisitor<JavaParameterizedType<?>> {
		public LeftParameterizedTypeRightVisitor(JavaParameterizedType<?> left) {
			super(left);
		}

		@Override
		public void visitType(JavaArrayType right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaClass<?> right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaParameterizedType<?> right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaPrimitiveType right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaTypeVariable right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitNullType() {
			this.result = visitTypeAndNull(left);
		}
	}
	private class LeftPrimitiveTypeRightVisitor extends NonNullRightVisitor<JavaPrimitiveType> {
		public LeftPrimitiveTypeRightVisitor(JavaPrimitiveType left) {
			super(left);
		}

		@Override
		public void visitType(JavaArrayType right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaClass<?> right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaParameterizedType<?> right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaPrimitiveType right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitType(JavaTypeVariable right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitNullType() {
			this.result = visitTypeAndNull(left);
		}
	}
	private class LeftTypeVariableRightVisitor extends NonNullRightVisitor<JavaTypeVariable> {
		public LeftTypeVariableRightVisitor(JavaTypeVariable left) {
			super(left);
		}

		@Override
		public void visitType(JavaArrayType right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaClass<?> right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaParameterizedType<?> right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaPrimitiveType right) {
			this.result = visitTypes(right, left);
		}
		@Override
		public void visitType(JavaTypeVariable right) {
			this.result = visitTypes(left, right);
		}
		@Override
		public void visitNullType() {
			this.result = visitTypeAndNull(left);
		}
	}
	private class LeftNullTypeRightVisitor extends RightVisitor {		
		@Override
		public void visitType(JavaArrayType right) {
			this.result = visitTypeAndNull(right);
		}
		@Override
		public void visitType(JavaClass<?> right) {
			this.result = visitTypeAndNull(right);
		}
		@Override
		public void visitType(JavaParameterizedType<?> right) {
			this.result = visitTypeAndNull(right);
		}
		@Override
		public void visitType(JavaPrimitiveType right) {
			this.result = visitTypeAndNull(right);
		}
		@Override
		public void visitType(JavaTypeVariable right) {
			this.result = visitTypeAndNull(right);
		}
		@Override
		public void visitNullType() {
			this.result = visitBothNull();
		}
	}
}
