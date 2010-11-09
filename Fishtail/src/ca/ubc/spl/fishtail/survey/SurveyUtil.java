package ca.ubc.spl.fishtail.survey;

import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.FishtailStatus;
import ca.ubc.spl.fishtail.wizards.SurveyWizard;
import ca.ubc.spl.fishtail.wizards.SurveyWizardPage;
import ca.ubc.spl.fishtail.wizards.SurveyWizardPage.Number;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

public abstract class SurveyUtil
{
  public static final String WEBSERVER = "http://stavanger.cs.ubc.ca:8080/fishtail";
  public static final String DEFAULT_UPLOAD_SERVLET_ID = "/GetUserIDServlet";
  public static final String DEFAULT_UPLOAD_SERVLET = "/MylarUsageUploadServlet";

  public static int getNewUid(String firstName, String lastName, String emailAddress, boolean anonymous)
    throws HttpException, IOException
  {
    PostMethod filePost = new PostMethod("http://stavanger.cs.ubc.ca:8080/fishtail/GetUserIDServlet");
    filePost.addParameter(new NameValuePair("MylarUserID", ""));

    filePost.addParameter(new NameValuePair("firstName", firstName));
    filePost.addParameter(new NameValuePair("lastName", lastName));
    filePost.addParameter(new NameValuePair("email", emailAddress));

    HttpClient client = new HttpClient();
    int status = 0;

    status = client.executeMethod(filePost);
    String resp = filePost.getResponseBodyAsString();

    if (status == 202) {
      InputStream inputStream = filePost.getResponseBodyAsStream();
      byte[] buffer = new byte[8];
      int numBytesRead = inputStream.read(buffer);
      int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
      filePost.releaseConnection();

      return uid;
    }if (status == 200) {
      resp = resp.substring(resp.indexOf(":") + 1).trim();
      int uid = Integer.parseInt(resp);
      FishtailPlugin.getDefault().getPreferenceStore().setValue(
        "org.eclipse.mylyn.user.id", uid);
      return uid;
    }

    return -1;
  }

  public static void upload(File f, int uid, Map<String, String> survey)
  {
    int status = 0;
    try
    {
      String servletUrl = "http://stavanger.cs.ubc.ca:8080/fishtail/MylarUsageUploadServlet";
      PostMethod filePost = new PostMethod(servletUrl);

      int i = 1;
      Part[] parts;
      if (f != null) {
        parts = new Part[survey.size() + 2];
        parts[1] = new FilePart("log", f);
        i++;
      } else {
        parts = new Part[survey.size() + 1];
      }
      parts[0] = new StringPart("uid", Integer.toString(uid));
      for (Map.Entry e : survey.entrySet())
      {
        parts[i] = new StringPart((String)e.getKey(), (String)e.getValue());
        i++;
      }
      MultipartRequestEntity mp = new MultipartRequestEntity(parts, filePost.getParams());

      filePost.setRequestEntity(mp);

      HttpClient client = new HttpClient();

      status = client.executeMethod(filePost);
      filePost.releaseConnection();
    }
    catch (final Exception e)
    {
      if (((e instanceof NoRouteToHostException)) || ((e instanceof UnknownHostException))) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openError(null, "Error Uploading", 
              "There was an error uploading the file: \nNo network connection.  Please try again later");
          } } );
      }
      else {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openError(null, "Error Uploading", 
              "There was an error uploading the file: \n" + 
              e.getClass().getCanonicalName());
          }
        });
        StatusHandler.log(new FishtailStatus("failed to upload", e));
      }
    }

    final int httpResponseStatus = status;

    if (status == 401)
    {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        public void run() {
          MessageDialog.openError(null, "Error Uploading", 
            "There was an error uploading.\nYour uid was incorrect.");
        } } );
    }
    else if (status == 407)
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
      {
        public void run() {
          MessageDialog.openError(
            null, 
            "Error Uploading", 
            "Could not upload because proxy server authentication failed.  Please check your proxy server settings.");
        } } );
    else if (status != 200)
    {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        public void run() {
          MessageDialog.openError(null, "Error Uploading", 
            "There was an error uploading.\nHTTP Response Code " + 
            httpResponseStatus + "\n" + "Please try again later");
        }
      });
    }
  }

  public static void displaySurvey(SurveyWizardPage.Number num)
  {
    Display.getDefault().asyncExec(new Runnable()
    {
      public void run() {
        IWorkbenchWizard wiz = new SurveyWizard();
        wiz.init(PlatformUI.getWorkbench(), new StructuredSelection());

        Shell shell = new Shell(Display.getDefault());
        new WizardDialog(shell, wiz).open();
      }
    });
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.survey.SurveyUtil
 * JD-Core Version:    0.6.0
 */