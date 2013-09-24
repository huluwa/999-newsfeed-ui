package oms.sns.facebook.ui.adapter;

import java.util.List;

import oms.sns.service.facebook.model.PhotoAlbum;

import android.content.Context;
import android.widget.ArrayAdapter;

public class FacebookAlbumSpinnerAdapter extends ArrayAdapter{
    List<PhotoAlbum> photoalbums ;
    Context mContext;
    public FacebookAlbumSpinnerAdapter(Context context,
            int resource, List<PhotoAlbum> albums) {
        super(context,resource, albums);
        photoalbums = albums;
        mContext = context;
    }
    @Override
    public int getCount() {
        return photoalbums.size();
    }
    @Override
    public PhotoAlbum getItem(int position) {
        return photoalbums.get(position);
    }   
    
    public int getPos(String aid)
    {
        for(int i=0;i<photoalbums.size();i++)
        {
            if(photoalbums.get(i).aid.equals(aid))
            {
                return i;
            }
        }
        
        return -1;
    }
}
