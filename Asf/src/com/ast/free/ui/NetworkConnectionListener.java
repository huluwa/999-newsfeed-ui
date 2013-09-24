package com.ast.free.ui;

import java.net.HttpURLConnection;

public interface NetworkConnectionListener {
	    public void stopLoading();
	    public void addHttpConnection(int UID, HttpURLConnection con);	    
	    public void releaseHttpConnection(int UID);
        public void addRunnable(Integer uid, Runnable run);
            
        public void titleUpdateAfterNetwork();
        public void updateProgress(int pos, int count);
        	
}
