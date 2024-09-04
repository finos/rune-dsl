package com.regnosys.rosetta.xcore.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * MWE fragment that extracts code from long methods in the generated ANTLR V3 semantic sequencer in order to avoid problems
 * with methods exceeding the 65535 bytes limit.
 */
public class SequencerCaseExtractorFragment extends AbstractXtextGeneratorFragment {

	private final static Logger LOGGER = Logger.getLogger(SequencerCaseExtractorFragment.class);


	private final List<String> grammarFiles;

	public SequencerCaseExtractorFragment() {
		grammarFiles = new ArrayList<>();
	}

	/**
	 * Adds a grammar file name.
	 */
	public void addGrammarFile(String fileName) {
		grammarFiles.add(fileName);
	}
	
	@Override
	public void generate() {
		for (String fileName : grammarFiles) {
			File file = new File(fileName);
			String javaSource = null;
			try {
				javaSource = Files.asCharSource(file, Charsets.UTF_8).read();
			} catch (Exception ex) {
				LOGGER.error("Error reading file " + fileName + ": " + ex.getMessage());
			}
			if (javaSource != null) {
				String processed = process(javaSource, file);
				LOGGER.info("File " + readableFileName(file) + " processed: " + javaSource.length() + " --> "
						+ processed.length() + " ("
						+ 100 * processed.length() / javaSource.length() + "%)");

				try {
					Files.asCharSink(file, Charsets.UTF_8).write(processed);
				} catch (IOException e) {
					LOGGER.error("Error writing processed file " + readableFileName(file) + ": " + e.getMessage());
				}
			}
		}
		
	}
	
	private String readableFileName(File f) {
		String path = f.getPath();
		int firstChar = 0;
		for (; firstChar < path.length(); firstChar++) {
			if (Character.isLetterOrDigit(path.charAt(firstChar))) {
				break;
			}
		}
		if (firstChar == path.length()) {
			firstChar = 0;
		}
		int firstSeg = path.indexOf(File.separatorChar, firstChar);
		if (firstSeg > 0) {
			return path.substring(firstChar, firstSeg) + "/.../" + f.getName();
		}
		return f.getName();
	}
	
	private String process(String input, File file) {
		StringBuffer result = new StringBuffer();
		Matcher sequenceMatcher = SEQUENCE_MATCHER.matcher(input);
		if (sequenceMatcher.find()) {
			String sequenceMethod = sequenceMatcher.group(2);
			List<String> replacementConentAndMethods = extractSwitchStatements(sequenceMethod);
			String replacement = "$1" + replacementConentAndMethods.get(0) + "$3" + replacementConentAndMethods.get(1);
			sequenceMatcher.appendReplacement(result, replacement);
			
		}
		sequenceMatcher.appendTail(result);
		return result.toString();
	}
	
	private List<String> extractSwitchStatements(String sequenceMethod) {
		StringBuffer content = new StringBuffer();
		StringBuffer newMethods = new StringBuffer();

		Matcher switchMatcher = SWITCH_MATCHER.matcher(sequenceMethod);
		while(switchMatcher.find()) {
			String switchContent = switchMatcher.group(1);
			List<String> replacementConentAndMethods = 
					extractCaseStatments(STRIP_CLOSING_BRACKET_MATCHER.matcher(switchContent).replaceAll("$1"));
			String replacement = "$1" + replacementConentAndMethods.get(0) + "$3";
			switchMatcher.appendReplacement(content, replacement);
			newMethods.append(replacementConentAndMethods.get(1));
		}
		switchMatcher.appendTail(content);
		return List.of(content.toString(), newMethods.toString());
	}
	
	private List<String> extractCaseStatments(String caseStatement) {
		Matcher caseMatcher = CASE_PATTERN.matcher(caseStatement);
		
		int i=0;
		
		StringBuffer newCaseContent = new StringBuffer();
		StringBuffer newMethods = new StringBuffer();
		
		while(caseMatcher.find()) {
			String methodName = "caseMethod" + i;
			String caseCondition = caseMatcher.group(1);
			
			newCaseContent.append(caseCondition);
			newCaseContent.append("\nboolean shouldBreak = " + methodName + "();\n");
			newCaseContent.append("if (shouldBreak) break; else return;\n");
			
			String newMethodContent = createMethod(methodName, caseMatcher.group().replace(caseCondition, ""));
			newMethods.append(newMethodContent);
			i++;
		}
		caseMatcher.appendTail(newCaseContent);
		return List.of(newCaseContent.toString(), newMethods.toString());
	}

	private String createMethod(String methodName, String caseContent) {
		StringBuffer method = new StringBuffer();
		method.append("\n");
		method.append("private boolean " + methodName + "() {\n");
		String newCaseContent = caseContent
		.replace("return;", "return false;")
		.replace("break;", "return true;");
		method.append(newCaseContent);
		method.append("}\n");
		return method.toString();
	}

	private static final Pattern SEQUENCE_MATCHER = Pattern.compile(
			"(^\\s*public void sequence\\(ISerializationContext context, EObject semanticObject\\)\\s*\\{\\n.*?)(^\\s*if\\s+\\(epackage\\s*==.*?)(^\\s*\\}\\s*?)\\/\\*\\*"
			, Pattern.DOTALL | Pattern.MULTILINE
			);
	
	private static final Pattern SWITCH_MATCHER = Pattern.compile(
			"(^\\s*(?:else)?\\s*if\\s+\\(epackage\\s*==.+?\\)\\s*switch[^{]+\\{\\s)((?:^(?!\\s*(?:else)?\\s*if\\s+\\(epackage\\s*==)(?!\\s*if\\s*\\(errorAcceptor).*?$\\s)+)"
			, Pattern.DOTALL | Pattern.MULTILINE);
	
	private static final Pattern STRIP_CLOSING_BRACKET_MATCHER = Pattern.compile("(.*)^\\s*?}$", Pattern.DOTALL | Pattern.MULTILINE);
			
	
	public static final Pattern CASE_PATTERN = Pattern.compile(
			"(?:^(\\s*case\\s+(?:\\w+\\.\\w+)\\s*:(?:\\s*)$\\s)\\s*if.*?)(?:^(?!\\s*case).*?$\\s)+" 
			, Pattern.DOTALL | Pattern.MULTILINE);

}
