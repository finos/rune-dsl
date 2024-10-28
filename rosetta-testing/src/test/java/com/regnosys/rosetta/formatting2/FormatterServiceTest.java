package com.regnosys.rosetta.formatting2;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.io.Resources;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class FormatterServiceTest {
	@Inject CodeFormatterService formatterService;
	@Inject Provider<ResourceSet> resourceSetProvider;
	@Inject ISerializer serializer;
	
	@Test
	void formatSingleDocument() throws IOException, URISyntaxException {
		ResourceSet resourceSet = resourceSetProvider.get();
		Resource resource = resourceSet.getResource(URI.createURI(Resources.getResource("formatting-test/input/test.rosetta").toString()), true);
		
		List<Resource> formatCollection = formatterService.formatCollection(List.of(resource));
		
		String expected = Files.readString(Path.of(Resources.getResource("formatting-test/expected/test.rosetta").toURI()));
		String result = serializer.serialize(formatCollection.get(0).getContents().get(0));
		assertEquals(expected, result);	
	}
}
