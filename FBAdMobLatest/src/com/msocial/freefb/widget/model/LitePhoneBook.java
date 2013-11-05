package com.msocial.freefb.widget.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LitePhoneBook implements Parcelable {
	public Long uid;
	public String email;
	public String cell;
	public String phone;
	// public boolean synced;
	public String username;
	public String screenname;
	public String pic_square;
	
	public String toString() {
		return "uid=" + uid + " email=" + email + " cell=" + cell + " phone="
				+ phone + " username=" + username + " screenname=" + screenname+ " pic_square=" + pic_square;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		out.writeLong(uid);
		out.writeString(email);
		out.writeString(cell);
		out.writeString(phone);
		out.writeString(username);
		out.writeString(screenname);
		out.writeString(pic_square);
	}

	public LitePhoneBook(Parcel in) {
		readFromParcel(in);
	}

	public LitePhoneBook(){
		
	}
	
	public void readFromParcel(Parcel in) {
		uid = in.readLong();
		email = in.readString();
		cell = in.readString();
		phone = in.readString();
		username = in.readString();
		screenname = in.readString();
		pic_square = in.readString();
	}

	public static final Parcelable.Creator<LitePhoneBook> CREATOR = new Parcelable.Creator<LitePhoneBook>() {
		public LitePhoneBook createFromParcel(Parcel in) {
			return new LitePhoneBook(in);
		}

		public LitePhoneBook[] newArray(int size) {
			return new LitePhoneBook[size];
		}
	};

}
