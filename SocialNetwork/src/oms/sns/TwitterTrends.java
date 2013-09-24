package oms.sns;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import java.util.ArrayList;

//TODO will move to framework/base/core/java/oms/social
//{ 
//  as_of  long 
//  count  int
//  
//  name1, name2, ..., name-n
//  url1,  url2,  ..., url-n
//  
//}
//
public class TwitterTrends implements Parcelable{

        /*
         * will save name---url maping
         */ 
	public List<String>names=null;
	public List<String>urls =null;
	public long as_of;
        public int  count = 0;
 
        public TwitterTrends()
        {

        } 
        public TwitterTrends(Parcel in)
        {
            readFromParcel(in);
        }
        public void readFromParcel(Parcel in)
        {
            as_of = in.readLong();
            count = in.readInt();
            
            names = new ArrayList<String>();
            urls  = new ArrayList<String>();
            for(int i=0;i<count;i++)
            {
                String name = in.readString();
                String value = in.readString();
                names.add(name);
                urls.add(value);
            }

        }

	public int describeContents() {
		//TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
	    out.writeLong(as_of);
            out.writeInt(count);
            for(int i=0;i<count;i++)
            {
                out.writeString(names.get(i));
                out.writeString(urls.get(i));
            }
             
	}
    
     public static final Parcelable.Creator<TwitterTrends> CREATOR
            = new Parcelable.Creator<TwitterTrends>() {
        public TwitterTrends createFromParcel(Parcel in) {
            return new TwitterTrends(in);
        }

        public TwitterTrends[] newArray(int size) {
            return new TwitterTrends[size];
        }
    };


}
