package com.msocial.nofree.service;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.FacebookSyncActivity;
import com.msocial.nofree.ui.lisenter.SyncLisenter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.client.FacebookMethod.Phonebook;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.PhoneBook;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Contacts;
import android.util.Log;

public class FacebookSyncHelper {
	 private static Context   mContext;
	 private static SocialORM orm;
	 private static FacebookSyncHelper _instance;
	 private static final String TAG = "FacebookSynHelper";
	 private static ContactSyncThread contactthread;
	 private static EventSyncThread eventthread;
	 private static HashMap contactSyncStatus = new HashMap();
     private static HashMap eventSyncStatus = new HashMap();
     final long SYNC_INTERVAL_TIME = 60*1000L;
     
     private static NotificationManager mNM;
     
     public final static int CALL_FROM_SERVICE = 1;
     public final static int CALL_FROM_ACTIVITY = 2;
	 final int FACEBOOK_CONTACT_SYNCING = 1;
     public static SyncLisenter synclisenter;
	
     
     public void hiddenNotification(){
    	 
     }
     
	 private FacebookSyncHelper(Context con) 
	 {
			mContext = con;
			orm = new SocialORM(mContext);
	 }
	 public static FacebookSyncHelper instance(Context con)
	 {
	    if(_instance == null){
	    		_instance = new FacebookSyncHelper(con);
	    }
	    
	    //re-assign the context
	    mContext = null;
	    mContext = con;
	    	
	    orm=null;
		orm = new SocialORM(mContext);
	
		mNM = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
	    return _instance;
	  }
	 
	 public void isFacebookUser(FacebookSession session,long peopleid){
		
		/* HashMap<String,Long> phonenumbers = getContactPhones(peopleid);
		 HashMap<String,Long> emails = getContactEmails(peopleid);
		 
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		 Iterator it = emails.entrySet().iterator();
	     while(it.hasNext()){    	  
	    	   Map.Entry<String, Long> para = (Map.Entry<String, Long>)it.next();
	    	   String email = para.getKey();
	    	   sb.append("{\"email\":\""+email+"\"},");

	      }
		
	     it = phonenumbers.entrySet().iterator();
	     while(it.hasNext()){    	  
	    	   Map.Entry<String, Long> para = (Map.Entry<String, Long>)it.next();
	    	   String mobile = para.getKey();
	    	   sb.append("{\"cell\":\""+mobile+"\"},");

	      }
		
		   
		String json_entries = sb.toString();
		if(json_entries.endsWith(",")){
			json_entries = json_entries.substring(0,json_entries.length()-1);
		}
		    json_entries = json_entries+"]";
		   
		    Log.d(TAG,"json_entries =="+json_entries);
		  
		try {
		   List<PhoneBook> phonebooks = session.phoneBookLookup(json_entries, null);
		   if(phonebooks!=null){
			  
			  for(PhoneBook phonebook : phonebooks){
				  if(phonebook.cell!=null){
					//TODO call interface from liaoxuwang;  
					  Uri uri = ContentUris.withAppendedId(Contacts.Phones.CONTENT_URI,((Long)phonenumbers.get(phonebook.cell)).longValue());
					  Contacts.People.updateFacebookTag(mContext, uri,Contacts.Phones.FACEBOOK, 0);
				  }
				  if(phonebook.email!=null){
					//TODO call interface from liaoxuwang
					Uri uri = ContentUris.withAppendedId(Contacts.ContactMethods.CONTENT_URI,((Long)phonenumbers.get(phonebook.email)).longValue());
					Contacts.People.updateFacebookTag(mContext, uri,Contacts.ContactMethods.FACEBOOK,0);
				  }
				  
				  Uri uri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
				  Contacts.People.updateFacebookTag(mContext, uri,Contacts.People.FACEBOOK, 1);
			  }
		   }
		} catch (FacebookException e) {
             Log.d(TAG, " add /edition is facebookuser interface exception "+e.getErrorCode()+"=="+e.getMessage());
		}*/
			 
	 }
	 
	
	 /**
	  * get Phone numbers by peopleid
	  * @param peopleid
	  * @return
	  */
	/*public HashMap<String,Long> getContactPhones(long peopleid){
	 HashMap<String,Long> phonenumbers = new HashMap<String,Long>();
	 
	 String[] PHONES_PROJECTION = new String[] {
            People.Phones._ID, // 0
            People.Phones.NUMBER, // 1
       };
     int PHONES_ID_COLUMN = 0;
     int PHONES_NUMBER_COLUMN = 1;

    Uri mUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, peopleId);
    
    // Build up the phone entries
    Uri phonesUri = Uri.withAppendedPath(mUri, People.Phones.CONTENT_DIRECTORY);
    Cursor phonesCursor = mResolver.query(phonesUri, PHONES_PROJECTION, null, null,null);

      if (phonesCursor != null) {
          while (phonesCursor.moveToNext()) {
               String number = phonesCursor.getString(PHONES_NUMBER_COLUMN);
               long id = phonesCursor.getLong(PHONES_ID_COLUMN);
                // Don't crash if the number is bogus
                if (TextUtils.isEmpty(number)) {
                	Log.d(TAG, "empty number for phone " + id);
                    continue;
                }else{
                	phonenumbers.put(number,new Long(id));
                }
            }
            phonesCursor.close();
      }
      
      return phonenumbers;
    
   }*/
	
