package state.util;

/**
 * @version 0.2
 */
public class Point {
	public int x;
	public int y;

	/**
	 * Constructor
	 * 
	 * @param x
	 *            - Tọa độ x
	 * @param y
	 *            - Tọa độ y
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * So sánh 2 Point
	 * 
	 * @param point
	 *            - Point cần so sánh
	 * @return 2 Point cần so sánh có bằng nhau hay không
	 */
	public boolean equals(Point point) {
		return (x == point.x) && (y == point.y);
	}

	/**
	 * Tạo 1 Point mới chứa các giá trị của Point hiện tại
	 * 
	 * @return
	 */
	public Point clone() {
		return new Point(x, y);
	}

	/**
	 * Thực hiện tạo mới Point hoặc chỉ thay đổi giá trị point đầu vào nếu point
	 * đầu vào khác null Hàm này dùng để tránh phải tạo Point mới trong mỗi lần
	 * dùng
	 * 
	 * @param point
	 *            - Point cần kiểm tra
	 * @param x
	 *            - Tọa độ x thiết lập
	 * @param y
	 *            - Tọa độ y thiết lập
	 * @return Point trả ra
	 */
	public static Point createNewOrSetValue(Point point, int x, int y) {
		if (point == null) {
			return new Point(x, y);
		} else {
			point.x = x;
			point.y = y;
			return point;
		}
	}

	/**
	 * Thực hiện phép cộng tọa độ của 2 Point
	 * 
	 * @param p1
	 *            - Point thứ nhất
	 * @param p2
	 *            - point thứ hai
	 */
	public static Point add(Point p1, Point p2) {
		Point tmp = new Point(0, 0);
		tmp.x = p1.x + p2.x;
		tmp.y = p1.y + p2.y;
		return tmp;
	}
}
