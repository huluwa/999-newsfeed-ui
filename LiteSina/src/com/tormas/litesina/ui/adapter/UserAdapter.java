package com.tormas.litesina.ui.adapter;

import java.util.List;

import com.tormas.litesina.ui.view.UserItemView;
import twitter4j.SimplyUser;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class UserAdapter extends BaseAdapter {
    private final String TAG = "UserAdapter";        
    private Context mContext;
    
    public UserAdapter(Context con,  List<SimplyUser>users)
    {
    	mContext = con;
    	mUserItems = users;
    }
 
	public int getCount() 
	{
		return mUserItems.size();
	}
	public Object getItem(int pos) 
	{
			return mUserItems.get(pos);
	}
	public long getItemId(int pos) 
	{	
		return mUserItems.get(pos).getId();
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 UserItemView v=null;
		 
		 SimplyUser di = (SimplyUser)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new UserItemView(mContext, di);
         } else {
              v = (UserItemView) convertView;
              v.setUserItem(di);
         }
         v.chooseFollowListener();
		
         return v;
	}
	
	private List<SimplyUser> mUserItems;	
	
}



