package com.regnosys.rosetta.generator.java.scope;

import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.st.STJavaClassGenerator;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaScope;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.rosetta.model.lib.context.RuneScope;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import jakarta.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.stringtemplate.v4.ST;

import java.util.stream.Stream;

public class ScopeGenerator extends STJavaClassGenerator<RosettaScope, RGeneratedJavaClass<? extends RuneScope>> {
    @Inject
    private JavaTypeTranslator typeTranslator;
    
    @Override
    protected EObject getSource(RosettaScope object) {
        return object;
    }
    
    @Override
    protected Stream<? extends RosettaScope> streamObjects(RosettaModel model) {
        RosettaScope scope = model.getScope();
        return Stream.ofNullable(scope);
    }

    @Override
    protected RGeneratedJavaClass<? extends RuneScope> createTypeRepresentation(RosettaScope object) {
        return RGeneratedJavaClass.create(JavaPackageName.escape(DottedPath.splitOnDots(object.getModel().getName())), object.getName(), RuneScope.class);
    }

    @Override
    protected ST generateClass(RosettaScope object, RGeneratedJavaClass<? extends RuneScope> typeRepresentation, String version, JavaClassScope scope) {
        ScopeTemplate template = new ScopeTemplate(object.getName());
        for (RosettaRootElement elem : object.getModel().getElements()) {
            if (elem instanceof Function f) {
                Function superFunction = f.getSuperFunction();
                if (superFunction != null) {
                    JavaClass<?> from = typeTranslator.toFunctionJavaClass(superFunction);
                    JavaClass<?> to = typeTranslator.toFunctionJavaClass(f);
                    template.addOverride(from, to);
                }
            }
        }
        return loadTemplate(template);
    }
}
