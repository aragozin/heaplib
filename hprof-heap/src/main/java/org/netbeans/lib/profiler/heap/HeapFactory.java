/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.heap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;

import org.gridkit.jvmtool.heapdump.io.ByteBufferPageManager;
import org.gridkit.jvmtool.heapdump.io.CompressdHprofByteBuffer;
import org.gridkit.jvmtool.heapdump.io.PagedFileHprofByteBuffer;


/**
 * This is factory class for creating {@link Heap} from the file in Hprof dump format.
 * @author Tomas Hurka
 */
public class HeapFactory {
    
    public static final long DEFAULT_BUFFER = 128 << 20;
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Fast {@link Heap} implementation is optimized for batch processing of dump.
     * Unlike normal {@link Heap} it doesn't create/use any temporary files. 
     */
    public static Heap createFastHeap(File heapDump) throws FileNotFoundException, IOException {
        return createFastHeap(heapDump, DEFAULT_BUFFER);
    }

    /**
     * Fast {@link Heap} implementation is optimized for batch processing of dump.
     * Unlike normal {@link Heap} it doesn't create/use any temporary files.
     * 
     * @param bufferSize if file can be mapped to memory no buffer would be used, overwise limits memory used for buffering
     */
    public static Heap createFastHeap(File heapDump, long bufferSize) throws FileNotFoundException, IOException {
        return new FastHprofHeap(createBuffer(heapDump, bufferSize), 0);
    }
    
