package state;

import java.util.Hashtable;

import javax.microedition.midlet.MIDlet;

public class GameGlobal {
  public static void init() {
    // Xác định kích cỡ màn hình hiện tại là màn hình ngang hay màn hình dọc
    systemCanvas = new SystemCanvas();
    if (systemCanvas.getWidth() >= 320) {
      GameConstants.IS_240x320_SCREEN = false;
      GameConstants.SCREEN_WIDTH = 320;
      GameConstants.SCREEN_HEIGHT = 240;
    }
  }
  
	public static SystemCanvas systemCanvas;
	public static Alert alert;
	
	// Các biến để download resource
	public static int totalDownloadResource = Integer.MAX_VALUE;
	public static int downloadedCount = 0;
	public static Hashtable imageLocationTable = ResourceRms.loadMasterRecord();
	
	/**
   * Tiến hành chuyển sang State tiếp theo mà không xử dụng hiệu ứng transform
   * 
   * @param state - State tiếp theo
   * @param params - Bảng chứa các tham số cần chuyển cho State tiếp theo
   */
	public static void nextState(GameForm state, Hashtable params) {
    systemCanvas.nextState(state, params);
  }
  
	/**
   * Tiến hành chuyển sang State tiếp theo có sử dụng các hiệu ứng Transform Các hiệu ứng transoform có thể sử dụng nằm trong class {@link Transformer} <br>
   * 
   * @param state - State tiếp theo
   * @param params - Bảng chứa các tham số cần chuyển cho State tiếp theo
   * @param transformType - Kiểu hiệu ứng transform sử dụng khi chuyển form 
   */
  public static void nextState(GameForm state, Hashtable params, int transformType) {
    systemCanvas.nextState(state, params, transformType);
  }
	
  /**
   * Tiến hành chuyển sang State tiếp theo mà không xử dụng hiệu ứng transform
   * 
   * @param state - State tiếp theo
   * @param params - Bảng chứa các tham số cần chuyển cho State tiếp theo
   * @param backForm - Form để quay lại khi gọi hàm goBack
   */
	public static void nextState(GameForm state, Hashtable params, GameForm backForm) {
	  state.backForm = backForm;
		systemCanvas.nextState(state, params);
	}
	
	/**
   * Tiến hành chuyển sang State tiếp theo có sử dụng các hiệu ứng Transform Các hiệu ứng transoform có thể sử dụng nằm trong class {@link Transformer} <br>
   * 
   * @param state - State tiếp theo
   * @param params - Bảng chứa các tham số cần chuyển cho State tiếp theo
   * @param backForm - Form để quay lại khi gọi hàm goBack
   * @param transformType - Kiểu hiệu ứng transform sử dụng khi chuyển form 
   */
	public static void nextState(GameForm state, Hashtable params, GameForm backForm, int transformType) {
	  state.backForm = backForm;
		systemCanvas.nextState(state, params, transformType);
	}
	
	public static MIDlet getMidlet() {
	  return systemCanvas.getMidlet();
	}
	
	public static void setMidlet(MIDlet midlet) {
	  systemCanvas.setMidlet(midlet);
	}
	
	public static void setTimerDelay(int delay) {
	  systemCanvas.setTimerDelay(delay);
	}
}
