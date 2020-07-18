package org.swdc.recorder;

import org.swdc.fx.FXApplication;
import org.swdc.fx.FXSplash;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.recorder.config.APPConfigure;
import org.swdc.recorder.ui.views.RecorderView;
import org.swdc.recorder.ui.views.ribbon.GroupViewManager;

@SFXApplication(splash = FXSplash.class, mainView = RecorderView.class,singleton = true)
public class RecorderApplication extends FXApplication {

    public static void main(String[] args) {
       launch(RecorderApplication.class,args);
    }

    @Override
    protected void onLaunch(ConfigManager configManager) {
        configManager.register(APPConfigure.class);
    }

    @Override
    protected void onStart(ApplicationContainer container) {
        container.register(GroupViewManager.class);
    }
}
