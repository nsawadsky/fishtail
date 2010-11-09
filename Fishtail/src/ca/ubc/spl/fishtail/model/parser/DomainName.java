package ca.ubc.spl.fishtail.model.parser;

import ca.ubc.spl.fishtail.TokenUtil;
import java.util.List;

public class DomainName
{
  private String name;
  private String tld;
  private static final String COLON_SLASH_SLASH = "://";

  public String getName()
  {
    return this.name;
  }

  public String getTld() {
    return this.tld;
  }

  public DomainName(String name, String tld) {
    this.name = name;
    this.tld = tld;
  }

  public boolean equals(Object arg0)
  {
    if ((arg0 instanceof DomainName)) {
      DomainName that = (DomainName)arg0;
      return (that.name.equals(this.name)) && (that.tld.equals(this.tld));
    }
    return false;
  }

  public int hashCode()
  {
    return this.name.hashCode() + this.tld.hashCode();
  }

  public String toString()
  {
    return this.name + '.' + this.tld;
  }

  public static String getParentDomain(String host) {
    host = getHost(host);
    int lastDot = -1;
    int prevDot = -1;
    int prevPrevDot = -1;
    while ((lastDot = host.indexOf('.', lastDot + 1)) >= 0) {
      prevPrevDot = prevDot;
      prevDot = lastDot;
    }
    if (prevPrevDot < -1)
      prevPrevDot = -1;
    return host.substring(prevPrevDot + 1, host.length());
  }

  public static String truncateAfterHost(String url) {
    int locCSS = url.indexOf("://");
    if (locCSS < 0)
      locCSS = 0 - "://".length();
    int locSlash = url.indexOf('/', locCSS + "://".length() + 1);
    if (locSlash < 0)
      locSlash = url.length();
    return url.substring(0, locSlash);
  }

  public static String getHost(String url) {
    int locCSS = url.indexOf("://");
    if (locCSS < 0)
      locCSS = 0 - "://".length();
    int locSlash = url.indexOf('/', locCSS + "://".length() + 1);
    if (locSlash < 0)
      locSlash = url.length();
    return url.substring(locCSS + "://".length(), locSlash);
  }

  public static boolean hostMatchesRegex(String host, String regex) {
    try {
      int locCSS = regex.indexOf("://");
      if (locCSS < 0) {
        host = chopHttpCSS(host);
      }
      return host.matches(regex); } catch (Exception localException) {
    }
    return false;
  }

  public static String chopHttpCSS(String url)
  {
    int locCSS = url.indexOf("://");
    if (locCSS < 0) {
      locCSS = 0 - "://".length();
    }
    return url.substring(locCSS + "://".length(), url.length());
  }

  public static boolean hostMatchesUrl(String host, String url) {
    if (!host.contains(".")) {
      return url.matches("([a-zA-Z]+://)?" + host + "\\.[a-zA-Z]+(/.*)?");
    }
    return url.matches("([a-zA-Z]+://)?" + host + "(/.*)?");
  }

  public static boolean domainMatchesUrl(String domain, String url) {
    if (!domain.contains(".")) {
      return url.matches("([a-zA-Z]+://)?([a-zA-Z0-9-_]+\\.)*" + domain + 
        "\\.[a-zA-Z]+(/.*)?");
    }
    return url.matches("([a-zA-Z]+://)?([a-zA-Z0-9-_]+\\.)*" + domain + "(/.*)?");
  }

  static DomainName createDomainFromPackageName(String id) {
    try {
      List l = TokenUtil.toList(id, '.');
      return new DomainName((String)l.get(1), (String)l.get(0)); } catch (Exception localException) {
    }
    return null;
  }

  public static String createRegexFromHost(String item)
  {
    String host = getHost(item);
    String regex = host.replace(".", "\\.");
    return regex + "(/.*)?";
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.parser.DomainName
 * JD-Core Version:    0.6.0
 */