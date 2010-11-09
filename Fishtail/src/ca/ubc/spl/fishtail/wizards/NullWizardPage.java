package ca.ubc.spl.fishtail.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NullWizardPage extends WizardPage
{
  protected NullWizardPage()
  {
    super("Fishtail Survey");
    setTitle("Fishtail Survey");
  }

  public void createControl(Composite parent) {
    Composite container = new Composite(parent, 0);

    GridLayout layout = new GridLayout(1, false);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    container.setLayout(layout);
    container.setData(gd);

    Label l = new Label(container, 0);
    l.setText("This wizard is automatically opened as you use the tool.");
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    l.setData(gd);
    setControl(container);
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.wizards.NullWizardPage
 * JD-Core Version:    0.6.0
 */