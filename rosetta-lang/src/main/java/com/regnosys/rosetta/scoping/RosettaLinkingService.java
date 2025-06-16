package com.regnosys.rosetta.scoping;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.linking.impl.ImportedNamesAdapter;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.scoping.impl.IDelegatingScopeProvider;

import java.util.Stack;

/*
 * Fix change detection when recursive scope provider calls are involved.
 * The original implementation would nullify the `wrapper` of the scope provider after the call to a nested scope provider call
 * - this implementation restores the previous `wrapper` by keeping track of recursive calls in a stack.
 * TODO: contribute to Xtext
 */
public class RosettaLinkingService extends DefaultLinkingService {
    private final Stack<ImportedNamesAdapter> adapterStack = new Stack<>();

    @Override
    protected void registerImportedNamesAdapter(IScopeProvider scopeProvider, EObject context) {
        ImportedNamesAdapter adapter = getImportedNamesAdapter(context);
        adapterStack.push(adapter);
        IDelegatingScopeProvider.setWrapper(scopeProvider, adapter);
    }

    @Override
    protected void unRegisterImportedNamesAdapter(IScopeProvider scopeProvider) {
        if (adapterStack.isEmpty()) {
            IDelegatingScopeProvider.setWrapper(scopeProvider, null);
        } else {
            adapterStack.pop();
            IDelegatingScopeProvider.setWrapper(scopeProvider, adapterStack.isEmpty() ? null : adapterStack.peek());
        }
    }
}
