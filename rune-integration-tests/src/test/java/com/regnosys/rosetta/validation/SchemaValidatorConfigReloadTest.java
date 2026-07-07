package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import com.regnosys.rosetta.utils.RuneConfigurationHolder;

/**
 * Regression test for live configuration reloads: validators must read the configuration through
 * {@link RuneConfigurationHolder} at validation time, not capture it at injection time. Otherwise a
 * schema imported into a running session (which updates {@code rune-config.yml} and triggers
 * {@code rosetta/updateConfig}) keeps failing validation with a stale "no external serialization
 * configuration" error until the session is restarted.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(ReloadableConfigInjectorProvider.class)
public class SchemaValidatorConfigReloadTest extends AbstractValidatorTest {

	private static final String MODEL = """
			namespace test
			version "1"

			schema reloadedXmlSchema XML
				[externalConfig]
			""";

	@Inject
	private RuneConfigurationHolder configHolder;

	@AfterEach
	void resetConfig() {
		ReloadableConfigInjectorProvider.CONFIG_RESOURCE.set(ReloadableConfigInjectorProvider.INITIAL_CONFIG);
		configHolder.reload();
	}

	@Test
	void schemaValidationSeesReloadedConfiguration() {
		// The initial config has no entry for the schema; this validation also forces the
		// validator singleton into existence, so a captured-at-injection-time config would now be baked in.
		assertIssues(MODEL, """
				ERROR (null) 'Schema 'reloadedXmlSchema' is marked [externalConfig] but no external serialization configuration is configured for it' at 4:8, length 17, on Schema
				""");

		// Simulate a live config update (e.g. a schema import writing rune-config.yml followed by
		// rosetta/updateConfig): the config now has an entry for the schema and the holder reloads.
		ReloadableConfigInjectorProvider.CONFIG_RESOURCE.set("schema-reload/rune-config-with-schema.yml");
		configHolder.reload();

		assertNoIssues(MODEL);
	}
}
