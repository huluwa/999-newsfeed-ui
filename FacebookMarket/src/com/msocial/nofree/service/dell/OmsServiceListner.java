package com.msocial.nofree.service.dell;

import java.util.HashMap;
import java.util.List;

import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.ContactInfo;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.PhoneBook;

public interface OmsServiceListner 
{	
	void onException(FacebookException te, int method, Object[]args);

	void phoneLookup(List<Long> tid,List<PhoneBook> phoneBookLookup);
	
    void eventSync(Long tid,List<Event> events);
    
    void eventAdd(Long tid,Long eid,Long p_eid);
    
    void contactSync(Long tid,List<PhoneBook> phonebooks);
    
    void phoneLookupAll(Long tid);

    void phoneLookup(List<Long> tids, List<Long> peopleids, List<PhoneBook> phoneBookLookup);

    void addAsFriend(Long tid, boolean retvalue);

    void phoneLookup(List<PhoneBook> phoneBookLookup);
}
