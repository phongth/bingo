package state.util;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


import state.GameConstants;
import state.GameGlobal;
import state.ResourceRms;
import state.font.ImageFontForMidp2;

/**
 * @version 0.3
 */
public class ImageUtil {
	public static Image getImage(String fileName) {
		Object value = null;
		
		// Nếu là màn hình 320x240 thì lấy thử ảnh ngang xem có được không
		if (!GameConstants.IS_240x320_SCREEN) {
			int index = fileName.indexOf('.');
			String ext = fileName.substring(index + 1);
			String name = fileName.substring(0, index);
			value = GameGlobal.imageLocationTable.get(name + "_320240" + "." + ext);
			if (value == null) {
				value = GameGlobal.imageLocationTable.get(fileName);
			} else {
				fileName = name + "_320240" + "." + ext;
			}
		} else {
			value = GameGlobal.imageLocationTable.get(fileName);
		}
		
		// Get image from rms
		if (value != null) {
		  return ResourceRms.getImage(((Integer) value).intValue());
		}
		
		// Get image from jar
		try {
			return Image.createImage(GameConstants.IMAGE_FOLDER + "/" + fileName);
		} catch (IOException e) {
			System.out.println("ERROR: Can't get image from: " + GameConstants.IMAGE_FOLDER + "/" + fileName);
			return null;
		}
	}
	
