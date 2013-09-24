package com.msocial.facebook.service.dell;

import android.content.Context;
import com.msocial.facebook.providers.SocialORM;
import oms.sns.service.facebook.model.PhoneBook;

public class ContactInternal 
{
	protected static boolean isEmpty(String str)
	{
	    	return str==null || str.length() ==0;
	}
	  
	public static int AddNewPhoneBook(Context con, SocialORM orm, PhoneBook phone)
	{
	    String peopleid = null;
        int pid = orm.getPhonebookPeopleID(phone.uid);
        if(pid != 0)
        {
            peopleid = String.valueOf(pid);
        }
        ContactHelp4Cupcake.saveUserFormData(con, peopleid , phone, orm);
        return 1;
	}
}
