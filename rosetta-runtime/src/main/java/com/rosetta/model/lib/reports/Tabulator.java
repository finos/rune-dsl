package com.rosetta.model.lib.reports;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;

public interface Tabulator<T> {

	List<Field> getFields();
	List<FieldValue> tabulate(T report);
	
	public interface Field {
		String getName();
		String getAttributeName();
		List<Field> getChildren();
		boolean isMulti();
	}
	public interface FieldValue {
		Field getField();
		Optional<? extends Object> getValue();
		
		default boolean isPresent() {
			return getValue().isPresent();
		}
		
		default void accept(FieldValueVisitor visitor) {
			visitor.visitSingle(this);
		}
	}
	public interface NestedFieldValue extends FieldValue {
		Optional<? extends List<? extends FieldValue>> getValue();
		
		default void accept(FieldValueVisitor visitor) {
			visitor.visitNested(this);
		}
	}
	public interface MultiNestedFieldValue extends FieldValue {
		Optional<? extends List<? extends List<? extends FieldValue>>> getValue();
		
		default void accept(FieldValueVisitor visitor) {
			visitor.visitMultiNested(this);
		}
	}
	public interface FieldValueVisitor {
		void visitSingle(FieldValue fieldValue);
		void visitNested(NestedFieldValue fieldValue);
		void visitMultiNested(MultiNestedFieldValue fieldValue);
	}
	
	public static class FieldImpl implements Field {
		private String attributeName;
		private boolean isMulti;
		private Optional<DottedPath> ruleName; // TODO: ModelSymbolId
		private Optional<String> identifier;
		private List<Field> children;
		
		public FieldImpl(String attributeName, boolean isMulti, Optional<DottedPath> ruleName, Optional<String> identifier, List<Field> children) {
			Objects.requireNonNull(ruleName);
			Objects.requireNonNull(attributeName);
			Objects.requireNonNull(identifier);
			Validate.noNullElements(children);
			this.ruleName = ruleName;
			this.attributeName = attributeName;
			this.isMulti = isMulti;
			this.identifier = identifier;
			this.children = children;
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
		
		public Optional<DottedPath> getRuleName() {
			return ruleName;
		}

		@Override
		public List<Field> getChildren() {
			return children;
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
			return "<" + field.getName() + ", " + value.map(Object::toString).orElse("<empty>") + ">";
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
	}
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
	}
}
