package ca.ubc.spl.fishtail;

import java.util.HashSet;
import java.util.Set;

public class BlacklistDelta
{
  protected Set<String> toRemove = new HashSet();
  protected Set<String> toAdd = new HashSet();

  public Set<String> getToRemove() {
    return this.toRemove;
  }

  public Set<String> getToAdd() {
    return this.toAdd;
  }

  public void addAdd(String s) {
    if (this.toRemove.contains(s))
      this.toRemove.remove(s);
    else
      this.toAdd.add(s);
  }

  public void addRemove(String s)
  {
    if (this.toAdd.contains(s))
      this.toAdd.remove(s);
    else
      this.toRemove.add(s);
  }

  public void clear()
  {
    this.toRemove.clear();
    this.toAdd.clear();
  }

  public String toString()
  {
    return "+" + this.toAdd + " -" + this.toRemove;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.BlacklistDelta
 * JD-Core Version:    0.6.0
 */