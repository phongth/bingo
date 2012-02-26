package zgame.dao;

public abstract class AbstractDao {
  public abstract void commit();

  public abstract void close();
}
