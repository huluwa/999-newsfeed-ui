package com.borqs.omshome25;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import android.opengl.GLU;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLDisplay;


import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.View;


import android.util.Log;
import android.graphics.BitmapFactory;

import android.graphics.Bitmap;
import java.io.IOException;
import java.io.InputStream;


import java.util.List;
import java.util.ArrayList;
import android.os.Process;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import com.borqs.omshome25.Quad;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.util.AttributeSet;






class TexturesRenderView extends GLSurfaceView implements GLSurfaceView.Renderer{

	public static final String TAG = "TextureRenderView";

	protected float mViewWidth = 0;
	protected float mViewHeight = 0;

    protected GL11 mGL = null;

	protected ArrayList<Bitmap> mPageList = null;
	protected ArrayList<Quad> mQuadList = new ArrayList<Quad>();
	protected List<View> mViewList = new ArrayList<View>();

	protected int mQuadNum = 0;
//	private int mFirstLoadIndex = 0;



    public TexturesRenderView(Context context) {
        super(context);
		setEGLConfigChooser(new SampleEGLConfigChooser());
        setRenderer(this);
       
		if(Launcher.LOGD)Log.d(TAG,"TextureView");

    }
	public TexturesRenderView(Context context,AttributeSet attrs){
		super(context, attrs);
		//setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		//setEGLContextFactory(new ContextFactory());
		setEGLConfigChooser(new SampleEGLConfigChooser());
        setRenderer(this);

		if(Launcher.LOGD)Log.d(TAG,"TextureView");

		

	}


	public void GetBitmaps( ArrayList<Bitmap> bitmaplist){
		mPageList = bitmaplist;
		//mQuadNum = bitmaplist.size();

		/*
		if(Launcher.LOGD)Log.i("TexturesRenderView","mPageList.size() "+ mPageList.size());

		mPageList.clear();
		for(int i=0;i<bitmaplist.size();++i){
			Bitmap bitmap = bitmaplist.get(i);
			mPageList.add(bitmap);	
			bitmap = null;
		}
		bitmaplist.clear();
		bitmaplist = null;
		mQuadNum = mPageList.size();
		*/
		
	}


	
	public void BitmapFromView( List<View> viewlist ){
		mViewList = viewlist;
		mQuadNum = viewlist.size();




/*
			synchronized (mPageList) {
            
				mPageList.clear();
				if(Launcher.LOGD)Log.i("TexturesRenderView","BitmapFromView ");
				for(int i=0;i<viewlist.size();++i){

					View view = viewlist.get(i);
					//view.destroyDrawingCache();
					if(Launcher.LOGD)Log.i("TexturesRenderView","setDrawingCacheEnabled ");
					view.setDrawingCacheEnabled(true);
					if(Launcher.LOGD)Log.i("TexturesRenderView","invalidate ");
					view.invalidate();
					if(Launcher.LOGD)Log.i("TexturesRenderView","buildDrawingCache ");
					view.buildDrawingCache();

					if(Launcher.LOGD)Log.i("TexturesRenderView","view.getDrawingCache(): ");
					mPageList.add(view.getDrawingCache());	


				}
			}
*/		

	}
	
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);		
		if(Launcher.LOGD)Log.d(TAG, "onSurfaceCreated" );
        if (mGL == null) {
            mGL = (GL11)gl;
			if(Launcher.LOGD)Log.d(TAG, "onSurfaceCreated " + mGL );
        }else {
            // The GL Object has changed.
            if(Launcher.LOGD)Log.d(TAG, "GLObject has changed from " + mGL + " to " + gl);
            mGL = (GL11)gl;
        }

        // Increase the priority of the render thread.
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

        // Disable unused state.
        gl.glEnable(GL11.GL_DITHER);
        gl.glDisable(GL11.GL_LIGHTING);

        // Set global state.
        gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

        // Enable textures.
        gl.glEnable(GL11.GL_TEXTURE_2D);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

        // Set up state for multitexture operations. Since multitexture is
        // currently used
        // only for layered crossfades the needed state can be factored out into
        // one-time
        // initialization. This section may need to be folded into drawMixed2D()
        // if multitexture
        // is used for other effects.

        // Enable Vertex Arrays
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glClientActiveTexture(GL11.GL_TEXTURE1);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glClientActiveTexture(GL11.GL_TEXTURE0);

        // Enable depth test.
       // gl.glEnable(GL11.GL_DEPTH_TEST);
       // gl.glDepthFunc(GL11.GL_LEQUAL);

        // Set the blend function for premultiplied alpha.
        gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		gl.glAlphaFunc(GL11.GL_GREATER,	0.1f); 


        // Set the background color.
       // gl.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
		gl.glClearColor(0,0,0,0);


		gl.glHint(GL11.GL_POLYGON_SMOOTH_HINT,GL11.GL_NICEST);
		gl.glEnable(GL11.GL_LINE_SMOOTH);
		gl.glHint(GL11.GL_LINE_SMOOTH_HINT,GL11.GL_NICEST);

		//gl.glEnable(GL11.GL_BLEND);

		int[]  bufs={0},samples={0};
		
		gl.glGetIntegerv(GL11.GL_SAMPLE_BUFFERS, bufs,0);
		gl.glGetIntegerv(GL11.GL_SAMPLES, samples,0);
		if(Launcher.LOGD)Log.d(TAG, "bufs"+bufs[0] );
		if(Launcher.LOGD)Log.d(TAG, "samples"+samples[0] );

		
        gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

        // Do nothing special.
       // gl.glEnable(GL10.GL_CULL_FACE);
		//gl.glEnable(GL10.GL_DEPTH_TEST);


		if (mQuadList != null) {
			for( int i=0;i<mQuadList.size();++i){
				mQuadList.get(i).OnDestory();
			}
			mQuadList.clear();
		}
		//System.gc();

		
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
		if(mGL!=gl){
            if(Launcher.LOGD)Log.d(TAG, "GLObject has changed from " + mGL + " to " + gl);
            mGL = (GL11)gl;

		}
        gl.glViewport(0, 0, w, h);

        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */

        float ratio = (float) w / h;
        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 5, 50);

