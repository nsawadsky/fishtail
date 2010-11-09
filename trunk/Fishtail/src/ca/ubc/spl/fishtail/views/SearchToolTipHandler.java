package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.ActivityMonitor;
import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class SearchToolTipHandler
{
  private Shell tipShell;
  private Widget tipWidget;
  private final IFishtailView view;

  public SearchToolTipHandler(IFishtailView view)
  {
    this.view = view;
  }

  public void activateHoverHelp(final Control control)
  {
    PlatformUI.getWorkbench().addWindowListener(new IWindowListener()
    {
      public void windowActivated(IWorkbenchWindow window) {
      }

      public void windowClosed(IWorkbenchWindow window) {
      }

      public void windowDeactivated(IWorkbenchWindow window) {
        SearchToolTipHandler.this.hideTooltip();
      }

      public void windowOpened(IWorkbenchWindow window)
      {
      }
    });
    control.addMouseListener(new MouseAdapter()
    {
      public void mouseDown(MouseEvent e)
      {
        SearchToolTipHandler.this.hideTooltip();
      }
    });
    control.addMouseTrackListener(new MouseTrackAdapter()
    {
      public void mouseExit(MouseEvent e)
      {
        if ((SearchToolTipHandler.this.tipShell != null) && (!SearchToolTipHandler.this.tipShell.isDisposed()) && (SearchToolTipHandler.this.tipShell.getDisplay() != null) && 
          (!SearchToolTipHandler.this.tipShell.getDisplay().isDisposed()) && (SearchToolTipHandler.this.tipShell.isVisible())) {
          SearchToolTipHandler.this.tipShell.setVisible(false);
        }
        SearchToolTipHandler.this.tipWidget = null;
      }

      public void mouseHover(MouseEvent event)
      {
        Point widgetPosition = new Point(event.x, event.y);
        Widget widget = event.widget;
        if ((widget instanceof ToolBar)) {
          ToolBar w = (ToolBar)widget;
          widget = w.getItem(widgetPosition);
        }
        if ((widget instanceof Table)) {
          Table w = (Table)widget;
          widget = w.getItem(widgetPosition);
        }
        if ((widget instanceof Tree)) {
          Tree w = (Tree)widget;
          widget = w.getItem(widgetPosition);
        }

        if (widget == null) {
          SearchToolTipHandler.this.hideTooltip();
          SearchToolTipHandler.this.tipWidget = null;
          return;
        }

        if (widget == SearchToolTipHandler.this.tipWidget)
        {
          return;
        }

        SearchToolTipHandler.this.tipWidget = widget;

        IFishtailView searchView = SearchView.getFromActivePerspective();

        SearchToolTipHandler.this.showTooltip(control.toDisplay(widgetPosition), searchView);
      }
    });
  }

  private void addIconAndLabel(Composite parent, Image image, String text) {
    if (text == null) {
      return;
    }

    Label textLabel = new Label(parent, 0);
    textLabel.setForeground(parent.getDisplay().getSystemColor(28));
    textLabel.setBackground(parent.getDisplay().getSystemColor(29));
    textLabel.setLayoutData(new GridData(772));

    textLabel.setText(removeTrailingNewline(text));
  }

  private Image getImage(Object element)
  {
    return new SearchLabelProvider().getImage(element);
  }

  private Object getSearchViewElement(Object hoverObject) {
    if ((hoverObject instanceof Widget)) {
      Object data = ((Widget)hoverObject).getData();
      if (data != null)
      {
        if ((data instanceof SearchHit)) {
          return data;
        }
      }
    }
    return null;
  }

  protected Widget getTipWidget(Event event) {
    Point widgetPosition = new Point(event.x, event.y);
    Widget widget = event.widget;
    if ((widget instanceof ToolBar)) {
      ToolBar w = (ToolBar)widget;
      return w.getItem(widgetPosition);
    }
    if ((widget instanceof Table)) {
      Table w = (Table)widget;
      return w.getItem(widgetPosition);
    }
    if ((widget instanceof Tree)) {
      Tree w = (Tree)widget;
      return w.getItem(widgetPosition);
    }

    return null;
  }

  private String getTitleText(Object element)
  {
    if ((element instanceof SearchHit)) {
      SearchHit hit = (SearchHit)element;
      StringBuilder sb = new StringBuilder();

      sb.append(hit.getName());
      sb.append("\n");

      sb.append(hit.getDesc());
      sb.append("\n");

      sb.append(hit.getUrl());

      return sb.toString();
    }
    return null;
  }

  private void hideTooltip() {
    if ((this.tipShell != null) && (!this.tipShell.isDisposed()) && (this.tipShell.isVisible()))
      this.tipShell.setVisible(false);
  }

  private String removeTrailingNewline(String text)
  {
    if (text.endsWith("\n")) {
      return text.substring(0, text.length() - 1);
    }
    return text;
  }

  private void setHoverLocation(Shell shell, Point position)
  {
    Rectangle displayBounds = shell.getMonitor().getClientArea();
    Rectangle shellBounds = shell.getBounds();

    Monitor[] array = PlatformUI.getWorkbench().getDisplay().getMonitors();
    for (Monitor m : array) {
      Rectangle monitorBounds = m.getBounds();
      if ((position.x < monitorBounds.x) || 
        (position.x >= monitorBounds.x + monitorBounds.width) || 
        (position.y < monitorBounds.y) || 
        (position.y >= monitorBounds.y + monitorBounds.height)) continue;
      displayBounds = m.getClientArea();
    }

    if (position.x + shellBounds.width > displayBounds.x + displayBounds.width)
      shellBounds.x = (displayBounds.x + displayBounds.width - shellBounds.width);
    else {
      shellBounds.x = position.x;
    }
    if (position.y + 10 + shellBounds.height > displayBounds.y + displayBounds.height)
      shellBounds.y = (displayBounds.y + displayBounds.height - shellBounds.height);
    else {
      shellBounds.y = (position.y + 10);
    }
    shell.setBounds(shellBounds);
  }

  private void showTooltip(Point location, IFishtailView searchView) {
    hideTooltip();

    Object element = getSearchViewElement(this.tipWidget);
    if (element == null) {
      return;
    }

    Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
    if (parent == null) {
      return;
    }

    if ((this.tipShell != null) && (!this.tipShell.isDisposed()) && (this.tipShell.getShell() != null)) {
      this.tipShell.close();
    }

    this.tipShell = new Shell(parent.getDisplay(), 540676);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginWidth = 5;
    gridLayout.marginHeight = 2;
    this.tipShell.setLayout(gridLayout);
    this.tipShell.setBackground(this.tipShell.getDisplay().getSystemColor(29));

    addIconAndLabel(this.tipShell, getImage(element), getTitleText(element));

    this.tipShell.pack();
    setHoverLocation(this.tipShell, location);
    this.tipShell.setVisible(true);
    FishtailPlugin.getDefault().getMonitor().tooltip((SearchHit)element, this.view);
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.SearchToolTipHandler
 * JD-Core Version:    0.6.0
 */