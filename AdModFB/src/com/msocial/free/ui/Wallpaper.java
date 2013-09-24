package com.msocial.free.ui;

import com.msocial.free.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

public class Wallpaper  extends Activity 
{
	    private static final String LOG_TAG = "SNS-Camera";
	    static final int PHOTO_PICKED = 1;
	    static final int CROP_DONE    = 2;

	    static final int SHOW_PROGRESS = 0;
	    static final int FINISH = 1;

	    static final String sDoLaunchIcicle = "do_launch";
	    static final String sTempFilePathIcicle = "temp_file_path";

	    private ProgressDialog mProgressDialog = null;
	    private boolean mDoLaunch = true;
	    private String mTempFilePath;

	    private Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case SHOW_PROGRESS: {
	                    CharSequence c = getText(R.string.wallpaper);
	                    mProgressDialog = ProgressDialog.show(Wallpaper.this, "", c, true, false);
	                    break;
	                }

	                case FINISH: {
	                    closeProgressDialog();
	                    setResult(RESULT_OK);
	                    Wallpaper.this.finish();
	                    break;
	                }
	            }
	        }
	    };


    
    static class SetWallpaperThread extends Thread {
        private final Bitmap mBitmap;
        private final Handler mHandler;
        private final Context mContext;
        private final File mFile;

        public SetWallpaperThread(Bitmap bitmap, Handler handler, Context context, File file) {
            mBitmap = bitmap;
            mHandler = handler;
            mContext = context;
            mFile = file;
        }

        @Override
        public void run() {
            try {
                mContext.setWallpaper(mBitmap);
                Log.e(LOG_TAG, "after set wallpaper.");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to set wallpaper.", e);
            } finally {
                mHandler.sendEmptyMessage(FINISH);
                //mFile.delete();
            }
        }
    }

    private synchronized void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Uri imageToUse = getIntent().getData();
        if (imageToUse != null) {
            Intent intent = new Intent();
            intent.setClassName("com.android.camera",
                                "com.android.camera.CropImage");
            intent.setData(imageToUse);
            formatIntent(intent);
            startActivityForResult(intent, CROP_DONE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            formatIntent(intent);
            startActivityForResult(intent, PHOTO_PICKED);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        icicle.putBoolean(sDoLaunchIcicle, mDoLaunch);
        icicle.putString(sTempFilePathIcicle, mTempFilePath);
    }
        
    protected void formatIntent(Intent intent) {
        int width  = getWallpaperDesiredMinimumWidth();
        int height = getWallpaperDesiredMinimumHeight();
        Bundle extras = new Bundle();
        extras.putInt("outputX",         width);
        extras.putInt("outputY",         height);
        extras.putInt("aspectX",         width);
        extras.putInt("aspectY",         height);
        extras.putBoolean("scale",           true);
        extras.putBoolean("noFaceDetection", true);
        extras.putBoolean("setWallpaper",    true);
        //extras.putBoolean("return-data",     true);       
                
        intent.putExtras(extras);
    }


    /*
    protected void formatIntent(Intent intent) {
        // TODO: A temporary file is NOT necessary
        // The CropImage intent should be able to set the wallpaper directly
        // without writing to a file, which we then need to read here to write
        // it again as the final wallpaper, this is silly
        File f = getFileStreamPath("temp-wallpaper");
        (new File(f.getParent())).mkdirs();
        mTempFilePath = f.toString();
        f.delete();

        int width = getWallpaperDesiredMinimumWidth();
        int height = getWallpaperDesiredMinimumHeight();
        intent.putExtra("outputX",         width);
        intent.putExtra("outputY",         height);
        intent.putExtra("aspectX",         width);
        intent.putExtra("aspectY",         height);
        intent.putExtra("scale",           true);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file:/" + mTempFilePath));
        intent.putExtra("outputFormat",    Bitmap.CompressFormat.PNG.name());
        // TODO: we should have an extra called "setWallpaper" to ask CropImage to
        // set the cropped image as a wallpaper directly
        // This means the SetWallpaperThread should be moved out of this class to CropImage
    }
    */

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if ((requestCode == PHOTO_PICKED || requestCode == CROP_DONE) && (resultCode == RESULT_OK)
                && (data != null)) {
        	
        	    Log.d(LOG_TAG, "what is data="+data);
        	    Bundle extrbudle = data.getExtras();
        	    Bitmap bitmap = (Bitmap)extrbudle.get("data");
            	if(bitmap == null)
            	{
            		Uri uri = Uri.parse(data.getAction()); 		
            		if(uri != null)
            		{
	            	    String[] cols = new String[] {MediaStore.Images.Media.DATA,};
	                    String who = uri.getPathSegments().get(uri.getPathSegments().size()-1);
	                    String[] whereclause = new String[]{who};
	                    
	                    Cursor mCursor = getContentResolver().query(uri, cols, android.provider.BaseColumns._ID+" = ?", whereclause, null);         
	                   
	                    if(mCursor != null)
	                    {
	                       mCursor.moveToFirst();
	                       String filePath = mCursor.getString(0);                       
	                       try{
	                    	   Log.d(LOG_TAG, "save path="+filePath);
		                       File tempFile = new File(filePath);
		                       InputStream s = new FileInputStream(filePath);
		                       bitmap        = BitmapFactory.decodeStream(s);
	                       }catch(Exception ne)                       
	                       {
	                    	   Log.d(LOG_TAG, "no such file="+ne.getMessage());
	                       }
	                    }
            		}
            	}
            	
                if (bitmap == null) 
                {
                    Log.e(LOG_TAG, "Failed to set wallpaper.  Couldn't get bitmap for path " + mTempFilePath);
                    finish();
                } 
                else 
                {
                    Log.v(LOG_TAG, "bitmap size is " + bitmap.getWidth() + " / " + bitmap.getHeight());
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    new SetWallpaperThread(bitmap, mHandler, this, null).start();
                }
                mDoLaunch = false;            
        } 
        else 
        {
        	Log.d(LOG_TAG, "no data");
            setResult(RESULT_CANCELED);
            finish();
        }
    }

}
