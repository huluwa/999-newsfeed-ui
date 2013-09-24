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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;

import com.borqs.omshome25.ui.Corpus;
import com.borqs.omshome25.ui.CorpusSelectionDialog;

public class AllApps2DWithCategory extends RelativeLayout implements
		AllAppsView, AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, View.OnKeyListener,
		OnClickListener, CorpusSelectionDialog.OnCorpusSelectedListener,
		DragSource {

	private static final String TAG = "oms2.5Launcher.AllApps2DWithCategory";
	private static final boolean DEBUG = false;

	private Launcher mLauncher;
	private DragController mDragController;

	private GridView mGrid;
	private ProgressBar mLoadingBar;

	private ArrayList<ApplicationInfo> mAllAppsList = new ArrayList<ApplicationInfo>();

	// preserve compatibility with 3D all apps:
	// 0.0 -> hidden
	// 1.0 -> shown and opaque
	// intermediate values -> partially shown & partially opaque
	private float mZoom;

	private AppsAdapter mAppsAdapter;

	private ImageView mCategoryDeleteView;
	private ImageView mCategoryAddView, mCategorySearch;
	private ImageView mCategoryView;
	private TextView mCategoryNameView;
	private TextView mNoAppTipsView;
	private EditText id_search_key;
	public CorpusSelectionDialog mCorpusSelectionDialog;
	private View mTopBarView;
	public int currentCategory = 0;
	public boolean isEditModel = false;
	private ImageButton homeButton = null;

	private MyWatcher watcher;
	// ------------------------------------------------------------

	public static class HomeButton extends ImageButton {
		public HomeButton(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public View focusSearch(int direction) {
			if (direction == FOCUS_UP)
				return super.focusSearch(direction);
			return null;
		}
	}

	public class AppsAdapter extends ArrayAdapter<ApplicationInfo> {
		private final LayoutInflater mInflater;

		public AppsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
			super(context, 0, apps);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo info = getItem(position);

			if (!isEditModel) {
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.application_screen_used, parent, false);
				}

				final TextView textView = (TextView) convertView;
				info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);

				textView.setText(info.title);
				textView.setTag(info);
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						new BitmapDrawable(info.iconBitmap), null, null);
			} else {
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.application_boxed_check, parent, false);
				}

				final RelativeLayout layout = (RelativeLayout) convertView;
				final TextView textView = (TextView) layout
						.findViewById(R.id.name);

				// if(position == (mAllAppsList.size()-1) &&
				// (mAllAppsList.size() <
				// Category.getAppsByCategory(Category.CATEGORY_ALLAPP).size())){
				// textView.setCompoundDrawablesWithIntrinsicBounds(null,
				// mLauncher.getResources().getDrawable(R.drawable.ic_allapp_add_shortcut),
				// null, null);
				// textView.setTag(currentCategory);
				// textView.setOnClickListener(new OnClickListener(){
				// public void onClick(View v) {
				// AllAppsListDialog aalDialog = new
				// AllAppsListDialog(mLauncher, true, (Integer)v.getTag());
				// aalDialog.createDialog(Category.getAppsByCategory(Category.CATEGORY_ALLAPP)).show();
				// }
				// });
				//            		
				// ImageView delImageView =
				// (ImageView)layout.findViewById(R.id.check);
				// delImageView.setVisibility(View.GONE);
				// }else{
				layout.setTag(info);
				info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);
				// textView.setOnClickListener(new OnClickListener(){
				// public void onClick(View v) {
				// Object tag = v.getTag();
				// if(currentCategory >= Category.CATEGORY_Entertainment &&
				// currentCategory <= Category.CATEGORY_SHORTCUT){
				// confirmDeleteShortCut((ApplicationInfo) tag);
				// }else{
				// Uri packageURI = Uri.parse("package:"+((ApplicationInfo)
				// tag).componentName.getPackageName());
				// Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
				// packageURI);
				// mLauncher.startActivity(uninstallIntent);
				// }
				// }
				// });
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						new BitmapDrawable(info.iconBitmap), null, null);
				textView.setText(info.title);
				// }
			}

			return convertView;
		}
	}

	public AllApps2DWithCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSoundEffectsEnabled(false);
		// Category.initialRes(context);
		mAppsAdapter = new AppsAdapter(getContext(), mAllAppsList);
		mAppsAdapter.setNotifyOnChange(false);
	}

	@Override
	protected void onFinishInflate() {
		setBackgroundColor(Color.BLACK);
		isEditModel = false;
		setOnKeyListener(this);
	}

	private void delayInitView() {
		try {
			mGrid = (GridView) findViewWithTag("all_apps_2d_grid");
			
			if (mGrid == null) {
				Log.e(TAG, "Resource not found, mGrid is null! ");
				return;
				// throw new Resources.NotFoundException();
			}
			
			final Resources res = getResources();
			mGrid.setLongClickable(true);
			mGrid.setOnItemClickListener(this);
			mGrid.setOnItemLongClickListener(this);
			mGrid.setBackgroundColor(Color.BLACK);
			mGrid.setCacheColorHint(Color.BLACK);
			mLoadingBar = (ProgressBar)findViewById(R.id.all_apps_load_bar);
			mLoadingBar.setIndeterminateDrawable(res.getDrawable(com.android.internal.R.drawable.progress_medium_white));
			
			mTopBarView = findViewById(R.id.all_apps_top_layout);
			mTopBarView.setBackgroundResource(R.drawable.oms_actionbar_background);

			mNoAppTipsView = (TextView) findViewById(R.id.all_apps_noapp_tips);
			mCategoryNameView = (TextView) findViewById(R.id.all_apps_category_name);
		
			mCategoryView = (ImageView) findViewById(R.id.corpus_indicator);
			mCategoryView.setBackgroundResource(R.drawable.corpus_indicator_bg);
			mCategoryView.setImageDrawable(res.getDrawable(R.drawable.cmcc_launcher_home_icon_all_application));
			
			mCategoryDeleteView = (ImageView) findViewById(R.id.all_apps_delete);
			mCategoryDeleteView.setBackgroundResource(R.drawable.actionbar_button_new_del);
			mCategoryDeleteView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					isEditModel = !isEditModel;
					displayAppsByCategory(currentCategory, isEditModel);
				}
			});

			id_search_key = (EditText)findViewById(R.id.id_search_key);
                        id_search_key.setHint(getContext().getText(R.string.group_search));
		        watcher = new MyWatcher();         
                        id_search_key.addTextChangedListener(watcher);
		
                        mCategorySearch  = (ImageView) findViewById(R.id.all_apps_search);
                        mCategorySearch.setBackgroundResource(R.drawable.actionbar_search);
                        mCategorySearch.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                id_search_key.setVisibility(View.VISIBLE);
                                id_search_key.requestFocus();
                                mCategorySearch.setVisibility(View.GONE);
                                mCategoryNameView.setVisibility(View.GONE);
                                
                                InputMethodManager inputManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.showSoftInput(id_search_key, 0);
                            }
                        });

	
			mCategoryAddView = (ImageView) findViewById(R.id.all_apps_edit_add);
			mCategoryAddView.setBackgroundResource(R.drawable.ic_allapp_add_shortcut);
			mCategoryAddView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AllAppsListDialog aalDialog = new AllAppsListDialog(
							mLauncher, true, currentCategory);
					aalDialog.setShortcutLayout(AllApps2DWithCategory.this);
					aalDialog.createDialog(Category.getAppsByCategory(Category.CATEGORY_ALLAPP)).show();
				}
			});

			// TODO
			// mAllAppsScreen.setCategoryView(this.findViewById(R.id.all_apps_page_indicator));
			mCategoryView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (mLoadingBar.getVisibility() != View.VISIBLE) {
						showCorpusSelectionDialog();
					} else {
						dismissCorpusSelectionDialog();
					}
				}
			});
			
			homeButton = (ImageButton) findViewWithTag("all_apps_2d_home");
			if (homeButton == null)
				throw new Resources.NotFoundException();
			homeButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mLauncher.closeAllApps(true);
				}
			});
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Can't find necessary layout elements for AllApps2D");
		}

	}

	public AllApps2DWithCategory(Context context, AttributeSet attrs,
			int defStyle) {
		this(context, attrs);
	}

	public void refreshAllAppsUI() {
		displayAppsByCategory(currentCategory, isEditModel);
	}

	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// if(Launcher.LOGD)Log.d(TAG,
		// "onKey v:"+v+"  keyCode:"+keyCode+"  event:"+event);
		if (!isVisible())
			return false;

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// if(Launcher.LOGD)Log.d(TAG,
			// "onKey  KEYCODE_BACK   isEditModel:"+isEditModel);
			if (isEditModel && currentCategory != Category.CATEGORY_SHORTCUT) {
				isEditModel = false;
				refreshAllAppsUI();
			} else {
				mLauncher.closeAllApps(true);
			}
			break;
		default:
			return false;
		}

		return true;
	}

	public void onItemClick(AdapterView parent, View v, int position, long id) {
		// if(Launcher.LOGD)Log.d(TAG,
		// "onItemClick  View  isEditModel:"+isEditModel);

		if (isEditModel) {
			Object tag = v.getTag();
			if (currentCategory >= Category.CATEGORY_Entertainment
					&& currentCategory <= Category.CATEGORY_CUSTOMIZE_MAX) {
				confirmDeleteShortCut((ApplicationInfo) tag);
			} else {
				Uri packageURI = Uri.parse("package:"
						+ ((ApplicationInfo) tag).componentName
								.getPackageName());
				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
						packageURI);
				mLauncher.startActivity(uninstallIntent);
			}
		} else {
			ApplicationInfo app = (ApplicationInfo) parent
					.getItemAtPosition(position);
			mLauncher.startActivitySafely(app.intent, app);
		}
	}

	private void confirmDeleteShortCut(final ApplicationInfo app) {
		AlertDialog dialog = new AlertDialog.Builder(mLauncher).setIcon(
				R.drawable.cmcc_dialog_question2).setTitle(
				R.string.remove_application_shortcut).setPositiveButton(
				R.string.remove_application_shortcut_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Category.removeApplicationCategory(mLauncher,
								currentCategory, app);
						refreshAllAppsList();
						AllApps2DWithCategory.this.refreshEditApps();
					}
				}).setNegativeButton(
				R.string.remove_application_shortcut_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();

		dialog.show();
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// if(Launcher.LOGD)Log.d(TAG,"onItemLongClick view:"+view);

		if (!view.isInTouchMode()) {
			return false;
		}

		if (mLauncher != null) {
			mLauncher.closeLiveFolder();
		}

		ApplicationInfo app = (ApplicationInfo) parent
				.getItemAtPosition(position);
		app = new ApplicationInfo(app);

		mDragController.startDrag(view, this, app,
				DragController.DRAG_ACTION_COPY);
		mLauncher.closeAllApps(true);

		return true;
	}

	protected void onFocusChanged(boolean gainFocus, int direction,
			android.graphics.Rect prev) {
		if (gainFocus) {
			mGrid.requestFocus();
		}
	}

	public void setDragController(DragController dragger) {
		mDragController = dragger;
	}

	public void onDropCompleted(View target, boolean success) {
	}

	public void zoom(float zoom, boolean animate) {
		cancelLongPress();

		mZoom = zoom;
		// animate = false;

		if (isVisible()) {
			if(mGrid == null){
				delayInitView();
			}
			
			if (homeButton.getDrawable() == null) {
				homeButton.setImageResource(R.drawable.oms_all_apps_button);
			}

			getParent().bringChildToFront(this);
			setVisibility(View.VISIBLE);
			mGrid.setAdapter(mAppsAdapter);
			if (animate) {
				startAnimation(AnimationUtils.loadAnimation(getContext(),
						R.anim.all_apps_2d_fade_in));
			} else {
				onAnimationEnd();
			}
		} else {
			if (animate) {
				startAnimation(AnimationUtils.loadAnimation(getContext(),
						R.anim.all_apps_2d_fade_out));
			} else {
				onAnimationEnd();
			}
		}
		
		dismissCorpusSelectionDialog();
	}

	protected void onAnimationEnd() {
		if (!isVisible()) {
			setVisibility(View.GONE);
			if (isEditModel) {
				isEditModel = false;

				if (currentCategory == Category.CATEGORY_SHORTCUT) {
					currentCategory = Category.CATEGORY_ALLAPP;
					mCategoryView.setImageDrawable(getResources().getDrawable(R.drawable.cmcc_launcher_home_icon_all_application));
				}

				refreshAllAppsUI();
			}
			// mGrid.setAdapter(null);
			mZoom = 0.0f;
			mLoadingBar.setVisibility(View.GONE);
                        if(currentCategory == Category.CATEGORY_ALLAPP)
                        {
                            id_search_key.setVisibility(View.GONE);
                            mCategorySearch.setVisibility(View.VISIBLE);
                            mCategoryNameView.setVisibility(View.VISIBLE);
                        }

		} else {
			mZoom = 1.0f;
			if (mLauncher.isAllAppsLoading()) {
				mLoadingBar.setVisibility(View.VISIBLE);
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
		Log.d(TAG, "setApps  list:" + list.size());
		if (mLauncher.isNeedAllAppsSort) {
			// final long sortTime = SystemClock.uptimeMillis();
			Collections.sort(list, LauncherModel.APP_NAME_COMPARATOR);
			// Log.d(TAG, "sort took " + (SystemClock.uptimeMillis()-sortTime) +
			// "ms");
			mLauncher.isNeedAllAppsSort = false;
		}

		Category.allapps = list;
		if (mCorpusSelectionDialog != null)
			mCorpusSelectionDialog.refreshCorpus();
		// addApps(list);
		
		if(mGrid == null){
			delayInitView();
		}
		displayAppsByCategory(Category.CATEGORY_ALLAPP, false);
		mLoadingBar.setVisibility(View.GONE);
	}

	public ArrayList<ApplicationInfo> getApps() {
		return mAllAppsList;
	}

	public void addAppsWithoutSort(ArrayList<ApplicationInfo> list) {
		// if(Launcher.LOGD)Log.d(TAG, "addAppsWithoutSort: "+list.size());
		addApps(list);
	}

	public void displayAppsByCategory(int category, boolean editFlag) {
		if (Launcher.LOGD)
			Log.d(TAG, "displayAppsByCategory category:" + category
					+ " editFlag:" + editFlag);

		currentCategory = category;
		isEditModel = editFlag;

		if (currentCategory == Category.CATEGORY_SHORTCUT) {
			mCategoryView.setVisibility(View.GONE);
		} else {
			mCategoryView.setVisibility(View.VISIBLE);
		}

		mCategoryNameView.setText(Category
				.getCategoryNameByCategory(currentCategory));

		if(category == Category.CATEGORY_3RDAPP)
			mCategoryDeleteView.setBackgroundResource(R.drawable.actionbar_button_new_del);//ic_allapps_uninstall		    
		else
			mCategoryDeleteView.setBackgroundResource(R.drawable.actionbar_button_new_del);
		
		if(currentCategory == Category.CATEGORY_ALLAPP)
                {
		    mCategorySearch.setVisibility(View.VISIBLE);
                    mCategoryNameView.setVisibility(View.VISIBLE);
                    id_search_key.setVisibility(View.GONE);
                }
		else
                {
		    mCategorySearch.setVisibility(View.GONE); 
                    mCategoryNameView.setVisibility(View.VISIBLE);
                    id_search_key.setVisibility(View.GONE); 
		}
		refreshAllAppsList();

		if (isEditModel) {
			refreshEditApps();
		} else {
			refreshApps();
		}
	}

	boolean isNeedRefresh = false;

	private void refreshApps() {
		// if(Launcher.LOGD)Log.d(TAG,
		// "refreshApps category:"+currentCategory+" editFlag:"+isEditModel);
		if (!mLauncher.amVisible) {
			// if(Launcher.LOGD)Log.d(TAG,
			// "Launcher is on the back, refreshAppsWithCategory later");
			isNeedRefresh = true;
			return;
		} else {
			isNeedRefresh = false;
		}

		if (isCorpusSelectionDialogShowing()) {
			dismissCorpusSelectionDialog();
		}
					
		showDeleteIcon();

		if (currentCategory >= Category.CATEGORY_Entertainment
				&& currentCategory <= Category.CATEGORY_CUSTOMIZE_MAX
				&& mAllAppsList.size() < Category.getAppsByCategory(
						Category.CATEGORY_ALLAPP).size()) {
			mCategoryAddView.setVisibility(View.VISIBLE);
		} else {
			mCategoryAddView.setVisibility(View.GONE);
		}

		mAppsAdapter = new AppsAdapter(getContext(), mAllAppsList);
		mGrid.setAdapter(mAppsAdapter);
		// mAppsAdapter.notifyDataSetChanged();
	}

	private void refreshEditApps() {
		// if(Launcher.LOGD)Log.d(TAG,
		// "refreshEditApps category:"+currentCategory+" editFlag:"+isEditModel);
		if (!mLauncher.amVisible) {
			// if(Launcher.LOGD)Log.d(TAG,
			// "Launcher is on the back, refreshEditAppsWithCategory later");
			isNeedRefresh = true;
			return;
		} else {
			isNeedRefresh = false;
		}

		if (isCorpusSelectionDialogShowing()) {
			dismissCorpusSelectionDialog();
		}
		
		if (currentCategory != Category.CATEGORY_3RDAPP
				&& mAllAppsList.size() < Category.getAppsByCategory(
						Category.CATEGORY_ALLAPP).size()) {
			mCategoryAddView.setVisibility(View.VISIBLE);
		}
		
		showDeleteIcon();

		isEditModel = true;		

		// if(mAllAppsList.size() <
		// Category.getAppsByCategory(Category.CATEGORY_ALLAPP).size()){
		// ApplicationInfo addInfo = new ApplicationInfo();
		// mAllAppsList.add(addInfo);
		// }
		mAppsAdapter = new AppsAdapter(getContext(), mAllAppsList);
		mGrid.setAdapter(mAppsAdapter);
	}
	
	private void showDeleteIcon()
	{
		if(mAllAppsList.size() > 0)
		{
			mNoAppTipsView.setVisibility(View.GONE);
			
			if (currentCategory == Category.CATEGORY_SHORTCUT || currentCategory == Category.CATEGORY_ALLAPP) {
				mCategoryDeleteView.setVisibility(View.GONE);
			}
			else
			{
				mCategoryDeleteView.setVisibility(View.VISIBLE);
			}
		}
		else
		{
			mCategoryDeleteView.setVisibility(View.GONE);
			mNoAppTipsView.setText(R.string.noapps);
			mNoAppTipsView.setVisibility(View.VISIBLE);
		}
	}

	private void refreshAllAppsList() {
		switch (currentCategory) {
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

		// if(Launcher.LOGD)Log.d(TAG, "refreshAllAppsList :"+
		// currentCategory+" size:"+mAllAppsList.size());
	}

	public void addApps(ArrayList<ApplicationInfo> list) {
		final int N = list.size();

		if (N == 0)
			return;

		boolean isNeedFreshUI = Category.addApps(getContext(), list, currentCategory);
		if (mCorpusSelectionDialog != null)
			mCorpusSelectionDialog.refreshCorpus();

		// need refresh ui
		if (currentCategory == Category.CATEGORY_ALLAPP || isNeedFreshUI
				|| (isEditModel && currentCategory == Category.CATEGORY_3RDAPP)) {
			displayAppsByCategory(currentCategory, isEditModel);
		}
	}

	public void removeApps(ArrayList<ApplicationInfo> list) {
		final int N = list.size();

		if (N == 0)
			return;

		boolean isNeedFreshUI = Category.removeApps(list, currentCategory);
		if (mCorpusSelectionDialog != null)
			mCorpusSelectionDialog.refreshCorpus();

		// need refresh ui
		if (currentCategory == Category.CATEGORY_ALLAPP || isNeedFreshUI
				|| isEditModel) {
			displayAppsByCategory(currentCategory, isEditModel);
		}
	}

	public void updateApps(ArrayList<ApplicationInfo> list) {
		final int N = list.size();
		// if(Launcher.LOGD)Log.d(TAG, "updateAppsWithCategory:"+N);

		if (N == 0)
			return;

		Category.removeApps(list, currentCategory);
		addApps(list);
	}

	public void dumpState() {
		ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList",
				mAllAppsList);
	}

	public void surrender() {
	}

	public void setFocusable(boolean focusable) {

	}

	private boolean forPicker = false;

	public void setStartForPicker(boolean fPicker) {
		forPicker = fPicker;
	}

	public void onClick(View v) {
		if (Launcher.LOGD)
			Log.d(TAG, "onClick v:" + v + "  isEditModel:" + isEditModel);
		final int vId = v.getId();
		Object tag = v.getTag();

		if (vId == R.id.all_apps_category
				&& tag instanceof Integer
				&& (((Integer) tag).intValue() > Category.CATEGORY_CUSTOMIZE && ((Integer) tag)
						.intValue() <= Category.CATEGORY_CUSTOMIZE_MAX)) {
			AllAppsListDialog aalDialog = new AllAppsListDialog(mLauncher,
					true, (Integer) tag);
			aalDialog.createDialog(
					Category.getAppsByCategory(Category.CATEGORY_ALLAPP))
					.show();
		}
	}

	public void onCorpusSelected(Corpus corpus) {
		// if(Launcher.LOGD)Log.d(TAG,
		// "onCorpusSelected category:"+corpus.category);
		if (currentCategory == corpus.category) {
			return;
		}

		mCategoryView.setImageDrawable(corpus.icon);

		if (mLauncher.isNeedCheckCategory) {
			// Log.d(TAG, "onCorpusSelected  initCategory");
			Category.initApplicationData(mLauncher, Category.allapps);
			mLauncher.isNeedCheckCategory = false;
		}

		displayAppsByCategory(corpus.category, false);
	}

	protected void showCorpusSelectionDialog() {
		if (mCorpusSelectionDialog == null) {
			mCorpusSelectionDialog = new CorpusSelectionDialog(getContext());
			mCorpusSelectionDialog.setOwnerActivity(mLauncher);
			mCorpusSelectionDialog.setOnCorpusSelectedListener(this);
		}

		if(isVisible())
		{
		    mCorpusSelectionDialog.show();
		}
	}

	protected boolean isCorpusSelectionDialogShowing() {
		return mCorpusSelectionDialog != null
				&& mCorpusSelectionDialog.isShowing();
	}

	public void dismissCorpusSelectionDialog() {
		if (mCorpusSelectionDialog != null) {
			mCorpusSelectionDialog.dismiss();
		}
	}
	
	private void doSearch(String key)
        {	
            Log.d(TAG, "keyword="+key);

            if(key == null || key.length() == 0)
		{
			mAppsAdapter = new AppsAdapter(getContext(), mAllAppsList);
			mGrid.setAdapter(mAppsAdapter);
		}
		else
		{		
			ArrayList<ApplicationInfo> seachs = new ArrayList<ApplicationInfo>(); 
			for(ApplicationInfo infod: mAllAppsList)
			{
				String title      = infod.title.toString().toLowerCase();
				String sdcardname = infod.sourceDir.toLowerCase();
				String stringname = infod.componentName.toString().toLowerCase();			
				if(title.contains(key) 
					|| sdcardname.contains(key) 
					|| stringname.contains(key))
				{
				    seachs.add(infod);
				}
			}
                        mAppsAdapter = new AppsAdapter(getContext(), seachs);
                        mGrid.setAdapter(mAppsAdapter);
		}
    }
	
    private class MyWatcher implements TextWatcher 
    {   
       public void afterTextChanged(Editable s) 
       {
           //do search
           doSearch(s.toString().toLowerCase());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
}
