package matranq.fem.shared;

import com.google.appengine.api.utils.SystemProperty;

public class Constants {
	
	/** GAE APPLICATION **/
	public static final String GAE_APP_ID = SystemProperty.applicationId.get();
	public static final String GAE_APP_SUFFIX = ".appspot.com";
	public static final String PROTOCOL_HTTP = "http://";
	public static final String PROTOCOL_HTTPS = "https://";
	public static final String GAE_APP_URL = PROTOCOL_HTTPS+GAE_APP_ID + GAE_APP_SUFFIX;
	public static final String GAE_DATA_BACKEND="GCS"; //use Drive
	
	/** SERVLET URLs **/
	public static final String SERVLET_URL_LOGIN="/login";
	public static final String SERVLET_URL_USER_TO_BE_REGISTERED="/registerEmailAddress";
	public static final String SERVLET_URL_USER_REGISTERED="/userRegistered.jsp";
	public static final String SERVLET_URL_USER_NOT_REGISTERED_MISSING_DRIVE_FOLDER = "/userNotRegisteredMissingDriveFolder.jsp";
	public static final String SERVLET_URL_INDEX="/index.jsp";
	public static final String SERVLET_URL_ADMIN_INDEX="/adminIndex.jsp";
	public static final String SERVLET_URL_NO_ACCESS="/noAccess.jsp";
	public static final String SERVLET_URL_OAUTH2_CALLBACK=
			(Utils.isGaeProduction())?GAE_APP_URL+"/oauth2callback":"http://localhost:8080/oauth2callback";
	public static final String SERVLET_URL_TASK_MAIL_ELABORATION_USER="/task/gmail/mailElaborationUser";
	public static final String SERVLET_URL_TASK_MAIL_ELABORATION_USER_CHECK="/task/check/mailElaborationUser";
	public static final String SERVLET_URL_TASK_MAIL_ELABORATION_USER_MESSAGE="/task/gmail/mailElaborationUserMessage";
	public static final String SERVLET_URL_TASK_MAIL_ELABORATION_CHECK="/task/check/mailElaboration";
	public static final String SERVLET_URL_TASK_ACTIVITY_REPORT_GENERATION="/task/report/activityReportGeneration";
	public static final String SERVLET_URL_TASK_FIND_SIMULTANEOUS_EMAIL="/task/findSimultaneousEmail";
	public static final String SERVLET_URL_TASK_FIND_SIMULTANEOUS_EMAIL_REPORT="/task/fixesReportGeneration";
	public static final String SERVLET_URL_TASK_FIND_SIMULTANEOUS_EMAIL_FILTERED_REPORT="/task/filterFixesReportGeneration";
	public static final String SERVLET_URL_TASK_LABEL_EMAIL_TO_FIX="/task/labelEmailToFix";
	
	
	/** HTML CODES **/
	public static final int HTML_CODE_200_OK=200;
	public static final int HTML_CODE_400_BAD_REQUEST=400;
	public static final int HTML_CODE_401_UNAUTHORIZED=401;
	public static final int HTML_CODE_404_NOT_FOUND=404;
	public static final int HTML_CODE_500_INTERNAL_SERVER_ERROR=500;
	public static final String HTML_CODE_401_UNAUTHORIZED_MESSAGE="The client is not authorized to use this API.";
	
	/** API **/
	public static final String API_PARAM_SECURITY_CODE="securityCode";
	public static final String API_PARAM_USER_NAME="userName";
	public static final String API_PARAM_USER_TYPE="userType";
	public static final String API_PARAM_USER_METHOD="method";
	public static final String API_PARAM_VALUE_USER_TYPE_USER="user";
	public static final String API_PARAM_VALUE_USER_TYPE_ADMIN="admin";
	public static final String API_PARAM_VALUE_USER_TYPE_PENDING="pending";
	public static final String API_PARAM_VALUE_USER_TYPE_TECH="tech";
	public static final String API_PARAM_VALUE_USER_METHOD_ADD="add";
	public static final String API_PARAM_VALUE_USER_METHOD_REMOVE="remove";
	public static final String API_PARAM_VALUE_SECURITY_CODE_METHOD_ADD="add";
	public static final String API_PARAM_VALUE_SECURITY_CODE_METHOD_REMOVE="remove";
	public static final String API_PARAM_REPORT_ACTIVITY_ID="reportActivityId";
	public static final String API_PARAM_DRIVE_FILE_ID="driveFileId";
	public static final String API_PARAM_MAIL_ELABORATION_ID="idMailElaboration";
	public static final String API_PARAM_MAIL_ELABORATION_USER_ID="idMailElaborationUser";
	public static final String API_PARAM_GMAIL_MESSAGE_ID="idGmailMessage";
	public static final String API_PARAM_GCS_FILE_NAME="gcsFileName";
	public static final String API_PARAM_GCS_FOLDER_NAME="gcsFolderName";

	public static final String SEND_TO = "sendTo";
	
	/** LOG **/
	public static final String LOG_LEVEL_WARNING="WARNING";
	public static final String LOG_LEVEL_SEVERE="SEVERE";
	public static final String LOG_LEVEL_INFO="INFO";
	
	/** VARIOUS **/
	public static final String MIME_TYPE_APPLICATION_JSON="application/json";
}