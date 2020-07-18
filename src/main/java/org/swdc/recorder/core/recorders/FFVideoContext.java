package org.swdc.recorder.core.recorders;

import lombok.Getter;
import org.bytedeco.javacpp.*;

import java.io.File;
import java.nio.DoubleBuffer;
import java.util.function.Consumer;

public class FFVideoContext {

    @FunctionalInterface
    public interface ConfigureCtx {
        void onConfigCodec(avcodec.AVCodecContext codecContext, avformat.AVFormatContext input, avformat.AVFormatContext output);
    }

    private File file;
    private String deviceFormat;
    private String deviceAddr;

    @Getter
    private avformat.AVStream videoSteam;

    @Getter
    private avformat.AVStream outputStream;

    @Getter
    private avcodec.AVPacket videoPacket;

    @Getter
    private avutil.AVFrame originalFrame;

    @Getter
    private avutil.AVFrame decodedFrame;

    @Getter
    private swscale.SwsContext swsContext;

    @Getter
    private avformat.AVFormatContext inputCtx;

    @Getter
    private avformat.AVFormatContext outputCtx;

    @Getter
    private avcodec.AVCodecContext encoderCtx;

    @Getter
    private avcodec.AVCodecContext decoderCtx;

    private String defaultFormatPix = "yuv420p";

    private ConfigureCtx configureCtx;


    public FFVideoContext(File file, String deviceFormat, String deviceAddr,String pixFormat) {
        this.file = file;
        this.deviceFormat = deviceFormat;
        this.deviceAddr = deviceAddr;
        this.defaultFormatPix = pixFormat;
    }

    public void onCodecSetup(ConfigureCtx configureCtx) {
        this.configureCtx = configureCtx;
    }

