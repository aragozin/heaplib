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

import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

/**
 * Function to extract class name from instance
 */
class ClassNameFunc implements InstanceFunction {

    public final static ClassNameFunc SIMPLE_NAME = new ClassNameFunc("simpleClassName");

    public final static ClassNameFunc FQ_NAME = new ClassNameFunc("className");

    final String func;

    public ClassNameFunc(String func) {
        this.func = func;
    }

    @Override
    public Object apply(Instance i) {
        return applyToTypeName(i.getJavaClass().getName());
    }

    private String applyToTypeName(String typeName) {
        if (SIMPLE_NAME.func.equals(func)) {
            return HeapHelper.simpleClassName(typeName);
        } else if (FQ_NAME.func.equals(func)) {
            return typeName;
        } else {
            throw new UnsupportedOperationException("Unknown class name function: " + func);
        }
    }

    @Override
    public Object applyToField(Instance i, FieldValue fv) {
        if (fv instanceof ObjectFieldValue) {
            return apply(((ObjectFieldValue)fv).getInstance());
        } else {
            return applyToTypeName(fv.getField().getType().getName());
        }
    }

    @Override
    public Object applyToArray(PrimitiveArrayInstance array, int index) {
        String typeName = array.getJavaClass().getName();
        if (typeName.endsWith("[]")) {
            typeName = typeName.substring(0, typeName.length() - 2);
        }
        return applyToTypeName(typeName);
    }

    @Override
    public String toString() {
        return "?" + func;
    }
}
