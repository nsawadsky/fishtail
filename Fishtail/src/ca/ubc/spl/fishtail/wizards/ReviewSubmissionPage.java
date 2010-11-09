package ca.ubc.spl.fishtail.wizards;

import ca.ubc.spl.fishtail.ActivityLogger;
import ca.ubc.spl.fishtail.ActivityMonitor;
import ca.ubc.spl.fishtail.FishtailPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ReviewSubmissionPage extends WizardPage
  implements IWizardPage
{
  private static final String VIEW = "View ";
  private static final String FULL_LOG = "Full Log";
  private static final String SHOWING = "Showing: ";
  private static final String LAST_20_EVENTS = "Last 20";
  private static final String REVIEW = "Review activity log";
  private static final String DESCRIPTION = "Review the activity log before submission";
  private Button disableUpload;
  private Label last20;
  private Text preview;
  private Button logButton;
  protected boolean showAll = false;

  public ReviewSubmissionPage() {
    super("Review activity log");
    setDescription("Review the activity log before submission");
    setTitle("Review activity log");
  }

  public void createControl(Composite parent) {
    Composite c = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    c.setLayout(layout);

    this.last20 = new Label(c, 0);
    this.last20.setLayoutData(new GridData(768));
    this.last20.setText("Showing: Last 20");

    this.logButton = new Button(c, 0);
    this.logButton.setText("View Full Log");
    this.logButton.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        ReviewSubmissionPage.this.logButton.setText("View " + (ReviewSubmissionPage.this.showAll ? "Full Log" : "Last 20"));
        ReviewSubmissionPage.this.showAll = (!ReviewSubmissionPage.this.showAll);
        ReviewSubmissionPage.this.last20.setText("Showing: " + (ReviewSubmissionPage.this.showAll ? "Full Log" : "Last 20"));
        try {
          String logContents = ReviewSubmissionPage.this.getLogContents();
          if (!ReviewSubmissionPage.this.showAll)
            logContents = ReviewSubmissionPage.this.getLastItems(logContents);
          ReviewSubmissionPage.this.preview.setText(logContents);
        }
        catch (Exception localException)
        {
        }
      }
    });
    createTextPreview(c);

    this.disableUpload = new Button(c, 32);
    this.disableUpload.setText("Do not upload log");
    this.disableUpload.setSelection(false);

    setControl(c);
  }

  private void createTextPreview(Composite composite) {
    try {
      String data = getLogContents();

      data = getLastItems(data);

      createTextPreview(composite, data);
    }
    catch (IOException localIOException) {
      createErrorPreview(composite, "Error reading file.");
    }
  }

  private String getLogContents() throws FileNotFoundException, IOException {
    File f = FishtailPlugin.getDefault().getMonitor().getLogger().getOutputFile();
    StringBuffer content = new StringBuffer();
    BufferedReader in = new BufferedReader(new FileReader(f));
    String line;
    while ((line = in.readLine()) != null)
    {
      content.append(line);
      content.append("\n");
    }
    in.close();
    String data = content.toString();
    return data;
  }

  private String getLastItems(String data) {
    String tag = "<activityevent>";
    int loc = data.length();
    for (int i = 0; i < 20; i++) {
      loc = data.lastIndexOf(tag, loc) - 1;
    }

    if (loc >= 0) {
      return data.substring(loc + 1);
    }
    return data;
  }

  private void createTextPreview(Composite composite, String contents) {
    this.preview = new Text(composite, 2826);

    GridData gd = new GridData(1808);
    gd.heightHint = composite.getBounds().y;
    gd.widthHint = composite.getBounds().x;
    this.preview.setLayoutData(gd);
    this.preview.setText(contents);
  }

  private void createErrorPreview(Composite composite, String message) {
    Label label = new Label(composite, 0);
    label.setLayoutData(new GridData(1808));
    label.setText(message);
  }

  public boolean isUploadDisabled() {
    return this.disableUpload.getSelection();
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.wizards.ReviewSubmissionPage
 * JD-Core Version:    0.6.0
 */