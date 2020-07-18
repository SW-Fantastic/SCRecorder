package org.swdc.recorder.ui.views.ribbon;

import javafx.scene.control.ComboBox;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.recorder.core.RecorderService;
import org.swdc.recorder.core.platform.Devices;
import org.swdc.recorder.core.platform.MediaDevice;

import java.util.List;
import java.util.stream.Collectors;

@View(stage = false)
public class InputToolView extends RibbonGroupView {

    @Aware
    private RecorderService recorderService = null;

    @Override
    public void initialize() {
        ComboBox<MediaDevice> audioInput = findById("audioInput");
        if (!recorderService.supportMicroPhone()) {
            audioInput.setDisable(true);
        } else {
            List<MediaDevice> audioDevices = Devices.getAudioDevices().stream()
                    .filter(i -> i.isInput())
                    .collect(Collectors.toList());
            audioInput.getItems().addAll(audioDevices);
            recorderService.disabledOnRecord(audioInput);
        }
    }

    public MediaDevice getSelectAudioDevice() {
        ComboBox<MediaDevice> audioInput = findById("audioInput");
        return audioInput.getSelectionModel().getSelectedItem();
    }

    @Override
    public RibbonGroup getGroup() {
        return RibbonGroup.RIBBON_RECORD;
    }

    @Override
    public int getIndex() {
        return 2;
    }
}
