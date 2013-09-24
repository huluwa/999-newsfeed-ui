package com.tormas.litesina.ui;

import com.tormas.litesina.R;
import com.tormas.litesina.providers.SocialORM;
import com.tormas.litesina.ui.AccountListener.AccountManager;
import com.tormas.litesina.ui.TwitterAccountListener.TwitterAccountManager;

import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

import oms.sns.service.facebook.util.StringUtils;
import twitter4j.AsyncTwitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
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
    
    
    private static final String CONSUMER_KEY = "2359031321";// 替换为开发者的appkey，例如"1646212960";
	private static final String CONSUMER_SECRET = "ccce25d219a325d27e67369b58e2d9a8";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        
        Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);

		// Oauth2.0
		// 隐式授权认证方式
		weibo.setRedirectUrl("http://www.borqs.com");// 此处回调页内容应该替换为与appkey对应的应用回调页
		// 对应的应用回调页可在开发者登陆新浪微博开发平台之后，
		// 进入我的应用--应用详情--应用信息--高级信息--授权设置--应用回调页进行设置和查看，
		// 应用回调页不可为空

		weibo.authorize(TwitterLoginActivity.this, new AuthDialogListener());
        
        setTitle(R.string.twitter_main_screen);
    }
    

    //weibo.getAccessToken().getToken(), weibo.getAccessToken().getSecret()
	class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");	
			String uid = values.getString("uid");
			com.weibo.net.AccessToken accessToken = new AccessToken(token, CONSUMER_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);
			
			 
            //orm.updateTwitterScreenname(accessToken..screenName);
			SocialORM orm = new SocialORM(TwitterLoginActivity.this);
			
			orm.updateTwitterUID(uid);
			orm.updateTwitterScreenname(uid);
            orm.updateTwitterToken(accessToken.getToken());
            orm.updateTwitterTokenSecret(accessToken.getSecret());
             
            //notify twitter account listener
            TwitterAccountManager.login();
            TwitterLoginActivity.this.setResult(200);
            TwitterLoginActivity.this.finish();    
             
			
			Intent intent = new Intent();
			intent.setClass(TwitterLoginActivity.this, TwitterTweetsActivity.class);
			startActivity(intent);
		}

		@Override
		public void onError(DialogError e) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}
      
    
/*  

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
                  

  */  
   
    
}
