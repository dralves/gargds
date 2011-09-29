package edu.ut.dsi.tickets.mutex;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.Message.MsgType;
import edu.ut.dsi.tickets.Writable;
import edu.ut.dsi.tickets.mutex.Clock.Timestamp;
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
    this.myId = clock.myId();
  }

  public void setComms(Comms comms) {
    this.comms = comms;
    this.queue = new int[comms.remoteServers().size() + 1];
    for (int i = 0; i < queue.length; i++) {
      queue[i] = Integer.MAX_VALUE;
    }
  }

  /**
   * Waits until the mutex has been acquired, only one thread at a time can call lock().
   */
  public void lock() {
    queue[myId] = clock.time();
    LOG.debug("Process broadcasting desire to acquire lock: " + clock);
    for (int i = 0; i < queue.length; i++) {
      if (i != myId && clock.time(i) != -1) {
        Message<MutexReq> msg = this.clock.newOutMsg(MsgType.CS_REQ, new MutexReq(queue[myId]));
        Message<MutexAck> reponse = comms.send(i, msg);
        if (reponse != null) {
          receive(reponse);
        }
      }
    }
    LOG.debug("Proces wiil try and qcquire the lock: " + clock + " queue: " + Arrays.toString(queue));
    while (!okCS()) {
      LOG.debug("Process waiting to aquire the lock: " + clock);
      myWait();
    }
    LOG.debug("Process acquired the lock: " + clock);
  }

  private void myWait() {
    synchronized (clock) {
      try {
        clock.wait();
      } catch (InterruptedException e) {
        System.err.println(e);
      }
    }
  }

  public void unlock() {
    queue[myId] = Integer.MAX_VALUE;
    LOG.debug("Process about to send lock release messages at time: " + clock);
    for (int i = 0; i < queue.length; i++) {
      if (i != myId && clock.time(i) != -1) {
        Message<MutexRel> msg = this.clock.newOutMsg(MsgType.CS_REL, new MutexRel(queue[myId]));
        comms.send(i, msg);
      }
    }
    LOG.debug("Process released the lock at time: " + clock);
  }

  public Message<MutexAck> receive(Message<?> msg) {
    LOG.debug("Process " + clock.myId() + " received message at " + clock.time() + " remote Pid: " + msg.senderId()
        + " remote clock: " + msg.ts().time + " message type: " + msg.type());
    clock.newInMsg(msg);
    switch (msg.type()) {
      case CS_REQ:
        queue[msg.senderId()] = ((MutexReq) msg.value()).qi;
        LOG.debug("Process " + clock.myId() + " received CS request from " + msg.senderId() + " at " + clock.time()
            + " and is about to ACK.");
        return clock.newOutMsg(MsgType.ACK, new MutexAck());
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
    return null;
  }

  public void fail(int pid) {
    queue[pid] = Integer.MAX_VALUE;
    clock.newInMsg(new Message<Writable>(MsgType.FAILURE, new Timestamp(-1), pid, null));
    synchronized (clock) {
      clock.notify();
    }
  }

  private boolean okCS() {
    for (int i = 0; i < queue.length; i++) {
      if (clock.time(i) != -1) {
        if (isGreater(queue[myId], myId, queue[i], i)) {
          return false;
        }
        if (isGreater(queue[myId], myId, clock.time(i), i)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean isGreater(int time1, int pid1, int time2, int pid2) {
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
