package com.regnosys.rosetta.blueprints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class DataItemReportUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataItemReportUtils.class);
	
	public static <T> void setField(Consumer<T> setter, Class<T> dataType, Object data, Class<?> ruleType) {
		if (dataType.isInstance(data)) {
			setter.accept(dataType.cast(data));
		} else {
			LOGGER.warn(String.format("Failed to set report field for rule %s. Expected type %s but found %s.",
					ruleType.getName(), dataType.getName(), data.getClass().getName()));
		}
	}
	
	public static <T> void setListField(Consumer<List<? extends T>> setter, Class<T> dataType, Object data, Class<?> ruleType) {
		if (data instanceof List<?>) {
			List<? extends T> validItems = ((List<?>)data).stream()
				.filter((item) -> {
					if (dataType.isInstance(item)) {
						return true;
					}
					LOGGER.warn(String.format("Failed to add report item for rule %s. Expected type %s but found %s.",
							ruleType.getName(), dataType.getName(), item.getClass().getName()));
					return false;
				})
				.map((item) -> dataType.cast(item))
				.collect(Collectors.toList());
			setter.accept(validItems);
		} else {
			LOGGER.warn(String.format("Failed to set report field for rule %s. Expected a list but found %s.",
					ruleType.getName(), data.getClass().getName()));
		}
	}
}
