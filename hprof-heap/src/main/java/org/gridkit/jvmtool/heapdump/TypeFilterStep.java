package org.gridkit.jvmtool.heapdump;

import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

class TypeFilterStep extends PathStep {

    private final String pattern;
    private final Pattern filter;
    private final boolean matchSuperclass;

    TypeFilterStep(String pattern, boolean matchSuperclass) {
        this.pattern = pattern;
        this.filter = translate(pattern, ".");
        this.matchSuperclass = matchSuperclass;
    }

    @Override
    public Iterator<Instance> walk(Instance instance) {
        if (match(instance.getJavaClass())) {
            return Collections.singleton(instance).iterator();
        }
        else {
            return Collections.<Instance>emptyList().iterator();
        }
    }

    private boolean match(JavaClass javaClass) {
        String name = javaClass.getName();
        if (filter.matcher(name).matches()) {
            return true;
        }
        else {
            if (matchSuperclass) {
                JavaClass sc = javaClass.getSuperClass();
                return sc != null && match(sc);
            }
            return false;
        }
    }

    /**
     * GLOB pattern supports *, ** and ? wild cards.
     * Leading and trailing ** have special meaning, consecutive separator become optional.
     */
    private static Pattern translate(String pattern, String separator) {
        StringBuffer sb = new StringBuffer();
        String es = escape(separator);
        // special starter
        Matcher ss = Pattern.compile("^([*][*][" + es + "]).*").matcher(pattern);
        if (ss.matches()) {
            pattern = pattern.substring(ss.group(1).length());
            // make leading sep optional
            sb.append("(.*[" + es + "])?");
        }
        // special trailer
        Matcher st = Pattern.compile(".*([" + es + "][*][*])$").matcher(pattern);
        boolean useSt = false;
        if (st.matches()) {
            pattern = pattern.substring(0, st.start(1));
            useSt = true;
        }

        for(int i = 0; i != pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (c == '?') {
                sb.append("[^" + es + "]");
            }
            else if (c == '*') {
                if (i + 1 < pattern.length() && pattern.charAt(i+1) == '*') {
                    i++;
                    // **
                    sb.append(".*");
                }
                else {
                    sb.append("[^" + es + "]*");
                }
            }
            else {
                if (c == '$') {
                    sb.append("\\$");
                }
                else if (Character.isJavaIdentifierPart(c) || Character.isWhitespace(c) || c == '|') {
                    sb.append(c);
                }
                else {
                    sb.append('\\').append(c);
                }
            }
        }

        if (useSt) {
            sb.append("([" + es + "].*)?");
        }

        return Pattern.compile(sb.toString());
    }

    private static String escape(String separator) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i != separator.length(); ++i) {
            char c = separator.charAt(i);
            if ("\\[]&-".indexOf(c) >= 0){
                sb.append('\\').append(c);
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "(" + pattern + ")";
    }
}
