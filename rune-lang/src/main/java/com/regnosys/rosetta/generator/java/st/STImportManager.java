package com.regnosys.rosetta.generator.java.st;

import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaTypeDeclaration;
import jakarta.inject.Inject;
import org.stringtemplate.v4.ST;

import java.util.Comparator;
import java.util.List;

public class STImportManager {
    @Inject
    private STTemplateLoader templateLoader;
    
    public ST toJavaFileTemplate(JavaTypeDeclaration<?> type, STTemplate classTemplate) {
        ST loadedClassTemplate = templateLoader.loadTemplate(classTemplate);
        STTemplateConfigurator classTemplateConfigurator = new STTemplateConfigurator(loadedClassTemplate);
        classTemplate.configure(classTemplateConfigurator);
        
        List<JavaClass<?>> imports = classTemplateConfigurator.getImports();
        imports.sort(Comparator.comparing(JavaClass::getCanonicalName));
        JavaFileTemplate fileTemplate = new JavaFileTemplate(type.getPackageName(), imports, loadedClassTemplate);
        ST loadedFileTemplate = templateLoader.loadTemplate(fileTemplate);
        STTemplateConfigurator fileTemplateConfigurator = new STTemplateConfigurator(loadedFileTemplate);
        fileTemplate.configure(fileTemplateConfigurator);
        
        return loadedFileTemplate;
    }
    
    private static class JavaFileTemplate extends STTemplate {
        private final DottedPath packageName;
        private final List<JavaClass<?>> imports;
        private final ST loadedClassTemplate;
        
        public JavaFileTemplate(DottedPath packageName, List<JavaClass<?>> imports, ST loadedClassTemplate) {
            super("templates/imports/imports.stg", "fullJavaFile");
            this.packageName = packageName;
            this.imports = imports;
            this.loadedClassTemplate = loadedClassTemplate;
        }

        @Override
        protected void configure(STTemplateConfigurator configurator) {
            configurator.addArgument("packageName", packageName);
            configurator.addArgument("imports", imports);
            configurator.addArgument("renderClass", loadedClassTemplate);
        }
    }
}
