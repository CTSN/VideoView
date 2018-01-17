package com.roy.testvideo;

import android.os.Bundle;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

import com.zero.smallvideorecord.LocalMediaCompress;
import com.zero.smallvideorecord.model.AutoVBRMode;
import com.zero.smallvideorecord.model.LocalMediaConfig;
import com.zero.smallvideorecord.model.OnlyCompressOverBean;

import java.io.IOException;

public class MainActivity extends Activity {

    private VideoRecorderView mRecorderView;
    private Button mShootBtn;
    private boolean isFinish = true;
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecorderView = (VideoRecorderView) findViewById(R.id.movieRecorderView);
        mShootBtn = (Button) findViewById(R.id.shoot_button);

        //用户长按事件监听
        mShootBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {//用户按下拍摄按钮
                    mRecorderView.record(new VideoRecorderView.OnRecordFinishListener() {

                        @Override
                        public void onRecordFinish() {
                            if(!success&&mRecorderView.getTimeCount()<10){//判断用户按下时间是否大于10秒
                                success = true;
                                handler.sendEmptyMessage(1);
                            }
                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_UP) {//用户抬起拍摄按钮
                    if (mRecorderView.getTimeCount() > 3){//判断用户按下时间是否大于3秒
                        if(!success){
                            success = true;
                            handler.sendEmptyMessage(1);
                        }
                    } else {
                        success = false;
                        if (mRecorderView.getmVecordFile() != null)
                            mRecorderView.getmVecordFile().delete();//删除录制的过短视频
                        mRecorderView.stop();//停止录制
                        Toast.makeText(MainActivity.this, "视频录制时间太短", Toast.LENGTH_SHORT).show();
                        initCamera();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initCamera();
//        isFinish = true;
//        if (mRecorderView.getmVecordFile() != null)
//            mRecorderView.getmVecordFile().delete();//视频使用后删除
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isFinish = false;
        success = false;
        mRecorderView.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRecorderView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecorderView.stop();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(success){
                finishActivity();
            }
        }
    };

    //视频录制结束后，跳转的函数
    private void finishActivity() {
        if (isFinish) {
//            Intent intent = new Intent(this, SuccessActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("text", mRecorderView.getmVecordFile().toString());
//            intent.putExtras(bundle);
//            startActivity(intent);
            // 选择本地视频压缩
            LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
            final LocalMediaConfig config = buidler
                    .setVideoPath(mRecorderView.getmVecordFile().getAbsolutePath())
                    .captureThumbnailsTime(1)
                    .doH264Compress(new AutoVBRMode())
                    .setFramerate(15)
                    .build();
            mShootBtn.setText("压缩中");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShootBtn.setText("按住拍");
                                try {
                                    Toast.makeText(MainActivity.this, onlyCompressOverBean.getVideoPath(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                initCamera();
                            }
                        });
                        mRecorderView.getmVecordFile().delete();//删除原视频
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
//            mRecorderView.stop();
        }
        success = false;
    }


    private void initCamera(){
        try {
            mRecorderView.initCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 录制完成回调
     */
    public interface OnShootCompletionListener {
        public void OnShootSuccess(String path, int second);
        public void OnShootFailure();
    }
}

