package org.org.packageindexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

class SocketWorker implements Runnable{
   private Socket connection;
   private Indexer indexer;

   // ([a-zA-Z][a-zA-Z0-9_\-]*) - alphanumeric pattern that can contain _ and - and + and is at least one letter
   // Used for package name validation
   private static Pattern validationPattern =
         Pattern.compile("(INDEX|REMOVE|QUERY)\\|([a-zA-Z][a-zA-Z0-9_\\-\\+]*)\\|([a-zA-Z][a-zA-Z0-9_\\-\\+]*,?)*");

   SocketWorker(Socket connection, Indexer indexer) {
      this.connection = connection;
      this.indexer = indexer;
   }

   /**
    * Process the socket
    */
   public void run() {
      BufferedReader input = null;
      PrintWriter output = null;

      // initialize input and output to socket
      try {
         input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         output = new PrintWriter(connection.getOutputStream(), true);
      } catch (IOException e) {
         e.printStackTrace();
         try {
            connection.close();
         } catch (IOException e1) {
            e1.printStackTrace();
            return;
         }
         return;
      }

      // command read loop
      String line;
      String[] command = null;
      try {
         while (true) {
            line = input.readLine();
            // connection closed
            if (line == null) {
               output.close();
               input.close();
               connection.close();
               break;
            }
            if(!validationPattern.matcher(line).matches()) {
               System.out.println("ERROR: " + line);
               output.write("ERROR\n");
               output.flush();
               continue;
            }

            command = line.split("\\|", -1);
            if(command.length != 3) break;
            boolean result = false;

            switch(command[0]) {
               case "INDEX":
                  if("".equals(command[2])) result = indexer.add(command[1], new LinkedList<String>());
                  else result = indexer.add(command[1], Arrays.asList(command[2].split(",", 0)));
                  break;
               case "REMOVE":
                  result = indexer.remove(command[1]);
                  break;
               case "QUERY":
                  result = indexer.query(command[1]);
                  break;
               default:
                  output.write("ERROR\n");
                  output.flush();
                  continue;
            }

            if(result == true) {
               output.write("OK\n");
            } else {
               output.write("FAIL\n");
            }
            output.flush();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      try {
         output.close();
         input.close();
         connection.close();
      } catch (IOException e1) {
         e1.printStackTrace();
      }
   }
}
