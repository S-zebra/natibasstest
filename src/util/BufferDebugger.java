package util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class BufferDebugger {
  public static void dumpBuffer(Buffer buf) {
    int lim = buf.limit();
    int[] res = new int[lim];
    buf.rewind();
    for (int i = 0; i < lim; i++) {
      res[i] = Byte.toUnsignedInt( ((ByteBuffer) buf).get(i) );
    }
    buf.rewind();
    System.out.println(Arrays.toString(res));
  }
}
