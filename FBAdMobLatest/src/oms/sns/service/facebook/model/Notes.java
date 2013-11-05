package oms.sns.service.facebook.model;

import java.util.Date;

public class Notes  implements Comparable {
	public long   note_id;
	public String title;
	public String content;
	public long   created_time;
	public long   updated_time;
	public long   uid;
	public long   id;
	
	public int compareTo(Object another) 
	{		
		if(Notes.class.isInstance(another))
		{
			long anDate = ((Notes)another).updated_time;
			if(updated_time > anDate)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		
		return 0;
	}

    public void despose() {
         title = null;
         content = null;
    }
}
