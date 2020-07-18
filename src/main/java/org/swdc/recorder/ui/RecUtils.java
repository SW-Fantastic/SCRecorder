package org.swdc.recorder.ui;

import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;

public class RecUtils {

    public static boolean checkDshow() {
        // 检查Screen Capture Recorder
        avformat.AVInputFormat format =
                avformat.av_find_input_format("dshow");
        avformat.AVFormatContext ctx = avformat.avformat_alloc_context();
        int rst = avformat.avformat_open_input(ctx,"video=screen-capture-recorder",format,new avutil.AVDictionary());
        boolean result = rst == 0;
        avformat.avformat_close_input(ctx);
        avformat.avformat_free_context(ctx);
        return result;
    }

    public static String convertSecondsToString(long seconds) {
        seconds = seconds / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h,m,s);
    }

}
