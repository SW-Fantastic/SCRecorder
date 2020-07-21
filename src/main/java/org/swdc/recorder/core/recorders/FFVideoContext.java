package org.swdc.recorder.core.recorders;

import lombok.Getter;
import org.bytedeco.javacpp.*;
import org.swdc.recorder.core.ffmpeg.*;

import java.io.File;
import java.nio.DoubleBuffer;
import java.util.function.Consumer;

public class FFVideoContext {

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

    private RecorderConfig config;


    public FFVideoContext(File file, String deviceFormat, String deviceAddr,String pixFormat) {
        this.file = file;
        this.deviceFormat = deviceFormat;
        this.deviceAddr = deviceAddr;
        this.defaultFormatPix = pixFormat;
    }

    public void onCodecSetup(RecorderConfig config) {
        this.config = config;
    }

    public boolean initializeContextForOutput() {
        int rst = 0;
        try {
            FFInputContext inputContext = new FFInputContext();
            inputContext = inputContext.openWithDevice(deviceFormat,deviceAddr,defaultFormatPix);
            if (inputContext == null) {
                rst = -1;
                return false;
            }

            inputCtx = inputContext.getInputCtx();
            videoSteam = inputContext.getVideoStream();
            if (videoSteam == null) {
                rst = -1;
                return false;
            }
            FFDecodeCodec decodeCodec = new FFDecodeCodec();
            decodeCodec = decodeCodec.openVideoInputDecoder(inputContext,defaultFormatPix);
            if (decodeCodec == null) {
                rst = -1;
                return false;
            }
            decoderCtx = decodeCodec.getDecoderCtx();
            // 打开输出设备（文件）
            FFOutContext ffOutContext = new FFOutContext();
            ffOutContext = ffOutContext.open(file);
            if (ffOutContext == null) {
                rst = -1;
                return false;
            }

            outputCtx = ffOutContext.getOutputCtx();
            FFEncodeCodec encodeCodec = new FFEncodeCodec();
            encodeCodec.configVideoEncoder(config.getWidth(),config.getHeight(),config.getPixFormat(),config.getFrameRate(),config.getBitRate());
            encodeCodec = encodeCodec.openVideoOutputWithConfig(ffOutContext);
            if (encodeCodec == null) {
                rst = -1;
                return false;
            }

            outputStream = ffOutContext.createEncodeStream(encodeCodec);
            encoderCtx = encodeCodec.getCodecContext();

            // 将编码器的数据配置到流
            rst = avcodec.avcodec_parameters_from_context(outputStream.codecpar(),encoderCtx);
            if (rst < 0){
                return false;
            }
            // 写入视频文件数据
            rst = avformat.avformat_write_header(outputCtx,new avutil.AVDictionary());
            if (rst < 0){
                return false;
            }

            FFFrame originFFrame = new FFFrame();
            originFFrame = originFFrame.allocForVideoDecode(decodeCodec);
            if (originFFrame == null) {
                rst = -1;
                return false;
            }

            // 初始化frame和packet
            originalFrame = originFFrame.getFrame();

            FFFrame decorderFFrame = new FFFrame();
            decorderFFrame = decorderFFrame.allocForVideoEncode(encodeCodec);
            if (decorderFFrame == null) {
                rst = -1;
                return false;
            }
            decodedFrame = decorderFFrame.getFrame();
            FFPacket packet = new FFPacket().alloc();
            if (packet == null){
                rst = -1;
                return false;
            }
            videoPacket = packet.getPacket();
            SWSConvertorContext convertorContext = new SWSConvertorContext().open(decodeCodec,encodeCodec);
            if (convertorContext == null) {
                rst = -1;
                return false;
            }
            swsContext = convertorContext.getContext();
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

}
