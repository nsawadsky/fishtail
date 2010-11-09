package ca.ubc.spl.fishtail.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class UsageFileSelectionWizardPage extends WizardPage
{
  private static final String PAGE_TITLE = "Select any archived Mylyn usage files you wish to upload";
  private static final String DESCRIPTION = "Please select the archived usage files you want to upload to eclipse.org";
  private Table zippedFilesTable;
  public static final String SUBMISSION_LOG_FILE_NAME = "submittedUsageLogs.txt";

  protected UsageFileSelectionWizardPage(String pageName)
  {
    super("org.eclipse.mylyn.monitor.usage.fileSelectionPage", "Select any archived Mylyn usage files you wish to upload", 
      UiUsageMonitorPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.monitor.usage", 
      "icons/wizban/banner-submission.gif"));
    setDescription("Please select the archived usage files you want to upload to eclipse.org");
  }

  private static List<File> getBackupFiles() {
    ArrayList backupFiles = new ArrayList();
    try
    {
      String destination = MonitorFileRolloverJob.getZippedMonitorFileDirPath();

      File backupFolder = new File(destination);

      if (backupFolder.exists()) {
        File[] files = backupFolder.listFiles();
        File submissionLogFile = new File(destination, "submittedUsageLogs.txt");

        if (!submissionLogFile.exists()) {
          submissionLogFile.createNewFile();
        }

        FileInputStream inputStream = new FileInputStream(submissionLogFile);

        int bytesRead = 0;
        byte[] buffer = new byte[1000];

        String fileContents = "";

        if (submissionLogFile.exists()) {
          while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileContents = fileContents + new String(buffer, 0, bytesRead);
          }
        }

        for (File file : files) {
          if ((!file.getName().contains("monitor-log")) || 
            (fileContents.contains(file.getName()))) continue;
          backupFiles.add(file);
        }
      }

    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return backupFiles;
  }

  public static boolean unsubmittedLogsExist() {
    List backupFiles = getBackupFiles();
    return backupFiles.size() > 0;
  }

  private void addZippedFileView(Composite composite) {
    this.zippedFilesTable = new Table(composite, 2050);

    GridDataFactory.fillDefaults().span(2, -1).grab(true, true).applyTo(this.zippedFilesTable);

    TableColumn filenameColumn = new TableColumn(this.zippedFilesTable, 16384);
    filenameColumn.setWidth(200);

    List backupFiles = getBackupFiles();

    File[] backupFileArray = (File[])backupFiles.toArray(new File[backupFiles.size()]);

    if ((backupFileArray != null) && (backupFileArray.length > 0)) {
      Arrays.sort(backupFileArray, new Comparator() {
        public int compare(File file1, File file2) {
          return new Long(file1.lastModified()).compareTo(new Long(file2.lastModified())) * -1;
        }
      });
      for (File file : backupFileArray) {
        TableItem item = new TableItem(this.zippedFilesTable, 0);
        item.setData(file.getAbsolutePath());
        item.setText(file.getName());
      }
    }
  }

  public void createControl(Composite parent) {
    try {
      Composite container = new Composite(parent, 0);
      GridLayout layout = new GridLayout(3, false);
      layout.verticalSpacing = 15;
      container.setLayout(layout);
      addZippedFileView(container);
      setControl(container);
    }
    catch (RuntimeException e) {
      StatusHandler.fail(e, "Could not create import wizard page", true);
    }
  }

  public List<String> getZipFilesSelected()
  {
    List list = new ArrayList();
    if (this.zippedFilesTable.getSelectionCount() >= 1) {
      TableItem[] selectedItems = this.zippedFilesTable.getSelection();
      for (int i = 0; i < selectedItems.length; i++)
        list.add(selectedItems[i].getText());
    }
    else {
      list.add("<unspecified>");
    }
    return list;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.monitor.UsageFileSelectionWizardPage
 * JD-Core Version:    0.6.0
 */