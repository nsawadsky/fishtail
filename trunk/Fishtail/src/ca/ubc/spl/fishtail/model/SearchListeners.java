package ca.ubc.spl.fishtail.model;

import ca.ubc.spl.fishtail.IKeywordListener;
import ca.ubc.spl.fishtail.views.IFishtailView;
import ca.ubc.spl.fishtail.views.SearchContentProvider;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.swt.widgets.Display;

public class SearchListeners
{
  private Set<SearchContentProvider> searchListeners = new HashSet();
  private Set<IKeywordListener> searchViewListeners = new HashSet();

  public void addSearchListener(SearchContentProvider listener) {
    if (!this.searchListeners.contains(listener))
      this.searchListeners.add(listener);
  }

  public void addSearchViewListener(IKeywordListener kl) {
    if (!this.searchViewListeners.contains(kl))
      this.searchViewListeners.add(kl);
  }

  public void removeSearchListener(SearchContentProvider l) {
    this.searchListeners.remove(l);
  }

  public void removeKeywordListener(IFishtailView kl) {
    this.searchViewListeners.remove(kl);
  }

  void fireRefreshViews() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        for (IKeywordListener kl : SearchListeners.this.searchViewListeners)
          if ((kl instanceof IFishtailView)) {
            IFishtailView fv = (IFishtailView)kl;
            fv.refreshView();
          }
      }
    });
  }

  void fireContentRefresh() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        for (SearchContentProvider cp : SearchListeners.this.searchListeners)
          cp.refresh();
      }
    });
  }

  void fireKeywordRefresh() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        for (IKeywordListener sv : SearchListeners.this.searchViewListeners)
          sv.keywordsChanged();
      }
    });
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.SearchListeners
 * JD-Core Version:    0.6.0
 */