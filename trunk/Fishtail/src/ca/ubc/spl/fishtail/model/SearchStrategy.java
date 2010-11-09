package ca.ubc.spl.fishtail.model;

import ca.ubc.spl.fishtail.ActivityMonitor;
import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.FishtailStatus;
import ca.ubc.spl.fishtail.MultiThreadDequeuer;
import ca.ubc.spl.fishtail.TokenUtil;
import ca.ubc.spl.fishtail.model.engine.GoogleSearchEngine;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;
import ca.ubc.spl.fishtail.model.scoring.HitScorer;
import ca.ubc.spl.fishtail.views.IFishtailView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;

public class SearchStrategy
{
  protected static final int MORE_HITS = 5;
  private static final int NUM_THREADS = 3;
  private String args;
  protected final String customKeywords;
  protected final JdtDoiAdapter jda;
  private int maxShown;
  protected GoogleSearchEngine searchEngine;
  protected List<SearchHit> shownHits = new ArrayList();

  protected List<SearchHit> unshownHits = new ArrayList();
  private List<String> searchTokens;

  public SearchStrategy(String customKeywords, JdtDoiAdapter jda)
  {
    this.jda = jda;
    this.customKeywords = customKeywords;
    resetHitsShown();
  }

  protected void addHits(List<SearchHit> hits, IProgressMonitor monitor) {
    for (SearchHit hit : hits) {
      hit.setStrategy(this);
    }
    rankHits(hits, monitor);
    this.unshownHits.addAll(hits);
    showTopHits();
  }

  protected void clearHits() {
    this.unshownHits.clear();
    this.shownHits.clear();
    resetHitsShown();
  }

  public void executeSearch(String keywords, List<String> searchTokens, IProgressMonitor monitor) {
    this.searchTokens = searchTokens;
    if (searchTokens.size() == 0)
      return;
    String args = generateArgs(this.customKeywords, searchTokens, keywords);
    try
    {
      clearHits();
      this.searchEngine = new GoogleSearchEngine(args, this.maxShown * 2);
      addHits(this.searchEngine.getHits(), monitor);
    } catch (Exception localException) {
    }
  }

  protected String generateArgs(String s, List<String> searchTokens, String keywords) {
    s = spaceEnding(s);
    keywords = spaceEnding(keywords);
    this.args = (s + keywords + TokenUtil.toDelimited(searchTokens, " "));
    return this.args;
  }

  private String spaceEnding(String word) {
    if ((word != null) && (!word.equals("")) && (!word.endsWith(" ")))
      word = word + " ";
    return word;
  }
  @Deprecated
  public List<SearchHit> getAllHits() {
    List allHits = new ArrayList();
    allHits.addAll(this.unshownHits);
    allHits.addAll(this.shownHits);
    return allHits;
  }

  public String getArgs() {
    return this.args;
  }

  public String getName() {
    return this.customKeywords + "s";
  }

  public List<SearchHit> getShownHits()
  {
    return this.shownHits;
  }

  @Deprecated
  public List<SearchHit> getUnshownHits()
  {
    return this.unshownHits;
  }

  public int getWorkItems()
  {
    return 10;
  }

  protected void increaseHitsShown() {
    this.maxShown += 5;
  }

  protected void rankHits(List<SearchHit> hits, IProgressMonitor monitor) {
    List<Thread> scorerThreads = new ArrayList();
    MultiThreadDequeuer mtd = new MultiThreadDequeuer(hits);
    for (int i = 0; i < 3; i++) {
      Thread t = new ScorerThread(mtd, monitor);
      scorerThreads.add(t);
      t.start();
    }

    for (Thread t : scorerThreads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    Collections.sort(hits, SearchHit.getDescendingComparator());
  }

  public void broadenCategory(IFishtailView view, IJobChangeListener listener)
  {
    BroadenCategoryJob j = new BroadenCategoryJob(view);
    j.schedule();
    if (listener != null)
      j.addJobChangeListener(listener);
  }

  public void rerankHits(IProgressMonitor monitor)
  {
    rankHits(getAllHits(), monitor);
    showTopHits();
  }

  private void resetHitsShown() {
    this.maxShown = 5;
  }

  private void showTopHits() {
    List oldShown = new ArrayList(this.shownHits.size());
    oldShown.addAll(this.shownHits);

    this.unshownHits.addAll(this.shownHits);
    this.shownHits.clear();

    List allHits = new ArrayList(this.unshownHits.size());

    allHits.addAll(this.unshownHits);

    Collections.sort(allHits, SearchHit.getDescendingComparator());
    for (int i = 0; (this.shownHits.size() < this.maxShown) && (i < allHits.size()); i++) {
      SearchHit item = (SearchHit)allHits.get(i);
      if (item.getScore() < 0)
        break;
      if (item.isVisible()) {
        this.shownHits.add(item);
        this.unshownHits.remove(item);
      }
    }
  }

  public List<String> getSearchTokens()
  {
    return this.searchTokens;
  }

  private class BroadenCategoryJob extends Job
  {
    private IFishtailView view;
    private boolean done = false;

    public BroadenCategoryJob(IFishtailView view) {
      super("Broaden Category Job, view = " + view);
      this.view = view;
    }

    protected IStatus run(IProgressMonitor monitor)
    {
      try {
        this.done = false;
        new DoneThread(this).start();
        if (SearchStrategy.this.searchEngine == null) {
          this.done = true;
          return Status.OK_STATUS;
        }
        FishtailPlugin.getDefault().getMonitor().broadenCategory(SearchStrategy.this, this.view);

        SearchStrategy.this.jda.generatePackagesAndTokens();

        List newHits = SearchStrategy.this.searchEngine.broadenSearch();
        monitor.beginTask(getName(), SearchStrategy.this.getWorkItems());
        SearchStrategy.this.increaseHitsShown();
        SearchStrategy.this.addHits(newHits, monitor);
        AllSearches.getInstance().searchListeners.fireContentRefresh();
        monitor.done();

        this.done = true;
      } catch (Exception e) {
        StatusHandler.log(new FishtailStatus(getName() + " Refine failed", e));
      }
      return ASYNC_FINISH;
    }
    private class DoneThread extends Thread {
      private SearchStrategy.BroadenCategoryJob j;

      public DoneThread(SearchStrategy.BroadenCategoryJob j) {
        this.j = j;
      }

      public void run()
      {
        while (!this.j.done)
          try {
            sleep(500L);
          }
          catch (InterruptedException localInterruptedException) {
          }
        this.j.done(Status.OK_STATUS);
      }
    }
  }

  class ScorerThread extends Thread
  {
    private MultiThreadDequeuer<SearchHit> hits;
    private IProgressMonitor monitor;

    public ScorerThread( MultiThreadDequeuer<SearchHit> hits, IProgressMonitor monitor)
    {
      this.hits = hits;
      this.monitor = monitor;
    }

    public void run()
    {
      HitScorer scorer = new HitScorer(SearchStrategy.this.jda);
      SearchHit hit;
      while ((hit = (SearchHit)this.hits.getNext()) != null)
        try
        {
          scorer.scorePage(hit);
          if (this.monitor != null)
            this.monitor.worked(1);
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.SearchStrategy
 * JD-Core Version:    0.6.0
 */