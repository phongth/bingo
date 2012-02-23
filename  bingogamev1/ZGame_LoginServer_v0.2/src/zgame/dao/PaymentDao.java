package zgame.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import zgame.bean.User;
import zgame.dao.impl.PaymentDaoImpl;
import zgame.exception.NotFoundException;

public abstract class PaymentDao extends AbstractDao {
  public static final Logger log = Logger.getLogger(PaymentDao.class);

  public static PaymentDao createInstance() {
    return new PaymentDaoImpl();
  }

  public abstract void commitUserMoneyToDB(User user) throws SQLException, NotFoundException;

  public abstract int getUserMoneyFromDB(String username) throws SQLException, NotFoundException;

  public abstract void commit();

  public abstract void close();
}