	/**
	 * get Emails by Peopleid
	 * @param peopleid
	 * @return
	 */
	/*public HashMap<String,Long> getContactEmails(long peopleid){
		HashMap<String,Long> emails = new HashMap<String,Long>();
		String[] METHODS_PROJECTION = new String[] {
	            People.ContactMethods._ID, // 0
	            People.ContactMethods.KIND, // 1
	            People.ContactMethods.DATA, // 2
	    };
	    int METHODS_ID_COLUMN = 0;
	    int METHODS_KIND_COLUMN = 1;
	    int METHODS_DATA_COLUMN = 2;
	    
	    // Build the contact method entries
	    final Uri methodsUri = Uri.withAppendedPath(mUri, People.ContactMethods.CONTENT_DIRECTORY);
	    Cursor methodsCursor = mResolver.query(methodsUri, METHODS_PROJECTION, "kind = "+ Contacts.KIND_EMAIL, null,null);

	    if (methodsCursor != null) {
	         while (methodsCursor.moveToNext()) {
	              int kind = methodsCursor.getInt(METHODS_KIND_COLUMN);
	              String data = methodsCursor.getString(METHODS_DATA_COLUMN);
	              long id = methodsCursor.getLong(METHODS_ID_COLUMN);
	              
	              if(data!=null && !data.equals("")){
	            	  emails.put(data,new Long(id));
	              }
	          }
	           methodsCursor.close();
	   }
	    
	   return emails;	 
	   
	}*/
	 
	 public void addSyncLisenter(SyncLisenter synclisenter){
		 this.synclisenter = synclisenter;
	 }
	 
	 public void syncFacebookEvent(FacebookSession session,boolean forced,int caller){
		 Log.d(TAG, "entering sync facebook event ");	
		 
		 long lastsynctime = eventSyncStatus.get("lastsynctime")!=null?
	             ((Long)eventSyncStatus.get("lastsynctime")).longValue():0;
         long currenttime = new Date().getTime();

         // 0 normal finished  , 1 doing sync , 2 un-normal interrupt
         int syncstatus = eventSyncStatus.get("syncstatus")!=null?
	           ((Integer)eventSyncStatus.get("syncstatus")).intValue():0;

        if(syncstatus != 1){

          if(forced || currenttime-lastsynctime > SYNC_INTERVAL_TIME){ //fored sync event||over the sync interval time	    		    	      
        	 synchronized(this){
    			if(eventthread == null){
    			   eventthread = new EventSyncThread(session,caller);
    			}
    			Log.d(TAG,"=== sync eventthread is "+eventthread);
    			eventthread.start();
    			showSyncEventNotification();
    		 }
          }else{
        	  Log.d(TAG, "=== has synced in "+SYNC_INTERVAL_TIME);
        	  if(synclisenter!=null){
         		synclisenter.alertMsg("had synced in "+SYNC_INTERVAL_TIME/1000+" secs");
         	  } 
          }
       }else{
    	   Log.d(TAG, "== is doing syncstatus===");
    	   if(synclisenter!=null){
   			synclisenter.alertMsg("background is doing sync");
   		   } 
       }
        
	 }
	 
