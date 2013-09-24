package com.android.omshome;

import java.util.ArrayList;

import com.android.omshome.screenmanager.Gallery;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PageImageAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<Bitmap> pageviews;
//    int mGalleryItemBackground;
    private final static String TAG = "PageImageAdapter";
    public PageImageAdapter(Context c,ArrayList<Bitmap> pageviews) {
        mContext = c;
        TypedArray a = c.obtainStyledAttributes(R.styleable.NewGallery);
//        mGalleryItemBackground = a.getResourceId(R.styleable.NewGallery_android_galleryItemBackground, 0);
        a.recycle();
        this.pageviews = pageviews;
        
        //recode the init pageView order index
        
    }
    
    public int getCount() {
        return pageviews.size();
    }

    public Object getItem(int position) {
        return pageviews.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public  ArrayList<Bitmap> getAllBitmap()
    {
        return pageviews;
    }
    
    Drawable d;
    public View getView(int position, View convertView, ViewGroup parent) {
    	//Log.d(TAG, "getView");
        ScreenView view ;
        final Resources resources = mContext.getResources();
        final int imageWidth = (int) resources.getDimension(R.dimen.page_edit_image_width);
        final int imageHeight = (int) resources.getDimension(R.dimen.page_edit_image_height);
        final int imagePadding = (int)resources.getDimension(R.dimen.page_edit_image_padding);
        if(convertView != null && convertView instanceof ScreenView)
        {
            view = (ScreenView)convertView;
            view.setLayoutParams(new Gallery.LayoutParams(imageWidth,imageHeight));
            view.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
        }
        else
        {
        	//Log.d(TAG, "getView new ScreenView ");
        	view = new ScreenView(mContext);
            view.setLayoutParams(new Gallery.LayoutParams(imageWidth,imageHeight));
            view.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
        }
       
        view.setBitmap(pageviews.get(position));
//        view.setImageBitmap(pageviews.get(position));
//        view.setBackgroundResource(R.drawable.homescreen_menu_page_edit_bg);
        return view;
    }
}
