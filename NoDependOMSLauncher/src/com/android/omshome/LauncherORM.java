package com.android.omshome;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.android.omshome.LauncherProvider.DatabaseHelper;
import com.android.omshome.LauncherSettings.Favorites;
import com.android.omshome.quickaction.QuickLauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


public class LauncherORM {
	public final  static Uri HOME_CONTENT_URI = LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION;
	
	private Context mContext;

	private static final String TAG="LauncherORM";
	private static LauncherORM _intance;
	private AppWidgetHost mAppWidgetHost;
	
	public final static String default_page_index="default_page_index";
	public final static String delete_default_page="delete_default_page";
	public final static String default_2d_3d_choose="default_2d_3d_choose";
	public final static String dock_style="dock_style";
	public final static String allapps_view_style="allapps_view_style";
	public final static String pagemanager_view_style="pagemanager_view_style";
	
	private LauncherORM(Context con)
	{
		mContext = con.getApplicationContext();		
		if(Launcher.class.isInstance(con))
		{
			Launcher la = (Launcher)con;
		    mAppWidgetHost = la.getAppWidgetHost();
		}
		else
		{
			mAppWidgetHost = new AppWidgetHost(mContext.getApplicationContext(), Launcher.APPWIDGET_HOST_ID);
		}
	}
	
	public void destroy()
	{
		if(mAppWidgetHost != null)
		{
			mAppWidgetHost = null;
		}
		
		_intance = null;
		Log.d(TAG, "destroy");
		
	}
	public static synchronized LauncherORM instanceUnSafe()
	{
		return _intance;
	}
	
	public static synchronized LauncherORM instance(Context con)
	{
		if(_intance == null)
		{
			_intance = new LauncherORM(con);
		}
		
		return _intance;
	}
	
	//settings
    public class CategoryNameCol{
        public static final String ID     = "_id";
        public static final String CID    = "c_id";
        public static final String CNAME  = "cname";
    }
    public class CategoryName{
        public int ID  ;
        public int CID ;
        public String CatetoryName ;
    }
    public static String[]CategoryNameProject =  new String[]{
        "_id",
        "c_id",
        "cname",
    };
    
