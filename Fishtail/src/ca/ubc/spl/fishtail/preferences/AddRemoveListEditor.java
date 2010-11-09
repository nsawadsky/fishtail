package ca.ubc.spl.fishtail.preferences;

import ca.ubc.spl.fishtail.Blacklist;
import ca.ubc.spl.fishtail.BlacklistDelta;
import ca.ubc.spl.fishtail.FishtailPlugin;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class AddRemoveListEditor extends FieldEditor
{
  private Composite parent;
  private Text addText;
  private BlacklistDelta delta = new BlacklistDelta();
  private List list;
  private Composite buttonBox;
  private Button addButton;
  private Button removeButton;
  private SelectionAdapter selectionListener;

  public AddRemoveListEditor(Composite parent)
  {
    super("Blacklist", "Blacklist (accepts regular expressions)", parent);
    this.parent = parent;
  }

  public void load()
  {
    setPresentsDefaultValue(true);
    List l = getListControl(this.parent);
    l.removeAll();
    for (String s : FishtailPlugin.getDefault().getBlacklist().getItems()) {
      l.add(s);
    }
    this.delta.clear();
  }

  public void loadDefault()
  {
    load();
  }

  public void store()
  {
    FishtailPlugin.getDefault().getBlacklist().commit(this.delta);
    this.delta.clear();
  }

  protected void adjustForNumColumns(int numColumns)
  {
    Control control = getLabelControl();
    ((GridData)control.getLayoutData()).horizontalSpan = numColumns;
    ((GridData)this.list.getLayoutData()).horizontalSpan = (numColumns - 1);
  }

  protected void doLoad()
  {
  }

  protected void doLoadDefault()
  {
  }

  protected void doStore()
  {
  }

  public int getNumberOfControls()
  {
    return 2;
  }

  private void addPressed()
  {
    String item = this.addText.getText();
    if (item.length() > 0) {
      setPresentsDefaultValue(false);

      for (String listItem : this.list.getItems()) {
        if (listItem.equals(item)) {
          return;
        }
      }
      this.list.add(item, 0);

      this.delta.addAdd(item);
      this.addText.setText("");
      selectionChanged();
    }
  }

  protected void doFillIntoGrid(Composite parent, int numColumns)
  {
    Control control = getLabelControl(parent);
    GridData gd = new GridData();
    gd.horizontalSpan = numColumns;
    control.setLayoutData(gd);

    this.list = getListControl(parent);
    gd = new GridData(768);
    gd.verticalAlignment = 4;
    gd.horizontalSpan = (numColumns - 1);
    gd.grabExcessHorizontalSpace = true;
    this.list.setLayoutData(gd);

    this.buttonBox = getButtonBoxControl(parent);
    gd = new GridData();
    gd.verticalAlignment = 1;
    this.buttonBox.setLayoutData(gd);
  }

  public Composite getButtonBoxControl(Composite parent) {
    if (this.buttonBox == null) {
      this.buttonBox = new Composite(parent, 0);
      GridLayout layout = new GridLayout();
      layout.marginWidth = 0;
      this.buttonBox.setLayout(layout);

      this.addText = new Text(this.buttonBox, 2048);
      GridData gd = new GridData(768);
      gd.verticalAlignment = 4;
      gd.grabExcessHorizontalSpace = true;
      this.addText.setLayoutData(gd);

      this.addText.addKeyListener(new KeyListener() {
        public void keyPressed(KeyEvent e) {
          if ((e.keyCode == 13) || (e.keyCode == 16777296))
            AddRemoveListEditor.this.addPressed();
          else if (e.character == '\033')
          {
            AddRemoveListEditor.this.addText.setText("");
          }
        }

        public void keyReleased(KeyEvent e)
        {
        }
      });
      this.addButton = createPushButton(this.buttonBox, "Add");
      this.removeButton = createPushButton(this.buttonBox, "Remove");

      this.buttonBox.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent event) {
          AddRemoveListEditor.this.addButton = null;
          AddRemoveListEditor.this.removeButton = null;
          AddRemoveListEditor.this.buttonBox = null;
        } } );
    }
    else {
      checkParent(this.buttonBox, parent);
    }

    selectionChanged();
    return this.buttonBox;
  }

  private Button createPushButton(Composite parent, String text) {
    Button button = new Button(parent, 8);
    button.setText(text);
    button.setFont(parent.getFont());
    GridData data = new GridData(768);
    int widthHint = convertHorizontalDLUsToPixels(button, 61);
    data.widthHint = Math.max(widthHint, button.computeSize(-1, -1, true).x);
    button.setLayoutData(data);
    button.addSelectionListener(getSelectionListener());
    return button;
  }

  public void createListeners() {
    this.selectionListener = new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent event) {
        Widget widget = event.widget;
        if (widget == AddRemoveListEditor.this.addButton)
          AddRemoveListEditor.this.addPressed();
        else if (widget == AddRemoveListEditor.this.removeButton)
          AddRemoveListEditor.this.removePressed();
        else if (widget == AddRemoveListEditor.this.list)
          AddRemoveListEditor.this.selectionChanged();
      }
    };
  }

  public List getListControl(Composite parent) {
    if (this.list == null) {
      this.list = new List(parent, 2820);
      this.list.setFont(parent.getFont());
      this.list.addSelectionListener(getSelectionListener());
      this.list.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent event) {
          AddRemoveListEditor.this.list = null;
        } } );
    } else {
      checkParent(this.list, parent);
    }
    return this.list;
  }

  private SelectionListener getSelectionListener() {
    if (this.selectionListener == null) {
      createListeners();
    }
    return this.selectionListener;
  }

  private void removePressed() {
    setPresentsDefaultValue(false);
    int index = this.list.getSelectionIndex();
    if (index >= 0) {
      String item = this.list.getItem(index);
      this.delta.addRemove(item);

      this.list.remove(index);
      this.addText.setText(item);
      this.addText.setFocus();
      selectionChanged();
    }
  }

  private void selectionChanged() {
    this.removeButton.setEnabled(this.list.getSelectionIndex() >= 0);
  }

  public void setFocus()
  {
    if (this.list != null)
      this.list.setFocus();
  }

  public void setEnabled(boolean enabled, Composite parent)
  {
    super.setEnabled(enabled, parent);
    getListControl(parent).setEnabled(enabled);
    this.addButton.setEnabled(enabled);
    this.removeButton.setEnabled(enabled);
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.preferences.AddRemoveListEditor
 * JD-Core Version:    0.6.0
 */