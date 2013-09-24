package com.msocial.facebook.ui;


import java.util.Date;
import java.util.List;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.service.LocationRequest;
import com.msocial.facebook.service.LocationRequest.FacebookLocationListener;
import com.msocial.facebook.ui.AccountListener.AccountManager;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookLocationUpdateActivity extends FacebookBaseActivity implements FacebookLocationListener{
	private EditText contentEdit;
	private MyWatcher watcher;
	private TextView textCount, facebook_location;
	private CheckBox linkMapBox;
	private boolean linkedMap = true;
    LocationRequest locReq;
    Button facebook_share_button;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_location_update);
        
        contentEdit = (EditText)this.findViewById(R.id.facebook_location_message_editor);
        contentEdit.setHint(R.string.facebook_location_hint);      
        contentEdit.setVerticalScrollBarEnabled(true);
        textCount = (TextView)this.findViewById(R.id.facebook_location_text_counter);
        facebook_location= (TextView)this.findViewById(R.id.facebook_location);
        
        watcher = new MyWatcher(); 	    
        contentEdit.addTextChangedListener(watcher);        
        setTitle(R.string.facebook_location_update_title);
        
        linkMapBox = (CheckBox)findViewById(R.id.link_map);
        linkMapBox.setText(R.string.facebook_location_link_map);
        
        locReq = LocationRequest.instance();
        locReq.registerContext(this);
        locReq.setLocationListener(this);
        
        facebook_share_button = (Button)this.findViewById(R.id.facebook_share_button);
        facebook_share_button.setOnClickListener(shareClick);
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);	
	        	promptLocationSetting();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }     
    }
	
	View.OnClickListener shareClick = new View.OnClickListener() 
	{		
		public void onClick(View v) 
		{
			if(isInProcess() == false)
			{
			    handler.obtainMessage(UPDATE_STATUS_LOCATION).sendToTarget();
			}						
		}
	};	
	
	public void setTitle()
	{
	    title = this.getString(R.string.facebook_location_update_title);
	}
	
	@Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	perm_session = loginHelper.getPermanentSesstion(this);
    	if(perm_session == null)
    	{
    		//re-launch the login UI
    		launchFacebookLogin();
    	}
    	else
    	{
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    	}
    }
	
	/*
	@Override
	public void titleSelected() 
    {		
		super.titleSelected();
		//send message to my wall
		if(this.isInProcess() == false)
		{
		    handler.obtainMessage(UPDATE_STATUS_LOCATION).sendToTarget();
		}
	}*/
	
	private void promptLocationSetting()
    {
    	 //enable location
		 boolean hasenable = hasEnableLocProvider();	     
	     if(hasenable == false)
	   	 {
	       	 AlertDialog dialog = new AlertDialog.Builder(this)
	            .setTitle(R.string.facebook_location_setting_title)
	            .setMessage(getString(R.string.facebook_setting_location_message))
	            .setPositiveButton(getString(R.string.sns_ok), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                    Intent fireAlarm = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
	                    fireAlarm.addCategory(Intent.CATEGORY_DEFAULT);
	                    startActivityForResult(fireAlarm, AFTER_SETTING_LOC);	                    
	                }
	            })
	            .setNegativeButton(getString(R.string.sns_cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) 
	                {	            
	                    //no select location
	                    //remove text info and change the option menu
	                    
	                }
	            })
	            .create();
	            dialog.show();
	   	 }   
	     else
	     {
	    	 reqeustLoaction();
	     }
    }
	
	private boolean hasEnableLocProvider()
	{
		 String[] providers= new String[]{
	                "agps",/*LocationManager.AGPS_PROVIDER,*/
	                LocationManager.GPS_PROVIDER,
	                "network",
	                //LocationManager.NETWORK_PROVIDER,
	     }; 
	      
		 boolean hasenable = false; 
	     LocationManager mService = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	     for(int i=0;i<providers.length;i++)
	     {
	    	 String provider = providers[i];
	         //LocationProvider amProvider = mService.getProvider(provider);
	    	 try{
	             hasenable = mService.isProviderEnabled(provider);
	    	 }catch(Exception ne){
	    		 Log.d(TAG, "provider="+provider + " ex="+ne.getMessage());
	    	 }
	         if(hasenable)
	         {
	        	 Log.d(TAG, "provider="+provider + " enable");
	        	 break;
	         }
	     }
	     
	     return hasenable;
	}
	
	final int AFTER_SETTING_LOC=0;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		  switch(requestCode)
	      {  
	            case AFTER_SETTING_LOC:
	            {
	            	if(hasEnableLocProvider())
	            	{
	            		reqeustLoaction();
	            	}
	            	break;
	            }
	      }
	}
	
	private static boolean requested;
	public boolean isRequested()
	{
		return requested;
	}
	
	@Override protected void reqeustLoaction()
	{
	    if(this.hasEnableLocProvider())
	    {
    		Message mds = handler.obtainMessage(REQUEST_LOCATION);
    		if(requested == false)
    		{
    			facebook_location.setText(R.string.facebook_request_location);			
    			mds.getData().putBoolean("request", true);
    			requested = true;
    		}
    		else
    		{
    			facebook_location.setText(R.string.facebook_location_stop_request);			
    			mds.getData().putBoolean("request", false);
    			requested = false;
    		}
    		
    		handler.sendMessageDelayed(mds, 5*1000);
	    }
	    else
	    {
	        promptLocationSetting();
	        Log.d(TAG, "no GPS enable");
	    }
	}
	
	@Override protected void onPause() 
	{
        super.onPause();
        if(requested == true)
    	{
        	facebook_location.setText(R.string.facebook_location_stop_request);	
    	    locReq.deActivate(this);
    	    requested = false;
    	}
	}
	
	@Override protected void onResume() 
	{
        super.onResume();
        if(requested == false)
    	{
            if(this.hasEnableLocProvider())
            {
            	Message mds = handler.obtainMessage(REQUEST_LOCATION);    		
        		facebook_location.setText(R.string.facebook_request_location);
        		mds.getData().putBoolean("request", true);
        		requested = true;
        		handler.sendMessageDelayed(mds, 5*1000);
            }
    	}
	}
	
    @Override protected void onDestroy() 
    {
    	if(requested == true)
    	{
    	    locReq.deActivate(this);
    	    requested = false;
    	}
    	
    	clearAsyncFacebook(true);
    	super.onDestroy();
    }
	
	static Location lastLoc = new Location("");
	public void  getLocation()
	{	
		LocationManager locMan;		
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		Location tmp =  locReq.getCurrentLastLocation();
		if(tmp.getLatitude() == new Location("").getLatitude())
		{
			Criteria criteria = new Criteria();		
		    String bestProvider = locMan.getBestProvider(criteria, false);
		    
		    if(bestProvider == null)
		    {
		    	Log.d(TAG, "no location provider");
		    }
		    else
		    {
		    	Log.d(TAG, "re-request location="+bestProvider);	    
		    	tmp = locMan.getLastKnownLocation(bestProvider);
		    	if(tmp != null)
			    {
		    		lastLoc.set(tmp);
		    		//save into database;	    		
			    }
		    	else
		    	{
		    		tmp = lastLoc;
		    	}
		    }
		}
		else
		{
			lastLoc.set(tmp);
		}
		Log.d(TAG, "location is "+lastLoc.getLatitude() +"=="+lastLoc.getLongitude());
	}
	
	//call update status
	final int UPDATE_STATUS_LOCATION     = 1;
	final int UPDATE_STATUS_LOCATION_END = 2;
	final int POST_LINK                  = 3;
	final int REQUEST_LOCATION           = 4;
	
	private class UpdateHandler extends Handler 
    {
        public UpdateHandler()
        {
            super();            
            Log.d(TAG, "new UpdateHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case REQUEST_LOCATION:
                {
                	if(msg.getData().getBoolean("request") == true)
                	{
                	    locReq.activate(FacebookLocationUpdateActivity.this.getApplicationContext());
                	}
                	else
                	{
                		locReq.deActivate(FacebookLocationUpdateActivity.this.getApplicationContext());
                	}
                	break;
                }
            	case UPDATE_STATUS_LOCATION://update text
            	{	
            		//get context         		
            		if(isSafeCallFacebook(false) == false)
            		{
            			return;
            		}
            		
            		String content = contentEdit.getText().toString().trim();            		
            		getLocation();              		      		
        	
            		boolean postlink=false;
            		if(linkMapBox.isChecked() && lastLoc.getLatitude() != new Location("").getLatitude())
            		{            			
            			String linkurl = locReq.getStaticMapurl(lastLoc);
            			if(linkurl!=null && linkurl.length()>0 && linkurl.startsWith("http://"))
            			{
            				Message message = handler.obtainMessage(POST_LINK);
            				message.getData().putString("linkpath",linkurl);
            				if(content!=null && content.length()>0)
            				{
            				    content = content + "( " + locReq.getMapsSearchString(lastLoc) +" )";
            	  			    message.getData().putString("text", content);
            	  			}
            				else
            				{
            					content = locReq.getMapsSearchString(lastLoc);
            	  			    message.getData().putString("text", content);
            				}
            				postlink = true;
            				message.sendToTarget();
            		    }            		
            		}        			
            		
            		if(postlink == false)
            		{
	        			if(content != null && content.length() >0)
	            		{
	        				Address address = locReq.getLocationAddress(lastLoc);      
	            			if(address!=null)
	            			{              			
	                            content = content +"(from :"+locReq.getAddressInfo(address)+" )" ;                         
	                            Log.d(TAG, " content is =="+content);
	                  		}
	            			
	            			String search = locReq.getMapsSearchString(lastLoc);
	            			if(isEmpty(search) == false)
	            			{
	            				content = content + "( " + locReq.getMapsSearchString(lastLoc) + " )";
	            			}	            			
	            			
	            			updateStatus(content);
	            		}
	        			else if(lastLoc.getLatitude() != 0.0)//already get location
	        			{
	        			    content = locReq.getMapsSearchString(lastLoc);
                            updateStatus(content);
	        			}
            		}            		
            		break;
            	}
            	case UPDATE_STATUS_LOCATION_END:
            	{
            		end();
            		facebook_share_button.setEnabled(true);
            		dismissDialog(DLG_POST_WALL);
            		setTitle(R.string.facebook_status_update_title);
            		if(msg.getData().getBoolean(RESULT) == true)
            		{
            		    contentEdit.setText("");
            		    linkMapBox.setChecked(false);
            		    handler.post( new Runnable(){
            		       public void run()
            		       {
            		           Toast.makeText(FacebookLocationUpdateActivity.this, R.string.update_location_succeed, Toast.LENGTH_SHORT).show();
            		       }
            		    });
            		}
            		else
            		{
            		    handler.post( new Runnable(){
                            public void run()
                            {
                                Toast.makeText(FacebookLocationUpdateActivity.this, R.string.update_location_fail, Toast.LENGTH_SHORT).show();
                            }
                         });
            		}
            		break;
            	} 
            	case POST_LINK:
                {
                	long   sid   = msg.getData().getLong("sid");
                	String linkurl = msg.getData().getString("linkpath");
                	String text = msg.getData().getString("text");
                	postlink(sid, linkurl,text);
                	break;
                }            	
            }
        }
    }
	
	
	void postlink(long sid, String linkpath,String comment)
	{
		
		Log.d(TAG, "entering post link");
		if(this.isInProcess() == true)
		{
		    return;
		}
		else
		{
		    facebook_share_button.setEnabled(false);
		    showDialog(DLG_POST_WALL);
		}
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		
		facebookA.postLinkAsync(sid, linkpath,comment,new FacebookAdapter()
    	{
    		@Override public void postlink(long ssid, long linkid)
            {
    			//Log.d(TAG, "post to wall="+suc);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();
                }       
                
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_LOCATION_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "post to wall ex="+e.getMessage());
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_LOCATION_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
            	}
            }
    	});

	}
	
	
	void updateStatus(String content)
	{
	    if(this.isInProcess() == true)
	    {
	        return;
	    }
	    else
	    {
	        facebook_share_button.setEnabled(false);
	        showDialog(DLG_POST_WALL);
	    }
	    
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		facebookA.updateStatusAsync(content,new FacebookAdapter()
    	{
    		@Override public void updateStatus(boolean suc)
            {
    			Log.d(TAG, "post to wall="+suc);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();
                }       
                
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_LOCATION_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "post to wall ex="+e.getMessage());
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_LOCATION_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
            	}
            }
    	});
	}
	
	@Override
	protected void createHandler() 
	{
		handler = new UpdateHandler();
	}
	
	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {
    	   textCount.setText(String.format("%1$s", s.length()));
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }


	public void updateLocation(Location loc) 
	{
		this.lastLoc.set(loc);		
		//update UI
		handler.post(new Runnable()
		{
			public void run()
			{
			    String gpsdata = String.format("Provider:%1$s, GPS:%2$s, %3$s, last update: %4$s", lastLoc.getProvider(), lastLoc.getLatitude(), lastLoc.getLongitude(), new Date(lastLoc.getTime()).toLocaleString());
		        facebook_location.setText(gpsdata);
		        Log.d(TAG, "updated gps data="+gpsdata);
		        
		        linkMapBox.setChecked(true);
			}
		});
	}
	
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookLocationUpdateActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookLocationUpdateActivity");		
	}
}
