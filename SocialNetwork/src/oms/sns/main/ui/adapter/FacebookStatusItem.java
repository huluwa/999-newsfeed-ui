package oms.sns.main.ui.adapter;

import android.graphics.Bitmap;

public class FacebookStatusItem 
{
	public enum ContentType
	{
		IMAGE, CAMERA, VIDEO, LINK, NONE;		
	}
	
	public enum Status
	{
		INIT, UPLOADING, SUC_UPLOAD		
	}
	private static long nStep=0;
	public FacebookStatusItem()
	{
		nStep++;
		id=nStep;
		
		uploadStatus = Status.INIT;
	}
	
	public final long   id;
	public String url;
	public String name;
	public ContentType  type;
	public byte[] data; //	
	public Object  obj;
	public Bitmap  bmp;
	public Status  uploadStatus;
	public boolean hasProcessed;
	//0 init status or fail to upload, 1 uploading, 2 upload suc, //show whether the content is uploaded or not whatever it's uploaded successfully or not
}
