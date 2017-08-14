package mrtranqui.fem.servlet.security;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import mrtranqui.fem.data.RestResponse;
import mrtranqui.fem.datastore.DatastoreService;
import mrtranqui.fem.datastore.entity.User;
import mrtranqui.fem.shared.Constants;

import javax.servlet.http.HttpServletRequest;


public class RestSecurityFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(RestSecurityFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    	//before filter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
    	RestResponse restResponse=new RestResponse();
		Gson gson=new Gson();
		
    	// Get the HTTP Authorization header from the request
        String authorizationHeader = 
            ((HttpServletRequest)request).getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly 
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        	//respond with NOT AUTHORIZED
			response.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
			restResponse.setCode(Constants.HTML_CODE_401_UNAUTHORIZED);
			restResponse.setMessage(Constants.HTML_CODE_401_UNAUTHORIZED_MESSAGE);
			response.getWriter().println(gson.toJson(restResponse));
			return;
        }
        
        //get JWT token
        String jwtToken=authorizationHeader.substring(7);
        
        //verify JWT token and extract information 
        try {
            String userName=Jwts.parser().setSigningKey(Constants.JWT_ENCRYPTION_KEY).parseClaimsJws(jwtToken).getBody().getSubject();
            DatastoreService<User> dsUser=new DatastoreService<User>(User.class);
            User user=dsUser.get(userName);
            
            //check JWT token
            if(user.getJwt().equals(jwtToken)){
            	//user authenticated
            	chain.doFilter(request, response);
            	return;
            }
            

        } catch (SignatureException e) {
        	LOG.severe("JWT signature not valid");
        	//respond with NOT AUTHORIZED
			
        }
        catch(ExpiredJwtException e){
        	LOG.severe("JWT expired");
        	//respond with NOT AUTHORIZED
        }
        catch(Exception ex){
        	LOG.severe("JWT not valid");
        	//respond with NOT AUTHORIZED
        }
        //respond with NOT AUTHORIZED
		response.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
		restResponse.setCode(Constants.HTML_CODE_401_UNAUTHORIZED);
		restResponse.setMessage(Constants.HTML_CODE_401_UNAUTHORIZED_MESSAGE);
		response.getWriter().println(gson.toJson(restResponse));
		return;
        
    }

    @Override
    public void destroy() {
    	//destroy
    }
    
    
}