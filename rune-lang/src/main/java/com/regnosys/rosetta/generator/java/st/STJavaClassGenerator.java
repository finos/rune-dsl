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
    private STTemplateLoader templateLoader;
    
    protected abstract ST generateClass(T object, C typeRepresentation, String version, JavaClassScope scope);
    
    protected ST loadTemplate(STTemplate template) {
        return templateLoader.loadTemplate(template);
    }

    @Override
    protected String generate(T object, C typeRepresentation, String version, JavaClassScope scope, JavaGeneratorErrorHandler errorHandler) {
        ST st = generateClass(object, typeRepresentation, version, scope);
        AggregatingErrorListener errorListener = new AggregatingErrorListener();
        // TODO: pass input stream to fsa2
        try (InputStream generatedCodeStream = ProducerInputStreams.fromOutputStream(os -> writeTemplateToOutputStream(st, os, errorListener))) {
            handleErrors(errorListener, errorHandler);
            return new String(generatedCodeStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            errorHandler.handleError(e);
            return getStackTrace(e);
        }
    }
    
    private void writeTemplateToOutputStream(ST st, OutputStream os, AggregatingErrorListener errorListener) throws IOException {
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
    
    private void handleErrors(AggregatingErrorListener errorListener, JavaGeneratorErrorHandler errorHandler) {
        for (STMessage err : errorListener.getErrors()) {
            String errorMessage = String.format(err.error.message, err.arg, err.arg2, err.arg3);
            if (err.cause != null) {
                errorHandler.handleError(errorMessage, err.cause);
            } else {
                errorHandler.handleError(errorMessage);
            }
        }
    }
}
