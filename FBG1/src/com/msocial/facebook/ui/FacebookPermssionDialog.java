package com.msocial.facebook.ui;

import java.util.ArrayList;
import java.util.List;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class FacebookPermssionDialog extends Activity{
	
	private final String TAG="FacebookPermssionDialog"; 
	private Button   okBtn;
	private Button   cancelBtn;
	private TextView dialogMsg;
	private TextView dialogTitle;
	private CheckBox isAllwaysPromptDialog;
	SocialORM orm;
	
	private static List<String> msgs = new ArrayList<String>();
	public static synchronized void appendMessage(String msg)
	{
		boolean exist = false;
		synchronized(msgs)
		{
			for(String item: msgs)
			{				
				if(item.equals(msg) == true)
				{
					Log.d("FacebookPermssionDialog", "I am already exist="+msg);
					exist = true;
					break;
				}
			}
			
			if(exist == false)
			{
			    msgs.add(msg);
			}
		}
	}		
	
	@Override
	protected void onNewIntent(Intent intent) 
	{		 
		super.onNewIntent(intent);		
		Log.d(TAG, "new requestiong is comming="+this + " intent="+intent);
		
		if(dialogMsg != null)
		{
		    dialogMsg.setText(String.format(getString(R.string.facebook_permission_message), getPermissionList()));
		}
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
        
        dialogMsg.setText(String.format(getString(R.string.facebook_permission_message), getPermissionList()));
        dialogTitle.setText(R.string.assign_extend_permission);
        
        isAllwaysPromptDialog = (CheckBox)this.findViewById(R.id.checkbox_allways_prompt_dialog);
        isAllwaysPromptDialog.setVisibility(View.VISIBLE);
        isAllwaysPromptDialog.setText(R.string.is_allways_prompt_permission_dialog);
     
        View checkbox_span = (View)this.findViewById(R.id.checkbox_span);
        checkbox_span.setVisibility(View.VISIBLE);
        
        isAllwaysPromptDialog.setOnCheckedChangeListener(checkListener);
        isAllwaysPromptDialog.setChecked(orm.isEnableAssignPermission());
        
        okBtn.setOnClickListener(okClickListener);
        cancelBtn.setOnClickListener(cancelClickListener);
        
        this.setTitle(R.string.assign_extend_permission);        
    }
	
	private String getPermissionList()
	{
		String permissions = "";
		for(String msd: msgs)
		{
			if(permissions.length() > 0)
				permissions +=", ";
			
			permissions+= msd;
		}
		return permissions;
	}
	
	CompoundButton.OnCheckedChangeListener checkListener = new  CompoundButton.OnCheckedChangeListener() 
	{	
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
		{	
			orm.EnableAssignPermission(isChecked);
		}
	};
	
	View.OnClickListener okClickListener = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			if(msgs != null)
			{
				synchronized(msgs)
				{
					for(String msd: msgs)
					{
						//process message
						Log.d(TAG, "NO_EXT_PERMISSION =" + msd);
	  	                Intent intent = new Intent(FacebookPermssionDialog.this.getApplicationContext(), FacebookExtPermissionActivity.class);
	  	                intent.putExtra("permission", msd);
	  	                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	  	                startActivity(intent);
						
					}
					msgs.clear();
				}
			}
			
			orm.EnableAssignPermission(isAllwaysPromptDialog.isChecked());     
			FacebookPermssionDialog.this.finish();
		}		
	};
	
	View.OnClickListener cancelClickListener = new View.OnClickListener()
	{		
		public void onClick(View v)
		{
			if(msgs != null)
			{
				synchronized(msgs)
				{
					msgs.clear();
				}
			}
			     
			FacebookPermssionDialog.this.finish();
		}
	};
}
