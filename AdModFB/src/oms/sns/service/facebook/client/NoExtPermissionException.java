package oms.sns.service.facebook.client;

public class NoExtPermissionException extends Exception
{
	private static final long serialVersionUID	= 1L;

	private int errorCode;

	private String method;
	public int getErrorCode()
	{
		return this.errorCode;
	}

	public NoExtPermissionException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public NoExtPermissionException( String message )
	{
		super( message );
	}

	public NoExtPermissionException( Throwable cause )
	{
		super( cause );
	}

	public NoExtPermissionException(String cause, int responseCode) 
	{
		super( cause );
		errorCode = responseCode;
		method = cause;
	}

	public String getExtPermisson() 
	{		
		String ext = "";
		switch(errorCode)
		{
		    case 250:
		    {
		    	ext = "status_update";
			    break;
		    }
		    case 260:
		    {
		    	ext = "photo_upload";
			    break;
		    }
		    case 270:
		    {
		    	ext = "sms";
			    break;
		    }
		    case 280:
		    {
		    	ext = "create_listing";
			    break;
		    }
		    case 281:
		    {
		    	ext = "create_note";
			    break;
		    }
		    case 282:
		    {
		    	ext = "share_item";
			    break;
		    }
		    case 290:
		    {
		    	ext = "create_event";
			    break;
		    }
		    case 298:
		    {
		        ext = "read_mailbox";
		        break;
		    }
		    /*
		    case 291:
		    {
		    	ext = "";
			    break;
		    }
		    case 292:
		    {
		    	ext = "";
			    break;
		    }*/
		    case 299:
		    {
		    	ext = "rsvp_event";
			    break;
		    }
		    default:
		    {
		    	ext = FacebookMethod.getExtPermission(method);
		    	break;
		    }
		}
		
		return ext; 		
	}
}
