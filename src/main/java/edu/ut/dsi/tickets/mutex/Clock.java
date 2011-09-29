package edu.ut.dsi.tickets.mutex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.MDC;

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
    }

    public int value() {
      return time;
    }

    @Override
    public String toString() {
      return "Timestamp [time=" + time + "]";
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
    MDC.put("clock", Arrays.toString(clock));
  }

  public int time(int procId) {
    return clock[procId];
  }

  public int time() {
    return clock[myId];
  }

  public int myId() {
    return myId;
  }

  public <T extends Writable> Message<T> newOutMsg(MsgType type, T payload) {
    Timestamp ts = new Timestamp(time(myId));
    Message<T> msg = new Message<T>(type, ts, myId, payload);
    tick();
    MDC.put("clock", Arrays.toString(clock));
    return msg;
  }

  public void newInMsg(Message<?> msg) {
    clock[msg.senderId()] = Math.max(clock[msg.senderId()], msg.ts().value());
    clock[myId] = Math.max(clock[myId], msg.ts().value());
    MDC.put("clock", Arrays.toString(clock));
  }

  @Override
  public String toString() {
    return "Clock [clock=" + Arrays.toString(clock) + ", myId=" + myId + "]";
  }

}
