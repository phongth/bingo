package zgame.dao.stub;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.exception.DupplicateException;
import zgame.exception.NotFoundException;
import zgame.utils.MD5;

public class AuthenticateDaoStub extends AuthenticateDao {
	private static Map<String, User> userMockMap = new HashMap<String, User>(); // Map dùng để làm giả DB
	private static int idCreater = 0;
	
	public static int getNextId() {
	  idCreater++;
	  return idCreater;
	}
	
	static {
	  initMockDB();
	}
	
	private static void initMockDB() {
	  for (int i = 1; i <= 99; i++) {
      User user = new User();
      user.setUserId(getNextId());
      user.setUsername("w" + i);
      user.setMd5Pass(MD5.toBase64("123".getBytes()));
      user.setMoney(i * 1000); // Dư thừa dữ liệu
      user.setIsBlock('0');
      user.setAvatarId(i);
      userMockMap.put(user.getUsername(), user);
    }
    
    for (int i = 1; i <= 99; i++) {
      User user = new User();
      user.setUserId(getNextId());
      user.setUsername("a" + i);
      user.setMd5Pass(MD5.toBase64("123".getBytes()));
      user.setMoney(i * 1000); // Dư thừa dữ liệu
      user.setIsBlock('0');
      user.setAvatarId(i);
      userMockMap.put(user.getUsername(), user);
    }
	}
	
	public User getUserInfo(String username) {
	  return userMockMap.get(username);
	}
	
	@Override
  public User getUserInfo(int userId) throws SQLException {
    throw new UnsupportedOperationException("The method getUserInfo of class AuthenticateDao is not supported");
  }
	
  public void changePassword(String username, String passwordMd5) throws NotFoundException, SQLException {
    if (!isExist(username)) {
      throw new NotFoundException();
    }
    
    User user = getUserInfo(username);
    if (user == null) {
      throw new NotFoundException();
    }
    user.setMd5Pass(passwordMd5);
  }

  public void createUser(String username, String passwordMd5, int money, int providerId) throws DupplicateException, SQLException {
    if (isExist(username)) {
      throw new DupplicateException();
    }
    
    User user = new User();
    user.setUserId(getNextId());
    user.setUsername(username);
    user.setMd5Pass(MD5.toBase64("123".getBytes()));
    user.setProviderId(providerId);
    userMockMap.put(user.getUsername(), user);
  }
  
  @Override
  public boolean isExist(String username) throws SQLException {
    return (userMockMap.get(username) != null);
  }

  @Override
  public void close() {
  }

  @Override
  public void commit() {
  }
}
