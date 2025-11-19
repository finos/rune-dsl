package com.regnosys.rosetta.generator.java.st;

import com.rosetta.util.types.JavaClass;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;

public class STTemplateConfigurator {
    private final ST st;
    
    private final List<JavaClass<?>> imports = new ArrayList<>();
    
    public STTemplateConfigurator(ST st) {
        this.st = st;
    }
    
    public void addArgument(String key, Object value) {
        st.add(key, value);
    }
    public void addImport(JavaClass<?> clazz) {
        imports.add(clazz);
    }
    public void addImport(Class<?> clazz) {
        addImport(JavaClass.from(clazz));
    }
    
    public List<JavaClass<?>> getImports() {
        return imports;
    }
}
