package ca.ubc.spl.fishtail.monitor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.monitor.core.collection.ViewUsageCollector;
import org.eclipse.mylyn.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylyn.internal.monitor.usage.collectors.PerspectiveUsageCollector;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewUsageSummaryEditorWizard extends Wizard
  implements INewWizard
{
  private static final String TITLE = "New Usage Summary Report";
  private UsageSummaryEditorWizardPage usageSummaryPage;

  public NewUsageSummaryEditorWizard()
  {
    init();
    setWindowTitle("New Usage Summary Report");
  }

  private void init() {
    this.usageSummaryPage = new UsageSummaryEditorWizardPage();
  }

  public boolean performFinish()
  {
    if ((!this.usageSummaryPage.includePerspective()) && (!this.usageSummaryPage.includeViews())) {
      return false;
    }

    List collectors = new ArrayList();

    if (this.usageSummaryPage.includePerspective()) {
      collectors.add(new PerspectiveUsageCollector());
    }
    if (this.usageSummaryPage.includeViews()) {
      ViewUsageCollector mylynViewUsageCollector = new ViewUsageCollector();
      collectors.add(mylynViewUsageCollector);
    }

    MonitorFileRolloverJob job = new MonitorFileRolloverJob(collectors);
    job.setPriority(30);
    job.schedule();

    return true;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
  }

  public void addPages()
  {
    addPage(this.usageSummaryPage);
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.monitor.NewUsageSummaryEditorWizard
 * JD-Core Version:    0.6.0
 */