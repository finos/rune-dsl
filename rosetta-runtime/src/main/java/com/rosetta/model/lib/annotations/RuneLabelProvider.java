package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.rosetta.model.lib.functions.LabelProvider;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RuneLabelProvider {
    Class<? extends LabelProvider> labelProvider();
}
