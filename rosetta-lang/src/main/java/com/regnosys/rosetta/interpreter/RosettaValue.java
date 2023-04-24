package com.regnosys.rosetta.interpreter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

public class RosettaValue {
	private List<RosettaValueItem> items;
	
	public RosettaValue(List<RosettaValueItem> items) {
		Validate.notNull(items);
		this.items = items;
	}
	
	public static RosettaValue empty() {
		return new RosettaValue(Collections.emptyList());
	}
	public static RosettaValue of(RosettaValueItem item) {
		return new RosettaValue(List.of(item));
	}
	public static RosettaValue of(boolean item) {
		return of(new RosettaBooleanItem(item));
	}
	public static RosettaValue of(RosettaNumber item) {
		return of(new RosettaNumberItem(item));
	}
	public static RosettaValue of(String item) {
		return of(new RosettaStringItem(item));
	}
	public static RosettaValue of(LocalDateTime item) {
		return of(new RosettaDateTimeItem(item));
	}
	public static RosettaValue of(Pattern item) {
		return of(new RosettaPatternItem(item));
	}

	public List<RosettaValueItem> getItems() {
		return items;
	}
	
	public int size() {
		return items.size();
	}
	public Stream<RosettaValueItem> stream() {
		return items.stream();
	}
	
	public List<Boolean> getItemsAsBoolean() {
		if (items.size() == 0) {
			return Collections.emptyList();
		}
		if (items.get(0) instanceof RosettaBooleanItem) {
			return items.stream()
					.map(item -> ((RosettaBooleanItem)item).getValue())
					.collect(Collectors.toList());
		}
		throw new RosettaInterpreterTypeException("Expected items of type boolean, but got " + this + ".");
	}
	public List<RosettaNumber> getItemsAsNumber() {
		if (items.size() == 0) {
			return Collections.emptyList();
		}
		if (items.get(0) instanceof RosettaNumber) {
			return items.stream()
					.map(item -> ((RosettaNumberItem)item).getValue())
					.collect(Collectors.toList());
		}
		throw new RosettaInterpreterTypeException("Expected items of type number, but got " + this + ".");
	}
	public List<String> getItemsAsString() {
		if (items.size() == 0) {
			return Collections.emptyList();
		}
		if (items.get(0) instanceof RosettaStringItem) {
			return items.stream()
					.map(item -> ((RosettaStringItem)item).getValue())
					.collect(Collectors.toList());
		}
		throw new RosettaInterpreterTypeException("Expected items of type string, but got " + this + ".");
	}
	public <U extends RosettaValueItem> List<U> getItemsOfType(Class<U> type) {
		if (items.size() == 0) {
			return Collections.emptyList();
		}
		if (type.isInstance(items.get(0))) {
			return items.stream()
					.map(item -> type.cast(item))
					.collect(Collectors.toList());
		}
		throw new RosettaInterpreterTypeException("Expected items of type " + type.getSimpleName() + ", but got " + this + ".");
	}
	
	public Optional<RosettaValueItem> getSingle() {
		if (items.size() == 1) {
			return Optional.of(items.get(0));
		}
		return Optional.empty();
	}
	public RosettaValueItem getSingleOrThrow() {
		return getSingle().orElseThrow(() -> new RosettaInterpreterTypeException("Expected a single item, but got " + this + "."));
	}
	public <U> Optional<U> getSingleOfType(Class<U> clazz) {
		return getSingle().map(RosettaValueItem::getValue)
				.filter(v -> clazz.isInstance(v))
				.map(v -> clazz.cast(v));
	}
	public <U> U getSingleOfTypeOrThrow(Class<U> clazz) {
		Object v = getSingleOrThrow().getValue();
		if (clazz.isInstance(v)) {
			return clazz.cast(v);
		}
		throw new RosettaInterpreterTypeException("Expected a single item of type " + clazz.getSimpleName() + ", but got " + this + ".");
	}
	public Optional<Boolean> getSingleBoolean() {
		return getSingleOfType(Boolean.class);
	}
	public boolean getSingleBooleanOrThrow() {
		return getSingleOfTypeOrThrow(Boolean.class);
	}
	public Optional<RosettaNumber> getSingleNumber() {
		return getSingleOfType(RosettaNumber.class);
	}
	public RosettaNumber getSingleNumberOrThrow() {
		return getSingleOfTypeOrThrow(RosettaNumber.class);
	}
	public Optional<Integer> getSingleInteger() {
		return getSingleNumber().map(v -> v.intValue());
	}
	public int getSingleIntegerOrThrow() {
		return getSingleNumberOrThrow().intValue();
	}
	public Optional<String> getSingleString() {
		return getSingleOfType(String.class);
	}
	public String getSingleStringOrThrow() {
		return getSingleOfTypeOrThrow(String.class);
	}
	public Optional<LocalDate> getSingleDate() {
		return getSingleOfType(LocalDate.class);
	}
	public LocalDate getSingleDateOrThrow() {
		return getSingleOfTypeOrThrow(LocalDate.class);
	}
	public Optional<LocalTime> getSingleTime() {
		return getSingleOfType(LocalTime.class);
	}
	public LocalTime getSingleTimeOrThrow() {
		return getSingleOfTypeOrThrow(LocalTime.class);
	}
	public Optional<RosettaValueItemWithNaturalOrder<?>> getSingleWithNaturalOrder() {
		return getSingle().map(RosettaValueItem::getValue)
				.filter(v -> v instanceof RosettaValueItemWithNaturalOrder)
				.map(v -> (RosettaValueItemWithNaturalOrder<?>)v);
	}
	public RosettaValueItemWithNaturalOrder<?> getSingleWithNaturalOrderOrThrow() {
		Object v = getSingleOrThrow().getValue();
		if (v instanceof RosettaValueItemWithNaturalOrder) {
			return (RosettaValueItemWithNaturalOrder<?>)v;
		}
		throw new RosettaInterpreterTypeException("Expected a single item with a natural order, but got " + this + ".");
	}
	public Optional<Pattern> getSinglePattern() {
		return getSingleOfType(Pattern.class);
	}
	public Pattern getSinglePatternOrThrow() {
		return getSingleOfTypeOrThrow(Pattern.class);
	}
	
	@Override
	public String toString() {
		return "[" 
				+ items.stream().map(i -> i.toString()).collect(Collectors.joining(", "))
				+ "]";
	}
	
	@Override
	public int hashCode() {
		return items.hashCode();
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
        RosettaValue other = (RosettaValue)object;
        return Objects.equals(items, other.items);
	}
}
