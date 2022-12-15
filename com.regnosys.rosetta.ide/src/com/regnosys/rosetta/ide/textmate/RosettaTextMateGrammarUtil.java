package com.regnosys.rosetta.ide.textmate;

import java.net.URL;

public class RosettaTextMateGrammarUtil {
	private static final String TEXT_MATE_FILE = "/rosetta.tmLanguage.json";
	
	public URL getTextMateGrammarURL() {
		return RosettaTextMateGrammarUtil.class.getResource(TEXT_MATE_FILE);
	}
}
