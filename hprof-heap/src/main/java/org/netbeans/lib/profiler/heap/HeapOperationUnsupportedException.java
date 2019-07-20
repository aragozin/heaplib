package org.netbeans.lib.profiler.heap;

/**
 * {@link FastHprofHeap} does not support ceratain operations.
 *
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class HeapOperationUnsupportedException extends UnsupportedOperationException {

    private static final long serialVersionUID = 201904015L;

    public HeapOperationUnsupportedException() {
        super();
    }

    public HeapOperationUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeapOperationUnsupportedException(String message) {
        super(message);
    }

    public HeapOperationUnsupportedException(Throwable cause) {
        super(cause);
    }
}
