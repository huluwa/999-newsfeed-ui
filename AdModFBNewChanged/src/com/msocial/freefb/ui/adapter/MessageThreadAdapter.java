package com.msocial.freefb.ui.adapter;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.providers.SocialORM.MailThreadCol;
import com.msocial.freefb.ui.FacebookMessageActivity;
import com.msocial.freefb.ui.FacebookNotesActivity;
import com.msocial.freefb.ui.view.MessageThreadInfoItemView;
import oms.sns.service.facebook.model.MessageThreadInfo;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class MessageThreadAdapter extends BaseAdapter {
    int offset = 0;
    Cursor cursor;
    Context mContext;
    SocialORM orm;
    private boolean withfooterview = false;
    private int mType = -1;
    private static final String TAG = "MessageThreadAdapter";
    
    public MessageThreadAdapter(Context con,  Cursor cursor,SocialORM orm,boolean withfooterview,int type)
    {
        mContext      = con;
        this.cursor   = cursor;
        this.orm      = orm;
        mType         = type;
        this.withfooterview = withfooterview;
    }
    public MessageThreadAdapter(Context con,  Cursor cursor,SocialORM orm)
    {
        mContext      = con;
        this.cursor   = cursor;
        this.orm      = orm;
    }
    
    public MessageThreadAdapter(Context con,  Cursor cursor,SocialORM orm,int type)
    {
        mContext      = con;
        mType         = type;
        this.cursor   = cursor;
        this.orm      = orm;
    }
    
    public int getCount() 
    {
        if(cursor == null)
        {
            return 0;
        }
        else if(withfooterview && cursor.getCount()>0)
        {
            return cursor.getCount()+1;
        }
        else
        {
            return cursor.getCount();
        }
    }

    public Object getItem(int position) 
    {
        if(cursor != null && cursor.requery() == true)
        {
            if(cursor.moveToFirst())
            {
                if(cursor.moveToPosition(position))
                {
                    return orm.getMailboxThreadFromCusor(cursor);
                }
            }               
        }          
        return null;
    }

    public long getItemId(int position) 
    {
        if(cursor != null && cursor.requery() == true)
        {
            if(cursor.moveToFirst())
            {
                if(cursor.moveToPosition(position))
                {
                    return cursor.getLong(cursor.getColumnIndex(MailThreadCol.thread_id));
                }
            }
        }
        return -1;
    }
  
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        MessageThreadInfoItemView v=null;
        MessageThreadInfo di = (MessageThreadInfo)getItem(position);
        if(di != null)
        {
            if (convertView == null || false == (convertView instanceof MessageThreadInfoItemView) ) 
            {
                v = new MessageThreadInfoItemView(mContext, di,mType);
            }
            else 
            {
                 v = (MessageThreadInfoItemView) convertView;
                 v.setMessageItem(di,mType);
            }
            v.chooseMessageListener();
        }
        else
        {
            Log.d(TAG, "entering create footerview");
            Button but = new Button(mContext.getApplicationContext());
            but.setTextAppearance(mContext, R.style.sns_load_old);
            but.setBackgroundColor(Color.WHITE);              
            but.setText(mContext.getString(R.string.load_older_msg));
            if(FacebookMessageActivity.class.isInstance(mContext))
            {
                FacebookMessageActivity fn = (FacebookMessageActivity)mContext;
                boolean inProcess  = false;
                if(mType == 0)                    
                {   
                    inProcess = fn.isInProcess();
                    but.setOnClickListener(fn.loadOlderInboxClick);
                }
                else if(mType == 1)                    
                {   
                    inProcess = fn.inprocessSend;
                    but.setOnClickListener(fn.loadOlderSentClick);
                }   
                else if(mType == 2)
                {   
                    inProcess = fn.inprocessupdate;
                    but.setOnClickListener(fn.loadOlderUpdateClick);
                } 
                if(inProcess == true)
                {
                    but.setText(mContext.getString(R.string.loading_string));
                }
            }
           
            return but;
        }
        return v;
    }

}
