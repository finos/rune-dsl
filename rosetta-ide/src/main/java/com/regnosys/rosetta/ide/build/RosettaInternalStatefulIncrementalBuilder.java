package com.regnosys.rosetta.ide.build;

import com.regnosys.rosetta.generator.GenerationException;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.build.BuildRequest;
import org.eclipse.xtext.build.IncrementalBuilder.InternalStatefulIncrementalBuilder;
import org.eclipse.xtext.build.Source2GeneratedMapping;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.ITextRegionWithLineInformation;
import org.eclipse.xtext.util.LineAndColumn;
import org.eclipse.xtext.validation.DiagnosticConverterImpl;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.Issue.IssueImpl;

import javax.inject.Inject;

public class RosettaInternalStatefulIncrementalBuilder extends InternalStatefulIncrementalBuilder {

    @Override
    protected void generate(Resource resource, BuildRequest request, Source2GeneratedMapping newMappings) {
        try {
            super.generate(resource, request, newMappings);
        } catch (GenerationException e) {
            getRequest().getAfterValidate().afterValidate(e.getResourceUri(), List.of(constructIssue(e)));
        }
    }

    private Issue constructIssue(GenerationException e) {
        IssueImpl issue = new IssueImpl();
        issue.setSeverity(Severity.ERROR);
        issue.setMessage(e.getMessage() == null ? "Error during generation" : e.getMessage());
        ICompositeNode iNode = NodeModelUtils.findActualNodeFor(e.getContext());
        if (iNode != null) {
            setLocationForNode(iNode, issue);
        }
        return issue;
    }

    /*
     * This logic is copied from DiagnosticConverterImpl.getLocationForNode(INode node)
     */
    private void setLocationForNode(INode node, IssueImpl issue) {
        ITextRegionWithLineInformation nodeRegion = node.getTextRegionWithLineInformation();
        int offset = nodeRegion.getOffset();
        int length = nodeRegion.getLength();
        setLocationForNode(node, offset, length, issue);
    }

    private void setLocationForNode(INode node, int offset, int length, IssueImpl issue) {
        issue.setOffset(offset);
        issue.setLength(length);

        LineAndColumn lineAndColumnStart = NodeModelUtils.getLineAndColumn(node, offset);
        issue.setLineNumber(lineAndColumnStart.getLine());
        issue.setColumn(lineAndColumnStart.getColumn());

        LineAndColumn lineAndColumnEnd = NodeModelUtils.getLineAndColumn(node, offset + length);
        issue.setLineNumberEnd(lineAndColumnEnd.getLine());
        issue.setColumnEnd(lineAndColumnEnd.getColumn());
    }
}
