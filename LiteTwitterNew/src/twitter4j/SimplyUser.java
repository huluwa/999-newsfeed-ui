package twitter4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A data class representing Twitter User
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class SimplyUser extends TwitterResponse implements java.io.Serializable {
    static final String[] POSSIBLE_ROOT_NAMES = new String[]{"user", "sender", "recipient"};
    public Twitter twitter;
    public int id;
    public String name;
    public String screenName;    
    public String profileImageUrl;    
    public String description;
    public boolean notifications;
    public boolean following;
    public long next_cursor;
    public long pre_cursor;
    public static final long serialVersionUID = 3037057798600246529L;

    /*package*/SimplyUser(Element elem, Twitter twitter) throws TwitterException {
        super();
        if(twitter.exitma())
            throw new TwitterException("stop parse SimplyUser");
        
        Log.d("SimplyUser", "I am Parsing Simply User");
        
        this.twitter = twitter;
        ensureRootNodeNameIs(POSSIBLE_ROOT_NAMES, elem);
        parseSimplyUser(this, elem);
    }
    
    private static void parseSimplyUser(SimplyUser user, Element elem) {
        Log.d("SimplyUser", "I am Parsing Simply User");
        
        user.id = getChildInt("id", elem);
        user.name = getChildText("name", elem);
        user.screenName = getChildText("screen_name", elem);       
        user.profileImageUrl = getChildText("profile_image_url", elem);
        
        user.notifications = getChildBoolean("notifications", elem);
        user.following     = getChildBoolean("following", elem);
        user.description   = getChildText("description", elem);
    }
    
    public boolean Notifications()
    {
    	return notifications;
    }
    public boolean isFollowing()
    {
    	return following;
    }
    
    public SimplyUser() throws TwitterException
    {
    	super();
    }

    /**
     * Returns the id of the user
     *
     * @return the id of the user
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the user
     *
     * @return the name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the screen name of the user
     *
     * @return the screen name of the user
     */
    public String getScreenName() {
        return screenName;
    }
    
    /**
     * Returns the profile image url of the user
     *
     * @return the profile image url of the user
     */
    public String getProfileImageURL() {
        return profileImageUrl;       
    }    

    public DirectMessage sendDirectMessage(String text) throws TwitterException {
        return twitter.sendDirectMessage(this.getName(), text);
    }

    public static List<SimplyUser> constructUsers(Document doc, Twitter twitter) throws TwitterException {
        if(twitter.exitma())
            throw new TwitterException("activity is onPause or onDestroy");
        
	    twitter.finishNetwork();
        
        if (isRootNodeNilClasses(doc)) {
            return new ArrayList<SimplyUser>(0);
        } else {
            try {
                ensureRootNodeNameIs("users", doc);
                /*
                NodeList list = doc.getDocumentElement().getElementsByTagName(
                        "user");
                int size = list.getLength();
                List<SimplyUser> users = new ArrayList<SimplyUser>(size);
                for (int i = 0; i < size; i++) {
                	twitter.updateProgress(i, size);
                    users.add(new SimplyUser((Element) list.item(i), twitter));
                }*/
                List<SimplyUser> users = parseSimplyUserXML(doc,twitter);
                return users;
            } catch (TwitterException te) {
                throw te;
                /*
                if (isRootNodeNilClasses(doc)) {
                    return new ArrayList<SimplyUser>(0);
                } else {
                    throw te;
                }*/
            }
        }
    }
    
    public static List<SimplyUser> constructUsersForCursor(Document doc, Twitter twitter) throws TwitterException {
        if(twitter.exitma())
            throw new TwitterException("activity is onPause or onDestroy");
        
        twitter.finishNetwork();
        
        if (isRootNodeNilClasses(doc)) {
            return new ArrayList<SimplyUser>(0);
        } else {
            try {
                ensureRootNodeNameIs("users_list", doc);
                long next = -1;
                long pre = -1;
                
                NodeList tmpList = doc.getElementsByTagName("users_list");
                if(tmpList!=null && tmpList.getLength()>0)
                {
                    next = getChildLong("next_cursor", (Element)tmpList.item(0));
                    Log.d("constructUsersForCursor","next Cursor="+next);
                }
                
                if(tmpList!=null && tmpList.getLength()>0)
                {
                    pre = getChildLong("previous_cursor", (Element)tmpList.item(0));
                    Log.d("constructUsersForCursor","pre Cursor="+pre);
                }
                
                List<SimplyUser> users = parseSimplyUserXML(doc,twitter);
               /* NodeList list = doc.getDocumentElement().getElementsByTagName("user");
                int size = list.getLength();
                List<SimplyUser> users = new ArrayList<SimplyUser>(size);                
                for (int i = 0; i < size; i++) {
                    twitter.updateProgress(i, size);
                    users.add(new SimplyUser((Element) list.item(i), twitter));
                }*/
                
                SimplyUser simpUser = new SimplyUser();
                simpUser.next_cursor = next;
                simpUser.pre_cursor  = pre;
                simpUser.id = -1;
                users.add(0,simpUser);
                
                return users;
            } catch (TwitterException te) {
                throw te;
                /*
                if (isRootNodeNilClasses(doc)) {
                    return new ArrayList<SimplyUser>(0);
                } else {
                    throw te;
                }*/
            }
        }
    }
    
    private static List<SimplyUser> parseSimplyUserXML(Document doc,Twitter twitter) throws TwitterException {
        List<SimplyUser> users = new ArrayList<SimplyUser>();
        NodeList nodelist = doc.getElementsByTagName("users");
        if(nodelist!= null && nodelist.getLength()>0)
        {
            Node node = nodelist.item(0);
            //user nodes
            NodeList nodes = node.getChildNodes();
            if(nodes != null && nodes.getLength()>0)
            {
                for(int i=0;i<nodes.getLength();i++)
                    try {
                            twitter.updateProgress(i, nodes.getLength());
                            Node user_node = nodes.item(i);                    
                            if(user_node!=null && user_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                            {                   
                                SimplyUser user = new SimplyUser();
                                parseSimplyUser(user, (Element)user_node);
                                users.add(user);
                            }
                    } catch (TwitterException e) {
                        // TODO Auto-generated catch block
                       throw new TwitterException(e);
                    } 
            }
        }
        return users;
    }

    /*<?xml version="1.0" encoding="UTF-8"?>
    <user>
      <id>3516311</id>
      <name>&#12383;&#12384;&#12375;</name>
      <screen_name>_tad_</screen_name>
      <location>&#12388;&#12367;&#12400;&#24066;</location>
      <description>&#12360;&#12379;{hacker,Rubyist,&#27598;&#26085;&#12487;&#12470;&#12452;&#12531;&#32771;&#23519;&#20013;,&#12403;&#12424;&#12426;&#12377;&#12392;,&#32207;&#35676;&#12414;&#12395;&#12354;,&#12521;&#12531;&#12490;&#12540;,&#12407;&#12425;&#12368;&#12414;&#12385;&#12377;&#12392;} + &#12426;&#12354;&#12427;&#12395;&#12502;&#12524;&#12540;&#12461;&#22730;&#12428;&#12390;&#12427;&#20154;</description>
      <profile_image_url>http://assets1.twitter.com/system/user/profile_image/3516311/normal/NEC_0045.jpg?1182343831</profile_image_url>
      <url>http://www.coins.tsukuba.ac.jp/~i021179/blog/</url>
      <protected>false</protected>
      <followsers_count>274</followers_count>
    </user>
     */
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return obj instanceof SimplyUser && ((SimplyUser) obj).id == this.id;
    }

    @Override
    public String toString() {
        return "User{" +
                "twitter=" + twitter +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +            
                ", profileImageUrl='" + profileImageUrl + '\'' +     
                ", description='" + description +'\'' +
                '}';
    }
    public void despose() {
        twitter = null;
        id = 0;
        name            = null;
        screenName      = null;
        profileImageUrl = null;
        description     = null;
        notifications    = false;
        following        = false;      
    }
}
