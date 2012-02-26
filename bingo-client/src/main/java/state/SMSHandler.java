package state;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

public class SMSHandler implements Runnable {
	private static SMSHandler inst = new SMSHandler();
	private static SendSMSListener listener;

	private SMSHandler() {
	}

	public static SMSHandler getInstance(SendSMSListener listener) {
		SMSHandler.listener = listener;
		return inst;
	}

	private String mReceiver = null;
	private String mPort = null;
	private String msgString = null;
	private boolean mSending = false;

	public void sendMsg(String callNumber, String messageText) {
		sendMsg(callNumber, null, messageText);
	}

	public void sendMsg(String callNumber, String port, String messageText) {
		if (mSending) {
			return;
		}
		mReceiver = callNumber;
		mPort = port;
		msgString = messageText;
		Thread th = new Thread(this);
		th.start();
	}

	public boolean isSending() {
		return mSending;
	}

	// Send the color message
	public void run() {
		mSending = true;
		try {
			sendSMS();
			if (listener != null) {
				listener.onSendSMSSuccess();
			}
		} catch (IOException e) {
			if (listener != null) {
				listener.onSendSMSFail();
			}
		}
		mSending = false;
	}

	private void sendSMS() throws IOException {
		StringBuffer address = new StringBuffer();
		address.append("sms://");
		address.append(mReceiver);
		if (mPort != null) {
			address.append(":");
			address.append(mPort);
		}

		MessageConnection conn = null;
		conn = (MessageConnection) Connector.open(address.toString());
		TextMessage txtmessage = (TextMessage) conn
				.newMessage(MessageConnection.TEXT_MESSAGE);
		txtmessage.setPayloadText(msgString);
		conn.send(txtmessage);

		if (conn != null) {
			conn.close();
		}
	}
}
