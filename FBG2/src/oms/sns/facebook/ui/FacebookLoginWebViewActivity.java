package oms.sns.facebook.ui;

import java.util.HashMap;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.Toast;

public class FacebookLoginWebViewActivity extends Activity {
    private final String TAG="FacebookLoginWebViewActivity";
	private String loginURL;
	private WebView mWebView;
	private WebViewDatabase wDb;
    private SocialORM orm;
    
	LoginHandler handler;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
       
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        loginURL = getIntent().getCharSequenceExtra("facebook_loginURL").toString();
        Log.d(TAG, "create FacebookLoginWebViewActivity");
        
        setContentView(R.layout.facebook_login_webview);
    	mWebView = (WebView)findViewById(R.id.w_view);
    	

    	orm = new SocialORM(this);
    	
    	this.setTitle(loginURL);
    	handler = new LoginHandler();
        loadLoginPage();        
    }
    
    void loadLoginPage()
    {
    	Message msg = handler.obtainMessage(UI_LOAD_PAGE);
    	handler.sendMessageDelayed(msg, 2*1000);
    }
    final int UI_LOAD_PAGE=0;
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
	            case UI_LOAD_PAGE:
	            {
	            	constructUI();
	            	break;
	            }
            }
        }
    }  
      
    private void constructUI()
    {          
    	//if user reenter the page,
    	wDb = WebViewDatabase.getInstance(this);
    	
        mWebView.setWebViewClient(mWebViewClient);        
        mWebView.setWebChromeClient(mWebChromeClient);
        
        loadURL();   
    }
    
    void loadURL()
    {
    	wDb.clearFormData();
        wDb.clearHttpAuthUsernamePassword();
        wDb.clearUsernamePassword();
        
        mWebView.clearCache(true);
        mWebView.clearFormData();
        mWebView.clearMatches();
            
        //when ready in 1.4, will add these code    
        SocialORM.Account ac = orm.getFacebookAccount();
        //wDb.setFacebookUsernamePassword(ac.email, ac.password);
        //wDb.setFacebookUsernamePassword("httpwww.facebook.com", ac.email, ac.password);
        //wDb.setFacebookUsernamePassword("httpswww.facebook.com", ac.email, ac.password);
        
        HashMap<String, String> formmap = new HashMap<String, String>();
        formmap.put("email", ac.email);     
        //wDb.setFacebookFormData(formmap);
        //wDb.setFacebookFormData(loginURL, formmap);
        //end for 1.4        
        mWebView.loadUrl(loginURL);         
    }
    
    
    @Override protected void onDestroy() 
    {    
    	Log.d(TAG, "onDestrory="+this);
        super.onDestroy();        
       // mWebView.destroy(); 
    }
    
    private final WebChromeClient mWebChromeClient = new WebChromeClient() 
    {
    	 @Override
         public void onProgressChanged(WebView view, int newProgress) 
    	 {            
             setProgress(newProgress*100);             
         }
    }; 
    	
    private final WebViewClient mWebViewClient = new WebViewClient(){
        // return true if want to hijack the url to let another app to handle it
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	if(url.contains("desktopapp.php") == true)
        	{
        		Log.d(TAG, "Login suc shouldOverrideUrlLoading");
        		
        		view.stopLoading();
        		
        		Toast.makeText(FacebookLoginWebViewActivity.this, R.string.sns_login_succeed, Toast.LENGTH_SHORT).show();
        		
        		Intent data = new Intent();
        	 	FacebookLoginWebViewActivity.this.setResult(101, data.putExtra("succeed", true));        		
        	 	FacebookLoginWebViewActivity.this.finish();        	 	
        	}
        	Log.d(TAG, "load page="+url);
        	
        	return false;
        }
        @Override 
        public void onReceivedSslError(android.webkit.WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error)
        {
        	Log.e(TAG, "meet ssl issue");
        	handler.proceed();        	
        }
        
        @Override
        public void onPageFinished(android.webkit.WebView view, java.lang.String url)
        {
        	if(url.contains("desktopapp.php") == true)
        	{
        		Log.d(TAG, "Login suc onPageFinished");
        		Toast.makeText(FacebookLoginWebViewActivity.this, R.string.sns_login_succeed, Toast.LENGTH_SHORT).show();
        		
        	 	Intent data = new Intent();
        	 	FacebookLoginWebViewActivity.this.setResult(100, data.putExtra("succeed", true));
        	 	FacebookLoginWebViewActivity.this.finish();
        	}
        }
    };
    
    @Override
	 public boolean onPrepareOptionsMenu(Menu menu) 
	 {	
	      super.onPrepareOptionsMenu(menu);
	      return true;
	 }	
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
          switch(requestCode)
          {  
                case 100:
                {
                    if(resultCode == 1000)
                    {
                        Log.d(TAG, "account is changed");
                        loadURL();
                    }                   
                    break;
                }
          }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.facebook_login_menu_settings:
            {
            	Intent intent = new Intent(this, FacebookSettingPreference.class);            
        		startActivityForResult(intent, 100);
            	break;
            }
            case R.id.facebook_login_menu_refresh:
            {
            	loadURL();
            	break;
            }
        }
        return true;
    }
		 
	 //create Menu
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	     super.onCreateOptionsMenu(menu);
	     
	     //menu.setOptionalIconsVisible(false);
	     
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.facebook_login, menu);
	     
	     return true;
	 }
   
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {            
            Log.d(TAG, "KEYCODE_BACK is comming");    
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.facebook_exit_login_suc)
                .setMessage(getString(R.string.facebook_exit_login_msg))
                .setPositiveButton(getString(R.string.sns_ok), new DialogInterface.OnClickListener() 
                {
	                public void onClick(DialogInterface dialog, int whichButton) 
	                {	         
	                	FacebookLoginWebViewActivity.this.setResult(1000);
	                	
	                    finish();
	                }
	            })
	            .setNegativeButton(getString(R.string.sns_cancel), new DialogInterface.OnClickListener() 
	            {
	                public void onClick(DialogInterface dialog, int whichButton) 
	                {
	                    
	                }
	            })
	            .create();
	            dialog.show();
            
            return true;
        }        
        else
        {
        	return super.onKeyDown(keyCode, event);        
        }
    }
}
