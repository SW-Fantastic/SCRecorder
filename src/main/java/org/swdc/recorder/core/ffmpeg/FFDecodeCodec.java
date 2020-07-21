package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;

public class FFDecodeCodec implements AutoCloseable {

    @Getter
    private avcodec.AVCodecContext decoderCtx;

    public FFDecodeCodec openVideoInputDecoder(FFInputContext context,String pixFormat) {
        avcodec.AVCodec codec = avcodec.avcodec_find_decoder(context.getVideoStream().codecpar().codec_id());
        if (codec == null){
            return null;
        }
        decoderCtx = avcodec.avcodec_alloc_context3(codec);

        avutil.AVDictionary decoderOptions = new avutil.AVDictionary();
        avutil.av_dict_set(decoderOptions,"pixel_format",pixFormat,1);
        int rst = avcodec.avcodec_open2(decoderCtx,codec,decoderOptions);
        if (rst < 0) {
            this.close();
            return null;
        }
        avformat.AVStream stream = context.getVideoStream();
        if (stream == null) {
            this.close();
            return null;
        }
        rst = avcodec.avcodec_parameters_to_context(decoderCtx,stream.codecpar());
        if (rst < 0) {
            this.close();
            return null;
        }
        return this;
    }

    @Override
    public void close() {
        if (decoderCtx == null || decoderCtx.isNull()) {
            return;
        }
        avcodec.avcodec_close(decoderCtx);
        avcodec.avcodec_free_context(decoderCtx);
        decoderCtx = null;
    }
}
