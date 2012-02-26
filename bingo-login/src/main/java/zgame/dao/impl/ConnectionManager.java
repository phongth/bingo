package zgame.dao.impl;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import zgame.dao.Connection;
import zgame.main.Global;

public class ConnectionManager {
  private static Logger log = Logger.getLogger(ConnectionManager.class);
  private static Properties connectProp;

  public static Vector<Connection> availableConnections = new Vector<Connection>();
  public static Vector<Connection> onUseConnections = new Vector<Connection>();

  public static void init() {
    connectProp = new Properties();
    connectProp.setProperty("user", Global.DB_USER);
    connectProp.setProperty("password", Global.DB_PASSWORD);
  }

  public static Connection getConnection() {
    Connection conn;
    if (availableConnections.size() > 0) {
      conn = availableConnections.get(0).setStartTransactionTime(System.currentTimeMillis()).increateUseCount();
      availableConnections.remove(0);
    } else {
      conn = createConnection().setCreateTime(System.currentTimeMillis()).setStartTransactionTime(System.currentTimeMillis())
          .setUseCount(1);
    }
    onUseConnections.add(conn);
    return conn;
  }

  private static Connection createConnection() {
    try {
      Class.forName(Global.DB_DRIVER);
      java.sql.Connection conn = DriverManager.getConnection(Global.DB_URL, connectProp);
      conn.setAutoCommit(false);
      return new zgame.dao.Connection(conn);
    } catch (SQLException sqle) {
      log.error("Can not create the connection to DB", sqle);
    } catch (ClassNotFoundException cnfe) {
      log.error("Can not find the library ojdbc14.jar", cnfe);
    }
    return null;
  }

  private static void checkToStoreConnection(Connection conn) {
    try {
      if (conn != null && !conn.isClosed()) {
        conn.commit();
        onUseConnections.remove(conn);
        if (availableConnections.size() + onUseConnections.size() < Global.DB_CONNECTION_MAX_POOL) {
          availableConnections.add(conn);
        } else {
          conn.close();
        }
      }
    } catch (SQLException sqle) {
    }
  }

  public static void closeAll(PreparedStatement ps, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException sqle) {
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (SQLException sqle) {
      }
    }
  }

  public static void closeConnection(Connection conn) {
    checkToStoreConnection(conn);
  }
}