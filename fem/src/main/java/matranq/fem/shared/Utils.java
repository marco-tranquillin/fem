package matranq.fem.shared;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Throwables;
import com.google.gson.Gson;

import matranq.fem.datastore.DatastoreService;
import matranq.fem.datastore.entity.Log;

public class Utils {

	private static final Logger LOG = Logger.getLogger(Utils.class.getName());

	/**
	 * Convert an object to JSON
	 * 
	 * @param obj
	 *            the object to be encoded
	 * @return encoded object
	 */
	public static String toJSON(Object obj) {
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	/**
	 * Convert a JSON string to an Object
	 * 
	 * @param json
	 *            the json to be converted
	 * @param destinationClass
	 *            the object to be generated
	 * @return encoded object
	 */
	public static <T> T fromJSON(String json, Class<T> destinationClass) {
		Gson gson = new Gson();
		return gson.fromJson(json, destinationClass);
	}

	/**
	 * Check if the app is running in production or in local dev
	 * 
	 * @return true if it is in production, false otherwise
	 */
	public static boolean isGaeProduction() {
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	}

	/**
	 * Check if the running environment is the dev
	 * 
	 * @return true if it is the dev, false otherwise
	 */
	public static boolean isGaeDevEnvironment() {
		if (SystemProperty.applicationId.get() != null)
			return SystemProperty.applicationId.get().contains("dev");
		return true;
	}

	/**
	 * Get execution task of a task
	 * 
	 * @param req
	 *            the request of the task
	 * @return the execution time of a task if exist
	 */
	public static int getExecutionCount(HttpServletRequest req) {
		String header = req.getHeader("X-AppEngine-TaskExecutionCount");
		if (header != null && !header.isEmpty())
			return Integer.parseInt(header);
		return 0;
	}

	/**
	 * Get execution task name
	 * 
	 * @param req
	 *            the request of the task
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
	 * Convert a String array in a single string where all tokens are separated
	 * by a comma
	 * 
	 * @param input
	 *            the String array that you want to split
	 * @param tokenSeparator
	 *            the character that you want to use as token separator
	 * @return a string containing all elements of the input array separated by
	 *         the token separator character
	 */
	public static String fromArrayToString(String[] input, char tokenSeparator) {
		StringBuilder result = new StringBuilder("");
		int i = 0;
		for (String token : input) {
			if (i > 0) {
				result.append(tokenSeparator);
			}
			result.append(token);
			i++;
		}
		return result.toString();
	}

	/**
	 * Enqueue a task into Google App Engine
	 * 
	 * @param queueName
	 *            the name of the queue where to enqueue the task
	 * @param taskUrl
	 *            the URL of the task to be enqueued
	 * @param taskName
	 *            the name of the task to be enqueued
	 * @param method
	 *            the method to use (POST/GET)
	 * @param parametersMap
	 *            the parameters to be added to the task
	 * @param delay
	 *            the eventual delay to add to the task
	 */
	public static void enqueueTask(String queueName, String taskUrl, String taskName, Method method,
			Map<String, String> parametersMap, long delay) {

		// prepare task options
		final TaskOptions taskOptions = TaskOptions.Builder.withUrl(taskUrl).taskName(taskName).method(method);

		// add parameters
		for (String key : parametersMap.keySet()) {
			taskOptions.param(key, parametersMap.get(key));
		}

		// add eventual delay
		if (delay > 0) {
			taskOptions.countdownMillis(delay);
		}

		// create the queue
		final Queue queue = QueueFactory.getQueue(queueName);

		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				queue.add(taskOptions);
				return true;
			}
		};

		Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder().retryIfException()
				.withWaitStrategy(WaitStrategies.exponentialWait(100, 5, TimeUnit.MINUTES))
				.withStopStrategy(StopStrategies.stopAfterAttempt(5)).build();
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