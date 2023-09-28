package com.rosetta.model.lib;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rosetta.util.DottedPath;

public class ModelReportId extends ModelId implements Comparable<ModelReportId> {
	private static Pattern REGULATORY_REFERENCE_REPR_PATTERN = Pattern.compile("<(?<body>[a-zA-Z0-9_]+)(?: (?<corpusList>[a-zA-Z0-9_ ]+))>");
	
	private final String body;
	private final String[] corpusList;

	public ModelReportId(DottedPath namespace, String body, String... corpusList) {
		super(namespace);
		Objects.requireNonNull(body);
		Validate.noNullElements(corpusList);
		
		this.body = body;
		this.corpusList = corpusList;
	}
	
	@JsonCreator
	public static ModelReportId fromNamespaceAndRegulatoryReferenceString(String str) {
		DottedPath parts = DottedPath.splitOnDots(str);
		DottedPath namespace = parts.parent();
		Matcher matcher = REGULATORY_REFERENCE_REPR_PATTERN.matcher(parts.last());
		String body = matcher.group("body");
		String rawCorpusList = matcher.group("corpusList");
		String[] corpusList = rawCorpusList == null ? new String[0] : rawCorpusList.split(" ");
		return new ModelReportId(namespace, body, corpusList);
	}

	public String getBody() {
		return body;
	}
	public String[] getCorpusList() {
		return corpusList;
	}
	
	@Override
	public String getAlphanumericName() {
		return joinRegulatoryReference();
	}
	
	public String joinRegulatoryReference() {
		return joinRegulatoryReference("");
	}
	public String joinRegulatoryReference(String separator) {
		return joinRegulatoryReference(separator, separator);
	}
	public String joinRegulatoryReference(String bodySeparator, String corpusSeparator) {
		if (corpusList.length == 0) {
			return body;
		}
		return body + bodySeparator + String.join(corpusSeparator, corpusList);
	}

	@JsonValue
	@Override
	public String toString() {
		return getNamespace().withDots() + "<" + joinRegulatoryReference(" ") + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(corpusList);
		result = prime * result + Objects.hash(body, getNamespace());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelReportId other = (ModelReportId) obj;
		return Objects.equals(body, other.body) && Arrays.equals(corpusList, other.corpusList)
				&& Objects.equals(getNamespace(), other.getNamespace());
	}

	@Override
	public int compareTo(ModelReportId o) {
		int namespaceComp = getNamespace().compareTo(o.getNamespace());
		if (namespaceComp != 0) {
			return namespaceComp;
		}
		int bodyComp = body.compareTo(o.body);
		if (bodyComp != 0) {
			return bodyComp;
		}
		for (int i=0; i<corpusList.length && i<o.corpusList.length; i++) {
			int c = corpusList[i].compareTo(o.corpusList[i]);
			if (c != 0) {
				return c;
			}
		}
		return Integer.compare(corpusList.length, o.corpusList.length);
	}
}