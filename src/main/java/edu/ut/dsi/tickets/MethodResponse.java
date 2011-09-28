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
  private Message<?>        payload;
  public static final int[] ERROR     = new int[] { -2 };
  public static final int[] NOT_FOUND = new int[] { -1 };

  public MethodResponse() {
  }

  public MethodResponse(int[] values) {
    this.values = values;
  }

  public MethodResponse(Message<?> response) {
    this.payload = response;
  }

  public void write(DataOutput out) throws IOException {
    if (payload == null) {
      out.writeBoolean(false);
      out.writeInt(this.values.length);
      for (int i = 0; i < values.length; i++) {
        out.writeInt(values[i]);
      }
    } else {
      out.writeBoolean(true);
      this.payload.write(out);
    }

  }

  public void read(DataInput in) throws IOException {
    if (!in.readBoolean()) {
      this.values = new int[in.readInt()];
      for (int i = 0; i < this.values.length; i++) {
        this.values[i] = in.readInt();
      }
    } else {
      this.payload = new Message();
      this.payload.read(in);
    }
  }

  public int[] values() {
    return values;
  }

  public Message<?> msg() {
    return payload;
  }

  @Override
  public String toString() {
    return "Response [values=" + Arrays.toString(values) + "]";
  }

}
