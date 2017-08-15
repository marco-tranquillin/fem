package mrtranqui.fem.servlet.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import mrtranqui.fem.data.RestResponse;
import mrtranqui.fem.datastore.DatastoreService;
import mrtranqui.fem.datastore.entity.User;
import mrtranqui.fem.shared.Constants;
import mrtranqui.fem.shared.Utils;

import java.security.Key;

public class PostLogin extends HttpServlet {

	private static final long serialVersionUID = 5923330302646175252L;
	private static final Logger LOG = Logger.getLogger(PostLogin.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		//manage dev env
		if(!Utils.isGaeProduction())	{		
			resp.addHeader("Access-Control-Allow-Origin", Constants.GAE_DEV_ADDRESS);
		}
		//include cookies
		resp.addHeader("Access-Control-Allow-Credentials","true");
		
		//response variables
		RestResponse restResponse=new RestResponse();
		Gson gson=new Gson(); 
    	
    	//get parameters
    	JsonObject payload=Utils.getJsonPayload(req);
    	if(payload.get(Constants.API_PARAM_USER_PASSWORD)==null){	
    		LOG.info(Constants.HTML_CODE_400_BAD_REQUEST + " - " + "USER NAME MISSING");
    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
    		restResponse.setCode(Constants.HTML_CODE_400_BAD_REQUEST);
    		restResponse.setMessage("User name mising.");
    		resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
    		resp.getWriter().println(gson.toJson(restResponse));
    		return;
    	}
    	
    	if(payload.get(Constants.API_PARAM_USER_PASSWORD)==null){	
    		LOG.info(Constants.HTML_CODE_400_BAD_REQUEST + " - " + "USER PASSWORD MISSING");
    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
    		restResponse.setCode(Constants.HTML_CODE_400_BAD_REQUEST);
    		restResponse.setMessage("User password mising.");
    		resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
    		resp.getWriter().println(gson.toJson(restResponse));
    		return;
    	}
    	
    	String userName=payload.get(Constants.API_PARAM_USER_NAME).getAsString();
    	String userPassword=payload.get(Constants.API_PARAM_USER_PASSWORD).getAsString();
    	
    	//search for user
		DatastoreService<User> dsUser=new DatastoreService<User>(User.class);
		User registeredUser=dsUser.get(userName);
		if(registeredUser==null){ //the user does not exist
			LOG.info(Constants.HTML_CODE_403_FORBIDDEN + " - " + "USER " + userName + " IS NOT REGISTERED");
    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
    		restResponse.setCode(Constants.HTML_CODE_403_FORBIDDEN);
    		restResponse.setMessage("Utente non registrato.");
    		resp.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
    		resp.getWriter().println(gson.toJson(restResponse));
    		return;
		} 
		else{
			//get credentials
			byte[] password=Utils.hash(userPassword.toCharArray(), registeredUser.getSalt());
			if(!Arrays.equals(password, registeredUser.getPassword())){
				//password not correct
				LOG.info(Constants.HTML_CODE_403_FORBIDDEN + " - " + "USER " + userName + " IS NOT REGISTERED");
	    		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
	    		restResponse.setCode(Constants.HTML_CODE_403_FORBIDDEN);
	    		restResponse.setMessage("Password non corretta.");
	    		resp.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
	    		resp.getWriter().println(gson.toJson(restResponse));
	    		return;
			}
		}
			
		//user correctly logged in
		//check JWT token validity
		if(registeredUser.getExpirationDate()<=new java.util.Date().getTime()){
			//token expired --> updated it
			byte[] encodedJwtEncryptionKey = Base64.decodeBase64(Constants.JWT_ENCRYPTION_KEY);
	    	SecretKey jwtEncryptionKey = new SecretKeySpec(encodedJwtEncryptionKey, 0, encodedJwtEncryptionKey.length, "HmacSHA512");
	    	java.util.Date jwtExpirationDate=new java.util.Date(new java.util.Date().getTime() + 60*60*1000);
	    	String jwtToken = Jwts.builder()
	    			  .setSubject(userName)
	    			  .setExpiration(jwtExpirationDate)
	    			  .signWith(SignatureAlgorithm.HS512, jwtEncryptionKey)
	    			  .compact();
	    	//update data
	    	registeredUser.setJwt(jwtToken);
	    	registeredUser.setExpirationDate(jwtExpirationDate.getTime());
	    	dsUser.save(registeredUser);
		}
		
		LOG.info(Constants.HTML_CODE_200_OK + " - " + "USER " + userName + " CORRECTLY LOGGED IN");
		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
		restResponse.setCode(Constants.HTML_CODE_200_OK);
		restResponse.setMessage(registeredUser.getJwt());
		resp.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
		resp.getWriter().println(gson.toJson(restResponse));
		return;
		
		
		
	}

}