package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.Blacklist;
import ca.ubc.spl.fishtail.BlacklistDelta;
import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.DomainName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.osgi.framework.Bundle;

public class BlacklistAdapter
  implements ISelectionChangedListener
{
  private static final String BROADEN_CATEGORY = "Broaden Category (Alt+click)";
  private static final String BLACKLIST_HOST = "Blacklist Host";
  private static final String BLACKLIST_HOST_IMAGE = "/images/blacklist-host.png";
  private static final String BROADEN_CATEGORY_IMAGE = "/images/broaden.png";
  private TreeViewer tree;
  private IAction blacklistHostAction;
  private IAction broadenCategoryAction;
  protected final IFishtailView view;
  private IAction openAction = new Action("Open")
  {
    public void run()
    {
      BlacklistAdapter.this.view.openSelectedHit();
    }
  };

  public BlacklistAdapter(TreeViewer tree, IFishtailView view)
  {
    this.tree = tree;
    this.view = view;
  }

  public void selectionChanged(SelectionChangedEvent event) {
    ISelection sel = event.getSelection();

    if ((sel instanceof TreeSelection)) {
      TreeSelection ts = (TreeSelection)sel;
      Object element1 = ts.getFirstElement();

      this.openAction.setEnabled(false);
      this.blacklistHostAction.setEnabled(false);
      this.blacklistHostAction.setText("Blacklist Host");
      this.broadenCategoryAction.setEnabled(false);

      if ((element1 instanceof SearchHit)) {
        this.openAction.setEnabled(true);
        SearchHit hit = (SearchHit)element1;
        this.blacklistHostAction.setEnabled(true);
        this.blacklistHostAction.setText("Blacklist Host: " + 
          DomainName.getHost(hit.getUrl()));
      } else if ((element1 instanceof SearchStrategy)) {
        this.broadenCategoryAction.setEnabled(true);
      }
    }
  }

  public void addToolbarActions(IContributionManager manager) {
    createActions();
    manager.add(this.blacklistHostAction);
    manager.add(this.broadenCategoryAction);
  }

  public void addContextMenuActions(IContributionManager manager) {
    createActions();

    manager.add(this.openAction);
    manager.add(new Separator("additions"));
    manager.add(this.blacklistHostAction);
    manager.add(this.broadenCategoryAction);
  }

  private void createActions()
  {
    if (this.blacklistHostAction != null) {
      return;
    }
    this.blacklistHostAction = new Action("Blacklist Host")
    {
      public void run() {
        BlacklistAdapter.this.blacklist();
      }
    };
    this.blacklistHostAction.setToolTipText("Blacklist Host");
    this.blacklistHostAction.setImageDescriptor(ImageDescriptor.createFromURL(
      FishtailPlugin.getDefault().getBundle().getResource("/images/blacklist-host.png")));

    this.broadenCategoryAction = new Action("Broaden Category (Alt+click)")
    {
      public void run() {
        BlacklistAdapter.this.broadenCategory();
      }
    };
    this.broadenCategoryAction.setToolTipText("Broaden Category (Alt+click)");
    this.broadenCategoryAction.setImageDescriptor(ImageDescriptor.createFromURL(
      FishtailPlugin.getDefault().getBundle().getResource("/images/broaden.png")));

    this.broadenCategoryAction.setEnabled(false);
    this.openAction.setEnabled(false);
    this.blacklistHostAction.setEnabled(false);

    this.tree.addSelectionChangedListener(this);
  }

  protected void broadenCategory()
  {
    ISelection sel = this.tree.getSelection();
    if ((sel instanceof TreeSelection)) {
      TreeSelection ts = (TreeSelection)sel;
      Object element1 = ts.getFirstElement();
      if ((element1 instanceof SearchStrategy)) {
        SearchStrategy ss = (SearchStrategy)element1;
        ss.broadenCategory(this.view, null);
      }
    }
  }

  private void blacklist() {
    ISelection sel = this.tree.getSelection();
    if ((sel instanceof TreeSelection)) {
      TreeSelection ts = (TreeSelection)sel;
      Object element1 = ts.getFirstElement();
      if ((element1 instanceof SearchHit)) {
        SearchHit hit = (SearchHit)element1;

        BlacklistDelta delta = new BlacklistDelta();

        String regex = DomainName.createRegexFromHost(hit.getUrl());
        delta.addAdd(regex);
        FishtailPlugin.getDefault().getBlacklist().commit(delta);
      }
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.BlacklistAdapter
 * JD-Core Version:    0.6.0
 */