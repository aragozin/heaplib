/**
 * Copyright 2021 Alexey Ragozin
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
package org.gridkit.jvmtool.heapdump;

import java.util.Iterator;

import org.netbeans.lib.profiler.heap.Instance;

/**
 * This is a special step.
 *
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class FunctionStep extends PathStep {

    public final InstanceFunction func;

    public FunctionStep(InstanceFunction func) {
        this.func = func;
    }

    @Override
    public Iterator<Instance> walk(Instance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Move> track(Instance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return func.toString();
    }
}
