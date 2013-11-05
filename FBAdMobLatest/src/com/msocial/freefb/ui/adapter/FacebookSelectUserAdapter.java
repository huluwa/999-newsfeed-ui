package com.msocial.freefb.ui.adapter;

import java.util.List;

import com.msocial.freefb.ui.view.FacebookSelectUserItemView;
import oms.sns.service.facebook.model.FacebookUser;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookSelectUserAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookUserSelectActivity";        
    private Context mContext;    
    public FacebookSelectUserAdapter(Context con,  List<FacebookUser>users)
    {
    	mContext = con;
    	mUserItems = users;   
    	Log.d(TAG, "create FacebookGroupAdapter");
    }
    
	public int getCount() 
	{
		return mUserItems.size();
	}
	public Object getItem(int pos) {		
	    return mUserItems.get(pos);
	}
	public long getItemId(int pos) 
	{
		return mUserItems.get(pos).uid;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FacebookSelectUserItemView v=null;
	
		 FacebookUser di = (FacebookUser)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookSelectUserItemView(mContext, di);
         } else {
              v = (FacebookSelectUserItemView) convertView;
              v.setUserItem(di);
         }        
         v.chooseSelectListener();
         return v;
	}	
	private List<FacebookUser> mUserItems;
}
