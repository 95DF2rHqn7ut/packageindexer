package org.org.packageindexer;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
      int port = portTracker.getAndIncrement();

      Server server = new Server(port, 1);

      Thread t = new Thread(server);
      t.start();

      assertEquals(call(port, "REMOVE|aaa|\n"), "OK");

      server.stop();
      t.join();
   }

   public void testBroken() throws InterruptedException, IOException {
      int port = portTracker.getAndIncrement();

      Server server = new Server(port, 1);

      Thread t = new Thread(server);
      t.start();

      assertEquals(call(port, "INDEX|emacs elisp\n"), "ERROR");

      server.stop();
      t.join();
   }

   public void testBroken2() throws InterruptedException, IOException {
      int port = portTracker.getAndIncrement();

      Server server = new Server(port, 1);

      Thread t = new Thread(server);
      t.start();

      assertEquals(call(port, "INDEX|emacs☃elisp\n"), "ERROR");

      server.stop();
      t.join();
   }


   private String call(int port, String command) throws IOException {
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
