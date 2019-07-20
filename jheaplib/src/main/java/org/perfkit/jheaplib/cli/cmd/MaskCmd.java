package org.perfkit.jheaplib.cli.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.gridkit.jvmtool.heapdump.HeapWalker;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PatchableCharArray;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

public class MaskCmd implements CmdRef {

    @Override
    public String getCommandName() {
        return "mask";
    }

    @Override
    public Runnable newCommand(CommandLauncher host) {
        return new ScriptRunner(host);
    }

    @Parameters(commandDescription = "Patch heap dump on disk to mask out certain data")
    public static class ScriptRunner implements Runnable {

        private final CommandLauncher host;

        @ParametersDelegate
        private HeapProvider heapProvider = new HeapProvider();

        @Parameter(names = { "-s" }, required = true, description = "HeapPath selector to select values to be masked")
        private String selector = null;

        @Parameter(names = {
                "-m" }, required = false, description = "Pattern for masked string values, if capturing groups are present only captured fragments would be masked")
        private String pattern = null;

        @Parameter(names = { "--patch" }, required = false, description = "Enable dump file modification")
        private boolean patch = false;

        public ScriptRunner(CommandLauncher host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                if (patch) {
                    heapProvider.openWriteable(true);
                }
                Heap heap = heapProvider.openHeap(host);

                if (!patch) {
                    System.out.println("Dry mode, preview changes");
                } else {
                    System.out.println("Patch mode, apply modification to dump file");
                }

                Matcher m = pattern == null ? null : Pattern.compile(pattern).matcher("");

                // TODO patching in not supported on post Java 8 so far
                JavaClass jstring = heap.getJavaClassByName(String.class.getName());
                JavaClass jchars = heap.getJavaClassByName("char[]");
                for (Instance i : HeapWalker.walk(heap, selector)) {
                    if (i.getJavaClass() == jstring) {
                        i = HeapWalker.walkFirst(i, "value");
                    }
                    if (i.getJavaClass() == jchars) {

                        String text = new String((char[]) HeapWalker.valueOf(i));
                        if (m != null) {
                            m.reset(text);
                            if (!m.matches()) {
                                continue;
                            }
                        }

                        char[] newtext = (char[]) HeapWalker.valueOf(i);
                        if (m != null && m.groupCount() > 0) {
                            for (int g = 1; g <= m.groupCount(); g++) {
                                for (int j = m.start(g); j < m.end(g); ++j) {
                                    newtext[j] = '*';
                                }
                            }
                        } else {
                            for (int j = 0; j != newtext.length; ++j) {
                                newtext[j] = '*';
                            }
                        }

                        System.out
                                .println("#" + i.getInstanceId() + " '" + text + "' -> '" + new String(newtext) + "'");
                        if (patch) {
                            ((PatchableCharArray) i).patchContent(newtext);
                        }
                    } else {
                        System.out.println(
                                "#" + i.getInstanceId() + " -> skip unsupported type " + i.getJavaClass().getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw host.fail("Error during heap processing " + e.toString(), e);
            }
        }
    }
}
