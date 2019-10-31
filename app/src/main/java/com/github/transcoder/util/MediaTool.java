package com.github.transcoder.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MediaTool {

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



}
