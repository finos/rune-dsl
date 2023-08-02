package com.rosetta.model.lib.reports;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

public interface Tabulator<T> {

	List<Field> getFields();
	List<FieldValue<?>> tabulate(T report);
	
	public static class Field {
		private String name;
		
		public Field(String name) {
			Validate.notNull(name);
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public <V> FieldValue<V> createValue(V value) {
			return new FieldValue<>(this, Optional.of(value));
		}
		public <V> FieldValue<V> createEmptyValue() {
			return new FieldValue<>(this, Optional.empty());
		}
	}
	public static class FieldValue<V> {
		private Field field;
		private Optional<V> optionalValue;
		
		public FieldValue(Field field, Optional<V> optionalValue) {
			Validate.notNull(field);
			Validate.notNull(optionalValue);
			this.field = field;
			this.optionalValue = optionalValue;
		}
		
		public Field getField() {
			return field;
		}
		public Optional<V> getOptionalValue() {
			return optionalValue;
		}
		
		public boolean isPresent() {
			return optionalValue.isPresent();
		}
		public boolean isEmpty() {
			return !isPresent();
		}
	}
}
