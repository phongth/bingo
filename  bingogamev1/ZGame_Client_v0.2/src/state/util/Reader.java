package state.util;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Image;

import state.GameConstants;

public class Reader {

	/**
	 * Load ảnh từ file
	 * 
	 * @param datFile
	 *            Đường đẫn đến file chứa ảnh
	 * @return Mảng các ảnh đã load
	 */
	public static Image[] loadImage(String datFile) {
		InputStream inputStream = Reader.class
				.getResourceAsStream(GameConstants.IMAGE_FOLDER + datFile
						+ ".dat");
		byte[] numberOfImageBytes = new byte[4];
		Image[] result = null;
		try {
			inputStream.read(numberOfImageBytes);
			int numberOfImage = SerialUtil.deserialToInt(numberOfImageBytes);
			result = new Image[numberOfImage];
			int index = 0;
			while (index < numberOfImage) {
				byte[] imageBytes = new byte[5];
				inputStream.read(imageBytes);
				int imageSize = Integer.parseInt(new String(imageBytes));

				byte[] buffers = new byte[imageSize];
				inputStream.read(buffers);
				result[index] = Image.createImage(buffers, 0, imageSize);
				index++;
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
			}
		}
		return result;
	}

	/**
	 * Load các ảnh có số thứ tự trong file ảnh
	 * 
	 * @param datFile
	 *            Đường đẫn đến file chứa ảnh
	 * @param imageIndexArray
	 *            mảng các index ảnh cần load
	 * @return Mảng các ảnh đã load
	 */
	public static Image[] loadImage(String datFile, int[] imageIndexArray) {
		InputStream inputStream = Reader.class
				.getResourceAsStream(GameConstants.IMAGE_FOLDER + datFile
						+ ".dat");
		byte[] numberOfImageBytes = new byte[4];
		Image[] result = null;

		try {
			inputStream.read(numberOfImageBytes);
			int totalNumberOfImage = SerialUtil
					.deserialToInt(numberOfImageBytes);
			result = new Image[imageIndexArray.length];
			int index = 0;
			int imageIndexArrayIndex = 0;

			while ((index < totalNumberOfImage)
					&& (imageIndexArrayIndex < imageIndexArray.length)) {
				byte[] imageBytes = new byte[5];
				inputStream.read(imageBytes);
				int imageSize = Integer.parseInt(new String(imageBytes));

				byte[] buffers = new byte[imageSize];
				inputStream.read(buffers);

				if (imageIndexArray[imageIndexArrayIndex] == index) {
					result[imageIndexArrayIndex] = Image.createImage(buffers,
							0, imageSize);
					imageIndexArrayIndex++;
				}
				index++;
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
			}
		}
		return result;
	}
}
