package com.regnosys.rosetta.generator.java.scope;


import com.regnosys.rosetta.generator.java.st.STTemplate;
import com.rosetta.util.types.JavaClass;
import org.stringtemplate.v4.ST;

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
    protected void applyArguments(ST st) {
        st.add("scopeName", name);
        st.add("overrides", overrides);
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
