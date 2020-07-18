package org.swdc.recorder.ui.views.ribbon;

import org.swdc.fx.ViewManager;

public class GroupViewManager extends ViewManager {

    @Override
    public void initialize() {
        super.initialize();
        this.scanComponentAndInitialize();
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return RibbonGroupView.class.isAssignableFrom(clazz);
    }
}
