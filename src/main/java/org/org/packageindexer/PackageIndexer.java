package org.org.packageindexer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class PackageIndexer {
   public static void main(String[] args) {
      // Find number of processors
      int processors = Runtime.getRuntime().availableProcessors();

      Server server = new Server(8080, processors*10);

      System.out.println("Server going up on port 8080");

      Thread t = new Thread(server);
      t.start();

      ThreadMXBean tmx = ManagementFactory.getThreadMXBean();

      while(true) {
         long[] ids = tmx.findDeadlockedThreads();
         if (ids != null) {
            ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
            System.out.println("The following threads are deadlocked:");
            for (ThreadInfo ti : infos) {
               System.out.println(ti);
            }
            server.status();
            System.exit(1);
         }
      }
/*
      try {
         t.join();
      } catch (InterruptedException e) {
         e.printStackTrace();
      } */
   }
}
