package com.example.coronadiagnosticapp.ui.activities.oxymeter;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.util.Log;

import com.example.coronadiagnosticapp.ui.activities.ImageProcessing;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft2;
import com.example.coronadiagnosticapp.ui.activities.OxymeterActivity;
import com.example.coronadiagnosticapp.ui.activities.SMA;
import com.opencsv.CSVWriter;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class OxymeterImpl implements Oxymeter {
    public enum RGB {RED, GREEN, BLUE}

    private static final String TAG = "Oxymeter";
    private int counter = 0;
    private static long startTime;
    private double sumred = 0;
    private double sumblue = 0;
    private double peakBpm = 0;

    // window sample consts
    private final int WINDOW_TIME = 10; // the number of seconds for each window
    public final int FAILED_WINDOWS_MAX = 5; // the number of bad windows we allow to "throw away"

    //Arraylist
    private ArrayList<Double> RedAvgList = new ArrayList<>();
    private ArrayList<Double> BlueAvgList = new ArrayList<>();
    private ArrayList<Double> GreenAvgList = new ArrayList<>();

    //Initialize an object that calculates the rolling average of last 15 samples
    private SMA calc_mov_avg = new SMA(15);

    private String[] HEADER = new String[]{"id", "RED_VALUE", "RED_AVG_VALUE"};
    private List<String[]> rows = new ArrayList<String[]>() {{
        add(HEADER);
    }}; // Initializing List of string array's to generate CSV file, adding the HEADER which will be the fields inside the CSV
    private Function0<Unit> onBadFinger;

    //CSV Writing
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private String fileName = "AnalysisData.csv";
    private String filePath = baseDir + File.separator + fileName;

    // Sensor info
    private int width;
    private int height;
    private int bayer;

    @Inject
    public OxymeterImpl(int rawWidth, int rawHeight, int bayer) {
        this.width = rawWidth;
        this.height = rawHeight;
        this.bayer = bayer;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void updateWithFrame(@NotNull Double[] data) {
        double redAvg = data[0];
        double greenAvg = data[1];
        double blueAvg = data[2];

        /*Camera.Size size = cam.getParameters().getPreviewSize();
        if (size == null) throw new NullPointerException();
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

        //TODO:Add this back later
        //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
        if (checkImageIsBad(RedAvg)) {
            badFinger();
            return;
        }*/

        ++counter; //countes number of frames in 30 seconds
        RedAvgList.add(redAvg);
        GreenAvgList.add(greenAvg);
        BlueAvgList.add(blueAvg);
        long endTime = System.currentTimeMillis(); // Set an endTime each frame to check exactly when the timer reach 30 secondes
        double totalTimeInSecs = (endTime - startTime) / 1000d; //to convert time to seconds
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

    private double getColorIntensities(byte[] data, int height, int width, RGB choice) {
        if (choice == RGB.RED)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, width, height, 1); //1 stands for red intensity, 2 for blue, 3 for green
        if (choice == RGB.BLUE)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, width, height, 2); //1 stands for red intensity, 2 for blue, 3 for green
        if (choice == RGB.GREEN)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, width, height, 3); //1 stands for red intensity, 2 for blue, 3 for green
        return 0;
    }

    private boolean checkImageIsBad(double redIntensity) {
        //Image is bad!
        return redIntensity < 200;
    }

    private void writeCSV(List<String[]> data) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath));
            writer.writeAll(data);
            writer.close();
        } catch (IOException ex) {
            Log.e(TAG, "FileWriter Exception: " + ex);
        }
    }


    @Override
    public OxymeterData finish(double totalTimeInSecs) {
        // Round averages and log them.
//        for (int i = 0; i < RedAvgList.size(); i++) {
//            RedAvgList.set(i, new Double(Math.round(RedAvgList.get(i) * 100)) / 100);
//            GreenAvgList.set(i, new Double(Math.round(GreenAvgList.get(i) * 100)) / 100);
//            BlueAvgList.set(i, new Double(Math.round(BlueAvgList.get(i) * 100)) / 100);
//        }
//        Log.e(TAG, "red = " + RedAvgList);
//        Log.e(TAG, "green = " + GreenAvgList);
//        Log.e(TAG, "blue = " + BlueAvgList);

        double samplingFreq = (counter / totalTimeInSecs);
        int[] peekBpmAndO2 = calculateByWindowsBpmAndO2(RedAvgList, BlueAvgList, samplingFreq);
        int o2 = peekBpmAndO2[0];
        int peakBpm = peekBpmAndO2[1];
        int failed = peekBpmAndO2[2];
        if (failed == 1) {
            return null;
        }

        double[] breathAndBPM = calculateAverageFourierBreathAndBPM(RedAvgList, GreenAvgList, samplingFreq);
        int Breath = (int) breathAndBPM[0]; // 0 stands for breath respiration value
        double Beats = breathAndBPM[1]; // 1 stands for Heart Rate value
        // Calculate final result
        if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
            double BpmAvg = ceil((Beats + peakBpm) / 2);
            return new OxymeterData(o2, (int) BpmAvg, Breath);
        } else if (!(o2 < 80 || o2 > 99) && (Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
            return new OxymeterData(o2, peakBpm, Breath);
        } else if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && (peakBpm < 45 || peakBpm > 200)) {
            return new OxymeterData(o2, (int) Beats, Breath);
        } else {
            return null;
        }
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
        int peakBpmi = (int) peakBpm, o2i = (int) o2;
        // Calculate final result
        if (!(o2i < 80 || o2i > 99) && !(peakBpmi < 45 || peakBpmi > 200)) { // if any of the measurements is bad, the windows is bad sample
            return new double[]{o2, peakBpm, 0};
        }
        return new double[]{0, 0, 1};
    }

    private double sumDouble(List<Double> list) {
        double sum = 0;
        for (Double d : list)
            sum += d;
        return sum;
    }


    private void badFinger() {
        // Invokes the onBadFinger callback
        if (onBadFinger != null)
            onBadFinger.invoke();
    }

    @Override
    public void setOnBadFinger(@NotNull Function0<Unit> callback) {
        onBadFinger = callback;
    }
}
