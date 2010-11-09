package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.TokenUtil;
import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.SearchListeners;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class SearchView extends ViewPart
  implements IFishtailView
{
  private static final String KEYWORD = "ca.ubc.spl.fishtail.view.search.data.keyword";
  private static final int NUM_KEYWORDS = 10;
  private static final String MEMENTO_ID = "ca.ubc.spl.fishtail.view.search.data";
  private static final String ID = "ca.ubc.spl.fishtail.views.SearchView";
  private TreeViewer viewer;
  private Text searchText;
  private Text previewPaneText;
  private BlacklistAdapter blacklistDomainAdapter;
  private Button goButton;

  public void init(IViewSite site, IMemento memento)
    throws PartInitException
  {
    super.init(site, memento);

    AllSearches.getInstance().getSearchListeners().addSearchViewListener(this);
    if (memento != null) {
      IMemento root = memento.getChild("ca.ubc.spl.fishtail.view.search.data");
      if (root != null) {
        IMemento[] words = root.getChildren("ca.ubc.spl.fishtail.view.search.data.keyword");
        List keywords = new ArrayList();
        for (int i = 0; (i < 10) && (i < words.length); i++) {
          String s = words[i].getID();

          if ((s != null) && (!s.equals(""))) {
            keywords.add(s);
          }
        }

        AllSearches.getInstance().setKeywords(keywords);
      }
    }
  }

  public void refreshView() {
    if (this.viewer != null)
      this.viewer.refresh();
  }

  public static IFishtailView getFromActivePerspective()
  {
    if (PlatformUI.isWorkbenchRunning()) {
      IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
        .getActivePage();
      if (activePage != null) {
        IViewPart view = activePage.findView("ca.ubc.spl.fishtail.views.SearchView");
        if ((view instanceof SearchView)) {
          return (IFishtailView)view;
        }
      }
    }
    return null;
  }

  public void saveState(IMemento memento)
  {
    IMemento root = memento.createChild("ca.ubc.spl.fishtail.view.search.data");
    List words = AllSearches.getInstance().getKeywords();
    for (int i = 0; (i < 10) && (i < words.size()); i++)
    {
      root.createChild("ca.ubc.spl.fishtail.view.search.data.keyword", (String)words.get(i));
    }
  }

  public void keywordsChanged() {
    if (this.searchText != null)
      this.searchText.setText(TokenUtil.toDelimited(AllSearches.getInstance().getKeywords(), " "));
  }

  private void createUIListeners()
  {
    this.viewer.getTree().addListener(13, new Listener()
    {
      public void handleEvent(Event event) {
        TreeItem item = (TreeItem)event.item;
        if ((item.getData() instanceof SearchHit)) {
          SearchHit hit = (SearchHit)item.getData();
          StringBuilder sb = new StringBuilder();
          sb.append(hit.getDesc());
          sb.append("\n");
          sb.append(hit.getUrl());
          SearchView.this.previewPaneText.setText(sb.toString());
        } else {
          SearchView.this.previewPaneText.setText("");
        }
      }
    });
    this.viewer.getTree().addListener(42, new Listener() {
      public void handleEvent(Event event) {
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
    this.searchText.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e) {
        if ((e.keyCode == 13) || (e.keyCode == 16777296))
        {
          String t = ((Text)e.widget).getText();
          AllSearches as = AllSearches.getInstance();
          as.setKeywords(TokenUtil.getTokens(t));
          as.executeAll(false, true, false);
        } else if (e.character == '\033')
        {
          ((Text)e.widget).setText("");
        }
      }
    });
    this.viewer.getTree().addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e) {
        if (((e.stateMask & 0x10000) != 0) && 
          ((SearchView.this.getSelectedElement() instanceof SearchStrategy))) {
          SearchStrategy ss = (SearchStrategy)SearchView.this.getSelectedElement();
          ss.broadenCategory(SearchView.this, null);
        }
      }
    });
  }

  private Object getSelectedElement() {
    if (this.viewer == null) {
      return null;
    }
    return ((IStructuredSelection)this.viewer.getSelection()).getFirstElement();
  }

  public void createPartControl(Composite parent)
  {
    parent.setForeground(new Color(null, 255, 255, 255));
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    parent.setLayout(layout);

    Composite top = new Composite(parent, 0);
    GridData gd = new GridData(768);

    gd.horizontalAlignment = 4;
    top.setLayoutData(gd);

    layout = new GridLayout();
    layout.numColumns = 2;
    top.setLayout(layout);
    this.searchText = ViewUiUtil.createUIWidgetSearchText(top, true);

    this.goButton = new Button(top, 131076);
    this.goButton.setToolTipText("Search");
    this.goButton.addSelectionListener(new SelectionListener()
    {
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        String t;
        try {
          t = SearchView.this.searchText.getText();
        }
        catch (RuntimeException localRuntimeException)
        {
          t = "";
        }
        AllSearches as = AllSearches.getInstance();
        as.setKeywords(TokenUtil.getTokens(t));
        as.executeAll(false, true, false);
      }
    });
    this.viewer = ViewUiUtil.createTreeViewer(parent);

    this.previewPaneText = new Text(parent, 2314);
    this.previewPaneText.setBackground(Display.getDefault().getSystemColor(1));
    gd = new GridData();
    gd.verticalAlignment = 1028;
    gd.horizontalAlignment = 4;
    gd.heightHint = 32;
    gd.grabExcessHorizontalSpace = true;
    this.previewPaneText.setLayoutData(gd);

    this.blacklistDomainAdapter = new BlacklistAdapter(this.viewer, this);

    createUIListeners();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();

    SearchToolTipHandler toolTipHandler = new SearchToolTipHandler(this);
    toolTipHandler.activateHoverHelp(this.viewer.getControl());

    this.viewer.getTree().setToolTipText("");

    keywordsChanged();
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        SearchView.this.fillContextMenu(manager);
      }
    });
    Menu menu = menuMgr.createContextMenu(this.viewer.getControl());
    this.viewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, this.viewer);
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    IToolBarManager manager = bars.getToolBarManager();

    this.blacklistDomainAdapter.addToolbarActions(manager);
  }

  private void fillContextMenu(IMenuManager manager)
  {
    this.blacklistDomainAdapter.addContextMenuActions(manager);
  }

  private void hookDoubleClickAction() {
    Tree tree = this.viewer.getTree();
    tree.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e) {
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        SearchView.this.openSelectedHit();
      }
    });
  }

  public void openSelectedHit() {
    Object selectedElement = ViewUiUtil.getSelectedElement(this.viewer);
    ViewUiUtil.openBrowser(selectedElement, this);
  }

  public void setFocus()
  {
    this.viewer.getControl().setFocus();
  }

  public IFishtailView.ViewType getType() {
    return IFishtailView.ViewType.View;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.SearchView
 * JD-Core Version:    0.6.0
 */