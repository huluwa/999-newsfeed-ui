package com.msocial.freefb.widget.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LiteStatus implements Parcelable {
	public Long uid;
	public Long time;
	public String message;
	public String username;

	public String toString() {
		return uid + " " + time +  " " + message
				+ " " + username;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		out.writeLong(uid);
		out.writeLong(time);
		out.writeString(message);
		out.writeString(username);
	}

	public LiteStatus(Parcel in) {
		readFromParcel(in);
	}

	public LiteStatus() {
		// TODO Auto-generated constructor stub
	}

	public void readFromParcel(Parcel in) {
		uid = in.readLong();
		time = in.readLong();
		message = in.readString();
		username = in.readString();
	}

	public static final Parcelable.Creator<LiteStatus> CREATOR = new Parcelable.Creator<LiteStatus>() {
		public LiteStatus createFromParcel(Parcel in) {
			return new LiteStatus(in);
		}

		public LiteStatus[] newArray(int size) {
			return new LiteStatus[size];
		}
	};

}
