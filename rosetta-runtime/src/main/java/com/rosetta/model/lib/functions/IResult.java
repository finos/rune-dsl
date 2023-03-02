package com.rosetta.model.lib.functions;

import java.util.List;
import java.util.function.Function;

public interface IResult {
	
	/**
	 * @return key/value/type attribute for each result type.
	 */
	List<Attribute<?>> getAttributes();
	
	public static final class Attribute<T> {
		private String name;
		private Class<T> type;
		private Function<IResult, T> accesor;

		public Attribute(String name, Class<T> type, Function<IResult, T> accesor) {
			super();
			this.name = name;
			this.type = type;
			this.accesor = accesor;
		}

		public T get(IResult instance) {
			return accesor.apply(instance);
		}

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return type;
		}
	}
}