package com.regnosys.rosetta.tools.modelimport;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportTargetPreferences {
	private final CasingStrategy typeCasing;
	private final CasingStrategy attributeCasing;
	private final CasingStrategy enumValueCasing;
	
	public ImportTargetPreferences(
			@JsonProperty("typeCasing") CasingStrategy typeCasing,
			@JsonProperty("attributeCasing") CasingStrategy attributeCasing,
			@JsonProperty("enumValueCasing") CasingStrategy enumValueCasing) {
		
		this.typeCasing = typeCasing == null ? CasingStrategy.PascalCase : typeCasing;
		this.attributeCasing = attributeCasing == null ? CasingStrategy.camelCase : attributeCasing;
		this.enumValueCasing = enumValueCasing == null ? CasingStrategy.UPPER_SNAKE_CASE : enumValueCasing;
	}

	public CasingStrategy getTypeCasing() {
		return typeCasing;
	}

	public CasingStrategy getAttributeCasing() {
		return attributeCasing;
	}

	public CasingStrategy getEnumValueCasing() {
		return enumValueCasing;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributeCasing, enumValueCasing, typeCasing);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImportTargetPreferences other = (ImportTargetPreferences) obj;
		return attributeCasing == other.attributeCasing && enumValueCasing == other.enumValueCasing
				&& typeCasing == other.typeCasing;
	}
}
