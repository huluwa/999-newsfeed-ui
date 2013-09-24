package com.msocial.free.ui;

import com.msocial.free.*;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.service.dell.AsyncOmsService;
import com.msocial.free.ui.AccountListener.AccountManager;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookSetContactActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookSetContactActivity";
	
	private EditText cell;
	private EditText other;
	private EditText email;
	private Button   setB;
	private TextView getcontact_status;
	private boolean  dontshowui = true;
		
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);        
        Intent intent = this.getIntent();   
        setContentView(R.layout.facebook_set_contact);
        
        this.setTitle(R.string.facebook_set_contact_title);
        
        email = (EditText)this.findViewById(R.id.contact_email);
        cell  = (EditText)this.findViewById(R.id.contact_cell);
        other = (EditText)this.findViewById(R.id.contact_other);
        
        getcontact_status = (TextView)this.findViewById(R.id.facebook_get_contact);
        SocialORM.Account account = orm.getFacebookAccount();
        //email.setText(account.email);
        
        String simsn = android.telephony.TelephonyManager.getDefault().getLine1Number();
        if(simsn != null && simsn.length() > 0)
        {
        	cell.setText(simsn);
        	other.setText(simsn);
        }
        
        setB  = (Button)this.findViewById(R.id.contact_setup);
        setB.setOnClickListener(updateClick);
        
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
        	    //show get_contact_info worked in background so need a new AsyncFacebook without connectionListener to avoid 
                //onKeyDown-->stopLoading()
	        	//perm_session.attachActivity(this);	        	
	        	facebookA = new AsyncFacebook(perm_session);	        	
	        	getContactInfo();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }        
    }
	
	private void getContactInfo()
	{
	    PhoneBook temp = orm.getPhonebook(perm_session.getLogerInUserID());
        if(temp!=null && !isEmpty(temp.getEmail()))
        {
             email.setText(temp.getEmail());
             if(!isEmpty(temp.getCell()))cell.setText(temp.getCell());
             if(!isEmpty(temp.getPhone()))other.setText(temp.getPhone());      
        }
        else
        {         
	        handler.obtainMessage(GET_CONTACT_INFO).sendToTarget();
        }
	}
	
	@Override
	protected void loadRefresh()
	{
		handler.obtainMessage(GET_CONTACT_INFO).sendToTarget();
	}

	@Override
	protected void createHandler() 
	{
		handler = new ContactHandler();
	}

	View.OnClickListener updateClick = new View.OnClickListener()
	{
		public void onClick(View arg0) 
		{
			//do 
			handler.obtainMessage(SET_CONTACT).sendToTarget();			
		}		
	};
	public void setTitle() 
	{
		title = "Set contact";
	}
	
	final int SET_CONTACT       =0;
	final int SET_CONTACT_END   =1;
    final int GET_CONTACT_INFO  =2;
	private class ContactHandler extends Handler 
    {
        public ContactHandler()
        {
            super();
            Log.d(TAG, "new ContactHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            	case SET_CONTACT:
            	{
            		setContact();
            		break;
            	}
            	case SET_CONTACT_END:
            	{
            	    dismissDialog(DLG_SET_CONTACT);
            	    setB.setEnabled(true);
            		boolean suc = msg.getData().getBoolean(RESULT, false);
            		if(suc)
            		{
            			Toast.makeText(FacebookSetContactActivity.this, R.string.sns_update_succeed, Toast.LENGTH_SHORT).show();
            		}
            		else
            		{
            			Toast.makeText(FacebookSetContactActivity.this, R.string.sns_update_failed, Toast.LENGTH_SHORT).show();
            		}
            		
            		//save to db get contact_info in background
            		Message msd = handler.obtainMessage(GET_CONTACT_INFO);
            		msd.getData().putBoolean("dontchangeui",true);
            		handler.sendMessage(msd);
            		break;
            	}
            	case GET_CONTACT_INFO:
            	{
            	    dontshowui = msg.getData().getBoolean("dontchangeui");
            	    if(dontshowui == false)
            	    {
            	        getcontact_status.setText(R.string.facebook_getting_contact);   
            	    }
            	    
            	    facebookA.getContactInfo(perm_session.getLogerInUserID(), new FacebookAdapter()
                    {
                        @Override public void getContactInfo(PhoneBook phonebook)
                        {
                            if(phonebook!=null)
                            {
                                orm.addPhonebook(phonebook);
                            }
                           
                            if(dontshowui == false){
                                handler.post(new Runnable(){
                                    public void run()
                                    {
                                        PhoneBook temp = orm.getPhonebook(perm_session.getLogerInUserID());
                                        if(temp!=null)
                                        {
                                           if(!isEmpty(temp.getEmail()))email.setText(temp.getEmail());
                                           if(!isEmpty(temp.getCell()))cell.setText(temp.getCell());
                                           if(!isEmpty(temp.getPhone()))other.setText(temp.getPhone());
                                        }                                    
                                        //getcontact_status.setText(R.string.facebook_getting_contact_suc);  
                                        getcontact_status.setText("");
                                    }
                                 });  
                            }
                            
                         }
                        
                        @Override public void onException(FacebookException e, int method) 
                        {
                           //get from db
                            if(dontshowui == false){
                                handler.post(new Runnable(){
                                    public void run()
                                    {
                                        PhoneBook temp = orm.getPhonebook(perm_session.getLogerInUserID());
                                        if(temp!=null)
                                        {
                                           if(!isEmpty(temp.getEmail()))email.setText(temp.getEmail());
                                           if(!isEmpty(temp.getCell()))cell.setText(temp.getCell());
                                           if(!isEmpty(temp.getPhone()))other.setText(temp.getPhone());
                                        }
                                        
                                        getcontact_status.setText("");
                                        
                                    }
                                 });  
                            }
                       }
                    });
            	    break;
            	}
            }
        }
    }
	
	public void setContact() 
	{
		String semail = email.getText().toString().trim();
		String scell  = cell.getText().toString().trim();
		String sother = other.getText().toString().trim();
		if(validateInput() == false)
		{
		    Toast.makeText(mContext,R.string.facebook_set_contact_prompt, Toast.LENGTH_SHORT).show();
			return ;
		}
		
		begin();
		setB.setEnabled(false);
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		showDialog(DLG_SET_CONTACT);
		facebookA.setContactAsync(semail, scell, sother,new FacebookAdapter()
    	{
    		@Override public void setContact( boolean suc)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}		
				end();
				
				//save phone number, for ticket 34464
				Log.d(TAG, "update contact info in cache");
				
				PhoneBook temp = orm.getPhonebook(perm_session.getLogerInUserID());
				temp.cell  = cell.getText().toString().trim();
				temp.phone = other.getText().toString().trim();				
				orm.updatePhonebook(temp);
				//end save
				 
				Log.d(TAG, "after set contact info");
				Message msg = handler.obtainMessage(SET_CONTACT_END);
				msg.getData().putBoolean(RESULT, true);
				handler.sendMessage(msg);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}	
            	end();
            	
            	Log.d(TAG, "fail to set contact info="+e.getMessage());
            	if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
	            	Message msg = handler.obtainMessage(SET_CONTACT_END);
					msg.getData().putBoolean(RESULT, false);
					handler.sendMessage(msg);
             	}
            }
    	});
	}

	private boolean validateInput() 
	{		
		return true;
	}
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookSetContactActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookSetContactActivity");		
	}
}
