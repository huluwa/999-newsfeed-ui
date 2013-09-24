package oms.sns.service.facebook.client;

public class FacebookPermissionErrorException extends Exception{
    private static final long serialVersionUID  = 1L;

    private int errorCode;
    private String method;
    public int getErrorCode()
    {
        return this.errorCode;
    }

    public FacebookPermissionErrorException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public FacebookPermissionErrorException( String message )
    {
        super( message );
    }

    public FacebookPermissionErrorException( Throwable cause )
    {
        super( cause );
    }

    public FacebookPermissionErrorException(String methodname, int responseCode,String errorMsg) 
    {
        super(errorMsg);
        errorCode = responseCode;
        method = methodname;
    }

}
