package org.perfkit.jheaplib.cli.cmd;

import java.util.regex.Pattern;

import org.gridkit.jvmtool.heapdump.HeapWalker;

public class Selector {

    private final String heapPath;
    private final Pattern regEx;

    public static Selector parse(String selector) {
        int ch = selector.indexOf(':');
        String path;
        String pattern = null;
        if (ch >= 0) {
            path = selector.substring(0, ch);
            pattern = selector.substring(ch + 1, selector.length());
        }
        else {
            path = selector;
        }
        HeapWalker.validateHeapPath(path);
        return new Selector(path, pattern == null ? null : Pattern.compile(pattern));
    }

    public Selector(String heapPath, Pattern regEx) {
        this.heapPath = heapPath;
        this.regEx = regEx;
    }

    public String getHeapPath() {
        return heapPath;
    }

    public Pattern getRegEx() {
        return regEx;
    }

    @Override
    public String toString() {
        return regEx == null
                ? heapPath + ":" + regEx.pattern()
                : heapPath;
    }
}
