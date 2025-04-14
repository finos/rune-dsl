package com.regnosys.rosetta.ide.build;

import com.regnosys.rosetta.generator.GenerationException;
import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.build.IncrementalBuilder.InternalStatefulIncrementalBuilder;
import org.eclipse.xtext.build.IncrementalBuilder.Result;
import org.eclipse.xtext.build.IndexState;

import java.util.ArrayList;

public class RosettaInternalStatefulIncrementalBuilder extends InternalStatefulIncrementalBuilder {

    @Override
    public Result launch() {
        try {
            return super.launch();
        } catch (GenerationException e) {
            System.err.println(e.getMessage());
            //TODO: make call to afterValidate here with list of issues
//            getRequest().getAfterValidate().afterValidate()
            return  new IncrementalBuilder.Result(new IndexState(), new ArrayList<>());
        }
    }
    

}
