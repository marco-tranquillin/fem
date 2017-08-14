package mrtranqui.fem.data;

public class RestResponse{
	
	private int code;
	private String message;
	
	public RestResponse(){
		;
	}
	
	/**
	 * RestResponse message to be sent
	 * @param code the HTML code to be sent
	 * @param message the message to be sent
	 */
	public RestResponse(int code, String message){
		this.code=code;
		this.message=message;
	}
	
	public void setCode(int code){
		this.code=code;
	}
	
	public int getCode(){
		return this.code;
	}
	
	public void setMessage(String message){
		this.message=message;
	}
	
	public String getMessage(){
		return this.message;
	}
}