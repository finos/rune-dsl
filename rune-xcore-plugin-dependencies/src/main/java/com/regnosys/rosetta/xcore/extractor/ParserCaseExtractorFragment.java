package com.regnosys.rosetta.xcore.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.IntStream;
import org.antlr.runtime.NoViableAltException;
import org.apache.log4j.Logger;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.internal.LexerSpecialStateTransitionSplitter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * MWE fragment that extracts code from long methods in the generated ANTLR V3 parser in order to avoid problems
 * with methods exceeding the 65535 bytes limit.
 */
public class ParserCaseExtractorFragment extends AbstractXtextGeneratorFragment {

	private final static Logger LOGGER = Logger.getLogger(ParserCaseExtractorFragment.class);


	private final List<String> grammarFiles;

	/**
	 *
	 */
	public ParserCaseExtractorFragment() {
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
	
	public static final Pattern DFA_PATTERN = Pattern.compile(
			"(class DFA\\d+ extends DFA \\{.*?" +
			")(public int specialStateTransition\\(int s, IntStream _input\\) throws NoViableAltException \\{.*?" +
			"\\}\\s*NoViableAltException nvae =[^{}]*?" + // end of switch
			"\\})([^{]*?" + // end of specialStateTransition
			"\\})" // end of nested class
			, Pattern.DOTALL | Pattern.MULTILINE);
	
	public static final Pattern SPECIAL_STATE_TRANSITION_PATTERN = Pattern.compile(
			"(public int specialStateTransition\\(int s, IntStream _input\\) throws NoViableAltException \\{.*" +
			"\\}\\s*NoViableAltException nvae =[^{}]*" + // end of switch
			"\\})" // end of specialStateTransition
			, Pattern.DOTALL | Pattern.MULTILINE);
	
	public static final Pattern TOO_MANY_CASES_PATTERN = Pattern.compile("^\\s*case\\s+50", Pattern.MULTILINE);
	
	public static final Pattern CASE_PATTERN = Pattern.compile(
			"(^\\s*case\\s+(\\d+)\\s*:(\\s*))" +// case # -> $1, $2, $3
			"([^;]*;(\\s*int\\s+index[^;]*;\\s*input\\.rewind\\(\\)\\s*;)?)" + // int .. = input.LA(..); ... -> $4 $5
			"\\s*s = -1;"  + // local var init
			"(\\s*if.*?\\}(\\s*else if.*?\\})*(\\s*else s.*?;)?(\\s*input\\.seek[^;]*;)?)" + // $6
			"\\s*(if\\s*\\(\\s*s\\s*>=0\\s*\\)\\s*return\\s*s;\\s*" + // if ( s>=0 ) return s; $10
			"^\\s*break;$)" // break, end case
			, Pattern.DOTALL | Pattern.MULTILINE);
	
	public static final Pattern STATE_PATTERN = Pattern.compile(
			Pattern.quote("if (state.backtracking>0) {state.failed=true; return -1;}"));

	private String process(String input, File file) {
		Matcher dfaMatcher = DFA_PATTERN.matcher(input);
		StringBuffer result = new StringBuffer();
		while(dfaMatcher.find()) {
			String specialStateTransition = dfaMatcher.group(2);
			String staticOrNot = "$1";
			if (!STATE_PATTERN.matcher(specialStateTransition).find())
				staticOrNot = "static $1";
			String tmpSpecialStateTransition = extractSpecialStateMethods(specialStateTransition);
			String transformedDfa = staticOrNot + tmpSpecialStateTransition + "$3";
			dfaMatcher.appendReplacement(result, transformedDfa);
		}
		dfaMatcher.appendTail(result);
		return result.toString();
	}
	
	public String extractSpecialStateMethods(String specialStateTransition) {
		if (!TOO_MANY_CASES_PATTERN.matcher(specialStateTransition).find()) {
			return specialStateTransition.replace("\\", "\\\\").replace("$", "\\$");
		}
		Matcher caseMatcher = CASE_PATTERN.matcher(specialStateTransition);
		StringBuffer result = new StringBuffer();
		StringBuffer extractedMethods = new StringBuffer();
		while(caseMatcher.find()) {
			String replacedCaseBody = "$1s = specialStateTransition$2(input);$3$10";
			extractedMethods.append("\n        protected int specialStateTransition");
			extractedMethods.append(caseMatcher.group(2));
			extractedMethods.append("(IntStream input) {\n");
			extractedMethods.append("            int s = -1;\n            ");
			extractedMethods.append(caseMatcher.group(4).replaceAll("(^|\n)\\s+", "$1            "));
			extractedMethods.append("\n");
			extractedMethods.append(caseMatcher.group(6).replaceAll("(^|\n)\\s+", "$1            "));
			extractedMethods.append("\n            return s;\n");
			extractedMethods.append("        }");
			caseMatcher.appendReplacement(result, replacedCaseBody);
		}
		caseMatcher.appendTail(result);
		result.append(extractedMethods);
		result.append("\n");
		return result.toString().replace("\\", "\\\\").replace("$", "\\$");
	}
}