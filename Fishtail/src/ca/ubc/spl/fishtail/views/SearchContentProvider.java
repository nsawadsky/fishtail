package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.SearchListeners;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import java.util.List;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class SearchContentProvider
  implements ITreeContentProvider
{
  private Viewer viewer;

  public Object[] getChildren(Object parentElement)
  {
    if ((parentElement instanceof SearchStrategy))
      return ((SearchStrategy)parentElement).getShownHits().toArray();
    if ((parentElement instanceof AllSearches)) {
      return ((AllSearches)parentElement).getStrategies().toArray();
    }
    return null;
  }

  public Object getParent(Object element) {
    if ((element instanceof SearchHit)) {
      SearchHit hit = (SearchHit)element;
      return hit.getSearchEngine();
    }if ((element instanceof SearchStrategy)) {
      return AllSearches.getInstance();
    }
    return null;
  }

  public boolean hasChildren(Object element)
  {
    return ((element instanceof SearchStrategy)) || 
      ((element instanceof AllSearches));
  }

  public Object[] getElements(Object inputElement)
  {
    return getChildren(inputElement);
  }

  public void dispose() {
    AllSearches.getInstance().getSearchListeners().removeSearchListener(this);
    this.viewer = null;
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if ((newInput instanceof AllSearches)) {
      AllSearches searches = (AllSearches)newInput;
      searches.getSearchListeners().addSearchListener(this);
    }
    this.viewer = viewer;
  }

  public void refresh() {
    this.viewer.refresh();
    if ((this.viewer instanceof TreeViewer)) {
      TreeViewer tv = (TreeViewer)this.viewer;
      tv.expandAll();
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.SearchContentProvider
 * JD-Core Version:    0.6.0
 */