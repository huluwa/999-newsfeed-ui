package com.tormas.home;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;
import com.tormas.home.R;

import com.tormas.home.screenmanager.*;
import com.tormas.home.screenmanager.AdapterView.OnItemClickListener;
import com.tormas.home.screenmanager.AdapterView.OnItemLongClickListener;
import com.tormas.home.screenmanager.AdapterView.OnItemSelectedListener;

public class EditPageActivity extends Activity implements NewGallery.PageChangeListener{
    public ImageView addPage;
    public ImageView deletePage;
    public TextView curPage;
    public DefautPageTextView setDefaultPage;
    
    private NewGallery mGLView;
    private PageImageAdapter mAdapter;
    private ArrayList<Bitmap> mPageImageList;
    
    private static final String TAG = "EditPageActivity";
    
    Handler handler = new Handler();
    private ScaleAnimation screenExitAnimation;
    private  int pageCount = -1;
    private  int curPagePos = -1;
    private  boolean isDefaultBitmap = false;
    private Handler screenHandler;
    private boolean mProtrait = true;
    
   // private int scale = 1;//need to define for different dpi
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	if(Launcher.LOGD)Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_page);
        mGLView = (NewGallery) findViewById(R.id.glrenderview);
        mGLView.setPageChangeListener(this);
        mGLView.enableChildrenCache();
        mGLView.setUnselectedAlpha(1);
        mGLView.setOnItemSelectedListener(itemSelectedListener);
        mGLView.setOnItemLongClickListener(itemLongClickListener);
        mGLView.setOnItemClickListener(itemClickListener);
        
        addPage = (ImageView)findViewById(R.id.add_page);
        deletePage = (ImageView)findViewById(R.id.remove_page);
        curPage = (TextView)findViewById(R.id.cur_page);
        setDefaultPage = (DefautPageTextView)findViewById(R.id.setDefaultPage);
        
        int windowHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        int windowWidth = this.getWindowManager().getDefaultDisplay().getWidth();
        int statusBarHeight = Integer.valueOf(SystemProperties.get("sys.statusbar.height", "40"));
        mProtrait = windowHeight > windowWidth;
        int mGalleryPaddingTop = (windowHeight - statusBarHeight - (int)this.getResources().getDimension(R.dimen.page_edit_image_height))/2;
        mGLView.setPaddingTop(mGalleryPaddingTop);
        if(mProtrait)
        {
            addPage.getLayoutParams().width = windowWidth / 3; 
            deletePage.getLayoutParams().width = windowWidth / 3;
            setDefaultPage.getLayoutParams().width = windowWidth / 3;
        }
        else
        {
            addPage.getLayoutParams().height = Math.round((windowHeight-statusBarHeight)/3.f); 
            deletePage.getLayoutParams().height = Math.round((windowHeight-statusBarHeight)/3.f);
            setDefaultPage.getLayoutParams().height = Math.round((windowHeight-statusBarHeight)/3.f);
        }
        
//        Log.d(TAG,"mGalleryPaddingTop="+mGalleryPaddingTop+"button width="+windowWidth / 3);
        Bundle bundle=getIntent().getExtras();
        if( bundle!=null){
            int curPageNum = bundle.getInt(Launcher.CURRENT_PAGE_INDEX);
            int Pagecount = bundle.getInt(Launcher.PAGE_COUNT);
            curPagePos = curPageNum;
            pageCount = Pagecount;
        }
        
        mAdapter = getAdapter();
        mGLView.setAdapter(mAdapter); 
        mGLView.setSelection(curPagePos,true);
        mGLView.setFocusableInTouchMode(true);
        
        addPage.setClickable(false);
        deletePage.setClickable(false);
        setDefaultPage.setClickable(false);
       
        //curPage.setTextSize(18);

       setDefaultPage.setOnClickListener( new View.OnClickListener() {         
           public void onClick(View v) {
                boolean suc = LauncherORM.instance(EditPageActivity.this).addSetting(LauncherORM.default_page_index, String.valueOf((mGLView.getCurPageIndex())));
                if(suc == true)
                {
                    Toast.makeText(EditPageActivity.this, R.string.set_default_screen_hint,Toast.LENGTH_SHORT).show();
                }               
            }
        });

        getWindow().setWindowAnimations(-1);
        registerReceivers();
        
        curPage.setText(""+(mGLView.getCurPageIndex()+1));      
        setDefaultPage.setText(R.string.set_default_screen, null);
