package oms.sns.facebook.ui;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.ExtendedPermission;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;

public class FacebookExtPermissionActivity extends FacebookBaseActivity{

	String perm;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        perm = this.getIntent().getStringExtra("permission");        
        //prompt sub-activity Browser to get the permission        
        Log.d(TAG, "create FacebookExtPermissionActivity="+perm);
        
        this.setTitle(perm);
        
       
       String extPermURL = loginHelper.getExtPermURL(perm);         		   
       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(extPermURL));
       intent.setData(Uri.parse(extPermURL));
       intent.putExtra("facebook_loginURL", extPermURL); 
       
       intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
       formatFacebookIntent(intent, orm);                             
       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
       startActivityForResult(intent, RETRUN_FROM_BROWSER);    	        
    }   
	
	 public void setTitle() 
	 {
	 	title = ("Extended Permission");		
	 }
	 
	//check the permission
	@Override protected void onResume() 
    {
    	super.onResume();
    	
    	validatePermisson();
    	this.finish();
    }
	
	@Override
	protected void createHandler() 
	{
		handler = new ExtHandler();
	}
	
	//process the basic facebook action,
    //such as login, ext permission
	final static int FACEBOOK_EXT_PERM=0;
    private class ExtHandler extends Handler 
    {
        public ExtHandler()
        {
            super();            
            Log.d(TAG, "new ExtHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case FACEBOOK_EXT_PERM:
	            {
	            	int progress = msg.getData().getInt("progress");
	            	if(isShowTitleBar == false)
	            	{
	            	    setProgressNoTitle(progress);
	            	}
	            	else
	            	{
	            	    mContext.setProgress(progress);
	            	}
	            	break;
	            }
            }
        }
    }
    
    private void validatePermisson()
    {	
		loginHelper.restoreSesstion();
		FacebookSession sf = this.loginHelper.getTempSesstion();
		
		if(sf != null)
		{
		    facebookA = new AsyncFacebook(sf);
		    
		    facebookA.hasAppPermissionAsync(perm,new FacebookAdapter(){

                @Override
                public void hasAppPermission(String perm, boolean hasPerm) {
                    Log.d(TAG, "perm="+perm +"  permission="+hasPerm);
                    
                    if(hasPerm)
                        orm.enableExtPermissions(perm);
                    else
                        orm.disableExtPermissions(perm);
                }

                @Override
                public void onException(FacebookException te, int method) {
                    Log.d(TAG," has App Permission exception= "+ te.getMessage());
                }
		        
		    });
		}
    }
    
	final int RETRUN_FROM_BROWSER=0;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
	      switch(requestCode)
	      {  
	            case RETRUN_FROM_BROWSER:
	            {
	            	//check the permission and save it to database
	            	Log.d(TAG, "back from browser");
	            	//validate the permission
	            	validatePermisson();
	            	break;
	            }
	           
	      }
	}

	public void onLogin() 
	{
		Log.d(TAG, "onLogin="+this);		
	}

	public void onLogout() 
	{
		Log.d(TAG, "onLogout="+this);		
	}

	public void registerAccountListener() 
	{
		AccountManager.registerAccountListener("FacebookExtPermissionActivity", this);
	}

	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("FacebookExtPermissionActivity");		
	}
    
}
