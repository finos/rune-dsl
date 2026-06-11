package com.regnosys.rosetta.codegen.api;

@FunctionalInterface
public interface CodeRenderer {
    void render(CodeWriter out);
}
