package matranq.fem.servlet.rest;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import matranq.fem.data.RestResponse;
import matranq.fem.shared.Constants;

public class PostRegister extends HttpServlet {

	private static final long serialVersionUID = 5923330302646175253L;
	private static final Logger LOG = Logger.getLogger(PostRegister.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		//response variables
		RestResponse restResponse=new RestResponse();
		Gson gson=new Gson();
		
		LOG.info(Constants.HTML_CODE_200_OK + " - " + "PING OK");
		resp.setContentType(Constants.MIME_TYPE_APPLICATION_JSON);
		restResponse.setCode(Constants.HTML_CODE_200_OK);
		restResponse.setMessage("OK");
		resp.getWriter().println(gson.toJson(restResponse));
	}

}