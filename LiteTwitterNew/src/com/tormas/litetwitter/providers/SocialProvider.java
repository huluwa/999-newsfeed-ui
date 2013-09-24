package com.tormas.litetwitter.providers;

import java.util.Date;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class SocialProvider extends ContentProvider
{
	    public static final Uri CONTENT_URI = Uri.parse("content://com.tormas.litetwitter.providers.SocialProvider");
		private static final String TAG ="SocialProvider";    
		private static SQLiteOpenHelper    dbHelper;
		private static final String sDatabaseName = "sns.db";
	    /*
	     * 0------------1  create the database
	     * 1------------2  add facebook relative table
	     * 2------------3  change facebook user table	  
	     * 3------------4  change facebookcontacts to phonebook
	     * 5------------6  add mailthread, mailmessge table
	     * 6------------7  change mailmessage
	     * 7------------8  add issnsenable
	     * 8------------9  add c_event_id to facebookuses, add c_event_id to facebookevent
	     * 9------------10 add peoplemapfacebook
	     * 10-----------11 add new table walldraft
	     * 11-----------12 add new filed to phonebook
	     * 12-----------13 merge following and follower into follow
	     * 15-----------add update
	     */
	    
	    private static final int DATABASE_VERSION = 17;
	    private static final String Authorities = "com.tormas.litetwitter.providers.SocialProvider";
	    
	    /** URI matcher used to recognize URIs sent by applications */
	    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	    private static final int account_id        = 1;
	    private static final int settings_id       = 2;
	    private static final int trends_id         = 3;
	    private static final int follow_id         = 4;
	    
	    
	    private static final int facebookfriends_id = 6;
	    private static final int facebookcontacts_id = 7;
	    private static final int facebookeventtype_id = 8;
	    private static final int facebooksubeventtype_id = 9; 
	    private static final int facebookusers_id    = 10;
	    private static final int extpermission_id    = 11;
	    private static final int facebookevent_id    = 12;
	    private static final int mailThread_id        = 13;
	    private static final int mailMessage_id       = 14;
	    private static final int peoplemapfacebook_id=15;	    
	    private static final int walldraft_id          = 16;
	    private static final int album_id              = 17;
	    private static final int photo_id              = 18;
	    private static final int page_id               = 19;
	    private static final int note_id               = 20;
	    private static final int facebookusers_limit_id = 21;
	    static 
	    {
	        sURIMatcher.addURI(Authorities, "account",        account_id);
	        sURIMatcher.addURI(Authorities, "settings",       settings_id);
	        sURIMatcher.addURI(Authorities, "trends",         trends_id);	        
	        sURIMatcher.addURI(Authorities, "follow",         follow_id);
	        
	        sURIMatcher.addURI(Authorities, "facebookfriends",       facebookfriends_id);
	        sURIMatcher.addURI(Authorities, "phonebook",             facebookcontacts_id);
	        sURIMatcher.addURI(Authorities, "facebookeventtype",     facebookeventtype_id);
	        sURIMatcher.addURI(Authorities, "facebooksubeventtype",  facebooksubeventtype_id);
	        sURIMatcher.addURI(Authorities, "facebookusers",         facebookusers_id);  
	        sURIMatcher.addURI(Authorities,"facebookusers/#/#",      facebookusers_limit_id);
	        sURIMatcher.addURI(Authorities, "extpermission",         extpermission_id); 
	        sURIMatcher.addURI(Authorities, "facebookevent",         facebookevent_id);
	        sURIMatcher.addURI(Authorities, "mailthread",            mailThread_id);
	        sURIMatcher.addURI(Authorities, "mailmessage",           mailMessage_id);
	        sURIMatcher.addURI(Authorities, "peoplemapfacebook",     peoplemapfacebook_id);
	        sURIMatcher.addURI(Authorities, "walldraft",             walldraft_id);
	        sURIMatcher.addURI(Authorities, "album",                album_id);
	        sURIMatcher.addURI(Authorities, "photo",                photo_id);
	        sURIMatcher.addURI(Authorities, "page",                 page_id);
	        sURIMatcher.addURI(Authorities, "note",                 note_id);
	        
	    }
	    
	    
	    private static class DatabaseHelper extends SQLiteOpenHelper 
	    {
	        private Context mContext;
	        public DatabaseHelper(Context context) 
	        {
	            super(context, sDatabaseName, null, DATABASE_VERSION);
	            mContext = context;
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db) {
	            
	            db.beginTransaction();
	            try{
    	            //for Facebook, twitter account information
    	            db.execSQL("CREATE TABLE account (" +
    	                    "_id INTEGER PRIMARY KEY," +
    	                    "email  TEXT," +
    	                    "password TEXT," +
    	                    "type  INTEGER DEFAULT 0" +//0, Facebook, 1, twitter
    	                    ");");
    	            
    	            db.execSQL("CREATE TABLE settings (" +
    	                    "_id INTEGER PRIMARY KEY," +
    	                    "name  TEXT," +
    	                    "value TEXT" +
    	                    ");");
    	            
    	            db.execSQL("CREATE TABLE trends (" +
    	                    "_id INTEGER PRIMARY KEY," +
    	                    "name  TEXT," +
    	                    "url   TEXT," +
    	                    "date  INTEGER "+
    	                    ");");
    	            
    	            db.execSQL("CREATE TABLE follow (" +
    	                    "_id INTEGER PRIMARY KEY," +
    	                    "uid    INTEGER, "+
    	                    "name   TEXT," +
    	                    "sname  TEXT," +
    	                    "profileImgUrl   TEXT," +	   
    	                    "type  INTEGER DEFAULT 0" +//0, follower, 1, following
    	                    ");");
    	            
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"trends_timeout\", \"30000\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"tweet_view_count\", \"10\");");	          
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"follow_view_count\", \"20\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"f_friend_view_count\", \"20\");");	            
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"trend_view_timeout\", \"60\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"tweet_view_timeout\", \"60\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"stream_view_timeout\", \"120\");");	
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"issnsenable\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"enablesyncphonebook\", \"0\");");
    	            
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"copytoemail\", \"0\");");	
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"copynewmessagetosms\", \"0\");");	            
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"usepermanentsession\", \"0\");");	            
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"contact_update_period\", \"3\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"friend_update_period\", \"3\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_mail_check_period\", \"6\");");	            
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_show_on_homescreen\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"twitter_use_https_connection\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_use_https_connection\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_use_logo\", \"1\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_use_email\", \"1\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_use_phonenumber\", \"1\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_use_birthday\", \"1\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"twitter_show_on_homescrren\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_sync_birthday_event\", \"1\");");	 
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_upload_photo_size\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"twitter_upload_photo_size\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_icon_size\", \"2\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"facebook_addressbook_sync_period\", \"7\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"isalwayspromptsyncdialog\", \"0\");");
    	            db.execSQL("INSERT INTO settings (name, value) VALUES(\"enableassignpermission\", \"1\");");
    	            
    	            //add by jessie 09-04-22
    	            /************event category and sub-category**********/
    	            db.execSQL("CREATE TABLE facebookeventtype (" +
    	            		"eventtype INTEGER," +
    	            		"eventname TEXT" +
    	            		");");
    	            
    	            db.execSQL("CREATE TABLE facebooksubeventtype (" +
    	            		"eventtype INTEGER," +
    	            		"subeventtype INTEGER," +
    	            		"subeventname TEXT"+
    	            		");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (1, \"Party\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (2, \"Causes\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (3, \"Education\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (4, \"Mettings\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (5, \"Music/Arts\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (6, \"Sports\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (7, \"Trips\");");
    	            db.execSQL("INSERT INTO facebookeventtype (eventtype, eventname) VALUES (8, \"Other\");");
    	            
    
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,1, \"Birthday Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,2, \"Cocktail Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,3, \"Club Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,5, \"Fraternity/Sorority Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,8, \"Card Night\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,9, \"Dinner Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,10, \"Holiday Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,11, \"Night of Mayhem\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,12, \"Movie/TV Night\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,13, \"Drinking Games\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,14, \"Bar Night\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,15, \"LAN Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,17, \"Mixer\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,18, \"Slumber Party\");"); 
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,19, \"Erotic Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,20, \"Benefit\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,21, \"Goodbye Party\");"); 
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,22, \"House Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (1,23, \"Reunion\");");
    	            
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (2,24, \"Fundraiser\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (2,25, \"Protest\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (2,26, \"Rally\");");
    	   
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (3,16, \"Study Group\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (3,27, \"Class\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (3,28, \"Lecture\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (3,29, \"Office Hours\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (3,30, \"Workshop\");");
    	                   
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (4,6, \"Business Meeting\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (4,31, \"Club/Group Meeting\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (4,32, \"Convention\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (4,33, \"Dorm/House Meeting\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (4,34, \"Informational Meeting\");");
    	            
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,4, \"Concert\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,35, \"Audition\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,36, \"Exhibit\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,37, \"Jam Session\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,38, \"Listening Party\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,39, \"Opening\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,40, \"Performance\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,41, \"Preview\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,42, \"Recital\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (5,43, \"Rehearsal\");");
    	            
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (6,44, \"Pep Rally\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (6,45, \"Pick-Up\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (6,46, \"Sporting Event\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (6,47, \"Sports Practice\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (6,48, \"Tournament\");");
    	      
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (7,49, \"Camping Trip\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (7,50, \"Daytrip\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (7,51, \"Group Trip\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (7,52, \"Roadtrip\");");
    	            
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (8,53, \"Carnival\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (8,54, \"Ceremony\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (8,55, \"Festival\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (8,56, \"Flea Market\");");
    	            db.execSQL("INSERT INTO facebooksubeventtype (eventtype,subeventtype,subeventname) VALUES (8,57, \"Retail\");"); 
    	            
    	            /**************facebookfriends**********************/
    	            //
    	            // uid_2 is uid_1's friends
    	            //
    	            db.execSQL("CREATE TABLE facebookfriends (" +
    	                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	                    "uid_1  INTEGER," +
    	                    "uid_2  INTEGER"  +	                    
    	                    ");");
    	            
    	            /**************************************************/
    	            db.execSQL("CREATE TABLE phonebook (" +
    	                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	                    "uid INTEGER,"+
    	                    "name   TEXT," +
    	                    "email  TEXT," +
    	                    "cell   TEXT," +
    	                    "phone  TEXT," +
    	                    "screenname TEXT," +
    	                    "address TEXT," +
    	                    "street TEXT," +
    	                    "state TEXT," +
    	                    "city TEXT," +
    	                    "country TEXT," +
    	                    "zip TEXT," +
    	                    "latitude TEXT," +
    	                    "longitude TEXT," +
    	                    "synced INTEGER DEFAULT 0,"+
    	                    "peopleid INTEGER DEFAULT 0, "+
    	                    "UNIQUE(uid) "+
    	                    ");");
    	            
    	            /**************************************************/
    	            
    	            db.execSQL("CREATE TABLE page (" +
    	                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	                    "pageid          INTEGER,"+
    	                    "name             TEXT," +
    	                    "page_url         TEXT," +
    	                    "pic_small        TEXT," +
    	                    "pic_big          TEXT," +
    	                    "pic_square       TEXT," +
    	                    "pic              TEXT," +
    	                    "pic_large        TEXT," +
    	                    "type             TEXT," +
    	                    "website          TEXT," +
    	                    "company_overview TEXT," +
    	                    "isshotcut        INTEGER  NOT NULL DEFAULT 0, "+
    	    	             "UNIQUE(pageid) "+
    	                    ");");
    	            
    	            db.execSQL("CREATE TABLE peoplemapfacebook (" +
    	                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	                    "uid        INTEGER,"+
    	                    "peopleid   TEXT," +	                    
    	                    "requested INTEGER DEFAULT 0"+//0, not request, 1, request	                    
    	                    ");");
    	            
    	            /*********facebookcontacts*********/
    	            db.execSQL("CREATE TABLE facebookusers (" +
    	                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	                    "uid    INTEGER,"  +	                    
    	                    "first_name TEXT," +
    	                    "last_name TEXT," +
    	                    "name TEXT," +
    	                    "pic_square TEXT,"+
    	                    "pic TEXT,"+
    	                    "pic_small TEXT,"+
    	                    "birthday TEXT," +
    	                    "b_month  INTEGER,"+
    	                    "b_date   INTEGER,"+
    	                    "sex TEXT ," +
    	                    "message TEXT,"+
    	                    "status_id TEXT," +
    	                    "status_time TEXT," +
    	                    "event_sync INTEGER,"+
    	                    "event_last_synctime INTEGER,"+
    	                    "c_event_id INTEGER,"+
    	                    "isshotcut  INTEGER  NOT NULL DEFAULT 0, "+
    	                    " UNIQUE (uid) "+
    	                    ");");
    	            
    	            db.execSQL("CREATE TABLE extpermission (" +
    	            	       "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	            	       "methodname TEXT NOT NULL," +
    	            	       "permission INTEGER NOT NULL DEFAULT 0," +
    	            	       " UNIQUE (methodname)" +
    	            		   ");");
    	          //init extpermission
    	           String[] permissions = {"status_update","photo_upload","email","create_event","rsvp_event","read_stream","publish_stream","share_item"}; 
    	           
    	           for(int i = 0 ;i<permissions.length;i++) {
    	              db.execSQL("INSERT INTO extpermission (methodname,permission) VALUES (\""+permissions[i]+"\",0);"); 
    	           }  
    	           
    	           
    	           /****************facebookevent************************/
    	           db.execSQL("CREATE TABLE facebookevent (" +
    	        		       "_id  INTEGER PRIMARY KEY AUTOINCREMENT," +
    	        		       "eid  INTEGER NOT NULL," +
    	        		       "name TEXT NOT NULL," +
    	        		       "tagline TEXT," +
    	        		       "nid INTEGER," +
    	        		       "pic_small TEXT," +
    	        		       "pic       TEXT," +
    	        		       "pic_big   TEXT," +
    	        		       "host TEXT NOT NULL," +
    	        		       "description TEXT," +
    	        		       "event_type TEXT," +
    	        		       "event_subtype TEXT," +
    	        		       "starttime INTEGER NOT NULL," +
    	        		       "endtime INTEGER NOT NULL," +
    	        		       "creator INTEGER," +
    	        		       "location TEXT," +
    	        		       "venue TEXT," +
    	        		       "rsvp_status TEXT,"+
    	        		       "synced INTEGER DEFAULT 0,"+
    	        		       "c_event_id INTEGER, "+
    	        		       "UNIQUE(eid)" +
    	           		       ");");
    	           
    	           db.execSQL("CREATE TABLE mailthread (" +
    	        		   "_id  INTEGER PRIMARY KEY AUTOINCREMENT,"+
    	        		    "thread_id  INTEGER NOT NULL,"          +
    	        		    "update_update_time INTEGER DEFAULT 0,"            +	        		    
    	        		    "outbox_update_time INTEGER DEFAULT 0,"           +
    	        		    "inbox_update_time  INTEGER DEFAULT 0,"           +	        		    
    	        		    "recipients   TEXT,"                     +
    	        		    "parent_thread_id INTEGER,"             +
    	        		    "parent_message_id TEXT,  "             +
    	        		    "message_count     INTEGER DEFAULT 0,"  +
    	        		    "subject  TEXT,"                     +
    	        		    "snippet     TEXT,"                  +
    	        		    "snippet_author INTEGER,"            +
    	        		    "object_id INTEGER, "                   +
    	        		    "unread  INTEGER DEFAULT 0,"     +
    	        		    "isinbox  INTEGER DEFAULT 0," +
    	        		    "isoutbox INTEGER DEFAULT 0" +
    	        		    "); " );
    	           
    	           db.execSQL("CREATE TABLE mailmessage (" +
    	        		   "_id  INTEGER PRIMARY KEY AUTOINCREMENT," +
    	        		    "threadid  INTEGER NOT NULL,"     +
    	        		    "mid       TEXT,"     +
    	        		    "author    INTEGER,"              +
    	        		    "timesent  INTEGER,"              +
    	        		    "body      TEXT   ,"              +
    	        		    "hasattachment INTEGER DEFAULT 0,"+
    	        		    "synced INTEGER DEFAULT 0"        +	        		    
    	        		    "); " );
    	           
    	           db.execSQL("CREATE TABLE walldraft (" +
    	        		      "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    	        		      "fuid INTEGER NOT NULL," +
    	        		      "content TEXT," +
    	        		      "contenttype TEXT," +
    	        		      "lastmodify INTEGER"+
    	           		     ");");
    	           
    	           db.execSQL("CREATE TABLE album (" +
                           "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                           "albumid TEXT NOT NULL," +
                           "coverpid TEXT,"+
                           "cover_src_url TEXT, "+
                           "owner INTEGER NOT NULL," +
                           "name  TEXT,"+
                           "createdtime INTEGER," +
                           "modifiedtime INTEGER," +
                           "description TEXT," +
                           "location TEXT," +
                           "size INTEGER," +
                           "link TEXT," +
                           "visible TEXT,"+
                           "modified_major TEXT,"+
                           "UNIQUE(albumid)"+
                          ");");
    	           
    	           db.execSQL("CREATE TABLE photo (" +
                           "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                           "pid TEXT NOT NULL," +
                           "aid TEXT,"+                       
                           "owner INTEGER NOT NULL," +
                           "src_small  TEXT,"+
                           "src_big    TEXT," +
                           "src        TEXT," +
                           "link       TEXT," +
                           "caption    TEXT," +
                           "created    INTEGER );");
    	           
    	           db.execSQL("INSERT INTO album (albumid,coverpid,owner,name)" +
                   " VALUES(-2,1,100171413,'Default');");
    	           
    	           db.execSQL("CREATE TABLE note (" +
                           "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                           "note_id       INTEGER NOT NULL," +
                           "title         TEXT,"+
                           "content       TEXT,"+
                           "uid           INTEGER NOT NULL," +
                           "created_time  INTEGER," +
                           "updated_time  INTEGER" +
                           ");");
	               db.setTransactionSuccessful();
	            }
	            catch(Exception e)
	            {
	                Log.d(TAG,"onCreate database exception "+e.getMessage());
	            }
	            finally
	            {
	                db.endTransaction();
	            }
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                    + newVersion + ", which will destroy all old data");
	            db.beginTransaction();
	            try{
	                db.execSQL("DROP TABLE IF EXISTS account");
	                db.execSQL("DROP TABLE IF EXISTS settings");
	                db.execSQL("DROP TABLE IF EXISTS trends");              
	                db.execSQL("DROP TABLE IF EXISTS follow");  
	                
	                db.execSQL("DROP TABLE IF EXISTS facebookfriends");
	                db.execSQL("DROP TABLE IF EXISTS phonebook");
	                db.execSQL("DROP TABLE IF EXISTS facebookusers");
	                db.execSQL("DROP TABLE IF EXISTS facebookeventtype");
	                db.execSQL("DROP TABLE IF EXISTS facebooksubeventtype");
	                db.execSQL("DROP TABLE IF EXISTS extpermission");
	                db.execSQL("DROP TABLE IF EXISTS facebookevent");
	                db.execSQL("DROP TABLE IF EXISTS mailthread");
	                db.execSQL("DROP TABLE IF EXISTS mailmessage");
	                db.execSQL("DROP TABLE IF EXISTS peoplemapfacebook");
	                
	                db.execSQL("DROP TABLE IF EXISTS walldraft");
	                db.execSQL("DROP TABLE IF EXISTS album");
	                db.execSQL("DROP TABLE IF EXISTS photo");               
	                db.execSQL("DROP TABLE IF EXISTS page");
	                db.execSQL("DROP TABLE IF EXISTS note");
	                onCreate(db);
	                 
	                db.setTransactionSuccessful();
	            }
	            catch(Exception e)
	            {
	                Log.d(TAG, "onUpgrade drop table exception "+e.getMessage());
	            }
	            finally
	            {
	                db.endTransaction();
	            }
	            
	            
			}
	    }
	    @Override
		public boolean onCreate() {
			 dbHelper = new DatabaseHelper(getContext());
		     return (dbHelper == null) ? false : true;
		}
		
	    public static void resetDatabase(Context context)
	    {
	    	if(dbHelper == null) return;
	    	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
	    	
	    	mDB.beginTransaction();
	    	try{
	    	    Log.w(TAG, "reset Database ");
	            mDB.execSQL("DROP TABLE IF EXISTS account");
	            mDB.execSQL("DROP TABLE IF EXISTS settings");
	            mDB.execSQL("DROP TABLE IF EXISTS trends"); 
	            mDB.execSQL("DROP TABLE IF EXISTS follow"); 
	            
	            mDB.execSQL("DROP TABLE IF EXISTS facebookfriends");
	            mDB.execSQL("DROP TABLE IF EXISTS phonebook");
	            mDB.execSQL("DROP TABLE IF EXISTS facebookusers");
	            mDB.execSQL("DROP TABLE IF EXISTS facebookeventtype");
	            mDB.execSQL("DROP TABLE IF EXISTS facebooksubeventtype");
	            mDB.execSQL("DROP TABLE IF EXISTS extpermission");
	            mDB.execSQL("DROP TABLE IF EXISTS facebookevent");
	            mDB.execSQL("DROP TABLE IF EXISTS mailthread");
	            mDB.execSQL("DROP TABLE IF EXISTS mailmessage");
	            mDB.execSQL("DROP TABLE IF EXISTS peoplemapfacebook");
	            mDB.execSQL("DROP TABLE IF EXISTS walldraft");
	            mDB.execSQL("DROP TABLE IF EXISTS album");
	            mDB.execSQL("DROP TABLE IF EXISTS photo");
	            mDB.execSQL("DROP TABLE IF EXISTS page");
	            mDB.execSQL("DROP TABLE IF EXISTS note");
	            dbHelper.onCreate(mDB); 
	            mDB.setTransactionSuccessful();
	    	}
	    	catch(Exception e)
	    	{
	    	    Log.d(TAG,"resetDatabase exception "+e.getMessage());
	    	}
	    	finally
	    	{
	    	    mDB.endTransaction();
	    	    //mDB.close();
	    	}
	        
	    }
	    
	    
		@Override
		public int delete(Uri uri, String queryString, String[] selectionArgs)
		{
			int count = 0;
			SQLiteDatabase mDB = dbHelper.getWritableDatabase();        
	        int match = sURIMatcher.match(uri);
	        if(match == facebookusers_limit_id)
	        {
	           List<String> pathSeg = uri.getPathSegments();
	           int size = pathSeg.size();
	           if(size>2)
	           {
	               int offset = size-1;
	               int limit  = size-2;
	               String limit_str = pathSeg.get(limit);
	               String offset_str = pathSeg.get(offset);
	               Log.d(TAG,"entering facebookusers_limit pathseg size ="+size+" offset size="+offset+"limit size = "+limit);
	               String sql = "delete from facebookusers where _id in (select _id facebookusers  where uid not in "+
	                             "(select uid from facebookfriends) limit "+limit_str+","+offset_str+")";
	               Log.d(TAG,"delete sql is "+sql);
	               mDB.execSQL(sql); 
	           }  
	           
	        }
	        if(match == account_id)
	        {
	           count = mDB.delete("account", queryString, selectionArgs);
	           
	           // Notify any listeners and return the URI of the new row.
	           getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/account"), null);
	        }
	        else if(match == settings_id)
	        {
	            count = mDB.delete("settings", queryString, selectionArgs);
	        }
	        else if(match == trends_id)
	        {
	            count = mDB.delete("trends", queryString, selectionArgs);
	        }
	        else if(match == follow_id)
	        {
	            count = mDB.delete("follow", queryString, selectionArgs);
	        }	        
	        else if(match == facebookcontacts_id){
	        	count = mDB.delete("phonebook", queryString, selectionArgs);
	        }
	        else if(match == facebookusers_id){
	        	count = mDB.delete("facebookusers", queryString, selectionArgs);
	        }
	        else if(match == facebookfriends_id){
	        	count = mDB.delete("facebookfriends", queryString, selectionArgs);
	        }
	        else if(match == facebookeventtype_id){
	        	count = mDB.delete("facebookeventtype", queryString, selectionArgs);
	        }
	        else if(match == facebooksubeventtype_id)
	        {
	        	count = mDB.delete("facebooksubeventtype", queryString, selectionArgs);
	        }
	        else if(match == extpermission_id){
	        	count = mDB.delete("extpermission", queryString, selectionArgs);
	        }
	        else if(match == facebookevent_id){
	        	count = mDB.delete("facebookevent", queryString, selectionArgs);
	        }
	        else if(match == mailThread_id){
	        	count = mDB.delete("mailthread", queryString, selectionArgs);
	        }
	        else if(match == mailMessage_id){
	        	count = mDB.delete("mailmessage", queryString, selectionArgs);
	        }
	        else if(match == peoplemapfacebook_id)
	        {
	        	count = mDB.delete("peoplemapfacebook", queryString, selectionArgs);
	        }
	        else if(match == walldraft_id)
	        {
	        	count = mDB.delete("walldraft", queryString, selectionArgs);
	        }
	        else if(match == album_id)
            {
                count = mDB.delete("album", queryString, selectionArgs);
            }
	        else if(match == photo_id)
            {
                count = mDB.delete("photo", queryString, selectionArgs);
            }
	        else if(match == page_id)
            {
                count = mDB.delete("page", queryString, selectionArgs);
            }
	        else if(match == note_id)
            {
                count = mDB.delete("note", queryString, selectionArgs);
            }
			return count;
		}
	    @Override
	    public String getType(Uri uri) 
	    {
	        return "vnd.android.cursor.dir/vnd.sns";
	    }
		@Override
		public Uri insert(Uri uri, ContentValues values) 
		{
			Uri ret = null;
	        SQLiteDatabase mDB = dbHelper.getWritableDatabase();
	        long rowID = -1;
	        int match = sURIMatcher.match(uri);
	        if(match == account_id)
	        {   
	            rowID = mDB.insert("account", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/account/" + rowID);
	            }
	        }
	        else if(match == settings_id)
	        {
	            rowID = mDB.insert("settings", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/settings/" + rowID);
	            }
	        }	        
	        else if(match == trends_id)
	        {
	            rowID = mDB.insert("trends", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/trends/" + rowID);
	            }
	        }
	        else if(match == follow_id)
	        {
	            rowID = mDB.insert("follow", null, values);
	            if (rowID != -1) {	            	
	                ret = Uri.parse(CONTENT_URI + "/follow/" + rowID);
	                getContext().getContentResolver().notifyChange(ret, null);
	            }
	        }
	        else if(match == facebookcontacts_id){
	        	rowID = mDB.insert("phonebook", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/phonebook/" + rowID);
	            }
	        }
	        else if(match == facebookusers_id){
	        	rowID = mDB.insert("facebookusers", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/facebookusers/" + rowID);
	                getContext().getContentResolver().notifyChange( Uri.parse(CONTENT_URI + "/facebookusers/"), null);
	            }
	        }
	        else if(match == facebookfriends_id){
	        	rowID = mDB.insert("facebookfriends", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/facebookfriends/" + rowID);
	            }
	        }
	        else if(match == facebookeventtype_id){
	        	rowID = mDB.insert("facebookeventtype", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/facebookeventtype/" + rowID);
	            }
	        }
	        else if(match == facebooksubeventtype_id){
	        	rowID = mDB.insert("facebooksubeventtype", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/facebooksubeventtype/" + rowID);
	            }
	        }
	        else if(match == extpermission_id){
	        	rowID = mDB.insert("extpermission", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/extpermission/" + rowID);
	            }
	        }
	        else if(match == facebookevent_id){
	        	rowID = mDB.insert("facebookevent", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/facebookevent/" + rowID);
	            }
	        }	
	        
	        else if(match == mailThread_id){
	        	rowID = mDB.insert("mailthread", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/mailthread/" + rowID);
	            }
	        }
	        else if(match == mailMessage_id){
	        	rowID = mDB.insert("mailmessage", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/mailmessage/" + rowID);
	            }
	        }
	        else if(match == peoplemapfacebook_id){
	        	rowID = mDB.insert("peoplemapfacebook", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/peoplemapfacebook/" + rowID);
	            }
	        }
	        else if(match == walldraft_id){
	        	rowID = mDB.insert("walldraft", null, values);
	            if (rowID != -1) {
	                ret = Uri.parse(CONTENT_URI + "/walldraft/" + rowID);
	            }
	        }
	        else if(match == album_id){
                rowID = mDB.insert("album", null, values);
                if (rowID != -1) {
                    ret = Uri.parse(CONTENT_URI + "/album/" + rowID);
                }
            }
	        else if(match == photo_id){
                rowID = mDB.insert("photo", null, values);
                if (rowID != -1) {
                    ret = Uri.parse(CONTENT_URI + "/photo/" + rowID);
                }
            }
	        else if(match == page_id){
                rowID = mDB.insert("page", null, values);
                if (rowID != -1) {
                    ret = Uri.parse(CONTENT_URI + "/page/" + rowID);
                    getContext().getContentResolver().notifyChange( Uri.parse(CONTENT_URI + "/page/"), null);
                }
            }
	        else if(match == note_id){
                rowID = mDB.insert("note", null, values);
                if (rowID != -1) {
                    ret = Uri.parse(CONTENT_URI + "/note/" + rowID);
                }
            }
			return ret;
		}	
		
		@Override
		public Cursor query(Uri uri, String[] projection, String queryString,
	            String[] selectionArgs, String sortOrder) {
			Cursor cursor=null;
	        //Log.w(TAG,"query query="+queryString);
	        
	        SQLiteDatabase mDB = dbHelper.getReadableDatabase();	        
	        SQLiteQueryBuilder q = new SQLiteQueryBuilder();
	        int match = sURIMatcher.match(uri);
	        
	        if(match == facebookusers_limit_id)
            {
	           Log.d(TAG,"entering query facebookusers_limit_id");
               List<String> pathSeg = uri.getPathSegments();
               int size = pathSeg.size();
               if(size>2)
               {
                   int offset = size-1;
                   int limit  = size-2;
                   String limit_str = pathSeg.get(limit);
                   String offset_str = pathSeg.get(offset);
                   Log.d(TAG,"entering query facebookusers_limit pathseg size ="+size+" offset size="+offset+"limit size = "+limit);
                   String sql = "select uid  from facebookusers  where uid not in "+
                                 "(select uid from facebookfriends) limit "+limit_str+","+offset_str;
                   Log.d(TAG,"sql is "+sql);
                   cursor = mDB.rawQuery(sql, null); 
               }  
               
            }
	        if(match == account_id)
	        {   
	            q.setTables("account");            
	            cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == settings_id)
	        {            
	            q.setTables("settings");            
	            cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == trends_id)
	        {            
	            q.setTables("trends");            
	            cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == follow_id)
	        {            
	            q.setTables("follow");            
	            cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }	        
	        else if(match == facebookcontacts_id){
	        	 q.setTables("phonebook");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == facebookusers_id){
	        	 q.setTables("facebookusers");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == facebookfriends_id){
	        	 q.setTables("facebookfriends");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == facebookeventtype_id){
	        	 q.setTables("facebookeventtype");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == facebooksubeventtype_id){
	        	 q.setTables("facebooksubeventtype");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match == extpermission_id){
	        	q.setTables("extpermission");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }	
	        else if(match ==facebookevent_id){
	        	q.setTables("facebookevent");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }	
	        else if(match ==mailThread_id){
	        	q.setTables("mailthread");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match ==mailMessage_id){
                 q.setTables("mailmessage");            
 		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }
	        else if(match ==peoplemapfacebook_id){
                q.setTables("peoplemapfacebook");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }	
	        else if(match ==walldraft_id){
                q.setTables("walldraft");            
		         cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
	        }	
	        else if(match ==album_id){
                q.setTables("album");            
                 cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
            } 
	        else if(match ==photo_id){
                q.setTables("photo");            
                 cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
            } 
	        else if(match ==page_id){
                q.setTables("page");            
                 cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
            } 
	        else if(match ==note_id){
                q.setTables("note");            
                 cursor = q.query(mDB, projection, queryString, selectionArgs, null, null, sortOrder);
            } 
			return cursor;
		}
		@Override
		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
		{   
	        String myWhere;
	        int count = 0;
	        int match = sURIMatcher.match(uri);
	        if(match == account_id)
	        {
	            if(selection != null)
	            {
	                myWhere = selection ;
	            }
	            else
	            {
	                myWhere = " _id=1 ";
	            }

	            SQLiteDatabase mDB = dbHelper.getWritableDatabase();
	            count = mDB.update("account", values, myWhere, selectionArgs);
	            Log.d(TAG, "update count="+count);
	            //Notify any listeners and return the URI of the new row.
	            getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/account"), null);
	        }
	        else if(match == settings_id)
	        {
	            SQLiteDatabase mDB = dbHelper.getWritableDatabase();
	            count = mDB.update("settings", values, selection, selectionArgs);
	            
	            //Notify any listeners and return the URI of the new row.
	            getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/settings"), null);
	        }
	        else if(match == trends_id)
	        {
	            SQLiteDatabase mDB = dbHelper.getWritableDatabase();
	            count = mDB.update("trends", values, selection, selectionArgs);
	            
	            //Notify any listeners and return the URI of the new row.
	            getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/trends"), null);
	        }
	        else if(match == follow_id)
	        {
	            SQLiteDatabase mDB = dbHelper.getWritableDatabase();
	            count = mDB.update("follow", values, selection, selectionArgs);
	            
	            //Notify any listeners and return the URI of the new row.
	            getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/follow"), null);
	        }	        
	        else if(match == facebookcontacts_id){         
	        	 SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		         count = mDB.update("phonebook", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/facebookcontacts"), null);
	        }
	        else if(match == facebookusers_id){        
	        	 SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		         count = mDB.update("facebookusers", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/facebookusers"), null);
	        }
	        else if(match == facebookfriends_id){         
	        	 SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		         count = mDB.update("facebookfriends", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/facebookfriends"), null);
	        }
	        else if(match == facebookeventtype_id){
	        	
	        	 SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		         count = mDB.update("facebookeventtype", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/facebookeventtype"), null);
	        }
	        else if(match == facebooksubeventtype_id){        
	        	 SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		         count = mDB.update("facebooksubeventtype", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/facebooksubeventtype"), null);
	        }
	        else if(match == extpermission_id){
	        	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		        count = mDB.update("extpermission", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/extpermission"), null);
	        }
	        else if(match == facebookevent_id){
	        	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		        count = mDB.update("facebookevent", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/facebookevent"), null);
	        }	
	        else if(match == mailThread_id){
	        	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		        count = mDB.update("mailthread", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/mailthread"), null);
	        }	
	        else if(match == mailMessage_id){
	        	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		        count = mDB.update("mailmessage", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/mailmessage"), null);
	        }	
	        else if(match == peoplemapfacebook_id){
	        	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		        count = mDB.update("peoplemapfacebook", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/peoplemapfacebook"), null);
	        }	
	        else if(match == walldraft_id){
	        	SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		        count = mDB.update("walldraft", values, selection, selectionArgs);
		            
		         //Notify any listeners and return the URI of the new row.
		         getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/walldraft"), null);
	        }	
	        else if(match == album_id){
                SQLiteDatabase mDB = dbHelper.getWritableDatabase();
                count = mDB.update("album", values, selection, selectionArgs);
                    
                 //Notify any listeners and return the URI of the new row.
                 getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/album"), null);
            }
	        else if(match == photo_id){
                SQLiteDatabase mDB = dbHelper.getWritableDatabase();
                count = mDB.update("photo", values, selection, selectionArgs);
                    
                 //Notify any listeners and return the URI of the new row.
                 getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/photo"), null);
            }
	        else if(match == page_id){
                SQLiteDatabase mDB = dbHelper.getWritableDatabase();
                count = mDB.update("page", values, selection, selectionArgs);
                    
                 //Notify any listeners and return the URI of the new row.
                 getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/page"), null);
            }
	        else if(match == note_id){
                SQLiteDatabase mDB = dbHelper.getWritableDatabase();
                count = mDB.update("note", values, selection, selectionArgs);
                    
                 //Notify any listeners and return the URI of the new row.
                 getContext().getContentResolver().notifyChange(Uri.parse(CONTENT_URI + "/note"), null);
            }
			return count;
		}
}
