package com.ast.free.ui.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.ast.free.R;
import com.ast.free.ui.FacebookEventGuestActivity;
import com.ast.free.ui.FacebookMessageActivity;
import com.ast.free.ui.view.FacebookFriendItemView;
import com.ast.free.ui.view.FacebookStatusView;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FacebookUser.SimpleFBUser;


public class FacebookFriendAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookFriendAdapter";        
    private Context mContext;   
    private boolean isfriendbd = false;
    private int mType = -1;  // mtype = 1 attending event ruest, 2 maybe attending,3 declined ,4 not replied
    private boolean withfooterview = false;
    
    public FacebookFriendAdapter(Context con,  List<FacebookUser.SimpleFBUser>users, int type, boolean withfooterview)
    {
    	mContext = con;
    	mUsersItems = users;  
    	mType = type;
    	this.withfooterview = withfooterview;
    	Log.d(TAG, "create FacebookFriendAdapter");
    }
    
    public FacebookFriendAdapter(Context con,  List<FacebookUser.SimpleFBUser>users)
    {
        mContext = con;
        mUsersItems = users;
        Log.d(TAG, "create FacebookFriendAdapter");
    }
    
    public FacebookFriendAdapter(Context con,List<FacebookUser.SimpleFBUser>users,boolean isfriendbd){
    	mContext = con;
    	mUsersItems = users;   
    	this.isfriendbd = isfriendbd;
    }
    
	public int getCount() 
	{
	    if(withfooterview)
	    {
	        return mUsersItems.size() +1;
	    }
	    else
	    {
	        return mUsersItems.size();
	    }
	}
	public Object getItem(int pos) {	
	    if(withfooterview)
	    {
	        if(pos == (getCount()-1))
	        {
	            return null;
	        }
	    }
	       
	    return mUsersItems.get(pos);
	    
	}
	public long getItemId(int pos) 
	{  
	    if(withfooterview)
	    {
	        if(pos == (getCount()-1))
	        {
	            return -1;
	        }
	    }
		return mUsersItems.get(pos).uid;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FacebookFriendItemView v=null;
	
		 FacebookUser.SimpleFBUser di = (FacebookUser.SimpleFBUser)getItem(position);
		 if(di!=null)
		 {
		     di.isFriend(mContext);
	         Log.d(TAG," di. isfriend is "+di.isfriend);
	         if (convertView == null || false == (convertView instanceof FacebookFriendItemView)) {
	             v = new FacebookFriendItemView(mContext, di,isfriendbd);
	         } else {
	              v = (FacebookFriendItemView) convertView;
	              v.setUserItem(di,isfriendbd);
	         }        
		 }
		 else
		 {
		     // add footerview
		     if(withfooterview == true)
		     {
		        Button but = new Button(mContext);
	            but.setTextAppearance(mContext, R.style.sns_load_old);
	            but.setBackgroundColor(Color.WHITE);              
	            but.setText(mContext.getString(R.string.load_more_msg));
	            if(FacebookEventGuestActivity.class.isInstance(mContext))
	            {
	                FacebookEventGuestActivity fe = (FacebookEventGuestActivity)mContext;
	                but.setOnClickListener(fe.loadOlderGuestClick);
	               /* String footerText = "";
	                if(mType == 1) //attending   
	                {   
	                    footerText = mContext.getString(R.string.loading_attending_guest);
	                }
	                else if(mType == 2)   
	                {   
	                    footerText = mContext.getString(R.string.loading_attending_guest);
	                }   
	                else if(mType == 3)
	                {   
	                    footerText = mContext.getString(R.string.loading_attending_guest);
	                } 
	                else if(mType ==4)
	                {
	                    footerText = mContext.getString(R.string.loading_attending_guest);
	                }*/
	                if(fe.isInProcess() == true)
	                {
	                    but.setText(fe.getFooterText());
	                }
	            }
	            return but;
		     }
		 }
		 
         //v.chooseFriendListener();
         return v;
	}	
	private List<FacebookUser.SimpleFBUser> mUsersItems;
}


