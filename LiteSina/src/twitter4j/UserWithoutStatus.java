package twitter4j;

import org.w3c.dom.Element;

import twitter4j.http.Response;

public class UserWithoutStatus extends User {   
    
    private static final long serialVersionUID = -3338496376247577523L;

    public UserWithoutStatus(Response res, Twitter twitter) throws TwitterException {
        super(res, twitter);        
        
        if(twitter.exitma())
            throw new TwitterException("activity is onPause or onDestroy");
        
        twitter.finishNetwork();
    }   
}
