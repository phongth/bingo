package state.font;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

/**
 * <p>Text formatting and drawing utility.<p> 
 * <p>This code is a part of the Mobile Fonts Project (http://sourceforge.net/projects/mobilefonts)</p>
 * 
 * @author Sergey Tkachev <a href="http://sergetk.net">http://sergetk.net</a>
 */
public class ImageTextForMidp1 extends ImageFontForMidp1 implements ImageText {
	private static final byte DEFAULT_LINE_DISTANCE = 3;
	
	/**
	 * Latin and cyrillic vowels
	 */
	private static final String VOWELS = "aeiouy\u0430\u0435\u0451\u0438\u043E\u0443\u044B\u044D\u044E\u044F";
	/**
	 * Cyrillic hard and soft signs
	 */
	private static final String DONT_TEAR_OFF_AT_END = "\u044A\u044C'";
	/**
	 * Text wrapping mode: wrapping is off
	 */
	public static final int WRAPPING_NONE = 0;
	/**
	 * Text wrapping mode: wrapping by words
	 */
	public static final int WRAPPING_WORDS = 1;
	/**
	 * Text wrapping mode: wrapping by syllables
	 */
	public static final int WRAPPING_SYLLABLES = 2;
	/**
	 * Text left marging
	 */
	public int leftMargin = 8;
	/**
	 * Text right marging
	 */
	public int rightMargin = 8;
	/**
	 * Text top marging
	 */
	public int topMargin = 5;
	/**
	 * Text bottom marging
	 */
	public int bottomMargin = 5;
	/**
	 * Image alignment: can be LEFT, RIGHT or CENTER
	 */
	public int imageAlignment = Graphics.LEFT;
	/**
	 * An horizontal distance between the text and the image
	 */
	public int imageHorizontalMargin = 10;
	/**
	 * A vertical distance between the text and the image
	 */
	public int imageVerticalMargin = 5;
	/**
	 * Distance between paragraphs
	 */
	public int paragraphIndent = 5;
	/**
	 * Text area width
	 */
	public int width;

	private int textHeight;
	private int titleHeight;
	public int getTitleHeight() {
		return titleHeight;
	}

	public void setTitleHeight(int titleHeight) {
		this.titleHeight = titleHeight;
	}

	public int linesCount;
	public int actualWidth;
	public String title;
	public String text;

	public int cursorPosition;
	public int cursorWidth;
	public int cursorX;
	public int cursorY;

	public int globalColor;
	public int textColor;
	public int textAlignment = Graphics.LEFT;
	public int wrappingMode = WRAPPING_SYLLABLES;

	private int currentTextColor;
	private int currentTextAlignment;
	private int nextTextAlignment;
	private int currentFontHeight;
	private int cursorPartIndex;

	private int currentWrappingMode;

	public boolean editMode;
	public boolean underlined;

	private Vector textParts = new Vector(); // text parts

	public ImageTextForMidp1(String fontName) {
		super(fontName);
	}
	
	/**
	 * Sets the width and formats the text.
	 * @param width the width in pixels
	 */
	private int setWidth(int width) {
		int height = 0;
		this.width = width;
		height = format();

		return height;
	}

	private TextPart getTextPart(int index) {
		return (TextPart)this.textParts.elementAt(index);
	}

