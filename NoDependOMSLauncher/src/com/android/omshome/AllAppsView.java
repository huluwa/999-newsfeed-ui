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

package com.android.omshome;

import java.util.ArrayList;

import android.util.Log;

public interface AllAppsView {
    public interface Watcher {
        public void zoomed(float zoom);
    }

    public void setLauncher(Launcher launcher);

    public void setDragController(DragController dragger);

    public void zoom(float zoom, boolean animate);

    public boolean isVisible();

    public boolean isOpaque();

    public void setApps(ArrayList<ApplicationInfo> list);
    
    public ArrayList<ApplicationInfo> getApps();

    public void addApps(ArrayList<ApplicationInfo> list);

    public void addAppsWithoutSort(ArrayList<ApplicationInfo> list);
    
    public void removeApps(ArrayList<ApplicationInfo> list);

    public void updateApps(ArrayList<ApplicationInfo> list);
    
    public void dumpState();

    public void surrender();
    
    public void setFocusable(boolean focusable);
    
    static class Defines {
        public static final int ALLOC_PARAMS = 0;
        public static final int ALLOC_STATE = 1;
        public static final int ALLOC_ICON_IDS = 3;

        public static int COLUMNS_PER_PAGE = 4;
        public static int ROWS_PER_PAGE = 4;

        public static float ICON_TEXTURE_WIDTH_PX = 128; 
        public static float ICON_TEXTURE_HEIGHT_PX = 128;

        public static float SCREEN_WIDTH_PX;
        public static float SCREEN_HEIGHT_PX;
        public static String mResolution = "";
         
        public void recompute(int w, int h) {
        	SCREEN_WIDTH_PX = w; 
        	SCREEN_HEIGHT_PX = h;
            Log.d("Launcher.AllAppsView", "SCREEN_WIDTH_PX:" + Defines.SCREEN_WIDTH_PX + " SCREEN_HEIGHT_PX:" + Defines.SCREEN_HEIGHT_PX);
        }
    }

    public void setStartForPicker(boolean forPicker);
}
