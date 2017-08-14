package mrtranqui.fem.datastore.entity;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Log{
	
	@Id Long id;
	@Index String type;
	@Index long timestamp;
	String stackTrace;
	
	public Log(){
		;
	}
	
	/**
	 * Constructor
	 * @param type the type of the LOG (WARNING/SEVERE)
	 * @param timestamp the timestamp when the logs occurred
	 * @param stackTrace the stackTrace of the error
	 */
	public Log(String type,long timestamp, String stackTrace){
		this.type=type;
		this.timestamp=timestamp;
		this.stackTrace=stackTrace;
	}
	
	public void setType(String type){
		this.type=type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp){
		this.timestamp=timestamp;
	}
	
	public String getStackTrace(){
		return this.stackTrace;
	}
	
	public void setStackTrace(String stackTrace){
		this.stackTrace=stackTrace;
	}
	
}
