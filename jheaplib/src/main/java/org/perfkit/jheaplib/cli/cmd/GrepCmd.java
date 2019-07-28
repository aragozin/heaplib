package org.perfkit.jheaplib.cli.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.gridkit.jvmtool.heapdump.HeapWalker;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

public class GrepCmd implements CmdRef {

    @Override
    public String getCommandName() {
        return "grep";
    }

    @Override
    public Runnable newCommand(CommandLauncher host) {
        return new GrepRunner(host);
    }

    @Parameters(commandDescription = "A simple way to extract strings and other simple data from dump")
    public static class GrepRunner implements Runnable {

        @ParametersDelegate
        private final CommandLauncher host;

        @ParametersDelegate
        private HeapProvider heapProvider = new HeapProvider();

        @Parameter(names = {"-s" }, variableArity = true, required = true, description = "Selectors in form HEAPATH[:REGEX]")
        private List<String> selectors = new ArrayList<String>();

        public GrepRunner(CommandLauncher host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Heap heap = heapProvider.openHeap(host);

                List<Selector> sl = new ArrayList<Selector>();
                for(String selector: selectors) {
                    sl.add(Selector.parse(selector));
                }

                // TODO single pass for all selectors
                for(Selector s: sl) {

                    for(Instance i: HeapWalker.walk(heap, s.getHeapPath())) {
                        String txt = stringify(HeapWalker.valueOf(i));
                        if (s.getRegEx() == null || s.getRegEx().matcher(txt).matches()) {
                            System.out.println("#" + i.getInstanceId() + " '" + txt + "'");
                        }
                    }
                }
            } catch (Exception e) {
                throw host.fail("Error during heap processing " + e.toString(), e);
            }
        }

        private String stringify(Object value) {
            if (value instanceof char[]) {
                return new String((char[])value);
            }
            else if (value instanceof boolean[]) {
                return Arrays.toString((boolean[])value);
            }
            else if (value instanceof short[]) {
                return Arrays.toString((short[])value);
            }
            else if (value instanceof int[]) {
                return Arrays.toString((int[])value);
            }
            else if (value instanceof long[]) {
                return Arrays.toString((long[])value);
            }
            else if (value instanceof float[]) {
                return Arrays.toString((float[])value);
            }
            else if (value instanceof double[]) {
                return Arrays.toString((double[])value);
            }
            else if (value instanceof Object[]) {
                return Arrays.toString((Object[])value);
            }
            else {
                return String.valueOf(value);
            }
        }
    }
}
