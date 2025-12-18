package com.regnosys.rosetta.generator.java.scope;


import com.regnosys.rosetta.generator.java.st.STTemplate;
import com.regnosys.rosetta.generator.java.st.STTemplateConfigurator;
import com.rosetta.model.lib.context.AbstractRuneScope;
import com.rosetta.util.types.JavaClass;

import java.util.ArrayList;
import java.util.List;

public class ScopeTemplate extends STTemplate {
    private final String name;
    private final List<OverridePair> overrides = new ArrayList<>();
    
    public ScopeTemplate(String name) {
        super("templates/scope/scope.stg", "scopeClass");
        this.name = name;
    }

    @Override
    protected void configure(STTemplateConfigurator configurator) {
        configurator.addArgument("scopeName", name);
        configurator.addArgument("overrides", overrides);
        
        configurator.addImport(AbstractRuneScope.class);
        for (OverridePair override : overrides) {
            configurator.addImport(override.from());
            configurator.addImport(override.to());
        }
    }

    public String getName() {
        return name;
    }
    
    public List<OverridePair> getOverrides() {
        return overrides;
    }
    public void addOverride(JavaClass<?> from, JavaClass<?> to) {
        overrides.add(new OverridePair(from, to));
    }

    public record OverridePair(JavaClass<?> from, JavaClass<?> to) {}
}
