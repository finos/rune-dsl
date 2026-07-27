package com.regnosys.rosetta.formatting2;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.io.Resources;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ResourceFormatterServiceTest {
	@Inject
	ResourceFormatterService formatterService;
	@Inject
	Provider<ResourceSet> resourceSetProvider;

	private void testFormatting(Collection<String> inputUrls, Collection<String> expectedUrls)
			throws IOException, URISyntaxException {
		ResourceSet resourceSet = resourceSetProvider.get();
		List<Resource> resources = new ArrayList<>();
		List<String> formattedText = new ArrayList<>();
		List<String> expectedText = new ArrayList<>();

		for (String url : inputUrls) {
			Resource resource = resourceSet.getResource(URI.createURI(Resources.getResource(url).toString()), true);
			resources.add(resource);
		}

		for (String url : expectedUrls) {
			expectedText.add(Files.readString(Path.of(Resources.getResource(url).toURI())));
		}

		formatterService.formatCollection(resources, (resource, formattedContent) -> {
			formattedText.add(formattedContent); // Collect formatted content for assertions
		});

		Assertions.assertIterableEquals(expectedText, formattedText);
	}

	@Test
	void formatNonContiguousImportBlock() throws IOException, URISyntaxException {
		testFormatting(List.of("formatting-test/input/nonContiguousImportBlock.rosetta"),
				List.of("formatting-test/expected/nonContiguousImportBlock.rosetta"));
	}

	@Test
	void formatSingleDocument() throws IOException, URISyntaxException {
		testFormatting(List.of("formatting-test/input/typeAlias.rosetta"),
				List.of("formatting-test/expected/typeAlias.rosetta"));
	}

	@Test
	void formatMultipleDocuments() throws IOException, URISyntaxException {
		testFormatting(
				List.of("formatting-test/input/typeAlias.rosetta",
						"formatting-test/input/typeAliasWithDocumentation.rosetta"),
				List.of("formatting-test/expected/typeAlias.rosetta",
						"formatting-test/expected/typeAliasWithDocumentation.rosetta"));
	}
	
	@Test
	void formatNestedConstructor() throws IOException, URISyntaxException {
		testFormatting(List.of("formatting-test/input/nestedConstructor.rosetta"),
				List.of("formatting-test/expected/nestedConstructor.rosetta"));
	}

	@Test
	void formatDocumentKeepsItsOwnLineSeparator() throws IOException {
		// A document written with CRLF line endings (e.g. by an editor on Windows)
		// must be formatted with CRLF throughout, independent of the platform.
		String content = "namespace test\r\nversion \"1.2.3\"\r\n\r\ntype Foo:\r\n  attr int (1..1)\r\n";
		ResourceSet resourceSet = resourceSetProvider.get();
		Resource resource = resourceSet.createResource(URI.createURI("dummy:/crlf.rosetta"));
		resource.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), null);

		List<String> formattedText = new ArrayList<>();
		formatterService.formatCollection(List.of(resource), (r, formattedContent) -> formattedText.add(formattedContent));

		Assertions.assertEquals(1, formattedText.size());
		String formatted = formattedText.get(0);
		Assertions.assertTrue(formatted.contains("\r\n"), "formatted document should keep CRLF line endings");
		Assertions.assertFalse(formatted.replace("\r\n", "").contains("\n"),
				"formatted document should not mix bare LF into a CRLF document");
	}

}
