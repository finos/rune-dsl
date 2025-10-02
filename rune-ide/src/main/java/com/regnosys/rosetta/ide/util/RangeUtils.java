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

package com.regnosys.rosetta.ide.util;

import java.util.Iterator;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.common.collect.Iterators;

/**
 * TODO: contribute to Xtext?
 *
 */
public class RangeUtils {
	private static final int INSIGNIFICANT_INDEX = -1;
	
	@Inject
	private DocumentExtensions documentExtensions;
	
	public Range getRange(EObject obj) {
		return documentExtensions.newFullLocation(obj).getRange();
	}
	public Range getRange(EObject obj, EStructuralFeature feature) {
		return getRange(obj, feature, INSIGNIFICANT_INDEX);
	}
	public Range getRange(EObject obj, EStructuralFeature feature, int featureIndex) {
		return documentExtensions.newLocation(obj, feature, featureIndex).getRange();
	}
	public Range getRange(EObject obj, Keyword keyword) {
		ICompositeNode node = NodeModelUtils.findActualNodeFor(obj);
		for (INode child: node.getChildren()) {
			EObject elem = child.getGrammarElement();
			if (elem.equals(keyword)) {
				return documentExtensions.newLocation(obj.eResource(), child.getTextRegionWithLineInformation()).getRange();
			}
		}
		return null;
	}
	
	public TreeIterator<EObject> iterateOverlapping(ResourceImpl resource, Range filter) {
		return new AbstractTreeIterator<EObject>(resource, false) {
	        private static final long serialVersionUID = 1L;

	        @Override
	        public Iterator<EObject> getChildren(Object object)
	        {
	        	Iterator<EObject> allChildren = object == resource ? resource.getContents().iterator() : ((EObject)object).eContents().iterator();
	        	return Iterators.filter(allChildren, obj -> overlap(filter, obj));
	        }
	    };
	}
	
	public boolean overlap(Range a, Range b) {
		return strictlyComesBefore(a.getStart(), b.getEnd()) && strictlyComesBefore(b.getStart(), a.getEnd());
	}
	public boolean overlap(Range a, EObject b) {
		return overlap(a, getRange(b));
	}
	public boolean overlap(EObject a, EObject b) {
		return overlap(getRange(a), getRange(b));
	}
	
	public boolean strictlyComesBefore(Position a, Position b) {
		return a.getLine() < b.getLine() || a.getLine() == b.getLine() && a.getCharacter() < b.getCharacter();
	}
	
	public int comparePositions(Position a, Position b) {
		if (a.getLine() < b.getLine()) {
			return -1;
		}
		if (a.getLine() == b.getLine()) {
			if (a.getCharacter() < b.getCharacter()) {
				return -1;
			}
			if (a.getCharacter() == b.getCharacter()) {
				return 0;
			}
		}
		return 1;
	}
}
