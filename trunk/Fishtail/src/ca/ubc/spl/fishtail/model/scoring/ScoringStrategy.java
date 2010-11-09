package ca.ubc.spl.fishtail.model.scoring;

import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;

public abstract class ScoringStrategy
{
  protected JdtDoiAdapter adapter;

  public ScoringStrategy(JdtDoiAdapter adapter)
  {
    this.adapter = adapter;
  }

  public abstract int scorePage(SearchHit paramSearchHit)
    throws Exception;
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.scoring.ScoringStrategy
 * JD-Core Version:    0.6.0
 */