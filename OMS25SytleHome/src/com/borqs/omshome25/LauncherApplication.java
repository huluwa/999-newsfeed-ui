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

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;
import dalvik.system.VMRuntime;

public class LauncherApplication extends Application {
    public LauncherModel mModel;
    private final static float TARGET_HEAP_UTILIZATION = 0.75f;
    public IconCache mIconCache;

    @Override
    public void onCreate() {
    	Log.d("oms2.5LauncherApplication", "onCreate");
        VMRuntime.getRuntime().setMinimumHeapSize(6 * 1024 * 1024);
//        VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);

        super.onCreate();

        mIconCache = new IconCache(this);
        mModel = new LauncherModel(this, mIconCache);
 
        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);

        filter = new IntentFilter();
        filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        registerReceiver(mModel, filter);
        
        filter = new IntentFilter();
        filter.addAction("omshome.backup_screennumber");
        registerReceiver(mModel,filter);
        
        filter = new IntentFilter();
        filter.addAction("omshome.restore_screennumber");
        registerReceiver(mModel,filter);

        //Register for changes to the favorites
//        ContentResolver resolver = getContentResolver();
//        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
//                mFavoritesObserver);
    }

    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        unregisterReceiver(mModel);

        ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // TODO: lockAllApps();
        	Log.d("oms2.5Launcher", "mFavoritesObserver  data changing, start loader");
            mModel.startLoader(LauncherApplication.this, false);
        }
    };

    LauncherModel setLauncher(Launcher launcher) {
        mModel.initialize(launcher);
        
        // Register for changes to the favorites
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
        
        return mModel;
    }
    
    IconCache getIconCache() {
        return mIconCache;
    }

    LauncherModel getModel() {
        return mModel;
    }
}
