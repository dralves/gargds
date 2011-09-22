package edu.ut.dsi.tickets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * A response from the ticket server.
 * 
 * @author dralves
 * 
 */
public class MethodResponse implements Writable {

  private int[]             values;
  public static final int[] ERROR     = new int[] { -2 };
  public static final int[] NOT_FOUND = new int[] { -1 };

  public MethodResponse() {

  }

  public MethodResponse(int[] values) {
    this.values = values;
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(this.values.length);
    for (int i = 0; i < values.length; i++) {
      out.writeInt(values[i]);
    }
  }

  public void read(DataInput in) throws IOException {
    this.values = new int[in.readInt()];
    for (int i = 0; i < this.values.length; i++) {
      this.values[i] = in.readInt();
    }
  }

  public int[] values() {
    return values;
  }

  @Override
  public String toString() {
    return "Response [values=" + Arrays.toString(values) + "]";
  }

}
