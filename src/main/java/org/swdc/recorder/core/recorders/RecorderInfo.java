package org.swdc.recorder.core.recorders;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecorderInfo {
    private String address;
    private String format;
}
