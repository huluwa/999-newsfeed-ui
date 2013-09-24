package oms.sns;

import android.os.Parcel;
import android.os.Parcelable;

//TODO will move to framework/base/core/java/oms/social
public class FacebookEvent implements Parcelable{

        public int    type;
	public String location;
	public String category;
	
        public FacebookEvent()
        {

        } 
        public FacebookEvent(Parcel in)
        {
            readFromParcel(in);
        }
        public void readFromParcel(Parcel in)
        {
            location = in.readString();
            category = in.readString();
        }

	public int describeContents() {
		//TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		 //TODO Auto-generated method stub
		
	}
    
     public static final Parcelable.Creator<FacebookEvent> CREATOR
            = new Parcelable.Creator<FacebookEvent>() {
        public FacebookEvent createFromParcel(Parcel in) {
            return new FacebookEvent(in);
        }

        public FacebookEvent[] newArray(int size) {
            return new FacebookEvent[size];
        }
    };


}

