package org.swdc.recorder.test;

import org.bytedeco.javacpp.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.swdc.recorder.core.AbstractRecorder;
import org.swdc.recorder.core.ffmpeg.*;
import org.swdc.recorder.core.recorders.FFVideoContext;
import org.swdc.recorder.core.recorders.RecorderConfig;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 原理测试部分，
 * 测试音频和视频的录制。
 */
public class RecorderVideoTest {

    private AtomicBoolean recordingFlag = new AtomicBoolean();

    @Test
    public void testVideoRecorder() {
        AbstractRecorder.initFFMpeg();
        FFVideoContext videoContext = new FFVideoContext("gdigrab","desktop","yuv420p");
        videoContext.videoCodecSetup(RecorderConfig.builder()
                .width(1024)
                .height(768)
                .frameRate(15)
                .pixFormat("yuv420p")
                .bitRate(4000000)
                .build());
        Assertions.assertTrue(videoContext.initializeContextForOutput(new FFOutContext().open(new File("./test-created.mp4"))));

        avcodec.AVPacket packet = videoContext.getVideoPacket();
        avutil.AVFrame frame = videoContext.getOriginalFrame();
        avutil.AVFrame decodedFrame = videoContext.getDecodedFrame();
        avformat.AVStream videoSteam = videoContext.getVideoSteam();
        avcodec.AVCodecContext decoderContext = videoContext.getDecoderCtx();
        avcodec.AVCodecContext codecContext = videoContext.getEncoderCtx();
        swscale.SwsContext swsContext = videoContext.getSwsContext();
        avformat.AVFormatContext outContext = videoContext.getOutputCtx();
        avformat.AVStream stream = videoContext.getOutputStream();

        recordingFlag.set(true);
        int frameNum = 1;
        while (recordingFlag.get()) {

            int rs = avformat.av_read_frame(videoContext.getInputCtx(),videoContext.getVideoPacket());
            if (rs < 0) {
                continue;
            }
            packet.pts(avutil.av_rescale_q_rnd(packet.pts(), videoSteam.time_base(), decoderContext.time_base(),avutil.AV_ROUND_NEAR_INF | avutil.AV_ROUND_PASS_MINMAX));
            if (avcodec.avcodec_send_packet(decoderContext,packet) < 0) {
                continue;
            }
            if(avcodec.avcodec_receive_frame(decoderContext,frame) < 0) {
                continue;
            }
            swscale.sws_scale(swsContext,frame.data(),frame.linesize(),0,codecContext.height(),decodedFrame.data(),decodedFrame.linesize());

            rs = avcodec.avcodec_send_frame(codecContext,decodedFrame);
            if (rs < 0) {
                continue;
            }
            rs = avcodec.avcodec_receive_packet(codecContext,packet);
            if (rs < 0){
                continue;
            }
            packet.dts(avutil.av_rescale_q_rnd(frameNum,codecContext.time_base(),stream.time_base(),avutil.AV_ROUND_NEAR_INF | avutil.AV_ROUND_PASS_MINMAX));
            rs = avformat.av_interleaved_write_frame(outContext,packet);
            if (rs < 0) {
                continue;
            }
            frameNum ++;
            if (frameNum > 1200) {
                recordingFlag.set(false);
            }
        }
        avformat.av_write_trailer(outContext);
        videoContext.closeContext();
    }

    /**
     * DTS 和 PTS不正确。
     */
    @Test
    public void testRecorderWithSystemAudio() {
        AbstractRecorder.initFFMpeg();

        FFInputContext inputContext = new FFInputContext().openAudioInputDevice("dshow","audio=virtual-audio-capturer");

        Assertions.assertNotNull(inputContext);
        Assertions.assertNotNull(inputContext.getAudioStream());

        FFDecodeCodec decodeCodec = new FFDecodeCodec().openAudioInputCodec(inputContext);
        Assertions.assertNotNull(decodeCodec);

        FFOutContext outContext = new FFOutContext().open(new File("test-creation.mp3"));

        Assertions.assertNotNull(outContext);

        avcodec.AVCodecContext decoderCtx = decodeCodec.getDecoderCtx();
        FFEncodeCodec encodeCodec = new FFEncodeCodec();
        encodeCodec.configAudioEncoder(decoderCtx.channels(),decoderCtx.sample_rate(),avutil.AV_SAMPLE_FMT_FLTP);
        encodeCodec = encodeCodec.openAudioOutputWithConfig(outContext);

        Assertions.assertNotNull(encodeCodec);

        avformat.AVStream stream = outContext.createEncodeStream(encodeCodec);
        avcodec.avcodec_parameters_from_context(stream.codecpar(),encodeCodec.getCodecContext());
        Assertions.assertNotNull(stream);

        int rst = avformat.avformat_write_header(outContext.getOutputCtx(),new avutil.AVDictionary());
        Assertions.assertFalse(rst < 0);
        FFFrame originFFrame = new FFFrame().allocForAudioDecode(decodeCodec);
        Assertions.assertNotNull(originFFrame);

        SWRConvertorContext convertorContext = new SWRConvertorContext().open(encodeCodec,decodeCodec);
        Assertions.assertNotNull(convertorContext);

        FFPacket fpacket = new FFPacket().alloc();
        Assertions.assertNotNull(fpacket);

        recordingFlag.set(true);
        int frameNum = 1;

        avcodec.AVPacket packet = fpacket.getPacket();
        avutil.AVFrame frame = originFFrame.getFrame();
        FFFrame decodedFrame = new FFFrame().allocForAudioEncode(encodeCodec);

        avutil.AVFrame decodeFrame = decodedFrame.getFrame();

        avcodec.AVCodecContext codecContext = encodeCodec.getCodecContext();

        while (recordingFlag.get()) {
            int rs = avformat.av_read_frame(inputContext.getInputCtx(),fpacket.getPacket());
            if (rs < 0) {
                continue;
            }
            packet.pts(avutil.av_rescale_q_rnd(packet.pts(), inputContext.getAudioStream().time_base(), decoderCtx.time_base(),avutil.AV_ROUND_NEAR_INF | avutil.AV_ROUND_PASS_MINMAX));
            if (avcodec.avcodec_send_packet(decoderCtx,packet) < 0) {
                continue;
            }
            if(avcodec.avcodec_receive_frame(decoderCtx,frame) < 0) {
                continue;
            }

            swresample.swr_convert(convertorContext.getContext(),decodeFrame.data(),decodeFrame.nb_samples(),frame.data(),frame.nb_samples());
            //swscale.sws_scale(swsContext,frame.data(),frame.linesize(),0,codecContext.height(),decodedFrame.data(),decodedFrame.linesize());

            rs = avcodec.avcodec_send_frame(codecContext,decodeFrame);
            if (rs < 0) {
                continue;
            }
            rs = avcodec.avcodec_receive_packet(codecContext,packet);
            if (rs < 0){
                continue;
            }
            packet.dts(avutil.av_rescale_q_rnd(frameNum,codecContext.time_base(),stream.time_base(),avutil.AV_ROUND_NEAR_INF | avutil.AV_ROUND_PASS_MINMAX));
            rs = avformat.av_interleaved_write_frame(outContext.getOutputCtx(),packet);
            if (rs < 0) {
                continue;
            }
            frameNum ++;
            if (frameNum > 1600) {
                recordingFlag.set(false);
            }
        }
        avformat.av_write_trailer(outContext.getOutputCtx());
    }

}
