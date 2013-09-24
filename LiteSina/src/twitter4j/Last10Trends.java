package twitter4j;

import java.util.ArrayList;

import twitter4j.org.json.JSONArray;
import twitter4j.org.json.JSONException;
import twitter4j.org.json.JSONObject;


public class Last10Trends  
{
	public Last10Trends(JSONObject asJSONObject, Twitter twitter) throws TwitterException{
	    if(twitter.exitma())
            throw new TwitterException("activity is onPause or onDestroy");
	    
	    twitter.finishNetwork();
	    
		 try {
			as_of = asJSONObject.getString("as_of");
		
			 JSONArray array = asJSONObject.getJSONArray("trends");
			 last_10 = new ArrayList<TrendsItem>(array.length());
	         for (int i = 0; i < array.length(); i++) 
	         {
	             JSONObject item = array.getJSONObject(i);
	             last_10.add(new TrendsItem(item, twitter));
	         }
		 }
		 catch (JSONException e) 
		 {
		 }
	}
	public Last10Trends() 
	{
		last_10 = new ArrayList<TrendsItem> (); 
	}
	public ArrayList<TrendsItem> last_10 = null;
	public String as_of;	
	
	public class TrendsItem
	{
		public TrendsItem(JSONObject item, Twitter twitter) {
			try {
				name = item.getString("name");
				link = item.getString("url");
			} catch (JSONException e) {}
			
		}
		
		public TrendsItem(String name, String link) 
		{
			this.name = name;
			this.link = link;
		}
		
		public String name;
		public String link;
		
		@Override
	    public String toString() {
			return "{name="+name +
					", url="+link;
		}
	}
	
	@Override
	public String toString() 
	{
        return "Last 10 trends{" +
                "as_od=" + as_of +	                
                ", trends=" + last_10 +
                '}';
    }

}
