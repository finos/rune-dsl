package com.regnosys.rosetta.generator.java.st;

import com.regnosys.rosetta.generator.java.JavaGeneratorErrorHandler;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

import java.util.ArrayList;
import java.util.List;

public class STJavaGeneratorErrorListener implements STErrorListener {
    private final JavaGeneratorErrorHandler handler;
    
    public STJavaGeneratorErrorListener(JavaGeneratorErrorHandler handler) {
        this.handler = handler;
    }
    
    private void handle(STMessage msg) {
        String errorMessage = String.format(msg.error.message, msg.arg, msg.arg2, msg.arg3);
        if (msg.cause != null) {
            handler.handleError(errorMessage, msg.cause);
        } else {
            handler.handleError(errorMessage);
        }
    }

    @Override
    public void compileTimeError(STMessage msg) {
        handle(msg);
    }

    @Override
    public void runTimeError(STMessage msg) {
        handle(msg);
    }

    @Override
    public void IOError(STMessage msg) {
        handle(msg);
    }

    @Override
    public void internalError(STMessage msg) {
        handle(msg);
    }
}
