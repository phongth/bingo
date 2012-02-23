package zgame.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import zgame.bean.User;
import zgame.dao.stub.AuthenticateDaoStub;
import zgame.exception.DupplicateException;
import zgame.exception.NotFoundException;

public abstract class AuthenticateDao extends AbstractDao {
  public static final Logger log = Logger.getLogger(AuthenticateDao.class);

  public static final String USER_INFO_TABLE = "User_Info";

  public static final String COLUMN_ID = "Id";
  public static final String COLUMN_USERNAME = "User_name";
  public static final String COLUMN_PASSWORD = "Password";
  public static final String COLUMN_FULL_NAME = "Full_Name";
  public static final String COLUMN_SEX = "Sex";
  public static final String COLUMN_AVATAR_ID = "Avatar_ID";
  public static final String COLUMN_BIRTHDAY = "Birthday";
  public static final String COLUMN_CMT = "CMT";
  public static final String COLUMN_ADDRESS = "Address";
  public static final String COLUMN_MONEY = "Money";
  public static final String COLUMN_EXPERIENCE = "Experience";
  public static final String COLUMN_LEVEL = "Level";
  public static final String COLUMN_SO_TRAN_THANG = "So_Tran_Thang";
  public static final String COLUMN_SO_TRAN_THUA = "So_Tran_Thua";
  public static final String COLUMN_TONG_SO_TRAN = "Tong_so_tran";
  public static final String COLUMN_CREATE_DATE = "Create_Date";
  public static final String COLUMN_PROVIDER_ID = "Provider_ID";
  public static final String COLUMN_PHONE_NUMBER = "Phone_Number";
  public static final String COLUMN_LAST_TIME_LOGIN = "Last_Time_Login";
  public static final String COLUMN_IS_BLOCKED = "IsBlocked";
  public static final String COLUMN_IS_DELETED = "IsDeleted";

  public static AuthenticateDao createInstance() {
    // return new AuthenticateDaoImpl();
    return new AuthenticateDaoStub();
  }

  public abstract User getUserInfo(String username) throws SQLException;

  public abstract User getUserInfo(int userId) throws SQLException;

  public abstract void createUser(String username, String passwordMd5, int money, int providerId) throws DupplicateException,
      SQLException;

  public abstract void changePassword(String username, String passwordMd5) throws NotFoundException, SQLException;

  public abstract boolean isExist(String username) throws SQLException;

  public abstract void commit();

  public abstract void close();
}
