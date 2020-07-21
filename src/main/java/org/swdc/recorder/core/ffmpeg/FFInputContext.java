package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;

public class FFInputContext implements AutoCloseable {

    @Getter
    private avformat.AVFormatContext inputCtx;

    @Getter
    private avformat.AVStream videoStream;

    @Getter
    private avformat.AVStream audioStream;

    public FFInputContext openWithDevice(String format, String address,String pixFormat) {
        avformat.AVInputFormat inputFormat = avformat.av_find_input_format(format);
        // 申请设备内存
        inputCtx = avformat.avformat_alloc_context();
        if (inputFormat == null || inputCtx == null || inputCtx.isNull()) {
            this.close();
            return null;
        }
        // 打开输入设备
        avutil.AVDictionary dictionary = new avutil.AVDictionary();
        avutil.av_dict_set(dictionary,"pixel_format",pixFormat,1);
        int rst = avformat.avformat_open_input(inputCtx,address,inputFormat,dictionary);
        if (rst < 0) {
            this.close();
            return null;
        }
        // 读取流信息
        rst = avformat.avformat_find_stream_info(inputCtx,new avutil.AVDictionary(null));
        if (rst < 0) {
            this.close();
            return null;
        }
        this.audioStream = findAudioStream(inputCtx);
        this.videoStream = findVideoStream(inputCtx);
        return this;
    }

    protected avformat.AVStream findVideoStream(avformat.AVFormatContext context) {
        if (context == null || context.isNull()) {
            return null;
        }
        avformat.AVStream videoSteam = null;
        for (int idx = 0; idx < context.nb_streams(); idx ++) {
            if (context.streams(idx).codecpar().codec_type() == avutil.AVMEDIA_TYPE_VIDEO) {
                videoSteam = context.streams(idx);
            }
        }
        return videoSteam;
    }

    protected avformat.AVStream findAudioStream(avformat.AVFormatContext context) {
        if (context == null || context.isNull()) {
            return null;
        }
        avformat.AVStream audioStream = null;
        for (int idx = 0; idx < context.nb_streams(); idx ++) {
            if (context.streams(idx).codecpar().codec_type() == avutil.AVMEDIA_TYPE_AUDIO) {
                audioStream = context.streams(idx);
            }
        }
        return audioStream;
    }

    @Override
    public void close() {
        if (this.inputCtx == null) {
            return;
        }
        audioStream = null;
        videoStream = null;
        avformat.avformat_close_input(inputCtx);
        avformat.avformat_free_context(inputCtx);
        inputCtx = null;
    }
}
