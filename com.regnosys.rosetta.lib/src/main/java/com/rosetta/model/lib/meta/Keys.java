package com.rosetta.model.lib.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class Keys {
	
	private final ImmutableList<Key> keys;

	public Keys(List<Key> keys) {
		this.keys = keys == null ? ImmutableList.of() : ImmutableList.copyOf(keys);
	}

	public ImmutableList<Key> getKeys() {
		return keys;
	}

	public KeysBuilder toBuilder() {
		KeysBuilder keysBuilder = new KeysBuilder();
		for (Key key : keys) {
			keysBuilder.addKey(key.toBuilder());
		}
		return keysBuilder;
	}

	@Override 
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Keys keys1 = (Keys) o;
		return Objects.equals(keys, keys1.keys);
	}

	@Override 
	public int hashCode() {
		return Objects.hash(keys);
	}

	@Override 
	public String toString() {
		return "Keys{" +
				"keys=" + keys +
				'}';
	}

	public static class KeysBuilder {
		private List<Key.KeyBuilder> keys;

		public Keys build() {
			List<Key> keysBuilt = keys != null ?
					keys.stream().map(k -> k.build()).collect(ImmutableList.toImmutableList()) :
					ImmutableList.of();
			return new Keys(keysBuilt);
		}

		public List<Key.KeyBuilder> getKeys() {
			return keys;
		}

		public void addKey(Key.KeyBuilder key) {
			if (keys == null)
				keys = new ArrayList<>();
			keys.add(key);
		}

		public void setKeys(List<Key.KeyBuilder> keys) {
			this.keys = keys;
		}

		public boolean hasData() {
			return keys.stream().anyMatch(Key.KeyBuilder::hasData);
		}

		@Override 
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			KeysBuilder that = (KeysBuilder) o;
			return Objects.equals(keys, that.keys);
		}

		@Override 
		public int hashCode() {
			return Objects.hash(keys);
		}

		@Override 
		public String toString() {
			return "KeysBuilder{" +
					"keys=" + keys +
					'}';
		}
	}
}
