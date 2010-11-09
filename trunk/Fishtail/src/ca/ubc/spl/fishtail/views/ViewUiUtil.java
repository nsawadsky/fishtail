package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public abstract class ViewUiUtil
{
  private static final Color PURPLE = new Color(Display.getDefault(), 85, 26, 139);

  public static TreeViewer createTreeViewer(Composite resultViewer) {
    TreeViewer treeViewer = new TreeViewer(resultViewer, 0);

    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = 4;
    layoutData.verticalAlignment = 4;
    treeViewer.getControl().setLayoutData(layoutData);

    IContentProvider provider = new SearchContentProvider();
    treeViewer.setContentProvider(provider);
    ILabelProvider labelProvider = new SearchLabelProvider();
    treeViewer.setLabelProvider(labelProvider);

    treeViewer.setInput(AllSearches.getInstance());

    return treeViewer;
  }

  public static void expandFirst(TreeViewer treeViewer) {
    List s = AllSearches.getInstance().getStrategies();
    if (s.size() > 0)
      treeViewer.expandToLevel(s.get(0), 1);
  }

  public static void expandAll(TreeViewer treeViewer)
  {
    List<SearchStrategy> s = AllSearches.getInstance().getStrategies();

    for (SearchStrategy ss : s)
      treeViewer.expandToLevel(ss, 1);
  }

  public static Object getSelectedElement(TreeViewer treeViewer)
  {
    if (treeViewer == null) {
      return null;
    }
    return ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
  }

  public static void openBrowser(Object selectedElement, IFishtailView view)
  {
    if ((selectedElement instanceof SearchHit)) {
      SearchHit hit = (SearchHit)selectedElement;
      FishtailPlugin.getDefault().openUrl(hit, view);
    }
  }

  public static Text createUIWidgetSearchText(Composite parent, boolean hasBorder)
  {
    Text queryBox = new Text(parent, hasBorder ? 2048 : 0);

    GC gc = new GC(parent);
    gc.setFont(parent.getFont());
    FontMetrics fontMetrics = gc.getFontMetrics();
    gc.dispose();

    GridData data = new GridData(768);
    data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
    data.horizontalAlignment = 4;

    queryBox.setLayoutData(data);

    return queryBox;
  }

  public static Color getPreviouslyHitColor() {
    return PURPLE;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.ViewUiUtil
 * JD-Core Version:    0.6.0
 */