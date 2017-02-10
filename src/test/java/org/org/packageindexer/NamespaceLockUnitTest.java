package org.org.packageindexer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.testng.annotations.Test;

@Test(groups = "unit", testName = "NamespaceLockUnitTest")
public class NamespaceLockUnitTest {

   @Test(timeOut = 1000)
   public void testRemoveEmpty() {
      NamespaceLock namespaceLock = new NamespaceLock();
      namespaceLock.unlock("libgcc++");
   }

   @Test(timeOut = 1000)
   public void testAddAndRemove() {
      NamespaceLock namespaceLock = new NamespaceLock();
      namespaceLock.lock("libgcc++");
      namespaceLock.unlock("libgcc++");
   }

   @Test(timeOut = 1000)
   public void testLockTwice() {
      NamespaceLock namespaceLock = new NamespaceLock();
      namespaceLock.lock("libgcc++");
      namespaceLock.lock("libgcc++");
      namespaceLock.unlock("libgcc++");
   }

   @Test(timeOut = 5000)
   public void testTwoThreadsDeadlocking() {
      final NamespaceLock namespaceLock = new NamespaceLock();

      Thread one = new Thread() {
         public void run() {
            namespaceLock.lock("one");
            try {
               Thread.sleep(2000);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            namespaceLock.lock("two");
         }
      };

      Thread two = new Thread() {
         public void run() {
            namespaceLock.lock("two");
            try {
               Thread.sleep(2000);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            namespaceLock.lock("one");
         }
      };

      one.start();
      two.start();

      ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
      while(true) {
         long[] ids = tmx.findDeadlockedThreads();
         if (ids != null) {
            ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
            one.stop();
            two.stop();
            return;
         }
      }
   }
}
