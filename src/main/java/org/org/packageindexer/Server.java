package org.org.packageindexer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
   private int concurrency;
   private int port;
   private volatile boolean stop;
   private ExecutorService executor;
   private Indexer indexer;
   private ServerSocket serverSocket;

   private Server() {
      throw new UnsupportedOperationException();
   }

   /**
    * @param port Port server listens on
    * @param concurrency The number of worker threads the server will run. Note that since this is a lightweight service
    *                    the number of threads should be significantly higher than the number of cores, because of
    *                    network latency.
    */
   public Server(int port, int concurrency) {
      this.port = port;
      this.concurrency = concurrency;
      // Create a thread pool for work. A static thread pool should be good enough, which is why
      // we'll be using a simple blocking ThreadQueue
      executor = Executors.newFixedThreadPool(concurrency);
      indexer = new Indexer();
   }

   @Override
   public void run() {
      // Allow for graceful shutdown
      Runtime.getRuntime().addShutdownHook(new ShutdownHook(executor));

      // Only stop on stop() or if port cannot be bound
      while (true) {
         try {
            if (stop) {
               if (serverSocket != null) serverSocket.close();
               executor.shutdown();
               return;
            }

            if(serverSocket == null) {
               serverSocket = open(port);
               if (serverSocket == null) return;
            }

            Socket socket = serverSocket.accept();

            // Makes DOS less easy | Also easier to test
            socket.setReuseAddress(true);
            socket.setSoTimeout(1000) ;
            socket.setSoLinger(true, 1000);

            // Hand off the new socket connection to a worker thread
            executor.execute(new SocketWorker(socket, indexer));
         } catch (IOException e) {
            // log error using a logging library
            if (!stop) e.printStackTrace();
            try {
               // prepare to restart the listening socket
               serverSocket.close();
               serverSocket = null;
            } catch (IOException e1) {
               e1.printStackTrace();
            }
            continue;
         }
      }
   }

   private ServerSocket open(int port) {
      try {
         return new ServerSocket(port);
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   public void stop() {
      this.stop = true;
      try {
         this.serverSocket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static class ShutdownHook extends Thread {
      ExecutorService executor;
      ShutdownHook(ExecutorService executor) {
         this.executor = executor;
      }

      @Override
      public void run() {
         System.out.println("Graceful shutdown in progress...");
         executor.shutdownNow();
      }
   }
}
