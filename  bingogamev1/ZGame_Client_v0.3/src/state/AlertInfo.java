package state;

import java.util.Vector;

public class AlertInfo {
  protected int alertType;
  protected Vector alertMessages = new Vector();
  protected int alertId;
  protected String[] buttonLabels;
  protected int alineType;
  protected int buttonWidth;
  protected Sprite[] customUISprites;
  protected int alertTimeOut = Integer.MAX_VALUE;
  protected AlertListener listenner;
  
  public void copyDataFrom(AlertInfo alertInfo) {
    this.alertType = alertInfo.alertType;
    this.alertId = alertInfo.alertId;
    this.buttonLabels = alertInfo.buttonLabels;
    this.alineType = alertInfo.alineType;
    this.buttonWidth = alertInfo.buttonWidth;
    this.customUISprites = alertInfo.customUISprites;
    this.alertTimeOut = alertInfo.alertTimeOut;
    this.listenner = alertInfo.listenner;
    
    this.alertMessages.removeAllElements();
    for (int i = 0; i < alertInfo.alertMessages.size(); i++) {
      this.alertMessages.addElement(alertInfo.alertMessages.elementAt(i));
    }
  }

  public String toString() {
    return "AlertInfo [alertId=" + alertId + ", alertMessages=" + alertMessages + ", alertTimeOut=" + alertTimeOut + ", alertType=" + alertType + ", alineType=" + alineType + ", buttonLabels="
        + ", buttonWidth=" + buttonWidth + ", customUISprites="  + ", listenner=" + listenner + "]";
  }
  
  public void detroy() {
    alertMessages.removeAllElements();
    buttonLabels = null;
    customUISprites = null;
  }
}
