package com.example.coronadiagnosticapp.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.Oxymeter;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterData;
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterImpl;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.animation.Animation.RELATIVE_TO_SELF;

interface OxymeterThreadEventListener {
    void onFrame(int frameNumber);

    void onSuccess(Oxymeter oxymeter);

    void onFingerRemoved();

    void onInvalidData();

    void onStartWithNewOxymeter();
}

class OxymeterThread extends Thread {
    private static final String TAG = "OxThread";
    private Oxymeter oxymeter;
    private int framesPassedToOxymeter = 0;
    private Queue<byte[]> framesQueue;
    private Camera cam;
    private Camera.Size previewSize;
    private Double samplingFrequency;
    private boolean enabled = false;
    private int totalFrames;
    private double previewFps;
    private Function1<? super Integer, Unit> onUpdateView;
    private Function2<? super Integer, ? super Double, Unit> onUpdateGraphView;
    private OxymeterThreadEventListener eventListener;

    OxymeterThread(Queue<byte[]> framesQueue,
                   Camera cam,
                   Camera.Size previewSize,
                   int totalFrames,
                   double previewFps,
                   Function1<? super Integer, Unit> onUpdateView,
                   Function2<? super Integer, ? super Double, Unit> onUpdateGraphView,
                   OxymeterThreadEventListener eventListener) {
        this.framesQueue = framesQueue;
        this.cam = cam;
        this.previewSize = previewSize;
        this.totalFrames = totalFrames;
        this.previewFps = previewFps;
        this.onUpdateView = onUpdateView;
        this.onUpdateGraphView = onUpdateGraphView;
        this.eventListener = eventListener;
    }

    private synchronized void onFingerRemoved() {
        enabled = false;
        this.oxymeter = null;
        eventListener.onFingerRemoved();
    }

    private synchronized void onInvalidData() {
        eventListener.onInvalidData();
        this.interrupt();
    }

    private synchronized void startWithNewOxymeter() {
        oxymeter = new OxymeterImpl(previewFps / 1000);
        oxymeter.setUpdateView(onUpdateView);
        oxymeter.setUpdateGraphView(onUpdateGraphView);
        oxymeter.setOnInvalidData(() -> {
            this.onInvalidData();
            return null;
        });
        enabled = true;
        framesQueue.clear();
        framesPassedToOxymeter = 0;
        eventListener.onStartWithNewOxymeter();
    }

    public void run() {
        Log.i(TAG, "Starting oxymeter thread.");
        // Keep running until we passed `totalFrames` frames to the oxymeter
        while (framesPassedToOxymeter < totalFrames) {
            if (interrupted()) {
                return;
            }
            if (!framesQueue.isEmpty()) {
                byte[] frame = framesQueue.remove();
                boolean fingerOnCamera = isFingerOnCamera(frame);

                if (enabled && !fingerOnCamera) {
                    // Should stop
                    onFingerRemoved();
                } else if (!enabled && fingerOnCamera) {
                    // Should start
                    startWithNewOxymeter();
                } else if (enabled) {
                    // Should keep goingOxymeter returned
                    passFrameToOxymeter(frame);
                }
            }
        }
        Log.i(TAG, "Finished Oxymeter!");
        eventListener.onSuccess(oxymeter);
    }

    private void passFrameToOxymeter(byte[] frame) {
        oxymeter.updateWithFrame(frame, cam);
        framesPassedToOxymeter++;
        eventListener.onFrame(framesPassedToOxymeter);
    }

    private boolean isFingerOnCamera(byte[] data) {
        double redAvg = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data,
                previewSize.height, previewSize.width, 1);
        //1 stands for red intensity, 2 for blue, 3 for green

        return redAvg >= 200;
    }
}


