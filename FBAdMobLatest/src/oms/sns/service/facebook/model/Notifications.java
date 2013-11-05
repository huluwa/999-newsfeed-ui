package oms.sns.service.facebook.model;

import java.util.ArrayList;

public class Notifications{
    
  public ArrayList<Notification> notificationlist = new ArrayList<Notification>();
  public ArrayList<AppInfo>     appinfo = new ArrayList<AppInfo>();


  public static class Notification implements Comparable ,java.io.Serializable {
      public long notification_id;
      public long sender_id;
      public long recipient_id;
      public long created_time;
      public long updated_time;
      public String title_html;
      public String title_text;
      public String body_html;
      public String body_text;
      public String href;
      public long app_id;
      public boolean is_unread;
      public boolean is_hidden;
      
      public String toString()
      {
          return "sender_id="+sender_id + " recipient_id=" + recipient_id +" text="+body_text + " read="+is_unread;
      }
      
      public int compareTo(Object another) {
          if(Notification.class.isInstance(another))
          {
              long anDate = ((Notification)another).updated_time;
              if(updated_time > anDate)
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

	public void despose() {
		 notification_id = 0;
	     sender_id       = 0;
	     recipient_id    = 0;
	     created_time    = 0;
	     updated_time    = 0;
	     title_html      = null;
	     title_text      = null;
	     body_html       = null;
	     body_text       = null;
	     href            = null;
	     app_id          = 0;
	     is_unread       = false;
	     is_hidden       = false;
	}          
  }
   
  public static class AppInfo implements java.io.Serializable 
  {
      public long app_id;
      public String api_key;
      public String canvas_name;
      public String display_name;
      public String icon_url;
      public String logo_url;
      public ArrayList<DeveloperInfo>  developer_info;
      public String company_name;
      public String description;
      public long daily_active_users;
      public long weekly_active_users;
      public long monthly_active_users;
      
	  public void despose() {
		  app_id = 0;
	      api_key      = null;
	      canvas_name  = null;
	      display_name = null;
	      icon_url     = null;
	      logo_url     = null;
	      if(developer_info != null)
	      {
	    	  for(DeveloperInfo item:developer_info)
	    	  {
	    		  item.despose();
	    		  item = null;
	    	  }
	    	  developer_info.clear();
	    	  developer_info = null;
	      }	      
	      company_name        = null;
	      description         = null;
	      daily_active_users  = 0;
	      weekly_active_users =0;
	      monthly_active_users=0;;
		
      }
  }
  
  public static class DeveloperInfo implements java.io.Serializable 
  {
      public long uid;
      public String name;
      
      public void despose()
      {
    	  uid = 0;
    	  name = null;
      }
  }
}
