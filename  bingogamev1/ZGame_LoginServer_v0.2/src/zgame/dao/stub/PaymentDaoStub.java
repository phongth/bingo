package zgame.dao.stub;

import zgame.bean.User;
import zgame.dao.PaymentDao;

public class PaymentDaoStub extends PaymentDao {
  @Override
  public int getUserMoneyFromDB(String username) {
    throw new UnsupportedOperationException("The method getUserMoneyFromDB is not supported");
  }

  @Override
  public void commitUserMoneyToDB(User user) {
    throw new UnsupportedOperationException("The method updateUserMoneyToDB is not supported");
  }
  
  @Override
  public void close() {
  }

  @Override
  public void commit() {
  }
}
