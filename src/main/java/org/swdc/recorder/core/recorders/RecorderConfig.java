package org.swdc.recorder.core.recorders;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RecorderConfig {

    private int width;
    private int height;
    private String pixFormat;
    private int frameRate;
    private int bitRate;

}
