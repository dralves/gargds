package edu.ut.dsi.tickets.mutex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.Message.MsgType;
import edu.ut.dsi.tickets.Writable;

/**
 * An implementation of a direct dependency clock. Right now as the only clock that is need is a ddclock this is the
 * only implementation of clock and {@link Timestamp}, later on these can be refactored to interfaces so that other
 * implementations of Clock and Timestamp can be used.
 * 
 * @author dralves
 * 
 */
public class Clock {

  public static class Timestamp implements Writable {

    public int time;

    public Timestamp() {
    }

    public Timestamp(int clockValue) {
      this.time = clockValue;
    }

    public void write(DataOutput out) throws IOException {
      out.writeInt(time);
    }

    public void read(DataInput in) throws IOException {
      this.time = in.readInt();
    };

    public int value() {
      return time;
    }
  }

  private int[] clock;
  private int   myId;

  public Clock(int myId, int numServers) {
    this.clock = new int[numServers];
    this.myId = myId;
  }

  public void tick() {
    clock[myId]++;
  }

  public int time(int procId) {
    return clock[procId];
  }

  public int time() {
    return clock[myId];
  }

  public <T extends Writable> Message<T> newOutMsg(MsgType type, T payload) {
    Timestamp ts = new Timestamp(time(myId));
    Message<T> msg = new Message<T>(type, ts, myId, payload);
    tick();
    return msg;
  }

  public void newInMsg(Message<?> msg) {
    clock[msg.senderId()] = Math.max(clock[msg.senderId()], msg.ts().value());
    clock[myId] = Math.max(clock[myId], msg.ts().value());
  }
}
