package oms.sns.service.facebook.model;

public class PhotoComment implements Comparable{
    public String pid;
    public long from;
    public String body;
    public long time;
    public String pcid;
    
    public int compareTo(Object another) {
        PhotoComment item = (PhotoComment)another;
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
