package ca.ubc.spl.fishtail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.internal.commons.core.XmlStringConverter;
import org.eclipse.mylyn.monitor.core.AbstractMonitorLog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer;
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer.Token;

public class ActivityLogger extends AbstractMonitorLog
{
  public static final String ACTIVITY_EVENT = "activityevent";
  private static final String FILENAME = "fishtail-activity.xml";
  private static final String CLOSE = ">";
  private static final String ENDL = "\n";
  private static final String OPEN = "<";
  private static final String SLASH = "/";
  private static final String TAB = "\t";
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z", 
    Locale.ENGLISH);

  private List<ActivityEvent> queue = new CopyOnWriteArrayList();

  public ActivityLogger() {
    File rootDir = FishtailPlugin.getDefault().getStateLocation().toFile();
    this.outputFile = new File(rootDir, "fishtail-activity.xml");
  }

  private void closeElement(StringBuffer buffer, String tag) {
    buffer.append("<");
    buffer.append("/");
    buffer.append(tag);
    buffer.append(">");
    buffer.append("\n");
  }

  private void formatContent(StringBuffer buffer, Date date) {
    buffer.append(this.dateFormat.format(date));
  }

  private void formatContent(StringBuffer buffer, String content) {
    if ((content != null) && (content.length() > 0))
    {
      String xmlContent = XmlStringConverter.convertToXmlString(content);
      xmlContent = xmlContent.replace("\n", "\n\t\t");
      buffer.append(xmlContent);
    }
  }

  private void formatContent(StringBuffer buffer, ActivityEvent.EventType type) {
    buffer.append(type.toString());
  }

  public List<ActivityEvent> getHistoryFromFile(File file) {
    List events = new ArrayList();
    try
    {
      if (file.getName().endsWith(".zip")) {
        ZipFile zip = new ZipFile(file);
        if (zip.entries().hasMoreElements()) {
          ZipEntry entry = (ZipEntry)zip.entries().nextElement();
          getHistoryFromStream(zip.getInputStream(entry), events);
        }
      } else {
        InputStream reader = new FileInputStream(file);
        getHistoryFromStream(reader, events);
        reader.close();
      }
    }
    catch (Exception e) {
      StatusHandler.log(new FishtailStatus("could not read interaction history"));
      e.printStackTrace();
    }
    return events;
  }

  private void getHistoryFromStream(InputStream reader, List<ActivityEvent> events)
    throws IOException
  {
    String buf = "";
    String tag = "</activityevent>";
    String endl = "\r\n";
    byte[] buffer = new byte[1000];
    int bytesRead = 0;
    while ((bytesRead = reader.read(buffer)) != -1) {
      buf = buf + new String(buffer, 0, bytesRead);
      int index;
      while ((index = buf.indexOf(tag)) != -1)
      {
        index += tag.length();
        String xml = buf.substring(0, index);
        ActivityEvent event = readEvent(xml);
        if (event != null) {
          events.add(event);
        }
        if (index + endl.length() > buf.length())
          buf = "";
        else {
          buf = buf.substring(index + endl.length(), buf.length());
        }
      }
      buffer = new byte[1000];
    }
  }

  private String getXmlForEvent(ActivityEvent e) {
    try {
      StringBuffer res = new StringBuffer();
      res.append("<");
      res.append("activityevent");
      res.append(">");
      res.append("\n");

      openElement(res, "type");
      formatContent(res, e.getType());
      closeElement(res, "type");

      openElement(res, "date");
      formatContent(res, e.getDate());
      closeElement(res, "date");

      for (Map.Entry attr : e.getAttributes().entrySet()) {
        openElement(res, (String)attr.getKey());
        formatContent(res, (String)attr.getValue());
        closeElement(res, (String)attr.getKey());
      }

      res.append("<");
      res.append("/");
      res.append("activityevent");
      res.append(">");
      res.append("\n");
      return res.toString();
    } catch (Throwable t) {
      StatusHandler.fail(new FishtailStatus("could not write event", t));
    }return "";
  }

  public synchronized void log(ActivityEvent event)
  {
    try
    {
      if (this.started) {
        String xml = getXmlForEvent(event);
        this.outputStream.write(xml.getBytes());
      } else if (event != null) {
        this.queue.add(event);
      }
    } catch (Throwable t) {
      StatusHandler.log(new FishtailStatus("could not log interaction event"));
    }
  }

  private void openElement(StringBuffer buffer, String tag) {
    buffer.append("\t");
    buffer.append("<");
    buffer.append(tag);
    buffer.append(">");
  }

  public ActivityEvent readEvent(String xml) {
    Reader reader = new StringReader(xml);
    HtmlStreamTokenizer tokenizer = new HtmlStreamTokenizer(reader, null);
    String kind = "";
    String date = "";
    Map attrs = new HashMap();
    try
    {
      String close;
      for (HtmlStreamTokenizer.Token token = tokenizer.nextToken(); token.getType() != HtmlStreamTokenizer.Token.EOF; token = tokenizer
        .nextToken())
      {
        String tag = token.getValue().toString();
        if (tag.equals("</activityevent>")) {
          break;
        }
        if (tag.equals("<type>")) {
          kind = readStringContent(tokenizer, "</type>");
          kind = kind.toLowerCase(Locale.ENGLISH);
        } else if (tag.equals("<date>")) {
          date = readStringContent(tokenizer, "</date>");
        }
        else if ((tag.matches("<[a-zA-Z0-9]+>")) && (!tag.equals("<activityevent>"))) {
          close = "</" + tag.substring(1, tag.length());
          attrs.put(token.getValue().toString(), readStringContent(tokenizer, close));
        }
      }

      ActivityEvent event = new ActivityEvent(ActivityEvent.EventType.fromString(kind), 
        this.dateFormat.parse(date));
      for (Object o : attrs.entrySet()) {
        Map.Entry e = (Map.Entry)o;
        event.setAttribute((String)e.getKey(), (String)e.getValue());
      }
      attrs.clear();
      return event;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }

  private String readStringContent(HtmlStreamTokenizer tokenizer, String endTag) throws IOException, ParseException
  {
    StringBuffer content = new StringBuffer();
    HtmlStreamTokenizer.Token token = tokenizer.nextToken();
    while (!token.getValue().toString().equals(endTag)) {
      if (content.length() > 0) {
        content.append(' ');
      }
      content.append(token.getValue().toString());
      token = tokenizer.nextToken();
    }
    return XmlStringConverter.convertXmlToString(content.toString()).trim();
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.ActivityLogger
 * JD-Core Version:    0.6.0
 */