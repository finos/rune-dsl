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

package com.regnosys.rosetta.generator.java.scoping;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaRule;

public class RuleOutputParameterRepresentation {
    private final RosettaRule rule;
    public RuleOutputParameterRepresentation(RosettaRule rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return "RuleInputParameter[" + rule.getName() + "]";
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), rule);
    }
    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        RuleOutputParameterRepresentation other = (RuleOutputParameterRepresentation) object;
        return Objects.equals(rule, other.rule);
    }
}
