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

package com.regnosys.rosetta.ide.inlayhints;

import com.regnosys.rosetta.ide.util.AbstractLanguageServerService;
import com.regnosys.rosetta.ide.util.RangeUtils;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import jakarta.inject.Inject;
import java.util.*;

/**
 * TODO: contribute to Xtext.
 *
 */
public abstract class AbstractInlayHintsService extends AbstractLanguageServerService<InlayHint> implements IInlayHintsService, IInlayHintsResolver {
	@Inject
	private RangeUtils rangeUtils;


	public AbstractInlayHintsService() {
		super(InlayHint.class, InlayHintCheck.class);
	}

	@Override
	public List<InlayHint> computeInlayHint(Document document, XtextResource resource, InlayHintParams params, CancelIndicator cancelIndicator) {
		return computeResult(document, resource, params.getRange(), cancelIndicator);
	}

	protected InlayHint inlayHintBefore(EObject hintObject, String label, String tooltip) {
		Position start = rangeUtils.getRange(hintObject).getStart();
		return inlayHintAt(start, label, tooltip, false, true);
	}
	protected InlayHint inlayHintBefore(EObject hintObject, EStructuralFeature feature, String label, String tooltip) {
		Position start = rangeUtils.getRange(hintObject, feature).getStart();
		return inlayHintAt(start, label, tooltip, false, true);
	}
	protected InlayHint inlayHintBefore(EObject hintObject, EStructuralFeature feature, int featureIndex, String label, String tooltip) {
		Position start = rangeUtils.getRange(hintObject, feature, featureIndex).getStart();
		return inlayHintAt(start, label, tooltip, false, true);
	}
	protected InlayHint inlayHintBefore(EObject hintObject, Keyword keyword, String label, String tooltip) {
		Position start = rangeUtils.getRange(hintObject, keyword).getStart();
		return inlayHintAt(start, label, tooltip, false, true);
	}
	
	protected InlayHint inlayHintAfter(EObject hintObject, String label, String tooltip) {
		Position end = rangeUtils.getRange(hintObject).getEnd();
		return inlayHintAt(end, label, tooltip, true, false);
	}
	protected InlayHint inlayHintAfter(EObject hintObject, EStructuralFeature feature, String label, String tooltip) {
		Position end = rangeUtils.getRange(hintObject, feature).getEnd();
		return inlayHintAt(end, label, tooltip, true, false);
	}
	protected InlayHint inlayHintAfter(EObject hintObject, EStructuralFeature feature, int featureIndex, String label, String tooltip) {
		Position end = rangeUtils.getRange(hintObject, feature, featureIndex).getEnd();
		return inlayHintAt(end, label, tooltip, true, false);
	}
	protected InlayHint inlayHintAfter(EObject hintObject, Keyword keyword, String label, String tooltip) {
		Position end = rangeUtils.getRange(hintObject, keyword).getEnd();
		return inlayHintAt(end, label, tooltip, true, false);
	}
	
	protected InlayHint inlayHintAt(Position position, String label, String tooltip, boolean paddingLeft, boolean paddingRight) {
		InlayHint inlayHint = createInlayHint();
		inlayHint.setPosition(position);
		inlayHint.setLabel(label);
		inlayHint.setTooltip(tooltip);
		inlayHint.setPaddingLeft(paddingLeft);
		inlayHint.setPaddingRight(paddingRight);
		return inlayHint;
	}
	
	protected InlayHint createInlayHint() {
		InlayHint inlayHint = new InlayHint();
		return inlayHint;
	}

	@Override
	public InlayHint resolveInlayHint(Document document, XtextResource resource, InlayHint unresolved, CancelIndicator cancelIndicator) {
		return unresolved;
	}
}
