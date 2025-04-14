package com.regnosys.rosetta.ide.build;

import java.util.ArrayList;

import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.build.IncrementalBuilder.InternalStatefulIncrementalBuilder;
import org.eclipse.xtext.build.IncrementalBuilder.Result;
import org.eclipse.xtext.build.IndexState;
import org.eclipse.xtext.resource.IResourceDescription;

import com.regnosys.rosetta.generator.GenerationException;

public class RosettaInternalStatefulIncrementalBuilder extends InternalStatefulIncrementalBuilder {

    @Override
    public Result launch() {
        try {
            return super.launch();
        } catch (GenerationException e) {
            System.err.println(e.getMessage());
            return  new IncrementalBuilder.Result(new IndexState(), new ArrayList<IResourceDescription.Delta>());
        }
    }
    

}
