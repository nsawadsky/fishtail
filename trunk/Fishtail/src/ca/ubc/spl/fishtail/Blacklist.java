package ca.ubc.spl.fishtail;

import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.DomainName;
import java.util.HashSet;
import java.util.Set;

public class Blacklist
{
  private final Set<IBlacklistMonitor> monitors = new HashSet();
  private Set<String> items = new HashSet();

  public void add(String host)
  {
    this.items.add(host);
  }

  public void remove(String host) {
    this.items.remove(host);
  }

  public Set<String> getItems() {
    return this.items;
  }

  public void addMonitor(IBlacklistMonitor m) {
    if (m != null)
      this.monitors.add(m);
  }

  public void removeMonitor(IBlacklistMonitor m) {
    this.monitors.remove(m);
  }

  public boolean isBlacklisted(SearchHit hit) {
    for (String host : getItems())
    {
      if (DomainName.hostMatchesRegex(hit.getUrl(), host))
      {
        return true;
      }
    }
    return false;
  }

  public void commit(BlacklistDelta delta) {
    commit(delta, true);
  }

  void commit(BlacklistDelta delta, boolean notifyListeners) {
    if ((delta.toAdd.size() == 0) && (delta.toRemove.size() == 0)) {
      return;
    }
    for (String add : delta.getToAdd()) {
      add(add);
    }
    for (String rem : delta.getToRemove()) {
      remove(rem);
    }
    if (notifyListeners)
      for (IBlacklistMonitor m : this.monitors)
        m.blacklistChanged(delta);
  }

  public String toString()
  {
    return this.items.toString();
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.Blacklist
 * JD-Core Version:    0.6.0
 */