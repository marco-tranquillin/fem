package mrtranqui.fem.shared;

import com.google.appengine.api.utils.SystemProperty;

public class Constants {
	
	/** GAE APPLICATION **/
	public static final String GAE_APP_ID = SystemProperty.applicationId.get();
	public static final String GAE_APP_SUFFIX = ".appspot.com";
	public static final String PROTOCOL_HTTP = "http://";
	public static final String PROTOCOL_HTTPS = "https://";
	public static final String GAE_APP_URL = PROTOCOL_HTTPS+GAE_APP_ID + GAE_APP_SUFFIX;
	public static final String GAE_DATA_BACKEND="GCS"; //use Drive
	
	/** API URLs **/
	public static final String API_URL_PING="/api/ping";
	public static final String API_URL_REGISTER="/api/register";
	
	
	/** HTML CODES **/
	public static final int HTML_CODE_200_OK=200;
	public static final int HTML_CODE_400_BAD_REQUEST=400;
	public static final int HTML_CODE_401_UNAUTHORIZED=401;
	public static final int HTML_CODE_403_FORBIDDEN=403;
	public static final int HTML_CODE_404_NOT_FOUND=404;
	public static final int HTML_CODE_409_CONFLICT=409;
	public static final int HTML_CODE_500_INTERNAL_SERVER_ERROR=500;
	public static final String HTML_CODE_401_UNAUTHORIZED_MESSAGE="The client is not authorized to use this API.";
	
	/** API **/
	public static final String API_PARAM_USER_NAME="userName";
	public static final String API_PARAM_USER_PASSWORD="userPassword";
	
	/** LOG **/
	public static final String LOG_LEVEL_WARNING="WARNING";
	public static final String LOG_LEVEL_SEVERE="SEVERE";
	public static final String LOG_LEVEL_INFO="INFO";
	
	/** VARIOUS **/
	public static final String MIME_TYPE_APPLICATION_JSON="application/json";
	
	/** USERS **/
	public static final String USER_TYPE_EMPLOYEE="EMPLOYEE";
	public static final String USER_TYPE_ADMIN="ADMIN";
	public static final String USER_TYPE_SUPER_ADMIN="SUPER_ADMIN";
	
	/** JWT **/
	public static final String JWT_ENCRYPTION_KEY="S46bUc0cr03yynXxMhZo3h6Wt5fSpAbN6cDeAgFACspvSWlxyzbpmWQ4toqcyJ015LIcxzXVuotUjRqWtF7YOQ==";
	
}