package edu.ut.dsi.tickets.mutex;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.Message.MsgType;
import edu.ut.dsi.tickets.server.Comms;

/**
 * Implementation of a {@link LamportMutexLock} that implemments {@link Lock} to ease in integration. In particular this
 * Lock implements a {@link ReadWriteLock} to maintain compatibility altough the only lock returned is itself and it is
 * in fact a mutex. <br/>
 * This {@link Lock} is NOT reentrant and cannot be used in place of a reentrant lock, otherwise deadlock will occur.
 * 
 * @author dralves
 * 
 */
public class LamportMutexLock implements Lock, ReadWriteLock {

  private static final Logger LOG = LoggerFactory.getLogger(LamportMutexLock.class);

  private Clock               clock;
  private Comms               comms;
  private int[]               queue;
  private int                 myId;

  public LamportMutexLock(Clock clock) {
    this.clock = clock;
  }

  public void setComms(Comms comms) {
    this.comms = comms;
    this.queue = new int[comms.remoteServers().size() + 1];
  }

  /**
   * Waits until the mutex has been acquired, only one thread at a time can call lock().
   */
  public void lock() {
    queue[myId] = clock.time();
    LOG.debug("Process " + clock.myId() + " trying to aquire the lock at time: " + clock.time());
    Message<MutexReq> msg = this.clock.newOutMsg(MsgType.CS_REQ, new MutexReq(queue[myId]));
    LOG.debug("Process " + clock.myId() + " broadcasting desire to acquire lock at time: " + clock.time());
    comms.sendToAll(msg);
    while (!okCS()) {
      LOG.debug("Process " + clock.myId() + " waiting to aquire the lock at time: " + clock.time());
      myWait();
    }
    LOG.debug("Process " + clock.myId() + " acquired the lock at time: " + clock.time());
  }

  private synchronized void myWait() {
    try {
      wait();
    } catch (InterruptedException e) {
      System.err.println(e);
    }
  }

  public void unlock() {
    queue[myId] = Integer.MAX_VALUE;
    Message<MutexReq> msg = this.clock.newOutMsg(MsgType.CS_REL, new MutexReq(queue[myId]));
    LOG.debug("Process " + clock.myId() + " about to send lock release messages at time: " + clock.time());
    comms.sendToAll(msg);
    LOG.debug("Process " + clock.myId() + " released the lock at time: " + clock.time());
  }

  public void receive(Message<?> msg) {
    LOG.debug("Process " + clock.myId() + " received message at " + clock.time() + " remote Pid: " + msg.senderId()
        + " remote clock: " + msg.ts().time + " message type: " + msg.type());
    clock.newInMsg(msg);
    switch (msg.type()) {
      case CS_REQ:
        queue[msg.senderId()] = ((MutexReq) msg.value()).qi;
        LOG.debug("Process " + clock.myId() + " received CS request from " + msg.senderId() + " at " + clock.time()
            + " and is about to ack.");
        comms.send(msg.senderId(), clock.newOutMsg(MsgType.ACK, new MutexAck()));
        break;
      case CS_REL:
        queue[msg.senderId()] = Integer.MAX_VALUE;
        LOG.debug("Process " + clock.myId() + " received CS release from " + msg.senderId() + " at " + clock.time());
        break;
      case ACK:
        LOG.debug("Process " + clock.myId() + " received ACK from " + msg.senderId() + " at " + clock.time());
        break;
      case JOIN:
        LOG.debug("Process " + clock.myId() + " received JOIN from " + msg.senderId() + " at " + clock.time());
        break;
      default:
        throw new IllegalStateException();
    }
    synchronized (clock) {
      clock.notify();
    }

  }

  private boolean okCS() {
    for (int i = 0; i < queue.length; i++) {
      if (isGreater(queue[myId], myId, queue[i], i)) {
        return false;
      }
      if (isGreater(queue[myId], myId, clock.time(i), i)) {
        return false;
      }
    }
    return true;
  }

  private boolean isGreater(int pid1, int time1, int pid2, int time2) {
    if (time2 == Integer.MAX_VALUE) {
      return false;
    }
    return ((time1 > time2) || ((time1 == time2) && (pid1 > pid2)));
  }

  public Lock readLock() {
    return this;
  }

  public Lock writeLock() {
    return this;
  }

  public void lockInterruptibly() throws InterruptedException {
    throw new UnsupportedOperationException("Lamport mutex only supports lock() and unlock()");
  }

  public boolean tryLock() {
    throw new UnsupportedOperationException("Lamport mutex only supports lock() and unlock()");
  }

  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException("Lamport mutex only supports lock() and unlock()");
  }

  public Condition newCondition() {
    throw new UnsupportedOperationException("Lamport mutex only supports lock() and unlock()");
  }

}
