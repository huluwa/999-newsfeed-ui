package com.msocial.freefb.ui;

import com.msocial.freefb.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TabHost;

public class FacebookTabActivity extends TabActivity
{
    private final String TAG = "FacebookTabActivity";
    protected TabHost mTabHost;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        //setTitleMenuIconVisible(true);
        mTabHost = getTabHost();
        initTabs();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming="+this);
        	
        	finish();
            System.gc();
                
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() 
    { 
    	mTabHost.clearAllTabs();
    	mTabHost = null;
    	
    	
    	super.onDestroy();
    }
    private void initTabs()
    {
        mTabHost.clearAllTabs();

        Intent friendsIntent   = new Intent(this,   FacebookFriendsActivity.class);
        friendsIntent.putExtra("fromtabview", true);
        
        /*
        Intent phonebookIntent   = new Intent(this,   FacebookPhonebookActivity.class);
        phonebookIntent.putExtra("fromtabview", true);
        */
        
        Intent pageIntent   = new Intent(this,   FacebookPageActivity.class);
        pageIntent.putExtra("fromtabview", true);
        
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.menu_title_friends)).
                setIndicator( getString(R.string.menu_title_friends))
                .setContent( friendsIntent));
        
        /*
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.menu_title_phonebook)).
                setIndicator( getString(R.string.menu_title_phonebook))
                .setContent( phonebookIntent));
        */
        
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.menu_title_pages)).
                setIndicator( getString(R.string.menu_title_pages))
                .setContent( pageIntent));
        
        mTabHost.setCurrentTab(0);
    }
}
