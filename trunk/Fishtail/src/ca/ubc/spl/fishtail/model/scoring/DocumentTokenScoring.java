package ca.ubc.spl.fishtail.model.scoring;

import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;
import java.util.ArrayList;
import java.util.List;

public class DocumentTokenScoring extends ScoringStrategy
{
  public DocumentTokenScoring(JdtDoiAdapter adapter)
  {
    super(adapter);
  }

  public int scorePage(SearchHit hit) throws Exception
  {
    int score = 0;
    String s;
    if ((hit == null) || ((s = hit.getDocument()) == null))
      return score;
    List<String> tokens = new ArrayList();
    tokens.addAll(this.adapter.getDocumentTerms());
    tokens.addAll(this.adapter.getSearchTokens());

    for (String token : tokens) {
      int loc = 0;

      if ((loc = s.indexOf(token, loc + 1)) < 0)
        continue;
      score++;
    }

    return score;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.scoring.DocumentTokenScoring
 * JD-Core Version:    0.6.0
 */