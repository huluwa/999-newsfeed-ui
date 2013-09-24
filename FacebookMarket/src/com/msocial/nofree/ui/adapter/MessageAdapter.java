package com.msocial.nofree.ui.adapter;

import java.util.List;
import com.msocial.nofree.ui.view.MessageItemView;
import com.msocial.nofree.ui.view.MessageThreadInfoItemView;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Wall;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class MessageAdapter extends BaseAdapter 
{
    private final String TAG = "MessageAdapter";        
    private Context mContext;
    int type;//0, wall, 1, mailbox
    private boolean showImage=true;
    
    public MessageAdapter(Context con,  List<Wall>walls)
    {
    	mContext = con;
    	mWallItems = walls;    	
    	type = 0;
    }
    
    public void disableImage()
    {
    	showImage = false;
    }

    public MessageAdapter(Context con,  List<MessageThreadInfo>mails, boolean forTrend)
    {
    	mContext = con;
    	mMailItems = mails;    	
    	type = 1;
    }
    
	public int getCount() 
	{
		if(type == 0)
		    return mWallItems.size();
		else
			return mMailItems.size();
	}
	
	public Object getItem(int pos) 
	{		
		if(type == 0)
		    return mWallItems.get(pos);
		else
			return mMailItems.get(pos);
	}
	
	public long getItemId(int pos) 
	{
		if(type == 0)
		    return mWallItems.get(pos).wpid;
		else
			return mMailItems.get(pos).thread_id;
	}
	
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 if(type == 0)
		 {
		     MessageItemView v=null;
			 Wall di = (Wall)getItem(position);
	         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
	             v = new MessageItemView(mContext, di);
	         } else {
	              v = (MessageItemView) convertView;
	              v.setWallItem(di);
	         }
	         v.chooseMessageListener();
	         
	         if(showImage == false)
	             v.disableImage();
	         
	         return v;
		 }
		 else
		 {
		     MessageThreadInfoItemView v=null;
			 MessageThreadInfo di = (MessageThreadInfo)getItem(position);
	         if (convertView == null ) 
	         {
	             v = new MessageThreadInfoItemView(mContext, di);
	         }
	         else 
	         {
	              v = (MessageThreadInfoItemView) convertView;
	              v.setMessageItem(di);
	         }
	         v.chooseMessageListener();
	         return v;
		 }
	}
	
	private List<Wall>           mWallItems;
	private List<MessageThreadInfo>  mMailItems;		
	
}


