/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/b058/code/borqsservice/b058sns/FacebookMarket/src/com/msocial/nofree/service/FacebookAPI.aidl
 */
package com.msocial.nofree.service;
public interface FacebookAPI extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.msocial.nofree.service.FacebookAPI
{
private static final java.lang.String DESCRIPTOR = "com.msocial.nofree.service.FacebookAPI";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.msocial.nofree.service.FacebookAPI interface,
 * generating a proxy if needed.
 */
public static com.msocial.nofree.service.FacebookAPI asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.msocial.nofree.service.FacebookAPI))) {
return ((com.msocial.nofree.service.FacebookAPI)iin);
}
return new com.msocial.nofree.service.FacebookAPI.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_addEventToFacebook:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.addEventToFacebook(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_syncFacebookEvent:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _result = this.syncFacebookEvent(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_syncFacebookContact:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _result = this.syncFacebookContact(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isFacebookUser:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.isFacebookUser(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.msocial.nofree.service.FacebookAPI
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public boolean addEventToFacebook(int eid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(eid);
mRemote.transact(Stub.TRANSACTION_addEventToFacebook, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean syncFacebookEvent(boolean forced) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((forced)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_syncFacebookEvent, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean syncFacebookContact(boolean forced) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((forced)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_syncFacebookContact, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean isFacebookUser(int peopleid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(peopleid);
mRemote.transact(Stub.TRANSACTION_isFacebookUser, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_addEventToFacebook = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_syncFacebookEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_syncFacebookContact = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isFacebookUser = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public boolean addEventToFacebook(int eid) throws android.os.RemoteException;
public boolean syncFacebookEvent(boolean forced) throws android.os.RemoteException;
public boolean syncFacebookContact(boolean forced) throws android.os.RemoteException;
public boolean isFacebookUser(int peopleid) throws android.os.RemoteException;
}
