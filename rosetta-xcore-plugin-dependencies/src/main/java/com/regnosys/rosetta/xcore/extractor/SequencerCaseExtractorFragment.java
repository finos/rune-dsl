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
		Matcher sequenceMatchwer = SEQUENCE_MATCHER.matcher(input);
		while(sequenceMatchwer.find()) {
			
		}
		
		return result.toString();
	}
	
	private static final Pattern SEQUENCE_MATCHER = Pattern.compile(
			"public void sequence\\(ISerializationContext context, EObject semanticObject\\).*?(if \\(epackage == ExpressionPackage.eINSTANCE\\).*?\\}.*)else if \\(epackage == RosettaPackage.eINSTANCE\\).*?\\}\\n\\n" //match long case statement
			, Pattern.DOTALL | Pattern.MULTILINE);
	
	public static final Pattern CASE_PATTERN = Pattern.compile(
			"(?:^\\s*case\\s+(?:\\w+\\.\\w+)\\s*:(?:\\s*))(?:^(?!\\s*case).*?$\\n)+" 
			, Pattern.DOTALL | Pattern.MULTILINE);

}
