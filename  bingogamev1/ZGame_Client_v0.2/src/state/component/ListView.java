package state.component;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import state.GameConstants;
import state.font.FontManager;
import state.font.ImageText;
import state.font.ImageTextForMidp2;
import state.util.Color;

public class ListView extends Component {
	public static final int DEFAULT_COLUMN_WIDTH = 50;
	public static final int DEFAULT_ROW_HEIGHT = 20;

	private Vector[] data;
	private String[] columnTitles;
	private int[] columnWidths;

	private int rowHeight = DEFAULT_ROW_HEIGHT;
	private int titleRowHeight = DEFAULT_ROW_HEIGHT;
	protected boolean isShowColumnTitle = true;
	protected boolean isFillTitleBackGround = true;
	protected int tileBackgroundColor = Color.WHITE_CODE;
	protected int titleForceGroundColor = Color.BLACK_CODE;
	private ImageText titleFont;

	private int totalColumnWidth = 0;

	public ListView(int numberOfColumn) {
		if (numberOfColumn < 1) {
			throw new IllegalArgumentException(
					"ListView : numberOfColumn can not be smaller than 1");
		}

		focusable = false;
		columnTitles = new String[numberOfColumn];
		columnWidths = new int[numberOfColumn];
		data = new Vector[numberOfColumn];
		for (int i = 0; i < data.length; i++) {
			data[i] = new Vector();
			columnTitles[i] = "";
			columnWidths[i] = DEFAULT_COLUMN_WIDTH;
		}
		calculateTotalWidth();
	}

	public ListView(int numberOfColumn, String[] columnTitle) {
		this(numberOfColumn);

		if (columnTitle == null) {
			throw new IllegalArgumentException(
					"ListView : coloumTitle can not be NULL");
		}

		int maxIndex = Math.min(columnTitle.length, numberOfColumn);
		for (int i = 0; i < maxIndex; i++) {
			this.columnTitles[i] = columnTitle[i];
		}
	}

	public void setStyle(Style style) {
		super.setStyle(style);

		if (style instanceof ListViewStyle) {
			ListViewStyle listViewStyle = (ListViewStyle) style;
			isShowColumnTitle = listViewStyle.isShowColumnTitle;
			isFillTitleBackGround = listViewStyle.isFillTitleBackGround;
			tileBackgroundColor = listViewStyle.tileBackgroundColor;
			titleForceGroundColor = listViewStyle.titleForceGroundColor;
			rowHeight = listViewStyle.rowHeight;
		}
	}

	public void addRow(String[] rowData) {
		if (rowData == null) {
			throw new IllegalArgumentException(
					"ListView : addRow : rowData can not be NULL");
		}

		int maxIndex = Math.min(rowData.length, data.length);
		for (int i = 0; i < maxIndex; i++) {
			data[i].addElement(rowData[i]);
		}

		for (int i = 0; i < (data.length - rowData.length); i++) {
			data[rowData.length + i].addElement("");
		}
	}

	public void setColumnWidth(int cloumnIndex, int width) {
		if ((cloumnIndex < 0) || (cloumnIndex > columnWidths.length - 1)) {
			throw new IllegalArgumentException(
					"ListView : setColumnWidth : cloumnIndex is invalid: "
							+ cloumnIndex);
		}

		if (width < 0) {
			throw new IllegalArgumentException(
					"ListView : setColumnWidth : width is invalid: " + width);
		}

		columnWidths[cloumnIndex] = width;
		calculateTotalWidth();
	}

	public void setColumnWidths(int[] columnWidths) {
		if (columnWidths == null) {
			throw new IllegalArgumentException(
					"ListView : setColumnWidths : columnWidths can not be NULL");
		}

		int maxIndex = Math.min(columnWidths.length, this.columnWidths.length);
		for (int i = 0; i < maxIndex; i++) {
			this.columnWidths[i] = columnWidths[i];
		}
		calculateTotalWidth();
	}

	private void calculateTotalWidth() {
		totalColumnWidth = 0;
		for (int i = 0; i < columnWidths.length; i++) {
			totalColumnWidth += columnWidths[i];
		}
	}

	public void draw(Graphics g) {
		if (!isVisible()) {
			return;
		}

		int dy = 0;
		int numberOfRow = data[0].size();
		int numberOfColumn = data.length;
		ImageText font = getFont();
		ImageText titleFont = getTitleFont();

		if (isShowColumnTitle) {
			if (isFillTitleBackGround) {
				g.setColor(tileBackgroundColor);
				g.fillRect(realX, realY, totalColumnWidth, titleRowHeight);
			}
			if (hasBorder) {
				g.setColor(borderColor);
				g.drawRect(realX, realY, totalColumnWidth - 1, titleRowHeight);
			}

			int currentColumnX = realX;
			for (int i = 0; i < columnTitles.length; i++) {
				titleFont.drawString(g, columnTitles[i], titleForceGroundColor,
						currentColumnX + columnWidths[i] / 2, realY
								+ titleRowHeight / 2,
						GameConstants.CENTER_ANCHOR);
				currentColumnX += columnWidths[i];
			}

			dy = titleRowHeight;
		}

		if (isFillBackGround) {
			g.setColor(backgroundColor);
			g.fillRect(realX, realY + dy, totalColumnWidth, numberOfRow
					* rowHeight);
		}

		if (hasBorder) {
			g.setColor(borderColor);
			g.drawRect(realX, realY + dy, totalColumnWidth, numberOfRow
					* rowHeight - 1);
		}

		int currentRowY = realY + dy;
		for (int i = 0; i < numberOfRow; i++) {
			if (hasBorder) {
				g.setColor(borderColor);
				g.drawRect(realX, currentRowY, totalColumnWidth, rowHeight);
			}

			int currentColumnX = realX;
			for (int j = 0; j < numberOfColumn; j++) {
				font.drawString(g, String.valueOf(data[j].elementAt(i)),
						forceGroundColor, currentColumnX + columnWidths[i] / 2,
						currentRowY + rowHeight / 2,
						GameConstants.CENTER_ANCHOR);
				currentColumnX += columnWidths[i];
			}
			currentRowY += rowHeight;
		}
	}

	public ImageText getTitleFont() {
		if (titleFont == null) {
			titleFont = FontManager.getFont(FontManager.FONT_SIZE_8);
		}
		return titleFont;
	}

	public void setTitleFont(ImageTextForMidp2 font) {
		this.titleFont = font;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	public boolean isShowColumnTitle() {
		return isShowColumnTitle;
	}

	public void setShowColumnTitle(boolean isShowColumnTitle) {
		this.isShowColumnTitle = isShowColumnTitle;
	}

	public boolean isFillTitleBackGround() {
		return isFillTitleBackGround;
	}

	public void setFillTitleBackGround(boolean isFillTitleBackGround) {
		this.isFillTitleBackGround = isFillTitleBackGround;
	}

	public int getTileBackgroundColor() {
		return tileBackgroundColor;
	}

	public void setTileBackgroundColor(int tileBackgroundColor) {
		this.tileBackgroundColor = tileBackgroundColor;
	}

	public int getTitleForceGroundColor() {
		return titleForceGroundColor;
	}

	public void setTitleForceGroundColor(int titleForceGroundColor) {
		this.titleForceGroundColor = titleForceGroundColor;
	}

	public int getTitleRowHeight() {
		return titleRowHeight;
	}

	public void setTitleRowHeight(int titleRowHeight) {
		this.titleRowHeight = titleRowHeight;
	}

	public void detroy() {
		super.detroy();
		data = null;
		columnTitles = null;
		columnWidths = null;
	}
}
