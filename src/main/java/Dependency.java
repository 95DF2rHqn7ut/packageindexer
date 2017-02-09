import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Dependency {
   public Lock lock;
   public long dependablesCount;
   // List of dependencies. ImmutableList would have been best.
   public ConcurrentSkipListSet<String> dependencies;
   // not implemented; TODO: DAG circular dependency check
   public boolean mark;

   public Dependency() {
      lock = new ReentrantLock();
      dependablesCount = 0;
      this.dependencies = new ConcurrentSkipListSet<String>();
   }

   public Dependency(List<String> dependencies) {
      lock = new ReentrantLock();
      dependablesCount = 0;
      this.dependencies = new ConcurrentSkipListSet<String>();
      this.dependencies.addAll(dependencies);
   }
}
