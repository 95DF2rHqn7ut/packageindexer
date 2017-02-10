package org.org.packageindexer;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a simple DTO to store dependency node data for the DAG
 */
class Dependency {
   public long dependablesCount;
   // List of dependencies. ImmutableList would have been best.
   public ConcurrentSkipListSet<String> dependencies;
   // not implemented; TODO: DAG circular dependency check
   public boolean mark;

   public Dependency() {
      dependablesCount = 0;
      this.dependencies = new ConcurrentSkipListSet<String>();
   }

   public Dependency(List<String> dependencies) {
      dependablesCount = 0;
      this.dependencies = new ConcurrentSkipListSet<String>();
      this.dependencies.addAll(dependencies);
   }
}
