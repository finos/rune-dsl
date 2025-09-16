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

import java.util.List;
import java.util.Objects;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.TargetLanguageRepresentation;
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
	public void appendTo(TargetStringConcatenation target) {
		type.accept(new ConcatenationVisitor(target));
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

	private static class ConcatenationVisitor implements JavaTypeVisitor, JavaTypeArgumentVisitor {
		private TargetStringConcatenation target;

		public ConcatenationVisitor(TargetStringConcatenation target) {
			this.target = target;
		}
		
		@Override
		public void visitType(JavaArrayType type) {
			type.getBaseType().accept(this);
			target.append("[]");
		}

		@Override
		public void visitType(JavaClass<?> type) {
			target.append(type);
		}

		@Override
		public void visitType(JavaParameterizedType<?> type) {
			type.getGenericTypeDeclaration().getBaseType().accept((JavaTypeVisitor)this);
			target.append("<");
			List<JavaTypeArgument> arguments = type.getArguments();
			if (!arguments.isEmpty()) {
				arguments.get(0).accept(this);
				for (int i=1; i<arguments.size(); i++) {
					target.append(", ");
					arguments.get(i).accept(this);
				}
			}
			target.append(">");
		}

		@Override
		public void visitType(JavaPrimitiveType type) {
			target.append(type.getSimpleName());
		}

		@Override
		public void visitType(JavaTypeVariable type) {
			target.append(type.getName());
		}
		
		@Override
		public void visitNullType() {
			// The null type has no representation - this will throw.
			target.append(JavaReferenceType.NULL_TYPE.getSimpleName());
		}

		@Override
		public void visitTypeArgument(JavaWildcardTypeArgument arg) {
			target.append("?");
			if (arg.hasExtendsBound()) {
				target.append(" extends ");
				arg.getBound().get().accept((JavaTypeVisitor)this);
			}
			if (arg.hasSuperBound()) {
				target.append(" super ");
				arg.getBound().get().accept((JavaTypeVisitor)this);
			}
		}

		@Override
		public void visitTypeArgument(JavaReferenceType arg) {
			arg.accept((JavaTypeVisitor)this);
		}
	}
}
