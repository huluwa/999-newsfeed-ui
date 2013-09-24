package com.tormas.litetwitter.ui.adapter;

import java.util.List;

import com.tormas.litetwitter.ui.view.FacebookStatusContentItemView;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookStatusContentAdapter  extends BaseAdapter 
{
    private final String TAG = "FacebookStatusContentAdapter";        
    private Context mContext;    
    public FacebookStatusContentAdapter(Context con,  List<FacebookStatusItem>statuses)
    {
    	mContext = con;
    	mStatusItems = statuses;   
    	Log.d(TAG, "create FacebookStatusContentAdapter");
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
		return mStatusItems.get(pos).id;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
		 }         
		 FacebookStatusContentItemView v=null;
		 FacebookStatusItem di = (FacebookStatusItem)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookStatusContentItemView(mContext, di);
         } else {
              v = (FacebookStatusContentItemView) convertView;
              v.setContentItem(di);
         }        
         v.chooseRemoveListener();
         return v;
	}	
	private List<FacebookStatusItem> mStatusItems;
}


