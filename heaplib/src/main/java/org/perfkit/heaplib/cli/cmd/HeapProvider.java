package org.perfkit.heaplib.cli.cmd;

import java.io.File;
import java.io.IOException;

import org.gridkit.jvmtool.cli.CommandLauncher;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory2;

import com.beust.jcommander.Parameter;

public class HeapProvider {

    @Parameter(names = { "-d" }, required = true, description = "Path for JVM heap dump")
    private String heapPath;

    @Parameter(names = { "--index" }, required = false, description = "Path to peristent index files for heap dump")
    private String indexName;

    @Parameter(names = {
            "--noindex" }, required = false, description = "Disable usage of temporary files (FastHeap mode)")
    private boolean noIndex = false;

    @Parameter(names = {
            "--buffer-size" }, required = false, description = "Size of data buffer in MiB, required if file cannot be memory mapped")
    private long bufferSize = -1;

    private boolean writeable = false;

    public void openWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    public Heap openHeap(CommandLauncher host) throws IOException {

        File dump = new File(heapPath);
        if (!dump.isFile()) {
            throw host.fail("No such file: " + dump.getAbsolutePath());
        }

        if (noIndex) {
            if (indexName != null) {
                throw host.fail("--noindex and --index are mutually exclusive");
            }

            if (writeable) {
                if (bufferSize > 0) {
                    throw host.fail("--buffer-size required is not allowed for writeable mode");
                }
                return HeapFactory2.createWriteableHeap(dump);
            } else {
                if (bufferSize > 0) {
                    return HeapFactory2.createFastHeap(dump, bufferSize << 20);
                } else {
                    if (!HeapFactory2.canBeMemMapped(dump)) {
                        throw host.fail("File cannot be mapped to memmory, --buffer-size is required");
                    }
                    return HeapFactory2.createFastHeap(dump);
                }
            }
        } else {
            if (writeable) {
                throw host.fail("--noindex is required for patching heap dump");
            }
            if (bufferSize > 0) {
                throw host.fail("--buffer-size required --noindex");
            }

            if (HeapFactory2.isCompressed(dump)) {
                throw host
                        .fail("File '" + dump.getName() + "' is compressed, --noindex and --buffer-size are required");
            }

            if (indexName != null) {
                File strechDir = new File(indexName);
                if (!strechDir.isDirectory() && !strechDir.mkdirs()) {
                    throw host.fail("Failed to create directory: " + strechDir.getAbsolutePath());
                }

                return HeapFactory2.createHeap(dump, strechDir);
            } else {
                return HeapFactory2.createHeap(dump);
            }
        }
    }
}
