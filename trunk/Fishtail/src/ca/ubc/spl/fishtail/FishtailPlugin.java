package ca.ubc.spl.fishtail;

import ca.ubc.spl.fishtail.model.AllSearches;
import ca.ubc.spl.fishtail.model.engine.SearchHit;
import ca.ubc.spl.fishtail.model.parser.DomainName;
import ca.ubc.spl.fishtail.model.parser.ItemCounter;
import ca.ubc.spl.fishtail.survey.SurveyUtil;
import ca.ubc.spl.fishtail.views.IFishtailView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class FishtailPlugin extends AbstractUIPlugin
  implements IBlacklistMonitor
{
  public static final String PREF_USER_ID = "ca.ubc.spl.fishtail.user.id";
  public static final String PLUGIN_ID = "ca.ubc.spl.fishtail";
  private static final String HIT_DOMAINS = "domains.hit";
  private static final String BLACKLISTED_HOSTS = "domains.blacklisted.hosts";
  private static final String FISHTAIL_DOMAINS = "FishtailDomains";
  private static final int DOMAINS_TO_SAVE = 20;
  private static FishtailPlugin plugin;
  private ItemCounter<String> domainsHit = new ItemCounter();

  private Blacklist blacklist = new Blacklist();
  private ActivityMonitor monitor = new ActivityMonitor();

  private IPropertyChangeListener prop = new IPropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent event) {
      if (event.getProperty().equals("BackgroundSearch"))
        try {
          boolean enable = ((Boolean)event.getNewValue()).booleanValue();
          FishtailPlugin.enableBackgroundSearch(enable);
        }
        catch (Exception localException)
        {
        }
    }
  };
  private int uid;
  private Set<String> urlTracking = new HashSet();

  private static AbstractContextListener interactionContextListener = new MylynContextListener();

  public static void enableBackgroundSearch(boolean enable) {
    if (enable)
      ContextCore.getContextManager().addListener(interactionContextListener);
    else
      ContextCore.getContextManager().removeListener(interactionContextListener);
  }

  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    plugin = this;

    loadDomains();
    loadPrefs();

    this.monitor.start();
    this.blacklist.addMonitor(this);
  }

  private void loadPrefs() {
    getPreferenceStore().addPropertyChangeListener(this.prop);

    if (getPreferenceStore().getBoolean("BackgroundSearch"))
      enableBackgroundSearch(true);
  }

  public void stop(BundleContext context) throws Exception
  {
    this.blacklist.removeMonitor(this);
    this.monitor.stop();
    stopPrefs();
    enableBackgroundSearch(false);
    saveDomains();

    plugin = null;
    super.stop(context);
  }

  private void stopPrefs()
  {
    getPreferenceStore().removePropertyChangeListener(this.prop);
    enableBackgroundSearch(false);
  }

  public static FishtailPlugin getDefault() {
    return plugin;
  }

  private void loadDomains() {
    IDialogSettings d = getDialogSettings();
    IDialogSettings domains = d.getSection("FishtailDomains");
    if (domains != null) {
      String[] arr = domains.getArray("domains.hit");
      int count;
      if (arr != null) {
        for (String domain : arr)
        {
          try {
            count = domains.getInt(domain);
          }
          catch (NumberFormatException localNumberFormatException)
          {
            count = 1;
          }
          this.domainsHit.incrementItem(domain, count);
        }
      }

      BlacklistDelta delta = new BlacklistDelta();

      String[] black = domains.getArray("domains.blacklisted.hosts");
      if (black != null) {
        for (String host : black) {
          delta.addAdd(host);
        }

      }

      this.blacklist.commit(delta, false);
    }
  }

  private void saveDomains()
  {
    IDialogSettings persistedDomains = getDialogSettings().addNewSection("FishtailDomains");

    String[] hits = (String[])getDomainsHit().toArray(new String[getDomainsHit().size()]);
    persistedDomains.put("domains.hit", hits);
    for (String s : hits) {
      int count = this.domainsHit.getCount(s);
      if (count >= 0) {
        persistedDomains.put(s, count);
      }
    }
    Set hostsBlacklisted = this.blacklist.getItems();
    String[] black = (String[])hostsBlacklisted.toArray(new String[hostsBlacklisted.size()]);
    persistedDomains.put("domains.blacklisted.hosts", black);
  }

  public List<String> getDomainsHit()
  {
    return this.domainsHit.getTopNItems(20);
  }

  public Blacklist getBlacklist() {
    return this.blacklist;
  }

  public void blacklistChanged(BlacklistDelta changes) {
    for (String hit : this.domainsHit.getAll()) {
      for (String host : changes.getToAdd()) {
        if (host.matches(hit))
          this.domainsHit.remove(hit);
      }
    }
    AllSearches.getInstance().rerankAll();
  }

  public ActivityMonitor getMonitor() {
    return this.monitor;
  }

  public int getUid() {
    if (this.uid == 0) {
      this.uid = getPreferenceStore().getInt("ca.ubc.spl.fishtail.user.id");
      if (this.uid == 0) {
        try
        {
          this.uid = SurveyUtil.getNewUid("first", "last", "email", false);
          getDefault().getPreferenceStore().setValue(
            "ca.ubc.spl.fishtail.user.id", this.uid);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return this.uid;
  }

  public void openUrl(SearchHit hit, IFishtailView view) {
    TasksUiUtil.openUrl(hit.getUrl());
    this.monitor.openUrl(hit, view);

    trackUrl(hit.getUrl());
  }

  public Set<String> getHitUrls() {
    return this.urlTracking;
  }

  @Deprecated
  public void trackUrl(String url)
  {
    this.urlTracking.add(url);
    String parentDomain = DomainName.getParentDomain(url);
    this.domainsHit.incrementItem(parentDomain);
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.FishtailPlugin
 * JD-Core Version:    0.6.0
 */