/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.omshome25;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.Intent.ShortcutIconResource;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.borqs.omshome25.LauncherProvider.DatabaseHelper;
import com.borqs.omshome25.LauncherSettings.Favorites;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final boolean DEBUG_LOADERS = android.util.Config.DEBUG;
    static final boolean PROFILE_LOADERS = false;
    static final String TAG = "oms2.5Launcher.Model";

    private int mBatchSize; // 0 is all apps at once
    private int mAllAppsLoadDelay; // milliseconds between batches

    private final LauncherApplication mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
//    private Handler mRsClearHandler = new Handler();
    private Loader mLoader = new Loader();

    // We start off with everything not loaded.  After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery.  These are only ever touched from the loader thread.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    private boolean mBeforeFirstLoad = true; // only access this from main thread
    private WeakReference<Callbacks> mCallbacks;

    private final Object mAllAppsListLock = new Object();
    private AllAppsList mAllAppsList;
    
    public ArrayList<Intent> waitedIntents = new ArrayList<Intent>();
    public boolean isLoadOK = false;
    public boolean isHaveSdcardMountMessage = false;
    
    public ArrayList<ApplicationInfo> getAllAppInfo()
    {
    	return (ArrayList<ApplicationInfo>)mAllAppsList.data.clone();
    }
    private IconCache mIconCache;

    private Bitmap mDefaultIcon;

    public interface Callbacks {
        public int getCurrentWorkspaceScreen();
        public void startBinding();
        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems();
        public void bindAppWidget(LauncherAppWidgetInfo info);	    
        public void bindAllApplications(ArrayList<ApplicationInfo> apps);
        public void bindAllApplications(ArrayList<ApplicationInfo> apps, boolean forSdcardReload);
        public void bindAppsAdded(ArrayList<ApplicationInfo> apps);
        public void bindAppsUpdated(ArrayList<ApplicationInfo> apps);
        public void bindAppsRemoved(ArrayList<ApplicationInfo> apps);
        public boolean isAllAppsVisible();        
        public void postWaitedIntents();
    }

    LauncherModel(LauncherApplication app, IconCache iconCache) {
        mApp = app;
        mAllAppsList = new AllAppsList(iconCache);
        mIconCache = iconCache;

        mDefaultIcon = Utilities.createIconBitmap(
                app.getPackageManager().getDefaultActivityIcon(), app);

        mAllAppsLoadDelay = app.getResources().getInteger(R.integer.config_allAppsBatchLoadDelay);

        mBatchSize = app.getResources().getInteger(R.integer.config_allAppsBatchSize);
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     */
    static void addOrMoveItemInDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY) {
//        if(Launcher.LOGD)Log.d(TAG,"addOrMoveItemInDatabase item:"+item+" screen:"+screen+" cellX:"+cellX+" cellY:"+cellY);
        if (item.container == ItemInfo.NO_ID) {
            // From all apps
            addItemToDatabase(context, item, container, screen, cellX, cellY, false);
        } else {
            // From somewhere else
            moveItemInDatabase(context, item, container, screen, cellX, cellY);
        }
    }

    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, ItemInfo item, long container, int screen,
            int cellX, int cellY) {
//        if(Launcher.LOGD)Log.d(TAG,"moveItemInDatabase time:"+item+" screen:"+screen+" cellX:"+cellX+" cellY:"+cellY);
        item.container = container;
        item.screen = screen;
        item.cellX = cellX;
        item.cellY = cellY;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screen);

        try{
        	cr.update(LauncherSettings.Favorites.getContentUri(item.id, false), values, null, null);
        }catch(Exception ex){}
    }

    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
    	boolean result = false;
        final ContentResolver cr = context.getContentResolver();
        Cursor c = null;
       
        try {
            c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
            new String[] { "title", "intent" }, "title=? and intent=?",
            new String[] { title, intent.toUri(0) }, null);
            result = c.moveToFirst();
        }catch(Exception ex){
        }finally {
            if(null != c)c.close();
        }
        return result;
    }

    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
     */
    FolderInfo getFolderById(Context context, HashMap<Long,FolderInfo> folderList, long id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = null;
        try {
            c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                "_id=? and (itemType=? or itemType=?)",
                new String[] { String.valueOf(id),
                        String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER),
                        String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER) }, null);

            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

                FolderInfo folderInfo = null;
                switch (c.getInt(itemTypeIndex)) {
                    case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                        folderInfo = findOrMakeUserFolder(folderList, id);
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
                        folderInfo = findOrMakeLiveFolder(folderList, id);
                        break;
                }

                folderInfo.title = c.getString(titleIndex);
                folderInfo.id = id;
                folderInfo.container = c.getInt(containerIndex);
                folderInfo.screen = c.getInt(screenIndex);
                folderInfo.cellX = c.getInt(cellXIndex);
                folderInfo.cellY = c.getInt(cellYIndex);

                return folderInfo;
            }
        } catch(Exception ex){        	
        }finally {
        	if(null != c)c.close();
        }

        return null;
    }

    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY, boolean notify) {
//        if(Launcher.LOGD)Log.d(TAG,"addItemToDatabase item:"+item);
        item.container = container;
        item.screen = screen;
        item.cellX = cellX;
        item.cellY = cellY;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        item.onAddToDatabase(values);
        Uri result = null;
        try{
        	 result = cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
        	 LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);
        }catch(Exception ex){}

        if (result != null) {
            item.id = Integer.parseInt(result.getPathSegments().get(1));
        }
    }

    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, ItemInfo item) {
//        if(Launcher.LOGD)Log.d(TAG,"updateItemInDatabase item:"+item);
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        item.onAddToDatabase(values);

        cr.update(LauncherSettings.Favorites.getContentUri(item.id, false), values, null, null);
    }

    static void updateItemInDatabase(Context context, long id, int newScreenIndex) {
//        if(Launcher.LOGD)Log.d(TAG,"updateItemInDatabase id:"+id);
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        
        values.put(LauncherSettings.Favorites.SCREEN, newScreenIndex);
        cr.update(LauncherSettings.Favorites.getContentUri(id, false), values, null, null);
    }
    
    static ArrayList<Long> getItemsIdWithScreenIndex(Context context, int screen) {
    	ArrayList<Long> ids = new ArrayList<Long>();
    	
    	final ContentResolver cr = context.getContentResolver();
    	String[] projection = {LauncherSettings.Favorites._ID};
    	Cursor cursor = null;
    	
    	try{
    		cursor = cr.query(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, projection, LauncherSettings.Favorites.SCREEN+"="+screen, null, null);
    		if(cursor != null && cursor.getCount() > 0) {
    			cursor.moveToFirst();
    			do{
    				Long id = cursor.getLong(cursor.getColumnIndexOrThrow(LauncherSettings.Favorites._ID));
    				if(null != id){
    					ids.add(id);
    				}
    			} while(cursor.moveToNext());
    		}
    	}catch(Exception ex){
    		Log.d(TAG, ex.getMessage());
    	}finally{
    		if(null != cursor){
    			cursor.close();
    		}
    	}
    	
//    	Log.d(TAG, "getItemsIdWithScreenIndex, screen:"+screen+",find count:"+ids.size());
    	return ids;
    }
    
    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, ItemInfo item) {
//        if(Launcher.LOGD)Log.d(TAG,"deleteItemFromDatabase item:"+item);
        final ContentResolver cr = context.getContentResolver();
        try{
            cr.delete(LauncherSettings.Favorites.getContentUri(item.id, false), null, null);
        }catch(Exception ex){}
   }
    
    static void deleteItemsFromDatabase(Context context, int screenIndex) {
    	  if(Launcher.LOGD)Log.d(TAG,"deleteItemsFromDatabase screenIndex:"+screenIndex);
          final ContentResolver cr = context.getContentResolver();
          try{
        	  cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, LauncherSettings.Favorites.SCREEN+"="+String.valueOf(screenIndex), null);
          }catch(Exception ex){}
    }
    
    static void updateItemScreen(Context context, String where, boolean isAdd) throws Exception{
//    	Log.d(TAG, "updateItemScreen where:"+where+" isAdd:"+isAdd);
    	SQLiteDatabase mDB = null;
    	
    	try {
    			String sql = "";
				if(isAdd){
					sql = "update "+ LauncherProvider.TABLE_FAVORITES + " set "+ 
					             LauncherSettings.Favorites.SCREEN +"="+LauncherSettings.Favorites.SCREEN+"+1 where "+where; 
				}else{
					sql = "update "+ LauncherProvider.TABLE_FAVORITES + " set "+ 
		             			 LauncherSettings.Favorites.SCREEN +"="+LauncherSettings.Favorites.SCREEN+"-1 where "+where; 
				}
//				Log.d(TAG, "updateItemScreen sql:"+sql);
		    	mDB = LauncherProvider.mOpenHelper.getWritableDatabase();
				mDB.execSQL(sql);
    	}catch(Exception ex) {
    		Log.e(TAG, ex.toString());
    		throw ex;
    	} finally {
    		if(mDB != null) {
    			mDB.close();
    		}
    	}
    }
    
    static void moveItemsWithScreenIndex(Context context, int fromIndex, int toIndex) throws Exception {
//    	if(Launcher.LOGD)Log.d(TAG,"moveItemsWithScreenIndex exit fromIndex:"+fromIndex+" toIndex:"+toIndex);

    	ArrayList<Long> ids = getItemsIdWithScreenIndex(context, fromIndex);
    	  
    	SQLiteDatabase db = LauncherProvider.mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
        	  String sql = "";
        	  if(-1 == toIndex){ // move to last screen
        		  String where = LauncherSettings.Favorites.SCREEN+">"+fromIndex;        		  
        	    				
				sql = "update "+ LauncherProvider.TABLE_FAVORITES + " set "+ 
		             			 LauncherSettings.Favorites.SCREEN +"="+LauncherSettings.Favorites.SCREEN+"-1 where "+where;
				
//				Log.d(TAG, "updateItemScreen sql:"+sql);        			    	
				db.execSQL(sql);
				
        	  } else if(fromIndex < toIndex) { // move right
        		  String where = LauncherSettings.Favorites.SCREEN+">"+fromIndex+" and "+LauncherSettings.Favorites.SCREEN+"<="+toIndex;
        		  			
  				  sql = "update "+ LauncherProvider.TABLE_FAVORITES + " set "+ 
  		             			 LauncherSettings.Favorites.SCREEN +"="+LauncherSettings.Favorites.SCREEN+"-1 where "+where;
  				
//  				  Log.d(TAG, "updateItemScreen sql:"+sql);        			    	
  				  db.execSQL(sql);
        	  } else {  //move left
        		  String where = LauncherSettings.Favorites.SCREEN+">="+toIndex+" and "+LauncherSettings.Favorites.SCREEN+"<"+fromIndex;        		  
        		  sql = "update "+ LauncherProvider.TABLE_FAVORITES + " set "+ 
		             LauncherSettings.Favorites.SCREEN +"="+LauncherSettings.Favorites.SCREEN+"+1 where "+where;
        		  
        		  db.execSQL(sql);
        	  }
        	  
    		  if(ids != null && ids.size()>0){
    			  StringBuffer buffer = new StringBuffer();
    			  for(Long id:ids){
    				  buffer.append(id + ",");
    			  }
    			  
    			  int changeToScreen=0;    			  
    			  if(-1 == toIndex){
    				  changeToScreen = Launcher.getScreenCount()-1;
    			  }else{
    				  changeToScreen = toIndex;
    			  }
    			  
    			  String where =  LauncherSettings.Favorites._ID+" in ("+buffer.substring(0, buffer.length() -1)+")";
    			  sql = "update "+ LauncherProvider.TABLE_FAVORITES + " set "+ 
		             LauncherSettings.Favorites.SCREEN +"="+changeToScreen+" where "+where;
    			  db.execSQL(sql);
    		  }
    		 
    		  db.setTransactionSuccessful();
             
        } catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
            db.endTransaction();
        }

        if(Launcher.LOGD)Log.d(TAG,"moveItemsWithScreenIndex exit ok");
    }
    
    static void updateItemsWithDeleteScreenIndexFromDatabase(Context context, int screenIndex) {
//        if(Launcher.LOGD)Log.d(TAG,"updateItemsWithDeleteScreenIndexFromDatabase screenIndex:"+screenIndex);
        final ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
        	cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, LauncherSettings.Favorites.SCREEN+"="+String.valueOf(screenIndex), null);
            String[] projection = new String[] {LauncherSettings.Favorites._ID, LauncherSettings.Favorites.SCREEN};
            cursor = cr.query(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, projection, LauncherSettings.Favorites.SCREEN+">"+String.valueOf(screenIndex),null,null);
            if(cursor != null && cursor.getCount() > 0) {
//                if(Launcher.LOGD)Log.d(TAG,"updateItemsWithDeleteScreenIndexFromDatabase count:"+cursor.getCount());
                cursor.moveToFirst();
                ContentValues values = new ContentValues();
                do{
                	values.clear();
                    values.put(LauncherSettings.Favorites.SCREEN, String.valueOf(cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.SCREEN))-1));
                    cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values, LauncherSettings.Favorites._ID+"="+String.valueOf(cursor.getLong(cursor.getColumnIndex(LauncherSettings.Favorites._ID))),null);
