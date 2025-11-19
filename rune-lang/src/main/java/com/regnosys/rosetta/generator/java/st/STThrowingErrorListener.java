package com.regnosys.rosetta.generator.java.st;

import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

public class STThrowingErrorListener implements STErrorListener {
    private final String templateGroupName;
    
    public STThrowingErrorListener(String templateGroupName) {
        this.templateGroupName = templateGroupName;
    }
    
    private void handle(STMessage msg) {
        String errorMessage = "Error while parsing " + templateGroupName + ": " + String.format(msg.error.message, msg.arg, msg.arg2, msg.arg3);
        if (msg.cause != null) {
            throw new RuntimeException(errorMessage, msg.cause);
        } else {
            throw new RuntimeException(errorMessage);
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
