package edu.ut.dsi.tickets.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.ut.dsi.tickets.Writable;

public class ServerInfo implements Writable {

  public String  address;
  public int     clientPort;
  public int     serverPort;
  public int     id;
  public boolean failed;

  public ServerInfo() {
  }

  ServerInfo(int id) {
    this(null, -1, -1, id);
  }

  public ServerInfo(String address, int clientPort, int serverPort, int id) {
    super();
    this.address = address;
    this.clientPort = clientPort;
    this.serverPort = serverPort;
    this.id = id;
    this.failed = false;
  }

  public void read(DataInput in) throws IOException {
    this.id = in.readInt();
    if (in.readBoolean()) {
      this.address = in.readUTF();
    }
    this.serverPort = in.readInt();
    this.clientPort = in.readInt();
    this.failed = in.readBoolean();
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(this.id);
    out.writeBoolean(this.address != null);
    if (this.address != null) {
      out.writeUTF(this.address);
    }
    out.writeInt(this.serverPort);
    out.writeInt(this.clientPort);
    out.writeBoolean(failed);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ServerInfo other = (ServerInfo) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ServerInfo [address=" + address + ", clientPort=" + clientPort + ", serverPort=" + serverPort + ", id="
        + id + ", failed=" + failed + "]";
  }

}
