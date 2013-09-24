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

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.TypedArray;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;
import android.util.AttributeSet;
import android.net.Uri;
import android.text.TextUtils;
import android.provider.Settings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;

import com.android.internal.util.XmlUtils;
import com.borqs.omshome25.LauncherORM.CategoryCol;
import com.borqs.omshome25.LauncherSettings.Favorites;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "Launcher.LauncherProvider";
    private static final boolean LOGD = false;

    private static final String DATABASE_NAME = "launcher.db";
    
    private static final int DATABASE_VERSION = 9;

    static final String AUTHORITY = "com.borqs.omshome25.settings";
    
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_SETTINGS = "settings";
    public static final String PARAMETER_NOTIFY    = "notify";    
    public static final String TABLE_CATEGORY      = "category";
    public static final String TABLE_CATEGORY_name = "categoryname";
    
    public final static int ID_Entertainment = 1;
    public final static int ID_Information   = 2;
    public final static int ID_Tools         = 3;
    public final static int ID_SHORT         = 4;
    public final static int ID_Game          = 5;
    

    /**
     * {@link Uri} triggered at any registered {@link android.database.ContentObserver} when
     * {@link AppWidgetHost#deleteHost()} is called during database creation.
     * Use this to recall {@link AppWidgetHost#startListening()} if needed.
     */
    static final Uri CONTENT_APPWIDGET_RESET_URI =
            Uri.parse("content://" + AUTHORITY + "/appWidgetReset");
    
    public static SQLiteOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
	Cursor result = null;
    	try{
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);
	}catch(Exception ex){}
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);
        long rowId = 0;
        try{
        	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        	rowId = db.insert(args.table, null, initialValues);
        	if (rowId <= 0) return null;
        }catch(Exception ex){}

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(args.table, null, values[i]) < 0) return 0;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        int count = 0;
        try{
        	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        	count = db.delete(args.table, args.where, args.args);
//        	if(Launcher.LOGD)Log.d(TAG, "delete table:"+args.table+" where:"+args.where+" args:"+args.args+" count:"+count);
        }catch(Exception ex){}

        if (count > 0) sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        int count = 0;
        
        try{
        	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        	count = db.update(args.table, values, args.where, args.args);
        }catch(Exception ex){
        	Log.d(TAG, "update error:"+ ex.getMessage());
        }
      
        if (count > 0) sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_CLOCK = "clock";
        private static final String TAG_SEARCH = "search";
        private static final String TAG_APPWIDGET = "appwidget";
        private static final String TAG_SHORTCUT  = "shortcut";
        private static final String TAG_CATEGORYS  = "categorys";
        private static final String TAG_CATEGORY  = "category";
        
        private final Context mContext;
        private final AppWidgetHost mAppWidgetHost;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            mAppWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
        }

        /**
         * Send notification that we've deleted the {@link AppWidgetHost},
         * probably as part of the initial database creation. The receiver may
         * want to re-call {@link AppWidgetHost#startListening()} to ensure
         * callbacks are correctly set.
         */
        private void sendAppWidgetResetNotify() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (Launcher.LOGD)Log.d(TAG, "creating new launcher database");
            
            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "itemType INTEGER," +
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +
                    "isShortcut INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "displayMode INTEGER" +
                    ");");

            
            db.execSQL("CREATE TABLE settings (" +
                    "_id INTEGER PRIMARY KEY," +
                    "name  TEXT," +
                    "value TEXT" +
                    ");");
            
            db.execSQL("INSERT INTO settings (name, value) VALUES(\""+LauncherORM.default_page_index+"\", \"1\");");
            db.execSQL("INSERT INTO settings (name, value) VALUES(\""+LauncherORM.delete_default_page+"\", \"1\");");
            db.execSQL("INSERT INTO settings (name, value) VALUES(\""+LauncherORM.dock_style+"\", \"0\");");
            db.execSQL("INSERT INTO settings (name, value) VALUES(\""+LauncherORM.allapps_view_style+"\", \"0\");");
            db.execSQL("INSERT INTO settings (name, value) VALUES(\""+LauncherORM.pagemanager_view_style+"\", \"0\");");
            
            db.execSQL("CREATE TABLE "+TABLE_CATEGORY+" (" +
                    "_id     INTEGER PRIMARY KEY," +
                    "c_id    INTEGER ," +
                    "intent  TEXT);");
            
            db.execSQL("CREATE TABLE "+TABLE_CATEGORY_name+" (" +
                    "_id    INTEGER PRIMARY KEY," +
                    "c_id    INTEGER ," +
                    "cname  TEXT);");
            
            db.execSQL("INSERT INTO categoryname (c_id, cname) VALUES(1, \""+ mContext.getString(R.string.app_entertainment) +"\");");
            db.execSQL("INSERT INTO categoryname (c_id, cname) VALUES(2, \""+ mContext.getString(R.string.app_information) +"\");");
            db.execSQL("INSERT INTO categoryname (c_id, cname) VALUES(3, \""+ mContext.getString(R.string.app_tools) +"\");");
            db.execSQL("INSERT INTO categoryname (c_id, cname) VALUES(4, \""+ mContext.getString(R.string.app_shortcut) +"\");");
            db.execSQL("INSERT INTO categoryname (c_id, cname) VALUES(5, \""+ mContext.getString(R.string.app_game) +"\");");
            
            // Database was just created, so wipe any previous widgets
            if (mAppWidgetHost != null) {
                mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }
            
            if (!convertDatabase(db)) {
                // Populate favorites table with initial favorites
                loadFavorites(db);
                
                loadCategorys(db);
            }
        }

        private boolean convertDatabase(SQLiteDatabase db) {
            if (Launcher.LOGD)Log.d(TAG, "converting database from an older format, but not onUpgrade");
            boolean converted = false;

            final Uri uri = Uri.parse("content://" + Settings.AUTHORITY +
                    "/old_favorites?notify=true");
            final ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;

            try {
                cursor = resolver.query(uri, null, null, null, null);
            } catch (Exception e) {
	            // Ignore
            }

            // We already have a favorites database in the old provider
            if (cursor != null && cursor.getCount() > 0) {
                try {
                    converted = copyFromCursor(db, cursor) > 0;
                } catch(Exception ex){
                } finally {
                    cursor.close();
                }

                if (converted) {
                	try{
                		resolver.delete(uri, null, null);
                	}catch(Exception ex){
                	}
                }
            }
            
            if (converted) {
                // Convert widgets from this import into widgets
                if (Launcher.LOGD)Log.d(TAG, "converted and now triggering widget upgrade");
                convertWidgets(db);
            }

            return converted;
        }

        private int copyFromCursor(SQLiteDatabase db, Cursor c) {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
            final int displayModeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.DISPLAY_MODE);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(LauncherSettings.Favorites._ID, c.getLong(idIndex));
                values.put(LauncherSettings.Favorites.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.Favorites.TITLE, c.getString(titleIndex));
                values.put(LauncherSettings.Favorites.ICON_TYPE, c.getInt(iconTypeIndex));
                values.put(LauncherSettings.Favorites.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put(LauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex));
                values.put(LauncherSettings.Favorites.ITEM_TYPE, c.getInt(itemTypeIndex));
                values.put(LauncherSettings.Favorites.APPWIDGET_ID, -1);
                values.put(LauncherSettings.Favorites.SCREEN, c.getInt(screenIndex));
                values.put(LauncherSettings.Favorites.CELLX, c.getInt(cellXIndex));
                values.put(LauncherSettings.Favorites.CELLY, c.getInt(cellYIndex));
                values.put(LauncherSettings.Favorites.URI, c.getString(uriIndex));
                values.put(LauncherSettings.Favorites.DISPLAY_MODE, c.getInt(displayModeIndex));
                rows[i++] = values;
            }

            db.beginTransaction();
            int total = 0;
            try {
                int numValues = rows.length;
                for (i = 0; i < numValues; i++) {
                    if (db.insert(TABLE_FAVORITES, null, rows[i]) < 0) {
                        return 0;
                    } else {
                        total++;
                    }
                }
                db.setTransactionSuccessful();
            } catch(Exception ex){
            	//TODO
            }finally {
                db.endTransaction();
            }

            return total;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (Launcher.LOGD)Log.d(TAG, "onUpgrade triggered");
            
            int version = oldVersion;
            if (version < 3) {
                // upgrade 1,2 -> 3 added appWidgetId column
                db.beginTransaction();
                try {
                    // Insert new column for holding appWidgetIds
                    db.execSQL("ALTER TABLE favorites " +
                        "ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    db.setTransactionSuccessful();
                    version = 3;
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
                
                // Convert existing widgets only if table upgrade was successful
                if (version == 3) {
                    convertWidgets(db);
                }
            }

            if (version < 4) {
                version = 4;
            }
            
            // Where's version 5?
            // - Donut and sholes on 2.0 shipped with version 4 of launcher1.
            // - Passion shipped on 2.1 with version 6 of omshome
            // - Sholes shipped on 2.1r1 (aka Mr. 3) with version 5 of launcher 1
            //   but version 5 on there was the updateContactsShortcuts change
            //   which was version 6 in launcher 2 (first shipped on passion 2.1r1).
            // The updateContactsShortcuts change is idempotent, so running it twice
            // is okay so we'll do that when upgrading the devices that shipped with it.
            if (version < 6) {
                // We went from 3 to 5 screens. Move everything 1 to the right
                db.beginTransaction();
                try {
                    db.execSQL("UPDATE favorites SET screen=(screen + 1);");
                    db.setTransactionSuccessful();
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
            
               // We added the fast track.
                if (updateContactsShortcuts(db)) {
                    version = 6;
                }
            }

            if (version < 7) {
                // Version 7 gets rid of the special search widget.
                convertWidgets(db);
                version = 7;
            }

            if (version < 8) {
                // Version 8 (froyo) has the icons all normalized.  This should
                // already be the case in practice, but we now rely on it and don't
                // resample the images each time.
                normalizeIcons(db);
                version = 8;
            }

            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_name);
                onCreate(db);
            }
        }

        private boolean updateContactsShortcuts(SQLiteDatabase db) {
            Cursor c = null;
            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE,
                    new int[] { Favorites.ITEM_TYPE_SHORTCUT });

            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID, Favorites.INTENT },
                        selectWhere, null, null, null, null);
                
                if (Launcher.LOGD)Log.d(TAG, "found upgrade cursor count=" + c.getCount());
                
                final ContentValues values = new ContentValues();
                final int idIndex = c.getColumnIndex(Favorites._ID);
                final int intentIndex = c.getColumnIndex(Favorites.INTENT);
                
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(idIndex);
                    final String intentUri = c.getString(intentIndex);
                    if (intentUri != null) {
                        try {
                            Intent intent = Intent.parseUri(intentUri, 0);
                            android.util.Log.d("Home", intent.toString());
                            final Uri uri = intent.getData();
                            final String data = uri.toString();
                            if (Intent.ACTION_VIEW.equals(intent.getAction()) &&
                                    (data.startsWith("content://contacts/people/") ||
                                    data.startsWith("content://com.android.contacts/contacts/lookup/"))) {

                                intent = new Intent("com.android.contacts.action.QUICK_CONTACT");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                                intent.setData(uri);
                                intent.putExtra("mode", 3);
                                intent.putExtra("exclude_mimes", (String[]) null);

                                values.clear();
                                values.put(LauncherSettings.Favorites.INTENT, intent.toUri(0));
    
                                String updateWhere = Favorites._ID + "=" + favoriteId;
                                db.update(TABLE_FAVORITES, values, updateWhere, null);                                
                            }
                        } catch (RuntimeException ex) {
                            Log.e(TAG, "Problem upgrading shortcut", ex);
                        } catch (URISyntaxException e) {
                            Log.e(TAG, "Problem upgrading shortcut", e);                            
                        }
                    }
                }
                
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while upgrading contacts", ex);
                return false;
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }

            return true;
        }

        private void normalizeIcons(SQLiteDatabase db) {
            Log.d(TAG, "normalizing icons");

            db.beginTransaction();
            Cursor c = null;
            SQLiteStatement update = null;
            try {
                boolean logged = false;
                update = db.compileStatement("UPDATE favorites "
                        + "SET icon=? WHERE _id=?");

                c = db.rawQuery("SELECT _id, icon FROM favorites WHERE iconType=" +
                        Favorites.ICON_TYPE_BITMAP, null);

                final int idIndex = c.getColumnIndexOrThrow(Favorites._ID);
                final int iconIndex = c.getColumnIndexOrThrow(Favorites.ICON);

                while (c.moveToNext()) {
                    long id = c.getLong(idIndex);
                    byte[] data = c.getBlob(iconIndex);
                    try {
                        Bitmap bitmap = Utilities.resampleIconBitmap(
                                BitmapFactory.decodeByteArray(data, 0, data.length),
                                mContext);
                        if (bitmap != null) {
                            update.bindLong(1, id);
                            data = ItemInfo.flattenBitmap(bitmap);
                            if (data != null) {
                                update.bindBlob(2, data);
                                update.execute();
                            }
                            bitmap.recycle();
                        }
                    } catch (Exception e) {
                        if (!logged) {
                            Log.e(TAG, "Failed normalizing icon " + id, e);
                        } else {
                            Log.e(TAG, "Also failed normalizing icon " + id);
                        }
                        logged = true;
                    }
                }
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (update != null) {
                    update.close();
                }
                if (c != null) {
                    c.close();
                }
            }
            
        }

        /**
         * Upgrade existing clock and photo frame widgets into their new widget
         * equivalents.
         */
        private void convertWidgets(SQLiteDatabase db) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            final int[] bindSources = new int[] {
                    Favorites.ITEM_TYPE_WIDGET_CLOCK,
                    Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
                    Favorites.ITEM_TYPE_WIDGET_SEARCH,
            };

            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE, bindSources);
            
            Cursor c = null;
            
            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID, Favorites.ITEM_TYPE },
                        selectWhere, null, null, null, null);
                
                if (LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());
                
                final ContentValues values = new ContentValues();
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(0);
                    int favoriteType = c.getInt(1);

                    // Allocate and update database with new appWidgetId
                    try {
                        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
                        
                        if (LOGD) {
                            Log.d(TAG, "allocated appWidgetId=" + appWidgetId
                                    + " for favoriteId=" + favoriteId);
                        }
                        values.clear();
                        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                        values.put(Favorites.APPWIDGET_ID, appWidgetId);

                        // Original widgets might not have valid spans when upgrading
                        if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
                            values.put(LauncherSettings.Favorites.SPANX, 4);
                            values.put(LauncherSettings.Favorites.SPANY, 1);
                        } else {
                            values.put(LauncherSettings.Favorites.SPANX, 2);
                            values.put(LauncherSettings.Favorites.SPANY, 2);
                        }

                        String updateWhere = Favorites._ID + "=" + favoriteId;
                        db.update(TABLE_FAVORITES, values, updateWhere, null);

                        if (favoriteType == Favorites.ITEM_TYPE_WIDGET_CLOCK) {
                            appWidgetManager.bindAppWidgetId(appWidgetId,
                                    new ComponentName("com.android.alarmclock",
                                    "com.android.alarmclock.AnalogAppWidgetProvider"));
                        } else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME) {
                            appWidgetManager.bindAppWidgetId(appWidgetId,
                                    new ComponentName("com.android.camera",
                                    "com.android.camera.PhotoAppWidgetProvider"));
                        } else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
                            appWidgetManager.bindAppWidgetId(appWidgetId,
                                    getSearchWidgetProvider());
                        }
                    } catch (RuntimeException ex) {
                        Log.e(TAG, "Problem allocating appWidgetId", ex);
                    }
                }
                
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }
        }
        private int getScreenCount() {            
            int counts = 0;

            try {
                counts = Launcher.getSettingsIntValue(mContext, Workspace.TAG_SCREEN_NUM);
            } catch (Exception e) {}
            
            return counts;
        }

        /*
         * 
         */
        private int loadCategorys(SQLiteDatabase db) {
            if(Launcher.LOGD)Log.d(TAG,"loadCategorys");
            
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);            

