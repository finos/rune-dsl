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

package com.rosetta.model.lib.reports;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

import com.rosetta.model.lib.ModelSymbolId;

//Deprecated since 9.43.0
@Deprecated
public interface Tabulator<T> {

	default List<Field> getFields() {
		return Arrays.asList();
	}
	List<FieldValue> tabulate(T report);
	
	@Deprecated
	public interface Field {
		String getName();
		String getAttributeName();
		List<Field> getChildren();
		boolean isMulti();
	}
	@Deprecated
	public interface FieldValue {
		Field getField();
		Optional<? extends Object> getValue();
		
		default boolean isPresent() {
			return getValue().isPresent();
		}
		
		default <C> void accept(FieldValueVisitor<C> visitor, C context) {
			visitor.visitSingle(this, context);
		}
	}
	@Deprecated
	public interface NestedFieldValue extends FieldValue {
		Optional<? extends List<? extends FieldValue>> getValue();
		
		default <C> void accept(FieldValueVisitor<C> visitor, C context) {
			visitor.visitNested(this, context);
		}
	}
	@Deprecated
	public interface MultiNestedFieldValue extends FieldValue {
		Optional<? extends List<? extends List<? extends FieldValue>>> getValue();
		
		default <C> void accept(FieldValueVisitor<C> visitor, C context) {
			visitor.visitMultiNested(this, context);
		}
	}
	@Deprecated
	public interface FieldValueVisitor<C> {
		void visitSingle(FieldValue fieldValue, C context);
		void visitNested(NestedFieldValue fieldValue, C context);
		void visitMultiNested(MultiNestedFieldValue fieldValue, C context);
	}
	@Deprecated
	public static class FieldImpl implements Field {
		private final String attributeName;
		private final boolean isMulti;
		private final Optional<ModelSymbolId> ruleId;
		private final Optional<String> identifier;
		private final List<Field> children;
		
		public FieldImpl(String attributeName, boolean isMulti, Optional<ModelSymbolId> ruleId, Optional<String> identifier, List<Field> children) {
			Objects.requireNonNull(ruleId);
			Objects.requireNonNull(attributeName);
			Objects.requireNonNull(identifier);
			Validate.noNullElements(children);
			this.ruleId = ruleId;
			this.attributeName = attributeName;
			this.isMulti = isMulti;
			this.identifier = identifier;
			this.children = Collections.unmodifiableList(children);
		}
		@Override
		public String getName() {
			return identifier.orElse(attributeName);
		}
		@Override
		public String getAttributeName() {
			return attributeName;
		}
		@Override
		public boolean isMulti() {
			return isMulti;
		}
		public Optional<ModelSymbolId> getRuleId() {
			return ruleId;
		}
		@Override
		public List<Field> getChildren() {
			return children;
		}
		@Override
		public int hashCode() {
			return Objects.hash(attributeName, children, identifier, isMulti, ruleId);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldImpl other = (FieldImpl) obj;
			return Objects.equals(attributeName, other.attributeName) && Objects.equals(children, other.children)
					&& Objects.equals(identifier, other.identifier) && isMulti == other.isMulti
					&& Objects.equals(ruleId, other.ruleId);
		}
	}
	public static class FieldValueImpl implements FieldValue {
		private Field field;
		private Optional<? extends Object> value;
		
		public FieldValueImpl(Field field, Optional<? extends Object> value) {
			Objects.requireNonNull(field);
			Objects.requireNonNull(value);
			this.field = field;
			this.value = value;
		}
		@Override
		public Field getField() {
			return field;
		}
		@Override
		public Optional<? extends Object> getValue() {
			return value;
		}
		@Override
		public String toString() {
			return String.format("<%s, %s>", field.getName(), value.map(Object::toString).orElse("<empty>"));
		}
		@Override
		public int hashCode() {
			return Objects.hash(field, value);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldValueImpl other = (FieldValueImpl) obj;
			return Objects.equals(field, other.field) && Objects.equals(value, other.value);
		}
	}
	public static class NestedFieldValueImpl implements NestedFieldValue {
		private Field field;
		private Optional<? extends List<? extends FieldValue>> value;
		
		public NestedFieldValueImpl(Field field, Optional<? extends List<? extends FieldValue>> value) {
			Objects.requireNonNull(field);
			Objects.requireNonNull(value);
			value.ifPresent(vs -> {
				Validate.noNullElements(vs);
			});
			this.field = field;
			this.value = value;
		}
		@Override
		public Field getField() {
			return field;
		}
		@Override
		public Optional<? extends List<? extends FieldValue>> getValue() {
			return value;
		}
		@Override
		public String toString() {
			String valueRepr = value
					.map(vs -> vs.stream()
							.map(Object::toString)
							.collect(Collectors.joining(", ", "{", "}")))
					.orElse("<empty>");
			return "<" + field.getName() + ", " + valueRepr + ">";
		}
		@Override
		public int hashCode() {
			return Objects.hash(field, value);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NestedFieldValueImpl other = (NestedFieldValueImpl) obj;
			return Objects.equals(field, other.field) && Objects.equals(value, other.value);
		}
	}
	@Deprecated
	public static class MultiNestedFieldValueImpl implements MultiNestedFieldValue {
		private Field field;
		private Optional<? extends List<? extends List<? extends FieldValue>>> value;
		
		public MultiNestedFieldValueImpl(Field field, Optional<? extends List<? extends List<? extends FieldValue>>> value) {
			Objects.requireNonNull(field);
			Objects.requireNonNull(value);
			value.ifPresent(vs -> {
				Validate.noNullElements(vs);
				vs.forEach(v -> Validate.noNullElements(v));
			});
			this.field = field;
			this.value = value;
		}
		@Override
		public Field getField() {
			return field;
		}
		@Override
		public Optional<? extends List<? extends List<? extends FieldValue>>> getValue() {
			return value;
		}
		@Override
		public String toString() {
			String valueRepr = value
					.map(vs -> vs.stream()
							.map(v -> v.stream()
									.map(Object::toString)
									.collect(Collectors.joining(", ", "{", "}")))
							.collect(Collectors.joining(", ", "[", "]")))
					.orElse("<empty>");
			return "<" + field.getName() + ", " + valueRepr + ">";
		}
		@Override
		public int hashCode() {
			return Objects.hash(field, value);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MultiNestedFieldValueImpl other = (MultiNestedFieldValueImpl) obj;
			return Objects.equals(field, other.field) && Objects.equals(value, other.value);
		}
	}
}
