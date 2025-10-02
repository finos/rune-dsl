package com.regnosys.rosetta.ide.build;

import com.regnosys.rosetta.generator.AggregateGenerationException;
import com.regnosys.rosetta.generator.GenerationException;

import java.util.List;

import com.regnosys.rosetta.utils.EnvironmentUtil;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.build.BuildRequest;
import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.build.IncrementalBuilder.InternalStatefulIncrementalBuilder;
import org.eclipse.xtext.build.Source2GeneratedMapping;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.ITextRegionWithLineInformation;
import org.eclipse.xtext.util.LineAndColumn;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.Issue.IssueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * This class handles errors thrown during code generation and sends them back to the
 * client in the form of diagnostics.
 * 
 * It also supports turning on logging of build statistics by setting an environment variable to `true`.
 * If enabled, for each build, it will log:
 * 1. At the start of the build, which files are dirty.
 * 2. At the end of the build, how many source files were validated, how many target files were generated or deleted,
 *    and whether the build actually finished. The latter may not be true if the build was canceled or an unexpected
 *    error occurred during the build.
 * See {@code INCREMENTAL_BUILDER_STATISTICS_VARIABLE_NAME} for the name of the variable.
 */
public class RosettaStatefulIncrementalBuilder extends InternalStatefulIncrementalBuilder {
    public static final String ISSUE_CODE = RosettaStatefulIncrementalBuilder.class.getName() + ".generationError";

    public static final String INCREMENTAL_BUILDER_STATISTICS_VARIABLE_NAME = "ENABLE_INCREMENTAL_BUILDER_STATISTICS";
    private static final boolean INCREMENTAL_BUILDER_STATISTICS_ENABLED = EnvironmentUtil.getBooleanOrDefault(INCREMENTAL_BUILDER_STATISTICS_VARIABLE_NAME, false);
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RosettaStatefulIncrementalBuilder.class);
    
    private int sourceFilesValidated = 0;
    private int targetFilesGenerated = 0;
    private int targetFilesDeleted = 0;
    
    @Override
    public IncrementalBuilder.Result launch() {
        if (!hasAnyChanges()) {
            // Prevent empty builds from cluttering the logs.
            return new IncrementalBuilder.Result(getRequest().getState(), List.of());
        }
        if (INCREMENTAL_BUILDER_STATISTICS_ENABLED) {
            resetBuildStatistics();
            IncrementalBuilder.Result buildResult = null;
            try {
                buildResult = super.launch();
            } finally {
                logBuildStatistics(buildResult);
            }
            return buildResult;
        }
        return super.launch();
    }
    
    private boolean hasAnyChanges() {
        BuildRequest request = getRequest();
        return !request.getDirtyFiles().isEmpty() || !request.getDeletedFiles().isEmpty() || !request.getExternalDeltas().isEmpty();
    }
    
    private void resetBuildStatistics() {
        sourceFilesValidated = 0;
        targetFilesGenerated = 0;
        targetFilesDeleted = 0;
        BuildRequest request = getRequest();
        
        var oldAfterValidate = request.getAfterValidate();
        request.setAfterValidate((validated, issues) -> {
            sourceFilesValidated++;
            return oldAfterValidate.afterValidate(validated, issues);
        });
        
        var oldAfterDeleteFile = request.getAfterDeleteFile();
        request.setAfterDeleteFile((uri) -> {
            targetFilesDeleted++;
            oldAfterDeleteFile.apply(uri);
        });
        
        var oldAfterGenerateFile = request.getAfterGenerateFile();
        request.setAfterGenerateFile((sourceUri, generated) -> {
            targetFilesGenerated++;
            oldAfterGenerateFile.apply(sourceUri, generated);
        });
        
        List<String> changedFiles = Stream.concat(
                request.getDirtyFiles().stream(),
                request.getDeletedFiles().stream()
            ).map(Object::toString).toList();
        LOGGER.info("Starting build for {} dirty source files: {}", changedFiles.size(), changedFiles);
    }
    private void logBuildStatistics(@Nullable IncrementalBuilder.Result buildResult) {
        if (buildResult == null) {
            LOGGER.info("Build STOPPED  - validated {} files, generated {} files, deleted {} files", sourceFilesValidated, targetFilesGenerated, targetFilesDeleted);
        } else {
            LOGGER.info("Build FINISHED - validated {} files, generated {} files, deleted {} files", sourceFilesValidated, targetFilesGenerated, targetFilesDeleted);
        }
    }
    
    @Override
    protected void generate(Resource resource, BuildRequest request, Source2GeneratedMapping newMappings) {
        try {
            super.generate(resource, request, newMappings);
        } catch (AggregateGenerationException e) {
            List<Issue> issues = e.getGenerationExceptions().stream().map(this::constructIssue).toList();
            getRequest().getAfterValidate().afterValidate(e.getResourceUri(), issues);
        } catch (GenerationException e) {
            getRequest().getAfterValidate().afterValidate(e.getResourceUri(), List.of(constructIssue(e)));
        }
    }

    private Issue constructIssue(GenerationException e) {
        IssueImpl issue = new IssueImpl();
        issue.setSeverity(Severity.ERROR);
        issue.setCode(ISSUE_CODE);
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
