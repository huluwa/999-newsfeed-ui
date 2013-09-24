package twitter4j;

import com.tormas.litesina.ui.TwitterBaseActivity;

public class AndroidTwitter extends Twitter implements java.io.Serializable {
    private static final long serialVersionUID = -7550633067620779907L;
    
    public AndroidTwitter() 
    {
        super();
    }
    
    public void attachActivity(TwitterBaseActivity baseAc)
    {
        super.attachActivity(baseAc);
        http.attachActivity(baseAc);
        
        if(baseAc != null)
        {
        	this.setUseHttps(baseAc.getSocialORM().getTwitterUseHttps());
        }
    }

    public AndroidTwitter(String baseURL) {
       super(baseURL);
    }

    public AndroidTwitter(String id, String password) {
       super(id, password);
    }

    public AndroidTwitter(String id, String password, String baseURL) {
       super(id, password, baseURL);
    }

	public AndroidTwitter(String param1, String param2, boolean oauth) {
		super(param1,param2,oauth);
	}  
}
