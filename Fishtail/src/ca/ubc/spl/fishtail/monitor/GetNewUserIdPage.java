package ca.ubc.spl.fishtail.monitor;

import java.net.URL;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.monitor.usage.StudyParameters;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

public class GetNewUserIdPage extends WizardPage
{
  private static final String SELECT_BELOW = "<Select Below>";
  private Text firstName;
  private Text lastName;
  private Text emailAddress;
  private Button contactAgreement;
  private Button getNewUid;
  private Button getExistingUid;
  private String first;
  private String last;
  private String email;
  private boolean contactEmail = false;
  private boolean anon;
  private boolean hasValidated = false;

  private String jobFunction = "<Select Below>";

  private String companySize = "<Select Below>";

  private String companyFunction = "<Select Below>";
  private UsageSubmissionWizard wizard;
  private boolean performUpload;
  private boolean extendedMonitor = false;

  public GetNewUserIdPage(UsageSubmissionWizard wizard, boolean performUpload) {
    super("Statistics Wizard");
    this.performUpload = performUpload;
    setTitle("Get Mylyn Feedback User ID");
    setDescription("In order to submit usage feedback you first need to get a User ID.\n");
    this.wizard = wizard;
    if (UiUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
      this.extendedMonitor = true;
      String customizedTitle = UiUsageMonitorPlugin.getDefault().getStudyParameters().getTitle();
      if (!customizedTitle.equals(""))
        setTitle(customizedTitle + ": Consent Form and User ID");
    }
  }

  public void createControl(Composite parent)
  {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 1;
    if (this.extendedMonitor) {
      createBrowserSection(container);

      createInstructionSection(container);
      createNamesSection(container);
      createJobDetailSection(container);
      if (UiUsageMonitorPlugin.getDefault().usingContactField())
        createContactSection(container);
      createUserIdButtons(container);
    } else {
      createAnonymousParticipationButtons(container);
    }
    setControl(container);
  }

  private void createBrowserSection(Composite parent)
  {
    if (this.extendedMonitor) {
      Label label = new Label(parent, 0);
      label.setFont(JFaceResources.getFontRegistry().getBold("org.eclipse.jface.defaultfont"));
      label.setText(UiUsageMonitorPlugin.getDefault().getCustomizedByMessage());

      Composite container = new Composite(parent, 0);
      GridLayout layout = new GridLayout();
      container.setLayout(layout);
      layout.numColumns = 1;
      Browser browser = new Browser(parent, 0);
      GridData gd = new GridData(768);
      gd.heightHint = 200;
      gd.widthHint = 600;
      browser.setLayoutData(gd);

      URL url = Platform.getBundle(UiUsageMonitorPlugin.getDefault().getCustomizingPlugin()).getEntry(
        UiUsageMonitorPlugin.getDefault().getStudyParameters().getFormsConsent());
      try {
        URL localURL = Platform.asLocalURL(url);
        browser.setUrl(localURL.toString());
      } catch (Exception localException) {
        browser.setText("Feedback description could not be located.");
      }
    } else {
      Label label = new Label(parent, 0);
      label.setText("bla bla");
    }
  }

