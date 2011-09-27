package edu.ut.dsi.tickets;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A named thread factory that produces named threads and, optionally, daemon threads.
 * 
 * @author dralves
 */
public class NamedThreadFactory implements ThreadFactory {

  private final AtomicInteger counter = new AtomicInteger(0);
  public final String         name;
  private final boolean       daemon;
  private final int           priority;
  private final int           index;

  public NamedThreadFactory(String name) {
    this(name, -1);
  }

  public NamedThreadFactory(String name, boolean daemon) {
    this(name, -1, daemon, Thread.NORM_PRIORITY);
  }

  public NamedThreadFactory(String name, int index) {
    this(name, index, true);
  }

  public NamedThreadFactory(String name, int index, boolean daemon) {
    this(name, index, daemon, Thread.NORM_PRIORITY);
  }

  public NamedThreadFactory(String name, int index, boolean daemon, int priority) {
    this.name = name;
    this.daemon = daemon;
    this.priority = priority;
    this.index = index;
  }

  public Thread newThread(Runnable r) {
    Thread thread = new Thread(r, name + "[" + counter.getAndIncrement() + (index != -1 ? ":" + index : "") + "]");
    thread.setDaemon(daemon);
    thread.setPriority(priority);
    return thread;
  }

}
