package com.msocial.freefb.service.dell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.TwitterHelper;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;

public class ContactHelp4Cupcake 
{
    static final String  TAG = "ContactHelp4Cupcake";
    public static boolean addNewContact(Context con, PhoneBook phone, SocialORM orm)
    {
        Log.d(TAG, "entering ContactHelp4Cupcake");
        String userName = phone.username;
        String mobile   = phone.cell;
        String office   = phone.phone;
        String home     = phone.phone;
        String other    = phone.phone;
        String email    = phone.email;
        
        StringBuilder sb = new StringBuilder();     
        String seperator=" ";       
        if(isEmpty(phone.address) == false)   sb.append("address: "+phone.address+seperator);
        if(isEmpty(phone.street) == false)    sb.append("street: "+phone.street+seperator);     
        if(isEmpty(phone.state)  == false)    sb.append("state: "+phone.state+seperator);
        if(isEmpty(phone.city)   == false)    sb.append("city: "+phone.city+seperator);
        if(isEmpty(phone.country)== false)    sb.append("country: "+phone.country+seperator);
        if(isEmpty(phone.zip)    == false)    sb.append("zip: "+phone.zip+seperator);       
        if(isEmpty(phone.latitude)==false)    sb.append("latitude: "+phone.latitude+seperator);     
        if(isEmpty(phone.longitude)==false)   sb.append("longitude: "+phone.longitude+seperator);       
                
        String postalinfo = sb.toString();
             
        boolean use_logo         = orm.getFacebookUseLogo();
        boolean use_cell         = orm.getFacebookUsePhonenumber();
        boolean use_birthday     = orm.getFacebookUseBirthday();
        boolean use_email        = orm.getFacebookUseEmail();
        
        
        ContentValues values = new ContentValues();
        if(!isEmpty(userName))values.put(People.NAME, userName);
        if(!isEmpty(userName))values.put(People.PHONETIC_NAME, userName);
        // Add the contact to the My Contacts group
        Uri contactUri = People.createPersonInMyContactsGroup(con.getContentResolver(), values);
        
        FacebookUser user = orm.getFacebookUser(phone.uid);         
        if(user != null)
        {
            if(use_logo && !isEmpty(user.getPic_square())){
                // Handle the photo
                String filepath = TwitterHelper.getImagePathFromURL(con, user.getPic_square(), false);  
                if(!isEmpty(filepath)){                                      
                    Bitmap mPhoto = BitmapFactory.decodeFile(filepath);            
                    if (mPhoto != null) {
                        File file = new File(filepath);
                        if(file.exists() && file.length()<10*1024)
                        {
                            ByteBuffer dst = ByteBuffer.allocate(10*1024);
                            try{
                                mPhoto.copyPixelsToBuffer(dst);
                                byte[] bytes = dst.array();
                                Contacts.People.setPhotoData(con.getContentResolver(),contactUri,bytes);
                                dst.clear();
                                dst = null;
                            }catch(Exception ne){}                           
                        }
                        else
                        {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            mPhoto.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                            Contacts.People.setPhotoData(con.getContentResolver(), contactUri, stream.toByteArray());
                            
                            try {
                                stream.close();
                            } catch (IOException e){}
                            stream = null;
                        }
                    }    
                    
                    mPhoto = null;
                }
            }
        }
        
        //update the contact with other data,like notes.
        // there is no birthday data
        if(!isEmpty(postalinfo))
        {
            ContentValues people_cv = new ContentValues();
            people_cv.put(People.NOTES, postalinfo);
            con.getContentResolver().update(contactUri, values, null, null);
        }
        
        // Create the contact methods
        if(use_email && !isEmpty(email))
        {
        	addContactEmail(con,contactUri,email);
        }
        
        //Create the Phone info       
        if(use_cell)
        {
            if(!isEmpty(mobile))
            {
               addContactPhone(con,contactUri,mobile,Phones.TYPE_MOBILE);          
            }
            if(!isEmpty(other))
            {
                addContactPhone(con,contactUri,other,Phones.TYPE_OTHER);
            }
        }
           
        return true;
    }
    
    private static Uri addContactPhone(Context con,Uri contactUri,String number,int type)
    {
        Uri phoneUri = Uri.withAppendedPath(contactUri,  People.Phones.CONTENT_DIRECTORY);
        ContentValues phone_cv = new ContentValues();
        phone_cv.put(Phones.TYPE, type);
        phone_cv.put(Phones.NUMBER, number);
        if(type==Phones.TYPE_MOBILE)
           phone_cv.put(Phones.ISPRIMARY, 1);
        else
           phone_cv.put(Phones.ISPRIMARY, 0);
        
       return con.getContentResolver().insert(phoneUri, phone_cv);
    }
    
    private static Uri addContactEmail(Context con,Uri contactUri,String email)
    {
        ContentValues contact_methods_cv = new ContentValues();
        // contact_methods_cv.put(ContactMethods.LABEL, null);
         contact_methods_cv.put(ContactMethods.KIND, Contacts.KIND_EMAIL);
         contact_methods_cv.put(ContactMethods.TYPE, ContactMethods.TYPE_HOME);
         contact_methods_cv.put(ContactMethods.DATA, email);
         return con.getContentResolver().insert(Uri.withAppendedPath(contactUri,  People.ContactMethods.CONTENT_DIRECTORY), contact_methods_cv);
         
    }
    
    private static boolean isEmpty(String str)
    {
        return str==null || str.length()==0;
        
    }
    
