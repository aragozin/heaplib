package org.netbeans.lib.profiler.heap;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This is extended implementation of char[] heap instance, which allows
 * patching content of array.
 * <br/>
 * Instance of this class is only instantiated in heap is open in writeable mode.
 *
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PatchableCharArray extends PrimitiveArrayDump {

	public PatchableCharArray(ClassDump cls, long offset) {
		super(cls, offset);
		assert getType() == HprofHeap.CHAR;
	}

	public char[] getContent() {
		return getChars(0, getLength());
	}

	public void patchContent(char[] patch) {
		if (patch.length != getLength()) {
			throw new IllegalArgumentException("Length mismatch");
		}

		HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();
		if (dumpBuffer instanceof PatchableHprofByteBuffer) {

	        long offset = getArrayStartOffset();

	        byte[] bpatch = new byte[2 * patch.length];
	        ByteBuffer bb = ByteBuffer.wrap(bpatch);
	        for(char c: getContent()) {
	        	bb.putChar(c);
	        }

	        byte[] borig = new byte[2 * patch.length];
	        ((PatchableHprofByteBuffer) dumpBuffer).readPatch(offset, borig);
	        if (!Arrays.equals(bpatch, borig)) {
	        	throw new RuntimeException("Patch verification failed");
	        }

	        bb.clear();
	        for(char c: patch) {
	        	bb.putChar(c);
	        }
	        ((PatchableHprofByteBuffer) dumpBuffer).writePatch(offset, bpatch);
		}
		else {
			throw new IllegalStateException("Dump is read only");
		}
	}

}
