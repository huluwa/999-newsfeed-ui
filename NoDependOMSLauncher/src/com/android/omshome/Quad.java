

package com.android.omshome;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.lang.String;

import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLUtils;
import android.util.Log;

//import com.jme.Math.Vector3f;
import android.graphics.Bitmap;


public class Quad  {
	private GL11 mGL; 
	//protected Vector3f worldTranslation;
	
	protected FloatBuffer mFVertexBuffer;
	protected FloatBuffer mTexBuffer;
	protected ShortBuffer mIndexBuffer;	


	public String mName="";

    protected float width = 1;    
    protected float height = 1;

    public float wRatio = 1f;    
    public float hRatio = 1f;	
    
	private final static int VERTS = 4;   
	//private final static int num = 1; 
	//private final static int VERTS = 6*num*num;  
	
	int[] mTextureId = new int[1];

    public Quad() {
    	this(1f,1f);
    }


    public Quad(String name) {
        this(name, 1, 1);
    }
    
    

    /**
     * Constructor creates a new <code>Quade</code> object with the provided
     * width and height.
     * 
     * @param width
     *            the width of the <code>Quad</code>.
     * @param height
     *            the height of the <code>Quad</code>.
     */
    public Quad(String name, float width, float height) {
    	this(width,height);
    	mName = name;
    }

    /**
     * Constructor creates a new <code>Quade</code> object with the provided
     * width and height.
     * 
     * @param width
     *            the width of the <code>Quad</code>.
     * @param height
     *            the height of the <code>Quad</code>.
     */
    public Quad( float width, float height) {
    	initialize();
        this.width = width;
        this.height = height;
        updateGeometry();
    }    
	
    public Quad(GL11 gl11, float width, float height) {
   		mGL = gl11;
    	initialize();
        this.width = width;
        this.height = height;
        updateGeometry();
    }     


	public Quad(GL11 gl11, float width, float height,float wRatio, float hRatio) {
   		mGL = gl11;
		this.width = width;
        this.height = height;
		this.wRatio = wRatio;
        this.hRatio = hRatio;
    	initialize();
		Log.i(mName, "wRatio "+this.wRatio);
		Log.i(mName, "hRatio "+this.hRatio);

        updateGeometry();
    } 
    
    /**
     * <code>getCenter</code> returns the center of the <code>Quad</code>.
     * 
     * @return Vector3f the center of the <code>Quad</code>.
     */

/*
	public void setCenter( Vector3f pos) {
         worldTranslation = pos;
    }
    public Vector3f getCenter() {
        return worldTranslation;
    }
    */

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    /**
     * <code>initialize</code> builds the data for the <code>Quad</code>
     * object.
     * 
     * @deprecated Use {@link #updateGeometry(float,float)} instead
     */
    public void initialize() {
    	
    	ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
    	vbb.order(ByteOrder.nativeOrder());
    	mFVertexBuffer = vbb.asFloatBuffer();
    	
    	ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
    	tbb.order(ByteOrder.nativeOrder());
    	mTexBuffer = tbb.asFloatBuffer();
    	
    	ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
    	ibb.order(ByteOrder.nativeOrder());
    	mIndexBuffer = ibb.asShortBuffer();
    	
    	mTextureId[0]=-1;
                  	
        //updateGeometry();
    }

    /**
     * <code>resize</code> changes the width and height of the given quad by
     * altering its vertices.
     * 
     * @param width
     *            the new width of the <code>Quad</code>.
     * @param height
     *            the new height of the <code>Quad</code>.
     */
    public void resize(float width, float height) {
        this.width = width;
        this.height = height;
        mFVertexBuffer.clear();
		mFVertexBuffer.put(-width / 2f).put(-height / 2f).put(0);
        mFVertexBuffer.put(width / 2f).put(-height / 2f).put(0); 
        mFVertexBuffer.put(-width / 2f).put(height / 2f).put(0);        
        mFVertexBuffer.put(width / 2f).put(height / 2f).put(0);

     


		mFVertexBuffer.position(0);

              
    }

    /**
     * Rebuild this quad based on a new set of parameters.
     * 
     * @param width the width of the quad.
     * @param height the height of the quad.
     */
    public void updateGeometry() {

        resize(width, height);
//		Log.i(mName, "width: "+width);
//		Log.i(mName, "height: "+height);

    	float[] uv = {
    		    0,            hRatio,
                wRatio,    hRatio,
                0,    0,
                wRatio,            0
    		};
    	mTexBuffer.clear();
    	for (int i = 0; i < VERTS; i++) {
    		for(int j = 0; j < 2; j++) {
   			mTexBuffer.put(uv[i*2+j]);
    		}
   		}
//		Log.i(mName,"uv[1]"+uv[1]);
//		Log.i(mName,"uv[2]"+uv[2]);
//		Log.i(mName,"uv[6]"+uv[6]);

		
    	mIndexBuffer.clear();
    	for(int i = 0; i < VERTS; i++) {
    		mIndexBuffer.put((short) i);
    	}          

		mTexBuffer.position(0);
		mIndexBuffer.position(0);
       
    }
    
