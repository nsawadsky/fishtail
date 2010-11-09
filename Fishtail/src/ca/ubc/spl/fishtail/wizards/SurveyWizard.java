package ca.ubc.spl.fishtail.wizards;

import ca.ubc.spl.fishtail.ActivityLogger;
import ca.ubc.spl.fishtail.ActivityMonitor;
import ca.ubc.spl.fishtail.FishtailPlugin;
import ca.ubc.spl.fishtail.FishtailStatus;
import ca.ubc.spl.fishtail.survey.SurveyUtil;
import java.io.File;
import java.util.Map;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class SurveyWizard extends Wizard
  implements INewWizard
{
  private ReviewSubmissionPage rev;
  private SurveyWizardPage page;
  private SurveyWizardPage.Number num = null;
  static final String SURVEY = "Fishtail Survey";

  public SurveyWizard()
  {
    setWindowTitle("Fishtail Survey");
    setNeedsProgressMonitor(true);
  }

  public SurveyWizard(SurveyWizardPage.Number num) {
    this.num = num;
  }

  public void addPages()
  {
    if (this.num != null) {
      this.page = new SurveyWizardPage(this.num);
      addPage(this.page);

      this.rev = new ReviewSubmissionPage();
      addPage(this.rev);
    } else {
      addPage(new NullWizardPage());
    }
  }

  public boolean performFinish()
  {
    if (this.num != null) {
      Map survey = this.page.getSurveyResults();
      int uid = FishtailPlugin.getDefault().getUid();
      try
      {
        File f;
        if (this.rev.isUploadDisabled())
          f = null;
        else {
          f = FishtailPlugin.getDefault().getMonitor().getLogger().getOutputFile();
        }
        SurveyUtil.upload(f, uid, survey);
      } catch (Exception e) {
        StatusHandler.fail(new FishtailStatus("Survey Upload", e));
      }
    }
    return true;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
  }

  public boolean canFinish() {
    return super.canFinish();
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.wizards.SurveyWizard
 * JD-Core Version:    0.6.0
 */