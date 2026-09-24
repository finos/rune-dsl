/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator.java.util;

import com.google.common.html.HtmlEscapers;
import com.regnosys.rosetta.rosetta.RosettaCorpus;
import com.regnosys.rosetta.rosetta.RosettaDefinable;
import com.regnosys.rosetta.rosetta.RosettaDocReference;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression;
import com.regnosys.rosetta.rosetta.simple.References;
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;

public class ModelGeneratorUtil {

	@Inject
	private AnnotationPathExpressionUtil annotationPathUtil;

	public String javadoc(RosettaNamed named) {
		String doc = javadoc(named, null);
		return doc == null ? "" : doc;
	}

	private String javadoc(RosettaNamed named, String version) {
		String definition = named instanceof RosettaDefinable definable ? definable.getDefinition() : "";
		List<RosettaDocReference> docReferences = named instanceof References references
				? references.getReferences()
				: Collections.emptyList();
		return javadoc(definition, docReferences, version);
	}

	public String javadoc(String definition, List<RosettaDocReference> docReferences, String version) {
		if (definition == null && docReferences.isEmpty() && version == null) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		out.append("/**");
		terminateLine(out);
		out.append(javadocDefinition(definition));
		terminateLine(out);
		out.append(javadocVersion(version));
		terminateLine(out);
		out.append(javadocDocRef(docReferences));
		terminateLine(out);
		out.append(" */");
		terminateLine(out);
		return out.toString();
	}

	public String emptyJavadocWithVersion(String version) {
		StringBuilder out = new StringBuilder();
		out.append("/**");
		terminateLine(out);
		out.append(" * @version ").append(escapeComment(version));
		terminateLine(out);
		out.append(" */");
		terminateLine(out);
		return out.toString();
	}

	/**
	 * Turns model text into javadoc text: HTML-escapes it, so the text renders as written, and
	 * makes it safe inside a comment (see {@link #escapeComment(String)}).
	 */
	public String escape(String text) {
		return text != null && !text.isEmpty()
				? escapeComment(HtmlEscapers.htmlEscaper().escape(text))
				: "";
	}

	/**
	 * Makes text safe to put inside a Java comment, without changing how the javadoc renders.
	 * <ul>
	 * <li>The Java compiler reads a backslash followed by {@code u} as a unicode escape before it
	 * even looks for comments, so model text such as <code>C:&#92;users</code> fails to compile. That
	 * backslash becomes the HTML character reference {@code &#92;}, which javadoc renders as a
	 * backslash.</li>
	 * <li>A {@code *}{@code /} would end the comment early. Its slash becomes {@code &#47;}, which
	 * javadoc renders as a slash.</li>
	 * </ul>
	 * An HTML character reference keeps the rendered javadoc identical; a Java escape such as
	 * {@code \\} would not work, because comments have no escape sequences. Returns an empty
	 * string for {@code null}.
	 */
	public String escapeComment(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("\\u", "&#92;u").replace("*/", "*&#47;");
	}

	private String javadocDefinition(String definition) {
		StringBuilder out = new StringBuilder();
		if (definition != null && !definition.isEmpty()) {
			out.append(" * ").append(escape(definition));
		}
		terminateLine(out);
		return out.toString();
	}

	private String javadocVersion(String version) {
		StringBuilder out = new StringBuilder();
		if (version != null && !version.isEmpty()) {
			out.append(" * @version ").append(escapeComment(version));
		}
		terminateLine(out);
		return out.toString();
	}

	private String javadocDocRef(List<RosettaDocReference> references) {
		StringBuilder out = new StringBuilder();
		if (references != null && !references.isEmpty()) {
			for (RosettaDocReference reference : references) {
				out.append(" *");
				terminateLine(out);
				if (reference.getPath() != null) {
					out.append(" * ").append(javadocPath(reference.getPath()));
				}
				terminateLine(out);
				out.append(" * Body ").append(reference.getDocReference().getBody().getName());
				terminateLine(out);
				for (RosettaCorpus mandate : reference.getDocReference().getCorpusList()) {
					out.append(" * Corpus ")
							.append(mandate.getCorpusType())
							.append(" ")
							.append(mandate.getName())
							.append(" ");
					if (mandate.getDisplayName() != null) {
						out.append(escape(mandate.getDisplayName()));
					}
					out.append(" ");
					if (mandate.getDefinition() != null) {
						out.append("\"").append(escape(mandate.getDefinition())).append("\"");
					}
					out.append(" ");
				}
				terminateLine(out);
				for (RosettaSegmentRef segment : reference.getDocReference().getSegments()) {
					out.append(" * ")
							.append(segment.getSegment().getName())
							.append(" \"")
							.append(escape(segment.getSegmentRef()))
							.append("\"");
				}
				terminateLine(out);
				out.append(" *");
				terminateLine(out);
				out.append(" * Provision ").append(escapeComment(reference.getProvision()));
				terminateLine(out);
				out.append(" *");
				terminateLine(out);
			}
		}
		return out.toString();
	}

	private String javadocPath(AnnotationPathExpression expr) {
		return annotationPathUtil.<String>fold(
				expr,
				a -> a.getName(),
				a -> "item",
				(r, p) -> r + " -> " + p.getAttribute().getName(),
				(r, dp) -> r + " ->> " + dp.getAttribute().getName());
	}

	/**
	 * Ends the current line with a newline, unless nothing has been written to it yet.
	 * Mirrors the line-elision behaviour of the original Xtend template, where a template
	 * line whose only content was an empty expression contributes no output at all.
	 */
	private static void terminateLine(StringBuilder out) {
		int length = out.length();
		if (length > 0 && out.charAt(length - 1) != '\n') {
			out.append('\n');
		}
	}
}
