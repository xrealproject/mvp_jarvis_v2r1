LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := testvideo
LOCAL_SRC_FILES := TestVideo.cpp

include $(BUILD_SHARED_LIBRARY)

  