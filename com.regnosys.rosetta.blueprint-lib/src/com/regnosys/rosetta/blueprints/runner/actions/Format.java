package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Optional;

public abstract class Format<I, K extends Comparable<K>> extends NamedNode implements ProcessorNode<I, String, K> {

	protected final String formatString;

	public Format(String uri, String label, String formatString) {
		super(uri, label);
		this.formatString = formatString;
	}

	public static class StringFormat<I, K extends Comparable<K>> extends Format<I, K> {

		public StringFormat(String uri, String label, String formatString) {
			super(uri, label, formatString);
		}

		@Override
		public <T extends I> Optional<GroupableData<String, K>> process(GroupableData<T, K> input) {
			String output = String.format(formatString, input.getData().toString());
			return Optional.of(input.withIssues(output, Collections.emptyList(), this));

		}
	}
	
	public static class NumberFormat<K extends Comparable<K>> extends Format<Number, K> {

		public NumberFormat(String uri, String label, String formatString) {
			super(uri, label, formatString);
		}

		@Override
		public <T extends Number> Optional<GroupableData<String, K>> process(GroupableData<T, K> input) {
			T data = input.getData();
			DecimalFormat decimalFormat = new DecimalFormat(formatString);
			String output = decimalFormat.format(data);
			return Optional.of(input.withIssues(output, Collections.emptyList(), this));
		}
	}
}
