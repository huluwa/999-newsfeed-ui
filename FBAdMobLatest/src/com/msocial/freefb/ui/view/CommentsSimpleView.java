package com.msocial.freefb.ui.view;

import java.util.Date;
import java.util.List;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Stream.Comments;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.msocial.freefb.R;
import com.msocial.freefb.util.DateUtil;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.FacebookBaseActivity;

public class CommentsSimpleView extends SNSItemView {
    private final String TAG="CommentsSimpleView";
    
    private TextView                username;
    private TextView                message;
    private TextView                publishTime;
    private Context                 mContext;
    private Comments.Stream_Post    comment;
    private SocialORM  orm;
    private Handler    mHandler;
    
    public CommentsSimpleView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;
        orm = SocialORM.instance(ctx);
        mHandler = new Handler();
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public CommentsSimpleView(Context context, Comments.Stream_Post di) 
    {       
        super(context);
        mContext = context;
        orm = SocialORM.instance(context);
        mHandler = new Handler();
        //Log.d(TAG, "call CommentsSimpleView");
        init();
    }
    
    public Comments.Stream_Post getComment() {
        return comment;
    }

    public void setComment(Comments.Stream_Post comment) {
        this.comment = comment;
    }

    private void init() 
    {
        //Log.d(TAG,  "call CommentsSimpleView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View v  = factory.inflate(R.layout.comments_simple_item, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(v);            
        
        publishTime  = (TextView)v.findViewById(R.id.tweet_publish_time);
        message      = (TextView)v.findViewById(R.id.tweet_publish_text);       
        username     = (TextView)v.findViewById(R.id.tweet_user_name);
        
        setCommentsUI();        
    
    }
    
    private FacebookUser   user;
    private void setCommentsUI()
    { 
    	if(comment != null)
    	{
    		boolean getuserInfoFromWeb = false;
	        if(comment!=null && isEmpty(comment.username) == false)
	        {
	            username.setText(comment.username);
	        }
	        else
	        {
	        	user = orm.getFacebookUser(comment.fromid);
	            if(user != null)
	            {
	                username.setText(user.name);            
	            }
	            else
	            {
	                username.setText(String.valueOf(comment.fromid));
	                getuserInfoFromWeb = true;
	            }
	            
	            if(getuserInfoFromWeb)
	            {
	                AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
	                if(af != null)
	                {
	                    long[] uids = new long[1];
	                    uids[0] = comment.fromid;
	                    af.getBasicUsersAsync(uids, new FacebookAdapter()
	                    {
	                        @Override public void getUsers(List<FacebookUser> users)
	                        {
	                            if(users != null && users.size()>0)
	                            {
	                                user = users.get(0);                                                
	                                //update database
	                                orm.addFacebookUser(user);                                  
	                                mHandler.post(new Runnable(){
	                                	public void run()
	                                	{
	                                		try{
	                                		    username.setText(user.name);
	                                		}catch(Exception ne){}
	                                	}
	                                });
	                            }
	                        }
	                        
	                        @Override public void onException(FacebookException e, int method) 
	                        {
	                            Log.d(TAG, "fail to get the image");	                                         
	                        }
	                    });
	                }
	            }
	        }
	        
	        
	        publishTime.setText(DateUtil.converToRelativeTime(mContext, new Date(getCreateTime())));       
	        message.setText(getCommentText());
    	}
    }
    
    private long getCreateTime()
    {
        return comment.time;        
    }
    
    private String getCommentText()
    {
        return comment.text;        
    }  
   
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    public void setCommentItem(Comments.Stream_Post di) 
    {
        comment = di;
        setCommentsUI();
    }   

	@Override
	public String getText() 
	{		
		return comment.text;
	}
}
