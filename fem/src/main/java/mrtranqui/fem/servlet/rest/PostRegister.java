package mrtranqui.fem.servlet.rest;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import mrtranqui.fem.data.RestResponse;
import mrtranqui.fem.datastore.DatastoreService;
import mrtranqui.fem.datastore.entity.User;
import mrtranqui.fem.shared.Constants;
import mrtranqui.fem.shared.Utils;

public class PostRegister extends HttpServlet {

	private static final long serialVersionUID = 5923330302646175252L;
	private static final Logger LOG = Logger.getLogger(PostRegister.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		//response variables
		RestResponse restResponse=new RestResponse();
		Gson gson=new Gson();
				
		//get parameters
    	Map<String,String[]> parameters=req.getParameterMap();
    	
    	//check username
    	if(parameters.get(Constants.API_PARAM_USER_NAME)==null){	
    		LOG.info(Constants.HTML_CODE_400_BAD_REQUEST + " - " + "USERNAME MISSING");
    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
    		restResponse.setCode(Constants.HTML_CODE_400_BAD_REQUEST);
    		restResponse.setMessage("User name mising.");
    		resp.getWriter().println(gson.toJson(restResponse));
    		return;
    	}
    	
    	//check password
    	if(parameters.get(Constants.API_PARAM_USER_PASSWORD)==null){	
    		LOG.info(Constants.HTML_CODE_400_BAD_REQUEST + " - " + "USER PASSWORD MISSING");
    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
    		restResponse.setCode(Constants.HTML_CODE_400_BAD_REQUEST);
    		restResponse.setMessage("User password mising.");
    		resp.getWriter().println(gson.toJson(restResponse));
    		return;
    	}
    	
    	//get parameters
    	String userName=parameters.get(Constants.API_PARAM_USER_NAME)[0];
    	String userPassword=parameters.get(Constants.API_PARAM_USER_PASSWORD)[0];
    	
    	//generate JWT token
    	byte[] encodedJwtEncryptionKey = Base64.decodeBase64(Constants.JWT_ENCRYPTION_KEY);
    	SecretKey jwtEncryptionKey = new SecretKeySpec(encodedJwtEncryptionKey, 0, encodedJwtEncryptionKey.length, "HmacSHA512");
    	java.util.Date jwtExpirationDate=new java.util.Date(new java.util.Date().getTime() + 60*60*1000);
    	String jwtToken = Jwts.builder()
    			  .setSubject(userName)
    			  .setExpiration(jwtExpirationDate)
    			  .signWith(SignatureAlgorithm.HS512, jwtEncryptionKey)
    			  .compact();
    	
    	//register user
		DatastoreService<User> dsUser=new DatastoreService<User>(User.class);
		User registeredUser=dsUser.get(userName);
		if(registeredUser!=null){ //the user is already present
			LOG.info(Constants.HTML_CODE_409_CONFLICT + " - " + "USER " + userName + " IS ALREADY PRESENT");
    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
    		restResponse.setCode(Constants.HTML_CODE_409_CONFLICT);
    		restResponse.setMessage("User " + userName + " is already present.");
    		resp.getWriter().println(gson.toJson(restResponse));
    		return;
		} 
		else{
			//register new user
			registeredUser=new User();
			registeredUser.setLogin(userName);
			byte[] salt=Utils.getNextSalt();
			registeredUser.setPassword(Utils.hash(userPassword.toCharArray(), salt));
			registeredUser.setSalt(salt);
			registeredUser.setJwt(jwtToken);
			registeredUser.setExpirationDate(jwtExpirationDate.getTime());
			registeredUser.setType(Constants.USER_TYPE_EMPLOYEE);
			dsUser.save(registeredUser);
		}
			
		LOG.info(Constants.HTML_CODE_200_OK + " - " + "USER " + userName + " CORRECTLY REGISTERED");
		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
		restResponse.setCode(Constants.HTML_CODE_200_OK);
		restResponse.setMessage("User " + userName + " correctly registered");
		resp.getWriter().println(gson.toJson(restResponse));
		return;
		
		
		
	}

}