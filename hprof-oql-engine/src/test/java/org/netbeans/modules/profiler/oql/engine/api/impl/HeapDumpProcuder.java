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
package org.netbeans.modules.profiler.oql.engine.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.zip.GZIPOutputStream;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.gridkit.lab.jvm.attach.HeapDumper;
import org.junit.Test;

public class HeapDumpProcuder {

    private static int PID;
    static {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        PID = Integer.valueOf(pid.substring(0, pid.indexOf('@')));
    }

    private static String HEAP_DUMP_PATH = "target/dump/testdump.hprof";
    private static String HEAP_DUMP_GZ_PATH = "target/dump/testdump.hprof.gz";

    public static File getHeapDump() {
        File file = new File(HEAP_DUMP_PATH);
        if (!file.exists()) {
            System.out.println("Generating heap dump: " + HEAP_DUMP_PATH);
            holder.initTestHeap();
            System.out.println(dumpLive(HEAP_DUMP_PATH, 120000));
        }
        return file;
    }

    public static File getCompressedHeapDump() throws IOException {
        File file = new File(HEAP_DUMP_GZ_PATH);
        if (!file.exists()) {
            System.out.println("Generating compressing heap dump: " + HEAP_DUMP_GZ_PATH);
            FileInputStream fis = new FileInputStream(getHeapDump());
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            byte[] buf = new byte[64 << 10];
            int n = 0;
            while(true) {
                n = fis.read(buf);
                if (n < 0) {
                    break;
                }
                gzos.write(buf, 0, n);
            }
            gzos.close();
            fis.close();
            fos.close();
        }
        return file;
    }

    public static String dumpLive(String targetFile, long timeoutMs) {
        try {
            return HeapDumper.dumpLive(PID, targetFile, timeoutMs);
        }
        catch(RuntimeException e) {
            Throwable ee = e.getCause();
            if (ee instanceof IOException && ee.getMessage().equals("Can not attach to current VM")) {
                // look like it is modern Java, let's try JMX
                return dumpHeapViaMBean(targetFile);
            }
            else {
                throw e;
            }
    	}
    }

    private static String dumpHeapViaMBean(String targetFile) {
        try {
            MBeanServer factory = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
            HotSpotDiagnostic hsDiag = JMX.newMXBeanProxy(factory, name, HotSpotDiagnostic.class);
            hsDiag.dumpHeap(targetFile, true);
            return "Dump heap via JMX";
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

	public interface HotSpotDiagnostic {
    	public void dumpHeap(String fileName, boolean live);
    }

    // Called manually from IDE to clean cached dump
    @Test
    public void cleanDump() {
        new File(HEAP_DUMP_PATH).delete();
        new File(HEAP_DUMP_GZ_PATH).delete();
    }

    static Holder holder = new Holder();

    static class Holder {

	    public void initTestHeap() {
	    }
    }
}
