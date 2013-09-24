package com.msocial.nofree.ui.adapter;

import java.util.List;

import com.msocial.nofree.ui.view.FacebookMailItemView;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Wall;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MailAdapter extends BaseAdapter 
{
    private final String TAG = "MailAdapterextends";        
    private Context   mContext;    
    private boolean   showImage=true;
    MailboxMessage    msg;
    public boolean isupdate = false;
        
    public MailAdapter(Context con,  List<MailboxMessage>msgs)
    {
    	mContext = con;
    	mMailItems = msgs;
    }
    
    public void disableImage()
    {
    	showImage = false;
    }   
    
	public int getCount() 
	{
	    return mMailItems.size();
	}
	
	public Object getItem(int pos) 
	{		
		return mMailItems.get(pos);
	}
	
	public long getItemId(int pos) 
	{
		return mMailItems.get(pos).mid.hashCode();
	}
	
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FacebookMailItemView v=null;
		
		 MailboxMessage di = (MailboxMessage)getItem(position);
         if (convertView == null ) 
         {
             v = new FacebookMailItemView(mContext, di,isupdate);
         }
         else 
         {
              v = (FacebookMailItemView) convertView;
              v.setMessageItem(di,isupdate);
         }
         v.chooseMessageListener();
		         
		 if(showImage == false)
			 v.disableImage();
		 
         return v;
	}
	private List<MailboxMessage>  mMailItems;		
	
}


