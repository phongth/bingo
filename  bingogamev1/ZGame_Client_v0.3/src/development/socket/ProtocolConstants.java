package development.socket;

public interface ProtocolConstants {
	public static final int DEFAULT_HEART_BREATH_SEQUENCE_TIME = 10000; // in milisecond
	
	public interface ErrorCode {
		/** Lỗi xảy ra trong trường hợp client này đã đăng nhập từ máy khác rồi */
		int ALREADY_ONLINE_ERROR = -10;
		
		/** Lỗi xảy ra trong trường hợp account này đã bị block */
		int USER_BLOCK_ERROR = -11;
		
		/** Lỗi authenticate thất bại (normal case) */
		int AUTHENTICATE_FAIL_ERROR = -12;
		
		/** Lỗi khi thực hiện 1 action mà chưa authenticate */
		int NO_AUTHEN_ERROR = -20;
		
		int ROOM_FULL_ERROR = -21;
		
		int NOT_ENOUGH_MONEY_TO_JOIN_TABLE_ERROR = -22;
		
		int TABLE_PASSWORD_WRONG_ERROR = -23;
		
		int NOT_ENOUGH_MONEY_TO_SET_TABLE_CONFIG_ERROR = -24;
		
		int NOT_ENOUGN_MONEY_TO_JOIN_ROOM_ERROR = -25;
		
		int TABLE_PASSWORD_TOO_LONG_ERROR = -26;
		
		int TABLE_FULL_ERROR = -27;
		
		int TABLE_ALREADY_PLAY_ERROR = -28;
		
		int USER_NOT_READY_ERROR = -29;
		
		int NOT_ENOUGH_USER_TO_START_GAME_ERROR = -30;
		
		int USER_NAME_EXIST_ERROR = -31;
	  
		int USER_NAME_TOO_LONG_ERROR = -32;
		
		int USER_NAME_NOT_VALID = -33;
		
		int USER_NAME_TOO_SHORT_ERROR = -34;
		
		int PASSWORD_NOT_CORRECT_ERROR = -35;
		
		int USER_NOT_EXIST_ERROR = -36;
		
		int USER_MUST_CHOOSE_GAME = -37;
		
		int NO_MORE_FREE_USER = -38;
	}

	public interface RequestHeader {
		/** Client yêu cầu server đóng kết nối */
		int CLOSE_CONNECTION_REQUEST = 0;
		
		/** Client gửi request định kỳ lên server để giữ kết nối */
		int HEART_BREATH_REQUEST = 1;
		
		/** Client gửi yêu cầu tải resource lên server */
		int DOWNLOADED_RESOURCE_LIST_REQUEST = 2;
		
		/** Client gửi yêu cầu check version */
    int CHECK_VERSION_REQUEST = 3;
    
    
    
    // *************** GAME SERVICE FUNCTION (100 ~ 119) ***********************
    /** Thông báo đăng ký GameService thất bại */
    int GAME_SERVICE_REGISTER_FAIL_REQUEST = 100;
    
    /** Thông báo đăng ký GameService thành công */
    int GAME_SERVICE_REGISTER_SUCCESS_REQUEST = 101;
    
    /** Default service yêu cầu GameService cung cấp thông tin */
    int GAME_SERVICE_INFO_REQUEST = 102;
    
    /** GameService báo cho DefaultService khi có user authen thành công vào GameService */
    int USER_JOIN_IN_GAME_SERVER_INFORM_REQUEST = 103;
    
    /** GameService báo cho DefaultService khi có user out khỏi GameService */
    int USER_OUT_GAME_SERVER_INFORM_REQUEST = 104;
    
    /** GameService cập nhật thông tin cho DefaultService lặp theo chu kỳ */
    int UPDATE_GAME_SERVER_INFO_REQUEST = 105;
    
    /** Client yêu cầu server gửi danh sách GameService */
    int GAME_SERVICE_LIST_REQUEST = 106;
    // ********************************************************
    
    
    
		// *************** AVATAR FUNCTION (120 ~ 139) ***********************
		/** Client gửi yêu cầu tải danh sách category của avatar */
		int AVARAR_CATEGORY_LIST_REQUEST = 120;
		
		/** Client gửi yêu cầu tải danh sách avatar theo category */
		int GET_AVATAR_BY_CATEGORY_REQUEST = 121;
		// ********************************************************

		
		
		// *************** AUTHENTICATE FUNCTION (140 ~ 159) ***********************
		/** Client sign out */
    int SIGN_OUT_REQUEST = 140;
		
