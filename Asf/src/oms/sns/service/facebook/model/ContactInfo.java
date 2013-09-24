package oms.sns.service.facebook.model;

import oms.sns.service.facebook.client.FacebookField;
import oms.sns.service.facebook.client.FacebookObject;
import oms.sns.service.facebook.model.UserInfo.Field;

public class ContactInfo extends FacebookObject<ContactInfo.Field>{
    
	public Long getUid(){
		return getLong(Field.UID);
	}
	
	public String getEmail(){
		return getString(Field.EMIL);
	}
	
	public String getCell(){
		return getString(Field.CELL);
	}
	
	public String getPhone(){
		return getString(Field.PHONE);
	}
	
	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}
	
	public enum Field implements FacebookField
	{
		UID,EMIL,CELL,PHONE;
		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
