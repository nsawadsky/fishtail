package ca.ubc.spl.fishtail;

import org.eclipse.core.runtime.IStatus;

/**
 * Encapsulates Fishtail error statuses for logging.
 */
public class FishtailStatus implements IStatus {
  private String message;
  private Throwable except;
  
  // Create a new status object with the given message.
  public FishtailStatus(String message) {
    this.message = message;
  }
  
  // Create a new status object with the given message and exception.
  public FishtailStatus(String message, Throwable e) {
    this.message = message;
    except = e;
  }

  @Override
  public IStatus[] getChildren() {
    return new IStatus[]{};
  }

  @Override
  public int getCode() {
    return 0;
  }

  @Override
  public Throwable getException() {
    return except;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getPlugin() {
    return FishtailPlugin.getDefault().getDescriptor().getUniqueIdentifier();
  }

  @Override
  public int getSeverity() {
    return IStatus.ERROR;
  }

  @Override
  public boolean isMultiStatus() {
    return false;
  }

  @Override
  public boolean isOK() {
    return false;
  }

  @Override
  public boolean matches(int mask) {
    return (mask & IStatus.ERROR) != 0;
  }

}
