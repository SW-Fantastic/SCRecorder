package org.swdc.recorder.core.ffmpeg;

import lombok.Getter;
import org.bytedeco.javacpp.avformat;

import java.io.File;

public class FFOutContext implements AutoCloseable {

    @Getter
    private avformat.AVFormatContext outputCtx;

    public FFOutContext open(File file) {
        outputCtx = new avformat.AVFormatContext();
        int rst = avformat.avformat_alloc_output_context2(outputCtx,null,null,file.getAbsolutePath());
        if (rst < 0) {
            this.close();
            return null;
        }
        // 判断是否存在文件，不存在就打开
        if ((outputCtx.flags() & avformat.AVFMT_NOFILE) == 0 ) {
            avformat.AVIOContext ctxPb = new avformat.AVIOContext(null);
            rst = avformat.avio_open(ctxPb, file.getAbsolutePath(),avformat.AVIO_FLAG_WRITE);
            if (rst < 0){
                this.close();
                return null;
            }
            outputCtx.pb(ctxPb);
        }
        return this;
    }

    public avformat.AVStream createEncodeStream(FFEncodeCodec encodeCodec) {
        return avformat.avformat_new_stream(this.outputCtx,encodeCodec.getCodecContext().codec());
    }

    @Override
    public void close() {
        if (outputCtx == null){
            return;
        }
        if (outputCtx.isNull()) {
            return;
        }
        avformat.avformat_flush(outputCtx);
        avformat.avformat_free_context(outputCtx);
        outputCtx = null;
    }
}
