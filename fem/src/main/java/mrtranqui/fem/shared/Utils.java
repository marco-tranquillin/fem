package mrtranqui.fem.shared;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import mrtranqui.fem.datastore.DatastoreService;
import mrtranqui.fem.datastore.entity.Log;

public class Utils {

	private static final Logger LOG = Logger.getLogger(Utils.class.getName());
	
	/**
	 * Convert an object to JSON
	 * @param obj the object to be encoded
	 * @return encoded object
	 * @throws JsonProcessingException 
	 */
	public static String toJSON(Object obj) throws JsonProcessingException{
		Gson gson=new Gson();
		return gson.toJson(obj);
	}
	
	/**
	 * Convert a JSON string to an Object
	 * @param json the json to be converted
	 * @param destinationClass the object to be generated
	 * @return encoded object
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static <T> T fromJSON(String json, Class<T> destinationClass){
		Gson gson=new Gson();
		return gson.fromJson(json, destinationClass);
	}
	
	/**
	 * Check if the app is running in production or in local dev
	 * @return true if it is in production, false otherwise
	 */
	public static boolean isGaeProduction(){
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	}
	
	/**
	 * Check if the running environment is the dev
	 * @return true if it is the dev, false otherwise
	 */
	public static boolean isGaeDevEnvironment(){
		return !isGaeProduction();
	}
	
	/**
	 * Get input parameters if they are stored in JSON format
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public static Map<String,String > getRequestParameters(HttpServletRequest req) throws IOException{
		//read input data
		InputStream is = req.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r=0;
        while( r >= 0 ) {
            r = is.read(buf);
            if( r >= 0 ) os.write(buf, 0, r);
        }
        
        String inputParams=new String(os.toByteArray(), "UTF-8");
        Gson gson=new Gson();
        
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> map = gson.fromJson(inputParams, type);
         
        return map;
	}
	
	/**
     * Get execution task of a task
     * @param req the request of the task
     * @return the execution time of a task if exist
     */
    public static int getExecutionCount(HttpServletRequest req) {
        String header = req.getHeader("X-AppEngine-TaskExecutionCount");
		if(header != null && !header.isEmpty())
        	return Integer.parseInt(header);
		return 0;
    }
    
    /**
     * Get execution task name 
     * @param req the request of the task
     * @return the execution time of a task if exist
     */
    public static String getExecutionName(HttpServletRequest req) {
        String header = req.getHeader("X-AppEngine-TaskName");
        if (header == null) {
            return null;
        } else {
            return header;
        }
    }
    
    /**
     * Convert a String array in a single string where all tokens are separated by a comma
     * @param input the String array that you want to split
     * @param tokenSeparator the character that you want to use as token separator
     * @return a string containing all elements of the input array separated by the token separator character
     */
    public static String fromArrayToString(String[] input,char tokenSeparator){
    	StringBuilder result=new StringBuilder("");
    	int i=0;
    	for(String token:input){
    		if(i>0){
    			result.append(tokenSeparator);
    		}
    		result.append(token);
    		i++;
    	}
    	return result.toString();
    }
    
    
    /**
     * Enqueue a task into Google App Engine
     * @param queueName the name of the queue where to enqueue the task
     * @param taskUrl the URL of the task to be enqueued
     * @param taskName the name of the task to be enqueued
     * @param method the method to use (POST/GET)
     * @param parametersMap the parameters to be added to the task
     * @param delay the eventual delay to add to the task
     */
    public static void enqueueTask(String queueName,String taskUrl,String taskName,Method method,Map<String, String> parametersMap,long delay){
    	
    	//prepare task options
        final TaskOptions taskOptions=TaskOptions.Builder
        			  .withUrl(taskUrl)
        			  .taskName(taskName)
        			  .method(method)
        			  ;
        
        //add parameters
        for(String key: parametersMap.keySet()){
        	taskOptions.param(key, parametersMap.get(key));
        }
        
        //add eventual delay			  
        if(delay>0){
        	taskOptions.countdownMillis(delay);
        }
        
        //create the queue
        final Queue queue = QueueFactory.getQueue(queueName);
        
        Callable<Boolean> callable = new Callable<Boolean>() {
		    public Boolean call() throws Exception {
		    	queue.add(taskOptions);
		        return true;
		    }
		};

		Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
		        .retryIfException()
		        .withWaitStrategy(WaitStrategies.exponentialWait(100, 5, TimeUnit.MINUTES))
		        .withStopStrategy(StopStrategies.stopAfterAttempt(5))
		        .build();
		try {
				retryer.call(callable);
		} catch (RetryException e) {
			LOG.warning("enqueueTask() failed.\nStackTrace: " + Throwables.getStackTraceAsString(e));
		} catch (ExecutionException e) {
			LOG.warning("enqueueTask() failed.\nStackTrace: " + Throwables.getStackTraceAsString(e));
		}
		
		return;
    }
    
