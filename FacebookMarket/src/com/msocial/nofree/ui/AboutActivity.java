package com.msocial.nofree.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import com.msocial.nofree.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity{
   private Button close_ui;
   private TextView about_info;
   private WebView  w_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_ui);    
        about_info = (TextView)findViewById(R.id.sns_about_info);  
        about_info.setText(R.string.sns_about_info);       
        close_ui   = (Button)findViewById(R.id.close_about_ui);
        close_ui.setText(R.string.close_about);
        
        w_view = (WebView)findViewById(R.id.w_view);
        close_ui.setOnClickListener(new OnClickListener()
        {          
            public void onClick(View view){
                AboutActivity.this.finish(); 
            }
        });
        
        boolean fortwitter = this.getIntent().getBooleanExtra("fortwitter", false);
        if(fortwitter)
        {
        	View facebook_header_id = this.findViewById(R.id.facebook_header_id);
        	facebook_header_id.setVisibility(View.GONE);
        }
        boolean forabout = this.getIntent().getBooleanExtra("forabout", false);
        if(forabout == true)
        {        
	        setTitle(R.string.about_title);
	        
	        try{
		        StringBuilder sb = new StringBuilder();
		        InputStream in  = this.getAssets().open("about_facebook");
		        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));        
		        StringBuffer buf = new StringBuffer();
		        
		        char[] chBuf = new char[1024];
		        int lineLen =0;            
		        while ( ( lineLen = br.read(chBuf, 0, 1024) ) >0 ) 
		        {           
		        	 buf.append( chBuf, 0, lineLen );
		        }       
		        
		        br.close();
		        
		        br = null;
		        in = null;
		        chBuf = null;
		        w_view.loadData(buf.toString(), "text/html", "utf-8");
	        }catch(java.io.IOException ne)
	        {
	        	w_view.loadUrl("http://www.facebook.com/apps/application.php?id=94437770062");
	        }
        }
        else
        {
        	setTitle(R.string.pref_facebook_help_tips_setting);
        	about_info.setVisibility(View.GONE);
        	
        	View sns_about_image = this.findViewById(R.id.sns_about_image);
        	sns_about_image.setVisibility(View.GONE);
        	
        	 
	        try{
		        StringBuilder sb = new StringBuilder();
		        InputStream in  = this.getAssets().open("help_facebook");
		        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));        
		        StringBuffer buf = new StringBuffer();
		        
		        char[] chBuf = new char[1024];
		        int lineLen =0;            
		        while ( ( lineLen = br.read(chBuf, 0, 1024) ) >0 ) 
		        {           
		        	 buf.append( chBuf, 0, lineLen );
		        }       
		        
		        br.close();
		        
		        br = null;
		        in = null;
		        chBuf = null;
		        w_view.loadData(buf.toString(), "text/html", "utf-8");
	        }catch(java.io.IOException ne)
	        {
	        	w_view.loadUrl("http://www.facebook.com/apps/application.php?id=94437770062");
	        }
        }
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    
  
    
    
}
