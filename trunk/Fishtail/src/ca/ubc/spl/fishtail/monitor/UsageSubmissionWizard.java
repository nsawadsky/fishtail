package ca.ubc.spl.fishtail.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.monitor.core.util.ZipFileUtil;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylyn.internal.monitor.usage.StudyParameters;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.monitor.usage.AbstractStudyBackgroundPage;
import org.eclipse.mylyn.monitor.usage.AbstractStudyQuestionnairePage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class UsageSubmissionWizard extends Wizard
  implements INewWizard
{
  public static final String LOG = "log";
  public static final String STATS = "usage";
  public static final String QUESTIONAIRE = "questionaire";
  public static final String BACKGROUND = "background";
  private static final String ORG_ECLIPSE_PREFIX = "org.eclipse.";
  public static final int HTTP_SERVLET_RESPONSE_SC_OK = 200;
  public static final int SIZE_OF_INT = 8;
  private boolean failed = false;

  private boolean displayBackgroundPage = false;

  private boolean displayFileSelectionPage = false;
  private int uid;
  private final File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();

  private static int processedFileCount = 1;
  private UsageUploadWizardPage uploadPage;
  private UsageFileSelectionWizardPage fileSelectionPage;
  private AbstractStudyQuestionnairePage questionnairePage;
  private AbstractStudyBackgroundPage backgroundPage;
  private boolean performUpload = true;
  private List<String> backupFilesToUpload;
  private File questionnaireFile = null;

  private File backgroundFile = null;
  private int status;
  private String resp;

  public UsageSubmissionWizard()
  {
    setTitles();
    init(true);
  }

  public UsageSubmissionWizard(boolean performUpload)
  {
    setTitles();
    init(performUpload);
  }

  private void setTitles() {
    super.setDefaultPageImageDescriptor(UiUsageMonitorPlugin.imageDescriptorFromPlugin(
      "org.eclipse.mylyn.monitor.usage", "icons/wizban/banner-user.gif"));
    super.setWindowTitle("Mylyn Feedback");
  }

  private void init(boolean performUpload) {
    this.performUpload = performUpload;
    setNeedsProgressMonitor(true);
    this.uid = UiUsageMonitorPlugin.getDefault().getPreferenceStore()
      .getInt("org.eclipse.mylyn.user.id");
    if ((this.uid == 0) || (this.uid == -1)) {
      this.uid = getNewUid();
      UiUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(
        "org.eclipse.mylyn.user.id", this.uid);
    }
    this.uploadPage = new UsageUploadWizardPage(this);
    this.fileSelectionPage = new UsageFileSelectionWizardPage("TODO, change this string");
    if (UiUsageMonitorPlugin.getDefault().isBackgroundEnabled()) {
      AbstractStudyBackgroundPage page = UiUsageMonitorPlugin.getDefault()
        .getStudyParameters().getBackgroundPage();
      this.backgroundPage = page;
    }
    if ((UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled()) && (performUpload)) {
      AbstractStudyQuestionnairePage page = UiUsageMonitorPlugin.getDefault()
        .getStudyParameters().getQuestionnairePage();
      this.questionnairePage = page;
    }
    super.setForcePreviousAndNextButtons(true);
  }

  public boolean performFinish()
  {
    if (!this.performUpload)
      return true;
    if ((UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled()) && (this.performUpload) && 
      (this.questionnairePage != null)) {
      this.questionnaireFile = this.questionnairePage.createFeedbackFile();
    }
    if ((UiUsageMonitorPlugin.getDefault().isBackgroundEnabled()) && (this.performUpload) && 
      (this.displayBackgroundPage) && (this.backgroundPage != null)) {
      this.backgroundFile = this.backgroundPage.createFeedbackFile();
    }

    if (this.displayFileSelectionPage) {
      this.backupFilesToUpload = this.fileSelectionPage.getZipFilesSelected();
    }

    Job j = new Job("Upload User Statistics")
    {
      protected IStatus run(IProgressMonitor monitor)
      {
        try {
          monitor.beginTask("Uploading user statistics", 3);
          UsageSubmissionWizard.this.performUpload(monitor);
          monitor.done();

          return Status.OK_STATUS;
        } catch (Exception e) {
          StatusHandler.log(e, "Error uploading statistics");
        }return new Status(4, "org.eclipse.mylyn.monitor.usage", 4, 
          "Error uploading statistics", e);
      }
    };
    j.setPriority(50);
    j.schedule();
    return true;
  }

  public void performUpload(IProgressMonitor monitor) {
    if ((UiUsageMonitorPlugin.getDefault().isBackgroundEnabled()) && (this.performUpload) && 
      (this.backgroundFile != null)) {
      upload(this.backgroundFile, "background", monitor);

      if (this.failed) {
        this.failed = false;
      }

      if (this.backgroundFile.exists()) {
        this.backgroundFile.delete();
      }
    }

    if ((UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled()) && (this.performUpload) && 
      (this.questionnaireFile != null)) {
      upload(this.questionnaireFile, "questionaire", monitor);

      if (this.failed) {
        this.failed = false;
      }

      if (this.questionnaireFile.exists()) {
        this.questionnaireFile.delete();
      }
    }
    File zipFile = zipFilesForUpload();
    if (zipFile == null) {
      return;
    }
    upload(zipFile, "usage", monitor);

    if (zipFile.exists()) {
      zipFile.delete();
    }

    if (!this.failed) {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
            "Successful Upload", 
            "Your usage statistics have been successfully uploaded.\n Thank you for participating.");
        }
      });
    }
    UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
    UiUsageMonitorPlugin.setPerformingUpload(false);
  }

  public boolean performCancel()
  {
    UiUsageMonitorPlugin.getDefault().userCancelSubmitFeedback(new Date(), true);
    return true;
  }

  public boolean canFinish()
  {
    if (!this.performUpload) {
      return true;
    }
    return (getContainer().getCurrentPage() == this.uploadPage) || (!this.performUpload);
  }

  public UsageUploadWizardPage getUploadPage()
  {
    return this.uploadPage;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
  }

  public void addPages()
  {
    if ((UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled()) && (this.performUpload) && 
      (this.questionnairePage != null)) {
      addPage(this.questionnairePage);
    }
    if (this.performUpload) {
      if (UsageFileSelectionWizardPage.unsubmittedLogsExist()) {
        addPage(this.fileSelectionPage);
        this.displayFileSelectionPage = true;
      }
      addPage(this.uploadPage);
    }
  }

  public void addBackgroundPage() {
    if ((UiUsageMonitorPlugin.getDefault().isBackgroundEnabled()) && (this.backgroundPage != null)) {
      addPage(this.backgroundPage);
      this.displayBackgroundPage = true;
    }
  }

  private void upload(File f, String type, IProgressMonitor monitor)
  {
    if (this.failed) {
      return;
    }
    int status = 0;
    try
    {
      String servletUrl = UiUsageMonitorPlugin.getDefault().getStudyParameters()
        .getServletUrl();
      PostMethod filePost = new PostMethod(servletUrl);

      Part[] parts = { new FilePart("temp.txt", f) };

      filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

      HttpClient client = new HttpClient();

      status = client.executeMethod(filePost);
      filePost.releaseConnection();
    }
    catch (Exception e)
    {
      this.failed = true;
      if (((e instanceof NoRouteToHostException)) || ((e instanceof UnknownHostException))) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openError(null, "Error Uploading", 
              "There was an error uploading the file: \nNo network connection.  Please try again later");
          } } );
      }
      else {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(e) {
          public void run() {
            MessageDialog.openError(null, "Error Uploading", 
              "There was an error uploading the file: \n" + 
              this.val$e.getClass().getCanonicalName());
          }
        });
        StatusHandler.log(e, "failed to upload");
      }
    }

    monitor.worked(1);

    String filedesc = f.getName();

    int httpResponseStatus = status;

    if (status == 401)
    {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(filedesc) {
        public void run() {
          MessageDialog.openError(null, "Error Uploading", 
            "There was an error uploading the " + this.val$filedesc + ": \n" + 
            "Your uid was incorrect: " + UsageSubmissionWizard.this.uid + "\n");
        } } );
    } else if (status == 407) {
      this.failed = true;
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
      {
        public void run() {
          MessageDialog.openError(
            null, 
            "Error Uploading", 
            "Could not upload because proxy server authentication failed.  Please check your proxy server settings.");
        } } );
    } else if (status != 200) {
      this.failed = true;

      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(filedesc, httpResponseStatus) {
        public void run() {
          MessageDialog.openError(null, "Error Uploading", 
            "There was an error uploading the " + this.val$filedesc + ": \n" + 
            "HTTP Response Code " + this.val$httpResponseStatus + "\n" + 
            "Please try again later");
        }
      });
    }
  }

  public String getMonitorFileName()
  {
    return this.monitorFile.getAbsolutePath();
  }

  public int getExistingUid(String firstName, String lastName, String emailAddress, boolean anonymous)
  {
    if (this.failed) {
      return -1;
    }

    try
    {
      String url = UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl() + 
        UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl();
      GetMethod getUidMethod = new GetMethod(url);

      NameValuePair first = new NameValuePair("firstName", firstName);
      NameValuePair last = new NameValuePair("lastName", lastName);
      NameValuePair email = new NameValuePair("email", emailAddress);
      NameValuePair job = new NameValuePair("jobFunction", "");
      NameValuePair size = new NameValuePair("companySize", "");
      NameValuePair buisness = new NameValuePair("companyBuisness", "");
      NameValuePair contact = new NameValuePair("contact", "");
      NameValuePair anon = null;
      if (anonymous)
        anon = new NameValuePair("anonymous", "true");
      else {
        anon = new NameValuePair("anonymous", "false");
      }

      if (UiUsageMonitorPlugin.getDefault().usingContactField())
        getUidMethod.setQueryString(new NameValuePair[] { first, last, email, job, size, 
          buisness, anon, contact });
      else {
        getUidMethod.setQueryString(new NameValuePair[] { first, last, email, job, size, 
          buisness, anon });
      }

      HttpClient client = new HttpClient();
      UiUsageMonitorPlugin.getDefault().configureProxy(client, url);

      ProgressMonitorDialog pmd = new ProgressMonitorDialog(
        Display.getCurrent().getActiveShell());
      pmd.run(false, false, new IRunnableWithProgress(client, getUidMethod)
      {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          monitor.beginTask("Get User Id", 1);
          try
          {
            UsageSubmissionWizard.this.status = this.val$client.executeMethod(this.val$getUidMethod);

            UsageSubmissionWizard.this.resp = UsageSubmissionWizard.this.getData(this.val$getUidMethod.getResponseBodyAsStream());

            this.val$getUidMethod.releaseConnection();
          }
          catch (Exception e)
          {
            UsageSubmissionWizard.this.failed = true;
            if (((e instanceof NoRouteToHostException)) || 
              ((e instanceof UnknownHostException))) {
              PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
              {
                public void run() {
                  MessageDialog.openError(
                    null, 
                    "Error Uploading", 
                    "There was an error getting a new user id: \nNo network connection.  Please try again later");
                } } );
            }
            else {
              PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(e) {
                public void run() {
                  MessageDialog.openError(null, "Error Uploading", 
                    "There was an error getting a new user id: \n" + 
                    this.val$e.getClass().getCanonicalName() + 
                    this.val$e.getMessage());
                }
              });
              StatusHandler.log(e, "error uploading");
            }
          }
          monitor.worked(1);
          monitor.done();
        }
      });
      if (this.status != 200)
      {
        this.failed = true;

        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openError(null, "Error Getting User ID", 
              "There was an error getting a user id: \nHTTP Response Code " + 
              UsageSubmissionWizard.this.status + "\n" + "Please try again later");
          } } );
      } else {
        this.resp = this.resp.substring(this.resp.indexOf(":") + 1).trim();
        this.uid = Integer.parseInt(this.resp);
        UiUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(
          "org.eclipse.mylyn.user.id", this.uid);
        return this.uid;
      }

    }
    catch (Exception e)
    {
      this.failed = true;
      if (((e instanceof NoRouteToHostException)) || ((e instanceof UnknownHostException))) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openError(null, "Error Uploading", 
              "There was an error getting a new user id: \nNo network connection.  Please try again later");
          } } );
      }
      else {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(e) {
          public void run() {
            MessageDialog.openError(null, "Error Uploading", 
              "There was an error getting a new user id: \n" + 
              this.val$e.getClass().getCanonicalName());
          }
        });
        StatusHandler.log(e, "error uploading");
      }
    }
    return -1;
  }

  public int getNewUid() {
    PostMethod filePost = new PostMethod("http://mylyn.eclipse.org/monitor/upload/GetUserIDServlet");

    filePost.addParameter(new NameValuePair("MylarUserID", ""));
    HttpClient client = new HttpClient();
    int status = 0;
    try
    {
      status = client.executeMethod(filePost);

      if (status == 200) {
        InputStream inputStream = filePost.getResponseBodyAsStream();
        byte[] buffer = new byte[8];
        int numBytesRead = inputStream.read(buffer);
        int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
        filePost.releaseConnection();

        return uid;
      }
      return -1;
    }
    catch (Exception localException)
    {
    }

    return -1;
  }

  public int getNewUid(String firstName, String lastName, String emailAddress, boolean anonymous, String jobFunction, String companySize, String companyFunction, boolean contactEmail)
  {
    if (this.failed)
      return -1;
    try {
      addBackgroundPage();

      PostMethod filePost = new PostMethod("http://mylyn.eclipse.org/monitor/upload/GetUserIDServlet");

      filePost.addParameter(new NameValuePair("MylarUserID", ""));
      HttpClient client = new HttpClient();
      int status = 0;
      try
      {
        status = client.executeMethod(filePost);

        if (status == 202) {
          InputStream inputStream = filePost.getResponseBodyAsStream();
          byte[] buffer = new byte[8];
          int numBytesRead = inputStream.read(buffer);
          int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
          filePost.releaseConnection();

          return uid;
        }
        return -1;
      }
      catch (Exception localException1)
      {
        if (status != 200)
        {
          this.failed = true;

          MessageDialog.openError(null, "Error Getting User ID", 
            "There was an error getting a user id: \nHTTP Response Code " + status + 
            "\n" + "Please try again later");
        } else {
          this.resp = this.resp.substring(this.resp.indexOf(":") + 1).trim();
          this.uid = Integer.parseInt(this.resp);
          UiUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(
            "org.eclipse.mylyn.user.id", this.uid);
          return this.uid;
        }
      }
    }
    catch (Exception e)
    {
      this.failed = true;
      if (((e instanceof NoRouteToHostException)) || ((e instanceof UnknownHostException))) {
        MessageDialog.openError(null, "Error Uploading", 
          "There was an error getting a new user id: \nNo network connection.  Please try again later");
      }
      else {
        MessageDialog.openError(null, "Error Uploading", 
          "There was an error getting a new user id: \n" + 
          e.getClass().getCanonicalName());
        StatusHandler.log(e, "error uploading");
      }
    }
    return -1;
  }

  private String getData(InputStream i) {
    String s = "";
    String data = "";
    BufferedReader br = new BufferedReader(new InputStreamReader(i));
    try {
      while ((s = br.readLine()) != null)
        data = data + s;
    } catch (IOException e) {
      StatusHandler.log(e, "error uploading");
    }
    return data;
  }

  public int getUid() {
    return this.uid;
  }

  public boolean failed() {
    return this.failed;
  }

  private File processMonitorFile(File monitorFile) {
    File processedFile = new File("processed-monitor-log" + 
      processedFileCount++ + ".xml");
    InteractionEventLogger logger = new InteractionEventLogger(processedFile);
    logger.startMonitoring();
    List eventList = logger.getHistoryFromFile(monitorFile);

    if (eventList.size() > 0) {
      for (InteractionEvent event : eventList) {
        if (event.getOriginId().startsWith("org.eclipse.")) {
          logger.interactionObserved(event);
        }
      }
    }

    return processedFile;
  }

  private void addToSubmittedLogFile(String fileName) {
    File submissionLogFile = new File(MonitorFileRolloverJob.getZippedMonitorFileDirPath(), 
      "submittedUsageLogs.txt");
    try {
      FileWriter fileWriter = new FileWriter(submissionLogFile, true);
      fileWriter.append(fileName + "\n");
      fileWriter.flush();
      fileWriter.close();
    }
    catch (FileNotFoundException e) {
      StatusHandler.log(e, "error unzipping backup monitor log files");
    } catch (IOException e) {
      StatusHandler.log(e, "error unzipping backup monitor log files");
    }
  }

  private File zipFilesForUpload()
  {
    UiUsageMonitorPlugin.setPerformingUpload(true);
    UiUsageMonitorPlugin.getDefault().getInteractionLogger().stopMonitoring();

    List files = new ArrayList();
    File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();
    File fileToUpload = processMonitorFile(monitorFile);
    files.add(fileToUpload);

    if ((this.displayFileSelectionPage) && (this.backupFilesToUpload.size() > 0)) {
      for (String currFilePath : this.backupFilesToUpload) {
        File file = new File(MonitorFileRolloverJob.getZippedMonitorFileDirPath(), 
          currFilePath);
        if (!file.exists())
          continue;
        try {
          List unzippedFiles = ZipFileUtil.unzipFiles(file, 
            System.getProperty("java.io.tmpdir"));

          if (unzippedFiles.size() > 0)
            for (File f : unzippedFiles) {
              files.add(processMonitorFile(f));
              addToSubmittedLogFile(currFilePath);
            }
        }
        catch (FileNotFoundException e) {
          StatusHandler.log(e, "error unzipping backup monitor log files");
        } catch (IOException e) {
          StatusHandler.log(e, "error unzipping backup monitor log files");
        }
      }

    }

    UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
    try {
      File zipFile = File.createTempFile(this.uid + ".", ".zip");
      ZipFileUtil.createZipFile(zipFile, files);
      return zipFile;
    } catch (Exception e) {
      StatusHandler.log(e, "error uploading");
    }return null;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.monitor.UsageSubmissionWizard
 * JD-Core Version:    0.6.0
 */