package com.msocial.facebook.service.dell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.msocial.facebook.service.dell.ContactMethod.DatabaseCollection;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;

import android.util.Log;

public class User
{
  
	 public static final int bufferSize = 2*8192;
	 /** Copy Stream in to Stream for byteCount bytes or until EOF or exception.
     */
    public static void copy(InputStream in,
                            OutputStream out,
                            long byteCount)throws IOException
    {
        byte buffer[] = new byte[bufferSize];
        int len=bufferSize;

        if (byteCount>=0)
        {
            while (byteCount>0)
            {
                //don't be too fast to read resource
                /*
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
                                */
                if (byteCount<bufferSize)
                    len=in.read(buffer,0,(int)byteCount);
                else
                    len=in.read(buffer,0,bufferSize);

                if (len==-1)
                    break;

                byteCount -= len;
                out.write(buffer,0,len);
            }
        }
        else
        {
            while (true)
            {
                len=in.read(buffer,0,bufferSize);
                if (len<0 )
                    break;
                out.write(buffer,0,len);

                /*
                //don't be too fast to read resource
                try {
                    Thread.sleep(50);
                catch (InterruptedException e) {}
                */
            }
        }
    }

    /**
     * UserCollection
     *
     * Inner class wrapping a Cursor over Contacts
     */
    public static class UserCollection extends DatabaseCollection
    {
        public UserCollection (Cursor cursor)
        {
            super(cursor);
        }

        public ContentValues cursorToValues(Cursor cursor)
        {
           return cursorToUserValues(cursor);
        }
       
    }
    
  

    
    static final String[] baseProjection = 
        new String[] 
                   {
                       android.provider.BaseColumns._ID,
                       android.provider.Contacts.PeopleColumns.DISPLAY_NAME,
                       android.provider.Contacts.PeopleColumns.NOTES,
                       android.provider.Contacts.PeopleColumns.STARRED,
                       android.provider.Contacts.PeopleColumns.SEND_TO_VOICEMAIL,
                       android.provider.Contacts.PeopleColumns.CUSTOM_RINGTONE
                   };
    
   
    
    
    /**
     * create
     * 
     * Create a new Contact.
     * 
     * @param resolver
     * @param values
     * @return
     */
    public static Uri create (ContentResolver resolver, ContentValues values)
    {
        if (resolver == null)
            return null;
        if (values == null)
            return null;
        
         return Contacts.People.createPersonInMyContactsGroup(resolver, values);        
    }


    public static void savePhoto (ContentResolver resolver, String id, File photo)
    {
        if (resolver == null)
            return;
        if (photo == null)
            return;
        if (id == null)
            return;
        Uri uri = Uri.withAppendedPath(Contacts.People.CONTENT_URI, id);
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            User.copy(new FileInputStream(photo), out, -1);
            Contacts.People.setPhotoData(resolver, uri, out.toByteArray());
            /*
            ContentValues values = new ContentValues();
            final ContentResolver mResolver = resolver;
          
            values.clear();
            values.put(Data.RAW_CONTACT_ID, id);
            values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
            values.putNull(Photo.PHOTO);
            Uri dataUri = mResolver.insert(Data.CONTENT_URI, values);
            long photoId = ContentUris.parseId(dataUri);

            values.clear();            
            values.put(Photo.PHOTO, out.toByteArray());
            int size = mResolver.update(dataUri, values, null, null);
            
            out.close();
            */
        }
        catch (Exception e)
        {
            Log.e("Jetty", "Problem converting photo to bytes for "+photo.getAbsolutePath(), e);
        }
    }

    /**
     * save
     * 
     * Update an existing Contact.
     * 
     * @param resolver
     * @param values
     * @param id
     */
    public static void save (ContentResolver resolver, ContentValues values, String id)
    {
        if (resolver == null)
            return;
        if (values == null)
            return;
        if (id == null)
            return;
        Uri uri = Uri.withAppendedPath(Contacts.People.CONTENT_URI, id);
        resolver.update (uri, values, null, null);
    }
    
    
    
    /**
     * delete
     * 
     * Delete a Contact.
     * 
     * @param resolver
     * @param id
     */
    public static void delete (ContentResolver resolver, String id)
    {
        if (id == null)
            return;
        
        resolver.delete(Uri.withAppendedPath(Contacts.People.CONTENT_URI, id), null, null);
    }
    
      
    
    /**
     * getAll
     * 
     * Get all Contacts.
     * 
     * @param resolver
     * @return
     */
    public static UserCollection getAll (ContentResolver resolver)
    {
        return new UserCollection(resolver.query(Contacts.People.CONTENT_URI, baseProjection, null, null, null)); 
    }
    
    
    
    /**
     * get
     * 
     * Get a single Contact.
     * 
     * @param resolver
     * @param id
     * @return
     */
    public static ContentValues get (ContentResolver resolver, String id)
    {
        if (id == null)
            return null;
        
        String[] whereArgs = new String[]{id};
        Cursor cursor = resolver.query(Contacts.People.CONTENT_URI, baseProjection, 
                "people."+android.provider.BaseColumns._ID+" = ?", 
                whereArgs, Contacts.PeopleColumns.NAME+" ASC");
        cursor.moveToFirst();
        ContentValues values =  cursorToUserValues(cursor);
        cursor.close();
        return values;
    }

    
   
    
    public static ContentValues cursorToUserValues(Cursor cursor)
    {
        if (cursor == null)
            return null;
        
        ContentValues values = new ContentValues();
        String val;
        val = cursor.getString(cursor.getColumnIndex(android.provider.BaseColumns._ID));  
        values.put(android.provider.BaseColumns._ID, val);
        
        val =  cursor.getString(cursor.getColumnIndex(Contacts.PeopleColumns.DISPLAY_NAME));
        values.put(Contacts.PeopleColumns.DISPLAY_NAME, val);
        
        Integer intVal = new Integer(cursor.getInt(cursor.getColumnIndex(Contacts.PeopleColumns.STARRED)));
        values.put(Contacts.PeopleColumns.STARRED, intVal);
        
        val = cursor.getString(cursor.getColumnIndex(Contacts.PeopleColumns.NOTES));
        values.put(Contacts.PeopleColumns.NOTES, val);
        
        intVal = new Integer(cursor.getInt(cursor.getColumnIndex(Contacts.PeopleColumns.SEND_TO_VOICEMAIL)));
        values.put(Contacts.PeopleColumns.SEND_TO_VOICEMAIL, intVal);
        
        val = cursor.getString(cursor.getColumnIndex(Contacts.PeopleColumns.CUSTOM_RINGTONE));
        values.put(Contacts.PeopleColumns.CUSTOM_RINGTONE, val);
        return values;
    }
}
