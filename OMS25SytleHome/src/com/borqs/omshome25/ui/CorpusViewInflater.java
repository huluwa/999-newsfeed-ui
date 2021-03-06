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

package com.borqs.omshome25.ui;

import com.borqs.omshome25.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Inflates corpus views.
 */
public class CorpusViewInflater implements CorpusViewFactory {

    private final Context mContext;

    public CorpusViewInflater(Context context) {
        mContext = context;
    }

    public LayoutInflater getInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public TextView createGridCorpusView(ViewGroup parentViewType) {
        return inflateCorpusView(R.layout.corpus_grid_item, parentViewType);
    }

    public TextView createListCorpusView(ViewGroup parentViewType) {
        return inflateCorpusView(R.layout.corpus_list_item, parentViewType);
    }

    protected TextView inflateCorpusView(int res, ViewGroup parentViewType) {
        return (TextView) getInflater().inflate(res, parentViewType, false);
    }

    public String getGlobalSearchLabel() {
        return mContext.getString(R.string.corpus_label_global);
    }

    private int getGlobalSearchIconResource() {
        return R.drawable.search_app_icon;
    }

    public Drawable getGlobalSearchIcon() {
        return mContext.getResources().getDrawable(getGlobalSearchIconResource());
    }

    public Uri getGlobalSearchIconUri() {
        return Util.getResourceUri(mContext, getGlobalSearchIconResource());
    }

}
