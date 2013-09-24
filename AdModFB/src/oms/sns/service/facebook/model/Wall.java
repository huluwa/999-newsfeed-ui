package oms.sns.service.facebook.model;

import java.util.Date;

/*<wall_get_response xmlns="http://api.facebook.com/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://api.facebook.com/1.0/ http://api.facebook.com/1.0/facebook.xsd" list="true">
<wallpost>
  
</wallpost>
</wall_get_response>*/

public class Wall implements Comparable {
    public long wpid;
    public long touserid;
    public long fromid;
    public String fromusername;
    public String body;
    public Date time;
    
    
	public Long getWpid() {
		return wpid;
	}

	public void setWpid(Long wpid) {
		this.wpid = wpid;
	}

	public Long getTouserid() {
		return touserid;
	}
	
	public Long getFromid() {
		return fromid;
	}

	public void setFromid(Long fromid) {
		this.fromid = fromid;
	}



	public void setTouserid(Long touserid) {
		this.touserid = touserid;
	}



	public String getFromusername() {
		return fromusername;
	}



	public void setFromusername(String fromusername) {
		this.fromusername = fromusername;
	}



	public String getBody() {
		return body;
	}



	public void setBody(String body) {
		this.body = body;
	}



	public Date getTime() {
		return time;
	}



	public void setTime(Date time) {
		this.time = time;
	}



	public enum Field
	{
		WPID,TO,FROM,NAME,BODY,TIME;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}



	public int compareTo(Object another) 
	{		
		if(Wall.class.isInstance(another))
		{
			Date anDate = ((Wall)another).time;
			if(time.getTime() > anDate.getTime())
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		
		return 0;
	}
}
