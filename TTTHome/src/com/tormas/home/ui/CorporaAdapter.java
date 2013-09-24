/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.tormas.home.ui;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.tormas.home.Category;
import com.tormas.home.R;
import android.os.SystemProperties;

public class CorporaAdapter extends BaseAdapter {

    private static final String TAG = "CorporaAdapter";
    private static final boolean DBG = false;
    private boolean supportMoreCustomize = false;
    private final CorpusViewFactory mViewFactory;

    private List<Corpus> mRankedEnabledCorpora = new ArrayList<Corpus>();

    private boolean mGridView;
    private Corpus allCorpus, systemCorpus, carrierappCorpus, oemCorpus, the3rdCorpus, entertainmentCorpus, informationCorpus, toolsCorpus, shortcutCorpus;
    
    private CorporaAdapter(CorpusViewFactory viewFactory, boolean gridView, Context con) {
        mViewFactory = viewFactory;
        mGridView = gridView;
        
        supportMoreCustomize = (SystemProperties.getInt("home_category_more", 1) == 1); 
        initData();
        updateCorpora();
    }

    private void initData()
    {
    	allCorpus = new Corpus();
    	allCorpus.label = Category.nameAll;
    	allCorpus.icon  = Category.drawableAll;
    	allCorpus.category = Category.CATEGORY_ALLAPP;
    	mRankedEnabledCorpora.add(allCorpus);
    	
    	if((SystemProperties.getInt("home_category_support_system", 0) == 1))
    	{
	    	systemCorpus = new Corpus();
	    	systemCorpus.label = Category.nameSystem;
	    	systemCorpus.icon  = Category.drawableSystem;
	    	systemCorpus.category = Category.CATEGORY_SYSTEMAPP;
	    	mRankedEnabledCorpora.add(systemCorpus);
    	}
    	
		carrierappCorpus = new Corpus();
		carrierappCorpus.label = Category.nameCarrier;
		carrierappCorpus.icon  = Category.drawableCarrier;
		carrierappCorpus.category = Category.CATEGORY_CARRIERAPP;
    	if(Category.carrierapps.size()>0){
    		mRankedEnabledCorpora.add(carrierappCorpus);
    	}
    	
		oemCorpus = new Corpus();
		oemCorpus.label = Category.nameOem;
		oemCorpus.icon  = Category.drawableOem;
		oemCorpus.category = Category.CATEGORY_OEMAPP;
		if(Category.oemapps.size()>0){
    		mRankedEnabledCorpora.add(oemCorpus);
    	} 
    	
		the3rdCorpus = new Corpus();
		the3rdCorpus.label = Category.nameThe3rd;
		the3rdCorpus.icon  = Category.drawableThe3rd;
		the3rdCorpus.category = Category.CATEGORY_3RDAPP;
		//if(Category.the3rdapps.size()>0){
    		mRankedEnabledCorpora.add(the3rdCorpus);
    	//}
    	
		shortcutCorpus = new Corpus();
		shortcutCorpus.label = Category.nameShortcut;
		shortcutCorpus.icon  = Category.drawableShortcut;
		shortcutCorpus.category = Category.CATEGORY_SHORTCUT;
		mRankedEnabledCorpora.add(shortcutCorpus);
		
		if(supportMoreCustomize)
		{
			entertainmentCorpus = new Corpus();
			entertainmentCorpus.label = Category.nameEntertainment;
			entertainmentCorpus.icon  = Category.drawableEntertainment;
			entertainmentCorpus.category = Category.CATEGORY_Entertainment;
			mRankedEnabledCorpora.add(entertainmentCorpus);
			
			informationCorpus = new Corpus();
			informationCorpus.label = Category.nameInformation;
			informationCorpus.icon  = Category.drawableInformation;
			informationCorpus.category = Category.CATEGORY_Information;
			mRankedEnabledCorpora.add(informationCorpus);
			
			toolsCorpus = new Corpus();
			toolsCorpus.label = Category.nameTools;
			toolsCorpus.icon  = Category.drawableTools;
			toolsCorpus.category = Category.CATEGORY_Tools;
			mRankedEnabledCorpora.add(toolsCorpus);
		}		
		
    }
    
    public void refreshCorpus(){
    	mRankedEnabledCorpora.clear();
    	mRankedEnabledCorpora.add(allCorpus);
    	mRankedEnabledCorpora.add(systemCorpus);
    	if(Category.carrierapps.size()>0){
    		mRankedEnabledCorpora.add(carrierappCorpus);
    	}
    	
    	if(Category.oemapps.size()>0){
    		mRankedEnabledCorpora.add(oemCorpus);
    	}
    	
    	//if(Category.the3rdapps.size()>0){
    		mRankedEnabledCorpora.add(the3rdCorpus);
    	//}
    	
    	mRankedEnabledCorpora.add(shortcutCorpus);
    	
    	if(supportMoreCustomize)
		{
	    	mRankedEnabledCorpora.add(entertainmentCorpus);
	    	mRankedEnabledCorpora.add(informationCorpus);
	    	mRankedEnabledCorpora.add(toolsCorpus);    	
		}
    	
    	updateCorpora();
    }
    
    public static CorporaAdapter createListAdapter(CorpusViewFactory viewFactory,Context con) {
        return new CorporaAdapter(viewFactory, false, con);
    }

    public static CorporaAdapter createGridAdapter(CorpusViewFactory viewFactory, Context con) {
        return new CorporaAdapter(viewFactory, true, con);
    }

    private void updateCorpora() {        
        notifyDataSetChanged();
    }

    public void close() {        
    }

    public int getCount() {
        return mRankedEnabledCorpora.size();
    }

    public Corpus getItem(int position) {
        return mRankedEnabledCorpora.get(position);        
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Gets the position of the given corpus.
     */
    public int getCorpusPosition(Corpus corpus) {
        if (corpus == null) {
            return 0;
        }
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (corpus.equals(getItem(i))) {
                return i;
            }
        }
        Log.w(TAG, "Corpus not in adapter: " + corpus);
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
//        TextView view = (TextView) convertView;
//        if (view == null) {
//            view = (TextView)((CorpusViewInflater)mViewFactory).getInflater().inflate(R.layout.corpus_grid_item, null, false);
//        }
//        
//        Corpus corpus = getItem(position);
//        view.setCompoundDrawablesWithIntrinsicBounds(corpus.icon, null, null, null);
//        view.setText( corpus.label);

       View view = convertView;
       if (view == null) {
          view =((CorpusViewInflater)mViewFactory).getInflater().inflate(R.layout.corpus_grid_item, null, false);
       }

       Corpus corpus = getItem(position);
       TextView tv = (TextView)view.findViewById(R.id.source_tv);
       ImageView iv = (ImageView)view.findViewById(R.id.source_iv);
       
//       tv.setCompoundDrawablesWithIntrinsicBounds(null, corpus.icon, null, null);
       iv.setImageDrawable(corpus.icon);
       tv.setText( corpus.label);
        
       return view;
    }

    protected TextView createView(ViewGroup parent) {
        if (mGridView) {
            return mViewFactory.createGridCorpusView(parent);
        } else {
            return mViewFactory.createListCorpusView(parent);
        }
    } 

}
