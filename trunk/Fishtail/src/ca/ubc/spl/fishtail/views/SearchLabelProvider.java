package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class SearchLabelProvider
  implements ILabelProvider
{
  public Image getImage(Object element)
  {
    return null;
  }

  public String getText(Object element) {
    if ((element instanceof SearchStrategy)) {
      SearchStrategy s = (SearchStrategy)element;
      return s.getName();
    }if ((element instanceof SearchHit)) {
      SearchHit hit = (SearchHit)element;
      return 
        hit.getName();
    }
    return "";
  }

  public void addListener(ILabelProviderListener listener)
  {
  }

  public void dispose() {
  }

  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  public void removeListener(ILabelProviderListener listener)
  {
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.SearchLabelProvider
 * JD-Core Version:    0.6.0
 */