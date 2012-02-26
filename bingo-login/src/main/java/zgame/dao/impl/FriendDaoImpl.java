package zgame.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import zgame.bussiness.AuthenticateBussiness;
import zgame.dao.Connection;
import zgame.dao.FriendDao;

public class FriendDaoImpl extends FriendDao {
  private Connection conn;

  public FriendDaoImpl() {
    conn = ConnectionManager.getConnection();
  }

  public List<String> getFriendList(int userId) throws SQLException {
    List<String> friendList = new ArrayList<String>();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT * ");
    sql.append(" FROM ").append(FRIEND_TABLE);
    sql.append(" WHERE ").append(User_1_ID_COLUMN + " = ?").append(" OR ").append(User_2_ID_COLUMN + " = ?");

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setInt(1, userId);
      ps.setInt(2, userId);

      rs = ps.executeQuery();
      while (rs.next()) {
        int userId1 = rs.getInt(User_1_ID_COLUMN);
        int userId2 = rs.getInt(User_2_ID_COLUMN);
        if (userId1 == userId) {
          friendList.add(AuthenticateBussiness.getUsernameByUserId(userId2));
        } else {
          friendList.add(AuthenticateBussiness.getUsernameByUserId(userId1));
        }
      }
      return friendList;
    } catch (SQLException e) {
      log.warn("Can not get friend list of user: " + userId, e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, rs);
    }
  }

  public void makeFriend(int userId1, int userId2) throws SQLException {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO ");
    sql.append(FRIEND_TABLE);
    sql.append(" (").append(User_1_ID_COLUMN).append(",").append(User_2_ID_COLUMN).append(")");
    sql.append(" VALUES ").append("(?, ?)");

    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql.toString());
      ps.setInt(1, userId1);
      ps.setInt(2, userId2);

      ps.executeUpdate();
    } catch (SQLException e) {
      conn.rollback();
      log.warn("Can not create friend for: " + AuthenticateBussiness.getUsernameByUserId(userId1) + " and "
          + AuthenticateBussiness.getUsernameByUserId(userId2), e);
      throw e;
    } finally {
      ConnectionManager.closeAll(ps, null);
    }
  }

  @Override
  public void blockFriend(int fromUserId, int toUserId) throws SQLException {
    throw new UnsupportedOperationException("The method blockFriend of class FriendDao is not supported");
  }

  @Override
  public List<String> getBlockList(int userId) throws SQLException {
    throw new UnsupportedOperationException("The method getBlockList of class FriendDao is not supported");
  }

  @Override
  public void removeFriend(int fromUserId, int toUserId) throws SQLException {
    throw new UnsupportedOperationException("The method removeFriend of class FriendDao is not supported");
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
