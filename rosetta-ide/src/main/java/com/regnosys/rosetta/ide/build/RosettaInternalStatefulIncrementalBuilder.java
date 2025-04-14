package com.regnosys.rosetta.ide.build;

import com.regnosys.rosetta.generator.GenerationException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.build.BuildRequest;
import org.eclipse.xtext.build.IncrementalBuilder.InternalStatefulIncrementalBuilder;
import org.eclipse.xtext.build.Source2GeneratedMapping;

public class RosettaInternalStatefulIncrementalBuilder extends InternalStatefulIncrementalBuilder {

    @Override
    protected void generate(Resource resource, BuildRequest request, Source2GeneratedMapping newMappings) {
        // TODO Auto-generated method stub
        try {
        super.generate(resource, request, newMappings);
        } catch (GenerationException e) {
            System.err.println(e.getMessage());
            //TODO: make call to afterValidate here with list of issues
//            getRequest().getAfterValidate().afterValidate()
        }
    }
    
    
    

}