//		if(h>=w){
			mViewWidth = w;
			mViewHeight = h;
//		}
//		else{
//			mViewWidth = h;
//			mViewHeight = w;
//		}
		//if(Launcher.LOGD)Log.d(TAG, "requestRender" );
    }

	public void onDrawFrame(GL10 gl) {
		if(mGL!=gl){
            if(Launcher.LOGD)Log.d(TAG, "GLObject has changed from " + mGL + " to " + gl);
            mGL = (GL11)gl;

		}
		// UpdateBitmaps();
    }

	public void surfaceDestroyed(SurfaceHolder holder){

		mGL = null;
		if(Launcher.LOGD)Log.d(TAG,"surfaceDestroyed" );
		super.surfaceDestroyed(holder);
		for( int i=0;i<mQuadList.size();++i){
			mQuadList.get(i).OnDestory();
		}
		mQuadList.clear();
		if (mPageList != null) {
			mPageList.clear();
		}
		if(Launcher.LOGD)Log.d(TAG,"surfaceDestroyed mQuadList.clear()");

		System.gc();
	}




    public static final Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        int width = maxSize;
        int height = maxSize;
        boolean needsResize = false;
        if (srcWidth > srcHeight) {
            if (srcWidth > maxSize) {
                needsResize = true;
                height = ((maxSize * srcHeight) / srcWidth);
            }
        } else {
            if (srcHeight > maxSize) {
                needsResize = true;
                width = ((maxSize * srcWidth) / srcHeight);
            }
        }
        if (needsResize) {
            Bitmap retVal = Bitmap.createScaledBitmap(bitmap, width, height, true);
			bitmap.recycle();
			bitmap= null;
            return retVal;
        } else {
            return bitmap;
        }
    }


	
    public static boolean isPowerOf2(int n) {
        return (n & -n) == n;
    }

    public static int nextPowerOf2(int n) {
        n -= 1;
        n |= n >>> 16;
        n |= n >>> 8;
        n |= n >>> 4;
        n |= n >>> 2;
        n |= n >>> 1;
        return n + 1;
    }

    public static int prevPowerOf2(int n) {
        if (isPowerOf2(n)) {
            return nextPowerOf2(n);
        } else {
            return nextPowerOf2(n/2);
        }
    }


	public Quad CreateQuad( Bitmap bitmap ,float w, float h){

			if (bitmap != null) {
			
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();

					Quad quad;

					// Create a padded bitmap if the natural size is not a power of
					// 2.
					if (!isPowerOf2(width) || !isPowerOf2(height) ) {
						int paddedWidth = nextPowerOf2(width);
						int paddedHeight = nextPowerOf2(height);

						if( paddedWidth>mViewWidth ) paddedWidth = prevPowerOf2(width);
						if( paddedHeight>mViewHeight ) paddedHeight = prevPowerOf2(height);

//						Bitmap.Config config = bitmap.getConfig();
						//Log.i(TAG,"~~~~~~~~~~~~~~~~~~~~~~config "+ bitmap.getConfig());


						Bitmap padded = Bitmap.createScaledBitmap(bitmap,paddedWidth, paddedHeight, true);
						
						quad = new Quad(mGL,w,h);
						quad.BindBitmap(padded);

						padded.recycle();
						//bitmap = padded;

					}
					else{
						quad = new Quad(mGL,w,h);
						quad.BindBitmap(bitmap);

					}

					//bitmap.recycle();
					//bitmap = null;

					return quad;

			}
			else{
				return null;
			}

	}

	public Quad CreateQuadRealSize( Bitmap bitmap ,float w, float h){

		if (bitmap != null && bitmap.isRecycled()==false) {
		
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();

				Quad quad;

				String vendor="Imagination Technologies"; 		//for TI
				if(vendor.equals(mGL.glGetString(GL11.GL_VENDOR))){

					// Create a padded bitmap if the natural size is not a power of
					// 2.
					if (!isPowerOf2(width) || !isPowerOf2(height) ) {
						int paddedWidth = nextPowerOf2(width);
						int paddedHeight = nextPowerOf2(height);


//						Bitmap.Config config = bitmap.getConfig();

	                    Bitmap padded = Bitmap.createBitmap(paddedWidth, paddedHeight,Bitmap.Config.ARGB_8888);
	                    Canvas canvas = new Canvas(padded);
	                    canvas.drawBitmap(bitmap, 0, 0, null);
						
						
						quad = new Quad(mGL,w,h,(float)width/(float)paddedWidth,(float)height/(float)paddedHeight);
						quad.BindBitmap(padded);
						padded.recycle();
						padded = null;
					
					}
					else{
						quad = new Quad(mGL,w,h);
						quad.BindBitmap(bitmap);
					}
				}
				else{
					quad = new Quad(mGL,w,h);
					quad.BindBitmap(bitmap);
				}
				
				//bitmap.recycle();
				//bitmap = null;

				return quad;

		}
		else{
			return null;
		}

}



	/*private void UpdateBitmaps(){
		//if(true) return;
        //QuadReference quadReference;
//		int[] textureId=new int[1];
		
        while ((quadReference = (QuadReference) QuadReference.mUnreferencedQuadQueue.poll()) != null) {
            textureId[0] = quadReference.textureId;
            GL11 gl = mGL;
            if (gl != null ) {
                gl.glDeleteTextures(1, textureId, 0);
				if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~glDeleteTextures: "+ textureId[0]);
            }
        }
		
             if (mPageList != null ) {


				if(mPageList.size()==0) return;
				
				for( int i=0;i<mQuadList.size();++i){
					mQuadList.get(i).OnDestory();
				}
				mQuadList.clear();
				
				
				//for( int i=0;i<mPageList.size();++i){
				if(mPageList.size()>0){
			
					//Bitmap bitmap = mPageList.get(i);
					Bitmap bitmap = mPageList.remove(0);


					if (bitmap != null&&bitmap.isRecycled()==false) {



						
						int width = bitmap.getWidth();
						int height = bitmap.getHeight();

						Quad quad;

						// Create a padded bitmap if the natural size is not a power of
						// 2.
						if (!isPowerOf2(width) || !isPowerOf2(height)|| width>256 ||height>512) {
							int paddedWidth = nextPowerOf2(width);
							int paddedHeight = nextPowerOf2(height);

							if(paddedWidth>512)paddedWidth = 256; 
							if(paddedHeight>512)paddedHeight = 512; 
							Bitmap.Config config = bitmap.getConfig();
							Log.i(TAG,"~~~~~~~~~~~~~~~~~~~~~~config "+ bitmap.getConfig());


							Bitmap padded = Bitmap.createScaledBitmap(bitmap,paddedWidth, paddedHeight, true);
							

							bitmap.recycle();
							bitmap = padded;

						}
						else{

						}

						quad = new Quad(mGL,2*mViewWidth/mViewHeight,2f);
						quad.BindBitmap(bitmap);
						bitmap.recycle();
						bitmap = null;

						mQuadList.add(quad);	


						if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~new Quad"+ quad.mTextureId[0]);


					}
					

					
				}
				if(mPageList.size()<1)
					mPageList.clear();



            }
	}*/
