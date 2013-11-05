package com.msocial.freefb.ui.adapter;

import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.ui.FacebookAccountActivity;
import com.msocial.freefb.ui.FacebookCommentsActivity;
import com.msocial.freefb.ui.FacebookStreamActivity;
import com.msocial.freefb.ui.view.CommentItemView;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.Stream.Comments;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

/******************************************
 * Comments adapter and View item        
 *
 */
public class FacebookCommentsAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookCommentsAdapter";        
    private Context mContext;    
    private boolean hasmore=false;
    public FacebookCommentsAdapter(Context con,  Comments com, boolean hasmore)
    {
        mContext = con;
        comment = com;
        mCommentItems = com.stream_posts;   
        this.hasmore = hasmore;
        Log.d(TAG, "create FacebookCommentsAdapter for FacebookCommentActivity");
    }
    public int getCount() 
    {
    	if(hasmore)
            return mCommentItems.size()+1;
    	else
    		return mCommentItems.size();
    }
    public Object getItem(int pos) 
    {
    	if(pos == mCommentItems.size())
    		return null;
    	
        return mCommentItems.get(pos);
    }
    public long getItemId(int pos) 
    {
    	if(pos == mCommentItems.size())
    		return -1;
        return mCommentItems.get(pos).fromid;
    }
    public View getView(int position, View convertView, ViewGroup arg2) 
    {       
         if (position < 0 || position >= getCount()) 
         {
             return null;    
         }
         
         Comments.Stream_Post di = (Comments.Stream_Post)getItem(position);
         if(di != null)
         {
        	 CommentItemView v = null;
	         if (convertView == null || false == (convertView instanceof CommentItemView)) 
	         {
	             v = new CommentItemView(mContext, di);
	         } 
	         else 
	         {
	        	  if(convertView instanceof CommentItemView)
	        	  {
		        	  v = (CommentItemView) convertView;
		              v.setCommentItem(di);
	        	  }	        	  
	         }        
	         v.chooseCommentListener();
	         return v;
         }
         else
         {
        	  Button but = new Button(mContext);
		      but.setTextAppearance(mContext, R.style.sns_load_old);
		      but.setBackgroundColor(Color.WHITE);		        
		      but.setText(mContext.getString(R.string.load_older_msg));
		      if(FacebookCommentsActivity.class.isInstance(mContext))
		      {
		    	  FacebookCommentsActivity fs = (FacebookCommentsActivity)mContext;
		          but.setOnClickListener(fs.loadOlderClick);
		          if(fs.isInProcess())
                  {
                      but.setText(mContext.getString(R.string.loading_string));
                  }
		      }		         
		      return but;
         }
         
    }  
    private List<Comments.Stream_Post> mCommentItems;
    public Comments comment;
}
