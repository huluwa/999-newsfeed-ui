package com.msocial.freefb.ui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.FacebookLoginHelper;
import com.msocial.freefb.service.dell.OmsService.OmsHandler;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.StreamFilter;

public class FacebookLoginActivity extends Activity {
	private final String TAG = "FacebookLoginActivity";

	FacebookLoginHelper loghelper;
	String token;
	String loginURL;
	LoginHandler  handler;
	ThreadHandler mHandler;

	View     facebook_info_span;
	TextView facebook_info;
	EditText email;
	EditText pwd;
	Button   login;
	Button   sign_up;
	CheckBox checkbox_sync_phonebook;
	
	SocialORM orm;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.facebook_login_ui);

		// start login sub activity
		loghelper = FacebookLoginHelper.instance(this);
		handler = new LoginHandler();
		
		facebook_info_span = (View)this.findViewById(R.id.facebook_info_span);
		facebook_info      = (TextView)this.findViewById(R.id.facebook_info);
		facebook_info_span.setVisibility(View.VISIBLE);
		orm = SocialORM.instance(this);
		boolean forinvalidsession = getIntent().getBooleanExtra("forinvalidsession", false);
		if(forinvalidsession)
		{
			Log.d(TAG, "for invalid session issue="+this);
			loghelper.clearSesion();
			AccountListener.AccountManager.logout();
		}
		
		//for UI
		email = (EditText)this.findViewById(R.id.facebook_login_email);
		email.setHint(R.string.facebook_email_address);
		pwd = (EditText)this.findViewById(R.id.facebook_login_pwd);
		pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
		pwd.setHint(R.string.facebook_password);
		
		login  = (Button)this.findViewById(R.id.facebook_login_ok_button);
		sign_up = (Button)this.findViewById(R.id.facebook_login_sign_up_button);
		
		Paint p = login.getPaint();
		float width1 = p.measureText(login.getText().toString());
		p = null;
		
		p = sign_up.getPaint();
		float width2 = p.measureText(sign_up.getText().toString());		
		int width = Math.round(Math.max(width1, width1));
		
		width = width+40;
		login.getLayoutParams().width = width;
		sign_up.getLayoutParams().width = width;		
		p = null;
		
		login.setText(R.string.facebook_login_ok);
		sign_up.setText(R.string.facebook_login_sign_up);
		checkbox_sync_phonebook = (CheckBox)this.findViewById(R.id.checkbox_sync_phonebook);
		checkbox_sync_phonebook.setChecked(orm.isEnableSyncPhonebook());
		checkbox_sync_phonebook.setOnCheckedChangeListener(checkedListener);
		
		login.setOnClickListener(loginClick);
		sign_up.setOnClickListener(signupClick);
		
		SocialORM.Account ac = orm.getFacebookAccount();
		email.setText(ac.email);
		pwd.setText(ac.password);
		
		setTitle(R.string.menu_title_login);		
	}
	
    OnCheckedChangeListener checkedListener = new OnCheckedChangeListener()
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
        {  
            Log.d("checked", "is checked="+isChecked);
            if(isChecked == true)
            {
                checkbox_sync_phonebook.setOnCheckedChangeListener(null);
                Message msg = handler.obtainMessage(EBABLE_CHECK);
                msg.getData().putBoolean("fromlogin", true);
                checkbox_sync_phonebook.setChecked(false);
                SyncAddressBookHelper.processSyncAddressBook(FacebookLoginActivity.this, isChecked, null, msg);
            }  
            else
            {
            	orm.EnableSyncPhonebook(false);
            }
        }        
    };
    
    private void hideInputKeyBoard(View view)
    {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);//InputMethodManager.peekInstance();
        if (imm != null && view != null) 
        {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            imm = null;
        }
   }
    
	View.OnClickListener loginClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
		    hideInputKeyBoard(v);
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
				orm.updateFacebookAccount(emailStr, pwdStr);				
			    startThread();
			}
		}
	};
	
	View.OnClickListener cancelClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			FacebookLoginActivity.this.setResult(1000);
			sendMessageToService(false);
			quitLooperAndFinish();
		}
	};
	
	View.OnClickListener signupClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_signup_url)));
             intent.putExtra(Browser.EXTRA_APPLICATION_ID, FacebookLoginActivity.this.getPackageName());                                          
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
             startActivity(intent);
		}
	};
	
	@Override
	public void setTitle(CharSequence title) 
	{		
		super.setTitle(getString(R.string.menu_title_login));
	}

	
	final int NETWORK_UNAVAL =3;
	final int EBABLE_CHECK   =4;
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
                case NETWORK_UNAVAL:
                {
                	FacebookLoginActivity.this.setTitle(R.string.sns_network_unavailable);
                	facebook_info.setText(R.string.sns_network_unavailable);
                	break;
                }
                case EBABLE_CHECK:
                {
                    if(msg.getData().getBoolean("RESULT", false) == true)
                    {
                        checkbox_sync_phonebook.setChecked(true);
                    }
                    else
                    {
                        checkbox_sync_phonebook.setChecked(false);
                    }
                    checkbox_sync_phonebook.setOnCheckedChangeListener(checkedListener);
                    break;
                }
            }
        }
    }
    
    final int LOAD_LOGIN_PAGE=0;	
	final int QUIT_THREAD    =2;	
	final int AUTH_LOGIN     =3;
    private class ThreadHandler extends Handler 
    {
        public ThreadHandler(Looper loop)
        {
            super(loop);            
            Log.d(TAG, "new ThreadHandler loop="+loop);
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case LOAD_LOGIN_PAGE:
	            {
	            	this.post( new Runnable()
	            	{
	            		public void run()
	            		{
	            			launchLoginActivity();
	            		}
	            	});
	            	break;
	            }	           
                case AUTH_LOGIN:
                {
                	auth_login();
                	break;
                }
	            case QUIT_THREAD:
	            {
	            	Log.d(TAG, "QUIT_THREAD");
                    quitLooper();
	            	break;
	            }
            }
        }
    }

    boolean getToken = false;
    Looper mServiceLooper;
    Object mLock = new Object();
    
    //
	//    main thread will wait, until return or user cancel, or it will block the UI, if no network responsing
	//    new a thread to request the token
	//    and let user can cancel the process
	//    
    private synchronized void  startThread()
    {
    	HandlerThread thread = new HandlerThread("LoginThread",Process.THREAD_PRIORITY_BACKGROUND);
    	thread.start();		
		
    	mServiceLooper = thread.getLooper();
		mHandler = new  ThreadHandler(mServiceLooper);
    	
    	Message msd = mHandler.obtainMessage(AUTH_LOGIN);
	    mHandler.sendMessageDelayed(msd, 1*1000);	
    }
    
    void setHint(final String text)
    {
    	handler.post( new Runnable()
		{
			public void run()
			{
		        FacebookLoginActivity.this.setTitle(text);
		        facebook_info.setText(text);
			}
		});
    }
    void auth_login()
    {
    	isInProcess = true;
        setButtonEnable(false); 	
    	Log.d(TAG, "reconstrcut the facebook session");
		final FacebookSession tmps ;
		boolean         persm = false;
		try 
		{
			setHint(getString(R.string.verify_account_hint));
			tmps = loghelper.getDesktopApp().AuthLoginNoSession(loghelper.getEmail(), loghelper.getPwd());	
		}
        catch (FacebookException e) 
        {
            Log.e(TAG, "fail to get facebook session=" + e.getMessage());
            handler.post( new Runnable()
            {
                public void run()
                {
                    facebook_info.setText(R.string.facebook_fail_to_get_session);                       
                    //FacebookLoginActivity.this.setResult(200);
                    //sendMessageToService(false);
                    //quitLooperAndFinish();                   
                }
            });
            mHandler.obtainMessage(QUIT_THREAD).sendToTarget();
            isInProcess = false;
            setButtonEnable(true);
            return ;
        }
        try{
		    persm = tmps.AuthLogin(tmps.getSessionKey(),FacebookLoginHelper.SECRET_KEY, loghelper.getEmail(), loghelper.getPwd());
        }catch(FacebookException e){Log.e(TAG, "Fail to get permanant session, ignore this step.");}
		setHint(getString(R.string.verify_account_suc_hint));	
		
		//get login info in background
		
		// save into database
		String Sessionkey = tmps.getSessionKey();
		String secretKey = tmps.getSecretKey();
		long userId = tmps.getLogerInUserID();
		loghelper.recordTmpSession(Sessionkey, secretKey, userId);
		SocialORM orm = SocialORM.instance(FacebookLoginActivity.this);
		
		if (true == persm) 
		{
			recordPermSession(tmps, orm);
		}
		else
		{
			Log.d(TAG, "fail to get permnant session");							
			if(orm.isUsePermanentSeesion() == false)
			{
				 Log.d(TAG, "fail to get permnant session, use tmp session");
				 recordPermSession(tmps, orm);					
			}
			else
			{
				handler.post( new Runnable()
				{
					public void run()
					{
				        Toast.makeText(FacebookLoginActivity.this,R.string.facebook_get_permanent_session_failed, Toast.LENGTH_LONG).show();
					}
				});
			}
		}
		
		//reset the session and async in activity
		AccountManager.login();
		
		handler.post( new Runnable()
		{
			public void run()
			{
				FacebookLoginActivity.this.setResult(100);
				sendMessageToService(true);
				
			}
		});
		
		handler.post( new Runnable()
		{
			public void run()
			{
				quitLooperAndFinish();
			}
		});
		
		isInProcess = false;
		setButtonEnable(true);
    }    
   
    private void setButtonEnable(final boolean b) {
       handler.post(new Runnable()
       {
            public void run() {
                login.setEnabled(b);
            }  
       });
       
    }

    int count = 0;
    boolean isInProcess = false;
	void launchLoginActivity() 
	{	
	    try 
	    {
	    	setTitle(R.string.facebook_request_token_title);
			facebook_info.setText(R.string.facebook_request_token_title);
			
	    	isInProcess = true;
	    	token = loghelper.getDesktopApp().requestToken();
	    	isInProcess = false;
			getToken = true;
			loginURL = loghelper.getDesktopApp().getLoginUrl(token);
    		
    		if(getToken == false)
    		{
    			Toast.makeText(FacebookLoginActivity.this, R.string.facebook_get_accesstoken_failed, Toast.LENGTH_LONG).show();
    			FacebookLoginActivity.this.setTitle(R.string.sns_network_unavailable);    	
    			facebook_info.setText(R.string.facebook_login_failed_message);
    		}
    		else
    		{
    			Log.d(TAG, "after get token="+token);    			
    			mHandler.obtainMessage(QUIT_THREAD).sendToTarget();
    			Intent intent = new Intent(FacebookLoginActivity.this,FacebookLoginWebViewActivity.class);
    			intent.putExtra("facebook_loginURL", loginURL);
    			startActivityForResult(intent, LOGIN_WEBVIEW);    			
    		}
		}
		catch (FacebookException ne) 
		{
			isInProcess = false;
			Log.d(TAG, "fail to request the token="+ne.getMessage());
			sendMessageToService(false);
			
			count--;			
			if(count >=0)
			{
				Log.d(TAG, "get token again");
				Message msd = mHandler.obtainMessage(LOAD_LOGIN_PAGE);
				mHandler.sendMessageDelayed(msd, 5*1000);
			}			
		}		
			
		if(getToken == false)
		{
			handler.obtainMessage(NETWORK_UNAVAL).sendToTarget();  
		}
		
		Log.d(TAG, "continue");
		if(getToken == true || count <=0)
		{
			mHandler.obtainMessage(QUIT_THREAD).sendToTarget();
		}
		
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming");
        	setResult(1000);
        	quitLooperAndFinish();
			
            System.gc();
                
        }
        return super.onKeyDown(keyCode, event);
    }
	
	private void sendMessageToService(boolean suc) {
		Intent in = new Intent("com.msocial.freefb.getsession");
		in.putExtra("connected", suc);
		FacebookLoginActivity.this.sendBroadcast(in);
	}

	private class GetSessionThread extends Thread
	{
		Handler handler;
		GetSessionThread(Handler handler)
		{
			super();
			this.handler = handler;
		}
		
		public void run()
		{
			handler.post( new Runnable()
			{
				public void run()
				{
			        FacebookLoginActivity.this.setTitle(R.string.facebook_getsession_title);
			        facebook_info.setText(R.string.facebook_getsession_wait_message);
				}
			});
			
			Log.d(TAG, "reconstrcut the facebook session");
			FacebookSession tmps=null;
			try 
			{
				tmps = loghelper.getDesktopApp().requestSession(token);
				
				handler.post( new Runnable()
				{
					public void run()
					{				   
				        facebook_info.setText(R.string.facebook_getsession_successed);
					}
				});
			}
			catch (FacebookException e) 
			{
				Log.e(TAG, "fail to get facebook session=" + e.getMessage());
				handler.post( new Runnable()
				{
					public void run()
					{
						facebook_info.setText(R.string.facebook_fail_to_get_session);						
						FacebookLoginActivity.this.setResult(200);
						sendMessageToService(false);
						quitLooperAndFinish();
					}
				});
				return ;
			}
			// save into database
			String Sessionkey = tmps.getSessionKey();
			String secretKey = tmps.getSecretKey();
			long userId = tmps.getLogerInUserID();
			loghelper.recordTmpSession(Sessionkey, secretKey, userId);
			SocialORM orm = new SocialORM(FacebookLoginActivity.this);

			boolean persm = false;
			try
			{
				handler.post( new Runnable()
				{
					public void run()
					{				   
				        facebook_info.setText(R.string.facebook_make_session_permanent);
					}
				});
			    persm = tmps.AuthLogin(tmps.getSessionKey(),FacebookLoginHelper.SECRET_KEY, loghelper.getEmail(), loghelper.getPwd());
			    
			    handler.post( new Runnable()
				{
					public void run()
					{				   
				        facebook_info.setText(R.string.facebook_make_session_permanent_suc);
					}
				});
			}
			catch(FacebookException kke)
			{
				Log.d(TAG, "fail to get perm session="+kke.getMessage());
			}
			
			if (true == persm) 
			{
				recordPermSession(tmps, orm);
			}
			else
			{
				Log.d(TAG, "fail to get permnant session");							
				if(orm.isUsePermanentSeesion() == false)
				{
					 Log.d(TAG, "fail to get permnant session, use tmp session");
					 recordPermSession(tmps, orm);					
				}
				else
				{
					handler.post( new Runnable()
					{
						public void run()
						{
					        Toast.makeText(FacebookLoginActivity.this, R.string.facebook_get_permanent_session_failed, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
			
			//reset the session and async in activity
			AccountManager.login();
			
			handler.post( new Runnable()
			{
				public void run()
				{
					FacebookLoginActivity.this.setResult(100);
					sendMessageToService(true);
				}
			});
			
			handler.post( new Runnable()
			{
				public void run()
				{
					quitLooperAndFinish();
				}
			});			
		}
	}
	
	
	private final int LOGIN_WEBVIEW = 0;
	private final int FACEBOOK_SETTING = 1;

	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent intent) 
	{
		switch (requestCode) 
		{
		    case LOGIN_WEBVIEW:
		    {
				Log.d(TAG, "come back from WebView");				
				
				boolean failtologin= true;
				if (resultCode == 100 || resultCode == 101)// reset the session
				{
					failtologin = false;					
					//try to get session
					GetSessionThread sessionThread = new GetSessionThread(handler);					
					sessionThread.start();					
				} 
				else if(resultCode == 1000)
				{
					Log.d(TAG, "user choose to exit the login process");
					FacebookLoginActivity.this.setResult(1000);
					sendMessageToService(false);					
				}
				else 
				{
					// TODO
					// FAIL to login?
					Log.d(TAG, "fail to login in resultCode="+resultCode);
					FacebookLoginActivity.this.setResult(200);
					sendMessageToService(false);
				}
				
				if(failtologin)
				{
					quitLooperAndFinish();
				}
				
				break;
		    }			
			case FACEBOOK_SETTING: 
			{
				Log.d(TAG, "after FACEBOOK_SETTING");
				
				//reset the UI from database
				if(resultCode == 1000)
				{
				    SocialORM.Account ac = orm.getFacebookAccount();
				    email.setText(ac.email);
				    pwd.setText(ac.password);
				}
				//startThread();				
				break;
			}
		}
	}
	
	private void quitLooper()
	{
		if(mServiceLooper != null)
    	{
    	    mServiceLooper.quit();
    	    mServiceLooper = null;
    	}	
	}
	
	private void quitLooperAndFinish()
	{
		if(mServiceLooper != null)
    	{
    	    mServiceLooper.quit();
    	    mServiceLooper = null;
    	}
		FacebookLoginActivity.this.finish();
	}
	void recordPermSession(FacebookSession tmps, SocialORM orm)
	{
		 loghelper.recordPermanantSession(tmps.getSessionKey(),tmps.getSecretKey(), tmps.getLogerInUserID());         
         //clear user data
         Log.d(TAG, "clear the pre-user data, this will need some time");			            
         //if the account is not changes, still have no necessary to clear the data
         String pre_account = orm.getSettingValue(SocialORM.pre_account);
         SocialORM.Account ac = orm.getFacebookAccount();
         if(pre_account == null || pre_account.equalsIgnoreCase(ac.email))
         {
         	Log.d(TAG, "we are the same account="+pre_account);
         	//if it is the same account, no need to remove the cache data,
         	//if the user want to clear the history data, 
         	//she/he can use the "advanced setting" to reset the database
         }
         else
         {
         	Log.d(TAG, "we are different account="+pre_account);
             orm.clearCurrentUserData();
             TwitterHelper.ClearCache();
         }			            
         //update pre account
         orm.addSetting(SocialORM.pre_account, ac.email);    		
	}
	
	@Override
	protected void onNewIntent(Intent intent) 
	{		
		//super.onNewIntent(intent);
		
		Log.d(TAG, "onNewIntent why come here ="+intent);
	}

	final static int DLG_GET_TOKOEN=1;
    @Override
    protected Dialog onCreateDialog(int id) 
    {
        switch (id) {
            case DLG_GET_TOKOEN: 
            {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_request_accesstoken_title);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);                
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
        }
        return null;
    }
    
    @Override protected void onPause() 
    {
    	Log.d(TAG, "onPause");
        //quit thread        
        if(mHandler != null)
        {
            if(mServiceLooper != null)
            {
                mHandler.obtainMessage(QUIT_THREAD).sendToTarget();   
            }         
        }
        
        super.onPause();
    }    
    @Override protected void onStop() 
    {
    	Log.d(TAG, "onStop");
        //quit thread        
        if(mHandler != null)
        {
            if(mServiceLooper != null)
            {

                mHandler.obtainMessage(QUIT_THREAD).sendToTarget();  
            }       
        }
        super.onStop();
    }    
    @Override protected void onDestroy() 
    {    
        super.onDestroy();    
        
    	Log.d(TAG, "onDestory");
        //quit thread        
        if(mHandler != null)
        {
            if(mServiceLooper != null)
            {
                mHandler.obtainMessage(QUIT_THREAD).sendToTarget();    
            }
        }        
    }    
}
