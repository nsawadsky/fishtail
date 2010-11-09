package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.TokenUtil;
import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.SearchListeners;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class InplaceSearchDialog extends PopupDialog
  implements IFishtailView
{
  private static InplaceSearchDialog open;
  private Text keywordText;
  private Composite resultViewer;
  private TreeViewer treeViewer;
  private BlacklistAdapter blacklistDomainAdapter;

  public InplaceSearchDialog(Shell parent, int shellStyle)
  {
    super(parent, shellStyle, true, true, true, true, null, "Fishtail Search");
    create();

    AllSearches.getInstance().getSearchListeners().addSearchViewListener(this);
    open = this;
  }

  public void keywordsChanged() {
    if ((this.keywordText != null) && (!this.keywordText.isDisposed()))
      this.keywordText
        .setText(TokenUtil.toDelimited(AllSearches.getInstance().getKeywords(), " "));
  }

  public void setSize(int width, int height)
  {
    getShell().setSize(width, height);
  }

  public void setFocus() {
    getShell().forceFocus();
    this.keywordText.setFocus();
  }

  protected Control createTitleControl(Composite parent)
  {
    this.keywordText = ViewUiUtil.createUIWidgetSearchText(parent, false);

    keywordsChanged();

    createUIListeners();

    return this.keywordText;
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        InplaceSearchDialog.this.fillContextMenu(manager);
      }
    });
    Menu menu = menuMgr.createContextMenu(this.treeViewer.getControl());
    this.treeViewer.getControl().setMenu(menu);
  }

  private void fillContextMenu(IMenuManager manager)
  {
    this.blacklistDomainAdapter.addContextMenuActions(manager);
  }

  private void createUIListeners() {
    this.keywordText.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if ((e.keyCode == 13) || (e.keyCode == 16777296))
          try
          {
            String t = ((Text)e.widget).getText();

            AllSearches as = AllSearches.getInstance();
            as.setKeywords(TokenUtil.toList(t, ' '));
            as.executeAll(false, true, false);
          } catch (Exception localException) {
          }
        else if (e.character == '\033')
        {
          InplaceSearchDialog.this.close();
        }
      }

      public void keyReleased(KeyEvent e)
      {
      }
    });
  }

  private void createUIListenersTreeViewer() {
    Tree tree = this.treeViewer.getTree();
    tree.addListener(42, new Listener()
    {
      public void handleEvent(Event event)
      {
        TreeItem item = (TreeItem)event.item;

        Display d = Display.getDefault();
        if ((item.getData() instanceof SearchHit)) {
          SearchHit hit = (SearchHit)item.getData();
          if (hit.previouslyHit())
            item.setForeground(ViewUiUtil.getPreviouslyHitColor());
          else
            item.setForeground(d.getSystemColor(2));
        } else {
          item.setForeground(d.getSystemColor(2));
        }
      }
    });
    tree.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.character == '\033')
        {
          InplaceSearchDialog.this.close();
        }
      }

      public void keyReleased(KeyEvent e)
      {
      }
    });
    tree.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e) {
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        InplaceSearchDialog.this.openSelectedHit();
      }
    });
    tree.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e) {
        if ((e.stateMask & 0x10000) != 0) {
          Object element = ViewUiUtil.getSelectedElement(InplaceSearchDialog.this.treeViewer);
          if ((element instanceof SearchStrategy)) {
            InplaceSearchDialog.this.setInfoText("Broadening...");
            SearchStrategy ss = (SearchStrategy)element;
            ss.broadenCategory(InplaceSearchDialog.this, new JobChangeAdapter()
            {
              public void done(IJobChangeEvent event) {
                try {
                  Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                      InplaceSearchDialog.this.setInfoText("Broadening... Done!");
                    }
                  });
                }
                catch (Exception localException)
                {
                }
              }
            });
          }
        }
      }
    });
    SearchToolTipHandler toolTipHandler = new SearchToolTipHandler(this);
    toolTipHandler.activateHoverHelp(this.treeViewer.getControl());
  }

  public IFishtailView.ViewType getType() {
    return IFishtailView.ViewType.Inplace;
  }

  protected Control createDialogArea(Composite parent)
  {
    this.resultViewer = ((Composite)super.createDialogArea(parent));
    this.treeViewer = ViewUiUtil.createTreeViewer(this.resultViewer);
    createUIListenersTreeViewer();

    this.blacklistDomainAdapter = new BlacklistAdapter(this.treeViewer, this);
    hookContextMenu();

    this.treeViewer.getTree().setToolTipText("");
    ViewUiUtil.expandFirst(this.treeViewer);
    if (!FishtailPlugin.getDefault().getPreferenceStore().getBoolean(
      "BackgroundSearch")) {
      AllSearches.getInstance().executeAll(false, true, false);
    }
    return this.resultViewer;
  }

  public boolean close()
  {
    AllSearches.getInstance().getSearchListeners().removeSearchListener(
      (SearchContentProvider)this.treeViewer.getContentProvider());

    open = null;
    return super.close();
  }

  public void refreshView() {
    if (!this.treeViewer.getControl().isDisposed())
      this.treeViewer.refresh();
  }

  public TreeViewer getTreeViewer() {
    return this.treeViewer;
  }

  public void openSelectedHit() {
    Object selectedElement = ViewUiUtil.getSelectedElement(this.treeViewer);
    ViewUiUtil.openBrowser(selectedElement, this);
    close();
  }

  public static InplaceSearchDialog getOpen() {
    return open;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.InplaceSearchDialog
 * JD-Core Version:    0.6.0
 */