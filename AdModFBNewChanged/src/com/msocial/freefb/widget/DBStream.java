package com.msocial.freefb.widget;

import android.os.Parcel;
import android.os.Parcelable;

public class DBStream implements Parcelable {
	public long _id;
	public String post_id;
	public long source_id;
	public String user_name;
	public String user_head;
	public String message;
	public long updated_time;
	public long created_time;

	public String toString() {
		return _id + " " + post_id + " " + source_id + " " + user_name + " "
				+ user_head + " " + message + " " + updated_time + " "
				+ created_time;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		out.writeLong(_id);
		out.writeString(post_id);
		out.writeLong(source_id);
		out.writeString(user_name);
		out.writeString(user_head);
		out.writeString(message);
		out.writeLong(updated_time);
		out.writeLong(created_time);
	}

	public DBStream(Parcel in) {
		readFromParcel(in);
	}

	public DBStream() {
		// TODO Auto-generated constructor stub
	}

	public void readFromParcel(Parcel in) {
		_id = in.readLong();
		post_id = in.readString();
		source_id = in.readLong();
		user_name = in.readString();
		user_head = in.readString();
		message = in.readString();
		updated_time = in.readLong();
		created_time = in.readLong();
	}

	public static final Parcelable.Creator<DBStream> CREATOR = new Parcelable.Creator<DBStream>() {
		public DBStream createFromParcel(Parcel in) {
			return new DBStream(in);
		}

		public DBStream[] newArray(int size) {
			return new DBStream[size];
		}
	};

}
