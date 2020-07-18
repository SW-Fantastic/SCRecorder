package org.swdc.recorder.ui.views.ribbon;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.recorder.core.RecorderService;

@View(stage = false)
public class AVToolView extends RibbonGroupView {

    @Aware
    private RecorderService recorderService = null;

    @Override
    public void initialize() {
        TextField txtFrameRate = findById("framerate");
        recorderService.disabledOnRecord(txtFrameRate);
        ComboBox<String> pixFormat = findById("pixFormat");
        recorderService.disabledOnRecord(pixFormat);
        pixFormat.getItems().addAll("yuv420p","yuvj420p","uyvy422");
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("windows")) {
            pixFormat.getSelectionModel().select("yuv420p");
        } else if (name.contains("mac")) {
            pixFormat.getSelectionModel().select("uyvy422");
        }
        txtFrameRate.setText("15");
    }

    public String getPixFmt() {
        ComboBox<String> pixFormat = findById("pixFormat");
        return pixFormat.getSelectionModel().getSelectedItem();
    }

    public Integer getFramerate() {
        TextField txtFrameRate = findById("framerate");
        if (txtFrameRate.getText().isBlank()) {
            return 15;
        }
        return Integer.parseInt(txtFrameRate.getText());
    }

    @Override
    public RibbonGroup getGroup() {
        return RibbonGroup.RIBBON_AV_CONF;
    }

    @Override
    public int getIndex() {
        return 0;
    }
}
