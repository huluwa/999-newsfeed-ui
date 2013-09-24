package com.msocial.facebook.ui.adapter;

import java.util.List;

import com.msocial.facebook.ui.view.FacebookGroupItemView;
import oms.sns.service.facebook.model.Group;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookGroupAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookGroupAdapterextends";        
    private Context mContext;    
    public FacebookGroupAdapter(Context con,  List<Group>groups)
    {
    	mContext = con;
    	mGroupItems = groups;   
    	Log.d(TAG, "create FacebookGroupAdapter");
    }
    
	public int getCount() 
	{
		return mGroupItems.size();
	}
	public Object getItem(int pos) {		
	    return mGroupItems.get(pos);
	}
	public long getItemId(int pos) 
	{
		return mGroupItems.get(pos).gid;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FacebookGroupItemView v=null;
	
		 Group di = (Group)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookGroupItemView(mContext, di);
         } else {
              v = (FacebookGroupItemView) convertView;
              v.setGroupItem(di);
         }        
         v.chooseGroupListener();
         return v;
	}	
	private List<Group> mGroupItems;
}
