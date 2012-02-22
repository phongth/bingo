package development;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import state.GameGlobal;

class FrmContactList extends List implements CommandListener {
  private Command selectCommand, backCommand;
  private FrmSendGame parent;
  private FrmContactnumbers contactnumbers;
  private Display display;

  public FrmContactList(FrmSendGame parent) {
    super("Select Memory", Choice.IMPLICIT);
    this.display = Display.getDisplay(GameGlobal.getMidlet());
    this.parent = parent;
    selectCommand = new Command("Select", Command.OK, 0);
    backCommand = new Command("Back", Command.BACK, 1);
    addCommand(backCommand);
    setSelectCommand(selectCommand);
    setCommandListener(this);
    setFitPolicy(Choice.TEXT_WRAP_ON);
  }

  public void commandAction(Command cmd, Displayable displayable) {
    if (cmd == selectCommand) {
      final int selected = getSelectedIndex();
      if (selected >= 0) {
          contactnumbers = new FrmContactnumbers(parent);
          new Thread() {
            public void run() {
              try {
                contactnumbers.loadNames(getString(selected));
              } catch (PIMException e) {
                parent.showMessage(e.getMessage(), FrmContactList.this);
              } catch (SecurityException e) {
                parent.showMessage(e.getMessage(), FrmContactList.this);
              }
            }
          }.start();
          display.setCurrent(contactnumbers);
      } else {
        parent.showMain();
      }
    } else if (cmd == backCommand) {
      parent.showMain();
    }
  }

  private void displaycontactnames(String contactname) {
    append(contactname, null);
  }

  public void LoadContacts() {
    try {
      String[] allContactLists = PIM.getInstance().listPIMLists(PIM.CONTACT_LIST);
      if (allContactLists.length != 0) {
        for (int i = 0; i < allContactLists.length; i++) {
          displaycontactnames(allContactLists[i]);
        }
        addCommand(selectCommand);
      } else {
        append("No Contact lists available", null);
      }
    } catch (SecurityException e) {
      parent.showMessage(e.getMessage(), FrmContactList.this);
    }
  }
}