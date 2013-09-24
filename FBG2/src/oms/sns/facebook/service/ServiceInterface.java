package oms.sns.facebook.service;

public interface ServiceInterface {
	public void Start();
	public void Stop();
	public void afterLogin();
	public void logout();
	public void Pause();	
}
