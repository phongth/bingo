package zgame.jobs;

import org.apache.log4j.Logger;

import zgame.dao.Connection;
import zgame.dao.impl.ConnectionManager;
import zgame.main.Global;

public class CheckToReleaseTimeOutDBConnectionJob extends Job {
  private static final Logger log = Logger.getLogger(CheckToReleaseTimeOutDBConnectionJob.class);

  @Override
  protected void init() {
    log.info("CheckToReleaseTimeOutDBConnectionJob is started");
  }

  @Override
  public void loop() {
    long now = System.currentTimeMillis();
    for (Connection conn : ConnectionManager.onUseConnections) {
      if (now - conn.getLastTimeUse() > Global.DB_CONNECTION_TIME_OUT) {
        ConnectionManager.closeConnection(conn);
        log.info("Closed a DB connection, remain on use connection: " + ConnectionManager.onUseConnections.size());
      }
    }
  }
}
