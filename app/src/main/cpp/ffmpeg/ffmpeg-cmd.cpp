//
// Created by xulin on 2018/6/28 0028.
//

#include <jni.h>
#include <string>
#include "android/log.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ffmpeg-cmd", __VA_ARGS__)

extern "C"{
#include "ffmpeg.h"
#include "libavcodec/jni.h"
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_github_transcoder_jni_FFmpegCmd_run(JNIEnv *env, jclass type, jint cmdLen,
                                             jobjectArray cmd) {
    //set java vm
    JavaVM *jvm = NULL;
    env->GetJavaVM(&jvm);
    av_jni_set_java_vm(jvm, NULL);

    char *argCmd[cmdLen] ;
    jstring buf[cmdLen];

    for (int i = 0; i < cmdLen; ++i) {
        buf[i] = static_cast<jstring>(env->GetObjectArrayElement(cmd, i));
        char *string = const_cast<char *>(env->GetStringUTFChars(buf[i], JNI_FALSE));
        argCmd[i] = string;
        LOGD("argCmd=%s",argCmd[i]);
    }

    int retCode = ffmpeg_exec(cmdLen, argCmd);
    LOGD("ffmpeg-invoke: retCode=%d",retCode);

    return retCode;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_transcoder_jni_FFmpegCmd_getProgress(JNIEnv *env, jclass type) {
    return get_progress();
}