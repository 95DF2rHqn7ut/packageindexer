import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

class Indexer {
   private ConcurrentHashMap<String, Dependency> dependencyNodes;

   public Indexer () {
      dependencyNodes = new ConcurrentHashMap<String, Dependency>();
   }

   public boolean query(String dependency) {
      System.out.println("QUERY " + dependency);
      if(dependencyNodes.contains(dependency)) {
         System.out.println("QUERY " + dependency + " - found");
         return true;
      }
      System.out.println("QUERY " + dependency + " - not found");
      return false;
   }

   public boolean remove(String dependency) {
      System.out.println("REMOVE " + dependency);
      Dependency toDelete = dependencyNodes.get(dependency);
      if(toDelete == null) {
         System.out.println("REMOVE " + dependency + " - already gone");
         return true;
      } else {
         if(toDelete.dependablesCount > 0) {
            System.out.println("REMOVE " + dependency + " - cannot delete");
            return false;
         } else {
            if(toDelete.dependablesCount < 0) {
               throw new RuntimeException("concurrent modification exception");
            }

            orderedLock(dependency, toDelete.dependencies);

            // All nodes to be modified are locked. Ordered locking ensures no deadlocks.
            for(String dependee : toDelete.dependencies) {
               dependencyNodes.get(dependee).dependablesCount--;
            }

            dependencyNodes.remove(dependency);

            orderedUnlock(dependency, toDelete.dependencies);
         }
      }

      System.out.println("REMOVE " + dependency + " - deleted");
      return true;
   }

   public boolean add(String dependency, List<String> rawDependencies) {
      System.out.println("ADD " + dependency + "|" +rawDependencies);
      Dependency created = new Dependency(rawDependencies);

      // You can always index when no dependencies!
      if(rawDependencies.size() == 0) {
         System.out.println("ADD " + dependency + " - no dependencies");
         dependencyNodes.put(dependency, created);
         return true;
      }

      // Check if it can be indexed - all dependencies must be indexed
      for(String dep : created.dependencies) {
         if(!dependencyNodes.contains(dep)) {
            // It cannot be indexed
            System.out.println("ADD " + dependency + " - missing dependency " + "'" + dep + "'");
            return false;
         }
      }

      Dependency existing = dependencyNodes.get(dependency);

      if (existing == null) {
         // Create a new one

         orderedLock(dependency, created.dependencies);

         for(String dep : created.dependencies) {
            dependencyNodes.get(dep).dependablesCount++;
         }

         dependencyNodes.put(dependency, created);

         orderedUnlock(dependency, created.dependencies);

      } else {
         // Update an existing one

         orderedLock(dependency, created.dependencies, existing.dependencies);

         for(String dep : existing.dependencies) {
            dependencyNodes.get(dep).dependablesCount--;
         }

         for(String dep : created.dependencies) {
            dependencyNodes.get(dep).dependablesCount++;
         }

         dependencyNodes.remove(dependency);
         dependencyNodes.put(dependency, created);

         orderedUnlock(dependency, created.dependencies, existing.dependencies);
      }

      System.out.println("ADD " + dependency + " - added ");
      return true;
   }

   // Some common code for ordered locking and unlocking

   private void orderedLock(String dependency, ConcurrentSkipListSet<String> dependencies) {
      orderedLock(dependency, dependencies, new ConcurrentSkipListSet<String>());
   }

   private void orderedLock(String dependency, ConcurrentSkipListSet<String> dep1, ConcurrentSkipListSet<String> dep2) {
      ConcurrentSkipListSet<String> orderedLocks = new ConcurrentSkipListSet();
      orderedLocks.addAll(dep1);
      orderedLocks.addAll(dep2);
      orderedLocks.add(dependency);

      // Lock all modifiable objects
      for(String dep : orderedLocks) {
         dependencyNodes.get(dep).lock.lock();
      }
   }

   private void orderedUnlock(String dependency, ConcurrentSkipListSet<String> dependencies) {
      orderedUnlock(dependency, dependencies, new ConcurrentSkipListSet<String>());
   }

   private void orderedUnlock(String dependency, ConcurrentSkipListSet<String> dep1, ConcurrentSkipListSet<String> dep2) {
      ConcurrentSkipListSet<String> orderedLocks = new ConcurrentSkipListSet();
      orderedLocks.addAll(dep1);
      orderedLocks.addAll(dep2);
      orderedLocks.add(dependency);
      dependencyNodes.get(dependency).lock.unlock();
      for(String dep : orderedLocks) {
         dependencyNodes.get(dep).lock.unlock();
      }
   }
}
