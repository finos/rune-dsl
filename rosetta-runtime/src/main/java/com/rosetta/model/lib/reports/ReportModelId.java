package com.rosetta.model.lib.reports;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;

public class ReportModelId {
	private DottedPath namespace;
	private String body;
	private String[] corpuses;

	public ReportModelId(DottedPath namespace, String body, String... corpuses) {
		Validate.notNull(namespace);
		Validate.notNull(body);
		Validate.notNull(corpuses);
		Validate.noNullElements(corpuses);
		
		this.namespace = namespace;
		this.body = body;
		this.corpuses = corpuses;
	}

	public DottedPath getNamespace() {
		return namespace;
	}
	public String getBody() {
		return body;
	}
	public String[] getCorpuses() {
		return corpuses;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(namespace, body, Arrays.hashCode(corpuses));
	}
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        ReportModelId other = (ReportModelId) object;
        return Objects.equals(namespace, other.namespace)
        		&& Objects.equals(body, other.body)
        		&& Arrays.equals(corpuses, other.corpuses);
	}
}
