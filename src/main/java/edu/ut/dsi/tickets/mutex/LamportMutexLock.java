package edu.ut.dsi.tickets.mutex;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

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

  private Clock          clock;
  private Comms          comms;
  private int[]          queue;
  private int            myId;

  // latches to help in waiting until everything is dandy before either locking or unlocking the critical section.
  private CountDownLatch reqLatch;

  public LamportMutexLock(Clock clock, Comms comms) {
    this.clock = clock;
    this.comms = comms;
  }

  /**
   * Waits until the mutex has been acquired, only one thread at a time can call lock().
   */
  public synchronized void lock() {
    queue[myId] = clock.time();
    Message<MutexReq> msg = this.clock.newOutMsg(MsgType.CS_REQ, new MutexReq(queue[myId]));
    comms.sendToAll(msg);
    reqLatch = new CountDownLatch(1);
    try {
      reqLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while waiting for lock");
    }
  }

  public synchronized void unlock() {
    queue[myId] = Integer.MAX_VALUE;
    Message<MutexReq> msg = this.clock.newOutMsg(MsgType.CS_REL, new MutexReq(queue[myId]));
    comms.sendToAll(msg);
  }

  public void receive(Message<?> msg) {
    clock.newInMsg(msg);
    switch (msg.type()) {
      case CS_REQ:
        comms.send(msg.senderId(), clock.newOutMsg(MsgType.ACK, new MutexAck()));
        break;
      case CS_REL:
        queue[msg.senderId()] = Integer.MAX_VALUE;
        break;
      case ACK:
        // do nothing
        break;
      default:
        throw new IllegalStateException();
    }
    // see if we are allowed to enter the CSection
    if (okCS()) {
      reqLatch.countDown();
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
