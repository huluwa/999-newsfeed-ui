package com.tormas.litesina.ui;


import java.util.Date;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;

import com.tormas.litesina.R;
import com.tormas.litesina.providers.SocialORM;
import com.tormas.litesina.service.LocationRequest;
import com.tormas.litesina.service.LocationRequest.TwitterLocationListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

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

public class LocationUpdateActivity extends StatusViewBaseActivity implements TwitterLocationListener{
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
        
        handler = new UpdateHandler();
        
        facebook_share_button = (Button)this.findViewById(R.id.facebook_share_button);
        facebook_share_button.setOnClickListener(shareClick);
        
        SocialORM.Account account = orm.getTwitterAccount();
        if(checkTwitterAccount(this, account) == true)
		{
        	
		}
        promptLocationSetting();             
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
	    finalTitle = this.getString(R.string.facebook_location_update_title);
	}
	
	@Override protected void doAfterLoginNothing()
    {
        super.doAfterLoginNothing();
        setResult(RESULT_CANCELED);
        finish();    
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
	
	protected void reqeustLoaction()
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
                	    locReq.activate(LocationUpdateActivity.this.getApplicationContext());
                	}
                	else
                	{
                		locReq.deActivate(LocationUpdateActivity.this.getApplicationContext());
                	}
                	break;
                }
            	case UPDATE_STATUS_LOCATION://update text
            	{	
            		//get context
            		
            		String content = contentEdit.getText().toString().trim();            		
            		getLocation();              		      		
        	
            		boolean postlink=false;
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
	            				Log.d(TAG, " content is =="+content);
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
            		dismissDialog(TWITTER_UPDATE_STATUS_DLG);
            		setTitle(R.string.facebook_status_update_title);
            		if(msg.getData().getBoolean(RESULT) == true)
            		{
            		    contentEdit.setText("");
            		    linkMapBox.setChecked(false);
            		    handler.post( new Runnable(){
            		       public void run()
            		       {
            		           Toast.makeText(LocationUpdateActivity.this, R.string.update_location_succeed, Toast.LENGTH_SHORT).show();
            		       }
            		    });
            		}
            		else
            		{
            		    handler.post( new Runnable(){
                            public void run()
                            {
                                Toast.makeText(LocationUpdateActivity.this, R.string.update_location_fail, Toast.LENGTH_SHORT).show();
                            }
                         });
            		}
            		break;
            	} 
            	
            }
        }
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
	        showDialog(TWITTER_UPDATE_STATUS_DLG);
	    }
	    
		begin();
		
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		if(twitterA == null)
    	{
    		Log.d(TAG, "your twitter are not login 2, please login firstly");
    		return ;
    	}    	    	
    	twitterA.updateStatusAsync(content, new TwitterAdapter() 
        {
        	 @Override public void  updatedStatus(Status status)
             {
                Log.d(TAG, "after update status="+status);
                Message smd = handler.obtainMessage(UPDATE_STATUS_LOCATION_END);
                smd.getData().putBoolean(RESULT, true);
                handler.sendMessage(smd);
             }
            
             @Override public void onException(TwitterException e, int method) 
             {                   
                Log.d(TAG, "Fail to updated ="+e.getMessage());  
                if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
	                Message smd = handler.obtainMessage(UPDATE_STATUS_LOCATION_END);
	                smd.getData().putBoolean(RESULT, false);
	                smd.getData().putString("errormsg", e.getMessage());
	                handler.sendMessage(smd);
             	}
             }           
        });        	
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
	
}
