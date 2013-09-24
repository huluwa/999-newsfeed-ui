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

import com.android.omshome.quickaction.QuickLauncher;
import com.android.omshome.ui.Corpus;
import com.android.omshome.ui.CorpusSelectionDialog;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.os.SystemClock;

public class AllAppsScreenLayout
        extends RelativeLayout
        implements AllAppsView,OnClickListener,OnLongClickListener,CorpusSelectionDialog.OnCorpusSelectedListener,
                   DragSource {

    private static final String TAG = "oms2.5Launcher.AllAppsScreenLayout";

    private Launcher mLauncher;
    private DragController mDragController;

    private AllAppsScreen mAllAppsScreen;
    private ProgressBar mLoadingBar;
    
    private ImageView homeButton;
    private PageIndicatorView pageIndicator;
    public ImageView callButton;
    private ImageView mCategoryDeleteView;
    private TextView  mCategoryView;
    private TextView   mCategoryNameView;
    private PageIndicatorLineStyleView mLinePageIndicator;
    public int currentCategory = 0;
    public boolean isEditModel = false;
    public IndicatorWithMissCallNumberView mMissCallImageView;
    
    private View all_apps_bottom_layout;
    
    public CorpusSelectionDialog mCorpusSelectionDialog;
    private ArrayList<ApplicationInfo> mAllAppsList = new ArrayList<ApplicationInfo>();
    private float mZoom;

    private LayoutInflater mInflater;
    private boolean showListAtTop = true;
    
    public AllAppsScreenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        
       // Category.initialRes(context);
        
        setVisibility(View.GONE);
        setSoundEffectsEnabled(false);

        setFocusableInTouchMode(true);
        CharSequence[] scaleTexts = context.getResources().getTextArray(R.array.resolution);
        Defines.mResolution = scaleTexts[0].toString();
        
        mInflater = LayoutInflater.from(context);
    }

    
    private boolean mFirstLayout = true;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	super.onLayout(changed, left, top, right, bottom);
    	
    	 if (mFirstLayout) {            
             mFirstLayout = false;
             
             if(false){
            	 //post a toast to let user select category
            	 QuickLauncher.popupHint(mLauncher, pageIndicator);    		
            	 mLauncher.mCallStateHandler.postDelayed(new Runnable()
            	 {
            		 public void run()
            		 {
            			 QuickLauncher.dissmissHint();
            		 }
            	 }, 1000);
             }
         }    	
    }
    
    
    public void showHint()
    {
    	if(mFirstLayout == false)
    	{
	    	//post a toast to let user select category
			QuickLauncher.popupHint(mLauncher, pageIndicator);    		
			mLauncher.mCallStateHandler.postDelayed(new Runnable()
			{
				public void run()
				{
					QuickLauncher.dissmissHint();
				}
			}, 1000);
    	}    	
    }

	@Override
    protected void onFinishInflate() {
//        setBackgroundColor(Color.BLACK);
        
        try {
        	pageIndicator = (PageIndicatorView) findViewById(R.id.all_apps_page_indicator);
        	pageIndicator.setOnClickListener(this);

        	callButton = (ImageView) findViewById(R.id.all_apps_call);
        	callButton.setOnClickListener(this);

            mLoadingBar = (ProgressBar)findViewById(R.id.all_apps_load_bar);
            mMissCallImageView = (IndicatorWithMissCallNumberView)findViewById(R.id.all_apps_misscall_count_iv);
            
            mAllAppsScreen = (AllAppsScreen)findViewWithTag("all_apps_screen");
            mAllAppsScreen.setOnLongClickListener(this);
            mAllAppsScreen.setPageIndicator(pageIndicator);
            mAllAppsScreen.setLoadingBar(mLoadingBar); 
            homeButton = (ImageView)findViewWithTag("all_apps_screen_home");
            homeButton.setOnClickListener(this);
            
            mCategoryNameView = (TextView) findViewById(R.id.all_apps_category_name);
            mCategoryView = (TextView)findViewById(R.id.all_apps_category);
            mCategoryDeleteView = (ImageView)findViewById(R.id.all_apps_delete);
            mCategoryDeleteView.setOnClickListener(new View.OnClickListener() {				
				public void onClick(View v) {
					isEditModel = !isEditModel;
					displayAppsByCategory(AllAppsScreenLayout.this.currentCategory, isEditModel);
				}
			});
            
            
            mAllAppsScreen.setCategoryView(this.findViewById(R.id.all_apps_page_indicator));            
            mCategoryView.setOnClickListener(new View.OnClickListener() {			
    			public void onClick(View v) {
    				if(mLoadingBar.getVisibility() != View.VISIBLE){
    					showCorpusSelectionDialog();
    				}else{
    					dismissCorpusSelectionDialog();
    				}
    			}
    	   });           
           mLinePageIndicator =  (PageIndicatorLineStyleView) findViewById(R.id.all_apps_page_line_indicator);
           mAllAppsScreen.setPageIndicatorLineStyleView(mLinePageIndicator);
           isEditModel = false;
           
           all_apps_bottom_layout = findViewById(R.id.all_apps_bottom_layout); 
           
           showListAtTop = true;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find necessary layout elements for AllAppsScreenLayout");
        }

    }

    protected void showCorpusSelectionDialog() {
        if (mCorpusSelectionDialog == null) {
            mCorpusSelectionDialog = new CorpusSelectionDialog(getContext());
            mCorpusSelectionDialog.setOwnerActivity(mLauncher);
            mCorpusSelectionDialog.setOnCorpusSelectedListener(this);
        }
        
        mCorpusSelectionDialog.show();
    }

    protected boolean isCorpusSelectionDialogShowing() {
        return mCorpusSelectionDialog != null && mCorpusSelectionDialog.isShowing();
    }

    public void dismissCorpusSelectionDialog() {
        if (mCorpusSelectionDialog != null) {
            mCorpusSelectionDialog.dismiss();
        }
    }
    
    public AllAppsScreenLayout(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
        if(mAllAppsScreen != null)mAllAppsScreen.setLauncher(launcher);
    }

    @Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		if(Launcher.LOGD)Log.d(TAG, "onFocusChanged  gainFocus:"+gainFocus+"  direction:"+direction+" previouslyFocusedRect:"+previouslyFocusedRect);
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

    public void setDragController(DragController dragger) {
        mDragController = dragger;
    }

    public void onDropCompleted(View target, boolean success) {
    }

    public void zoom(float zoom, boolean animate) {
        if(Launcher.LOGD)Log.d(TAG, "zooming " + ((zoom == 1.0) ? "open" : "closed")+"  isEditModel:"+isEditModel);
        cancelLongPress();

        mZoom = zoom;
        animate = false;
        
        dismissCorpusSelectionDialog();

        if (isVisible()) {
       	
            getParent().bringChildToFront(this);
            if(isEditModel)
            {
            	displayAppsByCategory(currentCategory , false);
            }
            setVisibility(View.VISIBLE);
            if (animate) {
                startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.all_apps_2d_fade_in));
            } else {
                onAnimationEnd();
            }
        } else {
            if (animate) {
                startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.all_apps_2d_fade_out));
            } else {
                onAnimationEnd();
            }
        }
    }

    protected void onAnimationEnd() {
        if (!isVisible()) {
            setVisibility(View.GONE);
            mZoom = 0.0f;
            mLoadingBar.setVisibility(View.GONE);
        } else {
            mZoom = 1.0f;
            if(mLauncher.isAllAppsLoading()){
            	mLoadingBar.setVisibility(View.VISIBLE);
            }
            
            if(isNeedBackToAll){
            	isNeedBackToAll = false;
            	currentCategory = Category.CATEGORY_ALLAPP;
            	isEditModel = false;
            	refreshAllAppsUI();
            }
        }
        mLauncher.zoomed(mZoom);
    }

    public boolean isVisible() {
        return mZoom > 0.001f;
    }

    @Override
    public boolean isOpaque() {
        return mZoom > 0.999f;
    }

    public void setApps(ArrayList<ApplicationInfo> list) {
    	Category.initApplicationData(mLauncher, list);
    	if(mCorpusSelectionDialog != null)mCorpusSelectionDialog.refreshCorpus();
    	displayAppsByCategory(Category.CATEGORY_ALLAPP, false);
        mLoadingBar.setVisibility(View.GONE);
    }
    
    public ArrayList<ApplicationInfo> getApps(){
    	return mAllAppsList;
    }

    public void addAppsWithoutSort(ArrayList<ApplicationInfo> list) {
    	if(Launcher.LOGD)Log.d(TAG, "addAppsWithoutSort: "+list.size());
    	addApps(list);
    }
    
    public void addApps(ArrayList<ApplicationInfo> list) {
        final int N = list.size();
        
        if(N==0)
        	return;

        boolean isNeedFreshUI = Category.addApps(list, currentCategory);
        if(mCorpusSelectionDialog != null)mCorpusSelectionDialog.refreshCorpus();
        
        //need refresh ui
        if(currentCategory == Category.CATEGORY_ALLAPP || isNeedFreshUI || (isEditModel && currentCategory == Category.CATEGORY_3RDAPP)){
        	displayAppsByCategory(currentCategory, isEditModel);
        }
    }
    
    public void refreshAllAppsUI(){
    	displayAppsByCategory(currentCategory, isEditModel);
    }
    
    boolean isNeedBackToAll = false;
    public void refreshAllAppsUI(int category, boolean isEdit){
    	displayAppsByCategory(category, isEdit);
    	isNeedBackToAll = true;
    }
    
    private void displayAppsByCategory(int category, boolean editFlag) {
    	if(Launcher.LOGD)Log.d(TAG, "displayAppsByCategory category:"+category+" editFlag:"+editFlag);

    	currentCategory = category;
    	isEditModel = editFlag;
    	mCategoryNameView.setText(Category.getCategoryNameByCategory(currentCategory));

    	refreshAllAppsList();
    	
    	if(isEditModel || ((currentCategory >= Category.CATEGORY_Entertainment && currentCategory <= Category.CATEGORY_SHORTCUT) &&  Category.getAppsByCategory(currentCategory).size() == 0)){
    		refreshEditApps();
    	}else{
    		refreshApps();
    	}
    }
    
    public boolean isNeedRefresh = false;
    private void refreshApps(){
    	if(!mLauncher.amVisible){
    		if(Launcher.LOGD)Log.d(TAG, "Launcher is on the back, refreshAppsWithCategory later");
    		isNeedRefresh = true;
    		return ;
    	}else{
    		isNeedRefresh = false; 
    	}
    	
    	if(isCorpusSelectionDialogShowing()){
    		dismissCorpusSelectionDialog();
    	}
    	
    	final int appsCount = mAllAppsList.size();
    	if((currentCategory == Category.CATEGORY_3RDAPP && appsCount>0) || (currentCategory >= Category.CATEGORY_Entertainment && currentCategory <= Category.CATEGORY_CUSTOMIZE_MAX)){
    		mCategoryDeleteView.setVisibility(View.VISIBLE);
    	}else{
    		mCategoryDeleteView.setVisibility(View.GONE);
    	}
    	
    	final int screenCount = mAllAppsScreen.getChildCount();
		for(int index=0; index<screenCount; index++){
			CellLayout cell = (CellLayout)mAllAppsScreen.getChildAt(index);
			cell.removeAllViews();
		}
		
		//avoid blank when install or uninstall
		mAllAppsScreen.clearChildrenCache();
		mAllAppsScreen.setBackgroundResource(R.drawable.allapp_bg);
		
		mAllAppsScreen.removeAllViews();
		
		int cellCount = 0;
		if(mAllAppsList == null || mAllAppsList.size() <= 0){
			cellCount = 1;
		}else{
			cellCount = appsCount/16;
			if(cellCount == 0){ 
				cellCount = 1;
			}else if(appsCount%16 != 0){
				cellCount ++ ;
			}
		}
		
		mAllAppsScreen.initAllAppsScreenCellLayout(cellCount);
		if(Launcher.LOGD)Log.d(TAG, "refreshAppsWithCategory appsCount:"+appsCount+" re-create view count:"+cellCount);

    	for(int i=0; i<appsCount; i++){
    		final TextView textView = (TextView) mInflater.inflate(R.layout.application_screen_used, (ViewGroup)mAllAppsScreen.getChildAt(i/16), false);
    		
    		ApplicationInfo info = (ApplicationInfo)mAllAppsList.get(i);
    		info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);//b055, need check scale
    		
    		textView.setCompoundDrawablesWithIntrinsicBounds(null, Utilities.createAllAppIconWithBg(info.iconBitmap, mLauncher), null,null);
    		textView.setText(info.title);
    		textView.setTag(info);
    		textView.setOnClickListener(this);
    		textView.setFocusableInTouchMode(false);
    		if(mAllAppsScreen.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
    			mAllAppsScreen.addInScreen(textView, i/15, (i%15)%5, (i%15)/5, 1, 1);
    		}else{
    			mAllAppsScreen.addInScreen(textView, i/16, (i%16)%4, (i%16)/4, 1, 1);
    		}
    	}
    	
    	//no need again
    	mAllAppsScreen.setBackgroundDrawable(null);
    	mAllAppsScreen.snapToScreen(0);
    }

    private void refreshEditApps(){
    	if(!mLauncher.amVisible){
    		if(Launcher.LOGD)Log.d(TAG, "Launcher is on the back, refreshEditAppsWithCategory later");
    		isNeedRefresh = true;
    		return ;
    	}else{
    		isNeedRefresh = false; 
    	}
    	
    	if((currentCategory >= Category.CATEGORY_Entertainment && currentCategory <= Category.CATEGORY_CUSTOMIZE_MAX) && mAllAppsList.size() == 0){
    		mCategoryDeleteView.setVisibility(View.GONE);
    	}else{
    		mCategoryDeleteView.setVisibility(View.VISIBLE);
    	}
    	isEditModel = true;
    	
    	if(isCorpusSelectionDialogShowing()){
    		dismissCorpusSelectionDialog();
    	}
    	
    	final int screenCount = mAllAppsScreen.getChildCount();
		for(int index=0; index<screenCount; index++){
			CellLayout cell = (CellLayout)mAllAppsScreen.getChildAt(index);
			cell.removeAllViews();
		}
		
		mAllAppsScreen.removeAllViews();
		final int appsCount = mAllAppsList.size();
		int cellCount = 0;
		if(appsCount <= 0){
			cellCount = 1;
		}else{
			if(currentCategory == Category.CATEGORY_3RDAPP){
				cellCount = appsCount/16;
				if(cellCount == 0){ 
					cellCount = 1;
				}else if(appsCount%16 != 0){
					cellCount ++ ;
				}
			}else{ // currentCategory == Category.CATEGORY_SHORTCUT
				if(appsCount < Category.allapps.size()){
					cellCount = (appsCount+1)/16;
					if(cellCount == 0){ 
						cellCount = 1;
					}else if((appsCount+1)%16 != 0){
						cellCount ++ ;
					}
				}else{
					cellCount = appsCount/16;
					if(cellCount == 0){ 
						cellCount = 1;
					}else if(appsCount%16 != 0){
						cellCount ++ ;
					}
				}
			}
		}
		
		mAllAppsScreen.initAllAppsScreenCellLayout(cellCount);
		if(Launcher.LOGD)Log.d(TAG, "refreshEditAppsWithCategory appsCount:"+appsCount+" re-create view count:"+cellCount);

    	for(int i=0; i<appsCount; i++){
    		final ApplicationInfo info = (ApplicationInfo)mAllAppsList.get(i);
    		info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);
    		
    		RelativeLayout layout = (RelativeLayout)mInflater.inflate(R.layout.application_boxed_check,  (ViewGroup)mAllAppsScreen.getChildAt(i/16), false);
    		final TextView textView = (TextView) layout.findViewById(R.id.name);
    		textView.setTag(info);
    		textView.setOnClickListener(this);
	        textView.setCompoundDrawablesWithIntrinsicBounds(null, Utilities.createAllAppIconWithBg(info.iconBitmap, mLauncher), null, null);
	        textView.setText(info.title);
    		
	        if(mAllAppsScreen.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
    			mAllAppsScreen.addInScreen(layout, i/15, (i%15)%5, (i%15)/5, 1, 1);
    		}else{
    			mAllAppsScreen.addInScreen(layout, i/16, (i%16)%4, (i%16)/4, 1, 1);
    		}
    	}
    	
    	if((currentCategory >Category.CATEGORY_CUSTOMIZE && currentCategory <= Category.CATEGORY_CUSTOMIZE_MAX) && (appsCount < Category.allapps.size() || appsCount == 0)){
    		if(mAllAppsScreen.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
    			final TextView addShortcutIcon = (TextView) mInflater.inflate(R.layout.application_screen_used, (ViewGroup)mAllAppsScreen.getChildAt(appsCount/15), false); //TODO
    			addShortcutIcon.setCompoundDrawablesWithIntrinsicBounds(null, mLauncher.getResources().getDrawable(R.drawable.ic_allapp_add_shortcut), null, null);
    			addShortcutIcon.setTag(currentCategory);
    			addShortcutIcon.setOnClickListener(this);
    			mAllAppsScreen.addInScreen(addShortcutIcon, appsCount/15, (appsCount%15)%5, (appsCount%15)/5, 1, 1);
    		}else{
    			final TextView addShortcutIcon = (TextView) mInflater.inflate(R.layout.application_screen_used, (ViewGroup)mAllAppsScreen.getChildAt(appsCount/16), false); //TODO
    			addShortcutIcon.setCompoundDrawablesWithIntrinsicBounds(null, mLauncher.getResources().getDrawable(R.drawable.ic_allapp_add_shortcut), null, null);
    			addShortcutIcon.setTag(currentCategory);
    			addShortcutIcon.setOnClickListener(this);
    			mAllAppsScreen.addInScreen(addShortcutIcon, appsCount/16, (appsCount%16)%4, (appsCount%16)/4, 1, 1);
    		}
    	}
    	
    	mAllAppsScreen.snapToScreen(mAllAppsScreen.mCurrentScreen);
    }

    public void removeApps(ArrayList<ApplicationInfo> list) {
        final int N = list.size();
        
        if(N==0)
        	return;
        
        boolean isNeedFreshUI = Category.removeApps(list, currentCategory);
        if(mCorpusSelectionDialog != null)mCorpusSelectionDialog.refreshCorpus();
        
        //need refresh ui
        if(currentCategory == Category.CATEGORY_ALLAPP || isNeedFreshUI || isEditModel){
        	displayAppsByCategory(currentCategory, isEditModel);
        }
    }
    
    public void updateApps(ArrayList<ApplicationInfo> list) {
    	 final int N = list.size();
    	 if(Launcher.LOGD)Log.d(TAG, "updateAppsWithCategory:"+N);
         
         if(N==0)
         	return;
         
        Category.removeApps(list, currentCategory);
        addApps(list);
    }

    public void dumpState() {
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList", mAllAppsList);
    }
    
    public void surrender() {
    }
	public void setFocusable(boolean focusable) {
		 mAllAppsScreen.setFocusable(focusable);
	}
	
	private boolean forPicker = false;
	public void setStartForPicker(boolean fPicker)
	{
	    forPicker = fPicker;
	}
	
	public void onClick(View v) {
		if(Launcher.LOGD)Log.d(TAG, "onClick v:"+v+"  isEditModel:"+isEditModel);
    	final int vId = v.getId();
        Object tag = v.getTag();
        if (tag instanceof ApplicationInfo) {
        	final Intent intent = ((ApplicationInfo) tag).intent;
        	if(isEditModel){
        		if(currentCategory >= Category.CATEGORY_Entertainment && currentCategory <= Category.CATEGORY_CUSTOMIZE_MAX){
        			confirmDeleteShortCut((ApplicationInfo) tag);
        		}else{
        			Uri packageURI = Uri.parse("package:"+((ApplicationInfo) tag).componentName.getPackageName());
        			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                   // temporaryDisableInvalidate();//b055
                   // mLauncher.forceInvalidView(v);
        			mLauncher.startActivity(uninstallIntent);
        		}
        	}else{
        		int[] pos = new int[2];
        		v.getLocationOnScreen(pos);
        		intent.setSourceBounds(new Rect(pos[0], pos[1],
        				pos[0] + v.getWidth(), pos[1] + v.getHeight()));
                //b055
        		//v.setSelected(false);
        		//Log.d(TAG, "click v getFocus:"+v.findFocus()+"  isSelected:"+v.isSelected());
              //  temporaryDisableInvalidate();//b055
             //   mLauncher.forceInvalidView(v);
        		startActivitySafely(intent, tag);
        		Category.addRecentUseApp((ApplicationInfo) tag);
        	}
        }else if(vId == R.id.all_apps_screen_home){
        	mLauncher.closeAllApps(true);
        }else if(vId == R.id.all_apps_call){
        	//mLauncher.closeAllApps(true);
        	mLauncher.startCall(v);
        }else if(vId == R.id.all_apps_page_indicator){
        	if(showListAtTop == false)
        	{
	        	if(mLoadingBar.getVisibility() != View.VISIBLE){
					showCorpusSelectionDialog();
				}else{
					dismissCorpusSelectionDialog();
				}
        	}
        	else
        	{
        	    mAllAppsScreen.snapToScreen(mAllAppsScreen.mCurrentScreen==(mAllAppsScreen.getChildCount()-1)?0:(mAllAppsScreen.mCurrentScreen+1));
        	}
        }else if(tag instanceof Integer && (((Integer)tag).intValue() > Category.CATEGORY_CUSTOMIZE && ((Integer)tag).intValue() <= Category.CATEGORY_CUSTOMIZE_MAX)){
        	 AllAppsListDialog aalDialog = new AllAppsListDialog(mLauncher, true, (Integer)tag);
        	 aalDialog.setShortcutLayout(this);
             aalDialog.createDialog(Category.getAppsByCategory(Category.CATEGORY_ALLAPP)).show();
        }
	}
	
	private void refreshAllAppsList(){
		switch(currentCategory){
		case Category.CATEGORY_SYSTEMAPP:
			mAllAppsList = Category.systemapps;
			break;
		case Category.CATEGORY_CARRIERAPP:
			mAllAppsList = Category.carrierapps;
			break;
		case Category.CATEGORY_OEMAPP:
			mAllAppsList = Category.oemapps;
			break;
		case Category.CATEGORY_3RDAPP:
			mAllAppsList = Category.the3rdapps;
			break;
		case Category.CATEGORY_SHORTCUT:
			mAllAppsList = Category.shortcutapps;
			break;
	    case Category.CATEGORY_Entertainment:
				mAllAppsList = Category.entertainmentapps;
		    break;
	    case Category.CATEGORY_Information:
			mAllAppsList = Category.informationapps;
	        break;
	    case Category.CATEGORY_Tools:
			mAllAppsList = Category.toolsapps;
	        break;
	    case Category.CATEGORY_GAME:
			mAllAppsList = Category.gameapps;
	        break;
		default:
			mAllAppsList = Category.allapps;
		}
	}
	
	private void confirmDeleteShortCut(final ApplicationInfo app)
	{
		AlertDialog dialog =  new AlertDialog.Builder(mLauncher)
		.setIcon(R.drawable.cmcc_dialog_question2)
		.setTitle(R.string.remove_application_shortcut)
		.setPositiveButton(R.string.remove_application_shortcut_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
				Category.removeApplicationCategory(AllAppsScreenLayout.this.mLauncher, currentCategory,  app);
				refreshAllAppsList();
				AllAppsScreenLayout.this.refreshEditApps();
			}
		})
		.setNegativeButton(R.string.remove_application_shortcut_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		}).create();
		
		dialog.show();
 	}
	
	public boolean onLongClick(View view) {
		Log.d(TAG,"entering onLongClick==="+"forPicker="+forPicker+"===view isTouchMode="+view.isInTouchMode());
        if (!view.isInTouchMode()) {
            return false;
        }
        
        if(mLauncher != null){
        	mLauncher.closeLiveFolder();
        }
        
        Object tag = view.getTag();
        if (tag instanceof ApplicationInfo && isEditModel == false) {
        	mDragController.startDrag(view, this, tag, DragController.DRAG_ACTION_COPY);
        	mLauncher.closeAllApps(true);
        }

        return true;
	}
	
	void startActivitySafely(Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        } catch (SecurityException e) {
            Toast.makeText(getContext(), R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
    }
	
	boolean isbackkeyInCurrentView = false;
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(Launcher.LOGD)Log.d(TAG, "dispatchKeyEvent event:"+event + " currentFocusView="+mLauncher.getWindow().getCurrentFocus());
		
        //bact to category_the3rd model when in edit model.
        if(isEditModel && event.getAction() == KeyEvent.ACTION_UP && 
        		event.getKeyCode() == KeyEvent.KEYCODE_BACK && 
        		!(currentCategory > Category.CATEGORY_CUSTOMIZE  && Category.getAppsByCategory(currentCategory).size() == 0)){
        	isEditModel = false;
        	displayAppsByCategory(currentCategory, isEditModel);
        	return true;
        }
        
        //this is to process the useless back up event when back from other activity        
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
        	isbackkeyInCurrentView = true;
        	return super.dispatchKeyEvent(event);
        }
        else
        {
        	if(isbackkeyInCurrentView == true && 
        	   event.getAction() == KeyEvent.ACTION_UP && 
        	   event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        	{
        		mLauncher.closeAllApps(true);
        		isbackkeyInCurrentView = false; 
        		return true;
        	}        	
        	isbackkeyInCurrentView = false; 
        }
        	
		View currentFocusView = mLauncher.getWindow().getCurrentFocus();
		if(currentFocusView instanceof AllAppsScreenLayout)
		{
			if (event.getAction() == KeyEvent.ACTION_UP) 
			{
	           	switch (event.getKeyCode()) 
	           	{
	            	case KeyEvent.KEYCODE_DPAD_DOWN:
	            	case KeyEvent.KEYCODE_DPAD_UP:
	            	case KeyEvent.KEYCODE_DPAD_LEFT:
	            	case KeyEvent.KEYCODE_DPAD_RIGHT:
	            	{
	            		View view = mAllAppsScreen.getChildAt(mAllAppsScreen.mCurrentScreen);
	            		
	            		if(view == null){
	            			Log.d(TAG, "no child in all app view, need dispatch key to other region");
	            			return super.dispatchKeyEvent(event);
	            		}
	            		
	        			view.requestFocus(View.FOCUS_DOWN);
	        			return true;
	            	}
	            	case KeyEvent.KEYCODE_BACK:
	            	{
	            		mLauncher.closeAllApps(true);
	            		return true;
	            	}
	            	default:
	            		return super.dispatchKeyEvent(event);
	            }
            }
			else if (event.getAction() == KeyEvent.ACTION_DOWN) 
			{
				switch (event.getKeyCode()) 
	           	{
	            	case KeyEvent.KEYCODE_DPAD_DOWN:
	            	case KeyEvent.KEYCODE_DPAD_UP:
	            	case KeyEvent.KEYCODE_DPAD_LEFT:
	            	case KeyEvent.KEYCODE_DPAD_RIGHT:
	            	case KeyEvent.KEYCODE_BACK:
	            	{	     
	            		//process no application (include no "+" )case, need pass the key to other region,
	            		//other case eat the event
	            		if(mAllAppsScreen.getChildCount()==1)
	            		{
	            			ViewGroup viewp = (ViewGroup)mAllAppsScreen.getChildAt(mAllAppsScreen.mCurrentScreen);
	            			if(viewp != null && viewp.getChildCount() == 0)
	            			{
	            				Log.d(TAG, "no child in all app view, need dispatch key to other region");
	            				all_apps_bottom_layout.requestFocus();
	            				//return super.dispatchKeyEvent(event);
	            			}		            		
	            		}
	            		return true;
	            	}
	            	default:
	            		return super.dispatchKeyEvent(event);
	           	}
			}
			else
			{
				return super.dispatchKeyEvent(event);
			}
				
			
	    }else if(!currentFocusView.isSelected() && (currentFocusView instanceof TextView)){
	    	//control return from other activity
    	    if(event.getAction() == KeyEvent.ACTION_UP && 
         	   event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
         	   isbackkeyInCurrentView == false)
	        {
	    		return super.dispatchKeyEvent(event);
	        }
    	    
    	    //TODO need merge with the bellow code
    	    switch (event.getKeyCode()) 
           	{
            	case KeyEvent.KEYCODE_DPAD_DOWN:
            	case KeyEvent.KEYCODE_DPAD_UP:
            	case KeyEvent.KEYCODE_DPAD_LEFT:
            	case KeyEvent.KEYCODE_DPAD_CENTER:
            	case KeyEvent.KEYCODE_DPAD_RIGHT:            
            		currentFocusView.setSelected(true);
        	    	return true; 
            	default:
            		return super.dispatchKeyEvent(event);
           	}
	    }
		
		boolean ret = super.dispatchKeyEvent(event);
		
		if(event.getAction() == KeyEvent.ACTION_UP){
			View newFocusView = mLauncher.getWindow().getCurrentFocus();
			if(newFocusView instanceof TextView){
				switch (event.getKeyCode()) {
            	case KeyEvent.KEYCODE_DPAD_DOWN:
            	case KeyEvent.KEYCODE_DPAD_UP:
            	case KeyEvent.KEYCODE_DPAD_LEFT:
            	case KeyEvent.KEYCODE_DPAD_RIGHT:
            	case KeyEvent.KEYCODE_DPAD_CENTER:
            		newFocusView.setSelected(true);
            		break;            	
                default:
            	}
			}
		}
		
		return ret;
	}

	public void onCorpusSelected(Corpus corpus) {
		if(Launcher.LOGD)Log.d(TAG, "onCorpusSelected category:"+corpus.category);
		if(currentCategory == corpus.category){
			return;
		}
		
		displayAppsByCategory(corpus.category, false);
	}

	private void temporaryDisableInvalidate()
    {
    }
}
