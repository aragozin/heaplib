package org.gridkit.jvmtool.heapdump;

import java.util.Iterator;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;

public class InstanceIterator implements Iterator<Instance> {

    private final Heap heap;

    private final Iterator<Long> it;

    public InstanceIterator(Heap heap, Iterator<Long> it) {
        this.heap = heap;
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Instance next() {
        return heap.getInstanceByID(it.next());
    }
}
