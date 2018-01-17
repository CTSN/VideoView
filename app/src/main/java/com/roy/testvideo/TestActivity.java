package com.roy.testvideo;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.roy.testvideo.record.VideoInputDialog;
import com.zero.smallvideorecord.LocalMediaCompress;
import com.zero.smallvideorecord.model.AutoVBRMode;
import com.zero.smallvideorecord.model.LocalMediaConfig;
import com.zero.smallvideorecord.model.OnlyCompressOverBean;

import java.io.File;

/**
 * Created by roy on 2018/1/12.
 */
public class TestActivity extends AppCompatActivity implements VideoInputDialog.VideoCall {
    ImageView image;
    ImageView imag2;
    Button button;
    Button button2;

    String path;//视频录制输出地址
    /**
     * 压缩后
     */
    private TextView mShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mShow = (TextView) findViewById(R.id.show);
        image = (ImageView) findViewById(R.id.image);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        imag2 = (ImageView) findViewById(R.id.imag2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //显示视频录制控件
                VideoInputDialog.show(getSupportFragmentManager(), TestActivity.this, VideoInputDialog.Q480, TestActivity.this);
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openView(path);
            }
        });

        imag2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openView((String) view.getTag());
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShow.setText("压缩中");
                LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
                final LocalMediaConfig config = buidler
                        .setVideoPath(path)
                        .captureThumbnailsTime(1)
                        .doH264Compress(new AutoVBRMode(30))
                        .setFramerate(15)
                        .build();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(TestActivity.this, onlyCompressOverBean.getVideoPath(), Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mShow.setText("压缩后");
                                    new File(path).delete();
                                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(onlyCompressOverBean.getVideoPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                                    imag2.setImageBitmap(bitmap);
                                    imag2.setTag(onlyCompressOverBean.getVideoPath());
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * 小视屏录制回调
     *
     * @param path
     */
    @Override
    public void videoPathCall(String path) {
        Log.e("地址:", path);
        //根据视频地址获取缩略图
        this.path = path;
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        image.setImageBitmap(bitmap);
    }

    public void openView(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        SystemAppUtils.openFile(file, this);
    }
}
