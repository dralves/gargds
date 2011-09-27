package edu.ut.dsi.tickets.mutex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.ut.dsi.tickets.Writable;

public class MutexAck implements Writable {

  public void read(DataInput in) throws IOException {
  }

  public void write(DataOutput out) throws IOException {
  }

  @Override
  public String toString() {
    return "MutexAck []";
  }

}
