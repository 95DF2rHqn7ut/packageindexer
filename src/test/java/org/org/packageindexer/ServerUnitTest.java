package org.org.packageindexer;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

@Test(groups = "unit", testName = "ServerUnitTest")
public class ServerUnitTest {
   static AtomicInteger portTracker = new AtomicInteger(8081);

   public void testDeleteEmpty() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("REMOVE|aaa|\n"), "OK");
      testServer.close();
   }

   public void testAddEmpty() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|aaa|\n"), "OK");
      testServer.close();
   }

   public void testAddWithDeps() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|aaa|\n"), "OK");
      assertEquals(testServer.call("INDEX|bbb|aaa\n"), "OK");
      assertEquals(testServer.call("INDEX|ccc|aaa,bbb\n"), "OK");
      testServer.close();
   }

   public void testFailAddWithDepsMix() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|aaa|\n"), "OK");
      assertEquals(testServer.call("INDEX|bbb|aaa\n"), "OK");
      assertEquals(testServer.call("INDEX|ccc|aaa,ggg\n"), "FAIL");
      testServer.close();
   }

   public void testFailAddDependency() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|aaa|bbb\n"), "FAIL");
      testServer.close();
   }

   public void testFailAddDependency2() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|aaa|bbb,ccc\n"), "FAIL");
      testServer.close();
   }

   public void testBroken() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|emacs elisp\n"), "ERROR");
      testServer.close();
   }

   public void testBroken2() throws InterruptedException, IOException {
      TestServer testServer = new TestServer(portTracker.getAndIncrement());
      assertEquals(testServer.call("INDEX|emacs☃elisp\n"), "ERROR");
      testServer.close();
   }

   private static class TestServer {
      private int port;
      private int concurrency;
      private Server server;
      private Thread t;

      private TestServer() {
         this(8080,1);
      }
      public TestServer(int port) {
         this(port, 1);
      }
      public TestServer(int port, int concurrency) {
         this.port = port;
         this.concurrency = concurrency;
         server = new Server(this.port, this.concurrency);
         t = new Thread(server);
         t.start();
      }

      public void close() throws InterruptedException {
         server.stop();
         t.join();
      }

      public String call(String command) throws IOException {
         Socket client = new Socket("localhost", port);
         PrintWriter out =
               new PrintWriter(client.getOutputStream(), false);
         BufferedReader in =
               new BufferedReader(
                     new InputStreamReader(client.getInputStream()));
         out.write(command);
         out.flush();
         String result = in.readLine();
         out.close();
         in.close();
         client.close();
         return result;
      }
   }
}
