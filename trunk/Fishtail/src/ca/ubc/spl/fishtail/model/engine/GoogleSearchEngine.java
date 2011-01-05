package ca.ubc.spl.fishtail.model.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer;
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer.Token;
import org.eclipse.mylyn.commons.net.HtmlTag;
import org.xml.sax.SAXException;

import ca.ubc.spl.fishtail.FishtailStatus;

public class GoogleSearchEngine
{
  private HtmlStreamTokenizer.Token token;
  protected String args;
  protected List<SearchHit> hits = new ArrayList();
  private final int numHits;
  protected String url;
  private int lastStart;
  private int totalHits;
  private HtmlStreamTokenizer tokenizer;
  HtmlTag htmlTag = null;

  public GoogleSearchEngine(String args, int numHits) throws HttpException, IOException, SAXException, ParseException
  {
    this(args, 0, numHits);
  }

  private GoogleSearchEngine(String args, int startAt, int numHits) throws HttpException, IOException, SAXException, ParseException
  {
    this.args = args;
    this.url = 
      ("http://www.google.ca/search?hl=en&q=" + URLEncoder.encode(args, "UTF-8") + 
      "&start=" + startAt + "&num=" + numHits);
    this.numHits = numHits;
    this.lastStart = startAt;

    HttpClient c = new HttpClient();
    GetMethod gm = new GetMethod(this.url);

    c.executeMethod(gm);

    InputStream in = gm.getResponseBodyAsStream();
    this.tokenizer = new HtmlStreamTokenizer(new InputStreamReader(in), null);
    this.totalHits = getHitCount();
    generateSearchHits();

    gm.releaseConnection();
  }

  @Deprecated
  public GoogleSearchEngine(String args, int startAt, int numHits, HtmlStreamTokenizer tokenizer)
    throws HttpException, IOException, SAXException, ParseException
  {
    this.args = args;
    this.url = 
      ("http://www.google.ca/search?hl=en&q=" + URLEncoder.encode(args, "UTF-8") + 
      "&start=" + startAt + "&num=" + numHits);
    this.numHits = numHits;
    this.lastStart = startAt;
    this.tokenizer = tokenizer;
  }

  @Deprecated
  public int getHitCount()
    throws IOException, ParseException
  {
    String tag;
    do
    {
      this.token = this.tokenizer.nextToken();
      Object tval = this.token.getValue();

      if (this.token.getType() == HtmlStreamTokenizer.Token.TAG) {
        this.htmlTag = ((HtmlTag)tval);
        tag = this.htmlTag.getTagName();
        if (tag.equals("div") && "resultStats".equals(htmlTag.getAttribute("id"))) {
          String resultStatsContent = createFullNodeValueString("div", true);
          int start = "About ".length();
          int end = resultStatsContent.indexOf(" results");
          return getGoogleInt(resultStatsContent.substring(start, end));
        }
      } else {
        tag = "";
      }
    }
    while (this.token.getType() != HtmlStreamTokenizer.Token.EOF);

    return 2147483647;
  }

  @Deprecated
  public String createFullNodeValueString(String terminusTag, boolean endTag)
    throws IOException, ParseException
  {
    String ret = "";
    do
    {
      this.token = this.tokenizer.nextToken();
      Object tval = this.token.getValue();
      if (this.token.getType() == HtmlStreamTokenizer.Token.TAG) {
        this.htmlTag = ((HtmlTag)tval);
        if ((terminusTag.equalsIgnoreCase(this.htmlTag.getTagName())) && 
          (this.htmlTag.isEndTag() == endTag))
          return ret;
      } else if (this.token.getType() == HtmlStreamTokenizer.Token.TEXT) {
        StringBuffer sb = (StringBuffer)tval;
        if (ret.length() != 0) {
          ret = ret + " ";
        }
        ret = ret + sb.toString();
      }
    }
    while (this.token.getType() != HtmlStreamTokenizer.Token.EOF);

    return ret;
  }

