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
public class Request {

  public enum Method {
    RESERVE, SEARCH, DELETE;
  }

  private Method method;
  private String name;
  private int    count;

  public Request() {
  }

  public Request(Method method, String name) {
    this(method, name, -1);
  }

  public Request(Method method, String name, int count) {
    this.method = method;
    this.name = name;
    this.count = count;
  }

  public void write(DataOutput output) throws IOException {
    output.writeInt(this.method.ordinal());
    output.writeUTF(name);
    if (this.method == Method.RESERVE) {
      output.writeInt(count);
    }
  }

  public void read(DataInput in) throws IOException {
    this.method = Method.values()[in.readInt()];
    this.name = in.readUTF();
    if (this.method == Method.RESERVE) {
      this.count = in.readInt();
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

  @Override
  public String toString() {
    return "Request [method=" + method + ", name=" + name + ", count=" + count + "]";
  }

}
