package edu.ut.dsi.tickets.mutex;

public class MutexRel extends MutexBase {

  public MutexRel(int qi) {
    super(qi);
  }

  public MutexRel() {
    super();
  }

  @Override
  public String toString() {
    return "MutexRel [qi=" + qi + "]";
  }

}
