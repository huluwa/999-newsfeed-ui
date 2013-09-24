package com.ast.free.service.internal;

import java.util.List;

import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.PhoneBook;

public class OmsServiceAdapter implements OmsServiceListner{

	public void onException(FacebookException te, int method, Object[] args) {}
	
	public void eventSync(Long tid,List<Event> events){return;}
	
	public void eventAdd(Long tid,Long eid,Long p_eid){return;}

	public void contactSync(Long tid, List<PhoneBook> phonebooks) {return;}
	public void phoneLookup(List<Long> tids, List<PhoneBook> phoneBookLookup) 
	{		
		return;
	}
	public void phoneLookup( List<PhoneBook> phoneBookLookup) 
    {       
        return;
    }
	public void phoneLookup(List<Long> tids, List<Long>peopleids, List<PhoneBook> phoneBookLookup){}

	public void phoneLookupAll(Long tid) {
		// TODO Auto-generated method stub
		
	}

    public void addAsFriend(Long tid, boolean retvalue) {
        // TODO Auto-generated method stub
        return;
    }
	
}
