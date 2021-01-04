package org.gridkit.jvmtool.heapdump.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.gridkit.jvmtool.heapdump.StringCollector;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapDumpProcuder;
import org.netbeans.lib.profiler.heap.HeapFactory2;
import org.netbeans.lib.profiler.heap.JavaClass;

public class StringCollectorTest {

    @Test
    public void verify_string_collector() throws IOException {

        String path1 = "target/heapdump/verify_string_collector/dump1.hprof";
        String path2 = "target/heapdump/verify_string_collector/dump2.hprof";

        System.out.println(HeapDumpProcuder.dumpLive(path1, TimeUnit.SECONDS.toMillis(30)));

        Heap heap = HeapFactory2.createFastHeap(new File(path1));

        StringCollector collector = new StringCollector();
        collector.collect(heap);

        long ic1 = collector.getInstanceCount();
        long size1 = collector.getTotalSize();

        System.out.println("First dump: " + ic1 + " strings, rougthly " + (size1 >> 10) + " KiB");

        List<String> strings = new ArrayList<String>();

        int stringCount = 10000;
        int stringSize =100;

        produceLatin1Strings(strings, stringCount, stringSize);
        produceUtf16Strings(strings, stringCount, stringSize);

        System.out.println(HeapDumpProcuder.dumpLive(path2, TimeUnit.SECONDS.toMillis(30)));

        heap = HeapFactory2.createFastHeap(new File(path2));

        collector = new StringCollector();
        collector.collect(heap);

        long ic2 = collector.getInstanceCount();
        long size2 = collector.getTotalSize();

        System.out.println("Second dump: " + ic2 + " strings, rougthly " + (size2 >> 10) + " KiB");

        long nc = ic2 - ic1;
        if (nc < 2 * 0.8 * stringCount || nc > 2 * 1.2 * stringCount) {
            Assert.fail("Count delta should be rougthly " + (2 * stringCount));
        }

        long ns = size2 - size1;
        long sd = 0;
        if (areCompactStringsEnabled(heap)) {

            sd += 2 * stringCount * heap.getJavaClassByName(String.class.getName()).getInstanceSize();
            sd += 2 * stringCount * 16; // rough array header size
            sd += stringCount * stringSize;
            sd += 2 * stringCount * stringSize;
        } else {

            sd += 2 * stringCount * heap.getJavaClassByName(String.class.getName()).getInstanceSize();
            sd += 2 * stringCount * 16; // rough array header size
            sd += 2 * stringCount * stringSize;
            sd += 2 * stringCount * stringSize;
        }

        if (ns < 0.9 * sd || nc > 1.1 * sd) {
            Assert.fail("Size delta should be rougthly " + sd);
        }
    }

    private boolean areCompactStringsEnabled(Heap heap) {
        JavaClass jc = heap.getJavaClassByName(String.class.getName());
        Object val = jc.getValueOfStaticField("COMPACT_STRINGS");
        return Boolean.TRUE.equals(val);
    }

    private void produceLatin1Strings(List<String> strings, int count, int size) {
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i != count; ++i) {
            for (int l = 0; l != size; ++l) {
                sb.append(rnd.nextInt(32) + 32);
            }
            strings.add(sb.toString());
            sb.setLength(0);
        }
    }

    private void produceUtf16Strings(List<String> strings, int count, int size) {
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i != count; ++i) {
            for (int l = 0; l != size; ++l) {
                sb.append(rnd.nextInt(1024) + 32);
            }
            strings.add(sb.toString());
            sb.setLength(0);
        }
    }
}
