package com.example.coronadiagnosticapp.ui.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coronadiagnosticapp.R;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;

//public class VideoRecordActivity extends Activity implements SurfaceHolder.Callback {
//    private final String VIDEO_PATH_NAME = "/mnt/sdcard/DCIM/Corona/output.mp4";
//
//    private MediaRecorder mMediaRecorder;
//    private Camera mCamera;
//    private SurfaceView mSurfaceView;
//    private SurfaceHolder mHolder;
//    private View mToggleButton;
//    private boolean mInitSuccesful;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_record);
//
//        // we shall take the video in landscape orientation
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
////        mSurfaceView.setRotation(90);
//        mHolder = mSurfaceView.getHolder();
//        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//        mToggleButton = (ToggleButton) findViewById(R.id.toggleRecordingButton);
//        mToggleButton.setOnClickListener(new OnClickListener() {
//            @Override
//            // toggle video recording
//            public void onClick(View v) {
//                if (((ToggleButton) v).isChecked()) {
//                    mMediaRecorder.start();
//                    try {
//                        Thread.sleep(5 * 1000); // This will recode for 10 seconds, if you don't want then just remove it.
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    finish();
//                } else {
//                    mMediaRecorder.stop();
//                    mMediaRecorder.reset();
//                    try {
//                        initRecorder(mHolder.getSurface());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//
//    /* Init the MediaRecorder, the order the methods are called is vital to
//     * its correct functioning */
//    private void initRecorder(Surface surface) throws IOException {
//        // It is very important to unlock the camera before doing setCamera
//        // or it will results in a black preview
////        if (mCamera == null) {
////            mCamera = Camera.open();
////            mCamera.unlock();
////        }
//        mCamera = Camera.open();
//        mCamera.setDisplayOrientation(90);
//        mCamera.unlock();
//
//
//        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
//        mMediaRecorder.setPreviewDisplay(surface);
//        mMediaRecorder.setCamera(mCamera);
//
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//        //       mMediaRecorder.setOutputFormat(8);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoSize(640, 480);
//        mMediaRecorder.setOutputFile(VIDEO_PATH_NAME);
//
//        try {
//            mMediaRecorder.prepare();
//        } catch (IllegalStateException e) {
//            // This is thrown if the previous calls are not called with the
//            // proper order
//            e.printStackTrace();
//        }
//
//        mInitSuccesful = true;
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        try {
//            if (!mInitSuccesful)
//                initRecorder(mHolder.getSurface());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        shutdown();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//    }
//
//    private void shutdown() {
//        // Release MediaRecorder and especially the Camera as it's a shared
//        // object that can be used by other applications
//        mMediaRecorder.reset();
//        mMediaRecorder.release();
//        mCamera.release();
//
//        // once the objects have been released they can't be reused
//        mMediaRecorder = null;
//        mCamera = null;
//    }
//}
public class VideoRecordActivity extends AppCompatActivity {

    private Camera myCamera;
    private MyCameraSurfaceView myCameraSurfaceView;
    private MediaRecorder mediaRecorder;

    Button myButton;
    RadioButton flashOff, flashTorch;
    SurfaceHolder surfaceHolder;
    boolean recording;
    private String filename;
    CountDownTimer countDownTimer;
    ProgressBar barTimer;
    TextView textTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recording = false;

        setContentView(R.layout.activity_video_record);

        // Get Camera for preview
        myCamera = getCameraInstance();
        if (myCamera == null) {
            Toast.makeText(VideoRecordActivity.this, "Fail to get Camera", Toast.LENGTH_LONG).show();
        }

        myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
        //   FrameLayout myCameraPreview = (FrameLayout) findViewById(R.id.videoview);
        //   myCameraPreview.addView(myCameraSurfaceView);

        myButton = (Button) findViewById(R.id.ready_btn);
        myButton.setOnClickListener(myButtonOnClickListener);

//        flashOff = (RadioButton) findViewById(R.id.flashoff);
//        flashTorch = (RadioButton) findViewById(R.id.flashtorch);

        barTimer = findViewById(R.id.barTimer);
        textTimer = findViewById(R.id.textTimer);
    }

    Button.OnTouchListener flashButtonOnTouchListener = new Button.OnTouchListener() {

        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            // TODO Auto-generated method stub
            if (myCamera != null) {
                Camera.Parameters parameters = myCamera.getParameters();

                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        myCamera.setParameters(parameters);
                        break;
                    case MotionEvent.ACTION_UP:
                        parameters.setFlashMode(FLASH_MODE_OFF);
                        myCamera.setParameters(parameters);
                        break;
                }
                ;
            }

            return true;
        }
    };


    Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (recording) {
                myButton.setText("READY");
                barTimer.setVisibility(View.GONE);
                textTimer.setVisibility(View.GONE);
                // stop recording and release camera
                mediaRecorder.stop(); // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                countDownTimer.cancel();

                myCamera.lock();
                // take camera access back from
                recording = false;

                // MediaRecorder
                // inform the user that recording has stopped
                // Exit after saved
                //finish();
            } else {

                // Release Camera before MediaRecorder start
                releaseCamera();

                if (!prepareMediaRecorder()) {
                    Toast.makeText(VideoRecordActivity.this,
                            "Fail in prepareMediaRecorder()!\n - Ended -",
                            Toast.LENGTH_LONG).show();

                }
                mediaRecorder.start();
                recording = true;
                myButton.setText("CANCEL");
                barTimer.setVisibility(View.VISIBLE);
                textTimer.setVisibility(View.VISIBLE);
                startTimer(3);
            }
        }
    };

    private Camera getCameraInstance() {
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private String getFlashModeSetting() {
        if (flashTorch.isChecked()) {
            return Camera.Parameters.FLASH_MODE_TORCH;
        } else {
            return FLASH_MODE_OFF;
        }
    }

    private boolean prepareMediaRecorder() {

        filename = getApplicationContext().getExternalFilesDir("/").getAbsolutePath() + "video";
        myCamera = getCameraInstance();

        Camera.Parameters parameters = myCamera.getParameters();
//        parameters.setFlashMode(getFlashModeSetting());
        parameters.setFlashMode(FLASH_MODE_OFF);
        myCamera.setParameters(parameters);

        mediaRecorder = new MediaRecorder();

        myCamera.unlock();
        mediaRecorder.setCamera(myCamera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        //mediaRecorder.setVideoSize(1280, 720);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);

        mediaRecorder.setOutputFormat(profile.fileFormat);
        mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setVideoEncoder(profile.videoCodec);


        mediaRecorder.setOutputFile(filename);
//        mediaRecorder.setMaxDuration(6000000); // Set max duration 60 sec.
//        mediaRecorder.setMaxFileSize(500000000); // Set max file size 500M

        mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

        mediaRecorder.setOrientationHint(90);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("VideoRecordActivity", "mediaRecorder.prepare failed" + e.getMessage());
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            Log.d("VideoRecordActivity", "mediaRecorder.prepare failed");
            e.printStackTrace();
            return false;
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder(); // if you are using MediaRecorder, release it
        // first
        releaseCamera(); // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            myCamera.lock(); // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (myCamera != null) {
            myCamera.release();
            // release the camera for other applications
            myCamera = null;
        }
    }

    public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

        private SurfaceHolder mHolder;
        private Camera mCamera;

        public MyCameraSurfaceView(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
            // If your preview can change or rotate, take care of those events
            // here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // make any resize, rotate or reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub

        }
    }

    private static String getOutputMediaFile(String sufix) {

        String mediaFile;
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "/YappBack");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("VideoLogger", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        if (!sufix.equals("movie")) {
            mediaFile = mediaStorageDir.getPath() + File.separator + "output_" + timeStamp + "_" + sufix + ".txt";
        } else {
            mediaFile = mediaStorageDir.getPath() + File.separator + "output_" + timeStamp + ".mp4";

        }
//        mediaFile = Environment.getExternalStorageDirectory().getPath()+ "/default.mp4";
        return mediaFile;
    }


    private void startTimer(final int time_seconds) {
        countDownTimer = new CountDownTimer(time_seconds * 1000, 500) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                barTimer.setProgress((int) seconds);
                textTimer.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                if (textTimer.getText().equals("0")) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", filename);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    textTimer.setText("20");
                    barTimer.setProgress(time_seconds);
                }
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
    }
}