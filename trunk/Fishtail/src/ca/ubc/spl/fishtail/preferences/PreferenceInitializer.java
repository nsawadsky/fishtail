package ca.ubc.spl.fishtail.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
  public static final String SURVEYS_DISPLAYED = "SurveysDisplayed";
  public static final String BACKGROUND_SEARCH = "BackgroundSearch";
  public static final String OBFUSCATE = "ca.ubc.spl.fishtail.monitor.obfuscate";
  public static final String ACTIVITY_COUNT = "ca.ubc.spl.fishtail.monitor.count";
  public static final String DISABLE_SURVEY = "ca.ubc.spl.fishtail.monitor.disable.survey";
  public static final String SURVEY_COUNT = "ca.ubc.spl.fishtail.monitor.survey.count";

  public void initializeDefaultPreferences()
  {
    IEclipsePreferences node = new DefaultScope().getNode("ca.ubc.spl.fishtail");
    node.put("BackgroundSearch", Boolean.TRUE.toString());
    node.put("ca.ubc.spl.fishtail.monitor.obfuscate", Boolean.TRUE.toString());
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.preferences.PreferenceInitializer
 * JD-Core Version:    0.6.0
 */