	 public void stopSyncFacebookEvent(){
		 Log.d(TAG, "entering stop Sync facebook event");
		 if(eventthread != null){	
			 Log.d(TAG, "==stop eventthread is ==="+eventthread);
				if(eventthread.isAlive())
			    {
					//eventthread.stop();	
					eventthread.interrupt();
					Log.d(TAG, "stoped sync facebook event");
					eventSyncStatus.put("syncstatus", 2);
				}else
				{	
				  Log.d(TAG, "eventthread is not alive ");
				}
				eventthread = null;
				
		 }else{
			 Log.d(TAG,"eventthread is null");
		 }
		 
		 cancelSyncEventNotification();
	 }
	 
	 
	 public void syncContact(FacebookSession session,boolean forced,int caller){
		//1.select syn setting (last time of synchornization)
	    //if(should syn)
	    //select contactinfo store in db
	    //asyn process contacts synchronization
	    //else return 		 
		 long lastsynctime = contactSyncStatus.get("lastsynctime")!=null?
	             ((Long)contactSyncStatus.get("lastsynctime")).longValue():0;
         long currenttime = new Date().getTime();

         // 0 normal finished  , 1 doing sync , 2 un-normal interrupt
         int syncstatus = contactSyncStatus.get("syncstatus")!=null?
	           ((Integer)contactSyncStatus.get("syncstatus")).intValue():0;

         if(syncstatus != 1){
        	 
            if(forced || currenttime-lastsynctime > SYNC_INTERVAL_TIME){ // fored sync contact || over the sync interval time
            	synchronized(this){
            		if(contactthread == null){
            			contactthread = new ContactSyncThread(session,caller);	    
            		}
            		
            		if(contactthread.isAlive() || !contactthread.isInterrupted()){
            		   contactthread.start();
            		   showSyncContactNotification();
            		         		   
            		}      
               }
            }else{
               if(synclisenter!=null){
         			synclisenter.alertMsg("had synced in "+SYNC_INTERVAL_TIME/1000+" secs");
         	   } 
            }
  
        }else{
    	   Log.d(TAG, "== is doing syncstatus===");
    	   if(synclisenter!=null){
   			 synclisenter.alertMsg("background is doing sync");
   		   } 
       }	
	 }
	 
	 public void stopSyncContact(){
		Log.d(TAG, "entering stop sync Contact");
		if(contactthread != null){
			Log.d(TAG, "==stop contactthread is ==="+contactthread);
			if(contactthread.isAlive())
		    {
				contactthread.interrupt();	
				Log.d(TAG, "stop sync contact");
				contactSyncStatus.put("syncstatus", 2);
			}
			else{	
				 Log.d(TAG, "contactthread is not alive ");
			}
			contactthread = null;
	   }
	   else{
			 Log.d(TAG,"contactthread is null");
		}
		
		cancelSyncContactNotification();
	 }
	 
