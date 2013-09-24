package oms.sns;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import oms.sns.TwitterUser;
import twitter4j.RetweetDetails;

//TODO will move to framework/base/core/java/oms/social
//http://twitter.com/statuses/user_timeline/liuhuadong.xml
//public_timeline.xml
//http://twitter.com/statuses/friends_timeline/huadong.xml
//<status>
//     <created_at>Mon Mar 30 07:23:57 +0000 2009</created_at>
//     <id>1416572498</id>
//	<text>
//	cant go to sleep so im watching law and order thats my favorite show.:)
//	</text>
//	<source>web</source>
//	<truncated>false</truncated>
//	<in_reply_to_status_id/>
//	<in_reply_to_user_id/>
//	<favorited>false</favorited>
//	<user>
//		<id>27539784</id>
//		<name>Elizabeth</name>
//		<screen_name>lizliz15</screen_name>
//		<description/>
//		<location>somewhere.:)</location>
//		<profile_image_url>
//		http://static.twitter.com/images/default_profile_normal.png
//		</profile_image_url>
//		<url/>
//		<protected>false</protected>
//		<followers_count>2</followers_count>
//	</user>
//</status>
//
//
public class TwitterStatus implements Parcelable{

        public long       createdAt;
        public long       id;
        public String     text;
        public String     source;
        public boolean    isTruncated;
        public long       inReplyToStatusId;
        public int        inReplyToUserId;
        public boolean    isFavorited;
        public boolean    ismytweets; // if from my friend
        public  TwitterUser user;
        
        public String thumbnail_pic;
        public String bmiddle_pic;
        public String original_pic;
        public String original_text;
        
        public static final long serialVersionUID = 1608000492860584608L;
	
        public TwitterStatus()
        {

        } 
        public TwitterStatus(Parcel in)
        {
            readFromParcel(in);
        }
        public String toString()
        {
            String ret="id="+id+
                       " text="+text +
                       " user="+user;
            return ret;
        }

        public void readFromParcel(Parcel in)
        {
            createdAt = in.readLong();
            id        = in.readLong();
            text      = in.readString();
            source    = in.readString();
            isTruncated       = in.readInt()==0?false:true;
            inReplyToStatusId = in.readLong();
            inReplyToUserId   = in.readInt();
            isFavorited       = in.readInt()==0?false:true;
            ismytweets        = in.readInt()==0?false:true;
            
            thumbnail_pic = in.readString();
            bmiddle_pic   = in.readString();
            original_pic  = in.readString();
            
            original_text = in.readString(); 
            user              = new TwitterUser(in);
        }

	public int describeContents() {
		//TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
              out.writeLong(createdAt);
              out.writeLong(id);
              out.writeString(text==null?"":text);
              out.writeString(source==null?"":source);
              out.writeInt(isTruncated==true?1:0);	
              out.writeLong(inReplyToStatusId);
              out.writeInt(inReplyToUserId);
              out.writeInt(isFavorited==true?1:0);
              out.writeInt(ismytweets==true?1:0);
              
              out.writeString(thumbnail_pic==null?"":thumbnail_pic);
              out.writeString(bmiddle_pic==null?"":bmiddle_pic);
              out.writeString(original_pic==null?"":original_pic);              
              out.writeString(original_text==null?"":original_text);
              
              user.writeToParcel(out, 0);
	}
    
        public static final Parcelable.Creator<TwitterStatus> CREATOR
            = new Parcelable.Creator<TwitterStatus>() {
        public TwitterStatus createFromParcel(Parcel in) {
            return new TwitterStatus(in);
        }

        public TwitterStatus[] newArray(int size) {
            return new TwitterStatus[size];
        }
    };


}

