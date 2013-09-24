package com.ast.free.ui.view;

import java.util.Date;
import java.util.List;

import com.ast.free.R;
import com.ast.free.providers.SocialORM;
import com.ast.free.ui.FacebookAccountActivity;
import com.ast.free.ui.FacebookBaseActivity;
import com.ast.free.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.Stream.Comments;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentItemView extends SNSItemView {
    private final String TAG="CommtentItemView";
    
    private TextView                username;   
    private ImageView               userLogo;
    private TextView                message;
    private TextView                publishTime;
    
    private Comments.Stream_Post    comment;
    private PhotoComment            photocomment;
    private Context mContext;
    private SocialORM  orm;
    
    private boolean forPhotoComment = false;
    
    public Comments.Stream_Post getComment() {
        return comment;
    }

    public void setComment(Comments.Stream_Post comment) {
        this.comment = comment;
    }

    private FacebookUser            user;
    
    CommtentInternalHandler internalhandler;
    
    public CommentItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;  
        orm = SocialORM.instance(ctx);
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public CommentItemView(Context context, Comments.Stream_Post di) 
    {       
        super(context);
        mContext = context;
        comment = di;
        forPhotoComment = false;
        orm = SocialORM.instance(context);
        Log.d(TAG, "call CommtentItemView");
        internalhandler = new CommtentInternalHandler();
        init();
    }   
        
    public CommentItemView(Context context, PhotoComment di) {
        super(context);
        mContext = context;
        photocomment = di;
        forPhotoComment = true;
        orm = SocialORM.instance(context);
        Log.d(TAG, "call CommtentItemView");
        internalhandler = new CommtentInternalHandler();
        init();
    }

    private void init() 
    {
        Log.d(TAG,  "call CommtentItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //container
        
        FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);            
        FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);     
        view.setLayoutParams(paras);
        view.setVerticalScrollBarEnabled(true);
        addView(view);
        
        
        //child 1
        View v  = factory.inflate(R.layout.sns_common_message_view, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        view.addView(v);            
        
        userLogo     = (ImageView)v.findViewById(R.id.tweet_img_ui);            
        publishTime  = (TextView)v.findViewById(R.id.tweet_publish_time);
        message      = (TextView)v.findViewById(R.id.tweet_publish_text);       
        username     = (TextView)v.findViewById(R.id.tweet_user_name);
        
        userLogo.setOnClickListener(viewUserDetailsClick);
        username.setOnClickListener(viewUserDetailsClick);
        setCommentsUI();        
    
    }   
    
    View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Log.d(TAG, "viewUserDetailsClick you click first one=");    
            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
            if(user != null)
            {
                intent.putExtra("uid",      user.uid);
                intent.putExtra("username", user.name);
                intent.putExtra("imageurl", user.pic_square);               
            }
            else
            {
                intent.putExtra("uid",      comment.fromid);
                intent.putExtra("username", String.valueOf(comment.fromid));              
            }           
            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
        }
    };
    
    private long getFromUID()
    {
        if(forPhotoComment == true)
        {
            return photocomment.from;
        }
        else
        {
            return comment.fromid;
        }
    }
    
    private void setCommentsUI()
    {
        boolean getuserInfoFromWeb=false;
        userLogo.setImageResource(R.drawable.no_avatar);

        user = orm.getFacebookUser(getFromUID());
        if(user != null)
        {
            username.setText(user.name);            
        }
        else
        {
            String uname = "";
            if(forPhotoComment)
            {
                ;
            }
            else
            {
                if(comment!=null && isEmpty(comment.username) == false)
                {
                    username.setText(comment.username);
                }
                else
                {
                    username.setText(String.valueOf(getFromUID()));
                }
            }
        	
            getuserInfoFromWeb = true;
        }
        
        //don't call for each user
        if(forPhotoComment == false)
        {
            getuserInfoFromWeb = false;
        }

        if(getuserInfoFromWeb)
        {
            AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
            if(af != null)
            {
                long[] uids = new long[1];
                uids[0] = getFromUID();
                af.getBasicUsersAsync(uids, new FacebookAdapter()
                {
                    @Override public void getUsers(List<FacebookUser> users)
                    {
                        if(users != null && users.size()>0)
                        {
                            user = users.get(0);
                            Log.d(TAG, "after get user info="+user);
                            getCommentLogoImageBMP(user.pic_square, true);                                  
                            //update database
                            orm.addFacebookUser(user);                                  
                            updateCommentsUIFromUser();
                        }
                    }
                    
                    @Override public void onException(FacebookException e, int method) 
                    {
                        Log.d(TAG, "fail to get the image");
                        getCommentLogoImageBMP(null, true);             
                    }
                });
            }
        }
        else
        {
            if(user != null)
            {
                getCommentLogoImageBMP(user.pic_square, false);
            }
        }
        
        publishTime.setText(DateUtil.converToRelativeTime(mContext, new Date(getCreateTime())));       
        message.setText(getCommentText());
    }
    
    private long getCreateTime()
    {
        if(forPhotoComment == true)
        {
           return photocomment.time;
        }
        else
        {
            return comment.time;
        }
    }
    
    private String getCommentText()
    {
        if(forPhotoComment == true)
        {
            return photocomment.body;
        }
        else
        {
            return comment.text;
        }
    }
    

    private void getCommentLogoImageBMP(String url, boolean fromAnotherThread)
    {
        if(fromAnotherThread == true)
        {
            Message msg = internalhandler.obtainMessage(UPDATE_IMAGE_UI);
            msg.getData().putString("imageurl", url);
            msg.getData().putBoolean("forcomments", true);
            internalhandler.sendMessage(msg);
        }
        else//from the same thread
        {
            ImageRun imagerun = new ImageRun(internalhandler, url, 0);  
            imagerun.use_avatar = true;
            imagerun.setImageView(userLogo);
            imagerun.post(imagerun);
        }
    }
    
    private void updateCommentsUIFromUser()
    {
        internalhandler.obtainMessage(UPDATE_COMMENT_UI).sendToTarget();
    }
    
    final int UPDATE_COMMENT_UI=0;
    final int UPDATE_IMAGE_UI  =1;
    public class CommtentInternalHandler extends Handler
    {
        public CommtentInternalHandler()
        {
            super();            
            //Log.d(TAG, "new CommtentHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case UPDATE_COMMENT_UI:
                {          
                    if(user != null)
                    {   
                        username.setText(user.name);
                    }
                    break;
                }
                case UPDATE_IMAGE_UI:
                {
                    String url = msg.getData().getString("imageurl");                   
                    ImageRun imagerun = new ImageRun(internalhandler, url, 0);  
                    imagerun.use_avatar = true;
                    imagerun.setImageView(userLogo);                    
                    imagerun.post(imagerun);
                    break;
                }
            }
        }           
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
        //Log.d(TAG, "what is me="+di);
        setCommentsUI();
    }       
    public void chooseCommentListener() 
    {           
        
    }

    public void setCommentItem(PhotoComment di) {
        photocomment = di;
        if(user!=null)
        {
            user.despose();
            user = null;
        }
        setCommentsUI();        
    }

	@Override
	public String getText() 
	{		
		return comment.text;
	}
}
