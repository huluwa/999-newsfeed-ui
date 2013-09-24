package com.msocial.freefb.ui;

import java.util.ArrayList;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SynchronizeAlertActivity extends Activity{
	
	private final String TAG="SynchronizeAlertActivity"; 
	private Button   okBtn;
	private Button   cancelBtn;
	private TextView dialogMsg;
	private TextView dialogTitle;
	private CheckBox isAllwaysPromptDialog;
	SocialORM orm;
	
	private static List<Message> msgs = new ArrayList<Message>();
	public static synchronized void appendMessage(Message msg)
	{
		msgs.add(msg);
	}		
	
	@Override
	protected void onNewIntent(Intent intent) {		 
		super.onNewIntent(intent);
		
		Log.d(TAG, "new requestiong is comming="+this + " intent="+intent);
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming="+this);
            this.finish();            
                
        }
        return super.onKeyDown(keyCode, event);
    }
	    
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		
		if(msgs != null)
		{
			synchronized(msgs)
			{
				for(Message msd: msgs)
				{
					msd.getData().putBoolean("RESULT", false);
					//msd.sendToTarget();
					msd.getTarget().sendMessageDelayed(msd, 2*1000);
				}
				msgs.clear();
			}			
		}
	}



	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	orm = SocialORM.instance(this.getApplicationContext());
        setContentView(R.layout.facebook_sync_prompt_dialog);

        okBtn = (Button)this.findViewById(R.id.dialog_ok);
        okBtn.setText(android.R.string.ok);
        
        cancelBtn = (Button)this.findViewById(R.id.dialog_cancel);
        cancelBtn.setText(android.R.string.cancel);
        
        dialogMsg = (TextView)this.findViewById(R.id.dialog_message);
        dialogTitle = (TextView)this.findViewById(R.id.dialog_title);
        
        dialogMsg.setText(R.string.sync_address_book_desc);
        dialogTitle.setText(R.string.sync_address_book);
        
        isAllwaysPromptDialog = (CheckBox)this.findViewById(R.id.checkbox_allways_prompt_dialog);
        isAllwaysPromptDialog.setVisibility(View.GONE);
     
        View checkbox_span = (View)this.findViewById(R.id.checkbox_span);
        checkbox_span.setVisibility(View.GONE);
        
        okBtn.setOnClickListener(okClickListener);
        cancelBtn.setOnClickListener(cancelClickListener);
        
        this.setTitle(R.string.sync_address_book);        
    }
	
	View.OnClickListener okClickListener = new View.OnClickListener(){

		public void onClick(View v) {
			if(msgs != null)
			{
				synchronized(msgs)
				{
					for(Message msd: msgs)
					{
						msd.getData().putBoolean("RESULT", true);
						//msd.sendToTarget();
						msd.getTarget().sendMessageDelayed(msd, 2*1000);
					}
					msgs.clear();
				}
			}	
			
			orm.EnableSyncPhonebook(true);     
			SynchronizeAlertActivity.this.finish();
		}		
	};
	
	View.OnClickListener cancelClickListener = new View.OnClickListener(){
		public void onClick(View v){
			if(msgs != null)
			{
				synchronized(msgs)
				{
					for(Message msd: msgs)
					{
						msd.getData().putBoolean("RESULT", false);
						msd.sendToTarget();
					}
					msgs.clear();
				}
			}
			orm.EnableSyncPhonebook(false);     
			SynchronizeAlertActivity.this.finish();
		}
	};
	
	
}
