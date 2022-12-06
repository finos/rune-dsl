package com.regnosys.rosetta.utils;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.rosetta.Playground;
import com.regnosys.rosetta.rosetta.PlaygroundElement;
import com.regnosys.rosetta.rosetta.PlaygroundLocation;

public class PlaygroundLocationUtil {
	public Optional<PlaygroundElement> findElement(PlaygroundLocation loc, EObject relativeTo) {
		if (!(relativeTo.eContainer() instanceof Playground)) {
			throw new IllegalArgumentException("The given object should be a direct child of a Playground.");
		}
		final Playground pg = (Playground)relativeTo.eContainer();
		
		final List<EObject> contents = pg.eContents();
		int currentIndex = contents.indexOf(relativeTo);
		final int direction = loc.getDirection().getValue();
		int occurencesToGo = loc.getN();
		
		currentIndex += direction;
		while (0 <= currentIndex && currentIndex < contents.size()) {
			if (contents.get(currentIndex) instanceof PlaygroundElement) {
				PlaygroundElement currentElement = (PlaygroundElement)contents.get(currentIndex);
				if (loc.getValue().equals(currentElement.getValue())) {
					occurencesToGo--;
					if (occurencesToGo == 0) {
						return Optional.of(currentElement);
					}
				}
			}
			
			currentIndex += direction;
		}
		return Optional.empty();
	}
}
