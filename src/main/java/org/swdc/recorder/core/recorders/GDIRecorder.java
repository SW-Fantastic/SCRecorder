package org.swdc.recorder.core.recorders;

import org.swdc.recorder.core.AbstractRecorder;
import org.swdc.recorder.core.platform.Devices;

public class GDIRecorder extends AbstractRecorder {

    @Override
    public RecorderInfo getRecorderInfo() {
        return new RecorderInfo("desktop","gdigrab");
    }


    @Override
    public void recordSystemAudio(boolean systemAudio) {
        long count = Devices.getAudioDevices().stream()
                .filter(i -> i.getName()
                        .equalsIgnoreCase("virtual-audio-capturer"))
                .count();
        if (count > 0 && systemAudio){
            super.recordSystemAudio(true);
        }
        super.recordSystemAudio(systemAudio);
    }

    @Override
    public void recordVoice(boolean enableVoice) {
        if (enableVoice) {
            long audio = Devices.getAudioDevices()
                    .stream()
                    .filter(i -> i.isInput())
                    .count();
            if (audio > 0){
                super.recordVoice(true);
            }
            return;
        }
        super.recordVoice(false);
    }
}
