package org.swdc.recorder.ui.views;

import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.swdc.fx.FXView;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.View;
import org.swdc.fx.resource.icons.FontSize;
import org.swdc.fx.resource.icons.FontawsomeService;
import org.swdc.recorder.core.RecorderService;
import org.swdc.recorder.ui.views.ribbon.RibbonGroup;
import org.swdc.recorder.ui.views.ribbon.RibbonGroupView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@View(title = "时光印象",background = true,resizeable = true)
public class RecorderView extends FXView {

    private ToggleGroup group;

    @Aware
    private FontawsomeService fontawsomeService = null;

    @Aware
    private RecorderService recorderService = null;

    @Override
    public void initialize() {
        Stage stage = getStage();
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        group = new ToggleGroup();
        ToggleButton recordBtn = findById("record");
        recordBtn.setUserData(RibbonGroup.RIBBON_RECORD);
        ToggleButton avSettingBtn = findById("avSetting");
        avSettingBtn.setUserData(RibbonGroup.RIBBON_AV_CONF);
        ToggleButton systemBtn = findById("system");
        systemBtn.setUserData(RibbonGroup.RIBBON_CONFIG);

        group.getToggles().addAll(recordBtn,avSettingBtn,systemBtn);

        Button startRecord = findById("record-begin");
        Button stopRecord = findById("record-stop");

        recorderService.disabledOnNotRecord(stopRecord);
        stopRecord.setDisable(true);

        startRecord.setFont(fontawsomeService.getFont(FontSize.VERY_SMALL));
        startRecord.setText(fontawsomeService.getFontIcon("play"));
        startRecord.setPadding(new Insets(4,4,4,4));

        stopRecord.setFont(fontawsomeService.getFont(FontSize.VERY_SMALL));
        stopRecord.setText(fontawsomeService.getFontIcon("stop"));
        stopRecord.setPadding(new Insets(4,4,4,4));

        group.selectedToggleProperty().addListener(this::onSelectToggleChange);
        changeToggle(RibbonGroup.RIBBON_RECORD);
    }

    private void onSelectToggleChange(Observable toggle, Toggle toggleOld, Toggle toggleNew) {
        Toggle selected = toggleNew != null ? toggleNew : toggleOld;
        if (toggleNew == null) {
            group.selectToggle(toggleOld);
        }
        changeToggle((RibbonGroup) selected.getUserData());
    }

    public <T> T getRibbonView(RibbonGroup group, Class<T> clazz) {
        List<RibbonGroupView> views = getScoped(RibbonGroupView.class);
        List<RibbonGroupView> viewGroups = views.stream()
                .filter(view->view.getGroup().equals(group))
                .sorted(Comparator.comparingInt(RibbonGroupView::getIndex))
                .collect(Collectors.toList());
        for (RibbonGroupView view: viewGroups) {
            if (clazz == view.getClass()) {
                return (T)view;
            }
        }
        return null;
    }

    public void changeToggle(RibbonGroup group) {
        for (Toggle toggle : this.group.getToggles()) {
            if (toggle.getUserData().equals(group)) {
                List<RibbonGroupView> views = getScoped(RibbonGroupView.class);
                List<Node> viewGroups = views.stream()
                        .filter(view->view.getGroup().equals(group))
                        .sorted(Comparator.comparingInt(RibbonGroupView::getIndex))
                        .map(v->(Node)v.getView())
                        .collect(Collectors.toList());
                HBox hBox = findById("ribbonGroups");
                hBox.getChildren().clear();
                hBox.getChildren().addAll(viewGroups);
                if (!toggle.isSelected()) {
                    this.group.selectToggle(toggle);
                }
                break;
            }
        }
    }

}
