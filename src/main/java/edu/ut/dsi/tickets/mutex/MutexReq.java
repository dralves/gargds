package edu.ut.dsi.tickets.mutex;

public class MutexReq extends MutexBase {

  public MutexReq(int qi) {
    super(qi);
  }

  public MutexReq() {
    super();
  }

  @Override
  public String toString() {
    return "MutexReq [qi=" + qi + "]";
  }

}
