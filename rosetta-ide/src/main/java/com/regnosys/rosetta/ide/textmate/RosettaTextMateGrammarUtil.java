package com.regnosys.rosetta.ide.textmate;

import java.net.URL;

public class RosettaTextMateGrammarUtil {
	public static final String TEXT_MATE_FILE = "/syntaxes/rosetta.tmLanguage.json";
	
	public URL getTextMateGrammarURL() {
		return RosettaTextMateGrammarUtil.class.getResource(TEXT_MATE_FILE);
	}
}
