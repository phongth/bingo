package zgame.exception;

public class DupplicateException extends Exception {
  private static final long serialVersionUID = 1L;

  public DupplicateException() {
    super();
  }

  public DupplicateException(String message) {
    super(message);
  }
}
