package oms.sns.facebook.ui;

import java.util.ArrayList;
import java.util.List;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
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
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PromptUserSyncActivity extends Activity{
	
	private final String TAG="PromptUserSyncActivity"; 
	private Button okBtn;
	private Button cancelBtn;
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
    	
    	orm = new SocialORM(this);
        setContentView(R.layout.facebook_sync_prompt_dialog);

        okBtn = (Button)this.findViewById(R.id.dialog_ok);
        okBtn.setText(android.R.string.ok);
        
        cancelBtn = (Button)this.findViewById(R.id.dialog_cancel);
        cancelBtn.setText(android.R.string.cancel);
        
        dialogMsg = (TextView)this.findViewById(R.id.dialog_message);
        dialogTitle = (TextView)this.findViewById(R.id.dialog_title);
        
        dialogMsg.setText(String.format(getString(R.string.prompt_user_sync_string), getString(android.R.string.ok), getString(android.R.string.cancel)));
        dialogTitle.setText(R.string.prompt_user_sync_title);
        
        isAllwaysPromptDialog = (CheckBox)this.findViewById(R.id.checkbox_allways_prompt_dialog);
               
        
        okBtn.setOnClickListener(okClickListener);
        cancelBtn.setOnClickListener(cancelClickListener);
        isAllwaysPromptDialog.setChecked(orm.isAlwaysPromptSyncDialog());
        isAllwaysPromptDialog.setOnCheckedChangeListener(checkboxListener);
        
        this.setTitle(R.string.prompt_user_sync_title);
        alwaysPromptDialog = orm.isAlwaysPromptSyncDialog();
    }
	
	boolean alwaysPromptDialog;
   OnCheckedChangeListener checkboxListener = new OnCheckedChangeListener()
   {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
        {  
            Log.d(TAG, "is checked="+isChecked);
            alwaysPromptDialog = isChecked;
            orm.enableAlwaysPromptSyncDialog(isChecked);      
        }        
   };
	
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
			
			orm.enableAlwaysPromptSyncDialog(alwaysPromptDialog);     
			PromptUserSyncActivity.this.finish();
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
			PromptUserSyncActivity.this.finish();
		}
	};
	
	
}
