package org.org.packageindexer;

public class PackageIndexer {
   public static void main(String[] args) {
      // Find number of processors
      int processors = Runtime.getRuntime().availableProcessors();

      Server server = new Server(8080, processors*10);

      System.out.println("Server going up on port 8080");

      Thread t = new Thread(server);
      t.start();

      try {
         t.join();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
