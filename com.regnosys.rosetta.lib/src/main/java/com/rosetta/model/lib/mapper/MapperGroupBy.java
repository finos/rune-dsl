package com.rosetta.model.lib.mapper;

import java.util.Map;

public interface MapperGroupBy<T,G> extends Mapper<T> {

	Map<MapperS<G>, Mapper<T>> getGroups();
}
