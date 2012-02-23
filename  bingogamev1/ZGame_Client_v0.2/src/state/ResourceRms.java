package state;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import state.util.SerialUtil;

public class ResourceRms {
	public final static String MASTER_RMS = "master";
	public final static String RESOURCE_RMS = "resource";

	private static RecordStore resourceRecordStore;

	public static Hashtable loadMasterRecord() {
		Hashtable hashtable = new Hashtable();
		RecordStore masterRecordStore = null;
		try {
			masterRecordStore = RecordStore.openRecordStore(MASTER_RMS, true);
			if (masterRecordStore.getNumRecords() == 0) {
				saveMasterRecord(hashtable);
				return hashtable;
			}

			byte[] data = masterRecordStore.getRecord(1);
			int index = 0;
			while (index < data.length - 1) {
				int len = SerialUtil.getInt(data, index);
				index += 4;
				String key = SerialUtil.getString(data, index, len);
				index += len;
				int value = SerialUtil.getInt(data, index);
				index += 4;
				hashtable.put(key, new Integer(value));
			}
			return hashtable;
		} catch (Exception e) {
			return null;
		} finally {
			if (masterRecordStore != null) {
				try {
					masterRecordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e) {
				} catch (RecordStoreException e) {
				}
			}
		}
	}

	public static void saveMasterRecord(Hashtable hashtable) {
		int len = 0;
		Enumeration e = hashtable.keys();
		while (e.hasMoreElements()) {
			String key = String.valueOf(e.nextElement());
			len += (4 + key.getBytes().length);
			len += 4; // 4 byte dành cho value
		}

		byte[] data = new byte[len];
		e = hashtable.keys();
		int index = 0;
		while (e.hasMoreElements()) {
			String key = String.valueOf(e.nextElement());
			byte[] bytes = key.getBytes();
			System.arraycopy(SerialUtil.serialNumber(bytes.length), 0, data,
					index, 4);
			index += 4;
			System.arraycopy(bytes, 0, data, index, bytes.length);
			index += bytes.length;
			int value = ((Integer) hashtable.get(key)).intValue();
			System.arraycopy(SerialUtil.serialNumber(value), 0, data, index, 4);
			index += 4;
		}

		RecordStore masterRecordStore = null;
		try {
			RecordStore.deleteRecordStore(MASTER_RMS);
			masterRecordStore = RecordStore.openRecordStore(MASTER_RMS, true);
			masterRecordStore.addRecord(data, 0, data.length);
		} catch (Exception e1) {
		} finally {
			if (masterRecordStore != null) {
				try {
					masterRecordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
	}

	public static Image getImage(int recordIndex) {
		try {
			if (resourceRecordStore == null) {
				resourceRecordStore = RecordStore.openRecordStore(RESOURCE_RMS,
						true);
			}
			if (resourceRecordStore.getNumRecords() < recordIndex) {
				return null;
			}
			return Image.createImage(new ByteArrayInputStream(
					resourceRecordStore.getRecord(recordIndex)));
		} catch (Exception e) {
			return null;
		}
	}

	public static int saveImageData(byte[] data) {
		try {
			if (resourceRecordStore == null) {
				resourceRecordStore = RecordStore.openRecordStore(RESOURCE_RMS,
						true);
			}
			return resourceRecordStore.addRecord(data, 0, data.length);
		} catch (Exception e) {
			return -1;
		}
	}

	public static void close() {
		try {
			if (resourceRecordStore != null) {
				resourceRecordStore.closeRecordStore();
				resourceRecordStore = null;
			}
		} catch (RecordStoreNotOpenException e) {
		} catch (RecordStoreException e) {
		}
	}
}
