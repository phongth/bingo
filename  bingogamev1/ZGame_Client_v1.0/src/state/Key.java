package state;

import javax.microedition.lcdui.game.GameCanvas;



public class Key {
    public static final byte UP = 0;
    public static final byte DOWN = 1;
    public static final byte RIGHT = 2;
    public static final byte LEFT = 3;
    public static final byte K_0 = 4;
    public static final byte K_1 = 5;
    public static final byte K_2 = 6;
    public static final byte K_3 = 7;
    public static final byte K_4 = 8;
    public static final byte K_5 = 9;
    public static final byte K_6 = 10;
    public static final byte K_7 = 11;
    public static final byte K_8 = 12;
    public static final byte K_9 = 13;
    public static final byte STAR = 14;
    public static final byte POUND = 15;
    public static final byte FIRE = 16;
    public static final byte SOFT_LEFT = 17;
    public static final byte SOFT_RIGHT = 18;
    public static final byte END = 19;
    public static final byte CLR = 20;
    public static final byte BACKSPACE = 21;
    public static final byte ENTER = 22;
    public static final byte SHIFT = 23;
    public static boolean isCommandKey = false;
    
    public static int getGameKey(int keyCode) {
        if (GameConstants.IS_240x320_SCREEN) {
            return getGameKey240x320(keyCode);
        }
        return getGameKey320x240(keyCode);
    }

    private static int getGameKey320x240(int keyCode) {
    	isCommandKey = true;
        switch (keyCode) {
            case -3:
                keyCode = LEFT;
                break;
            case -4:
                keyCode = RIGHT;
                break;
            case -1:
                keyCode = UP;
                break;
            case -2:
                keyCode = DOWN;
                break;
            case -6:
                keyCode = SOFT_LEFT;
                break;
            case -7:
                keyCode = SOFT_RIGHT;
                break;
            case 35:
                keyCode = POUND;
                break;
            case 42:
                keyCode = STAR;
                break;
            case -5:
                keyCode = FIRE;
                break;
            case 8:
            case -8:
                keyCode = BACKSPACE;
                break;
            case 10:
                keyCode = ENTER;
                break;
            case -50:
            	keyCode = SHIFT;
            	break;
            default:
            	isCommandKey = false;
                break;
        }
        return keyCode;
    }

    private static int getGameKey240x320(int keyCode) {
    	isCommandKey = true;
        switch (keyCode) {
            case GameCanvas.KEY_NUM0:
                keyCode = K_0;
                break;
            case GameCanvas.KEY_NUM1:
                keyCode = K_1;
                break;
            case GameCanvas.KEY_NUM2:
                keyCode = K_2;
                break;
            case GameCanvas.KEY_NUM3:
                keyCode = K_3;
                break;
            case GameCanvas.KEY_NUM4:
                keyCode = K_4;
                break;
            case GameCanvas.KEY_NUM5:
                keyCode = K_5;
                break;
            case GameCanvas.KEY_NUM6:
                keyCode = K_6;
                break;
            case GameCanvas.KEY_NUM7:
                keyCode = K_7;
                break;
            case GameCanvas.KEY_NUM8:
                keyCode = K_8;
                break;
            case GameCanvas.KEY_NUM9:
                keyCode = K_9;
                break;
            case -6:
                keyCode = SOFT_LEFT;
                break;
            case -7:
                keyCode = SOFT_RIGHT;
                break;
            case 35:
                keyCode = POUND;
                break;
            case 42:
                keyCode = STAR;
                break;
            case 8:
            case -8:
                keyCode = BACKSPACE;
                break;
            default:
                int gameAction = GameGlobal.systemCanvas.getGameAction(keyCode);
                switch (gameAction) {
                    case GameCanvas.UP:
                        keyCode = UP;
                        break;
                    case GameCanvas.DOWN:
                        keyCode = DOWN;
                        break;
                    case GameCanvas.RIGHT:
                        keyCode = RIGHT;
                        break;
                    case GameCanvas.LEFT:
                        keyCode = LEFT;
                        break;
                    case GameCanvas.FIRE:
                        keyCode = FIRE;
                    default:
                    	isCommandKey = false;
                        break;
                }
                break;
        }
        return keyCode;
    }
}
