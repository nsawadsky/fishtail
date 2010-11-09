package ca.ubc.spl.fishtail.monitor;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class UsageSummaryEditorWizardPage extends WizardPage
  implements IWizardPage
{
  private static final String TITLE = "Usage Summary and Submission";
  private static final String DESCRIPTION = "Summarizes usage and provides mechanism for uploading to eclipse.org \nserver for usage analysis. May take a lot of memory for large histories.";
  private Button perspectiveCheckbox = null;

  private Button viewCheckbox = null;

  public UsageSummaryEditorWizardPage() {
    super("Usage Summary and Submission");
    setTitle("Usage Summary and Submission");
    setDescription("Summarizes usage and provides mechanism for uploading to eclipse.org \nserver for usage analysis. May take a lot of memory for large histories.");
    setImageDescriptor(
      UiUsageMonitorPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.monitor.usage", 
      "icons/wizban/banner-usage.gif"));
  }

  public void createControl(Composite parent) {
    Composite container = new Composite(parent, 4);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 1;

    Label label = new Label(container, 16384);
    label.setText("This will run in the background because it may take a long time for large histories.\nThe editor will open when the summary has been generated.");

    createCheckboxes(container);

    setControl(container);
  }

  private void createCheckboxes(Composite parent)
  {
    Group checkboxGroup = new Group(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;

    checkboxGroup.setLayout(layout);
    checkboxGroup.setLayoutData(new GridData(768));
    checkboxGroup.setText("Show usage summaries for:");
    checkboxGroup.setFont(parent.getFont());

    this.perspectiveCheckbox = new Button(checkboxGroup, 540704);
    this.perspectiveCheckbox.setText("Use of perspectives");
    this.perspectiveCheckbox.setSelection(true);
    this.perspectiveCheckbox.addSelectionListener(new CheckboxSelectionListener(null));

    this.viewCheckbox = new Button(checkboxGroup, 540704);
    this.viewCheckbox.setText("Use of views");
    this.viewCheckbox.setSelection(true);
    this.viewCheckbox.addSelectionListener(new CheckboxSelectionListener(null));
  }

  public boolean includePerspective() {
    return this.perspectiveCheckbox.getSelection();
  }

  public boolean includeViews() {
    return this.viewCheckbox.getSelection();
  }
  private class CheckboxSelectionListener extends SelectionAdapter {
    private CheckboxSelectionListener() {
    }

    public void widgetSelected(SelectionEvent e) {
      if ((!UsageSummaryEditorWizardPage.this.perspectiveCheckbox.getSelection()) && (!UsageSummaryEditorWizardPage.this.viewCheckbox.getSelection()))
        UsageSummaryEditorWizardPage.this.setPageComplete(false);
      else
        UsageSummaryEditorWizardPage.this.setPageComplete(true);
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.monitor.UsageSummaryEditorWizardPage
 * JD-Core Version:    0.6.0
 */