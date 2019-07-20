package org.perfkit.jheaplib.cli.cmd;

import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

public class HistoCmd implements CmdRef {

    @Override
    public String getCommandName() {
        return "histo";
    }

    @Override
    public Runnable newCommand(CommandLauncher host) {
        return new ScriptRunner(host);
    }

    @Parameters(commandDescription = "Class histogram")
    public static class ScriptRunner implements Runnable {

        private final CommandLauncher host;

        @ParametersDelegate
        private HeapProvider heapProvider = new HeapProvider();

        public ScriptRunner(CommandLauncher host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Heap heap = heapProvider.openHeap(host);
                HeapHistogram histogram = new HeapHistogram();

                for (Instance i : heap.getAllInstances()) {
                    histogram.accumulate(i);
                }

                System.out.println(histogram.formatTop(Integer.MAX_VALUE));
            } catch (Exception e) {
                throw host.fail("", e);
            }
        }
    }
}
