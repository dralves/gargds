package edu.ut.dsi.tickets;

/**
 * An interface for failure detectors.
 * 
 * @author dralves
 * 
 */
public interface FailureDetector {

  public void suspect(int pid);

  public void suspect(int pid, Throwable reason);

  public void setFailureContingencyCallback(FailureContingencyCallback fcc);

}
