package com.github.transcoder.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Gutil {
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

    public static String bitrateFormat(int bitrate){
        DecimalFormat df = new DecimalFormat("#.00");
        String result;
        if ( bitrate < 1024 * 1024) {
            result = df.format(bitrate / 1024f) + " Kbps";
        } else {
            result = df.format(bitrate / 1024 / 1024f) + " Mbps";
        }
        return result;
    }
}
