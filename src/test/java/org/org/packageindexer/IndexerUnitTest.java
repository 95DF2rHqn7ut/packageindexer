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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;

import org.testng.annotations.Test;

@Test(groups = "unit", testName = "IndexerUnitTest")
public class IndexerUnitTest {

   public void testRemoveEmpty() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.remove("libgcc++"));
   }

   public void testAddNew() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("libgcc++", new LinkedList<String>()));
      assertTrue(indexer.query("libgcc++"));
   }

   public void testRemoveExisting() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("libgcc++", new LinkedList<String>()));
      assertTrue(indexer.query("libgcc++"));
      assertTrue(indexer.remove("libgcc++"));
      assertFalse(indexer.query("libgcc++"));
   }

   public void testAddDependency() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("libgcc++", new LinkedList<String>()));
      assertTrue(indexer.query("libgcc++"));
      assertTrue(indexer.add("gcc", new LinkedList<String>(Arrays.asList("libgcc++"))));
      assertTrue(indexer.query("gcc"));
   }

   public void testAddDependencyFail() {
      Indexer indexer = new Indexer();
      assertFalse(indexer.add("gcc", new LinkedList<String>(Arrays.asList("libgcc++"))));
      assertFalse(indexer.query("gcc"));
   }

   public void testAddCircularDependency() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("gcc1", new LinkedList<String>()));
      assertTrue(indexer.add("gcc2", new LinkedList<String>()));
      assertTrue(indexer.add("gcc3", new LinkedList<String>()));
      assertTrue(indexer.add("gcc1", new LinkedList<String>(Arrays.asList("gcc2"))));
      assertTrue(indexer.add("gcc2", new LinkedList<String>(Arrays.asList("gcc3"))));
      assertTrue(indexer.add("gcc3", new LinkedList<String>(Arrays.asList("gcc1"))));
   }

   public void testAddCircularDependency2() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("gcc1", new LinkedList<String>()));
      assertTrue(indexer.add("gcc2", new LinkedList<String>()));
      assertTrue(indexer.add("gcc1", new LinkedList<String>(Arrays.asList("gcc2"))));
      assertTrue(indexer.add("gcc2", new LinkedList<String>(Arrays.asList("gcc1"))));
   }

   public void testAddCircularDependency3() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("gcc1", new LinkedList<String>()));
      assertTrue(indexer.add("gcc1", new LinkedList<String>(Arrays.asList("gcc1"))));
   }

   public void testAddAndRemoveCircularDependency() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("gcc1", new LinkedList<String>()));
      assertTrue(indexer.add("gcc2", new LinkedList<String>()));
      assertTrue(indexer.add("gcc3", new LinkedList<String>()));
      assertTrue(indexer.add("gcc1", new LinkedList<String>(Arrays.asList("gcc2"))));
      assertTrue(indexer.add("gcc2", new LinkedList<String>(Arrays.asList("gcc3"))));
      assertTrue(indexer.add("gcc3", new LinkedList<String>(Arrays.asList("gcc1"))));
      assertTrue(indexer.add("gcc1", new LinkedList<String>()));
      assertTrue(indexer.add("gcc2", new LinkedList<String>()));
      assertTrue(indexer.add("gcc3", new LinkedList<String>()));
      assertTrue(indexer.remove("gcc1"));
      assertTrue(indexer.remove("gcc2"));
      assertTrue(indexer.remove("gcc3"));
   }

   public void testCountingDependents() {
      Indexer indexer = new Indexer();
      assertTrue(indexer.add("libgcc++", new LinkedList<String>()));
      assertTrue(indexer.add("gcc1", new LinkedList<String>(Arrays.asList("libgcc++"))));
      assertTrue(indexer.add("gcc2", new LinkedList<String>(Arrays.asList("libgcc++"))));
      assertTrue(indexer.add("gcc3", new LinkedList<String>(Arrays.asList("libgcc++"))));

      assertTrue(indexer.query("libgcc++"));
      assertTrue(indexer.query("gcc1"));
      assertTrue(indexer.query("gcc2"));
      assertTrue(indexer.query("gcc3"));

      assertFalse(indexer.remove("libgcc++"));
      assertTrue(indexer.remove("gcc1"));

      assertFalse(indexer.remove("libgcc++"));
      assertTrue(indexer.remove("gcc2"));

      assertFalse(indexer.remove("libgcc++"));
      assertTrue(indexer.remove("gcc3"));

      assertTrue(indexer.remove("libgcc++"));

      assertFalse(indexer.query("libgcc++"));
      assertFalse(indexer.query("gcc1"));
      assertFalse(indexer.query("gcc2"));
      assertFalse(indexer.query("gcc3"));
   }
}
