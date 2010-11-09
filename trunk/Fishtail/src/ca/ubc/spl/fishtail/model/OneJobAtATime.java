package ca.ubc.spl.fishtail.model;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class OneJobAtATime extends Job
{
  private static Map<Type, OneJobAtATime> jobs = new HashMap();
  protected final Type jobType;

  public OneJobAtATime(Type jobType)
  {
    super(jobType.name);
    this.jobType = jobType;
  }

  protected OneJobAtATime getCurrentRunningJob(Type type) {
    return (OneJobAtATime)jobs.get(type);
  }

  private boolean getLock() {
    synchronized (jobs) {
      if ((getCurrentRunningJob(this.jobType) == null) || (getCurrentRunningJob(this.jobType) == this)) {
        jobs.put(this.jobType, this);
        return true;
      }
      return false;
    }
  }

  private void overrideLockAndCancel()
  {
    synchronized (jobs) {
      OneJobAtATime j = getCurrentRunningJob(this.jobType);
      if ((j != null) && (j != this)) {
        j.cancel();
        jobs.put(this.jobType, this);
      }
    }
  }

  private void releaseLock() {
    synchronized (jobs) {
      if (getCurrentRunningJob(this.jobType) == this)
        jobs.remove(this.jobType); 
    }
  }

  protected final IStatus run(IProgressMonitor monitor) {
    boolean lock = getLock();
    boolean cancel;
    try {
      cancel = shouldCancelCurrentlyRunningJob();
    }
    catch (Exception localException)
    {
      cancel = false;
    }

    if ((!lock) && (cancel)) {
      overrideLockAndCancel();
      lock = true;
    }
    if (lock) {
      try {
        IStatus ret = runJob(monitor);
        IStatus localIStatus1 = ret;
        return localIStatus1;
      } finally {
        releaseLock();
      }
    }
    return Status.OK_STATUS;
  }

  protected abstract boolean shouldCancelCurrentlyRunningJob();

  protected abstract IStatus runJob(IProgressMonitor paramIProgressMonitor);

  public static enum Type
  {
    Rerank("Fishtail Reranking"), Search("Fishtail Search");

    private String name;

    private Type(String name) { this.name = name; }

    public String getName()
    {
      return this.name;
    }
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.OneJobAtATime
 * JD-Core Version:    0.6.0
 */