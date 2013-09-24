package com.ast.free.ui.adapter;

import java.util.List;

import com.ast.free.ui.view.CommentItemView;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.Stream.Comments;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookPhotoCommentAdapter  extends BaseAdapter{
    private final String TAG = "FacebookPhotoCommentAdapter";        
    private Context mContext;   
    private List<PhotoComment> mCommentItems;
    
    public List<PhotoComment> getMCommentItems() {
        return mCommentItems;
    }
    public FacebookPhotoCommentAdapter(Context con,List<PhotoComment> comments)
    {
        mContext = con;
        mCommentItems = comments;
        Log.d(TAG, "create FacebookPhotoCommentAdapter");
    }
    public int getCount() 
    {
        return mCommentItems.size();
    }
    
    public Object getItem(int pos) {        
        return mCommentItems.get(pos);
    }
    
    public long getItemId(int pos) 
    {
        return mCommentItems.get(pos).from;
    }
    
    public View getView(int position, View convertView, ViewGroup arg2) 
    {       
         if (position < 0 || position >= getCount()) 
         {
             return null;    
         }
         CommentItemView v = null;
    
         PhotoComment di = (PhotoComment)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new CommentItemView(mContext, di);
         } else {
              v = (CommentItemView) convertView;
              v.setCommentItem(di);
         }        
         v.chooseCommentListener();
         return v;
    } 
}
