package org.swdc.recorder.core.recorders;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.swscale;
import org.swdc.recorder.core.AbstractRecorder;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class FoundationRecorder extends AbstractRecorder {

    private AtomicBoolean flag = new AtomicBoolean();

    private AtomicBoolean pauseFlag = new AtomicBoolean(false);

    private long currentTime;

    @Override
    public void pause() {
        pauseFlag.set(true);
    }

    @Override
    public void resetPause() {
        this.pauseFlag.set(false);
    }

    @Override
    public long getRecordTimes() {
        return currentTime;
    }

    @Override
    public boolean isPaused() {
        return pauseFlag.get();
    }

    @Override
    public void record(File file) {
        FFVideoContext videoContext = new FFVideoContext(file,"avfoundation", "Capture screen 0",this.pixFormat);
        videoContext.onCodecSetup(((codecContext, input, output) -> {
            // 配置编码器
            codecContext.flags(avcodec.AV_CODEC_FLAG_QSCALE);
            codecContext.bit_rate(getBitRate());
            codecContext.bit_rate_tolerance(getBitRate());
            codecContext.time_base().den(getFrameRate());
            codecContext.time_base().num(1);
            codecContext.width(getWidth() <= 0 ? (int)getScreenSize().getWidth() : getWidth());
            codecContext.height(getHeight() <=0 ?(int)getScreenSize().getHeight(): getHeight());
            codecContext.gop_size(12);
            codecContext.max_b_frames(0);
            codecContext.thread_count(4);
            codecContext.codec_id(output.oformat().video_codec());
            codecContext.pix_fmt(avutil.AV_PIX_FMT_YUV420P);
            codecContext.codec_type(avutil.AVMEDIA_TYPE_VIDEO);

            switch (output.oformat().video_codec()) {
                case avcodec.AV_CODEC_ID_H264:
                    avutil.av_opt_set(codecContext.priv_data(),"b-pyramid", "none",0);
                    avutil.av_opt_set(codecContext.priv_data(),"preset", "superfast",0);
                    avutil.av_opt_set(codecContext.priv_data(),"tune", "zerolatency",0);
                    break;
            }

        }));
        if(videoContext.initializeContextForOutput()) {

            getService().disableUI(true);
            long lastRecordTime = System.currentTimeMillis();

            avcodec.AVPacket packet = videoContext.getVideoPacket();
            avutil.AVFrame frame = videoContext.getOriginalFrame();
            avutil.AVFrame decodedFrame = videoContext.getDecodedFrame();
            avformat.AVStream videoSteam = videoContext.getVideoSteam();
            avcodec.AVCodecContext decoderContext = videoContext.getDecoderCtx();
            avcodec.AVCodecContext codecContext = videoContext.getEncoderCtx();
            swscale.SwsContext swsContext = videoContext.getSwsContext();
            avformat.AVFormatContext outContext = videoContext.getOutputCtx();
            avformat.AVStream stream = videoContext.getOutputStream();

            flag.set(true);
            int frameNum = 1;
            while (flag.get()) {

                if (pauseFlag.get()) {
                    Thread.yield();
                    continue;
                }

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
                currentTime = currentTime + System.currentTimeMillis() - lastRecordTime;
                lastRecordTime = System.currentTimeMillis();
            }
            avformat.av_write_trailer(outContext);
            videoContext.closeContext();
            lastRecordTime = 0;
            currentTime = 0;
        }
        getService().disableUI(false);

    }

    @Override
    public void stop() {
        flag.set(false);
    }

}