    public static boolean canBeMemMapped(File heapDump) {
        
        try {
            if (isGZIP(heapDump)) {
                return false;
            }            
        }
        catch(NoClassDefFoundError e) {
            // GZip parser is not available
        }        
        
        try {
            FileInputStream fis = new FileInputStream(heapDump);
            FileChannel channel = fis.getChannel();
            long length = channel.size();
            int bufCount = (int)((length + ((1 << 30) - 1)) >> 30);
            MappedByteBuffer[] buffers = new MappedByteBuffer[bufCount];
            try {
                for(int i = 0; i != bufCount; ++i) {
                    long rm = length - (((long)i) << 30);
                    buffers[i] = channel.map(FileChannel.MapMode.READ_ONLY, ((long)i) << 30, Math.min(rm, 1 << 30));
                }
                return true;
            }
            catch(Exception e) {
                // ignore
            }
            finally {
                try {
                    channel.close();
                }
                catch(Exception e) {
                    // ignore
                }                
                try {
                    fis.close();
                }
                catch(Exception e) {
                    // ignore
                }                
                for(MappedByteBuffer mb: buffers) {
                    try {
                        callCleaner(mb);
                    }
                    catch(Exception e) {
                        // ignore
                    }                
                }
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }
        return false;
    }
    
    private static void callCleaner(MappedByteBuffer dumpBuffer) {
        Object c = rcall(dumpBuffer, "cleaner");
        rcall(c, "clean");        
    }
    
    private static Object rcall(Object o, String method, Object... args) {
        try {
            Class<?> c = o.getClass();
            Method m = getMethod(c, method);
            m.setAccessible(true);
            return m.invoke(o, args);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    private static Method getMethod(Class<?> c, String method) {
        for(Method m: c.getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                return m;
            }
        }
        if (c.getSuperclass() != null) {
            return getMethod(c.getSuperclass(), method);
        }
        return null;
    }

    private static HprofByteBuffer createBuffer(File heapDump, long bufferSize) throws IOException {
        try {
            if (isGZIP(heapDump)) {
                return createCompressedHprofBuffer(heapDump, bufferSize);
            }            
        }
        catch(NoClassDefFoundError e) {
            // GZip parser is not available
        }

        HprofByteBuffer bb = HeapFactory.createHprofByteBuffer(heapDump, bufferSize);
        return bb;
    }

    private static HprofByteBuffer createCompressedHprofBuffer(File heapDump, long bufferSize) throws IOException, FileNotFoundException {
        return new CompressdHprofByteBuffer(new RandomAccessFile(heapDump, "r"), new ByteBufferPageManager(512 << 10, bufferSize));
    }
    
    private static boolean isGZIP(File headDump) {
        try {
            FileInputStream in = new FileInputStream(headDump);        
            GZIPInputStream is;
            try {
                is = new GZIPInputStream(in);
                is.read();
                is.close();
                return true;
            } catch (IOException e) {
                in.close();
            }
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    static HprofByteBuffer createHprofByteBuffer(File dumpFile, long bufferSize)
                                          throws IOException {
        long fileLen = dumpFile.length();
    
        if (fileLen < HprofByteBuffer.MINIMAL_SIZE) {
            String errText = "File size is too small";
            throw new IOException(errText);
        }
    
        try {
            if (fileLen < Integer.MAX_VALUE) {
                return new HprofMappedByteBuffer(dumpFile);
            } else {
                return new HprofLongMappedByteBuffer(dumpFile);
            }
        } catch (IOException ex) {
            if (ex.getCause() instanceof OutOfMemoryError) { // can happen on 32bit Windows, since there is only 2G for memory mapped data for whole java process.
                return new PagedFileHprofByteBuffer(new RandomAccessFile(dumpFile, "r"), new ByteBufferPageManager(1 << 20, 1 << 20, bufferSize));
            }
    
            throw ex;
        }
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * this factory method creates {@link Heap} from a memory dump file in Hprof format.
     * <br>
     * <b>This implementation is using temporary disk files for building auxiliary indexes</b>
     * <br>
     * Speed: slow
     * @param heapDump file which contains memory dump
     * @return implementation of {@link Heap} corresponding to the memory dump
     * passed in heapDump parameter
     * @throws java.io.FileNotFoundException if heapDump file does not exist
     * @throws java.io.IOException if I/O error occurred while accessing heapDump file
     */
    public static Heap createHeap(File heapDump) throws FileNotFoundException, IOException {
        return createHeap(heapDump, 0);
    }

    /**
     * this factory method creates {@link Heap} from a memory dump file in Hprof format.
     * If the memory dump file contains more than one dump, parameter segment is used to
     * select particular dump.
     * <br>
     * <b>This implementation is using temporary disk files for building auxiliary indexes</b>
     * <br>
     * Speed: slow
     * @return implementation of {@link Heap} corresponding to the memory dump
     * passed in heapDump parameter
     * @param segment select corresponding dump from multi-dump file
     * @param heapDump file which contains memory dump
     * @throws java.io.FileNotFoundException if heapDump file does not exist
     * @throws java.io.IOException if I/O error occurred while accessing heapDump file
     */
    public static Heap createHeap(File heapDump, int segment)
                           throws FileNotFoundException, IOException {
        CacheDirectory cacheDir = CacheDirectory.getHeapDumpCacheDirectory(heapDump);
        if (!cacheDir.isTemporary()) {
            File savedDump = cacheDir.getHeapDumpAuxFile();

            if (savedDump.exists() && savedDump.isFile() && savedDump.canRead()) {
                try {
                    return loadHeap(cacheDir);
                } catch (IOException ex) {
                    System.err.println("Loading heap dump "+heapDump+" from cache failed.");
                    ex.printStackTrace(System.err);
                }
            }
        }
        return new HprofHeap(heapDump, segment, cacheDir);

    }
    
    static Heap loadHeap(CacheDirectory cacheDir)
                           throws FileNotFoundException, IOException {
        File savedDump = cacheDir.getHeapDumpAuxFile();
        InputStream is = new BufferedInputStream(new FileInputStream(savedDump), 64*1024);
        DataInputStream dis = new DataInputStream(is);
        Heap heap = new HprofHeap(dis, cacheDir);
        dis.close();
        return heap;
    }    
}
