package zgame.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import zgame.dao.stub.FriendDaoStub;

public abstract class FriendDao extends AbstractDao {
  public static final Logger log = Logger.getLogger(FriendDao.class);
  
  public static final String FRIEND_TABLE = "Friend";
  public static final String User_1_ID_COLUMN = "User_1_ID";
  public static final String User_2_ID_COLUMN = "User_2_ID";
  
  public static FriendDao createInstance() {
    return new FriendDaoStub();
  }
  
  public abstract List<String> getFriendList(int userId) throws SQLException;
  
  public abstract List<String> getBlockList(int userId) throws SQLException;
  
  public abstract void makeFriend(int userId1, int userId2) throws SQLException;
  
  public abstract void blockFriend(int fromUserId, int toUserId) throws SQLException;
  
  public abstract void removeFriend(int fromUserId, int toUserId) throws SQLException;
  
  public abstract void commit();
  
  public abstract void close();
}
