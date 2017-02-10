package org.org.packageindexer;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allow simple named locks
 */
public class NamespaceLock {
   private ConcurrentHashMap<String, MyReentrantLock> namespace;

   public NamespaceLock() {
      namespace = new ConcurrentHashMap<String, MyReentrantLock>();
   }

   public void lock(String name) {
      MyReentrantLock lock;
      lock = namespace.get(name);
      if (lock != null) {
         lock.lock();
         return;
      }
      lock = new MyReentrantLock();
      ReentrantLock oldLock = namespace.putIfAbsent(name, lock);
      if (oldLock != null) {
         oldLock.lock();
         return;
      }
      else lock.lock();
   }

   public void unlock(String name) {
      namespace.get(name).unlock();
   }

   public void status() {
      for (Entry<String, MyReentrantLock> entry : namespace.entrySet()) {
         if (!entry.getValue().tryLock()) {
            System.out.println(entry.getKey() + " : LOCK LOCKED " + entry.getValue().owner());
         } else {
            System.out.println(entry.getKey() + " : LOCK UNLOCKED");
            entry.getValue().unlock();
         }
      }
   }

   private static class MyReentrantLock extends ReentrantLock {
      String owner() {
         Thread t =  this.getOwner();
         if (t != null) {
            return t.getName();
         } else {
            return "none";
         }
      }
   }
}
