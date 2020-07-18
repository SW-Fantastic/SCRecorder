package org.swdc.recorder.ui.views.ribbon;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.fx.resource.icons.FontawsomeService;
import org.swdc.recorder.core.RecorderService;

@View(stage = false)
public class VirtualDeviceToolView extends RibbonGroupView {

    @Aware
    private FontawsomeService fontawsomeService = null;

    @Override
    public void initialize() {
        setupButton("windows","windows",fontawsomeService);
        Button btnSetupWindowsDevice = findById("windows");
        btnSetupWindowsDevice.setTooltip(new Tooltip("安装或卸载DShow组件。"));
        String name = System.getProperty("os.name").toLowerCase();
        if (!name.contains("windows")) {
            btnSetupWindowsDevice.setDisable(true);
        }
    }

    @Override
    public RibbonGroup getGroup() {
        return RibbonGroup.RIBBON_CONFIG;
    }

    @Override
    public int getIndex() {
        return 0;
    }
}
