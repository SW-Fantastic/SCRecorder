module recorder {
    requires java.desktop;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slf4j.api;

    requires lombok;
    requires fx.framework.core;
    requires fx.framework.resource;
    requires fx.framework.deploy;
    // 对ffmpeg进行了重新打包，所以动态库以及依赖等都在这一个里了
    // 包含除android外所有平台。
    requires ffmpeg.all;

    opens org.swdc.recorder.ui.views to
            fx.framework.core,
            javafx.controls,
            javafx.fxml,
            javafx.graphics;

    opens org.swdc.recorder.ui.controller.ribbon to
            fx.framework.core,
            javafx.fxml;

    opens org.swdc.recorder.ui.controller to
            fx.framework.core,
            javafx.fxml;

    opens org.swdc.recorder.config to
            fx.framework.core;

    opens org.swdc.recorder.core to
            fx.framework.core;

    opens org.swdc.recorder.ui.views.ribbon to
            fx.framework.core,
            javafx.fxml;

    opens org.swdc.recorder to
            fx.framework.core,
            javafx.graphics;

    opens views to
            fx.framework.core,
            javafx.fxml,
            javafx.graphics;
}