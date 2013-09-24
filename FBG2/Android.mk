LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := user development


LOCAL_STATIC_JAVA_LIBRARIES := admob_g2

LOCAL_SRC_FILES := $(call all-java-files-under, src)
    
	
LOCAL_PACKAGE_NAME := NewsFeedG2 

include $(BUILD_PACKAGE)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := admob_g2:admob-sdk-android.jar
include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
