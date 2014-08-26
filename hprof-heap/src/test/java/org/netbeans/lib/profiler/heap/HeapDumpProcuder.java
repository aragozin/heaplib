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
package org.netbeans.lib.profiler.heap;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.gridkit.lab.jvm.attach.HeapDumper;

public class HeapDumpProcuder {

    private static int PID;
    static {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        PID = Integer.valueOf(pid.substring(0, pid.indexOf('@')));
    }

    private static String HEAP_DUMP_PATH = "target/dump/test.dump";

    public static File getHeapDump() {
        File file = new File(HEAP_DUMP_PATH);
        if (!file.exists()) {
            System.out.println("Generating heap dump: " + HEAP_DUMP_PATH);
            initTestHeap();
            System.out.println(HeapDumper.dumpLive(PID, HEAP_DUMP_PATH, 120000));
        }
        return file;
    }

    static List<DummyA> dummyA = new ArrayList<DummyA>();
    static List<DummyB> dummyB = new ArrayList<DummyB>();
    static DummyC dummyC = new DummyC();

    public static void initTestHeap() {

        for(int i = 0; i != 50; ++i) {
            dummyA.add(new DummyA());
        }

        for(int i = 0; i != 50; ++i) {
            DummyB dmb = new DummyB();
            dmb.seqNo = String.valueOf(i);
            for(int j = 0; j != i; ++j) {
                dmb.list.add(String.valueOf(j));
                dmb.map.put("k" + String.valueOf(j), "v" + String.valueOf(j));
            }
            dummyB.add(dmb);
        }
    }
}
