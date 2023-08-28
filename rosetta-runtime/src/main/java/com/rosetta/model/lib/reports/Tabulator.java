package com.rosetta.model.lib.reports;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.rosetta.model.lib.ModelSymbolId;

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
		
		default <C> void accept(FieldValueVisitor<C> visitor, C context) {
			visitor.visitSingle(this, context);
		}
	}
	public interface NestedFieldValue extends FieldValue {
		Optional<? extends List<? extends FieldValue>> getValue();
		
		default <C> void accept(FieldValueVisitor<C> visitor, C context) {
			visitor.visitNested(this, context);
		}
	}
	public interface MultiNestedFieldValue extends FieldValue {
		Optional<? extends List<? extends List<? extends FieldValue>>> getValue();
		
		default <C> void accept(FieldValueVisitor<C> visitor, C context) {
			visitor.visitMultiNested(this, context);
		}
	}
	public interface FieldValueVisitor<C> {
		void visitSingle(FieldValue fieldValue, C context);
		void visitNested(NestedFieldValue fieldValue, C context);
		void visitMultiNested(MultiNestedFieldValue fieldValue, C context);
	}
	
	public static class FieldImpl implements Field {
		private String attributeName;
		private boolean isMulti;
		private Optional<ModelSymbolId> ruleId;
		private Optional<String> identifier;
		private List<Field> children;
		
		public FieldImpl(String attributeName, boolean isMulti, Optional<ModelSymbolId> ruleId, Optional<String> identifier, List<Field> children) {
			Objects.requireNonNull(ruleId);
			Objects.requireNonNull(attributeName);
			Objects.requireNonNull(identifier);
			Validate.noNullElements(children);
			this.ruleId = ruleId;
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
		
		public Optional<ModelSymbolId> getRuleId() {
			return ruleId;
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
