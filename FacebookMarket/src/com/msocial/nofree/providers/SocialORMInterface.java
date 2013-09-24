package com.msocial.nofree.providers;

public interface SocialORMInterface 
{
	//if no user exist, please create the user and set the image url
    public boolean updateUserImageUrl(long uid, String url);
    
    public boolean isExtPermissionAllow(String permission);
    public boolean clearExtPermissions();
    public boolean disableExtPermissions(String permission);
    public boolean enableExtPermissions(String permission);
}
