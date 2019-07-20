package org.netbeans.lib.profiler.heap;

interface PatchableHprofByteBuffer {

    void writePatch(long index, byte[] dataPatch);

    void readPatch(long index, byte[] dataPatch);
}
