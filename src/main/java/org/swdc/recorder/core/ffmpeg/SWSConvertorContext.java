package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.swscale;

import java.nio.DoubleBuffer;

public class SWSConvertorContext implements AutoCloseable{

    @Getter
    private swscale.SwsContext context;

    public SWSConvertorContext open(FFDecodeCodec decodeCodec, FFEncodeCodec encodeCodec) {
        avcodec.AVCodecContext encoderCtx = encodeCodec.getCodecContext();
        avcodec.AVCodecContext decoderCtx = decodeCodec.getDecoderCtx();
        context = swscale.sws_getContext(decoderCtx.width(),decoderCtx.height(),decoderCtx.pix_fmt(),encoderCtx.width(),encoderCtx.height(),encoderCtx.pix_fmt(),swscale.SWS_BILINEAR,null,null,(DoubleBuffer) null);
        if (context == null) {
            return null;
        }
        return this;
    }

    public void resolve(FFFrame form,FFFrame dest, FFEncodeCodec encodeCodec, FFDecodeCodec decodeCodec) {
        swscale.sws_scale(context,
                form.getFrame().data(),form.getFrame().linesize(),
                0,encodeCodec.getCodecContext().height(),
                dest.getFrame().data(),dest.getFrame().linesize());
    }

    @Override
    public void close() throws Exception {
        if (context != null) {
            swscale.sws_freeContext(context);
        }
    }

}
