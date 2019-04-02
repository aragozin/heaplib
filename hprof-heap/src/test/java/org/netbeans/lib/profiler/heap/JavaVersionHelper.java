package org.netbeans.lib.profiler.heap;

public class JavaVersionHelper {
	
	public static final boolean JAVA_6 = System.getProperty("java.vm.version").startsWith("1.6.");
	public static final boolean JAVA_7 = System.getProperty("java.vm.version").startsWith("1.7.");
	public static final boolean JAVA_8 = System.getProperty("java.vm.version").startsWith("1.8.");

	public static final boolean JAVA_11 = System.getProperty("java.vm.version").startsWith("11.");

	
}
