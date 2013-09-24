package com.tormas.litesina.ui.adapter;

import java.util.List;

import com.tormas.litesina.R;
import com.tormas.litesina.ui.TwitterFavoritesActivity;
import com.tormas.litesina.ui.TwitterTweetsActivity;
import com.tormas.litesina.ui.view.SimplyStatusItemView;
import twitter4j.SimplyStatus;
import twitter4j.Tweet;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class SimplyStatusAdapter extends BaseAdapter {
	    private final String TAG = "SimplyStatusAdapter";        
	    private Context mContext;
	    int type;//0, status, 1, tweet
	    private boolean forDetailView=false;
	    public boolean withfootview = false;
	    public SimplyStatusAdapter(Context con,  List<SimplyStatus>status)
	    {
	    	mContext = con;
	    	mStatusItems = status;    	
	    	type = 0;
	    }
	    
	    public void showForDetail()
	    {
	    	forDetailView = true;
	    }

	    public SimplyStatusAdapter(Context con,  List<Tweet>tweets, boolean forTrend)
	    {
	    	mContext = con;
	    	mTrendsItems = tweets;    	
	    	type = 1;
	    }
	    
		public int getCount() {
			if(type == 0)
			{
			    if(withfootview == true)
			    {
			        return (mStatusItems.size()+1);
			    }
			    else
			    {
			        return mStatusItems.size();
			    }
			}   
			else
			{
			    if(withfootview == true)
			    {
			        return (mTrendsItems.size()+1);
			    }
			    else
			    {
			        return mTrendsItems.size();
			    }
			}
				
		}
		public Object getItem(int pos) {		
			if(type == 0)
			{
			    if(pos<mStatusItems.size())
                {
                    return mStatusItems.get(pos);
                }
			    else
			    {
			        return null;
			    }   
			}  
			else
			{
			    if(pos<mTrendsItems.size())
			    {
			        return mTrendsItems.get(pos);
			    }
			    else
			    {
			        return null;
			    }
			}	
		}
		
		public long getItemId(int pos) {
			if(type == 0)
			{
			    if(pos<mStatusItems.size())
			    {
			        return mStatusItems.get(pos).getId();
			    }
			    else
			    {
			        return -1;
			    }
			}   
			else
			{
			    if(pos<mTrendsItems.size())
			    {
			        return mTrendsItems.get(pos).getId();
			    }
			    else
			    {
			        return -1;
			    }
			}			
		}
		
		public View getView(int position, View convertView, ViewGroup arg2) 
		{		
			 if (position < 0 || position >= getCount()) 
			 {
	             return null;    
	         }
	         
			 SimplyStatusItemView v=null;
			 if(type == 0)
			 {
				 SimplyStatus di = (SimplyStatus)getItem(position);
				 
				 if(di != null)
				 {
				     if (convertView == null || false == (convertView instanceof SimplyStatusItemView)) {
	                     v = new SimplyStatusItemView(mContext, di);
	                 } else {
	                      v = (SimplyStatusItemView) convertView;
	                      v.setStatusItem(di);
	                 }
	                 v.chooseTweetsListener();
	                 if(forDetailView)
                         v.showForDetail();
	                 return v;
				 }
				 else
				 {
				     return getFooterView();
				 }
		         
			 }
			 else
			 {
				 Tweet di = (Tweet)getItem(position);
				 if(di != null)
				 {
				     if (convertView == null || false == (convertView instanceof SimplyStatusItemView)) {
	                     v = new SimplyStatusItemView(mContext, di);
	                 } else {
	                      v = (SimplyStatusItemView) convertView;
	                      v.setTweetItem(di);
	                 }
	                 v.chooseTweetsListener();
	                 if(forDetailView)
	                     v.showForDetail();
	                 
	                 return v;
				 }
				 else
				 {
				     return getFooterView();
				 }
			 }       
		}
		
		private View getFooterView() {
		    Log.d(TAG, "entering create footerview");
            Button but = new Button(mContext.getApplicationContext());
            but.setTextAppearance(mContext, R.style.sns_load_old);
            but.setBackgroundColor(Color.WHITE);              
            but.setText(mContext.getString(R.string.load_older_msg));
            if(TwitterTweetsActivity.class.isInstance(mContext))
            {
                TwitterTweetsActivity fn = (TwitterTweetsActivity)mContext;
                boolean inProcess  = fn.isInProcess();
                but.setOnClickListener(fn.loadOlderClick);
                if(inProcess == true)
                {
                    but.setText(R.string.loading_string);
                }
            }
            else if(TwitterFavoritesActivity.class.isInstance(mContext))
            {
                TwitterFavoritesActivity fn = (TwitterFavoritesActivity)mContext;
                boolean inProcess = fn.isInProcess();
                but.setOnClickListener(fn.loadOlderClick);
                if(inProcess == true)
                {
                    but.setText(R.string.loading_string);
                }
            }
            return but;
        }

        private List<SimplyStatus> mStatusItems;
		private List<Tweet>  mTrendsItems;		
		
	}


