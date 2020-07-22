package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.swresample;

public class SWRConvertorContext implements AutoCloseable{

    @Getter
    private swresample.SwrContext context;

    public SWRConvertorContext open(FFEncodeCodec encodeCodec, FFDecodeCodec decodeCodec) {
        context = swresample.swr_alloc();
        if (context == null){
            return null;
        }
        avcodec.AVCodecContext encoderCtx = encodeCodec.getCodecContext();
        avcodec.AVCodecContext decoderCtx = decodeCodec.getDecoderCtx();
        swresample.SwrContext ctx = swresample.swr_alloc_set_opts(context,encoderCtx.channel_layout(),
                encoderCtx.sample_fmt(),encoderCtx.sample_rate(),
                decoderCtx.channel_layout() > 0 ? decoderCtx.channel_layout() : avutil.av_get_default_channel_layout(decoderCtx.channels()),
                decoderCtx.sample_fmt(),
                decoderCtx.sample_rate(),0,new Pointer());
        if (ctx == null) {
            this.close();
            return null;
        }
        context = ctx;
        int rst = swresample.swr_init(context);
        if (rst < 0){
            this.close();
            return null;
        }
        return this;
    }

    @Override
    public void close() {
        if (context != null){
            swresample.swr_close(context);
            swresample.swr_free(context);
        }
    }

}