    /*
     * if cid > 0, it is to find the real category, or return all category 
     */
    public List<CategoryName> getCategoryName(int cid)
    {
        final Uri CONTENT_URI = Favorites.CONTENT_categoryname_URI_NO_NOTIFICATION;
        String[] selectionArgs = new String[1];
        selectionArgs[0] = "";
        String queryString ="";
        
        try{
            if(cid > 0)
            {
                selectionArgs[0] = String.valueOf(cid);
                queryString = " c_id=? ";
            }
            else
            {
                queryString   = null;
                selectionArgs = null;
            }
        }catch(NumberFormatException ne){}
        
        List<CategoryName> ls = new ArrayList<CategoryName>();
        Cursor cursor = null;
        try
        {
	        cursor = mContext.getContentResolver().query(CONTENT_URI, CategoryNameProject, queryString, selectionArgs, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	            	CategoryName item = new CategoryName();
	                item.ID           =  cursor.getInt(cursor.getColumnIndex(CategoryNameCol.ID));  
	                item.CID          =  cursor.getInt(cursor.getColumnIndex(CategoryNameCol.CID));
	                item.CatetoryName =  cursor.getString(cursor.getColumnIndex(CategoryNameCol.CNAME));              
	                    
	                ls.add(item);
	            }          
	        }
         }
	     catch(Exception ne){
	     }
	     finally
	     {
		     if(cursor != null)
		     {
		        cursor.close();
		     }
	     }
         return ls;
    } 
    
    
    //category
    public class CategoryCol{
        public static final String ID      = "_id";
        public static final String CID     = "c_id";
        public static final String Intent  = "intent";
    }
    public class Category{
        public int    ID  ;
        public int    CID ;
        public String Intent;
    }
    public static String[]CategoryProject =  new String[]{
        "_id",
        "c_id",
        "intent",
    };
    
    /*
     * if cid > 0, it is to find the real category, or return all category 
     */
    public List<Category> getCategory(int cid)
    {
        final Uri CONTENT_URI = Favorites.CONTENT_category_URI_NO_NOTIFICATION;
        String[] selectionArgs = new String[1];
        selectionArgs[0] = "";
        String queryString ="";
        
        try{
            if(cid > 0)
            {
                selectionArgs[0] = String.valueOf(cid);
                queryString = " c_id=? ";
            }
            else
            {
                queryString   = null;
                selectionArgs = null;
            }
        }catch(NumberFormatException ne){}
        
        List<Category> ls = new ArrayList<Category>();
        Cursor cursor = null;
        try
        {
	        cursor = mContext.getContentResolver().query(CONTENT_URI, CategoryProject, queryString, selectionArgs, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	            	Category item = new Category();
	                item.ID           =  cursor.getInt(cursor.getColumnIndex(CategoryCol.ID));  
	                item.CID          =  cursor.getInt(cursor.getColumnIndex(CategoryCol.CID));
	                item.Intent       =  cursor.getString(cursor.getColumnIndex(CategoryCol.Intent));              
	                    
	                ls.add(item);
	            }          
	        }
         }
	     catch(Exception ne){
	    	 Log.d(TAG, "error", ne);
	     }
	     finally
	     {
		     if(cursor != null)
		     {
		        cursor.close();
		     }
	     }
	     
		 Log.d(TAG, "get category cid="+cid + " size="+ls.size());
         return ls;
    } 
    
    private void checkShortcutChanged(int cid, boolean ret)
    {
    	 if(cid == LauncherProvider.ID_SHORT && ret == true)
 	     {
 	    	 QuickLauncher.ChangedShortcut = true;
 	     }
    }
    
    public boolean deleteCategory(int cid)
    {
        boolean ret = false;
    	final Uri CONTENT_URI = Favorites.CONTENT_category_URI_NO_NOTIFICATION;
        
        try
        {
        	String where = String.format("c_id=%1$s", cid);
	        int line = mContext.getContentResolver().delete(CONTENT_URI, where, null);
	        if(line >0)
	        {     
	        	ret = true;
	            Log.d(TAG, "deleted category="+cid);          
	        }
         }
	     catch(Exception ne){
	     }	     
	     
	     checkShortcutChanged(cid, ret);	     
	     
	     return ret;	    
    } 
    
    public boolean AddCategoryItem(int cid, String intent)
    {
    	boolean ret = false;
    	final Uri CONTENT_URI = Favorites.CONTENT_category_URI_NO_NOTIFICATION;
        
    	if(deleteCategoryItem(cid, intent) == true)
    	{
    		 Log.d(TAG, "delete first cid="+cid+" intent="+intent);       
    	}
        
        try
        {
        	ContentValues values = new ContentValues();
        	values.put(CategoryCol.CID,    cid);
        	values.put(CategoryCol.Intent, intent);
	        Uri line = mContext.getContentResolver().insert(CONTENT_URI, values);
	        if(line != null)
	        {     
	        	ret = true;
	            Log.d(TAG, "add cid="+cid+" intent="+intent);          
	        }
         }
	     catch(Exception ne){
	     }
	    
	     checkShortcutChanged(cid, ret);
	     
	     return ret;
	    
    } 
    
    public boolean deleteCategoryItem(int cid, String intent)
    {
        boolean ret = false;
    	final Uri CONTENT_URI = Favorites.CONTENT_category_URI_NO_NOTIFICATION;
        
        try
        {
        	String where = String.format("c_id=%1$s and intent='%2$s'", cid, intent);
	        int line = mContext.getContentResolver().delete(CONTENT_URI, where, null);
	        if(line >0)
	        {     
	        	ret = true;
	            Log.d(TAG, "deleted intent="+intent);          
	        }
         }
	     catch(Exception ne){
	     }
	     
	     checkShortcutChanged(cid, ret);
	     
	     return ret;
	    
    } 
    
    
	//settings
    public class SettingsCol{
        public static final String ID      = "_id";
        public static final String Name    = "name";
        public static final String Value   = "value";
    }
    public class Settings{
        public String ID   ;
        public String Name ;
        public String Value ;
    }
    public static String[]settingsProject =  new String[]{
        "_id",
        "name",
        "value",
    };
    
    
	public List<Settings> getSettings(String name)
    {
        final Uri CONTENT_URI = Favorites.CONTENT_SETTING_URI_NO_NOTIFICATION;
        String[] selectionArgs = new String[1];
        selectionArgs[0] = "";
        String queryString ="";
        
        try{
            if(name != null)
            {
                selectionArgs[0] = name;
                queryString = " name=? ";
            }
            else
            {
                queryString   = null;
                selectionArgs = null;
            }
        }catch(NumberFormatException ne){}
        
        List<Settings> ls = new ArrayList<Settings>();
        Cursor cursor = null;
        try
        {
	        cursor = mContext.getContentResolver().query(CONTENT_URI, settingsProject, queryString, selectionArgs, null);
	        if(cursor != null)
	        {     
	            while(cursor.moveToNext())
	            {
	                Settings item = new Settings();
	                item.ID         =  cursor.getString(cursor.getColumnIndex(SettingsCol.ID));  
	                item.Name       =  cursor.getString(cursor.getColumnIndex(SettingsCol.Name));
	                item.Value      =  cursor.getString(cursor.getColumnIndex(SettingsCol.Value));              
	                    
	                ls.add(item);
	            }          
	        }
         }
	     catch(Exception ne){
	     }
	     finally
	     {
		     if(cursor != null)
		     {
		        cursor.close();
		     }
	     }
         return ls;
    } 
	
    public String getSettingValue(String name)
    {
        String va = null;
        List<Settings> st = getSettings(name);
        if(st.size() > 0)
        {
            va = st.get(0).Value;
        }
        
        return va;
    }
    
    public boolean removeSetting(String name)
    {
    	if(Launcher.LOGD)Log.d(TAG, "removeSetting:"+name);
        int ret = -1;
        final Uri CONTENT_URI = Favorites.CONTENT_SETTING_URI_NO_NOTIFICATION;
        try{
            ret = mContext.getContentResolver().delete(CONTENT_URI, " name='"+name+"'", null);
        }catch(Exception ne){}
        return ret > 0;
    } 
	    
    public boolean addSetting(String name, String value)
    {
    	if(Launcher.LOGD)Log.d(TAG, "addSetting name:"+name+" value:"+value);
    	boolean suc = false;
        Uri ret = null;
        final Uri CONTENT_URI = Favorites.CONTENT_SETTING_URI_NO_NOTIFICATION;
        android.content.ContentValues ct = new android.content.ContentValues();
        ct.put(SettingsCol.Name, name);              
        ct.put(SettingsCol.Value, value);
        
        //if exist, update
        if(getSettings(name).size() > 0)
        {
            suc = updateSetting(name, value);
        }
        else
        {        
        	try{
        		ret = mContext.getContentResolver().insert(CONTENT_URI, ct);
        	}catch(Exception ex){
        		Log.e(TAG, ex.getMessage());
        		ret = null;
        	}
        	
            suc = (ret != null);
        }
        
        return suc;
    } 
    
    public boolean updateSetting(String name, String value)
    {
        boolean ret = false;
        final Uri CONTENT_URI = Favorites.CONTENT_SETTING_URI_NO_NOTIFICATION;
        String where = String.format(" name = \"%1$s\" ", name);
        android.content.ContentValues ct = new android.content.ContentValues();
        ct.put("value", value);
        try{
        	if(mContext.getContentResolver().update(CONTENT_URI, ct, where, null) > 0)
        	{
        		Log.d(TAG, "update default page="+value);
        		ret = true;
        	}        
        }catch(Exception ex){
        	ret = false;
        }
        
        return ret;
    }
    
    public boolean isEnableDeleteDefaultPage()
    {
    	String value = getSettingValue(LauncherORM.delete_default_page);
		int allow = 1;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
    }
    
    public void EnableDeleteDefaultPage( boolean enable)
    {
    	addSetting(delete_default_page, enable==true?"1":"0");
    }  
    
    public void Enable2D( boolean enable)
    {
    	addSetting(default_2d_3d_choose, enable==true?"1":"0");
    }  
    
    public boolean isEnable2D()
    {
    	String value = getSettingValue(LauncherORM.default_2d_3d_choose);
		int allow = 0;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
    }
	
	public void resetFavorites(Context con)
	{
		try{
			DatabaseHelper dbh = new DatabaseHelper(con);
			SQLiteDatabase mDB = dbh.getWritableDatabase();
			
			Log.w(TAG, "reset Database ");
	        mDB.execSQL("DROP TABLE IF EXISTS " +  LauncherProvider.TABLE_FAVORITES);
	        mDB.execSQL("DROP TABLE IF EXISTS " + LauncherProvider.TABLE_SETTINGS);
	        mDB.execSQL("DROP TABLE IF EXISTS " + LauncherProvider.TABLE_CATEGORY);
	        mDB.execSQL("DROP TABLE IF EXISTS " + LauncherProvider.TABLE_CATEGORY_name);
	        
	        dbh.onCreate(mDB);     
	        
	        mDB.close();
	        dbh.close();        
	    }catch(Exception ex){
	    	Log.d(TAG,"resetFavorites exit with 0");
	    }
	}
	
	public  void RebindAppWidgets() 
    {
        final int[] bindSources = new int[] {
                Favorites.ITEM_TYPE_WIDGET_CLOCK,
                Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
                Favorites.ITEM_TYPE_APPWIDGET,
        };
        
        final String selectWhere = LauncherProvider.buildOrWhereString(Favorites.ITEM_TYPE, bindSources);        
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        Cursor c = null;        
        
        try {
            String[]project = new String[] { Favorites._ID,  Favorites.APPWIDGET_ID, Favorites.ITEM_TYPE, Favorites.INTENT, Favorites.TITLE};
            // Select and iterate through each matching widget
            c = mContext.getContentResolver().query(HOME_CONTENT_URI, project, selectWhere, null, null);            
            if (Launcher.LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());
            
            final ContentValues values = new ContentValues();
            while (c != null && c.moveToNext()) {
                long favoriteId    = c.getLong(0);
                int preAppWidgetId = c.getInt(1);
                String packageName = c.getString(c.getColumnIndex(Favorites.INTENT));
                String className   = c.getString(c.getColumnIndex(Favorites.TITLE));
                
                // Allocate and update database with new appWidgetId
                int appWidgetId = -1;
                if(null != mAppWidgetHost){
                	try {
                		appWidgetId = mAppWidgetHost.allocateAppWidgetId();
                		
                		if (Launcher.LOGD) 
                		{
                			Log.d(TAG, "rebind allocated appWidgetId=" + appWidgetId + " for favoriteId=" + favoriteId);
                		}
                		
                		values.clear();
                		values.put(LauncherSettings.Favorites.APPWIDGET_ID, appWidgetId);
                		
                		String updateWhere = Favorites._ID + "=" + favoriteId;
                		mContext.getContentResolver().update(HOME_CONTENT_URI, values, updateWhere, null);
                		
                		ComponentName cn = new ComponentName(packageName, className);
                		appWidgetManager.bindAppWidgetId(appWidgetId, cn);
                		
                		//update database
                		
                		//if(packageName.contains("com.cooliris.media") || packageName.contains("com.android.camera"))
                		{
                			Intent intent = new Intent("android.appwidget.action.APPWIDGET_REBINDID");
                			intent.setClassName(packageName, className);
                			intent.putExtra("ID_OLD", preAppWidgetId);
                			intent.putExtra("ID_NEW", appWidgetId);
                			mContext.sendBroadcast(intent);
                		}
                	} catch (RuntimeException ex) {
                		if(appWidgetId != -1)
                		{
                			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                		}
                		Log.e(TAG, "Problem allocating packageName:"+packageName + " ex="+ex.getMessage());
                	}
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
        } finally {
        	if(c != null)
        	{
                c.close();
        	}
        }        
    }
	
	public  int RemoveAppWidgets(int appOldWidgetId) 
    {
		int   lines = 0;
        final String selectWhere = Favorites.ITEM_TYPE + " = "+Favorites.ITEM_TYPE_APPWIDGET + " AND "+ Favorites.APPWIDGET_ID + " = " +appOldWidgetId;
        if(null != mAppWidgetHost)
        {
	        try {
	        	lines = mContext.getContentResolver().delete(HOME_CONTENT_URI, selectWhere, null);            
	            Log.d(TAG, "no app widget exist, delete count=" + lines);
	            
	            mAppWidgetHost.deleteAppWidgetId(appOldWidgetId);
	        } catch (Exception ex) {
	            Log.w(TAG, "Problem while remove appWidgetIds for existing widgets", ex);
	        }
        }
        
        return lines;
    }
	
	public  int RebindAppWidgets(int appOldWidgetId) 
    {
        final String selectWhere = Favorites.ITEM_TYPE + " = "+Favorites.ITEM_TYPE_APPWIDGET + " AND "+ Favorites.APPWIDGET_ID + " = " +appOldWidgetId;        
        Cursor c = null;        
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        
        if(null != mAppWidgetHost){
	        try {
	            mAppWidgetHost.deleteAppWidgetId(appOldWidgetId);
	            String[]project = new String[] { Favorites._ID,  Favorites.APPWIDGET_ID, Favorites.ITEM_TYPE, Favorites.INTENT, Favorites.TITLE};
	            // Select and iterate through each matching widget
	            c = mContext.getContentResolver().query(HOME_CONTENT_URI, project, selectWhere, null, null);            
	            if (Launcher.LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());
	            
	            final ContentValues values = new ContentValues();
	            while (c != null && c.moveToNext()) {
	                long favoriteId    = c.getLong(0);
	                int preAppWidgetId = c.getInt(1);
	                String packageName = c.getString(c.getColumnIndex(Favorites.INTENT));
	                String className   = c.getString(c.getColumnIndex(Favorites.TITLE));
	                
	                // Allocate and update database with new appNewWidgetId
	                int appNewWidgetId = -1;
	                	try {
	                		appNewWidgetId = mAppWidgetHost.allocateAppWidgetId();
	                		
	                		if (Launcher.LOGD) 
	                		{
	                			Log.d(TAG, "rebind allocated appWidgetId=" + appNewWidgetId + " old id=" + appOldWidgetId + " for favoriteId=" + favoriteId);
	                		}
	                		
	                		values.clear();
	                		values.put(LauncherSettings.Favorites.APPWIDGET_ID, appNewWidgetId);
	                		
	                		String updateWhere = Favorites._ID + "=" + favoriteId;
	                		mContext.getContentResolver().update(HOME_CONTENT_URI, values, updateWhere, null);
	                		
	                		ComponentName cn = new ComponentName(packageName, className);
	                		appWidgetManager.bindAppWidgetId(appNewWidgetId, cn);
	                		
	                		//if(packageName.contains("com.cooliris.media") || packageName.contains("com.android.camera"))
	                		{
	                			Intent intent = new Intent("android.appwidget.action.APPWIDGET_REBINDID");
	                			intent.setClassName(packageName, className);
	                			intent.putExtra("ID_OLD", preAppWidgetId);
	                			intent.putExtra("ID_NEW", appNewWidgetId);
	                			mContext.sendBroadcast(intent);
	                		}
	                	} catch (RuntimeException ex) {
	                		if(appNewWidgetId != -1)
	                		{
	                			mAppWidgetHost.deleteAppWidgetId(appNewWidgetId);
	                		}
	                		Log.e(TAG, "Problem allocating packageName:"+packageName + " ex="+ex.getMessage());
	                	}
	                
	                return appNewWidgetId;
	            }
	        } catch (Exception ex) {
	            Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
	        } finally {
	        	if(c != null)
	        	{
	                c.close();
	        	}
	        } 
        }
        
        return -1;
    }

	public int getCurrentCategoryID() {
		
		return 0;
	}

	public boolean isEnableAutoAddScreen() {		
		String value = getSettingValue("isEnableAutoAddScreen");
		int allow = 0;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableAutoAddScreen( boolean enable)
    {
    	addSetting("isEnableAutoAddScreen", enable==true?"1":"0");
    }

	public boolean isLandscapeSupport() {		
		String value = getSettingValue("isLandscapeSupport");
		int allow = 0;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableLandscapeSupport( boolean enable)
    {
    	addSetting("isLandscapeSupport", enable==true?"1":"0");
    }

	public boolean isAlwaysShowAddQuickLaunch() {		
		String value = getSettingValue("isAlwaysShowAddQuickLaunch");
		//default is always
		int allow = 1;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableAlwaysShowAddQuickLaunch( boolean enable)
    {
    	addSetting("isAlwaysShowAddQuickLaunch", enable==true?"1":"0");
    }

	public boolean isAlwaysShowHint() {		
		String value = getSettingValue("EnableAlwaysShowHint");
		//default is always
		int allow = 0;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableAlwaysShowHint( boolean enable)
    {
    	addSetting("EnableAlwaysShowHint", enable==true?"1":"0");
    }
	
	public boolean isUseColorfulBG() {		
		String value = getSettingValue("isUseColorfulBG");
		//default is always
		int allow = 1;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableisUseColorfulBG( boolean enable)
    {
    	addSetting("isUseColorfulBG", enable==true?"1":"0");
    }

	public boolean isUserBGInAllAppView() {		
		String value = getSettingValue("isUserBGInAllAppView");
		//default is always
		int allow = 0;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableUseBGInAllAppView( boolean enable)
    {
    	addSetting("isUserBGInAllAppView", enable==true?"1":"0");
    }

	
	public boolean isEnableSwitchCategory() {		
		String value = getSettingValue("isEnableSwitchCategory");
		//default is always
		int allow = 0;
	    if(value != null)
	    {	    	 
	    	try{
	    	    allow = Integer.parseInt(value);
	    	}catch(Exception ne){}
	    }	
	    final boolean enableDelete = (allow==0?false:true);	    
	    return enableDelete;
	}
	
	public void EnableSwitchCategory( boolean enable)
    {
    	addSetting("isEnableSwitchCategory", enable==true?"1":"0");
    }
    
    public boolean isShowTopBar() {       
        String value = getSettingValue("isShowTopBar");
        //default is always
        int allow = 1;
        if(value != null)
        {            
            try{
                allow = Integer.parseInt(value);
            }catch(Exception ne){}
        }   
        final boolean enableDelete = (allow==0?false:true);     
        return enableDelete;
    }
    
    public void ShowTopBar( boolean enable)
    {
        addSetting("isShowTopBar", enable==true?"1":"0");
    }

    public boolean isUserAlphabetOrder() {       
        String value = getSettingValue("isUserAlphabetOrder");
        //default is always
        int allow = 1;
        if(value != null)
        {            
            try{
                allow = Integer.parseInt(value);
            }catch(Exception ne){}
        }   
        final boolean enableDelete = (allow==0?false:true);     
        return enableDelete;
    }
    
    public void UserAlphabetOrder( boolean enable)
    {
        addSetting("isUserAlphabetOrder", enable==true?"1":"0");
    }	
	
	public boolean isAlwaysWallPaperMove() {       
        String value = getSettingValue("EnableAlwaysWallPaperMove");
        //default is not always
        int allow = 0;
        if(value != null)
        {            
            try{
                allow = Integer.parseInt(value);
            }catch(Exception ne){}
        }   
        final boolean enableDelete = (allow==0?false:true);     
        return enableDelete;
    }
    
    public void EnableAlwaysWallPaperMove( boolean enable)
    {
        addSetting("EnableAlwaysWallPaperMove", enable==true?"1":"0");
    }
    
    public boolean isAppViewTransparent() {       
        String value = getSettingValue("EnableAppViewTransparent");
        //default is not always
        int allow = 0;
        if(value != null)
        {            
            try{
                allow = Integer.parseInt(value);
            }catch(Exception ne){}
        }   
        final boolean enableDelete = (allow==0?false:true);     
        return enableDelete;
    }
    
    public void EnableAppViewTransparent( boolean enable)
    {
        addSetting("EnableAppViewTransparent", enable==true?"1":"0");
    }	
}
