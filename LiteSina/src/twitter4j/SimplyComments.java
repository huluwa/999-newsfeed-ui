package twitter4j;

import com.tormas.litesina.providers.SocialORM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A data class representing one single status of a user.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class SimplyComments extends TwitterResponse implements java.io.Serializable, Comparable {
    public Date createdAt;
    public long id;
    public String text;    
    
    public RetweetDetails retweetDetails;
    
    private static final long serialVersionUID = 1608000492860584608L;

    /*package*/SimplyComments(Element elem, Twitter twitter) throws TwitterException {
        super();
        if(twitter.exitma())
            throw new TwitterException("stop parse SimplyComments");
        
        Log.d("SimplyStatus", "I am Parsing Simply comment");
        ensureRootNodeNameIs("comment", elem);
        user = new SimplyUser((Element) elem.getElementsByTagName("user").item(0),
                twitter);
        id = getChildLong("id", elem);
        text = getChildText("text", elem);        
        createdAt = getChildDate("created_at", elem);       
       
		
		Element eld = (Element) elem.getElementsByTagName("retweeted_status").item(0);
		if(eld != null){
			retweetDetails = new RetweetDetails(eld);
		}
    }
   
    public boolean isRetweet(){
        return null != retweetDetails;
    }
    
    public boolean isStatus(Element elem, Twitter twitter) throws TwitterException
    {
    	  if(twitter.exitma())
              throw new TwitterException("stop parse SimplyComments");
    	  
    	  id = getChildLong("id", elem);
    	  
    	  return id>0;
    }

    public SimplyComments() throws TwitterException
    {
    	super();    	
    }
    /**
     * Return the created_at
     *
     * @return created_at
     * @since Twitter4J 1.1.0
     */

    public Date getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Returns the id of the status
     *
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Returns the text of the status
     *
     * @return the text
     */
    public String getText() {
        return this.text;
    }
    
    
    public SimplyUser user = null;

    /**
     * Return the user
     *
     * @return the user
     */
    public SimplyUser getUser() {
        return user;
    }

    //get the small size of the view count and the update
    public static int getViewSize(NodeList list)
    {
        //get view count from cache
        //int viewCount = SocialORM.StatusViewCount;
        int size = list.getLength();
        
        //return viewCount>size?size:viewCount;
        return size;
    }
    //TODO just get the required count of status
    /*package*/
     static List<SimplyComments> constructStatuses(Document doc,
                                          Twitter twitter) throws TwitterException {
        if(twitter.exitma())
            throw new TwitterException("activity is onPause or onDestroy");
        
        twitter.finishNetwork();
        
        if (isRootNodeNilClasses(doc)) {
            return new ArrayList<SimplyComments>(0);
        } else {
            try {
                ensureRootNodeNameIs("comments", doc);
                NodeList list = doc.getDocumentElement().getElementsByTagName(
                        "comment");
                int size = getViewSize(list);
                List<SimplyComments> statuses = new ArrayList<SimplyComments>(size);
                for (int i = 0; i < size; i++) {
                	twitter.updateProgress(i, size);
                    Element status = (Element) list.item(i);
                    statuses.add(new SimplyComments(status, twitter));
                }
                return statuses;
            } catch (TwitterException te) {
            	throw te;
            	/*
                ensureRootNodeNameIs("nil-classes", doc);
                return new ArrayList<SimplyStatus>(0);
                */
            }
        }
    }

    /*
  <status>
    <created_at>Fri May 30 17:04:22 +0000 2008</created_at>
    <id>823477057</id>
    <text>double double at in n out on Magnolia</text>
    <source>web</source>
    <truncated>false</truncated>
    <in_reply_to_status_id></in_reply_to_status_id>
    <in_reply_to_user_id></in_reply_to_user_id>
    <favorited>false</favorited>
    <user>
      <id>14500444</id>
      <name>arenson</name>
      <screen_name>arenson</screen_name>
      <location>Texas</location>
      <description>I like girls, Mexican Food, and laughter. </description>
      <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/54044033/s7958437_39956964_9393_normal.jpg</profile_image_url>
      <url></url>
      <protected>false</protected>
      <followers_count>12</followers_count>
    </user>
  </status>*/
    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return obj instanceof SimplyComments && ((SimplyComments) obj).id == this.id;
    }

    @Override
    public String toString() {
        return "Status{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", text='" + text + '\'' +                
                ", user=" + user +
                '}';
    }

	public int compareTo(Object another) 
	{
		if(SimplyComments.class.isInstance(another))
		{
			SimplyComments ss = (SimplyComments)another;
			Date anDate = ss.createdAt;
			if(createdAt.getTime() > anDate.getTime())
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

    public void despose() {
        createdAt    = null;
        id           = -1;
        text         = null;      
        user.despose();
        user = null;
    }
}
