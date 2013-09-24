package oms.sns.service.facebook.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Stream implements Comparable, java.io.Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public String post_id;
	public long   viewer_id;
	public long   source_id;//come from who
	public int    type;
	public long   app_id;
	public long   actor_id; //who done this
	public long   target_id;//
	public String message;
	public String attribution;
	public boolean is_hidden;
	
	public Likes      likes;
	public Comments   comments;
	public Attachment attachment;
	public Action_Links links;
	
	public long updated_time;
	public long created_time;
	
	public String permalink;
	public boolean frompage;
	
	public boolean ididlikeit=false;
	public boolean isFromSerialize = false;
	/*
	 * 
	 * 
	 <likes>
        <href>http://www.facebook.com/s.php?k=100000004&amp;id=83745831466&amp;gr=2409997254</href>
        <count>1</count>
        <sample list="true"/>
        <friends list="true">
          <uid>1624264093</uid>
        </friends>
        <user_likes>0</user_likes>
        <can_like>1</can_like>
     </likes>
	 */
	public static class Likes implements java.io.Serializable 
	{
		private static final long serialVersionUID = 1L;
		public int count;
		public int user_likes;
		public int can_like;
		public List<Long> friends;	
		
		public Likes()
    	{
			friends = new ArrayList<Long>();
    	}
		
		public void despose()
		{
			friends.clear();
			friends = null;
		}
	}
	
	/*
	 *  
	 <comments>
        <can_remove>1</can_remove>
        <can_post>1</can_post>
        <count>2</count>
        <posts list="true">
          <stream_post>
            <fromid>1624264093</fromid>
            <time>1241406395</time>
            <text>don't play more Kaixin</text>
            <id>1624264093_83745831466_1650317</id>
          </stream_post>
          <stream_post>
            <fromid>625142542</fromid>
            <time>1241406909</time>
            <text>yeah, thanks</text>
            <id>1624264093_83745831466_1650389</id>
          </stream_post>
          
        </posts>
     </comments>
	 */
    public static class Comments implements java.io.Serializable
    {
    	private static final long serialVersionUID = 1L;
    	public int               count;
    	public List<Stream_Post> stream_posts;
    	
    	public Comments()
    	{
    		stream_posts = new ArrayList<Stream_Post>();
    	}
    	
    	public void despose()
    	{
    		while(stream_posts.size() > 0)
    		{
    			Stream_Post item = stream_posts.get(0);    			
    			item.despose();
    			
    			stream_posts.remove(0);    			
    		}
    		
    		stream_posts = null;
    	}
    	
    	public static class Stream_Post implements Comparable, java.io.Serializable
    	{
    		private static final long serialVersionUID = 1L;
	    	public long   fromid;
	    	public long   time;
	    	public String id;
	    	public String text;
	    	public String username;
	    	public long   parentsuid;	    	
	    	
	    	public void despose()
	    	{
	    		id   = null;
	    		text = null;
	    	}
	    	public String toString()
	    	{
	    		return " fromid="+fromid +
	    		       " time  = "+time+
	    		       " id    = "+id+
	    		       " Text  = "+text+
	    		       " username="+username;
	    	}
	    	
            public int compareTo(Object another) 
            {
                Stream_Post item = (Stream_Post)another;
                if(item.time < this.time)
                {
                    return -1;
                }
                else if(item.time > this.time)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
               
            }
    	}    	
	}
    
    
    public static class CommentsParcel implements Parcelable 
    {
        public Comments group;
        public long source_id;
        
    	public int describeContents() 
    	{	
    		return 0;
    	}
    	
    	 public CommentsParcel(Comments groups)
         {
    		 group = groups;
         }
    	 
    	 public CommentsParcel(Comments groups,Long source_id)//source_id ====> who post the stream
    	 {
    	     group = groups;
    	     this.source_id = source_id.longValue();
    	 }
    	 public CommentsParcel(Parcel in)
         {
             readFromParcel(in);
         }
    	 
         public void readFromParcel(Parcel in)
         {
             group = new Comments();
             
             group.count = in.readInt();
             source_id   = in.readLong();
        	 int tmp      = in.readInt();
        	 for(int i=0;i<tmp;i++)
        	 {
        		 Comments.Stream_Post so = new Comments.Stream_Post();
        		 so.fromid = in.readLong();
        		 so.time   = in.readLong();
        		 so.id     = in.readString();
        		 so.text   = in.readString();
        		 so.parentsuid = in.readLong();
        		 group.stream_posts.add(so);
        	 }
        	     	 
         }
         
    	 public void writeToParcel(Parcel out, int arg1) 
    	 {
    	     out.writeInt(group.count);
    	     out.writeLong(source_id);
    		 out.writeInt(group.stream_posts.size());
    		 for(int i=0;i<group.stream_posts.size();i++)
    		 {
    			 Comments.Stream_Post so = group.stream_posts.get(i);
    			 out.writeLong(so.fromid);
    			 out.writeLong(so.time);
    			 out.writeString(so.id);
    			 out.writeString(so.text);
    			 out.writeLong(so.parentsuid);
    		 }
    		
    	 }
    	 
    	 public static final Parcelable.Creator<CommentsParcel> CREATOR
    		     = new Parcelable.Creator<CommentsParcel>() 
    		 {
    			 public CommentsParcel createFromParcel(Parcel in) 
    			 {
    				 return new CommentsParcel(in);
    		     }
    	
    	     public CommentsParcel[] newArray(int size) 
    	     {
    	         return new CommentsParcel[size];
    	     }
         };
    }

    
    /*
     * 
      <attachment>
	    <media list="true">
	    <stream_media>
			<href>http://apps.facebook.com/ninetype/</href>
			<type>link</type>
			<src>
				http://platform.ak.facebook.com/www.new/app_full_proxy.php?app=89714566800&v=1&size=b&cksum=37237995d0005597e65f324a445a31f8&src=http%3A%2F%2Fimg528.imageshack.us%2Fimg528%2F5195%2Fninetype.gif
			</src>
		</stream_media>
	    <media/>
	    <name>sssss</name>
	    <href>http://www.facebook.com/ext/share.php?sid=83745831466&amp;h=jMMwy&amp;u=ELNpZ</href>
	    <caption>Source: tech.sina.com.cn</caption>
	    <description>xxxxx</description>
	    <properties list="true"/>
      </attachment>
    */
    public static class Attachment implements java.io.Serializable
    {
    	private static final long serialVersionUID = 1L;
		public String description;
    	public String name;
    	public String href;
    	public String caption;
    	public String icon;
    	
    	public List<Media> attachments;
    	
    	public Attachment()
    	{
    		attachments = new ArrayList<Media>();
    	}
    	
    	public static class Media implements java.io.Serializable
    	{
	    	private static final long serialVersionUID = 1L;
			public String href;
	    	public String type;
	    	public String src;
	    	
	    	public void despose()
	    	{
	    		href = null;
	    		type = null;
	    		src  = null;
	    	}
    	}
    	
    	public void despose()
    	{
    		description = null;
    		name = null;
    		href = null;
    		caption  = null;
    		icon = null;
    		
    		for(Media item: attachments)
    		{
    			item.despose();
    		}
    		attachments.clear();
    		
    		attachments = null;
    	}
    }   
    
    /*
     * 
        <action_links>
            <text/>
            <href/>
        </action_links>        
    */
    public static class Action_Links implements java.io.Serializable
    {
    	private static final long serialVersionUID = 1L;
		public List<Links> links;
    	public Action_Links()
    	{
    		links = new ArrayList<Links>();
    	}
    	
    	
    	public void despose()
    	{
    		for(Links item: links)
    		{
    			item.despose();
    		}
    		links.clear();
    		links = null;
    	}
    	public static class Links implements java.io.Serializable
    	{
    		private static final long serialVersionUID = 1L;
	    	public String text;
	    	public String href;
	    	public void despose()
	    	{
	    		text = null;
	    		href = null;
	    	}
    	}
    }
    
    public int compareTo(Object another) 
	{		
		if(Stream.class.isInstance(another))
		{
			long anDate = ((Stream)another).created_time;
			if(created_time > anDate)
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

	public void dispose() 
	{
		post_id = null;		
		message = null;
		attribution = null;
		
		if(likes != null)
		{
			likes.despose();
			likes = null;
		}
		
		if(comments != null)
		{
			comments.despose();
			comments = null;
		}
		
		if(attachment != null)
		{
			attachment.despose();
			attachment = null;
		}
		
		if(links != null)
		{
			links.despose();
			links = null;
		}
		
		permalink = null;		
	}
}
