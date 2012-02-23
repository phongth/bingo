package state.socket;

public interface DefaultProtocolConstants {
	public static final int DEFAULT_HEART_BREATH_SEQUENCE_TIME = 10000; // in
																		// milisecond

	public interface ErrorCode {
	}

	public interface RequestHeader {
		/** Client yêu cầu server đóng kết nối */
		int CLOSE_CONNECTION_REQUEST = 0;

		/** Client gửi request định kỳ lên server để giữ kết nối */
		int HEART_BREATH_REQUEST = 1;
	}

	public interface ResponseHeader {
		/** Báo là client cần phải đóng kết nối */
		int CLOSE_CONNECTION_RESPONSE = 0;

		/** Báo với client là connection đã bị ngắt do time out */
		int TIME_OUT_NOTIFY_RESPONSE = 1;
	}
}
