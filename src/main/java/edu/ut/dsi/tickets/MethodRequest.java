package edu.ut.dsi.tickets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A request made to the ticket server.
 * 
 * @author dralves
 * 
 */
public class MethodRequest implements Writable {

  public enum Method {
    RESERVE,
    SEARCH,
    DELETE,
    REPLICATE_PUT,
    REPLICATE_DELETE,
    LOCK_MSG,
    REPLICA_UPDATE;
  }

  private Method     method;
  // client-server args
  private String     name;
  private int        count;
  // server-server args
  private Message<?> msg;

  public MethodRequest() {
  }

  public MethodRequest(Method method, String name) {
    this(method, name, -1);
  }

  public MethodRequest(Method method, String name, int count) {
    this.method = method;
    this.name = name;
    this.count = count;
  }

  public MethodRequest(Method method, Message<?> message) {
    this.method = method;
    this.msg = message;
  }

  public MethodRequest(Method method) {
    this.method = method;
  }

  public void write(DataOutput output) throws IOException {
    output.writeInt(this.method.ordinal());
    switch (this.method) {
      case RESERVE:
      case SEARCH:
      case DELETE:
      case REPLICATE_PUT:
      case REPLICATE_DELETE:
        output.writeUTF(name);
        output.writeInt(count);
        break;
      case LOCK_MSG:
        // case JOIN:
        msg.write(output);
        break;
      case REPLICA_UPDATE:
        break;
      default:
        throw new IllegalStateException();
    }

  }

  @SuppressWarnings("rawtypes")
  public void read(DataInput in) throws IOException {
    this.method = Method.values()[in.readInt()];
    switch (this.method) {
      case RESERVE:
      case SEARCH:
      case DELETE:
      case REPLICATE_PUT:
      case REPLICATE_DELETE:
        this.name = in.readUTF();
        this.count = in.readInt();
        break;
      case LOCK_MSG:
        // case JOIN:
        this.msg = new Message();
        this.msg.read(in);
        break;
      case REPLICA_UPDATE:
        break;
      default:
        throw new IllegalStateException();
    }
  }

  public int count() {
    return count;
  }

  public Method method() {
    return method;
  }

  public String name() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public <T extends Writable> Message<T> msg() {
    return (Message<T>) msg;
  }

  @Override
  public String toString() {
    return "MethodRequest [method=" + method + ", name=" + name + ", count=" + count + ", msg=" + msg + "]";
  }

}
