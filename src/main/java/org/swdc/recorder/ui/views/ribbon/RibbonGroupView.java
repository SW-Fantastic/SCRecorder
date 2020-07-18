package org.swdc.recorder.ui.views.ribbon;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import org.swdc.fx.FXView;
import org.swdc.fx.resource.icons.FontSize;
import org.swdc.fx.resource.icons.FontawsomeService;

public abstract class RibbonGroupView extends FXView {

    public abstract RibbonGroup getGroup();

    public abstract int getIndex();

    public void setupButton(String id, String icon, FontawsomeService fontawsomeService) {
        Button btn = findById(id);
        Label lblIcon = new Label();
        lblIcon.setFont(fontawsomeService.getFont(FontSize.MIDDLE_SMALL));
        lblIcon.setText(fontawsomeService.getFontIcon(icon));
        btn.setGraphic(lblIcon);
        btn.setContentDisplay(ContentDisplay.TOP);
    }

}
