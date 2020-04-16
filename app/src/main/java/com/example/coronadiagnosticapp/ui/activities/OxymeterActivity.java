package com.example.coronadiagnosticapp.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft2;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class OxymeterActivity extends Activity {
    public enum RGB {RED, GREEN, BLUE}

    // Variables Initialization
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static final int MIN_LIGHT_VALUE = 170; // lux units.
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    //Freq + timer variable
    private static long startTime = 0;
    //Arraylist
    public ArrayList<Double> RedAvgList = new ArrayList<Double>();
    public ArrayList<Double> BlueAvgList = new ArrayList<Double>();
    public ArrayList<Double> GreenAvgList = new ArrayList<Double>();
    public int counter = 0;
    //ProgressBar
    ProgressBar progressBarView;
    TextView tv_time;
    int progress;
    CountDownTimer countDownTimer;
    int endTime = 250;
    RotateAnimation makeVertical;
    private SurfaceView preview = null;
    //Button
    private Button readyBtn;
    //TextView
    private TextView put_finger_alert;
    private TextView improve_lightning_alert;
    private double SamplingFreq;
    //getting frames data from the camera and start the heartbeat process
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            //if data or size == null ****
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();
            if (startTime == 0) // Don't count if not started yet!
                processing.set(false);
            else
                processing.set(true);

            //Atomically sets the value to the given updated value if the current value == the expected value.
            if (processing.compareAndSet(false, true)) return;

            //put width + height of the camera inside the variables
            int width = size.width;
            int height = size.height;
            // Get color intensity
            double RedAvg = getColorIntensities(data.clone(), height, width, RGB.RED);
            double BlueAvg = getColorIntensities(data.clone(), height, width, RGB.BLUE);
            double GreenAvg = getColorIntensities(data.clone(), height, width, RGB.GREEN);

            RedAvgList.add(RedAvg);
            BlueAvgList.add(BlueAvg);
            GreenAvgList.add(GreenAvg);

            //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
            if (checkImageIsBad(RedAvg)) {
                resetProcessing();
                removeProgressBarAndShowAlert();
                processing.set(false);
                return;
            } else {
                showProgressBarAndHideAlert();
            }

            ++counter; //countes number of frames in 30 seconds

            long endTime = System.currentTimeMillis(); // Set an endTime each frame to check exactly when the timer reach 30 secondes
            double totalTimeInSecs = (endTime - startTime) / 1000d; //to convert time to seconds
            if (totalTimeInSecs >= 30 && startTime != 0) { //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
                SamplingFreq = (counter / totalTimeInSecs);
                ArrayList<Double> RedMoveAverage = calculateMovingAverage(RedAvgList);
                ArrayList<Integer> peaksList = createWindowsToFindPeaks(RedMoveAverage, RedAvgList);
                double peakBpm = findIntervalsAndCalculateBPM(peaksList);
                int o2 = (int) calculateSPO2(RedAvgList, BlueAvgList);
                double[] breathAndBPM = calculateAverageFourierBreathAndBPM(RedAvgList, GreenAvgList);
                int Breath = (int) breathAndBPM[0]; // 0 stands for breath respiration value
                int Beats = (int) breathAndBPM[1]; // 1 stands for Heart Rate value
                if ((Breath == 0) || (Beats == 0)) {
                    failedProcessing();
                }
                // Calculate final result
                if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
                    int BpmAvg = (int) ceil((Beats + peakBpm) / 2);
                    setMetricsResult(o2, BpmAvg, Breath);
                    finish();
                } else if (!(o2 < 80 || o2 > 99) && (Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
                    setMetricsResult(o2, (int) peakBpm, Breath);
                    finish();
                } else if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && (peakBpm < 45 || peakBpm > 200)) {
                    setMetricsResult(o2, Beats, Breath);
                    finish();
                } else {
                    failedProcessing();
                    return;
                }
            }
            processing.set(true);
        }

        public double[] calculateAverageFourierBreathAndBPM(ArrayList<Double> RedList, ArrayList<Double> GreenList) {
            double bufferAvgBr = 0;
            double bufferAvgB = 0;
            Double[] Red = RedList.toArray(new Double[RedList.size()]);
            Double[] Green = GreenList.toArray(new Double[GreenList.size()]);
            double HRFreq = Fft.FFT(Green, counter, SamplingFreq);
            double bpmGreen = (int) ceil(HRFreq * 60);
            double HR1Freq = Fft.FFT(Red, counter, SamplingFreq);
            double bpmRed = (int) ceil(HR1Freq * 60);

            double RRFreq = Fft2.FFT(Green, counter, SamplingFreq);
            double breathGreen = (int) ceil(RRFreq * 60);
            double RR1Freq = Fft2.FFT(Red, counter, SamplingFreq);
            double breathRed = (int) ceil(RR1Freq * 60);
            if ((bpmGreen > 40 && bpmGreen < 200) || (breathGreen > 6 && breathGreen < 20)) {
                if ((bpmRed > 40 && bpmRed < 200) || (breathRed > 6 && breathRed < 24)) {
                    bufferAvgB = (bpmGreen + bpmRed) / 2;
                    bufferAvgBr = (breathGreen + breathRed) / 2;
                } else {
                    bufferAvgB = bpmGreen;
                    bufferAvgBr = breathGreen;
                }
            } else if ((bpmRed > 45 && bpmRed < 200) || (breathRed > 10 && breathRed < 20)) {
                bufferAvgB = bpmRed;
                bufferAvgBr = breathRed;
            }
            return new double[]{bufferAvgBr, bufferAvgB};
        }

        public double calculateSPO2(ArrayList<Double> red, ArrayList<Double> blue) {
            double Stdr = 0;
            double Stdb = 0;
            double meanr = sumDouble(red) / counter;
            double meanb = sumDouble(blue) / counter;
            for (int i = 0; i < counter - 1; i++) {
                Double bufferb = blue.get(i);
                Stdb = Stdb + ((bufferb - meanb) * (bufferb - meanb));
                Double bufferr = red.get(i);
                Stdr = Stdr + ((bufferr - meanr) * (bufferr - meanr));
            }
            double varr = sqrt(Stdr / (counter - 1));
            double varb = sqrt(Stdb / (counter - 1));

            double R = (varr / meanr) / (varb / meanb);

            double spo2 = 100 - 5 * (R);

            return spo2;
        }

        public ArrayList<Double> calculateMovingAverage(ArrayList<Double> list) {
            //Initialize an object that calculates the rolling average of last 15 samples
            SMA calc_mov_avg = new SMA(15);
            ArrayList<Double> MovAvgRed = new ArrayList<Double>();
            double avg_hr = (sumDouble(list)) / (list.size());
            for (int i = 0; i < list.size(); i++) {
                if (i < 15) {                                   //Assign the average red received to the first 15 samples
                    calc_mov_avg.compute(avg_hr);               //Add the value to the moving average object
                    MovAvgRed.add(i, avg_hr);                   //Add the value to the MobAvgRed list
                } else {
                    MovAvgRed.add(calc_mov_avg.compute(list.get(i)));
                    Log.e(TAG, "Current Average = " + calc_mov_avg.currentAverage());
                }
            }
            return MovAvgRed;
        }

        private void setMetricsResult(int o2, int beats, int breath) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("OXYGEN_SATURATION", Integer.toString(o2));
            returnIntent.putExtra("BEATS_PER_MINUTE", Integer.toString(beats));
            returnIntent.putExtra("BREATHS_PER_MINUTE", Integer.toString(breath));
            setResult(Activity.RESULT_OK, returnIntent);

        }

        private double sumDouble(ArrayList<Double> list) {
            double sum = 0;
            for (Double d : list)
                sum += d;
            return sum;
        }

        private ArrayList<Integer> createWindowsToFindPeaks(ArrayList<Double> MovAvgRed, ArrayList<Double> list) {
            //peakPositionList represents a list which containts the peak positions
            ArrayList<Integer> peakPositionList = new ArrayList<Integer>();
            //Create window which start's when RedAvg > MovAvg and ends when RedAvg < MovAvg and calculate's the highest point (peak) within the window
            ArrayList<Double> window = new ArrayList<Double>();
            int windowIndex = 0;
            for (int i = 0; i < list.size(); i++) {
                if (MovAvgRed.get(i) > list.get(i) && window.isEmpty())
                    continue;
                else if (list.get(i) > MovAvgRed.get(i)) {
                    window.add(windowIndex, list.get(i));
                    windowIndex++;
                } else {
                    if (window.isEmpty())
                        continue;
                    //Finding the maximum value within the window calculating it's position then storing it at peakPositionList variable
                    Object maximum = Collections.max(window);
                    int beatposition = i - window.size() + window.indexOf(maximum);
                    peakPositionList.add(beatposition);
                    window.clear();
                    windowIndex = 0;
                }
            }
            return peakPositionList; //Return list of peak positions
        }

        private double findIntervalsAndCalculateBPM(ArrayList<Integer> peakPositionList) {
            //Calculating the intervals or distance between the peaks, between then storing the result in milliseconds into RR_List ArrayList
            ArrayList<Double> RR_List = new ArrayList<Double>();
            for (int i = 0; i < peakPositionList.size() - 1; i++) {
                int RR_interval = peakPositionList.get(i + 1) - peakPositionList.get(i);
                double ms_dist = ((RR_interval / SamplingFreq) * 1000d);
                RR_List.add(ms_dist);
            }
            //Calculating the average interval
            double sumRR_List = 0;
            for (int i = 0; i < RR_List.size(); i++) {
                sumRR_List += RR_List.get(i);
            }
            double avgRR_List = (sumRR_List) / RR_List.size();
            //Providing result
            return 60000 / (avgRR_List);
        }
    };
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // XML - Java Connecting
        preview = (SurfaceView) findViewById(R.id.preview);
        put_finger_alert = (TextView) findViewById(R.id.putfinger);
        improve_lightning_alert = (TextView) findViewById(R.id.improve_lightning);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        readyBtn = (Button) findViewById(R.id.ready_btn);
        progressBarView = (ProgressBar) findViewById(R.id.barTimer);
        tv_time = (TextView) findViewById(R.id.textTimer);

        /*Animation*/
        makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(0);

        readyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fn_countdown();
                startTime = System.currentTimeMillis();
                Log.e(TAG, "Time has started = " + Long.toString(startTime));
            }
        });

        SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor != null){
            mySensorManager.registerListener(
                    lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);

        } else {
            Log.e(TAG, "Couldn't find light sensor.");
        }
    }


    private final SensorEventListener lightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                Log.d(TAG, "light sensor value:" + event.values[0]);
                if(event.values[0] < MIN_LIGHT_VALUE) {
                    improve_lightning_alert.setVisibility(View.VISIBLE);
                }
                else
                    improve_lightning_alert.setVisibility(View.INVISIBLE);
            }
        }

    };

    //Prevent the system from restarting your activity during certain configuration changes,
    // but receive a callback when the configurations do change, so that you can manually update your activity as necessary.
    //such as screen orientation, keyboard availability, and language

    private void fn_countdown() {

        try {
            countDownTimer.cancel();

        } catch (Exception e) {

        }

        progress = 1;
        endTime = 30;

        countDownTimer = new CountDownTimer(endTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setProgress(progress, endTime);
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
                progress = 0;
                setProgress(progress, 30);
            }
        };
        countDownTimer.start();


    }

    public void setProgress(int startTime, int endTime) {
        progressBarView.setMax(endTime);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(startTime);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
    }

    private void failedProcessing() {
        Toast.makeText(getApplicationContext(), R.string.measurement_failed, Toast.LENGTH_LONG).show();
        resetProcessing();
        startTime = 0; //Re-assign startTime to 0, because the resetProcessing() sets it to current time and we want to stop the process until user press the start button.
        processing.set(false);
        removeProgressBar();
    }

    private void resetProcessing() {
        RedAvgList.clear();
        BlueAvgList.clear();
        counter = 0;
        setProgress(0, 30);
        progress = 0;
        fn_countdown();
        startTime = System.currentTimeMillis();
    }

    private void removeProgressBar() {
        if (progressBarView.getVisibility() == View.VISIBLE) {
            progressBarView.clearAnimation();
            progressBarView.setVisibility(View.GONE);
        }
    }

    private void removeProgressBarAndShowAlert() {
        put_finger_alert.setVisibility(View.VISIBLE); // Make alert "no finger" - visible
        removeProgressBar();
    }

    private void showProgressBarAndHideAlert() {
        put_finger_alert.setVisibility(View.INVISIBLE);
        if (!(progressBarView.getVisibility() == View.VISIBLE)) {
            progressBarView.startAnimation(makeVertical);
            progressBarView.setVisibility(View.VISIBLE);
        }
        progressBarView.setVisibility(View.VISIBLE);
    }

    public boolean checkImageIsBad(double redIntensity) {
        if (redIntensity < 200) //Image is bad!
            return true;
        return false;
    }

    private double getColorIntensities(byte[] data, int height, int width, RGB choice) {
        //1 stands for red intensity, 2 for blue, 3 for green
        if (choice == RGB.RED)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, height, width, 1);
        if (choice == RGB.BLUE)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, height, width, 2);
        if (choice == RGB.GREEN)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, height, width, 3);
        return 0;
    }

}

