package com.regnosys.rosetta.generator.java.st;

import jakarta.inject.Singleton;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton // Singleton to make sure we compile the templates only once
public class STTemplateLoader {
    private final Map<String, STGroup> templateCache = new ConcurrentHashMap<>();
    
    public ST loadTemplate(STTemplate template) {
        STGroup group = getGroup(template.getGroupFile());
        return group.getInstanceOf(template.getTemplateName());
    }
    
    private STGroup getGroup(String groupFile) {
        return templateCache.computeIfAbsent(groupFile, this::doGetGroup);
    }
    private STGroup doGetGroup(String groupFile) {
        STGroup group = new STGroupFile(groupFile);
        group.setListener(new STThrowingErrorListener(groupFile));
        group.registerModelAdaptor(Record.class, new RecordModelAdaptor());
        return group;
    }
}