		/** Client gửi yêu cầu bắt đầu authenticate (chờ nhận salt từ server) */
		int AUTHENTICATE_REQUEST = 141;
		
		/** Client gửi yêu cầu bắt đầu authenticate bằng mật khẩu */
		int AUTHENTICATE_WITH_PASSWORD_REQUEST = 142;
		
		/** Client gửi yêu cầu bắt đầu authenticate bằng sessionId */
		int AUTHENTICATE_WITH_SESSION_ID_REQUEST = 143;
		
		int USER_INFO_REQUEST = 144;
		
		int REGISTER_REQUEST = 145;
		
		int CHANGE_PASSWORD_REQUEST = 146;
		// ********************************************************
		
		
		
		// *************** GAME - ROOM - TABLE FUNCTION (160 ~ 179) ***********************
		/** Client lấy danh sách room từ GameService */
		int GET_ROOM_LIST_REQUEST = 160;
		
		/** Client lấy danh sách table từ GameService */
		int GET_TABLE_LIST_REQUEST = 161;
		
		int JOIN_GAME_REQUEST = 162;
		int JOIN_ROOM_REQUEST = 163;
		int JOIN_TABLE_REQUEST = 164;
		int LEAVE_ROOM_REQUEST = 165;
		int LEAVE_TABLE_REQUEST = 166;
		int CONFIG_TABLE_REQUEST = 167;
		int SEND_READY_REQUEST = 168;
		int START_GAME_REQUEST = 169;
		int GAME_ACTION_REQUEST = 170;
		int LIST_USER_WANT_TO_PLAY_REQUEST = 171;
		// ********************************************************
		
		
		
		// *************** FRIEND FUNCTION (180 ~ 199) ***********************
		int ADD_FRIEND_REQUEST = 180;
		int ADD_FRIEND_AGREE_REQUEST = 181;
		int ADD_FRIEND_DENY_REQUEST = 182;
		int GET_FRIEND_LIST_REQUEST = 183;
		int REMOVE_FRIEND_REQUEST = 184;
		int BLOCK_FRIEND_REQUEST = 185;
		int UNBLOCK_FRIEND_REQUEST = 186;
		// ********************************************************
		
		
		
		// *************** CHAT FUNCTION (200 ~ 220) ***********************
    int SEND_MESSAGE_REQUEST = 200;  // to (user, table or group) + message
    int INVITE_USER_TO_GROUP_REQUEST = 201; // toUser + groupId (client generate)
    int ACCEPT_JOIN_TO_GROUP_REQUEST = 202; // fromUser + groupId
    int DENY_JOIN_TO_GROUP_REQUEST = 203; // fromUser + groupId
    // ********************************************************
	}

	public interface ResponseHeader {
		/** Báo là client cần phải đóng kết nối */
		int CLOSE_CONNECTION_RESPONSE = 0;
		
		/** Báo với client là connection đã bị ngắt do time out */
		int TIME_OUT_NOTIFY_RESPONSE = 1;
		
		/** Trả về client danh sách resource mà client cần phải lưu lại */
		int RESOURCE_RESPONSE = 2;
		
		/** Trả về client số lượng image mà client cần lưu */
		int TOTAL_DOWNLOAD_IMAGE_RESPONSE = 3;
		
		/** Báo cho client biết là đã download xong resource */
		int DOWNLOAD_RESOURCE_DONE_RESPONSE = 4;
		
		/** Yêu cầu update lại bản client */
    int UPDATE_CLIENT_RESPONSE = 5;
    
    /** Yêu cầu update lại provider ID */
    int UPDATE_PROVIDER_RESPONSE = 6;
    
    /** Báo là client không cần update gì cả */
    int NO_UPDATE_RESPONSE = 7;
		
    
    
    // *************** GAME SERVICE FUNCTION (100 ~ 119) ***********************
    /** GameService trả lại thông tin */
    int GAME_SERVICE_INFO_RESPONSE = 100;
    // ********************************************************
    
    
    
		// *************** AVATAR FUNCTION (120 ~ 139) ***********************
		/** Trả về client danh sách category của avatar */
		int AVATAR_CATEGORY_LIST_RESPONSE = 120;
		
		/** Trả về client danh sách avatar theo category */
		int GET_AVATAR_BY_CATEGORY_RESPONSE = 121;
		
		/** Báo là không tìm thấy avatar mà client yêu cầu */
		int AVATAR_NOT_FOUND_RESPONSE = 122;
		// ********************************************************
		
		
		
