package com.msocial.nofree.ui;

import com.msocial.nofree.*;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.FacebookBaseActivity;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Notes;

public class FacebookNoteEditActivity extends FacebookBaseActivity{
    final String TAG = "FacebookNoteEditActivity";
    private View     facebook_compose_span;
    private View     facebook_compose_title_span;
    private TextView textCount;
    private EditText sendcontent;
    private EditText sendtitle;
    private MyWatcher watcher;
    private long      note_id;    
    Button facebook_save_note;
    
    protected void showOptionMenu() 
    {    
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_note_edit_ui);
        
        FacebookNoteEditActivity.this.setTitle(R.string.facebook_notes_title);
        
        facebook_compose_span = this.findViewById(R.id.facebook_compose_span);
        facebook_compose_span.setVisibility(View.VISIBLE);
        
        facebook_compose_title_span = this.findViewById(R.id.facebook_compose_title_span);
        facebook_compose_title_span.setVisibility(View.VISIBLE);
        sendtitle  = (EditText) this.findViewById(R.id.facebook_wall_message_title_editor);
        sendtitle.setHint(R.string.notes_title);        
        
       
        textCount = (TextView)this.findViewById(R.id.facebook_wall_text_counter);
        sendcontent = (EditText) this.findViewById(R.id.facebook_wall_message_editor);
        sendcontent.setHint(R.string.notes_content);
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(4*1024)};
        sendcontent.setFilters(filters);   
        sendcontent.setVerticalScrollBarEnabled(true);
        watcher = new MyWatcher();      
        sendcontent.addTextChangedListener(watcher);
        
        facebook_save_note = (Button)this.findViewById(R.id.facebook_save_note);
        facebook_save_note.setOnClickListener(saveClick);        
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);           
                facebookA = new AsyncFacebook(perm_session);     
                showUI(getIntent());
            }
            else
            {
                launchFacebookLogin();
            }
        }        
    }
    
    private void showUI(Intent intent)
    {
        if(intent != null)
        {
            note_id = intent.getLongExtra("note_id", -1);            
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            if(isEmpty(title) == false)   sendtitle.setText(title);
            if(isEmpty(content) == false) sendcontent.setText(content);
        }
        
        if(note_id > 0)
        {
        	facebook_save_note.setText(R.string.facebook_notes_edit);
        }
        else
        {
        	facebook_save_note.setText(R.string.facebook_notes_new);
        }
        setTitle();
        setTitle(title);
    }
    
    View.OnClickListener saveClick = new View.OnClickListener() {
		
		public void onClick(View v) {
			handler.obtainMessage(FACEBOOK_NOTES_CREATE).sendToTarget();
		}
	};
    
	/*
    @Override
    public void titleSelected() 
    {   
        super.titleSelected();
        
        //create the new notes
        handler.obtainMessage(FACEBOOK_NOTES_CREATE).sendToTarget();
    }*/
    
    protected  static final int FACEBOOK_NOTES_CREATE      = 1;
    protected  static final int FACEBOOK_NOTES_CREATE_END  = 2;
    private class NotesHanlder extends Handler
    {
        public NotesHanlder()
        {
            super();            
            Log.d(TAG, "new NotesHanlder");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {               
                case FACEBOOK_NOTES_CREATE:
                {
                    createNotes();
                    break;
                }
                case FACEBOOK_NOTES_CREATE_END:
                {
                    end();
                    facebook_save_note.setEnabled(true);
                    dismissDialog(DLG_SAVE_NOTE);
                    if(msg.getData().getBoolean(RESULT) == true)
                    {
                        setResult(100);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(FacebookNoteEditActivity.this, R.string.fail_create_notes, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }     
            }
        }        
    }
    
    private void createNotes()
    {
        if(this.isInProcess() == true)
        {
            dismissDialog(DLG_SAVE_NOTE);
            showDialog(DLG_SAVE_NOTE);
            return;
        }
        if(existSession() == false)
        {
            return;
        }              
        String content = sendcontent.getText().toString().trim();
        String title   = sendtitle.getText().toString().trim();
        if(isEmpty(content) == true || isEmpty(title) == true)
        {
            Log.d(TAG, "need title and content="+this);
            return ;
        }       
        
        begin();     
        facebook_save_note.setEnabled(false);
        Log.d(TAG, "before create notes message");
        
        synchronized(mLock)
        {
            inprocess = true;
        }

        showDialog(DLG_SAVE_NOTE);
        
        if( note_id > 0 )
        {
            editNote(note_id,title,content);
        }
        else
        {
            createNote(title,content);
        }
    }
    
    private void editNote(long noteid,final String title,final String content)
    {
        facebookA.editNotesAsync(note_id,title,content, new FacebookAdapter()
        {
            @Override public void createNotes(boolean suc)
            {
                Log.d(TAG, "after eite notes="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                Notes note = orm.getNoteByNid(note_id);
                note.title = title;
                note.content = content;
                orm.insertNote(note);
                Message msd = handler.obtainMessage(FACEBOOK_NOTES_CREATE_END); 
                msd.getData().putBoolean(RESULT, true);
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to create notes="+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                     Message msd = handler.obtainMessage(FACEBOOK_NOTES_CREATE_END);       
                     msd.getData().putBoolean(RESULT, false);
                     handler.sendMessage(msd);
                }
            }
        });
    }
    private void createNote(String title,String content)
    {
        facebookA.createNotesAsync(title,content, new FacebookAdapter()
        {
            @Override public void createNotes(boolean suc)
            {
                Log.d(TAG, "after create notes="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTES_CREATE_END); 
                msd.getData().putBoolean(RESULT, true);
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to create notes="+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                     Message msd = handler.obtainMessage(FACEBOOK_NOTES_CREATE_END);       
                     msd.getData().putBoolean(RESULT, false);
                     handler.sendMessage(msd);
                }
            }
        });
    }
    
    @Override
    protected void createHandler() 
    {
        handler = new NotesHanlder();
    }
    
    public void setTitle() 
    {
       // title = getString(R.string.facebook_notes_title); 
        if(note_id >0 )
        {
            title = getString(R.string.facebook_notes_edit);
        }
        else
        {
            title = getString(R.string.facebook_notes_new);
        }
    }

    private class MyWatcher implements TextWatcher 
    {   
        public void afterTextChanged(Editable s) 
        {
            textCount.setText(String.format("%1$s", s.length()));
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) 
        {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
  
    public void registerAccountListener() {
        AccountManager.registerAccountListener("FacebookNotesActivity", this);          
    }

    public void unregisterAccountListener() {
        AccountManager.unregisterAccountListener("FacebookNotesActivity");          
    }
}
