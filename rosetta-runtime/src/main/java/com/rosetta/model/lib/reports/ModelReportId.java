package com.rosetta.model.lib.reports;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;

public class ModelReportId {
	private DottedPath namespace;
	private String body;
	private String[] corpusList;

	public ModelReportId(DottedPath namespace, String body, String... corpusList) {
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(body);
		Validate.noNullElements(corpusList);
		
		this.namespace = namespace;
		this.body = body;
		this.corpusList = corpusList;
	}

	public DottedPath getNamespace() {
		return namespace;
	}
	public String getBody() {
		return body;
	}
	public String[] getCorpusList() {
		return corpusList;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(namespace, body, Arrays.hashCode(corpusList));
	}
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        ModelReportId other = (ModelReportId) object;
        return Objects.equals(namespace, other.namespace)
        		&& Objects.equals(body, other.body)
        		&& Arrays.equals(corpusList, other.corpusList);
	}
}