	/**
	 * Looks for splitting point for the current line.
	 * @param part
	 * @param start start index
	 * @param x start x position
	 * @param maxX maximum x value
	 * @return split position index
	 */
	private int splitBySyllables(TextPart part, int start, int x, int maxX) {
		boolean hasVowel = false;
		boolean eol = false;
		boolean overrun = false;
		boolean isVowel = false;

		int hyphenWidth = charWidth('-');
		maxX -= hyphenWidth;

		int lastValidSplit = -1;
		int lastValidX = 0;

		int splitCandidate = -1;
		int splitCandidateX = 0; 

		int consIndex2 = -1;
		int consX2 = 0;
		int consCount = 0;

		int p = start;

		char c = text.charAt(p);

		for (;;) {
			if (p < text.length()) {
				c = text.charAt(p);
				if (c == ' ') {
					eol = true;
				} else {
					isVowel = VOWELS.indexOf(toLowerCase(c)) != -1;
				}
			} else {
				c = 0;
				eol = true;
			}

			if (isVowel) {
				if (hasVowel) {
					if (consCount > 1) { // closed syllable
						splitCandidate = consIndex2;
						splitCandidateX = consX2;
					} else
						if (consCount == 1) { // opened syllable
							splitCandidate = consIndex2;
							splitCandidateX = consX2;
						} else {
							splitCandidate = p;
							splitCandidateX = x;
						}

					if (splitCandidateX <= maxX) {
						lastValidSplit = splitCandidate;
						lastValidX = splitCandidateX;
					} else {
						overrun = true;
					}
				}
				hasVowel = true;
				consIndex2 = -1;
				consCount = 0;
			} else {
				if (hasVowel) {
					c = toLowerCase(c);
					if (DONT_TEAR_OFF_AT_END.indexOf(toLowerCase(c)) < 0) {
						consIndex2 = p;
						consX2 = x;
					}
					consCount++;
				}
			}

			if (eol) {
				if (lastValidSplit == start + 1)
					return -1;

				if (lastValidSplit != -1) {
					part.hasHyphen = true;
					part.width = lastValidX - part.x + hyphenWidth;
					part.end = lastValidSplit;
				}
				return lastValidSplit;
			}

			if (!overrun)
				x += charWidth(c);
			overrun = x > maxX;

			p++;
		}
	}

