package ca.ubc.spl.fishtail;

import ca.ubc.spl.fishtail.views.IFishtailView;
import ca.ubc.spl.fishtail.views.IFishtailView.ViewType;
import ca.ubc.spl.fishtail.views.InplaceSearchDialog;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityEvent
{
  private final EventType type;
  private final Date date;
  private final Map<String, String> attributes = new HashMap();

  public ActivityEvent(EventType type) {
    this(type, Calendar.getInstance().getTime());
  }

  public ActivityEvent(EventType type, Date date) {
    this.type = type;
    this.date = date;
  }

  public ActivityEvent(EventType tooltip, IFishtailView view) {
    this(tooltip);
    IFishtailView.ViewType viewType;
    if (view == null)
    {
      if (InplaceSearchDialog.getOpen() == null)
        viewType = IFishtailView.ViewType.View;
      else
        viewType = IFishtailView.ViewType.Inplace;
    } else {
      viewType = view.getType();
    }
    setAttribute("viewType", viewType.toString());
  }

  public EventType getType() {
    return this.type;
  }

  public Date getDate() {
    return this.date;
  }

  public String toString()
  {
    return this.type + " " + this.date + " " + getAttribute("data");
  }

  public String getAttribute(String attr) {
    return (String)this.attributes.get(attr);
  }

  public String setAttribute(String attr, String value) {
    return (String)this.attributes.put(attr, value);
  }

  public Map<String, String> getAttributes() {
    return this.attributes;
  }

  public static enum EventType
  {
    Tooltip("tooltip"), Broaden("broaden"), Search("search"), Blacklist("blacklist"), 
    Keyword("keyword"), OpenUrl("openUrl"), Scroll("scroll");

    private String s;

    private EventType(String s) { this.s = s; }

    public static EventType fromString(String s)
    {
      for (EventType t : values()) {
        if (s.equals(t.s))
          return t;
      }
      return null;
    }

    public String toString()
    {
      return this.s;
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.ActivityEvent
 * JD-Core Version:    0.6.0
 */