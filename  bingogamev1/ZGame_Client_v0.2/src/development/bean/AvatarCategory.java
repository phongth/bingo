package development.bean;

import javax.microedition.lcdui.Image;

public class AvatarCategory {
	private String categoyName;
	private int numberOfAvatars;
	private Image[] avatars = new Image[0];

	public AvatarCategory(String categoyName) {
		this.categoyName = categoyName;
	}
	
	public int getNumberOfAvatars() {
		return numberOfAvatars;
	}

	public void setNumberOfAvatars(int numberOfAvatars) {
		this.numberOfAvatars = numberOfAvatars;
		avatars = new Image[numberOfAvatars];
	}

	public void putAvatar(Image avatar, int index) {
		avatars[index] = avatar;
	}
	
	public Image getAvatarAt(int index) {
		if (index < avatars.length) {
			return avatars[index];
		} 
		return null;
	}
	
	public String getCategoyName() {
		return categoyName;
	}
}