//                    if(Launcher.LOGD)Log.d(TAG,"update id:"+cursor.getLong(cursor.getColumnIndex(LauncherSettings.Favorites._ID))+" screen:"+cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.SCREEN)));
                } while(cursor.moveToNext());
            }
        }catch(Exception ex) {
            Log.d(TAG,"updateItemsWithDeleteScreenIndexFromDatabase exit with 0");
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    static void addItemInToApplicationShortcuts(Context context, ApplicationInfo info){
//    	 if(Launcher.LOGD)Log.d(TAG,"addItemInToApplicationShortcuts info:"+info);
    	 String intent = info.intent.getComponent().getPackageName()+"/"+info.intent.getComponent().getClassName();
         LauncherORM.instance(context).AddCategoryItem(LauncherProvider.ID_SHORT, intent);
    }
    
    static void deleteItemFromApplicationShortcuts(Context context, ApplicationInfo info) {    	
//        if(Launcher.LOGD)Log.d(TAG,"deleteItemFromApplicationShortcuts info intent:"+info.intent.toUri(0));
        
        String intent = info.intent.getComponent().getPackageName()+"/"+info.intent.getComponent().getClassName();        
        LauncherORM.instance(context).deleteCategoryItem(LauncherProvider.ID_SHORT, intent);
   }
    
    static List<LauncherORM.Category> listApplicationInfoShortcuts(Context context){
//    	  if(Launcher.LOGD)Log.d(TAG,"listApplicationInfoShortcuts");
    	  List<LauncherORM.Category> shortcuts = new ArrayList<LauncherORM.Category>();
    	  shortcuts = LauncherORM.instance(context).getCategory(LauncherProvider.ID_SHORT);
      	
      	return shortcuts;
    }
    
    /**
     * Remove the contents of the specified folder from the database
     */
    static void deleteUserFolderContentsFromDatabase(Context context, UserFolderInfo info) {
//        if(Launcher.LOGD)Log.d(TAG,"deleteUserFolderContentsFromDatabase info:"+info);
        final ContentResolver cr = context.getContentResolver();

        try {
        	cr.delete(LauncherSettings.Favorites.getContentUri(info.id, false), null, null);
        	cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
        			LauncherSettings.Favorites.CONTAINER + "=" + info.id, null);
        }catch(Exception ex){
        	Log.d(TAG,"deleteUserFolderContentsFromDatabase exit with 0");
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
//        if(Launcher.LOGD)Log.d(TAG,"initialize");
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    public void startLoader(Context context, boolean isLaunching) {
        if(Launcher.LOGD)Log.d(TAG,"startLoader");
        mLoader.startLoader(context, isLaunching);
    }
    
    public void startLoader(Context context, boolean isLaunching, boolean justAllApp) {
        if(Launcher.LOGD)Log.d(TAG,"startLoader all app");
        mLoader.startLoader(context, isLaunching, justAllApp);
    }

    public void stopLoader() {
        if(Launcher.LOGD)Log.d(TAG,"stopLoader");
        mLoader.stopLoader();
    }
    
    public Loader getLoader(){
    	return mLoader;
    }

    private Launcher mLauncher;
    public void setLauncher(Launcher launcher){
    	mLauncher = launcher;
    }
    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     */
    public void onReceive(Context context, Intent intent) {
    	Log.d(TAG, "onReceive intent:"+intent);
    	
        // Use the app as the context.
        context = mApp;
        
        if(intent.getAction().equals("omshome.backup_screennumber")){
        	SharedPreferences settings = context.getSharedPreferences(Launcher.INIT_TAG_FILE_NAME, Context.MODE_PRIVATE);
            int screen_count = settings.getInt(Workspace.TAG_SCREEN_NUM, Workspace.MAX_SCREEN_COUNT);
            int default_screen = settings.getInt(LauncherORM.default_page_index, Workspace.DEFAULT_CURRENT_SCREEN);
            
    		Intent in = new Intent("omshome.backup_screennumber_ok");
    		in.putExtra("com.borqs.omshome25.screen_num",     screen_count);
    		in.putExtra("com.borqs.omshome25.default_screen", default_screen);
    		context.sendBroadcast(in);
    		return;
    	}
        else if(intent.getAction().equals("omshome.restore_screennumber")){     	
        	int number     = intent.getIntExtra("com.borqs.omshome25.screen_num", Workspace.MAX_SCREEN_COUNT);
        	int def_number = intent.getIntExtra("com.borqs.omshome25.default_screen", Workspace.DEFAULT_CURRENT_SCREEN);
        	
        	Log.d(TAG,"restore number="+number + " def="+def_number);
        	SharedPreferences settings = context.getSharedPreferences(Launcher.INIT_TAG_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(Workspace.TAG_SCREEN_NUM,       number);
            editor.putInt(LauncherORM.default_page_index, def_number);
            editor.putInt(Launcher.HOME_INIT_TAG, 1);
            
            editor.commit();
            Intent in = new Intent("omshome.restore_screennumber_ok");
    		context.sendBroadcast(in);
    		return;
        }
        

        ArrayList<ApplicationInfo> added = null;
        ArrayList<ApplicationInfo> removed = null;
        ArrayList<ApplicationInfo> modified = null;

        Log.d(TAG, "onReceive  isLoadOK:"+isLoadOK);
        if(!isLoadOK){
        	Log.d(TAG, "onReceive  isLoadOK is false!");
        	synchronized(waitedIntents){
        		waitedIntents.add(intent);
        		return;
        	}
        }

        if (mBeforeFirstLoad) {
            // If we haven't even loaded yet, don't bother, since we'll just pick
            // up the changes.
            return;
        }
        
        
        synchronized (mAllAppsListLock) {
            final String action = intent.getAction();
            if(Launcher.LOGD)Log.d(TAG,"onReceive action:"+action);
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                if (packageName == null || packageName.length() == 0) {
                    // they sent us a bad intent
                    return;
                }

                if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    mAllAppsList.updatePackage(context, packageName);
                    
                    if(packageName.equals("com.android.borqsstk")){
                    	Log.d(TAG, "onReceive packageName:"+packageName+"  isNeedinstallSTK:"+mLauncher.isNeedinstallSTK);
                    	if(mLauncher.isNeedinstallSTK == true){
                    		mLauncher.isNeedinstallSTK = false;
                    		Intent installSTK = new Intent(InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);
                    		
                    		ComponentName cn = new ComponentName("com.android.borqsstk", "com.android.borqsstk.MainScreen");
                    		PackageManager packageManager = context.getPackageManager();
                    		try {
                    			ActivityInfo info = packageManager.getActivityInfo(cn, 0);
                    			if(info != null){
                    				Intent stkIntent = new Intent(Intent.ACTION_MAIN, null);
                    				stkIntent.setComponent(cn);
                    				installSTK.putExtra(Intent.EXTRA_SHORTCUT_NAME, info.loadLabel(packageManager).toString());
                    				
                    				Bitmap icon = Utilities.createIconBitmap(info.loadIcon(packageManager), context);
                    				installSTK.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
                    				installSTK.putExtra(Intent.EXTRA_SHORTCUT_INTENT,stkIntent);
                    				
                    				Log.d(TAG, "install shortcut: STK");
                    				context.sendBroadcast(installSTK);
                    			}else{
                    				Log.d(TAG, "onReceive stkapp, find info is null!! cn:"+cn);
                    			}
                    		} catch (PackageManager.NameNotFoundException e) {
                    			Log.w(TAG, "Unable to find :" + packageName, e);                                
                    		}
                    	}else{
                    		mLauncher.getWorkspace().refreshSTKShortcuts();
                    	}
                    }
                } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    if (!replacing) {
                        mAllAppsList.removePackage(packageName);
                    }
                    // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                    // later, we will update the package at this time
                } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    if (!replacing) {
                        mAllAppsList.addPackage(context, packageName);
                    } else {
                        mAllAppsList.updatePackage(context, packageName);
                    }
                    
                    if(packageName.equals("com.android.borqsstk")){
                    	if(mLauncher.isNeedinstallSTK == true){
                    		mLauncher.isNeedinstallSTK = false;
                    		Intent installSTK = new Intent(InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);
                    		
                    		ComponentName cn = new ComponentName("com.android.borqsstk", "com.android.borqsstk.MainScreen");
                    		PackageManager packageManager = context.getPackageManager();
                    		try {
                    			ActivityInfo info = packageManager.getActivityInfo(cn, 0);
                    			if(info != null){
                    				Intent stkIntent = new Intent(Intent.ACTION_MAIN, null);
                    				stkIntent.setComponent(cn);
                    				installSTK.putExtra(Intent.EXTRA_SHORTCUT_NAME, info.loadLabel(packageManager).toString());
                    				
                    				Bitmap icon = Utilities.createIconBitmap(info.loadIcon(packageManager), context);
                    				installSTK.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
                    				installSTK.putExtra(Intent.EXTRA_SHORTCUT_INTENT,stkIntent);
                    				
                    				if(Launcher.LOGD)Log.d(TAG, "install shortcut: STK");
                    				context.sendBroadcast(installSTK);
                    			}
                    		} catch (PackageManager.NameNotFoundException e) {
                    			Log.w(TAG, "Unable to find :" + packageName, e);                                
                    		}
                    	}else{
                    		mLauncher.getWorkspace().refreshSTKShortcuts();
                    	}
                    }
                }

                if (mAllAppsList.added.size() > 0) {
                    added = mAllAppsList.added;
                    mAllAppsList.added = new ArrayList<ApplicationInfo>();
                }
                if (mAllAppsList.removed.size() > 0) {
                    removed = mAllAppsList.removed;
                    mAllAppsList.removed = new ArrayList<ApplicationInfo>();
                    for (ApplicationInfo info: removed) {
                        mIconCache.remove(info.intent.getComponent());
                    }
                }
                if (mAllAppsList.modified.size() > 0) {
                    modified = mAllAppsList.modified;
                    mAllAppsList.modified = new ArrayList<ApplicationInfo>();
                }

                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                if (callbacks == null) {
                    Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                    return;
                }

                if (added != null) {
                    final ArrayList<ApplicationInfo> addedFinal = added;
                    mHandler.post(new Runnable() {
                        public void run() {
                            callbacks.bindAppsAdded(addedFinal);
                        }
                    });
                }
                if (modified != null) {
                    final ArrayList<ApplicationInfo> modifiedFinal = modified;
                    mHandler.post(new Runnable() {
                        public void run() {
                        	try{
                        		callbacks.bindAppsUpdated(modifiedFinal);
                        	}catch(Exception ex){
                        		Log.d(TAG, ex.getMessage());
                        	}
                        }
                    });
                }
                if (removed != null) {
                    final ArrayList<ApplicationInfo> removedFinal = removed;
                    mHandler.post(new Runnable() {
                        public void run() {
                        	try{
                        		callbacks.bindAppsRemoved(removedFinal);
                        	}catch(Exception ex){
                        		Log.d(TAG, ex.getMessage());
                        	}
                        }
                    });
                }
            }
            else {
            	if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                     String packages[] = intent.getStringArrayExtra(
                             "android.intent.extra.changed_package_list");
                     if (packages == null || packages.length == 0) {
                         return;
                     }                     
                     synchronized (this) {
                         mAllAppsLoaded = mWorkspaceLoaded = false;
                     }
                     
                     externalSdcardChangedApp = packages.clone();
                     isHaveSdcardMountMessage =true;                     
//                    startLoader(context, false);
                } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                     String packages[] = intent.getStringArrayExtra(
                             "android.intent.extra.changed_package_list");
                     if (packages == null || packages.length == 0) {
                         return;
                     }
                     synchronized (this) {
                         mAllAppsLoaded = mWorkspaceLoaded = false;
                     }
                     
                     externalSdcardChangedApp = packages.clone();
                     isHaveSdcardMountMessage =true;                     
//                     startLoader(context, false);
                }
            }
        }
    }

	public void unSetLoaded()
	{
	     synchronized (this) {
             mAllAppsLoaded = mWorkspaceLoaded = false;
         }
	}
	
    String externalSdcardChangedApp[];
    
    boolean ExistExternalAppAtDesktop(String []extApps, boolean force)
    {
    	if(mLauncher != null && extApps != null)
    	{
    		for(int i=0;i<mLauncher.mDesktopItems.size();i++)
    		{
    		    ItemInfo ii = mLauncher.mDesktopItems.get(i);
    		    if(ii.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET)
    		    {
    		        LauncherAppWidgetInfo appinfo = (LauncherAppWidgetInfo)ii;
    		        for(String pack: extApps)
    		        {
    		            if(pack.equals(appinfo.packageName))
	            		{
    		            	return true;
	            		}
    		        }
    		    }
    		    else if(ii.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || ii.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
    		    {
    		    	ShortcutInfo si = (ShortcutInfo)ii;
    		    	for(String pack: extApps)
    		        {
    		    		final ComponentName cm = si.intent.getComponent();
    		    		if(cm != null)
    		    		{
	    		    		String intentp = cm.getPackageName();
	    		            if(pack.equals(intentp))
		            		{
	    		            	Log.d(TAG, "check sdcard intent="+si.intent);
	    		            	return true;
		            		}
    		    		}
    		        } 
    		    }	
    		}
    		
    		for(int i=0;i<mLauncher.mFolders.size();i++)
 		    {
    			Set<Long> set = mLauncher.mFolders.keySet();
    			Iterator<Long> it= set.iterator();
    			while(it.hasNext())
    			{
    				long key = it.next();
    				FolderInfo fi = mLauncher.mFolders.get(key);
    				
    				if(fi.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER)
    				{
    					UserFolderInfo ufi = (UserFolderInfo)fi;
    					
    					for(ShortcutInfo si :ufi.contents)
    					{
	    					for(String pack: extApps)
	        		        {	
	    						final ComponentName cm = si.intent.getComponent();
	        		    		if(cm != null)
	        		    		{
		        		    		String intentp = cm.getPackageName();
		        		            if(pack.equals(intentp))
		    	            		{
		        		            	Log.d(TAG, "folder has check sdcard intent="+si.intent);
		        		            	return true;
		    	            		}
	        		    		}
	        		        }
    					}
    				}
    			}
 		    	
 		    }
    	}    	
    	else 
    	{
    		if(force)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    	
    }

    public class Loader {
        private int ITEMS_CHUNK;       

        private LoaderThread mLoaderThread;

        final ArrayList<ItemInfo> mItems = new ArrayList<ItemInfo>();
        final ArrayList<LauncherAppWidgetInfo> mAppWidgets = new ArrayList<LauncherAppWidgetInfo>();
        final HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>();		
                
        /**
         * Call this from the ui thread so the handler is initialized on the correct thread.
         */
        public Loader() {
        	ITEMS_CHUNK = 8;
        }

        public void startLoader(Context context, boolean isLaunching, boolean justAllApp) {
            synchronized (mLock) {
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "startLoader isLaunching=" + isLaunching);
                }
                // Don't bother to start the thread if we know it's not going to do anything
                if (mCallbacks != null && mCallbacks.get() != null) {
                    LoaderThread oldThread = mLoaderThread;
                    if (oldThread != null) {
                        if (oldThread.isLaunching()) {
                            // don't downgrade isLaunching if we're already running
                            isLaunching = true;
                        }
                        oldThread.stopLocked();
                    }
                    mLoaderThread = new LoaderThread(context, oldThread, isLaunching);
                    mLoaderThread.justAllApp = justAllApp;
                    mLoaderThread.start();
                }
            }
        }
        
        public void startLoader(Context context, boolean isLaunching) {
            synchronized (mLock) {
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "startLoader isLaunching=" + isLaunching);
                }
                // Don't bother to start the thread if we know it's not going to do anything
                if (mCallbacks != null && mCallbacks.get() != null) {
                    LoaderThread oldThread = mLoaderThread;
                    if (oldThread != null) {
                        if (oldThread.isLaunching()) {
                            // don't downgrade isLaunching if we're already running
                            isLaunching = true;
                        }
                        oldThread.stopLocked();
                    }
                    mLoaderThread = new LoaderThread(context, oldThread, isLaunching);
                    mLoaderThread.start();
                }
            }
        }

        public void stopLoader() {
            synchronized (mLock) {
                if (mLoaderThread != null) {
                    mLoaderThread.stopLocked();
                }
            }
        }

        /**
         * Runnable for the thread that loads the contents of the launcher:
         *   - workspace icons
         *   - widgets
         *   - all apps icons
         */
        private class LoaderThread extends Thread {
            public boolean justAllApp=false;
			private Context mContext;
            private Thread mWaitThread;
            private boolean mIsLaunching;
            private boolean mStopped;
            private boolean mLoadAndBindStepFinished;

            LoaderThread(Context context, Thread waitThread, boolean isLaunching) {
                mContext = context;
                mWaitThread = waitThread;
                mIsLaunching = isLaunching;
            }

            boolean isLaunching() {
                return mIsLaunching;
            }

            /**
             * If another LoaderThread was supplied, we need to wait for that to finish before
             * we start our processing.  This keeps the ordering of the setting and clearing
             * of the dirty flags correct by making sure we don't start processing stuff until
             * they've had a chance to re-set them.  We do this waiting the worker thread, not
             * the ui thread to avoid ANRs.
             */
            private void waitForOtherThread() {
                if (mWaitThread != null) {
                    boolean done = false;
                    while (!done) {
                        try {
                            mWaitThread.join();
                            done = true;
                        } catch (InterruptedException ex) {
                            // Ignore
                        }
                    }
                    mWaitThread = null;
                }
            }

            private void loadAndBindWorkspace() {
            	if(justAllApp == true)
            		return ;
            	
            	if(Launcher.LOGD)Log.d(TAG, "loadAndBindWorkspace");
            	
                // Load the workspace

                // Other other threads can unset mWorkspaceLoaded, so atomically set it,
                // and then if they unset it, or we unset it because of mStopped, it will
                // be unset.
                boolean loaded;
                synchronized (this) {
                    loaded = mWorkspaceLoaded;
                    mWorkspaceLoaded = true;
                }

                // For now, just always reload the workspace.  It's ~100 ms vs. the
                // binding which takes many hundreds of ms.
                // We can reconsider.
                if (DEBUG_LOADERS || Launcher.LOGD) Log.d(TAG, "loadAndBindWorkspace loaded=" + loaded);
                if (true || !loaded) {
                    loadWorkspace();
                    if (mStopped) {
                        mWorkspaceLoaded = false;
                        return;
                    }
                }

                // Bind the workspace
                bindWorkspace();
            }

            private void waitForIdle() {
                // Wait until the either we're stopped or the other threads are done.
                // This way we don't start loading all apps until the workspace has settled
                // down.
                synchronized (LoaderThread.this) {
                    final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                    mHandler.postIdle(new Runnable() {
                            public void run() {
                                synchronized (LoaderThread.this) {
                                    mLoadAndBindStepFinished = true;
                                    if (DEBUG_LOADERS) {
                                        Log.d(TAG, "done with previous binding step");
                                    }
                                    LoaderThread.this.notify();
                                }
                            }
                        });

                    while (!mStopped && !mLoadAndBindStepFinished) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                            // Ignore
                        }
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "waited "
                                + (SystemClock.uptimeMillis()-workspaceWaitTime) 
                                + "ms for previous step to finish binding");
                    }
                }
            }

            public void run() {
                waitForOtherThread();

                // Optimize for end-user experience: if the Launcher is up and // running with the
                // All Apps interface in the foreground, load All Apps first. Otherwise, load the
                // workspace first (default).
                final Callbacks cbk = mCallbacks.get();
                final boolean loadWorkspaceFirst = cbk != null ? (!cbk.isAllAppsVisible()) : true;

                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                //lock for ANR, we don't want to lock other thread
                //synchronized (mLock) 004243
                {
                	long before = System.currentTimeMillis();
                    //android.os.Process.setThreadPriority(mIsLaunching
                    //        ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                    long span = System.currentTimeMillis() - before;
                    
                    //TODO, please close the log later
                    if(DEBUG_LOADERS)
                    Log.d(TAG, "setThreadPriority 1 span="+span);
                }               
                if (PROFILE_LOADERS) {
                    android.os.Debug.startMethodTracing("/sdcard/launcher-loaders");
                }
                
                if (loadWorkspaceFirst) {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 1: loading workspace");
                    loadAndBindWorkspace();
                } else {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 1: special: loading all apps");
                    loadAndBindAllApps();
                }
                
                // Whew! Hard work done.
                synchronized (mLock) {
                    if (mIsLaunching) {
                    	long before = System.currentTimeMillis();
                        //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        long span = System.currentTimeMillis() - before;
                        
                        //TODO, please close the log later
                        if(DEBUG_LOADERS)
                        Log.d(TAG, "setThreadPriority 2 span="+span);
                    }
                }                
                
                // second step
                if (loadWorkspaceFirst) {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 2: loading all apps");
                    loadAndBindAllApps();
                } else {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 2: special: loading workspace");
                    loadAndBindWorkspace();
                }
                
                

                // Clear out this reference, otherwise we end up holding it until all of the
                // callback runnables are done.
                mContext = null;

                synchronized (mLock) {
                    // Setting the reference is atomic, but we can't do it inside the other critical
                    // sections.
                    mLoaderThread = null;
                }

                if (PROFILE_LOADERS) {
                    android.os.Debug.stopMethodTracing();
                }

                // Trigger a gc to try to clean up after the stuff is done, since the
                // renderscript allocations aren't charged to the java heap.
                mHandler.post(new Runnable() {
                        public void run() {
                            System.gc();
                        }
                    });
            }

            public void stopLocked() {
                synchronized (LoaderThread.this) {
                    mStopped = true;
                    this.notify();
                }
            }

            /**
             * Gets the callbacks object.  If we've been stopped, or if the launcher object
             * has somehow been garbage collected, return null instead.
             */
            Callbacks tryGetCallbacks() {
                synchronized (mLock) {
                    if (mStopped) {
                        return null;
                    }

                    final Callbacks callbacks = mCallbacks.get();
                    if (callbacks == null) {
                        Log.w(TAG, "no mCallbacks");
                        return null;
                    }

                    return callbacks;
                }
            }


            /**
             * Gets the callbacks object.  If we've been stopped, or if the launcher object
             * has somehow been garbage collected, return null instead.  Pass in the Callbacks
             * object that was around when the deferred message was scheduled, and if there's
             * a new Callbacks object around then also return null.  This will save us from
             * calling onto it with data that will be ignored.
             */
            Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
                synchronized (mLock) {
                    if (mStopped) {
                        return null;
                    }

                    if (mCallbacks == null) {
                        return null;
                    }

                    final Callbacks callbacks = mCallbacks.get();
                    if (callbacks != oldCallbacks) {
                        return null;
                    }
                    if (callbacks == null) {
                        Log.w(TAG, "no mCallbacks");
                        return null;
                    }

                    return callbacks;
                }
            }

            // check & update map of what's occupied; used to discard overlapping/invalid items
            private long checkItemPlacement(ItemInfo occupied[][][], ItemInfo item) {
                if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    return -1;
                }

                for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                    for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                        if (occupied[item.screen][x][y] != null) {
                            Log.e(TAG, "Error loading shortcut " + item
                                + " into cell (" + item.screen + ":" 
                                + x + "," + y
                                + ") occupied by " 
                                + occupied[item.screen][x][y]);
                            
                            //replace the pre-one
                            final long preid = occupied[item.screen][x][y].id; 
                            occupied[item.screen][x][y] = item;
                            return  preid;                           
                        }
                    }
                }
                for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                    for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                        occupied[item.screen][x][y] = item;
                    }
                }
                return -1;
            }

            private void loadWorkspace() {
                final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                if(Launcher.LOGD){
                	Log.d(TAG, "loadworkspace");
                }
                
                final Context context = mContext;
                final ContentResolver contentResolver = context.getContentResolver();
                final PackageManager manager = context.getPackageManager();
                final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
                final boolean isSafeMode = manager.isSafeMode();
              
//                for(int i = 0; i < mItems.size(); i++){
//                	ItemInfo tmpInfo = mItems.get(i);
//                	if(null != tmpInfo){
//                		if(tmpInfo instanceof ShortcutInfo){
//                			Bitmap icon = ((ShortcutInfo) tmpInfo).mIcon;
//                			if(icon != null){
//                				icon.recycle();
//                				icon = null;
//                			}
//                		}else if(tmpInfo instanceof UserFolderInfo){
//                			ArrayList<ShortcutInfo> infosInFolder = ((UserFolderInfo)tmpInfo).contents;
//                			if(null != infosInFolder && infosInFolder.size()>0){
//                				for(ShortcutInfo tmp:infosInFolder){
//                					Bitmap icon = ((ShortcutInfo) tmp).mIcon;
//                        			if(icon != null){
//                        				icon.recycle();
//                        				icon = null;
//                        			}
//                				}
//                				infosInFolder.clear();
//                			}
//                		}else if(tmpInfo instanceof LiveFolderInfo){
//                			if(null != ((LiveFolderInfo)tmpInfo).icon){
//                				((LiveFolderInfo)tmpInfo).icon.recycle();
//                				((LiveFolderInfo)tmpInfo).icon = null;
//                			}
//                			
//                			if(null != ((LiveFolderInfo)tmpInfo).iconResource){
//                				((LiveFolderInfo)tmpInfo).iconResource = null;
//                			}
//                		} 
//                		
//            			tmpInfo.unbind();
//                	}
//                	
//                	tmpInfo = null;
//                }
//             
//                for(int i=0; i<mAppWidgets.size(); i++){
//                	final LauncherAppWidgetInfo appWidgetInfo = mAppWidgets.get(i);
//                	if(null != appWidgetInfo && null != appWidgetInfo.hostView){
//                		mRsClearHandler.post(new Runnable()
//                		{
//                			public void run()
//                			{
//                				Log.d(TAG,"mRsClearHandler unbind appWidgetInfo");
//                				appWidgetInfo.unbind();
//                			}
//                		});                		                		
//                	}
//                }

                mItems.clear();
                mAppWidgets.clear();
                mFolders.clear();

          //remove all views in WebWidgetRunning list if existed.
                
                final ArrayList<Long> itemsToRemove = new ArrayList<Long>();

                final Cursor c = contentResolver.query(
                        LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);

                final ItemInfo occupied[][][] = new ItemInfo[Launcher.getScreenCount()][Launcher.NUMBER_CELLS_X][Launcher.NUMBER_CELLS_Y];

                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int appWidgetIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_ID);
                    final int screenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SCREEN);
                    final int cellXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLX);
                    final int cellYIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLY);
                    final int spanXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.SPANX);
                    final int spanYIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SPANY);
                    final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
                    final int displayModeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.DISPLAY_MODE);

                    ShortcutInfo info;
                    String intentDescription;
                    LauncherAppWidgetInfo appWidgetInfo;
                    int container;
                    long id;
                    Intent intent;

                    while (!mStopped && c.moveToNext()) {
                        try {
                            int itemType = c.getInt(itemTypeIndex);
                          //  boolean isStkItem = false;
                            switch (itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                                intentDescription = c.getString(intentIndex);
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                } catch (URISyntaxException e) {
                                    continue;
                                }
                                
//                                if(intent!=null && intent.getComponent()!=null && "com.android.borqsstk".equals(intent.getComponent().getPackageName())){
//                                	isStkItem = true;
//                                }
                                
                                if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                                    info = getShortcutInfo(manager, intent, context, c, iconIndex,
                                            titleIndex);
                                } else {
                                    info = getShortcutInfo(c, context, iconTypeIndex,
                                            iconPackageIndex, iconResourceIndex, iconIndex,
                                            titleIndex);
                                }

                                if (info != null) {
                                    updateSavedIcon(context, info, c, iconIndex);

                                    info.intent = intent;
                                    info.id = c.getLong(idIndex);
                                    container = c.getInt(containerIndex);
                                    info.container = container;
                                    info.screen = c.getInt(screenIndex);
                                    info.cellX = c.getInt(cellXIndex);
                                    info.cellY = c.getInt(cellYIndex);

                                    // check & update map of what's occupied
                                    final long pid = checkItemPlacement(occupied, info);
                                    if (-1 != pid) {      
                                    	Log.e(TAG, "Error loading shortcut " + info.id + ", removing it");
                                        contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                    pid, false), null, null);   
                                        
                                        //remove from mItems
                                        for(int i=0;i<mItems.size();i++)
                                        {
                                        	if(mItems.get(i).id == pid)
                                        	{
                                        		mItems.remove(i);
                                        		break;
                                        	}
                                        }
                                    }

                                    switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
