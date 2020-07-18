package org.swdc.recorder.core.events;


import org.swdc.fx.AppComponent;
import org.swdc.fx.event.AppEvent;
import org.swdc.recorder.core.RecordStatus;

import java.io.File;

public class RecordStatusChangeEvent extends AppEvent<RecordStatus> {

    private File targetFile;

    public RecordStatusChangeEvent(RecordStatus data, AppComponent source) {
        super(data, source);
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public File getTargetFile() {
        return targetFile;
    }

}
