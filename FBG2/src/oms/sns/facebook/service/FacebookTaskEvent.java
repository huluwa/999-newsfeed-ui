package oms.sns.facebook.service;

import oms.sns.facebook.R;
import oms.sns.facebook.service.dell.ContactHelper;
import oms.sns.facebook.service.dell.OmsService;
import oms.sns.facebook.service.dell.OmsTask;
import oms.sns.facebook.service.dell.OmsTask.AddAsFriendTask;
import oms.sns.facebook.service.dell.OmsTask.ContactSyncTask;
import oms.sns.facebook.service.dell.OmsTask.EventAddTask;
import oms.sns.facebook.service.dell.OmsTask.EventSyncTask;
import oms.sns.facebook.service.dell.OmsTask.LookupTask;
import oms.sns.facebook.ui.TaskManagementActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FacebookTaskEvent 
{
	final String TAG ="FacebookEvent";
	SNSService service;
	OmsService omsService;

	public FacebookTaskEvent(SNSService snsService, OmsService omsService)
	{
		service = snsService;
		this.omsService = omsService;
	}
	
	public void doContactLookup(int[] peopleids)
	{
		Log.d(TAG, "entering doContactLookup "+peopleids.length);
		boolean ret = service.PromptLoginUI();
		if(ret)
		{		
			for(int i=0;i<peopleids.length;i++)
			{
				OmsTask omstask = new LookupTask();
				omstask.id = peopleids[i];
				omsService.queueTask(omstask);	
			}			
			showNotification();
		}		
	}
	
	public void doContactLookup(int peopleid)
	{
		Log.d(TAG, "entering doContactLookup "+peopleid);
		boolean ret = service.PromptLoginUI();
		if(ret)
		{
			OmsTask omstask = new LookupTask();
			omstask.id = peopleid;
			omsService.queueTask(omstask);		
			//showNotification();
		}
	}
	
	public void doContactSync(int peopleid){
		boolean ret = service.PromptLoginUI();
		if(ret)
		{
			OmsTask omstask = new ContactSyncTask();
			omsService.queueTask(omstask);
			//showNotification();
		}
		
	}
	
	public void doAddAsFriend(long fuid)
	{
	    boolean ret = service.PromptLoginUI();
	    if(ret)
	    {
	        OmsTask omstask = new AddAsFriendTask(fuid);
	        omsService.queueTask(omstask);
	        showNotification();
	    }
	}
	
	public void doUpdateContact(long fuid,long peopleid,boolean updatelogo,
	                            boolean updatebirthday,boolean updateemail,boolean updatecell)
	{
	  
	    omsService.processUpdateContactInfo(fuid,peopleid,updatelogo,updatebirthday,updateemail,updatecell);

	}
	
	public void doEventAdd(int p_eid,int subcategoryid,int categoryid)
	{
	    boolean ret = service.PromptLoginUI();
        if(ret)
        {
            Log.d(TAG, "eid="+p_eid + " cate="+categoryid + " sub="+subcategoryid);
            if(p_eid!=-1 && subcategoryid!=-1 && categoryid!=-1)
            {
                EventAddTask omstask = new EventAddTask();
                omstask.id = p_eid;
                omstask.subcategoryid = subcategoryid;
                omstask.categoryid = categoryid;
                omsService.queueTask(omstask);
            }            
            //showNotification();
        }
	}
	
	public void doEventAdd(int p_eid){
		boolean ret = service.PromptLoginUI();
		if(ret)
		{
			OmsTask omstask = new EventAddTask();
			omstask.id = p_eid;
			omsService.queueTask(omstask);
			//showNotification();
		}
	}
	
	public void doEventSync(){
		boolean ret = service.PromptLoginUI();
		if(ret)
		{
			OmsTask omstask = new EventSyncTask();
			omstask.id = OmsTask.EventSyncID;
			omsService.queueTask(omstask);
			//showNotification();
		}
	}
	
	public void showNotification(){
		showNotification(SNS_TASK_NOTIFICATION,"Social Task","Social network","background tasks");
	}
	
	 public void showNotification(int notificationid,String tickerText,String title,String message){
		  //String tickerText = "is syncing facebook contact";
	  	
	      Notification notification = new Notification(R.drawable.facebook_logo, tickerText,
	              System.currentTimeMillis());

	      // The PendingIntent to launch our activity if the user selects this notification
	      PendingIntent contentIntent = PendingIntent.getActivity(service, 0,new Intent(service, TaskManagementActivity.class), 0);
	      // Set the info for the views that show in the notification panel.
	      notification.setLatestEventInfo(service,title,message, contentIntent);

	      // Send the notification.
	      // We use a layout id because it is a unique number.  We use it later to cancel.
	      NotificationManager mNM = (NotificationManager)service.getSystemService(service.NOTIFICATION_SERVICE);
	      if(mNM!=null)
	          mNM.notify(notificationid, notification);
	  }
	 
	   public void cancelNotification(Context con, int notificationid){
		   NotificationManager mNM = (NotificationManager)con.getSystemService(con.NOTIFICATION_SERVICE);
		   if(mNM!=null)
		          mNM.cancel(notificationid);
	   }
		   
	
	final static int EVENT_SYNC_NOTIFICATION = 1;
	final static int CONTACT_SYNC_NOTIFICATION = 2;	  
	final static int CONTACT_ADD_NOTIFICATION = 3;
	final static int EVENT_ADD_NOTIFICATION = 4;
	final static int SNS_TASK_NOTIFICATION = 5;
}
