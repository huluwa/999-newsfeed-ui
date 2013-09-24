package com.msocial.free.ui.adapter;

import java.util.List;

import com.msocial.free.providers.SocialORM.PeopleMapFacebook;
import com.msocial.free.ui.view.FacebookFindFriendItemView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookFindFriendsAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookFindFriendsAdapter";        
    private Context mContext;   
   
    public FacebookFindFriendsAdapter(Context con,  List<PeopleMapFacebook>users)
    {
    	mContext = con;
    	mUsersItems = users;   
    	Log.d(TAG, "create FacebookFindFriendsAdapter");
    }
       
	public int getCount() 
	{
		return mUsersItems.size();
	}
	public Object getItem(int pos) {		
	    return mUsersItems.get(pos);
	}
	public long getItemId(int pos) 
	{
		return mUsersItems.get(pos).uid;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FacebookFindFriendItemView v=null;
	
		 PeopleMapFacebook di = (PeopleMapFacebook)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookFindFriendItemView(mContext, di);
         } else {
              v = (FacebookFindFriendItemView) convertView;
              v.setUserItem(di);
         }        
         v.chooseFriendListener();
         return v;
	}	
	private List<PeopleMapFacebook> mUsersItems;
}


