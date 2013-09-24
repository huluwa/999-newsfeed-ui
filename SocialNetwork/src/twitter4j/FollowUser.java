package twitter4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A data class representing Twitter User
 * @author Liu Huadong
 */
public class FollowUser extends TwitterResponse implements java.io.Serializable {
    static final String[] POSSIBLE_ROOT_NAMES = new String[]{"user", "sender", "recipient"};
    private Twitter twitter;
    private int id;
    private String name;
    private String screenName;
    private String location;
    private String description;
    private String profileImageUrl;
    private String url;
    private boolean isProtected;
    private int followersCount;
    private static final long serialVersionUID = 3037057798600246529L;

    /*package*/FollowUser(Element elem, Twitter twitter) throws TwitterException {
        super();
        if(twitter.exitma)
            throw new TwitterException("activity is onPause or onDestroy");
        
        this.twitter = twitter;
        ensureRootNodeNameIs(POSSIBLE_ROOT_NAMES, elem);
        id = getChildInt("id", elem);
        name = getChildText("name", elem);
        screenName = getChildText("screen_name", elem);
        location = getChildText("location", elem);
        description = getChildText("description", elem);
        profileImageUrl = getChildText("profile_image_url", elem);
        url = getChildText("url", elem);
        isProtected = getChildBoolean("protected", elem);
        followersCount = getChildInt("followers_count", elem);
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
     * Returns the location of the user
     *
     * @return the location of the user
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the description of the user
     *
     * @return the description of the user
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the profile image url of the user
     *
     * @return the profile image url of the user
     */
    public URL getProfileImageURL() {
        try {
            return new URL(profileImageUrl);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * Returns the url of the user
     *
     * @return the url of the user
     */
    public URL getURL() {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * Test if the user status is protected
     *
     * @return true if the user status is protected
     */
    public boolean isProtected() {
        return isProtected;
    }


    /**
     * Returns the number of followers
     *
     * @return the number of followers
     * @since Twitter4J 1.0.4
     */
    public int getFollowersCount() {
        return followersCount;
    }

    public DirectMessage sendDirectMessage(String text) throws TwitterException {
        return twitter.sendDirectMessage(this.getName(), text);
    }

    public static List<FollowUser> constructUsers(Document doc, Twitter twitter) throws TwitterException {
        if(twitter.exitma)
            throw new TwitterException("activity is onPause or onDestroy");
        
        if (isRootNodeNilClasses(doc)) {
            return new ArrayList<FollowUser>(0);
        } else {
            try {
                ensureRootNodeNameIs("users", doc);
                NodeList list = doc.getDocumentElement().getElementsByTagName(
                        "user");
                int size = list.getLength();
                List<FollowUser> users = new ArrayList<FollowUser>(size);
                for (int i = 0; i < size; i++) {
                    users.add(new FollowUser((Element) list.item(i), twitter));
                }
                return users;
            } catch (TwitterException te) {
                if (isRootNodeNilClasses(doc)) {
                    return new ArrayList<FollowUser>(0);
                } else {
                    throw te;
                }
            }
        }
    }

    /*<?xml version="1.0" encoding="UTF-8"?>
    <user>
		<id>19895589</id>
		<name>vietnamtravelguide</name>
		<screen_name>vietnamtravels</screen_name>
		<location>Vietnam</location>
		<description>Vietnam Traavel and Tours information</description>
		<profile_image_url>
		    http://s3.amazonaws.com/twitter_production/profile_images/74708638/LOGO_ATM_tron_normal.jpeg
		</profile_image_url>
		<url>http://www.asianatravelmate.com</url>
		<protected>false</protected>
		<followers_count>286</followers_count>
		<profile_background_color>9AE4E8</profile_background_color>
		<profile_text_color>333333</profile_text_color>
		<profile_link_color>0084B4</profile_link_color>
		<profile_sidebar_fill_color>DDFFCC</profile_sidebar_fill_color>
		<profile_sidebar_border_color>BDDCAD</profile_sidebar_border_color>
		<friends_count>911</friends_count>
		<created_at>Mon Feb 02 06:07:04 +0000 2009</created_at>
		<favourites_count>28</favourites_count>
		<utc_offset>25200</utc_offset>
		<time_zone>Hanoi</time_zone>
		<profile_background_image_url>
		    http://static.twitter.com/images/themes/theme1/bg.gif
		</profile_background_image_url>
		<profile_background_tile>false</profile_background_tile>
		<statuses_count>62</statuses_count>
		<notifications>false</notifications>
		<following>false</following>
		
		<status>
			<created_at>Mon Apr 13 05:02:08 +0000 2009</created_at>
			<id>1507429387</id>
			<text>
			book now www.asianatravelmate.com for Oriental Sails: 75usd/pp inclusive Deluxe cabin, Transfer, guide, kayaking and full-board 4 meals
			</text>
			<source>web</source>
			<truncated>false</truncated>
			<in_reply_to_status_id/>
			<in_reply_to_user_id/>
			<favorited>false</favorited>
			<in_reply_to_screen_name/>
		</status>
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
        return obj instanceof FollowUser && ((FollowUser) obj).id == this.id;
    }

    @Override
    public String toString() {
        return "User{" +
                "twitter=" + twitter +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", url='" + url + '\'' +
                ", isProtected=" + isProtected +
                ", followersCount=" + followersCount +
                '}';
    }
}

