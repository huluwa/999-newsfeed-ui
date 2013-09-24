 /*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.tormas.home;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewRoot;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.SurfaceHolder;
import android.view.Gravity;

import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.widget.Button;
import android.view.View.OnClickListener;

import java.lang.Math.*;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import android.opengl.GLSurfaceView;

import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;

public class TextureSwitchActivity extends Activity implements RotateTexturesView.PageChangeListener {
    public ImageView addPage;
    public ImageView deletePage;
	public TextView curPage;
	public DefautPageTextView setDefaultPage;
    private static final String TAG = "Launcher.TextureSwitchActivity";
    
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	 	setContentView(R.layout.ringpages);

		mGLView = (RotateTexturesView) findViewById(R.id.glrenderview);
		mGLView.setPageChangeListener(this);
		mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		
		//if( Workspace.getPageViews().size()==0){
		//	Workspace.createPageBitmaps();
		//}
       	mGLView.GetBitmaps(Workspace.getPageViews());
		 //mGLView.BitmapFromView(Workspace.getPageViews());

		
		addPage = (ImageView)findViewById(R.id.add_page);
		deletePage = (ImageView)findViewById(R.id.remove_page);
		curPage = (TextView)findViewById(R.id.cur_page);
		setDefaultPage = (DefautPageTextView)findViewById(R.id.setDefaultPage);
	
		Bundle bundle=getIntent().getExtras();
		if( bundle!=null){
			int curPageNum = bundle.getInt(Launcher.CURRENT_PAGE_INDEX);
			int Pagecount = bundle.getInt(Launcher.PAGE_COUNT);
			mGLView.SetCurPage(curPageNum);
			mGLView.SetPageCount(Pagecount);
			if(Launcher.LOGD)Log.d(TAG,"Pagecount"+Pagecount);
		}
		else{
			finish();
			if(Launcher.LOGD)Log.d(TAG,"Pagecount");
		}

		addPage.setClickable(false);
		deletePage.setClickable(false);
		setDefaultPage.setClickable(false);

		setDefaultPage.setOnClickListener( new View.OnClickListener() {			
			public void onClick(View v) {
				boolean suc = LauncherORM.instance(TextureSwitchActivity.this).addSetting(LauncherORM.default_page_index, String.valueOf((mGLView.getCurPageIndex())));
				if(suc == true)
				{
					Toast.makeText(TextureSwitchActivity.this, R.string.set_default_screen_hint,Toast.LENGTH_SHORT).show();
				}				
			}
		});

		getWindow().setWindowAnimations(-1);
		registerReceivers();

		
		curPage.setText(""+(mGLView.getCurPageIndex()+1));		
		setDefaultPage.setText(R.string.set_default_screen, null);


		//Intent intent = new Intent(Launcher.INTENT_CACHE_SCREEN);
		//sendBroadcast(intent);
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
	    getWindow().setBackgroundDrawable(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();

		if(Launcher.LOGD)Log.d(TAG,"startActivity end ");
		
		final boolean enableDelete = LauncherORM.instance(this).isEnableDeleteDefaultPage();
	    int curPage = mGLView.getCurPageIndex();
	    if(curPage <=2)
		{
			if(enableDelete == false)
			{
				deletePage.setEnabled(false);		
				deletePage.setImageBitmap(null);
			}
			else
			{
				deletePage.setEnabled(true);
				deletePage.setImageResource(R.drawable.cmcc_switch_delete);
			}
		}
		else
		{
			deletePage.setEnabled(true);
			deletePage.setImageResource(R.drawable.cmcc_switch_delete);
		}

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    		Log.d(TAG, "onConfigurationChanged");

 //       if (mGLView != null){
 //           mGLView.SetOrientation(newConfig.orientation);
 //       	Log.d(TAG, "onConfigurationChanged");
 //       }
        super.onConfigurationChanged(newConfig);
 		finish();

    }

	public void removeTempView(){
		//mFrameLayout.removeView(mImageBgView);
		//mImageBgView.setVisibility(View.INVISIBLE);
	}

	private FrameLayout mFrameLayout;
	private RelativeLayout mRelativeLayout;
    private RotateTexturesView mGLView;
	//private GLSurfaceView mGLView;


    public static final int UpdateCurPageText_MSG = 1;
    public static final int Back_MSG = 2;


	
    private static final int DIALOG_YES_NO_MESSAGE = 1;


	public Handler GetHandler(){
		return mHandler;
	};



	private Handler mHandler = new Handler() {
   		@Override 
		public void handleMessage(Message msg) {
            switch (msg.what) {
                // Upon receiving the fade pulse, we have the view perform a
                // fade and then enqueue a new message to pulse at the desired
                // next time.
                case UpdateCurPageText_MSG: {
                    curPage.setText(""+(mGLView.getCurPageIndex()+1));
                    break;
                }
				case Back_MSG:{
					curPage.setVisibility(View.INVISIBLE);
					break;
					}
                default:
                    super.handleMessage(msg);
            }
        }
    };

		
	private BroadcastReceiver mPagechangeReceiver = new PagechangeReceiver();

	
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_YES_NO_MESSAGE:
			AlertDialog dialog =  new AlertDialog.Builder(TextureSwitchActivity.this)
							.setIcon(R.drawable.cmcc_dialog_question2)
							.setTitle(R.string.remove_screen_title)
							.setPositiveButton(R.string.remove_screen_ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									
									//User clicked OK so do some stuff 

									Intent intent = new Intent(Launcher.INTENT_REMOVE_SCREEN);
									intent.putExtra(Launcher.INTENT_REMOVE_SCREEN_PAGEINDEX, mGLView.getCurPageIndex()); 
									sendBroadcast(intent);
									Toast.makeText(TextureSwitchActivity.this, getResources().getString(R.string.remove_one_screen)+(mGLView.getCurPageIndex()+1),1000).show();
									
									addPage.setClickable(false);
									deletePage.setClickable(false);
									mGLView.RemoveCurPage();
									mGLView.UpdatePages(false);
								}
							})
							.setNegativeButton(R.string.remove_screen_cancel, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									//User clicked Cancel so do some stuff 
								}
							}).create();
			
		                 	dialog.setOnDismissListener(new OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
									mGLView.setIgnoreSensor(false);
								}
							});
		                 	
		                 	return dialog;

		}
        
		return null;
	}

	//TODO  need modify the tips, and consider whether we need set the image view un-onclickable
    public void addPage(View view) {
        if(Launcher.LOGD)Log.d(TAG,"addPage");
		//if(Launcher.LOGD)Log.d(TAG,"Launcher.getScreenCount() " +Launcher.getScreenCount() );
		//if(Launcher.LOGD)Log.d(TAG,"Workspace.MAX_SCREEN_COUNT" + Workspace.MAX_SCREEN_COUNT);
        if(Launcher.getScreenCount() >= Workspace.MAX_SCREEN_COUNT) {
            Toast.makeText(this, getResources().getString(R.string.add_screen_max),1000).show();
        } else {
            Intent intent = new Intent(Launcher.INTENT_ADD_SCREEN);
            sendBroadcast(intent);
            Toast.makeText(this, getResources().getString(R.string.add_one_screen),1000).show();
			addPage.setClickable(false);
			deletePage.setClickable(false);
			mGLView.AddPage();
			mGLView.UpdatePages(false);
		}
    }

    public void removePage(View view) {
    	mGLView.setIgnoreSensor(true);
	
        if(Launcher.LOGD)Log.d(TAG,"removePage");
        if(Launcher.getScreenCount() <= Workspace.MIN_SCREEN_COUNT) {
            Toast.makeText(this, this.getResources().getString(R.string.remove_screen_min), 1000).show();
        } else {
            if(Launcher.LOGD)Log.d(TAG,"AlertDialog");
			showDialog(DIALOG_YES_NO_MESSAGE);
		}
    }

	public void CurPage(View view){
		mGLView.ChangeState();
	}
	

    @Override
    protected void onDestroy() {
       if(Launcher.LOGD)Log.d(TAG,"onDestroy");
       unregisterReceiver(mPagechangeReceiver);
       unregisterReceiver(mHomeKeyReceiver);
       super.onDestroy();
    }

    @Override
    protected void onStop() {
        if(Launcher.LOGD)Log.d(TAG,"onStop");
        super.onStop();
    }
    
 	private class PagechangeReceiver extends BroadcastReceiver {
		   @Override
		   public void onReceive(Context context, Intent intent) {
			   String action = intent.getAction();
			   //if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~PagechangeReceiver onReceive action:"+action);
			   if(action.equals(Launcher.INTENT_UPDATE_SCREEN_OK)) {
					mGLView.UpdatePages(true);
					if(Launcher.LOGD)Log.d(TAG,"PagechangeReceiver onReceive action:"+action);
					
			   } 
		   	}
	}  

	private void registerReceivers() {
	    if(Launcher.LOGD)Log.d(TAG,"registerReceivers");
        IntentFilter Refreshfilter = new IntentFilter(Launcher.INTENT_UPDATE_SCREEN_OK);
       // Refreshfilter.addAction(Launcher.INTENT_UPDATE_SCREEN_OK);
        registerReceiver(mPagechangeReceiver,Refreshfilter);
        
        IntentFilter backAndHomefilter = new IntentFilter(Launcher.HOMEKEY_PRESSED_IN_HOME); //home
        backAndHomefilter.addAction(Launcher.ENDCALLKEY_PRESSED_IN_HOME);//end       
       registerReceiver(mHomeKeyReceiver, backAndHomefilter);
	}
	
	private final  BroadcastReceiver mHomeKeyReceiver = new HomeKeyIntentReceiver();
	private class HomeKeyIntentReceiver extends BroadcastReceiver
	{
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            if(intent != null)
            {
                String action = intent.getAction();
                if(action != null && action.equals(Launcher.HOMEKEY_PRESSED_IN_HOME))
                {
                	Log.d(TAG, "HOMEKEY_PRESSED_IN_HOME  nothing here");
                    //backToLauncher(1) ;
                } else if(action != null && action.equals(Launcher.ENDCALLKEY_PRESSED_IN_HOME)) {
                	Log.d(TAG, "ENDCALLKEY_PRESSED_IN_HOME");
                    backToLauncher(1) ;
                    finish();
                } 
            }
        }       
	}
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

		if(Launcher.LOGD)Log.d(TAG,"onKeyDown------"+keyCode);

        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
            Log.d(TAG, "onKeyDown back");
            backToLauncher(1);
            finish();
            return true;
        }

        return  super.onKeyUp(keyCode, event);
    }
    
	private void backToLauncher(int with_effect) {
	    if(Launcher.LOGD)Log.d(TAG, "backToLauncher index="+mGLView.getCurPageIndex());
        Intent i = new Intent(Launcher.INTENT_FINISH_PAGEMANAGER);            
        Bundle b = new Bundle();  
        b.putInt(Launcher.STRING_PAGE_INDEX, mGLView.getCurPageIndex());
        b.putInt(Launcher.WITH_FADE_EFFECT,  with_effect);
        i.putExtras(b);  
        sendBroadcast(i);
	}
	
	
//	 private static final int MENU_SETTINGS = 1;
//	 private static final int MENU_SWITCH_STYLE = MENU_SETTINGS + 1;
//	@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		
////	    menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
////        .setIcon(R.drawable.cmcc_toolbar_setting).setAlphabeticShortcut('P');
//        menu.add(0,MENU_SWITCH_STYLE,0,R.string.menu_switch_to_2D)
//        .setIcon(R.drawable.switch_2d_view);
//		
//	    return true;
//	}
//	
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//	     super.onPrepareOptionsMenu(menu);
//
//	     MenuItem mItem = menu.getItem(0);
//	     if(Launcher.isAllowSwitchView())
//	     {
//	         mItem.setVisible(true);
//	     }
//	     else
//	     {
//	        mItem.setVisible(false);
//	     }
//	     return true;
//	  }
//	
//	final int HOME_SETTING = 100;
//	@Override
//    public boolean onOptionsItemSelected(MenuItem item) 
//	{
//        switch (item.getItemId()) 
//        {        
//	        case MENU_SETTINGS:
//	        {
//	        	Intent intent = new Intent(this, HomeSettingPreference.class);
//	        	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//	            startActivityForResult(intent, HOME_SETTING);
//	        	break;
//	        }
//	        case MENU_SWITCH_STYLE :
//	        {
//	            switchStyle();
//	            break;
//	        }
//        }        
//        return super.onOptionsItemSelected(item);
//	}
//	
//	private void switchStyle() {
//	    //switchTo2D
//	    LauncherORM.instance(this.getApplicationContext()).Enable2D(true);
//	    restartLauncher();
//    }
	
	private void restartLauncher() {
	    //backToLauncher(1);
//	    Intent i = new Intent(Launcher.INTENT_RELAUNCHE_ACTIVITY);
//	    sendBroadcast(i);
//        finish();
		   try{                   
	           Process.killProcess(Process.myPid());                	              	
	       }catch(Exception ne){}
        //relaunch Launcher
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) 
    {
        switch (requestCode) 
        {
//	        case HOME_SETTING:
//	        {
//	        	Log.d(TAG, "back from setting");
//	        	
//	            Process.killProcess(Process.myPid());
//	        	break;
//	        }
        }
    }

	public void onPageChange(final int prePage, final int curPage) {	
		
	    final boolean enableDelete = LauncherORM.instance(this).isEnableDeleteDefaultPage();
		handler.post( new Runnable()
		{
			public void run()
			{
				if(curPage <=2)
				{
					if(enableDelete == false)
					{
						deletePage.setEnabled(false);		
						deletePage.setImageBitmap(null);
					}
					else
					{
						deletePage.setEnabled(true);
						deletePage.setImageResource(R.drawable.cmcc_switch_delete);
					}
				}
				else
				{
					deletePage.setEnabled(true);
					deletePage.setImageResource(R.drawable.cmcc_switch_delete);
				}
			}
		});
	}
}
