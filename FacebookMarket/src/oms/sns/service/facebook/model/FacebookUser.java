package oms.sns.service.facebook.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.msocial.nofree.providers.SocialORM;

import android.content.Context;
import android.util.Log;

public class FacebookUser implements Comparable<FacebookUser>
{
	public static class SimpleFBUser implements  Comparable<SimpleFBUser>
	{
		public long    uid;
		public String  name;
		public String  pic_square;
		public String  birthday;
		public long    bd_date;
		public boolean isShoutcut;
		public boolean isfriend;
		public int     b_month=-1;
		public int     b_date;
		
		public void despose() {
			uid        = 0;
			name       = null;
			pic_square = null;
			birthday   = null;
			bd_date    = 0;
			isShoutcut = false;
		}
		
		public int compareTo(SimpleFBUser user) {
			 //this.bd_date = 
			if(this.bd_date < user.bd_date) return -1;
			
			else if(bd_date > user.bd_date) return 1;
					
			else return 0;
		}	
		
		
		public void setBirthday(String birthday) {
			this.birthday = birthday;	
			if(this.birthday!=null && !this.birthday.equals("")){
				String tempstr = this.birthday;
				int year = new Date().getYear()+1900;
				
				if(tempstr.indexOf(",")==-1){
					tempstr = tempstr + ","+ year;
				}else{
					tempstr = tempstr.substring(0,tempstr.indexOf(","))+","+year;
				}
				
			    SimpleDateFormat df = new SimpleDateFormat("MMMM dd,yyyy",Locale.US);
		        try {
		            Date tempDate = df.parse(tempstr);
		            b_month = tempDate.getMonth();
		            b_date  = tempDate.getDate();
					this.bd_date = tempDate.getTime();
					long distance = System.currentTimeMillis() - bd_date;
					if(distance >= 7*24*60*60*1000L)
					{
						Date dd = new Date(bd_date);
						dd.setYear(dd.getYear() + 1+1900);
						bd_date= dd.getTime();					
					}
				} catch (ParseException e) {
					this.bd_date = 0;
				}
			}
		}

		//should move this to sql query
        public boolean isFriend(Context context) {
            SocialORM orm = SocialORM.instance(context);
            isfriend = orm.isFriends(uid); 
            return isfriend;
        }
	}
	
	public long uid;
	public String birthday;
	public String first_name;
	public String last_name;
	public String name;
	public String pic_square;
	public String pic;
	public String pic_small;
	public String sex;
	public int  event_sync;
	public long event_last_sync;
	public long ceid;
	
    public String message;
    public long statusid;
    public long statustime;
    
    public int     b_month;
    public int     b_date;
    public long bd_date;
    //for UI checkbox
    public boolean selected;
    
    //is my friend
    public boolean isfriend;
    
    //for more user info
    public String about_me;
    public String activities;    
    public String quotes;
    public String books;
    public String movies;
    public String music;
    public String tv;
    public String interests;
            
    public Current_Location        current_location;    
    public List<Education_History> education_history;    
    public Current_Location        hometown_location;
    public List<String>            meeting_for;
    public List<String>            meeting_sex;          
    public String                  relationship_status;
    public List<Work_History>      work_history;
    public String                  online_presence;
    public boolean                 is_app_user;
    
    public boolean isShoutcut;
    
    public static class Work_History
    {
    	public String city;
    	public String state;
    	public String country;
    	public String company_name;
    	public String position;
    	public String description;
    	public String start_date;
    	public String end_date;
    	
    	public String toString()
    	{
    		return " city="+city+" state="+state +" country="+country+" company_name="+company_name +
    		              " position="+position + " description="+description +" start_date="+start_date + " end_date="+end_date;
    	}
    }
    
    public static class Education_History
    {
    	public String name;
    	public String year;
    	public List<String> concentrations= new ArrayList<String>();    	
    	public String degree;
    	public String school_type;
    	
    	public String toString()
    	{
    		String str = "Name="+name + " year="+year+" degree="+degree + " school type="+school_type ;
    		if(concentrations.size() != 0)
    		{
    			for(int i=0;i<concentrations.size();i++)
    			{
    				str += " con="+concentrations.get(i);
    			}
    		}
    		
    		return str;
    	}
    }
    public static class Current_Location
    {
    	public String city;
    	public String state;
    	public String country;
    	public String zip;
    	
    	public String toString()
    	{
    		return "city="+city+ " state="+state+ " country="+country + " zip="+zip;
    	}
    }
    
