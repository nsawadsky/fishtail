package ca.ubc.spl.fishtail.preferences;

import ca.ubc.spl.fishtail.FishtailPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage
  implements IWorkbenchPreferencePage
{
  public PreferencePage()
  {
    super(1);
    setPreferenceStore(FishtailPlugin.getDefault().getPreferenceStore());
  }

  public void createFieldEditors()
  {
    Composite p = getFieldEditorParent();
    addField(
      new BooleanFieldEditor("BackgroundSearch", 
      "Enable background searching", p));
    addField(new BooleanFieldEditor("ca.ubc.spl.fishtail.monitor.obfuscate", "Enable obfuscation", p));

    p = getFieldEditorParent();
    AddRemoveListEditor domainList = new AddRemoveListEditor(p);
    addField(domainList);
    domainList.fillIntoGrid(p, 3);
    domainList.load();
  }

  public void init(IWorkbench workbench)
  {
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.preferences.PreferencePage
 * JD-Core Version:    0.6.0
 */