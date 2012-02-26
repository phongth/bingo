package zgame.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.dao.Connection;
import zgame.dao.PaymentDao;
import zgame.exception.NotFoundException;

public class PaymentDaoImpl extends PaymentDao {
  private Connection conn;

  public PaymentDaoImpl() {
    conn = ConnectionManager.getConnection();
  }

  @Override
  public int getUserMoneyFromDB(String username) throws SQLException, NotFoundException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    if (!authenticateDao.isExist(username)) {
      throw new NotFoundException();
    }
    authenticateDao.close();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ").append(AuthenticateDao.COLUMN_MONEY);
    sql.append(" FROM ").append(AuthenticateDao.USER_INFO_TABLE);
    sql.append(" WHERE ").append(AuthenticateDao.COLUMN_USERNAME + " = ?");
    sql.append(" AND ").append(AuthenticateDao.COLUMN_IS_DELETED + " = '0'");

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setString(1, username);

      rs = ps.executeQuery();
      if (!rs.next()) {
        throw new NotFoundException();
      }
      return rs.getInt(AuthenticateDao.COLUMN_MONEY);
    } catch (SQLException e) {
      log.warn("Can not get money of user: " + username, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, rs);
    }
  }

  @Override
  public void commitUserMoneyToDB(User user) throws SQLException, NotFoundException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    if (!authenticateDao.isExist(user.getUsername())) {
      throw new NotFoundException();
    }
    authenticateDao.close();

    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE ").append(AuthenticateDao.USER_INFO_TABLE);
    sql.append(" SET ").append(AuthenticateDao.COLUMN_MONEY + " = ? ");
    sql.append(" WHERE ").append(AuthenticateDao.COLUMN_USERNAME + " = ?");
    sql.append(" AND ").append(AuthenticateDao.COLUMN_IS_DELETED + " = '0'");

    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setInt(1, user.getMoney());
      ps.setString(2, user.getUsername());

      ps.executeUpdate();
    } catch (SQLException e) {
      conn.rollback();
      log.warn("Can not commit money of user: " + user.getUsername(), e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, null);
    }
  }

  @Override
  public void close() {
    ConnectionManager.closeConnection(conn);
  }

  @Override
  public void commit() {
    try {
      conn.commit();
    } catch (SQLException e) {
    }
  }
}
