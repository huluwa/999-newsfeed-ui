package com.android.omshome;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference;
public class HomeSettingPreference extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
	public static final int allapps_view_style_2d_land = 0;
	public static final int allapps_view_style_2d      = 1;
	public static final int allapps_view_style_3d      = 2;
	public static final int pagemanager_view_style_2d  = 0;
	public static final int pagemanager_view_style_3d  = 1;
	
	public boolean onPreferenceChange(Preference pref, Object arg1) 
	{		
		String key = pref.getKey(); 
		if(key.equals("key_home_page_list"))
        {
			String Value = (String)arg1;			
			final ContentResolver cr = getContentResolver();
			Launcher.setSettingsIntValue(this, LauncherORM.default_page_index, Integer.valueOf(Value));
			ListPreference le = (ListPreference)pref;
			le.setValue(Value);
        }
		else if(key.equals("key_delete_default_enable"))
		{
			boolean value = (Boolean)arg1;			
			orm.EnableDeleteDefaultPage(value);
			CheckBoxPreference dp  = (CheckBoxPreference)pref;
			dp.setChecked(value);
		}
//		else if(key.equals("key_2d_3d_exchange"))
//		{
//			boolean value = (Boolean)arg1;			
//			orm.Enable2D(value);
//			CheckBoxPreference dp  = (CheckBoxPreference)pref;
//			dp.setChecked(value);
//		}
		else if(key.equals("key_dock_style"))
		{
			String value = (String)arg1;			
			orm.addSetting(LauncherORM.dock_style, value);
			ListPreference le = (ListPreference)pref;
			le.setValue(value);
		}
		else if(key.equals("key_allapps_view_style"))
		{
			String value = (String)arg1;
			orm.addSetting(LauncherORM.allapps_view_style, value);
			ListPreference le = (ListPreference)pref;
			le.setValue(value);
		}
		else if(key.equals("key_pagemanager_view_style"))
		{
			String value = (String)arg1;
			orm.addSetting(LauncherORM.pagemanager_view_style, value);
			ListPreference le = (ListPreference)pref;
			le.setValue(value);
		}
		
		return false;
	}
	
	LauncherORM orm;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		 super.onCreate(savedInstanceState);
		 addPreferencesFromResource(R.xml.home_setting);
		 
		 orm = LauncherORM.instance(this);
		 
//		 ListPreference le = (ListPreference)findPreference("key_home_page_list");
//	     le.setOnPreferenceChangeListener(this);
//	     le.setValue(value);
	     
	     String dockStyle = orm.getSettingValue(LauncherORM.dock_style);
		 ListPreference lp = (ListPreference)findPreference("key_dock_style");
		 lp.setOnPreferenceChangeListener(this);
		 lp.setValue(dockStyle);
	     
//	     boolean allow = orm.isEnableDeleteDefaultPage();	     
//	     CheckBoxPreference dp  = (CheckBoxPreference)findPreference("key_delete_default_enable");
//	     dp.setOnPreferenceChangeListener(this);
//	     dp.setChecked(allow);	
	     
//	     boolean is2DEnable = orm.isEnable2D();	     
//	     CheckBoxPreference twodp  = (CheckBoxPreference)findPreference("key_2d_3d_exchange");
//	     twodp.setOnPreferenceChangeListener(this);
//	     twodp.setChecked(is2DEnable);	
	     
	     String allAppsViewStyle = orm.getSettingValue(LauncherORM.allapps_view_style);
		 ListPreference allappsviewsytlep = (ListPreference)findPreference("key_allapps_view_style");
		 allappsviewsytlep.setOnPreferenceChangeListener(this);
		 allappsviewsytlep.setValue(allAppsViewStyle);
		 
		 String pageManagerViewStyle = orm.getSettingValue(LauncherORM.pagemanager_view_style);
		 ListPreference pagemanagerviewsytlep = (ListPreference)findPreference("key_pagemanager_view_style");
		 pagemanagerviewsytlep.setOnPreferenceChangeListener(this);
		 pagemanagerviewsytlep.setValue(pageManagerViewStyle);
	     
    }	
}