    public FacebookUser clone()
    {
    	FacebookUser user = new FacebookUser();
    	user.uid = this.uid;
    	user.birthday = this.birthday;
    	user.first_name = this.first_name;
    	user.last_name  = this.last_name;
    	user.name       = this.name;
    	user.pic_square = this.pic_square;
    	user.pic        = this.pic;
    	user.pic_small  = this.pic_small;
    	user.sex        = this.sex;
    	user.event_sync = this.event_sync;
    	user.event_last_sync = this.event_last_sync;
    	user.ceid            = this.ceid;
    	user.message         = this.message;
    	user.statusid        = this.statusid;
    	user.statustime      = this.statustime;
    	user.bd_date         = this.bd_date;
    	user.selected        = this.selected;
    	user.isfriend        = this.isfriend;
    	
    	user.about_me        = this.about_me;
    	user.activities      = this.activities;
    	user.quotes          = this.quotes;
    	user.books           = this.books;
    	user.movies          = this.movies;
    	user.music           = this.music;
    	user.tv              = this.tv;
    	user.online_presence = this.online_presence;
    	user.is_app_user     = this.is_app_user;
    	user.relationship_status = relationship_status;
    	user.isShoutcut      = this.isShoutcut;
    	
    	if(this.current_location != null)
    	{
    	    user.current_location= new Current_Location(); 
    	    user.current_location.city    = this.current_location.city;
    	    user.current_location.country = this.current_location.country;
    	    user.current_location.state   = this.current_location.state;
    	    user.current_location.zip     = this.current_location.zip;
    	}
    	
    	if(this.education_history != null && this.education_history.size() > 0)
    	{
    		user.education_history = new ArrayList<Education_History>();
    		for(int i=0;i<this.education_history.size();i++)
    		{
    			Education_History item = this.education_history.get(i); 
    			Education_History newitem = new Education_History();
    			newitem.degree = item.degree;
    			newitem.name   = item.name;
    			newitem.year   = item.year;    			
    			newitem.concentrations = new ArrayList<String>();    
    			for(int j=0;i<item.concentrations.size();j++)
    			{
    				newitem.concentrations.add(item.concentrations.get(j));
    			}
    			user.education_history.add(newitem);
    		}
    	}
    	
    	if(this.hometown_location != null)
    	{
    		user.hometown_location= new Current_Location(); 
      	    user.hometown_location.city    = hometown_location.city;
      	    user.hometown_location.country = hometown_location.country;
      	    user.hometown_location.state   = hometown_location.state;
      	    user.hometown_location.zip     = hometown_location.zip;
    	}
    	
    	if(meeting_for != null && meeting_for.size() > 0)
    	{
    		user.meeting_for = new ArrayList<String>();
    		user.meeting_for.addAll(meeting_for);
    	}
    	
    	if(meeting_sex != null && meeting_sex.size() > 0)
    	{
    		user.meeting_sex = new ArrayList<String>();
    		user.meeting_sex.addAll(meeting_sex);
    	}
    	
    	if(work_history != null && work_history.size() > 0)
    	{
    		user.work_history = new ArrayList<Work_History>();
    		for(int i=0;i<work_history.size();i++)
    		{
    			Work_History item = work_history.get(i);
    			Work_History object = new Work_History();
    			object.city = item.city;    			
    			object.country      = item.company_name;
    			object.state        = item.state;
    			
    			object.company_name = item.company_name;
    			object.description  = item.description;
    			object.position     = item.position;
    			object.start_date   = item.start_date;
    			object.end_date     = item.end_date;    			
    			user.work_history.add(object);
    		}
    	}
    	return user;    	
    }
    
