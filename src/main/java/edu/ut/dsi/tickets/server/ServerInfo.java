package edu.ut.dsi.tickets.server;

public class ServerInfo {

  public final String address;
  public final int    clientPort;
  public final int    serverPort;
  public final int    id;

  public ServerInfo(String address, int clientPort, int serverPort, int id) {
    super();
    this.address = address;
    this.clientPort = clientPort;
    this.serverPort = serverPort;
    this.id = id;
  }

  ServerInfo(int id) {
    this.id = id;
    this.address = null;
    this.serverPort = -1;
    this.clientPort = -1;
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
        + id + "]";
  }

}
