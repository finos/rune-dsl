package com.regnosys.rosetta.generator.java.st;

import com.regnosys.rosetta.generator.java.JavaClassGenerator;
import com.regnosys.rosetta.generator.java.JavaGeneratorErrorHandler;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.rosetta.util.types.JavaTypeDeclaration;
import jakarta.inject.Inject;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STWriter;
import org.stringtemplate.v4.misc.STMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public abstract class STJavaClassGenerator<T, C extends JavaTypeDeclaration<?>> extends JavaClassGenerator<T, C> {
    @Inject
    private STImportManager importManager;
    
    protected abstract STTemplate generateClass(T object, C typeRepresentation, String version, JavaClassScope scope);

    @Override
    protected String generate(T object, C typeRepresentation, String version, JavaClassScope scope, JavaGeneratorErrorHandler errorHandler) {
        STTemplate classTemplate = generateClass(object, typeRepresentation, version, scope);
        ST javaFileTemplate = importManager.toJavaFileTemplate(typeRepresentation, classTemplate);
        
        STJavaGeneratorErrorListener errorListener = new STJavaGeneratorErrorListener(errorHandler);
        // TODO: pass input stream to fsa2
        try (InputStream generatedCodeStream = ProducerInputStreams.fromOutputStream(os -> writeTemplateToOutputStream(javaFileTemplate, os, errorListener))) {
            return new String(generatedCodeStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            errorHandler.handleError(e);
            return getStackTrace(e);
        }
    }
    
    private void writeTemplateToOutputStream(ST st, OutputStream os, STJavaGeneratorErrorListener errorListener) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            STWriter writer = new AutoIndentWriter(osw, "\n");
            writer.setLineWidth(STWriter.NO_WRAP);
            st.write(writer, Locale.ROOT, errorListener);
        }
    }
    
    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
