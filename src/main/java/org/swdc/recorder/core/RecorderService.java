package org.swdc.recorder.core;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.swdc.fx.services.Service;
import org.swdc.recorder.core.platform.Devices;
import org.swdc.recorder.core.recorders.DShowRecorder;
import org.swdc.recorder.core.recorders.FoundationRecorder;
import org.swdc.recorder.core.recorders.GDIRecorder;
import org.swdc.recorder.ui.RecUtils;
import org.swdc.recorder.ui.views.RecorderView;
import org.swdc.recorder.ui.views.ribbon.AVToolView;
import org.swdc.recorder.ui.views.ribbon.FormatToolView;
import org.swdc.recorder.ui.views.ribbon.RibbonGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RecorderService extends Service {

    public SimpleBooleanProperty recordingStateHolder = new SimpleBooleanProperty(false);

    private List<Node> disabledStateList = new ArrayList<>();

    private List<Node> disabledStateRevertList = new ArrayList<>();

    private List<Consumer<Boolean>> onRecordStateChange = new ArrayList<>();

    private AbstractRecorder recorder;

    @Override
    public void initialize() {
        recordingStateHolder.addListener((observable, old, t1) -> {
            for (Node node: disabledStateList) {
                node.setDisable(t1 != null ? t1 : old);
            }
            for (Node node: disabledStateRevertList) {
                node.setDisable(t1 != null ? !t1 : !old);
            }
            Platform.runLater(() -> {
                for (Consumer<Boolean> consumer : onRecordStateChange) {
                    consumer.accept(t1 != null ? t1 : old);
                }
            });
        });
    }

    public void onRecorderStateChange(Consumer<Boolean> onChange) {
        this.onRecordStateChange.add(onChange);
    }

    public void disableUI(boolean disabled) {
        recordingStateHolder.setValue(disabled);
    }

    public void disabledOnRecord(Node node) {
        disabledStateList.add(node);
    }

    public void disabledOnNotRecord(Node node) {
        disabledStateRevertList.add(node);
    }

    public boolean supportSystemSounds() {
        AbstractRecorder.initFFMpeg();
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("windows")) {
            avformat.AVInputFormat format = avformat.av_find_input_format("dshow");
            if (format == null) {
                return false;
            }
            avformat.AVFormatContext ctx = avformat.avformat_alloc_context();
            int rst = avformat.avformat_open_input(ctx,"audio=virtual-audio-capturer",format,new avutil.AVDictionary());
            boolean result = rst == 0;
            avformat.avformat_close_input(ctx);
            avformat.avformat_free_context(ctx);
            return result;
        } else if (name.contains("mac")){
            return false;
        } else {
            return false;
        }
    }

    public AbstractRecorder record(File file) {
        if (this.recorder != null) {
            return this.recorder;
        }
        this.recorder = this.getRecorder();
        RecorderView view = findView(RecorderView.class);
        AVToolView toolView = view.getRibbonView(RibbonGroup.RIBBON_AV_CONF, AVToolView.class);
        FormatToolView formatToolView = view.getRibbonView(RibbonGroup.RIBBON_RECORD,FormatToolView.class);
        recorder.frameRate(toolView.getFramerate());
        recorder.setPixFormat(toolView.getPixFmt());
        recorder.recordSystemAudio(formatToolView.recordSystemSound());
        Thread recordThread = new Thread(() ->{
            recorder.record(file);
            recorder = null;
        });
        recordThread.start();
        return this.recorder;
    }

    public boolean supportMicroPhone() {
        long audioSource = Devices.getAudioDevices()
                .stream().filter(i -> i.isInput())
                .count();
        return audioSource > 0;
    }

    public AbstractRecorder getCurrentRecorder() {
        return recorder;
    }

    private AbstractRecorder getRecorder() {
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("windows")) {
            // 检查Screen Capture Recorder
            boolean result = RecUtils.checkDshow();
            if (!result) {
                // 未安装screen-capture-recorder的时候，使用gdigrab
                GDIRecorder recorder = new GDIRecorder();
                recorder.setService(this);
                return recorder;
            } else {
                // 返回dshow的recorder
                DShowRecorder recorder = new DShowRecorder();
                recorder.setService(this);
                return recorder;
            }
        } else if (name.contains("mac")) {
            // 返回avfoundation的recorder
            FoundationRecorder recorder = new FoundationRecorder();
            recorder.setService(this);
            return recorder;
        } else if (name.contains("linux")) {
            // 返回Linux的recorder
        }
        return null;
    }

}
