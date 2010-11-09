package ca.ubc.spl.fishtail.model.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemCounter<T>
{
  private Map<T, Integer> items = new HashMap();

  public List<T> getTopNItems(int n) {
    TIntegerComparator comp = new TIntegerComparator();
    List list = new ArrayList((Collection)this.items.entrySet());
    Collections.sort(list, comp);

    List ret = new ArrayList();

    for (int i = 0; (i < n) && (i < list.size()); i++) {
      ret.add(((Map.Entry)list.get(i)).getKey());
    }

    return ret;
  }

  public int getCount(T item) {
    Integer i = (Integer)this.items.get(item);
    if (i != null)
      return i.intValue();
    return 0;
  }

  public int size() {
    return this.items.size();
  }

  public List<T> getAll() {
    return getTopNItems(this.items.size());
  }

  public void remove(T item) {
    this.items.remove(item);
  }

  public void incrementItem(T item) {
    incrementItem(item, 1);
  }

  public void incrementItem(T item, int nTimes) {
    if (item == null)
      return;
    Integer old = (Integer)this.items.get(item);
    if (old == null)
      this.items.put(item, Integer.valueOf(nTimes));
    else
      this.items.put(item, Integer.valueOf(old.intValue() + nTimes));
  }

  public String toString()
  {
    return this.items.toString();
  }

  public class TIntegerComparator<V>
    implements Comparator<Map.Entry<V, Integer>>
  {
    public TIntegerComparator()
    {
    }

    public int compare(Map.Entry<V, Integer> arg0, Map.Entry<V, Integer> arg1)
    {
      return ((Integer)arg1.getValue()).intValue() - ((Integer)arg0.getValue()).intValue();
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.parser.ItemCounter
 * JD-Core Version:    0.6.0
 */