//                                    	if(isStkItem){
//                                    		stkList.add(info);
//                                    	}else{
                                    		mItems.add(info);
//                                        }
                                        //Log.d("load item", info.getClass().toString());
                                        break;
                                    default:
                                        // Item is in a user folder
                                        UserFolderInfo folderInfo =  findOrMakeUserFolder(mFolders, container);
//                                        if(isStkItem){
//                                        	stkInFolderList.add(info);
//                                        }else{
                                            folderInfo.add(info);
//                                        }
                                        break;
                                    }
                                } else {
                                    // Failed to load the shortcut, probably because the
                                    // activity manager couldn't resolve it (maybe the app
                                    // was uninstalled), or the db row was somehow screwed up.
                                    // Delete it.
                                    id = c.getLong(idIndex);
                                    Log.e(TAG, "Error loading shortcut " + id + ", removing it");
                                    contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                id, false), null, null);
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                                id = c.getLong(idIndex);
                                UserFolderInfo folderInfo = findOrMakeUserFolder(mFolders, id);

                                folderInfo.title = c.getString(titleIndex);

                                folderInfo.id = id;
                                container = c.getInt(containerIndex);
                                folderInfo.container = container;
                                folderInfo.screen = c.getInt(screenIndex);
                                folderInfo.cellX = c.getInt(cellXIndex);
                                folderInfo.cellY = c.getInt(cellYIndex);

                                // check & update map of what's occupied
                                final long pid = checkItemPlacement(occupied, folderInfo);
                                if (-1 != pid) {                                	
                                	Log.e(TAG, "Error loading shortcut " + id + ", removing it");
                                    contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                pid, false), null, null);
                                    
                                    //remove from mItems
                                    for(int i=0;i<mItems.size();i++)
                                    {
                                    	if(mItems.get(i).id == pid)
                                    	{
                                    		mItems.remove(i);
                                    		break;
                                    	}
                                    }
                                    
                                }

                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                        mItems.add(folderInfo);
                                        break;
                                }

                                mFolders.put(folderInfo.id, folderInfo);
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
                                id = c.getLong(idIndex);
                                Uri uri = Uri.parse(c.getString(uriIndex));

                                // Make sure the live folder exists
                                final ProviderInfo providerInfo =
                                        context.getPackageManager().resolveContentProvider(
                                                uri.getAuthority(), 0);

                                if (providerInfo == null && !isSafeMode) {
                                    itemsToRemove.add(id);
                                } else {
                                    LiveFolderInfo liveFolderInfo = findOrMakeLiveFolder(mFolders, id);
    
                                    intentDescription = c.getString(intentIndex);
                                    intent = null;
                                    if (intentDescription != null) {
                                        try {
                                            intent = Intent.parseUri(intentDescription, 0);
                                        } catch (URISyntaxException e) {
                                            // Ignore, a live folder might not have a base intent
                                        }
                                    }
    
                                    liveFolderInfo.title = c.getString(titleIndex);
                                    liveFolderInfo.id = id;
                                    liveFolderInfo.uri = uri;
                                    container = c.getInt(containerIndex);
                                    liveFolderInfo.container = container;
                                    liveFolderInfo.screen = c.getInt(screenIndex);
                                    liveFolderInfo.cellX = c.getInt(cellXIndex);
                                    liveFolderInfo.cellY = c.getInt(cellYIndex);
                                    liveFolderInfo.baseIntent = intent;
                                    liveFolderInfo.displayMode = c.getInt(displayModeIndex);

                                    // check & update map of what's occupied                                    
                                    final long lpid = checkItemPlacement(occupied, liveFolderInfo);
                                    if (-1 != lpid) {                                	
                                    	Log.e(TAG, "Error loading shortcut " + id + ", removing it");
                                        contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                        		lpid, false), null, null);
                                        
                                        //remove from mItems
                                        for(int i=0;i<mItems.size();i++)
                                        {
                                        	if(mItems.get(i).id == lpid)
                                        	{
                                        		mItems.remove(i);
                                        		break;
                                        	}
                                        }
                                    }

                                    loadLiveFolderIcon(context, c, iconTypeIndex, iconPackageIndex,
                                            iconResourceIndex, liveFolderInfo);
    
                                    switch (container) {
                                        case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                            mItems.add(liveFolderInfo);
                                            break;
                                    }
                                    mFolders.put(liveFolderInfo.id, liveFolderInfo);
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                // Read all Launcher-specific widget details
                                int appWidgetId = c.getInt(appWidgetIdIndex);
                                id = c.getLong(idIndex);

                                /* no check for sdcard application, widget
                                final AppWidgetProviderInfo provider =
                                        widgets.getAppWidgetInfo(appWidgetId);
                                
                                if (appWidgetId < 1000000 && !isSafeMode && 
                                		(provider == null || provider.provider == null ||
                                        provider.provider.getPackageName() == null)) {
                                    Log.e(TAG, "Deleting widget that isn't installed anymore: id="
                                            + id + " appWidgetId=" + appWidgetId);
                                    itemsToRemove.add(id);
                                } else*/ {                                	
                                	if(appWidgetId > 1000000)
                                	{
                                		Log.e(TAG, "for restore process id=" + id + " appWidgetId=" + appWidgetId);                                		 
                                	}
                                	
                                    appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.screen = c.getInt(screenIndex);
                                    appWidgetInfo.cellX = c.getInt(cellXIndex);
                                    appWidgetInfo.cellY = c.getInt(cellYIndex);
                                    appWidgetInfo.spanX = c.getInt(spanXIndex);
                                    appWidgetInfo.spanY = c.getInt(spanYIndex);
                                    appWidgetInfo.packageName = c.getString(intentIndex);
                                    appWidgetInfo.className = c.getString(titleIndex);                                  
                                    container = c.getInt(containerIndex);
                                    if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                        Log.e(TAG, "Widget found where container "
                                                + "!= CONTAINER_DESKTOP -- ignoring!");
                                        continue;
                                    }
                                    appWidgetInfo.container = c.getInt(containerIndex);
    
                                    // check & update map of what's occupied
                                    final long apid = checkItemPlacement(occupied, appWidgetInfo);
                                    if (-1 != apid) {
                                        Log.e(TAG, "already have appwidget " + id + ", removing it="+apid);
                                        contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                     apid, false), null, null);
                                         

                                        //remove from mItems
                                        for(int i=0;i<mAppWidgets.size();i++)
                                        {
                                        	if(mAppWidgets.get(i).id == apid)
                                        	{
                                        		mAppWidgets.remove(i);
                                        		break;
                                        	}
                                        }
                                        break;
                                    }

                                    mAppWidgets.add(appWidgetInfo);
                                }
                                break;
								
							case LauncherSettings.Favorites.ITEM_TYPE_WEBWIDGET:								
								break;
								
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Desktop items loading interrupted:", e);
                        }
                    }
                } finally {
                    c.close();
                }

                if (itemsToRemove.size() > 0) {
                    ContentProviderClient client = contentResolver.acquireContentProviderClient(
                                    LauncherSettings.Favorites.CONTENT_URI);
                    // Remove dead items
                    for (long id : itemsToRemove) {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Removed id = " + id);
                        }
                        // Don't notify content observers
                        try {
                            client.delete(LauncherSettings.Favorites.getContentUri(id, false),
                                    null, null);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not remove id = " + id);
                        }
                    }
                }

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loaded workspace in " + (SystemClock.uptimeMillis()-t) + "ms");
                    Log.d(TAG, "workspace layout: ");
                    for (int y = 0; y < Launcher.NUMBER_CELLS_Y; y++) {
                        String line = "";
                        for (int s = 0; s < Launcher.getScreenCount(); s++) {
                            if (s > 0) {
                                line += " | ";
                            }
                            for (int x = 0; x < Launcher.NUMBER_CELLS_X; x++) {
                                line += ((occupied[s][x][y] != null) ? "#" : ".");
                            }
                        }
                        Log.d(TAG, "[ " + line + " ]");
                    }
                }
            }

            /**
             * Read everything out of our database.
             */
            private void bindWorkspace() {
                final long t = SystemClock.uptimeMillis();

                // Don't use these two variables in any of the callback runnables.
                // Otherwise we hold a reference to them.
                final Callbacks oldCallbacks = mCallbacks.get();
                if (oldCallbacks == null) {
                    // This launcher has exited and nobody bothered to tell us.  Just bail.
                    Log.w(TAG, "LoaderThread running with no launcher");
                    return;
                }

                int N;
                // Tell the workspace that we're about to start firing items at it
                //ticket 1997
                final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                mHandler.post(new Runnable() {
                    public void run() {
                        if (callbacks != null) {
                            callbacks.startBinding();
                        }
                    }
                });
                // Add the items to the workspace.
                N = mItems.size();               
                for (int i=0; i<N; i+=ITEMS_CHUNK) {
                    final int start = i;
                    final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
                    mHandler.post(new Runnable() {
                        public void run() {                        	
                            Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                            if (callbacks != null) {
                            	try{
                            		callbacks.bindItems(mItems, start, start+chunkSize);
                            	}catch(Exception ex){
                            		Log.d(TAG, ex.getMessage(), ex);
                            	}
                            }
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindFolders(mFolders);
                        }
                    }
                });
                // Wait until the queue goes empty.
//                mHandler.post(new Runnable() {
//                    public void run() {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Going to start binding widgets soon.");
                        }
