package com.msocial.freefb.ui.adapter;

import java.util.List;

import com.msocial.freefb.ui.FacebookFriendsStatusActivity;
import com.msocial.freefb.ui.view.FacebookStatusView;
import oms.sns.service.facebook.model.UserStatus;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookStatusAdapter  extends BaseAdapter 
{
    private final String TAG = "FacebookStatusAdapter";        
    private Context mContext;
	private boolean showusername;    
    public FacebookStatusAdapter(Context con,  List<UserStatus>status)
    {
    	mContext = con;
    	mStatusItems = status;   
    	Log.d(TAG, "create FacebookStatusAdapter");
    	showusername = false;
    }
    
	public FacebookStatusAdapter(Context con,  List<UserStatus>status, boolean showUserName) {
		this(con, status);
		showusername = showUserName;
	}

	public int getCount() 
	{
		return mStatusItems.size();
	}
	public Object getItem(int pos) {		
	    return mStatusItems.get(pos);
	}
	public long getItemId(int pos) 
	{
		return mStatusItems.get(pos).statusid;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FacebookStatusView v=null;
	
		 UserStatus di = (UserStatus)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookStatusView(mContext, di, showusername);
         } else {
              v = (FacebookStatusView) convertView;
             
              v.showUserName(showusername);
              v.setStatusItem(di);
              
              
         }        
         return v;
	}	
	private List<UserStatus> mStatusItems;
}


