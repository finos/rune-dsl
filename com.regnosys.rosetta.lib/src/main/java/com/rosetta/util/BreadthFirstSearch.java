package com.rosetta.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class BreadthFirstSearch {
	public static <T> List<T> search(T start, Function<T,Collection<T>> nextFunction, Predicate<T> matcher) {
		Set<T> visited = new HashSet<>();
		Deque<SearchState<T>> searchList = new ArrayDeque<>();
		searchList.add(new SearchState<T>(Collections.emptyList(), start));
		while (!searchList.isEmpty()) {
			SearchState<T> state = searchList.removeFirst();
			if (visited.contains(state.current)) {
				continue;
			}
			else {
				visited.add(state.current);
				ArrayList<T> newPath = new ArrayList<>(state.path);
				newPath.add(state.current);
				if (matcher.apply(state.current)) {
					return newPath;
				}
				else {
					Collection<T> nexts = nextFunction.apply(state.current);
					for (T next:nexts) {
						searchList.add(new SearchState<>(newPath, next));
					}
				}
			}
		}
		return null;
	}
	
	static class SearchState<T> {
		private final List<T> path;
		private final T current;
		public SearchState(List<T> path, T current) {
			super();
			this.path = path;
			this.current = current;
		}
		
		public String toString() {
			return path.toString()+current.toString();
		}
	}
}
