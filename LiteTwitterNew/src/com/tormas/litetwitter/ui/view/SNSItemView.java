package com.tormas.litetwitter.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public abstract class SNSItemView extends LinearLayout
{
	protected Context mContext;
	public SNSItemView(Context context) {
		super(context);	
		mContext = context;
	}
	public SNSItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		mContext = ctx;
	}
	
	public boolean isProfile(String url)
	{
    	Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	Log.d("sns-link", "isProfile url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/profile.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	 
	public boolean isPhoto(String url)
	{
		Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	
    	Log.d("sns-link", "isPhoto url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/album.php") || path.equals("/photo.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	
	public abstract String getText();
	public boolean isEmpty(String str)
	{
		return str == null || str.trim().length() == 0;
	}
	
	static final String urlPartern="(^|[ \t\r\n])(" +
	"(ftp|http|https|gopher|mailto|tel|news|nntp|telnet|wais|file|prospero|aim|webcal)" +
	":" +
	"(" +
	"([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))";
	public List<String> getLinks()
	{
		List<String> links = new ArrayList<String>();
		String text = this.getText();
		if(isEmpty(text) == false)
		{
			Pattern p = Pattern.compile(urlPartern);
			Matcher m = p.matcher(text);
			
			while(m.find())
			{
				links.add(text.substring(m.start(), m.end()).trim());
			}
		}
		return links;
	}

	
	/**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".
     * Optionally replace HTML tags with a space.
     *
     * @param str
     * @param addSpace
     * @return
     */
    public static String removeHTML(String str, boolean addSpace) 
    {
        if (str == null) return "";
        StringBuffer ret = new StringBuffer(str.length());
        int start = 0;
        int beginTag = str.indexOf("<");
        int endTag = 0;
        if (beginTag == -1)
            return str;
       
        while (beginTag >= start) {
            if (beginTag > 0) {
                ret.append(str.substring(start, beginTag));
               
                // replace each tag with a space (looks better)
                if (addSpace) ret.append(" ");
            }
            endTag = str.indexOf(">", beginTag);
           
            // if endTag found move "cursor" forward
            if (endTag > -1) {
                start = endTag + 1;
                beginTag = str.indexOf("<", start);
            }
            // if no endTag found, get rest of str and break
            else {
                ret.append(str.substring(beginTag));
                break;
            }
        }
        // append everything after the last endTag
        if (endTag > -1 && endTag + 1 < str.length()) {
            ret.append(str.substring(endTag + 1));
        }
        return ret.toString().trim();
    }
}
