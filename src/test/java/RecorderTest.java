import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.swdc.recorder.core.AbstractRecorder;
import org.swdc.recorder.core.platform.Devices;
import org.swdc.recorder.core.platform.MediaDevice;
import org.swdc.recorder.core.recorders.GDIRecorder;
import org.swdc.recorder.ui.RecUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class RecorderTest {

    public static void main(String[] args) throws InterruptedException {
        /*GDIRecorder recorder = new GDIRecorder();
        Thread thread = new Thread(() -> {
            recorder.record(new File("test.mp4"));
        });
        thread.start();
        Thread.sleep(1000 * 30);
        recorder.stop();*/
        /* List<MediaDevice> devices = Devices.getAudioDevices();
        devices.forEach(i -> {
            System.out.println(i.getName());
        });
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Long milles  = System.currentTimeMillis();
        //Thread.sleep(500);
        System.out.println(RecUtils.convertSecondsToString(System.currentTimeMillis() - milles));

         */
        AbstractRecorder.initFFMpeg();
        avformat.AVInputFormat inputFormat = avformat.av_find_input_format("dshow");
        avformat.AVFormatContext context = avformat.avformat_alloc_context();
        int rst = avformat.avformat_open_input(context,"audio=virtual-audio-capturer",inputFormat,new avutil.AVDictionary());
        if (rst < 0) {
            return;
        }
        rst = avformat.avformat_find_stream_info(context,new avutil.AVDictionary());
        if (rst < 0) {
            return;
        }
        avformat.AVStream audioSteam = findAudioStream(context);
        if (audioSteam == null) {
            return;
        }
        avcodec.AVCodec sysAudioCodec = avcodec.avcodec_find_decoder(audioSteam.codecpar().codec_id());
        avcodec.AVCodecContext audioCtx = avcodec.avcodec_alloc_context3(sysAudioCodec);
        avcodec.avcodec_parameters_to_context(audioCtx,audioSteam.codecpar());
        rst = avcodec.avcodec_open2(audioCtx,sysAudioCodec,new avutil.AVDictionary());
        if (rst < 0){
            return;
        }
        avcodec.AVCodec decoder = avcodec.avcodec_find_decoder(audioSteam.codecpar().codec_id());
        avcodec.AVCodecContext decoderCtx = avcodec.avcodec_alloc_context3(decoder);
        rst = avcodec.avcodec_open2(decoderCtx,decoder,new avutil.AVDictionary());
        if (rst < 0) {
            return;
        }
    }

    static avformat.AVStream findAudioStream(avformat.AVFormatContext context) {
        if (context == null || context.isNull()) {
            return null;
        }
        avformat.AVStream audioStream = null;
        for (int idx = 0; idx < context.nb_streams(); idx ++) {
            if (context.streams(idx).codecpar().codec_type() == avutil.AVMEDIA_TYPE_AUDIO) {
                audioStream = context.streams(idx);
            }
        }
        return audioStream;
    }

}