    public boolean initializeContextForOutput() {
        int rst = 0;
        try {
            avformat.AVInputFormat inputFormat = avformat.av_find_input_format(deviceFormat);
            // 申请设备内存
            inputCtx = avformat.avformat_alloc_context();
            // 打开输入设备
            avutil.AVDictionary dictionary = new avutil.AVDictionary();
            avutil.av_dict_set(dictionary,"pixel_format",defaultFormatPix,1);

            rst = avformat.avformat_open_input(inputCtx,deviceAddr,inputFormat,dictionary);
            if (rst < 0) {
                return false;
            }
            // 读取流信息
            rst = avformat.avformat_find_stream_info(inputCtx,new avutil.AVDictionary(null));
            if (rst < 0) {
                return false;
            }
            videoSteam = findVideoStream(inputCtx);
            if (videoSteam == null) {
                rst = -1;
                return false;
            }
            // 获取解码器，用来解码packet包
            avcodec.AVCodec decoderCodec = avcodec.avcodec_find_decoder(videoSteam.codecpar().codec_id());
            if (decoderCodec == null){
                rst = -1;
                return false;
            }

            avutil.AVDictionary decoderOptions = new avutil.AVDictionary();
            avutil.av_dict_set(decoderOptions,"pixel_format",defaultFormatPix,1);

            // 申请解码器内存并且打开解码器
            decoderCtx = avcodec.avcodec_alloc_context3(decoderCodec);
            rst = avcodec.avcodec_open2(decoderCtx,decoderCodec,decoderOptions);
            if (rst < 0){
                return false;
            }
            rst = avcodec.avcodec_parameters_to_context(decoderCtx,videoSteam.codecpar());
            if (rst < 0){
                return false;
            }
            // 打开输出设备（文件）
            outputCtx = new avformat.AVFormatContext();
            rst = avformat.avformat_alloc_output_context2(outputCtx,null,null,file.getAbsolutePath());
            if (rst < 0) {
                // 打开失败
                rst = -1;
                return false;
            }
            // 查找编码器
            avcodec.AVCodec codec = avcodec.avcodec_find_encoder(outputCtx.oformat().video_codec());
            if (codec == null) {
                rst = -1;
                return false;
            }

            // 申请内存并且打开编码器
            encoderCtx = avcodec.avcodec_alloc_context3(codec);
            if (encoderCtx == null) {
                rst = -1;
                return false;
            }

            // 创建流
            outputStream = avformat.avformat_new_stream(outputCtx,codec);
            if (outputStream == null) {
                rst = -1;
                return false;
            }
            configureCtx.onConfigCodec(encoderCtx, inputCtx,outputCtx);
            if (((outputCtx.oformat().flags() > 0 ? 1 : 0) & avformat.AVFMT_GLOBALHEADER) > 0 ) {
                outputCtx.oformat().flags(outputCtx.flags() | avformat.AVFMT_GLOBALHEADER);
            }

            // 打开编码器
            rst = avcodec.avcodec_open2(encoderCtx,codec,new avutil.AVDictionary());
            if (rst < 0) {
                return false;
            }

            // 将编码器的数据配置到流
            rst = avcodec.avcodec_parameters_from_context(outputStream.codecpar(),encoderCtx);
            if (rst < 0){
                return false;
            }
            // 判断是否存在文件，不存在就打开
            if ((encoderCtx.flags() & avformat.AVFMT_NOFILE) == 0 ) {
                avformat.AVIOContext ctxPb = new avformat.AVIOContext(null);
                rst = avformat.avio_open(ctxPb, file.getAbsolutePath(),avformat.AVIO_FLAG_WRITE);
                if (rst < 0){
                    return false;
                }
                outputCtx.pb(ctxPb);
            }

            // 写入视频文件数据
            rst = avformat.avformat_write_header(outputCtx,new avutil.AVDictionary());
            if (rst < 0){
                return false;
            }

            // 初始化frame和packet
            originalFrame = avutil.av_frame_alloc();
            if (originalFrame == null) {
                rst = -1;
                return false;
            }
            originalFrame.format(decoderCtx.pix_fmt());
            originalFrame.width(decoderCtx.width());
            originalFrame.height(decoderCtx.height());

            decodedFrame = avutil.av_frame_alloc();
            if (decodedFrame == null) {
                rst = -1;
                return false;
            }
            decodedFrame.format(encoderCtx.pix_fmt());
            decodedFrame.width(encoderCtx.width());
            decodedFrame.height(encoderCtx.height());

            // 缓存申请
            int size = avutil.av_image_get_buffer_size(decoderCtx.pix_fmt(),decoderCtx.width(),decoderCtx.height(),1);
            rst = avutil.av_image_fill_arrays(originalFrame.data(),originalFrame.linesize(),new BytePointer(avutil.av_malloc(size)),decoderCtx.pix_fmt(),decoderCtx.width(),decoderCtx.height(),1);
            if (rst < 0) {
                return false;
            }

            size = avutil.av_image_get_buffer_size(encoderCtx.pix_fmt(),encoderCtx.width(),encoderCtx.height(),1);
            rst = avutil.av_image_fill_arrays(decodedFrame.data(),decodedFrame.linesize(),new BytePointer(avutil.av_malloc(size)),encoderCtx.pix_fmt(),encoderCtx.width(),encoderCtx.height(),1);
            if (rst < 0) {
                return false;
            }
            // packet初始化
            videoPacket = avcodec.av_packet_alloc();
            avcodec.av_init_packet(videoPacket);
            swsContext = swscale.sws_getContext(decoderCtx.width(),decoderCtx.height(),decoderCtx.pix_fmt(),encoderCtx.width(),encoderCtx.height(),encoderCtx.pix_fmt(),swscale.SWS_BILINEAR,null,null,(DoubleBuffer) null);
            if (swsContext == null) {
                rst = -1;
                return false;
            }
            return true;
        } finally {
            if (rst < 0) {
                closeContext();
            }
        }
    }

    public void closeContext() {
        if (swsContext != null) {
            swscale.sws_freeContext(swsContext);
            swsContext = null;
        }
        if (videoPacket != null) {
            avcodec.av_packet_free(videoPacket);
            videoPacket = null;
        }
        if (decodedFrame != null) {
            avutil.av_frame_free(decodedFrame);
            decodedFrame = null;
        }
        if (originalFrame != null) {
            avutil.av_frame_free(originalFrame);
            originalFrame = null;
        }
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
        if (outputCtx != null && !outputCtx.isNull()) {
            avformat.avformat_flush(outputCtx);
            if (outputCtx.pb() != null && !outputCtx.pb().isNull()) {
                avformat.avio_flush(outputCtx.pb());
                avformat.avio_close(outputCtx.pb());
            }
            avformat.avformat_free_context(outputCtx);
            outputCtx = null;
        }
        if (encoderCtx != null && !encoderCtx.isNull()) {
            avcodec.avcodec_free_context(encoderCtx);
            encoderCtx = null;
        }
        if (inputCtx != null && !inputCtx.isNull()) {
            avformat.avformat_close_input(inputCtx);
            avformat.avformat_free_context(inputCtx);
            inputCtx = null;
        }
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

}
