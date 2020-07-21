package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;

import java.io.Closeable;

public class FFEncodeCodec implements Closeable {

    @Getter
    private avcodec.AVCodecContext codecContext;

    private int width;
    private int height;
    private int colorMode = avutil.AV_PIX_FMT_YUV420P;
    private int frameRate;
    private int bitrate;

    public void configVideoEncoder(int width, int height, int colorMode, int framerate, int bitrate) {
        this.width = width;
        this.height = height;
        this.colorMode = colorMode;
        this.frameRate = framerate;
        this.bitrate = bitrate;
    }

    public void configVideoEncoder(int width, int height, String colorMode, int framerate, int bitrate) {
        this.width = width;
        this.height = height;
        this.colorMode = avutil.av_get_pix_fmt(colorMode);
        this.frameRate = framerate;
        this.bitrate = bitrate;
    }

    public FFEncodeCodec openVideoOutputWithConfig(FFOutContext outContext) {
        avcodec.AVCodec codec = avcodec.avcodec_find_encoder(outContext.getOutputCtx().oformat().video_codec());
        if (codec == null) {
            return null;
        }
        codecContext = avcodec.avcodec_alloc_context3(codec);
        codecContext.flags(avcodec.AV_CODEC_FLAG_QSCALE);
        codecContext.bit_rate(bitrate);
        codecContext.bit_rate_tolerance(bitrate);
        codecContext.time_base().den(frameRate);
        codecContext.time_base().num(1);
        codecContext.width(width);
        codecContext.height(height);
        codecContext.gop_size(12);
        codecContext.max_b_frames(0);
        codecContext.thread_count(4);
        codecContext.codec_id(outContext.getOutputCtx().oformat().video_codec());
        codecContext.pix_fmt(colorMode);
        codecContext.codec_type(avutil.AVMEDIA_TYPE_VIDEO);

        switch (outContext.getOutputCtx().oformat().video_codec()) {
            case avcodec.AV_CODEC_ID_H264:
                avutil.av_opt_set(codecContext.priv_data(),"b-pyramid", "none",0);
                avutil.av_opt_set(codecContext.priv_data(),"preset", "superfast",0);
                avutil.av_opt_set(codecContext.priv_data(),"tune", "zerolatency",0);
                break;
        }

        int rst = avcodec.avcodec_open2(codecContext,codec,new avutil.AVDictionary());
        if (rst < 0) {
            this.close();
            return null;
        }
        if (((outContext.getOutputCtx().oformat().flags() > 0 ? 1 : 0) & avformat.AVFMT_GLOBALHEADER) > 0 ) {
            outContext.getOutputCtx().oformat().flags(outContext.getOutputCtx().flags() | avformat.AVFMT_GLOBALHEADER);
        }
        return this;
   }

    @Override
    public void close() {
        if (codecContext == null) {
            return;
        }
        avcodec.avcodec_close(codecContext);
        avcodec.avcodec_free_context(codecContext);
        codecContext = null;
    }
}
