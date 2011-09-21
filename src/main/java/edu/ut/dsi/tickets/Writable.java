package edu.ut.dsi.tickets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * An interface for classes that can be written/read from a socket.
 * 
 * @author dralves
 * 
 */
public interface Writable {

  public void write(DataOutput out) throws IOException;

  public void read(DataInput in) throws IOException;

}
