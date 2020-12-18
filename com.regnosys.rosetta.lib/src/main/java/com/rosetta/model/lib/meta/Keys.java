package com.rosetta.model.lib.meta;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class Keys {
	private final ImmutableList<Key> keys;

	public Keys(List<Key> keys) {
		super();
		this.keys = keys==null?ImmutableList.of():ImmutableList.copyOf(keys);
	}

	public ImmutableList<Key> getKeys() {
		return keys;
	}

	public KeysBuilder toBuilder() {
		KeysBuilder keysBuilder = new KeysBuilder();
		for (Key key:keys) {
			keysBuilder.addKey(key.toBuilder());
		}
		return keysBuilder;
	}
	
	public static class KeysBuilder {
		private List<Key.KeyBuilder> keys;
		
		public Keys build() {
			List<Key> keysBuilt = keys!=null ? 
					keys.stream().map(k->k.build()).collect(ImmutableList.toImmutableList()) : 
					ImmutableList.of();
			return new Keys(keysBuilt);
		}

		public List<Key.KeyBuilder> getKeys() {
			return keys;
		}
		
		public void addKey(Key.KeyBuilder key) {
			if (keys==null) keys = new ArrayList<>();
			keys.add(key);
		}

		public void setKeys(List<Key.KeyBuilder> keys) {
			this.keys = keys;
		}
	}
}
