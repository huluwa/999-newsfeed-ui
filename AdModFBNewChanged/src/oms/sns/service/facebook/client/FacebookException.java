/**
 * fb4j: Java API for Facebook
 * 
 * (c) 2007 belasius.com
 */
package oms.sns.service.facebook.client;

/**
 * Base class for checked exceptions thrown by fb4j API
 * 
 * @author Gino Miceli
 */
public class FacebookException extends Exception
{
	private static final long serialVersionUID	= 1L;

	private int errorCode;
	public int getErrorCode()
	{
		return this.errorCode;
	}
	public FacebookException()
	{
		super();
	}

	public FacebookException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public FacebookException( String message )
	{
		super( message );
	}

	public FacebookException( Throwable cause )
	{
		super( cause );
	}

	public FacebookException(String cause, int responseCode) 
	{
		super( cause );
		errorCode = responseCode;
	}
}
