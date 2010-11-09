package ca.ubc.spl.fishtail.monitor;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.monitor.usage.StudyParameters;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class UsageUploadWizardPage extends WizardPage
{
  private Text serverAddrText;
  private Text usageFileText;
  private Text idText;
  private UsageSubmissionWizard wizard;

  public UsageUploadWizardPage(UsageSubmissionWizard wizard)
  {
    super("Usage Data Submission Wizard");

    setTitle("Usage Data Submission");
    if (UiUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
      String customizedTitle = UiUsageMonitorPlugin.getDefault().getStudyParameters().getTitle();
      if (!customizedTitle.equals("")) {
        setTitle(customizedTitle + ": Usage Data Upload");
      }
    }

    setDescription("The usage file listed below will be uploaded along with the archived files you selected (there may not have been any to select from).\nInformation about program elements that you worked with is obfuscated to ensure privacy.");

    this.wizard = wizard;
  }

  public void createControl(Composite parent)
  {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 1;

    Composite topContainer = new Composite(container, 0);
    GridLayout topContainerLayout = new GridLayout();
    topContainer.setLayout(topContainerLayout);
    topContainerLayout.numColumns = 2;
    topContainerLayout.verticalSpacing = 9;

    if (UiUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
      Label label = new Label(parent, 0);
      label.setFont(JFaceResources.getFontRegistry().getBold("org.eclipse.jface.defaultfont"));
      label.setText(UiUsageMonitorPlugin.getDefault().getCustomizedByMessage());
    }

    Label label = new Label(topContainer, 0);
    label.setText("Upload URL:");

    this.serverAddrText = new Text(topContainer, 2052);
    GridData gd = new GridData(768);
    this.serverAddrText.setLayoutData(gd);
    this.serverAddrText.setEditable(false);
    this.serverAddrText.setText(UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl());

    label = new Label(topContainer, 0);
    label.setText("Usage file location:");

    this.usageFileText = new Text(topContainer, 2052);
    gd = new GridData(768);
    this.usageFileText.setLayoutData(gd);
    this.usageFileText.setEditable(false);

    this.usageFileText.setText(this.wizard.getMonitorFileName());

    Composite bottomContainer = new Composite(container, 0);
    GridLayout bottomContainerLayout = new GridLayout();
    bottomContainer.setLayout(bottomContainerLayout);
    bottomContainerLayout.numColumns = 2;

    Label submissionLabel = new Label(bottomContainer, 0);
    submissionLabel.setText("Only events from org.eclipse.* packages will be submitted to Eclipse.org");

    setControl(container);
  }

  public IWizardPage getNextPage()
  {
    return null;
  }

  public void updateUid() {
    if ((this.idText != null) && (!this.idText.isDisposed()))
      this.idText.setText(this.wizard.getUid());
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.monitor.UsageUploadWizardPage
 * JD-Core Version:    0.6.0
 */