package ca.ubc.spl.fishtail.model;

import ca.ubc.spl.fishtail.ActivityMonitor;
import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.FishtailStatus;
import ca.ubc.spl.fishtail.TokenUtil;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.AbstractErrorReporter;
import org.eclipse.mylyn.commons.core.StatusHandler;

public class AllSearches
{
  private static AllSearches instance = new AllSearches();

  private JdtDoiAdapter jda = new JdtDoiAdapter();
  private List<SearchStrategy> strategies = new ArrayList();

  private List<String> keywords = new ArrayList();
  public static final int DELAY = 5000;
  SearchListeners searchListeners = new SearchListeners();

  public static AllSearches getInstance()
  {
    return instance;
  }

  public List<String> getKeywords()
  {
    return this.keywords;
  }

  public void rerankAll() {
    Job rankJob = new RerankJob();
    rankJob.setSystem(true);
    rankJob.schedule();
  }

  public void setKeywords(List<String> words) {
    if (TokenUtil.hasDifference(this.keywords, words)) {
      this.keywords = words;
      this.searchListeners.fireKeywordRefresh();
    }
  }

  private AllSearches() {
    this.strategies.add(new SearchStrategy("Article", this.jda));
    this.strategies.add(new SearchStrategy("Blog", this.jda));
    this.strategies.add(new SearchStrategy("Example", this.jda));
    this.strategies.add(new SearchStrategy("Tutorial", this.jda));
  }

  public void executeAll(boolean delay, boolean show, boolean automatic) {
    Job execJob = new SearchJob(this.jda.getSearchTokens(), automatic);
    if (!show)
      execJob.setSystem(show);
    execJob.schedule(delay ? 5000 : 0);
  }

  public List<SearchStrategy> getStrategies() {
    return this.strategies;
  }

  public SearchListeners getSearchListeners()
  {
    return this.searchListeners;
  }

  public JdtDoiAdapter getAdapter() {
    return this.jda;
  }

  private class RerankJob extends OneJobAtATime
  {
    private RerankJob()
    {
      super(Type.Rerank);
    }

    protected IStatus runJob(IProgressMonitor monitor)
    {
      try
      {
        AllSearches.this.jda.generatePackagesAndTokens();

        int workItems = 0;

        for (int i = 0; i < AllSearches.this.strategies.size(); i++) {
          SearchStrategy ss = (SearchStrategy)AllSearches.this.strategies.get(i);

          workItems += ss.getAllHits().size();
        }
        monitor.beginTask(getName(), workItems);

        for (int i = 0; i < AllSearches.this.strategies.size(); i++) {
          SearchStrategy ss = (SearchStrategy)AllSearches.this.strategies.get(i);

          ss.rerankHits(monitor);
          if (monitor.isCanceled()) {
            AllSearches.this.searchListeners.fireContentRefresh();
            return Status.CANCEL_STATUS;
          }
        }

        AllSearches.this.searchListeners.fireContentRefresh();
        monitor.done();
      } catch (Exception e) {
        StatusHandler.log(new FishtailStatus(getName() + " Rerank failed", e));
      }
      return Status.OK_STATUS;
    }

    protected boolean shouldCancelCurrentlyRunningJob()
    {
      return true;
    }
  }

  private class SearchJob extends OneJobAtATime
  {
    private final List<String> searchTokens;
    private final boolean automatic;

    private SearchJob(List<String> searchTokens, boolean automatic)
    {
      super(Type.Search);
      this.searchTokens = searchTokens;
      this.automatic = automatic;
    }

    protected IStatus runJob(IProgressMonitor monitor)
    {
      if (!AllSearches.this.jda.isTaskActive()) {
        return Status.OK_STATUS;
      }
      AllSearches.this.jda.generatePackagesAndTokens();

      FishtailPlugin.getDefault().getMonitor().search(this.automatic);
      int n = 0;
      for (SearchStrategy ss : AllSearches.this.strategies) {
        n += ss.getWorkItems();
      }

      monitor.beginTask(getName(), n);
      List k = AllSearches.this.getKeywords();
      for (SearchStrategy s : AllSearches.this.strategies) {
        s.executeSearch(TokenUtil.toDelimited(k, ""), this.searchTokens, monitor);

        AllSearches.this.searchListeners.fireContentRefresh();
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }
      }

      monitor.done();
      return Status.OK_STATUS;
    }

    protected boolean shouldCancelCurrentlyRunningJob()
    {
      SearchJob that = (SearchJob)getCurrentRunningJob(this.jobType);
      if (that == null)
        return true;
      return TokenUtil.hasDifference(that.searchTokens, this.searchTokens);
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.AllSearches
 * JD-Core Version:    0.6.0
 */