package com.example.coronadiagnosticapp.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.Oxymeter;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterData;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterImpl;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.view.animation.Animation.RELATIVE_TO_SELF;


class OxymeterThread extends Thread {
    private Oxymeter oxymeter;
    private Queue<Double[]> framesQueue;
    private CameraCharacteristics chars;
    private boolean doStop = false;

    OxymeterThread(Oxymeter oxymeter, Queue<Double[]> framesQueue, CameraCharacteristics chars) {
        this.oxymeter = oxymeter;
        this.framesQueue = framesQueue;
        this.chars = chars;
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    public void run() {
        // Keep running until we need to stop and we cleared the queue
        while (!(framesQueue.isEmpty() && doStop)) {
            // push any available frames to the oxymeter
            if (!framesQueue.isEmpty()) {
                oxymeter.updateWithFrame(framesQueue.remove());
            }
        }
    }
}


public class OxymeterActivity extends Activity {
    // Variables Initialization
    private static final String TAG = "HeartRateMonitor";
    ;
    private static SurfaceHolder previewHolder = null;
    private static CameraDevice camera = null;
    private static CameraManager manager = null;
    private static CameraCharacteristics chars = null;
    private static ImageReader reader = null;
    //Freq + timer variable
    private static long startTime = 0;
    //ProgressBar
    ProgressBar progressBarView;
    TextView tv_time;
    int progress;
    CountDownTimer countDownTimer;
    RotateAnimation makeVertical;
    //TextView
    private TextView alert;


    private Queue<Double[]> framesQueue;
    private Oxymeter oxymeter;
    private OxymeterThread oxymeterUpdater;
    private int totalTime = 30;

    /*private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // if we have started recording
            if (framesQueue != null) {
                framesQueue.add(data);
            }
        }
    };*/

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                manager = (CameraManager) OxymeterActivity.this.getSystemService(Context.CAMERA_SERVICE);
                if (ActivityCompat.checkSelfPermission(OxymeterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                try {
                    manager.openCamera("0", cameraStateCallback, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            /*og.e("surfaceChanged:", "OK");
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);


            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }

            camera.setParameters(parameters);
            camera.startPreview();*/
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };
    private int rawHeight;
    private int rawWidth;
    private int bayer;

    /*private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }*/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // XML - Java Connecting
        SurfaceView preview = (SurfaceView) findViewById(R.id.preview);
        alert = (TextView) findViewById(R.id.putfinger);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Button
        Button readyBtn = (Button) findViewById(R.id.ready_btn);
        progressBarView = (ProgressBar) findViewById(R.id.barTimer);
        tv_time = (TextView) findViewById(R.id.textTimer);


        /*Animation*/
        makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setSecondaryProgress(totalTime * 1000);
        progressBarView.setProgress(0);

        OxymeterActivity thisActivity = this;
        readyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showProgressBarAndHideAlert();
                oxymeter = new OxymeterImpl(rawWidth, rawHeight, bayer);
                oxymeter.setOnBadFinger(() -> {
                    thisActivity.badFinger();
                    return null;
                });
                framesQueue = new LinkedList<Double[]>();
                oxymeterUpdater = new OxymeterThread(oxymeter, framesQueue, chars);
                oxymeterUpdater.start();

                ArrayList<Surface> targets = new ArrayList<Surface>();
                targets.add(reader.getSurface());
                targets.add(previewHolder.getSurface());

                try {
                    camera.createCaptureSession(targets, cameraSessionCallback, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                startCountdown();
            }
        });
    }

    //Prevent the system from restarting your activity during certain configuration changes,
    // but receive a callback when the configurations do change, so that you can manually update your activity as necessary.
    //such as screen orientation, keyboard availability, and language

    private void startCountdown() {
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
        }

        progress = 1;
        countDownTimer = new CountDownTimer(totalTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Millies until finished:" + millisUntilFinished);
                setProgress(progress, totalTime);
                progress = progress + 1;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                String newtime = seconds + " seconds";

                if (newtime.equals("0")) {
                    tv_time.setText("Press start");
                } else {
                    tv_time.setText(seconds + " seconds");
                }
            }

            @Override
            public void onFinish() {
                finishOxymeter();
            }
        };
        countDownTimer.start();
    }

