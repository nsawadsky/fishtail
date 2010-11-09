package ca.ubc.spl.fishtail.model.scoring;

import ca.ubc.spl.fishtail.Blacklist;
import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HitScorer
{
  private List<ScoringStrategy> scorers = Collections.synchronizedList(new ArrayList());
  private final JdtDoiAdapter adapter;

  public HitScorer(JdtDoiAdapter a)
  {
    this.adapter = a;
    this.scorers.add(new DocumentTokenScoring(this.adapter));
    this.scorers.add(new PackageDomainScoring(this.adapter));
    this.scorers.add(new PreviousDomainScoring(this.adapter));
  }

  public void scorePage(SearchHit hit) throws Exception {
    if (hit == null) {
      return;
    }
    int score = 0;
    try
    {
      if (FishtailPlugin.getDefault().getBlacklist().isBlacklisted(hit))
      {
        hit.setVisible(false);
      } else {
        for (ScoringStrategy s : this.scorers) {
          score += s.scorePage(hit);
        }
        hit.setVisible(true);
        hit.setScore(score);
      }
    }
    catch (NullPointerException localNullPointerException)
    {
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.scoring.HitScorer
 * JD-Core Version:    0.6.0
 */