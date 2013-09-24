package com.borqs.omshome25;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import android.opengl.GLU;
import android.opengl.GLES11Ext;

import android.content.Context;
import android.util.Log;



import android.view.KeyEvent;
import android.view.MotionEvent; 
import android.view.View;
import android.os.SystemClock;

import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.app.WallpaperManager;
//import android.view.Window;
import android.content.Context;	
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceHolder;
import android.opengl.GLUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Object;
import android.view.GestureDetector;
import android.util.AttributeSet;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.graphics.Rect;
import android.content.res.Configuration;
import android.view.HapticFeedbackConstants;

import android.graphics.Canvas;



class RotateTexturesView extends TexturesRenderView{
	//private static final String TAG = "RotateTexturesView";

	public interface PageChangeListener
	{
		public void onPageChange(int prePage, int curPage);
	}
	
	PageChangeListener pageListener;
	public void setPageChangeListener(PageChangeListener listener)
	{
		pageListener = listener;
	}
	
	float mRotate = 0;
//	int mRotateDirect = 0;
	int mMoveDistance = 0;
	float mRotateSpeed = 0;
	boolean mIsRotate = false;
	long mTime = 0;

	private float mTouchX = -1;
	private float mTouchY = -1;
	public boolean mIsSelect = false;


	public enum EState{ ESTART, EROUND, EBACK,};
	AnimateState mAnimaestate;
	RoundAnimateState mRoundAnimateState = new RoundAnimateState();
	StartAnimateState mStartAnimateState = new StartAnimateState();
	BackAnimateState mBackAnimateState = new BackAnimateState();

	int mCurPageIndex = 0;

	float mCameraX = 0f;
	float mCameraY = 1.8f;
	float mCameraZ = 11f;
	float mCameraLX = -4f;
	float mCameraLY = 0f;
	float mCameraLZ = 11f;



	float mRadius = 0.8f;

	float mAngleX = -28f;
	float mAngleY = 0f;
	float mRingUp = 0.5f;

	float mRadiusLX = 0.5f;
	float mRadiusLY = 3f;	
	float mRadiusLZ = 12f;



	

	float mCameraXoffset = 0f;
	float mCameraYoffset = 0f;
	float mCameraZoffset = 0f;

	float mCameraXoffsetAnim = 0f;
	float mCameraYoffsetAnim = 0f;
	float mCameraZoffsetAnim = 0f;	


//	Bitmap background = null;
//	int mBackGroundTexId = -1;	
	Quad Quadback = null;

	Bitmap PageBackground = null;
	Bitmap PageBackgroundCopy = null;
	Quad PageQuadback = null;

	Bitmap Selected = null;
	Bitmap unSelected = null;
	Quad QuadSelected = null;
	Quad unQuadSelected = null;
	
//	Bitmap Wallpaper = null;
//	int mWallpaperTexId = -1;
	
	boolean back = false;

	long mDeltaTime = 0;
	long mPrevFrameTime = 0;
//	int mframecount = 0;
	int mLoadViewNum = 0;
	boolean updateviews = true;
	int removePgae = -1;
	int addPgae = -1;
	int mSwapPage = -1;
	float mSwapTrans = 0;
	float mSwapdrag = 0;

	int mOrientation = 1;
	
	Quad mBackQuad = null;

	private final Deque<MotionEvent> mTouchEventQueue = new Deque<MotionEvent>();
	private final Deque<Integer> mKeyEventQueue = new Deque<Integer>();
    private SensorManager mSensorManager;
	private SensorListener mSensorListener = new SensorListener();

	

	public RotateTexturesView(Context context,AttributeSet attrs){
		super(context, attrs);

		if(Launcher.LOGD)Log.d(TAG,"RotateTexturesView");

		ChangeAnimateState(RotateTexturesView.EState.ESTART);
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

	}