	/**
	 * Formats the text. Call this method if you changed any parameters such as text, alignment or margins.
	 * Note that setWidth() calls this method itself.
	 */
	private int format() {
		this.cursorPartIndex = -1;
		this.textHeight = 0;
		this.textParts.removeAllElements();
		boolean cursorFound = false;

		// if it is no text or text has zero width...
		if (text == null || text.length() == 0) {
			cursorX = 0;
			cursorY = 0;
			return textHeight;
		}

		currentWrappingMode = wrappingMode;
		currentTextColor = textColor;
		nextTextAlignment = currentTextAlignment = this.textAlignment;
		currentFontHeight = getHeight();

		linesCount = 0;

		int y = this.topMargin;					// y position of current character		
		int maxX = this.width - this.rightMargin;
		int minX = this.leftMargin;

		int x = 0;
		int textLength = 0;
		if (text == null || text.length() == 0) {
			this.textHeight = y + bottomMargin;
			return textHeight;
		}

		textLength= text.length();

		x = minX;
		int lineHeight = 0;

		TextPart currentPart = null;

		boolean isWord = false;
		boolean isSpace = false;
		boolean newLine = false;
		boolean partEnds = false;

		int p = 0;				// index of the current character 
		int spaceStart = -1;
		int wordStart = -1;
		int wordX = 0;
		int spaceX = 0;

		int firstPartAtLine = -1;
		int lastPartAtLine = -1;
		int localCursorX = -1;

		while (p < textLength) {
			boolean isCursor = (!cursorFound && p == cursorPosition);
			char c = text.charAt(p);

			{
				if (c == '\n' || c == '\r') { // new line
					if (currentPart != null) {
						currentPart.end = p;
						currentPart.width = x - currentPart.x;
						partEnds = true;
					}
					newLine = true;
					y += paragraphIndent;
				} else {
					boolean charIsSpace = c == ' ';

					// new text part if:
					// there are no any text parts
					// or current character isn't space or part isnt first at line
					if (currentPart == null && (editMode || !charIsSpace || firstPartAtLine != -1)) {
						currentPart = new TextPart();
						currentPart.start = p;
						currentPart.x = x;
						currentPart.y = y;
						currentPart.height = currentFontHeight;
						currentPart.color = this.currentTextColor;
						lineHeight = Math.max(lineHeight, currentPart.height);

						wordStart = spaceStart = -1;
						isWord = isSpace = false;
					}

					// if current part is started, process the character
					if (currentPart != null) {
						if (charIsSpace) {
							if (!isSpace) {
								isSpace = true;
								isWord = false;
								spaceStart = p;
								spaceX = x;
							}
						} else {
							if (!isWord) {
								isSpace = false;
								isWord = true;
								wordStart = p;
								wordX = x;
							}
						}

						int charWidth = charWidth(c);
						if (isCursor) {
							localCursorX = x;
							charWidth += cursorWidth;
						}
						if (x + charWidth > maxX) {
							newLine = true;
							int split = -1;

							switch (this.currentWrappingMode) {
							case WRAPPING_NONE: {
								split = p;
								currentPart.width = x - currentPart.x;							
							} break;

							case WRAPPING_WORDS: {
								if(editMode)
								{
									if(spaceStart != -1 )
									{
										if(wordStart != -1 && wordStart>spaceStart)
											split = wordStart;
										else 
											split = p;
									}
									else
									{
										split = p;
									}
								}
								else
								{
									if (spaceStart != -1) { // split by spaces
										split = spaceStart;
										currentPart.width = spaceX - currentPart.x;
									}
									else {
										if (firstPartAtLine != -1) {
											p = currentPart.start - 1;
											currentPart = null;
											newLine = true;
										} else {
											split = p;
											currentPart.width = x - currentPart.x;									
										}
									}
								}
							} break;

							case WRAPPING_SYLLABLES: {
								// trying to split by syllables
								if (wordStart != -1)
									if (isSpace) {
										split = spaceStart;
										currentPart.width = spaceX - currentPart.x;
									} else {
										split = splitBySyllables(currentPart, wordStart, wordX, maxX);
									}

								if (split == -1) { // can't split
									if (spaceStart != -1) { // split by spaces
										split = spaceStart;
										currentPart.width = spaceX - currentPart.x;
									} else { // can't fit, split by words
										if (firstPartAtLine != -1) {
											p = currentPart.start - 1;
											currentPart = null;
											newLine = true;
										} else {
											split = p;
											currentPart.width = x - currentPart.x;									
										}
									}
								}							
							} break;
							}

							if (split != -1) {
								currentPart.end = split;
								partEnds = true;
								p = split - 1;
							}
						} else {
							if (p == textLength-1) {
								newLine = true;
								currentPart.end = textLength;
								currentPart.width = x - currentPart.x + charWidth;
								partEnds = true;
							}
						}

						if (currentPart != null) {
							x += charWidth;
						}
					}
				}
			}
			if (partEnds) {
				if (currentPart != null) {
					if (!cursorFound
							&& cursorPosition >= currentPart.start 
							&& cursorPosition <= currentPart.end)
					{
						cursorX = currentPart.x;
						if (localCursorX < 0) {
							cursorX += currentPart.width;
						} else {
							cursorX += localCursorX;
						}
						cursorY = currentPart.y;
						cursorPartIndex = textParts.size();

						cursorFound = true;
					}
					textParts.addElement(currentPart);
					if (firstPartAtLine == -1)
						firstPartAtLine = textParts.size()-1;
					if (lastPartAtLine == -1)
						lastPartAtLine = textParts.size()-1;
					currentPart = null;
				}
				partEnds = false;
			}

			if (newLine) {
				int lineWidth = 0;
				int widthShortage = 0;

				if (firstPartAtLine >= 0 && lastPartAtLine >=0) {
					TextPart firstPart = getTextPart(firstPartAtLine);
					TextPart lastPart = getTextPart(lastPartAtLine);
					lineWidth = lastPart.x + lastPart.width - firstPart.x;
					actualWidth = Math.max(actualWidth, lineWidth);
					widthShortage = (maxX - minX) - lineWidth;
				}

				if (firstPartAtLine != -1 && currentTextAlignment != Graphics.LEFT) {

					if (widthShortage > 0) { // need to align
						if (currentTextAlignment == Graphics.RIGHT || currentTextAlignment == Graphics.HCENTER) {
							int dx = currentTextAlignment == Graphics.RIGHT ? widthShortage : (widthShortage) / 2;
							for (int i = firstPartAtLine; i <= lastPartAtLine; i++) {
								getTextPart(i).x += dx;
							}
						}
					}
				}

				firstPartAtLine = lastPartAtLine = -1;

				// go to new line
				y += lineHeight;
				linesCount++;
				if (y >= this.imageVerticalMargin) {
					minX = this.leftMargin;
					maxX = this.width - this.rightMargin;
				}
				x = minX;

				newLine = false;
				lineHeight = 0;
				this.currentTextAlignment = this.nextTextAlignment;
			} // newLine

			p++;
		}

		if (!cursorFound) {
			cursorX = x;
			cursorY = y;
			cursorPartIndex = textParts.size();
		}

		this.textHeight = y + bottomMargin;
		
		return textHeight;
	}

