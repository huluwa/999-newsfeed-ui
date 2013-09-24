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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = "oms2.5Launcher.InstallShortcutReceiver";
    public static final String ACTION_INSTALL_SHORTCUT =  "com.android.launcher.action.INSTALL_SHORTCUT";
    
    private static final String OMSHOME_ICON_FIRST_BOOT="oms.home.intent.extra.ICON_FIRST_BOOT";

    private final int[] mCoordinates = new int[2];
    private Launcher mLauncher;
    
    public InstallShortcutReceiver(Launcher launcher) {
    	mLauncher = launcher;
    }
    
    public void onReceive(Context context, Intent data) {
        Log.d(TAG,"onReceive data:"+data);
        if (!ACTION_INSTALL_SHORTCUT.equals(data.getAction())) {
            return;
        }

        int screen = Launcher.getScreen();

        if (!installShortcut(context, data, screen)) {
            // The target screen is full, let's try the other screens
//            for (int i = 0; i < Launcher.getScreenCount(); i++) {
//                if (i != screen && installShortcut(context, data, i)) break;
//            }

            //final ContentResolver cr = context.getContentResolver();
            int counts = 0;

            try {
                counts = Launcher.getSettingsIntValue(context, Workspace.TAG_SCREEN_NUM);
            } catch (Exception e) {}
            
            if(0 == counts) {
            	Log.w(TAG, "screen num is 0, return");
                return;
            }
            
            boolean isNeedToast = true;
            for(int i = screen+1; i % counts != screen; i++){
                if(Launcher.LOGD)Log.d(TAG," install in screen:"+i % counts);
                if (installShortcut(context, data, i % counts)) {
                	isNeedToast = false;
                	break;
                }
            }
            
            if(isNeedToast){
            	if(data.getBooleanExtra(OMSHOME_ICON_FIRST_BOOT, false) == false)
            	{
                    Toast.makeText(context, context.getString(R.string.out_of_space),
                        Toast.LENGTH_SHORT).show();
            	}
            }
        }
    }

    private boolean installShortcut(Context context, Intent data, int screen) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        
        if(findEmptyCell(context, mCoordinates, screen)) {
            CellLayout.CellInfo cell = new CellLayout.CellInfo();
            cell.cellX = mCoordinates[0];
            cell.cellY = mCoordinates[1];
            cell.screen = screen;

            String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

            if(Launcher.LOGD)Log.d(TAG,"installShortcut  intent:"+intent);
            if (intent.getAction() == null) {
                intent.setAction(Intent.ACTION_VIEW);
            }

            // By default, we allow for duplicate entries (located in
            // different places)
            boolean duplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
            if (duplicate || !LauncherModel.shortcutExists(context, name, intent)) {
            	/*
                ((LauncherApplication)context.getApplicationContext()).getModel()
                        .addShortcut(context, data, cell, true);
                Toast.makeText(context, context.getString(R.string.shortcut_installed, name),
                        Toast.LENGTH_SHORT).show();
            	 */
                if (mLauncher!=null && !mLauncher.getRestoring()) {
                	if(!mLauncher.getIsRestoreBackupWidget()){
                		final LauncherModel mModel = mLauncher.getLauncherModel();
                		if(mModel!=null)
                		{
                			final ShortcutInfo info = mModel.addShortcut(context, data, cell, false);
                			if(info!=null){
                				final LauncherModel.Loader loader = mModel.getLoader(); 
                				if(loader != null && loader.mItems!=null) {
                					final View view = mLauncher.createShortcut(info);
                					
                					//close open folder
                					mLauncher.closeFolder(screen);                					
                					
                					mLauncher.getWorkspace().addInScreen(view, cell.screen,cell.cellX, cell.cellY, 1, 1,mLauncher.isWorkspaceLocked());
                					loader.mItems.add(info);
                					
                					//check orientation changed, need reload the UI
                					mLauncher.orientatonChangedInBackground();
                					
                					if(mLauncher.mDesktopItems!=null){
                						mLauncher.mDesktopItems.add(info);
                				    }
                					if(Launcher.LOGD)Log.d(TAG, "Toast install name:"+name);
                					Toast.makeText(context, context.getString(R.string.shortcut_installed, name),
                							Toast.LENGTH_SHORT).show();
                				}
                			}
                		}
                	}
                } else {
                	Log.d(TAG, "mLauncher is null !!");
                }
            } else {
            	Log.d(TAG, "shortcut already exists.");
                Toast.makeText(context, context.getString(R.string.shortcut_duplicate, name),
                        Toast.LENGTH_SHORT).show();
            }

            return true;
        } else {
//        	if(data.getBooleanExtra(OMSHOME_ICON_FIRST_BOOT, false) == false)
//        	{
//                Toast.makeText(context, context.getString(R.string.out_of_space),
//                    Toast.LENGTH_SHORT).show();
//        	}
        }

        mLauncher.setIsRestoreBackupWidget(false);
        return false;
    }

    private static boolean findEmptyCell(Context context, int[] xy, int screen) {
        final int xCount = Launcher.NUMBER_CELLS_X;
        final int yCount = Launcher.NUMBER_CELLS_Y;

        boolean[][] occupied = new boolean[xCount][yCount];

        final ContentResolver cr = context.getContentResolver();
        Cursor c = null;
        try {
	            c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
	            new String[] { LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY,
	                    LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY },
	            LauncherSettings.Favorites.SCREEN + "=?",
	            new String[] { String.valueOf(screen) }, null);
	
	        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
	        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
	        final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
	        final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
	
	            while (c.moveToNext()) {
	                int cellX = c.getInt(cellXIndex);
	                int cellY = c.getInt(cellYIndex);
	                int spanX = c.getInt(spanXIndex);
	                int spanY = c.getInt(spanYIndex);
	
	                for (int x = cellX; x < cellX + spanX && x < xCount; x++) {
	                    for (int y = cellY; y < cellY + spanY && y < yCount; y++) {
	                        occupied[x][y] = true;
	                    }
	                }
	            }
        } catch (Exception e) {
            return false;
        } finally {
        	if(null != c) c.close();
        }

        return CellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }
}
