package com.tormas.litesina.providers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import twitter4j.SimplyUser;
import twitter4j.http.HttpClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class SocialORM implements SocialORMInterface{
	public final  static String SNS_CONTENT_URI = "content://com.tormas.litesina.providers.SocialProvider";
	
	public static boolean twitterChanged = false;
	
	final  static String TAG      = "SocialORM";
	final  static String facebook_email="facebook_email";
	final  static String facebook_pwd  ="facebook_pwd";
    final  static String tweet_view_count="tweet_view_count";
    final  static String follow_view_count="follow_view_count";
    final  static String tweet_view_timeout="tweet_view_timeout";
    final  static String trend_view_timeout="trend_view_timeout";
    final  static String stream_view_timeout="stream_view_timeout";
    final  static String issnsenable ="issnsenable";
    final  static String enablesyncphonebook    ="enablesyncphonebook";
    final  static String enableassignpermission ="enableassignpermission";
    final  static String showtitlebar ="showtitlebar";
    final  static String enablenotifications ="enablenotifications";
    final  static String enablenotifications_message ="enablenotifications_message";
    final  static String enablenotifications_pokes   ="enablenotifications_pokes";
    final  static String enablenotifications_event   ="enablenotifications_event";
    final  static String enablenotifications_group   ="enablenotifications_group";
    final  static String enablenotifications_request ="enablenotifications_request";
    final  static String notification_interval="notification_interval";
    final  static String isalwayspromptsyncdialog = "isalwayspromptsyncdialog";
    final  static String usepermanentsession ="usepermanentsession";
    final  static String copytoemail ="copytoemail";
    final  static String copynewmessagetosms ="copynewmessagetosms";
    
    final static String  facebook_icon_size   ="facebook_icon_size";    
    final static String  facebook_mail_check_period       = "facebook_mail_check_period";
    final static String  facebook_addressbook_sync_period   = "facebook_addressbook_sync_period";
    
    final static String  facebook_contact_update_period   = "contact_update_period";
    final static String  facebook_friend_update_period    = "friend_update_period";
    final static String facebook_contact_last_update_time = "contact_last_update_time";
    final static String date_check_time = "date_check_time";
    final static String facebook_addressbook_last_update_time  = "facebook_addressbook_last_update_time";
    final static String facebook_notification_last_update_time = "facebook_notification_last_update_time";
    final static String facebook_friend_last_update_time  = "friend_last_update_time";
    final static String facebook_show_on_homescreen       = "facebook_show_on_homescreen";
    final static String twitter_show_on_homescreen        = "twitter_show_on_homescrren";
    final static String twitter_use_https_connection      = "twitter_use_https_connection";
    final static String facebook_use_https_connection     = "facebook_use_https_connection";    
    final static String facebook_use_logo                 = "facebook_use_logo";
    final static String facebook_use_email                = "facebook_use_email";
    final static String facebook_use_phonenumber          = "facebook_use_phonenumber";
    final static String facebook_use_birthday             = "facebook_use_birthday";
    final static String facebook_sync_birthday_event      = "facebook_sync_birthday_event";
    final static String facebook_notification_vibrate     = "facebook_notification_vibrate";
    final static String facebook_notification_led         = "facebook_notification_led";
    
    final static String twitter_upload_photo_size         = "twitter_upload_photo_size"; //1 original 0 compressed
    final static String facebook_upload_photo_size        = "facebook_upload_photo_size";//1 original 0 compressed
    
    final static String proxy_host          = "prxoy_host";
    final static String proxy_port          = "prxoy_port";
    final static String proxy_username      = "prxoy_username";
    final static String proxy_password      = "prxoy_password";
    final static String proxy_enable        = "prxoy_enable";
    
    final  static String f_friend_view_count="f_friend_view_count";
	
	final  static String twitter_account="twitter_account";
	final  static String twitter_pwd    ="twitter_pwd";
	final  static String twitter_uid    ="twitter_uid";
	final  static String twitter_screen_name = "twitter_screen_name";
	
	final  static String twitter_token = "twitter_token";
	final  static String twitter_token_secret="twitter_token_secret";
	
	public final  static String pre_account    ="pre_account";
	public final  static String trendsTimeout  ="trends_timeout";
	
	public static int FFriendViewCount;	
	public static int FollowViewCount;
	public static long StatusViewTimeout=60;
	public static long TrendsViewTimeout=60;
	//
	//TODO, why we choose 1000, this is a BUG
	//need use Cursor to select UI	
	public static int  maxsize=1000;
	
	public long getStatusViewTimeout()
	{
		return StatusViewTimeout;
	}
	public long getTrendsViewTimeout()
	{
		return TrendsViewTimeout;
	}
	
	private Context context;
    public SocialORM(Context co)
    {
    	context = co.getApplicationContext();        
    }
    
    private static SocialORM _instance;
    public static SocialORM instance(Context co)
    {
    	if(_instance == null)
    	{
    		_instance = new SocialORM(co.getApplicationContext());
    	}
    	
    	return _instance;
    }
   
    public boolean isAlwaysPromptSyncDialog()
    {
    	 int enable = Integer.parseInt(getSettingValue(isalwayspromptsyncdialog));
         return enable==0?false:true;
    }
    
    public void enableAlwaysPromptSyncDialog(boolean enable)
    {
    	 updateSetting(isalwayspromptsyncdialog, String.valueOf(enable==true?1:0));
    }
    public boolean isEnableSyncPhonebook()
    {
        int ienable = Integer.parseInt(getSettingValue(enablesyncphonebook));
        return ienable==0?false:true;
    }    
    
    public boolean isEnableAssignPermission()
    {
    	try{
	        int ienable = Integer.parseInt(getSettingValue(enableassignpermission));
	        return ienable==0?false:true;
    	}catch(NumberFormatException ne){}
    	return false;
    }
    public void EnableAssignPermission(boolean enable)
    {
    	addSetting(enableassignpermission, String.valueOf(enable==true?1:0));
    }
    
    public boolean isShowTitleBar() 
    {       
        boolean ret = true;
        try{
            int ienable = Integer.parseInt(getSettingValue(showtitlebar));
            ret = (ienable==0?false:true);
        }catch(NumberFormatException ne){}
        return ret;        
    }
    
    public void setTitleBarVisible(boolean visible)
    {
        addSetting(showtitlebar, String.valueOf(visible==true?1:0));
    }
    
    public boolean isSNSEnable() 
    {	
    	int ienable = Integer.parseInt(getSettingValue(issnsenable));
    	return ienable==0?false:true;
    }
    
    public boolean isUsePermanentSeesion() 
    {	
    	int ienable = Integer.parseInt(getSettingValue(usepermanentsession));
    	return ienable==0?false:true;    
    }
    
    public void setUsePermanentSeesion(boolean enable)
    {
    	updateSetting(usepermanentsession, String.valueOf(enable==true?1:0));
    }
    
    public void setSNSEnable(boolean enable)
    {
    	updateSetting(issnsenable, String.valueOf(enable==true?1:0));
    }
    
    public boolean copytoEmail() 
    {   
        int ienable = Integer.parseInt(getSettingValue(copytoemail));
        return ienable==0?false:true;
    }
    
    public void setcopytoEmail(boolean enable)
    {
        updateSetting(copytoemail, String.valueOf(enable==true?1:0));
    }
    
    public boolean copyNewMessagetoSms() 
    {   
        int ienable = Integer.parseInt(getSettingValue(copynewmessagetosms));
        return ienable==0?false:true;
    }
    
    public void setCopyNewMessagetoSms(boolean enable)
    {
        updateSetting(copynewmessagetosms, String.valueOf(enable==true?1:0));
    }
    
    //
    //0 Facebook
    //1 twitter
    public class Account
    {
    	public String email;
    	public String password;
    	public String uid;
    	public String screenname;
    	public int    type;
    	public String token;
    	public String token_secret;
    }
    
    //settings
    public class SettingsCol{
        public static final String ID      = "_id";
        public static final String Name    = "name";
        public static final String Value   = "value";
    }
    public class Settings{
        public String ID   ;
        public String Name ;
        public String Value ;
    }
    public static String[]settingsProject =  new String[]{
        "_id",
        "name",
        "value",
    };
    
    //trends
    public class TrendsCol{
        public static final String ID      = "_id";
        public static final String Name    = "name";
        public static final String URL     = "url";
        public static final String Date    = "date";
        
    }
    public class Trend{
        public String ID   ;
        public String Name ;
        public String URL ;
        public String Date ;
    }
    public static String[]TrendProject =  new String[]{
        "_id",
        "name",
        "url",
        "date"
    };
    
    public class PageCol
    {
    	public static final String ID           = "_id";
    	public static final String PAGEID       = "pageid";
    	public static final String NAME         = "name";
    	public static final String PAGE_URL     = "page_url";
    	public static final String PIC_SMALL    = "pic_small";
    	public static final String PIC_BIG      = "pic_big";
    	public static final String PIC_SQUARE   = "pic_square";
    	public static final String PIC          = "pic";
    	public static final String PIC_LARGE    = "pic_large";
    	public static final String TYPE         = "type";
    	public static final String WEBSITE      = "website";
    	public static final String COMPANY_OVERVIEW = "company_overview";
    	
    }
    
    public static String[] PageProject =  new String[]{
        "_id",
        "pageid",
        "name",
        "page_url",
        "pic_small",
        "pic_big",
        "pic_square",
        "pic",
        "pic_large",
        "type",
        "website",
        "company_overview"
    };
    //to save the network memory and enhance the speed
    public class FollowCol{
        public static final String ID      = "_id";
        public static final String UID     = "uid";
        public static final String Name    = "name";
        public static final String SName   = "sname";
        public static final String ProfileImgUrl  = "profileImgUrl";
        public static final String Type  = "type";
        
    }
    public class Follow{
        public int    ID   ;
        public long   UID ;
        public String Name ;
        public String SName ;
        public String ProfileImgUrl;
        public boolean isFollower ;//0 is follower, 1 following
        //for UI
        public boolean selected;
    }
    public static String[]FollowProject =  new String[]{
        "_id",
        "uid",
        "name",
        "sname",
        "profileImgUrl",
        "type"
    };
 
    public static class FacebookUsersCol{
    	public static final String ID = "_id";
    	public static final String UID = "uid";
    	public static final String FIRSTNAME = "first_name";
    	public static final String LASTNAME = "last_name";
    	public static final String NAME = "name";
    	public static final String PICSQUARE = "pic_square";
    	public static final String BIRTHDAY = "birthday";
    	public static final String B_MONTH = "b_month";
    	public static final String B_DATE  =  "b_date";
    	public static final String SEX = "sex";
    	public static final String MESSAGE = "message";
    	public static final String STATUSID = "status_id";
    	public static final String STATUSTIME = "status_time";
    	public static final String  EVENT_SYNC = "event_sync";
    	public static final String EVENT_LAST_SYNCTIME = "event_last_synctime";
    	public static final String C_EVENT_ID = "c_event_id";
    	public static final String PIC = "pic";
    	public static final String PIC_SMALL = "pic_small";
    	public static final String IsShoutCut = "isshotcut";
    	

    }
    public static String[] FacebookUsersProject = new String[]{
    	"_id",
    	"uid",
    	"first_name",
    	"last_name",
    	"name",
    	"pic_square",
    	"pic",
    	"pic_small",    	
    	"birthday",
    	"sex",
    	"message",
        "status_id",
        "status_time",
        "event_sync",
        "event_last_synctime",
        "c_event_id",
        "isshotcut",
        "b_month"
    };
    
    public static String[] FacebookSimpleUsersProject = new String[]{
        "_id",
        "uid",        
        "name",
        "pic_square",        
        "birthday",
        "isshotcut"
    };
    
    public static class FacebookFriendsCol{
    	public static final String ID = "_id";
    	public static final String UID1 = "uid_1";
    	public static final String UID2 = "uid_2";
    }
    
    public static String[] FacebookFriendsProject = new String[]{
    	"_id",
    	"uid_1",
    	"uid_2",
    };
    
    public static class ExtPermissionsCol{
    	public static final String ID = "_id";
    	public static final String METHODNAME = "methodname";
    	public static final String PERMISSION = "permission";
    }
    
    public static String[] ExtPermissionProject = new String[]{
    	"_id",
    	"methodname",
    	"permission"
    };
    
    public static class PhonebookCol{
    	public static final String ID = "_id";
    	public static final String UID = "uid";
    	public static final String Name = "name";
    	public static final String EMAIL = "email";
    	public static final String CELL = "cell";
    	public static final String Phone = "phone";
    	public static final String Synced = "synced";
    	public static final String Screenname = "screenname";
    	public static final String Address = "address";
    	public static final String Street = "street";
    	public static final String State = "state";
    	public static final String City = "city";
    	public static final String Country = "country";
    	public static final String Zip = "zip";
    	public static final String Latitude = "latitude";
    	public static final String Longitude = "longitude";
    	public static final String PeopleId = "peopleid";
    	
    }
    
    public static String[] PhonebookProject = {
    	"_id",
    	"uid",
    	"name",
    	"email",
    	"cell",
    	"phone",
    	"synced",
    	"screenname",
    	"address",
    	"street",
    	"state",
    	"city",
    	"country",
    	"zip",
    	"latitude",
    	"longitude",
    	"peopleid"
    };
    
    public static class FacebookeventCol{
    	public static final String ID = "_id";
    	public static final String EID = "eid";
    	public static final String Name = "name";
    	public static final String TAGLINE = "tagline";
    	public static final String NID = "nid";
    	public static final String PIC_small = "pic_small";
    	public static final String PIC       = "pic";
    	public static final String PIC_big   = "pic_big";
    	public static final String HOST = "host";
    	public static final String CREATOR = "creator";
    	public static final String DESC = "description";
    	public static final String EVENTTYPE = "event_type";
    	public static final String EVENTSUBTYPE = "event_subtype";
    	public static final String STARTTIME = "starttime";
    	public static final String ENDTIME = "endtime";
    	public static final String LOCATION = "location";
    	public static final String VENUE = "venue";
    	public static final String RSVP_STASTUS = "rsvp_status";
    	public static final String Synced = "synced";
    	public static final String CEID = "c_event_id";
    }
    
    public static String[] FacebookeventProject = {
    	"_id",
    	"eid",
    	"name",
    	"tagline",
    	"nid",
    	"pic_small",
    	"pic",
    	"pic_big",
    	"host",
    	"creator",
    	"description",
    	"event_type",
    	"event_subtype",
    	"starttime",
    	"endtime",
    	"location",
    	"venue",
    	"rsvp_status",
    	"synced",
    	"c_event_id"
    };
    
    public static class peoplemapfacebookCol{
    	public static final String ID = "_id";
    	public static final String UID = "uid";
    	public static final String PeopleID = "peopleid";
    	public static final String Requested = "requested";    	
    }
    
    public static String[] peoplemapfacebooProject = {
    	"_id",
    	"uid",
    	"peopleid",
    	"requested",    	
    };
    
    public static class walldraftCol{
    	public static final String ID                = "_id";
    	public static final String FUID              = "fuid";
    	public static final String CONTENT           = "content";
    	public static final String CONTENTTYPE       = "contenttype";
    	public static final String LASTMODIFY        = "lastmodify";
    }
    
    public static String[] walldraftProject = {
    	"_id",
    	"fuid",
    	"content",
    	"contenttype",
    	"lastmodify"
    };
    
    
    public static class notecol{
        public static final String ID                  = "_id";
        public static final String NOTE_ID             = "note_id";
        public static final String TITLE               = "title";
        public static final String CONTENT             = "content";
        public static final String CREATED_TIME        = "created_time";
        public static final String UPDATED_TIME        = "updated_time";
        public static final String UID                 = "uid";
    }
    
    public static String[] noteprojection = {
        "_id",
        "note_id",
        "title",
        "content",
        "created_time",
        "updated_time",
        "uid"
    };
    
    static public class PeopleMapFacebook
    {
    	public int     _id;
    	public long    uid;
    	public String  Name;
    	public int     peopleid;
    	public boolean requested;
    	public boolean isFriend;
    }    
   
    public boolean existNote(long noteid)
    {
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note");
         String queryString ="";
         queryString = String.format( notecol.NOTE_ID +"=%1$s ", noteid);
         String[] noteprojection = {notecol.ID,notecol.NOTE_ID}; 
         boolean ret=false;
         Cursor cursor = null;
         try
         {
            cursor = context.getContentResolver().query(CONTENT_URI, noteprojection, queryString, null, null);
            if(cursor != null)
            {     
                while(cursor.moveToNext())
                {
                    ret = true;
                    break;
                }               
            }
        }
        catch(SQLiteException ne)
        {
           ret = false;
        }
        finally
        {
           if(cursor != null)
           {
               cursor.close();
           }
        }
        return ret;
    }
    
   
    public Cursor getNoteCursorByUID(long uid)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note");
        String orderSelection = notecol.UPDATED_TIME + " DESC";
        
        String where = notecol.UID+ " = "+uid;
        Cursor cursor = null;       
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, noteprojection, where, null, orderSelection);
        }
        catch(SQLiteException ne)
        {}      
        return cursor;      
    }
    
    
      
    
    public boolean existTwitterUser(long uid, boolean isFollower)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
        String[] selectionArgs = new String[2];
        selectionArgs[0] = "";
        String queryString ="";
        
        selectionArgs[0] = String.format("%1$s", uid);
        selectionArgs[1] = isFollower?"0":"1";
        queryString = String.format(" uid=%1$s and type=%2$s ", uid, isFollower?"0":"1");
        
        
        boolean ret=false;
        Cursor cursor = null;
        try
        {
	        cursor = context.getContentResolver().query(CONTENT_URI, FollowProject, queryString, null, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	            	ret = true;
	                break;
	            }	            
	        }
	    }
	    catch(SQLiteException ne)
	    {
		   ret = false;
	    }
	    finally
	    {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	    }
        return ret;
    } 
    
    public Follow getLastTwitterFollowUser() 
    {		
    	 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
    	 Cursor cursor      = null;
    	 Follow fuser = null;
    	 try
    	 {
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FollowProject, null, null, null);    	 
	         if(cursor != null)
	         {    
	        	 
	             while(cursor.moveToLast())
	             {
	                 fuser = new Follow();
	                 Follow item = new Follow();
	                 item.ID       =  cursor.getInt(cursor.getColumnIndex(FollowCol.ID));  
	                 item.Name     =  cursor.getString(cursor.getColumnIndex(FollowCol.Name));
	                 item.UID      =  cursor.getInt(cursor.getColumnIndex(FollowCol.UID));              
	                 item.SName     =  cursor.getString(cursor.getColumnIndex(FollowCol.SName));
	                 item.ProfileImgUrl    =  cursor.getString(cursor.getColumnIndex(FollowCol.ProfileImgUrl));    
	                 item.isFollower       =  cursor.getInt(cursor.getColumnIndex(FollowCol.Type))==0?true:false;
	                 break;
	             }         
	         }
    	}
	    catch(SQLiteException ne)
	    {}
	    finally
	    {
		    if(cursor != null)
		    {
		       cursor.close();
		    }
	    }
		return fuser;
	} 	
    
    
    public ArrayList<Follow> getTwitterUser(int uid)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
        String[] selectionArgs = new String[1];
        selectionArgs[0] = "";
        String queryString ="";
        
        selectionArgs[0] = String.format("%1$s", uid);
        queryString = " uid=? ";
        
        ArrayList<Follow> ls = new ArrayList<Follow>();
        Cursor cursor = null ;
        try
        {
	        cursor = context.getContentResolver().query(CONTENT_URI, FollowProject, queryString, selectionArgs, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	                Follow item = new Follow();
	                item.ID       =  cursor.getInt(cursor.getColumnIndex(FollowCol.ID));  
	                item.Name     =  cursor.getString(cursor.getColumnIndex(FollowCol.Name));
	                item.UID      =  cursor.getInt(cursor.getColumnIndex(FollowCol.UID));              
	                item.SName     =  cursor.getString(cursor.getColumnIndex(FollowCol.SName));
	                item.ProfileImgUrl    =  cursor.getString(cursor.getColumnIndex(FollowCol.ProfileImgUrl));    
	                item.isFollower       =  cursor.getInt(cursor.getColumnIndex(FollowCol.Type))==0?true:false; 
	                ls.add(item);
	            }           
	        }
	    }
	    catch(SQLiteException ne)
	    {}
	    finally
	    {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	    }
    
        return ls;
    } 
    
    public ArrayList<Follow> getFollowerUser(){
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
        
        ArrayList<Follow> ls = new ArrayList<Follow>();
        Cursor cursor = null;
        try
        {
	        cursor = context.getContentResolver().query(CONTENT_URI, FollowProject, " type = 0 ", null, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	                Follow item = new Follow();
	                item.ID       =  cursor.getInt(cursor.getColumnIndex(FollowCol.ID));  
	                item.Name     =  cursor.getString(cursor.getColumnIndex(FollowCol.Name));
	                item.UID      =  cursor.getInt(cursor.getColumnIndex(FollowCol.UID));              
	                item.SName     =  cursor.getString(cursor.getColumnIndex(FollowCol.SName));
	                item.ProfileImgUrl    =  cursor.getString(cursor.getColumnIndex(FollowCol.ProfileImgUrl));    
	                item.isFollower       =  cursor.getInt(cursor.getColumnIndex(FollowCol.Type))==0?true:false;
	                if(ls.size() >= maxsize)
	                {
	                	break;
	                }
	                ls.add(item);
	            }            
	        }
	    }
	    catch(SQLiteException ne)
	    {}
	    finally
	    {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	    }
        return ls;
    }
    
    public ArrayList<String> getTwitterUserImageURL()
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
        String[]imageProject =  {
        		"_id",
                "profileImgUrl"
            };
        ArrayList<String> ls = new ArrayList<String>();
        Cursor cursor = null;
        try{
	        cursor = context.getContentResolver().query(CONTENT_URI, imageProject, null, null, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {  
	                String path    =  cursor.getString(cursor.getColumnIndex(FollowCol.ProfileImgUrl));
	                if(path != null)
	                    ls.add(path);
	            }         
	        }
        }
	    catch(SQLiteException ne)
	    {}
	    finally
	    {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	    }
        return ls;
    } 
    
    public Follow getTwitterFollowingUser(String sname)
    {
    	return getTwitterUser(sname, false);
    }
    public Follow getTwitterFollowerUser(String sname)
    {
    	return getTwitterUser(sname, true);
    }
    private Follow getTwitterUser(String sname, boolean isFollower)
    {
    	 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
         String[] selectionArgs = new String[2];
         selectionArgs[0] = "";
         String queryString ="";
         
         if(sname != null)
         {
             selectionArgs[0] = sname;
             selectionArgs[1] = isFollower==true?"0":"1";
             queryString = " sname=? and type=?";              
         }
         else
         {
             queryString   = null;
             selectionArgs = null;
         }
         
         Cursor cursor = null;
         Follow item = null;
         try{
	         cursor = context.getContentResolver().query(CONTENT_URI, FollowProject, queryString, selectionArgs, null);
	         if(cursor != null && cursor.moveToFirst())
	         {   
	             item = new Follow();
	             item.ID       =  cursor.getInt(cursor.getColumnIndex(FollowCol.ID));  
	             item.Name     =  cursor.getString(cursor.getColumnIndex(FollowCol.Name));
	             item.UID      =  cursor.getInt(cursor.getColumnIndex(FollowCol.UID));              
	             item.SName     =  cursor.getString(cursor.getColumnIndex(FollowCol.SName));
	             item.ProfileImgUrl    =  cursor.getString(cursor.getColumnIndex(FollowCol.ProfileImgUrl)); 
	             item.isFollower       =  cursor.getInt(cursor.getColumnIndex(FollowCol.Type))==0?true:false;	             	             
	         }	         
         }
 	     catch(SQLiteException ne)
 	     {}
 	     finally
 	     {
 		     if(cursor != null)
 		     {
 		        cursor.close();
 		     }
 	     }
 	     return item;
 	     
    }
    
    private Follow getTwitterUserByID(String uid, boolean isFollower)
    {
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
         String[] selectionArgs = new String[2];
         selectionArgs[0] = "";
         String queryString ="";
         
         if(uid != null)
         {
             selectionArgs[0] = uid;
             selectionArgs[1] = isFollower==true?"0":"1";
             queryString = " uid=? and type=?";              
         }
         else
         {
             queryString   = null;
             selectionArgs = null;
         }
         
         Cursor cursor = null;
         Follow item = null;
         try{
             cursor = context.getContentResolver().query(CONTENT_URI, FollowProject, queryString, selectionArgs, null);
             if(cursor != null && cursor.moveToFirst())
             {   
                 item = new Follow();
                 item.ID       =  cursor.getInt(cursor.getColumnIndex(FollowCol.ID));  
                 item.Name     =  cursor.getString(cursor.getColumnIndex(FollowCol.Name));
                 item.UID      =  cursor.getInt(cursor.getColumnIndex(FollowCol.UID));              
                 item.SName     =  cursor.getString(cursor.getColumnIndex(FollowCol.SName));
                 item.ProfileImgUrl    =  cursor.getString(cursor.getColumnIndex(FollowCol.ProfileImgUrl)); 
                 item.isFollower       =  cursor.getInt(cursor.getColumnIndex(FollowCol.Type))==0?true:false;                                
             }           
         }
         catch(SQLiteException ne)
         {}
         finally
         {
             if(cursor != null)
             {
                cursor.close();
             }
         }
         return item;
         
    }
    
    public ArrayList<Follow> getTwitterUser(String sname)
    {
        ArrayList<Follow> ls = new ArrayList<Follow>();
        Follow following = getTwitterUser(sname, false);
        Follow follower  = getTwitterUser(sname, true);
        
        if(following != null)
        {
            ls.add(following);
        }
        
        if(follower != null)
        {
        	if(following == null || (following != null && following.UID != follower.UID))
        	{
                ls.add(follower);
        	}
        }
        return ls;
    } 
    
    public ArrayList<Follow> getTwitterUserByID(String uid)
    {
        ArrayList<Follow> ls = new ArrayList<Follow>();
        Follow following = getTwitterUserByID(uid, false);
        Follow follower  = getTwitterUserByID(uid, true);
        
        if(following != null)
        {
            ls.add(following);
        }
        
        if(follower != null)
        {
            if(following == null || (following != null && following.UID != follower.UID))
            {
                ls.add(follower);
            }
        }
        return ls;
    } 
    
    
    public boolean updateTwitterUser(long uid, Follow user)
    {
        boolean ret = false;
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
        String where = String.format(" uid = %1$s ", uid);
        android.content.ContentValues ct = new android.content.ContentValues();                      
        ct.put(FollowCol.Name,  user.Name);
        ct.put(FollowCol.SName, user.SName);
        ct.put(FollowCol.Type,  user.isFollower?0:1);
        ct.put(FollowCol.ProfileImgUrl, user.ProfileImgUrl);        
        
        if(context.getContentResolver().update(CONTENT_URI, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
    }
    
	public  void AddTwitterFollowerUser(List<SimplyUser> users) 
	{				
		List<Follow> follows = new ArrayList<Follow>();
		if(users!=null && users.size()>0){
			for(SimplyUser user : users){
				Follow follow = new Follow();
				follow.UID = user.getId();
				follow.Name = user.getName();
				follow.SName = user.getScreenName();
				follow.ProfileImgUrl = user.getProfileImageURL();
				follow.isFollower = true;
				follows.add(follow);
			}
			
		}
		AddTwitterUser(follows);
	}
    
    public boolean AddTwitterUser(List<Follow> users)
    {
      if(users!=null && users.size()>0){
    	  for(Follow user: users){
    	      AddTwitterUserInternal(user);
    	  }
      }
      return true;
    }
    
    private boolean AddTwitterUserInternal(Follow user)
    {
    	 Uri ret = null;
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/follow");
         //if exist, update
         if(existTwitterUser(user.UID, user.isFollower) == true)
         {
        	 updateTwitterUser(user.UID, user);
         }
         else
         {        
        	 android.content.ContentValues ct = new android.content.ContentValues();
             ct.put(FollowCol.UID, user.UID);              
             ct.put(FollowCol.Name, user.Name);
             ct.put(FollowCol.SName, user.SName);
             ct.put(FollowCol.Type,  user.isFollower?0:1);
             ct.put(FollowCol.ProfileImgUrl, user.ProfileImgUrl);
             
             ret = context.getContentResolver().insert(CONTENT_URI, ct); 
         }
         
         return ret != null;
    }
    
    
    public ArrayList<Trend> getLastTrend()
    {
    	 ArrayList<Trend> al = new ArrayList<Trend>();
    	 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/trends");
         Cursor cursor = null;
         try
         {
	         cursor = context.getContentResolver().query(CONTENT_URI, TrendProject, null, null, null);
	         if(cursor != null)
	         {     
	             while(cursor.moveToNext())
	             {
	            	 Trend item = new Trend();
	                 item.ID         =  cursor.getString(cursor.getColumnIndex(TrendsCol.ID));  
	                 item.Name       =  cursor.getString(cursor.getColumnIndex(TrendsCol.Name));
	                 item.URL        =  cursor.getString(cursor.getColumnIndex(TrendsCol.URL));              
	                 item.Date       =  new Date(cursor.getLong(cursor.getColumnIndex(TrendsCol.Date))).toLocaleString();
	                     
	                 al.add(item);
	             }             
	         }
	     }
	     catch(SQLiteException ne)
	     {}
	     finally
	     {
		     if(cursor != null)
		     {
		        cursor.close();
		     }
	     }
         return al;    	
    }
    
    public int UpdateTrend(ArrayList<Trend> trends)
    {
    	//remove all first
    	int ret = -1;
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/trends");
        try{
            ret = context.getContentResolver().delete(CONTENT_URI, null, null);
        }catch(SQLiteException ne){}
                
    	//insert them
        android.content.ContentValues ct = new android.content.ContentValues();
        for(int i=0;i<trends.size();i++)
        {
        	Trend item = trends.get(i);
	        ct.put(TrendsCol.Name, item.Name);              
	        ct.put(TrendsCol.URL,  item.URL);
	        ct.put(TrendsCol.Date, new Date(item.Date).getTime());
	        
	        context.getContentResolver().insert(CONTENT_URI, ct); 	        
        }
    	return 0;
    }
      
    public Account getTwitterAccount()
    {
    	Account ac = new Account();
    	ac.email    = getSettingValue(twitter_account);//"jessierettiwt";//
    	ac.password =getSettingValue(twitter_pwd);//"111111";//   
    	ac.uid      =getSettingValue(twitter_uid);//"jessierettiwt";//getSettingValue(twitter_uid);
    	ac.screenname = getSettingValue(twitter_screen_name);
    	ac.token = getSettingValue(twitter_token);
    	ac.token_secret = getSettingValue(twitter_token_secret);
    	//ac.token = "17439563-gR3TeviSZkej1a4tV0XVoIKyrJjCs5SpxGyO5SsgV";
    	//ac.token_secret="jbL3gQcthsMJ0xAQZL39W4jMeafyRv9XEgojzUubc8";
    	return ac;
    }
    
    public int getTweetViewCount()
    {
        int nCount=20;
        try{
            nCount = Integer.parseInt(getSettingValue(tweet_view_count));
        }catch(NumberFormatException ne){}
        return  nCount<=0?20:nCount;
        
    }
    
    public int getFacebookFriendViewCount()
    {
        int nCount=10;
        try{
            nCount = Integer.parseInt(getSettingValue(f_friend_view_count));
        }catch(NumberFormatException ne){}
        FFriendViewCount =  nCount<=0?5:nCount;
        return FFriendViewCount;
    }
    
    public int setFaceFriendViewCount(String strCount)
    {
        int nCount = 10;
        try{
            nCount = Integer.parseInt(strCount);
            nCount = nCount>20?20:nCount;
            
            FFriendViewCount = nCount;
            this.addSetting(f_friend_view_count, String.format("%1$s", nCount));
            
        }catch(NumberFormatException ne){}        
       
        return nCount;
    }
    
    public int getFollowViewCount()
    {
        int nCount=10;
        try{
            nCount = Integer.parseInt(getSettingValue(follow_view_count));
        }catch(NumberFormatException ne){}
        FollowViewCount =  nCount<=0?10:nCount;
        return FollowViewCount;
    }
    
    public int setFollowViewCount(String strCount)
    {
        int nCount = 10;
        try{
            nCount = Integer.parseInt(strCount);
            nCount = nCount>100?100:nCount;
            FollowViewCount = nCount;
            this.addSetting(follow_view_count, String.format("%1$s", nCount));
            
        }catch(NumberFormatException ne){}
        
       
        return nCount;
    }
    
    
	public long getTrendsTimeout() 
	{		
	    long nCount=60;
        try{
            nCount = Long.parseLong(getSettingValue(trend_view_timeout));
        }catch(NumberFormatException ne){}
        TrendsViewTimeout =  (nCount<=0?30:nCount)*1000;
	    return TrendsViewTimeout;		
	}
	
	public long getTweetTimeout() 
	{		
	    long nCount=60;
        try{
            nCount = Long.parseLong(getSettingValue(tweet_view_timeout));
        }catch(NumberFormatException ne){}
        StatusViewTimeout =  (nCount<=0?60:nCount)*1000;
	    return StatusViewTimeout;		
	}
	
	public int setTweetsViewTimeout(String strCount)
    {
        int nCount = 60;
        try{
            nCount = Integer.parseInt(strCount);
            nCount = nCount<60?60:nCount;
        }catch(NumberFormatException ne){}
        
        StatusViewTimeout = nCount;
        this.addSetting(tweet_view_timeout, String.format("%1$s", nCount));
        return nCount;
    }
    
	public int setTrendsViewTimeout(String strCount)
    {
        int nCount = 60;
        try{
            nCount = Integer.parseInt(strCount);
            nCount = nCount<60?60:nCount;
        }catch(NumberFormatException ne){}
        
        TrendsViewTimeout = nCount;
        this.addSetting(trend_view_timeout, String.format("%1$s", nCount));
        return nCount;
    }
	
    public int setTweetViewCount(String strCount)
    {
        int nCount = 10;
        try{
            nCount = Integer.parseInt(strCount);
            nCount = nCount>20?20:nCount;
            
            this.addSetting(tweet_view_count, String.format("%1$s", nCount));
        }catch(NumberFormatException ne){}        
        
        return nCount;
    }
    
    public Account getFacebookAccount()
    {
    	Account ac = new Account();
    	ac.email    = getSettingValue(facebook_email);
    	ac.password = getSettingValue(facebook_pwd);    	
    	return ac;
    }
    
    public int getFacebookStreamTimeout() 
    {	
        int nCount=120;
        try{
            nCount = Integer.parseInt(getSettingValue(stream_view_timeout));
        }catch(NumberFormatException ne){}        
        return  nCount;
	}
    
    public int setFacebookStreamTimeout(int seconds) 
    {
        int nCount=120;
        if(seconds >120)
        {
            nCount = seconds;
        }
        this.addSetting(stream_view_timeout, String.valueOf(nCount));
        return  nCount;
	}
    
    public int getFacebookContactUpdatePeriod(){
    	int ret = 3;
    	try{
    		String org_ret = getSettingValue(facebook_contact_update_period);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Integer.parseInt(org_ret.trim());
    		} 		
    	}catch(NumberFormatException ne){}
    	
    	return ret;
    }
    
    
    public int setFacebookContactUpdatePeriod(int day)
    {
    	addSetting(facebook_contact_update_period, String.valueOf(day));
        return day;
    }
    
    public int getFacebookFriendUpdatePeriod(){
    	int ret = 3;
    	try{
    		String org_ret = getSettingValue(facebook_friend_update_period);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Integer.parseInt(org_ret.trim());
    		} 		
    	}catch(NumberFormatException ne){}
    	
    	return ret;
    }
    
    public int setFacebookFriendUpdatePeriod(int day)
    {
       this.addSetting(facebook_friend_update_period, String.valueOf(day));
       return day;
    }
    
    public int setFacebookMailUpdatePeriod(int hours)
    {
    	addSetting(facebook_mail_check_period, String.valueOf(hours));
        return hours;
    }
    
    public int getFacebookMailUpdatePeriod()
    {
    	int ret = 6;
    	try{
    		String org_ret = getSettingValue(facebook_mail_check_period);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Integer.parseInt(org_ret.trim());
    		} 		
    	}catch(NumberFormatException ne){}
    	
    	return ret;
    }
    
    public int setAddressbookLookupPeriod(int period)
    {
        addSetting(facebook_addressbook_sync_period,String.valueOf(period));
        return period;
    }
    
    public int getAddressbookLookupPeriod()
    {
    	int ret = 7;
        try
        {
     	  String org_ret = getSettingValue(facebook_addressbook_sync_period);
     	  if(org_ret != null && !org_ret.equals(""))
     	  {
     		 ret = Integer.parseInt(org_ret.trim());
     	  }
     	   
        }catch(NumberFormatException ne){}
        
        return ret;
    }    
    
    //notification begin
    
    //minutes
    public String getNotificationInterval() 
	{		
		String value = getSettingValue(notification_interval);
		if(isEmpty(value) == true)
		{
			value = "60";
		}
		return value;
	}
	private boolean isEmpty(String value) {		
		return value==null || value.length()==0;
	}
	public void setNotificationInterval(String value) 
	{		
		addSetting(notification_interval, value);
	}
    
    
    public long getNotificationLastTime()
    {
    	long ret = 0;
        try
        {
     	  String org_ret = getSettingValue(facebook_notification_last_update_time);
     	  if(org_ret != null && !org_ret.equals(""))
     	  {
     		 ret = Integer.parseInt(org_ret.trim());
     	  }
     	   
        }catch(NumberFormatException ne){}
        
        return ret;
    }
    
    public long setLastNotificationSyncTime(long time){
    	long ret = 0;
    	if(this.addSetting(facebook_notification_last_update_time, String.valueOf(time))!=null){
    		ret = time;
    	}  
    	return ret;
    }
    
    
    //notification end
    
    public int getFacebookIconSizeSetting()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(facebook_icon_size);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret;
    }
    
    public String setFacebookIconSizeSetting(String value)
    {
        addSetting(facebook_icon_size,value);
        return value;
    }
    
    
   
    
    public boolean getFacebookShowOnHomescreen()
    {
        int ret = 1;
        try{
            String org_ret = getSettingValue(facebook_show_on_homescreen);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;

    }
    
    public boolean setFacebookShowOnHomescreen(boolean value)
    {
        Uri uri = this.addSetting(facebook_show_on_homescreen, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getFacebookUseLogo()
    {
        int ret = 1;
        try{
            String org_ret = getSettingValue(facebook_use_logo);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    
    public boolean setFacebookUseLogo(boolean value)
    {
        Uri uri = this.addSetting(facebook_use_logo, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getFacebookUseEmail()
    {
        int ret = 1;
        try{
            String org_ret = getSettingValue(facebook_use_email);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    
    public boolean setFacebookUseEmail(boolean value)
    {
        Uri uri = this.addSetting(facebook_use_email, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getFacebookUsePhonenumber()
    {
        int ret = 1;
        try{
            String org_ret = getSettingValue(facebook_use_phonenumber);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    
    public boolean setFacebookUsePhonenumber(boolean value)
    {
        Uri uri = this.addSetting(facebook_use_phonenumber, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getFacebookUseBirthday()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(facebook_use_birthday);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    
    public boolean setFacebookUseBirthday(boolean value)
    {
        Uri uri = this.addSetting(facebook_use_birthday, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getFacebookSyncBirthdayEvent()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(facebook_sync_birthday_event);
            if(isEmpty(org_ret) == false)
            {
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}        
        return ret==1? true :false;
    }
    
    public boolean setFacebookSyncBirthdayEvent(boolean value)
    {
        Uri uri = this.addSetting(facebook_sync_birthday_event, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    
    //default is false
    public boolean getNotificationVibrate()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(facebook_notification_vibrate);
            if(isEmpty(org_ret) == false)
            {
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}        
        return ret==1? true :false;
    }
    
    //default is false
    public boolean getNotificationLED()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(facebook_notification_led);
            if(isEmpty(org_ret) == false)
            {
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}        
        return ret==1? true :false;
    }
    
    public boolean enableVibrate(boolean value)
    {
        Uri uri = this.addSetting(facebook_notification_vibrate, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean enableLED(boolean value)
    {
        Uri uri = this.addSetting(facebook_notification_led, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
  
    public boolean getTwitterShowOnHomescreen()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(twitter_show_on_homescreen);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    
    public boolean setTwitterShowOnHomescreen(boolean value)
    {
        Uri uri = this.addSetting(twitter_show_on_homescreen, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getTwitterUseHttps()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(twitter_use_https_connection);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    public static boolean is_twitter_use_https_connection = false;
    public boolean setTwitterUseHttps(boolean value)
    {
    	is_twitter_use_https_connection = value;
        Uri uri = this.addSetting(twitter_use_https_connection, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    public boolean getFacebookUseHttps()
    {
        int ret = 0;
        try{
            String org_ret = getSettingValue(facebook_use_https_connection);
            if(org_ret !=null && !org_ret.equals("")){
                ret = Integer.parseInt(org_ret.trim());
            }       
        }catch(NumberFormatException ne){}
        
        return ret==1? true :false;
    }
    
    public static boolean is_facebook_use_https_connection = false;       
    
    public boolean isTwitterUseOriginalPhoto()
    {
        int ret = 0;
        try{
           String org_ret = getSettingValue(twitter_upload_photo_size); 
           if(org_ret !=null &&! org_ret.equals(""))
           {
               ret = Integer.parseInt(org_ret.trim());
           }
        }
        catch(NumberFormatException e){}
       
        return ret==1? true:false;
    }
    
    public boolean isTwitterLoadAutoPhoto()
    {
        int ret = 1;
        try{
           String org_ret = getSettingValue("isTwitterLoadAutoPhoto"); 
           if(org_ret !=null &&! org_ret.equals(""))
           {
               ret = Integer.parseInt(org_ret.trim());
           }
        }
        catch(NumberFormatException e){}
       
        return ret==1? true:false;
    }
    
    public boolean setTwitterLoadAutoPhoto(boolean value)
    {
        Uri uri = this.addSetting("isTwitterLoadAutoPhoto", value?"1":"0");
        if(uri!=null)
             return true;
        else
             return false;
    }
    
    public boolean setTwitterUseOriginalPhoto(boolean value)
    {
        Uri uri = this.addSetting(twitter_upload_photo_size, value?"1":"0");
        if(uri!=null)
             return true;
        else
             return false;
    }
    
    public boolean isFacebookUseOriginalPhoto()
    {
        int ret = 0;
        try{
           String org_ret = getSettingValue(facebook_upload_photo_size); 
           if(org_ret !=null &&! org_ret.equals(""))
           {
               ret = Integer.parseInt(org_ret.trim());
           }
        }
        catch(NumberFormatException e){}
       
        return ret==1? true:false;
    }
    
    public boolean setFacebookUseOriginalPhoto(boolean value)
    {
        Uri uri = this.addSetting(facebook_upload_photo_size, value?"1":"0");
        if(uri!=null)
             return true;
        else
             return false;
    }
    
    
    public long getLastUpdateFriendTime(){
    	long ret = 0;
    	try{
    		String org_ret = getSettingValue(facebook_friend_last_update_time);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Long.parseLong(org_ret);
    		} 		
    	}catch(NumberFormatException ne){}
    	return ret;
    }
    
    public long getLastAddressbookSyncTime()
    {
    	long ret = 0;
    	try{
    		String org_ret = getSettingValue(facebook_addressbook_last_update_time);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Long.parseLong(org_ret);
    	    } 		
    	}catch(NumberFormatException ne){}
    	return ret;
    }
    
    public long getLastUpdateContactTime(){
    	long ret = 0;
    	try{
    		String org_ret = getSettingValue(facebook_contact_last_update_time);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Long.parseLong(org_ret);
    	    } 		
    	}catch(NumberFormatException ne){}
    	return ret;
    }
    
    public long getLastDataCheckTime(){
    	long ret = 0;
    	try{
    		String org_ret = getSettingValue(date_check_time);
    		if(org_ret !=null && !org_ret.equals("")){
    			ret = Long.parseLong(org_ret);
    	    } 		
    	}catch(NumberFormatException ne){}
    	return ret;
    }
    
    
    public long setLastUpdateFriendTime(long time){
    	long ret = 0;
    	if(this.addSetting(facebook_friend_last_update_time, String.valueOf(time))!=null){
    		ret = time;
    	}  
    	return ret;
    }
    
    public long setLastUpdateContactTime(long time){
    	long ret = 0;
    	if(this.addSetting(facebook_contact_last_update_time, String.valueOf(time))!=null){
    		ret = time;
    	}  
    	return ret;
    }
    public long setLastDataCheckTime(long time)
    {
    	long ret = 0;
    	if(this.addSetting(date_check_time, String.valueOf(time))!=null){
    		ret = time;
    	}  
    	return ret;
    }
    
    public long setLastAddressbookSyncTime(long time){
    	long ret = 0;
    	if(this.addSetting(facebook_addressbook_last_update_time, String.valueOf(time))!=null){
    		ret = time;
    	}  
    	return ret;
    }
    
    public boolean updateFacebookAccount(String email, String pwd)
    {
    	this.addSetting(facebook_email, email);
    	this.addSetting(facebook_pwd,   pwd);    	
    	return true; 
    }
    
    public boolean updateFacebookAccount(String email)
    {
    	this.addSetting(facebook_email, email);
    	return true; 
    }
    public boolean updateFacebookPwd(String pwd)
    {	
    	this.addSetting(facebook_pwd,   pwd);    	
    	return true; 
    }
    public boolean updateTwitterAccount(String username, String pwd)
    {
    	this.addSetting(twitter_account, username);
    	this.addSetting(twitter_pwd,     pwd);    	
    	return true; 
    }
    
    public boolean updateTwitterPwd( String pwd)
    {
    	this.addSetting(twitter_pwd,     pwd);
    	return true; 
    }
    public boolean updateTwitterUID( String uid)
    {
    	this.addSetting(twitter_uid,     uid);
    	return true; 
    }
    public boolean updateTwitterScreenname( String sname)
    {
        this.addSetting(twitter_screen_name,     sname);
        return true; 
    }
    public boolean updateTwitterUsername( String username)
    {
    	this.addSetting(twitter_account, username);
    	return true; 
    }
    
    public List<Settings> getSettings(String name)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/settings");
        String[] selectionArgs = new String[1];
        selectionArgs[0] = "";
        String queryString ="";
        
        try{
            if(name != null)
            {
                selectionArgs[0] = name;
                queryString = " name=? ";
            }
            else
            {
                queryString   = null;
                selectionArgs = null;
            }
        }catch(NumberFormatException ne){}
        
        List<Settings> ls = new ArrayList<Settings>();
        Cursor cursor = null;
        try
        {
	        cursor = context.getContentResolver().query(CONTENT_URI, settingsProject, queryString, selectionArgs, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	                Settings item = new Settings();
	                item.ID         =  cursor.getString(cursor.getColumnIndex(SettingsCol.ID));  
	                item.Name       =  cursor.getString(cursor.getColumnIndex(SettingsCol.Name));
	                item.Value      =  cursor.getString(cursor.getColumnIndex(SettingsCol.Value));              
	                    
	                ls.add(item);
	            }          
	        }
         }
	     catch(SQLiteException ne)
	     {}
	     finally
	     {
		     if(cursor != null)
		     {
		        cursor.close();
		     }
	     }
         return ls;
    } 
    public String getSettingValue(String name)
    {
        String va = null;
        List<Settings> st = getSettings(name);
        if(st.size() > 0)
        {
            va = st.get(0).Value;
        }
        return va;
    }
    
    public boolean removeSetting(String name)
    {
        int ret = -1;
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/settings");
        try{
            ret = context.getContentResolver().delete(CONTENT_URI, " name='"+name+"'", null);
        }catch(SQLiteException ne){}
        return ret > 0;
    } 
    
    public Uri addSetting(String name, String value)
    {
        Uri ret = null;
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/settings");
        android.content.ContentValues ct = new android.content.ContentValues();
        ct.put(SettingsCol.Name, name);              
        ct.put(SettingsCol.Value, value);
        
        //if exist, update
        if(getSettings(name).size() > 0)
        {
            updateSetting(name, value);
        }
        else
        {        
            ret = context.getContentResolver().insert(CONTENT_URI, ct); 
        }
        
        return ret;
    } 
    
    public boolean updateSetting(String name, String value)
    {
        boolean ret = false;
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/settings");
        String where = String.format(" name = \"%1$s\" ", name);
        android.content.ContentValues ct = new android.content.ContentValues();
        //ct.put(IJettyORM.SettingsCol.Name, name);              
        ct.put(SettingsCol.Value, value);
        
        if(context.getContentResolver().update(CONTENT_URI, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
    }
   
    


    
    public String getFacebookUserNames(List<Long> uids)
    {
        String where =  null;
        String[] paras = null;
        
        String ret = "";
        //remove owner
        uids.remove(loginid);
        
        if(uids!=null && uids.size()>0)
        {
           where = "";
           paras = new String[uids.size()];
           for(int i = 0 ; i< uids.size() ; i++)
           {
               if(i>0)
                   where += ",";
               
                where    += uids.get(i) ;
                paras[i] = String.valueOf(uids.get(i));
           }
          
           where = " uid in (" + where + ")";
           Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
           String[] UsersProject = {
               "_id",
               "uid",              
               "name",              
           };
           
           Cursor cursor = null;
           try{
	           cursor = context.getContentResolver().query(CONTENT_URI, UsersProject, where, null, null);	           
	           if(cursor != null)
	           {    
	               while(cursor.moveToNext())
	               {    
	                   if(ret.length()>0)
	                       ret +=",";
	                   
	                   ret += cursor.getString(cursor.getColumnIndex(FacebookUsersCol.NAME));                    
	               }	               
	            }
            }
	  	    catch(SQLiteException ne)
	  	    {}
	  	    finally
	  	    {
	  		    if(cursor != null)
	  		    {
	  		       cursor.close();
	  		    }
	  	    }
        }        
        return ret;         
    }
    


    
    public List<Long> getFriendIDs(long uid)
    {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends"); 
   	   Cursor cursor = null;
   	   String where = FacebookFriendsCol.UID1+" = "+uid;
   	   ArrayList<Long> ls = new ArrayList<Long>();
   	   try{
	       cursor = context.getContentResolver().query(CONTENT_URI, FacebookFriendsProject, where , null, null);	       
	       if(cursor != null)
	       {          	
	           while(cursor.moveToNext())
	           {
	              ls.add(cursor.getLong(cursor.getColumnIndex(FacebookFriendsCol.UID2)));
	           }
	       }
        }
        catch(SQLiteException ne)
        {}
        finally
        {
	        if(cursor != null)
	        {
	           cursor.close();
	        }
        }
        return ls;
   }
   
   public boolean removeFriends(){
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends"); 
	   if(context.getContentResolver().delete(CONTENT_URI, null, null)>0){
		   return true;
	   }else{
		   return false;
	   }	   
   }
   
   public boolean removeFriendsByuid(long uid){
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends"); 
	   String where = FacebookFriendsCol.UID1+" = ? ";
	   String[] params = {String.valueOf(uid)};
	   if(context.getContentResolver().delete(CONTENT_URI, where, params)>0){
		   return true;
	   }else{
		   return false;
	   }	   
   }
   
   
   public boolean isFriends(long uid1, long uid2)
   {
       return isExistFriends(uid1, uid2);       
   }
   
   public void addFriends(long uid, long[] uids, boolean removepre){
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends");
       if(removepre == true)
       {
           context.getContentResolver().delete(CONTENT_URI, FacebookFriendsCol.UID1 +"="+uid, null);
       }       
       addFriends(uid, uids);       
   }
   
   public void addFriends(long uid, long[] uids){
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends");
	   List<ContentValues> cvs_list = new ArrayList<ContentValues>();
	   for(int i=0;i<uids.length;i++){
	      
	      if(isExistFriends(uid, uids[i]) == false)
	      {
	          ContentValues cv = new ContentValues();
	          cv.put(FacebookFriendsCol.UID1, uid);
	          cv.put(FacebookFriendsCol.UID2, uids[i]);
	          cvs_list.add(cv);
	      } 
	   }	
	   
	   if(cvs_list.size()>0)
	   {
	       ContentValues[] cvs = new ContentValues[cvs_list.size()];
	       for(int j=0;j<cvs.length;j++)
	       {
	           cvs[j]=cvs_list.get(j);
	       }
	       context.getContentResolver().bulkInsert(CONTENT_URI, cvs); 
	       cvs_list.clear();
	       cvs_list = null;
	       cvs = null;
	   }
	  
   }
   
   private boolean isExistFriends(long uid1, long uid2)
   {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends");
       Cursor cursor = null;
       String[] projection = {FacebookFriendsCol.UID2};
       String whereclause = FacebookFriendsCol.UID1 + "="+uid1 + " and " + FacebookFriendsCol.UID2 +"="+uid2;
       try{
           cursor = context.getContentResolver().query(CONTENT_URI, projection,whereclause , null,null);
           if(cursor != null && cursor.getCount()>0)
           {
               cursor.close();
               cursor = null;
               return true;
           }
       }
       catch(Exception e){
           Log.d(TAG,"isExistFriends exception "+e.getMessage());
       }
       finally
       {
           if(cursor!=null)
           {
               cursor.close();
               cursor = null;
           }
       }
       return false;
   }
   
   public boolean deleteAllFriendsShip()
   {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends");
       int ret = context.getContentResolver().delete(CONTENT_URI, null, null);
       if(ret >0 )
       {
           return true;
       }
       else
       {
           return false;
       }
   }
   
   public boolean removeFriends(long uid)
   {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends");
       String where = FacebookFriendsCol.UID1 +" = "+uid;
       int ret = context.getContentResolver().delete(CONTENT_URI, where, null);
       if(ret >0 )
       {
           return true;
       }
       else
       {
           return false;
       }
   }
   
   public boolean removeFacebookUser(long uid)
   {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
       String where = " where "+FacebookUsersCol.UID +" = "+uid;
       int ret = context.getContentResolver().delete(CONTENT_URI, where, null);
       if(ret >0 )
       {
           return true;
       }
       else
       {
           return false;
       }
   }

   public boolean clearExtPermissions() {
	  Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/extpermission");
	  int ret = context.getContentResolver().delete(CONTENT_URI, null, null);
	  return ret > 0 ? true : false;
  }
     
   public boolean disableExtPermissions(String permission) {
	   boolean ret =  updateExtPermissions(permission,false);
	  return ret ;
   }
   
   public boolean enableExtPermissions(String permission) {
	   boolean ret = updateExtPermissions(permission,true);
	return ret;
  }
   
   public boolean isExistedExtPermissions(String permission){
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/extpermission");
	   String where = "methodname = ? ";
	   String[] params = {permission};
	   Cursor cursor = null;
	   boolean ret   = false;
	   try{
		   cursor = context.getContentResolver().query(CONTENT_URI, ExtPermissionProject, where, params,null);		   
		   if(cursor != null){
			   ret = cursor.getCount() > 0 ? true:false;
		   } 
	   }
       catch(SQLiteException ne)
       {}
       finally
       {
	       if(cursor != null)
	       {
	          cursor.close();
	       }
       }
	   return ret;
   }
   
   public Uri saveExtPermissions(String permission,boolean allowed){
	   int valid = allowed?1:0;
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/extpermission");
	   ContentValues values = new ContentValues();
	   values.put(ExtPermissionsCol.METHODNAME, permission);
	   values.put(ExtPermissionsCol.PERMISSION, valid);
	   Uri uri = null;
	   try{
		   uri = context.getContentResolver().insert(CONTENT_URI, values);
	   }catch(SQLiteException e){
		   Log.d(TAG,"save ExtPermission Exception "+e.getMessage());
	   }
	   return uri;
   }
   
   private int updateExtPermissionsInternal(String permission,boolean allowed){
	   int valid = allowed?1:0;
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/extpermission");
	   String where = "methodname = ? ";
	   String[] params = {permission};
	   ContentValues values = new ContentValues();
	   values.put(ExtPermissionsCol.PERMISSION, valid);
	   int ret = -1;
	   try{
		  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
	   }catch(SQLiteException e){
		  Log.d(TAG, "updateExtPermission Exception "+e.getMessage());
	   }
	   return ret;
   }
   
   public boolean updateExtPermissions(String permission,boolean allowed){
	   boolean ret = false;
	   if(isExistedExtPermissions(permission)){
		   int retvalue = updateExtPermissionsInternal(permission,allowed);
		   ret = retvalue > 0 ? true:false;
	   }else{
		   saveExtPermissions(permission,allowed);
		   ret = true;
	   }
	   return ret;
   }
   
   public boolean isExtPermissionAllow(String permission) {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/extpermission");
	   String where = " methodname = ? ";
	   String[] params = {permission};
	   
	   Cursor cursor = null;
	   boolean ret = false;
	   try{
		   cursor = context.getContentResolver().query(CONTENT_URI, ExtPermissionProject, where, params, null);
		   if(cursor != null && cursor.getCount() > 0)
		   {
		       cursor.moveToFirst();
		       int val = cursor.getInt(cursor.getColumnIndex(ExtPermissionsCol.PERMISSION));		       	       
		       ret = (val == 0)?false:true;
		   }
	   }
	   catch(SQLiteException ne)
	   {
		   ret = false;
	   }
	   finally
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
	   
	   return ret;
  }
   
   public boolean clearFacebookContact(){
    	int ret = -1;
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
    	try{
    	      ret = context.getContentResolver().delete(CONTENT_URI, null, null);
    	}catch(SQLiteException ne){
    	      Log.d(TAG, "delete FacebookContacts exception "+ne.getMessage());
    	}
	    return ret > 0 ?true : false;
   }
   
   //for PeopleMapFacebook
   public int addPeopleMapFacebook(List<PeopleMapFacebook> peoples)
   {
	   int count=0;
	   for(int i=0;i<peoples.size();i++)
	   {
		   PeopleMapFacebook item = peoples.get(i);
	       if(isPeopleMapFacebookExist(item.uid))
	       {
	    	   updatePeopleMapFacebook(item);
	       }
	       else
	       {
	    	   insertPeopleMapFacebook(item);
	       }
	   }
	   
	   return count;
   }
   
   private void insertPeopleMapFacebook(PeopleMapFacebook people){
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/peoplemapfacebook");
       ContentValues ct = new ContentValues();
     
       ct.put(peoplemapfacebookCol.UID,      people.uid);
	   ct.put(peoplemapfacebookCol.PeopleID, people.peopleid);
	   ct.put(peoplemapfacebookCol.Requested,  people.requested==true?1:0);	  
	   
	   context.getContentResolver().insert(CONTENT_URI, ct);  
   }  
   
   private boolean isPeopleMapFacebookExist(long uid)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/peoplemapfacebook"); 
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   
	   boolean ret = false;
	   Cursor cursor = null;
	   try{
	  	   cursor = context.getContentResolver().query(CONTENT_URI, peoplemapfacebooProject, where, params, null);  	   
	       if(cursor != null && cursor.getCount() > 0)
	       {
	           ret = true;
	       }
	   }
	   catch(SQLiteException ne)
	   {
		   ret = false;
	   }
	   finally
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return ret;
   }
   
   public int reqestedFriends(long uid, boolean requested)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/peoplemapfacebook");
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   ContentValues values = new ContentValues();
	   values.put(peoplemapfacebookCol.Requested, requested==true?1:0);
	   int ret = -1;
	   try{
		  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
	   }catch(SQLiteException e){
		  Log.d(TAG, "reqestedFriends Exception "+e.getMessage());
	   }
	   return ret;
   }
   
   public int updatePeopleMapFacebook(PeopleMapFacebook people)
   {	   
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/peoplemapfacebook");
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(people.uid)};
	   ContentValues ct = new ContentValues();
	   ct.put(peoplemapfacebookCol.UID,      people.uid);
	   ct.put(peoplemapfacebookCol.PeopleID, people.peopleid);
	   ct.put(peoplemapfacebookCol.Requested,  people.requested==true?1:0);	  
	   int ret = -1;
	   try{
		  ret = context.getContentResolver().update(CONTENT_URI, ct, where, params);
	   }catch(SQLiteException e){
		  Log.d(TAG, "updatePeopleMapFacebook Exception "+e.getMessage());
	   }
	   return ret;
   }
   
   
   public List<PeopleMapFacebook> getPeopleMapFacebook(long uid)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/peoplemapfacebook"); 
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   
  	   Cursor cursor = context.getContentResolver().query(CONTENT_URI, peoplemapfacebooProject, where, params, null);
  	   List<PeopleMapFacebook> al = new ArrayList<PeopleMapFacebook>();
       if(cursor != null)
       {       	 
           while(cursor.moveToNext())
           {
        	   PeopleMapFacebook friends = new PeopleMapFacebook();
        	   friends.uid      = cursor.getLong(cursor.getColumnIndex(peoplemapfacebookCol.UID));
        	   friends.peopleid  = cursor.getInt(cursor.getColumnIndex(peoplemapfacebookCol.PeopleID));
        	   friends.requested = cursor.getInt(cursor.getColumnIndex(peoplemapfacebookCol.Requested))==0?false:true;
               
               al.add(friends);
           }
           cursor.close();
       }
       return al;
   }
   
   public List<PeopleMapFacebook> getPeopleMapFacebooks()
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/peoplemapfacebook"); 
	   
  	   Cursor cursor = null;
  	   List<PeopleMapFacebook> al = new ArrayList<PeopleMapFacebook>();
  	   try
  	   {
	  	   cursor = context.getContentResolver().query(CONTENT_URI, peoplemapfacebooProject, null, null, null);	  	   
	       if(cursor != null)
	       {       	 
	           while(cursor.moveToNext())
	           { 
	        	   PeopleMapFacebook friends = new PeopleMapFacebook();
	        	   friends.uid      = cursor.getLong(cursor.getColumnIndex(peoplemapfacebookCol.UID));
	        	   friends.peopleid  = cursor.getInt(cursor.getColumnIndex(peoplemapfacebookCol.PeopleID));
	        	   friends.requested = cursor.getInt(cursor.getColumnIndex(peoplemapfacebookCol.Requested))==0?false:true;
	               
	               al.add(friends);
	           }        
	       }
  	   }
       catch(SQLiteException ne)
	   {}
	   finally
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return al;
   }
   //end for facebook and people map
   
   
   private boolean isPhoneBookExist(long uid)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook"); 
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   
	   boolean ret = false;
  	   Cursor cursor = null;
  	   try{
	  	   cursor = context.getContentResolver().query(CONTENT_URI, PhonebookProject, where, params, null);  	   
	       if(cursor != null && cursor.getCount() > 0)
	       {       	 
	          ret = true;       
	       }
  	   }
       catch(SQLiteException ne)
	   {
    	   ret = false;
	   }
	   finally
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return ret;
   }
   
   public int updateSyncTag(long uid, boolean synced)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   ContentValues values = new ContentValues();
	   values.put(PhonebookCol.Synced, synced);
	   int ret = -1;
	   try{
		  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
	   }catch(SQLiteException e){
		  Log.d(TAG, "updateSyncTag Exception "+e.getMessage());
	   }
	   return ret;
   }
   
  

   /*
    * Mail Thread and Message 
    */
   public class MailMessageCol{
        public static final String ID       = "_id";
        public static final String ThreadID = "threadid";
        public static final String Mid      = "mid";
        public static final String Author   = "author";
        public static final String TimeSent = "timesent";
        public static final String Body     = "body";
        public static final String HasAttachment = "hasattachment";
        public static final String Synced        = "synced";
    }
	 
    public static String[]MailMessageProject =  new String[]{
    	 "_id",
		 "threadid",
		 "mid",
		 "author",
		 "timesent",
		 "body",
		 "hasattachment",
		 "synced",	        
    };
	
	
	
	private static long loginid = 0;
	private boolean savetoMessage=true;
	
	
	private List<Long> getLongArry(String uids)
	{
		List<Long> lids = new ArrayList<Long>();
		if(uids != null)
		{
		    String[] struids = uids.split(",");
		    if(struids != null)
		    {
		    	for(String item:struids)
		    	{
		    	    lids.add(Long.valueOf(item));
		    	}
		    }
		}
		return lids;
	}
	
	public int updateUserBDSyncTag(long uid, boolean synced,long ceid)
    {
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
		   String where = " uid = ? ";
		   String[] params = {String.valueOf(uid)};
		   ContentValues values = new ContentValues();
		   values.put(FacebookUsersCol.EVENT_SYNC,synced);
		   values.put(FacebookUsersCol.EVENT_LAST_SYNCTIME, new Date().getTime());
		   values.put(FacebookUsersCol.C_EVENT_ID, ceid);
		   int ret = -1;
		   try{
			  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
		   }catch(SQLiteException e){
			  Log.d(TAG, "updateSyncTag Exception "+e.getMessage());
		   }
		   return ret;
	}

	String[] monthArray = {"January","February","March","April","May","June","Junly","August","September","December","Octomber","Nobember"};	
	
	public Cursor getUpComingBDUserCursor(String month)
	{
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
        String where = " birthday like '"+month+"%'";   
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where,null, null);         
        }
        catch(SQLiteException ne)
        {}
        return cursor;   
	}
	
	
    //
	//when user re-login with different account, need remove phonebook, facebook user, ......
    //but if with same account, to speed up the access, we keep the pre-content
    //
	//when to do? maybe the user just want to logout, she don't want to switch user
	//so we can do this when success login.
	//
	public void clearCurrentUserData() 
	{
		Log.d(TAG, "call clearCurrentUserData");
		
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookfriends");
    	int count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove facebookfriends ="+count);
    	
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove phonebook ="+count);
    	
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove page ="+count);
		
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove facebookusers ="+count);
		
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/extpermission");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove extpermission ="+count);
		
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove facebookevent ="+count);
    			
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);
    	Log.d(TAG, "remove mailthread ="+count);
		
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);		
    	Log.d(TAG, "remove mailmessage ="+count);
    	
    	CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note");
    	count = context.getContentResolver().delete(CONTENT_URI, null, null);		
    	Log.d(TAG, "remove note ="+count);
    	removeSetting("lastNotificationID");
    	removeSetting(facebook_notification_last_update_time);
    	removeSetting(facebook_addressbook_last_update_time);
    	removeSetting(facebook_contact_last_update_time);
    	removeSetting(facebook_friend_last_update_time);
    	removeSetting(date_check_time);
	}
	
	
	
	public void clearWallDraftByFuid(long fuid){
		if(fuid <= 0)
			return ;
		
		Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/walldraft");
		String where = " fuid = ? ";
		String[] params = {String.valueOf(fuid)};
		int count = context.getContentResolver().delete(CONTENT_URI, where, params);
		Log.d(TAG,"remove walldraft = "+count);
		
	}
	

	
	public String getProxyHost() 
	{		
		return getSettingValue(proxy_host);
	}
    public void setProxyHost(String host) 
    {
    	addSetting(proxy_host, host);
	}
    
	public String getProxyPort() 
	{	
		return getSettingValue(proxy_port);
	}
	public void setProxyPort(String port) 
	{	
		addSetting(proxy_port, port);
	}
	
	public String getProxyUsername() 
	{	
		return getSettingValue(proxy_username);
	}
	public void setProxyUsername(String username) 
	{
		addSetting(proxy_username, username);
	}
	
	public String getProxyPassword() 
	{	
		return getSettingValue(proxy_password);
	}
	public void setProxyPassword(String pwd) 
	{		
		addSetting(proxy_password, pwd);
	}
	public boolean isProxyEnable()
	{
		boolean ret = false;
		String data = getSettingValue(proxy_enable);
		if(data != null)
		{
			ret =  data.equals("1")?true:false;
		}
		else
		{
			ret = false;
		}
		
		//set proxy
		int port = 80;
		try{
		    port = Integer.valueOf(getProxyPort());
		}catch(NumberFormatException ne){}
		
		
		HttpClient.setProxy(ret, 
				getProxyHost(), 
				port,
				getProxyUsername(), 
				getProxyPassword());
		
		
		return ret;
	}
	
	public void setProxyEnable(boolean checked)
	{
		addSetting(proxy_enable, String.valueOf(checked==true?1:0));		
		
		int port = 80;
		try{
		    port = Integer.valueOf(getProxyPort());
		}catch(NumberFormatException ne){}
				

		HttpClient.setProxy(checked, 
				getProxyHost(), 
				port,
				getProxyUsername(), 
				getProxyPassword());
		
	}
	
	
    public void cleanAllMail() 
    {
        int ret = -1;
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
        try{
              ret = context.getContentResolver().delete(CONTENT_URI, null, null);
              Log.d(TAG, " delete mail message count="+ret);
        }catch(SQLiteException ne){
              Log.d(TAG, "delete mail message exception "+ne.getMessage());
        }     
        
        CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");
        try{
              ret = context.getContentResolver().delete(CONTENT_URI, null, null);
              Log.d(TAG, " delete mail thread count="+ret);
        }catch(SQLiteException ne){
              Log.d(TAG, "delete mail thread exception "+ne.getMessage());
        }      
    }
    
    //default true
	public boolean isNotificationEnable()
	{
		try{
	        int ienable = Integer.parseInt(getSettingValue(enablenotifications));
	        return ienable==0?false:true;
		}catch(NumberFormatException ne){}
		return true;
    }
	
	public void setNotificationEnable(boolean enable) 
	{
		addSetting(enablenotifications, String.valueOf(enable==true?1:0));
	}
	
	/*
	 * enablenotifications_message
	 * enablenotifications_pokes
	 * enablenotifications_event
	 * enablenotifications_group
	 * enablenotifications_request
	 * 
	 * default is true
	 */
	public boolean isNotificationEnable(int type)
	{
		try{
	        int ienable = 1;
	        switch(type)
	        {
		        case 0:
		        {
		        	ienable = Integer.parseInt(getSettingValue(enablenotifications_message));
		        	break;
		        }
		        case 1:
		        {
		        	ienable = Integer.parseInt(getSettingValue(enablenotifications_pokes));
		        	break;
		        }
		        case 2:
		        {
		        	ienable = Integer.parseInt(getSettingValue(enablenotifications_event));
		        	break;
		        }
		        case 3:
		        {
		        	ienable = Integer.parseInt(getSettingValue(enablenotifications_group));
		        	break;
		        }
		        case 4:
		        {
		        	ienable = Integer.parseInt(getSettingValue(enablenotifications_request));
		        	break;
		        }
	        }
	        
	        return ienable==0?false:true;
		}catch(NumberFormatException ne){}
		return true;
    }
	
	public void setNotificationEnable(int type, boolean enable) 
	{
		switch(type)
        {
	        case 0:
	        {
	        	addSetting(enablenotifications_message, String.valueOf(enable==true?1:0));
	        	break;
	        }
	        case 1:
	        {
	        	addSetting(enablenotifications_pokes, String.valueOf(enable==true?1:0));
	        	break;
	        }
	        case 2:
	        {
	        	addSetting(enablenotifications_event, String.valueOf(enable==true?1:0));
	        	break;
	        }
	        case 3:
	        {
	        	addSetting(enablenotifications_group, String.valueOf(enable==true?1:0));
	        	break;
	        }
	        case 4:
	        {
	        	addSetting(enablenotifications_request, String.valueOf(enable==true?1:0));
	        	break;
	        }
        }		
	}
	
	
	
	/*
	 * if none friends member big than 100, remove them
	 */
    public void checkNoneFriends() 
    {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
       String[] projection = {FacebookUsersCol.ID};
       Cursor cursor = null;
       long offset = 500;
       Uri CONTENT_URI_LIMIT = ContentUris.withAppendedId(CONTENT_URI, 0);
       CONTENT_URI_LIMIT = ContentUris.withAppendedId(CONTENT_URI_LIMIT, offset);
       Log.d(TAG,"checkNoneFriens uri is "+CONTENT_URI_LIMIT.toString());
       try{
           
          cursor = context.getContentResolver().query(CONTENT_URI_LIMIT, null, null, null, null);
          
          if(cursor !=null && cursor.getCount() >= offset)
          {
              Log.d(TAG," cursor count is "+cursor.getCount());
              String sql = "select _id from facebookusers where uid not in (select uid from facebookfriends) limit 0,"+offset;
              String where = FacebookUsersCol.ID+" in "+"("+sql+")";
              int row = context.getContentResolver().delete(CONTENT_URI,where, null);
              Log.d(TAG,"delete facebookusers who are not my friends successfuly, row = "+row);
          }
          
       }catch(SQLiteException e)
       {
           Log.d(TAG," checkNoneFriends exception "+e.getMessage());
       }
        finally{
            if(cursor !=null)
            {
                cursor.close();
                cursor = null;
            }
           
        }
        
    }
    
    public void logoutTwitter()
	 {
	     removeSetting(twitter_account);
	     removeSetting(twitter_pwd);
	     removeSetting(twitter_uid);
	     removeSetting(twitter_screen_name);	
	     removeSetting(twitter_token);
	     removeSetting(twitter_token_secret);
	 }
    
    
    final Uri mailthread_uri =  Uri.parse(SNS_CONTENT_URI+"/mailthread");
    final Uri mailmessage_uri = Uri.parse(SNS_CONTENT_URI+"/mailmessage");
    final Uri album_uri = Uri.parse(SNS_CONTENT_URI+"/album");
    final Uri event_uri = Uri.parse(SNS_CONTENT_URI+"/facebookevent");
    final Uri note_uri = Uri.parse(SNS_CONTENT_URI+"/note");
    final Uri photo_uri = Uri.parse(SNS_CONTENT_URI+"/photo");
    public void clearDatabase() {
        Log.d(TAG,"low space clear database");
        context.getContentResolver().delete(mailthread_uri, null, null);
        context.getContentResolver().delete(mailmessage_uri, null, null);
        context.getContentResolver().delete(album_uri, null, null);
        context.getContentResolver().delete(event_uri, null, null);
        context.getContentResolver().delete(note_uri, null, null);
        context.getContentResolver().delete(photo_uri, null, null);
    }
    
    final static String tweetnotifymode = "tweetnotifymode";
    public int getTweetNotifyMode() {
        // TODO Auto-generated method stub
        int default_mode = 3; //Silent
        String notifymode = getSettingValue(tweetnotifymode);
        if(isEmpty(notifymode) == false)
        {
            try{
                default_mode = Integer.parseInt(notifymode);
            }catch(Exception e){}
        }
        return default_mode;
    }
    
    public void setTweetNotifyMode(int mode)
    {
        addSetting(tweetnotifymode, String.valueOf(mode));
    }
	public void updateTwitterToken(String token) {
		addSetting(twitter_token,token);
	}
	public void updateTwitterTokenSecret(String tokenSecret) {
		addSetting(twitter_token_secret,tokenSecret);
		
	}
	public boolean updateUserImageUrl(long uid, String url) {
		// TODO Auto-generated method stub
		return false;
	}
}
