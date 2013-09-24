package com.msocial.free.ui.adapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.msocial.free.R;
import com.msocial.free.providers.SocialORM.FacebookUsersCol;
import com.msocial.free.providers.SocialORM.FollowCol;
import com.msocial.free.providers.SocialORM.PhonebookCol;
import com.msocial.free.ui.FacebookMailActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.google.android.mms.util.SqliteWrapper;

public class RecipientsAdapter extends ResourceCursorAdapter {
    private static final String TAG = "RecipientsAdapter";
    public static final int SMART_INDEX_ID      = 0;
    public static final int SMART_INDEX_UID     = 1;//for twitter, it is a screen name
    public static final int SMART_INDEX_NAME    = 2;//for twitter, it is the user name
    
    final String[] PROJECTION_SMARTLEARNER = {
    	FacebookUsersCol.ID,
    	FacebookUsersCol.UID,
    	FacebookUsersCol.NAME,
    };
    
    final String[] PHONEBOOK_SMARTLEARNER = {
            PhonebookCol.ID,
            PhonebookCol.UID,
            PhonebookCol.EMAIL
    };
    
    final String[] TWITTER_SMARTLEARNER = {
        	FollowCol.ID,
        	FollowCol.SName,
        	FollowCol.Name,
        };

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private boolean fortwitter;

    public RecipientsAdapter(Context context) {
        super(context,R.layout.recipient_filter_item, null);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public RecipientsAdapter(Context context, boolean fortwitter) {
        super(context,R.layout.recipient_filter_item, null);
        mContext = context;
        this.fortwitter = fortwitter;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public final CharSequence convertToString(Cursor cursor) {
    	Log.d(TAG, "entering convertToString");  	
        String name = cursor.getString(SMART_INDEX_NAME);        
        String number = cursor.getString(SMART_INDEX_UID).trim();
        if (number.length() == 0) {
            return number;
        }

        SpannableString out = new SpannableString(number);
        int len = out.length();
        
        if (!TextUtils.isEmpty(name)) {
            out.setSpan(new Annotation("name", name), 0, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            out.setSpan(new Annotation("name", number), 0, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if(fortwitter)
        {
        	out.setSpan(new Annotation("number", number), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else
        {
            out.setSpan(new Annotation("number", number), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return out;
    }

    @Override
    public final void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(cursor.getString(SMART_INDEX_NAME));
        name.setTextColor(Color.GRAY);
        
        TextView number = (TextView) view.findViewById(R.id.number);
        number.setText("   (" + cursor.getString(SMART_INDEX_UID) + ")");
        number.setTextColor(Color.GRAY);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) 
    {	        
        String cons = null;
        String arg = "";
        if (constraint != null) 
        {
            cons = constraint.toString();
            //find the last number
            if(cons.contains(",")) {
                String[] strSplit = cons.split(",");
                arg = strSplit[strSplit.length -1];
            } else {
                arg = cons;
            }
            if(arg.contains("'")) {
                arg = "";
            } else {
                arg = arg.trim();
            }
        }
        
        if (null == arg || 0 == arg.length()) {
            return null;
        }

        if(fortwitter == false)
        {
	        String[] args = new String[]{arg};	   
	        //for user name
	        Cursor cursor = null;
	       
	        Uri CONTENT_URI = Uri.parse("content://com.msocial.free.providers.SocialProvider/facebookusers");
            cursor = SqliteWrapper.query(mContext, mContentResolver,CONTENT_URI, PROJECTION_SMARTLEARNER, " name like '%"+arg+"%' " , null, null);
	        
    	    if(cursor == null || cursor.moveToFirst() == false)
    	    {
    	        CONTENT_URI = Uri.parse("content://com.msocial.free.providers.SocialProvider/phonebook");
    	        cursor = SqliteWrapper.query(mContext, mContentResolver,CONTENT_URI, PHONEBOOK_SMARTLEARNER, " email like '%"+arg+"%' " , null, null);
    	    }
	        return cursor;
        }
        else
        {
        	String[] args = new String[]{arg};
	        Uri CONTENT_URI = Uri.parse("content://com.msocial.free.providers.SocialProvider/follow");
	        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
	        		CONTENT_URI, TWITTER_SMARTLEARNER, " name like '%"+arg+"%' and type = 0 " , null, null);
	        return cursor;
        }
    }
    
}
