package com.msocial.nofree.ui;

import java.util.List;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.view.FacebookMailItemView;
import com.msocial.nofree.ui.view.ImageRun;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxMessage;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class FacebookSingleMailDetailActivity extends FacebookBaseActivity{
   
private final String TAG="FacebookSignalMailDetailActivity";
    
    private ImageView imageView;
    private TextView publishDate;
    private TextView publishTxt;
    private TextView username;  
    private String   imageURL;
    private MailboxMessage msg;
    FacebookUser user;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_mail_item_ui);
        imageView  = (ImageView)findViewById(R.id.tweet_img_ui);
        publishDate  = (TextView)findViewById(R.id.tweet_publish_time);
        publishTxt   = (TextView)findViewById(R.id.tweet_publish_text);   
        publishTxt.setMovementMethod(LinkMovementMethod.getInstance());
        publishTxt.setLinksClickable(true);
        username     = (TextView)findViewById(R.id.tweet_user_name);
        username.setTextAppearance(this, R.style.sns_link);
        showUI(getIntent());
        
        setTitle(R.string.facebook_mail_one_detail);       
     }
    
    private void showUI(Intent intent) {
        msg = intent.getParcelableExtra("mailboxmessage");
        
        if(msg != null)
        {
            Log.d(TAG,"msg info is author="+msg.author +"& body="+"msg.body & time="+msg.timesent.getTime());
            username.setText(getViewUserName());
            username.setOnClickListener(viewUserDetailsClick);
            setImage();
            imageView.setOnClickListener(viewUserDetailsClick);
        
            publishDate.setText(getDate());        
            publishTxt.setText(getText());
        }
        else
        {
           Log.d(TAG," why msg is null");
        }
        
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
            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
        }
    };

    public String getViewUserName() 
    {
    
        FacebookUser user = orm.getFacebookUser(getFromUID());
        if(user != null)
        {
            return user.name;
        }       
        return String.valueOf(getFromUID());        
    }
    
    public String getUserName() 
    {
        return getViewUserName();
    }
    
    public long getFromUID()
    {
        return msg.author;      
    }
    
    private void setImage()
    {   
        if(imageURL == null)
        {
            long id = getFromUID();         
            user = orm.getFacebookUser(id);         
            
            boolean getFromDB=false;
            if(user == null)
            {
                getFromDB = true;
            }
            else
            {
                Log.d(TAG, "who am I="+user);
                imageURL = user.pic_square;
                //no user data, maybe the user has image
                if((imageURL == null || imageURL.length() ==0 ) && (user.name == null || user.name.length() == 0))
                {
                    getFromDB = true;
                }
                
            }               
            //the person might have no pic, so no need to call this fun
            if(getFromDB == true)
            {   
                if(FacebookBaseActivity.class.isInstance(mContext))
                {
                    AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
                    if(af != null)
                    {
                        long[] uids = new long[1];
                        uids[0] = id;
                        af.getBasicUsersAsync(uids, new FacebookAdapter()
                        {
                            @Override public void getUsers(List<FacebookUser> users)
                            {
                                if(users != null && users.size()>0)
                                {
                                    Log.d(TAG, "after get user info="+user);
                                    user = users.get(0);
                                    imageURL = user.pic_square;
                                    getImageBMP(imageURL, true);
                                    
                                    //update database
                                    orm.addFacebookUser(user);
                                    
                                    updateUIFromUser();
                                }
                            }
                            
                            @Override public void onException(FacebookException e, int method) 
                            {
                                Log.d(TAG, "fail to get the image");
                                getImageBMP(null, true);            
                            }
                        });
                    }
                }
                
            }
            else
            {   
                getImageBMP(imageURL, false);
            }
            
        }
        else//I have get the image
        {
            getImageBMP(imageURL, false);
        }
    }
    
    private void updateUIFromUser()
    {
        handler.obtainMessage(UPDATE_UI).sendToTarget();
    }
    
    private void getImageBMP(String url, boolean fromAnotherThread)
    {
        if(fromAnotherThread == true)
        {
            Message msg = handler.obtainMessage(UPDATE_IMAGE_UI);
            msg.getData().putString("imageurl", url);
            handler.sendMessage(msg);
        }
        else//from the same thread
        {
            ImageRun imagerun = new ImageRun(handler, url, 0);      
            imagerun.use_avatar = true;
            imagerun.setImageView(imageView);
            imagerun.post(imagerun);
        }
    }
    private String getDate()
    {
        return msg.timesent.toLocaleString();
    }
    public String getText()
    {
        return msg.body;
    }

    @Override
    protected void createHandler() {
        handler = new FacebookSingleMailHandler();
    }

    public void setTitle() 
    {
    	title = getString(R.string.facebook_mail_one_detail);
    }
    
    private final int UPDATE_UI       = 1;
    private final int UPDATE_IMAGE_UI = 2;
    private class FacebookSingleMailHandler extends Handler{
        
        public FacebookSingleMailHandler()
        {
            super();            
            Log.d(TAG, "new FacebookSignalMailHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case UPDATE_UI:
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
                    ImageRun imagerun = new ImageRun(handler, url, 0);      
                    imagerun.use_avatar = true;
                    imagerun.setImageView(imageView);
                    imagerun.post(imagerun);
                    break;
                }
            }
        }
    }
}
