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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends FrameLayout {
    DragController mDragController;

    final static String TAG="DragLayer" ;
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDragController(DragController controller) {
        mDragController = controller;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    
    public void dumpFocus()
    {
    	Log.d(TAG, "begin dump focus for all view");
    	dumpFocus(this);
    	Log.d(TAG, "end dump focus for all view");
    }
    
    public void dumpFocus(ViewGroup view)
    {
    	Log.d(TAG, "B view="+view);
    	if(this.hasFocus())
    	{
    		Log.d(TAG, view + "*********************** get foucs *****************************="+view);
    	}
    	else
    	{
    		Log.d(TAG, view + " no foucs");
    	}
    	Log.d(TAG, "E  view="+view);
    	
    	for(int i=0;i<view.getChildCount();i++)
    	{
    		View v = view.getChildAt(i);
    		if(v !=null)
    		{
    			Log.d(TAG, "B view="+view);
    	    	if(v.hasFocus())
    	    	{
    	    		Log.d(TAG, v + "*********************** get foucs *****************************="+v);
    	    	}
    	    	else
    	    	{
    	    		Log.d(TAG, v + " no foucs");
    	    	}
    	    	Log.d(TAG, "E view="+v);
    	    	
    	    	if(ViewGroup.class.isInstance(v))
    	    	{
    	    		if(Workspace.class.isInstance(v) || CellLayout.class.isInstance(v))
    	    		{
    			        dumpFocus((ViewGroup)v);
    	    		}
    	    	}
    		}    		
    	}
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragController.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDragController.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mDragController.dispatchUnhandledMove(focused, direction);
    }
}
