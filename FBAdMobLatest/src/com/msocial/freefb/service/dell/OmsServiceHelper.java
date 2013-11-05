package com.msocial.freefb.service.dell;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class OmsServiceHelper {
	final static String TAG="OmsServiceHeper";
	
	public static String createLookupEntries(Context con, List<OmsTask> nexttasks)
	{
		Entity phone = new Entity();
		Entity email = new Entity();
	
		for(OmsTask task: nexttasks)
		{
			long peopleid = task.id;
			if(peopleid > 0)
			{
				Entity tmpphone = ContactHelper.getContactPhones(con, peopleid);
				phone.data.addAll(tmpphone.data);
				
				Entity tempemail = ContactHelper.getContactEmails(con, peopleid);	
				email.data.addAll(tempemail.data);		    
			}
		}
		
		Log.d(TAG, "email="+email.data.size() + " phone="+phone.data.size());		
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
	
	
	
	public static void getAllocatedTask(List<OmsTask> cancel, List<OmsTask> all, List<OmsTask> lookups,
			List<OmsTask> eventsync, List<OmsTask> eventadds,
			List<OmsTask> contactSyncs, List<OmsTask> lookupall,List<OmsTask> addasfriend) 
	{
		for(int i=0;i<all.size();i++)
		{
			OmsTask item = all.get(i);
			item.dispatchtime = System.currentTimeMillis();
			if(item.action == OmsTask.ACTION.CONTACT_LOOK_UP)
			{
				if(item.id == OmsTask.LOOKUPALLID)
					lookupall.add(item);
				else
				{
				    lookups.add(item);
				}
			
			}
			else if(item.action == OmsTask.ACTION.CONACT_SYNC)
			{
				contactSyncs.add(item);
			}			
			else if(item.action == OmsTask.ACTION.EVENT_ADD)
			{
				eventadds.add(item);
			}
			else if(item.action == OmsTask.ACTION.EVENT_SYNC)
			{
				eventsync.add(item);
			}
			else if(item.action == OmsTask.ACTION.CANCEL_ALL)
			{
				cancel.add(item);
			}
			else if(item.action == OmsTask.ACTION.ADD_AS_FRIEND)
			{
			    addasfriend.add(item);
			}
		}
	}
}
