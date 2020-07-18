package org.swdc.recorder.core;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.*;

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

    public void pause() {
        pauseFlag.set(true);
    }

    public boolean isPaused() {
        return pauseFlag.get();
    }

    public void resetPause() {
        pauseFlag.set(false);
    }

    public abstract long getRecordTimes();

    public void setService(RecorderService service) {
        this.service = service;
    }

    public RecorderService getService() {
        return service;
    }

    public Dimension getScreenSize() {
        return screenSize;
    }

    public abstract void record(File file);

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
