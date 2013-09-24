package com.msocial.facebook.providers;

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

import com.msocial.facebook.service.FacebookLoginHelper;
import com.msocial.facebook.ui.TwitterHelper;
import com.msocial.facebook.ui.SyncSwitchListener.SyncSwithManager;
import com.msocial.facebook.ui.adapter.FacebookStatusItem;
import com.msocial.facebook.util.DateUtil;
import oms.sns.service.facebook.client.FacebookClient;
import oms.sns.service.facebook.client.RestPostMethod;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Notes;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.StreamFilter;
import oms.sns.service.facebook.model.UserStatus;
import oms.sns.service.facebook.model.FacebookUser.SimpleFBUser;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class SocialORM implements SocialORMInterface{
	public final  static String SNS_CONTENT_URI = "content://com.msocial.facebook.providers.SocialProvider";
	
	public static boolean twitterChanged = false;
	
	final  static String TAG      = "SocialORM";
	final  static String facebook_email="facebook_email";
	public final  static String facebook_pwd  ="facebook_pwd";
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
    	int ienable = 0;
    
    	try{
            ienable = Integer.parseInt(getSettingValue(enablesyncphonebook));
    	}catch(Exception ne){}
        return ienable==0?false:true;
    }
    public void EnableSyncPhonebook(boolean enable)
    {
    	//for sync switch monitor
    	SyncSwithManager.setEnable(enable);    	
    	
        updateSetting(enablesyncphonebook, String.valueOf(enable==true?1:0));
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
        public int    UID ;
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
    
    public boolean existPage(long pageid)
    {
    	 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page");
         String queryString ="";
         queryString = String.format(" pageid=%1$s ", pageid);
         String[] pageprojection = {"_id","pageid"};
         
         boolean ret=false;
         Cursor cursor = null;
         try
         {
 	        cursor = context.getContentResolver().query(CONTENT_URI, pageprojection, queryString, null, null);
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
 	    /*noneed*/
 	    {
 		   if(cursor != null)
 		   {
 		       cursor.close();
 		   }
 	    }
        return ret;
    }
    
    public void insertPage(List<Page> pagelist)
    {
    	if(pagelist != null && pagelist.size() > 0 )
    	{
    		for(Page page : pagelist)
    		{
    			insertPage(page);
    		}
    	}
    }
    
    public int insertPage(Page page)
    {
    	if(page!=null)
    	{
    		if(existPage(page.page_id))
    		{
    			updatePage(page);
    		}
    		else
    		{
    			addPage(page);
    		}
    		
    	}
    	return -1;
    }
    private ContentValues pageToContentValue(Page page)
    {
    	ContentValues ct = new ContentValues();
    	ct.put(PageCol.PAGEID, page.page_id);
    	if(!isEmpty(page.name)) ct.put(PageCol.NAME, page.name);
    	if(!isEmpty(page.page_url)) ct.put(PageCol.PAGE_URL,page.page_url);
    	if(!isEmpty(page.pic)) ct.put(PageCol.PIC,page.pic);
    	if(!isEmpty(page.pic_small)) ct.put(PageCol.PIC_SMALL,page.pic_small);
    	if(!isEmpty(page.pic_square)) ct.put(PageCol.PIC_SQUARE,page.pic_square);
    	if(!isEmpty(page.pic_big)) ct.put(PageCol.PIC_BIG,page.pic_big);
    	if(!isEmpty(page.pic_large)) ct.put(PageCol.PIC_LARGE,page.pic_large);
    	if(!isEmpty(page.website)) ct.put(PageCol.WEBSITE,page.website);
    	if(!isEmpty(page.company_overview)) ct.put(PageCol.COMPANY_OVERVIEW,page.company_overview);
    	if(!isEmpty(page.type)) ct.put(PageCol.TYPE,page.type);
      
   	    return ct; 
    }
    
    private void addPage(Page page)
    {
       if(page == null) return;
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page");
       ContentValues ct = pageToContentValue(page);
  	   try
  	   {
  	       context.getContentResolver().insert(CONTENT_URI, ct);
  	   }
  	   catch(SQLiteException ne)
  	   {
  		   Log.e(TAG, " addPage exception = "+ne.getMessage());
  	   }
    }
    
    private int updatePage(Page page)
    {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page");
  	   String where = " pageid = ? ";
  	   String[] params = {String.valueOf(page.page_id)};
  	   ContentValues values = pageToContentValue(page);	   
  	   int ret = -1;
  	   try{
  		  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
  	   }catch(SQLiteException e){
  		  Log.d(TAG, "updatePage Exception = "+e.getMessage());
  	   }
  	   return ret;
    }
    
    public Page getLastPage()
    {
        List<Page> pages = new ArrayList<Page>();
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page"); 
        Cursor cursor = null;
        Page page = null;
        
        try{
             cursor = context.getContentResolver().query(CONTENT_URI,PageProject, null, null, null);
             if(cursor != null)
             {    
                 while(cursor.moveToLast())
                 {
                     page = formatPage(cursor);    
                     break;
                 }          
             }
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"getPageByPid exception = "+ne.getMessage());
        }
        /*noneed*/
        {
            if(cursor != null)
            {
               cursor.close();
            }
        }
        return page;
    }
    
    public Page getPageBypid(long pid)
    {
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page"); 
    	String where = PageCol.PAGEID + " = "+pid;
        Cursor cursor = null;
   	    Page page = null;
    	try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI,PageProject, where, null, null);
	         if(cursor != null)
	         {    
	             while(cursor.moveToNext())
	             {
	                page = formatPage(cursor);     
	                break;
	             }	        
	         }
    	}
  	    catch(SQLiteException ne)
  	    {
  	    	Log.d(TAG,"getPageByPid exception = "+ne.getMessage());
  	    }
  	    /*noneed*/
  	    {
  		    if(cursor != null)
  		    {
  		       cursor.close();
  		    }
  	    }
        return page;
    }
    
    public Page formatPage(Cursor cursor)
    {
 	    Page page = new Page();
 	    page.id = cursor.getInt(cursor.getColumnIndex(PageCol.ID));
 	    page.page_id = cursor.getLong(cursor.getColumnIndex(PageCol.PAGEID));
 	    page.name = cursor.getString(cursor.getColumnIndex(PageCol.NAME));
 	    page.page_url = cursor.getString(cursor.getColumnIndex(PageCol.PAGE_URL));
 	    page.pic_big = cursor.getString(cursor.getColumnIndex(PageCol.PIC_BIG));
 	    page.pic_small = cursor.getString(cursor.getColumnIndex(PageCol.PIC_SMALL));
 	    page.pic = cursor.getString(cursor.getColumnIndex(PageCol.PIC));
 	    page.pic_large = cursor.getString(cursor.getColumnIndex(PageCol.PIC_LARGE));
 	    page.pic_square = cursor.getString(cursor.getColumnIndex(PageCol.PIC_SQUARE));
 	    page.website = cursor.getString(cursor.getColumnIndex(PageCol.WEBSITE));
 	    page.company_overview = cursor.getString(cursor.getColumnIndex(PageCol.COMPANY_OVERVIEW));
 	    page.type = cursor.getString(cursor.getColumnIndex(PageCol.TYPE));
 	    
        return page;
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
        /*noneed*/
        {
           if(cursor != null)
           {
               cursor.close();
           }
        }
        return ret;
    }
    
    public void insertNote(List<Notes> notelist)
    {
        if(notelist != null && notelist.size() > 0 )
        {
            for(Notes note : notelist)
            {
                insertNote(note);
            }
        }
    }
    
    public int insertNote(Notes note)
    {
        if(note!=null)
        {
            if(existNote(note.note_id))
            {
                updateNote(note);
            }
            else
            {
                addNote(note);
            }
            
        }
        return -1;
    }
    private ContentValues noteToContentValue(Notes note)
    {
        ContentValues ct = new ContentValues();
        ct.put(notecol.NOTE_ID, note.note_id);
        if(!isEmpty(note.title)) ct.put(notecol.TITLE, note.title);
        if(!isEmpty(note.content)) ct.put(notecol.CONTENT,note.content);
        if(note.created_time >0 ) ct.put(notecol.CREATED_TIME,note.created_time);
        if(note.updated_time >0 ) ct.put(notecol.UPDATED_TIME,note.updated_time);
        if(note.uid >0 ) ct.put(notecol.UID,note.uid);
        //if(note.id > 0 ) ct.put(notecol.ID, note.id);
        return ct; 
    }
    
    private void addNote(Notes note)
    {
       if(note == null) return;
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note");
       ContentValues ct = noteToContentValue(note);
       try
       {
           context.getContentResolver().insert(CONTENT_URI, ct);
       }
       catch(SQLiteException ne)
       {
           Log.e(TAG, " addNote exception = "+ne.getMessage());
       }
    }
    
    private int updateNote(Notes note)
    {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note");
       String where = notecol.NOTE_ID+" = ? ";
       String[] params = {String.valueOf(note.note_id)};
       ContentValues values = noteToContentValue(note);    
       int ret = -1;
       try{
          ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
       }catch(SQLiteException e){
          Log.d(TAG, "updateNote Exception = "+e.getMessage());
       }
       return ret;
    }
    
    public Notes getNoteByNid(long nid)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note"); 
        String where = notecol.NOTE_ID + " = "+nid;
        Cursor cursor = null;
        Notes note = null;
        try{
             cursor = context.getContentResolver().query(CONTENT_URI,noteprojection, where, null, null);
             if(cursor != null)
             {    
                 while(cursor.moveToNext())
                 {
                    note = formatNote(cursor);     
                    break;
                 }          
             }
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"getNoteByNid exception = "+ne.getMessage());
        }
        /*noneed*/
        {
            if(cursor != null)
            {
               cursor.close();
            }
        }
        return note;
    }
    
    public int deleteAllNotes()
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/note");
        return context.getContentResolver().delete(CONTENT_URI, null, null);
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
    
    
    public Notes formatNote(Cursor cursor)
    {
        Notes note = new Notes();
        note.id = cursor.getLong(cursor.getColumnIndex(notecol.ID));
        note.note_id = cursor.getLong(cursor.getColumnIndex(notecol.NOTE_ID));
        note.title = cursor.getString(cursor.getColumnIndex(notecol.TITLE));
        note.content = cursor.getString(cursor.getColumnIndex(notecol.CONTENT));
        note.updated_time = cursor.getLong(cursor.getColumnIndex(notecol.UPDATED_TIME));
        note.created_time = cursor.getLong(cursor.getColumnIndex(notecol.CREATED_TIME));
        note.uid = cursor.getLong(cursor.getColumnIndex(notecol.UID));
        return note;
    }
    
    
    public boolean existTwitterUser(int uid, boolean isFollower)
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
	    /*noneed*/
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
	    /*noneed*/
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
	    /*noneed*/
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
	    /*noneed*/
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
	    /*noneed*/
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
 	     /*noneed*/
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
    
    
    public boolean updateTwitterUser(int uid, Follow user)
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
            nCount = nCount>20?20:nCount;
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
    public boolean setFacebookUseHttps(boolean value)
    {
        FacebookClient.Server.retSetURL(value);
    	is_facebook_use_https_connection = value; 
        Uri uri = this.addSetting(facebook_use_https_connection, value?"1":"0");
        if(uri!=null)
            return true;
        else
            return false;
    }
    
    
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
	     /*noneed*/
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
   
    
    
    /*************04-22*************/
    public FacebookUser getFacebookUser(long uid){
         String where = " uid = ? ";
         String[] paras = {String.valueOf(uid)};
         List ls = getFacebookUserByid(where, paras);
         if(ls !=null && ls.size()>0 ) {
             return  (FacebookUser)ls.get(0);
         }else{
        	 return null;
         }
       
    }
    
    @SuppressWarnings("unchecked")
    public FacebookUser.SimpleFBUser getSimpleFacebookUser(long uid){
    	if(uid == -1)
    		return null;
    	
        String where = " uid = ? ";
        String[] paras = {String.valueOf(uid)};
        List ls = getFacebookSimpleUserByid(where, paras);
        if(ls !=null && ls.size()>0 ) {
            return  (FacebookUser.SimpleFBUser)ls.get(0);
        }else{
       	 return null;
        }
      
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
	  	    /*noneed*/
	  	    {
	  		    if(cursor != null)
	  		    {
	  		       cursor.close();
	  		    }
	  	    }
        }        
        return ret;         
    }
    
    public List<FacebookUser> getFacebookUsers(long[] uid){
    	 
         String where =  null;
         String[] paras = null;
         
         if(uid!=null && uid.length>0)
         {
        	where = "";
        	paras = new String[uid.length];
        	for(int i = 0 ; i< uid.length ; i++)
        	{
        		if(i>0)
        			where += ",";
        		
        		 where    += uid[i] ;
        		 paras[i] = String.valueOf(uid[i]);
            }
           
            where = " uid in (" + where + ")";
            return getFacebookUserByid(where, null);
         }
         else
         {
             return null;
         }
         
    }

    public List<FacebookUser> getFacebookUserByid(String where,String[] paras){
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	Cursor cursor = null;
   	    ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
    	try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where, paras, null);
	         if(cursor != null)
	         {    
	             while(cursor.moveToNext())
	             {
	                 FacebookUser fuser = formatFacebookUser(cursor);	                
	                 ls.add(fuser);
	             }	        
	         }
    	}
  	    catch(SQLiteException ne)
  	    {}
  	    /*noneed*/
  	    {
  		    if(cursor != null)
  		    {
  		       cursor.close();
  		    }
  	    }
        return ls;
    }
    
    public List<FacebookUser.SimpleFBUser> getFacebookSimpleUserByid(String where,String[] paras){
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	Cursor cursor = null;
   	    ArrayList<FacebookUser.SimpleFBUser> ls = new ArrayList<FacebookUser.SimpleFBUser>();
    	try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where, paras, null);
	         if(cursor != null)
	         {    
	             while(cursor.moveToNext())
	             {
	                 FacebookUser.SimpleFBUser fuser = formatSimpleFacebookUser(cursor);	                
	                 ls.add(fuser);
	             }	        
	         }
    	}
  	    catch(SQLiteException ne)
  	    {}
  	    /*noneed*/
  	    {
  		    if(cursor != null)
  		    {
  		       cursor.close();
  		    }
  	    }
        return ls;
    }
    
    public FacebookUser.SimpleFBUser getLastSimpleFacebookUser() 
    {       
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
         Cursor cursor      = null;
         FacebookUser.SimpleFBUser fuser = null;
         try
         {
             cursor = context.getContentResolver().query(CONTENT_URI, FacebookSimpleUsersProject, null, null, null);       
             if(cursor != null)
             {    
                 
                 while(cursor.moveToLast())
                 {
                     fuser = this.formatSimpleFacebookUser(cursor);
                     break;
                 }         
             }
        }
        catch(SQLiteException ne)
        {}
        /*noneed*/
        {
            if(cursor != null)
            {
               cursor.close();
            }
        }
        return fuser;
    }   
    
    public FacebookUser getLastFacebookUser() 
    {		
    	 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
    	 Cursor cursor      = null;
    	 FacebookUser fuser = null;
    	 try
    	 {
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, null, null, null);    	 
	         if(cursor != null)
	         {    
	        	 
	             while(cursor.moveToLast())
	             {
	                 fuser = formatFacebookUser(cursor);
	                 break;
	             }         
	         }
    	}
	    catch(SQLiteException ne)
	    {}
	    /*noneed*/
	    {
		    if(cursor != null)
		    {
		       cursor.close();
		    }
	    }
		return fuser;
	} 	
    
    public String getFacebookImage(long uid){
    	String where = " uid = ?";
    	String[] paras = {String.valueOf(uid)};
    	List list = getFacebookImagesByuid(where,paras);
    	if(list!=null && list.size()>0)
    		return (String)list.get(0);
    	else
    		return null;
    }
    
    public List<String> getFacebookImages(long[] uid){
    	
    	String where =  null;
        String[] paras = null;
         
        if(uid!=null && uid.length>0){      	 
        	 paras = new String[uid.length];
        	 for(int i = 0 ; i< uid.length ; i++){
        		 where = "?,";
        		 paras[i] = String.valueOf(uid[i]);
        	 }
        	 if(where.endsWith(",")) where = where.substring(0,where.length()-1);
             where = " uid in ("+ where + ")";
         }
        
        return getFacebookImagesByuid(where,paras);
        
    }
    
    public List<String> getFacebookImagesByuid(String where,String[] paras){
    	 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	 String[] FacebookImageProject = {FacebookUsersCol.PICSQUARE};
    	 Cursor cursor = null;
    	 ArrayList<String> ls = new ArrayList<String>();
    	 try
    	 {
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookImageProject, where, paras, null);	    	 
	         if(cursor != null)
	         {       	 
	             while(cursor.moveToNext())
	             { 
	                 String img = cursor.getString(cursor.getColumnIndexOrThrow(FacebookUsersCol.PICSQUARE));
	                 if(img != null)
	                     ls.add(img);
	             }	         
	         }
    	 }
 	     catch(SQLiteException ne)
 	     {}
 	     /*noneed*/
 	     {
 		    if(cursor != null)
 		    {
 		       cursor.close();
 		    }
 	     }
         return ls;
    }
    public boolean isExistedFacebookUser(long uid){
    	 String where = " uid = ? ";
         String[] paras = {String.valueOf(uid)};
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
         String[] project = new String[]{
        	    	"_id",
        	    	"uid",
         };
         
         Cursor cursor = null;
         boolean ret = false;
         try{
	         cursor = context.getContentResolver().query(CONTENT_URI, project, where, paras, null);	         
	         if(cursor !=null )
	         {
	        	 ret = cursor.getCount()>0 ? true : false;        	
	         }
         }
 	     catch(SQLiteException ne)
 	     {
 	    	ret = false; 
 	     }
 	     /*noneed*/
 	     {
 		    if(cursor != null)
 		    {
 		       cursor.close();
 		    }
 	     }
         return ret;
    }  
  
    private Uri insertFacebookUser(FacebookUser user){
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        ContentValues cv = facebookUserToContentValues(user);
         Uri uri = null;
        try{
        	uri = context.getContentResolver().insert(CONTENT_URI, cv);
        }catch(SQLiteException e){
        	Log.d(TAG, " insert FacebookUser exception "+e.getMessage());
        }
    	return uri;
    }
    
    public boolean updateFacebookUserShortCut(FacebookUser user, boolean shortcut){
        int ret = -1;
        if(user != null)
        {
        	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        	ContentValues cv = new ContentValues();    	
        	cv.put(FacebookUsersCol.UID, user.getUid());
        	cv.put(FacebookUsersCol.IsShoutCut, shortcut==true?1:0);
        	
        	String where = " uid = ? ";
        	String[] paras = {user.getUid().toString()};        
        	try{
        	  ret = context.getContentResolver().update(CONTENT_URI, cv, where, paras);
        	}catch(SQLiteException ne){
        		Log.d(TAG, "update FacebookUser exception "+ne.getMessage());
        	}
        }
    	
        return ret > 0 ? true : false;
    }
    
    public int deleteFacebookShortCut()
    {
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	ContentValues cv = new ContentValues();
    	cv.put(FacebookUsersCol.IsShoutCut, 0);
    	int ret = -1;
    	try
    	{
    	    ret = context.getContentResolver().update(CONTENT_URI, cv, null, null);
    	}
    	catch(SQLiteException ne)
    	{
    		Log.d(TAG, "remove all short cut exception "+ne.getMessage());
    	}    	
        return ret ;
    }
    
    public boolean updateFacebookUserShortCut(FacebookUser.SimpleFBUser user, boolean shortcut){
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	ContentValues cv = new ContentValues();    	
    	cv.put(FacebookUsersCol.UID, user.uid);
    	cv.put(FacebookUsersCol.IsShoutCut, shortcut==true?1:0);
    	
    	String where = " uid = ? ";
    	String[] paras = {String.valueOf(user.uid)};
    	int ret = -1;
    	try{
    	  ret = context.getContentResolver().update(CONTENT_URI, cv, where, paras);
    	}catch(SQLiteException ne){
    		Log.d(TAG, "update FacebookUser exception "+ne.getMessage());
    	}
    	
        return ret > 0 ? true : false;
    }
    
    public boolean updateFacebookUser(FacebookUser user){
    	Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	ContentValues cv = facebookUserToContentValues(user);
    	String where = " uid = ? ";
    	String[] paras = {user.getUid().toString()};
    	int ret = -1;
    	try{
    	  ret = context.getContentResolver().update(CONTENT_URI, cv, where, paras);
    	}catch(SQLiteException ne){
    		Log.d(TAG, "update FacebookUser exception "+ne.getMessage());
    	}
    	
        return ret > 0 ? true : false;
    }
    
    public boolean saveOrupdateFacebookUser(FacebookUser user){
    	if(isExistedFacebookUser(user.getUid().longValue())){
    		 updateFacebookUser(user);
    	}else{
    		 insertFacebookUser(user);
    	}
    	return true;
    }
    
    public ContentValues facebookUserToContentValues(FacebookUser user){
    	ContentValues cv = new ContentValues();
    	if(user.getBirthday() != null){
    	    cv.put(FacebookUsersCol.BIRTHDAY, user.getBirthday());
    	    cv.put(FacebookUsersCol.B_MONTH, user.b_month);
    	    cv.put(FacebookUsersCol.B_DATE, user.b_date);
    	    if(getFacebookUser(user.getUid())!=null && !user.getBirthday().equals(getFacebookUser(user.getUid()).birthday)){
    	    	cv.put(FacebookUsersCol.EVENT_SYNC, false);
    	    }
    	}
    	
    	if(user.getFirst_name() != null)
    	    cv.put(FacebookUsersCol.FIRSTNAME, user.getFirst_name());
    	
    	if(user.getLast_name() != null)
    	    cv.put(FacebookUsersCol.LASTNAME, user.getLast_name());
    	
    	if(user.getName() != null)
    	    cv.put(FacebookUsersCol.NAME, user.getName());
    	
    	if(user.getPic_square() != null)
    	    cv.put(FacebookUsersCol.PICSQUARE, user.getPic_square());
    	
    	if(user.pic != null)
            cv.put(FacebookUsersCol.PIC, user.pic);
    	
    	if(user.pic_small != null)
            cv.put(FacebookUsersCol.PIC_SMALL, user.pic_small);
    	
    	if(user.getSex() != null)
    	    cv.put(FacebookUsersCol.SEX, user.getSex());
    	
    	if(user.getUid() >0)
    	    cv.put(FacebookUsersCol.UID, user.getUid());
    	
    	if(user.getMessage()!=null){
    		cv.put(FacebookUsersCol.MESSAGE, user.getMessage());
    	}
    	
    	if(user.getStatusid()!=null){
    		cv.put(FacebookUsersCol.STATUSID,user.getStatusid());
    	}
    	
    	if(user.getStatustime()!=null){
    		cv.put(FacebookUsersCol.STATUSTIME, user.getStatustime());
    	}    	
    	//cv.put(FacebookUsersCol.IsShoutCut, user.isShoutcut==true?1:0);
    	
    	return cv;
    }
    
    public boolean addFacebookUser(FacebookUser user){
    	
    	return saveOrupdateFacebookUser(user);    	
    }
    
    private void saveOrUpdateSimpleFBUser(SimpleFBUser simpleFBUser) {
       
        if(isExistedFacebookUser(simpleFBUser.uid))
        {
            updateFacebookSimpleUser(simpleFBUser);
        }
        else
        {
            insertFacebookSimpleUser(simpleFBUser);
        }
        
    }
    
    private void  insertFacebookSimpleUser(SimpleFBUser user) {
       
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        ContentValues cv = new ContentValues();     
        cv.put(FacebookUsersCol.UID, user.uid);
        cv.put(FacebookUsersCol.IsShoutCut,0);
        cv.put(FacebookUsersCol.NAME, user.name);
        cv.put(FacebookUsersCol.PICSQUARE, user.pic_square);
        cv.put(FacebookUsersCol.BIRTHDAY, user.birthday);
        Uri uri;
        try{
         uri = context.getContentResolver().insert(CONTENT_URI, cv);
        }catch(SQLiteException ne){
            Log.d(TAG, "update FacebookUser exception "+ne.getMessage());
        } 
    }
    
    private boolean updateFacebookSimpleUser(SimpleFBUser user) {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        ContentValues cv = new ContentValues();     
        cv.put(FacebookUsersCol.UID, user.uid);
        cv.put(FacebookUsersCol.IsShoutCut,0);
        cv.put(FacebookUsersCol.NAME, user.name);
        cv.put(FacebookUsersCol.PICSQUARE, user.pic_square);
        cv.put(FacebookUsersCol.BIRTHDAY, user.birthday);
        
        String where = " uid = ? ";
        String[] paras = {String.valueOf(user.uid)};
        int ret = -1;
        try{
          ret = context.getContentResolver().update(CONTENT_URI, cv, where, paras);
        }catch(SQLiteException ne){
            Log.d(TAG, "update FacebookUser exception "+ne.getMessage());
        }
        
        return ret > 0 ? true : false;
    }
    
    public boolean addFacebookSimpleUser(List<FacebookUser.SimpleFBUser> users){
        
        if(users==null || users.size() ==0) return false;
        for(int i=0;i<users.size();i++)
        {
            saveOrUpdateSimpleFBUser(users.get(i));
        }
        return true;
    }
    
    public boolean addFacebookUser(List<FacebookUser> users) 
    {
 	   if(users == null || users.size() == 0) return false ; 	   
 	   
 	   for(int i=0;i<users.size();i++)
 	   {
 		   saveOrupdateFacebookUser(users.get(i));
 	   }
 	   return true;
    }
    
    public List<Long> getFriendIDs()
    {
    	long uid = getLoginUserID();
    	return getFriendIDs(uid);
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
        /*noneed*/
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
   
   public boolean isFriends(long uid2)
   {
	   //low performance
	   long uid = this.getLoginUserID();
       return isExistFriends(uid, uid2);       
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
       /*noneed*/
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
   
   public boolean updateUserImageUrl(long uid, String url) {
	   boolean ret = false;
	   if(isExistedFacebookUser(uid)){
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 	
			  ContentValues cv = new ContentValues();
			  cv.put(FacebookUsersCol.PICSQUARE, url);	
			  String where = " uid = ? ";
			  String[] paras = {String.valueOf(uid)};	
			  if(context.getContentResolver().update(CONTENT_URI, cv, where, paras) > 0){
		         ret = true;
		      }        
	   }else{
		   FacebookUser user = new FacebookUser();
		   user.setUid(Long.valueOf(uid));
		   user.setPic_square(url);
		   addFacebookUser(user);
		   ret = true;
	   }
	  	 
       return ret;
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
       /*noneed*/
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
	   /*noneed*/
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
	   /*noneed*/
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
	   
  	   Cursor cursor = null;
  	   List<PeopleMapFacebook> al = new ArrayList<PeopleMapFacebook>();
  	   try{
	  	   cursor = context.getContentResolver().query(CONTENT_URI, peoplemapfacebooProject, where, params, null);	  	   
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
	   /*noneed*/
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
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
	   /*noneed*/
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return al;
   }
   //end for facebook and people map
   
   public int addPhonebook(List<PhoneBook> phonebooks)
   {
	   int count=0;
	   for(int i=0;i<phonebooks.size();i++)
	   {
		   PhoneBook item = phonebooks.get(i);
	       if(isPhoneBookExist(item.uid))
	       {
	    	   updatePhonebook(item);
	       }
	       else
	       {
	    	   insertPhonebook(item);
	       }
	   }
	   
	   return count;
   }
   
   public int addPhonebook(PhoneBook phonebook)
   {
	   int count=0;	   
	
       if(isPhoneBookExist(phonebook.uid))
       {
    	   count = updatePhonebook(phonebook);
       }
       else
       {
    	   insertPhonebook(phonebook);
       }
	   return count;
   }
   
   private static boolean isEmpty(String str)
   {
	   return str==null || str.length() == 0;
   }
   private void insertPhonebook(PhoneBook phonebook)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
       ContentValues ct = new ContentValues();
     
       ct.put(PhonebookCol.Name,  phonebook.getUsername());
       if(isEmpty(phonebook.getEmail()) == false)
       {
	       ct.put(PhonebookCol.EMAIL, phonebook.getEmail());
       }
	   if(isEmpty(phonebook.getCell()) == false)
	   {
	       ct.put(PhonebookCol.CELL,  phonebook.getCell());
	   }
	   if(isEmpty(phonebook.getPhone()) ==false )
	   {
	       ct.put(PhonebookCol.Phone, phonebook.getPhone());
	   }
	   ct.put(PhonebookCol.UID,   phonebook.getUid());
	   if(isEmpty(phonebook.getPhone()) == false)
	   {
	       ct.put(PhonebookCol.Phone,  phonebook.getPhone());
	   }
	   
	   if(isEmpty(phonebook.screenname) == false)
	       ct.put(PhonebookCol.Screenname,phonebook.screenname);
	   if(isEmpty(phonebook.address) == false)
	       ct.put(PhonebookCol.Address,phonebook.address);
	   if(isEmpty(phonebook.city) == false)
		   ct.put(PhonebookCol.City,phonebook.city);
	   if(isEmpty(phonebook.street) == false)
		   ct.put(PhonebookCol.Street,phonebook.street);
	   if(isEmpty(phonebook.state) == false)
		   ct.put(PhonebookCol.State,phonebook.state);
	   if(isEmpty(phonebook.country) == false)
		   ct.put(PhonebookCol.Country,phonebook.country);
	   if(isEmpty(phonebook.zip) == false)
		   ct.put(PhonebookCol.Zip,phonebook.zip);
	   if(isEmpty(phonebook.latitude) == false)
		   ct.put(PhonebookCol.Latitude,phonebook.latitude);
	   if(isEmpty(phonebook.longitude) == false)
		   ct.put(PhonebookCol.Longitude,phonebook.longitude);
	   
	   if(phonebook.peopleid > 0)
	       ct.put(PhonebookCol.PeopleId, phonebook.peopleid);

	   //ct.put(PhonebookCol.Synced, phonebook.synced);
	   try
	   {
	       context.getContentResolver().insert(CONTENT_URI, ct);
	   }
	   catch(SQLiteException ne)
	   {
		   Log.e(TAG, "eeeeeeeeeeeeeeeeeee error="+ne.getMessage());
	   }
   }  
   
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
	   /*noneed*/
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
   
   public int updatePhonebook(PhoneBook con)
   {	   
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(con.uid)};
	   ContentValues values = new ContentValues();
	   values.put(PhonebookCol.CELL, con.cell);
	   values.put(PhonebookCol.Name, con.username);
	   values.put(PhonebookCol.Phone, con.phone);
	   values.put(PhonebookCol.EMAIL, con.email);
	   if(con.screenname!=null && con.screenname.length()>0)
	       values.put(PhonebookCol.Screenname,con.screenname);
	   if(con.address!=null && con.address.length()>0)
	       values.put(PhonebookCol.Address,con.address);
	   if(con.city!=null && con.city.length()>0)
	       values.put(PhonebookCol.City,con.city);
	   if(con.street!=null && con.street.length()>0)
	       values.put(PhonebookCol.Street,con.street);
	   if(con.state!=null && con.state.length()>0)
	       values.put(PhonebookCol.State,con.state);
	   if(con.country!=null && con.country.length()>0)
	       values.put(PhonebookCol.Country,con.country);
	   if(con.zip!=null && con.zip.length()>0)
	       values.put(PhonebookCol.Zip,con.zip);
	   if(con.latitude!=null && con.latitude.length()>0)
	       values.put(PhonebookCol.Latitude,con.latitude);
	   if(con.longitude!=null && con.longitude.length()>0)
	       values.put(PhonebookCol.Longitude,con.longitude);	   
	   //values.put(PhonebookCol.Synced, con.synced);
	   
	   if(con.peopleid > 0)
	       values.put(PhonebookCol.PeopleId, con.peopleid);
	   
	   int ret = -1;
	   try{
		  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
	   }catch(SQLiteException e){
		  Log.d(TAG, "updatePhonebook Exception "+e.getMessage());
	   }
	   return ret;
   }
   
   
   public PhoneBook getPhonebook(long uid)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook"); 
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   
	   Cursor cursor      = null;
	   PhoneBook phonebook=null;
	   try{
		   cursor = context.getContentResolver().query(CONTENT_URI, PhonebookProject, where, params, null);	  	   
	       if(cursor != null && cursor.moveToFirst())
	       {    
	        	phonebook = formatPhoneBook(cursor);	                      
	       }
	   }
	   catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       
       return phonebook;
   }
   
   public int  getPhonebookPeopleID(long uid)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook"); 
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   
	   Cursor cursor   = null;
	   int PeopleID    = 0;
	   try{
		   String[] PBPProject = 
		   {
		    	"_id",
		    	"uid",		   
		    	"peopleid"
		   };
		   cursor = context.getContentResolver().query(CONTENT_URI, PBPProject, where, params, null);	  	   
	       if(cursor != null && cursor.moveToFirst())
	       {    
	    	   PeopleID = cursor.getInt(cursor.getColumnIndex(PhonebookCol.PeopleId));	                      
	       }
	   }
	   catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       
       return PeopleID;
   }
   
   public PhoneBook formatPhoneBook(Cursor cursor)
   {
	   PhoneBook phonebook = new PhoneBook();
       phonebook.uid = cursor.getLong(cursor.getColumnIndex(PhonebookCol.UID));
       phonebook.cell = cursor.getString(cursor.getColumnIndex(PhonebookCol.CELL));
       phonebook.username = cursor.getString(cursor.getColumnIndex(PhonebookCol.Name));
       phonebook.phone = cursor.getString(cursor.getColumnIndex(PhonebookCol.Phone));
       phonebook.email = cursor.getString(cursor.getColumnIndex(PhonebookCol.EMAIL));
       phonebook.synced = cursor.getInt(cursor.getColumnIndex(PhonebookCol.Synced))==0?false:true;
       phonebook.screenname = cursor.getString(cursor.getColumnIndex(PhonebookCol.Screenname));
       phonebook.address = cursor.getString(cursor.getColumnIndex(PhonebookCol.Screenname));
       phonebook.city = cursor.getString(cursor.getColumnIndex(PhonebookCol.City));
       phonebook.state = cursor.getString(cursor.getColumnIndex(PhonebookCol.State));
       phonebook.street = cursor.getString(cursor.getColumnIndex(PhonebookCol.Street));
       phonebook.country = cursor.getString(cursor.getColumnIndex(PhonebookCol.Country));
       phonebook.zip = cursor.getString(cursor.getColumnIndex(PhonebookCol.Zip));
       phonebook.latitude = cursor.getString(cursor.getColumnIndex(PhonebookCol.Latitude));
       phonebook.longitude = cursor.getString(cursor.getColumnIndex(PhonebookCol.Longitude));
       phonebook.peopleid  = cursor.getInt(cursor.getColumnIndex(PhonebookCol.PeopleId));
       
       return phonebook;
   }
   
   public Cursor searchPhonebooksCursor(String key)
   {
       
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
       Cursor cursor = null;
       List<PhoneBook> al = new ArrayList<PhoneBook>();
       String whereclause =  PhonebookCol.Name+" like '%"+key+"%' or "+PhonebookCol.CELL+" like '%"+key+"%' or "
                            + PhonebookCol.Phone+" like '%"+key+"%' or "+PhonebookCol.EMAIL+" like '%"+key+"%' ";
         
       try
       {
           cursor = context.getContentResolver().query(CONTENT_URI, PhonebookProject, whereclause, null, null);
         
       }
       catch(SQLiteException ne)
       {
           Log.d(TAG, " query exception "+ne.getMessage());
       }
       return cursor;
   }
   
   public Cursor getPhonebooksCursor()
   {
       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");           
       Cursor cursor = null;      
       try
       {
           cursor = context.getContentResolver().query(CONTENT_URI, PhonebookProject, null, null, " name ASC "); 
       }
       catch(SQLiteException ne)
       {}     
       return cursor;
   }
   public List<PhoneBook> getPhonebooks()
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook"); 	 	   
  	   Cursor cursor = null;
  	   List<PhoneBook> al = new ArrayList<PhoneBook>();
  	   try
  	   {
	  	   cursor = context.getContentResolver().query(CONTENT_URI, PhonebookProject, null, null, null);	  	   
	       if(cursor != null)
	       {       	 
	           while(cursor.moveToNext())
	           { 
	        	   PhoneBook phonebook = formatPhoneBook(cursor);
	               al.add(phonebook);
	           }           
	       }
	   }
	   catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
		   if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return al;
   }
   public int  setPhonebookPeopleid(long uid, int peopleid)
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/phonebook");
	   String where = " uid = ? ";
	   String[] params = {String.valueOf(uid)};
	   ContentValues values = new ContentValues();
	   values.put(PhonebookCol.PeopleId, peopleid);
	   
	   int ret = -1;
	   try{
		  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
	   }catch(SQLiteException e){
		  Log.d(TAG, "setPhonebookPeopleid Exception "+e.getMessage());
	   }
	   return ret;
   }
    //remove session
    public void logout() 
	{
		this.removeSetting(FacebookLoginHelper.DB_LOGGED_USER_ID);
		this.removeSetting(FacebookLoginHelper.DB_PERM_SECRET_KEY);
		this.removeSetting(FacebookLoginHelper.DB_PERM_SESSION_KEY);
		this.removeSetting(FacebookLoginHelper.DB_SECRET_KEY);
		this.removeSetting(FacebookLoginHelper.DB_SESSION_KEY);
	}
    
    public Cursor getFacebookUsersOrderByBirthdayCursor(long uid)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        String selection = FacebookUsersCol.UID +" IN (select "+ FacebookFriendsCol.UID2+" from facebookfriends where "+ FacebookFriendsCol.UID1 +"= "+uid+") AND "+
                            FacebookUsersCol.BIRTHDAY + "!=''";
        String orderSelection = FacebookUsersCol.B_MONTH+ " ASC,"+FacebookUsersCol.B_DATE+" ASC";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, selection, null, orderSelection);
        }
        catch(Exception ne)
        {
            Log.d(TAG,"getFacebookUsersOrderByBirthdayCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
    public Cursor getAllFacebookSimpleUsersCursor(long uid)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        String selection = FacebookUsersCol.UID +" IN (select "+ FacebookFriendsCol.UID2+" from facebookfriends where "+ FacebookFriendsCol.UID1 +"= "+uid+") ";
        String orderSelection = FacebookUsersCol.NAME + " ASC";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookSimpleUsersProject, selection, null, orderSelection);
        }
        catch(Exception ne)
        {
            Log.d(TAG,"getAllFacebookUsersCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
    /*
    public Cursor getAllFacebookSimpleUsersCursor()
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        String selection = FacebookUsersCol.UID +" IN (select "+ FacebookFriendsCol.UID+" from facebookfriends) ";
        String orderSelection = FacebookUsersCol.NAME + " ASC";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookSimpleUsersProject, selection, null, orderSelection);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"getAllFacebookUsersCursor exception "+ne.getMessage());
        }
        return cursor;
    }*/
    
    public Cursor getAllPageCursor()
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page");
        String orderSelection = PageCol.NAME + " ASC";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, PageProject, null, null, orderSelection);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"getAllPageCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
    private long getLoginUserID()
    {
    	long uid = -1;
    	try
    	{
	    	if(FacebookLoginHelper.instance(context).getPermanentSesstion() != null)
	    	{
	    		//this is more fast to get the long UID
	    		uid = FacebookLoginHelper.instance(context).getPermanentSesstion().getLogerInUserID();
	    	}	    	
    	}catch(NumberFormatException ne){}
    	return uid;
    }
    
    public Cursor getAllFacebookUsersCursor()
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");         
    	long uid = getLoginUserID();
        String selection = FacebookUsersCol.UID +" IN (select "+ FacebookFriendsCol.UID2+" from facebookfriends where "+ FacebookFriendsCol.UID1 +"= "+uid+") ";
        
        //String selection = FacebookUsersCol.UID +" IN (select "+ FacebookFriendsCol.UID+" from facebookfriends) ";
        String orderSelection = FacebookUsersCol.FIRSTNAME + " ASC";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, selection, null, orderSelection);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"getAllFacebookUsersCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
    public FacebookUser.SimpleFBUser formatSimpleFacebookUser(Cursor cursor)
    {
        FacebookUser.SimpleFBUser fuser = new FacebookUser.SimpleFBUser();
        fuser.uid = cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.UID));        
        fuser.name = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.NAME));        
        fuser.pic_square = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.PICSQUARE));       
        fuser.setBirthday(cursor.getString(cursor.getColumnIndex(FacebookUsersCol.BIRTHDAY)));
        fuser.isShoutcut= cursor.getString(cursor.getColumnIndex(FacebookUsersCol.IsShoutCut)).equals("1")?true:false;
        
        return fuser;
    }
    
    public FacebookUser formatFacebookUser(Cursor cursor)
    {
        FacebookUser fuser = new FacebookUser();
        fuser.uid = cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.UID));
        fuser.first_name = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.FIRSTNAME));
        fuser.last_name = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.LASTNAME));
        fuser.name = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.NAME));
        
        fuser.pic_square = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.PICSQUARE));        
        fuser.pic_small = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.PIC_SMALL));
        fuser.pic = cursor.getString(cursor.getColumnIndex(FacebookUsersCol.PIC));
        
        fuser.sex= cursor.getString(cursor.getColumnIndex(FacebookUsersCol.SEX));
        fuser.isShoutcut= cursor.getString(cursor.getColumnIndex(FacebookUsersCol.IsShoutCut)).equals("1")?true:false;
        fuser.setBirthday(cursor.getString(cursor.getColumnIndex(FacebookUsersCol.BIRTHDAY)));
        fuser.event_last_sync = cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.EVENT_LAST_SYNCTIME));
        fuser.event_sync = cursor.getInt(cursor.getColumnIndex(FacebookUsersCol.EVENT_SYNC));
        fuser.ceid = cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.C_EVENT_ID));
        fuser.setStatusid(cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.STATUSID)));
        fuser.setMessage(cursor.getString(cursor.getColumnIndex(FacebookUsersCol.MESSAGE)));
        fuser.setStatustime(cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.STATUSTIME)));
        return fuser;
    }
    
    public Cursor getFacebookUserCursorByUids(String uids)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        String whereclause = FacebookUsersCol.UID+" in ("+uids+")";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, whereclause, null, null);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"searchFacebookBDCursor exception "+ne.getMessage());
        }
        return cursor; 
    }
    
    public Cursor searchFacebookBDCursor(long uid, String key)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        String whereclause = "("+FacebookUsersCol.NAME + " like '%"+key+"%'  or "+FacebookUsersCol.BIRTHDAY +" like '"+key+"%')"+
                              " and "+FacebookUsersCol.UID + 
                              " IN (select "+ FacebookFriendsCol.UID2+" from facebookfriends where "+ FacebookFriendsCol.UID1 +"= "+uid+") ";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, whereclause, null, null);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"searchFacebookBDCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
    public Cursor searchFacebookUserCursor(long uid, String key)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
        String whereclause = FacebookUsersCol.NAME + " like '%"+key+"%'  and "+FacebookUsersCol.UID + 
                              " IN (select "+ FacebookFriendsCol.UID2+" from facebookfriends where "+ FacebookFriendsCol.UID1 +"= "+uid+") ";
        
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, whereclause, null, null);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"searchFacebookUserCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
    public Cursor searchPageCursor(String key)
    {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/page"); 
        String whereclause = PageCol.NAME + " like '%"+key+"%' ";
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CONTENT_URI, PageProject, whereclause, null, null);
        }
        catch(SQLiteException ne)
        {
            Log.d(TAG,"searchPageCursor exception "+ne.getMessage());
        }
        return cursor;
    }
    
	public List<FacebookUser> getAllFacebookUsers() 
	{		
		 ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
		 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	 Cursor cursor = null;
    	 try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, null, null, null);
	         if(cursor != null)
	         { 
	             while(cursor.moveToNext())
	             {
	                 FacebookUser fuser =  formatFacebookUser(cursor);
	                 ls.add(fuser);
	             }             
	         }    
    	 }
	  	 catch(SQLiteException ne)
	  	 {}
	  	 /*noneed*/
	  	 {
	  	     if(cursor != null)
	  		 {
	  		     cursor.close();
	  		 }
	  	 }
         return ls;        
	}
	
	public List<FacebookUser.SimpleFBUser> getAllFacebookSimpleUsers() 
	{		
		 ArrayList<FacebookUser.SimpleFBUser> ls = new ArrayList<FacebookUser.SimpleFBUser>();
		 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	 Cursor cursor = null;
    	 try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, null, null, null);
	         if(cursor != null)
	         { 
	             while(cursor.moveToNext())
	             {
	                 FacebookUser.SimpleFBUser fuser =  formatSimpleFacebookUser(cursor);
	                 ls.add(fuser);
	             }             
	         }    
    	 }
	  	 catch(SQLiteException ne)
	  	 {}
	  	 /*noneed*/
	  	 {
	  	     if(cursor != null)
	  		 {
	  		     cursor.close();
	  		 }
	  	 }
         return ls;        
	}
	
	public Cursor getAllShoutCutFacebookUsersCursor() 
	{		
		 ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
		 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
		 String sort = FacebookUsersCol.NAME + " ASC";
    	 Cursor cursor = null;
    	 try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, " isshotcut = 1 ", null, sort);	           
    	 }
	  	 catch(SQLiteException ne)
	  	 {}	  	
	  	 catch(Exception ne){}
         return cursor;        
	}
	
	public List<FacebookUser> getAllShoutCutFacebookUsers() 
	{		
		 ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
		 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 
    	 Cursor cursor = null;
    	 try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, " isshotcut = 1 ", null, null);
	         if(cursor != null)
	         { 
	             while(cursor.moveToNext())
	             {
	                 FacebookUser fuser =  formatFacebookUser(cursor);
	                 ls.add(fuser);
	             }             
	         }    
    	 }
	  	 catch(SQLiteException ne)
	  	 {}
	  	 /*noneed*/
	  	 {
	  	     if(cursor != null)
	  		 {
	  		     cursor.close();
	  		 }
	  	 }
         return ls;        
	}
	
	
	public List<UserStatus> getFriendsRecentStatus() 
	{
		long uid = getLoginUserID();
		List<Long> uids = getFriendIDs(uid);
        List<UserStatus> userstatuslist = new ArrayList<UserStatus>();
        if(uids!=null && uids.size()>0)
        {
        	for(int i=0;i<uids.size();i++)
        	{
        		FacebookUser fu = getFacebookUser(Long.valueOf(uids.get(i)));
        		UserStatus userstatus = new UserStatus();        		
        		if(fu.getMessage()!=null && !fu.getMessage().equals(""))
        		{
        			userstatus.setMessage(fu.getMessage());
        			userstatus.setStatusid(fu.getStatusid());
        			userstatus.setTime(new Date(fu.getStatustime()));
        			userstatus.setUid(fu.getUid());
        			userstatus.setUsername(fu.getName());
        		}
        		userstatuslist.add(userstatus);
        	}
        }
		return userstatuslist;
	}
	
	public void addFriendStatus(List<UserStatus> statuses) 
	{
		for(UserStatus us :  statuses)
		{
		   long uid = us.getUid().longValue();
		   boolean ret = false;
		   if(isExistedFacebookUser(uid))
		   {
		       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers"); 	
			   ContentValues cv = new ContentValues();
			   if(us.getMessage()!=null)  cv.put(FacebookUsersCol.MESSAGE, us.getMessage());	
			   if(us.getStatusid()!=null) cv.put(FacebookUsersCol.STATUSID, us.getStatusid());
			   if(us.getTime()!=null)     cv.put(FacebookUsersCol.STATUSTIME, us.getTime().getTime()) ;
			   String where = " uid = ? ";
			   String[] paras = {String.valueOf(uid)};	
			   if(context.getContentResolver().update(CONTENT_URI, cv, where, paras) > 0)
			   {
			       ret = true;
			   }        
		   }
		   else
		   {
			   FacebookUser user = new FacebookUser();
			   user.setUid(Long.valueOf(uid));
			   
			   if(us.getMessage()!=null)  user.setMessage(us.getMessage());
			   if(us.getStatusid()!=null) user.setStatusid(us.getStatusid());
			   if(us.getTime()!=null)     user.setStatustime(us.getTime().getTime()) ;
			   
			   insertFacebookUser(user);
			   ret = true;
		   }
		}

	}
	/********************facebookevent********************/
	public int addFacebookevent(List<Event> events)
	{
		   int count=0;
		   for(int i=0;i<events.size();i++)
		   {
			   Event item = events.get(i);
		       if(isFacebookeventExist(item.eid))
		       {
		    	   updateFacebookevent(item);
		       }
		       else
		       {
		    	   insertFacebookevent(item);
		       }
		   }
		   
		   return count;
	   }
	   
	   private ContentValues toFacebookEvent(Event event)
	   {
		    ContentValues ct = new ContentValues();
	        if(event.eid>0)ct.put(FacebookeventCol.EID, event.getEid());
	        if(!isEmpty(event.name))ct.put(FacebookeventCol.Name, event.getName());
	        if(!isEmpty(event.host))ct.put(FacebookeventCol.HOST, event.getHost());
	        if(!isEmpty(event.location))ct.put(FacebookeventCol.LOCATION, event.getLocation());
	        if(!isEmpty(event.tagline)) ct.put(FacebookeventCol.TAGLINE, event.getTagline());
	        if(event.nid>0)ct.put(FacebookeventCol.NID, event.getNid());
	        
	        if(!isEmpty(event.pic_big))ct.put(FacebookeventCol.PIC_big,     event.pic_big);
	        if(!isEmpty(event.pic))ct.put(FacebookeventCol.PIC,             event.pic);
	        if(!isEmpty(event.pic_small))ct.put(FacebookeventCol.PIC_small, event.pic_small);
	        
	        if(!isEmpty(event.description))ct.put(FacebookeventCol.DESC, event.getDescription());
	        if(!isEmpty(event.event_type))ct.put(FacebookeventCol.EVENTTYPE, event.getEvent_type());
	        if(!isEmpty(event.event_sbytype))ct.put(FacebookeventCol.EVENTSUBTYPE,event.getEvent_sbytype());
	        if(event.start_time!=null)ct.put(FacebookeventCol.STARTTIME, event.getStart_time().getTime());
	        if(event.end_time!=null)ct.put(FacebookeventCol.ENDTIME,event.getEnd_time().getTime());
	        if(!isEmpty(event.venue))ct.put(FacebookeventCol.VENUE, event.getVenue()==null?"":event.getVenue());
	        if(!isEmpty(event.rsvp_status))ct.put(FacebookeventCol.RSVP_STASTUS,event.rsvp_status);
	        if(event.creator>0)ct.put(FacebookeventCol.CREATOR, event.getCreator());
	        if(event.ceid>0)ct.put(FacebookeventCol.CEID,event.ceid);
	        ct.put(PhonebookCol.Synced, event.ceid>0?true:false);
	        
	        return ct;
	   }
	   private void insertFacebookevent(Event event)
	   {
		    Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
		    ContentValues ct = toFacebookEvent(event);	        
		    try{
		    context.getContentResolver().insert(CONTENT_URI, ct);
		    }catch(Exception ne)
		    {
		    	Log.d(TAG, "exception ne="+ne.getMessage());
		    }
	   }  
	   
	   public boolean isFacebookeventExist(long eid)
	   {
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent"); 
		   String where = " eid = ? ";
		   String[] params = {String.valueOf(eid)};
		   
		   boolean ret = false;
		   String[] eventProject = {
		    	"_id",
		    	"eid",
		   };
		   
	  	   Cursor cursor = null ;
	  	   try{
		  	   cursor = context.getContentResolver().query(CONTENT_URI, eventProject, where, params, null);  	   
		       if(cursor != null && cursor.getCount() > 0)
		       {       	 
		          ret = true;	          
		       }
	  	   }
	  	   catch(SQLiteException ne)
	  	   {
	  		   ret = true;
	  	   }
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
	       return ret;
	   }
	   
	   public int updateFacebookeventSyncTag(long eid, boolean synced,long ceid)
	   {
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
		   String where = " eid = ? ";
		   String[] params = {String.valueOf(eid)};
		   ContentValues values = new ContentValues();
		   values.put(FacebookeventCol.Synced, synced);
		   values.put(FacebookeventCol.CEID, ceid);
		   int ret = -1;
		   try{
			  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
		   }catch(SQLiteException e){
			  Log.d(TAG, "updateSyncTag Exception "+e.getMessage());
		   }
		   return ret;
	   }
	   
	   public int updateFacebookevent(Event event)
	   {	   
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
		   String where = " eid = ? ";
		   String[] params = {String.valueOf(event.eid)};		   
		   ContentValues values = toFacebookEvent(event);
		   
		   int ret = -1;
		   try{
			  ret = context.getContentResolver().update(CONTENT_URI, values, where, params);
		   }catch(SQLiteException e){
			  Log.d(TAG, "updateFacebookevent Exception "+e.getMessage());
		   }
		   return ret;
	   }
	   
	   public int deleteFacebookEvent(long eid)
	   {
	       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
	       String where = " eid = ? ";
	       String[] params = {String.valueOf(eid)};
	      
	       return context.getContentResolver().delete(CONTENT_URI, where, params);
	       
	   }
	   
	   public int deleteAllFacebookEvent()
	   {
	       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");          
           return context.getContentResolver().delete(CONTENT_URI,null,null);
           
	   }
	   
	   
	   public Event getFacebookevent(long eid)
	   {
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent"); 
		   String where = " eid = ? ";
		   String[] params = {String.valueOf(eid)};
		   
	  	   Cursor cursor = null;
	  	   Event event   = null;
	  	   try
	  	   {
		  	   cursor = context.getContentResolver().query(CONTENT_URI, FacebookeventProject, where, params, null);		  	   
		       if(cursor != null)
		       {       	 
		           while(cursor.moveToNext())
		           { 
		        	   event = formatEvent(cursor);
		               break;
		           }
		       }
		   }
	  	   catch(SQLiteException ne)
	  	   {}
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
	       return event;
	   }
	   
	   public Cursor getFacebookeventsCursor()
	   {
	       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");          
           Cursor cursor = null;
           String orderSelection = FacebookeventCol.STARTTIME + " DESC";
           List<Event> al = new ArrayList<Event>();
           try{
               cursor = context.getContentResolver().query(CONTENT_URI, FacebookeventProject, null, null, orderSelection);  
           }
           catch(SQLiteException ne)
           {}
           
           return cursor;
	   }
	   
	   public Cursor getFacebookeventsNotificationCursor(long[] eids) {
	       Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");          
           Cursor cursor = null;
           StringBuffer buff = new StringBuffer();
           if(eids !=null )
           {
               buff.append(FacebookeventCol.EID +" in (");
               for(int i = 0 ; i <eids.length;i++)
               {
                  buff.append(eids[i]);
                   if(i<(eids.length-1))
                   {
                       buff.append(",");
                   }
               }
               buff.append(")");
           }
           long now = DateUtil.getCurrentTimeForEvent();
           buff.append(" and "+FacebookeventCol.ENDTIME +">=" + now+" ");
           Log.d(TAG, " edis size is "+eids.length+"where clause is "+ buff.toString());
           String orderSelection = FacebookeventCol.STARTTIME + " DESC";
           List<Event> al = new ArrayList<Event>();
           try{
               cursor = context.getContentResolver().query(CONTENT_URI, FacebookeventProject, buff.toString(), null, orderSelection);  
           }
           catch(SQLiteException ne)
           {}
           
           return cursor;
	    }
	   
	   public List<Event> getFacebookevents()
	   {
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent"); 	 	   
	  	   Cursor cursor = null;
	  	   List<Event> al = new ArrayList<Event>();
	  	   try{
		  	   cursor = context.getContentResolver().query(CONTENT_URI, FacebookeventProject, null, null, null);		  	   
		       if(cursor != null)
		       {       	 
		           while(cursor.moveToNext())
		           { 
		        	   Event event = formatEvent(cursor);
		               al.add(event);
		           }		        
		       }
	  	   }
	       catch(SQLiteException ne)
	  	   {}
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
	       return al;
	   }
	   
	   
	   public List<Event> getUpcomingEvents()
	   {
		   Cursor cursor = null;
	  	   List<Event> al = new ArrayList<Event>();
	  	   try{
		  	   cursor = getUpcomingEventsCursor();		  	
		       if(cursor != null)
		       {       	 
		           while(cursor.moveToNext())
		           { 
		        	   Event event = formatEvent(cursor);
		               al.add(event);		               
		           }	           
		       }
	  	   }
	       catch(SQLiteException ne)
	  	   {}
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
	       return al;	       
	}
	   
   public List<Event> getPostEvents()
   {
	   Cursor cursor = null;
  	   List<Event> al = new ArrayList<Event>();
  	   try{
	  	   cursor = getPostEventsCursor();		  	
	       if(cursor != null)
	       {       	 
	           while(cursor.moveToNext())
	           { 
	        	   Event event = formatEvent(cursor);
	               al.add(event);
	           }	           
	       }
  	   }
       catch(SQLiteException ne)
  	   {}
  	   /*noneed*/
  	   {
  	       if(cursor != null)
  		   {
  		       cursor.close();
  		   }
  	   }
       return al;	       
	}
	
    public Event formatEvent(Cursor cursor)
    {
    	  Event event = new Event();
   	      event.eid = cursor.getLong(cursor.getColumnIndex(FacebookeventCol.EID));
          event.name = cursor.getString(cursor.getColumnIndex(FacebookeventCol.Name));
          event.tagline = cursor.getString(cursor.getColumnIndex(FacebookeventCol.TAGLINE));
          event.nid = cursor.getLong(cursor.getColumnIndex(FacebookeventCol.NID));
          event.pic       = cursor.getString(cursor.getColumnIndex(FacebookeventCol.PIC));
          event.pic_big   = cursor.getString(cursor.getColumnIndex(FacebookeventCol.PIC_big));
          event.pic_small = cursor.getString(cursor.getColumnIndex(FacebookeventCol.PIC_small));
          
          event.host = cursor.getString(cursor.getColumnIndex(FacebookeventCol.HOST));
          event.location = cursor.getString(cursor.getColumnIndex(FacebookeventCol.LOCATION));
          event.event_type = cursor.getString(cursor.getColumnIndex(FacebookeventCol.EVENTTYPE));
          event.event_sbytype = cursor.getString(cursor.getColumnIndex(FacebookeventCol.EVENTSUBTYPE));
          event.start_time = new Date(cursor.getLong(cursor.getColumnIndex(FacebookeventCol.STARTTIME)));
          event.end_time = new Date(cursor.getLong(cursor.getColumnIndex(FacebookeventCol.ENDTIME)));
          event.synced = cursor.getInt(cursor.getColumnIndex(FacebookeventCol.Synced))==0?false:true;
          event.ceid = cursor.getLong((cursor.getColumnIndex(FacebookeventCol.CEID)));
          event.creator = cursor.getLong(cursor.getColumnIndex(FacebookeventCol.CREATOR));
          event.venue = cursor.getString(cursor.getColumnIndex(FacebookeventCol.VENUE));
          event.rsvp_status = cursor.getString(cursor.getColumnIndex(FacebookeventCol.RSVP_STASTUS));
          event.description = cursor.getString(cursor.getColumnIndex(FacebookeventCol.DESC));
          
          return event;
    }
   public Cursor getUpcomingEventsCursor()
   {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
	   String orderSelection = FacebookeventCol.STARTTIME + " DESC";
	   
	   String where = FacebookeventCol.ENDTIME + " >= ?";
	   long endtime = new Date().getTime();
	   String[] params = {String.valueOf(endtime)};
  	   Cursor cursor = null;  	   
  	   try{
	  	   cursor = context.getContentResolver().query(CONTENT_URI, FacebookeventProject, where, params, orderSelection);
  	   }
       catch(SQLiteException ne)
  	   {}  	   
       return cursor;	       
	}
	   
	public Cursor getPostEventsCursor()
	{
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookevent");
	   String orderSelection = FacebookeventCol.STARTTIME + " DESC";
	   
	   String where = FacebookeventCol.STARTTIME + " < ?";
	   long endtime = new Date().getTime();
	   String[] params = {String.valueOf(endtime)};
	   Cursor cursor = null;
	   List<Event> al = new ArrayList<Event>();
	   try{
	      cursor = context.getContentResolver().query(CONTENT_URI, FacebookeventProject, where, params, orderSelection);
	   }
	   catch(SQLiteException ne){}
		
	   return cursor;	       
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
	
	/*
	 * Mail message process
	 */
	public void addMailMessages(List<MailboxMessage> msgs) 
	{
	    if(msgs != null)
	    {
    		for(int i=0;i<msgs.size();i++)
    		{
    			MailboxMessage msg = msgs.get(i);
    			addMailMessages(msg);
    		}
	    }
	}	
	
	public void addMailMessages(MailboxMessage msg) 
	{		
		if(isMailMessageExist(msg.threadid, msg.mid) == false)
		{
			insertMailMessages(msg);
		}
		else
		{
			updateMailMessages(msg);
		}
		
	}	
	
	private static long loginid = 0;
	private boolean savetoMessage=true;
	
	private void insertMailMessages(MailboxMessage msg)
	{
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
	       ContentValues ct = new ContentValues();
	       formatContent(ct, msg);
	       ct.put(MailMessageCol.Synced,        0);
	       	              
	       context.getContentResolver().insert(CONTENT_URI, ct);  
		   
	       //add them into message inbox/outbox	   
	       /*
	       if(this.copyNewMessagetoSms())
	       {
	           boolean tome=true;
	           if(msg.author == loginid)
	           {
	        	   tome = false;
	           }
	           
	           if(tome)
	           {
	        	   ct = null;                 
	               ct = new android.content.ContentValues();
	               MessageThreadInfo thread = this.getMailThread(msg.threadid);
	               FacebookUser user = this.getFacebookUser(msg.author);
	               if(user != null && user.name != null)
	               {
	            	   ct.put(Telephony.Sms.Inbox.ADDRESS, user.name);
	               }
	               else
	               {            	   
		               ct.put(Telephony.Sms.Inbox.ADDRESS, msg.author);
	               }
		           ct.put(Telephony.Sms.Inbox.READ,    Integer.valueOf(0));                      
		           ct.put(Telephony.Sms.Inbox.BODY,    msg.body==null?thread.subject:msg.body);
		           ct.put(Telephony.Sms.Inbox.PERSON,  msg.author);
		           ct.put(Telephony.Sms.Inbox.SUBJECT, thread.subject);
		           ct.put(Telephony.Sms.Inbox.DATE,    msg.timesent.getTime());
		           ct.put(Telephony.Sms.Inbox.REVDATE, msg.timesent.getTime());
		           ct.put(Telephony.Sms.Inbox.THREAD_ID,msg.threadid);
		           context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, ct);
	           }
	           else
	           {
	        	   ct = null;                 
	               ct = new android.content.ContentValues();
	               MessageThreadInfo thread = this.getMailThread(msg.threadid);
	               String users = this.getFacebookUserNames(thread.recipients);
	               if(users != null && users.length()> 0)
	               {
	            	   ct.put(Telephony.Sms.Sent.ADDRESS, users);
	               }
	               else
	               {            	   
		               ct.put(Telephony.Sms.Sent.ADDRESS, getString(thread.recipients));
	               }
	               
	               ct.put(Telephony.Sms.Sent.SUBJECT,  thread.subject);               
		           ct.put(Telephony.Sms.Sent.READ,     Integer.valueOf(1)); 
		           ct.put(Telephony.Sms.Sent.BODY,     msg.body==null?thread.subject:msg.body);
		           ct.put(Telephony.Sms.Sent.DATE,     msg.timesent.getTime());
		           ct.put(Telephony.Sms.Sent.REVDATE,     msg.timesent.getTime());
		           ct.put(Telephony.Sms.Sent.THREAD_ID,msg.threadid);
		           
	        	   context.getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, ct);
	           }
	       }	   
	       */    
		   
    }  
	private void formatContent(ContentValues ct, MailboxMessage msg)
	{
		ct.put(MailMessageCol.ThreadID,      msg.threadid);
        ct.put(MailMessageCol.Mid,           msg.mid);
        
        ct.put(MailMessageCol.Author,        msg.author);
        if(msg.timesent != null)
        {
            ct.put(MailMessageCol.TimeSent,      msg.timesent.getTime());
        }
        
        if(isEmpty(msg.body) == false)
        {
            ct.put(MailMessageCol.Body,          msg.body);
        }
        
        ct.put(MailMessageCol.HasAttachment, msg.hasattachment);
        ct.put(MailMessageCol.Synced,        msg.synced==true?1:0);
	}
	private void updateMailMessages(MailboxMessage msg)
	{
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
	       ContentValues ct = new ContentValues();
	       formatContent(ct, msg);
	       	              
	       String where = String.format(" %1$s = %2$s and mid='%3$s' ", MailMessageCol.ThreadID, msg.threadid, msg.mid);
		   
		   int count = context.getContentResolver().update(CONTENT_URI, ct, where, null);
		   if(count == 0)
		   {
			   Log.e(TAG, "fail to update mail message");
		   }
    }  
	   
    private boolean isMailMessageExist(long tid, String mid)
    {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage"); 
	   String where = String.format(" %1$s = %2$s and (mid='%3$s' or mid='-1') ", MailMessageCol.ThreadID, tid, mid);
	   
	   boolean ret = false;
	   String[] threadProject = {
	        MailMessageCol.ID,
	    	MailMessageCol.ThreadID,
	    	MailMessageCol.Mid,
	   };
  	   Cursor cursor = null;
  	   try
  	   {
	  	   cursor = context.getContentResolver().query(CONTENT_URI, threadProject, where, null, null);  	   
	       if(cursor != null && cursor.getCount() > 0)
	       {       	 
	          ret = true;       
	       }
  	   }
       catch(SQLiteException ne)
  	   {
    	   ret = false;
  	   }
  	   /*noneed*/
  	   {
  	       if(cursor != null)
  		   {
  		       cursor.close();
  		   }
  	   }
       return ret;
    }
	   
	public List<MailboxMessage> getMailMessages(boolean synced) 
	{
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
		   String where = String.format("synced = %1$s", synced==false?0:1);		   
	  	   Cursor cursor = null;
	  	   List<MailboxMessage> al = new ArrayList<MailboxMessage>();
	  	   try
	  	   {
		  	   cursor = context.getContentResolver().query(CONTENT_URI, MailMessageProject, where, null, null);		  	   
		       if(cursor != null)
		       {       	 
		           while(cursor.moveToNext())
		           { 
		        	   MailboxMessage item =getMailboxMessageFromCursor(cursor);	               
		               al.add(item);
		           }		          
		       }
	  	   }
	       catch(SQLiteException ne)
	  	   {}
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
	       return al;		
	}
	
	public List<MailboxMessage> getMailMessages(long tid) 
    {
           Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
           String where = String.format(" %1$s = %2$s ", MailMessageCol.ThreadID, tid);           
           Cursor cursor = null;
           List<MailboxMessage> al = new ArrayList<MailboxMessage>();
           try{
	           cursor = context.getContentResolver().query(CONTENT_URI, MailMessageProject, where, null, null);	           
	           if(cursor != null)
	           {         
	               Log.d(TAG, "get MailMessages size="+cursor.getCount() + " message tid "+tid);
	               while(cursor.moveToNext())
	               { 
	                   MailboxMessage item =getMailboxMessageFromCursor(cursor);                   
	                   al.add(item);
	               }            
	           }    
           }
	       catch(SQLiteException ne)
	  	   {}
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
           return al;       
    }
	
	public List<MailboxMessage> getMailMessages(long[] tids) 
	{
		   String where = ""; 
		   if(tids!=null && tids.length>0)
	       {
			   for(int i = 0 ; i< tids.length ; i++)
			   {
				   if(i>0)
					   where += ",";
	       	
				   where    += tids[i] ;        		
	          }           
	          where = MailMessageCol.ThreadID +" in (" + where + ")";
	       }
		 
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
		   		   
	  	   Cursor cursor = null;
	  	   List<MailboxMessage> al = new ArrayList<MailboxMessage>();
	  	   try
	  	   {
		  	   cursor = context.getContentResolver().query(CONTENT_URI, MailMessageProject, where, null, null);		  	
		       if(cursor != null)
		       {       	 
		           while(cursor.moveToNext())
		           { 
		        	   MailboxMessage item =getMailboxMessageFromCursor(cursor);
		               al.add(item);
		           }	       
		       }
	  	   }
	       catch(SQLiteException ne)
	  	   {}
	  	   /*noneed*/
	  	   {
	  	       if(cursor != null)
	  		   {
	  		       cursor.close();
	  		   }
	  	   }
	       return al;		
	}
	
	public List<MailboxMessage> getAllMailMessages() 
	{
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
	   List<MailboxMessage> al = new ArrayList<MailboxMessage>();
  	   Cursor cursor = null;
  	   try{
	  	   cursor = context.getContentResolver().query(CONTENT_URI, MailMessageProject, null, null, null);	  	 
	       if(cursor != null)
	       {       	 
	           while(cursor.moveToNext())
	           { 
	        	   MailboxMessage item =getMailboxMessageFromCursor(cursor);
	               al.add(item);
	           }           
	       }
	   }
       catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
	       if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return al;		
	}
	
	private MailboxMessage getMailboxMessageFromCursor(Cursor cursor)
	{
		   MailboxMessage item = new MailboxMessage();
	  	   item._id      = cursor.getInt(cursor.getColumnIndex(MailMessageCol.ID));
	  	   item.threadid = cursor.getLong(cursor.getColumnIndex(MailMessageCol.ThreadID));
	  	   item.mid      = cursor.getString(cursor.getColumnIndex(MailMessageCol.Mid));
	  	   item.author   = cursor.getLong(cursor.getColumnIndex(MailMessageCol.Author));
	  	   item.timesent = new Date(cursor.getLong(cursor.getColumnIndex(MailMessageCol.TimeSent)));
	  	   item.body     = cursor.getString(cursor.getColumnIndex(MailMessageCol.Body));
	  	   item.hasattachment = cursor.getInt(cursor.getColumnIndex(MailMessageCol.HasAttachment));
	  	   item.synced        = cursor.getInt(cursor.getColumnIndex(MailMessageCol.Synced)) == 0?false:true;
	  	   return item;
	}
	
	//Mail Message
    public class MailThreadCol{
        public static final String ID                  = "_id";
        public static final String thread_id           = "thread_id";
        public static final String recipients          = "recipients";        
        public static final String update_update_time  = "update_update_time";  //for update
        public static final String outbox_update_time  = "outbox_update_time"; //for outbox update time
        public static final String inbox_update_time   = "inbox_update_time"; //for inbox update time
        public static final String parent_message_id   = "parent_message_id";
        public static final String parent_thread_id   = "parent_thread_id";
        public static final String message_count      = "message_count";        
        public static final String subject            = "subject";
        public static final String snippet            = "snippet";
        public static final String snippet_author     = "snippet_author";
        public static final String object_id          = "object_id";
        public static final String unread             = "unread";
        public static final String isInbox            = "isinbox";
        public static final String isOutbox           = "isoutbox";
    }
    
    public static String[]MailThreadProject =  new String[]{
    	 "_id",
		 "thread_id",
		 "recipients",
		 "update_update_time",
		 "outbox_update_time",
		 "inbox_update_time",
		 "parent_message_id",
		 "parent_thread_id",
		 "message_count",
		 "subject",
		 "snippet",
		 "snippet_author",
		 "object_id",
		 "unread",
		 "isinbox", 
		 "isoutbox"
    };
   
	/*
	 * mail thread
	 */
    //TODO
    public void addOutMailThread(List<MessageThreadInfo> threads) 
    {       
        for(int i=0;i<threads.size();i++)
        {
            MessageThreadInfo mailbox = threads.get(i);
            mailbox.isinbox = false;
            mailbox.isoutbox = true;
            addMailThread(mailbox); 
        }
    }
    
    public void addInMailThread(List<MessageThreadInfo> threads) 
    {       
        for(int i=0;i<threads.size();i++)
        {
            MessageThreadInfo mailbox = threads.get(i);
            mailbox.isinbox = true;
            mailbox.isoutbox = false;
            addMailThread(mailbox); 
        }
    }     
    
    public void addUpdateMailThread(List<MessageThreadInfo> threads) 
    {       
        for(int i=0;i<threads.size();i++)
        {
            MessageThreadInfo mailbox = threads.get(i);
            mailbox.isinbox  = false;
            mailbox.isoutbox = false;
            addMailThread(mailbox); 
        }
    }     
  
    public void addMailThread(List<MessageThreadInfo> threads) 
	{		
		for(int i=0;i<threads.size();i++)
		{
			addMailThread(threads.get(i));
		}
	}
	
    public void addMailThread(MessageThreadInfo item) 
	{		
		if(this.isMailThreadExist(item.thread_id) == false)
		{
			insertMailThread(item);
		}
		else
		{
			updateMailThread(item);
		}
	}
	private void insertMailThread(MessageThreadInfo msg)
	{
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");
	       ContentValues ct = new ContentValues();
	       formatMailThreadContent(ct, msg);
           
		   context.getContentResolver().insert(CONTENT_URI, ct);
		   
		  //add Message
          this.addMailMessages(msg.messages);
    }  
	
	private String getString(List<Long> ids)
	{
		String ret ="";
		if(ids != null && ids.size() > 0)
		{
			for(int i=0;i<ids.size();i++)
			{
				if(i>0)
					ret +=",";
				
				ret += ids.get(i);
			}
		}
		return ret;
	}
	
	private void formatMailThreadContent(ContentValues ct, MessageThreadInfo msg)
	{
	   ct.put(MailThreadCol.thread_id,         msg.thread_id);
	   if(msg.isinbox)
	   {
	       ct.put(MailThreadCol.inbox_update_time,      msg.inbox_updated_time>0?msg.inbox_updated_time:msg.updated_time);
	   }
	   if(msg.isoutbox)
	   {
	       ct.put(MailThreadCol.outbox_update_time,     msg.outbox_updated_time>0?msg.outbox_updated_time:msg.updated_time);
	   }
	   if(msg.isinbox == false && msg.isoutbox == false)
	   {
	       ct.put(MailThreadCol.update_update_time,      msg.update_update_time>0?msg.update_update_time:msg.updated_time);
	   }
	   ct.put(MailThreadCol.recipients,        getString(msg.recipients));
       ct.put(MailThreadCol.parent_thread_id, msg.parent_thread_id);
       ct.put(MailThreadCol.parent_message_id, msg.parent_message_id);
       
       ct.put(MailThreadCol.message_count,      msg.message_count);
       ct.put(MailThreadCol.subject  ,          msg.subject);
       ct.put(MailThreadCol.snippet,            msg.snippet);
       ct.put(MailThreadCol.snippet_author,    msg.snippet_author);
       ct.put(MailThreadCol.object_id,         msg.object_id);
       ct.put(MailThreadCol.unread,            msg.unread);
       
       if(msg.isinbox == true)
       {
           ct.put(MailThreadCol.isInbox, 1);
       }
       if(msg.isoutbox == true)
       {
           ct.put(MailThreadCol.isOutbox, 1);
       }       
	}
	private void updateMailThread(MessageThreadInfo msg)
	{
		   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");
	       ContentValues ct = new ContentValues();	 
	       formatMailThreadContent(ct, msg);
	       
	       String where = String.format(" %1$s = %2$s ", MailThreadCol.thread_id, msg.thread_id);
		   
		   int count = context.getContentResolver().update(CONTENT_URI, ct, where, null);
		   if(count == 0)
		   {
			   Log.e(TAG, "fail to update mail thread");
		   }
		   
		   //add Message
	       this.addMailMessages(msg.messages);
    }  
	   
    private boolean isMailThreadExist(long tid)
    {
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread"); 
	   String where = String.format("%1$s = %2$s ", MailThreadCol.thread_id, tid);
	   
	   boolean ret = false;
	   String[] threadProject = {
	        MailThreadCol.ID,
	    	MailThreadCol.thread_id,	    	
	   };
  	   Cursor cursor = null;
  	   try{
	  	   cursor = context.getContentResolver().query(CONTENT_URI, threadProject, where, null, null);  	   
	       if(cursor != null && cursor.getCount() > 0)
	       {       	 
	          ret = true;	        
	       }
  	   }
       catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
	       if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return ret;
    }
    
    public MessageThreadInfo getMailThread(long tid, boolean needMsg) 
	{		
    	 String where = MailThreadCol.thread_id + " = "+tid; 
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");                  
         Cursor cursor      = null;       
         MessageThreadInfo item = null;
         try
         {
  	       cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, null);
  	       if(cursor != null)
  	       {         
  	           while(cursor.moveToNext())
  	           { 
  	               item = getMailboxThreadFromCusor(cursor, needMsg);               
  	               break;
  	           }        
  	       }
  	   }
  	   catch(SQLiteException ne)
  	   {}
  	   /*noneed*/
  	   {
  	       if(cursor != null)
  		   {
  		       cursor.close();
  		   }
  	   }
         return item;
	}
	public MessageThreadInfo getMailThread(long tid) 
	{		
		return getMailThread(tid, true);
	}
	
	public List<Long> getMailThreadIDs()
	{
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");	
	   String[] MailProject =  new String[] {	 MailThreadCol.ID, MailThreadCol.thread_id, };
			
  	   Cursor cursor = null;
  	   List<Long> al = new ArrayList<Long>();
  	   try
  	   {
	  	   cursor = context.getContentResolver().query(CONTENT_URI, MailProject, null, null, null);		  	   
	       if(cursor != null)
	       {       	 
	           while(cursor.moveToNext())
	           {   
	        	   al.add(cursor.getLong(cursor.getColumnIndex(MailThreadCol.thread_id)));
	           }	           
	       }
  	   }
	   catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
	       if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
	   return al;
	}
	public List<MessageThreadInfo> getMailThreads(long[] tids) 
	{
	    String where = ""; 
	    if(tids!=null && tids.length>0)
        {
		   for(int i = 0 ; i< tids.length ; i++)
		   {
			   if(i>0)
				   where += ",";
       	
			   where    += tids[i] ;        		
           }           
           where = MailThreadCol.thread_id + " in (" + where + ")";
        }
		
	   Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");		   		   
	   Cursor cursor = null;
	   List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
	   try
	   {
		   cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, null);	  	
	       if(cursor != null)
	       {       	 
	           while(cursor.moveToNext())
	           { 
	               MessageThreadInfo item = getMailboxThreadFromCusor(cursor);               
	               al.add(item);
	           }       
	       }
	   }
	   catch(SQLiteException ne)
	   {}
	   /*noneed*/
	   {
	       if(cursor != null)
		   {
		       cursor.close();
		   }
	   }
       return al;		   
	}
	
	public Cursor getAllMailThreadsCursor()
	{
		 Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");		   		   
	  	 return context.getContentResolver().query(CONTENT_URI, MailThreadProject, null, null, null);	  	   
	}
	
	public Cursor getMailInboxThreadsCursor()
	{
	    Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");     
        String where = " isinbox=1 ";
        String orderSelection = MailThreadCol.inbox_update_time + " DESC";
        Cursor cursor = null;
        List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, orderSelection); 
        }
        catch(SQLiteException ne)
        {}
        return cursor;
	}
	
	public List<MessageThreadInfo> getMailInboxThreads() 
    {
           Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");     
           String where = " isinbox=1 ";
           String orderSelection = MailThreadCol.inbox_update_time + " DESC";
           Cursor cursor = null;
           List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
           try
           {
	           cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, orderSelection);	       
	           if(cursor != null)
	           {         
	               while(cursor.moveToNext())
	               { 
	                   MessageThreadInfo item = getMailboxThreadFromCusor(cursor);
	                   al.add(item);
	               }               
	           }
           }
    	   catch(SQLiteException ne)
    	   {}
    	   /*noneed*/
    	   {
    	       if(cursor != null)
    		   {
    		       cursor.close();
    		   }
    	   }
           return al;
    }
	
	public Cursor getMailOutboxThreadsCursor()
	{
	    Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");     
        String where = " isoutbox=1 ";
        String orderSelection = MailThreadCol.outbox_update_time + " DESC";
        Cursor cursor = null;
        List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, orderSelection);             
        }
        catch(SQLiteException ne)
        {}
        return cursor;
	}
	
	public List<MessageThreadInfo> getMailOutboxThreads() 
    {
           Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");     
           String where = " isoutbox=1 ";
           String orderSelection = MailThreadCol.outbox_update_time + " DESC";
           Cursor cursor = null;
           List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
           try
           {
	           cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, orderSelection);	           
	           if(cursor != null)
	           {         
	               while(cursor.moveToNext())
	               { 
	                   MessageThreadInfo item = getMailboxThreadFromCusor(cursor);
	                   al.add(item);
	               }            
	           }
           }
    	   catch(SQLiteException ne)
    	   {}
    	   /*noneed*/
    	   {
    	       if(cursor != null)
    		   {
    		       cursor.close();
    		   }
    	   }
           return al;
    }
	
	public Cursor getMailUpdateThreadsCursor()
	{
	    Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread");     
        String where = " isinbox=0 and isoutbox = 0";
        String orderSelection = MailThreadCol.update_update_time + " DESC";
        Cursor cursor = null;
        List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, orderSelection); 
        }
        catch(SQLiteException ne)
        {}
        return cursor;
	}
	
	public MessageThreadInfo getMailboxThreadFromCusor(Cursor cursor, boolean needmsg)
	{
	    MessageThreadInfo item = new MessageThreadInfo();
	    item._id      = cursor.getInt(cursor.getColumnIndex(MailThreadCol.ID));
  	    item.thread_id = cursor.getLong(cursor.getColumnIndex(MailThreadCol.thread_id));
  	    item.update_update_time  = cursor.getLong(cursor.getColumnIndex(MailThreadCol.update_update_time));
  	    item.inbox_updated_time  = cursor.getLong(cursor.getColumnIndex(MailThreadCol.inbox_update_time));
  	    item.outbox_updated_time = cursor.getLong(cursor.getColumnIndex(MailThreadCol.outbox_update_time));
  	    item.recipients  = getLongArry(cursor.getString(cursor.getColumnIndex(MailThreadCol.recipients)));
  	    item.parent_thread_id = cursor.getLong(cursor.getColumnIndex(MailThreadCol.parent_thread_id));
  	    item.parent_message_id = cursor.getString(cursor.getColumnIndex(MailThreadCol.parent_message_id));
  	    
  	    item.message_count    = cursor.getInt(cursor.getColumnIndex(MailThreadCol.message_count));	
  	    item.subject    = cursor.getString(cursor.getColumnIndex(MailThreadCol.subject));
  	    item.snippet    = cursor.getString(cursor.getColumnIndex(MailThreadCol.snippet));
  	    item.snippet_author  = cursor.getLong(cursor.getColumnIndex(MailThreadCol.snippet_author));
  	    item.object_id    = cursor.getLong(cursor.getColumnIndex(MailThreadCol.object_id));  	  
  	    item.unread  = cursor.getInt(cursor.getColumnIndex(MailThreadCol.unread));
  	    item.isinbox       = cursor.getInt(cursor.getColumnIndex(MailThreadCol.isInbox))==0?false:true;
  	    item.isoutbox      = cursor.getInt(cursor.getColumnIndex(MailThreadCol.isOutbox))==0?false:true;
  	     	   
  	    //get message
  	    if(needmsg == true)
  	    {
  	        item.messages = this.getMailMessages(item.thread_id);
  	    }
  	    
  	    return item;
	}
	
	public MessageThreadInfo getMailboxThreadFromCusor(Cursor cursor)
	{
	    return getMailboxThreadFromCusor(cursor, true);
	}
	
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
	
	public Cursor getFriendBDByMonth(String month)
	{
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
        
        String where = " birthday like '"+month+"%'";
        
        Cursor cursor = null;
        ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where,null, null); 
        }
        catch(SQLiteException ne)
        {}
        return cursor;
	}
	
	public List<FacebookUser> getUpcomingBDUserByMonth(String month)
	{
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
        
        String where = " birthday like '"+month+"%'";
        
        Cursor cursor = null;
        ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where,null, null);        
            if(cursor != null)
            {                
                while(cursor.moveToNext())
                {
                     FacebookUser fuser = formatFacebookUser(cursor);
                     ls.add(fuser);                  
                 }             
            }
        }
        catch(SQLiteException ne)
        {}
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }         
        
        return ls;      
	}
	
	public Cursor getUpcomingBDUserCursor()
	{
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
        
        String where = " birthday like '"+monthArray[new Date().getMonth()]+"%'";
        
        Cursor cursor = null;
        ArrayList<FacebookUser.SimpleFBUser> ls = new ArrayList<FacebookUser.SimpleFBUser>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where,null, null);        
            if(cursor != null)
            {                
                    
            }
        }
        catch(SQLiteException ne)
        {}
       return cursor;
	}
	
	public List<FacebookUser.SimpleFBUser> getUpcomingBDUser()
	{
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
        
        String where = " birthday like '"+monthArray[new Date().getMonth()]+"%'";
        
    	Cursor cursor = null;
    	ArrayList<FacebookUser.SimpleFBUser> ls = new ArrayList<FacebookUser.SimpleFBUser>();
    	try
    	{
	        cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where,null, null);    	 
	        if(cursor != null)
	        {           	 
	            while(cursor.moveToNext())
	            {
	            	 FacebookUser.SimpleFBUser fuser = formatSimpleFacebookUser(cursor);
	                 ls.add(fuser);	                 
	             }             
	        }
		}
	    catch(SQLiteException ne)
	    {}
	    /*noneed*/
	    {
	        if(cursor != null)
		    {
		        cursor.close();
		    }
	    }         
	    
        return ls;      
   }
	
    public List<FacebookUser> getAllBDUser()
	{
         Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/facebookusers");
        
         String where = " birthday != '' ";
        
    	 Cursor cursor = null;
    	 ArrayList<FacebookUser> ls = new ArrayList<FacebookUser>();
    	 try{
	    	 cursor = context.getContentResolver().query(CONTENT_URI, FacebookUsersProject, where,null, null);	    	 
	         if(cursor != null)
	         {           	 
	             while(cursor.moveToNext())
	             {
	            	 FacebookUser fuser = formatFacebookUser(cursor);
	                 ls.add(fuser);
	             }             
	         }
    	}
 	    catch(SQLiteException ne)
 	    {}
 	    /*noneed*/
 	    {
 	        if(cursor != null)
 		    {
 		        cursor.close();
 		    }
 	    }
        return ls;      
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
	
	
	public void saveWallAsDraft(String message,List<FacebookStatusItem> contents,long fuid){
		clearWallDraftByFuid(fuid);
		
		Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/walldraft");
		
		if(contents!=null && contents.size()>0){
			for(FacebookStatusItem content:contents){
				 ContentValues ct = new ContentValues();
				 ct.put(walldraftCol.FUID,fuid);
				 ct.put(walldraftCol.CONTENT, content.url);
				 ct.put(walldraftCol.CONTENTTYPE,content.type.name());
				 ct.put(walldraftCol.LASTMODIFY, new Date().getTime());			
			     context.getContentResolver().insert(CONTENT_URI, ct);  
			}
		}
		
		if(message!=null && message.length()>0){
			ContentValues ct = new ContentValues();
			 ct.put(walldraftCol.FUID,fuid);
			 ct.put(walldraftCol.CONTENT, message);
			 ct.put(walldraftCol.CONTENTTYPE,FacebookStatusItem.ContentType.NONE.name());
			 ct.put(walldraftCol.LASTMODIFY, new Date().getTime());			
		     context.getContentResolver().insert(CONTENT_URI, ct);  
		}
			
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
	
	
	public  List<FacebookStatusItem> getWallDraftByFuid(long fuid){
		
		 String where = " fuid = "+fuid; 
	     Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/walldraft");                  
	     Cursor cursor = null;
	     List<FacebookStatusItem> items = new ArrayList<FacebookStatusItem>();
	     try{
		     cursor = context.getContentResolver().query(CONTENT_URI,walldraftProject , where, null, null);		  
		     if(cursor != null)
		     {         
		        while(cursor.moveToNext())
		        { 
		            FacebookStatusItem item = getFacebookStatusItemFromCursor(cursor);               
		            if(item!=null) items.add(item);
		        }	         
		     }
	     }
 	     catch(SQLiteException ne)
 	     {}
 	     /*noneed*/
 	     {
 	        if(cursor != null)
 		    {
 		        cursor.close();
 		    }
 	     }
	     return items;   
	}
	
	public FacebookStatusItem getFacebookStatusItemFromCursor(Cursor cursor)
	{
		FacebookStatusItem  item = new FacebookStatusItem();		
		if(cursor!=null)
		{
	  	   item.name          = cursor.getString(cursor.getColumnIndex(walldraftCol.CONTENT));
	  	   String contenttype = cursor.getString(cursor.getColumnIndex(walldraftCol.CONTENTTYPE));
	  	   
	  	   if(contenttype.equals(FacebookStatusItem.ContentType.IMAGE.name()))
	  	   {
	  		   item.type = FacebookStatusItem.ContentType.IMAGE;
	  	   }
	  	   else if(contenttype.equals(FacebookStatusItem.ContentType.CAMERA.name()))
	  	   {
	  		   item.type = FacebookStatusItem.ContentType.CAMERA;
	  	   }
	  	   else if(contenttype.equals(FacebookStatusItem.ContentType.VIDEO.name()))
	  	   {
	  		   item.type = FacebookStatusItem.ContentType.VIDEO;
	  	   }
	  	   else if(contenttype.equals(FacebookStatusItem.ContentType.LINK.name()))
	  	   {
	  		   item.type = FacebookStatusItem.ContentType.LINK;
	  	   }
	  	   else if(contenttype.equals(FacebookStatusItem.ContentType.NONE.name()))
	  	   {
	  		   item.type = FacebookStatusItem.ContentType.NONE;
	  	   }
	  	   
	  	   item.url = item.name;
	  	   item.uploadStatus = FacebookStatusItem.Status.INIT;
		}
	  	   
		return item;
	}
	
	
	/************************facebook album*****************/
	final Uri ALBUM_CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/album");
    public class PhotoAlbumCol
    {
        public final static String ID             = "_id";
        public final static String ALBUM_ID       = "albumid";
        public final static String COVER_PID      = "coverpid";
        public final static String cover_src_url  = "cover_src_url";
        public final static String OWNER          = "owner";
        public final static String NAME           = "name";
        public final static String CREATED_TIME   = "createdtime";
        public final static String MODIFIED_TIME  = "modifiedtime";
        public final static String DESC           = "description";
        public final static String LOCATION      = "location";
        public final static String LINK          = "link";
        public final static String SIZE          = "size";
        public final static String VISIBLE      =  "visible";
        public final static String MODIFIED_MAJOR = "modified_major";
    }
    private PhotoAlbum formatAlbumFromCursor(Cursor cursor)
    {
        PhotoAlbum photoalbum = new PhotoAlbum();
        photoalbum.id = cursor.getLong(cursor.getColumnIndex(PhotoAlbumCol.ID));
        photoalbum.aid = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.ALBUM_ID));
        photoalbum.cover_pid = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.COVER_PID));
        photoalbum.cover_src_url = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.cover_src_url));
        photoalbum.name = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.NAME));
        photoalbum.description = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.DESC));
        photoalbum.link = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.LINK));
        photoalbum.location = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.LOCATION));
        photoalbum.owner = cursor.getLong(cursor.getColumnIndex(PhotoAlbumCol.OWNER));
        photoalbum.size = cursor.getInt(cursor.getColumnIndex(PhotoAlbumCol.SIZE));
        photoalbum.created = new Date(cursor.getLong(cursor.getColumnIndex(PhotoAlbumCol.CREATED_TIME)));
        photoalbum.modified = new Date(cursor.getLong(cursor.getColumnIndex(PhotoAlbumCol.MODIFIED_TIME)));
        photoalbum.visible = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.VISIBLE));
        photoalbum.modified_major = cursor.getString(cursor.getColumnIndex(PhotoAlbumCol.MODIFIED_MAJOR));
        return photoalbum;
    }
    
    private ContentValues formatAlbumContentValues(PhotoAlbum photoalbum)
    {
        ContentValues cv = new ContentValues();
        cv.put(PhotoAlbumCol.ALBUM_ID, photoalbum.aid);
        cv.put(PhotoAlbumCol.COVER_PID, photoalbum.cover_pid);
        
        if(!isEmpty(photoalbum.cover_src_url))        cv.put(PhotoAlbumCol.cover_src_url, photoalbum.cover_src_url);
        if(photoalbum.created!=null)         cv.put(PhotoAlbumCol.CREATED_TIME, photoalbum.created.getTime());
        if(photoalbum.modified!=null)        cv.put(PhotoAlbumCol.MODIFIED_TIME, photoalbum.modified.getTime());
        if(!isEmpty(photoalbum.description)) cv.put(PhotoAlbumCol.DESC, photoalbum.description);
        if(photoalbum.owner > 0)             cv.put(PhotoAlbumCol.OWNER, photoalbum.owner);
        if(photoalbum.size > 0)              cv.put(PhotoAlbumCol.SIZE,photoalbum.size);
        if(!isEmpty(photoalbum.link))        cv.put(PhotoAlbumCol.LINK, photoalbum.link);
        if(!isEmpty(photoalbum.location))    cv.put(PhotoAlbumCol.LOCATION,photoalbum.location);
        if(!isEmpty(photoalbum.name))        cv.put(PhotoAlbumCol.NAME, photoalbum.name); 
        if(isEmpty(photoalbum.visible) == false) cv.put(PhotoAlbumCol.VISIBLE, photoalbum.visible);
        if(isEmpty(photoalbum.modified_major) == false) cv.put(PhotoAlbumCol.MODIFIED_MAJOR, photoalbum.modified_major);
        return cv;
    }
    
    private static String[] albumprojection = {
            PhotoAlbumCol.ID,
            PhotoAlbumCol.ALBUM_ID,
            PhotoAlbumCol.COVER_PID,
            PhotoAlbumCol.cover_src_url,
            PhotoAlbumCol.CREATED_TIME,
            PhotoAlbumCol.MODIFIED_TIME,
            PhotoAlbumCol.NAME,
            PhotoAlbumCol.DESC,
            PhotoAlbumCol.LINK,
            PhotoAlbumCol.LOCATION,
            PhotoAlbumCol.OWNER,
            PhotoAlbumCol.SIZE,
            PhotoAlbumCol.VISIBLE,
            PhotoAlbumCol.MODIFIED_MAJOR
    };      
	public List<PhotoAlbum> getAlbum(long owner)
	{
	    List<PhotoAlbum> photoalbums = new ArrayList<PhotoAlbum>();	  
	    String orderSelection = PhotoAlbumCol.CREATED_TIME + " DESC";
	    
	    long userID       = getLoginUserID();
	    Cursor cursor = null;
	    try{
		    cursor = context.getContentResolver().query(ALBUM_CONTENT_URI, albumprojection, PhotoAlbumCol.OWNER+"="+owner + " or "+PhotoAlbumCol.ALBUM_ID +"= '-2'", null,orderSelection);
		    if(cursor!=null)
		    {
		        while(cursor.moveToNext())
	            { 
	               PhotoAlbum photoalbum = formatAlbumFromCursor(cursor);
	               
	               if("-2".equals(photoalbum.aid) && owner == userID)
	               {
	                   photoalbums.add(0, photoalbum);
	               }
	               else
	               {
	                   photoalbums.add(photoalbum);
	               }
	               
	            }          
		    }
	    }
	    catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
	    return photoalbums;
	}
	
	public void addAlbum(PhotoAlbum photoalbum)
	{
	    if(photoalbum!=null)
	    {
	        if(!isAlbumExist(photoalbum.aid))
	        {
	            insertAlbum(photoalbum);
	        }
	        else
	        {
	            updateAlbum(photoalbum);
	        }
	    }    
	}
	
	private Uri insertAlbum(PhotoAlbum photoalbum)
	{    
        return context.getContentResolver().insert(ALBUM_CONTENT_URI, formatAlbumContentValues(photoalbum));
	}
	
	public int updateAlbum(PhotoAlbum photoalbum)
	{
	    String where = PhotoAlbumCol.ALBUM_ID+"= ? ";
	    String[] selectionArgs = {String.valueOf(photoalbum.aid)};
        return context.getContentResolver().update(ALBUM_CONTENT_URI, formatAlbumContentValues(photoalbum),where, selectionArgs);
	}
	
	public void addAlbum(List<PhotoAlbum> photoalbums)
	{   
	    if(photoalbums != null && photoalbums.size() > 0)
	    {
	        deleteAllAlbums(photoalbums.get(0).owner);
	    }
	    for(PhotoAlbum photoalbum : photoalbums)
	    {
	        if(false == isAlbumExist(photoalbum.aid))
	        {
	            insertAlbum(photoalbum);    
	        }
	        else
	        {
	            updateAlbum(photoalbum);
	        }    
	    }    
	}
	
	public void deleteAlbum(String albumid)
	{
	    String whereclause =  PhotoAlbumCol.ALBUM_ID+"= '"+albumid+"'";  
	    context.getContentResolver().delete(ALBUM_CONTENT_URI, whereclause, null);
	}
	
	public void deleteAllAlbums(long owner)
	{   
	    String whereclause = PhotoAlbumCol.ALBUM_ID+" != '-2' and "+PhotoAlbumCol.OWNER + " ="+owner;
	    context.getContentResolver().delete(ALBUM_CONTENT_URI, whereclause, null);
	}
	
	public boolean isAlbumExist(String albumid)
	{
	    boolean ret = false;
        String[] albumProject = {
             PhotoAlbumCol.ID,
             PhotoAlbumCol.ALBUM_ID
        };
        
        Cursor cursor = null ;
        try{
            cursor = context.getContentResolver().query(ALBUM_CONTENT_URI, albumProject, PhotoAlbumCol.ALBUM_ID+" = '"+albumid+"' ", null, null);        
            if(cursor != null && cursor.getCount() > 0)
            {         
               ret = true;             
            }
        }
        catch(SQLiteException ne)
        {
            ret = true;
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
        return ret;
	}
	
	//for Photo
	final Uri PHOTO_CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/photo");
    public class PhotoCol
    {
    	public final static String id       = "_id";
    	public final static String pid       = "pid";
    	public final static String aid       = "aid"; 
    	public final static String owner     = "owner";
    	public final static String src_small = "src_small";
    	public final static String src_big   = "src_big";
    	public final static String src       = "src";
    	public final static String link      = "link";
    	public final static String caption   = "caption";
    	public final static String created   = "created";
    }
    
	public List<Photo> getAlbumPhotos(String aid)
	{
	    List<Photo> photoalbums = new ArrayList<Photo>();	  
	    String orderSelection = PhotoCol.created + " ASC";
	    String[] projection = {
	            PhotoCol.id,
	            PhotoCol.pid,
	            PhotoCol.aid,
	            PhotoCol.owner,
	            PhotoCol.src_small,
	            PhotoCol.src_big,
	            PhotoCol.src,
	            PhotoCol.link,
	            PhotoCol.caption,
	            PhotoCol.created,	            
	    };	    
	    Cursor cursor = null;
	    try{
		    cursor = context.getContentResolver().query(PHOTO_CONTENT_URI, projection, PhotoCol.aid +" = '"+aid+"'", null,orderSelection);
		    if(cursor!=null)
		    {
		        while(cursor.moveToNext())
	            { 
	               Photo photo = formatPhotoFromCursor(cursor);
	               //Log.d(TAG, "photo,="+photo);
	               photoalbums.add(photo);
	            }          
		    }
	    } 
	    catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
	    
	    
	    return photoalbums;
	}
	
	public PhotoAlbum getAlbumForNotify(String owner, String aid)
	{
		PhotoAlbum photoalbum = null;	  
	    String orderSelection = PhotoAlbumCol.MODIFIED_TIME + " DESC";
	    long userID       = getLoginUserID();
	    Cursor cursor = null;
	    try{
		    cursor = context.getContentResolver().query(ALBUM_CONTENT_URI, albumprojection, 
		    		PhotoAlbumCol.OWNER+"="+owner + " and "+
		    		PhotoAlbumCol.LINK +" like '%aid=" +aid +"%' ", 
		    		null,
		    		orderSelection);
		    
		    if(cursor!=null)
		    {
		        while(cursor.moveToNext())
	            { 
	               photoalbum = formatAlbumFromCursor(cursor);
	               break;
	            }          
		    }
	    }
	    catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
	    return photoalbum;
	}
	
	public PhotoAlbum getAlbum(String aid)
	{
	    PhotoAlbum photoalbum = null;    
        String orderSelection = PhotoAlbumCol.MODIFIED_TIME + " DESC";    
        
        long userID       = getLoginUserID();
        Cursor cursor = null;
        try{
	        cursor = context.getContentResolver().query(ALBUM_CONTENT_URI, albumprojection, PhotoAlbumCol.ALBUM_ID +"= '" +aid +"'", null,orderSelection);
	        if(cursor!=null)
	        {
	            while(cursor.moveToNext())
	            { 
	               photoalbum = formatAlbumFromCursor(cursor);
	               break;
	            }          
	        }
        }
        catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
        return photoalbum;
	}
	
	public PhotoAlbum getAlbum(String owner, String aid)
	{
		PhotoAlbum photoalbum = null;	  
	    String orderSelection = PhotoAlbumCol.MODIFIED_TIME + " DESC";   
	    
	    long userID       = getLoginUserID();
	    Cursor cursor = null;
	    try{
		    cursor = context.getContentResolver().query(ALBUM_CONTENT_URI, albumprojection, PhotoAlbumCol.OWNER+"="+owner + " and "+PhotoAlbumCol.ALBUM_ID +"= '" +aid +"'", null,orderSelection);
		    if(cursor!=null)
		    {
		        while(cursor.moveToNext())
	            { 
	               photoalbum = formatAlbumFromCursor(cursor);
	               break;
	            }          
		    }
	    }
	    catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
	    return photoalbum;
	}
	
	public Photo getAlbumPhoto(String pid)
    {
        Photo photo = null;   
        String orderSelection = PhotoCol.created + " ASC";
        String[] projection = {
                PhotoCol.id,
                PhotoCol.pid,
                PhotoCol.aid,
                PhotoCol.owner,
                PhotoCol.src_small,
                PhotoCol.src_big,
                PhotoCol.src,
                PhotoCol.link,
                PhotoCol.caption,
                PhotoCol.created,               
        };      
        Cursor cursor = null;
        try{
	        cursor = context.getContentResolver().query(PHOTO_CONTENT_URI, projection," pid = '"+pid+"' " , null,orderSelection);
	        if(cursor!=null)
	        {
	            while(cursor.moveToNext())
	            { 
	               photo = formatPhotoFromCursor(cursor);
	               break;
	            }          
	        }
        } catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
        
        return photo;
    }
	
	public Photo getAlbumPhoto(String owner, String pid)
	{
		Photo photo = null;	  
	    String orderSelection = PhotoCol.created + " ASC";
	    String[] projection = {
	            PhotoCol.id,
	            PhotoCol.pid,
	            PhotoCol.aid,
	            PhotoCol.owner,
	            PhotoCol.src_small,
	            PhotoCol.src_big,
	            PhotoCol.src,
	            PhotoCol.link,
	            PhotoCol.caption,
	            PhotoCol.created,	            
	    };	    
	    Cursor cursor = null;
	    try{
		    cursor = context.getContentResolver().query(PHOTO_CONTENT_URI, projection, " owner= " + owner +" and pid = '"+pid+"' " , null,orderSelection);
		    if(cursor!=null)
		    {
		        while(cursor.moveToNext())
	            { 
	               photo = formatPhotoFromCursor(cursor);
	               break;
	            }          
		    }
	    } catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
	    
	    return photo;
	}
	public Photo getAlbumPhotoForNotify(String owner, String pid)
	{
		Photo photo = null;	  
	    String orderSelection = PhotoCol.created + " ASC";
	    String[] projection = {
	            PhotoCol.id,
	            PhotoCol.pid,
	            PhotoCol.aid,
	            PhotoCol.owner,
	            PhotoCol.src_small,
	            PhotoCol.src_big,
	            PhotoCol.src,
	            PhotoCol.link,
	            PhotoCol.caption,
	            PhotoCol.created,	            
	    };	    
	    Cursor cursor = null;
	    try{
		    cursor = context.getContentResolver().query(PHOTO_CONTENT_URI, projection, 
		    		" owner= " + owner +" and "+
		    		PhotoCol.link + " like '%pid="+ pid +"%' " , 
		    		null,
		    		orderSelection);
		    
		    if(cursor!=null)
		    {
		        while(cursor.moveToNext())
	            { 
	               photo = formatPhotoFromCursor(cursor);
	               break;
	            }          
		    }
	    } catch(SQLiteException ne)
        {
            
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
	    
	    return photo;
	}
	
	private Photo formatPhotoFromCursor(Cursor cursor)
    {
		 Photo photoalbum = new Photo();               
         photoalbum.pid = cursor.getString(cursor.getColumnIndex(PhotoCol.pid));
         photoalbum.aid = cursor.getString(cursor.getColumnIndex(PhotoCol.aid));
         photoalbum.owner = cursor.getLong(cursor.getColumnIndex(PhotoCol.owner));               
         photoalbum.src_small = cursor.getString(cursor.getColumnIndex(PhotoCol.src_small));
         photoalbum.src_big   = cursor.getString(cursor.getColumnIndex(PhotoCol.src_big));
         photoalbum.src       = cursor.getString(cursor.getColumnIndex(PhotoCol.src));
         photoalbum.link      = cursor.getString(cursor.getColumnIndex(PhotoCol.link));
         photoalbum.caption   = cursor.getString(cursor.getColumnIndex(PhotoCol.caption));               
         photoalbum.created   = new Date(cursor.getLong(cursor.getColumnIndex(PhotoCol.created))); 
        return photoalbum;
    }
	
	public void addPhoto(Photo photo)
	{
	    if(false == isPhotoExist(photo.pid))
	    {
	        insertPhoto(photo);
	    }
	    else
	    {
	        updatePhoto(photo);
	    }
	}
	
	private Uri insertPhoto(Photo photoalbum)
	{
	    ContentValues cv = new ContentValues();
	    cv.put(PhotoCol.aid,   photoalbum.aid);
        cv.put(PhotoCol.pid,   photoalbum.pid);
        cv.put(PhotoCol.owner, photoalbum.owner);
        
        if(!isEmpty(photoalbum.src))           cv.put(PhotoCol.src,   photoalbum.src);
        if(!isEmpty(photoalbum.src_small))     cv.put(PhotoCol.src_small,   photoalbum.src_small);
        if(!isEmpty(photoalbum.src_big))       cv.put(PhotoCol.src_big,   photoalbum.src_big);
        if(!isEmpty(photoalbum.caption)) cv.put(PhotoCol.caption, photoalbum.caption);        
        if(photoalbum.created!=null)     cv.put(PhotoCol.created, photoalbum.created.getTime());
        if(!isEmpty(photoalbum.link))    cv.put(PhotoCol.link,  photoalbum.link);   
        
        return context.getContentResolver().insert(PHOTO_CONTENT_URI, cv);
	}
	
	public int updatePhoto(Photo photoalbum)
	{
	    String where = PhotoCol.pid+"= ? ";
	    String[] selectionArgs = {String.valueOf(photoalbum.pid)};
	    
	    ContentValues cv = new ContentValues();
	    cv.put(PhotoCol.aid,   photoalbum.aid);
        cv.put(PhotoCol.pid,   photoalbum.pid);
        cv.put(PhotoCol.owner, photoalbum.owner);
        
        if(!isEmpty(photoalbum.src))           cv.put(PhotoCol.src,   photoalbum.src);
        if(!isEmpty(photoalbum.src_small))     cv.put(PhotoCol.src_small,   photoalbum.src_small);
        if(!isEmpty(photoalbum.src_big))       cv.put(PhotoCol.src_big,   photoalbum.src_big);
        if(!isEmpty(photoalbum.caption)) cv.put(PhotoCol.caption, photoalbum.caption);        
        if(photoalbum.created!=null)     cv.put(PhotoCol.created, photoalbum.created.getTime());
        if(!isEmpty(photoalbum.link))    cv.put(PhotoCol.link,  photoalbum.link);
        
        return context.getContentResolver().update(PHOTO_CONTENT_URI, cv,where, selectionArgs);
	}
	
	public void addPhoto(List<Photo> photoalbums,boolean clearOldphoto)
	{
	    if(clearOldphoto == true)
	    {
	        clearOldPhoto(photoalbums);
	    }    
	    addPhoto(photoalbums);
	}
	
	public void addPhoto(List<Photo> photoalbums)
	{  
	    for(Photo photoalbum : photoalbums)
	    {
	        if(false == isPhotoExist(photoalbum.pid))
	        {
	            insertPhoto(photoalbum);    
	        }
	        else
	        {
	            updatePhoto(photoalbum);
	        }    
	    }    
	}
	
	private void clearOldPhoto(List<Photo> photoalbums) {
	    //first clear not exist photos from DB
        if(photoalbums!=null && photoalbums.size()>0)
        {
          String aid = photoalbums.get(0).getAid();
          List<Photo> oldphotos = getAlbumPhotos(aid);
          for(Photo oldphoto: oldphotos)
          {
              boolean exist = false;
              for(Photo newphoto : photoalbums)
              {
                  if(oldphoto.pid.equalsIgnoreCase(newphoto.pid))
                  {
                      exist = true;
                      break;
                  }
              }
              
              if(exist == false)
              {
                  deletePhoto(oldphoto.pid);
              }
          }
        }
        
    }
    public void deletePhoto(String pid)
	{
	    String whereclause =  PhotoCol.pid+"= '"+pid+"'";  
	    context.getContentResolver().delete(PHOTO_CONTENT_URI, whereclause, null);
	}
	
	public void deleteAllPhotoInAlbum(String aid)
	{   
		 String whereclause =  PhotoCol.aid+"= '"+aid+"'";  
		 context.getContentResolver().delete(PHOTO_CONTENT_URI, whereclause, null);
	}
	
	public void deleteAllPhoto()
	{   
	    context.getContentResolver().delete(PHOTO_CONTENT_URI, null, null);
	}
	
	public boolean isPhotoExist(String pid)
	{
	    boolean ret = false;
        String[] albumProject = {
             PhotoCol.id,
             PhotoCol.pid
        };
        
        Cursor cursor = null ;
        try{
            cursor = context.getContentResolver().query(PHOTO_CONTENT_URI, albumProject, PhotoCol.pid+" ='"+pid+"' ", null, null);        
            if(cursor != null && cursor.getCount() > 0)
            {         
               ret = true;             
            }
        }
        catch(SQLiteException ne)
        {
            ret = true;
        }
        /*noneed*/
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
        return ret;
	}
	//end photo
	
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
		
		RestPostMethod.setProxy(ret, 
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
		
		RestPostMethod.setProxy(checked, 
				getProxyHost(), 
				port,
				getProxyUsername(), 
				getProxyPassword());
		
	}
	
	/*
	 * type 
	 * 0 inbox, 1 out, 2 update
	 */
    public Cursor searchMailCursor(int type, String key) {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailthread"); 
        String where =  "("+MailThreadCol.subject + " like '%"+key+"%' or "+
                        MailThreadCol.snippet + " like '%"+key+"&') ";
        if(type == 0)
        {
            where = where +" and "+ MailThreadCol.isInbox +"=1";
        }
        else if(type == 1)
        {
            where = where  +" and "+  MailThreadCol.isOutbox +"=1";
        }
        else if(type == 2)
        {
            where = where  +" and "+  MailThreadCol.isInbox +"= 0" + " and " + MailThreadCol.isOutbox +"= 0";
        }
        
        Cursor cursor = null;
        List<MessageThreadInfo> al = new ArrayList<MessageThreadInfo>();
        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, MailThreadProject, where, null, null); 
        }
        catch(SQLiteException ne)
        {}
        return cursor;
    }
    
    public List<PhotoAlbum> getAlbumPhotosByOwner(long uid) {        
        List<PhotoAlbum> photoalbums = new ArrayList<PhotoAlbum>();
        Cursor cursor = null;
        try{
	        String orderSelection = PhotoAlbumCol.MODIFIED_TIME + " DESC";    
	        cursor = context.getContentResolver().query(ALBUM_CONTENT_URI, albumprojection, PhotoAlbumCol.OWNER +"="+ uid, null,orderSelection);
	        if(cursor!=null)
	        {
	            while(cursor.moveToNext())
	            { 
	               PhotoAlbum photoalbum = formatAlbumFromCursor(cursor);
	               
	               if("-2".equals(photoalbum.aid))
	               {
	                   photoalbums.add(0, photoalbum);
	               }
	               else
	               {
	                   photoalbums.add(photoalbum);
	               }
	               
	            }          
	        }
        }
        catch(Exception ne)
        {
        	
        }
        /*noneed*/
        {
            if(cursor !=null)
            {
                cursor.close();
                cursor = null;
            }           
        }
        
        return photoalbums;
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
        /*noneed*/{
            if(cursor !=null)
            {
                cursor.close();
                cursor = null;
            }
           
        }
        
    }
	public void addStreamFilter(List<StreamFilter> filters) 
	{				
	   if(filters != null)
 	   {
 		    FileOutputStream fos = null;
			ObjectOutputStream out = null;
			try
			{
			    fos = new FileOutputStream(TwitterHelper.filter);
			    out = new ObjectOutputStream(fos);
			    Date date = new Date();
			    out.writeLong(date.getTime());
			    int count = filters.size();
			    out.writeInt(count);
			    for(int i=0;i<count;i++)
			    {
			    	StreamFilter item = filters.get(i);
			    	out.writeObject(item);
			    }	       				    
			    out.close();
			}
			catch(IOException ex)
			{
			    Log.d(TAG, "serialization fail="+ex.getMessage());
			}
			catch(Exception ex)
			{
			    Log.d(TAG, "serialization fail="+ex.getMessage());
			}
 	   }
	}
	public List<StreamFilter> getStreamFilter()
	{
		FileInputStream fis  = null;
		ObjectInputStream in = null;
		List<StreamFilter>  streams = new ArrayList<StreamFilter> ();
		try{
		    fis = new FileInputStream(TwitterHelper.filter);
		    in = new ObjectInputStream(fis);
		    long lastrecord = in.readLong();		    
		    Log.d(TAG, "stream filter last time ="+new Date(lastrecord).toLocaleString());
		    		    
		    int count = in.readInt();
		    for(int i=0;i<count;i++)
		    {
		    	StreamFilter item = (StreamFilter) in.readObject();
		    	streams.add(item);
		    }
		    in.close();
		}
		catch(IOException ex)
		{
			Log.d(TAG, "deserialization fail="+ex.getMessage());
		}
		catch(ClassNotFoundException ex)
		{
			Log.d(TAG, "deserialization fail="+ex.getMessage());
		}
		catch(Exception ex)
		{
			Log.d(TAG, "deserialization fail="+ex.getMessage());
		}
		
		return streams;
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
     public MailboxMessage getLatestedMessage(long thread_id, long meid) {
        Uri CONTENT_URI = Uri.parse(SNS_CONTENT_URI + "/mailmessage");
        List<MailboxMessage> al = new ArrayList<MailboxMessage>();
        String whereclause =  MailMessageCol.Author + " != " + meid + " and "+MailMessageCol.ThreadID+"="+thread_id;
        String orderSelection = MailMessageCol.TimeSent+ " DESC";   
        Cursor cursor = null;
        MailboxMessage message = null;
        try{
           cursor = context.getContentResolver().query(CONTENT_URI, MailMessageProject, whereclause, null, orderSelection);      
           if(cursor != null)
           {         
               while(cursor.moveToNext())
               { 
                   message =getMailboxMessageFromCursor(cursor);
                   break;
               }           
           }
       }
       catch(SQLiteException ne)
       {}
       /*noneed*/
       {
           if(cursor != null)
           {
               cursor.close();
           }
       }    
        return message;
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
}