  private void createNamesSection(Composite parent)
  {
    Composite names = new Composite(parent, 0);
    GridLayout layout = new GridLayout(6, true);
    layout.verticalSpacing = 9;
    layout.horizontalSpacing = 4;
    names.setLayout(layout);

    Label label = new Label(names, 0);
    label.setText("First Name:");

    this.firstName = new Text(names, 2052);
    GridData gd = new GridData(4, 4, true, true);
    gd.horizontalSpan = 2;
    this.firstName.setLayoutData(gd);
    this.firstName.setEditable(true);
    this.firstName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        GetNewUserIdPage.this.first = GetNewUserIdPage.this.firstName.getText();
        GetNewUserIdPage.this.updateEnablement();
        GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
      }
    });
    label = new Label(names, 0);
    label.setText("Last Name:");

    this.lastName = new Text(names, 2052);
    gd = new GridData(4, 4, true, true);
    gd.horizontalSpan = 2;
    this.lastName.setLayoutData(gd);
    this.lastName.setEditable(true);
    this.lastName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        GetNewUserIdPage.this.last = GetNewUserIdPage.this.lastName.getText();
        GetNewUserIdPage.this.updateEnablement();
        GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
      }
    });
    label = new Label(names, 0);
    label.setText("Email Address:");

    this.emailAddress = new Text(names, 2052);
    gd = new GridData(1808);
    gd.horizontalSpan = 5;
    this.emailAddress.setLayoutData(gd);
    this.emailAddress.setEditable(true);
    this.emailAddress.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        GetNewUserIdPage.this.email = GetNewUserIdPage.this.emailAddress.getText();
        GetNewUserIdPage.this.updateEnablement();
        GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
      } } );
  }

  private void createJobDetailSection(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 2;

    Label l = new Label(container, 0);
    l.setText("Job Function:");
    Combo jobFunctionCombo = new Combo(container, 4);
    jobFunctionCombo.setText(this.jobFunction);
    jobFunctionCombo.add("Application Developer");
    jobFunctionCombo.add("QA/Testing");
    jobFunctionCombo.add("Program Director");
    jobFunctionCombo.add("CIO/CTO");
    jobFunctionCombo.add("VP Development Systems Integrator");
    jobFunctionCombo.add("Application Architect");
    jobFunctionCombo.add("Project Manager");
    jobFunctionCombo.add("Student");
    jobFunctionCombo.add("Faculty");
    jobFunctionCombo.add("Business");
    jobFunctionCombo.add("Analyst");
    jobFunctionCombo.add("Database Administrator");
    jobFunctionCombo.add("Other");
    jobFunctionCombo.addSelectionListener(new SelectionAdapter(jobFunctionCombo)
    {
      public void widgetSelected(SelectionEvent e) {
        GetNewUserIdPage.this.jobFunction = this.val$jobFunctionCombo.getText();
        GetNewUserIdPage.this.updateEnablement();
      }
    });
    l = new Label(container, 0);
    l.setText("Company Size:");
    Combo companySizecombo = new Combo(container, 4);
    companySizecombo.setText(this.companySize);
    companySizecombo.add("Individual");
    companySizecombo.add("<50");
    companySizecombo.add("50-100");
    companySizecombo.add("100-500");
    companySizecombo.add("500-1000");
    companySizecombo.add("1000-2500");
    companySizecombo.add(">2500");
    companySizecombo.addSelectionListener(new SelectionAdapter(companySizecombo)
    {
      public void widgetSelected(SelectionEvent e) {
        GetNewUserIdPage.this.companySize = this.val$companySizecombo.getText();
        GetNewUserIdPage.this.updateEnablement();
      }
    });
    l = new Label(container, 0);
    l.setText("Company Business");
    Combo companyBuisnesscombo = new Combo(container, 4);
    companyBuisnesscombo.setText(this.companyFunction);
    companyBuisnesscombo.add("Financial service/insurance");
    companyBuisnesscombo.add("Energy");
    companyBuisnesscombo.add("Government");
    companyBuisnesscombo.add("Hardware Manufacturer");
    companyBuisnesscombo.add("Networking");
    companyBuisnesscombo.add("Pharmaceutical/Medical");
    companyBuisnesscombo.add("Automotive");
    companyBuisnesscombo.add("Software Manufacturer");
    companyBuisnesscombo.add("Communications");
    companyBuisnesscombo.add("Transportation");
    companyBuisnesscombo.add("Retail");
    companyBuisnesscombo.add("Utilities");
    companyBuisnesscombo.add("Other Manufacturing");
    companyBuisnesscombo.add("Academic/Education");
    companyBuisnesscombo.addSelectionListener(new SelectionAdapter(companyBuisnesscombo)
    {
      public void widgetSelected(SelectionEvent e) {
        GetNewUserIdPage.this.companyFunction = this.val$companyBuisnesscombo.getText();
        GetNewUserIdPage.this.updateEnablement();
      } } );
  }

  private void createInstructionSection(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    container.setLayout(layout);

    Label l = new Label(container, 0);

    l.setText("To create a user ID please fill in the following information. If you already have an ID please fill out the information again to retrieve it.");

    GridData gd = new GridData(32);
    l.setLayoutData(gd);
  }

  private void createContactSection(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    container.setLayout(layout);

    this.contactAgreement = new Button(container, 32);
    this.contactAgreement.setText("I would be willing to receive email about my participation in this study.");
    this.contactAgreement.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e) {
        GetNewUserIdPage.this.contactEmail = GetNewUserIdPage.this.contactAgreement.getSelection();
      } } );
  }

  private void createUserIdButtons(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    container.setLayout(layout);

    Label l = new Label(container, 0);
    l.setFont(JFaceResources.getFontRegistry().getBold("org.eclipse.jface.defaultfont"));
    l.setText("By clicking \"I consent\" you acknowledge that you have received this consent form, and are consenting to participate in the study.");
    GridData gd = new GridData(32);
    l.setLayoutData(gd);

    container = new Composite(parent, 0);
    layout = new GridLayout();
    layout.numColumns = 2;
    container.setLayout(layout);

    this.getNewUid = new Button(container, 8);
    gd = new GridData(32);
    this.getNewUid.setLayoutData(gd);
    this.getNewUid.setSelection(false);
    this.getNewUid.setText("I consent; get me a new user ID");
    this.getNewUid.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        if ((e.widget instanceof Button)) {
          if (GetNewUserIdPage.this.hasAllFields(false)) {
            if (GetNewUserIdPage.this.wizard.getNewUid(GetNewUserIdPage.this.first, GetNewUserIdPage.this.last, GetNewUserIdPage.this.email, GetNewUserIdPage.this.anon, GetNewUserIdPage.this.jobFunction, GetNewUserIdPage.this.companySize, GetNewUserIdPage.this.companyFunction, 
              GetNewUserIdPage.this.contactEmail) != -1) {
              if (GetNewUserIdPage.this.wizard.getUploadPage() != null)
                GetNewUserIdPage.this.wizard.getUploadPage().updateUid();
              GetNewUserIdPage.this.hasValidated = true;
              MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Mylyn User Study ID", 
                "Your Mylyn user study ID is: " + GetNewUserIdPage.this.wizard.getUid());
            }
          }
          else MessageDialog.openError(Display.getDefault().getActiveShell(), "Incomplete Form Input", 
              "Please complete all of the fields.");

          GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
        }
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
    });
    this.getExistingUid = new Button(container, 8);
    gd = new GridData(32);
    this.getExistingUid.setLayoutData(gd);
    this.getExistingUid.setSelection(false);
    this.getExistingUid.setText("I have already consented; retrieve my existing user ID");
    this.getExistingUid.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        if ((e.widget instanceof Button)) {
          if (GetNewUserIdPage.this.hasAllFields(true)) {
            if (GetNewUserIdPage.this.wizard.getExistingUid(GetNewUserIdPage.this.first, GetNewUserIdPage.this.last, GetNewUserIdPage.this.email, GetNewUserIdPage.this.anon) != -1) {
              if (GetNewUserIdPage.this.wizard.getUploadPage() != null)
                GetNewUserIdPage.this.wizard.getUploadPage().updateUid();
              GetNewUserIdPage.this.hasValidated = true;
              MessageDialog.openInformation(
                Display.getDefault().getActiveShell(), 
                "Mylyn Feedback User ID", 
                "Your Mylyn feedback ID is: " + 
                GetNewUserIdPage.this.wizard.getUid() + 
                "\n\nPlease record this number if you are using multiple copies of eclipse so that you do not have to register again.\n\nYou can also retrieve this ID by repeating the consent process at a later time.");
            }
          }
          else MessageDialog.openError(Display.getDefault().getActiveShell(), "Incomplete Form Input", 
              "Please complete all of the fields.");

          GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
        }
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
    });
    updateEnablement();
  }

  private void createAnonymousParticipationButtons(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    container.setLayout(layout);

    Label label = new Label(container, 0);
    label.setText("Your data will not be traceable back to you, but an ID helps us analyze the usage statistics.");
    label = new Label(container, 0);
    label.setText("Before switching workspaces please retrieve this ID from the Mylyn Preferences so that you can use it again.");

    container = new Composite(parent, 0);
    layout = new GridLayout();
    layout.numColumns = 2;
    container.setLayout(layout);

    this.getNewUid = new Button(container, 8);
    GridData gd = new GridData(32);
    this.getNewUid.setLayoutData(gd);
    this.getNewUid.setSelection(false);
    this.getNewUid.setText("Create or Retrieve ID");
    this.getNewUid.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        if ((e.widget instanceof Button)) {
          if (GetNewUserIdPage.this.wizard.getNewUid(null, null, null, true, null, null, null, false) != -1) {
            if (GetNewUserIdPage.this.wizard.getUploadPage() != null)
              GetNewUserIdPage.this.wizard.getUploadPage().updateUid();
            GetNewUserIdPage.this.hasValidated = true;
            MessageDialog.openInformation(
              Display.getDefault().getActiveShell(), 
              "Mylyn User Study ID", 
              "Your Mylyn user study id is: " + 
              GetNewUserIdPage.this.wizard.getUid() + 
              "\n Please record this number if you are using multiple copies of eclipse so that you do not have to register again.");
          }
          GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
        }
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
    });
    updateEnablement();
  }

  private void updateEnablement() {
    if (!this.extendedMonitor)
      return;
    boolean nameFilled = ((!this.firstName.getText().equals("")) && (!this.lastName.getText().equals("")) && 
      (!this.emailAddress.getText().equals(""))) || 
      (this.anon);

    boolean jobFilled = (!this.jobFunction.equals("<Select Below>")) && (!this.companyFunction.equals("<Select Below>")) && 
      (!this.companySize.equals("<Select Below>"));

    if ((nameFilled) && (jobFilled)) {
      this.getNewUid.setEnabled(true);
      this.getExistingUid.setEnabled(true);
    } else {
      this.getExistingUid.setEnabled(false);
      this.getNewUid.setEnabled(false);
    }
  }

  public boolean hasAllFields(boolean existing) {
    if (!this.extendedMonitor)
      return true;
    boolean nameFilled = (!this.firstName.getText().equals("")) && (!this.lastName.getText().equals("")) && 
      (!this.emailAddress.getText().equals(""));
    if (!existing) {
      boolean jobFilled = (!this.jobFunction.equals("<Select Below>")) && (!this.companyFunction.equals("<Select Below>")) && 
        (!this.companySize.equals("<Select Below>"));
      return (jobFilled) && (nameFilled);
    }
    return (nameFilled) || (this.anon);
  }

  public boolean isPageComplete()
  {
    return (hasAllFields(true)) && (this.hasValidated);
  }

  public IWizardPage getNextPage()
  {
    if ((isPageComplete()) && (this.performUpload)) {
      this.wizard.addPage(this.wizard.getUploadPage());
    }
    return super.getNextPage();
  }

  public boolean isAnonymous()
  {
    return this.anon;
  }

  public String getEmailAddress() {
    return this.email;
  }

  public String getFirstName() {
    return this.first;
  }

  public String getLastName() {
    return this.last;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.monitor.GetNewUserIdPage
 * JD-Core Version:    0.6.0
 */