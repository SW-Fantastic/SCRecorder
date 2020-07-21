package org.swdc.recorder.core.recorders;

import lombok.Getter;
import org.bytedeco.javacpp.*;

public class FFAudioContext {

    private avformat.AVFormatContext outputCtx;
    private String audioAddr;
    private String format;

    @Getter
    private avformat.AVInputFormat inputFormat;

    @Getter
    private avformat.AVStream inputSteam;

    @Getter
    private avformat.AVFormatContext inputCtx;

    @Getter
    private avcodec.AVCodecContext decoderCtx;
    @Getter
    private avcodec.AVCodecContext encoderCtx;

    @Getter
    private swresample.SwrContext swrContext;
    @Getter
    private avutil.AVFrame audioDecodeFrame;


    public FFAudioContext(avformat.AVFormatContext outCtx, String format, String addr) {
        this.outputCtx = outCtx;
        this.format = format;
        this.audioAddr = addr;
    }


    public boolean initialize() {
        int rst = 0;
        try {
            inputFormat = avformat.av_find_input_format(format);
            inputCtx = avformat.avformat_alloc_context();
            rst = avformat.avformat_open_input(inputCtx, audioAddr,inputFormat,new avutil.AVDictionary());
            if (rst < 0) {
                return false;
            }
            rst = avformat.avformat_find_stream_info(inputCtx,new avutil.AVDictionary());
            if (rst < 0) {
                return false;
            }
            inputSteam = findAudioStream(inputCtx);
            if (inputSteam == null) {
                rst = -1;
                return false;
            }
            avcodec.AVCodec decoderCodec = avcodec.avcodec_find_decoder(inputSteam.codecpar().codec_id());
            decoderCtx = avcodec.avcodec_alloc_context3(decoderCodec);
            avcodec.avcodec_parameters_to_context(decoderCtx,inputSteam.codecpar());
            rst = avcodec.avcodec_open2(decoderCtx,decoderCodec,new avutil.AVDictionary());
            if (rst < 0) {
                return false;
            }
            avcodec.AVCodec encoderCodec = avcodec.avcodec_find_encoder(avcodec.AV_CODEC_ID_AAC);
            if (encoderCodec == null) {
                rst = -1;
                return false;
            }
            avformat.AVStream outSteam = avformat.avformat_new_stream(outputCtx,encoderCodec);
            if (outSteam == null) {
                rst = -1;
                return false;
            }
            encoderCtx = avcodec.avcodec_alloc_context3(encoderCodec);

            encoderCtx.codec_id(encoderCodec.id());
            encoderCtx.codec_type(avutil.AVMEDIA_TYPE_AUDIO);
            encoderCtx.sample_rate(44100);
            encoderCtx.bit_rate(64000);
            encoderCtx.sample_fmt(avutil.AV_SAMPLE_FMT_FLTP);
            encoderCtx.channel_layout(avutil.AV_CH_LAYOUT_STEREO);
            encoderCtx.channels(avutil.av_get_channel_layout_nb_channels(decoderCtx.channels()));

            // codecEncoderConfiger.onConfigCodec(encoderCtx,inputCtx,outputCtx);
            rst = avcodec.avcodec_open2(encoderCtx,encoderCodec,new avutil.AVDictionary());
            if (rst < 0) {
                return false;
            }
            swrContext = new swresample.SwrContext();
            swrContext = swresample.swr_alloc_set_opts(swrContext,
                    avutil.av_get_default_channel_layout(2),encoderCtx.sample_fmt(),
                    encoderCtx.sample_rate(),avutil.av_get_default_channel_layout(2),
                    decoderCtx.sample_fmt(),decoderCtx.sample_rate(),0,null);
            if (swrContext == null) {
                rst = -1;
                return false;
            }
            rst = swresample.swr_init(swrContext);
            if (rst < 0) {
                return false;
            }
            audioDecodeFrame = avutil.av_frame_alloc();
            audioDecodeFrame.nb_samples(decoderCtx.frame_size());
            audioDecodeFrame.format(decoderCtx.sample_fmt());
            int size = avutil.av_samples_get_buffer_size(new IntPointer(),
                    decoderCtx.channels(),
                    decoderCtx.frame_size(),
                    decoderCtx.sample_fmt(),1);
            Pointer buffer = avutil.av_malloc(size);
            rst = avcodec.avcodec_fill_audio_frame(audioDecodeFrame,decoderCtx.channels(),decoderCtx.sample_fmt(),new BytePointer(buffer),size,1);
            if (rst < 0) {
                return false;
            }
            return true;
        } finally {
            if (rst < 0) {
                this.closeContext();
            }
        }
    }

    public void closeContext() {

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