public class OxymeterActivity extends BaseActivity {
    public static final String EXTRA_OXYMETER_DATA = "OXYMETER_DATA";
    // Variables Initialization
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static final int MIN_LIGHT_VALUE = 170; // lux units.
    private static final int DATA_POINTS = 200;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    //Freq + timer variable
    private static long startTime = 0;
    private final SensorEventListener lightSensorListener
            = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_LIGHT) {
                return;
            }
            Log.d(TAG, "light sensor value:" + event.values[0]);
            if (event.values[0] < MIN_LIGHT_VALUE) {
                improveLightningAlert.setVisibility(VISIBLE);
                lightningImageView.setVisibility(VISIBLE);
            } else {
                improveLightningAlert.setVisibility(INVISIBLE);
                lightningImageView.setVisibility(INVISIBLE);
            }
        }

    };
    Button readyBtn;
    //ProgressBar
    ProgressBar progressBarView;
    ImageView tickImageView;
    ImageView lightningImageView;
    TextView timeLeftView;
    TextView heartRate;
    GraphView graphHeartRate;
    LineGraphSeries<DataPoint> mSeries;
    RotateAnimation makeVertical;
    public int currentHeartRate;
    //TextView
    private TextView putFingerAlert;
    private TextView improveLightningAlert;
    Camera.Size previewSize;
    // This value actually stores FPS * 1000 (because that's how the `Camera` module handles it's data).
    private int previewFps = -1;
    private Queue<byte[]> framesQueue;
    private OxymeterThread oxymeterUpdater;
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

            previewSize = getSmallestPreviewSize(width, height, parameters);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            Log.d(TAG, "Using width=" + previewSize.width + " height=" + previewSize.height);

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
    //    TODO check when this is used
    private String OXYMETER_DATA;

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width > width || size.height > height) {
                continue;
            }

            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea < resultArea) result = size;
            }
        }

        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // XML - Java Connecting
        SurfaceView preview = findViewById(R.id.preview);
        putFingerAlert = findViewById(R.id.putFingerMessage);
        improveLightningAlert = findViewById(R.id.improve_lightning);
        tickImageView = findViewById(R.id.fingerTickImage);
        lightningImageView = findViewById(R.id.lightningTickImage);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Button
        readyBtn = findViewById(R.id.ready_btn);

        progressBarView = findViewById(R.id.barTimer);
        timeLeftView = findViewById(R.id.textTimer);
        heartRate = findViewById(R.id.heartRate);
        graphHeartRate = findViewById(R.id.graphHeartRate);

        mSeries = new LineGraphSeries<>();
        mSeries.setColor(Color.WHITE);
        graphHeartRate.removeAllSeries();
        graphHeartRate.addSeries(mSeries);

        Viewport viewport = graphHeartRate.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(DATA_POINTS);

        GridLabelRenderer renderer = graphHeartRate.getGridLabelRenderer();
        renderer.setVerticalLabelsVisible(false);
        renderer.setHorizontalLabelsVisible(false);
        renderer.setGridStyle(GridLabelRenderer.GridStyle.NONE);

        int color = getResources().getColor(R.color.heartRateGraph);
        graphHeartRate.setBackgroundColor(color);
        /*Animation*/
