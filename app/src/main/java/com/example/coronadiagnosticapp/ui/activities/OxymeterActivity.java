package com.example.coronadiagnosticapp.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.text.TextUtils;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft2;

import org.apache.commons.lang3.ArrayUtils;

import retrofit2.http.HEAD;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class OxymeterActivity extends Activity {
    public enum RGB {RED, GREEN, BLUE}

    // Variables Initialization
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    //Freq + timer variable
    private static long startTime = 0;
    //Arraylist
    public ArrayList<Double> RedAvgList = new ArrayList<Double>();
    public ArrayList<Double> BlueAvgList = new ArrayList<Double>();
    public ArrayList<Double> GreenAvgList = new ArrayList<Double>();
    public int counter = 0;
    // window sample consts
    public final int WINDOW_TIME = 10; // the number of seconds for each window
    public final int FAILED_WINDOWS_MAX = 5; // the number of bad windows we allow to "throw away"
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
    private TextView alert;
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
                double samplingFreq = (counter / totalTimeInSecs);
                int[] peekBpmAndO2 = calculateByWindowsBpmAndO2(RedAvgList, BlueAvgList, samplingFreq);
                int o2 = peekBpmAndO2[0];
                int peakBpm = peekBpmAndO2[1];
                int failed = peekBpmAndO2[2];
                if (failed == 1) {
                    failedProcessing();
                    return;
                }

                double[] breathAndBPM = calculateAverageFourierBreathAndBPM(RedAvgList, GreenAvgList, samplingFreq);
                int Breath = (int) breathAndBPM[0]; // 0 stands for breath respiration value
                double Beats = breathAndBPM[1]; // 1 stands for Heart Rate value
                // Calculate final result
                if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
                    double BpmAvg = ceil((Beats + peakBpm) / 2);
                    setMetricsResult(o2, (int) BpmAvg, Breath);
                    finish();
                } else if (!(o2 < 80 || o2 > 99) && (Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
                    setMetricsResult(o2, peakBpm, Breath);
                    finish();
                } else if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && (peakBpm < 45 || peakBpm > 200)) {
                    setMetricsResult(o2, (int) Beats, Breath);
                    finish();
                } else {
                    failedProcessing();
                    return;
                }
            }
            processing.set(true);
        }

        private int[] calculateByWindowsBpmAndO2(ArrayList<Double> redList, ArrayList<Double> blueList, double samplingFreq) {
            final double WINDOW_FRAMES = samplingFreq * WINDOW_TIME;
            double[] results;
            double[] o2 = new double[30 - WINDOW_TIME + 1];
            double[] peakBpm = new double[30 - WINDOW_TIME + 1];
            int failed = 0;
            for (int i = 0; i <= 30 - WINDOW_TIME; i++) {
                int from = (int) (samplingFreq * i);
                int to = (int) (WINDOW_FRAMES + (int) (samplingFreq * i));
                results = calculateWindowSampleBpmAndO2(
                        new ArrayList(redList.subList(from, to)),
                        new ArrayList(blueList.subList(from, to)),
                        samplingFreq);
                o2[i] = results[0];     // if failed the result is 0
                peakBpm[i] = results[1];// if failed the result is 0
                failed += (int) results[2];
            }
            if (failed > FAILED_WINDOWS_MAX) { // too many failed windows, the samples are bad
                return new int[]{0, 0, 1};
            } else {
                return new int[]{
                        (int) (sumDouble(Arrays.asList(ArrayUtils.toObject(o2))) / (o2.length - failed)),
                        (int) (sumDouble(Arrays.asList(ArrayUtils.toObject(peakBpm))) / (peakBpm.length - failed)),
                        0};
            }
        }

        private double[] calculateWindowSampleBpmAndO2(ArrayList<Double> redList, ArrayList<Double> blueList, double samplingFreq) {
            ArrayList<Double> RedMoveAverage = calculateMovingAverage(redList);
            ArrayList<Integer> peaksList = createWindowsToFindPeaks(RedMoveAverage, redList);
            double peakBpm = findIntervalsAndCalculateBPM(peaksList, samplingFreq);
            double o2 = calculateSPO2(redList, blueList);
            Log.e("rere", "" + o2 + " " + peakBpm);

            int peakBpmi = (int) peakBpm, o2i = (int) o2;
            // Calculate final result
            if (!(o2i < 80 || o2i > 99) && !(peakBpmi < 45 || peakBpmi > 200)) { // if any of the measurements is bad, the windows is bad sample
                return new double[]{o2, peakBpm, 0};
            }
            return new double[]{0, 0, 1};
        }

        public double[] calculateAverageFourierBreathAndBPM(ArrayList<Double> RedList, ArrayList<Double> GreenList, double samplingFreq) {
            double bufferAvgBr = 0;
            double bufferAvgB = 0;
            Double[] Red = RedList.toArray(new Double[RedList.size()]);
            Double[] Green = GreenList.toArray(new Double[GreenList.size()]);
            double HRFreq = Fft.FFT(Green, Green.length, samplingFreq);
            double bpmGreen = (int) ceil(HRFreq * 60);
            double HR1Freq = Fft.FFT(Red, Red.length, samplingFreq);
            double bpmRed = (int) ceil(HR1Freq * 60);

            double RRFreq = Fft2.FFT(Green, Green.length, samplingFreq);
            double breathGreen = (int) ceil(RRFreq * 60);
            double RR1Freq = Fft2.FFT(Red, Red.length, samplingFreq);
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
            double meanr = sumDouble(red) / red.size();
            double meanb = sumDouble(blue) / blue.size();
            for (int i = 0; i < red.size() - 1; i++) {
                Double bufferb = blue.get(i);
                Stdb = Stdb + ((bufferb - meanb) * (bufferb - meanb));
                Double bufferr = red.get(i);
                Stdr = Stdr + ((bufferr - meanr) * (bufferr - meanr));
            }
            double varr = sqrt(Stdr / (red.size() - 1)); // should it really be -1 ?
            double varb = sqrt(Stdb / (red.size() - 1)); // should it really be -1 ?

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

        private double sumDouble(List<Double> list) {
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

        private double findIntervalsAndCalculateBPM(ArrayList<Integer> peakPositionList, double samplingFreq) {
            //Calculating the intervals or distance between the peaks, between then storing the result in milliseconds into RR_List ArrayList
            ArrayList<Double> RR_List = new ArrayList<Double>();
            for (int i = 0; i < peakPositionList.size() - 1; i++) {
                int RR_interval = peakPositionList.get(i + 1) - peakPositionList.get(i);
                double ms_dist = ((RR_interval / samplingFreq) * 1000d);
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
        alert = (TextView) findViewById(R.id.putfinger);
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


    }

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
        alert.setVisibility(View.VISIBLE); // Make alert "no finger" - visible
        removeProgressBar();
    }

    private void showProgressBarAndHideAlert() {
        alert.setVisibility(View.INVISIBLE);
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

