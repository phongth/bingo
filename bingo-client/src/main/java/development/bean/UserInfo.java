package development.bean;

import javax.microedition.lcdui.Image;

public class UserInfo {

	private String userName;
	private String point = "";
	private boolean isOnline;
	private boolean isChoice;
	private String dangCap = "Vô đối";
	private Image avataImage;

	/**
	 * @return the avataImage
	 */
	public Image getAvataImage() {
		return avataImage;
	}

	/**
	 * @param avataImage
	 *            the avataImage to set
	 */
	public void setAvataImage(Image avataImage) {
		this.avataImage = avataImage;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the point
	 */
	public String getPoint() {
		return point;
	}

	/**
	 * @param point
	 *            the point to set
	 */
	public void setPoint(String point) {
		this.point = point;
	}

	/**
	 * @return the isOnline
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * @param isOnline
	 *            the isOnline to set
	 */
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	/**
	 * @return the isChoice
	 */
	public boolean isChoice() {
		return isChoice;
	}

	/**
	 * @param isChoice
	 *            the isChoice to set
	 */
	public void setChoice(boolean isChoice) {
		this.isChoice = isChoice;
	}

	public void setDangCap(String dangCap) {
		this.dangCap = dangCap;
	}

	public String getDangCap() {
		return dangCap;
	}

}
