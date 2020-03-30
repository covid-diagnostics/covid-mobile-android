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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft2;
import com.opencsv.CSVWriter;

import retrofit2.http.HEAD;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class OxymeterActivity extends Activity {

    // Variables Initialization
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;

    //Initialize an object that calculates the rolling average of last 15 samples
    private SMA calc_mov_avg = new SMA(15);

    //Button
    private Button readyBtn;

    //TextView
    private TextView alert;

    //ProgressBar
    ProgressBar progressBarView;
    TextView tv_time;
    int progress;
    CountDownTimer countDownTimer;
    int endTime = 250;

    //Freq + timer variable
    private static long startTime = 0;
    private double SamplingFreq;

    // SPO2 variables
    private static double RedBlueRatio = 0;
    double Stdr = 0;
    double Stdb = 0;
    double sumred = 0;
    double sumblue = 0;
    double sumgreen = 0;
    public int o2;

    RotateAnimation makeVertical;

    //RespirationRate variable
    public int Breath = 0;
    public double bufferAvgBr = 0;

    // Heart Rate vaiables
    public double bufferAvgB = 0;
    public int Beats = 0;
    public double peakBpm = 0;

    //Arraylist
    public ArrayList<Double> RedAvgList = new ArrayList<Double>();
    public ArrayList<Double> BlueAvgList = new ArrayList<Double>();
    public ArrayList<Double> GreenAvgList = new ArrayList<Double>();
    public ArrayList<Double> MovAvgRed = new ArrayList<Double>();
    public int counter = 0;

    //CSV Writing
    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    String fileName = "AnalysisData.csv";
    String[] HEADER = new String[]{"id","RED_VALUE","RED_AVG_VALUE"};
    String filePath = baseDir + File.separator + fileName;
    File f = new File(filePath);
    CSVWriter writer;



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
        tv_time= (TextView)findViewById(R.id.textTimer);



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
                Log.e(TAG,"Time has started = " + Long.toString(startTime));
            }
        });


    }
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
                Log.e(TAG,"ticking progress = " + progress);
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                String newtime =  seconds + " seconds";

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

    //Prevent the system from restarting your activity during certain configuration changes,
    // but receive a callback when the configurations do change, so that you can manually update your activity as necessary.
    //such as screen orientation, keyboard availability, and language

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

        Log.e("OnResume():", "Called.");
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
            if (processing.compareAndSet(false, true)) {
                return;
            }

            //put width + height of the camera inside the variables
            int width = size.width;
            int height = size.height;
            double RedAvg;
            double BlueAvg;
            double GreenAvg;

            RedAvg = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 1); //1 stands for red intensity, 2 for blue, 3 for green
            sumred = sumred + RedAvg;
            BlueAvg = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 2); //1 stands for red intensity, 2 for blue, 3 for green
            sumblue = sumblue + BlueAvg;
            GreenAvg = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 3);
            sumgreen = sumgreen + GreenAvg;

            RedAvgList.add(RedAvg);
            BlueAvgList.add(BlueAvg);
            GreenAvgList.add(GreenAvg);

            //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
            if (RedAvg < 200) {
                alert.setVisibility(View.VISIBLE); // Make alert no finger - visible
                sumred = 0;
                sumblue = 0;
                RedAvgList.clear();
                BlueAvgList.clear();
                counter =0;
                processing.set(false);
                countDownTimer.cancel();
                setProgress(0,30);
                progress=0;
                fn_countdown();
                if(progressBarView.getVisibility() == View.VISIBLE) {
                    progressBarView.clearAnimation();
                    progressBarView.setVisibility(View.GONE);
                }
                startTime = System.currentTimeMillis();
                return;
            } else {
                alert.setVisibility(View.INVISIBLE);
                if(!(progressBarView.getVisibility() == View.VISIBLE)) {
                    progressBarView.startAnimation(makeVertical);
                    progressBarView.setVisibility(View.VISIBLE);
                }

                progressBarView.setVisibility(View.VISIBLE);
            }
            if(counter == 0)
                Log.e(TAG,"First count of counter, time =" + Long.toString(startTime));



            ++counter; //countes number of frames in 30 seconds
            Log.e(TAG,"Counter is at = " + counter);

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d; //to convert time to seconds
            if (totalTimeInSecs >= 30 && startTime != 0) { //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
                Log.e(TAG,"30 secondes passed ! totalTimeInSecs is now = " + Double.toString(totalTimeInSecs));
                Log.e(TAG,"The sampling frequent is = " + Double.toString(counter/totalTimeInSecs));
                double avg_hr = (sumred) / (RedAvgList.size());
                List<String[]> rows = new ArrayList<String[]>(); // Initializing List of string array's to generate CSV file
                rows.add(HEADER); // Adding Header to the CSV file
                Log.e(TAG,"The average red is = " + avg_hr);
                for (int i = 0; i<RedAvgList.size(); i++)
                {
                    if(i<15) {                                   //Assign the average red received to the first 15 samples
                        calc_mov_avg.compute(avg_hr);           //Add the value to the moving average object
                        //  r.add(avg_hr);
                        MovAvgRed.add(i, avg_hr);               //Add the value to the MobAvgRed list
                        rows.add(new String[]{Integer.toString(i),RedAvgList.get(i).toString(),Double.toString(avg_hr)});       ///Add to CSV
                    }else{
                        // r.add(RedAvgList.get(i));
                        MovAvgRed.add(calc_mov_avg.compute(RedAvgList.get(i)));
                        Log.e(TAG,"Current Average = " + calc_mov_avg.currentAverage());
                        rows.add(new String[]{Integer.toString(i),RedAvgList.get(i).toString(),Double.toString(calc_mov_avg.currentAverage())});
                    }

                }
//--------------------------CSV Writing-------------------------------
                try {
                    writer = new CSVWriter(new FileWriter(filePath));
                    writer.writeAll(rows);
                    writer.close();
                } catch (IOException ex) {
                    Log.e(TAG, "FileWriter Exception" + ex);
                }
//--------------------------------------------------------------------

                //Create window which start's when RedAvg > MovAvg and ends when RedAvg < MovAvg and calculate's the highest point (peak) within the window
                ArrayList<Double> window = new ArrayList<Double>();
                ArrayList<Integer> peakPositionList = new ArrayList<Integer>();
                int windowIndex = 0;
                for(int i=0;i<RedAvgList.size();i++)
                {
                    if(MovAvgRed.get(i) > RedAvgList.get(i) && window.isEmpty())
                        continue;
                    else
                    if(RedAvgList.get(i) > MovAvgRed.get(i)) {
                        window.add(windowIndex, RedAvgList.get(i));
                        windowIndex++;
                    }
                    else{
                        if(window.isEmpty())
                            continue;
                        //Finding the maximum value within the window calculating it's position then storing it at peakPositionList variable
                        Object maximum = Collections.max(window);
                        int beatposition = i - window.size() + window.indexOf(maximum);
                        peakPositionList.add(beatposition);
                        window.clear();
                        windowIndex = 0;
                    }
                }
                //Calculating the intervals or distance between the peaks, between then storing the result in milliseconds into RR_List ArrayList
                ArrayList<Double> RR_List = new ArrayList<Double>();
                for(int i = 0 ;i<peakPositionList.size() - 1;i++)
                {
                    int RR_interval = peakPositionList.get(i+1) - peakPositionList.get(i);
                    double ms_dist = ((RR_interval/(counter / totalTimeInSecs)) * 1000d);
                    RR_List.add(ms_dist);
                }
                //Calculating the average interval
                double sumRR_List = 0;
                for ( int i=0; i < RR_List.size() ; i++) {
                    sumRR_List += RR_List.get(i);
                }
                double avgRR_List = (sumRR_List)/RR_List.size();

                //Providing result
                peakBpm = 60000 / (avgRR_List);
                SamplingFreq = (counter / totalTimeInSecs);
                Double[] Red = RedAvgList.toArray(new Double[RedAvgList.size()]);
                Double[] Blue = BlueAvgList.toArray(new Double[BlueAvgList.size()]);
                Double[] Green = GreenAvgList.toArray(new Double[GreenAvgList.size()]);

                // double HRFreq = Fft.FFT(Red, counter, SamplingFreq);
                // double bpm = (int) ceil(HRFreq * 60);
                double HRFreq = Fft.FFT(Green, counter, SamplingFreq);
                double bpmGreen = (int) ceil(HRFreq * 60);
                double HR1Freq = Fft.FFT(Red, counter, SamplingFreq);
                double bpmRed = (int) ceil(HR1Freq * 60);

                double RRFreq = Fft2.FFT(Green, counter, SamplingFreq);
                double breathGreen = (int) ceil(RRFreq * 60);
                double RR1Freq = Fft2.FFT(Red, counter, SamplingFreq);
                double breathRed = (int) ceil(RR1Freq * 60);

                double meanr = sumred / counter;
                double meanb = sumblue / counter;

                for (int i = 0; i < counter - 1; i++) {

                    Double bufferb = Blue[i];

                    Stdb = Stdb + ((bufferb - meanb) * (bufferb - meanb));

                    Double bufferr = Red[i];

                    Stdr = Stdr + ((bufferr - meanr) * (bufferr - meanr));

                }


                if ((bpmGreen > 45 || bpmGreen < 200) || (breathGreen > 10 || breathGreen < 20)) {
                    if ((bpmRed > 45 || bpmRed < 200) || (breathRed > 10 || breathRed < 24)) {

                        bufferAvgB = (bpmGreen + bpmRed) / 2;
                        bufferAvgBr = (breathGreen + breathRed) / 2;

                    } else {
                        bufferAvgB = bpmGreen;
                        bufferAvgBr = breathGreen;
                    }
                } else if ((bpmRed > 45 || bpmRed < 200) || (breathRed > 10 || breathRed < 20)) {

                    bufferAvgB = bpmRed;
                    bufferAvgBr = breathRed;

                }


                double varr = sqrt(Stdr / (counter - 1));
                double varb = sqrt(Stdb / (counter - 1));

                double R = (varr / meanr) / (varb / meanb);

                double spo2 = 100 - 5 * (R);
                o2 = (int) (spo2);
                Log.e(TAG,"Value testBpm = " + Double.toString(peakBpm));
                Log.e("O2 Value = ", "" + Integer.toString(o2));

//-----------------Measurement failed, doing start-over by setting counter to 0 and setting startTime to current---------------------------------------------//
                if ((o2 < 80 || o2 > 99) || ((bufferAvgB < 45 || bufferAvgB > 200) && (peakBpm <45 || peakBpm >200))) {
                    Log.e("O2 Value before Toast = ", "" + Integer.toString(o2));
                    Toast.makeText(getApplicationContext(), "Measurement Failed, Please start the button again when you are ready !", Toast.LENGTH_SHORT).show();
                    sumred = 0;
                    sumblue = 0;
                    RedAvgList.clear();
                    BlueAvgList.clear();
                    counter = 0;
                    o2 = 0;
                    processing.set(false);
                    countDownTimer.cancel();
                    progress=0;
                    setProgress(0,30);
                    startTime = 0;
                    return;
                }
                Beats = (int) bufferAvgB;
                Breath = (int) bufferAvgBr;


                camera.setPreviewCallback(null);
                camera.stopPreview();
            }


            if ((Beats != 0)  && (o2 != 0) && (Breath != 0 ) && (peakBpm != 0)) {
                Intent returnIntent = new Intent();
                if (!(o2 < 80 || o2 > 99) && !(Beats <45 || Beats > 200) && !(peakBpm<45 || peakBpm>200)) {
                    int BpmAvg = (int)ceil((Beats + peakBpm) /2);
                    //TODO Need to pass Bats o2 and Breath
                    returnIntent.putExtra("OXYGEN_SATURATION", Integer.toString(o2));
                    returnIntent.putExtra("BEATS_PER_MINUTE",Integer.toString(BpmAvg));
                    returnIntent.putExtra("BREATHS_PER_MINUTE",Integer.toString(Breath));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else if(!(o2 < 80 || o2 > 99) && (Beats <45 || Beats > 200) && !(peakBpm<45 || peakBpm>200))
                {
                    returnIntent.putExtra("OXYGEN_SATURATION", Integer.toString(o2));
                    returnIntent.putExtra("BEATS_PER_MINUTE",Integer.toString((int)peakBpm));
                    returnIntent.putExtra("BREATHS_PER_MINUTE",Integer.toString(Breath));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else if(!(o2 < 80 || o2 > 99) && !(Beats <45 || Beats > 200) && (peakBpm<45 || peakBpm>200)){
                    returnIntent.putExtra("OXYGEN_SATURATION", Integer.toString(o2));
                    returnIntent.putExtra("BEATS_PER_MINUTE",Integer.toString(Beats));
                    returnIntent.putExtra("BREATHS_PER_MINUTE",Integer.toString(Breath));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"Please do the test again!",Toast.LENGTH_SHORT).show();
                }


            }

            processing.set(true);

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
    public void onBackPressed() {
        super.onBackPressed();
    }
}