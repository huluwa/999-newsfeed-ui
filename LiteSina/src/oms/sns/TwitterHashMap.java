package oms.sns;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import java.util.HashMap;

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
public class TwitterHashMap implements Parcelable{

        public  int                contentType;
        public  Map<String, String>content=null;

        public TwitterHashMap()
        {

        } 
        public TwitterHashMap(Parcel in)
        {
            readFromParcel(in);
        }
        public void readFromParcel(Parcel in)
        {
            contentType = in.readInt();
            content = in.readHashMap(null);
        }

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
              out.writeInt(contentType);
              if(content != null)
                  out.writeMap(content);
	}
    
        public static final Parcelable.Creator<TwitterHashMap> CREATOR
            = new Parcelable.Creator<TwitterHashMap>() {
        public TwitterHashMap createFromParcel(Parcel in) {
            return new TwitterHashMap(in);
        }

        public TwitterHashMap[] newArray(int size) {
            return new TwitterHashMap[size];
        }
    };


}

