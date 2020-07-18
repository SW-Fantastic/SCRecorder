package org.swdc.recorder.ui.views.ribbon;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.recorder.core.RecorderService;

@View(stage = false)
public class FormatToolView extends RibbonGroupView {

    @Aware
    private RecorderService recorderService = null;

    @Override
    public int getIndex() {
        return 1;
    }

    @Override
    public RibbonGroup getGroup() {
        return RibbonGroup.RIBBON_RECORD;
    }

    @Override
    public void initialize() {
        RecorderService service = findService(RecorderService.class);

        CheckBox recordSystemVol = findById("systemVol");
        CheckBox recordMicroPhone = findById("microPhone");
        ComboBox<String> cbxFormat = findById("cbxformat");

        cbxFormat.getItems().addAll("mp4","flv","mov","avi");

        recorderService.disabledOnRecord(cbxFormat);

        if (!service.supportMicroPhone()) {
            recordMicroPhone.setDisable(true);
        } else {
            recorderService.disabledOnRecord(recordMicroPhone);
        }

        if (!service.supportSystemSounds()){
            recordSystemVol.setDisable(true);
        } else {
            recorderService.disabledOnRecord(recordSystemVol);
        }
    }

    public String getSelectFormat() {
        ComboBox<String> cbxFormat = findById("cbxformat");
        return cbxFormat.getSelectionModel().getSelectedItem();
    }

    public boolean recordMicroPhone() {
        CheckBox recordMicroPhone = findById("microPhone");
        return recordMicroPhone.isSelected();
    }

    public boolean recordSystemSound() {
        CheckBox recordSystemVol = findById("systemVol");
        return recordSystemVol.isSelected();
    }

}