	 public void showSyncContactNotification(){
		  String tickerText = "sync facebook contact";
		  	
	      Notification notification = new Notification(R.drawable.twitter_logo, tickerText,System.currentTimeMillis());
	      PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,new Intent(mContext, FacebookSyncActivity.class), 0);
	      notification.setLatestEventInfo(mContext,tickerText,"is syncing facebook contact,please wait..", contentIntent);
	      if(mNM!=null){
	          mNM.notify(R.string.facebook_contact_sync, notification);
	      }
	  
	 }
	 
	 public void cancelSyncContactNotification(){
		 if(mNM!=null){
			 Log.d(TAG, "cancel facebook contact notification");
			 mNM.cancel(R.string.facebook_contact_sync);
		 }
	 }
	 
	 public void showSyncEventNotification(){
		 String tickerText = "sync facebook event";
		  	
	      Notification notification = new Notification(R.drawable.twitter_logo, tickerText,System.currentTimeMillis());
	      PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,new Intent(mContext, FacebookSyncActivity.class), 0);
	      notification.setLatestEventInfo(mContext,tickerText,"is syncing facebook event,please wait..", contentIntent);
	      if(mNM!=null){
	          mNM.notify(R.string.facebook_event_sync, notification);
	      }
	 }
	 
	 public void cancelSyncEventNotification(){
		 if(mNM!=null){
			 Log.d(TAG, "cancel facebook Sync Event notification");
			 mNM.cancel(R.string.facebook_event_sync);
		 }
	 }
	 
	 
    public class ContactSyncThread extends Thread{
    	FacebookSession session;
    	int callfrom;
    	public ContactSyncThread(){}
    	
    	public ContactSyncThread(FacebookSession session,int caller){
    		this.session = session;
    		callfrom = caller;
    	}
    	
    	public void run(){
    	   //getPhonebook from facebook
    		contactSyncStatus.put("lastsynctime", new Date().getTime());
           try {
			 List<PhoneBook> phonebooks = new ArrayList<PhoneBook>();
			 if(callfrom == CALL_FROM_SERVICE ){
				 phonebooks = session.getContactInfo();
				 orm.addPhonebook(phonebooks);
			 }else{
				 phonebooks = orm.getPhonebooks();
			 }
			 			 
			 for(int i = 0;i<phonebooks.size();i++){
				//TODO call liaoxuwang sync Interface;
				Log.d(TAG, "doing sync contact "+i);
				orm.updateSyncTag(phonebooks.get(i).uid, true);	
				sleep(60*1000L);
				synclisenter.syncProgress((i+1)*100/phonebooks.size());
			 }			 
			 // change contactSyncStatus
			 contactSyncStatus.put("syncstatus", 0);
			 Log.d(TAG, "syncing facebook contact successfully");
		   }catch (Exception e) {
			 Log.d(TAG,"syncContact exception "+e.getMessage());
			 // change contactSyncStatus;	
			 contactSyncStatus.put("syncstatus", 2);
		   }
		   finally{
			   this.interrupt();
			   contactthread = null;
			   cancelSyncContactNotification();
		   }

    	}
    	
    	
	}
    
    
    public class EventSyncThread extends Thread{
    	FacebookSession session;
    	int callfrom;
    	public EventSyncThread(){}
    	
    	public EventSyncThread(FacebookSession session,int caller){
    		this.session = session;
    		callfrom = caller;
    	}
    	
    	public void run(){
    	   //getFacebookEvent from facebook
    		eventSyncStatus.put("lastsynctime", new Date().getTime());
           try {
			 List<Event> events = new ArrayList<Event>();
		     if(callfrom==CALL_FROM_SERVICE){
		        events = session.getUpcomingEvents();
			    orm.addFacebookevent(events);	
		     }else{
		    	events = orm.getUpcomingEvents();
		     }
			 
			 for(int i = 0;i<events.size();i++){
				//TODO call liaoxuwang sync Interface;
				Log.d(TAG, "doing sync event "+i);
				orm.updateFacebookeventSyncTag(events.get(i).eid, true,0);
				sleep(60*1000L);		
				synclisenter.syncProgress((i+1)*100/events.size());
			 }			 
			 // change eventSyncStatus
			 eventSyncStatus.put("syncstatus", 0);
			 
			 Log.d(TAG, "syncing event successfully");
		   }catch (Exception e) {
			 Log.d(TAG,"syncEvent exception "+e.getMessage());
			 eventSyncStatus.put("syncstatus", 2);
		   }
		   finally{
			   this.interrupt();
			   eventthread = null;
		   }

    	}
    	
    	
	}
	 
}