  @Deprecated
  public void generateSearchHits()
    throws IOException, ParseException
  {
    String tag = ""; String classAttr = "";
    while (true)
    {
      this.token = this.tokenizer.nextToken();
      Object tval = this.token.getValue();

      if (this.token.getType() == HtmlStreamTokenizer.Token.TAG) {
        this.htmlTag = ((HtmlTag)tval);
        tag = this.htmlTag.getTagName();
        classAttr = this.htmlTag.getAttribute("class");
      } else {
        tag = "";
        classAttr = "";
      }

      if ((this.token.getType() != HtmlStreamTokenizer.Token.EOF) && (
        (!"li".equalsIgnoreCase(tag)) || (!
        "g".equalsIgnoreCase(classAttr))))
        continue;
      if (this.token.getType() == HtmlStreamTokenizer.Token.EOF)
        return;
      createHit();
    }
  }
  @Deprecated
  public void createHit() throws IOException, ParseException {
    String desc = ""; String url = ""; String name = ""; String tag = "";
    Object tval;
    do {
      this.token = this.tokenizer.nextToken();
      tval = this.token.getValue();

      if (this.token.getType() == HtmlStreamTokenizer.Token.TAG) {
        this.htmlTag = ((HtmlTag)tval);
        tag = this.htmlTag.getTagName();
      } else {
        tag = "";
      }
    }
    while ((this.token.getType() != HtmlStreamTokenizer.Token.EOF) && (!
      "a".equalsIgnoreCase(tag)));

    url = this.htmlTag.getAttribute("href");
    name = createFullNodeValueString("a", true);
    /*
    do
    {
      this.token = this.tokenizer.nextToken();
      tval = this.token.getValue();
      if (this.token.getType() == HtmlStreamTokenizer.Token.TAG) {
        this.htmlTag = ((HtmlTag)tval);
        tag = this.htmlTag.getTagName();
      } else {
        tag = "";
      }
    }
    while ((this.token.getType() != HtmlStreamTokenizer.Token.EOF) && (!
      "td".equalsIgnoreCase(tag)));
    do
    {
      this.token = this.tokenizer.nextToken();
      tval = this.token.getValue();
      if (this.token.getType() == HtmlStreamTokenizer.Token.TAG) {
        this.htmlTag = ((HtmlTag)tval);
        tag = this.htmlTag.getTagName();
      } else {
        tag = "";
      }
    }
    while ((this.token.getType() != HtmlStreamTokenizer.Token.EOF) && (!
      "font".equalsIgnoreCase(tag)));

    desc = createFullNodeValueString("br", false);
    */

    SearchHit hit = new SearchHit(this, name, url, desc);
    this.hits.add(hit);
  }

  public List<SearchHit> broadenSearch() throws HttpException, IOException, SAXException, ParseException
  {
    if (this.lastStart + this.numHits > this.totalHits)
      return new ArrayList();
    GoogleSearchEngine gs = new GoogleSearchEngine(this.args, getNextStartLocation(), this.numHits);
    return gs.getHits();
  }

  private int getNextStartLocation() {
    this.lastStart += this.numHits;
    return this.lastStart;
  }

  private static int getGoogleInt(String str) {
    int parseInt = 2147483647;
    /*
    str = str.trim();
    int i = str.indexOf("about");
    if (i >= 0)
      str = dropPunctuation(str.substring(i + 6));
    */
    try
    {
      parseInt = Integer.parseInt(str);
    } catch (NumberFormatException e) {
      StatusHandler.log(new FishtailStatus("Couldn't format", e));
    }
    return parseInt;
  }

  private static String dropPunctuation(String str) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if ((c != ',') && (c != ' ')) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public String getArgs() {
    return this.args;
  }

  public List<SearchHit> getHits() {
    return this.hits;
  }

  public String getUrl() {
    return this.url;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.engine.GoogleSearchEngine
 * JD-Core Version:    0.6.0
 */