	/**
	 * Ghép nhiều lần 1 ảnh nhỏ để tạo 1 ảnh lớn hơn
	 * 
	 * @param image - Ảnh nhỏ
	 * @param width - Chiều rộng của ảnh lớn cần tạo
	 * @param height - Chiều dài của ảnh lớn cần tạo
	 * @return Ảnh lớn sau khi đã được ghép
	 */
	public static Image joinAndCreateImages(Image image, int width, int height, boolean isTransparenceRGB) {
		if (!isTransparenceRGB) {
			return joinAndCreateImages(image, width, height);
		}

		int[] rgbs = new int[width * height];
		for (int i = 0; i < rgbs.length; i++) {
			rgbs[i] = 0x00000000;
		}
		
		int childWidth = image.getWidth();
		int childHeight = image.getHeight();
		int[] chidRgbs = new int[childWidth * childHeight];
		image.getRGB(chidRgbs, 0, childWidth, 0, 0, childWidth, childHeight);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				rgbs[i + j * width] = chidRgbs[i % childWidth + (j % childHeight) * childWidth];
			}
		}
		return Image.createRGBImage(rgbs, width, height, true);
	}

	/**
	 * Ghép nhiều lần 1 ảnh nhỏ để tạo 1 ảnh lớn hơn
	 * 
	 * @param image - Ảnh nhỏ
	 * @param width - Chiều rộng của ảnh lớn cần tạo
	 * @param height - Chiều dài của ảnh lớn cần tạo
	 * @return Ảnh lớn sau khi đã được ghép
	 */
	public static Image joinAndCreateImages(Image image, int width, int height) {
		Image resultImage = Image.createImage(width, height);
		Graphics g = resultImage.getGraphics();
		int heightTime = height / image.getHeight();
		int widthTime = width / image.getWidth();
		int x = 0;
		int y = 0;
		for (int i = 0; i < heightTime; i++) {
			for (int j = 0; j < widthTime; j++) {
				g.drawImage(image, x, y, Graphics.TOP | Graphics.LEFT);
				x += image.getWidth();
			}
			y += image.getHeight();
		}
		return resultImage;
	}

	/**
	 * Vẽ một chuỗi text lên ảnh
	 * 
	 * @param inputImage Ảnh cần vẽ text lên
	 * @param text Text cần vẽ
	 * @param font ImageFont sử dụng để vẽ text
	 * @param textColor Mầu của text
	 * @param textX Tọa độ x của text
	 * @param textY Tọa độ y của text
	 * @param textAnchor Anchor của text
	 * @param transparenceRGB Mầu dùng để phủ trong suốt, phải là mầu không có trong ảnh và không phải mầu chữ
	 * @return
	 */
	public static Image drawTextToImage(Image inputImage, String text, ImageFontForMidp2 font, int textColor, int textX, int textY, int textAnchor, int transparenceRGB) {
		Image image = Image.createImage(inputImage.getWidth(), inputImage.getHeight());
		Graphics g = image.getGraphics();
		g.setColor(transparenceRGB);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.drawImage(inputImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
		font.drawString(g, text, textColor, textX, textY, textAnchor);

		// Làm trong suốt lại ảnh
		return makeToTransparentImage(image, transparenceRGB);
	}

	/**
	 * Lấy ra 1 ảnh là 1 phần của ảnh đã có
	 * 
	 * @param image Ảnh cũ
	 * @param fromX Tọa độ X TOP-LEFT của khung hình lấy ra
	 * @param fromY Tọa độ Y TOP-LEFT của khung hình lấy ra
	 * @param newWidth Kích thước chiều ngang của khung hình lấy ra
	 * @param newHeight Kích thước chiều dài của khung hình lấy ra
	 * @return Ảnh con của ảnh đầu vào
	 */
	public static Image getSubImage(Image image, int fromX, int fromY, int newWidth, int newHeight, boolean isTransparence) {
		if (!isTransparence) {
			Image newImage = Image.createImage(newWidth, newHeight);
			newImage.getGraphics().setClip(0, 0, newWidth, newHeight);
			newImage.getGraphics().drawImage(image, -fromX, -fromY, GameConstants.TOP_LEFT_ANCHOR);
			return newImage;
		}

		int[] rgb = new int[newWidth * newHeight];
		image.getRGB(rgb, 0, newWidth, fromX, fromY, newWidth, newHeight);
		return Image.createRGBImage(rgb, newWidth, newHeight, true);
	}

	/**
	 * Chuyển đổi tất cả các điểm mầu của ảnh thành sáng hơn
	 * 
	 * @param image - Ảnh cần làm sáng
	 * @param rank - Độ làm sáng (phải nằm trong khoảng từ 0 đến 15)
	 * @return - Ảnh sau khi đã làm sáng
	 */
	public static Image makeBrighter(Image image, int rank) {
		if ((0 > rank) && (rank > 15)) {
			throw new IllegalArgumentException("ImageUtil : makeBrighter : rank must be in [0..15]");
		}

		int[] data = new int[image.getWidth() * image.getHeight()];
		image.getRGB(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
		for (int i = 0; i < data.length; i++) {
			data[i] = RGB.incr(data[i], rank);
		}
		return Image.createRGBImage(data, image.getWidth(), image.getHeight(), true);
	}

	/**
	 * Chuyển đổi tất cả các điểm mầu của ảnh thành tối hơn
	 * 
	 * @param image - Ảnh cần làm tối
	 * @param rank - Độ làm tối (phải nằm trong khoảng từ 0 đến 15)
	 * @return - Ảnh sau khi đã làm tối
	 */
	public static Image makeDarker(Image image, int rank) {
		if ((0 > rank) && (rank > 15)) {
			throw new IllegalArgumentException("ImageUtil : makeDarker : rank must be in [0..15]");
		}

		int[] data = new int[image.getWidth() * image.getHeight()];
		image.getRGB(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
		for (int i = 0; i < data.length; i++) {
			data[i] = RGB.decr(data[i], rank);
		}
		return Image.createRGBImage(data, image.getWidth(), image.getHeight(), true);
	}

	/**
	 * Tạo ảnh lưới (cứ 1 điểm ảnh không trong suốt xen kẽ với 1 điểm ảnh trong suốt)
	 * 
	 * @param rgb - mã mầu của điểm ảnh không trong suốt
	 * @param width - Chiều rộng của ảnh cần tạo
	 * @param height - Chiều cao của ảnh cần tạo
	 * @return Ảnh sau khi đã tạo
	 */
	public static Image createNetImage(int rgb, int width, int height) {
		int[] data = new int[width * height];
		for (int i = 0; i < height; i++) {
			for (int j = (i % 2); j < width; j += 2) {
				data[i * width + j] = rgb;
			}
		}

		for (int i = 0; i < height; i++) {
			for (int j = (i % 2 + 1); j < width; j += 2) {
				data[i * width + j] = 0x00000000;
			}
		}
		return Image.createRGBImage(data, width, height, true);
	}

	/**
	 * Zoom toàn bộ ảnh
	 * 
	 * @param src - Ảnh cần thay đổi
	 * @param scale - Kích thước muốn thay đổi, 0 < scale < 1 nếu muốn thu nhỏ ảnh và > 1 nếu muốn phóng to ảnh
	 * @return Ảnh sau khi zoom
	 */
	public static Image resizeImage(Image src, double scale) {
		return resizeImage(src, scale, 0, 0, true);
	}

	/**
	 * Zoom 1 phần của ảnh
	 * 
	 * @param src - Ảnh cần thay đổi
	 * @param scale - Kích thước muốn thay đổi, 0 < scale < 1 nếu muốn thu nhỏ ảnh và > 1 nếu muốn phóng to ảnh
	 * @param px - tọa độ x xác định của khoảng ảnh cần zoom
	 * @param py - tọa độ y xác định của khoảng ảnh cần zoom
	 * @param d - kích thước của khoảng hình vuông cần zoom
	 * @return Ảnh sau khi zoom
	 */
	public static Image resizeImage(Image src, double scale, int px, int py, boolean isNeedToMakeTransparent) {
		if (scale < 0) {
			throw new IllegalArgumentException("scale must be greater than 0");
		}

		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int dstW = (int) (srcW * scale);
		int dstH = (int) (srcH * scale);

		Image tmp = Image.createImage(dstW, srcH);
		Graphics g = tmp.getGraphics();
		double delta = (srcW << 2) / (double) dstW;
		double pos = delta / 2;
		py = -py;
		for (int x = 0; x < dstW; x++) {
			g.setClip(x, 0, 1, srcH);
			g.drawImage(src, round(x - (pos / 4) - px), py, GameConstants.TOP_LEFT_ANCHOR);
			pos += delta;
		}

		Image dst = Image.createImage(dstW, dstH);
		g = dst.getGraphics();
		delta = (srcH << 2) / (double) dstH;
		pos = delta / 2;
		for (int y = 0; y < dstH; y++) {
			g.setClip(0, y, dstW, 1);
			g.drawImage(tmp, 0, round(y - (pos / 4)), GameConstants.TOP_LEFT_ANCHOR);
			pos += delta;
		}

		tmp = null;
		if (isNeedToMakeTransparent) {
			return makeToTransparentImage(dst, 0xFFFFFFFF);
		}
		return dst;
	}

	public static Image transformMirror(Image image) {
		int[] rgb = new int[image.getWidth() * image.getHeight()];
		int[] newRgb = new int[rgb.length];
		image.getRGB(rgb, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				newRgb[i + j * image.getWidth()] = rgb[image.getWidth() - i - 1 + j * image.getWidth()];
			}
		}
		return Image.createRGBImage(newRgb, image.getWidth(), image.getHeight(), true);
	}

	public static Image changeColor(Image inputImage, int fromColor, int toColor) {
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		int[] rgb = new int[width * height];
		inputImage.getRGB(rgb, 0, width, 0, 0, width, height);
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == fromColor) {
				rgb[i] = toColor;
			}
		}
		return Image.createRGBImage(rgb, width, height, true);
	}

	public static Image rotate(Image image, int degrees) {
		int width = image.getWidth();
		int height = image.getHeight();
		int value = Math.max(width, height);
		Image tmpImage = Image.createImage(value, value);
		Graphics g = tmpImage.getGraphics();
		g.drawImage(image, value / 2, value / 2, GameConstants.CENTER_ANCHOR);

		int[] arr = new int[value * value];
		int[] dest = new int[arr.length];
		tmpImage.getRGB(arr, 0, value, 0, 0, value, value);
		int centerX = value / 2;
		int centerY = value / 2;

		double radians = Math.toRadians(-degrees);
		double cosDeg = Math.cos(radians);
		double sinDeg = Math.sin(radians);
		for (int x = 0; x < value; x++) {
			for (int y = 0; y < value; y++) {
				int x2 = round(cosDeg * (x - centerX) - sinDeg * (y - centerY) + centerX);
				int y2 = round(sinDeg * (x - centerX) + cosDeg * (y - centerY) + centerY);
				if (!((x2 < 0) || (y2 < 0) || (x2 >= value) || (y2 >= value))) {
					int destOffset = x2 + y2 * value;
					if ((destOffset >= 0) && (destOffset < dest.length)) {
						if (arr[destOffset] != 0xFFFFFFFF) {
							dest[x + y * value] = arr[destOffset];
						} else {
							dest[x + y * value] = 0x00000000;
						}
					}
				} else {
					dest[x + y * value] = 0x00000000;
				}
			}
		}
		Image tmpImage1 = Image.createRGBImage(dest, value, value, true);

		// Trim size
		int leftDx = value;
		int rightDx = 0;
		int topDy = value;
		int bottomDy = 0;

		for (int x = 0; x < value; x++) {
			for (int y = 0; y < value; y++) {
				if (dest[x + y * value] != 0x00000000) {
					if (x < leftDx) {
						leftDx = x;
					}
					if (x > rightDx) {
						rightDx = x;
					}
					if (y < topDy) {
						topDy = y;
					}
					if (y > bottomDy) {
						bottomDy = y;
					}
				}
			}
		}

		// Change all dark point to transparent point
		int resultWidth = rightDx - leftDx;
		int resultHeight = bottomDy - topDy;
		Image resultImage = Image.createImage(resultWidth, resultHeight);
		Graphics g1 = resultImage.getGraphics();
		g1.drawImage(tmpImage1, -leftDx, -topDy, GameConstants.TOP_LEFT_ANCHOR);
		int[] rgbResult = new int[resultWidth * resultHeight];
		resultImage.getRGB(rgbResult, 0, resultWidth, 0, 0, resultWidth, resultHeight);
		for (int x = 0; x < resultWidth; x++) {
			for (int y = 0; y < resultHeight; y++) {
				if (rgbResult[x + y * resultWidth] == 0xFFFFFFFF) {
					rgbResult[x + y * resultWidth] = 0x00000000;
				}
			}
		}
		return Image.createRGBImage(rgbResult, resultWidth, resultHeight, true);
	}

	private static int round(double d) {
		if (d < ((int) d) + 0.5) {
			return (int) d;
		}
		return ((int) d) + 1;
	}

	public static Image joinImages(Image[] images, int xs[], int ys[], int[] anchors, int resultImageWidth, int resultImageHeight, int notUseColor) {
		Image resultImage = Image.createImage(resultImageWidth, resultImageHeight);
		Graphics g = resultImage.getGraphics();
		g.setColor(notUseColor);
		g.setClip(0, 0, resultImageWidth, resultImageHeight);
		g.fillRect(0, 0, resultImageWidth, resultImageHeight);
		for (int i = 0; i < images.length; i++) {
			g.drawImage(images[i], xs[i], ys[i], anchors[i]);
		}

		int[] rgb = new int[resultImageWidth * resultImageHeight];
		resultImage.getRGB(rgb, 0, resultImageWidth, 0, 0, resultImageWidth, resultImageHeight);
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == notUseColor) {
				rgb[i] = 0x00000000;
			}
		}
		return Image.createRGBImage(rgb, resultImageWidth, resultImageHeight, true);
	}

//	public static void printImageData(Image image) {
//		int[] rgb = new int[image.getWidth() * image.getHeight()];
//		image.getRGB(rgb, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
//		for (int i = 0; i < image.getWidth(); i++) {
//			for (int j = 0; j < image.getHeight(); j++) {
//				System.out.print(HexaUtil.toHexaString(rgb[i + j * image.getWidth()], false) + " ");
//			}
//			System.out.println();
//		}
//	}

	public static Image makeToTransparentImage(Image src, int turnToTransparentRGB) {
		int[] rgb = new int[src.getWidth() * src.getHeight()];
		src.getRGB(rgb, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == turnToTransparentRGB) {
				rgb[i] = 0x00000000;
			}
		}
		return Image.createRGBImage(rgb, src.getWidth(), src.getHeight(), true);
	}
}
