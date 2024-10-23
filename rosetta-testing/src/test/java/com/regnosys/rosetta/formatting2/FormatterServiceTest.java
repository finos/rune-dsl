package com.regnosys.rosetta.formatting2;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResource;
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
	void test1 () throws IOException, URISyntaxException {
		//ResourceSet resourceSet = resourceSetProvider.get();
		//Resource resource = resourceSet.getResource(URI.createURI(Resources.getResource("FormattingTest/input/test.rosetta").toString()), true);
		
		//create xtext resource
		XtextResource res = new XtextResource(URI.createURI(Resources.getResource("FormattingTest/input/test.rosetta").toString()));
		formatterService.formatCollection(List.of(res));
		
		String expected = Files.readString(Path.of(Resources.getResource("FormattingTest/expected/test.rosetta").toURI()));
		String result = serializer.serialize(res.getContents().get(0)); //maybe change this to xtext too?
		assertEquals(expected, result);
		
	}
}
