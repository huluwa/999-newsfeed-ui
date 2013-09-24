package oms.sns.facebook.ui;

import android.os.Bundle;

public class FacebookShareActivity extends FacebookBaseActivity{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);  
        
        shareFacebookApp();
        finish();
    }
    
    
    @Override
    protected void createHandler() {
              
    }

    public void setTitle() {
                
    }
}
