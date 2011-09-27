package edu.ut.dsi.tickets;

public class PerfectFailureDetector implements FailureDetector {

  private FailureContingencyCallback fcc;

  public void setFailureContingencyCallback(FailureContingencyCallback fcc) {
    this.fcc = fcc;
  }

  public void suspect(int pid) {
    this.fcc.failed(pid);
  }

  public void suspect(int pid, Throwable reason) {
    this.fcc.failed(pid);
  }

}
