//
// Created by xulin on 2018/6/28 0028.
//

#include <jni.h>
#include <string>
#include "android/log.h"

extern "C"{
#include "ffmpeg.h"
#include "libavcodec/jni.h"
}

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "ffmpeg-invoke", __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_gamepp_video_jni_FFmpegInvoke_test(JNIEnv *env, jclass type) {

    std::string retValue = "FFmpeg invoke test";
    return env->NewStringUTF(retValue.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_gamepp_video_jni_FFmpegInvoke_run(JNIEnv *env, jclass type, jint cmdLen,
                                                       jobjectArray cmd) {

    //set java vm
    JavaVM *jvm = NULL;
    env->GetJavaVM(&jvm);
    av_jni_set_java_vm(jvm, NULL);
    
    char *argCmd[cmdLen] ;
    jstring buf[cmdLen];
    LOGD("length=%d",cmdLen);

    for (int i = 0; i < cmdLen; ++i) {
        buf[i] = static_cast<jstring>(env->GetObjectArrayElement(cmd, i));
        char *string = const_cast<char *>(env->GetStringUTFChars(buf[i], JNI_FALSE));
        argCmd[i] = string;
        LOGD("argCmd=%s",argCmd[i]);
    }

    LOGD("CPP INVOKE.\n");
    int retCode = ffmpeg_exec(cmdLen, argCmd);
    LOGD("ffmpeg-invoke: retCode=%d",retCode);
  
    return retCode;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_gamepp_video_jni_FFmpegInvoke_getProgress(JNIEnv *env, jclass type) {
    return get_progress();
}