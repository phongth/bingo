package zgame.bussiness;

import org.apache.log4j.Logger;

import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.client.ClientConnection;

public class RegistGameServiceBussiness {
  private static final Logger log = Logger.getLogger(RegistGameServiceBussiness.class);

  public static void gameServiceInfoRequest(ClientConnection client, DataPackage inputDataPackage) {
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.GAME_SERVICE_INFO_RESPONSE);
    dataPackage.putString(Global.GAME_SERVICE_ID);
    dataPackage.putString(Global.GAME_SERVICE_NAME);
    Global.client.write(dataPackage);
  }

  public static void gameServiceRegistSuccess(ClientConnection client, DataPackage inputDataPackage) {
    log.info("Regist game service success");
    Global.TIME_OUT = inputDataPackage.nextInt();
    Global.DATA_UPDATE_SEQUENCE_TIME = inputDataPackage.nextInt();
    Global.serverListener.start();
  }

  public static void gameServiceRegistFail(ClientConnection client, DataPackage inputDataPackage) {
    log.info("ERROR: REGIST GAME SERVICE FAIL");
  }
}
