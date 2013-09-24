package com.msocial.facebook.ui;

import java.net.URLDecoder;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.ui.view.ImageRun;
import com.msocial.facebook.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Notes;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FacebookNoteDetailActivity extends FacebookBaseActivity{
    final static String TAG = "FacebookNoteDetailActivity";
    ImageView imageview;
    TextView  usernameview;
    TextView  timeview;
    TextView  titleview;
    TextView  contentview;
    Notes note;
    long noteid;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_note_detail);  
        imageview = (ImageView)this.findViewById(R.id.note_user_img_ui);
        usernameview = (TextView)this.findViewById(R.id.note_user_name);
        timeview = (TextView)this.findViewById(R.id.note_publish_time);
        titleview = (TextView)this.findViewById(R.id.note_publish_title);
        contentview = (TextView)this.findViewById(R.id.note_publish_content);
        this.setTitle(R.string.facebook_detail_note_title);
        noteid = this.getIntent().getLongExtra("noteid", -1);
        if(noteid > 0)
        {
            note = orm.getNoteByNid(noteid);
            if(note != null)
            {
                setUI(note); 
            }
        }
    }
    
    String imageURL = "";
    private void setUI(Notes note)
    {   
        FacebookUser user = orm.getFacebookUser(note.uid);           
        if(user != null)
        {
            usernameview.setText(user.name);
            imageURL = user.pic_square;
        }    
        
        ImageRun imagerun = new ImageRun(handler, imageURL, 0);  
        imagerun.use_avatar = true;
        imagerun.setImageView(imageview);
        imagerun.post(imagerun);
        
        timeview.setText(DateUtil.converToRelativeTime(mContext, note.updated_time));   
        titleview.setText(note.title);
        
        contentview.setText(note.content);
    }
    
    @Override
    protected void editNote() {
        if(note != null)
        {
            Intent intent = new Intent(mContext,FacebookNoteEditActivity.class);
            intent.putExtra("note_id", note.note_id);
            intent.putExtra("content", note.content);
            intent.putExtra("title",   note.title);
            startActivityForResult(intent, RESULT_OK);
        }
    }
    
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        Log.d(TAG, "entering activity result "+requestCode +" resultCode is "+resultCode);
        if(resultCode == 100)
        {
            note = orm.getNoteByNid(noteid);
            setUI(note);
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
    

    @Override
    protected void onResume() {
        Log.d(TAG, "entering onResume");
        note = orm.getNoteByNid(noteid);
        setUI(note);
        super.onResume();
    }

    @Override
    protected void createHandler() {
        // TODO Auto-generated method stub
        handler = new NoteDetailHandler();
    }

    public void setTitle() {
       title = getString(R.string.facebook_detail_note_title);
    }

    public class NoteDetailHandler extends Handler
    {
        public NoteDetailHandler()
        {
            super();            
            Log.d(TAG, "new NoteDetailHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            }
        }           
    }
            

}
