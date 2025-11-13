package com.regnosys.rosetta.ide.symbol;

import com.regnosys.rosetta.rosetta.expression.RosettaSuperCall;
import jakarta.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Location;
import org.eclipse.xtext.findReferences.IReferenceFinder;
import org.eclipse.xtext.ide.server.symbol.DocumentSymbolService;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import java.util.Collections;
import java.util.List;

public class RosettaDocumentSymbolService extends DocumentSymbolService {
    @Inject
    private EObjectAtOffsetHelper eObjectAtOffsetHelper;

    @Override
    public List<? extends Location> getDefinitions(
            XtextResource resource,
            int offset,
            IReferenceFinder.IResourceAccess resourceAccess,
            CancelIndicator cancelIndicator) {

        EObject at = eObjectAtOffsetHelper.resolveElementAt(resource, offset);

        if (at instanceof RosettaSuperCall superCall) {
            EObject target = resolveSuperTarget(superCall);
            if (target != null) {
                return Collections.singletonList(getSymbolLocation(target));
            }
        }
        return super.getDefinitions(resource, offset, resourceAccess, cancelIndicator);
    }

    private EObject resolveSuperTarget(RosettaSuperCall call) {
        return call.getSuperFunction();
    }
}
