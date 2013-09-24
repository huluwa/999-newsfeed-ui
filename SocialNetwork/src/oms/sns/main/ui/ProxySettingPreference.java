package oms.sns.main.ui;

import oms.sns.main.R;
import oms.sns.main.providers.SocialORM;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.Window;
import android.widget.EditText;

public class ProxySettingPreference extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener 
{
	final String TAG ="ProxySettingPreferenceextends";
	private SocialORM orm;
	
	public boolean onPreferenceChange(Preference pref, Object value) {
		String key = pref.getKey(); 
		if(key.equals("key_sns_proxy_enable"))
        {
        	 boolean checked = (Boolean)value;
             orm.setProxyEnable(checked);
             ((CheckBoxPreference)(pref)).setChecked(checked);
             pref.setSummary(checked==true?getString(R.string.sns_allow_summary):getString(R.string.sns_forbiden_summary));
        }
        else if(key.equals("key_sns_proxy_host"))
        {
        	 String host = (String)value;
             orm.setProxyHost(host);
             pref.setSummary(host);
        }
        else if(key.equals("key_sns_proxy_port")) 	
        {
        	 String port = (String)value;
             orm.setProxyPort(port);
             pref.setSummary(port);
        }
        else if(key.equals("key_sns_proxy_username"))
        {
        	String username = (String)value;
            orm.setProxyUsername(username);
            pref.setSummary(username);
        }
        else if(key.equals("key_sns_proxy_pwd"))
        {
        	String pwd = (String)value;
            orm.setProxyPassword(pwd);     
            pref.setSummary("******");
        }     
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{       
        super.onCreate(savedInstanceState);
        
        // Load the XML preferences file
        addPreferencesFromResource(R.xml.proxy_preference);
        
        orm = new SocialORM(this);
        
        
        boolean proxyenable = orm.isProxyEnable();
        Preference e = findPreference("key_sns_proxy_enable");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.sns_enable_proxy);
        e.setSummary(proxyenable==true?getString(R.string.sns_allow_summary):getString(R.string.sns_forbiden_summary));
        ((CheckBoxPreference)e).setChecked(proxyenable);
        
        String host = orm.getProxyHost();
        e = findPreference("key_sns_proxy_host");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.sns_proxy_host);
        if(host != null)
        {
	        e.setSummary(String.format("%1$s", host));
	        ((EditTextPreference)(e)).setText(String.format("%1$s", host));
        }
        
        String port = orm.getProxyPort();
        e = findPreference("key_sns_proxy_port");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.sns_proxy_port);
        e.setSummary(port);
        ((EditTextPreference)(e)).setText(port);
        
        String username = orm.getProxyUsername();
        e = findPreference("key_sns_proxy_username");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.sns_user_name);
        if(username != null)
        {
	        e.setSummary(String.format("%1$s", username));
	        ((EditTextPreference)(e)).setText(String.format("%1$s", username));
        }
        
        String pwd = orm.getProxyPassword();
        e = findPreference("key_sns_proxy_pwd");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.twitter_password_title);
        if(pwd != null)
        {
            e.setSummary(String.format("%1$s", "******"));
            ((EditTextPreference)(e)).setText(String.format("%1$s", pwd));
            
            EditText editText = ((EditTextPreference)e).getEditText();      
            if (editText != null) 
            {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());                
            }
        }
	}
}
