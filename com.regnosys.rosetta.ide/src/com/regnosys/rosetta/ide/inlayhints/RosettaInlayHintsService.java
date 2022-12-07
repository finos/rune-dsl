package com.regnosys.rosetta.ide.inlayhints;

import javax.inject.Inject;

import org.eclipse.lsp4j.InlayHint;

import com.regnosys.rosetta.rosetta.OnRequest;
import com.regnosys.rosetta.rosetta.PlaygroundElement;
import com.regnosys.rosetta.rosetta.RangeRequest;
import com.regnosys.rosetta.rosetta.RequestType;
import com.regnosys.rosetta.utils.PlaygroundLocationUtil;

public class RosettaInlayHintsService extends AbstractInlayHintsService {
	@Inject PlaygroundLocationUtil locationUtil;
	
	@InlayHintCheck
	public InlayHint checkPlaygroundOnRequest(OnRequest req) {
		if (req.getType().equals(RequestType.INLAY)) {
			return locationUtil.findElement(req.getLocation(), req)
					.map((PlaygroundElement elem) -> {
						return createInlayHint(elem, req.getContent(), "");
					}).orElse(null);
		}
		return null;
	}
	
	@InlayHintCheck
	public InlayHint checkPlaygroundRangeRequest(RangeRequest req) {
		if (req.getType().equals(RequestType.INLAY)) {
			return locationUtil.findElement(req.getFrom(), req)
					.map((PlaygroundElement elem) -> {
						return createInlayHint(elem, req.getContent(), "");
					}).orElse(null);
		}
		return null;
	}
}