    /**
     * Get headers of a HttpServletRequest
     * @param request the request to be parsed
     * @return map of request headers
     */
	public static Map<String, String> getHeadersInfo(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}
		return map;
	}
	
	/**
	 * Generate a random secure token
	 * @return String the token that will be used to authenticate calls
	 */
	public static String generateToken(){
		return UUID.randomUUID().toString();
	}
	
    
	/**
	 * Get JSON payload
	 * @param request the request that has to be processed
	 * @return payload of the request
	 * @throws IOException
	 */
	public static JsonObject getJsonPayload(HttpServletRequest request) throws IOException {
		JsonParser jsonParser = new JsonParser();
		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return jsonParser.parse(body).getAsJsonObject();
	}
	
	/**
	 * Convert a bytes array into a HEX string
	 * @param bytes the bytes array that you want to convert
	 * @return an HEX string representing the bytes array
	 */
	public static String toHexString(byte[] bytes) {
	    StringBuilder hexString = new StringBuilder();

	    for (int i = 0; i < bytes.length; i++) {
	        String hex = Integer.toHexString(0xFF & bytes[i]);
	        if (hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }

	    return hexString.toString().toUpperCase();
	}
	
	/**
	 * Convert JsonObject to HashMap<String,String>
	 * @param json the json containing the data to be converted
	 * @return converted data
	 */
	public static Map<String,String> jsonToHashMap(JsonObject json){
		Gson gson=new Gson();
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> myMap = gson.fromJson(json, type);
		return myMap;
	}
	
	
	/**
	 * Generate a random Long between a range
	 * @param min min value of the range
	 * @param max max value of the range
	 * @return an integer in the range
	 */
	public static int randomInt(int min,int max){
		Random rn = new Random();
		int n = max - min + 1;
		int i = rn.nextInt() % n;
		return  Math.abs(min + i);
		
	}
	
	/**
	 * Perform API call
	 * @param url url that you want to call
	 * @param method GET/POST/PUT/DELETE/HEAD
	 * @param payload eventual payload. Null if empty
	 * @return response of the remote object
	 * @throws IOException
	 */
	public static String apiCall(String url,String method,String payload) throws IOException{
		URL myURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(method);
			conn.setInstanceFollowRedirects(false);
			conn.setConnectTimeout(1000*60);

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		if(payload!=null){
			writer.write(payload);
			writer.close();
		}

		
		int respCode = conn.getResponseCode();
		if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_FOUND) {
		  StringBuffer response = new StringBuffer();
		  String line;

		  BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		  while ((line = reader.readLine()) != null) {
		    response.append(line);
		  }
		  reader.close();
		  return response.toString();
		  
		} else {
		  return (conn.getResponseCode() + " " + conn.getResponseMessage());
		}
	}
	
	/**
	 * Fetch an URL asynchronously
	 * @param url the URL that you want to call
	 * @throws IOException
	 */
	public static void asyncURLfetch(String url) throws IOException{
		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
		URL myURL = new URL(url);
        Future future = fetcher.fetchAsync(myURL);
        return;
	}

	/**
	 * Save Log
	 * 
	 * @param log
	 *            the log that you want to save
	 */
	public static void saveLog(Log log) {
		DatastoreService<Log> dsLog = new DatastoreService<Log>(Log.class);
		dsLog.save(log);
	}

	/**
	 * Returns a random salt to be used to hash a password.
	 *
	 * @return a 16 bytes random salt
	 */
	public static byte[] getNextSalt() {
		byte[] salt = new byte[16];
		Random rnd = new SecureRandom();
		rnd.nextBytes(salt);
		return salt;
	}

	/**
	 * Returns a salted and hashed password using the provided hash.<br>
	 * Note - side effect: the password is destroyed (the char[] is filled with
	 * zeros)
	 *
	 * @param password
	 *            the password to be hashed
	 * @param salt
	 *            a 16 bytes salt, ideally obtained with the getNextSalt method
	 *
	 * @return the hashed password with a pinch of salt
	 */
	public static byte[] hash(char[] password, byte[] salt) {
		int ITERATIONS = 10000;
		int KEY_LENGTH = 256;
		PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
		Arrays.fill(password, Character.MIN_VALUE);
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			return skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
		} finally {
			spec.clearPassword();
		}
	}

}