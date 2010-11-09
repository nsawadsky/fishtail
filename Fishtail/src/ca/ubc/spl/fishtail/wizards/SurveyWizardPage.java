package ca.ubc.spl.fishtail.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SurveyWizardPage extends WizardPage
{
  private Map<String, List<Button>> questions15 = new HashMap();
  private Map<String, Combo> questionsDrop = new HashMap();
  private Map<String, Text> questionsText = new HashMap();
  private Number n;

  public SurveyWizardPage(Number n)
  {
    super("Fishtail Survey");
    setTitle("Fishtail Survey");
    setDescription("Please rank 1 as Strongly Disagree, 3 as Neutral, 5 as Strongly Agree.");
    if (n == null)
      this.n = Number.One;
    else
      this.n = n;
  }

  public void createControl(Composite parent) {
    Composite container = new Composite(parent, 0);

    GridLayout layout = new GridLayout(2, false);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    container.setLayout(layout);
    container.setData(gd);

    List resps = new ArrayList();
    switch (this.n.ordinal())
    {
    case 1:
      resps.add("<1 year");
      resps.add("1-3 years");
      resps.add("3-5 years");
      resps.add("5+ years");
      addQuestionDrop("How long have you been programming in Java?", "q1-1", container, resps);

      resps.clear();
      resps.add("1.x");
      resps.add("2.x");
      resps.add("3.0");
      resps.add("3.1");
      resps.add("3.2");
      resps.add("3.3");
      addQuestionDrop("How long have you been programming using Eclipse?", "q1-2", container, 
        resps);

      resps.clear();
      resps.add("0.x");
      resps.add("1.x");
      resps.add("2.x");
      addQuestionDrop("How long have you been programming using Mylyn?", "q1-3", container, 
        resps);

      addQuestion15("The tool finds resources useful to my work.", "q1-4", container);

      addQuestion15("The tool misses finding resources I expected it to find.", "q1-5", 
        container);
      break;
    case 2:
      resps.clear();
      resps.add("0-25");
      resps.add("25-50");
      resps.add("50-75");
      resps.add("75+");
      addQuestionDrop(
        "What percentage of the time was the tool able\nto provide meaningful resources to you?", 
        "q2-1", container, resps);

      addQuestion15("The code base I work on has many resources available on the web.", 
        "q2-2", container);

      addQuestionText("What keywords have you found it useful to add?", "q2-3", container);

      addQuestionText("What other categories of searches would you find useful?", "q2-4", 
        container);

      addQuestion15("Automating web searches saves me time.", "q2-5", container);

      addQuestion15(
        "The tool helps me find resources faster\nthan manually searching the web.", 
        "q2-6", container);

      addQuestion15(
        "The suggested hits are relevant to your work\nmore of the time than manually searching the web.", 
        "q2-7", container);

      resps.clear();
      resps.add("<5 minutes");
      resps.add("5-10 minutes");
      resps.add("10-30 minutes");
      resps.add("30+ minutes");
      addQuestionDrop(
        "If the tool was unable to find useful resources, how long did it take you\n(on average) to manually search and find what you were looking for?", 
        "q2-8", container, resps);

      resps.clear();
      resps.add("0-25");
      resps.add("25-50");
      resps.add("50-75");
      resps.add("75+");
      addQuestionDrop(
        "What percentage of the time was the tool able\nto provide the exact information you were looking for?", 
        "q2-9", container, resps);
    }

    addQuestion15("I will continue to use the tool.", "q0", container);

    setControl(container);
  }

  private void addQuestionText(String q, String id, Composite parent) {
    Label l = new Label(parent, 0);
    l.setText(q);

    Text resp = new Text(parent, 2048);
    resp.setText("");
    GridData gd = new GridData(768);
    gd.horizontalAlignment = 4;
    gd.grabExcessHorizontalSpace = true;
    resp.setData(gd);

    this.questionsText.put(id, resp);
  }

  private void addQuestionDrop(String q, String id, Composite parent, List<String> resps) {
    Label l = new Label(parent, 0);
    l.setText(q);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    l.setData(gd);

    Combo resp = new Combo(parent, 8);
    String[] arr = (String[])resps.toArray(new String[resps.size() + 1]);
    arr[(arr.length - 1)] = "No Comment";
    resp.setItems(arr);
    resp.setText(q);
    resp.setText("No Comment");
    this.questionsDrop.put(id, resp);
  }

  private void addQuestion15(String q, String id, Composite parent) {
    Label l = new Label(parent, 0);
    l.setText(q);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    l.setData(gd);

    Group g = new Group(parent, 131072);

    g.setLayout(new GridLayout(6, false));
    int n = 5;
    List lb = new ArrayList(n);
    for (int i = 0; i < n; i++) {
      Button r = new Button(g, 16);
      r.setText(Integer.toString(i + 1));

      lb.add(r);
    }
    Button r = new Button(g, 16);
    r.setText("No Comment");
    lb.add(r);
    r.setSelection(true);

    this.questions15.put(id, lb);
  }

  public Map<String, String> getSurveyResults() {
    Map survey = new HashMap();

    for (Map.Entry e : this.questions15.entrySet()) {
      for (int i = 0; i < ((List)e.getValue()).size(); i++) {
        if (((Button)((List)e.getValue()).get(i)).getSelection()) {
          survey.put((String)e.getKey(), i + 1);
          break;
        }
      }
    }

    for (Map.Entry e : this.questionsDrop.entrySet()) {
      survey.put((String)e.getKey(), ((Combo)e.getValue()).getText());
    }

    for (Map.Entry e : this.questionsText.entrySet()) {
      survey.put((String)e.getKey(), ((Text)e.getValue()).getText());
    }

    return survey;
  }

  public static enum Number
  {
    One, Two;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.wizards.SurveyWizardPage
 * JD-Core Version:    0.6.0
 */