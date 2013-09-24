package com.ast.free.ui;

import com.ast.free.providers.SocialProvider;
import com.ast.free.service.internal.ContactHelper;

import android.content.Context;
import android.util.AttributeSet;
import com.android.internal.preference.YesNoPreference;

public class SNSClearFacebookinfoPreference extends YesNoPreference
{
    private final static String PREF_EXTRAS_RESET_DEFAULTS = "clear_fbinfo_key";
    // This is the constructor called by the inflater
   public SNSClearFacebookinfoPreference(Context context, AttributeSet attrs) {
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
                //SocialProvider.resetDatabase(getContext());  
                ContactHelper.clearFacebookInfo(getContext());
                FacebookSettingPreference.finishPreference();
           } 
       }
   }
}
