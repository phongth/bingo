package zgame.job;

import org.apache.log4j.Logger;

public abstract class Job extends Thread {
  private static final Logger log = Logger.getLogger(Job.class);

  private boolean isRunning = true;
  private long delay = 100;

  protected void init() {
  }

  public Job setDelay(long delay) {
    this.delay = delay;
    return this;
  }

  public long getDelay() {
    return delay;
  }

  public abstract void loop();

  public void run() {
    init();
    while (isRunning) {
      try {
        loop();
        sleep(delay);
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception on runs Job", e);
        }
      }
    }
  }

  public void cancel() {
    isRunning = false;
  }
}
