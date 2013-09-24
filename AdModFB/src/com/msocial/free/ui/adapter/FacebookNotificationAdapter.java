package com.msocial.free.ui.adapter;

import com.msocial.free.ui.view.FacebookNotificationItemView;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FBNotifications.NotifyBase;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookNotificationAdapter extends BaseAdapter {

	private final String TAG = "FacebookEventAdapter";        
    private Context         mContext;   
    private FBNotifications notifies;
    
    public FacebookNotificationAdapter(Context con,  FBNotifications notifies)
    {
    	mContext = con;
    	this.notifies = notifies; 
    	Log.d(TAG, "create FacebookEventAdapter");
    }
	
	public int getCount() 
	{		
		return notifies.getTypes();
	}

	public Object getItem(int pos) {
		return notifies.get(FBNotifications.getPos(pos));
	}

	public long getItemId(int pos) {
		
		return notifies.get(FBNotifications.getPos(pos)).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
		 }         
		 FacebookNotificationItemView v=null;
		 NotifyBase obj = (NotifyBase)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookNotificationItemView(mContext, obj);
         } else {
              v = (FacebookNotificationItemView) convertView;
              v.setContentItem(obj);
         }        
         v.chooseNotifyListener();
         return v;
	}	

}
