package ca.ubc.spl.fishtail.model.parser;

import ca.ubc.spl.fishtail.TokenUtil;
import java.util.List;

public class ActivePackages
{
  private ItemCounter<DomainName> domains = new ItemCounter();
  private ItemCounter<String> allTokens = new ItemCounter();

  public List<DomainName> getDomainNames() {
    return this.domains.getAll();
  }

  public List<String> getAllTokens() {
    return this.allTokens.getAll();
  }

  public List<String> getTopTokens() {
    return this.allTokens.getTopNItems(5);
  }

  public void addPackage(String pkg) {
    this.domains.incrementItem(DomainName.createDomainFromPackageName(pkg));
    for (String t : TokenUtil.toList(pkg, '.'))
    {
      this.allTokens.incrementItem(t);
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.parser.ActivePackages
 * JD-Core Version:    0.6.0
 */