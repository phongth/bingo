package zgame.jobs;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import zgame.bean.User;
import zgame.dao.PaymentDao;
import zgame.exception.NotFoundException;
import zgame.main.Global;

public class CommitPaymentDataJob extends Job {
  private static final Logger log = Logger.getLogger(CommitPaymentDataJob.class);
  private Hashtable<String, String> userToCommitQueue = new Hashtable<String, String>();

  @Override
  protected void init() {
    log.info("CommitPaymentDataJob is started");
  }

  @Override
  public void loop() {
    Set<String> keys = userToCommitQueue.keySet();

    if (keys.size() == 0) {
      return;
    }

    Vector<String> toCommitList = new Vector<String>();
    for (String username : keys) {
      toCommitList.add(username);
      userToCommitQueue.remove(username);
    }

    log.info("Start commit payment loopId: " + loopCount);
    PaymentDao paymentDao = PaymentDao.createInstance();
    for (String username : toCommitList) {
      User user = Global.userInfoCache.get(username);
      try {
        paymentDao.commitUserMoneyToDB(user);
      } catch (SQLException e) {
        log.warn("Fail to update money to DB for user: " + user.getUsername(), e);
      } catch (NotFoundException e) {
        log.warn("User is not found when trying to update money to DB for user: " + user.getUsername(), e);
      }
    }
    paymentDao.commit();
    paymentDao.close();
    log.info("End commit payment loopId: " + loopCount);
  }

  public void putUserToCommit(String username) {
    userToCommitQueue.put(username, username);
  }
}