//                    }
//                });
                // Bind the widgets, one at a time.
                // WARNING: this is calling into the workspace from the background thread,
                // but since getCurrentScreen() just returns the int, we should be okay.  This
                // is just a hint for the order, and if it's wrong, we'll be okay.
                // TODO: instead, we should have that push the current screen into here.
                final int currentScreen = oldCallbacks.getCurrentWorkspaceScreen();
                N = mAppWidgets.size();
                // once for the current screen
                for (int i=0; i<N; i++) {
                    final LauncherAppWidgetInfo widget = mAppWidgets.get(i);
                    if (widget.screen == currentScreen) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                                if (callbacks != null) {
                                    callbacks.bindAppWidget(widget);
                                }
                            }
                        });
                    }
                }
                // once for the other screens
                for (int i=0; i<N; i++) {
                    final LauncherAppWidgetInfo widget = mAppWidgets.get(i);
                    if (widget.screen != currentScreen) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                                if (callbacks != null) {
                                    callbacks.bindAppWidget(widget);
                                }
                            }
                        });
                    }
                }
                
                // Tell the workspace that we're done.
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.finishBindingItems();
                        }
                    }
                });
                // If we're profiling, this is the last thing in the queue.
//                mHandler.post(new Runnable() {
//                    public void run() {
                        if (DEBUG_LOADERS) {
                        	long no = SystemClock.uptimeMillis();
                            Log.d(TAG, "bound workspace in "
                                + (no-t) + "ms, now="+no);
                        }
