package oms.sns;

import android.os.Parcel;
import android.os.Parcelable;

//TODO will move to framework/base/core/java/oms/social
//
//
//
//
//
//
public class TwitterUser implements Parcelable{

        public long     id;
        public String  name;
        public String  screenName;
        public String  location;
        public String  description;
        public String  profileImageUrl;
        public String  url;
        public boolean isProtected;
        public boolean following;
        public boolean notifications;
        public int     followersCount;
        public static final long serialVersionUID = 3037057798600246529L;
	
        public String toString()
        {
            String ret="id="+id +
                       " name="+name +
                       " image="+profileImageUrl;

            return ret;
        }

        public TwitterUser()
        {

        } 
        public TwitterUser(Parcel in)
        {
            readFromParcel(in);
        }
        public void readFromParcel(Parcel in)
        {
            id = in.readLong();
            name = in.readString();
            screenName  = in.readString();
            location    = in.readString();
            description = in.readString();
            
            profileImageUrl = in.readString();
            url             = in.readString();
            isProtected     = in.readInt()==0?false:true;
            following       = in.readInt()==0?false:true;
            notifications   = in.readInt()==0?false:true;
            
            followersCount  = in.readInt();
        }

	public int describeContents() {
		//TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		 out.writeLong(id);
                 out.writeString(name==null?"":name); 
                 out.writeString(screenName==null?"":screenName);
                 out.writeString(location==null?"":location);
                 out.writeString(description==null?"":description);
                 out.writeString(profileImageUrl==null?"":profileImageUrl);
                 out.writeString(url==null?"":url);
                 out.writeInt(isProtected==true?1:0);
                 out.writeInt(following==true?1:0);
                 out.writeInt(notifications==true?1:0);
                 out.writeInt(followersCount);
		
	}
    
     public static final Parcelable.Creator<TwitterUser> CREATOR
            = new Parcelable.Creator<TwitterUser>() {
        public TwitterUser createFromParcel(Parcel in) {
            return new TwitterUser(in);
        }

        public TwitterUser[] newArray(int size) {
            return new TwitterUser[size];
        }
    };
}

