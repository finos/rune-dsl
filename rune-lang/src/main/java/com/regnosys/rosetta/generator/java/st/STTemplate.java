package com.regnosys.rosetta.generator.java.st;

public abstract class STTemplate {
    private final String groupFile;
    private final String templateName;
    
    public STTemplate(String groupFile, String templateName) {
        this.groupFile = groupFile;
        this.templateName = templateName;
    }
    
    protected abstract void configure(STTemplateConfigurator configurator);
    
    public String getGroupFile() {
        return groupFile;
    }
    
    public String getTemplateName() {
        return templateName;
    }
}
