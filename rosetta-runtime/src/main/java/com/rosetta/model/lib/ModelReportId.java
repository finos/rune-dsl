package com.rosetta.model.lib;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;

public class ModelReportId extends ModelId implements Comparable<ModelReportId> {
	private final String body;
	private final String[] corpusList;

	public ModelReportId(DottedPath namespace, String body, String... corpusList) {
		super(namespace);
		Objects.requireNonNull(body);
		Validate.noNullElements(corpusList);
		
		this.body = body;
		this.corpusList = corpusList;
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