package oms.sns.facebook.ui.lisenter;

public interface SyncLisenter {
  
	//public void stopSync();
	//public void startSync();
	public void alertMsg(String message);
	public void syncProgress(int progress);
}
