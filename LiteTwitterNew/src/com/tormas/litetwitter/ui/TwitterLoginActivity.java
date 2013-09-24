package com.tormas.litetwitter.ui;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.ui.AccountListener.AccountManager;
import com.tormas.litetwitter.ui.TwitterAccountListener.TwitterAccountManager;
import oms.sns.service.facebook.util.StringUtils;
import twitter4j.AsyncTwitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.http.AccessToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterLoginActivity extends Activity{
    private static final String TAG = "TwitterLoginActivity";
    private static final int LOAD_TWITTER_VERIFY = 10;
    EditText email;
    EditText pwd;
    Button login;
    SocialORM orm;
    Handler handler;
    TextView sign_up_view;
    View twitter_info_span;
    TextView twitter_info;
    boolean aminprocess = false;
    boolean isRightAccount = false;
    AsyncTwitter tw;
    AccessToken token;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.twitter_login_ui);
        email = (EditText)this.findViewById(R.id.twitter_login_email);
        pwd = (EditText)this.findViewById(R.id.twitter_login_pwd);
        twitter_info_span = (View)this.findViewById(R.id.twitter_info_span);
        twitter_info = (TextView)this.findViewById(R.id.twitter_info);
        sign_up_view = (TextView)this.findViewById(R.id.twitter_sign_up_view);
        login = (Button)this.findViewById(R.id.twitter_login_ok_button);
        orm = SocialORM.instance(this.getApplicationContext());
        handler = new LoginHandler();
        pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        email.setHint(R.string.twitter_login_email_hint);
        pwd.setHint(R.string.twitter_login_pwd_hint);
        login.setOnClickListener(loginClick); 
        
        Paint p = login.getPaint();
        float width1 = p.measureText(login.getText().toString());
        p = null;    
        int width = Math.round(width1); 
        width = width+40;
        login.getLayoutParams().width = width;      
        sign_up_view.setOnClickListener(signupClick);
        setTitle(R.string.twitter_main_screen);
    }
    
    void setHint(final String text)
    {
        handler.post( new Runnable()
        {
            public void run()
            {
                twitter_info.setText(text);
            }
        });
    }
    private void hideInputKeyBoard(View view)
    {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);//InputMethodManager.peekInstance();
        if (imm != null && view != null) 
        {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            imm = null;
        }
   }
    View.OnClickListener signupClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.twitter_signup_url)));
             intent.putExtra(Browser.EXTRA_APPLICATION_ID, TwitterLoginActivity.this.getPackageName());                                          
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
             startActivity(intent);
        }
    };
    
    View.OnClickListener loginClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            hideInputKeyBoard(v);
            setHint("");
            //validate the input
            String emailStr = email.getText().toString().trim();
            String pwdStr = pwd.getText().toString().trim();
            if(emailStr.length() == 0 || pwdStr.length() == 0)
            {
                setHint("Please input email and password.");
            }
            else
            {
                //update to db
                //orm.updateFacebookAccount(emailStr, pwdStr);   
                Message message = handler.obtainMessage(TWITTER_VERIFY_USER);
                message.getData().putString("email", emailStr);
                message.getData().putString("pwd", pwdStr);
                message.sendToTarget();
            }
        }
    };
    
    private void verifyAccount(final String email,final String pwd)
    {
    	 if(aminprocess == true)
         {
             Log.d(TAG, "i am check account, just return");
             return;
         }
         aminprocess = true;
         setButtonEnable(false);
         showDialog(LOAD_TWITTER_VERIFY);
         tw = new AsyncTwitter("","",true);
         //tw.setOAuthConsumer(null, null);   
         //tw.setUserId();
         tw.setUserId(email);
         tw.setPassword(pwd);
         tw.authLogin(new TwitterAdapter()
         {

			@Override
			public void authLogin(AccessToken accessToken) 
			{
				token = accessToken;
                if(token != null)
                {
                    isRightAccount = true; 
                }
                else
                {
                    isRightAccount = false;
                }
                
                aminprocess = false;
                Message msd = handler.obtainMessage(TWITTER_VERIFY_USER_END);
                msd.getData().putString("email", email);
                msd.getData().putString("pwd", pwd);
                msd.getData().putBoolean("result", isRightAccount);
                handler.sendMessage(msd);         
			}
			
			@Override public void onException(TwitterException e, int method) 
            {                   
	             Log.d(TAG, "Fail to get ="+e.getMessage());
	             isRightAccount = false; 
	             
	             aminprocess = false;
	             
	             Message msd = handler.obtainMessage(TWITTER_VERIFY_USER_END);
	             msd.getData().putBoolean("result", isRightAccount);
	             msd.getData().putString("errormsg", e.getMessage());
	             handler.sendMessage(msd);
            }           
        	 
         });
         
        /* tw.getUserVerifyAsync(new TwitterAdapter()
         {
             @Override public void gotUserDetail(User user )
             {
                 tUser = user;
                 if(tUser != null)
                 {
                     isRightAccount = true; 
                 }
                 else
                 {
                     isRightAccount = false;
                 }
                 
                 aminprocess = false;
                 Message msd = handler.obtainMessage(TWITTER_VERIFY_USER_END);
                 msd.getData().putString("email", email);
                 msd.getData().putString("pwd", pwd);
                 msd.getData().putBoolean("result", isRightAccount);
                 handler.sendMessage(msd);                                     
             }

             @Override public void onException(TwitterException e, int method) 
             {                   
                 Log.d(TAG, "Fail to get ="+e.getMessage());
                 isRightAccount = false; 
                 
                 aminprocess = false;
                 
                 Message msd = handler.obtainMessage(TWITTER_VERIFY_USER_END);
                 msd.getData().putBoolean("result", isRightAccount);
                 msd.getData().putString("errormsg", e.getMessage());
                 handler.sendMessage(msd);
             }           
         });*/
    }
    
   /* private void verifyAccount(final String email,final String pwd)
    {
        if(aminprocess == true)
        {
            Log.d(TAG, "i am check account, just return");
            return;
        }
        aminprocess = true;
        setButtonEnable(false);
        showDialog(LOAD_TWITTER_VERIFY);
        tw = new AsyncTwitter(email, pwd);
        tw.setUseHttps(orm.getTwitterUseHttps());
        //tw.setUserId();
        tw.getUserVerifyAsync(new TwitterAdapter()
        {
            @Override public void gotUserDetail(User user )
            {
                tUser = user;
                if(tUser != null)
                {
                    isRightAccount = true; 
                }
                else
                {
                    isRightAccount = false;
                }
                
                aminprocess = false;
                Message msd = handler.obtainMessage(TWITTER_VERIFY_USER_END);
                msd.getData().putString("email", email);
                msd.getData().putString("pwd", pwd);
                msd.getData().putBoolean("result", isRightAccount);
                handler.sendMessage(msd);                                     
            }

            @Override public void onException(TwitterException e, int method) 
            {                   
                Log.d(TAG, "Fail to get ="+e.getMessage());
                isRightAccount = false; 
                
                aminprocess = false;
                
                Message msd = handler.obtainMessage(TWITTER_VERIFY_USER_END);
                msd.getData().putBoolean("result", isRightAccount);
                msd.getData().putString("errormsg", e.getMessage());
                handler.sendMessage(msd);
            }           
        });
    }*/
    private final int TWITTER_VERIFY_USER     = 1;
    private final int TWITTER_VERIFY_USER_END = 2;
    private class LoginHandler extends Handler 
    {
        public LoginHandler()
        {
            super();            
            Log.d(TAG, "new LoginHandler");
        }
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case TWITTER_VERIFY_USER:
                {
                    String email = msg.getData().getString("email");
                    String pwd = msg.getData().getString("pwd");
                    verifyAccount(email,pwd);
                    break;
                }
                case TWITTER_VERIFY_USER_END:
                {
                    boolean result = msg.getData().getBoolean("result");
                    String errormsg = msg.getData().getString("errormsg");  
                    dismissDialog(LOAD_TWITTER_VERIFY);
                    setButtonEnable(true);
                    if(result == true)
                    {           
                      //TODO write username,email,pwd,uid to DB 
                        String email = msg.getData().getString("email");
                        String pwd = msg.getData().getString("pwd");
                        orm.updateTwitterAccount(email, pwd);
                        orm.updateTwitterUID(token.userId);
                        orm.updateTwitterScreenname(token.screenName);
                        orm.updateTwitterToken(token.getToken());
                        orm.updateTwitterTokenSecret(token.getTokenSecret());
                        //notify twitter account listener
                        TwitterAccountManager.login();
                        TwitterLoginActivity.this.setResult(200);
                        TwitterLoginActivity.this.finish();    
                    }
                    else
                    {
                        String toastMsg = "";
                        
                       // if(StringUtils.isEmpty(errormsg) == true)
                        //{
                            toastMsg = getString(R.string.wrong_username_pwd);
                       // }
                        Toast.makeText(TwitterLoginActivity.this,toastMsg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }
    
    private void setButtonEnable(final boolean b) {
        handler.post(new Runnable()
        {
             public void run() {
                 login.setEnabled(b);
             }  
        });
        
     }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
            Log.d(TAG, "KEYCODE_BACK coming");
            setResult(1000);
            TwitterLoginActivity.this.finish();
            System.gc();
                
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case LOAD_TWITTER_VERIFY: 
            {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.twitter_verify_account_title);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);                
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.setInverseBackgroundForced(false);
                return dialog;
            }
        }
        
        return null;
    }
    
}
