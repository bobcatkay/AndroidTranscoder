package com.github.transcoder.jni;

import android.util.Log;

import com.github.transcoder.bean.VideoInfo;
import com.google.gson.Gson;
import java.util.ArrayList;

public class FFmpegCmd {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpeg-cmd");
    }

    //执行FFmpeg命令
    private static native int run(int cmdLen, String[] cmd);

    //获取命令执行进度
    private static native int getProgress();

    //获取转码速率
    private static native double getSpeed();


    /**
     * 执行FFMpeg命令， 同步模式
     *
     * @param cmd
     * @return
     */
    public static int run(ArrayList<String> cmd) {
        String[] cmdArr = new String[cmd.size()];
        Log.d("FFmpegCmd", "run: " + cmd.toString());
        return run(cmd.size(), cmd.toArray(cmdArr));
    }

    public static int run(String[] cmd) {
        return run(cmd.length, cmd);
    }

    private static native String retrieveInfo(String path);

    /**
     * @param srcPath   视频源路径
     * @param outPath   视频输出路径
     * @param targetFPS 视频输出帧率
     * @param bitrate   输出码率
     * @param duration  视频时长(ms)
     * @param listener
     */
    public static void transcode(String srcPath, String outPath, int targetFPS, int bitrate, int targetWidth, int targetHeight, long duration, String presets, VideoInfo info,ProgressListener listener) {
        new Thread(() -> {
            int frame = -1;
            boolean started = false;
            while (frame != 0) {
                int frameTemp = getProgress();
                int progress;
                if (frameTemp > 0) {
                    started = true;
                }
                if (started) {
                    frame = frameTemp;
                    progress = (int) Math.ceil(frame*100/(targetFPS*duration/1000));
                    double speed = getSpeed();
                    long timeRemaining = 0;
                    if (speed>0) {
                        timeRemaining = (long) (duration * (1 - progress / 100.0) / speed);
                    }
                    listener.onProgressUpdate(progress,timeRemaining);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        transcode(srcPath, outPath, targetFPS, bitrate, targetWidth, targetHeight, presets,info);
    }

    public static void transcode(String srcPath, String outPath, int targetFPS, int bitrate, int width, int height, String presets, VideoInfo info) {
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        //cmd.add("-d");
        cmd.add("-y");

        //当rotation为0时使用硬件解码器
        // rotation不为0时使用硬件解码视频画面可能会变绿
        if (info.rotation==0){
            switch (info.videoCodec){
                case "h264":
                    cmd.add("-c:v");
                    cmd.add("h264_mediacodec");
                    break;
                case "hevc":
                    cmd.add("-c:v");
                    cmd.add("hevc_mediacodec");
                    break;
//                case "vp9":
//                    cmd.add("-c:v");
//                    cmd.add("vp9_mediacodec");
//                    break;
            }
        }
        cmd.add("-i");
        cmd.add(srcPath);
        cmd.add("-preset");
        cmd.add(presets);
        cmd.add("-b:v");
        cmd.add(bitrate + "k");
        if (width > 0) {
            cmd.add("-s");
            cmd.add(width + "x" + height);
        }
        cmd.add("-r");
        cmd.add(String.valueOf(targetFPS));
        cmd.add(outPath);
        run(cmd);
    }

    public interface ProgressListener {
        void onProgressUpdate(int progress,long timeRemaining);
    }

    public static VideoInfo getVideoInfo(String path){
        String s = null;
        try {
            s = retrieveInfo(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        VideoInfo info = new VideoInfo();
        if (s!=null){
            info = new Gson().fromJson(s, VideoInfo.class);
        }
        return info;
    }
}
