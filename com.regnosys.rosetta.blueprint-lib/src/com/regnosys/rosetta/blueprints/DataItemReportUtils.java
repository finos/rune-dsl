package com.regnosys.rosetta.blueprints;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

public class DataItemReportUtils {
	
	private static final Logger LOGGER = Logger.getLogger(DataItemReportUtils.class);
	
	public static <T> void setField(Consumer<T> setter, Class<T> dataType, Object data, Class<?> ruleType) {
		if (dataType.isInstance(data)) {
			setter.accept(dataType.cast(data));
		} else {
			LOGGER.warn(String.format("Failed to set report field for rule %s. Expected type %s but found %s.",
					ruleType.getName(), dataType.getName(), data.getClass().getName()));
		}
	}
}
