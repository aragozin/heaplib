package org.perfkit.jheaplib.cli;

import org.gridkit.jvmtool.cli.CommandLauncher;

public class HeapCLI extends CommandLauncher {

	public static void main(String[] args) {
		new HeapCLI().start(args);
	}
}
