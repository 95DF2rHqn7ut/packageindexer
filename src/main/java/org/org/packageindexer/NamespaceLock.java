package org.org.packageindexer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allow simple named locks within a namespace
 *
 * This allows a single thread to reserve, by a String identifier, an entire namespace of multiple named locks.
 *
 * The trick to this is that this namespace never shrinks and is persistent.
 */
public class NamespaceLock {
   private ConcurrentHashMap<String, ReentrantLock> namespace;

   public NamespaceLock() {
      namespace = new ConcurrentHashMap<String, ReentrantLock>();
   }

   public void lock(String name) {
      ReentrantLock lock;
      lock = namespace.get(name);
      if (lock != null) {
         lock.lock();
         return;
      }
      lock = new ReentrantLock();
      ReentrantLock oldLock = namespace.putIfAbsent(name, lock);
      if (oldLock != null) {
         oldLock.lock();
         return;
      }
      else lock.lock();
   }

   public void unlock(String name) {
      ReentrantLock lock = namespace.get(name);
      if (lock != null) lock.unlock();
   }
}
