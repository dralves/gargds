package edu.ut.dsi.tickets.mutex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.ut.dsi.tickets.Writable;

public abstract class MutexBase implements Writable {

  protected int qi;

  public MutexBase(int qi) {
    this.qi = qi;
  }

  public MutexBase() {
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(qi);
  }

  public void read(DataInput in) throws IOException {
    qi = in.readInt();
  }

  public int qi() {
    return qi;
  }

}
