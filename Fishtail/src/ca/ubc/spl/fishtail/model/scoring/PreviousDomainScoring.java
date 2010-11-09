package ca.ubc.spl.fishtail.model.scoring;

import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.DomainName;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;
import java.util.List;

public class PreviousDomainScoring extends ScoringStrategy
{
  public PreviousDomainScoring(JdtDoiAdapter adapter)
  {
    super(adapter);
  }

  public int scorePage(SearchHit hit) throws Exception
  {
    int score = 0;
    try {
      List<String> domains = FishtailPlugin.getDefault().getDomainsHit();
      for (String domain : domains) {
        if (DomainName.hostMatchesUrl(domain, hit.getUrl()))
          score++;
      }
    }
    catch (NullPointerException localNullPointerException)
    {
    }
    return score;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.scoring.PreviousDomainScoring
 * JD-Core Version:    0.6.0
 */