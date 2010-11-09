package ca.ubc.spl.fishtail.model.engine;

import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.FishtailStatus;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.mylyn.commons.core.StatusHandler;

public class SearchHit
{
  private final String name;
  private final String url;
  private final String desc;
  private GoogleSearchEngine searchEngine;
  private int score = -1;
  private boolean visible = true;
  private String document;
  private static Map<String, String> documentCache = new HashMap();
  private SearchStrategy strategy;
  private static List<String> contentTypes = Arrays.asList(new String[] { "text/html", 
    "text/plain", "application/xml" });
  private static Comparator<SearchHit> comp;

  public SearchHit(GoogleSearchEngine searchEngine, String name, String url, String desc)
  {
    this.name = name;
    this.url = url;
    this.desc = desc;
    this.searchEngine = searchEngine;
  }

  public String getName() {
    return this.name;
  }

  public String getUrl() {
    return this.url;
  }

  public String getDesc() {
    return this.desc;
  }

  public GoogleSearchEngine getSearchEngine() {
    return this.searchEngine;
  }

  public String toString()
  {
    return this.name;
  }

  public int getScore() {
    return this.score;
  }

  public void setScore(int score)
  {
    this.score = score;
  }

  public boolean isScored() {
    return this.score != -1;
  }

  public String getDocument()
  {
    if (this.document == null) {
      String doc = (String)documentCache.get(this.url);
      if (doc != null) {
        this.document = doc;
      } else {
        HttpClient c = new HttpClient();
        if (!this.url.matches("http://.*"))
          return null;
        GetMethod g = new GetMethod(this.url);
        g.getParams().setSoTimeout(10000);
        try {
          int ret = c.executeMethod(g);
          if (ret != 200) {
            g.releaseConnection();
            return null;
          }
          String ct = g.getResponseHeader("Content-Type").getValue();
          int i = ct.indexOf(';');
          if (i >= 0)
            ct = ct.substring(0, i);
          if (!contentTypes.contains(ct)) {
            g.releaseConnection();
            return null; 
          } else {
            try {
              this.document = g.getResponseBodyAsString();
            } catch (Exception localException2) {
              BufferedReader isr = new BufferedReader(
                new InputStreamReader(g.getResponseBodyAsStream(), Charset.forName("UTF-8")));
              StringBuilder sb = new StringBuilder();
              String temp;
              while ((temp = isr.readLine()) != null)
              {
                sb.append(temp + "\n");
              }
              this.document = sb.toString();
            }
            documentCache.put(this.url, this.document);
          }
        } catch (Exception e) {
          StatusHandler.fail(new FishtailStatus("document download: " + this.url, e));
        } finally {
          g.releaseConnection();
        }
      }
    }
    return this.document;
  }

  public static Comparator<SearchHit> getDescendingComparator() {
    if (comp == null)
      comp = new Comparator<SearchHit>()
      {
        public int compare(SearchHit o1, SearchHit o2) {
          if (o2.getScore() > o1.getScore())
            return 1;
          if (o2.getScore() == o1.getScore()) {
            return 0;
          }
          return -1;
        }
      };
    return comp;
  }

  public SearchStrategy getStrategy() {
    return this.strategy;
  }

  public void setStrategy(SearchStrategy strategy) {
    this.strategy = strategy;
  }

  public boolean previouslyHit() {
    return FishtailPlugin.getDefault().getHitUrls().contains(this.url);
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.engine.SearchHit
 * JD-Core Version:    0.6.0
 */