//        Log.d(TAG,"statusbar height="+Integer.valueOf(SystemProperties.get("sys.statusbar.height", "40"))+"=== windownWidth="+this.getWindowManager().getDefaultDisplay().getWidth()+"==windowHeight="+this.getWindowManager().getDefaultDisplay().getHeight());
        float padding = getResources().getDimension(R.dimen.page_edit_image_padding);
        float scaleHeight = (getWindowManager().getDefaultDisplay().getHeight() - Integer.valueOf(SystemProperties.get("sys.statusbar.height", "40"))) / (getResources().getDimension(R.dimen.page_edit_image_height)-2*padding);       
        float scaleWidth  = (getWindowManager().getDefaultDisplay().getWidth())/(getResources().getDimension(R.dimen.page_edit_image_width)-2*padding); 
        float scale = Math.max(scaleHeight,scaleWidth);
//        Log.d(TAG,"#########scaleHeight="+scaleHeight+"==scaleWidth="+scaleWidth);
        float povitX = getResources().getDimension(R.dimen.page_edit_image_width)/2.f;
        float povitY = getResources().getDimension(R.dimen.page_edit_image_height)/2.f;
        screenExitAnimation = new ScaleAnimation(1.0f,scale,1.f,scale,povitX,povitY);
        
        screenExitAnimation.setDuration(150);
        screenExitAnimation.setFillAfter(true);
        screenExitAnimation.setAnimationListener(new AnimationListener(){
			public void onAnimationEnd(Animation animation) {
				backToLauncher(1);
				finish();
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
        });
        
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setBackgroundDrawable(null);
    }
    
    private void Back(){
    	Intent i = new Intent(Launcher.INTENT_FINISH_PAGEMANAGER);            
        Bundle b = new Bundle();  
        b.putInt(Launcher.STRING_PAGE_INDEX, mGLView.getCurPageIndex()); //TODO pass current screen index.
		  
        i.putExtras(b);  
        sendBroadcast(i);
        safeFinish();
    }
    
    private void clearCache()
    {
    	if(Launcher.LOGD)Log.d(TAG,"clearCache");
    	if(defaultBitmap != null && !defaultBitmap.isRecycled())
    	{
    	    defaultBitmap.recycle();
    	    defaultBitmap = null;
    	}
    	if(mGLView != null)
    	{
    	    mGLView.removeCallbacks(mGLView.replaceDragedViewCallBack);
            mGLView.replaceDragedViewCallBack = null;
            
    		for(int i=0;i<mGLView.getChildCount();i++)
    		{
    		    ScreenView sv = (ScreenView)mGLView.getChildAt(i);
    		    sv.unbind();
    		} 
    		mGLView.clearCache();
    		mGLView = null;	
    	}
    	
    	if(mPageImageList != null)
    	{
	    	for(int i=0;i<mPageImageList.size();i++)
			{
			    Bitmap bmp = mPageImageList.get(i);
			    if(bmp != null && bmp.isRecycled()==false)
			    {
			    	bmp.recycle();
			    }
			}
			mPageImageList.clear();
			mPageImageList = null;
    	}
    	
    	System.gc();
    	if(Launcher.LOGD)Log.d(TAG,"exit clearCache");
    }
    
    private void safeFinish()
    {    	
    	if(Launcher.LOGD)Log.d(TAG, "call safeFinish");
    	unRegisterRev();
        if(isFinishing() == false)
        {
            try{
    	        finish();
            }catch(Exception ne){}
        }
    }

    OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener(){
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
            int curIndex = mGLView.getSelectedItemPosition();
            curPage.setText(""+(curIndex+1));  
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    };
    
    OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener()
    {
        public boolean onItemLongClick(AdapterView<?> arg0, View downTouchedView, int downTouchedPosition, long id) {
            if(mPageImageList.size()>1 && downTouchedView !=null && downTouchedView instanceof ScreenView)
            {
                mGLView.startDrag((ScreenView)downTouchedView,downTouchedPosition);
            }
            return false;
        }
    };
    
    OnItemClickListener itemClickListener = new OnItemClickListener()
    {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		    
		    if(mGLView.getChildCount() > 1)
		    {
		        if(position == 0)
		        {
		            mGLView.getChildAt(position+1).setVisibility(View.GONE);
		        }
		        else if(position == mGLView.getChildCount()-1)
		        {
		            mGLView.getChildAt(position-1).setVisibility(View.GONE);
		        }
		        else
		        {
		            mGLView.getChildAt(position+1).setVisibility(View.GONE);
		            mGLView.getChildAt(position-1).setVisibility(View.GONE);
		        }
		    }
		   
		    if(Launcher.LOGD)Log.d(TAG,"onItemClick w:"+mPageImageList.get(position).getWidth()+" h:"+mPageImageList.get(position).getHeight()+"  viewH:"+view.getWidth()+" viewW:"+view.getHeight()+"");
			curPage.setVisibility(View.GONE);
			addPage.setVisibility(View.GONE);
		    deletePage.setVisibility(View.GONE);
		    setDefaultPage.setVisibility(View.GONE);
		    
			((ScreenView)view).setBackgroundNull();
			view.startAnimation(screenExitAnimation);
		}
    };
    
    private void updatePages() {
        if(mAdapter != null)
        {
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private Bitmap defaultBitmap = null;
    private PageImageAdapter getAdapter() {
        if(mAdapter == null)
        {  
        	if(Launcher.LOGD)Log.d(TAG,"entering create DefaultBitmap");
            defaultBitmap = createDefaultBitmap();
            if(mPageImageList == null)
            {
                mPageImageList = new ArrayList();
            }
            for(int i=0;i<pageCount;i++)
            {
                mPageImageList.add(defaultBitmap);
            }
            mAdapter = new PageImageAdapter(this.getApplicationContext(),mPageImageList);
        }
        
        return mAdapter;
    }
    
    private Bitmap createDefaultBitmap() {
        int width = (int)this.getResources().getDimension(R.dimen.page_edit_image_width);
        int height = (int)this.getResources().getDimension(R.dimen.page_edit_image_height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mGLView.onResume();
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
    protected void onDestroy() {
       if(Launcher.LOGD)Log.d(TAG,"onDestroy");
       
       clearCache();
       unRegisterRev();
       
       super.onDestroy();
    }
    
    //
    //to make sure not  repeat unregister the receiver
    private synchronized void unRegisterRev()
    {
        if(mPagechangeReceiver !=null)
        {
            unregisterReceiver(mPagechangeReceiver);
            mPagechangeReceiver = null;
        }
        
        if(mHomeKeyReceiver != null)
        {
            unregisterReceiver(mHomeKeyReceiver);
            mHomeKeyReceiver = null;
        }
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	if(Launcher.LOGD)Log.d(TAG, "onKeyDown back");
            backToLauncher(1);
            safeFinish();
            return true;
        }

        return  super.onKeyUp(keyCode, event);
    }
    
    public static final int UpdateCurPageText_MSG = 1;
    public static final int Back_MSG = 2;
    
    private static final int DIALOG_YES_NO_MESSAGE = 1;
    private static final int UPDATE_PAGE_MSG = 3;


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
                case UPDATE_PAGE_MSG:
                {
                	if(msg.getData().getBoolean("clickable") == true)
                	{
                		addPage.setClickable(true);
                        deletePage.setClickable(true);
                        setDefaultPage.setClickable(true);
                	}
                	 
                     
                	updatePages();
                	break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_YES_NO_MESSAGE:
            return new AlertDialog.Builder(EditPageActivity.this)
                            .setIcon(R.drawable.cmcc_dialog_question2)
                            .setTitle(R.string.remove_screen_title)
                            .setPositiveButton(R.string.remove_screen_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
            
                                    //User clicked OK so do some stuff 

                                    Intent intent = new Intent(Launcher.INTENT_REMOVE_SCREEN);
                                    intent.putExtra(Launcher.INTENT_REMOVE_SCREEN_PAGEINDEX, mGLView.getCurPageIndex()); 
                                    sendBroadcast(intent);
                                    Toast.makeText(EditPageActivity.this, getResources().getString(R.string.remove_one_screen)+(mGLView.getCurPageIndex()+1),1000).show();
                                    
                                    addPage.setClickable(false);
                                    deletePage.setClickable(false);
                                    int removePage = mGLView.getCurPageIndex();
                                    if(removePage > -1 && removePage < mGLView.getCount())
                                    {
                                        Bitmap bitmap = mPageImageList.get(removePage);
                                        if(bitmap.isRecycled() == false)
                                        {
                                            bitmap.recycle();
                                        }
                                        mPageImageList.remove(removePage);
                                       // mGLView.removeViewAt(removePage);
                                        updatePages();
                                    }
                                    
                                }
                            })
                            .setNegativeButton(R.string.remove_screen_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
            
                                    //User clicked Cancel so do some stuff 
                                }
                            })
                            .create();

        }
        return null;
    }

    //TODO  need modify the tips, and consider whether we need set the image view un-onclickable
    public void addPage(View view) {
        if(Launcher.LOGD)Log.d(TAG,"addPage");
        if(Launcher.getScreenCount() >= Workspace.MAX_SCREEN_COUNT) {
            Toast.makeText(this, getResources().getString(R.string.add_screen_max),1000).show();
        } else {
            Intent intent = new Intent(Launcher.INTENT_ADD_SCREEN);
            sendBroadcast(intent);
            Toast.makeText(this, getResources().getString(R.string.add_one_screen),1000).show();
            
            
            Bitmap bitmap = createDefaultBitmap();//Bitmap.createBitmap(256*scale, 512*scale, Bitmap.Config.ARGB_8888);
            mPageImageList.add(bitmap);
            mGLView.needFlingToLast = true;
            mGLView.setLastViewAsSelectedScreen();
            updatePages();
            addPage.setClickable(false);
            deletePage.setClickable(false);
        }
    }
    
    public void removePage(View view) {
        if(Launcher.LOGD)Log.d(TAG,"removePage");
        if(mGLView.getChildCount() <= Workspace.MIN_SCREEN_COUNT) {
            Toast.makeText(this, this.getResources().getString(R.string.remove_screen_min), 1000).show();
        } else {
            if(Launcher.LOGD)Log.d(TAG,"AlertDialog");
            showDialog(DIALOG_YES_NO_MESSAGE);
        }
    }

    public void CurPage(View view){
       // mGLView.ChangeState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(Launcher.LOGD) Log.d(TAG,"entering onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        finish();
        
    }

    private BroadcastReceiver mPagechangeReceiver = new PagechangeReceiver();
    private class PagechangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Launcher.LOGD)Log.d(TAG,"PagechangeReceiver onReceive action:"+action);
            if(action.equals(Launcher.INTENT_UPDATE_SCREEN_OK)) {
                addPage.setClickable(true);
                deletePage.setClickable(true);
               
            } 
            else if(action.equals(Launcher.INTENT_CREATE_BITMAP_OK))
            {
               if(EditPageActivity.this.isFinishing()) 
               {
            	   if(Launcher.LOGD)Log.d(TAG,"EditPageActivity is finishing, ignore message INTENT_CREATE_BITMAP_OK");
                   return;
               }
               synchronized(mPageImageList)
               {
                   final ArrayList<Bitmap> imageList = Workspace.getPageViews(); 
                   for(int i=0;i<mPageImageList.size();i++)
                   {  
                       Bitmap bitmap = Bitmap.createBitmap(imageList.get(i));
                       if(Launcher.LOGD)Log.d(TAG,"bitmap widht="+bitmap.getWidth()+"bitmap height="+bitmap.getHeight());
                      /* if(i==0)
                       {
                           Bitmap oldBitmap = mPageImageList.get(i);
                           if(oldBitmap.isRecycled()==false)
                           {
                               oldBitmap.recycle();
                           }
                       }*/
                       mPageImageList.set(i, bitmap);              
                   }
                   
                   updatePages();
                   
                   addPage.setClickable(true);
                   deletePage.setClickable(true);
                   setDefaultPage.setClickable(true);
                   
                   mHandler.post( new Runnable(){
                        public void run() {   
                            Workspace.clearPageViews();
                            System.gc();
                        } 
                   });
                  
               }
            }
         }
    }  
    
    private void registerReceivers() {
        if(Launcher.LOGD)Log.d(TAG,"registerReceivers");
        IntentFilter Refreshfilter = new IntentFilter(Launcher.INTENT_UPDATE_SCREEN_OK);
        Refreshfilter.addAction(Launcher.INTENT_CREATE_BITMAP_OK);
        registerReceiver(mPagechangeReceiver,Refreshfilter);
        
        
        IntentFilter backAndHomefilter = new IntentFilter(Launcher.HOMEKEY_PRESSED_IN_HOME); //home
        backAndHomefilter.addAction(Launcher.ENDCALLKEY_PRESSED_IN_HOME);//end       
        registerReceiver(mHomeKeyReceiver, backAndHomefilter);
    }
    
    private  BroadcastReceiver mHomeKeyReceiver = new HomeKeyIntentReceiver();
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
                	if(Launcher.LOGD)Log.d(TAG, "HOMEKEY_PRESSED_IN_HOME  nothing here");
                    //backToLauncher(1) ;
                } else if(action != null && action.equals(Launcher.ENDCALLKEY_PRESSED_IN_HOME)) {
                	if(Launcher.LOGD)Log.d(TAG, "ENDCALLKEY_PRESSED_IN_HOME");
                	
                    backToLauncher(1) ;
                    safeFinish();
                } 
            }
        }       
    }
    
    private void backToLauncher(int with_effect) {
        // if(Launcher.LOGD)Log.d(TAG, "backToLauncher index="+mGLView.getCurPageIndex());
        if(this.isFinishing()==false)
        { 
            try{
                if(mGLView != null)
                {
                    Intent i = new Intent(Launcher.INTENT_FINISH_PAGEMANAGER);            
                    Bundle b = new Bundle();  
                    b.putInt(Launcher.STRING_PAGE_INDEX, mGLView.getCurPageIndex());
                    b.putInt(Launcher.WITH_FADE_EFFECT,  with_effect);
                    i.putExtras(b);  
                    sendBroadcast(i);
                } 
            }
            catch(Exception e)
            {
                Log.d(TAG,"why come here! has finished");
            }
        }
       
    }
    
    final int HOME_SETTING = 100;
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) 
    {
        switch (requestCode) 
        {
            case HOME_SETTING:
            {
            	 if(Launcher.LOGD)Log.d(TAG, "back from setting");
                break;
            }
        }
    }
    

    public void changeCurPageText(int pageIndex) {
        curPage.setText(""+(pageIndex+1));
    }


    public void onStartDrag() {
       addPage.setVisibility(View.GONE);
       deletePage.setVisibility(View.GONE);
       setDefaultPage.setVisibility(View.GONE);
    }
    
    public void onEndDrag()
    {
        addPage.setVisibility(View.VISIBLE);
        deletePage.setVisibility(View.VISIBLE);
        setDefaultPage.setVisibility(View.VISIBLE);
    }

    
    public void onPageChange(int prePageNum, final int curPageNum) {
        final boolean enableDelete = LauncherORM.instance(this).isEnableDeleteDefaultPage();
        handler.post( new Runnable()
        {
            public void run()
            {
                curPage.setText(""+(mGLView.getCurPageIndex()+1));  
                if(curPageNum <=2)
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
    
    public void onPageExchange(int oldPos, int newPos) {
       
        Intent intent = new Intent(Launcher.INTENT_CHANGE_SCREEN);
        int[] inArray = new int[2];
        inArray[0] = oldPos;
        inArray[1] = newPos;
        intent.putExtra(Launcher.BUNDLE_CHANGE_SCREEN_INFO, inArray); 
        sendBroadcast(intent);
    }

    public void removePage(int pageIndex) {
        if(pageIndex > -1 && pageIndex < mGLView.getCount())
        {
            Bitmap bitmap = mPageImageList.get(pageIndex);
            if(bitmap.isRecycled() == false)
            {
                bitmap.recycle();
            }
            mPageImageList.remove(pageIndex);
        }   
        updatePages();
    }

    public void flingToLastScreen() {    
        mGLView.flingToScreen(mGLView.getSelectedItemPosition(), mPageImageList.size()-1);
        mGLView.needFlingToLast = false;
    }
    
//    private static final int MENU_SETTINGS = 1;
//    private static final int MENU_SWITCH_STYLE = MENU_SETTINGS + 1;
//   @Override
//   public boolean onCreateOptionsMenu(Menu menu) {
//       super.onCreateOptionsMenu(menu);      
////     menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
////       .setIcon(R.drawable.cmcc_toolbar_setting).setAlphabeticShortcut('P');
//       menu.add(0,MENU_SWITCH_STYLE,0,R.string.menu_switch_to_3D)
//       .setIcon(R.drawable.switch_3d_view);
//       
//       return true;
//   }
//   
//   @Override
//   public boolean onPrepareOptionsMenu(Menu menu) {
//       super.onPrepareOptionsMenu(menu);
//
//       MenuItem mItem = menu.getItem(0);
//       if(Launcher.isAllowSwitchView())
//       {
//           mItem.setVisible(true);
//       }
//       else
//       {
//           mItem.setVisible(false);
//       }
//       return true;
//  }
//
//@Override
//   public boolean onOptionsItemSelected(MenuItem item) 
//   {
//       switch (item.getItemId()) 
//       {  
//           case MENU_SWITCH_STYLE :
//           {
//               switchStyle();
//               break;
//           }
//       }        
//       return super.onOptionsItemSelected(item);
//   }
//   
//   private void switchStyle() {
//       
//       //switchTo3D
//       LauncherORM.instance(this.getApplicationContext()).Enable2D(false);
//       restartLauncher();
//   }
   
   private void restartLauncher() {
       //backToLauncher(1);
//       Intent i = new Intent(Launcher.INTENT_RELAUNCHE_ACTIVITY);
//       sendBroadcast(i);
//       
//       safeFinish();
       
	   try{                   
           Process.killProcess(Process.myPid());                	              	
       }catch(Exception ne){}
       
       
       //relaunch Launcher
      // ActivityManagerService am = ((ActivityManagerService)ServiceManager.getService("activity"));
   }

   
}
