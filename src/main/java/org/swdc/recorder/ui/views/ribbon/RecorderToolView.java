package org.swdc.recorder.ui.views.ribbon;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.fx.resource.icons.FontawsomeService;
import org.swdc.recorder.core.RecorderService;

@View(stage = false)
public class RecorderToolView extends RibbonGroupView {

    @Aware
    private FontawsomeService fontawsomeService = null;

    @Aware
    private RecorderService service = null;

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public RibbonGroup getGroup() {
        return RibbonGroup.RIBBON_RECORD;
    }

    @Override
    public void initialize() {
        setupButton("record","play",fontawsomeService);
        setupButton("stop","stop",fontawsomeService);
        TextField txtFileName = findById("txtFileName");
        service.disabledOnRecord(txtFileName);

        Button btnRecord = findById("record");

        Button recordBtn = findById("stop");
        recordBtn.setDisable(true);
        service.disabledOnNotRecord(recordBtn);

        service.onRecorderStateChange(isRecording -> {
            if (isRecording) {
                setupButton("record","pause",fontawsomeService);
                btnRecord.setText("暂停");
            } else {
                setupButton("record","play",fontawsomeService);
                btnRecord.setText("录制");
            }
        });
    }

    public void setFileName(String name) {
        TextField txtFileName = findById("txtFileName");
        txtFileName.setText(name);
    }

    public String getFileName() {
        TextField txtFileName = findById("txtFileName");
        return txtFileName.getText();
    }

}
