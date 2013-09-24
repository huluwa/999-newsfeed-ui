package oms.sns.facebook.service;

import android.os.Parcel;
import android.os.Parcelable;

interface FacebookAPI{
        boolean addEventToFacebook(in int eid);
        boolean syncFacebookEvent(in boolean forced);
        boolean syncFacebookContact(in boolean forced);
        boolean isFacebookUser(in int peopleid);
 
}
