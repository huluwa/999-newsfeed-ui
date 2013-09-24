package twitter4j;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

public class StatusCount extends TwitterResponse implements java.io.Serializable, Comparable {

	private static final long serialVersionUID = -8938497062793752174L;

	public long id;
	public int comments;
	public int rt;
	
	/*
	 
	 <?xml version="1.0" encoding="UTF-8"?>
<counts>
  <count>
    <id>32817222</id>
    <comments>0</comments>
    <rt>0</rt>
  </count>
  <count>
    <id>32817223</id>
    <comments>3</comments>
    <rt>0</rt>
  </count>
</counts> 
	 */
		
	StatusCount(Element elem, Twitter twitter) throws TwitterException {
        super();
        if(twitter.exitma())
            throw new TwitterException("stop parse StatusCount");
        
        Log.d("StatusCount", "I am Parsing StatusCount");
        ensureRootNodeNameIs("count", elem);        
        id = getChildLong("id", elem);
        comments = getChildInt("comments", elem);        
        rt = getChildInt("rt", elem);
    }
	
	static List<StatusCount> constructStatuses(Document doc,
             Twitter twitter) throws TwitterException 
    {
		if(twitter!=null&& twitter.exitma())
		throw new TwitterException("activity is onPause or onDestroy");
		
		if(twitter!=null)
		twitter.finishNetwork();

		if (isRootNodeNilClasses(doc)) {
		    return new ArrayList<StatusCount>(0);
		} 
		else
		{
			try
			{
				ensureRootNodeNameIs("counts", doc);
				NodeList list = doc.getDocumentElement().getElementsByTagName(
				"count");
				final int size = list.getLength();
				List<StatusCount> statuses = new ArrayList<StatusCount>(size);
				for (int i = 0; i < size; i++) {
					if(twitter!=null)twitter.updateProgress(i, size);
					Element status = (Element) list.item(i);
					statuses.add(new StatusCount(status, twitter));
				}
				return statuses;
			} 
			catch (TwitterException te) 
			{
				throw te;		
			}
		}
    }
	
	public int compareTo(Object another) {		
		if(StatusCount.class.isInstance(another))
		{
			StatusCount ss = (StatusCount)another;
			long tid = ss.id;
			if(id > tid)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		return 0;
	}

}
