package com.rosetta.model.lib.functions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

public class MapperGroupByC<T,G> implements MapperGroupByBuilder<T,G> {
	
	private final Map<MapperItem<G,?>, MapperBuilder<T>> groupByItems;
	
	protected MapperGroupByC(Map<MapperItem<G,?>, MapperBuilder<T>> groupByItems) {
		this.groupByItems = new FunctionKeyedMap<>(MapperItem::getMappedObject);
		groupByItems.entrySet().stream().forEach(e->this.groupByItems.merge(e.getKey(), e.getValue(), (BinaryOperator<MapperBuilder<T>>) MapperBuilder<T>::unionSame));
	}
	
	@Override
	public Map<MapperS<G>, Mapper<T>> getGroups() {
		Function<Map.Entry<MapperItem<G,?>, MapperBuilder<T>>, MapperS<G>> f1 = e->new MapperS<G>(e.getKey());
		Function<Map.Entry<MapperItem<G,?>, MapperBuilder<T>>, Mapper<T>> f2 = e->e.getValue();
		BinaryOperator<Mapper<T>> merger = (a,b)->((MapperBuilder<T>)a).unionSame((MapperBuilder<T>)b);
		Supplier<Map<MapperS<G>, Mapper<T>>> mapSup = ()->new FunctionKeyedMap<>(MapperS::get);
		return groupByItems.entrySet().stream().filter(e->!e.getKey().isError())
				.collect(Collectors.toMap(f1, f2,
						merger, mapSup));
	}

	private Stream<MapperItem<T, ?>> getItems(boolean errors) {
		return groupByItems.entrySet().stream()
				.filter(e->errors == e.getKey().isError())
				.map(e->e.getValue())
				.flatMap(m->m.getItems().filter(i->i.isError()==errors));
	}

	@Override
	public T get() {
		List<MapperItem<T, ?>> collect = getItems(false).collect(Collectors.toList());
		if (collect.size()!=1) return null;
		return collect.get(0).getMappedObject();
	}

	@Override
	public List<T> getMulti() {
		return getItems(false)
				.map(i->i.getMappedObject())
				.collect(Collectors.toList());
	}

	@Override
	public Optional<?> getParent() {
		List<MapperItem<T, ?>> collect = getItems(false).collect(Collectors.toList());
		if (collect.size()!=1) return Optional.empty();
		return collect.get(0).getParent();
	}

	@Override
	public List<?> getParentMulti() {
		return getItems(false).map(i->i.getParent()).collect(Collectors.toList());
	}

	@Override
	public int resultCount() {
		return (int) getItems(false).count();
	}

	@Override
	public List<Path> getPaths() {
		 return getItems(false).map(i->i.getPath()).collect(Collectors.toList());
	}

	@Override
	public List<Path> getErrorPaths() {
		return getItems(true)
				.map(i->i.getPath())
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getErrors() {
		return getItems(true)
				.map(i->i.getPath())
				.map(p -> String.format("[%s] is not set", p.getFullPath()))
				.collect(Collectors.toList());
	}

	@Override
	public <F> MapperGroupByBuilder<F, G> map(NamedFunction<T, F> mappingFunc) {
		Map<MapperItem<G, ?>, MapperBuilder<F>> newItems = groupByItems.entrySet().stream()
				.collect(Collectors.toMap(e->e.getKey(), e->e.getValue().map(mappingFunc)));
		return new MapperGroupByC<F,G>(newItems);
	}

	@Override
	public <F> MapperGroupByBuilder<F, G> mapC(NamedFunction<T, List<F>> mappingFunc) {
		Map<MapperItem<G, ?>, MapperBuilder<F>> newItems = groupByItems.entrySet().stream()
				.collect(Collectors.toMap(e->e.getKey(), e->e.getValue().mapC(mappingFunc)));
		return new MapperGroupByC<F,G>(newItems);
	}
	
	private class FunctionKeyedMap<K, KREAL, V> implements Map<K,V> {
		Map<KREAL, V> underlyingMap = new HashMap<>();
		Map<KREAL, K> keyObjectLookup = new HashMap<>();
		
		Function<K, KREAL> keyFunction;
		
		public FunctionKeyedMap(Function<K, KREAL> keyFunction) {
			super();
			this.keyFunction = keyFunction;
		}

		@Override
		public int size() {
			return underlyingMap.size();
		}

		@Override
		public boolean isEmpty() {
			return underlyingMap.isEmpty();
		}
		
		@SuppressWarnings("unchecked")
		private KREAL realKey(Object key) {
			return keyFunction.apply((K) key);
		}

		@Override
		public boolean containsKey(Object key) {
			return underlyingMap.containsKey(realKey(key));
		}

		@Override
		public boolean containsValue(Object value) {
			return underlyingMap.containsValue(value);
		}

		@Override
		public V get(Object key) {
			return underlyingMap.get(realKey(key));
		}

		@Override
		public V put(K key, V value) {
			KREAL realKey = realKey(key);
			V result = underlyingMap.put(realKey, value);
			keyObjectLookup.put(realKey, key);
			return result;
		}

		@Override
		public V remove(Object key) {
			KREAL realKey = realKey(key);
			V result = underlyingMap.remove(realKey);
			keyObjectLookup.remove(realKey);
			return result;
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			m.entrySet().stream().forEach(e->put(e.getKey(), e.getValue()));
		}

		@Override
		public void clear() {
			underlyingMap.clear();
			keyObjectLookup.clear();
		}

		@Override
		public Set<K> keySet() {
			return new HashSet<>(keyObjectLookup.values());
		}

		@Override
		public Collection<V> values() {
			return underlyingMap.values();
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			return underlyingMap.entrySet().stream()
					.map(e->Maps.immutableEntry(keyObjectLookup.get(e.getKey()), e.getValue()))
					.collect(Collectors.toSet());
		}
	}
}