//                    }
//                });
            }

            private void loadAndBindAllApps() {
                // Other other threads can unset mAllAppsLoaded, so atomically set it,
                // and then if they unset it, or we unset it because of mStopped, it will
                // be unset.
                boolean loaded;
                synchronized (this) {
                    loaded = mAllAppsLoaded;
                    mAllAppsLoaded = true;
                }

                if (DEBUG_LOADERS || Launcher.LOGD) Log.d(TAG, "loadAndBindAllApps loaded=" + loaded);
                if (!loaded || mIconCache.getIsAllAppsCacheDirty()) {
                    loadAllAppsByBatch();
                    if (mStopped) {
                        mAllAppsLoaded = false;
                        return;
                    }
                } else {
                    onlyBindAllApps();
                }
            }

            private void onlyBindAllApps() {
            	if(Launcher.LOGD)Log.d(TAG, "onlyBindAllApps");
            	
                final Callbacks oldCallbacks = mCallbacks.get();
                if (oldCallbacks == null) {
                    // This launcher has exited and nobody bothered to tell us.  Just bail.
                    Log.w(TAG, "LoaderThread running with no launcher (onlyBindAllApps)");
                    return;
                }

                // shallow copy
                final ArrayList<ApplicationInfo> list
                        = (ArrayList<ApplicationInfo>)mAllAppsList.data.clone();
                mHandler.post(new Runnable() {
                    public void run() {
                      	final long t = SystemClock.uptimeMillis();
                        final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                        	try{
                        		callbacks.bindAllApplications(list);
                        	}catch(Exception ex){
                        		Log.d(TAG, ex.getMessage(), ex);
                        	}
                        }
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                    + (SystemClock.uptimeMillis()-t) + "ms");
                        }
                    }
                });

            }

            void loadAllAppsByBatch() {
            	if(Launcher.LOGD)Log.d(TAG, "loadAllAppsByBatch");
            	
                final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                
                isLoadOK = false;

                // Don't use these two variables in any of the callback runnables.
                // Otherwise we hold a reference to them.
                final Callbacks oldCallbacks = mCallbacks.get();
                if (oldCallbacks == null) {
                    // This launcher has exited and nobody bothered to tell us.  Just bail.
                    Log.w(TAG, "LoaderThread running with no launcher (loadAllAppsByBatch)");
                    return;
                }

                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                final PackageManager packageManager = mContext.getPackageManager();
                List<ResolveInfo> apps = null;

                int N = Integer.MAX_VALUE;

                int startIndex;
                int i=0;
                int batchSize = -1;
                while (i < N && !mStopped) {
                    synchronized (mAllAppsListLock) {
                        if (i == 0) {
                            // This needs to happen inside the same lock block as when we
                            // prepare the first batch for bindAllApplications.  Otherwise
                            // the package changed receiver can come in and double-add
                            // (or miss one?).
                            mAllAppsList.clear();
                            final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                            apps = packageManager.queryIntentActivities(mainIntent, 0);
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "queryIntentActivities took "
                                        + (SystemClock.uptimeMillis()-qiaTime) + "ms");
                            }
                            if (apps == null) {
                            	callSendWaitedIntents();
                                return;
                            }
                            N = apps.size();
                            if (DEBUG_LOADERS || Launcher.LOGD) {
                                Log.d(TAG, "queryIntentActivities got " + N + " apps");
                            }
                            if (N == 0) {
                                // There are no apps?!?
                            	callSendWaitedIntents();
                                return;
                            }
                            if (mBatchSize == 0) {
                                batchSize = N;
                            } else {
                                batchSize = mBatchSize;
                            }

//                            final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
//                            Collections.sort(apps,new ResolveInfo.DisplayNameComparator(packageManager));
//                            
//                            if (DEBUG_LOADERS) {
//                                Log.d(TAG, "sort took "
//                                        + (SystemClock.uptimeMillis()-sortTime) + "ms");
//                            }
                        }

                        final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                        startIndex = i;
                        for (int j=0; i<N && j<batchSize; j++) {
                            // This builds the icon bitmaps.
                            mAllAppsList.add(new ApplicationInfo(apps.get(i), mIconCache));
                            i++;
                        }

                        final boolean first = i <= batchSize;
                        final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        final ArrayList<ApplicationInfo> added = mAllAppsList.added;
                        mAllAppsList.added = new ArrayList<ApplicationInfo>();

                        //ticket 21839
                        final boolean fromSdcardReload = justAllApp;
                        mHandler.post(new Runnable() {
                            public void run() {
                                final long t = SystemClock.uptimeMillis();
                                if (callbacks != null) {
                                	try{
                                		if (first) {
                                			mBeforeFirstLoad = false;
                                			if(fromSdcardReload)
                                			    callbacks.bindAllApplications(added, fromSdcardReload);
                                			else
                                				callbacks.bindAllApplications(added);
                                		} else {
                                			callbacks.bindAppsAdded(added);
                                		}
                                	}catch(Exception ex){
                                		Log.d(TAG, ex.getMessage(), ex);
                                	}
                                	
                                    if (DEBUG_LOADERS) {
                                        Log.d(TAG, "bound " + added.size() + " apps in "
                                            + (SystemClock.uptimeMillis() - t) + "ms");
                                    }
                                } else {
                                    Log.i(TAG, "not binding apps: no Launcher activity");
                                }
                            }
                        });

                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "batch of " + (i-startIndex) + " icons processed in "
                                    + (SystemClock.uptimeMillis()-t2) + "ms");
                        }
                    }

                    if (mAllAppsLoadDelay > 0 && i < N) {
                        try {
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                            }
                            Thread.sleep(mAllAppsLoadDelay);
                        } catch (InterruptedException exc) {
                        }
                    }
                }
                
                mIconCache.setIsAllAppsCacheDirty(false);
                callSendWaitedIntents();
                
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "cached all " + N + " apps in "
                            + (SystemClock.uptimeMillis()-t) + "ms"
                            + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
                }
            }

            public void dumpState() {
                Log.d(TAG, "mLoader.mLoaderThread.mContext=" + mContext);
                Log.d(TAG, "mLoader.mLoaderThread.mWaitThread=" + mWaitThread);
                Log.d(TAG, "mLoader.mLoaderThread.mIsLaunching=" + mIsLaunching);
                Log.d(TAG, "mLoader.mLoaderThread.mStopped=" + mStopped);
                Log.d(TAG, "mLoader.mLoaderThread.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
            }
            
            public void callSendWaitedIntents(){
            	Log.d(TAG, "callSendWaitedIntents");
            	isLoadOK = true;
             	if(waitedIntents.size()>0){
             		Log.d(TAG, "callSendWaitedIntents  waitedIntents.size():"+waitedIntents.size());
             		final Callbacks oldCallbacks = mCallbacks.get();
             		if (oldCallbacks == null) {
             			Log.w(TAG, "LoaderThread running with no launcher (callSendWaitedIntents)");
             			return;
             		}
             		
             		mHandler.post(new Runnable() {
                        public void run() {
                             final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                             if (callbacks != null) {
                             	try{
                             		callbacks.postWaitedIntents();
                             	}catch(Exception ex){
                             		Log.d(TAG, ex.getMessage(), ex);
                             	}
                             }
                         }
                     });
             	}
            }
        }

        public void dumpState() {
            Log.d(TAG, "mLoader.mItems size=" + mLoader.mItems.size());
            if (mLoaderThread != null) {
                mLoaderThread.dumpState();
            } else {
                Log.d(TAG, "mLoader.mLoaderThread=null");
            }
        }
    }

    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex) {
        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        
        final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

//        if("com.android.calendar".equals(componentName.getPackageName())){
//        	icon = Utilities.createIconBitmap(context.getResources().getDrawable(R.drawable.app_icon_calendar), context);
//        }else{
        	if (resolveInfo != null) {
        		icon = mIconCache.getIcon(componentName, resolveInfo);
        	}
//        }
        
        // the db
        if (icon == null) {
            if (c != null) {
                icon = getIconFromCursor(c, iconIndex);
            }
        }
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        // from the resource
        if (resolveInfo != null) {
            info.title = resolveInfo.activityInfo.loadLabel(manager);
        }
        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title =  c.getString(titleIndex);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {

        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;

        // TODO: If there's an explicit component and we can't install that, delete it.

        info.title = c.getString(titleIndex);
        if(info.title == null || info.title.equals("")){
        	info.title = context.getResources().getString(R.string.application_shortcut_title_unknown);
        }

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            info.customIcon = false;
            // the resource
            try {
                Resources resources = packageManager.getResourcesForApplication(packageName);
                if (resources != null) {
                    final int id = resources.getIdentifier(resourceName, null, null);
                    icon = Utilities.createIconBitmap(resources.getDrawable(id), context);
                }
            } catch (Exception e) {
                // drop this.  we have other places to look for icons
            }
            // the db
            if (icon == null) {
                icon = getIconFromCursor(c, iconIndex);
            }
            // the fallback icon
            if (icon == null) {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
            break;
        case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
            icon = getIconFromCursor(c, iconIndex);
            if (icon == null) {
                icon = getFallbackIcon();
                info.customIcon = false;
                info.usingFallbackIcon = true;
            } else {
                info.customIcon = true;
            }
            break;
        default:
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
            info.customIcon = false;
            break;
        }
        info.setIcon(icon);
        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex) {
        if (false) {
            Log.d(TAG, "getIconFromCursor app="
                    + c.getString(c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE)));
        }
        byte[] data = c.getBlob(iconIndex);
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }

    public ShortcutInfo addShortcut(Context context, Intent data,
            CellLayout.CellInfo cellInfo, boolean notify) {

        final ShortcutInfo info = infoFromShortcutIntent(context, data);
        
        if(info == null){
        	Log.d(TAG, "addShortcut info is null!  data:"+data);
        	return null;
        }
        
        addItemToDatabase(context, info, LauncherSettings.Favorites.CONTAINER_DESKTOP,
        		cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify);

        return info;
    }

    public ShortcutInfo infoFromShortcutIntent(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;
        boolean filtered = false;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
        	BitmapDrawable bitmapDrawable = new BitmapDrawable((Bitmap)bitmap);
        	bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
        	
            icon = Utilities.createIconBitmap(bitmapDrawable, context);
            filtered = true;
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = Utilities.createIconBitmap(resources.getDrawable(id), context);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();
        if(intent.getComponent() != null && "com.android.borqsstk".equals(intent.getComponent().getPackageName())){
        	info.setActivity(intent.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }

        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;

        return info;
    }

    private static void loadLiveFolderIcon(Context context, Cursor c, int iconTypeIndex,
            int iconPackageIndex, int iconResourceIndex, LiveFolderInfo liveFolderInfo) {

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            try {
                Resources resources = packageManager.getResourcesForApplication(packageName);
                final int id = resources.getIdentifier(resourceName, null, null);
                liveFolderInfo.icon = Utilities.createIconBitmap(resources.getDrawable(id),
                        context);
            } catch (Exception e) {
                liveFolderInfo.icon = Utilities.createIconBitmap(
                        context.getResources().getDrawable(R.drawable.ic_launcher_folder),
                        context);
            }
            liveFolderInfo.iconResource = new Intent.ShortcutIconResource();
            liveFolderInfo.iconResource.packageName = packageName;
            liveFolderInfo.iconResource.resourceName = resourceName;
            break;
        default:
            liveFolderInfo.icon = Utilities.createIconBitmap(
                    context.getResources().getDrawable(R.drawable.ic_launcher_folder),
                    context);
        }
    }

    void updateSavedIcon(Context context, ShortcutInfo info, Cursor c, int iconIndex) {
        // If this icon doesn't have a custom icon, check to see
        // what's stored in the DB, and if it doesn't match what
        // we're going to show, store what we are going to show back
        // into the DB.  We do this so when we're loading, if the
        // package manager can't find an icon (for example because
        // the app is on SD) then we can use that instead.
        if (info.onExternalStorage && !info.customIcon && !info.usingFallbackIcon) {
            boolean needSave;
            byte[] data = c.getBlob(iconIndex);
            try {
                if (data != null) {
                    Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap loaded = info.getIcon(mIconCache);
                    needSave = false;//!saved.sameAs(loaded);
                } else {
                    needSave = true;
                }
            } catch (Exception e) {
                needSave = true;
            }
            if (needSave) {
                Log.d(TAG, "going to save icon bitmap for info=" + info);
                // This is slower than is ideal, but this only happens either
                // after the froyo OTA or when the app is updated with a new
                // icon.
                updateItemInDatabase(context, info);
            }
        }
    }

    /**
     * Return an existing UserFolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
    static UserFolderInfo findOrMakeUserFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null || !(folderInfo instanceof UserFolderInfo)) {
            // No placeholder -- create a new instance
            folderInfo = new UserFolderInfo();
            folders.put(id, folderInfo);
        }
        return (UserFolderInfo) folderInfo;
    }

    /**
     * Return an existing UserFolderInfo object if we have encountered this ID previously, or make a
     * new one.
     */
    private static LiveFolderInfo findOrMakeLiveFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null || !(folderInfo instanceof LiveFolderInfo)) {
            // No placeholder -- create a new instance
            folderInfo = new LiveFolderInfo();
            folders.put(id, folderInfo);
        }
        return (LiveFolderInfo) folderInfo;
    }

    private static String getLabel(PackageManager manager, ActivityInfo activityInfo) {
        String label = activityInfo.loadLabel(manager).toString();
        if (label == null) {
            label = manager.getApplicationLabel(activityInfo.applicationInfo).toString();
            if (label == null) {
                label = activityInfo.name;
            }
        }
        return label;
    }

    private static final Collator sCollator = Collator.getInstance();
    public static final Comparator<ApplicationInfo> APP_NAME_COMPARATOR
            = new Comparator<ApplicationInfo>() {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            return sCollator.compare(a.title.toString(), b.title.toString());
        }
    };
    
    public void dumpState() {
        Log.d(TAG, "mBeforeFirstLoad=" + mBeforeFirstLoad);
        Log.d(TAG, "mCallbacks=" + mCallbacks);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mAllAppsList.data);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mAllAppsList.added);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mAllAppsList.removed);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mAllAppsList.modified);
        mLoader.dumpState();
    }
    
}
