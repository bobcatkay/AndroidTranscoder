//
// Created by xulin on 2018/6/28 0028.
//

#include <jni.h>
#include <string>
#include "android/log.h"
#include <sstream>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ffmpeg-cmd", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "ffmpeg-cmd", __VA_ARGS__)

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
Java_com_github_transcoder_jni_FFmpegCmd_getProgress(JNIEnv *env, jclass clazz) {
    return get_progress();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_github_transcoder_jni_FFmpegCmd_getSpeed(JNIEnv *env, jclass clazz) {
    return get_speed();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_transcoder_jni_FFmpegCmd_retrieveInfo(JNIEnv *env, jclass clazz, jstring _path) {
    const char* path=env->GetStringUTFChars(_path, JNI_FALSE);
    AVFormatContext* ctx = nullptr;

    av_register_all();
    avcodec_register_all();

    int ret = avformat_open_input(&ctx, path, nullptr, nullptr);
    avformat_find_stream_info(ctx, nullptr);
    env->ReleaseStringUTFChars(_path,path);
    if (ret != 0) {
        LOGE("avformat_open_input() open failed! path:%s, err:%s", path, av_err2str(ret));
        return nullptr;
    }
    int nStreams = ctx->nb_streams;

    AVStream **pStream = ctx->streams;
    AVStream *vStream = nullptr;

    for (int i = 0; i < nStreams; i++) {
        if (pStream[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            vStream = pStream[i];
        }
    }

    int width = 0;
    int height = 0;
    int rotation = 0;
    long fps = 0;
    char *vCodecName = nullptr;
    if(vStream!=nullptr){
        width = vStream->codecpar->width;
        height = vStream->codecpar->height;
        rotation = static_cast<int>(get_rotation(vStream));
        int num = vStream->avg_frame_rate.num;
        int den = vStream->avg_frame_rate.den;
        if (den > 0) {
            fps = lround(num * 1.0 / den);
        }

        const char *codecName = avcodec_get_name(vStream->codecpar->codec_id);
        vCodecName = const_cast<char *>(codecName);
    }

    long bitrate = ctx->bit_rate;
    long duration = ctx->duration / 1000;//ms

    avformat_close_input(&ctx);
    std::ostringstream buffer;
    buffer << "{\"rotation\":"<<rotation<<",\"width\":"<<width<<",\"height\":"<<height<<",\"duration\":"<<duration<<",\"bitrate\":"<< bitrate<<",\"fps\":"<<fps<<R"(,"videoCodec":")"<<vCodecName<<"\"}";
    std::string result = buffer.str();
    return env->NewStringUTF(result.c_str());
}