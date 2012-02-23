package zgame.utils;

public abstract class Control extends Thread {
  public void run() {
    try {
      perform();
    } catch (RuntimeException ex) {
      ex.printStackTrace();
    }
  }

  public abstract void perform();
}
