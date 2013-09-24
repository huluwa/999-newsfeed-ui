package com.msocial.facebook.ui;

import java.util.ArrayList;

import com.msocial.facebook.R;
import com.msocial.facebook.service.FacebookSyncHelper;
import com.msocial.facebook.ui.lisenter.SyncFacebookAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FacebookSyncActivity extends Activity{
     private String TAG = "FacebookSyncActivity";
	 private  ProgressDialog progressdialog;
	 private AlertDialog alertdialog;

	 
	 public void onCreate(Bundle savedInstanceState)
	 {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.facebook_main_ui);	 	
	     setTitle("");
	     
	     FacebookSyncHelper syncHelper = FacebookSyncHelper.instance(this);	  
	     Intent intent = this.getIntent();
	     Log.d(TAG, "intent is "+intent.getAction()+intent.getDataString());
		 showProgressDialog();
		 syncHelper.addSyncLisenter(new SyncFacebookAdapter(){
			 @Override
	         public void syncProgress(int progress){
	        	Log.d(TAG, "syncProgress is comming "+progress);
	        	progressdialog.setProgress(progress);
	        }		    	
		 });
			
		
	  }
	 
	 
	 public void showProgressDialog(){
		 progressdialog = new ProgressDialog(FacebookSyncActivity.this,1);
	     progressdialog.setButton(getString(R.string.menu_title_stop), new DialogInterface.OnClickListener(){

		  public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				stopSync();
				finish();
		  }
	    	
	     });
	     
	     progressdialog.setButton2("hidden", new DialogInterface.OnClickListener(){
	    	 public void onClick(DialogInterface dialog,int arg1){
	    		finish();
	    	 }
	     });
	     
	     progressdialog.setTitle(R.string.facebook_sync_contact_title);
	     //progressdialog.setMessage("Please wait...");
	     progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         progressdialog.setMax(0);
	     progressdialog.show();
	 }
	 
	 
	protected void stopSync(){
		Log.d(TAG, "stop global sync contact");
		FacebookSyncHelper syncHelper = FacebookSyncHelper.instance(this);
		syncHelper.stopSyncContact();
	 }
	
	public void alertMessage(String message){
		 alertdialog =new AlertDialog.Builder(this)
	        .setTitle(R.string.sns_error_message)
	        .setMessage(message)
	        .setNegativeButton(getString(R.string.facebook_login_cancel), new DialogInterface.OnClickListener() 
	        {
	           public void onClick(DialogInterface dialog, int whichButton) 
	           {
	        	   
	           }
	        })
	        .create();
	        alertdialog.show();		
	 }
	 
	/* public void closeProgressDialog(){
		if(progressdialog!=null){
			progressdialog.dismiss();
		} 
		finish();
	 }
	 
	 public void hiddenSync(){ 
	   closeProgressDialog();                  
	 }*/
	
	
}

