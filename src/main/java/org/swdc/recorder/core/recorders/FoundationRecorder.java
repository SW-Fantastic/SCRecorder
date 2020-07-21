package org.swdc.recorder.core.recorders;

import org.swdc.recorder.core.AbstractRecorder;


public class FoundationRecorder extends AbstractRecorder {

    @Override
    public RecorderInfo getRecorderInfo() {
        return new RecorderInfo("Capture screen 0","avfoundation");
    }

}
