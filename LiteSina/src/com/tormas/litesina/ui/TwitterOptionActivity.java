package com.tormas.litesina.ui;

import oms.sns.TwitterStatus;
import com.tormas.litesina.R;
import com.tormas.litesina.providers.SocialORM;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TwitterOptionActivity extends TwitterBaseActivity{
    private static final String TAG = "TwitterOptionActivity";
    
    Button replybtn ;
    Button senddmbtn;
    Button retweetbtn;
    Button addfavorbtn;
    Button detailbtn;
    TwitterStatus status = null;
    boolean fromstatus = false;
    boolean fromtweet = false;
    Context mContext;
    long statusid;
    String twitterId;
    String content;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.twitter_option_ui);
        detailbtn = (Button)this.findViewById(R.id.twitter_to_detail_btn);
        replybtn = (Button)this.findViewById(R.id.twitter_reply_btn);
        senddmbtn = (Button)this.findViewById(R.id.twitter_send_dm_btn);
        addfavorbtn = (Button)this.findViewById(R.id.twitter_add_favorite_btn);
        retweetbtn = (Button)this.findViewById(R.id.twitter_retweet_btn);
        mContext = this.getApplicationContext();
        replybtn.setOnClickListener(replyClick);
        senddmbtn.setOnClickListener(senddmClick);
        addfavorbtn.setOnClickListener(addfavoriteClick);
        retweetbtn.setOnClickListener(retweetClick);
        detailbtn.setOnClickListener(detailClick);
        checkTwitterAccount(mContext,orm.getTwitterAccount());
        processIntent(); 
        if(status!=null && status.isFavorited == true)
        {
            addfavorbtn.setText(R.string.twitter_option_un_favorite);
        }
        else
        {
            addfavorbtn.setText(R.string.twitter_option_add_favorite);
        }
    }
    
    private void processIntent() {
        status = (TwitterStatus)getIntent().getParcelableExtra("currentstatus");
        
        if(status != null)
        {
            statusid = status.id;
            twitterId = String.valueOf(status.user.id);
            content = status.text;
        }
        else
        {
            status = (TwitterStatus)getIntent().getParcelableExtra("currenttweet");
            if(status !=null)
            {
                statusid = status.id;
                twitterId = String.valueOf(status.user.screenName);
                content = status.text;
            }
        }
        fromtweet = getIntent().getBooleanExtra("fromtweet", false);
        fromstatus = getIntent().getBooleanExtra("fromstatus",false);
    }
    
    View.OnClickListener detailClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            if(fromstatus == true)
            {
                Intent intent = new Intent(mContext,TwitterTweetsDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentstatus", status);
                intent.putExtra("fromstatus", true);
                mContext.startActivity(intent);
            }
            else if(fromtweet == true)
            {
                Intent intent = new Intent(mContext,TwitterTweetsDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currenttweet", status);
                intent.putExtra("fromtweet", true);
                mContext.startActivity(intent);
            }
        }
    };
    
    View.OnClickListener replyClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
            intent.putExtra(STATUS_ID,     statusid);    
            intent.putExtra(TWITTER_ID,    twitterId); 
            intent.putExtra(REPLY, true);
            startActivityForResult(intent, TWITTER_DONOTHING);
        }
    };
    View.OnClickListener senddmClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Intent intent = new Intent(mContext, TwitterComposeActivity.class);
            intent.putExtra(STATUS_ID,     statusid);    
            intent.putExtra(TWITTER_ID,    twitterId);        
            intent.putExtra(DIRECT, true);
            startActivityForResult(intent, TWITTER_DONOTHING);
        }
    };
    View.OnClickListener addfavoriteClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {  
            if(status.isFavorited == false)
            {
                Message message = basichandler.obtainMessage(TWEET_FAVOR);
                message.getData().putLong(STATUS_ID,       statusid);    
                message.getData().putString(TWITTER_ID,    twitterId); 
                message.sendToTarget();
            }
            else
            {
                Message message = basichandler.obtainMessage(TWEET_UNFAVOR);
                message.getData().putLong(STATUS_ID,       statusid);    
                message.getData().putString(TWITTER_ID,    twitterId); 
                message.sendToTarget();
            }
           
        }
    };
    View.OnClickListener retweetClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Message message = basichandler.obtainMessage(TWITTER_RETWEET);
            message.getData().putLong(STATUS_ID, statusid);
            message.sendToTarget();
        }
    };
    
    private class OptionHandler extends Handler 
    {
        public OptionHandler()
        {
            super();            
            Log.d(TAG, "new OptionHandler");
        }
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case TWEET_FAVOR_END:
                {
                    //set fav btn to unfav btn
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result == true)
                    {
                        if(status != null)
                        {
                            status.isFavorited = true;
                            addfavorbtn.setText(R.string.twitter_option_un_favorite);
                        }
                    }
                    break;
                }
                case TWEET_UNFAVOR_END:
                {
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result == true)
                    {
                        if(status!=null)
                        {
                            status.isFavorited = false;
                            addfavorbtn.setText(R.string.twitter_option_add_favorite);
                        }
                    }
                
                    break;
                }
                
            }
        }
    }
    
    @Override protected void onPause() 
    {
        Log.d(TAG, "onPause");
        super.onPause();
    }    
    @Override protected void onStop() 
    {
        Log.d(TAG, "onStop");
        super.onStop();
    }    
    @Override protected void onDestroy() 
    {  
        Log.d(TAG, "onDestory"); 
        super.onDestroy(); 
    }
    @Override protected void onResume() 
    {
        Log.d(TAG, "onResume");
        super.onResume();
    }  
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    { 
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void createHandler() 
    {
        handler = new OptionHandler();        
    }

    public void setTitle() {
        finalTitle = getString(R.string.twitter_option_title);
    }
    
}
