package com.regnosys.rosetta.blueprints.runner.data;

public class Issue {

	private final String buildPath;
	private final String issueText;

	public Issue(String buildPath, String text) {
		this.buildPath = buildPath;
		// TODO Auto-generated constructor stub
		this.issueText = text;
	}

	@Override
	public String toString() {
		return "Issue [buildPath=" + buildPath + ", issueText=" + issueText + "]";
	}

	public String getBuildPath() {
		return buildPath;
	}

	public String getIssueText() {
		return issueText;
	}
	
	
}
