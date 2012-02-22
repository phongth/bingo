package zgame.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Connection {
  private java.sql.Connection conn;
  private long createTime;
  private long startTransactionTime;
  private long lastTimeUse;
  private int useCount;
  
  public Connection() {
  }
  
  public Connection(java.sql.Connection conn) {
    this.conn = conn;
  }
  
  public long getCreateTime() {
    return createTime;
  }

  public Connection setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  public long getStartTransactionTime() {
    return startTransactionTime;
  }

  public Connection setStartTransactionTime(long startTransactionTime) {
    this.startTransactionTime = startTransactionTime;
    return this;
  }

  public int getUseCount() {
    return useCount;
  }
  
  public Connection setUseCount(int useCount) {
    this.useCount = useCount;
    return this;
  }
  
  public Connection increateUseCount() {
    this.useCount++;
    return this;
  }

  public boolean isClosed() throws SQLException {
    if (conn != null) {
      return conn.isClosed();
    }
    return true;
  }

  public Connection commit() throws SQLException {
    if (conn != null) {
      conn.commit();
    }
    lastTimeUse = System.currentTimeMillis();
    return this;
  }

  public Connection close() throws SQLException {
    if (conn != null) {
      conn.close();
    }
    lastTimeUse = System.currentTimeMillis();
    return this;
  }

  public long getLastTimeUse() {
    return lastTimeUse;
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException {
    lastTimeUse = System.currentTimeMillis();
    if (conn != null) {
      return conn.prepareStatement(sql);
    }
    return null;
  }
  
  public Connection rollback() throws SQLException {
    lastTimeUse = System.currentTimeMillis();
    if (conn != null) {
      conn.rollback();
    }
    return this;
  }
}
