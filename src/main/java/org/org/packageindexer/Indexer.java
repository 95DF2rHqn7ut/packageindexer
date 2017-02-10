package org.org.packageindexer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

class Indexer {
   private ConcurrentHashMap<String, Dependency> dependencyNodes;
   private NamespaceLock namespaceLock;

   public Indexer () {
      dependencyNodes = new ConcurrentHashMap<String, Dependency>();
      namespaceLock = new NamespaceLock();
   }

   public boolean query(String dependency) {
      System.out.println(Thread.currentThread() + "QUERY " + dependency);
      // dirty read
      if(dependencyNodes.containsKey(dependency)) {
         return true;
      }
      return false;
   }

   public boolean remove(String dependency) {
      //System.out.println(Thread.currentThread() + "REMOVE " + dependency);
      Dependency toDelete;

      // Attempt to lock this namespace
      ConcurrentSkipListSet<String> dependencySet;
      while (true) {
         // dirty read
         toDelete = dependencyNodes.get(dependency);
         if (toDelete == null) return true;
         if (toDelete.dependablesCount > 0 )return false;

         // Start transaction
         dependencySet = new ConcurrentSkipListSet<String>();
         dependencySet.addAll(toDelete.dependencies);
         dependencySet.add(dependency);
         System.out.println(Thread.currentThread() + " LOCK1 " + dependencySet);
         for(String dep : dependencySet) {
            namespaceLock.lock(dep);
         }
         // Now verify the state has not changed
         if( dependencyNodes.get(dependency) != toDelete ) {
            // The state has changed. Refresh and reapply command
            System.out.println(Thread.currentThread() + " UNLOCK2 " + dependencySet);
            for(String dep : dependencySet) {
               namespaceLock.unlock(dep);
            }
         } else {
            // success
            break;
         }
      }

      // The namespace is locked

      for(String dep : toDelete.dependencies) {
         dependencyNodes.get(dep).dependablesCount--;
      }

      dependencyNodes.remove(dependency);

      // Cleanup

      System.out.println(Thread.currentThread() + " UNLOCK3 " + dependencySet);
      for(String dep : dependencySet) {
         namespaceLock.unlock(dep);
      }

      return true;
   }

   public boolean add(String dependency, List<String> rawDependencies) {
      //System.out.println(Thread.currentThread() + "ADD " + dependency + "|" + rawDependencies);

      // Check if update, and if so, TRY to lock everything that needs to be updated
      ConcurrentSkipListSet<String> dependencySet;
      Dependency toUpdate;
      while(true) {
         // dirty read
         toUpdate = dependencyNodes.get(dependency);
         // Start transaction
         dependencySet = new ConcurrentSkipListSet<String>();
         if(toUpdate != null)dependencySet.addAll(toUpdate.dependencies);
         dependencySet.addAll(rawDependencies);
         dependencySet.add(dependency);
         System.out.println(Thread.currentThread() + " LOCK4 " + dependencySet);
         for(String dep : dependencySet) {
            namespaceLock.lock(dep);
         }
         if (toUpdate == dependencyNodes.get(dependency)) break;
         else {
            // State updated, refresh: we need to relock as the namespace has potentially changed
            System.out.println(Thread.currentThread() + " UNLOCK5 " + dependencySet);
            for(String dep : dependencySet) {
               namespaceLock.unlock(dep);
            }
         }
      }

      // Transaction started, check if all dependencies are present
      Dependency newDependency = new Dependency(rawDependencies);

      if (toUpdate != null && newDependency.dependencies.equals(toUpdate.dependencies)) {
         System.out.println(Thread.currentThread() + " UNLOCK5.5 " + dependencySet);
         for (String unlockDep : dependencySet) {
            namespaceLock.unlock(unlockDep);
         }
         return true;
      }

      for (String dep : newDependency.dependencies) {
         if (dependencyNodes.get(dep) == null) {
            System.out.println(Thread.currentThread() + " UNLOCK6 " + dependencySet);
            for (String unlockDep : dependencySet) {
               namespaceLock.unlock(unlockDep);
            }
            //System.out.println(Thread.currentThread() + " ADD " + dependency + "|" + rawDependencies + " not found: " + dep);
            return false; // missing
         }
      }
      // All dependencies present

      // Update if needed: remove old dependee counts
      if (toUpdate != null) {
         for(String dep : toUpdate.dependencies) {
            dependencyNodes.get(dep).dependablesCount--;
         }
      }

      // Increment new dependable counts
      for(String dep : newDependency.dependencies) {
         dependencyNodes.get(dep).dependablesCount++;
      }

      // Copy the dependable count
      if (toUpdate != null) newDependency.dependablesCount = toUpdate.dependablesCount;

      dependencyNodes.put(dependency, newDependency);
      //System.out.println(Thread.currentThread() + " ADD " + dependency + "|" + rawDependencies + " added");

      // CLEANUP
      System.out.println(Thread.currentThread() + " UNLOCK7 " + dependencySet);
      for (String unlockDep : dependencySet) {
         namespaceLock.unlock(unlockDep);
      }

      return true;
   }

   public void status() {
      namespaceLock.status();
   }
}