//            PackageManager packageManager = mContext.getPackageManager();
            int i = 0;            
            try {
            	XmlPullParser parser = null;
            	//get from opl firstly, them get from resource xml
            	if(new File("/opl/etc/new_omshome_cates.xml").exists() == true) 
            	{
            		Log.d(TAG, "load favorites from /opl/etc/new_omshome_cates.xml");            		
                	FileReader reader = new FileReader(new File("/opl/etc/new_omshome_cates.xml"));
                	parser = Xml.newPullParser();
                    parser.setInput(reader);
                    
                    String xml_ns = "";
                    XmlUtils.beginDocument(parser, TAG_CATEGORYS);
                    
                    final int depth = parser.getDepth();       
                    int type;
                    final ContentValues values = new ContentValues();
                    while (((type = parser.next()) != XmlPullParser.END_TAG ||
                            parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                        if (type != XmlPullParser.START_TAG) {
                            continue;
                        }
                        
                        if(TAG_CATEGORY.equals(parser.getName()))
                		{	
                        	ItemData item = getItem(parser);
                        	int cateid = Integer.valueOf(item.cateid);
                        	
                        	if(cateid >= Category.CATEGORY_CUSTOMIZE_MAX) {
                        	    if(Launcher.LOGD)Log.d(TAG, "cateid:"+item.cateid+" is too big, ignore");
                        	    continue;
                        	}
                        	
                        	try {
                                ComponentName cn = new ComponentName(item.packageName, item.className);                                
                                intent.setComponent(cn);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                          | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                
                                
                                String sintent = intent.getComponent().getPackageName()+"/"+intent.getComponent().getClassName();
//                                LauncherORM.instance(mContext).AddCategoryItem(cateid, sintent);    
                                values.clear();
                            	values.put(CategoryCol.CID,    cateid);
                            	values.put(CategoryCol.Intent, sintent);
                                db.insert(TABLE_CATEGORY, null, values);
                               
                            } catch (Exception e) {
                                  Log.w(TAG, "Unable to add cate: " + item.packageName +
                                          "/" + item.className, e);                                
                            }                              
                		}                        
                    }
            	}    
            	else
            	{
            		 //just fist time do this
            		 //if(Launcher.isNotOPhone() == false)
            	     {
            		     parser = mContext.getResources().getXml(R.xml.default_ophone_category);
            		     Log.d(TAG, "load category from R.xml.default_ophone_category");
            	     }
            	     /*
            		 else
            	     {
            	         parser = mContext.getResources().getXml(R.xml.default_category);
            	         Log.d(TAG, "load category from R.xml.default_category");
            	     }*/
            		
                     AttributeSet attrs = Xml.asAttributeSet(parser);
                     XmlUtils.beginDocument(parser, TAG_CATEGORYS);

                     final int depth = parser.getDepth();
                     final ContentValues values = new ContentValues();
                     int type;
                     while (((type = parser.next()) != XmlPullParser.END_TAG ||
                             parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                         if (type != XmlPullParser.START_TAG) {
                             continue;
                         }

                         boolean added = false;
                         final String name = parser.getName();

                         TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
                         
                         String packageName = a.getString(R.styleable.Favorite_packageName);
                         String className   = a.getString(R.styleable.Favorite_className);
                         int cateid      = Integer.parseInt(a.getString(R.styleable.Favorite_cateid));    
                         
                         try{
	                         ComponentName cn = new ComponentName(packageName, className);                                
	                         intent.setComponent(cn);
	                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
	                                   | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);                         
	                         
	                         String sintent = intent.getComponent().getPackageName()+"/"+intent.getComponent().getClassName();
                             values.clear();
                             values.put(CategoryCol.CID,    cateid);
                          	 values.put(CategoryCol.Intent, sintent);
                             db.insert(TABLE_CATEGORY, null, values);
                         }catch(Exception ne)
                         {
                        	 Log.d(TAG, ne.getMessage());
                         }
                        
                         if(Launcher.LOGD)Log.d(TAG,"loadCategory i:"+i+" className:"+ a.getString(R.styleable.Favorite_className));
                         a.recycle();
                     }
            	}
               
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch(Exception e){
            	 Log.w(TAG, "Got exception parsing favorites.", e);
            }

            return i;
        }
        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         */
        private int loadFavorites(SQLiteDatabase db) {
            if(Launcher.LOGD)Log.d(TAG,"loadFavorites");
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ContentValues values = new ContentValues();

            PackageManager packageManager = mContext.getPackageManager();
            int i = 0;
            final int screencount = getScreenCount();
            try {
            	XmlPullParser parser = null;
            	//get from opl firstly, them get from resource xml
            	if(new File("/opl/etc/new_omshome_favorites.xml").exists() == true) 
            	{
            		Log.d(TAG, "load favorites from /opl/etc/new_omshome_favorites.xml");            		
                	FileReader reader = new FileReader(new File("/opl/etc/new_omshome_favorites.xml"));
                	parser = Xml.newPullParser();
                    parser.setInput(reader);
                    
                    String xml_ns = "";
                    XmlUtils.beginDocument(parser, TAG_FAVORITES);
                    
                    final int depth = parser.getDepth();       
                    int type;
                    while (((type = parser.next()) != XmlPullParser.END_TAG ||
                            parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                        if (type != XmlPullParser.START_TAG) {
                            continue;
                        }
                        
                    	values.clear();                    
                        values.put(LauncherSettings.Favorites.CONTAINER, LauncherSettings.Favorites.CONTAINER_DESKTOP);
                         
                        if(TAG_FAVORITE.equals(parser.getName()))
                		{	
                        	ItemData item = getItem(parser);
                        	
                        	if(screencount > 0 && Integer.valueOf(item.screen) >= screencount) {
                        	    if(Launcher.LOGD)Log.d(TAG, "screen:"+item.screen+" is too big, ignore. screen count:"+screencount);
                        	    continue;
                        	}
                        	
                        	values.put(LauncherSettings.Favorites.SCREEN,    item.screen);
                    		values.put(LauncherSettings.Favorites.CELLX,     item.x);
                    		values.put(LauncherSettings.Favorites.CELLY,     item.y);
                    		
                        	ActivityInfo info;                            
                            try {
                                ComponentName cn = new ComponentName(item.packageName, item.className);
                                info = packageManager.getActivityInfo(cn, 0);
                                intent.setComponent(cn);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                          | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                values.put(Favorites.INTENT,    intent.toUri(0));
                                values.put(Favorites.TITLE,     info.loadLabel(packageManager).toString());
                                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                                values.put(Favorites.SPANX, 1);
                                values.put(Favorites.SPANY, 1);
                                db.insert(TABLE_FAVORITES, null, values);
                            } catch (PackageManager.NameNotFoundException e) {
                                  Log.w(TAG, "Unable to add favorite: " + item.packageName +
                                          "/" + item.className, e);                                
                            }                              
                		} 
                        /*else if(TAG_SEARCH.equals(parser.getName()))
                        {
                        	ItemData item = getItem(parser);
                        	
                        	values.put(LauncherSettings.Favorites.SCREEN,    item.screen);
                    		values.put(LauncherSettings.Favorites.CELLX,     item.x);
                    		values.put(LauncherSettings.Favorites.CELLY,     item.y);
                    		 
                        	values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_WIDGET_SEARCH);
                            values.put(Favorites.SPANX, 4);
                            values.put(Favorites.SPANY, 1);
                            
                            db.insert(TABLE_FAVORITES, null, values);
                        }*/
                        else if(TAG_CLOCK.equals(parser.getName()))
                        {
                        	addClockWidget(db, values);
                        }
                        else if(TAG_APPWIDGET.equals(parser.getName()))
                        {
                        	 ItemData item = getItem(parser);
                        	
                             if (item.packageName == null || item.className == null) {
                                 continue;
                             }
                             
                             if(screencount > 0 && Integer.valueOf(item.screen) >= screencount) {
                                 if(Launcher.LOGD)Log.d(TAG, "screen:"+item.screen+" is too big, ignore. screen count:"+screencount);
                                 continue;
                             }
                             
                             values.put(LauncherSettings.Favorites.SCREEN,    item.screen);
                    		 values.put(LauncherSettings.Favorites.CELLX,     item.x);
                    		 values.put(LauncherSettings.Favorites.CELLY,     item.y);
                             ComponentName cn = new ComponentName(item.packageName, item.className);
                             
                             boolean allocatedAppWidgets = false;
                             final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

                             int appWidgetId = -1;
                             try {
                                 appWidgetId = mAppWidgetHost.allocateAppWidgetId();
                                 
                                 values.put(Favorites.INTENT,    item.packageName);
                                 values.put(Favorites.TITLE,     item.className);
                                 
                                 values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                                 values.put(Favorites.SPANX,     item.spanX);
                                 values.put(Favorites.SPANY,     item.spanY);
                                 values.put(Favorites.APPWIDGET_ID, appWidgetId);
                                 db.insert(TABLE_FAVORITES, null, values);

                                 if(Launcher.LOGD)Log.d(TAG,"addAppWidget id:"+appWidgetId+" cn:"+cn);
                                 allocatedAppWidgets = true;                                 
                                 appWidgetManager.bindAppWidgetId(appWidgetId, cn);
                                 
                             } catch (RuntimeException ex) {
                                 Log.e(TAG, "Problem allocating ComponentName:"+cn+" ex:"+ex.getMessage());
                                 if(appWidgetId != -1)
                                 {
                                	 mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                                 }
                             } catch(Exception ex) {
                            	 Log.e(TAG, "Exception Problem allocating ComponentName:"+cn+" ex:"+ex.getMessage());
                                
                             }                       
                        }
                        else if(TAG_SHORTCUT.equals(parser.getName()))
                        {
                        	 Resources r = mContext.getResources();      
                        	 int iconResId  = 0;
                        	 int titleResId = 0;
                        	 try
                        	 {
                        		 ItemData item = getItem(parser);
                        		 
                        		 if(screencount > 0 && Integer.valueOf(item.screen) >= screencount) {
                        		     if(Launcher.LOGD)Log.d(TAG, "screen:"+item.screen+" is too big, ignore. screen count:"+screencount);
                                     continue;
                                 }
                        		 
                             	 values.put(LauncherSettings.Favorites.SCREEN,    item.screen);
                         		 values.put(LauncherSettings.Favorites.CELLX,     item.x);
                         		 values.put(LauncherSettings.Favorites.CELLY,     item.y);
	
	                             Intent sin;
	                             String uri = null;
	                             try {
	                                 uri = parser.getAttributeValue("android", "uri");
	                                 sin = Intent.parseUri(uri, 0);
	                             } catch (URISyntaxException e) {
	                                 Log.w(TAG, "Shortcut has malformed uri: " + uri);
	                                 continue;
	                             }
	
	                             if (iconResId == 0 || titleResId == 0) {
	                                 Log.w(TAG, "Shortcut is missing title or icon resource ID");
	                                 continue;
	                             }
	
	                             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                             values.put(Favorites.INTENT, sin.toUri(0));
	                             values.put(Favorites.TITLE, r.getString(titleResId));
	                             values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
	                             values.put(Favorites.SPANX, 1);
	                             values.put(Favorites.SPANY, 1);
	                             values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
	                             values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
	                             values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));
	
	                             db.insert(TABLE_FAVORITES, null, values);
                        	 }catch(Exception ne) 
                        	 {}
                        }
                    }
            	}
            	else
            	{
            		Log.d(TAG, "load favorites from R.xml.default_workspace");
            		parser = mContext.getResources().getXml(R.xml.default_workspace);
            		
            		 AttributeSet attrs = Xml.asAttributeSet(parser);
                     XmlUtils.beginDocument(parser, TAG_FAVORITES);

                     final int depth = parser.getDepth();

                     int type;
                     while (((type = parser.next()) != XmlPullParser.END_TAG ||
                             parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                         if (type != XmlPullParser.START_TAG) {
                             continue;
                         }

                         boolean added = false;
                         final String name = parser.getName();

                         TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);

                         if(screencount > 0 && Integer.valueOf(a.getString(R.styleable.Favorite_screen)) >= screencount) {
                             if(Launcher.LOGD)Log.d(TAG, "screen:"+a.getString(R.styleable.Favorite_screen)+" is too big, ignore. screen count:"+screencount);
                             continue;
                         }
                         
                         values.clear();                    
                         values.put(LauncherSettings.Favorites.CONTAINER,
                                 LauncherSettings.Favorites.CONTAINER_DESKTOP);
                         values.put(LauncherSettings.Favorites.SCREEN,
                                 a.getString(R.styleable.Favorite_screen));
                         values.put(LauncherSettings.Favorites.CELLX,
                                 a.getString(R.styleable.Favorite_x));
                         values.put(LauncherSettings.Favorites.CELLY,
                                 a.getString(R.styleable.Favorite_y));

                         if (TAG_FAVORITE.equals(name)) {
                             added = addAppShortcut(db, values, a, packageManager, intent);
                         } /*else if (TAG_SEARCH.equals(name)) {
                             added = addSearchWidget(db, values);
                         } */else if (TAG_CLOCK.equals(name)) {
                             added = addClockWidget(db, values);
                         } else if (TAG_APPWIDGET.equals(name)) {
                             added = addAppWidget(db, values, a,packageManager);
                         } else if (TAG_SHORTCUT.equals(name)) {
                             added = addUriShortcut(db, values, a);
                         }

                         if (added) i++;
                         if(Launcher.LOGD)Log.d(TAG,"loadFavorites i:"+i+" className:"+ a.getString(R.styleable.Favorite_className));
                         a.recycle();
                     }
            	}            	
               
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch(Exception e){
            	 Log.w(TAG, "Got exception parsing favorites.", e);
            }

            return i;
        }

        private static class ItemData
        {
        	String packageName=null;
         	String className=null;
         	String spanX=null;
         	String spanY=null;
         	String x=null;
         	String y=null;
         	String screen=null;
         	String cateid=null;
        }
        
        private ItemData getItem(XmlPullParser parser)
        {
        	ItemData item = new ItemData();
        	
        	int count = parser.getAttributeCount();
        	for(int step=0;step<count;step++)
        	{
        		String name  = parser.getAttributeName(step);
        		String value  = parser.getAttributeValue(step);  
        		
        		if("packageName".equals(name))
        		{
        			item.packageName = value;
        		}
        		else if("className".equals(name))
        		{
        			item.className = value;
        		}
        		else if("spanX".equals(name))
        		{
        			item.spanX = value;
        		}
        		else if("spanY".equals(name))
        		{
        			item.spanY = value;
        		}
        		else if("screen".equals(name))
        		{
        			item.screen = value;
        		}
        		else if("x".equals(name))
        		{
        			item.x = value;
        		}
        		else if("y".equals(name))
        		{
        			item.y = value;
        		}     
        		else if("cateid".equals(name))
        		{
        			item.cateid = value;
        		}     
        	}
        	return item;
        }
        private boolean addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent) {

            ActivityInfo info;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    /*String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                    info = packageManager.getActivityInfo(cn, 0);
                    */
                    return false;
                }

                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                values.put(Favorites.INTENT, intent.toUri(0));
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                values.put(Favorites.SPANX, 1);
                values.put(Favorites.SPANY, 1);
                db.insert(TABLE_FAVORITES, null, values);
            } catch (Exception ex){
            	Log.w(TAG, "Unable to add favorite: "+ ex.getMessage());
            	return false;
            }
            return true;
        }

        private ComponentName getSearchWidgetProvider() {
            return null;
        }

        /**
         * Gets an appwidget provider from the given package. If the package contains more than
         * one appwidget provider, an arbitrary one is returned.
         */
        private ComponentName getProviderInPackage(String packageName) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            List<AppWidgetProviderInfo> providers = appWidgetManager.getInstalledProviders();
            if (providers == null) return null;
            final int providerCount = providers.size();
            for (int i = 0; i < providerCount; i++) {
                ComponentName provider = providers.get(i).provider;
                if (provider != null && provider.getPackageName().equals(packageName)) {
                    return provider;
                }
            }
            return null;
        }

        private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
            ComponentName cn = getSearchWidgetProvider();
            return addAppWidget(db, values, cn, 4, 1);
        }

        private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
            ComponentName cn = new ComponentName("com.android.alarmclock",
                    "com.android.alarmclock.AnalogAppWidgetProvider");
            return addAppWidget(db, values, cn, 2, 2);
        }
        
        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager) {

            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);

            if (packageName == null || className == null) {
                return false;
            }

            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                /*String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                cn = new ComponentName(packages[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e1) {
                    hasPackage = false;
                }*/
                hasPackage = false;
            }

            if (hasPackage) {
                int spanX = a.getInt(R.styleable.Favorite_spanX, 0);
                int spanY = a.getInt(R.styleable.Favorite_spanY, 0);
                return addAppWidget(db, values, cn, spanX, spanY);
            }
            
            return false;
        }

        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, ComponentName cn,
                int spanX, int spanY) {
            boolean allocatedAppWidgets = false;
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            int appWidgetId = -1;
            try {
               appWidgetId = mAppWidgetHost.allocateAppWidgetId();
                
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                values.put(Favorites.SPANX, spanX);
                values.put(Favorites.SPANY, spanY);
                values.put(Favorites.INTENT,    cn.getPackageName());
                values.put(Favorites.TITLE,     cn.getClassName());  
                values.put(Favorites.APPWIDGET_ID, appWidgetId);
                db.insert(TABLE_FAVORITES, null, values);

                allocatedAppWidgets = true;
                
                appWidgetManager.bindAppWidgetId(appWidgetId, cn);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Problem allocating appWidgetId", ex);
                if(appWidgetId != -1)
                {
               	    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }catch (Exception ex) {
            	Log.e(TAG, "Problem allocating appWidgetId", ex);
                if(appWidgetId != -1)
                {
               	    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
            
            return allocatedAppWidgets;
        }
        
        private boolean addUriShortcut(SQLiteDatabase db, ContentValues values,
                TypedArray a) {
            Resources r = mContext.getResources();

            final int iconResId = a.getResourceId(R.styleable.Favorite_icon, 0);
            final int titleResId = a.getResourceId(R.styleable.Favorite_title, 0);

            Intent intent;
            String uri = null;
            try {
                uri = a.getString(R.styleable.Favorite_uri);
                intent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                Log.w(TAG, "Shortcut has malformed uri: " + uri);
                return false; // Oh well
            }

            if (iconResId == 0 || titleResId == 0) {
                Log.w(TAG, "Shortcut is missing title or icon resource ID");
                return false;
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.TITLE, r.getString(titleResId));
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
            values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
            values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));

            
            try {
            	db.insert(TABLE_FAVORITES, null, values);
            }catch(Exception ex){}

            return true;
        }
    }
    
    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);                
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
