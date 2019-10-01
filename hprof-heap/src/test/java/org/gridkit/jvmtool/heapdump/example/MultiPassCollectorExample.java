package org.gridkit.jvmtool.heapdump.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * This entry point for this example.
     */
    @Test
    public void check2() throws FileNotFoundException, IOException {
        String dumppath = "src/test/resources/org/netbeans/lib/profiler/heap/heap_dump.bin";
        Heap heap = HeapFactory.createHeap(new File(dumppath));
        printPathsForIntegers(heap);
    }

    public void printPathsForIntegers(Heap heap) {

        Map<Long, Long> count = new HashMap<Long, Long>();

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
                        inc(count, i);
                    }
                }
            }
            else {
                for(FieldValue fv: i.getFieldValues()) {
                    if (fv instanceof ObjectFieldValue) {
                        long refId = ((ObjectFieldValue)fv).getInstanceId();
                        if (refSet.get(refId)) {
                            // ref points to java.lang.Integer
                            inc(count, i);
                        }
                    }
                }
            }
        }

        for(Long id: count.keySet()) {
            Instance ref = heap.getInstanceByID(id);

            String pathToRoot = getPathToRoot(heap, ref);

            System.out.println(pathToRoot + " (" + count.get(id) + " java.lang.Integer references)");
        }

    }

    private String getPathToRoot(Heap heap, Instance ref) {
        String path = "";

        JavaClass jclass = heap.getJavaClassByName("java.lang.Class");

        while(true) {
            if (ref.isGCRoot()) {
                path = ref.getJavaClass().getName() + ":" + path;
                break;
            }
            Instance upref = ref.getNearestGCRootPointer();
            if (upref == null) {
                return "UNKNOWN(#" + ref.getInstanceId() + ")";
            }

            if (upref instanceof ObjectArrayInstance) {
                path = "[]" + path;
            }
            else if (upref.getJavaClass() == jclass) {
                JavaClass cls = heap.getJavaClassByID(upref.getInstanceId());
                for(FieldValue fv: cls.getStaticFieldValues()) {
                    if (fv instanceof ObjectFieldValue) {
                        if (ref.equals(((ObjectFieldValue)fv).getInstance())) {
                            path = fv.getField().getName() + ">" +path;
                            break;
                        }
                    }
                }
            }
            else {
                for(FieldValue fv: upref.getFieldValues()) {
                    if (fv instanceof ObjectFieldValue) {
                        if (ref.equals(((ObjectFieldValue)fv).getInstance())) {
                            path = fv.getField().getName() + ">" +path;
                            break;
                        }
                    }
                }
            }
            ref = upref;
        }

        return path;
    }

    private void inc(Map<Long, Long> count, Instance ref) {
        long id = ref.getInstanceId();
        if (!count.containsKey(id)) {
            count.put(id, 1l);
        }
        else {
            count.put(id, count.get(id) + 1);
        }
    }
}
