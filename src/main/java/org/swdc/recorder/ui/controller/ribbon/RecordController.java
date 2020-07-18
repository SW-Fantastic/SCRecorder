package org.swdc.recorder.ui.controller.ribbon;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.swdc.fx.FXController;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.Listener;
import org.swdc.fx.resource.icons.FontawsomeService;
import org.swdc.recorder.core.AbstractRecorder;
import org.swdc.recorder.core.RecordStatus;
import org.swdc.recorder.core.RecorderService;
import org.swdc.recorder.core.events.RecordStatusChangeEvent;
import org.swdc.recorder.ui.RecUtils;
import org.swdc.recorder.ui.views.RecorderView;
import org.swdc.recorder.ui.views.ribbon.FormatToolView;
import org.swdc.recorder.ui.views.ribbon.RecorderToolView;
import org.swdc.recorder.ui.views.ribbon.RibbonGroup;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RecordController extends FXController {

    @Aware
    private RecorderService recorderService = null;

    @Aware
    private FontawsomeService fontawsomeService = null;

    @FXML
    private TextField txtFileName;

    @FXML
    private Label lblTimer;

    @FXML
    private Button recordBtn;

    private AnimationTimer timer;

    @Listener(RecordStatusChangeEvent.class)
    public void onRecordChange(RecordStatusChangeEvent event) {
        RecorderToolView toolView = getView();
        switch (event.getData()) {
            case STOP: {
                if (recorderService.getCurrentRecorder() == null) {
                    break;
                }
                toolView.setupButton("record","play",fontawsomeService);
                recordBtn.setText("录制");
                break;
            }
            case PAUSE: {
                if (recorderService.getCurrentRecorder() == null) {
                    break;
                }
                toolView.setupButton("record","play",fontawsomeService);
                recordBtn.setText("继续");
                break;
            }
            case PROCESSING: {
                if (recorderService.getCurrentRecorder() != null) {
                    toolView.setupButton("record","pause",fontawsomeService);
                    recordBtn.setText("暂停");
                } else {
                    toolView.setupButton("record","play",fontawsomeService);
                    recordBtn.setText("录制");
                }
                break;
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                AbstractRecorder recorder = recorderService.getCurrentRecorder();
                if (recorder == null) {
                    lblTimer.setText("00:00");
                    return;
                }
                if (recorder.getRecordTimes() > 0) {
                    lblTimer.setText(RecUtils.convertSecondsToString(recorder.getRecordTimes()));
                } else {
                    lblTimer.setText("00:00");
                }
            }
        };
        timer.start();
    }

    @FXML
    public void onRecord() {
        if (recorderService.getCurrentRecorder() == null) {
            RecorderView recorderView = findView(RecorderView.class);
            FormatToolView formatToolView = recorderView.getRibbonView(RibbonGroup.RIBBON_RECORD,FormatToolView.class);
            if (txtFileName.getText().isBlank()) {
                return;
            }
            if (formatToolView.getSelectFormat() == null) {
                return;
            }
            File file = new File(txtFileName.getText() + "." + formatToolView.getSelectFormat());
            recorderService.record(file);
            RecordStatusChangeEvent event = new RecordStatusChangeEvent(RecordStatus.PROCESSING,this.getView());
            this.emit(event);
        } else if (recorderService.getCurrentRecorder().isPaused()){
            recorderService.getCurrentRecorder().resetPause();
            RecordStatusChangeEvent event = new RecordStatusChangeEvent(RecordStatus.PROCESSING,this.getView());
            this.emit(event);
        } else if (!recorderService.getCurrentRecorder().isPaused()) {
            recorderService.getCurrentRecorder().pause();
            RecordStatusChangeEvent event = new RecordStatusChangeEvent(RecordStatus.PAUSE,this.getView());
            this.emit(event);
        }

    }

    @Override
    public void destroy() {
        timer.stop();
    }

    @FXML
    public void onStop() {
        if (recorderService.getCurrentRecorder() == null) {
            return;
        }
        recorderService.getCurrentRecorder().stop();
        RecordStatusChangeEvent changeEvent = new RecordStatusChangeEvent(RecordStatus.STOP,this.getView());
        this.emit(changeEvent);
    }

}
