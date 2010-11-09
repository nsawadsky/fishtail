package ca.ubc.spl.fishtail;

import java.util.List;

public class MultiThreadDequeuer<T>
{
  private List<T> items;
  private int i = 0;

  public MultiThreadDequeuer(List<T> items) {
    this.items = items;
  }

  public synchronized T getNext() {
    if (!hasNext())
      return null;
    return this.items.get(this.i++);
  }

  private boolean hasNext() {
    return this.i < this.items.size();
  }

  public synchronized void restart() {
    this.i = 0;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.MultiThreadDequeuer
 * JD-Core Version:    0.6.0
 */