package zgame.dao.stub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zgame.dao.FriendDao;

public class FriendDaoStub extends FriendDao {
  private static Map<String, Map<String, String>> friendTable = new HashMap<String, Map<String, String>>();
  private static Map<String, Map<String, String>> blockListTable = new HashMap<String, Map<String, String>>();
  
  public List<String> getFriendList(int userId) {
    List<String> friendList = new ArrayList<String>();
    Map<String, String> friends = friendTable.get(String.valueOf(userId));
    if (friends == null) {
      return friendList;
    }
    
    friendList.addAll(friends.keySet());
    return friendList;
  }
  
  public List<String> getBlockList(int userId) {
    List<String> blockList = new ArrayList<String>();
    
    Map<String, String> blockMap = blockListTable.get(String.valueOf(userId));
    if (blockMap == null) {
      return blockList;
    }
    
    Set<String> userKeys = blockMap.keySet();
    for(String userKey : userKeys) {
      blockList.add(userKey);
    }
    return blockList;
  }
  
  public void makeFriend(int userId1, int userId2) {
    Map<String, String> friends1 = friendTable.get(String.valueOf(userId1));
    if (friends1 == null) {
      friends1 = new Hashtable<String, String>();
      friendTable.put(String.valueOf(userId1), friends1);
    }
    friends1.put(String.valueOf(userId2), String.valueOf(userId2));
    
    Map<String, String> friends2 = friendTable.get(String.valueOf(userId2));
    if (friends2 == null) {
      friends2 = new Hashtable<String, String>();
      friendTable.put(String.valueOf(userId2), friends2);
    }
    friends2.put(String.valueOf(userId1), String.valueOf(userId1));
  }
  
  public void blockFriend(int fromUserId, int toUserId) {
    Map<String, String> friends1 = blockListTable.get(String.valueOf(fromUserId));
    if (friends1 == null) {
      friends1 = new Hashtable<String, String>();
      blockListTable.put(String.valueOf(fromUserId), friends1);
    }
    friends1.put(String.valueOf(toUserId), String.valueOf(toUserId));
  }
  
  public void removeFriend(int fromUserId, int toUserId) {
    Map<String, String> friends = friendTable.get(String.valueOf(fromUserId));
    if (friends == null) {
      friends = new Hashtable<String, String>();
      friendTable.put(String.valueOf(fromUserId), friends);
    }
    friends.remove(String.valueOf(toUserId));
  }

  @Override
  public void close() {
  }

  @Override
  public void commit() {
  }
}
