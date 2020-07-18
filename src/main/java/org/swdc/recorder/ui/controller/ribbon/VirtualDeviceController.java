package org.swdc.recorder.ui.controller.ribbon;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.swdc.fx.FXController;
import org.swdc.fx.deploy.system.NSystem;
import org.swdc.recorder.ui.RecUtils;
import org.swdc.recorder.ui.views.ribbon.VirtualDeviceToolView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VirtualDeviceController extends FXController {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void installDshow() {
        String name = System.getProperty("os.name").toLowerCase();
        if (!name.contains("windows")) {
            return;
        }
        VirtualDeviceToolView virtualDeviceToolView = this.getView();

        boolean result = RecUtils.checkDshow();
        if (result) {
            if (NSystem.is32Bits()) {
                result = NSystem.uninstallModule(new File(getAssetsPath() + "/filters/audio_sniffer.dll"));
                result = result && NSystem.uninstallModule(new File(getAssetsPath() + "/filters/screen-capture-recorder.dll"));
            } else if (NSystem.is64Bits()) {
                result = NSystem.uninstallModule(new File(getAssetsPath() + "/filters/audio_sniffer-x64.dll"));
                result = result && NSystem.uninstallModule(new File(getAssetsPath() + "/filters/screen-capture-recorder-x64.dll"));
            }
            if (!result) {
                virtualDeviceToolView.showAlertDialog("失败","移除虚拟设备需要管理员权限，请以此权限启动然后重试。", Alert.AlertType.ERROR);
                return;
            }
            virtualDeviceToolView.showAlertDialog("成功","DShow组件的注册已经解除。", Alert.AlertType.INFORMATION);
            return;
        }

        if (NSystem.is32Bits()) {
            result = NSystem.installModule(new File(getAssetsPath() + "/filters/audio_sniffer.dll"));
            result = result && NSystem.installModule(new File(getAssetsPath() + "/filters/screen-capture-recorder.dll"));
        } else {
            result = NSystem.installModule(new File(getAssetsPath() + "/filters/audio_sniffer-x64.dll"));
            result = result && NSystem.installModule(new File(getAssetsPath() + "/filters/screen-capture-recorder-x64.dll"));
        }
        if (!result) {
            virtualDeviceToolView.showAlertDialog("失败","注册虚拟设备需要管理员权限，请以此权限启动然后重试。", Alert.AlertType.ERROR);
            return;
        }
        virtualDeviceToolView.showAlertDialog("成功","DShow组件的注册已经添加，下次启动将会生效。", Alert.AlertType.INFORMATION);
    }

}
