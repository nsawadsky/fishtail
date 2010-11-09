package ca.ubc.spl.fishtail;

import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.SearchListeners;
import ca.ubc.spl.fishtail.model.SearchStrategy;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.DomainName;
import ca.ubc.spl.fishtail.survey.SurveyUtil;
import ca.ubc.spl.fishtail.views.IFishtailView;
import ca.ubc.spl.fishtail.views.IFishtailView.ViewType;
import ca.ubc.spl.fishtail.wizards.SurveyWizardPage;
import ca.ubc.spl.fishtail.wizards.SurveyWizardPage.Number;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
//import org.eclipse.mylyn.internal.monitor.usage.InteractionEventObfuscator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

public class ActivityMonitor
  implements IBlacklistMonitor, IKeywordListener
{
  private static final int FIRST_SURVEY = 50;
  private static final int SECOND_SURVEY = 100;
  private static final int THIRD_SURVEY = 200;
  private boolean started = false;
  private ActivityLogger logger;
  private int activityCount;
  private boolean disableSurvey;
  private int surveyCount;
  private boolean obfuscate;
  private IPropertyChangeListener prop = new IPropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent event) {
      if (event.getProperty().equals("ca.ubc.spl.fishtail.monitor.obfuscate"))
        try {
          ActivityMonitor.this.obfuscate = ((Boolean)event.getNewValue()).booleanValue();
        }
        catch (Exception localException)
        {
        }
    }
  };

  //private static InteractionEventObfuscator handleObfuscator = new InteractionEventObfuscator();

  public void incrementCount()
  {
    this.activityCount += 1;

    if (this.disableSurvey)
      return;
    SurveyWizardPage.Number num;
    if (displayFirstSurvey()) {
      num = SurveyWizardPage.Number.One;
    }
    else
    {
      if (displaySecondOrThirdSurvey())
        num = SurveyWizardPage.Number.Two;
      else
        return;
    }
    this.surveyCount += 1;
    SurveyUtil.displaySurvey(num);
  }

  private boolean displaySecondOrThirdSurvey()
  {
    return ((this.surveyCount == 1) && (this.activityCount > 100) && (this.activityCount < 200)) || (
      (this.surveyCount == 2) && (this.activityCount > 200));
  }

  private boolean displayFirstSurvey() {
    return (this.surveyCount == 0) && (this.activityCount > 50) && (this.activityCount < 100);
  }

  public void start() {
    if (this.started)
      return;
    this.started = true;

    this.activityCount = FishtailPlugin.getDefault().getPreferenceStore()
      .getInt("ca.ubc.spl.fishtail.monitor.count");
    this.surveyCount = FishtailPlugin.getDefault().getPreferenceStore()
      .getInt("ca.ubc.spl.fishtail.monitor.survey.count");
    this.disableSurvey = FishtailPlugin.getDefault().getPreferenceStore()
      .getBoolean("ca.ubc.spl.fishtail.monitor.disable.survey");
    this.obfuscate = FishtailPlugin.getDefault().getPreferenceStore()
      .getBoolean("ca.ubc.spl.fishtail.monitor.obfuscate");

    FishtailPlugin.getDefault().getBlacklist().addMonitor(this);
    AllSearches.getInstance().getSearchListeners().addSearchViewListener(this);
    Display.getDefault().addFilter(13, new Listener()
    {
      public void handleEvent(Event event) {
        try {
          if (AllSearches.getInstance().equals(
            ((ScrollBar)event.widget).getParent().getData()))
            ActivityMonitor.this.scroll((ScrollBar)event.widget);
        }
        catch (Exception localException)
        {
        }
      }
    });
    FishtailPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this.prop);

    this.logger = new ActivityLogger();
    this.logger.startMonitoring();
  }

  public void stop()
  {
    FishtailPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this.prop);
    FishtailPlugin.getDefault().getPreferenceStore().setValue(
      "ca.ubc.spl.fishtail.monitor.disable.survey", this.disableSurvey);
    FishtailPlugin.getDefault().getPreferenceStore().setValue(
      "ca.ubc.spl.fishtail.monitor.count", this.activityCount);
    FishtailPlugin.getDefault().getPreferenceStore().setValue(
      "ca.ubc.spl.fishtail.monitor.survey.count", this.surveyCount);
    this.logger.stopMonitoring();

    this.started = false;
  }

  public void scroll(ScrollBar bar) {
    ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.Scroll);
    ae.setAttribute("direction", ((bar.getStyle() & 0x100) != 0 ? "H" : "") + (
      (bar.getStyle() & 0x200) != 0 ? "V" : ""));
    ae.setAttribute("location", Integer.toString(bar.getSelection()));
    ae.setAttribute("thumb", Integer.toString(bar.getThumb()));
    ae.setAttribute("max", Integer.toString(bar.getMaximum()));
    ae.setAttribute("size", bar.getSize().toString());
    this.logger.log(ae);
  }

  public void tooltip(SearchHit hit, IFishtailView view) {
    if (hit != null) {
      ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.Tooltip, view);
      String url = hit.getUrl();
      if (this.obfuscate) {
        url = obfuscate(url);
      }
      ae.setAttribute("url", url);
      ae.setAttribute("category", hit.getStrategy().getName());
      this.logger.log(ae);
    }
  }

  public void broadenCategory(SearchStrategy ss, IFishtailView view) {
    ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.Broaden, view);
    ae.setAttribute("category", ss.getName());
    this.logger.log(ae);
    incrementCount();
  }

  public void search(boolean automatic) {
    ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.Search);
    ae.setAttribute("keywords", 
      TokenUtil.toDelimited(AllSearches.getInstance().getKeywords(), 
      "/"));
    this.logger.log(ae);
    if (!automatic)
      incrementCount();
  }

  public void blacklistChanged(BlacklistDelta changes) {
    ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.Blacklist);
    if (this.obfuscate) {
      changes = obfuscate(changes);
    }
    ae.setAttribute("changes", changes.toString());
    this.logger.log(ae);
  }

  private BlacklistDelta obfuscate(BlacklistDelta changes) {
    BlacklistDelta delta = new BlacklistDelta();
    for (String add : changes.toAdd) {
      delta.toAdd.add(obfuscate(add));
    }
    for (String rem : changes.toRemove) {
      delta.toRemove.add(obfuscate(rem));
    }
    return delta;
  }

  public static String obfuscate(String url)
  {
    String chop = DomainName.chopHttpCSS(url);
    List<String> tokens = TokenUtil.toList(chop, '/');
    List oTokens = new ArrayList();
    for (String s : tokens) {
      // Obfuscation temporarily disabled, since it uses an internal Mylyn task
      // that is no longer accessible.
      //oTokens.add(handleObfuscator.obfuscateString(s));
      oTokens.add(s);
      oTokens.add(s);
    }

    return TokenUtil.toDelimited(oTokens, '/');
  }

  public void keywordsChanged() {
    ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.Keyword);
    ae.setAttribute("changes", 
      TokenUtil.toDelimited(AllSearches.getInstance().getKeywords(), 
      "/"));
    this.logger.log(ae);
  }

  public void refreshView() {
  }

  public ActivityLogger getLogger() {
    return this.logger;
  }

  public void openUrl(SearchHit hit, IFishtailView view) {
    String url = hit.getUrl();

    String category = hit.getStrategy().getName();
    if (this.obfuscate) {
      url = obfuscate(url);
    }

    ActivityEvent ae = new ActivityEvent(ActivityEvent.EventType.OpenUrl);

    ae.setAttribute("url", url);
    ae.setAttribute("view", view.getType().toString());
    ae.setAttribute("category", category);
    this.logger.log(ae);

    incrementCount();
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.ActivityMonitor
 * JD-Core Version:    0.6.0
 */