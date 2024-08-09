package com.rosetta.model.lib;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rosetta.util.DottedPath;

public class ModelTranslationId extends ModelId implements Comparable<ModelTranslationId> {
	private static Pattern TRAANSLATION_REPR_PATTERN = Pattern.compile("<(?<inputList>[a-zA-Z0-9_. ]+) -> (?<output>[a-zA-Z0-9_.]+)>");
	
	private final ModelSymbolId translateSource;
	private final List<ModelSymbolId> inputTypes;
	private final ModelSymbolId outputType;
	
	public ModelTranslationId(ModelSymbolId translateSource, List<ModelSymbolId> inputTypes, ModelSymbolId outputType) {
		super(translateSource.getNamespace());
		Objects.requireNonNull(translateSource);
		Validate.noNullElements(inputTypes);
		Objects.requireNonNull(outputType);
		
		this.translateSource = translateSource;
		this.inputTypes = inputTypes;
		this.outputType = outputType;
	}
	
	@JsonCreator
	public static ModelTranslationId fromNamespaceAndTranslationString(String str) {
		DottedPath parts = DottedPath.splitOnDots(str);
		DottedPath namespaceAndSource = parts.parent();
		ModelSymbolId source = new ModelSymbolId(namespaceAndSource.parent(), namespaceAndSource.last());
		Matcher matcher = TRAANSLATION_REPR_PATTERN.matcher(parts.last());
		if (matcher.matches()) {
			String rawInputList = matcher.group("inputList");
			if (rawInputList != null) {
				List<ModelSymbolId> inputList = Arrays.stream(rawInputList.split(" ")).map(qn -> ModelSymbolId.fromQualifiedName(qn)).collect(Collectors.toList());
				ModelSymbolId output = ModelSymbolId.fromQualifiedName(matcher.group("output"));
				return new ModelTranslationId(source, inputList, output);
			}
		}
		throw new IllegalArgumentException("Invalid format for translation representation: " + parts.last());
	}

	public ModelSymbolId getTranslateSource() {
		return translateSource;
	}
	public List<ModelSymbolId> getInputTypes() {
		return inputTypes;
	}
	public ModelSymbolId getOutputType() {
		return outputType;
	}
	
	@Override
	public String getAlphanumericName() {
		return inputTypes.stream().map(t -> t.getAlphanumericName()).collect(Collectors.joining("And")) + "To" + outputType.getAlphanumericName();
	}

	@JsonValue
	@Override
	public String toString() {
		return translateSource.getQualifiedName().child("<" + inputTypes.stream().map(t -> t.toString()).collect(Collectors.joining(" ")) + " -> " + outputType + ">").withDots();
	}

	@Override
	public int hashCode() {
		return Objects.hash(inputTypes, outputType, translateSource);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelTranslationId other = (ModelTranslationId) obj;
		return Objects.equals(inputTypes, other.inputTypes) && Objects.equals(outputType, other.outputType)
				&& Objects.equals(translateSource, other.translateSource);
	}

	@Override
	public int compareTo(ModelTranslationId o) {
		int namespaceComp = getNamespace().compareTo(o.getNamespace());
		if (namespaceComp != 0) {
			return namespaceComp;
		}
		int sourceComp = translateSource.compareTo(o.translateSource);
		if (sourceComp != 0) {
			return sourceComp;
		}
		for (int i=0; i<inputTypes.size() && i<o.inputTypes.size(); i++) {
			int c = inputTypes.get(i).compareTo(o.inputTypes.get(i));
			if (c != 0) {
				return c;
			}
		}
		int inputSizeComp = Integer.compare(inputTypes.size(), o.inputTypes.size());
		if (inputSizeComp != 0) {
			return inputSizeComp;
		}
		return outputType.compareTo(o.outputType);
	}
}
