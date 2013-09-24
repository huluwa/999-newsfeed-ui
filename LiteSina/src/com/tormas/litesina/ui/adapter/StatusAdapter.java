package com.tormas.litesina.ui.adapter;


import java.util.ArrayList;
import java.util.List;
import com.tormas.litesina.ui.view.StatusItemView;
import twitter4j.Status;
import twitter4j.Tweet;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StatusAdapter extends BaseAdapter {
    private final String TAG = "TweetsAdapter";        
    private Context mContext;
    int type;//0, status, 1, tweet
    
    public StatusAdapter(Context con,  List<Status>status)
    {
    	mContext = con;
    	mStatusItems = status;    	
    	type = 0;
    }
    

    public StatusAdapter(Context con,  List<Tweet>tweets, boolean forTrend)
    {
    	mContext = con;
    	mTrendsItems = tweets;    	
    	type = 1;
    }
    
	public int getCount() {
		if(type == 0)
		    return mStatusItems.size();
		else
			return mTrendsItems.size();
	}
	public Object getItem(int pos) {		
		if(type == 0)
		    return mStatusItems.get(pos);
		else
			return mTrendsItems.get(pos);
	}
	public long getItemId(int pos) {
		if(type == 0)
		    return mStatusItems.get(pos).getId();
		else
			return mTrendsItems.get(pos).getId();
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 StatusItemView v=null;
		 if(type == 0)
		 {
			 Status di = (Status)getItem(position);
	         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
	             v = new StatusItemView(mContext, di);
	         } else {
	              v = (StatusItemView) convertView;
	              v.setStatusItem(di);
	         }
	         v.chooseTweetsListener();
		 }
		 else
		 {
			 Tweet di = (Tweet)getItem(position);
	         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
	             v = new StatusItemView(mContext, di);
	         } else {
	              v = (StatusItemView) convertView;
	              v.setTweetItem(di);
	         }
	         v.chooseTweetsListener();
		 }         
         return v;
	}
	
	private List<Status> mStatusItems;
	private List<Tweet>  mTrendsItems;
	
	
}

