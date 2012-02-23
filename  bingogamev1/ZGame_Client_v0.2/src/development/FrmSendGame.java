package development;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import state.GameGlobal;
import state.SMSHandler;
import state.SendSMSListener;

public class FrmSendGame extends Form implements CommandListener,
		ItemCommandListener {
	private Display display;
	private TextField toWhom;
	private TextField message;
	private Alert alert;
	FrmContactList contactlistform;
	private StringItem send;
	private Command select, back, add;

	private SendSMSListener listener;

	public FrmSendGame(SendSMSListener listener) {
		super("Moi choi game");
		this.listener = listener;

		toWhom = new TextField("To", "", 13, TextField.PHONENUMBER);
		message = new TextField("Message", "", 160, TextField.ANY);
		message.setString("Chơi thử game này hay lắm");

		select = new Command("Select", Command.OK, 0);
		send = new StringItem("", "Send", StringItem.BUTTON);
		back = new Command("Back", Command.EXIT, 2);
		add = new Command("Add", Command.OK, 0);
		append(toWhom);
		append(message);
		append(send);
		toWhom.setDefaultCommand(add);
		send.setDefaultCommand(select);
		addCommand(back);
		toWhom.setItemCommandListener(this);
		send.setItemCommandListener(this);
		setCommandListener(this);

		display = Display.getDisplay(GameGlobal.getMidlet());
	}

	void showMessage(String message, Displayable displayable) {
		Alert alert = new Alert("");
		alert.setTitle("Error");
		alert.setString(message);
		alert.setType(AlertType.ERROR);
		alert.setTimeout(5000);
		display.setCurrent(alert, displayable);
	}

	void showMain() {
		display.setCurrent(this);
	}

	void showContactsList() {
		contactlistform = new FrmContactList(this);
		new Thread() {
			public void run() {
				contactlistform.LoadContacts();
			}
		}.start();
		display.setCurrent(contactlistform);
	}

	void contactSelected(String telephoneNumber) {
		this.setPhoneNumber(telephoneNumber);
		showMain();
	}

	void setPhoneNumber(String phoneNumber) {
		toWhom.setString(phoneNumber);
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == back && disp == this) {
			display.setCurrent(GameGlobal.systemCanvas);
			GameGlobal.systemCanvas.setFullScreenMode(true);
		}
	}

	public void commandAction(Command cmd, Item item) {
		if (cmd == add && item == toWhom) {
			showContactsList();
		}

		if (cmd == select && item == send) {
			String mno = toWhom.getString();
			String msg = message.getString();
			if (mno.equals("") || msg.equals("")) {
				if (mno.equals("")) {
					alert = new Alert("Alert");
					alert.setString("Enter Mobile Number!!!");
					alert.setTimeout(2000);
					display.setCurrent(alert);
				} else {
					alert = new Alert("Alert");
					alert.setString("Enter Message!!!");
					alert.setTimeout(2000);
					display.setCurrent(alert);
				}
			} else {
				SMSHandler.getInstance(listener).sendMsg(mno, msg);
			}
		}
	}
}