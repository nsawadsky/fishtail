package ca.ubc.spl.fishtail.actions;

import ca.ubc.spl.fishtail.views.InplaceSearchDialog;
import ca.ubc.spl.fishtail.views.ViewUiUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class OpenInplaceSearchAction
  implements IWorkbenchWindowActionDelegate
{
  public void dispose()
  {
  }

  public void init(IWorkbenchWindow window)
  {
  }

  public void run(IAction action)
  {
    Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    InplaceSearchDialog inplaceDialog = new InplaceSearchDialog(parent, 0);

    inplaceDialog.setSize(400, 500);
    inplaceDialog.open();
    inplaceDialog.setFocus();
    ViewUiUtil.expandAll(inplaceDialog.getTreeViewer());
  }

  public void selectionChanged(IAction action, ISelection selection)
  {
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.actions.OpenInplaceSearchAction
 * JD-Core Version:    0.6.0
 */