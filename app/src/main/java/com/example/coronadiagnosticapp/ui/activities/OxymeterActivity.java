package com.example.coronadiagnosticapp.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.Oxymeter;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterData;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterImpl;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

interface OxymeterThreadEventListener {
    void onFrame(int frameNumber);

    void onSuccess();
}

class OxymeterThread extends Thread {
    private static final String TAG = "OxThread";
    private Oxymeter oxymeter;
    private Queue<byte[]> framesQueue;
    private Camera cam;
    private boolean enabled = true;
    private int totalFrames;
    private OxymeterThreadEventListener eventListener;

    OxymeterThread(Oxymeter oxymeter, Queue<byte[]> framesQueue, Camera cam, int totalFrames, OxymeterThreadEventListener eventListener) {
        this.oxymeter = oxymeter;
        this.framesQueue = framesQueue;
        this.cam = cam;
        this.totalFrames = totalFrames;
        this.eventListener = eventListener;
    }

    public synchronized void doStop() {
        enabled = false;
    }

    public void run() {
        Log.i(TAG, "starting.");
        int framesPassedToOxymeter = 0;
        // Keep running until we passed `totalFrames` frames to the oxymeter
        while (framesPassedToOxymeter < totalFrames && enabled) {
            // push any available frames to the oxymeter
            if (!framesQueue.isEmpty()) {
                Log.i(TAG, "Processing Frame...");
                oxymeter.updateWithFrame(framesQueue.remove(), cam);
                framesPassedToOxymeter++;
                eventListener.onFrame(framesPassedToOxymeter);
            }
        }
        if (enabled) {
            Log.i(TAG, "Oxymeter processed all frames.");
            eventListener.onSuccess();
        }
        Log.i(TAG, "Finished Oxymeter!");
    }
}


public class OxymeterActivity extends Activity {
    // Variables Initialization
    private static final String TAG = "HeartRateMonitor";
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    //Freq + timer variable
    private static long startTime = 0;
    //ProgressBar
    ProgressBar progressBarView;
    TextView tv_time;
    TextView heartRate;
    RotateAnimation makeVertical;
    //TextView
    private TextView alert;
    // This value actually stores FPS * 1000 (because that's how the `Camera` module handles it's data).
    private int previewFps = -1;
    private Queue<byte[]> framesQueue;
    private Oxymeter oxymeter;
    private OxymeterThread oxymeterUpdater;
    private int totalTime = 30;
    public int currentHeartRate;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // if we have started recording
            if (framesQueue != null) {
                framesQueue.add(data);
            }
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.e("surfaceChanged:", "OK");
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);


            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            // Finds the fastest stable fps the preview can support.
            List<int[]> fpsRanges = parameters.getSupportedPreviewFpsRange();
            Log.i(TAG, "Available preview fps ranges:");
            for (int[] range : fpsRanges) {
                Log.i(TAG, "Range: " + range[0] + " - " + range[1]);
                // The fps range should be stable (min fps equals max fps)
                if (range[0] == range[1] && range[0] > previewFps) {
                    previewFps = range[0];
                }
            }
            if (previewFps == -1) {
                throw new RuntimeException("Could not find any stable fps range");
            }
            if ((previewFps % 1000) != 0) {
                Log.w(TAG, "Preview FPS is not a whole number");
            }
            Log.i(TAG, "Preview running on " + previewFps + " FPS");
            parameters.setPreviewFpsRange(previewFps, previewFps);

            camera.setParameters(parameters);
            camera.startPreview();
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
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
    }

    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(ApplicationLanguageHelper.Companion.wrap(newBase, "he"));
    }


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
        heartRate = (TextView) findViewById(R.id.heartRate);


        /*Animation*/
        makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setSecondaryProgress(totalTime * 1000);
        progressBarView.setProgress(0);

        OxymeterActivity thisActivity = this;
        readyBtn.setOnClickListener(view -> {
            Log.i(TAG, "Pressed start oxymeter button.");
            showProgressBarAndHideAlert();
            oxymeter = new OxymeterImpl(previewFps / 1000);
            oxymeter.setOnBadFinger(() -> {
                thisActivity.badFinger();
                return null;
            });
            oxymeter.setUpdateView(heartRate -> {
                thisActivity.updateView(heartRate);
                return null;
            });
            framesQueue = new LinkedList<>();
            final int totalFrames = 900;
            oxymeterUpdater = new OxymeterThread(oxymeter, framesQueue, camera, totalFrames, new OxymeterThreadEventListener() {
                @Override
                public void onFrame(int frameNumber) {
                    Log.i(TAG, "Current frame:" + frameNumber);
                    float approximateFinishTime = (totalFrames - frameNumber) / 30f;
                    runOnUiThread(() -> {
                        setProgress(frameNumber, totalFrames);
                        tv_time.setText(Integer.toString((int) approximateFinishTime));
                    });
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "finished processing all frames");
                    finishOxymeter();
                }
            });
            Log.i(TAG, "starting oxymeter.");
            oxymeterUpdater.start();
        });
    }

    public void finishOxymeter() {
        OxymeterData result = oxymeter.finish(totalTime, previewFps / 1000D);
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
            runOnUiThread(this::removeProgressBarAndShowAlert);
        }
    }

    public void badFinger() {
        Log.w(TAG, "Finger not recognised!");
        runOnUiThread(this::removeProgressBarAndShowAlert);
        stopAndReset();
    }

    public void updateView(int heartRate) {
        currentHeartRate = heartRate;
        runOnUiThread(this::updateMeasurements);
    }

    private void updateMeasurements() {
        heartRate.setText(Integer.toString(currentHeartRate));
    }

    public void stopAndReset() {
        Log.i(TAG, "Stopping Oxymeter");
        oxymeterUpdater.doStop();
    }

    public void setProgress(int currentProgress, int maxProgress) {
        progressBarView.setMax(maxProgress);
        progressBarView.setSecondaryProgress(maxProgress);
        progressBarView.setProgress(currentProgress);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //Wakelock + Open device camera + set orientation to 90 degree
    //store system time as a start time for the analyzing process
    //your activity to start interacting with the user.
    // This is a good place to begin animations, open exclusive-access devices (such as the camera)
    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    // this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    @Override
    public void onPause() {
        super.onPause();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
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

