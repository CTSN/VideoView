package com.roy.testvideo;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.zero.smallvideorecord.DeviceUtils;
import com.zero.smallvideorecord.JianXiCamera;

import java.io.File;

/**
 * Created by roy on 2018/1/12.
 */

public class MyApp extends Application {

    public static Context app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        initSmallVideo(this);
    }

    public static void initSmallVideo(Context context) {
        String appName =   context.getPackageName();
        File dcim = new File(Environment.getExternalStorageDirectory() + "/" + appName);
        // 设置拍摄视频缓存路径
//        File dcim = new File (Environment.getExternalStorageDirectory() + "/roy");
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim.getAbsolutePath() + "/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getAbsolutePath() + "/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/zip/");
        }
        // 开启log输出,ffmpeg输出到logcat
        //JianXiCamera.setDebugMode(true);
        // 初始化拍摄SDK，必须
        JianXiCamera.initialize(false,null);
    }
}
