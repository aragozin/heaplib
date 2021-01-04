package org.gridkit.jvmtool.heapdump;

class HeapHelper {

    public static String simpleClassName(String className) {
        int n = className.lastIndexOf('.');
        int m = className.lastIndexOf('$');
        if (n >= 0 || m >= 0) {
            return className.substring(Math.max(n, m) + 1);
        } else {
            return className;
        }
    }
}
