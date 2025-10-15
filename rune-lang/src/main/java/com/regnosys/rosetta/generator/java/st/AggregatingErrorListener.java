package com.regnosys.rosetta.generator.java.st;

import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

import java.util.ArrayList;
import java.util.List;

public class AggregatingErrorListener implements STErrorListener {
    private final List<STMessage> all = new ArrayList<>();

    @Override
    public void compileTimeError(STMessage msg) {
        all.add(msg);
    }

    @Override
    public void runTimeError(STMessage msg) {
        all.add(msg);
    }

    @Override
    public void IOError(STMessage msg) {
        all.add(msg);
    }

    @Override
    public void internalError(STMessage msg) {
        all.add(msg);
    }

    public boolean hasErrors() {
        return !all.isEmpty();
    }

    public List<STMessage> getErrors() {
        return all;
    }
}
