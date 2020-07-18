package org.swdc.recorder.core.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Native方法，获取本地的音视频设备、
 * 实现在src的native内。
 */
public class Devices {

    static {
        try {
            String name = System.getProperty("os.name").toLowerCase();
            String subFix = "";
            String libName = "MediaSupport";
            File support = null;
            if (name.toLowerCase().contains("mac")) {
                subFix = "dylib";
            } else if (name.toLowerCase().contains("windows")){
                libName = "libMediaSupport";
                subFix = "dll";
            } else if (name.toLowerCase().contains("linux")) {
                subFix = "so";
            }
            support = new File(libName + "." + subFix);
            if (!support.exists()) {
                Module module = Devices.class.getModule();
                Integer bit = Integer.valueOf(System.getProperty("sun.arch.data.model"));
                InputStream in = module.getResourceAsStream("libMediaSupport_" + bit + "." + subFix);
                FileOutputStream outputStream = new FileOutputStream(new File("libMediaSupport."+subFix));
                in.transferTo(outputStream);
                outputStream.close();
            }
            System.loadLibrary(libName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static native List<MediaDevice> getAudioDevices();

}
