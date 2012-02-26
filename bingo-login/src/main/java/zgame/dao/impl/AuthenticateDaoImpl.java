package zgame.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.dao.Connection;
import zgame.exception.DupplicateException;
import zgame.exception.NotFoundException;

public class AuthenticateDaoImpl extends AuthenticateDao {
  private Connection conn;

  public AuthenticateDaoImpl() {
    conn = ConnectionManager.getConnection();
  }

  public void changePassword(String username, String passwordMd5) throws NotFoundException, SQLException {
    if (!isExist(username)) {
      throw new NotFoundException();
    }

    String sql = "UPDATE " + USER_INFO_TABLE + " SET Password = ? WHERE User_name = ? AND IsDeleted = '0'";
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql);
      ps.setString(1, passwordMd5);
      ps.setString(2, username);

      ps.executeUpdate();
    } catch (SQLException e) {
      conn.rollback();
      log.warn("Can not change password: " + username, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, null);
    }
  }

  public void createUser(String username, String passwordMd5, int money, int providerId) throws DupplicateException, SQLException {
    if (isExist(username)) {
      throw new DupplicateException();
    }

    // TODO: use BufferString in DAO function
    String sql = "INSERT INTO " + USER_INFO_TABLE
        + "(User_name, Password, Avatar_ID, Money, Create_Date, Provider_ID) VALUES (?, ?, ?, ?, ?, ?)";
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql);
      ps.setString(1, username);
      ps.setString(2, passwordMd5);
      ps.setInt(3, 1);
      ps.setInt(4, money);
      ps.setDate(5, new java.sql.Date(new Date().getTime()));
      ps.setInt(6, providerId);

      ps.executeUpdate();
    } catch (SQLException e) {
      conn.rollback();
      log.warn("Can not create user: " + username, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, null);
    }
  }

  public User getUserInfo(String username) throws SQLException {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ").append(COLUMN_ID + ",").append(COLUMN_PASSWORD + ",").append(COLUMN_AVATAR_ID + ",").append(
        COLUMN_MONEY + ",").append(COLUMN_PROVIDER_ID + ",").append(COLUMN_IS_BLOCKED);
    sql.append(" FROM ").append(USER_INFO_TABLE);
    sql.append(" WHERE ").append(COLUMN_USERNAME + " = ?").append(" AND ").append(COLUMN_IS_DELETED + " = '0'");

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setString(1, username);

      rs = ps.executeQuery();
      if (rs.next()) {
        User user = new User();
        user.setUserId(rs.getInt(COLUMN_ID));
        user.setUsername(username);
        user.setMd5Pass(rs.getString(COLUMN_PASSWORD));
        user.setAvatarId(rs.getInt(COLUMN_AVATAR_ID));
        user.setMoney(rs.getInt(COLUMN_MONEY));
        user.setProviderId(rs.getInt(COLUMN_PROVIDER_ID));
        user.setIsBlock(rs.getString(COLUMN_IS_BLOCKED).charAt(0));
        return user;
      }
      return null;
    } catch (SQLException e) {
      log.warn("Can not get user info: " + username, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, rs);
    }
  }

  @Override
  public User getUserInfo(int userId) throws SQLException {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ").append(COLUMN_USERNAME + ",").append(COLUMN_PASSWORD + ",").append(COLUMN_AVATAR_ID + ",").append(
        COLUMN_MONEY + ",").append(COLUMN_PROVIDER_ID + ",").append(COLUMN_IS_BLOCKED);
    sql.append(" FROM ").append(USER_INFO_TABLE);
    sql.append(" WHERE ").append(COLUMN_ID + " = ?").append(" AND ").append(COLUMN_IS_DELETED + " = '0'");

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setInt(1, userId);

      rs = ps.executeQuery();
      if (rs.next()) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(rs.getString(COLUMN_USERNAME));
        user.setMd5Pass(rs.getString(COLUMN_PASSWORD));
        user.setAvatarId(rs.getInt(COLUMN_AVATAR_ID));
        user.setMoney(rs.getInt(COLUMN_MONEY));
        user.setProviderId(rs.getInt(COLUMN_PROVIDER_ID));
        user.setIsBlock(rs.getString(COLUMN_IS_BLOCKED).charAt(0));
        return user;
      }
      return null;
    } catch (SQLException e) {
      log.warn("Can not get user info of userId: " + userId, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, rs);
    }
  }

  public boolean isExist(String username) throws SQLException {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT count(Id) FROM ");
    sql.append(USER_INFO_TABLE);
    sql.append(" WHERE User_name = ? AND IsDeleted = '0'");

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setString(1, username);

      rs = ps.executeQuery();
      if (rs.next()) {
        int count = rs.getInt(1);
        if (count > 0) {
          return true;
        }
      }
      return false;
    } catch (SQLException e) {
      log.warn("Can not check user exist: " + username, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, rs);
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
