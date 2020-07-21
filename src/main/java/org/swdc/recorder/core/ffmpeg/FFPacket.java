package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.avcodec;

public class FFPacket implements AutoCloseable {

    @Getter
    private avcodec.AVPacket packet;

    public FFPacket alloc() {
        packet = avcodec.av_packet_alloc();
        if (packet == null) {
            return null;
        }
        avcodec.av_init_packet(packet);
        return this;
    }

    @Override
    public void close() throws Exception {
        if (packet != null && !packet.isNull()) {
            avcodec.av_packet_free(packet);
        }
    }
}