	public RotateTexturesView(Context context) {
        super(context);
		
		ChangeAnimateState(RotateTexturesView.EState.ESTART);

    }

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl,	config);
		
		updateviews = true;
		mLoadViewNum = 0;

		Intent intent = new Intent(Launcher.INTENT_CACHE_SCREEN);
		mContext.sendBroadcast(intent);

		mOrientation = mContext.getResources().getConfiguration().orientation;
		
		if( mOrientation != Configuration.ORIENTATION_LANDSCAPE){
			PageBackground = BitmapFactory.decodeStream(mContext.getResources().openRawResource( R.drawable.cmcc_home_page));
			Selected = BitmapFactory.decodeStream(mContext.getResources().openRawResource( R.drawable.pageselected));
		}
		else{
			PageBackground = BitmapFactory.decodeStream(mContext.getResources().openRawResource( R.drawable.cmcc_home_page_land));
			Selected = BitmapFactory.decodeStream(mContext.getResources().openRawResource( R.drawable.pageselected_land));


		}


	}



	@Override
    public boolean onTouchEvent(MotionEvent event) {


        // Ignore events received before the surface is created to avoid
        // deadlocking with GLSurfaceView's needToWait().
        if (mGL == null) {
            return false;
        }
        // Wait for the render thread to process this event.
        if (mTouchEventQueue.size() > 6 && event.getAction() == MotionEvent.ACTION_MOVE)
            return true;
        synchronized (mTouchEventQueue) {
            MotionEvent eventCopy = MotionEvent.obtain(event);
            mTouchEventQueue.addLast(eventCopy);
            requestRender();
        }
        return true;


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Ignore events received before the surface is created to avoid
        // deadlocking with GLSurfaceView's needToWait().
        if (mGL == null) {
            return false;
        }

        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER||keyCode == KeyEvent.KEYCODE_ENTER||
        	keyCode == KeyEvent.KEYCODE_DPAD_UP||keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
	        // Wait for the render thread to process this event.
	        if (mKeyEventQueue.size() > 6 ){
	            return true;
	        }
	        synchronized (mKeyEventQueue) {
	            int eventCopy = keyCode;
	            mKeyEventQueue.addLast(eventCopy);
	            requestRender();
	            
				if(Launcher.LOGD)Log.d(TAG,"onKeyDown "+ keyCode);
	        }
	        return true;
        }
        else{
			return false;
        }


    }
	

	public void onDrawFrame(GL10 gl) {
		super.onDrawFrame(gl);

		UpdateBitmaps();


		long timestamp = SystemClock.elapsedRealtime();
		mDeltaTime = timestamp - mPrevFrameTime;
		if(mDeltaTime>40) mDeltaTime = 40;
		//mDeltaTime = 30;
		mPrevFrameTime = timestamp;



		processTouchEvent();
		processKeyEvent();

		UpdateAnim();


		
		if(mAnimaestate!=null){
			if( mOrientation == Configuration.ORIENTATION_LANDSCAPE){
				mAnimaestate.OnDrawFrameLandscape( gl);
			}
			else{
				mAnimaestate.OnDrawFrame( gl);
			}
		}
		
    }

	 @Override
	 public void onPause() {
       super.onPause();
	   mSensorManager.unregisterListener(mSensorListener);

    }
	 @Override
	 public void onResume() {
		 super.onResume();
		 requestRender();


		 Sensor sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		 //Sensor sensorOrientation	= mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		 if (sensorAccelerometer != null) {
			 mSensorManager.registerListener(mSensorListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
		 }


	 }
 

	protected boolean ChangeAnimateState( EState state){
			requestRender();
		switch( state ){
			case ESTART:
				mStartAnimateState.InitAnimate();
				mAnimaestate = mStartAnimateState;
				break;
			case EROUND:
				mRoundAnimateState.InitAnimate();
				mAnimaestate = mRoundAnimateState;
				((TextureSwitchActivity)mContext).addPage.setClickable(true);
				((TextureSwitchActivity)mContext).deletePage.setClickable(true);
				((TextureSwitchActivity)mContext).setDefaultPage.setClickable(true);
				break;
			case EBACK:
				//if(mRoundAnimateState.mIsRotate==false){
					mBackAnimateState.InitAnimate();
					mBackAnimateState.ReBindTexture();
					mAnimaestate = mBackAnimateState;

					((TextureSwitchActivity)mContext).addPage.setClickable(false);
					((TextureSwitchActivity)mContext).deletePage.setClickable(false);
					((TextureSwitchActivity)mContext).setDefaultPage.setClickable(false);
					((TextureSwitchActivity)mContext).GetHandler().sendEmptyMessage(TextureSwitchActivity.Back_MSG);

				//}
				//else{
				//	return false;
				//}
				break;				
		}
		
		return true;
	}

	public void DeleteTextures(){
		if(Quadback!=null){
			Quadback.OnDestory();
			Quadback = null;

		}
		if(PageQuadback!=null){
			PageQuadback.OnDestory();
			PageQuadback = null;
		}

	}

	public void surfaceDestroyed(SurfaceHolder holder){

		if(Quadback!=null){
			Quadback.OnDestory();
			Quadback = null;

		}
		if(PageQuadback!=null){
			PageQuadback.OnDestory();
			PageQuadback = null;
		}
		if(PageBackgroundCopy!=null){
			PageBackgroundCopy.recycle();
			PageBackgroundCopy = null;
		}
		
		 super.surfaceDestroyed( holder);
		//gl.glDeleteTextures(1, textureId, 0);
		

	}

	public int  OESdrawbind(Bitmap bitmap){
        GL11 gl = mGL;
//        int glError = GL11.GL_NO_ERROR;
		int[] textureId = new int[1];
        if (bitmap != null) {

             int width = bitmap.getWidth();
             int height = bitmap.getHeight();


			if (!isPowerOf2(width) || !isPowerOf2(height) || width>512 ||height>512) {
				int paddedWidth = nextPowerOf2(width);
				int paddedHeight = nextPowerOf2(height);
			
				if(paddedWidth>512)paddedWidth = 512; 
				if(paddedHeight>512)paddedHeight = 512; 			
			
				Bitmap padded = Bitmap.createScaledBitmap(bitmap,paddedWidth, paddedHeight, true);				
			
				//bitmap.recycle();
				bitmap = padded;
			
			}			


            width = bitmap.getWidth();
            height = bitmap.getHeight();

            // Define a vertically flipped crop rectangle for OES_draw_texture.
            int[] cropRect = { 0, height, width, -height };

            // Upload the bitmap to a new texture.
            gl.glGenTextures(1, textureId, 0);
            gl.glBindTexture(GL11.GL_TEXTURE_2D, textureId[0]);
            gl.glTexParameteriv(GL11.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, cropRect, 0);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
//            glError = gl.glGetError();

            //bitmap.recycle();
			
            }
			return textureId[0];
        }


	
	public void Back(){
        //if(true) return;
        back = true;
        
         Intent i = new Intent(Launcher.INTENT_FINISH_PAGEMANAGER);            
         Bundle b = new Bundle();  
         b.putInt(Launcher.STRING_PAGE_INDEX, mCurPageIndex);  
         b.putInt(Launcher.WITH_FADE_EFFECT,  1);
		  
         if(Launcher.LOGD)Log.d("RotateTexturesView","mCurPageIndex  "+mCurPageIndex);
         
         i.putExtras(b);  
        
        ((Activity)mContext).sendBroadcast(i);
        ((Activity)mContext).finish();  
    }

	public void SetCurPage( int index ){
		mCurPageIndex =  index;
		pageListener.onPageChange(mCurPageIndex, mCurPageIndex);
		//mRotate = (360/mQuadList.size())*mCurPageIndex;
	}

	public void ChangeState(){
		mAnimaestate.mChangeState = true;
		mIsRotate = true;
		requestRender();
	}

	private  abstract class AnimateState {

		boolean mChangeState = false;
		public AnimateState(){
			}
		abstract public void OnDrawFrame(GL10 gl);
		abstract public void OnDrawFrameLandscape(GL10 gl);
		
		abstract public boolean onTouchEvent(MotionEvent event);
		abstract public boolean onKeyDown(int keycode);

		abstract public boolean CanChangeState();
		abstract public void InitAnimate();
	}

	private class RoundAnimateState extends AnimateState {


		int mRotateDirect = 0;
		//float mRotateSpeed = 0;
		

//		long mPrevTouchTime = 0;
//		long mPrevTouchDownTime = 0;

		/**
		 * Helper for detecting touch gestures.
		 */
		private GestureDetector mGestureDetector;
		private MyGesture mGestureListener;
		
		public RoundAnimateState(){
			InitAnimate();
			mGestureListener = new MyGesture();
			mGestureDetector = new GestureDetector(mGestureListener);
        	mGestureDetector.setIsLongpressEnabled(true);
		}

		public void InitAnimate(){

		 mRotateDirect = 0;
		 mRotateSpeed = 0;
//		 mPrevTouchTime = 0;
//		 mPrevTouchDownTime = 0;

		}

		
		@Override
		public void OnDrawFrame(GL10 gl){

					  					
			gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
					
					 
			// Now we're ready to draw some 3D objects
					  					
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();					
			GLU.gluLookAt(gl, mCameraX+mCameraXoffsetAnim, mCameraY+mCameraYoffsetAnim, mCameraZ+mCameraZoffsetAnim, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
						
			//int QuadSize = mQuadList.size();
			int QuadSize = mQuadNum;

			if(mSwapPage!=-1) QuadSize = mQuadNum-1;
			if( QuadSize > 0 ){ 
				long Time = System.currentTimeMillis();
				//mRotate -=(Time - mTime)/10;
				if( mIsRotate){


					int a = 0;
/*
								if(Math.abs(mRotateSpeed)<6){
									if( Math.abs(mRotate%(360/QuadSize))>(360/QuadSize)/2){
										//mRotateDirect = Math.abs(mRotateDirect);
										//mRotateSpeed = Math.abs(mRotateSpeed);
										a = 1;
									}
									else{
										//mRotateDirect = -Math.abs(mRotateDirect);
										//mRotateSpeed = -Math.abs(mRotateSpeed);
										a = -1;
									}
								}
								
*/
					if(Math.abs(mRotateSpeed)<4){
						if( Math.abs(mRotate%(360/QuadSize))>(360/QuadSize)/2){
							mRotateSpeed = 3;
						}
						else{
							mRotateSpeed = -3;
						}
					}

					float f = 0;
					if(mRotateSpeed>3) f = -1;
					if(mRotateSpeed<-3) f = 1;
					
					//Log.d("RotateTexturesView","mRotateSpeed: 111111111111111111111111	"+mRotateSpeed);

					long deltMove = (long)(mRotateSpeed*mDeltaTime/30 + (f *(mDeltaTime)*(mDeltaTime) + 2*a*(mDeltaTime)*(mDeltaTime))/900);
					mRotateSpeed += ((2*a*(mDeltaTime)+f*(mDeltaTime))/30);
					if(mRotateSpeed<3&&mRotateSpeed>0) mRotateSpeed = 3;
					if(mRotateSpeed>-3&&mRotateSpeed<0) mRotateSpeed = -3;


				
				//Log.d("RotateTexturesView","mDeltaTime: 111111111111111111111111  "+mDeltaTime);
				//Log.d("RotateTexturesView","deltMove: 111111111111111111111111  "+deltMove);
				//Log.d("RotateTexturesView","mRotateSpeed: 111111111111111111111111  "+mRotateSpeed);


					
					if(deltMove>0){
						//mRotate += mRotateSpeed;
						if(Math.abs((360/QuadSize)-mRotate%(360/QuadSize))<Math.abs(deltMove)&&Math.abs(mRotateSpeed)<4){
							mRotate += ((360/QuadSize)-mRotate%(360/QuadSize));
						}
						else{
							mRotate += deltMove;
						}
					}
					else if(deltMove<0)	{
						//mRotate += mRotateSpeed;
						if(mRotate%(360/QuadSize)<Math.abs(deltMove)&&Math.abs(mRotateSpeed)<4){
							mRotate -= mRotate%(360/QuadSize);
						}
						else{
							mRotate += deltMove;
						}
					}
					if(mRotate < 0) mRotate += 360;
					if(mRotate > 360) mRotate -= 360;					
					if(Math.abs(mRotateSpeed)<4&& mRotate%(360/QuadSize)==0)
					{
						mRotateDirect = 0;
						mIsRotate = false;
						mRotateSpeed = 0;
					//	if(Launcher.LOGD)Log.d("TextureView","mIsRotate = false ");
						mCurPageIndex = (int)(360-mRotate)/(360/QuadSize)%QuadSize;
						pageListener.onPageChange(mCurPageIndex, mCurPageIndex);

						if(mChangeState){
							ChangeAnimateState(RotateTexturesView.EState.EBACK);
							mChangeState = false;
						}

						((TextureSwitchActivity)mContext).GetHandler().sendEmptyMessage(TextureSwitchActivity.UpdateCurPageText_MSG);
					}
				}
			
			
						
				if(mMoveDistance!=0){			
	
						mRotateDirect = mMoveDistance;
						//mRotateSpeed = mMoveDistance*2/10;

						if(mRotateSpeed>20) mRotateSpeed = 20;
						if(mRotateSpeed<-20) mRotateSpeed = -20;
						mRotate += mMoveDistance/(mViewWidth/160);
						mMoveDistance = 0;
						
						if(mRotate < 0) mRotate += 360;
						if(mRotate > 360) mRotate -= 360;		
				}
	
				//mCurPageIndex = (int)(360-mRotate)/(360/QuadSize)%QuadSize;
				pageListener.onPageChange(mCurPageIndex, mCurPageIndex);
				


				//if( mRotateDirect!=0 || mIsRotate )	{	
					//Log.d("RotateTexturesView","requestRender: 00000000000");	
				//	requestRender();
				//}
					
				mTime = Time;


				int[] orderlist = new int[QuadSize];
				for( int i=0;i<QuadSize;++i){
					orderlist[i]=i;
				}


				for( int i=0;i<QuadSize;++i){
					for( int j=i+1;j<QuadSize;++j){
						float iz = (float)Math.cos(((360/QuadSize)*orderlist[i]+mRotate)*Math.PI/180);
						float jz = (float)Math.cos(((360/QuadSize)*orderlist[j]+mRotate)*Math.PI/180);
						if(jz<iz){
							int temp = orderlist[i];
							orderlist[i] = orderlist[j];
							orderlist[j] = temp;
						}
					}
				}
						
	
	//        			Comparator comp = new Zcomparator();
    //    				Collections.sort(orderlist,comp);  
						
						//for( int i=0;i<QuadSize;++i){
				for( int j=0;j<QuadSize;++j){
					int i=orderlist[j];
					gl.glPushMatrix();

					gl.glRotatef(i*(360/QuadSize)+mRotate, 0, 1, 0);	
					//gl.glTranslatef(0, 0, mRadius);

	
					double r = (i*(360/QuadSize)+mRotate+mAngleX)*Math.PI/180; 
					gl.glTranslatef(0, -mRingUp/2+mRingUp*(float)Math.sin(r/2)*(float)Math.sin(r/2), mRadius);			
			
					gl.glRotatef(-i*(360/QuadSize)-mRotate + mAngleX, 0, 1, 0); 
					gl.glRotatef(mAngleY, 1, 0, 0); 


					gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);			
					float alpha = Math.abs(((360/QuadSize)*i+mRotate)%360-180)/180*0.6f+0.4f;
					gl.glColor4f(alpha, alpha, alpha, alpha);






					//if(PageQuadback!=null)
					//	PageQuadback.Draw();
					//gl.glEnable(GL11.GL_BLEND);
					if(((i*(360/QuadSize)+mRotate)%360<20||(i*(360/QuadSize)+mRotate)%360>340)){
						//if(QuadSelected!=null)
							//QuadSelected.Draw();
						//Quad.drawMixed2D(QuadSelected, mQuadList.get(i),1);

						}
					else{
						//if(PageQuadback!=null)
							//PageQuadback.Draw();
						//Quad.drawMixed2D(PageQuadback, mQuadList.get(i),1);
					}

					if( i<mQuadList.size()){
						mQuadList.get(i).Draw();
					}
					else if(PageQuadback!=null){
						
						PageQuadback.Draw();
					}

					
					if(mSwapPage == -1){
						if(((i*(360/QuadSize)+mRotate)%360<20||(i*(360/QuadSize)+mRotate)%360>340)&&QuadSelected!=null){
							gl.glEnable(GL11.GL_BLEND);
							QuadSelected.Draw();
							gl.glDisable(GL11.GL_BLEND);
							mCurPageIndex = i;
						}
					}
					else{
						if(((i*(360/QuadSize)+mRotate)%360<(360/QuadSize)||(i*(360/QuadSize)+mRotate)%360>=(360-360/QuadSize))){
							gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
							gl.glEnable(GL11.GL_BLEND);
							QuadSelected.Draw();
							gl.glDisable(GL11.GL_BLEND);
							gl.glColor4f(1f, 1f, 1f, 1f);

						}
						
					}
	
					gl.glColor4f(1f, 1f, 1f, 1f);
					gl.glPopMatrix(); 

				}
				if(mSwapPage != -1 ){
					gl.glPushMatrix();
				
					double r = (mAngleX)*Math.PI/180; 
					gl.glTranslatef(0, -mRingUp/2+mRingUp*(float)Math.sin(r/2)*(float)Math.sin(r/2), mRadius);						
					gl.glRotatef(mAngleX, 0, 1, 0); 
					gl.glRotatef(mAngleY, 1, 0, 0);
					gl.glTranslatef(mSwapTrans,0,0);
					
					gl.glScalef(1.07f, 1.07f, 1.07f);
					//gl.glColor4f(0.7f, 0.7f, 0.7f, 0.7f);
					//gl.glEnable(GL11.GL_BLEND);
					mQuadList.get(mQuadList.size()-1).Draw();
					//gl.glDisable(GL11.GL_BLEND);
					//gl.glColor4f(1f, 1f, 1f, 1f);
					gl.glEnable(GL11.GL_BLEND);
					QuadSelected.Draw();
					gl.glDisable(GL11.GL_BLEND); 
					gl.glPopMatrix(); 


					//Log.d("RotateTexturesView","mSwapTrans"+mSwapTrans); 
					//Log.d("RotateTexturesView","mSwapdrag"+mSwapdrag); 

					//if( Math.abs(mSwapTrans - mSwapdrag)<0.00001){
					//	mRotateSpeed = -5;
					//	mIsRotate = true;
					//}
					//else if(Math.abs(mSwapTrans + mSwapdrag)<0.00001 ){
					//	mRotateSpeed = 5;
					//	mIsRotate = true;
					//}

				}
				if( mRotateDirect!=0 || mIsRotate ) {	
					//Log.d("RotateTexturesView","requestRender: 00000000000"); 
					requestRender();
				}
						
			}

		};


		@Override
		public void OnDrawFrameLandscape(GL10 gl){

										
			gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
					
					 
			// Now we're ready to draw some 3D objects
										
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();					
			GLU.gluLookAt(gl, mCameraLX+mCameraXoffsetAnim, mCameraLY+mCameraYoffsetAnim, mCameraLZ+mCameraZoffsetAnim, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
						
			//int QuadSize = mQuadList.size();
			int QuadSize = mQuadNum;

			if(mSwapPage!=-1) QuadSize = mQuadNum-1;
			if( QuadSize > 0 ){ 
				long Time = System.currentTimeMillis();
				//mRotate -=(Time - mTime)/10;
				if( mIsRotate){
					int a = 0;
					if(Math.abs(mRotateSpeed)<4){
						if( Math.abs(mRotate%(360/QuadSize))>(360/QuadSize)/2){
							mRotateSpeed = 3;
						}
						else{
							mRotateSpeed = -3;
						}
					}
					float f = 0;
					if(mRotateSpeed>3) f = -1;
					if(mRotateSpeed<-3) f = 1;
					
					Log.d("RotateTexturesView","mRotateSpeed: 111111111111111111111111	"+mRotateSpeed);

					long deltMove = (long)(mRotateSpeed*mDeltaTime/30 + (f *(mDeltaTime)*(mDeltaTime) + 2*a*(mDeltaTime)*(mDeltaTime))/900);
					mRotateSpeed += ((2*a*(mDeltaTime)+f*(mDeltaTime))/30);
					if(mRotateSpeed<3&&mRotateSpeed>0) mRotateSpeed = 3;
					if(mRotateSpeed>-3&&mRotateSpeed<0) mRotateSpeed = -3;


				
				//Log.d("RotateTexturesView","mDeltaTime: 111111111111111111111111  "+mDeltaTime);
				//Log.d("RotateTexturesView","deltMove: 111111111111111111111111  "+deltMove);
				//Log.d("RotateTexturesView","mRotateSpeed: 111111111111111111111111	"+mRotateSpeed);


					
					if(deltMove>0){
						//mRotate += mRotateSpeed;
						if(Math.abs((360/QuadSize)-mRotate%(360/QuadSize))<Math.abs(deltMove)&&Math.abs(mRotateSpeed)<4){
							mRotate += ((360/QuadSize)-mRotate%(360/QuadSize));
						}
						else{
							mRotate += deltMove;
						}
					}
					else if(deltMove<0) {
						//mRotate += mRotateSpeed;
						if(mRotate%(360/QuadSize)<Math.abs(deltMove)&&Math.abs(mRotateSpeed)<4){
							mRotate -= mRotate%(360/QuadSize);
						}
						else{
							mRotate += deltMove;
						}
					}
					if(mRotate < 0) mRotate += 360;
					if(mRotate > 360) mRotate -= 360;					
					if(Math.abs(mRotateSpeed)<4&& mRotate%(360/QuadSize)==0)
					{
						mRotateDirect = 0;
						mIsRotate = false;
						mRotateSpeed = 0;
					//	if(Launcher.LOGD)Log.d("TextureView","mIsRotate = false ");
						mCurPageIndex = (int)(360-mRotate)/(360/QuadSize)%QuadSize;
						pageListener.onPageChange(mCurPageIndex, mCurPageIndex);

						if(mChangeState){
							ChangeAnimateState(RotateTexturesView.EState.EBACK);
							mChangeState = false;
						}

						((TextureSwitchActivity)mContext).GetHandler().sendEmptyMessage(TextureSwitchActivity.UpdateCurPageText_MSG);
					}
				}
			
			
						
				if(mMoveDistance!=0){			
	
					mRotateDirect = mMoveDistance;
					//mRotateSpeed = mMoveDistance*2/10;

					if(mRotateSpeed>20) mRotateSpeed = 20;
					if(mRotateSpeed<-20) mRotateSpeed = -20;
					mRotate += mMoveDistance/3;
					mMoveDistance = 0;
					
					if(mRotate < 0) mRotate += 360;
					if(mRotate > 360) mRotate -= 360;		
				}
	
				//mCurPageIndex = (int)(360-mRotate)/(360/QuadSize)%QuadSize;
				pageListener.onPageChange(mCurPageIndex, mCurPageIndex);
				


				if( mRotateDirect!=0 || mIsRotate ) {	
					//Log.d("RotateTexturesView","requestRender: 00000000000"); 
					requestRender();
				}
					
				mTime = Time;



				int[] orderlist = new int[QuadSize];
				for( int i=0;i<QuadSize;++i){
					orderlist[i]=i;
				}


				for( int i=0;i<QuadSize;++i){
					for( int j=i+1;j<QuadSize;++j){
						
						//float iz = (float)Math.cos(((360/QuadSize)*orderlist[i]+mRotate)*Math.PI/180);
						//float jz = (float)Math.cos(((360/QuadSize)*orderlist[j]+mRotate)*Math.PI/180);
						float r = (orderlist[i]*(360/QuadSize)+mRotate + 360 -10)%360;
						float ix = (float)Math.cos(r*Math.PI/180);
						r = (orderlist[j]*(360/QuadSize)+mRotate + 360 -10)%360;
						float jx = (float)Math.cos(r*Math.PI/180);
						
						//if(jz<iz){
						if(jx<ix){
							int temp = orderlist[i];
							orderlist[i] = orderlist[j];
							orderlist[j] = temp;
						}
					}
				}
						
	
				for( int j=0;j<QuadSize;++j){
					int i=orderlist[j];
					gl.glPushMatrix();

					float r = (i*(360/QuadSize)+mRotate + 360 -10)%360;
					if(r<=180){
						r = r/2;					
					}
					else{
						r = (r-180)/ 2+ 270;
					}
					
					gl.glTranslatef(/*1f*Math.abs((i*(360/QuadSize)+mRotate)%360-180)/90+*/mRadiusLX*(float)Math.sin(r*Math.PI/180)+2*mRadiusLX*(float)Math.cos(r*Math.PI/180), 
						/*mRadiusLY*(float)Math.sin(r*Math.PI/180)+*/(i*(360/QuadSize)+mRotate + 180 -10)%360/360*mRadiusLY*2-mRadiusLY, 
						mRadiusLZ*(float)Math.cos(r*Math.PI/180)-mRadiusLZ);


					gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);			
					float alpha = (Math.abs(((360/QuadSize)*i+mRotate)%360-180))/180*0.9f+0.1f;
					gl.glColor4f(alpha, alpha, alpha, alpha);


					if( i<mQuadList.size()){
						mQuadList.get(i).Draw();
					}
					else if(PageQuadback!=null){
						
						PageQuadback.Draw();
					}


					if(mSwapPage == -1){
						if(((i*(360/QuadSize)+mRotate)%360<20||(i*(360/QuadSize)+mRotate)%360>340)&&QuadSelected!=null){
							gl.glEnable(GL11.GL_BLEND);
							QuadSelected.Draw();
							gl.glDisable(GL11.GL_BLEND);
							mCurPageIndex = i;
						}
					}
					else{
						if(((i*(360/QuadSize)+mRotate)%360<(360/QuadSize)||(i*(360/QuadSize)+mRotate)%360>=(360-360/QuadSize))){
							gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
							gl.glEnable(GL11.GL_BLEND);
							QuadSelected.Draw();
							gl.glDisable(GL11.GL_BLEND);
							gl.glColor4f(1f, 1f, 1f, 1f);

						}
						
					}
				
					//gl.glDisable(GL11.GL_BLEND);		
					gl.glColor4f(1f, 1f, 1f, 1f);
					gl.glPopMatrix(); 

				}
				if(mSwapPage != -1 ){
					gl.glPushMatrix();
								
					gl.glTranslatef(2*mRadiusLX, 
						(180 -10)%360*mRadiusLY*2/360-mRadiusLY, 
						0);

					gl.glTranslatef(0,mSwapTrans,0);
					
					gl.glScalef(1.07f, 1.07f, 1.07f);
					mQuadList.get(mQuadList.size()-1).Draw();
					gl.glEnable(GL11.GL_BLEND);
					QuadSelected.Draw();
					gl.glDisable(GL11.GL_BLEND); 
					gl.glPopMatrix(); 

				}

						
			}

		};

		@Override
		public boolean onTouchEvent(MotionEvent event){

				//Give everything to the gesture detector
				mGestureDetector.onTouchEvent(event);
				{

//				long timestamp = SystemClock.elapsedRealtime();
//				long delta = timestamp - mPrevTouchTime;
//				mPrevTouchTime = timestamp;

				switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
								
						//if(Launcher.LOGD)Log.d("RotateTexturesView","onTouchEvent ACTION_MOVE");

						if( mOrientation != Configuration.ORIENTATION_LANDSCAPE){
							if(mTouchX!=-1){
								mMoveDistance = (int)(event.getX()- mTouchX);
							}
							if(mSwapPage !=-1 ){
								mMoveDistance = -5*(int)(event.getX()- mTouchX);
								mSwapdrag = mViewWidth/4000f;
								mSwapTrans += 5*(event.getX()- mTouchX)/1000;
								
								if( mSwapTrans >mSwapdrag){
									mSwapTrans = mSwapdrag;
								}
								else if( mSwapTrans <-mSwapdrag ){
									mSwapTrans = -mSwapdrag;
								}
							}
							
						}
						else{
							if(mTouchY!=-1){
								mMoveDistance = -(int)(event.getY()- mTouchY)*2;
							}
							if(mSwapPage !=-1 ){
								mMoveDistance = 5*(int)(event.getY()- mTouchY)*2;
								mSwapdrag = mViewHeight/4000f;
								mSwapTrans -= 5*(event.getY()- mTouchY)/1000;
								if( mSwapTrans >mSwapdrag ){
									mSwapTrans = mSwapdrag;
								}
								else if( mSwapTrans <-mSwapdrag ){
									mSwapTrans = -mSwapdrag;
								}
							}
						}

	
						mTouchX = event.getX();		
						mTouchY = event.getY();
							break;
					case MotionEvent.ACTION_DOWN:
						mTouchX = event.getX();	
						mTouchY = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						mIsRotate = true;						
						mTouchX = -1;
						mTouchY= -1;
						if(mSwapPage !=-1 &&  mQuadList.size() > 1){
							int index = (int)((360-mRotate)/(360/(mQuadList.size()-1)));
							if( index >=0 && index<mQuadList.size()-1){						
								//mQuadList.set(mCurPageIndex, mQuadList.get(mSwapPage));
								//mQuadList.set(mSwapPage, quad);
								Quad quad = mQuadList.remove(mQuadList.size()-1);
								if(Launcher.LOGD)Log.d("RotateTexturesView","onTouchEvent swap page "+index);
								mQuadList.add(index+1,quad);
								onPageExchange(mSwapPage, index+1);
								mRotate = 360-360/mQuadList.size()*(index+1);
								mSwapPage = -1;

			
							}
							else if(index >= mQuadList.size()-1){
								onPageExchange(mSwapPage, -1);
								mRotate = 360-360/mQuadList.size()*(mQuadList.size()-1);
								mSwapPage = -1;

							}
							if(Launcher.LOGD)Log.d("RotateTexturesView","onTouchEvent from page "+mSwapPage);
							if(Launcher.LOGD)Log.d("RotateTexturesView","onTouchEvent to page "+index);
						}
						break;				
					}	
				requestRender();
				}
				return true;


		};

		@Override
		public boolean CanChangeState(){
			return true;
		};


		private class MyGesture implements GestureDetector.OnGestureListener{
	    /**
	     * {@inheritDoc}
	     */
	    public boolean onSingleTapUp(MotionEvent e) {
			//Log.d("TextureView","onSingleTapUp ACTION_UP");
			float x = e.getX();
			float y = e.getY();
			if( mOrientation != Configuration.ORIENTATION_LANDSCAPE){
				if( y>mViewHeight/4 && y<mViewHeight*3/4){	
					if(x>mViewWidth/4 && x<mViewWidth*3/4){
						mChangeState = true;
					}
					else if( x<mViewWidth/4  ){
						mRotateSpeed = 17-mQuadNum;
					}
					else if(x>mViewWidth*3/4){
						mRotateSpeed = -17+mQuadNum;
					}
				}
			}
			else{
				if(x>mViewWidth/4 && x<mViewWidth*3/4){
					if( y>mViewHeight/4 && y<mViewHeight*3/4){	
						mChangeState = true;
					}
					else if( y<mViewHeight/4  ){
						mRotateSpeed = -17+mQuadNum;
					}
					else if(y>mViewHeight*3/4){
						mRotateSpeed = 17-mQuadNum;
					}
				}	
			}
			return true;

	    }

	    /**
	     * {@inheritDoc}
	     */
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        
	   		//Log.d("TextureView","onFling velocityX" + velocityX);  
	   		//Log.d("TextureView","onFling velocityY" + velocityY);  
			if( mOrientation != Configuration.ORIENTATION_LANDSCAPE){
				mRotateSpeed = velocityX/(mViewWidth*mViewWidth/1500);
			}
			else{
				mRotateSpeed = -velocityY/(mViewHeight*mViewHeight/1500);
			}

			//Log.d("TextureView","onFling mRotateSpeed" + mRotateSpeed); 
	        return true;
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

	        
	        /*
	         * Now's a good time to tell our parent to stop intercepting our events!
	         * The user has moved more than the slop amount, since GestureDetector
	         * ensures this before calling this method. Also, if a parent is more
	         * interested in this touch's events than we are, it would have
	         * intercepted them by now (for example, we can assume when a Gallery is
	         * in the ListView, a vertical scroll would not end up in this method
	         * since a ListView would have intercepted it by now).
	         */
	       // mParent.requestDisallowInterceptTouchEvent(true);

	//		Log.d("TextureView","onScroll");
	        
	//		mMoveDistance = -(int)distanceX;


	        return true;
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    public boolean onDown(MotionEvent e) {

	        
	        // Must return true to get matching events for this down event.
	  //      Log.d("TextureView","onDown");
	        return true;
	    }

	    /**
	     * Called when a touch event's action is MotionEvent.ACTION_UP.
	     */
	    void onUp() {
	  //  	Log.d("TextureView","onUp");
	        mIsRotate = true;
	    }
	    
	    /**
	     * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	     */
	    void onCancel() {
	 //   	Log.d("TextureView","onCancel");
	        onUp();
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    public void onLongPress(MotionEvent e) {
	        if(Launcher.LOGD)Log.d("TextureView","onLongPress");
	 		if( mSwapPage == -1 && mQuadList.size() > 1){
				float x = e.getX();
				float y = e.getY();
				if( y>mViewHeight/4 && y<mViewHeight*3/4 && x>mViewWidth/4 && x<mViewWidth*3/4){
					if( mCurPageIndex>=0 && mCurPageIndex <mQuadList.size()){
						Quad quad = mQuadList.remove(mCurPageIndex);
						mQuadList.add(quad);
						mSwapPage = mCurPageIndex;
						mSwapTrans = 0;
						requestRender();
						performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
		                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
					}
				}
	 		}
	    }

	    // Unused methods from GestureDetector.OnGestureListener below
	    
	    /**
	     * {@inheritDoc}
	     */
	    public void onShowPress(MotionEvent e) {
	 //   	Log.d("TextureView","onShowPress");
	    }
		
		}

	public boolean onKeyDown(int keyCode) {
		if(mIsRotate)
			return true;
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
			{
				ChangeState();
				break;
			}
            case KeyEvent.KEYCODE_DPAD_UP:
            {
            	mIsRotate = true;
            	mRotateSpeed = -17+mQuadNum;
            	break;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN:
            {
                mIsRotate = true;
            	mRotateSpeed = 17-mQuadNum;
            	break;
            }
        }
        return true;
	}


		
	}




	
	private class StartAnimateState extends AnimateState{
		long mStartTime = 0;

		long mAnimateTime1 = 120;
		long mAnimateTime2 = 120;
		long mWaitTime1 = 300;			     
		long mWaitTime2 = 120;
		boolean  loaded = false;
		long framecount = 0;

		
		public StartAnimateState(){
			
			InitAnimate();
		}

		public void InitAnimate(){
			mStartTime = SystemClock.elapsedRealtime();

		};




		
		@Override
		public void OnDrawFrame(GL10 gl){			
			  // Usually, the first thing one might want to do is to clear
			  // the screen. The most efficient way of doing this is to use
			  //glClear().
			requestRender();			
			gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);			
			 
			 // Now we're ready to draw some 3D objects			  			
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();		
			GLU.gluLookAt(gl, mCameraX+mCameraXoffsetAnim, mCameraY+mCameraYoffsetAnim, mCameraZ+mCameraZoffsetAnim, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
			
			
			int QuadSize = mQuadList.size();

			if( QuadSize > 0 /*&& QuadSize == mQuadNum*//*&&mLoadViewNum>=mCurPageIndex*/){
				if(loaded==false){

					InitAnimate();
					mRotate = -(360/mQuadNum)*mCurPageIndex;
					if(mRotate < 0) mRotate += 360;
					if(mRotate > 360) mRotate -= 360;	
					loaded = true;

				}
				
				long systemtime = SystemClock.elapsedRealtime();
				
				if(mStartTime + mWaitTime1 + mAnimateTime1 + mWaitTime2+ mAnimateTime2- systemtime<0&&QuadSize == mQuadNum){
					ChangeAnimateState(RotateTexturesView.EState.EROUND);

				}


				QuadSize = mQuadNum;


				int[] orderlist = new int[QuadSize];
				for( int i=0;i<QuadSize;++i){
					orderlist[i]=i;
				}
		
				for( int i=0;i<QuadSize;++i){
					for( int j=i+1;j<QuadSize;++j){
						float iz = (float)Math.cos(((360/QuadSize)*orderlist[i]+mRotate)*Math.PI/180);
						float jz = (float)Math.cos(((360/QuadSize)*orderlist[j]+mRotate)*Math.PI/180);
						if(jz<iz){
							int temp = orderlist[i];
							orderlist[i] = orderlist[j];
							orderlist[j] = temp;
						}
					}
				}
				//for( int i=0;i<QuadSize;++i){
				for( int j=0;j<QuadSize;++j){
					int i=orderlist[j];

					gl.glPushMatrix();
				
					float dstx = mRadius*(float)Math.sin(((360/QuadSize)*i+mRotate)*Math.PI/180);
					float dstz = mRadius*(float)Math.cos(((360/QuadSize)*i+mRotate)*Math.PI/180);
					double r = (i*(360/QuadSize)+mRotate+mAngleX)*Math.PI/180;
					float dsty = mRingUp*(float)Math.sin(r/2)*(float)Math.sin(r/2);

					if(mStartTime +mWaitTime1 - systemtime>0){
						gl.glTranslatef(0, -mRingUp/2, mRadius);
					
					}
					else if(mStartTime +mWaitTime1+ mAnimateTime1 - systemtime>0){
						float transRatio = (float)(mStartTime + mWaitTime1+mAnimateTime1 - systemtime )/mAnimateTime1;
						gl.glTranslatef(0, -mRingUp/2+dsty*(1-transRatio), mRadius+(dstz-mRadius)*(1-transRatio));
					
					}
					else if(mStartTime+mWaitTime1 + mAnimateTime1 + mWaitTime2- systemtime>0){
						gl.glTranslatef(0, -mRingUp/2+dsty, dstz);
					
					}
					else{
						float transRatio = (float)(mStartTime +mWaitTime1+ mAnimateTime1+ + mWaitTime2 +mAnimateTime2- systemtime )/mAnimateTime2;
						if( transRatio<0 ) transRatio=0;
						gl.glTranslatef(dstx*(1-transRatio), -mRingUp/2+dsty, dstz);
									
					}
					gl.glRotatef( mAngleX, 0, 1, 0); 

								
							
					//		gl.glEnable(GL11.GL_BLEND);
					gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
					
					float alpha = Math.abs(((360/QuadSize)*i+mRotate)%360-180)/180*0.6f+0.4f;
					gl.glColor4f(alpha, alpha, alpha, alpha);

							

					if(((i*(360/QuadSize)+mRotate)%360<20||(i*(360/QuadSize)+mRotate)%360>340)){
						//if(QuadSelected!=null)
						//	QuadSelected.Draw();

					}
					else{
						//if(PageQuadback!=null)
						//	PageQuadback.Draw();
					}
		
					if( i<mQuadList.size()){
						mQuadList.get(i).Draw();
					}
					else{
						if(PageQuadback!=null)
							PageQuadback.Draw();
					}					
					gl.glDisable(GL11.GL_BLEND);
					gl.glColor4f(1f, 1f, 1f, 1f);
					gl.glPopMatrix(); 
				
				}

			}

			if(framecount<3)
			++framecount;

		};


		@Override
		public void OnDrawFrameLandscape(GL10 gl){			
			  // Usually, the first thing one might want to do is to clear
			  // the screen. The most efficient way of doing this is to use
			  //glClear().
			requestRender();			
			gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);			
			 
			 // Now we're ready to draw some 3D objects			  			
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();		
			GLU.gluLookAt(gl, mCameraLX+mCameraXoffsetAnim, mCameraLY+mCameraYoffsetAnim, mCameraLZ+mCameraZoffsetAnim, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
			
			
			int QuadSize = mQuadList.size();

			if( QuadSize > 0 /*&& QuadSize == mQuadNum*//*&&mLoadViewNum>=mCurPageIndex*/){
				if(loaded==false){

					InitAnimate();
					mRotate = -(360/mQuadNum)*mCurPageIndex;
					if(mRotate < 0) mRotate += 360;
					if(mRotate > 360) mRotate -= 360;	
					loaded = true;

				}
				
				long systemtime = SystemClock.elapsedRealtime();
				
				if(mStartTime + mWaitTime1 + mAnimateTime1 + mWaitTime2+ mAnimateTime2- systemtime<0&&QuadSize == mQuadNum){
					ChangeAnimateState(RotateTexturesView.EState.EROUND);

				}




				QuadSize = mQuadNum;


				int[] orderlist = new int[QuadSize];
				for( int i=0;i<QuadSize;++i){
					orderlist[i]=i;
				}
		
				for( int i=0;i<QuadSize;++i){
					for( int j=i+1;j<QuadSize;++j){
						float iz = (float)Math.cos(((360/QuadSize)*orderlist[i]+mRotate)*Math.PI/180);
						float jz = (float)Math.cos(((360/QuadSize)*orderlist[j]+mRotate)*Math.PI/180);
						if(jz<iz){
							int temp = orderlist[i];
							orderlist[i] = orderlist[j];
							orderlist[j] = temp;
						}
					}
				}
				//for( int i=0;i<QuadSize;++i){
				for( int j=0;j<QuadSize;++j){
					int i=orderlist[j];

					gl.glPushMatrix();



					float r = (i*(360/QuadSize)+mRotate + 360 -10)%360;
					if(r<=180){
						r = r/2;					
					}
					else{
						r = (r-180)/ 2+ 270;
					}
					

					
					float dstx = mRadiusLX*(float)Math.sin(r*Math.PI/180)+2*mRadiusLX*(float)Math.cos(r*Math.PI/180);
					float dsty = (i*(360/QuadSize)+mRotate + 180 -10)%360/360*mRadiusLY*2-mRadiusLY;
					float dstz = mRadiusLZ*(float)Math.cos(r*Math.PI/180)-mRadiusLZ;


					if(mStartTime +mWaitTime1 - systemtime>0){
						gl.glTranslatef(-mRingUp/2, 0, mRadius);

					}
					else{
						float transRatio = (float)(mStartTime +mWaitTime1+ mAnimateTime1+ + mWaitTime2 +mAnimateTime2- systemtime )/(mAnimateTime1+ + mWaitTime2 +mAnimateTime2);
						if( transRatio<0 ) transRatio=0;
						gl.glTranslatef( dstx*(1-transRatio), dsty*(1-transRatio),dstz*(1-transRatio));

					}

								
							
					gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
					
					float alpha = Math.abs(((360/QuadSize)*i+mRotate)%360-180)/180*0.6f+0.4f;
					gl.glColor4f(alpha, alpha, alpha, alpha);

							

					if(((i*(360/QuadSize)+mRotate)%360<20||(i*(360/QuadSize)+mRotate)%360>340)){
						//if(QuadSelected!=null)
						//	QuadSelected.Draw();

					}
					else{
						//if(PageQuadback!=null)
						//	PageQuadback.Draw();
					}
		
					if( i<mQuadList.size()){
						mQuadList.get(i).Draw();
					}
					else{
						if(PageQuadback!=null)
							PageQuadback.Draw();
					}					
					gl.glDisable(GL11.GL_BLEND);
					gl.glColor4f(1f, 1f, 1f, 1f);
					gl.glPopMatrix(); 
				
				}

			}

			if(framecount<3)
			++framecount;

		};


		
		
		@Override
		public boolean onTouchEvent(MotionEvent event){			
			return true;
		};

		public boolean onKeyDown(int keyCode) {
			return true;
		}

		@Override
		public boolean CanChangeState(){
			return true;
		};
	}
	
	private class BackAnimateState extends AnimateState{

//		long mStartTime = 0;
//		long mAnimateTime = 200;
//		long mWaitTime = 30;
		float transRatio = 1f;
		public BackAnimateState(){
		}
		public void InitAnimate(){

//			mStartTime = SystemClock.elapsedRealtime();
		};

		public void ReBindTexture(){
			if( mPageList !=null && mCurPageIndex<mPageList.size() ){ 
				Bitmap bitmap= mPageList.get(mCurPageIndex);
				if( bitmap !=null)
					mBackQuad = CreateQuadRealSize(bitmap,2*mViewWidth/mViewHeight,2f); 
			}
		}
		@Override
		public void OnDrawFrame(GL10 gl){
			if(transRatio ==0){
				if( back==false ) Back();
				back = true;	

			}
			/*
			if(mStartTime + mAnimateTime - SystemClock.elapsedRealtime() +mWaitTime <0 ){
			 	if(Launcher.LOGD) Log.d("BackAnimateState","mCurPageIndex  "+mCurPageIndex);
				if( back==false ) Back();
				back = true;						 
				// return; 
			 }
			 */

			if( back==false ) requestRender();
			  						
			gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			 
			  // Now we're ready to draw some 3D objects
			  
			
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();


			//float transRatio0 = (float)(mStartTime + mAnimateTime - SystemClock.elapsedRealtime())/mAnimateTime;
			//if(transRatio0<0) transRatio0 = 0.0f;
			transRatio -= 0.25f;
			if(transRatio<0) transRatio = 0.0f;

			GLU.gluLookAt(gl, mCameraX, mCameraY*transRatio*transRatio, mCameraZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);	

			if(Launcher.LOGD) Log.d("BackAnimateState","zzzzzzzzzzzzzzzzzzzzzzzzzzz: transRatio"+transRatio);


			int QuadSize = mQuadList.size();
			if( QuadSize > 0 ){											

				gl.glPushMatrix();
				
				//float transRatio = (float)(mStartTime + mAnimateTime - SystemClock.elapsedRealtime())/mAnimateTime;
				//if(transRatio<0) transRatio=0;
				gl.glTranslatef(0, 0, mCameraZ-5f-(mCameraZ-5-mRadius)*transRatio);
				if(Launcher.LOGD) Log.d("BackAnimateState","z: "+(mCameraZ-5-(mCameraZ-5-mRadius)*transRatio));

				if(mBackQuad !=null)
					mBackQuad.Draw();
				//if(Launcher.LOGD) Log.d("BackAnimateState","wRatio"+mBackQuad.wRatio);
				//if(Launcher.LOGD) Log.d("BackAnimateState","hRatio"+mBackQuad.hRatio);

				gl.glPopMatrix(); 
			
	
			}

		};


		@Override
		public void OnDrawFrameLandscape(GL10 gl){
			if(transRatio ==0){
				if( back==false ) Back();
				back = true;	

			}
			/*
			if(mStartTime + mAnimateTime - SystemClock.elapsedRealtime() +mWaitTime <0 ){
			 	if(Launcher.LOGD) Log.d("BackAnimateState","mCurPageIndex  "+mCurPageIndex);
				if( back==false ) Back();
				back = true;						 
				// return;
			 }
			 */

			if( back==false ) requestRender();
			  						
			gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			 
			  // Now we're ready to draw some 3D objects
			  
			
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();


			//float transRatio0 = (float)(mStartTime + mAnimateTime - SystemClock.elapsedRealtime())/mAnimateTime;
			//if(transRatio0<0) transRatio0 = 0.0f;
			transRatio -= 0.25f;
			if(transRatio<0) transRatio = 0.0f;			 

			GLU.gluLookAt(gl, mCameraLX*transRatio*transRatio, mCameraLY*transRatio*transRatio, mCameraLZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);	


			int QuadSize = mQuadList.size();

			
			if( QuadSize > 0 ){											
 
				gl.glPushMatrix();
				
				//float transRatio = (float)(mStartTime + mAnimateTime - SystemClock.elapsedRealtime())/mAnimateTime;
				//if(transRatio<0) transRatio=0;
				gl.glTranslatef(0, 0, mCameraLZ-5f-(mCameraLZ-5-mRadius)*transRatio);
				if(Launcher.LOGD) Log.d("BackAnimateState","z: "+(mCameraLZ-5-(mCameraLZ-5-mRadius)*transRatio));

				//( i<mQuadList.size())
				//QuadList.get(i).Draw();
				if(mBackQuad !=null)
					mBackQuad.Draw();

				gl.glPopMatrix(); 
			}
				
 

		};

		
		
		@Override
		public boolean onTouchEvent(MotionEvent event){
				
				return true;

		};

		public boolean onKeyDown(int keyCode) {
			return true;
		}	

		@Override
		public boolean CanChangeState(){
			return true;
		};
	};	


    private void processTouchEvent(){
        MotionEvent event = null;
//        int numEvents = mTouchEventQueue.size();
//        int i = 0;
        //do {
            // We look at the touch event queue and process one event at a time
            synchronized (mTouchEventQueue) {
                event = mTouchEventQueue.pollFirst();
            }
            if (event == null)
                return;

			if(mAnimaestate==null) return;
			mAnimaestate.onTouchEvent( event);

				
            event.recycle();
//            ++i;
        //} while (event != null && i < numEvents);
		
    }

    private void processKeyEvent(){
        Integer keycode = 0;
        int numEvents = 0;
        if(mKeyEventQueue!=null){
        	 numEvents = mKeyEventQueue.size();
        }
        if(numEvents<=0) return;
        if(Launcher.LOGD)Log.d(TAG,"processKeyEvent2 "+ keycode);
//        int i = 0;
        //do {
            // We look at the touch event queue and process one event at a time
            synchronized (mKeyEventQueue) {
                keycode = mKeyEventQueue.pollFirst();

                if(Launcher.LOGD)Log.d(TAG,"processKeyEvent "+ keycode);
            }
            if (keycode == null)
                return;

			if(mAnimaestate==null) return;
			mAnimaestate.onKeyDown( keycode);

//            ++i;
        //} while (event != null && i < numEvents);
		
    }

	private void UpdateBitmaps(){

		if(PageBackground!=null){

			PageQuadback = CreateQuad(PageBackground,2*mViewWidth/mViewHeight,2f);

			PageBackgroundCopy = PageBackground;
			PageBackground = null;

			boolean recyclecopy = PageBackgroundCopy.isRecycled();

			if(Launcher.LOGD)Log.d(TAG,"PageBackgroundCopy.isRecycled() "+ recyclecopy);
		}

		
		if(Selected!=null){
			if( mOrientation != Configuration.ORIENTATION_LANDSCAPE){
				QuadSelected = CreateQuad(Selected,2*mViewWidth/mViewHeight*(256f)/194f,2f*(512f)/436f);
			}
			else{
				QuadSelected = CreateQuad(Selected,2*mViewWidth/mViewHeight*(512f)/436f,2f*(256f)/194f);
			}
			Selected.recycle();
			Selected = null;

		}
		if(unSelected!=null){

			unQuadSelected = CreateQuad(unSelected,2*mViewWidth/mViewHeight*(256f)/194f,2f*(512f)/436f);
			unSelected.recycle();
			unSelected = null;

		}
		
		synchronized(mPageList){


		if(updateviews==false || mPageList == null ) return;


		int viewsize = mPageList.size();

		requestRender();
		if(viewsize==0) return;
	

		//mQuadNum = viewsize;
		if(removePgae>=0&&removePgae<mQuadList.size()){
			mPageList.remove(removePgae);
			
			mQuadList.get(removePgae).OnDestory();
			mQuadList.remove(removePgae);
			removePgae = -1;
			
			mLoadViewNum = mQuadList.size();
			mIsRotate = true;

			mQuadNum = mPageList.size();
			long timestamp = SystemClock.elapsedRealtime();
			mPrevFrameTime = timestamp;
			return;
		}
		if( addPgae>=0 ){
			Bitmap bitmap = Bitmap.createBitmap(PageBackgroundCopy);
			mPageList.add(bitmap);
			viewsize = mPageList.size();
			addPgae = -1;
			mIsRotate = true;
			mQuadNum = mPageList.size();

		}


		if( mLoadViewNum>=mQuadNum) {

			updateviews=false;
			if(mAnimaestate == mRoundAnimateState){
				((TextureSwitchActivity)mContext).addPage.setClickable(true);
				((TextureSwitchActivity)mContext).deletePage.setClickable(true);
			}
			System.gc();
			return;
		}

		if(mLoadViewNum>=mPageList.size()) return;

		Bitmap bitmap = mPageList.get(mLoadViewNum);

		if( bitmap == null) return;
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		GL11 gl = mGL;

		
		Quad quad;
		quad = new Quad(mGL,2*mViewWidth/mViewHeight,2f);


		String vendor="Imagination Technologies"; 		//for TI
		if(vendor.equals(gl.glGetString(GL11.GL_VENDOR))){

			// Create a padded bitmap if the natural size is not a power of
			// 2.	
			if (!isPowerOf2(width) || !isPowerOf2(height)) {
				int paddedWidth = nextPowerOf2(width);
				int paddedHeight = nextPowerOf2(height);
				if( paddedWidth>mViewWidth ) paddedWidth = prevPowerOf2(width);
				if( paddedHeight>mViewHeight ) paddedHeight = prevPowerOf2(height);
			
				//if(paddedWidth>256)paddedWidth = 256; 
				//if(paddedHeight>256)paddedHeight = 256; 
//				Bitmap.Config config = bitmap.getConfig();
				//Bitmap padded = Bitmap.createScaledBitmap(bitmap,paddedWidth, paddedHeight, true);				
				Bitmap padded = Bitmap.createScaledBitmap(PageBackgroundCopy,paddedWidth, paddedHeight, true);
				Canvas canvas = new Canvas(padded);
				Rect srcR = new Rect(0, 0, width, height);
        		Rect dstR = new Rect(0, 0, paddedWidth, paddedHeight);
				canvas.drawBitmap( bitmap, srcR, dstR ,null);
				quad.BindBitmap(padded);
	//			if( mLoadViewNum < mPageList.size())
	//				mPageList.set(mLoadViewNum,padded);
	//			else{
	//				if(Launcher.LOGD)Log.d(TAG,"mLoadViewNum>mPageList.size()" + mLoadViewNum +" " +mPageList.size());
	//			}
				padded.recycle();
				padded = null;
				width = paddedWidth;
				height = paddedHeight;
	
			}
			else{
				quad.BindBitmap(bitmap);
			}


		}
		else{
			Bitmap padded = Bitmap.createScaledBitmap(PageBackgroundCopy,width/2, height/2, true);
			Canvas canvas = new Canvas(padded);
			Rect srcR = new Rect(0, 0, width, height);
        	Rect dstR = new Rect(0, 0, width/2, height/2);
			canvas.drawBitmap( bitmap, srcR, dstR ,null);
			quad.BindBitmap(padded);
			padded.recycle();			

		}


		

		//bitmap.recycle();
		bitmap = null;
		
		mQuadList.add(quad);	

		//if(Launcher.LOGD)Log.d(TAG,"UpdateBitmaps width"+width);
		//if(Launcher.LOGD)Log.d(TAG,"UpdateBitmaps height"+height);

/*		
		// clear buffers

		gl.glViewport(0, 0, width, height);
			
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GLU.gluLookAt(gl, 0, 0, 5.0001f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);


		gl.glRotatef( 180f, 1f, 0, 0); 

		int[] texmax= new int[1];
		gl.glGetIntegerv(GL11.GL_MAX_TEXTURE_UNITS,texmax,0);
		if( texmax[0] >=2)	{	
			Quad.drawMixed2D(PageQuadback, mQuadList.get(mLoadViewNum),1);
			if(Launcher.LOGD)Log.d(TAG,"texmax "+texmax[0]);
		}
		else{
			PageQuadback.Draw();
			gl.glEnable(GL11.GL_BLEND); 
			mQuadList.get(mLoadViewNum).Draw(); 
			gl.glDisable(GL11.GL_BLEND);

		}

		// unbind FBO
		//glext.glBindFramebufferOES(GLES11Ext.GL_FRAMEBUFFER_OES, 0);
		//gl.glDeleteTextures(1, mQuadList.get(mLoadViewNum).mTextureId, 0);
		//mQuadList.get(mLoadViewNum).mTextureId[0] = textureId[0];
			
		gl.glBindTexture(GL11.GL_TEXTURE_2D,mQuadList.get(mLoadViewNum).mTextureId[0]);
		if( mOrientation != Configuration.ORIENTATION_LANDSCAPE)
			gl.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,0, 0, width, height*9/10);
		else
			gl.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,0, 0, width, height);
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);		

		gl.glViewport(0, 0, (int)mViewWidth, (int)mViewHeight);	
		
		*/

		
		++mLoadViewNum;


		long timestamp = SystemClock.elapsedRealtime();
		mPrevFrameTime = timestamp;


			}
	}




	public int getCurPageIndex() {
	    return mCurPageIndex;
	}

	public void	RemoveCurPage(){
		removePgae = mCurPageIndex;
	}

	public void AddPage(){
		addPgae = 1;
	}

	public void UpdatePages( boolean bupdate){
		updateviews = bupdate;
		if(updateviews) requestRender();
	}

	public void SetPageCount( int count){
		mQuadNum = count;
		if(Launcher.LOGD)Log.d(TAG,"Pagecount"+mQuadNum);

	}

	boolean ignoreSensor = false;
	public void setIgnoreSensor(boolean flag){
		ignoreSensor = flag;
	}
	
	public final class SensorListener implements SensorEventListener {
		float mPrevShakeValueX = 0;
//		float mPrevShakeValueY = 0;
		float mPrevShakeValueZ = 0;
		static final float SHAKE = 3; 
		/** Indicates that a sensor value has changed. */
		public void onSensorChanged(SensorEvent event) {
			
			if(ignoreSensor){
				return ;
			}
			
			final int type = event.sensor.getType();
			//if(true) return;
			if ( type == Sensor.TYPE_ACCELEROMETER || type == Sensor.TYPE_ORIENTATION) {
//				final SensorEvent e = event;				
				if (mAnimaestate == mRoundAnimateState){
				switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					float[] values = event.values;
					//if(Launcher.LOGD)Log.d(TAG,"values "+values);
					//if(Launcher.LOGD)Log.d(TAG,"values[0]"+values[0]);
					//if(Launcher.LOGD)Log.d(TAG,"values[1]"+values[1]);
					
					if( Float.isNaN(values[0]) || Float.isNaN(values[1])){
						if(Launcher.LOGD) Log.d(TAG,"values[0]: "+values[0]);
						return;
					}
					//float valueToUse = (mCamera.mWidth < mCamera.mHeight) ? values[0] : -values[1];
					float tiltValueX = values[0];
					if (Math.abs(tiltValueX) < 2.5f)
						tiltValueX = 0.0f;

						
					float tiltValueY = values[1];
					if (Math.abs(tiltValueY) < 2.5f)
						tiltValueY = 0.0f;
						
					float tiltValueZ = values[2];
					if (Math.abs(tiltValueZ) < 2.5f)
						tiltValueZ = 0.0f;

						

					if (tiltValueX != 0f||tiltValueY!=0f)
						requestRender();

					if( mOrientation != Configuration.ORIENTATION_LANDSCAPE){
						mCameraXoffset =  tiltValueX*0.5f;
						mCameraYoffset =  tiltValueY*0.3f;

						//if(Launcher.LOGD)Log.d(TAG,"tiltValueX"+tiltValueX );

						//if(Launcher.LOGD)Log.d(TAG,"Math.abs(mPrevShakeValueX - tiltValueX)"+Math.abs(mPrevShakeValueX - tiltValueX));
						if((mPrevShakeValueX >=0&&tiltValueX>mPrevShakeValueX)||(mPrevShakeValueX <=0&&tiltValueX<mPrevShakeValueX)){
							if(  mIsRotate == false && Math.abs(mPrevShakeValueX - tiltValueX)>SHAKE &&  Math.abs(mPrevShakeValueZ- tiltValueZ)<SHAKE/2){
								mIsRotate = true;
								if( mPrevShakeValueX - tiltValueX>0  ){
									mRotateSpeed = -17+mQuadNum;
								}
								else {
									mRotateSpeed = 17-mQuadNum;
								}
								if(Launcher.LOGD)Log.d(TAG,"Math.abs(mPrevShakeValueX )"+mPrevShakeValueX );
								if(Launcher.LOGD)Log.d(TAG,"Math.abs(tiltValueX)"+tiltValueX );
							}
						}


					}
//					else{
//						mCameraXoffset =  tiltValueY*0.5f;
//						mCameraYoffset =  tiltValueX*0.1f;
//					}


					mPrevShakeValueX = tiltValueX;
					//mPrevShakeValueY = tiltValueY;
					mPrevShakeValueZ = tiltValueZ;

					//if(Launcher.LOGD)Log.d(TAG,"tiltValueX "+tiltValueX);
					//if(Launcher.LOGD)Log.d(TAG,"tiltValueY "+tiltValueY);
					//if(Launcher.LOGD)Log.d(TAG,"tiltValueZ "+tiltValueZ);


					break;
				/*case Sensor.TYPE_ORIENTATION:


					break;
					*/

				}


				}
			}
		}
		    /** Indicates that the accuracy of a sensor value has changed. */
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	}
	}

	 private final void UpdateAnim(){
		if( mCameraXoffset!= mCameraXoffsetAnim ||mCameraYoffset!= mCameraYoffsetAnim ||mCameraZoffset!= mCameraZoffsetAnim ){
			mCameraXoffsetAnim = animateAfterFactoringSpeed( mCameraXoffsetAnim,mCameraXoffset,0.3f);
			mCameraYoffsetAnim = animateAfterFactoringSpeed( mCameraYoffsetAnim,mCameraYoffset,0.3f);
			mCameraZoffsetAnim = animateAfterFactoringSpeed( mCameraZoffsetAnim,mCameraZoffset,0.3f);
			requestRender();
		}
	
	 }


	 private static final float animateAfterFactoringSpeed(float prevVal, float targetVal, float timeElapsed) {
        if (prevVal == targetVal)
            return targetVal;
        float newVal = prevVal + ((targetVal - prevVal) * timeElapsed);
        if (Math.abs(newVal - prevVal) < 0.0001f)
            return targetVal;
        if (newVal == prevVal) {
            return targetVal;
        } else { // } else if (...) { ... }; no need for a new level of
                 // indentation.
            if (prevVal > targetVal && newVal < targetVal) {
                return targetVal;
            } else if (prevVal < targetVal && newVal > targetVal) {
                return targetVal;
            } else {
                return newVal;
            }
        }
    }



	public void SetOrientation( int orientation ){
		mOrientation = orientation;
		if (mAnimaestate == mRoundAnimateState){
			Back();
		}
	}


	public void onPageExchange(int oldPos, int newPos) {
	   
		Intent intent = new Intent(Launcher.INTENT_CHANGE_SCREEN);
		int[] inArray = new int[2];
		inArray[0] = oldPos;
		inArray[1] = newPos;
		intent.putExtra(Launcher.BUNDLE_CHANGE_SCREEN_INFO, inArray); 
		mContext.sendBroadcast(intent);
	}

	 
}
	

		



