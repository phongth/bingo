package zgame.bussiness;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.exception.NotEnoughMoneyException;
import zgame.exception.NotFoundException;
import zgame.main.Global;

public class PaymentBussiness {
  private static final Logger log = Logger.getLogger(PaymentBussiness.class);

  public static void withdrawMoney(String username, int money) throws NotFoundException, NotEnoughMoneyException, SQLException {
    if (money < 0) {
      throw new IllegalArgumentException("Money can not be negative");
    }

    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    User user = AuthenticateBussiness.getUserInfo(username, authenticateDao);
    if (user == null) {
      throw new NotFoundException();
    }

    if (user.getMoney() < money) {
      throw new NotEnoughMoneyException();
    }
    user.setMoney(user.getMoney() - money);

    log.info("Withdraw " + money + " coin from user: " + username);

    Global.commitPaymentDataJob.putUserToCommit(username);
    authenticateDao.close();
  }

  public static void depositMoney(String username, int money) throws NotFoundException, SQLException {
    if (money < 0) {
      throw new IllegalArgumentException("Money can not be negative");
    }

    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    User user = AuthenticateBussiness.getUserInfo(username, authenticateDao);
    if (user == null) {
      throw new NotFoundException();
    }

    // Tính trừ phế
    money = money - (int) (money * 1.0 * Global.TAX_PERCENT / 100);
    user.setMoney(user.getMoney() + money);

    log.info("Deposit " + money + " coin to user: " + username);

    Global.commitPaymentDataJob.putUserToCommit(username);
    authenticateDao.close();
  }

  public static void transferMoney(String fromUser, String toUser, int money) throws NotFoundException, NotEnoughMoneyException,
      SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();

    if (!AuthenticateBussiness.isExist(fromUser, authenticateDao) || !AuthenticateBussiness.isExist(toUser, authenticateDao)) {
      throw new NotFoundException();
    }
    withdrawMoney(fromUser, money);
    depositMoney(toUser, money);

    authenticateDao.close();
  }
}
