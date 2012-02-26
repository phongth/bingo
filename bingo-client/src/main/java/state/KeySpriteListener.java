package state;

public interface KeySpriteListener {
	public void keyPressed(Sprite source, int keyCode);

	public void keyReleased(Sprite source, int keyCode);

	public void keyRepeated(Sprite source, int keyCode);
}
