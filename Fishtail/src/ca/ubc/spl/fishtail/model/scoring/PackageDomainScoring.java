package ca.ubc.spl.fishtail.model.scoring;

import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.ActivePackages;
import ca.ubc.spl.fishtail.model.parser.DomainName;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;

public class PackageDomainScoring extends ScoringStrategy
{
  public PackageDomainScoring(JdtDoiAdapter adapter)
  {
    super(adapter);
  }

  public int scorePage(SearchHit hit) throws Exception
  {
    int score = 0;
    for (DomainName host : this.adapter.getActivePackages().getDomainNames()) {
      if (DomainName.hostMatchesUrl(host.toString(), hit.getUrl()))
        score++;
    }
    return score;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.scoring.PackageDomainScoring
 * JD-Core Version:    0.6.0
 */