package com.msocial.facebook.service.dell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.service.SNSService;
import com.msocial.facebook.ui.TwitterHelper;
import oms.sns.service.facebook.client.FacebookClient;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FriendRelationship;
import oms.sns.service.facebook.model.PhoneBook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.util.Log;
import org.apache.commons.codec.binary.Base64;

public class ContactHelper 
{
	final static String TAG="ContactHelper";	
	public final static int KIND_FACEBOOK       = 7; //   7 ---->Contacts.Facebook.TYPE_FACE_LAST_TIME
	public final static int TYPE_FACE_ID        = 1;          //1
	final static int TYPE_FACE_CELL      = 2;         //2
	final static int TYPE_FACE_PHONE     = 3;        //3
	final static int TYPE_FACE_EMAIL     = 4;        //4	
	final static int TYPE_FACE_FRIEND    = 5;       //5
	final static int TYPE_FACE_STATUS    = 6;       //6
	final static int TYPE_FACE_OTHER     = 7;       //7
	final static int TYPE_FACE_LAST_TIME = 8; //8
	final static int TYPE_FACE_BIRTHDAY  = 9;
	final static int TYPE_FACE_LOGO      = 10; 
	public final static int TYPE_FACE_ADDRESS   = 11;
	final static int TYPE_FACE_COMPANY   = 12;
	final static int TYPE_FACE_OTHER1    = 13;
	final static int TYPE_FACE_OTHER2    = 14;
	final static boolean DEBUG=SNSService.DEBUG;
	
	public static String createLookupEntriesBypids(Context con, List<Long> pids)
	{
		Entity phone = new Entity();
		Entity email = new Entity();
	
		for(Long pid : pids)
		{
			int peopleid = pid.intValue();
			if(peopleid > 0)
			{
				Entity tmpphone = getContactPhones(con, peopleid);
				phone.data.addAll(tmpphone.data);
				
				Entity tempemail = getContactEmails(con, peopleid);	
				email.data.addAll(tempemail.data);
			}
		}
		//save to hard disk
		JSONArray jsonArray   = new JSONArray();		
		JSONObject jsonObject = new JSONObject();
		for(int i=0;i<email.data.size();i++)
		{
			 String item = email.data.get(i);
	    	 jsonObject = new JSONObject();
	    	 try {
				jsonObject.put("email", item);
				jsonArray.put(jsonObject);
			} catch (JSONException e){
				Log.d(TAG, "create  email jsonobject exception "+e.getMessage());
			}
		}
		
		
		for(int i=0;i<phone.data.size();i++)
		{
			 String item = phone.data.get(i);
	    	 jsonObject = new JSONObject();
	    	 try {
				jsonObject.put("cell", item);
				jsonArray.put(jsonObject);
			} catch (JSONException e){
				Log.d(TAG, "create  cell jsonobject exception "+e.getMessage());
			}
		}		
		
		return jsonArray.length()>0?jsonArray.toString():"";		
	}
	
	public static List<Integer> getContactPhoneids(Context con,int peopleid)
	{
	    List<Integer> phoneids = new ArrayList<Integer>();
	    Entity entity = new Entity();
        entity.people_id = peopleid;
        
        String[] PHONES_PROJECTION = new String[]{
                People.Phones._ID, // 0
                People.Phones.NUMBER, // 1
        };
        int PHONES_ID_COLUMN = 0;
        int PHONES_NUMBER_COLUMN = 1;
        Uri mUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, peopleid);
        // Build up the phone entries
        Uri phonesUri = Uri.withAppendedPath(mUri, People.Phones.CONTENT_DIRECTORY);
        Cursor phonesCursor = con.getContentResolver().query(phonesUri, PHONES_PROJECTION, null, null,null);

