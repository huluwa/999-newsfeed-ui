package twitter4j;

public class AndroidAsyncTwitter extends AsyncTwitter {
    private static final long serialVersionUID = -2008667933225051907L;

    public AndroidAsyncTwitter(String id, String password) {
        super(id, password);
    }
    
    public AndroidAsyncTwitter(String id, String password, String baseURL) {
        super(id, password, baseURL);
    }

	public AndroidAsyncTwitter(String param1, String param2, boolean oauth) {
		super(param1,param2,oauth);
	}
}

