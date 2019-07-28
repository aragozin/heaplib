package org.perfkit.jheaplib.cli.cmd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine.ObjectVisitor;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

public class ExecCmd implements CmdRef {

    @Override
    public String getCommandName() {
        return "exec";
    }

    @Override
    public Runnable newCommand(CommandLauncher host) {
        return new ScriptRunner(host);
    }

    @Parameters(commandDescription = "OQL Script executor")
    public static class ScriptRunner implements Runnable {

        @ParametersDelegate
        private final CommandLauncher host;

        @ParametersDelegate
        private HeapProvider heapProvider = new HeapProvider();

        @Parameter(names = {"-e" }, required = true, description = "Script one liner or @FILE")
        private String script;

        @Parameter(names = {"-a" }, variableArity = true, required = false, description = "Script arguments in form NAME=VALUE")
        private List<String> args = new ArrayList<String>();

        public ScriptRunner(CommandLauncher host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Heap heap = heapProvider.openHeap(host);

                OQLEngine engine = new OQLEngine(heap);
                String query = script;
                if (query.startsWith("@")) {
                    query = readScript(query.substring(1));
                }

                engine.executeQuery(query, new ObjectVisitor() {

                    @Override
                    public boolean visit(Object o) {
                        output(printObject(o));
                        return false;
                    }

                });

            } catch (Exception e) {
                throw host.fail("Error during heap processing " + e.toString(), e);
            }
        }

        private String printObject(Object o) {
            return String.valueOf(o);
        }

        private String readScript(String file) throws IOException {
            BufferedReader fr = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            while(true) {
                String line = fr.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append('\n');
            }
            fr.close();
            return sb.toString();
        }

        private void output(Object o) {
            System.out.println(stringify(o));
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
