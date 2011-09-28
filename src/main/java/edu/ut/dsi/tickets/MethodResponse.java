package edu.ut.dsi.tickets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import edu.ut.dsi.tickets.server.reservations.Reservation;

/**
 * A response from the ticket server.
 * 
 * @author dralves
 * 
 */
public class MethodResponse implements Writable {

  private int[]                        values;
  private Message<?>                   payload;
  private HashMap<String, Reservation> seatMap;
  public static final int[]            ERROR     = new int[] { -2 };
  public static final int[]            NOT_FOUND = new int[] { -1 };

  public MethodResponse() {
  }

  public MethodResponse(int[] values) {
    this.values = values;
  }

  public MethodResponse(Message<?> response) {
    this.payload = response;
  }

  public MethodResponse(HashMap<String, Reservation> currentSeatMap) {
    this.seatMap = currentSeatMap;
  }

  public void write(DataOutput out) throws IOException {
    if (seatMap != null) {
      out.writeInt(-1);
      serializeWritable(seatMap, out);
      return;
    } else {
      out.writeInt(0);
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

  }

  public void read(DataInput in) throws IOException {
    int read = in.readInt();
    if (read == -1) {
      this.seatMap = deserializeWritable(in);
      return;
    }
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

  public HashMap<String, Reservation> getSeatMap() {
    return seatMap;
  }

  @Override
  public String toString() {
    return "Response [values=" + Arrays.toString(values) + "]";
  }

  public static void serializeWritable(Serializable serializable, DataOutput output) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(serializable);
    oos.flush();
    byte[] serialized = baos.toByteArray();
    output.writeInt(serialized.length);
    output.write(serialized);
  }

  @SuppressWarnings("unchecked")
  public static <T> T deserializeWritable(DataInput input) throws IOException {
    byte[] serialized = new byte[input.readInt()];
    input.readFully(serialized);
    ByteArrayInputStream baos = new ByteArrayInputStream(serialized);
    ObjectInputStream oos = new ObjectInputStream(baos);
    try {
      return (T) oos.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Error deserializing Writable.", e);
    }
  }

}
