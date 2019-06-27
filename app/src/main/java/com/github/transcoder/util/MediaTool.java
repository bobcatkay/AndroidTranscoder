package com.github.transcoder.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MediaTool {
    public static VideoInfo getVideoInfo(String path) {
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(path);
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度 ]
        String duration = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String bitrate = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.width = Integer.valueOf(width);
        videoInfo.height = Integer.valueOf(height);
        videoInfo.bitrate = Integer.valueOf(bitrate);

        videoInfo.duration = Long.valueOf(duration);
        retr.release();
        fillInfo(path, videoInfo);
        return videoInfo;
    }

    public static Bitmap getVideoFrame(String path, long timeUs) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        try {
            media.setDataSource(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return media.getFrameAtTime(timeUs);
    }

    public static void fillInfo(String path, VideoInfo info) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(path);
            int trackCount = extractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    info.fps = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    info.videoCodec = mime;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            extractor.release();
        }

    }

    public static void insertMedia(Context context, String filePath) {
        if (!checkFile(filePath))
            return;
        //保存图片后发送广播通知更新数据库
        Uri uri = Uri.fromFile(new File(filePath));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    private static boolean checkFile(String filePath) {
        File file = new File(filePath);
        boolean result = file.exists();
        if (!result) {
            Log.e("MediaTool", "文件不存在 path = " + filePath);
        }
        return result;
    }

    public static String parseTime(int seconds) {
        String format = "mm:ss";

        if (seconds >= 600 && seconds < 3600) {
            //>=10min
            format = "mm:ss";
        } else if (seconds >= 3600 && seconds < 36000) {
            //>=1h
            format = "H:mm:ss";
        } else if (seconds >= 36000) {
            //>=10:00:00
            format = "HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return sdf.format(new Date(seconds * 1000));
    }

    public static class VideoInfo {
        public int width;
        public int height;
        public long duration;
        public int bitrate;
        public int fps;
        public String videoCodec;
    }
}
