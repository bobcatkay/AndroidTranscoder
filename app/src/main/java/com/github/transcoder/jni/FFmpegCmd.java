package com.github.transcoder.jni;

import android.util.Log;

import java.util.ArrayList;

public class FFmpegCmd {
    static {
        System.loadLibrary("avutil");
        System.loadLibrary("avcodec");
        System.loadLibrary("swresample");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
        System.loadLibrary("ffmpeg-cmd");
    }

    //执行FFmpeg命令
    private static native int run(int cmdLen, String[] cmd);

    //获取命令执行进度
    public static native int getProgress();


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

    /**
     * @param srcPath   视频源路径
     * @param outPath   视频输出路径
     * @param targetFPS 视频输出帧率
     * @param bitrate   输出码率
     * @param duration  视频时长(ms)
     * @param listener
     */
    public static void transcode(String srcPath, String outPath, int targetFPS, int bitrate, int targetWidth, int targetHeight, long duration, String presets, ProgressListener listener) {
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
                    listener.onProgressUpdate(progress);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        transcode(srcPath, outPath, targetFPS, bitrate, targetWidth, targetHeight, presets);
    }

    public static void transcode(String srcPath, String outPath, int targetFPS, int bitrate, int width, int height, String presets) {
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-d");
        cmd.add("-y");
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
        void onProgressUpdate(int progress);
    }
}