//        TODO optional use xml anim - cleaner code
        makeVertical = new RotateAnimation(
                0, -90,
                RELATIVE_TO_SELF,
                0.5f,
                RELATIVE_TO_SELF,
                0.5f);

        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setProgress(0);

        readyBtn.setOnClickListener(view -> {
            Log.i(TAG, "Pressed start oxymeter button.");
            initializeOxymeterUpdater();
            readyBtn.setClickable(false);
        });

        SensorManager mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            mySensorManager.registerListener(
                    lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);


        } else {
            Log.e(TAG, "Couldn't find light sensor.");
        }
    }

    public void initializeOxymeterUpdater() {
//        TODO use arraylist
        framesQueue = new LinkedList<>();
        final int totalFrames = 900;
        oxymeterUpdater = new OxymeterThread(framesQueue, camera, previewSize, totalFrames, previewFps,
                heartRate -> {
                    this.updateView(heartRate);
                    return null;
                },
                (frame, point) -> {
                    this.updateGraphView(frame, point);
                    return null;
                }
                ,
//                TODO use this to implement or make an adapter
                new OxymeterThreadEventListener() {
                    @Override
                    public void onFrame(int frameNumber) {
                        Log.i(TAG, "Current frame:" + frameNumber);
                        runOnUiThread(() -> {
                            setProgress(frameNumber, totalFrames);
                        });
                    }

                    @Override
                    public void onSuccess(Oxymeter oxymeter) {
                        Log.i(TAG, "finished processing all frames");
                        finishWithOxymeter(oxymeter);
                    }

                    @Override
                    public void onFingerRemoved() {
                        fingerRemoved();
                    }

                    @Override
                    public void onInvalidData() {
                        Log.w(TAG, "Invalid measurement");
                        measurementFailed();
                    }

                    @Override
                    public void onStartWithNewOxymeter() {
                        runOnUiThread(() -> {
                            showProgressBarAndShowAlert(getString(R.string.things_look_ok));
                        });
                    }
                });
        Log.i(TAG, "starting oxymeter.");
        oxymeterUpdater.start();
    }

    public void finishWithOxymeter(Oxymeter oxymeter) {
        OxymeterData result = oxymeter.finish(previewFps / 1000D);
        if (result != null) {
            Log.i(TAG, "Oxymeter finished successfully!");
            Intent returnIntent = new Intent()
                    .putExtra(EXTRA_OXYMETER_DATA, result);

//            returnIntent.putExtra("OXYGEN_SATURATION", result.getOxSaturation());
//            returnIntent.putExtra("BEATS_PER_MINUTE", result.getHeartRate());
//            returnIntent.putExtra("BREATHS_PER_MINUTE",result.getBreathRate());

            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            Log.w(TAG, "Oxymeter returned null");
            measurementFailed();
        }
    }

    public void fingerRemoved() {
        Log.w(TAG, "Finger not recognised!");
        runOnUiThread(() -> removeProgressBarAndShowAlert(getString(R.string.please_put_your_finger_on_camera)));
    }

    public void measurementFailed() {
        runOnUiThread(() -> {
            removeProgressBarAndShowAlert(getString(R.string.measurement_failed));
            readyBtn.setClickable(true);
        });
    }

    public void updateView(int heartRate) {
        currentHeartRate = heartRate;
        runOnUiThread(this::updateMeasurements);
    }

    private void updateMeasurements() {
        heartRate.setText(Integer.toString(currentHeartRate));
    }


    private void updateGraphView(int frame, double point) {
        runOnUiThread(() -> this.updateGraph(frame, point));
    }

    private void updateGraph(int frame, double point) {
        mSeries.appendData(new DataPoint(frame, point), true, DATA_POINTS, false);
        graphHeartRate.onDataChanged(false, false);
    }


    public void setProgress(int currentFrame, int totalFrames) {
        progressBarView.setMax(totalFrames);
        progressBarView.setSecondaryProgress(totalFrames);
        progressBarView.setProgress(currentFrame);

        double secondsLeft = (totalFrames - currentFrame) / ((double) previewFps / 1000);
        timeLeftView.setText(String.format("%s", (int) secondsLeft));
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
        oxymeterUpdater.interrupt();
    }

    private void removeProgressBarAndShowAlert(String alertText) {
        tickImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning));
        putFingerAlert.setText(alertText);
        progressBarView.clearAnimation();
        progressBarView.setVisibility(INVISIBLE);
        timeLeftView.setVisibility(INVISIBLE);
        mSeries.resetData(new DataPoint[]{});
        heartRate.setText("-");
    }

    private void showProgressBarAndShowAlert(String alertText) {
        tickImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_tick));
        putFingerAlert.setText(alertText);
        if (progressBarView.getVisibility() != VISIBLE) {
            progressBarView.startAnimation(makeVertical);
            progressBarView.setVisibility(VISIBLE);
            timeLeftView.setVisibility(VISIBLE);
        }
    }
}

