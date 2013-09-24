/*
 * Copyright (C) 2008 Google Inc.
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

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;

public class WallpaperChooser extends Activity implements AdapterView.OnItemSelectedListener,
        OnClickListener {

    private static final Integer[] THUMB_IDS = {
            R.drawable.wallpaper_lake_small,
    };

    private static final Integer[] IMAGE_IDS = {
            R.drawable.wallpaper_lake,
    };
    
    private ArrayList<File> mThumb_files = new ArrayList<File>();
    private ArrayList<File> mImage_files = new ArrayList<File>();    

    private Gallery mGallery;
    private ImageView mImageView;
    private boolean mIsWallpaperSet;

    private BitmapFactory.Options mOptions;
    private Bitmap mBitmap;
    
    private int mImageClicked = 0;
    private int lastImagePosition = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run() {
        	mImageClicked = 0;
        	itemSelected(lastImagePosition);
        }
    };
    
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.wallpaper_chooser);

        mOptions = new BitmapFactory.Options();
        mOptions.inDither = false;
        mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        getWallpaperFiles();
        
        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(new ImageAdapter(this));
        mGallery.setOnItemSelectedListener(this);
        
        Button b = (Button) findViewById(R.id.set);
        b.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.wallpaper);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsWallpaperSet = false;
    }
    
    private void getWallpaperFiles() {
        File dir = new File("/opl/data/res/wallpaper");
        if (!dir.exists() || !dir.isDirectory())
            return;
        
        String[] files = dir.list();
        if (files == null)
            return;
        
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals("default_wallpaper.jpg") 
                || files[i].endsWith("small.jpg"))
                continue;
            
            //Log.i("home", "wallpaper name is: "+files[i]);
            File image = new File(dir, files[i]);
            String s = files[i].substring(0, files[i].indexOf(".jpg")) + "_small.jpg";
            //Log.i("home", "thumb of wallpaper name is: "+s);
            File thumb = new File(dir, s);
            if (image.exists() && image.isFile() && thumb.exists() && thumb.isFile()) {
                mThumb_files.add(thumb);
                mImage_files.add(image);
            }
            //Log.i("home", "count of wallpaper is: "+mImage_files.size());
        }
    }

    public void onItemSelected(AdapterView parent, View v, int position, long id) {
    	if (mImageClicked == 1) {
    		lastImagePosition = position;
    		return;
    	}
        
        mImageClicked = 1;
        lastImagePosition = position;
        mHandler.postDelayed(mRunnable, 600);
    }
    
    private void itemSelected(int lastPosition) {
    	final ImageView view = mImageView;
        Bitmap b;
        if (mImage_files.size() == 0) 
            b = BitmapFactory.decodeResource(getResources(), IMAGE_IDS[lastPosition], mOptions);
        else
            b = BitmapFactory.decodeFile(mImage_files.get(lastPosition).getPath(), mOptions);
        view.setImageBitmap(b);

        // Help the GC
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        mBitmap = b;

        final Drawable drawable = view.getDrawable();
        drawable.setFilterBitmap(true);
        drawable.setDither(true);
    }

    /*
     * When using touch if you tap an image it triggers both the onItemClick and
     * the onTouchEvent causing the wallpaper to be set twice. Ensure we only
     * set the wallpaper once.
     */
    private void selectWallpaper(int position) {
        if (mIsWallpaperSet) {
            return;
        }

        mIsWallpaperSet = true;
    	try{
        	if (mImage_files.size() == 0) {
                InputStream stream = getResources().openRawResource(IMAGE_IDS[position]);
                setWallpaper(stream);
            } else {
		setWallpaper(new FileInputStream(mImage_files.get(position).getPath()));
            }
        	
        setResult(RESULT_OK);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set wall paper delay
        //imagePosition = position;
        //mHandler.postDelayed(mWallPaperRunnable, 600);
        //Toast.makeText(this, R.string.setting_wallpaper, 5000).show();
        finish();
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(WallpaperChooser context) {
            mLayoutInflater = context.getLayoutInflater();
        }

        public int getCount() {
        	//Log.i("home", "now count of wallpaper is: "+mImage_files.size());
            if (mImage_files.size() == 0) 
                return THUMB_IDS.length;
            
            return mImage_files.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;

            if (convertView == null) {
                image = (ImageView) mLayoutInflater.inflate(R.layout.wallpaper_item, parent, false);
            } else {
                image = (ImageView) convertView;
            }

            if (mImage_files.size() == 0) {
                image.setImageResource(THUMB_IDS[position]);
            } else {
            	Bitmap b = BitmapFactory.decodeFile(mThumb_files.get(position).getPath());
                image.setImageBitmap(b);
            }
            
            
            image.getDrawable().setDither(true);
            return image;
        }
    }

    public void onClick(View v) {
        selectWallpaper(mGallery.getSelectedItemPosition());
    }
}
