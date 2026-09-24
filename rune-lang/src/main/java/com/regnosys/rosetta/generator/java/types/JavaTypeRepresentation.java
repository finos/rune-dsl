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

package com.regnosys.rosetta.generator.java.types;

import java.util.Objects;

import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.codegen.api.TargetLanguageRepresentation;
import com.rosetta.util.types.JavaArrayType;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaTypeArgument;
import com.rosetta.util.types.JavaTypeArgumentVisitor;
import com.rosetta.util.types.JavaTypeVariable;
import com.rosetta.util.types.JavaTypeVisitor;
import com.rosetta.util.types.JavaWildcardTypeArgument;
import com.rosetta.util.types.JavaType;

public class JavaTypeRepresentation implements TargetLanguageRepresentation {
	private JavaType type;
	
	public JavaTypeRepresentation(JavaType type) {
		Objects.requireNonNull(type);
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Repr[" + type.toString() + "]";
	}
	
	@Override
	public void render(CodeWriter out) {
		type.accept(new WriterVisitor(out));
	}
	
	@Override
	public int hashCode() {
		return type.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaTypeRepresentation other = (JavaTypeRepresentation) object;
        return Objects.equals(type, other.type);
	}

	private static class WriterVisitor implements JavaTypeVisitor, JavaTypeArgumentVisitor {
		private final CodeWriter out;

		public WriterVisitor(CodeWriter out) {
			this.out = out;
		}

		@Override
		public void visitType(JavaArrayType type) {
			type.getBaseType().accept(this);
			out.write("[]");
		}

		@Override
		public void visitType(JavaClass<?> type) {
			out.write(type);
		}

		@Override
		public void visitType(JavaParameterizedType<?> type) {
			type.getGenericTypeDeclaration().getBaseType().accept((JavaTypeVisitor)this);
			out.write("<");
			out.join(type.getArguments(), ", ", arg -> arg.accept(this));
			out.write(">");
		}

		@Override
		public void visitType(JavaPrimitiveType type) {
			out.write(type.getSimpleName());
		}

		@Override
		public void visitType(JavaTypeVariable type) {
			out.write(type.getName());
		}

		@Override
		public void visitNullType() {
			// The null type has no representation - this will throw.
			out.write(JavaReferenceType.NULL_TYPE.getSimpleName());
		}

		@Override
		public void visitTypeArgument(JavaWildcardTypeArgument arg) {
			out.write("?");
			if (arg.hasExtendsBound()) {
				out.write(" extends ");
				arg.getBound().get().accept((JavaTypeVisitor)this);
			}
			if (arg.hasSuperBound()) {
				out.write(" super ");
				arg.getBound().get().accept((JavaTypeVisitor)this);
			}
		}

		@Override
		public void visitTypeArgument(JavaReferenceType arg) {
			arg.accept((JavaTypeVisitor)this);
		}
	}
}