/*
    private static final class QuadReference extends WeakReference<Quad> {
        @SuppressWarnings("unchecked")
        public QuadReference(Quad quad, ReferenceQueue referenceQueue,int textureId) {
            super(quad, referenceQueue);
			this.textureId = textureId;
        	}	
		public final int textureId;
    	static ReferenceQueue mUnreferencedQuadQueue = new ReferenceQueue();
    }
    */


public final class Deque<E extends Object> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private E[] mArray;
    private int mHead = 0;
    private int mTail = 0;

    @SuppressWarnings("unchecked")
    public Deque() {
        mArray = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    @SuppressWarnings("unchecked")
    public Deque(int initialCapacity) {
        // CR: check initialCapacity & (initialCapacity - 1) == 0.
        mArray = (E[]) new Object[initialCapacity];
    }

    public boolean isEmpty() {
        return mHead == mTail;
    }

    public int size() {
        return (mTail - mHead) & (mArray.length - 1); // CR: wtf?!? this
                                                      // definitely needs a
                                                      // comment.
    }

    public void clear() {
        E[] array = mArray;
        int head = mHead;
        int tail = mTail;
        if (head != tail) {
            int mask = array.length - 1;
            do {
                array[head] = null;
                head = (head + 1) & mask;
            } while (head != tail);
            mHead = 0;
            mTail = 0;
        }
    }

    public E get(int index) {
        E[] array = mArray;
        if (index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return array[(mHead + index) & (array.length - 1)];
    }

    public void addFirst(E e) {
        E[] array = mArray;
        int head = (mHead - 1) & (array.length - 1);
        mHead = head;
        array[head] = e;
        if (head == mTail) {
            expand();
        }
    }

    public void addLast(E e) {
        E[] array = mArray;
        int tail = mTail;
        array[tail] = e;
        tail = (tail + 1) & (array.length - 1);
        mTail = tail;
        if (mHead == tail) {
            expand();
        }
    }

    public E pollFirst() {
        E[] array = mArray;
        int head = mHead;
        E result = array[head];
        if (result == null) {
            return null;
        }
        array[head] = null;
        mHead = (head + 1) & (array.length - 1);
        return result;
    }

    public E pollLast() {
        E[] array = mArray;
        int tail = (mTail - 1) & (array.length - 1);
        E result = array[tail];
        if (result == null) {
            return null;
        }
        array[tail] = null;
        mTail = tail;
        return result;
    }

    @SuppressWarnings("unchecked")
    private void expand() {
        // Must be called only when head == tail.
        E[] array = mArray;
        int head = mHead;
        int capacity = array.length;
        int rightSize = capacity - head;
        int newCapacity = capacity << 1;
        Object[] newArray = new Object[newCapacity];
        System.arraycopy(array, head, newArray, 0, rightSize);
        System.arraycopy(array, 0, newArray, rightSize, head);
        mArray = (E[]) newArray;
        mHead = 0;
        mTail = capacity;
    }
}


	private static class SampleEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {

        private int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;


		protected int mSampleBuffer;
		protected int mSamples;
		protected int[] mConfigSpec;
		private static int EGL_OPENGL_ES2_BIT = 4;


		public SampleEGLConfigChooser() {
			mRedSize = 8;
        	mGreenSize = 8;
        	mBlueSize = 8;
        	mAlphaSize = 8;
			mSampleBuffer = 1;
			mSamples = 4;
			mDepthSize = 0;
			mStencilSize = 0;
			//mEGLRenderType = EGL_OPENGL_ES2_BIT;
			mValue = new int[1];
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config);
		
			int numConfigs = num_config[0];
		
			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}
		
			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
					num_config);
			EGLConfig config = chooseConfig(egl, display, configs);
			if (config == null) {
				throw new IllegalArgumentException("No config chosen");
			}
			return config;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {
		
			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}


        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs) {
            EGLConfig closestConfig = null;
            int closestDistance = 1000;
            for(EGLConfig config : configs) {
				//int t = findConfigAttrib(egl, display, config,
                //        EGL10.EGL_RENDERABLE_TYPE, 0);
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);
				//if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EGL_RENDERABLE_TYPE: "+ t);



                if ( /*t== EGL_OPENGL_ES2_BIT &&*/ d >= mDepthSize && s>= mStencilSize) {
                    int r = findConfigAttrib(egl, display, config,
                            EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config,
                             EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config,
                              EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config,
                            EGL10.EGL_ALPHA_SIZE, 0);

					int sb = findConfigAttrib(egl, display, config,
                        EGL10.EGL_SAMPLE_BUFFERS, 0);
					int samples = findConfigAttrib(egl, display, config,
                        EGL10.EGL_SAMPLES, 0);
					
                    int distance = Math.abs(r - mRedSize)
                                + Math.abs(g - mGreenSize)
                                + Math.abs(b - mBlueSize)
                                + Math.abs(a - mAlphaSize)
								+ Math.abs(sb - mSampleBuffer)
								+ Math.abs(samples - mSamples);


                    if (distance < closestDistance ) {
                        closestDistance = distance;
                        closestConfig = config;
						
						if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~distance: "+ distance);
						if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~config: "+ config);
					
                    }
                }
            }
			if(Launcher.LOGD)Log.d(TAG,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~closestConfig: "+ closestConfig);
            return closestConfig;
        }
	}
	

	private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
			Log.w(TAG, "creating OpenGL ES 2.0 context");
			checkEglError("Before eglCreateContext", egl);
			int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
			EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
			checkEglError("After eglCreateContext", egl);
			return context;
		}

		public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
			egl.eglDestroyContext(display, context);
		}
	}

	private static void checkEglError(String prompt, EGL10 egl) {
		int error;
		while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
			Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
		}
	}




}