		// *************** AUTHENTICATE FUNCTION (140 ~ 159) ***********************
		/** Gửi salt về cho client */
    int SALT_REPONSE = 140;
    
		/** Báo là client authenticate thất bại */
		int AUTHENTICATE_FAIL_RESPONSE = 141;
		
		/** Báo là client authenticate thành công */
		int AUTHENTICATE_SUCCESS_RESPONSE = 142;
		
		/** Default Service báo về cho game Service là session theo username đã yêu cầu không tồn tại */
		int SESSION_NOT_EXIST = 143;
		
		/** Default Service trả về sessionID và salt theo yêu cầu của GameService */
		int SESSION_RESPONSE = 144;
		
		/** GameService báo về cho client là đã đầy */
		int GAME_SERVER_FULL_RESPONSE = 145;
		
		int USER_INFO_REPONSE = 146;
		
		int REGISTER_SUCCESS_RESPONSE = 147;
		int REGISTER_FAIL_RESPONSE = 148;
		
		int CHANGE_PASSWORD_SUCCESS_RESPONSE = 149;
		int CHANGR_PASSWORD_FAIL_RESPONSE = 150;
		// ********************************************************
		
		
		
		// *************** GAME - ROOM - TABLE FUNCTION (160 ~ 179) ***********************
		/** DefautService gửi trả về danh sách GameService */
		int GAME_SERVICE_LIST_RESPONSE = 160;
		
		/** GameService trả về danh sách phòng cho user */
		int ROOM_LIST_RESPONSE = 161;
		
		/** GameService trả về danh sách bàn cho user */
		int TABLE_LIST_RESPONSE = 162;
		
		int JOIN_GAME_SUCCESS_RESPONSE = 163;
		int JOIN_GAME_FAIL_RESPONSE = 164;
		int JOIN_ROOM_SUCCESS_RESPONSE = 165;
		int JOIN_ROOM_FAIL_RESPONSE = 166;
		int JOIN_TABLE_SUCCESS_RESPONSE = 167;
		int JOIN_TABLE_FAIL_RESPONSE = 168;
		int USER_LIST_OF_TABLE_RESPONSE = 169;
		int CONFIG_TABLE_SUCCESS_RESPONSE = 170;
		int CONFIG_TABLE_FAIL_RESPONSE = 171;
		int TABLE_CONFIG_CHANGE_RESPONSE = 172;
		int GAME_START_RESPONSE = 173;
		int GAME_START_FAIL_RESPONSE = 174;
		int GAME_ACTION_RESPONSE = 175;
		int LIST_USER_WANT_TO_PLAY_RESPONSE = 176;
		// ********************************************************
		
				
				
		// *************** FRIEND FUNCTION (180 ~ 199) ***********************
		int ADD_FRIEND_NOTIFY_RESPONSE = 180;
		int ADD_FRIEND_SUCCESS_RESPONSE = 181;
		int ADD_FRIEND_FAIL_USER_DENY_RESPONSE = 182;
		int ADD_FRIEND_FAIL_USER_NOT_EXIST_RESPONSE = 183;
		int FRIEND_LIST_RESPONSE = 184;
		int REMOVE_FRIEND_SUCCESS_RESPONSE = 185;
		int REMOVE_FRIEND_FAIL_RESPONSE = 186;
		int BLOCK_FRIEND_SUCCESS_RESPONSE = 187;
		int BLOCK_FRIEND_FAIL_RESPONSE = 188;
		int UNBLOCK_FRIEND_SUCCESS_RESPONSE = 189;
		int UNBLOCK_FRIEND_FAIL_RESPONSE = 190;
		
    int MAKE_FRIEND_SUCCESS_RESPONSE = 191;
    int MAKE_FRIEND_FAIL_RESPONSE = 192;
    int ALREADY_FRIEND_RESPONSE = 193;
    // ********************************************************
    
    
    
    // *************** CHAT FUNCTION (200 ~ 220) ***********************
    int MESSAGE_RESPONSE = 200;  // fromUser + fromGroup (if only fron User, then fromGroup = fromUser) + message
    int INVITE_USER_TO_GROUP_RESPONSE = 201; // fromUser + groupId
    int ACCEPT_JOIN_TO_GROUP_RESPONSE = 202; // toUser + groupId
    int DENY_JOIN_TO_GROUP_RESPONSE = 203; // toUser + groupId
    // ********************************************************
	}
}
