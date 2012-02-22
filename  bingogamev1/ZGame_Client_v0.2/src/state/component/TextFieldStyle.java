package state.component;

import state.GameConstants;

public class TextFieldStyle extends Style {
	public static final int ALIGN_LEFT = GameConstants.VCENTER_LEFT_ANCHOR;
	public static final int ALIGN_CENTER = GameConstants.CENTER_ANCHOR;
	public static final int ALIGN_RIGHT = GameConstants.VCENTER_RIGHT_ANCHOR;
	
	protected int align;

	public int getAlign() {
		return align;
	}

	public TextFieldStyle setAlign(int align) {
		this.align = align;
		return this;
	}
}
