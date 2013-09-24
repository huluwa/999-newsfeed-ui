package com.msocial.facebook.ui;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.FacebookUsersCol;
import com.msocial.facebook.ui.view.FacebookFriendItemView;
import oms.sns.service.facebook.model.FacebookUser;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
public class FacebookFriendsBirthdayActivity extends ExpandableListActivity implements View.OnCreateContextMenuListener{
    final static String TAG = "FacebookFriendsBirthdayActivity";
    private FriendBirthdayAdapter friendBDAdapter;
    private ExpandableListView friendList;
    private ListView listView;
    private SocialORM orm;
    @Override
    protected void onCreate(Bundle icicle) {
        
        super.onCreate(icicle);   
        Drawable mCacheSym = getResources().getDrawable(R.color.facebook_backgroud);
        this.getWindow().setBackgroundDrawable(mCacheSym);
        orm = new SocialORM(this);
        friendList = getExpandableListView();
        friendList.setCacheColorHint(Color.WHITE);
        friendList.setSelector(R.drawable.list_selector_background);
        friendBDAdapter = new FriendBirthdayAdapter(this,orm);
        
        friendList.setOnCreateContextMenuListener(this);
        friendList.setAdapter(friendBDAdapter);   
    }
    
    public class FriendBirthdayAdapter extends BaseExpandableListAdapter
    {
        String[] groups = {"January","February","March","April","May","June","Junly","August","September","December","Octomber","Nobember"};    
        Context mContext;
        SocialORM orm;
        public FriendBirthdayAdapter(Context context,SocialORM orm)
        {   
            Log.d(TAG,"entering FriendBirthdayAdapter ...");
            mContext = context;
            this.orm = orm;
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            FacebookUser suer = null;
            Cursor temp= orm. getUpComingBDUserCursor(groups[groupPosition]);
            if(temp!=null)
            {
               if(temp.moveToPosition(childPosition))
               {
                   suer= orm.formatFacebookUser(temp);
               }
               temp.close();               
            }
            return suer;
        }

        public long getChildId(int groupPosition, int childPosition) {
            long uid = -1;
            Cursor temp= orm. getUpComingBDUserCursor(groups[groupPosition]);
            if(temp!=null)
            {
               if(temp.moveToPosition(childPosition))
               {
                   uid=  temp.getLong(temp.getColumnIndex(FacebookUsersCol.UID));
               }
               temp.close();               
            }
            return uid;
        }

        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {             
                
            if (childPosition < 0 || childPosition >= getChildrenCount(groupPosition)) 
            {
                return null;    
            }
            
            FacebookFriendItemView v=null;
       
            FacebookUser.SimpleFBUser di = (FacebookUser.SimpleFBUser)getChild(groupPosition, childPosition);
            if (convertView == null) {
                v = new FacebookFriendItemView(mContext, di,true);
            } else {
                 v = (FacebookFriendItemView) convertView;
                 v.setUserItem(di,true);
            }
            return v;
        }

        public int getChildrenCount(int groupPosition) {
            int size = 0;
            Cursor temp= orm. getUpComingBDUserCursor(groups[groupPosition]);
            if(temp!=null)
            {
               size = temp.getCount();
               temp.close();               
            }
            
            return size;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            
            return textView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        } 
        
        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
            TextView textView = new TextView(FacebookFriendsBirthdayActivity.this);            
            textView.setTextAppearance(mContext, R.style.sns_link);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }        
    }    
    
    @Override
    public void onGroupCollapse(int groupPosition) {
        super.onGroupCollapse(groupPosition);
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        super.onGroupExpand(groupPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }
    
    
  
}
