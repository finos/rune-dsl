package com.regnosys.rosetta.tests.util;

import com.regnosys.rosetta.resource.RosettaResource;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.diagnostics.IDiagnosticConsumer;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.linking.lazy.LazyLinker;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.resource.XtextSyntaxDiagnosticWithRange;
import org.eclipse.xtext.resource.impl.ListBasedDiagnosticConsumer;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.util.CancelIndicator;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExpressionParser {
    @Inject
    private IParser parser;
    @Inject
    private RosettaGrammarAccess grammar;
    @Inject
    private ModelHelper modelHelper;
    @Inject
    private Provider<ExpressionResource> resourceProvider;
    @Inject
    private RosettaStaticLinker linker;

    public RosettaExpression parseExpression(CharSequence expr) {
        return parseExpression(expr, List.of());
    }

    public RosettaExpression parseExpression(CharSequence expr, Collection<? extends CharSequence> attrs) {
        return parseExpression(expr, List.of(), attrs);
    }

    public RosettaExpression parseExpression(CharSequence expr, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
        List<Attribute> attributes = attrs.stream().map(a -> parseAttribute(a, context)).toList();
        return parseExpression(expr, context, attributes.toArray(Attribute[]::new));
    }

    public RosettaExpression parseExpression(CharSequence expr, Attribute... attributes) {
        return parseExpression(expr, List.of(), attributes);
    }

    public RosettaExpression parseExpression(CharSequence expr, List<RosettaModel> context, Attribute... attributes) {
        List<RosettaModel> cont = context.isEmpty() ? defaultContext() : context;
        IParseResult result = parser.parse(grammar.getExpressionRule(), new StringReader(expr.toString()));
        RosettaExpression expression = (RosettaExpression) result.getRootASTElement();
        addSyntaxDiagnostics(createResource("expr", expression, cont), result);
        link(expression, cont, List.of(attributes));
        return expression;
    }

    public Attribute parseAttribute(CharSequence attr) {
        return parseAttribute(attr, defaultContext());
    }

    public Attribute parseAttribute(CharSequence attr, List<RosettaModel> context) {
        List<RosettaModel> cont = context.isEmpty() ? defaultContext() : context;
        IParseResult result = parser.parse(grammar.getAttributeRule(), new StringReader(attr.toString()));
        Attribute attribute = (Attribute) result.getRootASTElement();
        addSyntaxDiagnostics(createResource("attribute", attribute, cont), result);
        link(attribute, context, List.of());
        return attribute;
    }

    private void addSyntaxDiagnostics(Resource resource, IParseResult parseResult) {
        for (INode error : parseResult.getSyntaxErrors()) {
            var syntaxErrorMessage = error.getSyntaxErrorMessage();
            if (Diagnostic.SYNTAX_DIAGNOSTIC_WITH_RANGE.equals(syntaxErrorMessage.getIssueCode())) {
                String[] issueData = syntaxErrorMessage.getIssueData();
                if (issueData.length == 1) {
                    String data = issueData[0];
                    int colon = data.indexOf(':');
                    resource.getErrors().add(new XtextSyntaxDiagnosticWithRange(error, Integer.parseInt(data.substring(0, colon)),
                            Integer.parseInt(data.substring(colon + 1)), null));
                    return;
                }
            }
            resource.getErrors().add(new XtextSyntaxDiagnostic(error));
        }
    }

    private ExpressionResource createResource(String name, EObject content, List<RosettaModel> context) {
        var resourceSet = context.getFirst().eResource().getResourceSet();
        int nr = 0;
        URI uniqueURI = URI.createURI("synthetic://" + name + nr++);
        while (resourceSet.getResource(uniqueURI, false) != null) {
            uniqueURI = URI.createURI("synthetic://" + name + nr++);
        }
        ExpressionResource resource = resourceProvider.get();
        resource.setURI(uniqueURI);
        resource.getContents().add(content);
        resourceSet.getResources().add(resource);
        return resource;
    }

    private List<RosettaModel> defaultContext() {
        var rs = modelHelper.testResourceSet();
        List<RosettaModel> result = new ArrayList<>();
        rs.getResources().forEach(r -> result.add((RosettaModel) r.getContents().getFirst()));
        return result;
    }

    private void link(EObject obj, List<RosettaModel> context, Collection<? extends EObject> globals) {
        ExpressionResource resource = (ExpressionResource) obj.eResource();

        linker.setStateForNextLink(context, globals);
        var consumer = new ListBasedDiagnosticConsumer();
        resource.setLoading(true);
        linker.linkModel(obj, consumer);
        resource.setLoading(false);

        obj.eResource().getErrors().addAll(consumer.getResult(Severity.ERROR));

        EcoreUtil2.resolveLazyCrossReferences(obj.eResource(), CancelIndicator.NullImpl);
    }

    private static class RosettaContextBasedScopeProvider extends RosettaScopeProvider {
        List<RosettaModel> context = List.of();

        public void setContext(List<RosettaModel> context) {
            this.context = context;
        }

        @Override
        protected List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
            List<ImportNormalizer> base = super.getImplicitImports(ignoreCase);
            List<ImportNormalizer> extra = context.stream()
                    .map(RosettaModel::getName)
                    .distinct()
                    .map(ns -> createImportedNamespaceResolver(ns + ".*", ignoreCase))
                    .toList();
            List<ImportNormalizer> merged = new ArrayList<>(base.size() + extra.size());
            merged.addAll(base);
            merged.addAll(extra);
            return merged;
        }
    }

    private static class RosettaStaticLinker extends LazyLinker {
        @Inject
        RosettaContextBasedScopeProvider scopeProvider;

        IScope staticScope = IScope.NULLSCOPE;

        public void setStateForNextLink(List<RosettaModel> context, Collection<? extends EObject> globals) {
            scopeProvider.setContext(context);
            staticScope = Scopes.scopeFor(globals);
        }

        private void clearState() {
            scopeProvider.setContext(List.of());
            staticScope = IScope.NULLSCOPE;
        }

        @Override
        protected void doLinkModel(EObject root, IDiagnosticConsumer consumer) {
            // TODO: this is hacky
            ((DefaultLinkingService) ((LazyLinkingResource) root.eResource()).getLinkingService()).setScopeProvider(scopeProvider);

            super.doLinkModel(root, consumer);
            EcoreUtil2.resolveAll(root);
            clearState();
        }

        @Override
        protected void createAndSetProxy(EObject obj, INode node, EReference eRef) {
            String varName = NodeModelUtils.getTokenText(node);
            var staticElement = staticScope.getSingleElement(QualifiedName.create(varName));
            if (staticElement != null) {
                EObject resolved = staticElement.getEObjectOrProxy();
                if (eRef.isMany()) {
                    @SuppressWarnings("unchecked")
                    InternalEList<EObject> list = (InternalEList<EObject>) obj.eGet(eRef, false);
                    list.addUnique(resolved);
                } else {
                    obj.eSet(eRef, resolved);
                }
            } else {
                super.createAndSetProxy(obj, node, eRef);
            }
        }
    }

    private static class ExpressionResource extends RosettaResource {
        public void setLoading(boolean value) {
            this.isLoading = value;
        }
    }
}