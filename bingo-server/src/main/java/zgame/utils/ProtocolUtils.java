package zgame.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

public class ProtocolUtils {
  private static final Logger log = Logger.getLogger(ProtocolUtils.class);

  public static void toByteArray(Vector<String[]> v, DataOutputStream output) throws IOException {
    // Write vector's size
    output.writeInt(v.size());

    // Write String[]'s size
    output.writeInt(v.get(0).length);

    Enumeration<String[]> e = v.elements();
    String[] strArr = null;
    byte[] data = null;

    while (e.hasMoreElements()) {
      strArr = e.nextElement();

      for (int i = 0; i < strArr.length; i++) {
        data = strArr[i].getBytes(Constants.UTF8);
        output.writeInt(data.length);
        output.write(data);
      }
    }

    output.flush();
  }

  public static Vector<String[]> getVector(DataInputStream input) throws IOException {
    int sizeOfVector = input.readInt();
    int arrayLength = input.readInt();

    Vector<String[]> vector = new Vector<String[]>(sizeOfVector);

    String[] str = new String[arrayLength];
    int sizeOfMessage = 0;
    byte[] messages = null;

    while (sizeOfVector-- > 0) {
      for (int i = 0; i < arrayLength; i++) {
        sizeOfMessage = input.readInt();
        messages = new byte[sizeOfMessage];
        input.readFully(messages);
        str[i] = new String(messages, Constants.UTF8);
      }

      vector.addElement(str);
    }

    return vector;
  }

  public static String convertDate(String time) {
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
    Date date = null;
    try {
      date = sdf.parse(time);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      sdf.applyPattern("dd/MM/yy HH:mm");

      return sdf.format(cal.getTime()).replace(":", "h");
    } catch (ParseException e) {
      log.warn("convertDate : Exception on converting date", e);
      return "";
    }
  }
}
