package com.msocial.facebook.ui.adapter;

import java.util.List;

import com.msocial.facebook.R;
import com.msocial.facebook.ui.FacebookAccountActivity;
import com.msocial.facebook.ui.FacebookStreamActivity;
import com.msocial.facebook.ui.view.FacebookStatusContentItemView;
import com.msocial.facebook.ui.view.FacebookStreamItemView;
import oms.sns.service.facebook.model.Stream;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class FacebookStreamAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookStreamAdapter";        
    private Context mContext; 
    private boolean forwall = false;
    public FacebookStreamAdapter(Context con,  List<Stream>streams)
    {
    	mContext = con;
    	mStreamItems = streams;   
    	
    	Log.d(TAG, "create FacebookStreamAdapter");
    }
    
    public FacebookStreamAdapter(Context con,  List<Stream>streams,boolean forwall)
    {
    	mContext = con;
    	mStreamItems = streams;  
    	this.forwall = forwall;
    	Log.d(TAG, "create FacebookStreamAdapter forwall");
    }
    
	public int getCount() 
	{
	    if(mStreamItems.size() > 0)
		    return mStreamItems.size()+1;
	    else 
	        return 0;
	}
	public Object getItem(int pos) {
	    if(pos == mStreamItems.size())
	    {
	        return null;
	    }
	    return mStreamItems.get(pos);
	}
	public long getItemId(int pos) 
	{
	    if(pos == mStreamItems.size())
	        return pos;
	    
		return mStreamItems.get(pos).post_id.hashCode();
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
		 }         
		 
		 Stream di = (Stream)getItem(position);
		 if(di != null)
		 {
		     FacebookStreamItemView v=null;
             if (convertView == null || false == (convertView instanceof FacebookStreamItemView)) {
                 v = new FacebookStreamItemView(mContext, di,forwall);
             } 
             else 
             {
                  v = (FacebookStreamItemView) convertView;
                  v.setStreamItem(di,forwall);
             }        
             v.chooseStreamListener();
             return v;
		 }
		 else
		 {
		      Button but = new Button(mContext);
		      but.setTextAppearance(mContext, R.style.sns_load_old);
		      but.setBackgroundColor(Color.WHITE);		        
		      but.setText(mContext.getString(R.string.load_older_msg));
		      if(FacebookStreamActivity.class.isInstance(mContext))
		      {
		          FacebookStreamActivity fs = (FacebookStreamActivity)mContext;
		          but.setOnClickListener(fs.loadOlderClick);
		          if(fs.isInProcess())
                  {
                      but.setText(mContext.getString(R.string.loading_string));
                  }
		      }
		      else if( FacebookAccountActivity.class.isInstance(mContext))
		      {
		          FacebookAccountActivity fs = (FacebookAccountActivity)mContext;
                  but.setOnClickListener(fs.loadOlderClick);
                  if(fs.isInProcess())
                  {
                      but.setText(mContext.getString(R.string.loading_string));
                  }
		      }		      
		      return but;
		 }
	}
	
	private List<Stream> mStreamItems;
}