	/**
	 * Draws the text in the specified range of lines.
	 * @param g the graphics context
	 * @param x the x position
	 * @param y the y position
	 * @param start the start line to be drawn
	 * @param end the end line to be drawn
	 */
	private void draw(Graphics g, int textColor, int x, int y, int start, int end) {
		try {
			int clipLeft = g.getClipX();
			int clipTop = g.getClipY();
			int clipRight = clipLeft + g.getClipWidth();
			int clipBottom = clipTop + g.getClipHeight();

			int partsCount = textParts.size();
			if (start < 0 || start >= partsCount || end <= start) {
				return;
			}
			if (end > partsCount) {
				end = partsCount;
			}
			for (int i = start; i < end; i++) {
				TextPart part = getTextPart(i);

				int xPos = x + part.x;
				int yPos = y + part.y;

				if (xPos + part.width >= clipLeft
						&& xPos < clipRight
						&& yPos + part.height >= clipTop
						&& yPos < clipBottom)
				{
					g.setColor(part.color == textColor ? globalColor : part.color);
					int hyphenX = 0;
					if (editMode && cursorWidth > 0 && i == cursorPartIndex) {
						int xx = drawSubstring(g, text, textColor, part.start, cursorPosition - part.start, xPos, yPos, Graphics.TOP|Graphics.LEFT) + cursorWidth;
						hyphenX = drawSubstring(g, text,textColor, cursorPosition, part.end - cursorPosition, xx, yPos, Graphics.TOP|Graphics.LEFT);
					} else {
						hyphenX = drawSubstring(g, text, textColor, part.start, part.end - part.start, xPos, yPos, Graphics.TOP|Graphics.LEFT);
					}

					if (part.hasHyphen) {
						drawOneChar(g, '-', textColor, hyphenX, yPos);
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * Converts a character to lower case. Many mobile devices have no full implementation
	 * of toLowerCase() method. They aren't support national alphabets. Following method
	 * can convert cyrillic strings to lower case. You can extend it to another alphabets.
	 * @param c source character
	 * @return character in lower case
	 */
	private static char toLowerCase(char c) {
		if ((c >= '\u0430' && c <= '\u044F') || c == '\u0451') {
			return c;
		} else if (c >= '\u0410' && c <= '\u042F') {
			return (char)(c + ('\u0430' - '\u0410'));
		} else if (c == '\u0401') {
			return '\u0451';
		} else {
			return Character.toLowerCase(c);	
		}
	}

	public int drawParagraph(Graphics g, String text, int color, int width, int lineDistance, int x, int y) {
		if (this.width != width || !text.equals(this.text)) {
			this.text = text;
			setWidth(width);
		}
		draw(g, color, x, y, 0, textParts.size());
		return 0;
	}

	public void prepareCache(String[] texts, int width) {
	}

	public void prepareCache(String text, int width) {
	}
	
	public int drawParagraph(Graphics g, String text, int color, int width, int x, int y) {
		return drawParagraph(g, text, color, width, DEFAULT_LINE_DISTANCE, x, y);
	}
	
	public int drawParagraph(Graphics g, String text, int width, int x, int y) {
		return drawParagraph(g, text, 0xFF000000, width, DEFAULT_LINE_DISTANCE, x, y);
	}
	
	public void detroy() {
		super.detroy();
	}
	
	/**
	 * Atomic part of the text.
	 * @author Sergey Tkachev <a href="http://sergetk.net">http://sergetk.net</a>
	 */
	public class TextPart {
		int color;
		int x;
		int y;
		int width;
		int height;
		int start, end;
		boolean hasHyphen = false;
	}
}