    public static void saveUserFormData (Context con, String id, PhoneBook phone, SocialORM orm) 
    {
    	String format = con.getString(R.string.update_contact_notes);
		String notes = String.format(format, new Date(System.currentTimeMillis()).toLocaleString());
        
        ContentValues person = new ContentValues();
        person.put(Contacts.People.NAME,               phone.username);
        person.put(Contacts.People.NOTES,              notes);
        person.put(Contacts.People.STARRED,            1);
        person.put(Contacts.People.SEND_TO_VOICEMAIL,  0);


        id = (id == null? id : id.trim());
        id = (id == null? id : ("".equals(id) ? null : id));
        FacebookUser user = orm.getFacebookUser(phone.uid);    
        boolean created = false;
        if (id != null)
        {	
	        User.delete(con.getContentResolver(), id);
        }
        
        // Create it first if necessary (so we can save phone data)
    	Uri peopleUri= null;
    	if(user != null && isEmpty(user.message) == false)
    	{
    		person.put(Contacts.People.NOTES,user.message);
    	}
    	peopleUri = User.create(con.getContentResolver(), person);
        Log.d(TAG, "Inserted new user id "+peopleUri);
        created = true;        
        id =peopleUri.getLastPathSegment();
        //set phonebook peopleid
        orm.setPhonebookPeopleid(phone.uid, Integer.parseInt(id));
        
        String userName = phone.username;
        String mobile   = phone.cell;
        String office   = phone.phone;
        String home     = phone.phone;
        String other    = phone.phone;
        String email    = phone.email;
        
        StringBuilder sb = new StringBuilder();  
        String seperator=" ";       
        if(isEmpty(phone.address) == false)   sb.append("address: "+phone.address+seperator);
        if(isEmpty(phone.street) == false)    sb.append("street: "+phone.street+seperator);     
        if(isEmpty(phone.state)  == false)    sb.append("state: "+phone.state+seperator);
        if(isEmpty(phone.city)   == false)    sb.append("city: "+phone.city+seperator);
        if(isEmpty(phone.country)== false)    sb.append("country: "+phone.country+seperator);
        if(isEmpty(phone.zip)    == false)    sb.append("zip: "+phone.zip+seperator);       
        if(isEmpty(phone.latitude)==false)    sb.append("latitude: "+phone.latitude+seperator);     
        if(isEmpty(phone.longitude)==false)   sb.append("longitude: "+phone.longitude+seperator);       
                
        String postalinfo = sb.toString();
             
        boolean use_logo         = orm.getFacebookUseLogo();
        boolean use_cell         = orm.getFacebookUsePhonenumber();
        boolean use_birthday     = orm.getFacebookUseBirthday();
        boolean use_email        = orm.getFacebookUseEmail();
        
        
        
        if (use_logo)
        {     
            if(user != null && isEmpty(user.getPic_square()) == false)
            {
                
                // Handle the photo
                String filepath = TwitterHelper.getImagePathFromURL(con, user.getPic_square(), false);  
                //a new picture for the user has been uploaded
                if(isEmpty(filepath) == false)
                {
                    User.savePhoto(con.getContentResolver(), id, new File(filepath));
                }
            }
        }
        
        ContactMethod.deleteContactMethod(con.getContentResolver(), id, null);
                      
        if(use_cell)
        {
            if(!isEmpty(mobile))
            {
            	 ContentValues phonedata = new ContentValues();
            	 phonedata.put(Contacts.Phones.NUMBER,      mobile);
            	 phonedata.put(Contacts.Phones.TYPE,        Contacts.Phones.TYPE_MOBILE);
            	 phonedata.put(Contacts.Phones.ISPRIMARY,   1);
            	
                 Phone.addPhone(con.getContentResolver(), phonedata, id);            	
            }
            if(!isEmpty(other))
            {
            	ContentValues phonedata = new ContentValues();
           	    phonedata.put(Contacts.Phones.NUMBER,     mobile);
           	    phonedata.put(Contacts.Phones.TYPE,       Contacts.Phones.TYPE_OTHER);
           	    phonedata.put(Contacts.Phones.ISPRIMARY,   0);
           	    Phone.addPhone(con.getContentResolver(),  phonedata, id);           	    
            }
        }
       
        // Create the contact methods
        if(use_email && !isEmpty(email))
        {
            ContentValues contactMethod = new ContentValues();
            contactMethod.put(Contacts.ContactMethodsColumns.KIND, Contacts.KIND_EMAIL);
            contactMethod.put(Contacts.ContactMethodsColumns.TYPE, Contacts.ContactMethodsColumns.TYPE_HOME);
            contactMethod.put(Contacts.ContactMethodsColumns.DATA, email);     
            ContactMethod.addContactMethod(con.getContentResolver(), contactMethod, id);   
            
            /*
            contactMethod.clear();
            contactMethod.put(Contacts.ContactMethodsColumns.KIND, Contacts.KIND_IM);
            contactMethod.put(Contacts.ContactMethodsColumns.TYPE, Contacts.ContactMethodsColumns.TYPE_CUSTOM);
            contactMethod.put(Contacts.ContactMethodsColumns.DATA, "fb://profile/"+phone.uid);     
            ContactMethod.addContactMethod(con.getContentResolver(), contactMethod, id);
            */
        }
        
        if(!isEmpty(postalinfo))
        {
        	 ContentValues contactMethod = new ContentValues();
             contactMethod.put(Contacts.ContactMethodsColumns.KIND, Contacts.KIND_POSTAL);
             contactMethod.put(Contacts.ContactMethodsColumns.TYPE, Contacts.ContactMethodsColumns.TYPE_HOME);
             contactMethod.put(Contacts.ContactMethodsColumns.DATA, postalinfo);
             ContactMethod.addContactMethod(con.getContentResolver(), contactMethod, id);             
        }
    }
}
