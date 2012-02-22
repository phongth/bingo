package development;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

public class FrmContactnumbers extends Form implements CommandListener {
  private Command select, back;
  boolean available;
  private Vector allTelNumbers = new Vector();
  private ChoiceGroup contactnum;
  FrmSendGame parent;
  FrmContactList contactlistform;

  public FrmContactnumbers(FrmSendGame parent) {
    super("");
    this.parent = parent;
    contactnum = new ChoiceGroup("Contacts", ChoiceGroup.EXCLUSIVE);
    contactnum.deleteAll();
    append(contactnum);
    select = new Command("Submit", Command.BACK, 0);
    back = new Command("Back", Command.SCREEN, 1);
    addCommand(select);
    addCommand(back);
    setCommandListener(this);
  }

  public void commandAction(Command cmd, Displayable disp) {
    if (cmd == select) {
      int selected = contactnum.getSelectedIndex();
      if (selected >= 0) {
        parent.contactSelected((String) allTelNumbers.elementAt(selected));
      }
    } else if (cmd == back) {
      parent.showContactsList();
    }
  }

  public void loadNames(String name) throws PIMException, SecurityException {
    ContactList contactList = null;
    try {
      contactList = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY, name);
      if (contactList.isSupportedField(Contact.FORMATTED_NAME) && contactList.isSupportedField(Contact.TEL)) {
        Enumeration items = contactList.items();
        Vector telNumbers = new Vector();
        while (items.hasMoreElements()) {
          Contact contact = (Contact) items.nextElement();
          int telCount = contact.countValues(Contact.TEL);
          int nameCount = contact.countValues(Contact.FORMATTED_NAME);
          if (telCount > 0 && nameCount > 0) {
            String contactName = contact.getString(Contact.FORMATTED_NAME, 0);
            for (int i = 0; i < telCount; i++) {
              String telNumber = contact.getString(Contact.TEL, i);
              telNumbers.addElement(telNumber);
              allTelNumbers.addElement(telNumber);
            }
            for (int i = 0; i < telNumbers.size(); i++) {
              contactnum.append(contactName, null);
              contactnum.setSelectedIndex(0, true);
            }
            telNumbers.removeAllElements();
          }
        }
        available = true;
      } else {
        contactnum.append("Contact list required items not supported", null);
        available = false;
      }
    } finally {
      if (contactList != null) {
        contactList.close();
      }
    }
  }
}