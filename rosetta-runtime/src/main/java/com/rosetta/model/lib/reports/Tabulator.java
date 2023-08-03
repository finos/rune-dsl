package com.rosetta.model.lib.reports;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;

public interface Tabulator<T> {

	List<Field> getFields();
	List<FieldValue<?>> tabulate(T report);
	
	public interface Field {
		String getName();
		DottedPath getRosettaPath();
		Optional<Field> getParent();
	}
	public interface FieldValue<T> {
		Field getField();
		Optional<T> getValue();
		
		default boolean isPresent() {
			return getValue().isPresent();
		}
		
		default void accept(FieldValueVisitor visitor) {
			visitor.visitSingle(this);
		}
	}
	public interface FieldListValue<T> extends FieldValue<List<Optional<T>>> {
		default void accept(FieldValueVisitor visitor) {
			visitor.visitList(this);
		}
	}
	public interface FieldValueVisitor {
		void visitSingle(FieldValue<?> fieldValue);
		void visitList(FieldListValue<?> fieldValue);
	}
	
	public static class FieldImpl implements Field {
		private DottedPath rosettaPath;
		private Optional<DottedPath> ruleName;
		private Optional<String> identifier;
		private Optional<Field> parent;
		
		public FieldImpl(DottedPath rosettaPath, Optional<DottedPath> ruleName, Optional<String> identifier, Optional<Field> parent) {
			Validate.notNull(ruleName);
			Validate.notNull(rosettaPath);
			Validate.notNull(identifier);
			Validate.notNull(parent);
			this.ruleName = ruleName;
			this.rosettaPath = rosettaPath;
			this.identifier = identifier;
			this.parent = parent;
		}
		
		@Override
		public String getName() {
			return identifier.orElse(rosettaPath.withSeparator("->"));
		}

		@Override
		public DottedPath getRosettaPath() {
			return rosettaPath;
		}
		
		public Optional<DottedPath> getRuleName() {
			return ruleName;
		}

		@Override
		public Optional<Field> getParent() {
			return parent;
		}
	}
	public static class FieldValueImpl<T> implements FieldValue<T> {
		private Field field;
		private Optional<T> value;
		
		public FieldValueImpl(Field field, Optional<T> value) {
			Validate.notNull(field);
			Validate.notNull(value);
			this.field = field;
			this.value = value;
		}
		
		@Override
		public Field getField() {
			return field;
		}
		@Override
		public Optional<T> getValue() {
			return value;
		}
	}
	public static class FieldListValueImpl<T> implements FieldListValue<T> {
		private Field field;
		private Optional<List<Optional<T>>> value;
		
		public FieldListValueImpl(Field field, Optional<List<Optional<T>>> value) {
			Validate.notNull(field);
			Validate.notNull(value);
			this.field = field;
			this.value = value;
		}
		
		@Override
		public Field getField() {
			return field;
		}
		@Override
		public Optional<List<Optional<T>>> getValue() {
			return value;
		}
	}
}
