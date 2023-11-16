package com.regnosys.rosetta.generator.java.statement;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.TargetLanguageRepresentation;

public class JavaStatementList extends ArrayList<JavaStatement> implements TargetLanguageRepresentation {
	private static final long serialVersionUID = 1L;
	
	public JavaStatementList(JavaStatement... items) {
		super(Arrays.asList(items));
	}
	public JavaStatementList() {
		super();
	}
	
	public static JavaStatementList of(JavaStatement... items) {
		return new JavaStatementList(items);
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		for (int i = 0; i < size(); i++) {
			target.append(get(i));
			if (i < size() - 1) {
				target.newLine();
			}
		}
	}
}
