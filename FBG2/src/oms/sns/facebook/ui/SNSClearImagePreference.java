package oms.sns.facebook.ui;

import java.io.File;

import oms.sns.facebook.providers.SocialProvider;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.android.internal.preference.YesNoPreference;

public class SNSClearImagePreference extends YesNoPreference{
  private final static String PREF_EXTRAS_RESET_DEFAULTS = "clear_cached_facebook_image_key";
  private final static String TAG = "SNSClearImagePreference";
	 // This is the constructor called by the inflater
   public SNSClearImagePreference(Context context, AttributeSet attrs) {
       super(context, attrs);
   }

   @Override
   protected void onDialogClosed(boolean positiveResult) 
   {
       super.onDialogClosed(positiveResult);
       if (positiveResult) {
           setEnabled(false);
           if (PREF_EXTRAS_RESET_DEFAULTS.equals(getKey())) 
           {
           	   //rm all images
	           Log.d(TAG,"entering clear cached images ");
	           File path = new File(TwitterHelper.tempimagePath);
	           deleteFiles(path);
	        	 
	           File path_nosdcard = new File(TwitterHelper.tempimagePath_nosdcard);
	           deleteFiles(path_nosdcard);
           } 
       }
   }
   
   public boolean deleteDirectory(File path) 
   {
	    if( path.exists() ) 
	    {
	        File[] files = path.listFiles();
	        for(int i=0; i<files.length; i++) 
                {
	            if(files[i].isDirectory()) 
	            {
	                deleteDirectory(files[i]);
	            }
	            else 
	            {
	            	try
	            	{
	                    files[i].delete();
	            	}
	            	catch(Exception ne)
	            	{
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	            	}
	            }
	        }
	    }
	    return( path.delete() );
   }
   
   public void deleteFiles(File path) 
   {
           if( path.exists() ) 
	   {
	       File[] files = path.listFiles();
	       for(int i=0; i<files.length; i++) 
	       {
	           if(files[i].isDirectory()) 
	           {
	               try
	               {
	                   deleteDirectory(files[i]);
	               }
	               catch(Exception ne)
	               {
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	               }
	           }
	           else 
	           {
	               try
	               {
	                   files[i].delete();
	               }
	               catch(Exception ne)
	               {
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	               }
	           }
	       }
	   }     
    }
}
