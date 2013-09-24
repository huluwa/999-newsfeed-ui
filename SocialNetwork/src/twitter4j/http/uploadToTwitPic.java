package twitter4j.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

import android.net.http.AndroidHttpClient;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import twitter4j.TwitterException;

import android.util.Log;

public class uploadToTwitPic extends HttpPost
{
	public uploadToTwitPic(String url)
	{
		super( url );
		this.setHeader("User-Agent", "sns-dell");		
	}
	
	public void setMultipart( Map<String, String> params, Map<String, String>files )
	{
		try{
			MultipartEntity entity = new MultipartEntity();
			
			HttpEntity[] parts = new HttpEntity[ params.size() + files.size()];
			int i = 0;
			for ( String name : params.keySet() )
			{
				String value = params.get( name );			
				entity.addPart(name, new StringBody(value ));
				
			}
			
			for ( String name : files.keySet() )
			{
				String value = files.get( name );
				entity.addPart(name, new FileBody(new File(value)));
			}		
			this.setEntity(entity);
			
		} catch (Exception e) {
			
		}
	}
	
	public boolean uploadPic( String username, String password, String filepath, String message)throws TwitterException 
	{
		boolean ret = false;
		StringBuffer buffer = new StringBuffer();
		AndroidHttpClient httpClient=null;
		try
		{
			MultipartEntity entity = new MultipartEntity();
			entity.addPart("username", new StringBody(username ));
			entity.addPart("password", new StringBody(password ));
			if(message != null)
				entity.addPart("message", new StringBody(message ));			    
			else
				entity.addPart("message", new StringBody(""));
			entity.addPart("media", new FileBody(new File(filepath)));					
			setEntity(entity);
			
			httpClient = AndroidHttpClient.newInstance("oms-dell");
			
			HttpResponse res = httpClient.execute(this );
			
			InputStream inputStream = res.getEntity().getContent();
			Scanner scanner = new Scanner( inputStream );
			
			while ( scanner.hasNextLine() )
			{
				buffer.append( scanner.nextLine() );
			}
			
			Log.d("twitpic", buffer.toString());
			ret = true;
		}
		catch(IOException ne)
		{
			ret = false;
			Log.d("twitpic", "upload pci="+ne.getMessage());
		}		
		finally
		{
			Log.d("twitpic", "response="+buffer.toString());
			if(httpClient != null)
			{
				try{
				    httpClient.close();
				}catch(Exception ne){}
			}
		}
		
		return ret;
	}
}
