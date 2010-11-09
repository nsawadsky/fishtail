package ca.ubc.spl.fishtail;

import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter;
import java.util.List;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.IInteractionElement;


public class MylynContextListener
  extends AbstractContextListener
{
  public void contextActivated(IInteractionContext context)
  {
  }

  public void contextCleared(IInteractionContext context)
  {
  }

  public void contextDeactivated(IInteractionContext context)
  {
  }

  public void elementDeleted(IInteractionElement element)
  {
  }

  public void interestChanged(List<IInteractionElement> elements)
  {
    contextChanged((IInteractionElement)elements.get(elements.size() - 1));
  }

  public void landmarkAdded(IInteractionElement element) {
    contextChanged(element);
  }

  public void landmarkRemoved(IInteractionElement element) {
  }

  public void relationsChanged(IInteractionElement element) {
  }

  private void contextChanged(IInteractionElement element) {
    JdtDoiAdapter jda = AllSearches.getInstance().getAdapter();
    jda.setElement(element);
    if (jda.shouldSearch())
      AllSearches.getInstance().executeAll(false, true, true);
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.MylynContextListener
 * JD-Core Version:    0.6.0
 */