    public boolean BindBitmap( Bitmap bitmap ){
		Log.i(mName, "BindBitmap "+bitmap);
    	if(bitmap==null){
    		return false;
    	}
		GL11 gl = mGL;
		if(gl==null){
			Log.i(mName,"Draw gl==null");
			return false;
		}

		int glError = GL11.GL_NO_ERROR;
	
		gl.glEnable(GL11.GL_TEXTURE_2D);
		// Upload the bitmap to a new texture.
		gl.glGenTextures(1, mTextureId, 0);
//		Log.i(mName, "Texture "+ mTextureId[0]);

				
		gl.glBindTexture(GL11.GL_TEXTURE_2D, mTextureId[0]);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,GL11.GL_REPLACE);
		GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
		Log.i(mName, "Texture creation bitmap.getWidth " + bitmap.getWidth());
		Log.i(mName, "Texture creation bitmap.getHeight " + bitmap.getHeight());		
		glError = gl.glGetError();
			//bitmap.recycle();		
				
		if (glError != GL11.GL_NO_ERROR) {
					// There was an error, we need to retry this texture at some
					// later time
			Log.i(mName, "Texture creation fail, glError " + glError);
			return false;
		 	
		}
		return true;
    		
    }

	
    
    public void Draw(){
    	GL11 gl = mGL;
		if(gl==null){
			Log.i(mName,"Draw gl==null");
			return;
		}
    	


		//gl.glActiveTexture(GL11.GL_TEXTURE0); 


		String vendor="Imagination Technologies";		//for TI
		if(vendor.equals(mGL.glGetString(GL11.GL_VENDOR))){
	        gl.glClientActiveTexture(GL11.GL_TEXTURE0);
			gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
	        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}
		
        gl.glBindTexture(GL11.GL_TEXTURE_2D, mTextureId[0]);
        

		
        //gl.glFrontFace(GL11.GL_CCW);
        gl.glVertexPointer(3, GL11.GL_FLOAT, 0, mFVertexBuffer);
        gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL11.GL_TRIANGLE_STRIP, VERTS,
			GL11.GL_UNSIGNED_SHORT, mIndexBuffer);  	


		
 //   	Log.i(mName, "Texture "+mTextureId[0]);
    }

	public void OnDestory(){
		GL11 gl = mGL;

		if(gl==null){
			Log.i(mName,"Draw gl==null");
			return;
		}
		Log.i(mName, "OnDestory " + mTextureId[0]);
		if(mTextureId[0]!=-1)			
			gl.glDeleteTextures(1, mTextureId, 0);
		mTextureId[0]=-1;
		mFVertexBuffer.clear();
	mTexBuffer.clear();
	mIndexBuffer.clear();	

	}

	static public void drawMixed2D(Quad from, Quad to, float ratio) {
        final GL11 gl = to.mGL;

        // Bind "from" and "to" to TEXTURE0 and TEXTURE1, respectively.


		gl.glActiveTexture(GL11.GL_TEXTURE0);
        gl.glBindTexture(GL11.GL_TEXTURE_2D, from.mTextureId[0]);

        gl.glActiveTexture(GL11.GL_TEXTURE1);
		gl.glBindTexture(GL11.GL_TEXTURE_2D, to.mTextureId[0]);

                // Enable TEXTURE1.
                gl.glEnable(GL11.GL_TEXTURE_2D);

                // Interpolate the RGB and alpha values between both textures.
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_INTERPOLATE);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_ADD);

				//gl.glTexEnvf()

                // Specify the interpolation factor via the alpha component of
                // GL_TEXTURE_ENV_COLOR.
                final float[] color = { 0f, 0f, 0f, 0f };
                gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, color, 0);

                // Wire up the interpolation factor for RGB.
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_RGB, GL11.GL_TEXTURE);
                gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);

                // Wire up the interpolation factor for alpha.
                //gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_ALPHA, GL11.GL_CONSTANT);
                //gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);

				



				gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
									
				gl.glFrontFace(GL11.GL_CCW);
				gl.glVertexPointer(3, GL11.GL_FLOAT, 0, to.mFVertexBuffer);
				gl.glEnable(GL11.GL_TEXTURE_2D);
				gl.glClientActiveTexture(GL11.GL_TEXTURE0);
				gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, to.mTexBuffer);
				gl.glClientActiveTexture(GL11.GL_TEXTURE1);
				gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, from.mTexBuffer);
				gl.glDrawElements(GL11.GL_TRIANGLE_STRIP, VERTS,
					GL11.GL_UNSIGNED_SHORT, to.mIndexBuffer);	

                // Disable TEXTURE1.
                gl.glDisable(GL11.GL_TEXTURE_2D);


            // Switch back to the default texture unit.
            gl.glActiveTexture(GL11.GL_TEXTURE0);

	}





}