	public Integer getEvent_sync() {
		return event_sync;
	}
	public void setEvent_sync(Integer event_sync) {
		this.event_sync = event_sync;
	}
	public Long getEvent_last_sync() {
		return event_last_sync;
	}
	public void setEvent_last_sync(Long event_last_sync) {
		this.event_last_sync = event_last_sync;
	}
	public Long getCeid() {
		return ceid;
	}
	public void setCeid(Long ceid) {
		this.ceid = ceid;
	}
	public long getBd_date() {
		return bd_date;
	}
	public void setBd_date(long bd_date) {
		this.bd_date = bd_date;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Long getStatusid() {
		return statusid;
	}
	public void setStatusid(Long statusid) {
		this.statusid = statusid;
	}
	public Long getStatustime() {
		return statustime;
	}
	public void setStatustime(Long statustime) {
		this.statustime = statustime;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;	
		if(this.birthday!=null && !this.birthday.equals("")){
			String tempstr = this.birthday;
			int year = new Date().getYear()+1900;
			
			if(tempstr.indexOf(",")==-1){
				tempstr = tempstr + ","+ year;
			}else{
				tempstr = tempstr.substring(0,tempstr.indexOf(","))+","+year;
			}
			
		    SimpleDateFormat df = new SimpleDateFormat("MMMM dd,yyyy",Locale.US);
	        try {
	            
	            Date tempDate = df.parse(tempstr);
                b_month = tempDate.getMonth();
                b_date  = tempDate.getDate();
	            
				this.bd_date = tempDate.getTime();
				long distance = System.currentTimeMillis() - bd_date;
				if(distance >= 7*24*60*60*1000L)
				{
					Date dd = new Date(bd_date);
					dd.setYear(dd.getYear() + 1+1900);
					bd_date= dd.getTime();					
				}
			} catch (ParseException e) {
				this.bd_date = 0;
			}
		}
	}
	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPic_square() {
		return pic_square;
	}
	public void setPic_square(String pic_square) {
		this.pic_square = pic_square;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	
	public static class Field 
	{
		public final static String UID="uid";
		public final static String FIRST_NAME="first_name";
		public final static String LAST_NAME = "last_name";
		public final static String NAME      ="name";
		public final static String SCREENNAME="screenname";
		public final static String PIC_SMALL = "pic_small";
		public final static String PIC_BIG   = "pic_big";
		public final static String PIC_SQUARE= "pic_square";
		public final static String PIC       = "pic";
		public final static String AFFILIATIONS = "affiliations";
		public final static String PROFILE_UPDATE_TIME = "profile_update_time";
		public final static String TIMEZONE = "timezone";
		public final static String RELIGION = "religion";
		public final static String BIRTHDAY = "birthday";
		public final static String SEX      = "sex";
		public final static String HOMETOWN_LOCATION = "hometown_location"; 
		public final static String MEETING_SEX       = "meeting_sex"; 
		public final static String MEETING_FOR       = "meeting_for";
		public final static String RELATIONSHIP_STATUS = "relationship_status";
		public final static String SIGNIFICANT_OTHER_ID= "significant_other_id";
		public final static String POLITICAL           = "political";
		public final static String CURRENT_LOCATION    = "current_location";
		public final static String ACTIVITIES          = "activities";
		public final static String INTERESTS           = "interests"; 
		public final static String IS_APP_USER         = "is_app_user";
		public final static String MUSIC               = "music";		    
		public final static String TV                  = "tv";
		public final static String MOVIES              = "movies";
		public final static String BOOKS               = "books";
		public final static String QUOTES              = "quotes";
		public final static String ABOUT_ME            = "about_me";
		public final static String HS_INFO             = "hs_info";
		public final static String EDUCATION_HISTORY   = "education_history";
		public final static String WORK_HISTORY        = "work_history";
		public final static String NOTES_COUNT         = "notes_count";
		public final static String WALL_COUNT          = "wall_count";
		public final static String STATUS              = "status";
		public final static String HAS_ADDED_APP	    = "has_added_app";	
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\nid="+uid+" name="+name  + " pic="+pic_square);
		if(education_history != null)
		{
			sb.append("\nedu history="+education_history.size());
			for(int i=0;i<education_history.size();i++)
			{
				sb.append("\n"+education_history.get(i).toString());
			}
		}
		
		if(work_history != null)
		{
			sb.append("\nwork history="+work_history.size());
			for(int i=0;i<work_history.size();i++)
			{
				sb.append("\n"+work_history.get(i).toString());
			}
		}
		
		if(current_location != null)
		{
			sb.append("\n current loc="+ current_location.toString());
		}
		
		if(hometown_location != null)
		{
			sb.append("\n home town="+hometown_location.toString());
		}
		
		if(meeting_for != null)
		{
			sb.append("\n meeting for");
			for(int i=0;i<meeting_for.size();i++)
			{
				sb.append(" "+meeting_for.get(i));
			}
		}
		

		if(meeting_sex != null)
		{
			sb.append("\n meeting sex");
			for(int i=0;i<meeting_sex.size();i++)
			{
				sb.append(" "+meeting_sex.get(i));
			}
		}
		
		if(online_presence != null)
		{
			sb.append("\n online_presence="+online_presence);
		}
		
		return sb.toString();
	}
	
	public int compareTo(FacebookUser user) {
		// TODO Auto-generated method stub
		
		 //this.bd_date = 
		if(this.bd_date < user.bd_date) return -1;
		
		else if(bd_date > user.bd_date) return 1;
				
		else return 0;
	}

    public void despose() 
    {
        uid      =0;
        birthday = null; 
        first_name = null;
        last_name  = null;
        name       = null;
        pic_square = null;
        pic        = null;
        pic_small  = null;
        sex        = null;
        event_sync = 0;
        event_last_sync = 0;
        ceid       = 0;
        
        message    = null;
        statusid   = 0;
        statustime = 0;        
        bd_date    = 0;
        //for UI checkbox
        selected = false;
        
        //is my friend
        isfriend = false;
        
        //for more user info
        about_me = null;
        activities = null;    
        quotes = null;
        books  = null;
        movies = null;
        music  = null;
        tv     = null;
                
        //TODO, despose children
        current_location = null;    
        if(education_history != null)
        {
            education_history.clear();    
            education_history = null;
        }
        hometown_location = null; 
        if(meeting_for != null)
        {
            meeting_for.clear();
            meeting_for = null;
        }
        if(meeting_sex != null)
        {
            meeting_sex.clear();
            meeting_sex = null;
        }
        relationship_status = null; 
        if(work_history != null)
        {
            work_history.clear();
            work_history = null;
        }
        
        online_presence = null;
        is_app_user = false;        
        isShoutcut  = false;
    }
}
