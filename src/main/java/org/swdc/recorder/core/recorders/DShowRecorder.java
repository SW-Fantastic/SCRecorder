package org.swdc.recorder.core.recorders;

import org.swdc.recorder.core.AbstractRecorder;


public class DShowRecorder extends AbstractRecorder {

    @Override
    public RecorderInfo getRecorderInfo() {
        return new RecorderInfo("video=screen-capture-recorder","dshow");
    }

}
