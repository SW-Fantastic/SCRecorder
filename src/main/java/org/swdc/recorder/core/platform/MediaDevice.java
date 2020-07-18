package org.swdc.recorder.core.platform;

public class MediaDevice {

    private String name;
    private int index;
    private boolean input;
    private boolean output;

    public MediaDevice(String name,int index, boolean input, boolean output) {
        this.name = name;
        this.index = index;
        this.input = input;
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isOutput() {
        return output;
    }

    @Override
    public String toString() {
        return name;
    }
}
