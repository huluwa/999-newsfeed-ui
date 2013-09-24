package oms.sns.service.facebook.model;

public class Video {
    public long vid;
    public String title;
    public String description;
    public String link;
    
	public Long getVid() {
		return vid;
	}
	
	public void setVid(Long vid) {
		this.vid = vid;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	/*
	 * <vid>15943367753</vid>  
	 * <title>Some Epic</title> 
	 *  <description>Check it out</description> 
	 *   <link>http://www.facebook.com/video/video.php?v=15943367753</link>
	 */
	public enum Field 
	{
		VID,TITLE,DESCRIPTION,LINK;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}	
}
