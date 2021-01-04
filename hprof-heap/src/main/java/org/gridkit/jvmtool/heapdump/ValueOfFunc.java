package org.gridkit.jvmtool.heapdump;

import java.lang.reflect.Array;

import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

class ValueOfFunc implements InstanceFunction {

    public static final ValueOfFunc INSTANCE = new ValueOfFunc();

    @Override
    public Object apply(Instance i) {
        return HeapWalker.valueOf(i);
    }

    @Override
    public Object applyToField(Instance i, FieldValue fv) {

        if (fv instanceof ObjectFieldValue) {
            return HeapWalker.valueOf(((ObjectFieldValue) fv).getInstance());
        } else {
            // have to use this as private package API is used behind scene
            return i.getValueOfField(fv.getField().getName());
        }
    }

    @Override
    public Object applyToArray(PrimitiveArrayInstance array, int n) {

        if (n < array.getLength()) {
            // n may be -1 for [*] step which is defaults to [0]
            int idx = Math.max(0, n);
            Object arrayObj = HeapWalker.valueOf(array);
            if (arrayObj != null && idx < Array.getLength(arrayObj)) {
                return Array.get(arrayObj, idx);
            }
        }

        return null;
    }
}
