package org.org.packageindexer;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This thread work unit queue is a common way to implement simple but effective blocking behavior in
 * fixed size java thread pools. Not for use in dynamic thread pools.
 * @param <E> - Type of element held in this queue
 */
public final class ThreadQueue<E> extends LinkedBlockingQueue<E>
{
   public ThreadQueue(int maxSize)
   {
      super(maxSize);
   }

   /**
    * A blocking offer. This changes the interface contract.
    * @param e The element to insert.
    * @return true if the element was successfully inserted
    */
   @Override
   public boolean offer(E e)
   {
      try {
         put(e);
      } catch(InterruptedException ie) {
         Thread.currentThread().interrupt();
         return false;
      }
      return true;
   }
}
