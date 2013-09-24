package oms.sns.service.facebook.model;

import java.io.Serializable;

public class StreamFilter  implements Comparable, Serializable
{
	 public long   uid;		
	 public String filter_key;
	 public String name;
	 public int    rank;
	 public String icon_url;
	 public boolean is_visible;
	 public String  type;
	 public String  value;
	 
	public int compareTo(Object another) 
	{
		if(StreamFilter.class.isInstance(another))
		{
			int crank  = ((StreamFilter)another).rank;
			if(rank > crank)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}		
		return 0;		
	}
	
	public void despose()
	{
		uid = 0;
		filter_key = null;
		name = null;
		rank = 0;
		icon_url = null;
		type = null;
		value = null;
	}
}
