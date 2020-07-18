package org.swdc.recorder.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.swdc.fx.FXController;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.Listener;
import org.swdc.fx.resource.icons.FontawsomeService;
import org.swdc.recorder.core.AbstractRecorder;
import org.swdc.recorder.core.RecordStatus;
import org.swdc.recorder.core.RecorderService;
import org.swdc.recorder.core.events.RecordStatusChangeEvent;
import org.swdc.recorder.ui.views.RecorderView;
import org.swdc.recorder.ui.views.ribbon.FormatToolView;
import org.swdc.recorder.ui.views.ribbon.RecorderToolView;
import org.swdc.recorder.ui.views.ribbon.RibbonGroup;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class RecorderController extends FXController {

    @Aware
    private RecorderService recorderService = null;

    @Aware
    private FontawsomeService fontawsomeService = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Listener(RecordStatusChangeEvent.class)
    public void onRecordChange(RecordStatusChangeEvent event) {
        RecorderView recorderView = getView();
        Button startRecord = recorderView.findById("record-begin");
        switch (event.getData()) {
            case STOP: {
                if (recorderService.getCurrentRecorder() == null) {
                    break;
                }
                startRecord.setText(fontawsomeService.getFontIcon("play"));
                break;
            }
            case PAUSE: {
                if (recorderService.getCurrentRecorder() == null) {
                    break;
                }
                startRecord.setText(fontawsomeService.getFontIcon("play"));
                break;
            }
            case PROCESSING: {
                if (recorderService.getCurrentRecorder() != null) {
                    startRecord.setText(fontawsomeService.getFontIcon("pause"));
                } else {
                    startRecord.setText(fontawsomeService.getFontIcon("play"));
                }
                break;
            }
        }
    }

    @FXML
    public void startRecord() {
        AbstractRecorder recorder = recorderService.getCurrentRecorder();
        if (recorder== null) {
            RecorderView recorderView = findView(RecorderView.class);
            RecorderToolView recorderToolView = recorderView.getRibbonView(RibbonGroup.RIBBON_RECORD,RecorderToolView.class);
            FormatToolView formatToolView = recorderView.getRibbonView(RibbonGroup.RIBBON_RECORD,FormatToolView.class);

            if (formatToolView.getSelectFormat() == null) {
                return;
            }

            String name = recorderToolView.getFileName();
            if (name.isBlank()) {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
                name = dateFormat.format(date);
                recorderToolView.setFileName(name);
            }

            File file = new File(name + "." + formatToolView.getSelectFormat());
            RecordStatusChangeEvent changeEvent = new RecordStatusChangeEvent(RecordStatus.PROCESSING,this.getView());
            recorderService.record(file);
            this.emit(changeEvent);
        } else if (recorder.isPaused()){
            recorder.resetPause();
            RecordStatusChangeEvent changeEvent = new RecordStatusChangeEvent(RecordStatus.PROCESSING,this.getView());
            this.emit(changeEvent);
        } else if (!recorder.isPaused()){
            RecordStatusChangeEvent changeEvent = new RecordStatusChangeEvent(RecordStatus.PAUSE,this.getView());
            recorder.pause();
            this.emit(changeEvent);
        }

    }

    @FXML
    public void stopRecord() {
        if (recorderService.getCurrentRecorder() == null) {
            return;
        }

        RecordStatusChangeEvent changeEvent = new RecordStatusChangeEvent(RecordStatus.STOP,this.getView());
        recorderService.getCurrentRecorder().stop();
        this.emit(changeEvent);
    }

}
