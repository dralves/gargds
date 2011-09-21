package edu.ut.dsi.tickets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.ut.dsi.tickets.Clock.Timestamp;

public class Message implements Writable {

  /**
   * Type fo the msg, more types can be added later.
   * 
   * @author dralves
   * 
   */
  public enum MsgType {
    REQUEST, RESPONSE
  }

  private Timestamp ts;
  private int       senderId;
  private Request   req;
  private Response  res;
  private MsgType   type;

  public Message(Timestamp ts, Request request, int senderId) {
    this.ts = ts;
    this.req = request;
    this.type = MsgType.REQUEST;
    this.senderId = senderId;
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(this.senderId);
    this.ts.write(out);
    out.write(this.type.ordinal());
    switch (type) {
      case REQUEST:
        this.req.write(out);
        break;
      case RESPONSE:
        this.res.write(out);
        break;
    }
  }

  public void read(DataInput in) throws IOException {
    this.senderId = in.readInt();
    this.ts = new Timestamp();
    this.ts.read(in);
    this.type = MsgType.values()[in.readInt()];
    switch (this.type) {
      case REQUEST:
        this.req = new Request();
        this.req.read(in);
        break;
      case RESPONSE:
        this.res = new Response();
        this.res.read(in);
        break;
    }
  }

  public int senderId() {
    return senderId;
  }

  public Timestamp ts() {
    return ts;
  }
}
