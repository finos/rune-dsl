/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.codegen.api;

import java.util.Objects;

/**
 * Configuration of a {@link CodeWriter}: the newline string and the string
 * used per indentation level. Defaults to {@code "\n"} and four spaces.
 *
 * <p>Construct using the builder, e.g.,
 * {@code CodeWriterConfig.builder().indent("\t").build()}.
 */
public final class CodeWriterConfig {
    public static final CodeWriterConfig DEFAULT = builder().build();

    private final String newline;
    private final String indent;

    private CodeWriterConfig(Builder builder) {
        this.newline = builder.newline;
        this.indent = builder.indent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getNewline() {
        return newline;
    }

    public String getIndent() {
        return indent;
    }

    public static final class Builder {
        private String newline = "\n";
        private String indent = "    ";

        private Builder() {
        }

        public Builder newline(String newline) {
            this.newline = Objects.requireNonNull(newline);
            return this;
        }

        public Builder indent(String indent) {
            this.indent = Objects.requireNonNull(indent);
            return this;
        }

        public CodeWriterConfig build() {
            return new CodeWriterConfig(this);
        }
    }
}
