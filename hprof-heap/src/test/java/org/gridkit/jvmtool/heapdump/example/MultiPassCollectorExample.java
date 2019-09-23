package org.gridkit.jvmtool.heapdump.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.gridkit.jvmtool.heapdump.RefSet;
import org.junit.Test;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;

public class MultiPassCollectorExample {

    /**
     * This entry point for this example.
     */
    @Test
    public void check() throws FileNotFoundException, IOException {
        String dumppath = "src/test/resources/org/netbeans/lib/profiler/heap/heap_dump.bin";
        Heap heap = HeapFactory.createFastHeap(new File(dumppath));
        collectBoxedIntegerReferrers(heap);
    }

    public void collectBoxedIntegerReferrers(Heap heap) {

        HeapHistogram histo = new HeapHistogram();

        RefSet refSet = new RefSet();

        JavaClass integerClass = heap.getJavaClassByName(Integer.class.getName());

        // pass collect addresses of all boxed integers
        for(Instance i: integerClass.getInstances()) {
            refSet.set(i.getInstanceId(), true);
        }

        for(Instance i: heap.getAllInstances()) {
            if (i instanceof ObjectArrayInstance) {
                ObjectArrayInstance a = (ObjectArrayInstance) i;
                for(Long ref: a.getValueIDs()) {
                    if (ref != null && refSet.get(ref)) {
                        // ref points to java.lang.Integer
                        histo.accumulate(i);
                    }
                }
            }
            else {
                for(FieldValue fv: i.getFieldValues()) {
                    if (fv instanceof ObjectFieldValue) {
                        long refId = ((ObjectFieldValue)fv).getInstanceId();
                        if (refSet.get(refId)) {
                            // ref points to java.lang.Integer
                            histo.accumulate(i);
                        }
                    }
                }
            }
        }

        System.out.println("In histogram below, 'Count' is number");
        System.out.println("of outbound links to java.lang.Integer");
        System.out.println("for instance of class");
        System.out.println("'Size' does mean nothing");

        System.out.println(histo.toString());
    }
}
