package org.swdc.recorder.core;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.*;
import org.swdc.recorder.core.recorders.FFVideoContext;
import org.swdc.recorder.core.recorders.RecorderConfig;
import org.swdc.recorder.core.recorders.RecorderInfo;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class AbstractRecorder {

    private static boolean isReady = false;

    private RecordStatus status;

    protected AtomicBoolean recordingFlag = new AtomicBoolean(false);

    protected AtomicBoolean pauseFlag = new AtomicBoolean(false);

    protected String pixFormat = "YUV420P";

    public void setStatus(RecordStatus status) {
        switch (status) {
            case PROCESSING:
                if (this.isPaused()) {
                    this.resetPause();
                    this.status = status;
                }
                return;
            case STOP:
                if (this.recordingFlag.get()) {
                    this.stop();
                    this.status = status;
                }
                return;
            case PAUSE:
                if (!this.pauseFlag.get() && this.recordingFlag.get()) {
                    this.pause();
                    this.status = status;
                }
                return;
        }
    }

    public RecordStatus getStatus() {
        if (recordingFlag.get()) {
            if (pauseFlag.get()) {
                return RecordStatus.PAUSE;
            }
            return RecordStatus.PROCESSING;
        }
        return RecordStatus.STOP;
    }

    static {
        initFFMpeg();
    }

    public static void initFFMpeg() {
        if (isReady) {
            return;
        }
        Loader.load(avcodec.class);
        Loader.load(avutil.class);
        Loader.load(avdevice.class);
        Loader.load(avfilter.class);
        Loader.load(avformat.class);
        Loader.load(swscale.class);
        avdevice.avdevice_register_all();
        isReady = true;
    }

    public void setPixFormat(String pixFormat) {
        this.pixFormat = pixFormat;
    }

    public String getPixFormat() {
        return pixFormat;
    }

    private boolean enableAudio;

    private boolean enableVoice;

    private int targetWidth;

    private int targetHeight;

    private int bitRate = 4000000;

    private int frameRate = 15;

    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private RecorderService service;

    private long currentTime;

    public void pause() {
        pauseFlag.set(true);
    }

    public boolean isPaused() {
        return pauseFlag.get();
    }

    public void resetPause() {
        pauseFlag.set(false);
    }

    public long getRecordTimes() {
        return currentTime;
    }

    public void setService(RecorderService service) {
        this.service = service;
    }

    public RecorderService getService() {
        return service;
    }

    public Dimension getScreenSize() {
        return screenSize;
    }

    public abstract RecorderInfo getRecorderInfo();

    public void record(File file){
        RecorderInfo info = getRecorderInfo();
        FFVideoContext videoContext = new FFVideoContext(file,info.getFormat(),info.getAddress(),getPixFormat());
        videoContext.onCodecSetup(RecorderConfig.builder()
                .width(getWidth() <= 0 ? (int)getScreenSize().getWidth() : getWidth())
                .height(getHeight() <=0 ?(int)getScreenSize().getHeight(): getHeight())
                .frameRate(getFrameRate())
                .pixFormat(getPixFormat())
                .bitRate(getBitRate())
                .build());
        if (videoContext.initializeContextForOutput()) {

            long lastRecordTime = System.currentTimeMillis();

            getService().disableUI(true);

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
                currentTime = currentTime + (System.currentTimeMillis() - lastRecordTime);
                lastRecordTime = System.currentTimeMillis();
            }
            avformat.av_write_trailer(outContext);
            videoContext.closeContext();
            lastRecordTime = 0;
            currentTime = 0;
        }
        getService().disableUI(false);
    }

    public void stop() {
        recordingFlag.set(false);
    }

    public void recordSystemAudio(boolean systemAudio) {
        this.enableAudio = systemAudio;
    }

    public void recordVoice(boolean enableVoice) {
        this.enableVoice = enableVoice;
    }

    public void size(int width, int height) {
        targetHeight = height;
        targetWidth = width;
    }

    public int getWidth() {
        return targetWidth;
    }

    public int getHeight() {
        return targetHeight;
    }

    public int getBitRate() {
        return bitRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void frameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public void bitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public boolean recordSystemAudio() {
        return this.enableAudio;
    }

    public boolean recordVoice() {
        return this.enableVoice;
    }

}
