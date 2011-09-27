package edu.ut.dsi.tickets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.ut.dsi.tickets.mutex.Clock.Timestamp;
import edu.ut.dsi.tickets.mutex.MutexAck;
import edu.ut.dsi.tickets.mutex.MutexRel;
import edu.ut.dsi.tickets.mutex.MutexReq;
import edu.ut.dsi.tickets.server.ServerInfo;

/**
 * A wrapper for any mesasge that includes a timestamp, in order to deal with clocks.
 * 
 * @author dralves
 * 
 */
public class Message<T extends Writable> implements Writable {

  /**
   * Type for the msg, more types can be added later.
   * 
   * @author dralves
   * 
   */
  public enum MsgType {
    REQUEST(MethodRequest.class),
    RESPONSE(MethodResponse.class),
    CS_REQ(MutexReq.class),
    CS_REL(MutexRel.class),
    ACK(MutexAck.class),
    JOIN(ServerInfo.class);

    Class<? extends Writable> payloadClass;

    private MsgType(Class<? extends Writable> payloadClass) {
      this.payloadClass = payloadClass;
    }

    public Class<? extends Writable> payloadClass() {
      return this.payloadClass;
    }

  }

  private Timestamp ts;
  private int       senderId;
  private T         value;
  private MsgType   type;

  public Message() {
  }

  public Message(MsgType type, Timestamp ts, int senderId, T payload) {
    this.ts = ts;
    this.type = type;
    this.senderId = senderId;
    this.value = payload;
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(this.senderId);
    this.ts.write(out);
    out.writeInt(this.type.ordinal());
    this.value.write(out);
  }

  @SuppressWarnings("unchecked")
  public void read(DataInput in) throws IOException {
    this.senderId = in.readInt();

    this.ts = new Timestamp();
    this.ts.read(in);
    this.type = MsgType.values()[in.readInt()];
    try {
      this.value = (T) this.type.payloadClass().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Error instanciating payload.", e);
    }
    this.value.read(in);
  }

  public MsgType type() {
    return this.type;
  }

  public int senderId() {
    return senderId;
  }

  public Timestamp ts() {
    return ts;
  }

  public T value() {
    return value;
  }

  @Override
  public String toString() {
    return "Message [ts=" + ts + ", senderId=" + senderId + ", value=" + value + ", type=" + type + "]";
  }

}
