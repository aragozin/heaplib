/**
 * Copyright 2014 Alexey Ragozin
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

public class PredicateStep extends PathStep {

    private PathStep[] path;
    private InstanceFunction func;
    private PathStep lastStep;
    private String matcher;
    private boolean inverted;

    public PredicateStep(PathStep[] path, String matcher, boolean inverted) {
        if (path.length > 0 && isTailingFunction(path)) {
            this.path = Arrays.copyOf(path, path.length - 1);
            this.func = ((FunctionStep) path[path.length - 1]).func;
        }
        else {
            this.path = path;
            this.func = ValueOfFunc.INSTANCE;
        }
        if (this.path.length > 0 && mayYeildPrimitive(path)) {
            this.lastStep = this.path[this.path.length - 1];
            this.path = Arrays.copyOf(this.path, this.path.length - 1);
        }
        this.matcher = matcher;
        this.inverted = inverted;
    }

    private boolean isTailingFunction(PathStep[] path) {
        return path.length > 0 && path[path.length - 1] instanceof FunctionStep;
    }

    private boolean mayYeildPrimitive(PathStep[] path) {
        return path.length > 0 && (path[path.length - 1] instanceof FieldStep || path[path.length - 1] instanceof ArrayIndexStep);
    }

    @Override
    public Iterator<Instance> walk(Instance instance) {
        if (instance != null && evaluate(instance)) {
            return Collections.singleton(instance).iterator();
        }
        else {
            return Collections.<Instance>emptyList().iterator();
        }
    }

    @Override
    public Iterator<Move> track(Instance instance) {
        if (instance != null && evaluate(instance)) {
            return Collections.singleton(new Move("", instance)).iterator();
        }
        else {
            return Collections.<Move>emptyList().iterator();
        }
    }

    protected boolean evaluate(Instance instance) {
        for(Instance i: HeapWalker.collect(instance, path)) {
            if (lastStep instanceof FieldStep) {
                String fname = ((FieldStep) lastStep).getFieldName();
                for(FieldValue fv: i.getFieldValues()) {
                    if ((fname == null && fv.getField().isStatic())
                            || (fname.equals(fv.getField().getName()))) {

                        Object obj = func.applyToField(i, fv);

                        if (evaluate(obj)) {
                            return !inverted;
                        }
                    }
                }
            }
            else if (lastStep instanceof ArrayIndexStep) {
                if (i instanceof PrimitiveArrayInstance) {
                    PrimitiveArrayInstance array = (PrimitiveArrayInstance) i;
                    int n = ((ArrayIndexStep) lastStep).getIndex();
                    if (n < 0) {
                        for (int j = 0; j != array.getLength(); ++j) {
                            Object obj = func.applyToArray(array, j);

                            if (evaluate(obj)) {
                                return !inverted;
                            }
                        }
                    } else {
                        Object obj = func.applyToArray(array, n);

                        if (evaluate(obj)) {
                            return !inverted;
                        }
                    }
                } else {
                    Iterator<Instance> it = lastStep.walk(i);
                    while(it.hasNext()) {

                        Instance ii = it.next();
                        Object obj = func.apply(ii);

                        if (evaluate(obj)) {
                            return !inverted;
                        }
                    }
                }
            }
            else {

                Object obj = func.apply(i);
                if (evaluate(obj)) {
                    return !inverted;
                }
            }
        }
        return inverted;
    }

    private boolean evaluate(Object value) {
        if (!(value instanceof Instance)) {
            String str = String.valueOf(value);
            if (str.equals(matcher)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(PathStep step: path) {
            sb.append(step).append(", ");
        }
        if (lastStep != null) {
            sb.append(lastStep).append(", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(inverted ? "!=" : "=").append(matcher).append("]");
        return sb.toString();
    }
}
