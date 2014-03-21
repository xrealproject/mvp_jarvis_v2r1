#include <jni.h>
#include <string.h>
#include <include/com_example_testvideo_MainActivity.h>
//#define DEBUG_TAG "NDK_Testvideo"
extern "C"
{
JNIEXPORT jstring JNICALL Java_com_example_testvideo_MainActivity_helloLog(JNIEnv *env,jobject thisObj)
{
	   return env->NewStringUTF("Hello from native code!");
	}
}

/*
void Java_com_mamlambo_sample_ndk1_AndroidNDK1SampleActivity_helloLog(JNIEnv * env, jobject this, jstring logThis)
{
    jboolean isCopy;
    const char * szLogThis = (*env)->GetStringUTFChars(env, logThis, &isCopy);
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);
    (*env)->ReleaseStringUTFChars(env, logThis, szLogThis);
}



JNIEXPORT jstring JNICALL Java_com_mytest_JNIActivity_getMessage
          (JNIEnv *env, jobject thisObj) {
   return (*env)->NewStringUTF(env, "Hello from native code!");
}

*/
