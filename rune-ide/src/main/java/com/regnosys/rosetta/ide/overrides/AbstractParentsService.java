package com.regnosys.rosetta.ide.overrides;

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.SimpleAttributeResolver;
import org.eclipse.xtext.util.Strings;

import com.regnosys.rosetta.ide.util.AbstractLanguageServerService;
import com.regnosys.rosetta.ide.util.RangeUtils;
import com.regnosys.rosetta.rosetta.RosettaModel;

import jakarta.inject.Inject;

public class AbstractParentsService extends AbstractLanguageServerService<ParentsResult> implements IParentsService {
	@Inject
	private RangeUtils rangeUtils;
	@Inject
	private DocumentExtensions documentExtensions;
	
	private final Function<EObject, String> nameResolver = SimpleAttributeResolver.newResolver(String.class, "name");
	private final String separator = " → ";
	
	public AbstractParentsService() {
		super(ParentsResult.class, ParentsCheck.class);
	}

	@Override
	public List<? extends ParentsResult> computeParents(Document document, XtextResource resource, ParentsParams params, CancelIndicator indicator) {
		return computeResult(document, resource, indicator);
	}
	
	protected ParentsResult fromEObject(EObject child, EObject parent) {
		return fromEObjects(child, parent);
	}
	protected ParentsResult fromEObjects(EObject child, EObject... parents) {
		return fromEObjects(child, List.of(parents));
	}
	protected ParentsResult fromEObjects(EObject child, List<? extends EObject> parents) {
		Range range = rangeUtils.getRange(child);
		
		List<Parent> parentResults = parents.stream()
				.map(p -> {
					String title = getTitle(p);
					Location loc = documentExtensions.newFullLocation(p);
					return new Parent(title, loc);
				})
				.toList();
		 
		return new ParentsResult(range, parentResults);
	}
	
	private String getTitle(EObject object) {
		return getTitle(object, new StringBuilder(), true).toString();
	}
	private StringBuilder getTitle(EObject object, StringBuilder result, boolean isLast) {
		String name = nameResolver.apply(object);
		boolean hasName = !Strings.isEmpty(name);
		
		EObject container = object.eContainer();
		if (container != null && !(container instanceof RosettaModel)) {
			result = getTitle(container, result, isLast && !hasName);
		}
		
		if (hasName) {
			result.append(name);
			if (!isLast) {
				result.append(separator);
			}
		}
		return result;
	}
}
