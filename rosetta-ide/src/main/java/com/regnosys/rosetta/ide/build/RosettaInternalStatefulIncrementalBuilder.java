package com.regnosys.rosetta.ide.build;

import com.regnosys.rosetta.generator.GenerationException;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.build.BuildRequest;
import org.eclipse.xtext.build.IncrementalBuilder.InternalStatefulIncrementalBuilder;
import org.eclipse.xtext.build.Source2GeneratedMapping;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.Issue.IssueImpl;

public class RosettaInternalStatefulIncrementalBuilder extends InternalStatefulIncrementalBuilder {

    @Override
    protected void generate(Resource resource, BuildRequest request, Source2GeneratedMapping newMappings) {
        // TODO Auto-generated method stub
        try {
            super.generate(resource, request, newMappings);
        } catch (GenerationException e) {
            System.err.println(e.getMessage());
            //TODO: create a meaningfull issue
            IssueImpl issue = new IssueImpl();
            issue.setSeverity(Severity.ERROR);
            issue.setMessage("its broken");
            getRequest().getAfterValidate().afterValidate(e.getResourceUri(), List.of(issue));
        }
    }
    
    
    

}