        if (phonesCursor != null) 
        {
           while (phonesCursor.moveToNext()) 
           {
               int id = phonesCursor.getInt(PHONES_ID_COLUMN);
               phoneids.add(id);
           }
           phonesCursor.close();
        }         
	    return phoneids;
	}
	
	public static List<Integer> getContactEmailids(Context con,int peopleid)
	{
	    List<Integer> emailids = new ArrayList<Integer>();
	    Entity entity = new Entity();
        entity.people_id = peopleid;
        
        String[] METHODS_PROJECTION = new String[] {
                People.ContactMethods._ID, // 0
                People.ContactMethods.KIND, // 1
                People.ContactMethods.DATA, // 2
        };
        int METHODS_ID_COLUMN = 0;
        int METHODS_KIND_COLUMN = 1;
        int METHODS_DATA_COLUMN = 2;
        
        Uri mUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, peopleid);
        // Build the contact method entries
        final Uri methodsUri = Uri.withAppendedPath(mUri, People.ContactMethods.CONTENT_DIRECTORY);
        Cursor methodsCursor = con.getContentResolver().query(methodsUri, METHODS_PROJECTION, "kind = "+ Contacts.KIND_EMAIL, null,null);

        if (methodsCursor != null) 
        {
             while (methodsCursor.moveToNext()) 
             {
                  int id = methodsCursor.getInt(METHODS_ID_COLUMN);
                  emailids.add(id);
              }
              methodsCursor.close();
       }        
	    return emailids;
	}

	public static Entity getContactPhones(Context con, long peopleid)
	{
		Entity entity = new Entity();
		entity.people_id = peopleid;
		
		String[] PHONES_PROJECTION = new String[]{
	            People.Phones._ID, // 0
	            People.Phones.NUMBER, // 1
	    };
	    int PHONES_ID_COLUMN = 0;
	    int PHONES_NUMBER_COLUMN = 1;
	    Uri mUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, peopleid);
	    // Build up the phone entries
	    Uri phonesUri = Uri.withAppendedPath(mUri, People.Phones.CONTENT_DIRECTORY);
	    Cursor phonesCursor = con.getContentResolver().query(phonesUri, PHONES_PROJECTION, null, null,null);

	    if (phonesCursor != null) 
	    {
	       while (phonesCursor.moveToNext()) 
	       {
               String number = phonesCursor.getString(PHONES_NUMBER_COLUMN);
               long id = phonesCursor.getInt(PHONES_ID_COLUMN);
              
               if (TextUtils.isEmpty(number)) 
               {
                   Log.d(TAG, "empty number for phone " + id);
                   continue;
               }
               else
               {
                   entity.data.add(number);
               }
	       }
	       phonesCursor.close();
	    }	      
	    return entity;	    
	 }
	
	public static Entity getContactEmails(Context con, long peopleid)
	{
		Entity entity = new Entity();
		entity.people_id = peopleid;
		
		String[] METHODS_PROJECTION = new String[] {
	            People.ContactMethods._ID, // 0
	            People.ContactMethods.KIND, // 1
	            People.ContactMethods.DATA, // 2
	    };
	    int METHODS_ID_COLUMN = 0;
	    int METHODS_KIND_COLUMN = 1;
	    int METHODS_DATA_COLUMN = 2;
	    
	    Uri mUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, peopleid);
	    // Build the contact method entries
	    final Uri methodsUri = Uri.withAppendedPath(mUri, People.ContactMethods.CONTENT_DIRECTORY);
	    Cursor methodsCursor = con.getContentResolver().query(methodsUri, METHODS_PROJECTION, "kind = "+ Contacts.KIND_EMAIL, null,null);

	    if (methodsCursor != null) 
	    {
	         while (methodsCursor.moveToNext()) 
	         {
	              String data = methodsCursor.getString(METHODS_DATA_COLUMN);
	              if(data!=null && !data.equals(""))
	              {
	            	  entity.data.add(data);
	              }
	          }
	          methodsCursor.close();
	   }	    
	   return entity;
	}
	

	public static int getContactnumber(Context con){
		int totalcount = 0;
		String[] PEOPLE_PROJECTION = {"_id"};
		Cursor cursor = con.getContentResolver().query(Contacts.People.CONTENT_URI,PEOPLE_PROJECTION, null, null,null);
	    if(cursor!=null){
	    	totalcount = cursor.getCount();
	    }
	    Log.d(TAG, "total contact number is "+totalcount);
	    return totalcount;
	}
	

	static public class CachePeople
	{
		public String filePath;
		public long   lasttime;
	}
	
	
	public static CachePeople entrys     = null; 
	public static CachePeople peopledatas= null;
	static 
	{
		entrys     = new CachePeople(); 
		peopledatas= new CachePeople();
		
		//set file path to tmp
		entrys.filePath = "/tmp/lookupentry.source";
		entrys.lasttime = System.currentTimeMillis();
		
		peopledatas.filePath = "/tmp/peopledatas.source";
		peopledatas.lasttime = System.currentTimeMillis();		
	}
	
	//
	//TODO, if added/update contact, need update the cache, can reach 10 contacts will update
	//and have one hour not do, 
	//this is to block the batch add contact.
	//Observer the contact database
	static int nStep=0;
	public static void updateLookupEntries(Context con)
	{
		if(nStep >10 && ((System.currentTimeMillis()-entrys.lasttime) > 60*60*1000))
		{
		    createLookupEntries(con);
		    nStep = 0;
		}
		else
		{
			nStep++;
		}
	}	
	
	public static String createLookupEntriesFromCache(Context con)
	{
		String content       ="";	
		if(System.currentTimeMillis()- entrys.lasttime < 10*60*1000 && System.currentTimeMillis()- entrys.lasttime > 0)
		{		
			FileInputStream fis  = null;
			ObjectInputStream in = null;
			try
			{
			    fis = new FileInputStream(entrys.filePath);
			    in = new ObjectInputStream(fis);
			    content = (String)in.readObject();
			    in.close();
			}
			catch(IOException ex)
			{
			    ex.printStackTrace();
			}
			catch(ClassNotFoundException ex)
			{
			    ex.printStackTrace();
			}		
			
			if(content.length() == 0)
			{
				Log.d(TAG, "no cache, create a new one");
				content = createLookupEntries(con);
			}
			else
			{
				Log.d(TAG, "get from cache");
			}			
		}
		else
		{
			content = createLookupEntries(con);
		}
		return content;
	}
	
	public static String createLookupEntries(Context con)
	{
		  List<Long> pids = getPeopleIds(con);		  
		  Log.d(TAG, "peopleids is "+pids.size());
		  
		  String entries = createLookupEntriesBypids(con,  pids);
		  
		  FileOutputStream fos = null;
		  ObjectOutputStream out = null;
		  try
		  {
		      fos = new FileOutputStream(entrys.filePath);		      
		      out = new ObjectOutputStream(fos);
		      out.writeObject(entries);
		      out.close();
		      entrys.lasttime = System.currentTimeMillis();
		  }
		  catch(IOException ex)
		  {
		      Log.d(TAG, "fail to save the entry="+ex.getMessage());
		  }
		  
		  return entries;
	 }
	
	public static String createLookupEntries(Context con,List<Long> pids)
	{
	    String entries = createLookupEntriesBypids(con,  pids);
        
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(entrys.filePath);            
            out = new ObjectOutputStream(fos);
            out.writeObject(entries);
            out.close();
            entrys.lasttime = System.currentTimeMillis();
        }
        catch(IOException ex)
        {
            Log.d(TAG, "fail to save the entry="+ex.getMessage());
        }
        
        return entries;
	}
	

	public static String createLookupEntries(Context con,int limit,int offset)
	{
		  List<Long> pids = getPeopleIds(con,limit,offset);
		  Log.d(TAG, "peopleids is "+pids.size());
		  String entries = createLookupEntriesBypids(con,  pids);
		  
		  FileOutputStream fos = null;
		  ObjectOutputStream out = null;
		  try
		  {
		      fos = new FileOutputStream(entrys.filePath);		      
		      out = new ObjectOutputStream(fos);
		      out.writeObject(entries);
		      out.close();
		      entrys.lasttime = System.currentTimeMillis();
		  }
		  catch(IOException ex)
		  {
		      Log.d(TAG, "fail to save the entry="+ex.getMessage());
		  }
		  
		  return entries;
	 }
	
	public static List<Long> getPeopleIds(Context con)
	{
		String[] PEOPLE_PROJECTION = {
				 "_id"
		};
		List<Long> pids = new ArrayList<Long>();
		 
		Cursor methodsCursor = con.getContentResolver().query(Contacts.People.CONTENT_URI,PEOPLE_PROJECTION, null, null,null);

	    if (methodsCursor != null) 
	    {
	         while (methodsCursor.moveToNext()) 
	         {
	              int data = methodsCursor.getInt(methodsCursor.getColumnIndex(PEOPLE_PROJECTION[0]));
	              if(data>0)
	              {
	            	 pids.add(new Long(data));
	              }
	          }
	          methodsCursor.close();
	   }	    
	   return pids;
	}	
	 
	public static List<Long> getPeopleIds(Context con,int limit,int offset)
	{
	    String[] PEOPLE_PROJECTION = {
                "_id"
        };
        List<Long> pids = new ArrayList<Long>(); 
        Cursor methodsCursor = con.getContentResolver().query(Contacts.People.CONTENT_URI,PEOPLE_PROJECTION, null, null,null);
	    if (methodsCursor != null) 
	    {   
	    	 int flag = 0;
	    	 int startindex = limit*offset;
	    	 int endindex = limit*offset + limit;
	    	 while (methodsCursor.moveToNext()) 
	         {
	        	  if(flag >= startindex && flag < endindex )
	        	  {
	        	      long pid = methodsCursor.getLong(methodsCursor.getColumnIndex(PEOPLE_PROJECTION[0]));
	                  pids.add(pid);
	        	  }else if(flag >= endindex){
	        		  break;
	        	  } 
	        	  flag ++ ;        	  
	          }
	          methodsCursor.close();
	   }	    
	   return pids;
	}	
	
    //TODO change to Contact Column
	public static List<Integer> getPeopleList(Context con, long fuid)
	{	
	    String encodefuid = encryData(String.valueOf(fuid));
		List<Integer> pids = new ArrayList<Integer>();		
		String[] PEOPLE_PROJECTION = {Contacts.ContactMethods.PERSON_ID};
		
		String whereclause =  Contacts.ContactMethodsColumns.KIND+ " = "+KIND_FACEBOOK+" and "+
		                      Contacts.ContactMethodsColumns.TYPE+ " = "+TYPE_FACE_ID+" and "+
		                      Contacts.ContactMethodsColumns.DATA+ " = '"+encodefuid+"'";
		 
		Cursor methodsCursor = con.getContentResolver().query(Contacts.ContactMethods.CONTENT_URI,PEOPLE_PROJECTION, whereclause, null,null);

	    if (methodsCursor != null) 
	    {	 
	         while (methodsCursor.moveToNext()) 
	         {
	        	  int pid = methodsCursor.getInt(methodsCursor.getColumnIndex(PEOPLE_PROJECTION[0]));
	        	  pids.add(pid);

	          }
	          methodsCursor.close();
	   }	    
	   return pids;
	}
	
	public static List<ContactID> getContactInfoWithNoLookUp(Context con,PhoneBook phonebook)
	{
	    List<ContactID> al = new ArrayList<ContactID>();
	    List<Integer> peopleids = ContactHelper.getPeopleList(con, phonebook.uid);
        for(Integer people: peopleids)
        {
            ContactID item = new ContactID();
            item.people_id = people;
            
            al.add(item);
        }
        java.util.Collections.sort(al);    
        return al;
	}
	
	public static List<ContactID> getContactInfo(Context con, PhoneBook phonebook)
	{
		List<ContactID> al = new ArrayList<ContactID>();
		String[] email_PROJECTION = new String[] {
	            "_id", // 0
	            "person"//1
	    };
		if(phonebook.email != null && phonebook.email.length() > 0)
		{
		    final Uri methodsUri = Uri.withAppendedPath(Contacts.CONTENT_URI,"contact_methods");
		    Cursor methodsCursor = con.getContentResolver().query(methodsUri, email_PROJECTION, "kind = "+ Contacts.KIND_EMAIL+" and data = '"+phonebook.email+"'", null,null);		    
		    if (methodsCursor != null && methodsCursor.getCount()>0) 
		    {	   
		         while (methodsCursor.moveToNext()) 
		         {
		             int pid = methodsCursor.getInt(methodsCursor.getColumnIndex(email_PROJECTION[1]));		                          
		        	 ContactID item = new ContactID();
		        	 item.email_id   = methodsCursor.getInt(methodsCursor.getColumnIndex(email_PROJECTION[0]));
		        	 item.people_id  = pid;
		        	 item.email = phonebook.email;
		             al.add(item); 
		          }
		          methodsCursor.close();		          
		   }
	    }	
		
		//check for facebook id, current, we will not save cell and email data in contact, so will lead to repeat save contact,
		//why??
		//because the lookup just care the contact email and cell kind type, will not concern facebook cell and email.
		//
		List<Integer> peopleids = ContactHelper.getPeopleList(con, phonebook.uid);
		for(Integer people: peopleids)
		{
			ContactID item = new ContactID();
			item.people_id = people;
			item.uid = phonebook.uid;
			
			al.add(item);
		}
		
		if(phonebook.cell != null && phonebook.cell.length()>0)
        {
            String[] phone_PROJECTION = new String[]{
                    "_id", // 0
                    "person"//1
            };          
            final Uri methodsUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, phonebook.cell);
            Cursor methodsCursor = con.getContentResolver().query(methodsUri, phone_PROJECTION,null, null,null);
            //we just care one match phone number, if more than two number, we don't know which one should match
            //so ignore the found phonebook, if have more than one match
            if (methodsCursor != null && methodsCursor.getCount()>0) 
            {               
                 while (methodsCursor.moveToNext()) 
                 {
                     ContactID item = new ContactID();
                     item.phone_id = methodsCursor.getInt(methodsCursor.getColumnIndex(phone_PROJECTION[0]));       
                     item.people_id= methodsCursor.getInt(methodsCursor.getColumnIndex(phone_PROJECTION[1]));   
                     item.cell  = phonebook.cell;
                     item.phone = phonebook.phone;
                     al.add(item);
                 }
                 methodsCursor.close();              
            }
        }
		
		java.util.Collections.sort(al);
		return al;
	}
	
	
	public static HashMap<Integer,Object> batchRungetPhone_User_Friend_Info(FacebookSession fs,final List<PhoneBook> phonebooks)  throws FacebookException
	{      
        List<Long> fids = new ArrayList<Long>();
        for(PhoneBook phonebook : phonebooks)
        {
            fids.add(phonebook.uid);
        }
        
	    return fs.batch_run_getPhone_User_Friend_Info(fids);
	}
	
	/*
	 * solution: email has high priority to do sync with facebook
	 * firstly use contact email to sync with facebook. if synced facebook info. okay
	 * if synced nothing, use contact phone number to sync with facebook.
	 */
	public static void mappingPeopleIdToFuids(Context con,List<PhoneBook> phonebooks,HashMap<Long,List<Integer>> fmapsp)
	{
	    SocialORM orm = new SocialORM(con.getApplicationContext());
        boolean use_cell         = orm.getFacebookUsePhonenumber();               
        boolean use_email        = orm.getFacebookUseEmail();   
        
        Map<Integer, Long> peopleFBUID = new HashMap<Integer, Long>(); 
        for(PhoneBook phonebook : phonebooks)
        {
        	if(SNSService.DEBUG)
            Log.d(TAG, "Mapping phonebook="+phonebook);
        	
            List<ContactID> cons = ContactHelper.getContactInfo(con, phonebook);
            if(cons.size() == 0)
            {
                Log.d(TAG,"why enter here "+phonebook.toString());
                // actually shouldn't enter here....add the phonebook into Contact
                int pid = ContactInternal.AddNewPhoneBook(con, orm,  phonebook);
                if(pid > 0)
                {   
                	//TODO need delete
		    	    //cons= ContactHelper.getContactInfo(con, phonebook); //refresh cons list, 
		    		//this will lead a repeat request, replaced by the following code                    
                    cons.add(new ContactID(pid));
                }
                else
                {
                    continue;
                }
            }
            
            for(int i=0;i<cons.size();i++)
            {
                ContactID item = cons.get(i);
                //already have people --->facebook
                boolean NeedUserCurrentPeople = true;
                if(peopleFBUID.get(item.people_id) != null)
                {
                    if(item.email_id  == -1)
                    {
                        //this one is not matched email, just match phone, so if already have map, just ignore
                        NeedUserCurrentPeople = false;
                    } 
                    else
                    {
                        //clear pre uid-->pid mapping relationship because email has higher priority
                        final long fuid = peopleFBUID.get(item.people_id);
                        clearPreMapping(fuid,item.people_id,fmapsp);
                    }                    
                }
                
                if(NeedUserCurrentPeople == true)
                {
                    //end remember the facebook-------peoples 
                    if(fmapsp != null)
                    {
                        List<Integer> peopels = fmapsp.get(phonebook.uid);
                        if(peopels == null)
                        {
                            peopels = new ArrayList<Integer>();
                            fmapsp.put(phonebook.uid, peopels);
                        }
                        boolean exist=false;
                        for(int step=0;step<peopels.size();step++)
                        {
                            int pid = peopels.get(step);
                            if(pid == item.people_id)
                            {
                                exist = true;
                                break;
                            }                   
                        }
                        if(exist == false)
                        {
                            peopels.add(item.people_id);
                            //temp
                            peopleFBUID.put(item.people_id, phonebook.uid);
                        }
                    }
                }
            }
        }
        
        peopleFBUID.clear();
        peopleFBUID = null;
	}
	
	
	private static void clearPreMapping(long uid, int people_id,HashMap<Long, List<Integer>> fmapsp) {
        List<Integer> pidlist = fmapsp.get(uid);
        if(pidlist != null)
        {
            for(int i=0;i<pidlist.size();i++)
            {
                if(pidlist.get(i) == people_id)
                {
                    pidlist.remove(i);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void syncFacebookUserToContact(SocialORM orm,FacebookSession fs,Context con,List<PhoneBook> phonebooks)
	{
		 if(phonebooks == null || phonebooks.size() == 0)
		 {
			return;
		 }
		 //1 get user detail data from web site
		 List<PhoneBook>    result_phonebook = null;
		 List<FacebookUser> result_users      = null;
		 try{
    		 HashMap<Integer,Object> results = batchRungetPhone_User_Friend_Info(fs,phonebooks);
    		 result_phonebook  = (ArrayList<PhoneBook>)results.get(0);
             result_users      = (ArrayList<FacebookUser>)results.get(1);
		 } 
		 catch(FacebookException ne)
		 {
		     Log.d(TAG, "Fail to get Facebook user detail to save Phonebook="+ne.getMessage());
		     return;
		 }  
		 
		 //2 get map for Facebook------people id(more than one)
         HashMap<Long,List<Integer>> fmapsp = new HashMap<Long,List<Integer>>();         
         mappingPeopleIdToFuids(con,phonebooks,fmapsp);
         
         //3 sync phonebook to contact         
         boolean use_cell         = orm.getFacebookUsePhonenumber();            
         boolean use_email        = orm.getFacebookUseEmail();
         if(use_cell == true || use_email == true)
         {
             syncFacebookContact(con, result_phonebook, fmapsp);
         }         
         
         //save logo
         sybcFacebookLogo(con,orm, result_users, fmapsp);         
    }
	
	//for contact service to save, all info come from friends
	public static void savePhonebookIntoContact(SocialORM orm,FacebookSession fs,Context con,List<PhoneBook> phonebooks)
	{
		 if(phonebooks == null || phonebooks.size() == 0)
		 {
			return;
		 }
	     // areFriends; if we know they are come from friends' phonebook,
		 // no need do check		 
		 for(int i = 0;i<phonebooks.size();i++)
         {                   
             PhoneBook user = phonebooks.get(i);
             user.isFriend = true;
         }
		 
	     //do tag
	     HashMap<Long, List<Integer>>fmapsp = new HashMap<Long, List<Integer>>();
	     tagContactAsFacebook(con,phonebooks, fmapsp, true);
	     
	     //do sync logo icon for facebook friends
	     long[] uids = new long[phonebooks.size()];
	     for(int i=0;i<phonebooks.size();i++)
	     {
	         uids[i] = phonebooks.get(i).uid;
	     }
	     List<FacebookUser> users = orm.getFacebookUsers(uids);	     
	     sybcFacebookLogo(con,orm, users, fmapsp);
	}
	
	private void getPhonebookDetail(SocialORM orm, FacebookSession fs, List<PhoneBook> phonebooks)
	{
		//get detail contacts	     
        List<PhoneBook> fPB = null;
        if(phonebooks != null && phonebooks.size() > 0)
        {
           List<Long>ids = new ArrayList<Long>();
           for(PhoneBook pb: phonebooks)
           {
           	    if(pb.isFriend)
           	        ids.add(pb.uid);
           }   
           if(ids.size() > 0)
           {
	            long [] lids = new long[ids.size()];
	            for(int i=0;i<ids.size();i++)
	            {
	            	lids[i] = ids.get(i);
	            }
	            
	            try 
	            {
	                fPB = fs.getContactInfo(lids);
	                //save to database	                
	                orm.addPhonebook(fPB);
	            
	                lids = null;
	                ids.clear();
	                ids = null;
	            }
	            catch (FacebookException e) 
	            {                
	                Log.e(TAG, "fail to get all contact info="+e.getMessage());
	            }
           }
        }
	}
	
	
	//do at a seperate thread
	private static void syncFacebookContact(Context con, List<PhoneBook> pbs, HashMap<Long, List<Integer>>fmapsp)
	{
	    SavePhoneBookThread st = new SavePhoneBookThread("SavePhoneBookThread");
	    st.con = con;
	    st.pbs  = pbs;
	    st.fmapsp = fmapsp;
	    
	    st.start();
	}
	public static class SavePhoneBookThread extends Thread
    {
	    public Context con;
	    public List<PhoneBook> pbs;
	    public HashMap<Long, List<Integer>>fmapsp;
	    
	    public SavePhoneBookThread(String name)
	    {
	        super(name);
            Log.d(TAG, "SavePhoneBookThread+"+this.getId());
	    }
	    
	    public void run()
	    {
	        Log.d(TAG, "start run save phone book thread " + fmapsp.size());	  
	        
	        if(pbs != null && pbs.size()> 0)
	        {
	            for(PhoneBook phone : pbs)
	            {
	                List<Integer> peopels = fmapsp.get(phone.uid);
	                if(peopels != null)
	                {
    	                Log.d(TAG, "peopleid ---> peoples size "+phone.uid+"--->"+peopels.size());
    	                savePhoneBookToContact(peopels, phone);	     
	                }
	            }
	        }      
	    }
	    
	    private void savePhoneBookToContact(List<Integer> peoples, PhoneBook phone)
	    {
	        //if phone.cell is not one of the people's number add this number into phones	        
	        //if phone.email is not one of the people's email add this email into contactmethods
	        SocialORM orm = new SocialORM(con);
	        boolean use_logo         = orm.getFacebookUseLogo();
            boolean use_cell         = orm.getFacebookUsePhonenumber();            
            boolean use_email        = orm.getFacebookUseEmail();
            
            if(SNSService.DEBUG)
	        Log.d(TAG, "entering savePhoneBookToContact method "+phone);
            for(Integer peopleid : peoples)
            {
                if(!isEmpty(phone.cell) && use_cell)
                {
                	if(SNSService.DEBUG)
                    Log.d(TAG, " cell is "+phone.cell +"peopleid is "+peopleid);
                    addFacebookInfoToContactMethod(con,phone.cell,peopleid, KIND_FACEBOOK, TYPE_FACE_CELL);
                }
                if(!isEmpty(phone.phone) && use_cell)
                {
                	if(SNSService.DEBUG)
                    Log.d(TAG, " phone is "+phone.phone +"peopleid is "+peopleid);
                    addFacebookInfoToContactMethod(con,phone.phone,peopleid, KIND_FACEBOOK, TYPE_FACE_PHONE);                 

                }
                
                if(!isEmpty(phone.email) && use_email)
                {
                	if(SNSService.DEBUG)
                    Log.d(TAG," phone email is "+phone.email + " peopleid is "+peopleid);
                    addFacebookInfoToContactMethod(con,phone.email,peopleid, KIND_FACEBOOK, TYPE_FACE_EMAIL);   
                }
                
                addFacebookInfoToContactMethod(con,String.valueOf(phone.uid),peopleid,KIND_FACEBOOK, TYPE_FACE_ID, false);       
                
                String friendTag = phone.isFriend ==true?"1":"0";
                addFacebookInfoToContactMethod(con,friendTag ,peopleid,   KIND_FACEBOOK,  TYPE_FACE_FRIEND);      
                                    
                addFacebookInfoToContactMethod(con,String.valueOf(System.currentTimeMillis()), peopleid, KIND_FACEBOOK, TYPE_FACE_LAST_TIME, true);
                
                //update contact note
                updateContactNote(con,peopleid);
            } 
	    }
    }
	
	 private static void updateContactNote(Context con,int peopleid)
     {
		 String format = con.getString(R.string.update_contact_notes);
		 String notes = String.format(format, new Date(System.currentTimeMillis()).toLocaleString());
         
         Uri contactUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, peopleid);
         ContentValues people_cv = new ContentValues();
         people_cv.put(People.NOTES, notes);
         con.getContentResolver().update(contactUri, people_cv, null, null);
     }
	
	public static class LogoThread extends Thread
	{
	    private void syncLogo(List<FacebookUser> users, HashMap<Long, List<Integer>> maps)
        {
            for(FacebookUser user : users)
            {
                long uid = user.getUid();
                //List<Integer> pids = ContactHelper.getPeopleList(con, uid);
                List<Integer> pids = fmapsp.get(user.uid);
                if(pids == null)
                {
                    continue;
                }
                for(Integer pid : pids)
                {
                	if(enableLogo || enableBirthday)
                	{
	                    Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, pid);
	                    addPhotoAndBirthdayToContact(con,peopleuri,enableLogo,enableBirthday,user);
	                    //addPhotoAndBirthdayToContact(con,peopleuri,enableLogo, user.getPic_square(), enableBirthday, user.birthday);
                	}
                	
                	if(enableStatus)
                	{
	                    //add status into ContactMethod
	                    if(!isEmpty(user.message))
	                    {
	                        addFacebookInfoToContactMethod(con,user.message,(int)pid.longValue(), KIND_FACEBOOK, TYPE_FACE_STATUS);
	                    }
                	}
                }
            }
        }
	    	    
	    public boolean enableLogo     = true;
	    public boolean enableBirthday = true;
	    public boolean enableStatus   = true;	    
	    public List<FacebookUser>            users;
	    public HashMap<Long, List<Integer>> fmapsp;
	    public Context con;
	    public LogoThread(String name)
	    {
	        super(name);
	        Log.d(TAG, "LogoThread+"+this.getId());
	    }
	    
	    public void run()
	    {
	    	if(enableLogo == false && enableStatus == false)
	    	{
	    		Log.d(TAG, "logo, status disable");
	    		return ;
	    	}
	    	if(SNSService.DEBUG)
	        Log.d(TAG, "start run Logo Thread "+fmapsp.size());
	       	                
	        syncLogo(users,fmapsp);
	        
	        users.clear();
	        users = null;
	    }	   
	}
	
    public static void addPhotoAndBirthdayToContact(Context con, Uri peopleuri,boolean enablelogo, boolean enableBirthday,FacebookUser user)
    {
    	
    	long peopleid = ContentUris.parseId(peopleuri);
    	String picurl = user.pic_small;
    	String birthday = user.birthday;
    	
        if(enablelogo)
        {
            String filePath = TwitterHelper.getImagePathFromURL(con, picurl, false);      
            if(isEmpty(filePath)) return;
            File file = new File(filePath); 
            if(!file.exists() || file.length() == 0)
            {
            	addFacebookInfoToContactMethod(con,"0",(int)peopleid,KIND_FACEBOOK,TYPE_FACE_LOGO);
                return;
            }        
            addImageToContact(Uri.fromFile(new File(filePath)),con,peopleuri);
           
            addFacebookInfoToContactMethod(con,"1",(int)peopleid,KIND_FACEBOOK,TYPE_FACE_LOGO);
        }
        else
        {
        	addFacebookInfoToContactMethod(con,"0",(int)peopleid,KIND_FACEBOOK,TYPE_FACE_LOGO);
        }
        
        if(enableBirthday && isEmpty(birthday) == false)
        {
            syncBirthdayToContact(con,peopleuri,user);    
        	//addFacebookInfoToContactMethod(con,birthday,(int)peopleid,KIND_FACEBOOK,TYPE_FACE_BIRTHDAY);
        }
    }
 

    /**
     * TODO enhance the memory process
     * 
    * This notifier is created after an attachment completes downloaded.  It attaches to the
    * media scanner and waits to handle the completion of the scan.  At that point it tries
    * to start an ACTION_VIEW activity for the attachment.
   */
   private static class MediaScannerNotifier implements MediaScannerConnectionClient 
   {
       private Context mContext;
       private MediaScannerConnection mConnection;
       private File mFile;
       private Uri peopleuri;
       // MessageViewHandler mHandler;        

       public MediaScannerNotifier(Context context) 
       {
           mConnection = new MediaScannerConnection(context, this);                       
       }
       public void connect(Context con, File file, Uri peopleuri) 
       {
           mContext       = con;
           mFile          = file;            
           this.peopleuri = peopleuri;
           mConnection.connect();   
       }        

       public void onMediaScannerConnected() 
       {
           mConnection.scanFile(mFile.getAbsolutePath(), null);            
       }

       public void onScanCompleted(String path, Uri uri) 
       {
           try 
           {
               if (uri != null) 
               {
                   //inser into people DB
                   Log.d(TAG, "uri is "+uri.getPath());            
                   addImageToContact(uri,mContext,peopleuri);
               }
           } 
           catch (Exception e) 
           {
              Log.d(TAG, "Exception === "+e.getMessage());
           } 
           finally 
           {
               mContext = null;
               mConnection.disconnect();
           }
       }
   }
	
	//do at a seperate thread
	private static void sybcFacebookLogo(Context con,SocialORM orm, List<FacebookUser> users, HashMap<Long, List<Integer>>fmapsp)
	{
	    LogoThread th = new LogoThread("LogoThread");	    
	    th.users      = users;
	    th.fmapsp     = fmapsp;
	    th.con        = con;
	    th.enableLogo    = orm.getFacebookUseLogo();
	    th.enableBirthday= orm.getFacebookUseBirthday();
	    th.enableStatus = true;
	    th.start();
	}
	
	private static boolean isEmpty(String str)
	{
	    return str==null || str.length() ==0;
	}
	
	/*
	 * this is mainly for contact service
	 */
	public static void tagContactAsFacebook(Context con, List<PhoneBook> phonebooks, HashMap<Long, List<Integer> > fmapsp, boolean needCheckAddress)
	{	    
		if(phonebooks==null || phonebooks.size() ==0)
		{
			return;			
		}
		SocialORM orm = new SocialORM(con.getApplicationContext());
		boolean use_cell         = orm.getFacebookUsePhonenumber();               
        boolean use_email        = orm.getFacebookUseEmail();   
        
	    for(PhoneBook phonebook : phonebooks)
		{
	    	if(SNSService.DEBUG)
		    Log.d(TAG, "tagContactAsFacebook phonebook="+phonebook);
	    	
		    List<ContactID> cons= ContactHelper.getContactInfoWithNoLookUp(con, phonebook);
		    if(cons.size() == 0)
		    {
		    	//add the phonebook into Contact
		    	int pid = ContactInternal.AddNewPhoneBook(con, orm,  phonebook);
		    	if(pid > 0)
		    	{	
		    		//TODO need delete
		    	    //cons= ContactHelper.getContactInfo(con, phonebook); //refresh cons list, 
		    		//this will lead a repeat request, replaced by the following code
		    		cons.add(new ContactID(pid));
		    	}
		    	else
		    	{
		    	    continue;
		    	}
		    }
		    
		    //update contact info from facebook including logo, birthday,address, cell, phone.....
            for(int i=0;i<cons.size();i++)
		    {
		    	ContactID item = cons.get(i);
		    	//end remember the facebook-------peoples 
		    	if(fmapsp != null)
		    	{
    		    	List<Integer> peopels = fmapsp.get(phonebook.uid);
    		    	if(peopels == null)
    		    	{
    		    	    peopels = new ArrayList<Integer>();
    		    	    fmapsp.put(phonebook.uid, peopels);
    		    	}
    		    	boolean exist=false;
    		    	for(int step=0;step<peopels.size();step++)
    		    	{
    		    	    int pid = peopels.get(step);
    		    	    if(pid == item.people_id)
    		    	    {
    		    	        exist = true;
    		    	        break;
    		    	    }		    	    
    		    	}
    		    	if(exist == false)
    		    	{
    		    	    peopels.add(item.people_id);
    		    	}
		    	}
		    	//end remember the facebook-------peoples 
		    	
		    	if(item.people_id > 0)
		    	{		    	    
		    	    addFacebookInfoToContactMethod(con,String.valueOf(phonebook.uid),item.people_id,KIND_FACEBOOK, TYPE_FACE_ID, false);		    	    
		    	    String friendTag = phonebook.isFriend ==true?"1":"0";
                    addFacebookInfoToContactMethod(con,friendTag ,item.people_id,   KIND_FACEBOOK,  TYPE_FACE_FRIEND);      
                                        
                    //for facebook user, not our friends, we also need save the email and phone
                    if(item.email_id > 0 && use_email)
                    {
                        addFacebookInfoToContactMethod(con,item.email,item.people_id, KIND_FACEBOOK, TYPE_FACE_EMAIL);
                    }
                    
                    if(item.phone_id > 0 && use_cell)
                    {   
                        addFacebookInfoToContactMethod(con,item.cell,  item.people_id, KIND_FACEBOOK, TYPE_FACE_CELL);
                        
                        if(isEmpty(item.phone) == false)
                            addFacebookInfoToContactMethod(con,item.phone, item.people_id, KIND_FACEBOOK, TYPE_FACE_PHONE);
                    }
                    
                    addFacebookInfoToContactMethod(con,String.valueOf(System.currentTimeMillis()), item.people_id, KIND_FACEBOOK, TYPE_FACE_LAST_TIME, true);                    	    	
                    updateContactNote(con,item.people_id);
                    
                    //add address to contact_methods 
                    if(needCheckAddress)
                    {
	                    String postalinfo = formatFBPhonebookAddress(phonebook);
		                if(false == isEmpty(postalinfo))
		                {
		                    ContactHelper.addFacebookInfoToContactMethod(con,postalinfo, item.people_id,  ContactHelper.KIND_FACEBOOK, ContactHelper.TYPE_FACE_ADDRESS);
		                }	  
                    }
		    	}
		    }   
	    }
	}
	
	public static String formatFBPhonebookAddress(PhoneBook phonebook)
	{
		if(phonebook == null)
			return "";
		
		StringBuilder sb = new StringBuilder();		
 		String seperator=" ";		
 		if(isEmpty(phonebook.address) == false)   sb.append(phonebook.address+seperator);
 		if(isEmpty(phonebook.street) == false)    sb.append(phonebook.street+seperator);		
 		if(isEmpty(phonebook.state)  == false)    sb.append(phonebook.state+seperator);
 		if(isEmpty(phonebook.city)   == false)    sb.append(phonebook.city+seperator);
 		if(isEmpty(phonebook.country)== false)    sb.append(phonebook.country+seperator);
 		if(isEmpty(phonebook.zip)    == false)    sb.append(phonebook.zip+seperator);		
 		if(isEmpty(phonebook.latitude)==false)	  sb.append(phonebook.latitude+seperator);		
 		if(isEmpty(phonebook.longitude)==false)   sb.append(phonebook.longitude+seperator);		
 				
 		String postalinfo = sb.toString();
 		return postalinfo;
	}
	
	/*
	 * use return value to decrease the call update time
	 */
	public static void addFacebookInfoToContactMethod(Context con,String data,int peopleid,int kind,int type)
	{
		addFacebookInfoToContactMethod(con,data,peopleid,kind,type, true);
	}
        static boolean encryptdata=true;
        public static String encryData(String data)
        {
            if(encryptdata == false)
            {
                return data;
            }
            else
            {
                String str = new String(Base64.encodeBase64(data.getBytes()));
                return str;
            }
        }
	
	
	private static String decryData(String data)
    {
        String str = new String(Base64.decodeBase64(data.getBytes()));
        return str;
    }
	
	public static int removeFacebookDataByPid(Context con,int peopleid, long fuid)
	{
		long fid = -1;
		Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        Uri uri = Uri.withAppendedPath(peopleuri, Contacts.People.ContactMethods.CONTENT_DIRECTORY); 
        
        //check if the data is existed   if existed do nothing else insert data to Contact_methods table
        String[] projection = {Contacts.ContactMethods._ID, Contacts.ContactMethodsColumns.DATA};
        
        int count = 0;
        for(int i=1;i<=TYPE_FACE_OTHER2;i++)
        {
	        String whereclause =  Contacts.ContactMethods.PERSON_ID +"="+peopleid+" and "+
	        	                  Contacts.ContactMethodsColumns.KIND+"="+KIND_FACEBOOK  +" and "+ 
	        	                  Contacts.ContactMethodsColumns.TYPE+"=" +i;
	        
	        Uri resultUri=null;
	        Cursor cursor = con.getContentResolver().query(uri, projection, whereclause, null, null);
	        if(cursor!=null && cursor.getCount()>0 ) 
	        {
	            if ( cursor.moveToNext())
	            {
	                long id      = cursor.getLong(0);
	                resultUri = ContentUris.withAppendedId(uri, id);
	                try{
	                    count += con.getContentResolver().delete(resultUri, null, null);
	                    if(DEBUG)
	                    Log.d(TAG, "deleted peopleid="+resultUri + " fuid="+fuid + " count="+count);   
	                }catch(Exception ne){}
	            }
	        }        
        }
        return count;        
	}
	
	private static boolean isFacebookCategory(int peopleid, Context con)
	{
        return false;
    }

    public static long getFacebookIDByPid(Context con,int peopleid)
	{
		long fid = -1;
		Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        Uri uri = Uri.withAppendedPath(peopleuri, Contacts.People.ContactMethods.CONTENT_DIRECTORY); 
        
        //check if the data is existed   if existed do nothing else insert data to Contact_methods table
        String[] projection = {Contacts.ContactMethods._ID, Contacts.ContactMethodsColumns.DATA};
        String whereclause =  Contacts.ContactMethods.PERSON_ID  +"=" +peopleid      + " and "+
        	                  Contacts.ContactMethodsColumns.KIND+"=" +KIND_FACEBOOK + " and "+ 
                              Contacts.ContactMethodsColumns.TYPE+"=" +TYPE_FACE_ID;        					  
        
        Cursor cursor = con.getContentResolver().query(uri, projection, whereclause, null, null);
        if(cursor!=null && cursor.getCount()>0 /*&& force == true*/) 
        {
            if ( cursor.moveToNext())
            {
                long id      = cursor.getLong(0);
                String fdata  = cursor.getString(1);
                String dfdata = decryData(fdata);
                try{
                    fid = Long.parseLong(dfdata);
                }catch(NumberFormatException ne)
                {
                	Log.d(TAG, "what is the data="+dfdata);
                }
            }
        }
        if(DEBUG)
        Log.d(TAG, "entering getFacebookIDByPid pid="+ peopleid+" fid="+fid);
        return fid;        
	}
	
	private static void addFacebookInfoToContactMethod(Context con,String data,int peopleid,int kind,int type, boolean force)
	{
	    String encodedData = encryData(data);
	    if(DEBUG)
	    Log.d(TAG, " entering addFacebookInfoContactMethod people id is "+peopleid+" type = "+type+" data="+data + " encrypt="+encodedData);
	      
        Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        Uri uri = Uri.withAppendedPath(peopleuri, Contacts.People.ContactMethods.CONTENT_DIRECTORY); 
        
        //check if the data is existed   if existed do nothing else insert data to Contact_methods table
        String[] projection = {Contacts.ContactMethods._ID, Contacts.ContactMethodsColumns.DATA};
        String whereclause =  Contacts.ContactMethods.PERSON_ID +"="+peopleid+" and "+
        	                  Contacts.ContactMethodsColumns.KIND+"="+kind+" and "+
                              Contacts.ContactMethodsColumns.TYPE+"="+type;
        Cursor cursor = con.getContentResolver().query(uri, projection, whereclause, null, null);
        
        ContentValues contentvalue = new ContentValues();
        contentvalue.put(Contacts.ContactMethodsColumns.KIND,kind);
        contentvalue.put(Contacts.ContactMethodsColumns.TYPE, type);
        contentvalue.put(Contacts.ContactMethodsColumns.DATA, encodedData);
        contentvalue.put(Contacts.ContactMethodsColumns.ISPRIMARY, 1);
               
        if(cursor!=null && cursor.getCount()>0 /*&& force == true*/) 
        {
        	if(DEBUG)
            Log.d(TAG, "entering update contact_methods "+ peopleid);   
            if ( cursor.moveToNext())
            {
                long id      = cursor.getLong(0);
                String fdata = cursor.getString(1);
                cursor.close();
                
                if(fdata != null && fdata.equals(encodedData))
                {
                	if(DEBUG)
                	Log.d(TAG, "no need to update, save value="+fdata+" data="+encodedData);
                	return ;
                }
                
                Uri methodsUri = ContentUris.withAppendedId(uri, id);
                if(TYPE_FACE_FRIEND == type)
                {
                	//if we had set as friend, the fail to get isFriend from Facebook, we should not set it as false
                	if(data.equals("1"))
                	{
                		con.getContentResolver().update(methodsUri, contentvalue, null,null);
                	}
                	else
                	{
                		if(DEBUG)
                		Log.d(TAG, "no need to update is friend to 0");
                	}
                }
                else
                {
               	    con.getContentResolver().update(methodsUri, contentvalue, null,null);
                }
            }        
        }
        else
        {
            con.getContentResolver().insert(uri, contentvalue);
        }
        
	}	

   
	public static class Contact 
	{
		public int    people_id;
		public long   uid ;
		public String Name;		
	}
	
	public static class SyncData
	{
		public long fuid;
		public long peopleid;
		
		public boolean updatelogo;
		public boolean updatebirthday;
		public boolean updateemail;
		public boolean updatecell;
		
		//new
		public boolean updateaddress;
		public boolean updatecompany;
	}
	
	public static void updateContactInfo(Context con,SocialORM orm, FacebookSession fs, SyncData data, boolean force, boolean isFriend)
	{
		if(data.peopleid<= 0)
			return ;
		if(DEBUG)
	    Log.d(TAG,"entering updateContactInfo " + "updatelogo ="+data.updatelogo + " updatebirthday = "+data.updatebirthday + " updatecell = "+data.updatecell + " updateemail ="+data.updateemail);
	    
	    //batch_run 
	    //fs.batch_run_updateContact(fuid)
	    
        boolean ismyfriend =  isFriend;
        if(ismyfriend == false)
        {
            try 
            {
            	ismyfriend = fs.isMyFriend(data.fuid);
            	addFacebookInfoToContactMethod(con, ismyfriend?"1":"0", (int)data.peopleid,  KIND_FACEBOOK, TYPE_FACE_FRIEND);
            }
            catch (FacebookException e) 
            {
            	addFacebookInfoToContactMethod(con, "0", (int)data.peopleid,  KIND_FACEBOOK, TYPE_FACE_FRIEND);            	
                Log.d(TAG, "for update one contact="+e.getMessage());            
            }
        }
        else
        {
        	addFacebookInfoToContactMethod(con, "1",(int)data.peopleid,  KIND_FACEBOOK, TYPE_FACE_FRIEND);
        }
        
                                          
        addFacebookInfoToContactMethod(con,String.valueOf(data.fuid),(int)data.peopleid,KIND_FACEBOOK, TYPE_FACE_ID);
        //add last update time
        addFacebookInfoToContactMethod(con,String.valueOf(System.currentTimeMillis()), (int)data.peopleid, KIND_FACEBOOK, TYPE_FACE_LAST_TIME, true);                    
        
        Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, data.peopleid);
        PhoneBook phonebook = null;
        FacebookUser user   = null;    
        String email        = null;
        String cell         = null;
        String logo         = null;
        String birthday     = null;
        String phone        = null;
        
        if(ismyfriend && (data.updateemail || data.updatecell))
        {
            try
            {
                if(force == false)
                {
                     phonebook = orm.getPhonebook(data.fuid);
                     if(phonebook == null)
                     {
                         phonebook = fs.getContactInfo(data.fuid);
                     }
                }
                else
                {
                    phonebook = fs.getContactInfo(data.fuid);                  
                }
            }
            catch (FacebookException e) 
            {
                Log.d(TAG, "for update one contact="+e.getMessage());            
            }
            //if current network is down, get data from database
            if(phonebook == null)
            {
                phonebook = orm.getPhonebook(data.fuid);
            }
            
            if(phonebook != null)
            {
                 email = phonebook.getEmail();
                 cell  = phonebook.getCell();
                 phone = phonebook.getPhone();
            }               
        }
        
        if(data.updatelogo || data.updatebirthday)
        {   
            try
            {
                if(force == false)
                {
                    user = orm.getFacebookUser(data.fuid);
                    if(user == null)
                    {
                        user= fs.getUserInfo(data.fuid,FacebookUser.Field.NAME,FacebookUser.Field.STATUS,FacebookUser.Field.BIRTHDAY, FacebookUser.Field.PIC_SQUARE, FacebookUser.Field.PIC,FacebookUser.Field.PIC_SMALL);
                    }
                }
                else
                {
                    user= fs.getUserInfo(data.fuid,FacebookUser.Field.NAME,FacebookUser.Field.STATUS,FacebookUser.Field.BIRTHDAY, FacebookUser.Field.PIC_SQUARE, FacebookUser.Field.PIC,FacebookUser.Field.PIC_SMALL);                 
                }
            }
            catch (FacebookException e) 
            {
                Log.d(TAG, "for update one contact="+e.getMessage());            
            }
            
            //if current network is down, get data from database
            if(user == null)
            {
                user = orm.getFacebookUser(data.fuid);
            }
            
            if(user != null)
            {
                logo     = user.getPic_square();
                birthday = user.getBirthday();
            }
        }  
        
        if(DEBUG)
        Log.d(TAG, "update Contact Info user info ");
        if(user != null && !isEmpty(user.message))
        {
        	addFacebookInfoToContactMethod(con,user.message,(int)data.peopleid, KIND_FACEBOOK, TYPE_FACE_STATUS);
        }
        
        if(data.updateemail &&    !isEmpty(email))    
        	addFacebookInfoToContactMethod(con,email,(int)data.peopleid, KIND_FACEBOOK, TYPE_FACE_EMAIL);
        
        if(data.updatecell)   
        {
            if(!isEmpty(cell))
        	   addFacebookInfoToContactMethod(con,cell,(int)data.peopleid, KIND_FACEBOOK, TYPE_FACE_CELL);
            if(!isEmpty(phone))
               addFacebookInfoToContactMethod(con,phone,(int)data.peopleid, KIND_FACEBOOK, TYPE_FACE_PHONE);
        }
        
        if(data.updatebirthday || data.updatelogo)
        {
        	addPhotoAndBirthdayToContact(con,peopleuri,data.updatelogo,data.updatebirthday,user); 
        }
        
        //update address to contact_methods
        if(data.updateaddress )
        {
        	String postalinfo = ContactHelper.formatFBPhonebookAddress(phonebook);
        	if(false == isEmpty(postalinfo))
	        {
	           ContactHelper.addFacebookInfoToContactMethod(con,postalinfo, (int)data.peopleid,  ContactHelper.KIND_FACEBOOK, ContactHelper.TYPE_FACE_ADDRESS);
	        }
        }
	}
	
	private static void syncBirthdayToContact(Context con,Uri peopleuri,FacebookUser user)
	{ 

	}
	
    private static void addImageToContact(Uri imageUri,Context context,Uri peopleuri)
    {
        ContentValues cv = new ContentValues();
        cv.put("photo_data", imageUri.toString());
        int ret = context.getContentResolver().update(peopleuri,cv, null, null);
        
        if(DEBUG)
        Log.d(TAG, "update peopleuri ="+peopleuri + " return="+ret + " image="+imageUri);
    }
    
    public static void addImageToContact(String photodata,Context context,long peopleid)
    {
        Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        ContentValues cv = new ContentValues();
        cv.put("photo_data", photodata);
        int ret = context.getContentResolver().update(peopleuri,cv, null, null);
        //Log.d(TAG, "update peopleuri ="+peopleuri + " return="+ret + " image="+imageUri);
    }
    
    public static String getPhotoDataByPID(Context context,long peopleid)
    {
        Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        String[] projection = {"photo_data"};
        Cursor cursor = context.getContentResolver().query(peopleuri, projection, null, null, null);
        if(cursor!=null && cursor.moveToFirst())
        {
            String photo_data = cursor.getString(0);
            cursor.close();
            return photo_data;
        }
        return null;
    }
    
    public static void reviveContactBackData(ContactBackData backdata,Context context,long peopleid)
    {
        Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        ContentValues cv = new ContentValues();
        boolean isnull = true;
        if(backdata.photodata!=null)
        {
        	isnull = false;
        	cv.put("photo_data", backdata.photodata);
        }
        if(backdata.calendar_eventid>0)
        {
        	isnull = false;
        	cv.put("calendar_eventid", backdata.calendar_eventid);
        }
        
        if(isnull == false)
        {
            int ret = context.getContentResolver().update(peopleuri,cv, null, null);
        }
        //Log.d(TAG, "update peopleuri ="+peopleuri + " return="+ret + " image="+imageUri);
    }
    
    public static ContactBackData getContactBackData(Context context,long peopleid)
    {
        Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,peopleid);
        String[] projection = {"photo_data","calendar_eventid"};
        Cursor cursor = context.getContentResolver().query(peopleuri, projection, null, null, null);
        if(cursor!=null && cursor.moveToFirst())
        {
            ContactBackData contactbackdata = new ContactBackData();
            String photo_data = cursor.getString(0);
            long   calendar_eventid = cursor.getLong(1);
            contactbackdata.photodata = photo_data;
            contactbackdata.calendar_eventid = calendar_eventid;
            cursor.close();
            return contactbackdata;
        }
        return null;
    }
    
    public static void clearFacebookInfo(Context con)
    {
        //1 delete all Facebook category contacts        
        removeAllFacebookCategoryContact(con);
        
        //2 delete user manually add Facebook info            
        String[] PEOPLE_PROJECTION = {Contacts.ContactMethods._ID};
        String whereclause =  Contacts.ContactMethodsColumns.KIND+ " = "+KIND_FACEBOOK;
        Cursor cursor = con.getContentResolver().query(Contacts.ContactMethods.CONTENT_URI,PEOPLE_PROJECTION, whereclause, null, null);
        if(cursor !=null && cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                long id = cursor.getLong(cursor.getColumnIndex(Contacts.ContactMethods._ID));
                removeFacebookInfo(id,con);
            }
            cursor.close();
            cursor = null;
        }
      
    }
    
    private static void removeFacebookInfo(long id, Context con)
   {
        ContentResolver cr = con.getContentResolver();
        Uri cUri = ContentUris.withAppendedId(Contacts.ContactMethods.CONTENT_URI, id);
        Log.d(TAG,"remove facebook info id="+id +" uri is="+cUri);
        int rows = cr.delete(cUri, null, null);   
    }

    private static void removeAllFacebookCategoryContact(Context con)
    {
    }

    private static void removeContact(long pid, Context con)
    {
        Uri peopleUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, pid);
        Log.d(TAG,"remove Contact uri is="+peopleUri);
        int rows = con.getContentResolver().delete(peopleUri, null, null);  
    }

    public static class ContactBackData
    {
        public String photodata;
        public long   calendar_eventid;
    }
}
