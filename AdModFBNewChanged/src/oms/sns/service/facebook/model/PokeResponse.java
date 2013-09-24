package oms.sns.service.facebook.model;

import java.util.Date;

public class PokeResponse {
   
	public Long uid;
	public Date time;
	public String name;
	
	
	
   public Long getUid() {
		return uid;
	}
   
	public void setUid(Long uid) {
		this.uid = uid;
	}
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
/**
    * <poke>
    *<uid>220016</uid>
    *<time>1188356039</time>
    *</poke>
    * @author b191
    *
    */
	public enum Field
	{
		UID,TIME,NAME;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
