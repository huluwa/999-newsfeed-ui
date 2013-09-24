package com.msocial.facebook.service.dell;

import java.io.Serializable;

public class ContactID implements Serializable, Comparable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3505392654485332515L;
	public long uid;
	public int people_id;
	public int phone_id;
	public int email_id;	
	public String email;
	public String cell;
	public String phone;
	
	public ContactID()
	{
	    uid = -1;
		people_id = -1;
		phone_id  = -1;
		email_id  = -1;
	}
	public ContactID(int people_id)
	{
		this.people_id=people_id;
		phone_id = -1;
		email_id = -1;
		uid = -1;
	}

	public int compareTo(Object obj) {
		if(ContactID.class.isInstance(obj))
		{
			ContactID item = (ContactID)(obj);
			if(item.people_id>this.people_id)
				return 1;
			else if(item.people_id<this.people_id)
				return -1;
			else
				return 0;
				
		}
		return 0;
	}
}
