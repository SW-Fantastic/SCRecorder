package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.swdc.recorder.core.recorders.FFVideoContext;

public class FFFrame implements AutoCloseable {

    @Getter
    private avutil.AVFrame frame;

    public FFFrame allocForVideoDecode(FFDecodeCodec context) {
        avcodec.AVCodecContext decoderCtx = context.getDecoderCtx();
        frame = avutil.av_frame_alloc();

        if (frame == null) {
            return null;
        }

        frame.width(decoderCtx.width());
        frame.height(decoderCtx.height());
        frame.format(decoderCtx.pix_fmt());

        int size = avutil.av_image_get_buffer_size(decoderCtx.pix_fmt(),decoderCtx.width(),decoderCtx.height(),1);
        int rst = avutil.av_image_fill_arrays(frame.data(),frame.linesize(),new BytePointer(avutil.av_malloc(size)),decoderCtx.pix_fmt(),decoderCtx.width(),decoderCtx.height(),1);
        if (rst < 0) {
            this.close();
            return null;
        }

        return this;
    }

    public FFFrame allocForVideoEncode(FFEncodeCodec encodeCodec) {
        frame = avutil.av_frame_alloc();
        if (frame == null) {
            return null;
        }
        avcodec.AVCodecContext encoderCtx = encodeCodec.getCodecContext();
        frame.format(encoderCtx.pix_fmt());
        frame.width(encoderCtx.width());
        frame.height(encoderCtx.height());
        int size = avutil.av_image_get_buffer_size(encoderCtx.pix_fmt(),encoderCtx.width(),encoderCtx.height(),1);
        int rst = avutil.av_image_fill_arrays(frame.data(),frame.linesize(),new BytePointer(avutil.av_malloc(size)),encoderCtx.pix_fmt(),encoderCtx.width(),encoderCtx.height(),1);
        if (rst < 0) {
            this.close();
            return null;
        }
        return this;
    }

    public FFFrame allocForAudioDecode(FFDecodeCodec codec) {
        this.frame = avutil.av_frame_alloc();
        if (frame == null) {
            this.close();
            return null;
        }
        avcodec.AVCodecContext codecContext = codec.getDecoderCtx();
        frame.sample_rate(codecContext.sample_rate());
        frame.nb_samples(codecContext.frame_size() > 0 ? codecContext.frame_size(): 1024);
        frame.channel_layout(codecContext.channel_layout());
        frame.format(codecContext.sample_fmt());
        int size = avutil.av_samples_get_buffer_size(new IntPointer(),codecContext.channels(),codecContext.frame_size() > 0 ? codecContext.frame_size(): 1024,codecContext.sample_fmt(),0);
        if (size < 0){
            this.close();
            return null;
        }
        int rst = avcodec.avcodec_fill_audio_frame(frame,codecContext.channels(),codecContext.sample_fmt(),new BytePointer(avutil.av_malloc(size)),size,0);
        if (rst < 0) {
            this.close();
            return null;
        }
        return this;
    }

    public FFFrame allocForAudioEncode(FFEncodeCodec codec) {
        this.frame = avutil.av_frame_alloc();
        if (frame == null) {
            this.close();
            return null;
        }
        avcodec.AVCodecContext codecContext = codec.getCodecContext();
        frame.sample_rate(codecContext.sample_rate());
        frame.nb_samples(codecContext.frame_size());
        frame.channel_layout(codecContext.channel_layout());
        frame.format(codecContext.sample_fmt());
        int size = avutil.av_samples_get_buffer_size(frame.linesize(),codecContext.channels(),codecContext.sample_rate(),codecContext.sample_fmt(),0);
        int rst = avcodec.avcodec_fill_audio_frame(frame,codecContext.channels(),codecContext.sample_fmt(),new BytePointer(avutil.av_malloc(size)),size,1);
        if (rst < 0) {
            this.close();
            return null;
        }
        return this;
    }

    @Override
    public void close() {
        if (frame != null) {
            avutil.av_frame_free(frame);
        }
    }
}
