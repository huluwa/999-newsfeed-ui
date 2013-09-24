package oms.sns.service.facebook.client;


public class InvalidSesssionException extends Exception
{
	private static final long serialVersionUID	= 1L;

	private int errorCode;

	public int getErrorCode()
	{
		return this.errorCode;
	}

	public InvalidSesssionException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public InvalidSesssionException( String message )
	{
		super( message );
	}

	public InvalidSesssionException( Throwable cause )
	{
		super( cause );
	}

	public InvalidSesssionException(String cause, int responseCode) 
	{
		super( cause );
		errorCode = responseCode;
		
	}
}
