#include <jni.h>

//JNIEXPORT jstring JNICALL
//Java_com_cmtsbsnl_cnmc_MainActivity_getBaseApi(JNIEnv *env, jobject instance) {
// return (*env)->NewStringUTF(env, "TmF0aXZlNWVjcmV0UEBzc3cwcmQy");
//}

JNIEXPORT jstring JNICALL
Java_com_cmtsbsnl_cnmc_Login_getBaseApi(JNIEnv *env, jclass instance) {
 return (*env)->NewStringUTF(env, "TmF0aXZlNWVjcmV0UEBzc3cwcmQy");
}

JNIEXPORT jstring JNICALL
Java_com_cmtsbsnl_cnmc_Constants_getBaseURL(JNIEnv *env, jclass instance) {
 return (*env)->NewStringUTF(env, "NjEuMC4yMzQuMi9jbm1jL3Yx");
}

JNIEXPORT jstring JNICALL
Java_com_cmtsbsnl_cnmc_Constants_getAPIUsername(JNIEnv *env, jclass instance) {
    // TODO: implement getAPIUsername()
 return (*env)->NewStringUTF(env, "cnmc");
}

JNIEXPORT jstring JNICALL
Java_com_cmtsbsnl_cnmc_Constants_getAPIPassword(JNIEnv *env, jclass instance) {
 // TODO: implement getAPIPassword()
 return (*env)->NewStringUTF(env, "cnmcmob@1234");
}

JNIEXPORT jstring JNICALL
Java_com_cmtsbsnl_cnmc_Constants_getAuth(JNIEnv *env, jclass instance) {
 // TODO: implement getAPIPassword()
 return (*env)->NewStringUTF(env, "cnmcmobktk@1234$#");
}


