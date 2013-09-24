package com.ast.free.service.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Entity implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4885791295473778505L;
	
	public long    people_id;
	List<String>  data;		
	public Entity()
	{
		data = new ArrayList<String>();
	}
}
