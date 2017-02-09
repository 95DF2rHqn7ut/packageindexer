import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PackageIndexer {
   public static void main(String[] args) {

      // Find number of processors
      int processors = Runtime.getRuntime().availableProcessors();

      // Create a thread pool for work. A static thread pool should be good enough, which is why
      // we'll be using a simple blocking ThreadQueue
      ThreadPoolExecutor executor = new ThreadPoolExecutor(processors*100, processors*100,
            0L, TimeUnit.MILLISECONDS,
            new ThreadQueue<Runnable>(10000));

      // Allow for graceful shutdown
      Runtime.getRuntime().addShutdownHook(new ShutdownHook(executor));

      ServerSocket serverSocket = null;
      try {
         serverSocket = new ServerSocket(8080);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      Indexer indexer = new Indexer();

      while (true) {
         try {
            Socket socket = serverSocket.accept() ;

            // Makes DOS less easy
            socket.setReuseAddress(true) ;
            socket.setSoTimeout(1000) ;
            socket.setSoLinger(true, 1000) ;

            // Hand off the new socket connection to a worker thread
            executor.execute(new SocketWorker(socket, indexer)) ;
         } catch (IOException e) {
            // log error using a logging library
            e.printStackTrace();
            continue ;
         }
      }
   }

   private static class ShutdownHook extends Thread {
      ThreadPoolExecutor executor = null;
      ShutdownHook(ThreadPoolExecutor executor) {
         this.executor = executor;
      }

      @Override
      public void run() {
         System.out.println("Graceful shutdown in progress...");
         executor.shutdown();
      }
   }
}
