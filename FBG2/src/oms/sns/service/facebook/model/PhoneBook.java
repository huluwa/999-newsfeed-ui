package oms.sns.service.facebook.model;

import java.util.Date;

import oms.sns.service.facebook.client.FacebookField;

public class PhoneBook {
   public long    uid;
   public String  email;
   public String  cell;
   public String  phone;
   public boolean synced;
   public String  username;
      
   public String screenname;
   public String address;
   public String street;
   public String state;
   public String city;
   public String country;
   public String zip;
   public String latitude;
   public String longitude;
   public String pic_square;
   
   public int    peopleid;
  
   public void recycle()
   {
       uid = 0;
       email = null;
       cell  = null;
       phone = null;
       username = null;
       screenname = null;
       address    = null;
       street = null;
       state  = null;
       city   = null;
       country = null;
       zip = null;
       latitude = null;
       longitude = null;
       pic_square = null;
       peopleid = 0;
   }
   public PhoneBook clone()
   {
       PhoneBook clon = new PhoneBook();
       clon.uid = uid;
       clon.email = email;
       clon.cell = cell;
       clon.phone = phone;
       clon.synced = synced;
       clon.username = username;
       clon.screenname = screenname;
       clon.address = address;
       clon.street = street;
       clon.state = state;
       clon.city = city;
       clon.country = country;
       clon.zip = zip;
       clon.latitude = latitude;
       clon.longitude = longitude;
       clon.pic_square = pic_square;
       clon.peopleid   = peopleid;
       return clon;
   }
   public String getPic_square() {
    return pic_square;
   }

    public void setPic_square(String pic_square) {
        this.pic_square = pic_square;
    }

//default is false
   public boolean isFriend;


    public String toString()
    {
	   return "uid="+uid+
	          " email="+email+
	          " cell="+cell+
	          " phone="+phone+
	          " synced="+synced+
	          " username="+username+
	          " city="+city+
	          " address="+address+
	          " zip="+zip+
	          " country="+country+
	          " screenname="+screenname+
	          " street="+street+
	          " state="+state+
	          " latitude="+latitude+
	          " longitude="+longitude+
	          " peopleid ="+peopleid;
    }
	
    public String getScreenname() {
	    return screenname;
    }
   
    public void setScreenname(String screenname) {
	    this.screenname = screenname;
    }
   
    public String getAddress() {
	  return address;
    }
   
	public void setAddress(String address) {
		this.address = address;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
			
	public String getUsername() {
	    return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public Long getUid() {
	    return uid;
    }
	
    public void setUid(Long uid) {
	    this.uid = uid;
    }
    
    public String getEmail() {
	    return email;
    }
    
    public void setEmail(String email) {
	    this.email = email;
    }
    
    public String getCell() {
	    return cell;
    }
    
    public void setCell(String cell) {
	    this.cell = cell;
    }
    
    public String getPhone() {
	    return phone;
    }
    
    public void setPhone(String phone) {
	     this.phone = phone;
    }
 
	public static class Field 
	{
		public static final String UID="uid";
		public static final String NAME="name";
		public static final String EMAIL="email";
		public static final String CELL="cell";
		public static final String PHONE="phone";
		public static final String SCREENNAME="screenname";
		public static final String ADDRESS="address";
		public static final String STREET="street";
		public static final String STATE="state";
		public static final String CITY="city";
		public static final String COUNTRY = "country";
		public static final String ZIP="zip";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE= "longitude";		
	}

	public void dispose() 
	{
		recycle();		
	}
}