    public void finishOxymeter() {
        OxymeterData result = oxymeter.finish(totalTime);
        stopAndReset();
        if (result != null) {
            Log.i(TAG, "Oxymeter finished successfully!");
            Intent returnIntent = new Intent();
            returnIntent.putExtra("OXYGEN_SATURATION", Integer.toString(result.getOxSaturation()));
            returnIntent.putExtra("BEATS_PER_MINUTE", Integer.toString(result.getHeartRate()));
            returnIntent.putExtra("BREATHS_PER_MINUTE", Integer.toString(result.getBreathRate()));
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            Log.w(TAG, "Oxymeter returned null");
            removeProgressBarAndShowAlert();
        }
    }

    public void badFinger() {
        Log.w(TAG, "Finger not recognised!");
        runOnUiThread(this::removeProgressBarAndShowAlert);
        stopAndReset();
    }

    public void stopAndReset() {
        Log.i(TAG, "Stopping Oxymeter");
        progress = 0;
        setProgress(progress, 30);
        countDownTimer.cancel();
        oxymeterUpdater.doStop();
    }

    public void setProgress(int startTime, int endTime) {
        progressBarView.setMax(endTime);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(startTime);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static ByteBuffer clone(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }


    ImageReader.OnImageAvailableListener imageAvailableCallback = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireNextImage();
            /*if (framesQueue != null) {
                ByteBuffer data = img.getPlanes()[0].getBuffer();
                framesQueue.add(OxymeterActivity.clone(data));
            }*/
            Double[] decoded = RawImageProcessing.decodeCentralSquareInRawImage(img, rawHeight, rawWidth, bayer);
            if (framesQueue != null) {
                framesQueue.add(decoded);
            }
            img.close();
        }
    };

    CameraCaptureSession.StateCallback cameraSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            CaptureRequest.Builder captureRequest;
            try {
                captureRequest = OxymeterActivity.this.camera.createCaptureRequest(OxymeterActivity.this.camera.TEMPLATE_PREVIEW);
                // TODO: We should use one of the ranges in chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                // I am guessing that [30, 30] will always be supported...
                captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<Integer>(30, 30));
                captureRequest.addTarget(reader.getSurface());
                captureRequest.addTarget(previewHolder.getSurface());
                session.setRepeatingRequest(captureRequest.build(), null, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            OxymeterActivity.this.camera = camera;


            try {
                chars = manager.getCameraCharacteristics("0");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
//            Range<Integer> availableRanges[] = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
//            Log.e(TAG, "CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES: " + Arrays.asList(availableRanges));
            StreamConfigurationMap map = chars.get(SCALER_STREAM_CONFIGURATION_MAP);
            Size smallestRaw = Collections.min(
                    Arrays.asList(map.getOutputSizes(ImageFormat.RAW_SENSOR)),
                    new CompareSizesByArea());

            OxymeterActivity.this.rawWidth = smallestRaw.getWidth();
            OxymeterActivity.this.rawHeight = smallestRaw.getHeight();
            OxymeterActivity.this.bayer = chars.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT);

            OxymeterActivity.this.reader = ImageReader.newInstance(rawWidth, rawHeight, ImageFormat.RAW_SENSOR, 50);
            reader.setOnImageAvailableListener(imageAvailableCallback, null);



        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }


    };

    //Wakelock + Open device camera + set orientation to 90 degree
    //store system time as a start time for the analyzing process
    //your activity to start interacting with the user.
    // This is a good place to begin animations, open exclusive-access devices (such as the camera)
    @Override
    public void onResume() {
        super.onResume();

        //camera.setDisplayOrientation(90);
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    // this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    @Override
    public void onPause() {
        super.onPause();
        //camera.setPreviewCallback(null);
        //camera.stopPreview();
        //camera.release();
        //camera = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopAndReset();
    }

    private void removeProgressBarAndShowAlert() {
        alert.setVisibility(View.VISIBLE); // Make alert "no finger" - visible
        progressBarView.setVisibility(View.INVISIBLE);
    }

    private void showProgressBarAndHideAlert() {
        alert.setVisibility(View.INVISIBLE);
        if (!(progressBarView.getVisibility() == View.VISIBLE)) {
            progressBarView.startAnimation(makeVertical);
            progressBarView.setVisibility(View.VISIBLE);
        }
    }
}

class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }

}

